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
package org.opencastproject.videoeditor.gstreamer;

import org.gstreamer.Bus;
import org.gstreamer.Element;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.event.EOSEvent;
import org.gstreamer.GstObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author wsmirnow
 */
public class VideoEditorGstreamer {
  
  /** The logging instance */
  private static final Logger logger = LoggerFactory.getLogger(VideoEditorGstreamer.class);
  
  private static final int WAIT_FOR_NULL_SLEEP_TIME = 1000;
  public static final long GST_SECOND = 1000000000L;
  
  private Pipeline pipeline;
  
  public VideoEditorGstreamer() {
    pipeline = new Pipeline();
  }
  
  public void run() {
    logger.debug("starting pipeline...");
    pipeline.setState(State.PLAYING);
  }
  
  public boolean stop() {
    for (Element sourceElem : pipeline.getSources()) {
      logger.debug("sending EOS to {}", sourceElem.getName());
      sourceElem.sendEvent(new EOSEvent());
    }
    
    for (int count = 0; count < 15; count++) {
      logger.debug("wait until pipeline stop...");
      if (pipeline != null) {
        if (pipeline.getState(WAIT_FOR_NULL_SLEEP_TIME * GST_SECOND) == State.NULL) {
          logger.debug("pipeline stopped");
          return true;
        }
      } else return true;
    }
    return false;
  }
  
  protected void addListener() {
    pipeline.getBus().connect(new Bus.INFO() {

      /**
       * {@inheritDoc}
       * 
       * @see org.gstreamer.Bus.INFO#infoMessage(org.gstreamer.GstObject, int, java.lang.String)
       */
      @Override
      public void infoMessage(GstObject source, int code, String message) {
        logger.debug("INFO from {}: ", source.getName(), message);
      }
    });
    
    pipeline.getBus().connect(new Bus.ERROR() {

      /**
       * {@inheritDoc}
       * 
       * @see org.gstreamer.Bus.ERROR#errorMessage(org.gstreamer.GstObject, int, java.lang.String)
       */
      @Override
      public void errorMessage(GstObject source, int code, String message) {
        logger.warn("ERROR from {}: ", source.getName(), message);
      }
    });
    
    pipeline.getBus().connect(new Bus.EOS() {

      /**
       * {@inheritDoc}
       * 
       * @see org.gstreamer.Bus.EOS#endOfStream(org.gstreamer.GstObject)
       */
      @Override
      public void endOfStream(GstObject source) {
        logger.debug("EOS: stop pipeline");
        pipeline.setState(State.NULL);
        pipeline = null;
      }
    });
    pipeline.getBus().connect(new Bus.WARNING() {

      /**
       * {@inheritDoc}
       * 
       * @see org.gstreamer.Bus.WARNING#warningMessage(org.gstreamer.GstObject, int, java.lang.String)
       */
      @Override
      public void warningMessage(GstObject source, int code, String message) {
        logger.warn("WARNING from {}: ", source.getName(), message);
      }
    });
  }
}
