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
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gstreamer.Gst;
import org.opencastproject.inspection.api.MediaInspectionException;
import org.opencastproject.inspection.api.MediaInspectionService;
import org.opencastproject.job.api.AbstractJobProducer;
import org.opencastproject.job.api.Job;
import org.opencastproject.job.api.JobBarrier;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.mediapackage.MediaPackageElementParser;
import org.opencastproject.mediapackage.MediaPackageException;
import org.opencastproject.mediapackage.Track;
import org.opencastproject.mediapackage.identifier.IdBuilder;
import org.opencastproject.mediapackage.identifier.IdBuilderFactory;
import org.opencastproject.security.api.OrganizationDirectoryService;
import org.opencastproject.security.api.SecurityService;
import org.opencastproject.security.api.UserDirectoryService;
import org.opencastproject.serviceregistry.api.ServiceRegistry;
import org.opencastproject.serviceregistry.api.ServiceRegistryException;
import org.opencastproject.smil.entity.MediaElement;
import org.opencastproject.smil.entity.ParallelElement;
import org.opencastproject.smil.entity.Smil;
import org.opencastproject.videoeditor.api.ProcessFailedException;
import org.opencastproject.videoeditor.api.VideoEditorService;
import org.opencastproject.videoeditor.gstreamer.VideoEditorPipeline;
import org.opencastproject.videoeditor.gstreamer.sources.SourceBinsFactory;
import org.opencastproject.workspace.api.Workspace;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wsmirnow
 */
public class VideoEditorServiceImpl extends AbstractJobProducer implements VideoEditorService, ManagedService {

  /**
   * The logging instance
   */
  private static final Logger logger = LoggerFactory.getLogger(VideoEditorServiceImpl.class);
  
  private static final String JOB_TYPE = "org.opencastproject.videoeditor";
  private static final String COLLECTION_ID = "videoeditor";
  private static final String FLAVOR_SUBTYPE_EDITED = "edited";
  
  private static enum Operation {
    PROCESS_SMIL
  }
      
  /** Reference to the media inspection service */
  private MediaInspectionService inspectionService = null;

  /** Reference to the workspace service */
  private Workspace workspace = null;
  
  /** Id builder used to create ids for encoded tracks */
  private final IdBuilder idBuilder = IdBuilderFactory.newInstance().newIdBuilder();

  /** Reference to the receipt service */
  private ServiceRegistry serviceRegistry;

  /** The organization directory service */
  protected OrganizationDirectoryService organizationDirectoryService = null;

  /** The security service */
  protected SecurityService securityService = null;

  /** The user directory service */
  protected UserDirectoryService userDirectoryService = null;
  
  
  
  private Properties properties;
  private String storageDir;

  public VideoEditorServiceImpl() {
    super(JOB_TYPE);
  }

  protected String processSmil(Job job, Smil smil, Track track) throws ProcessFailedException {
    String outputFileExtension = properties.getProperty(VideoEditorProperties.OUTPUT_FILE_EXTENSION, VideoEditorPipeline.DEFAULT_OUTPUT_FILE_EXTENSION);
    if (!outputFileExtension.startsWith("."))
      outputFileExtension = '.' + outputFileExtension;

    // create working directory
    String sourceFlavor = track.getFlavor().getType();
    File outputPath = new File(storageDir + "/" + job.getId(),
            sourceFlavor + "_" + track.getIdentifier() + outputFileExtension);

    if (!outputPath.getParentFile().exists()) {
      outputPath.getParentFile().mkdirs();
    }
    SourceBinsFactory sourceBins = new SourceBinsFactory(outputPath.getAbsolutePath());

    // create source bins
    try {
      for (ParallelElement pe : smil.getBody().getSequence().getElements()) {
        for (MediaElement me : pe.getElements()) {
          if (!track.getIdentifier().equals(me.getMhElement())) {
            continue;
          }

          long begin = me.getClipBeginMS();
          long end = me.getClipEndMS();
          long duration = end - begin;
          String srcFilePath = me.getSrc();

          sourceBins.addFileSource(srcFilePath, begin, duration);
        }
      }

      // create and run editing pipeline
      VideoEditorPipeline runningPipeline = new VideoEditorPipeline(properties);
      runningPipeline.addSourceBinsAndCreatePipeline(sourceBins);
      runningPipeline.run();
      runningPipeline.mainLoop();
      String error = runningPipeline.getLastErrorMessage();

      if (error != null) {
        FileUtils.deleteDirectory(outputPath.getParentFile());
        throw new ProcessFailedException("Editing pipeline exited abnormaly! Error: " + error);
      }
      
      // create Track for edited file
      String newTrackId = idBuilder.createNew().toString();
      InputStream in = new FileInputStream(outputPath);
      URI newTrackURI;
      try {
        newTrackURI = workspace.putInCollection(COLLECTION_ID, sourceFlavor + "-" + newTrackId + outputFileExtension, in);
      } catch (IllegalArgumentException ex) {
        throw new ProcessFailedException("Copy track into workspace failed! " + ex.getMessage());
      } finally {
        IOUtils.closeQuietly(in);
      }
      logger.info("Copied the edited file to workspace at {}.", newTrackURI);
      FileUtils.deleteDirectory(outputPath.getParentFile());
      
      // inspect new Track
      Job inspectionJob = inspectionService.inspect(newTrackURI);
      JobBarrier barrier = new JobBarrier(serviceRegistry, inspectionJob);
      if (!barrier.waitForJobs().isSuccess()) {
        throw new ProcessFailedException("Media inspection of " + newTrackURI + " failed");
      }
      Track editedTrack = (Track) MediaPackageElementParser.getFromXml(inspectionJob.getPayload());
      editedTrack.setIdentifier(newTrackId);
      editedTrack.setFlavor(new MediaPackageElementFlavor(track.getFlavor().getType(), FLAVOR_SUBTYPE_EDITED));
      editedTrack.referTo(track);
            
      return MediaPackageElementParser.getAsXml(editedTrack);

    } catch (MediaInspectionException ex) {
      throw new ProcessFailedException("Inspecting encoded Track failed with: " + ex.getMessage());
//    } catch (FileNotFoundException ex) {
//      throw new ProcessFailedException(ex.getMessage());
//    } catch (PipelineBuildException ex) {
//      throw new ProcessFailedException("Pipeline build error: " + ex.getMessage());
//    } catch (IOException ex) {
//      throw new ProcessFailedException(ex.getMessage());
    } catch (MediaPackageException ex) {
      throw new ProcessFailedException("Unable to serialize edited Track! " + ex.getMessage());
    } catch (Exception ex) {
      throw new ProcessFailedException(ex.getMessage());
    }
  }

