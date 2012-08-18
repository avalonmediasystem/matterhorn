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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.gstreamer.Gst;
import org.opencastproject.smil.entity.MediaElement;
import org.opencastproject.smil.entity.ParallelElement;
import org.opencastproject.smil.entity.Smil;
import org.opencastproject.videoeditor.api.VideoEditor;
import org.opencastproject.videoeditor.gstreamer.GstreamerFileTypeFinder;
import org.opencastproject.videoeditor.gstreamer.VideoEditorPipeline;
import org.opencastproject.videoeditor.gstreamer.exceptions.CanNotAddElementException;
import org.opencastproject.videoeditor.gstreamer.exceptions.PipelineBuildException;
import org.opencastproject.videoeditor.gstreamer.exceptions.UnknownSourceTypeException;
import org.opencastproject.videoeditor.gstreamer.sources.GStreamerSourceBin;
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
  public List<File> process(Smil smil) {

    if (smil == null) {
      return new LinkedList<File>();
    }

    List<File> files = new LinkedList<File>();

    Map<String, GStreamerSourceBin> pipelineSources = new HashMap<String, GStreamerSourceBin>();

    for (ParallelElement pe : smil.getBody().getSequence().getElements()) {
      for (MediaElement me : pe.getElements()) {
        int begin = me.getClipBeginMS();
        int end = me.getClipEndMS();
        String srcFile = me.getSrc();

        try {
          
          GStreamerSourceBin sourceBin = pipelineSources.get(srcFile);
          
          if (sourceBin == null) {
            GstreamerFileTypeFinder typeFinder;
            typeFinder = new GstreamerFileTypeFinder(srcFile);

            if (typeFinder.isAudioFile()) {
              sourceBin = new GStreamerSourceBin(GStreamerSourceBin.SourceType.Audio);
            } else if (typeFinder.isVideoFile()) {
              sourceBin = new GStreamerSourceBin(GStreamerSourceBin.SourceType.Video);
            }
          }
          
          sourceBin.addFileSource(srcFile, -1, begin, end);
          pipelineSources.put(srcFile, sourceBin);
          

        } catch (FileNotFoundException ex) {
          logger.error("'{}' does not exist!", srcFile);
        } catch (PipelineBuildException ex) {
          logger.error("Can't build pipeline: {}", ex.getMessage());
        } catch (UnknownSourceTypeException ex) {
          logger.error(ex.getMessage());
        } catch (CanNotAddElementException ex) {
          logger.error(ex.getMessage());
        }
      }
    }
    
    VideoEditorPipeline pipeline = new VideoEditorPipeline(properties);
    
    for (String srcFile : pipelineSources.keySet()) {
      try {
        // TODO set output filename
        String outputFilePath = srcFile + "_trimmed";
        pipeline.addSourceBin(pipelineSources.get(srcFile), outputFilePath);
        files.add(new File(outputFilePath));
      } catch (PipelineBuildException ex) {
        logger.error(ex.getMessage());
      }
    }
    
    pipeline.waitTilStop();
    
    return files;
  }
}
