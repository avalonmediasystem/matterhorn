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

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import org.gstreamer.Bus;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Gst;
import org.gstreamer.GstObject;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.event.EOSEvent;
import org.opencastproject.videoeditor.gstreamer.exceptions.PipelineBuildException;
import org.opencastproject.videoeditor.gstreamer.sources.GStreamerSourceBin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author wsmirnow
 */
public class VideoEditorPipeline {
  
  /** The logging instance */
  private static final Logger logger = LoggerFactory.getLogger(VideoEditorPipeline.class);
  
  private static final int WAIT_FOR_NULL_SLEEP_TIME = 1000;
  public static final long GST_SECOND = 1000000000L;
  public static final long GST_MILLI_SECOND = 1000000L;
  
  
  public static final String DEFAULT_AUDIO_ENCODER = "mpeg2enc";
  public static final String DEFAULT_AUDIO_ENCODER_PROPERTIES = "";
  
  public static final String DEFAULT_VIDEO_ENCODER = "lame";
  public static final String DEFAULT_VIDEO_ENCODER_PROPERTIES = "";
  
  public static final String DEFAULT_MUXER = "mpegpsmux";
  public static final String DEFAULT_MUXER_PROPERTIES = "";
  
  
  private Dictionary properties;
  private Pipeline pipeline;
  
  public VideoEditorPipeline(Dictionary properties) {
    this.properties = properties;
    Gst.init();
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
    pipeline.setState(State.NULL);
    return false;
  }
  
  public void waitTilStop() {
    Gst.main();
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
        Gst.quit();
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
  
  public String getStateString() {
    if (pipeline == null) return "null";
    
    switch(pipeline.getState()) {
      case NULL:
        return "null";
      case READY:
        return "ready";
      case PAUSED:
        return "paused";
      case PLAYING:
        return "playing";
      case VOID_PENDING:
        return "pending";
      default: 
        // wouldn't pass
        return "";
    }
  }
  
  public State getState() {
    if (pipeline == null) return State.NULL;
    return pipeline.getState();
  }
  
  public void addSourceBin(GStreamerSourceBin sourceBin, String outputFileName) 
          throws PipelineBuildException {
    
    Element encoder = null;
    switch (sourceBin.getType()) {
      case Audio: 
        createAudioEncoder();
        break;
      case Video: 
        createVideoEncoder();
        break;
      default: return;
    }
    Element muxer = createMuxer();
    Element fileSink = ElementFactory.make(GstreamerElements.FAKESINK, null);
    pipeline.addMany(sourceBin.getBin(), encoder, muxer, fileSink);
    
    if (!sourceBin.getSinkElement().link(encoder)) {
      throw new PipelineBuildException();
    }
    if (!encoder.link(muxer)) {
      throw new PipelineBuildException();
    }
    if (!muxer.link(fileSink)) {
      throw new PipelineBuildException();
    }
    
    fileSink.set(GstreamerElementProperties.LOCATION, outputFileName);
    fileSink.set(GstreamerElementProperties.SYNC, false);
    fileSink.set(GstreamerElementProperties.ASYNC, false);
  }
  
  protected Element createAudioEncoder() {
    String encoder;
    String encoderProperties;
    
    // TODO get all from config
    
    encoder = DEFAULT_AUDIO_ENCODER;
    Element encoderElem = ElementFactory.make(encoder, null);
    
    encoderProperties = DEFAULT_AUDIO_ENCODER_PROPERTIES;
    Map<String, String> encoderPropertiesDict = getPropertiesFromString(encoderProperties);
    
    for (String key : encoderPropertiesDict.keySet()) {
      encoderElem.set(key, encoderPropertiesDict.get(key));
    }
    
    return encoderElem;
  }
  
  protected Element createVideoEncoder() {
    String encoder;
    String encoderProperties;
    
    // TODO get all from config
    
    encoder = DEFAULT_VIDEO_ENCODER;
    Element encoderElem = ElementFactory.make(encoder, null);
    
    encoderProperties = DEFAULT_VIDEO_ENCODER_PROPERTIES;
    Map<String, String> encoderPropertiesDict = getPropertiesFromString(encoderProperties);
    
    for (String key : encoderPropertiesDict.keySet()) {
      encoderElem.set(key, encoderPropertiesDict.get(key));
    }
    
    return encoderElem;
  }
  
  protected Element createMuxer() {
    String muxer;
    String muxerProperties;
    
    // TODO get all from config
    
    muxer = DEFAULT_MUXER;
    Element muxerElem = ElementFactory.make(muxer, null);
    
    muxerProperties = DEFAULT_MUXER_PROPERTIES;
    Map<String, String> encoderPropertiesDict = getPropertiesFromString(muxerProperties);
    
    for (String key : encoderPropertiesDict.keySet()) {
      muxerElem.set(key, encoderPropertiesDict.get(key));
    }
    
    return muxerElem;
  }

  private Map<String, String> getPropertiesFromString(String encoderProperties) {
    Map<String, String> properties = new HashMap<String, String>();
    
    for (String prop : encoderProperties.trim().split(" ")) {
      if (prop.isEmpty()) break;
      
      if (prop.trim().split("=").length == 2) {
        properties.put(prop.trim().split("=")[0], prop.trim().split("=")[1]);
      }
    }
    
    return properties;
  }
}