  @Override
  public Job processSmil(Smil smil) throws ProcessFailedException {
    if (smil == null) {
      throw new ProcessFailedException("Smil document is null!");
    }

    MediaPackage mp = smil.getMediaPackage();
    List<String> args = new LinkedList<String>();

    try {
      args.add(smil.toXML());
      
      // get all tracks from first sequence of smil document
      for (MediaElement me : smil.getBody().getSequence().getElements().get(0).getElements()) {
        Track track = mp.getTrack(me.getMhElement());
        if (!args.contains(track))
          args.add(MediaPackageElementParser.getAsXml(track));
      }
      
      // create processing job
      return serviceRegistry.createJob(
                getJobType(), 
                Operation.PROCESS_SMIL.toString(), 
                args);

    } catch (MediaPackageException ex) {
      throw new ProcessFailedException("Unable to serailize track!");
    } catch (ServiceRegistryException ex) {
      throw new ProcessFailedException("unable to create job for smil processing! " + ex.getMessage());
    } catch (JAXBException ex) {
      throw new ProcessFailedException("Unable to serialize smil document!");
    }
  }

  @Override
  protected String process(Job job) throws Exception {
    if (Operation.PROCESS_SMIL.toString().equals(job.getOperation())) {
      
      List<String> args = job.getArguments();
      
      Smil smil = Smil.fromXML(args.remove(0));
      if (smil == null) {
        throw new ProcessFailedException("Smil document is null!");
      }
      
      logger.info("Start processing smil {}.", smil.getId());
      List<String> results = new LinkedList<String>();
      for (String trackXml : args) {
        Track track = (Track) MediaPackageElementParser.getFromXml(trackXml);
        logger.info("Editing track '{}' id: {}.", new String[] {
          track.getFlavor().getType(), track.getIdentifier()
        });
        String result = processSmil(job, smil, track);
        logger.info("Track {} edited.", track.getIdentifier());
        results.add(result);
      }
      
      logger.info("Smil {} precessing finished.", smil.getId());
      return Arrays.toString(results.toArray());
    }

    throw new ProcessFailedException("Can't handle this operation: " + job.getOperation());
  }

  @Override
  protected ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }

  @Override
  protected SecurityService getSecurityService() {
    return securityService;
  }

  @Override
  protected UserDirectoryService getUserDirectoryService() {
    return userDirectoryService;
  }

  @Override
  protected OrganizationDirectoryService getOrganizationDirectoryService() {
    return organizationDirectoryService;
  }
  
  protected void activate(ComponentContext context) {
    logger.debug("activating...");
    Gst.setUseDefaultContext(true);
    Gst.init();
    
    storageDir = context.getBundleContext().getProperty("org.opencastproject.storage.dir");
//    String hostURL = context.getBundleContext().getProperty("org.opencastproject.server.url");
  }

  protected void deactivate(ComponentContext context) {
    logger.debug("deactivating...");
  }

  @Override
  public void updated(Dictionary properties) throws ConfigurationException {
    this.properties = new Properties();
    Enumeration keys = properties.keys();
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();
      this.properties.put(key, properties.get(key));
    }
    logger.debug("Properties updated!");
  }
  
  public void setMediaInspectionService(MediaInspectionService inspectionService) {
    this.inspectionService = inspectionService;
  }
  
  public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }
  
  public void setWorkspace(Workspace workspace) {
    this.workspace = workspace;
  }
  
  public void setSecurityService(SecurityService securityService) {
    this.securityService = securityService;
  }
  
  public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
    this.userDirectoryService = userDirectoryService;
  }
  
  public void setOrganizationDirectoryService(OrganizationDirectoryService organizationDirectoryService) {
    this.organizationDirectoryService = organizationDirectoryService;
  }
}
