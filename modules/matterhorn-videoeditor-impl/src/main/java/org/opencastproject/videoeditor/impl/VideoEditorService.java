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

import java.io.FileNotFoundException;
import java.util.Dictionary;
import org.gstreamer.Gst;
import org.opencastproject.job.api.Job;
import org.opencastproject.smil.entity.Smil;
import org.opencastproject.videoeditor.api.VideoEditor;
import org.opencastproject.videoeditor.gstreamer.GstreamerFileTypeFinder;
import org.opencastproject.videoeditor.gstreamer.exceptions.PipelineBuildException;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class VideoEditorService implements VideoEditor, ManagedService {

  /** The logging instance */
  private static final Logger logger = LoggerFactory.getLogger(VideoEditorService.class);
  
  
  protected void activate(ComponentContext context)
  {
    Gst.init();
    String file = "/home/wsmirnow/Videos/Sintel.mp4";
    logger.info("parse caps from " + file);
    
    try {
      GstreamerFileTypeFinder tf = new GstreamerFileTypeFinder(file);
      logger.info("{} is audio: {}", file, tf.isAudioFile());
      logger.info("{} is image: {}", file, tf.isImageFile());
      logger.info("{} is video: {}", file, tf.isVideoFile());
      
    } catch (FileNotFoundException ex) {
      logger.error(ex.getMessage());
    } catch (PipelineBuildException ex) {
      logger.error(ex.getMessage());
    }
    file = "/home/wsmirnow/Videos/Sintel.mpg";
    logger.info("parse caps from " + file);
    try {
      GstreamerFileTypeFinder tf = new GstreamerFileTypeFinder(file);
      logger.info("{} is audio: {}", file, tf.isAudioFile());
      logger.info("{} is image: {}", file, tf.isImageFile());
      logger.info("{} is video: {}", file, tf.isVideoFile());
      
    } catch (FileNotFoundException ex) {
      logger.error(ex.getMessage());
    } catch (PipelineBuildException ex) {
      logger.error(ex.getMessage());
    }
  }
  
  protected void deactivate(ComponentContext context)
  {
  }
  
  @Override
  public void updated(Dictionary properties) throws ConfigurationException {
    logger.debug("updated");
  }
  
  @Override
  public Job process(Smil smil) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
