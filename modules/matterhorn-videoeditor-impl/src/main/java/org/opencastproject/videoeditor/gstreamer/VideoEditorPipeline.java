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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.gstreamer.Bin;
import org.gstreamer.Bus;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Gst;
import org.gstreamer.GstObject;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.lowlevel.MainLoop;
import org.opencastproject.videoeditor.gstreamer.exceptions.PipelineBuildException;
import org.opencastproject.videoeditor.gstreamer.sources.SourceBinsFactory;
import org.opencastproject.videoeditor.impl.VideoEditorProperties;
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
  public static final String DEFAULT_AUDIO_ENCODER_PROPERTIES = "bitrate=128000";
  
  public static final String DEFAULT_VIDEO_ENCODER = "x264enc";
  public static final String DEFAULT_VIDEO_ENCODER_PROPERTIES = "";
  
  public static final String DEFAULT_MUX = "mp4mux";
  public static final String DEFAULT_MUX_PROPERTIES = "";
  
  public static final String DEFAULT_OUTPUT_FILE_EXTENSION = ".mp4";
  
  
  private Properties properties;
  private Pipeline pipeline;
  private MainLoop mainLoop = new MainLoop();
  private String lastErrorMessage = null;
  
  public VideoEditorPipeline(Properties properties) {
    this.properties = properties != null ? properties : new Properties();
    
    pipeline = new Pipeline();
  }
  
  public void run() {
    logger.debug("starting pipeline...");
    pipeline.play();
  }
  
  public boolean stop() {
    if (pipeline == null) return true;
    pipeline.setState(State.NULL);
    return true;
  }
  
  public void mainLoop() {
    
    mainLoop.run();
    logger.debug("main loop quit!");
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
        lastErrorMessage = String.format("%s: %s", source.getName(), message);
        mainLoop.quit();
        Gst.quit();
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
        logger.debug("EOS from {}: stop pipeline", new String[] { source.getName() });
        mainLoop.quit();
        Gst.quit();
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
      public void stateChanged(GstObject source, State old, State current, State pending) {
        if (source instanceof Pipeline) {
          logger.debug("{} changed state to {}", new String[] {
            source.getName(), current.toString()
          });
          if (current == State.READY || current == State.PLAYING) {
            pipeline.debugToDotFile(Pipeline.DEBUG_GRAPH_SHOW_NON_DEFAULT_PARAMS | Pipeline.DEBUG_GRAPH_SHOW_STATES, 
                    "videoeditor-pipeline-" + current.name(), true);
          }
        }
      }
    });
  }
  
  public State getState() {
    return getState(0);
  }
  
  public State getState(long timeoutMillis) {
    if (pipeline == null) return State.NULL;
    return pipeline.getState(TimeUnit.MILLISECONDS.toNanos(timeoutMillis));
  }
  
  public void addSourceBinsAndCreatePipeline(SourceBinsFactory sourceBins) 
          throws PipelineBuildException {
    
    // create and link muxer and filesink    
    Element muxer = createMux();
    Element fileSink = ElementFactory.make(GstreamerElements.FILESINK, null);
    pipeline.addMany(muxer, fileSink);
    
    fileSink.set(GstreamerElementProperties.LOCATION, sourceBins.getOutputFilePath());
    fileSink.set(GstreamerElementProperties.SYNC, false);
    fileSink.set(GstreamerElementProperties.ASYNC, false);
    
    if (!muxer.link(fileSink)) {
      throw new PipelineBuildException();
    }
    
    Bin sourceBin;
    Element capsfilter;
    Element encoder;
    
    if (sourceBins.hasAudioSource()) {
      // create and link audio bin and audio encoder
      sourceBin = sourceBins.getAudioSourceBin();
      capsfilter = ElementFactory.make("capsfilter", "audiocaps");
      encoder = createAudioEncoder();
      pipeline.addMany(sourceBin, capsfilter, encoder);
      if (!Element.linkMany(sourceBin, capsfilter, encoder, muxer)) {
        throw new PipelineBuildException();
      }
      
      if (properties.containsKey(VideoEditorProperties.AUDIO_CAPS)) {
        capsfilter.setCaps(Caps.fromString(properties.getProperty(VideoEditorProperties.AUDIO_CAPS)));
      }
    }

    if (sourceBins.hasVideoSource()) {
      // create and link video bin and audio encoder
      sourceBin = sourceBins.getVideoSourceBin();
      capsfilter = ElementFactory.make("capsfilter", "videocaps");
      encoder = createVideoEncoder();
      pipeline.addMany(sourceBin, capsfilter, encoder);
      if (!Element.linkMany(sourceBin, capsfilter, encoder, muxer)) {
        throw new PipelineBuildException();
      }
      
      if (properties.containsKey(VideoEditorProperties.VIDEO_CAPS)) {
        capsfilter.setCaps(Caps.fromString(properties.getProperty(VideoEditorProperties.VIDEO_CAPS)));
      }
    }
    
    addListener();
  }
  
  public String getLastErrorMessage() {
    return lastErrorMessage;
  }
  
  protected Element createAudioEncoder() {
    String encoder = properties.getProperty(VideoEditorProperties.AUDIO_ENCODER, DEFAULT_AUDIO_ENCODER);
    String encoderProperties = properties.getProperty(VideoEditorProperties.AUDIO_ENCODER_PROPERTIES, DEFAULT_AUDIO_ENCODER_PROPERTIES);
        
    Element encoderElem = ElementFactory.make(encoder, null);
    Map<String, String> encoderPropertiesDict = getPropertiesFromString(encoderProperties);
    
    for (String key : encoderPropertiesDict.keySet()) {
      encoderElem.set(key, encoderPropertiesDict.get(key));
    }
    
    return encoderElem;
  }
  
  protected Element createVideoEncoder() {
    String encoder = properties.getProperty(VideoEditorProperties.VIDEO_ENCODER, DEFAULT_VIDEO_ENCODER);
    String encoderProperties = properties.getProperty(VideoEditorProperties.VIDEO_ENCODER_PROPERTIES, DEFAULT_VIDEO_ENCODER_PROPERTIES);
        
    Element encoderElem = ElementFactory.make(encoder, null);
    Map<String, String> encoderPropertiesDict = getPropertiesFromString(encoderProperties);
    
    for (String key : encoderPropertiesDict.keySet()) {
      encoderElem.set(key, encoderPropertiesDict.get(key));
    }
    
    return encoderElem;
  }
  
  protected Element createMux() {
    String mux = properties.getProperty(VideoEditorProperties.MUX, DEFAULT_MUX);
    String muxProperties = properties.getProperty(VideoEditorProperties.MUX_PROPERTIES, DEFAULT_MUX_PROPERTIES);
    
    Element muxElem = ElementFactory.make(mux, null);
    Map<String, String> muxPropertiesDict = getPropertiesFromString(muxProperties);
    
    for (String key : muxPropertiesDict.keySet()) {
      muxElem.set(key, muxPropertiesDict.get(key));
    }
    
    return muxElem;
  }

  private static Map<String, String> getPropertiesFromString(String encoderProperties) {
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
