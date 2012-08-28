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
import java.util.concurrent.TimeUnit;
import org.gstreamer.Bin;
import org.gstreamer.Bus;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Gst;
import org.gstreamer.GstObject;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.event.EOSEvent;
import org.gstreamer.lowlevel.MainLoop;
import org.opencastproject.videoeditor.gstreamer.exceptions.PipelineBuildException;
import org.opencastproject.videoeditor.gstreamer.sources.FileSourceBins;
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
  
  public static final String DEFAULT_AUDIO_ENCODER = "faac";
  public static final String DEFAULT_AUDIO_ENCODER_PROPERTIES = "";
  
  public static final String DEFAULT_VIDEO_ENCODER = "x264enc";
  public static final String DEFAULT_VIDEO_ENCODER_PROPERTIES = "";
  
  public static final String DEFAULT_MUXER = "mp4mux";
  public static final String DEFAULT_MUXER_PROPERTIES = "";
  
  
  private Dictionary properties;
  private Pipeline pipeline;
  private MainLoop mainLoop = new MainLoop();
  private String lastErrorMessage = null;
  
  public VideoEditorPipeline(Dictionary properties) {
    this.properties = properties;
    
    Gst.setUseDefaultContext(true);
    Gst.init();
    pipeline = new Pipeline();
  }
  
  public void run() {
    logger.info("starting pipeline");
    //TODO debug remove
    pipeline.debugToDotFile(Pipeline.DEBUG_GRAPH_SHOW_ALL, "videoeditor-pipeline", true);
    
    pipeline.play();
  }
  
  public boolean stop() {
    if (pipeline == null) return true;
    
    for (Element sourceElem : pipeline.getSources()) {
      logger.debug("sending EOS to {}", sourceElem.getName());
      sourceElem.sendEvent(new EOSEvent());
    }
    
    for (int count = 0; count < 15; count++) {
      logger.debug("wait until pipeline stop...");
      if (pipeline != null) {
        if (getState(TimeUnit.MILLISECONDS.toNanos(WAIT_FOR_NULL_SLEEP_TIME)) == State.NULL) {
          logger.debug("pipeline stopped");
          return true;
        }
      } else return true;
    }
    pipeline.setState(State.NULL);
    return false;
  }
  
  public void mainLoop() {
    
    mainLoop.run();
    logger.info("main quit!");
    stop();
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
        logger.info("INFO from {}: ", source.getName(), message);
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
        lastErrorMessage = String.format("%s: %s", source.getName(), message);
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
        logger.info("EOS: stop pipeline");
        mainLoop.quit();
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
    
    pipeline.getBus().connect(new Bus.STATE_CHANGED() {

      @Override
      public void stateChanged(GstObject go, State state, State state1, State state2) {
        logger.info("{} changed state to {}", new String[] {
          go.getName(), state.toString()
        });
      }
    });
  }
  
  public State getState() {
    return getState(0);
  }
  
  public State getState(long timeout) {
    if (pipeline == null) return State.NULL;
    return pipeline.getState(timeout);
  }
  
  public void addSourceBinsAndCreatePipeline(FileSourceBins sourceBins) 
          throws PipelineBuildException {
    
    // create and link muxer and filesink    
    Element muxer = createMuxer();
    Element fileSink = ElementFactory.make(GstreamerElements.FILESINK, null);
    pipeline.addMany(muxer, fileSink);
    
    fileSink.set(GstreamerElementProperties.LOCATION, sourceBins.getOutputFilePath());
    fileSink.set(GstreamerElementProperties.SYNC, false);
    fileSink.set(GstreamerElementProperties.ASYNC, false);
    
    if (!muxer.link(fileSink)) {
      throw new PipelineBuildException();
    }
    
    Bin sourceBin;
    Element encoder;
    
    if (sourceBins.hasAudioSource()) {
      // create and link audio bin and audio encoder
      sourceBin = sourceBins.getAudioSourceBin();
      encoder = createAudioEncoder();
      pipeline.addMany(sourceBin, encoder);
      if (!Element.linkMany(sourceBin, encoder, muxer)) {
        throw new PipelineBuildException();
      }
    }

    if (sourceBins.hasVideoSource()) {
      // create and link video bin and audio encoder
      sourceBin = sourceBins.getVideoSourceBin();
      encoder = createVideoEncoder();
      pipeline.addMany(sourceBin, encoder);
      if (!Element.linkMany(sourceBin, encoder, muxer)) {
        throw new PipelineBuildException();
      }
    }
  }
  public String getLastErrorMessage() {
    return lastErrorMessage;
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
