/**
 *  Copyright 2009, 2010 The Regents of the University of California
 *  Licensed under the Educational Community License, Version 2.0
 *  (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *  http://www.osedu.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */
package org.opencastproject.videoeditor.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.IOUtils;
import org.gstreamer.Gst;
import org.opencastproject.inspection.api.MediaInspectionException;
import org.opencastproject.inspection.api.MediaInspectionService;
import org.opencastproject.job.api.Job;
import org.opencastproject.job.api.JobBarrier;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.mediapackage.MediaPackageElement;
import org.opencastproject.mediapackage.MediaPackageElementParser;
import org.opencastproject.mediapackage.MediaPackageException;
import org.opencastproject.mediapackage.Track;
import org.opencastproject.mediapackage.identifier.IdBuilder;
import org.opencastproject.mediapackage.identifier.IdBuilderFactory;
import org.opencastproject.serviceregistry.api.ServiceRegistry;
import org.opencastproject.smil.entity.MediaElement;
import org.opencastproject.smil.entity.ParallelElement;
import org.opencastproject.smil.entity.Smil;
import org.opencastproject.videoeditor.api.ProcessFailedException;
import org.opencastproject.videoeditor.api.VideoEditor;
import org.opencastproject.videoeditor.gstreamer.VideoEditorPipeline;
import org.opencastproject.videoeditor.gstreamer.exceptions.PipelineBuildException;
import org.opencastproject.videoeditor.gstreamer.exceptions.UnknownSourceTypeException;
import org.opencastproject.videoeditor.gstreamer.sources.SourceBinsFactory;
import org.opencastproject.workspace.api.Workspace;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class VideoEditorService implements VideoEditor, ManagedService {

  /**
   * The logging instance
   */
  private static final Logger logger = LoggerFactory.getLogger(VideoEditorService.class);
  
  public static final String COLLECTION = "editor";
  public static final String STORAGE_DIR_PROPERTY_NAME = "org.opencastproject.storage.dir";
  public static final String TARGET_FLAVOUR = "trimmed";
  
  /** Reference to the receipt service */
  private ServiceRegistry serviceRegistry = null;
  
  /** Reference to the media inspection service */
  private MediaInspectionService inspectionService = null;
  
  /** Reference to the workspace service */
  private Workspace workspace = null;
  
  /** Id builder used to create ids for encoded tracks */
  private final IdBuilder idBuilder = IdBuilderFactory.newInstance().newIdBuilder();
  
  private Properties properties = new Properties();
  
  private String storageDir;
  private boolean serviceRunning = false;
  private List<VideoEditorPipeline> runningPipelines = Collections.synchronizedList(new LinkedList<VideoEditorPipeline>());

  protected void activate(ComponentContext context) {
    Gst.setUseDefaultContext(true);
    Gst.init();
    
    storageDir = context.getBundleContext().getProperty(STORAGE_DIR_PROPERTY_NAME);
    serviceRunning = true;
  }

  protected void deactivate(ComponentContext context) {
    serviceRunning = false;
    for (VideoEditorPipeline pipeline : runningPipelines) {
      pipeline.stop();
    }
  }

  @Override
  public void updated(Dictionary properties) throws ConfigurationException {
    this.properties.clear();
    Enumeration keys = properties.keys();
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();
      this.properties.put(key, properties.get(key));
    }
    logger.debug("Properties updated!");
  }
  
  public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }
  
  public void setInspectionService(MediaInspectionService inspectionService) {
    this.inspectionService = inspectionService;
  }
  
  public void setWorkspace(Workspace workspace) {
    this.workspace = workspace;
  }

  @Override
  public List<Track> process(Smil smil) throws ProcessFailedException {

    if (smil == null || smil.getBody().getSequence().getElements().isEmpty()) {
      throw new ProcessFailedException("Smil document is empty!");
    } else {
      logger.info("processing smil:");
      try {
        logger.info(smil.toXML());
      } catch (JAXBException ex) {
        throw new ProcessFailedException(ex.getMessage());
      }
    }
    
    Map<String, SourceBinsFactory> pipelineSources = createSourceBinsFromSmil(smil);
    List<Track> encodedTracks = new LinkedList<Track>();

    for (SourceBinsFactory fileSourceBin : pipelineSources.values()) {
      if (!serviceRunning) throw new ProcessFailedException("Service unavailable!");
      File encodedFile = runPipeline(fileSourceBin);
      
      // Put the file in the workspace
      URI returnURL = null;
      InputStream in = null;
      String trackId = idBuilder.createNew().toString();
      try {
        in = new FileInputStream(encodedFile);
        returnURL = workspace.putInCollection(COLLECTION, trackId + "_" + encodedFile.getName(), in);
        logger.info("Copied the trimmed file to the workspace at {}", returnURL);
        encodedFile.delete();
        logger.info("Deleted the local copy of the trimmed file at {}", encodedFile.getAbsolutePath());
      } catch (FileNotFoundException e) {
        throw new ProcessFailedException("Encoded file " + encodedFile.getAbsolutePath() + " not found");
      } catch (IOException e) {
        throw new ProcessFailedException("Error putting " + encodedFile.getName() + " into the workspace");
      } catch (IllegalArgumentException ex) {
        throw new ProcessFailedException(ex.getMessage());
      } finally {
        IOUtils.closeQuietly(in);
      }
      if (encodedFile != null)
        encodedFile.delete(); // clean up the encoding output, since the file is now safely stored in the file repo

      // Have the encoded track inspected and return the result
      Job inspectionJob = null;
      try {
        inspectionJob = inspectionService.inspect(returnURL);
        JobBarrier barrier = new JobBarrier(serviceRegistry, inspectionJob);
        if (!barrier.waitForJobs().isSuccess()) {
          throw new ProcessFailedException("Media inspection of " + returnURL + " failed");
        }
        Track inspectedTrack = (Track) MediaPackageElementParser.getFromXml(inspectionJob.getPayload());
        inspectedTrack.setIdentifier(trackId);
        encodedTracks.add(inspectedTrack);
      } catch (MediaPackageException ex) {
        throw new ProcessFailedException(ex.getMessage());
      } catch (MediaInspectionException e) {
        throw new ProcessFailedException("Media inspection of " + returnURL + " failed");
      }
    }

    return encodedTracks;
  }
  
  protected Map<String, SourceBinsFactory> createSourceBinsFromSmil(Smil smil) throws ProcessFailedException {
    Map<String, SourceBinsFactory> pipelineSources = new Hashtable<String, SourceBinsFactory>();
    
    MediaPackage mp = smil.getMediaPackage();
    String suffix = properties.getProperty(VideoEditorProperties.OUTPUT_FILE_SUFFIX, VideoEditorPipeline.DEFAULT_OUTPUT_FILE_SUFFIX);
    String extension = properties.getProperty(VideoEditorProperties.OUTPUT_FILE_EXTENSION, VideoEditorPipeline.DEFAULT_OUTPUT_FILE_EXTENSION);

    for (ParallelElement pe : smil.getBody().getSequence().getElements()) {
      for (MediaElement me : pe.getElements()) {

        long begin = me.getClipBeginMS();
        long end = me.getClipEndMS();
        long duration = end - begin;
        String srcFilePath = me.getSrc();
        
        String mhElementID = me.getMhElement();
        MediaPackageElement t = mp.getElementById(mhElementID);
        if (t == null || t.getFlavor() == null) 
          throw new ProcessFailedException("Source track or flavour is null!");
        
        String sourceFlafour = t.getFlavor().getType();
        File outputDir = new File(storageDir,smil.getId() + '-' + sourceFlafour);
        if (!outputDir.exists()) outputDir.mkdirs();
        
        File srcFile = new File(srcFilePath);
        int index = srcFile.getName().lastIndexOf('.');
        String fileName = srcFile.getName().substring(0, index) + suffix + extension;
        
        String outputFilePath = new File(outputDir, fileName).getAbsolutePath();
        
        SourceBinsFactory sourceBins = pipelineSources.get(outputFilePath);
        if (sourceBins == null) {
          sourceBins = new SourceBinsFactory(outputFilePath);
        }
        
        logger.info("add source file " + new File(srcFilePath).getName() + " to " + new File(outputFilePath).getName() + " with mh elem: " + me.getMhElement());
        
        try {
          sourceBins.addFileSource(srcFilePath, begin, duration);
          sourceBins.setSourceMHElementID(me.getMhElement());
          pipelineSources.put(sourceBins.getOutputFilePath(), sourceBins);
        } catch (UnknownSourceTypeException ex) {
          logger.error(ex.getMessage());
          throw new ProcessFailedException(ex.getMessage());
        } catch (FileNotFoundException ex) {
          logger.error(ex.getMessage());
          throw new ProcessFailedException(ex.getMessage());
        } catch (PipelineBuildException ex) {
          logger.error(ex.getMessage());
          throw new ProcessFailedException(ex.getMessage());
        } catch (Exception ex) {
          throw new ProcessFailedException(ex.getMessage());
        }
      }
    }
    return pipelineSources;
  }
  
  protected File runPipeline(SourceBinsFactory fileSourceBin) throws ProcessFailedException {

      if (!serviceRunning) throw new ProcessFailedException("Service unavailable!");
      
      VideoEditorPipeline runningPipeline = new VideoEditorPipeline(properties);
      try {
        runningPipeline.addSourceBinsAndCreatePipeline(fileSourceBin);
        
        runningPipelines.add(runningPipeline);
        runningPipeline.run();
        runningPipeline.mainLoop();
        
        File encodedFile = new File(fileSourceBin.getOutputFilePath());
        if (!serviceRunning) {
          if (encodedFile.exists()) {
            encodedFile.delete();
          }
          throw new ProcessFailedException("Service unavailable!");
        }
        
        String error = runningPipeline.getLastErrorMessage();
        if (error != null) {
          logger.error("Pipeline exited with error: " + error);
          if (encodedFile.exists()) {
            encodedFile.delete();
          }
          throw new PipelineBuildException();
        }
        
        return encodedFile;
        
      } catch (PipelineBuildException ex) {
        logger.error(ex.getMessage());
        throw new ProcessFailedException(ex.getMessage());
      } finally {
        if (runningPipelines.contains(runningPipeline)) {
          runningPipelines.remove(runningPipeline);
        }
        runningPipeline = null;
      }
  }
}
