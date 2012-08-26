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
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import org.gstreamer.Gst;
import org.opencastproject.smil.entity.MediaElement;
import org.opencastproject.smil.entity.ParallelElement;
import org.opencastproject.smil.entity.Smil;
import org.opencastproject.videoeditor.api.VideoEditor;
import org.opencastproject.videoeditor.gstreamer.VideoEditorPipeline;
import org.opencastproject.videoeditor.gstreamer.exceptions.CanNotAddElementException;
import org.opencastproject.videoeditor.gstreamer.exceptions.PipelineBuildException;
import org.opencastproject.videoeditor.gstreamer.exceptions.UnknownSourceTypeException;
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
  private Dictionary properties;

  protected void activate(ComponentContext context) {
    Gst.init();
  }

  protected void deactivate(ComponentContext context) {
    Gst.deinit();
  }

  @Override
  public void updated(Dictionary properties) throws ConfigurationException {
    this.properties = properties;
  }

  @Override
  public Set<String> process(Smil smil) {

    if (smil == null) {
      return new HashSet<String>();
    }
    
    VideoEditorPipeline pipeline = new VideoEditorPipeline(properties);

    Map<String, FileSourceBins> pipelineSources = new Hashtable<String, FileSourceBins>();
    for (ParallelElement pe : smil.getBody().getSequence().getElements()) {
      for (MediaElement me : pe.getElements()) {

        long begin = me.getClipBeginMS();
        long end = me.getClipEndMS();
        long duration = end - begin;
        String srcFilePath = me.getSrc();

        File srcFile = new File(srcFilePath);
        int index = srcFile.getAbsolutePath().lastIndexOf('.');
        String outputFilePath = srcFile.getAbsolutePath().substring(0, index) + "_edited"
                  + srcFile.getAbsolutePath().substring(index, srcFile.getAbsolutePath().length());
        
        FileSourceBins sourceBins = pipelineSources.get(outputFilePath);
        if (sourceBins == null) {
          sourceBins = new FileSourceBins(outputFilePath);
        }
        
        try {
          sourceBins.addFileSource(srcFilePath, begin, duration);
          pipelineSources.put(sourceBins.getOutputFilePath(), sourceBins);
        } catch (UnknownSourceTypeException ex) {
          logger.error(ex.getMessage());
          return null;
        } catch (CanNotAddElementException ex) {
          logger.error(ex.getMessage());
          return null;
        } catch (FileNotFoundException ex) {
          logger.error(ex.getMessage());
          return null;
        } catch (PipelineBuildException ex) {
          logger.error(ex.getMessage());
          return null;
        }
      }
    }

    for (FileSourceBins fileSourceBins : pipelineSources.values()) {
      try {
        logger.info("outputfile: " + fileSourceBins.getOutputFilePath());
        pipeline.addSourceBinsAndCreatePipeline(fileSourceBins);
      } catch (PipelineBuildException ex) {
        logger.error(ex.getMessage());
        return null;
      }
    }
    
    pipeline.run();
    pipeline.mainLoop();

    return pipelineSources.keySet();
  }
}
