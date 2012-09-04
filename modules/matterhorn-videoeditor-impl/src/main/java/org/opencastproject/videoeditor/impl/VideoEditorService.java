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
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.gstreamer.Gst;
import org.opencastproject.smil.entity.MediaElement;
import org.opencastproject.smil.entity.ParallelElement;
import org.opencastproject.smil.entity.Smil;
import org.opencastproject.videoeditor.api.ProcessFailedException;
import org.opencastproject.videoeditor.api.VideoEditor;
import org.opencastproject.videoeditor.gstreamer.VideoEditorPipeline;
import org.opencastproject.videoeditor.gstreamer.exceptions.PipelineBuildException;
import org.opencastproject.videoeditor.gstreamer.exceptions.UnknownSourceTypeException;
import org.opencastproject.videoeditor.gstreamer.sources.SourceBinsFactory;
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
  private Properties properties;
  
  private boolean serviceRunning = false;
  private List<VideoEditorPipeline> runningPipelines = Collections.synchronizedList(new LinkedList<VideoEditorPipeline>());

  protected void activate(ComponentContext context) {
    Gst.setUseDefaultContext(true);
    Gst.init();
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
    this.properties = new Properties();
    Enumeration keys = properties.keys();
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();
      this.properties.put(key, properties.get(key));
    }
  }

  @Override
  public Set<String> process(Smil smil) throws ProcessFailedException {

    if (smil == null) {
      return null;
    }
    
    Map<String, SourceBinsFactory> pipelineSources = new Hashtable<String, SourceBinsFactory>();
    for (ParallelElement pe : smil.getBody().getSequence().getElements()) {
      for (MediaElement me : pe.getElements()) {

        long begin = me.getClipBeginMS();
        long end = me.getClipEndMS();
        long duration = end - begin;
        String srcFilePath = me.getSrc();
        String outputFilePath = me.getOutputFile();
        
        if (outputFilePath == null || outputFilePath.isEmpty()) {

          String suffix = properties.getProperty(VideoEditorProperties.OUTPUT_FILE_SUFFIX, "_trimmed");
          String extension = properties.getProperty(VideoEditorProperties.OUTPUT_FILE_SUFFIX, VideoEditorPipeline.DEFAULT_OUTPUT_FILE_EXTENSION);

          File srcFile = new File(srcFilePath);
          int index = srcFile.getAbsolutePath().lastIndexOf('.');
          outputFilePath = srcFile.getAbsolutePath().substring(0, index) + suffix + extension;

          me.setOutputFile(outputFilePath);
        }
        
        SourceBinsFactory sourceBins = pipelineSources.get(outputFilePath);
        if (sourceBins == null) {
          sourceBins = new SourceBinsFactory(outputFilePath);
        }
        
        try {
          sourceBins.addFileSource(srcFilePath, begin, duration);
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
        }
      }
    }

    for (SourceBinsFactory fileSourceBin : pipelineSources.values()) {
      
      if (!serviceRunning) throw new ProcessFailedException("Service unavailable!");
      
      VideoEditorPipeline runningPipeline = new VideoEditorPipeline(properties);
      try {
        runningPipeline.addSourceBinsAndCreatePipeline(fileSourceBin);
        
        runningPipelines.add(runningPipeline);
        runningPipeline.run();
        runningPipeline.mainLoop();
        
        if (!serviceRunning) {
          throw new ProcessFailedException("Service unavailable!");
        }
        
        String error = runningPipeline.getLastErrorMessage();
        if (error != null) {
          logger.warn("Last pipeline error: " + error);
          //TODO throw exception?
        }
        
      } catch (PipelineBuildException ex) {
        //TODO logger error
        logger.error(ex.getMessage());
        throw new ProcessFailedException(ex.getMessage());
      } finally {
        if (runningPipelines.contains(runningPipeline)) {
          runningPipelines.remove(runningPipeline);
        }
        runningPipeline = null;
      }
    }

    return pipelineSources.keySet();
  }
}
