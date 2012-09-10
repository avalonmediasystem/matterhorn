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
package org.opencastproject.videoeditor.gstreamer.silencedetector;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.gstreamer.Bus;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Message;
import org.gstreamer.Pad;
import org.gstreamer.Pipeline;
import org.gstreamer.elements.FakeSink;
import org.gstreamer.lowlevel.MainLoop;
import org.opencastproject.videoeditor.api.MediaSegment;
import org.opencastproject.videoeditor.api.ProcessFailedException;
import org.opencastproject.videoeditor.gstreamer.exceptions.PipelineBuildException;
import org.opencastproject.videoeditor.impl.VideoEditorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author wsmirnow
 */
public class GstreamerSilenceDetector {
  
  private static final Logger logger = LoggerFactory.getLogger(GstreamerSilenceDetector.class);
  
  private static final long DEFAULT_SILENCE_MIN_LENGTH = 5L;
  private static final long DEFAULT_SILENCE_PRE_LENGTH = 2L;
  private static final double DEFAULT_THRESHOLD_DB = -40L;
  
  private final Properties properties;
  private final Pipeline pipeline;
  private final MainLoop mainLoop = new MainLoop();
  
  private String lastPipelineError = null;
  
  private List<MediaSegment> segments = null;
  private long lastSilenceStart = 0;
  private long lastSilenceStop = 0;
  
  public GstreamerSilenceDetector(Properties properties, String filePath) throws PipelineBuildException {
    this.properties = properties;
    pipeline = new Pipeline();
    
    final Element filesource = ElementFactory.make("filesrc", null);
    final Element decodebin = ElementFactory.make("decodebin", null);
    final Element audioconvert = ElementFactory.make("audioconvert", null);
    final Element cutter = ElementFactory.make("cutter", "cutter");
    final FakeSink fakesink = (FakeSink) ElementFactory.make("fakesink", null);
    
    pipeline.addMany(filesource, decodebin, audioconvert, cutter, fakesink);
    
    if (!Element.linkMany(filesource, decodebin)) {
      throw new PipelineBuildException();
    }
    
    if (!Element.linkMany(audioconvert, cutter, fakesink)) {
      throw new PipelineBuildException();
    }
    
    filesource.set("location", filePath);
    
    Long minSilenceLength;
    if (properties.containsKey(VideoEditorProperties.SILENCE_PRE_LENGTH)) {
      minSilenceLength = Long.parseLong(properties.getProperty(VideoEditorProperties.SILENCE_PRE_LENGTH));
    } else {
      minSilenceLength = DEFAULT_SILENCE_PRE_LENGTH;
    }
    cutter.set("run-length", TimeUnit.SECONDS.toNanos(minSilenceLength));
    
    Double thresholdDB = DEFAULT_THRESHOLD_DB;
    if (properties.containsKey(VideoEditorProperties.SILENCE_THRESHOLD_DB)) {
      thresholdDB = Double.parseDouble(properties.getProperty(VideoEditorProperties.SILENCE_THRESHOLD_DB));
    }
    cutter.set("threshold-dB", thresholdDB);
    
    fakesink.set("sync", false);
    fakesink.set("silent", true);
    
    decodebin.connect(new Element.PAD_ADDED() {

      @Override
      public void padAdded(Element element, Pad pad) {
        Pad sinkPad = audioconvert.getSinkPads().get(0);
        if (pad.acceptCaps(sinkPad.getCaps()) && !sinkPad.isLinked()) {
          pad.link(sinkPad);
        }
      }
    });
    
    pipeline.getBus().connect(new Bus.MESSAGE() {

      @Override
      public void busMessage(Bus bus, Message message) {
        
        switch(message.getType()) {
          case EOS:
            logger.debug("EOS from " + message.getSource().getName());
            mainLoop.quit();
            if (lastSilenceStart > lastSilenceStop) {
              addVideoSegment(TimeUnit.NANOSECONDS.toMillis(lastSilenceStart), 
                      fakesink.getLastBuffer().getTimestamp().toMillis());
            }
            break;
          case ERROR:
            logger.warn("ERROR from {}: {}", new String[] { 
              message.getSource().getName(), message.getStructure().toString() });
            mainLoop.quit();
            lastPipelineError = String.format("Gstreamer ERROR from %s", 
                    message.getSource().getName());
            break;
          case ELEMENT:
            if (message.getSource() == cutter) {
              parseMessage(message);
            }
            break;
          default: break;
        }
      }
    });
  }
  
  private void parseMessage(Message message) {
//    logger.debug(message.getStructure().toString());
    
    if ("cutter".equals(message.getSource().getName()) 
            && "cutter".equals(message.getStructure().getName())) {
      
      long timestamp = (Long) message.getStructure().getValue("timestamp");
      boolean above = message.getStructure().getBoolean("above");
      
      logger.debug("{}: {}", new String[] {
        Long.toString(timestamp), (above ? "silence stop" : "silence start")
      });
      
      if (above) {
        lastSilenceStart = timestamp;
      } else {
        lastSilenceStop = timestamp;
        addVideoSegment(
                TimeUnit.NANOSECONDS.toMillis(lastSilenceStart), 
                TimeUnit.NANOSECONDS.toMillis(timestamp));
      }
    }
  }
  
  private void addVideoSegment(long startMillis, long stopMillis) {
    if (startMillis < stopMillis)
      segments.add(new MediaSegmentImpl(startMillis, stopMillis));
  }
  
  public void detect() throws ProcessFailedException {
    segments = new LinkedList<MediaSegment>();
    
    pipeline.play();
    mainLoop.run();
    pipeline.stop();
    
    if (lastPipelineError != null) {
      throw new ProcessFailedException(lastPipelineError);
    }
    
     long minSilenceLength = TimeUnit.SECONDS.toMillis(DEFAULT_SILENCE_MIN_LENGTH);
    if (properties.containsKey(VideoEditorProperties.SILENCE_MIN_LENGTH)) {
      minSilenceLength = Long.parseLong(properties.getProperty(VideoEditorProperties.SILENCE_MIN_LENGTH));
      minSilenceLength = TimeUnit.SECONDS.toMillis(lastSilenceStop);
    }
    
    List<MediaSegment> segmentsTmp = new LinkedList<MediaSegment>();
    MediaSegment lastSegment = null;
    for (int i = 0; i < segments.size(); i++) {
      MediaSegment segment = segments.get(i);
      if (lastSegment == null) {
        lastSegment = segment;
      } else {
      
        if (segment.getSegmentStart() - lastSegment.getSegmentStop() < minSilenceLength) {
          segmentsTmp.remove(lastSegment);
          lastSegment = new MediaSegmentImpl(lastSegment.getSegmentStart(), segment.getSegmentStop());
        } else {
          lastSegment = segment;
        }
      }
      segmentsTmp.add(lastSegment);
    }
    
    segments = segmentsTmp;
  }
  
  public void interruptDetection() {
    lastPipelineError = "Detection interrupted!";
    mainLoop.quit();
  }
  
  public List<MediaSegment> getMediaSegments() {
    return segments;
  }
}
