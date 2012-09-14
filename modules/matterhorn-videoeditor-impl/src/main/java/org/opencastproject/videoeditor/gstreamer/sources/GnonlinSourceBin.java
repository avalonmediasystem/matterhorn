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
package org.opencastproject.videoeditor.gstreamer.sources;

import java.util.concurrent.TimeUnit;
import org.gstreamer.Bin;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.GhostPad;
import org.gstreamer.Pad;
import org.gstreamer.event.EOSEvent;
import org.opencastproject.videoeditor.gstreamer.VideoEditorPipeline;
import org.opencastproject.videoeditor.gstreamer.exceptions.PipelineBuildException;
import org.opencastproject.videoeditor.gstreamer.exceptions.UnknownSourceTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wsmirnow
 */
public class GnonlinSourceBin {
  
  public static enum SourceType {
    Null, Audio, Image, Video
  }
  
  /** The logging instance */
  private static final Logger logger = LoggerFactory.getLogger(VideoEditorPipeline.class);
  
  private final SourceType type;
  private final Bin bin;
  private final Bin gnlComposition;
  private final Caps caps;
  
  /** Bin's max duration in millisecond */
  private long maxLengthMillis = 0L;
  
  GnonlinSourceBin(SourceType type, Caps sourceCaps) throws UnknownSourceTypeException, PipelineBuildException {
    this.type = type;
    
    bin = new Bin();
    gnlComposition = (Bin) ElementFactory.make("gnlcomposition", null);
    final Element decodebin = ElementFactory.make("decodebin", null);
    final Element identity = ElementFactory.make("identity", null);
    final Element queue = ElementFactory.make("queue", null);
    final Element converter;
    final Element rate;
    switch(type) {
      case Audio: 
        converter = ElementFactory.make("audioconvert", null);
        rate = ElementFactory.make("audiorate", null);
        if (sourceCaps != null)
          caps = sourceCaps;
        else 
          caps = Caps.fromString("audio/x-raw-int; audio/x-raw-float");
        
        break;
      case Video: 
        converter = ElementFactory.make("ffmpegcolorspace", null);
        rate = ElementFactory.make("videorate", null);
        if (sourceCaps != null)
          caps = sourceCaps;
        else 
          caps = Caps.fromString("video/x-raw-yuv; video/x-raw-rgb");
        break;
      default:
        throw new UnknownSourceTypeException(type);
    }
    
    bin.addMany(gnlComposition, decodebin, identity, converter, rate, queue);
    if (!Element.linkMany(identity, converter, rate, queue)) {
      throw new PipelineBuildException();
    }
    
    if (type == SourceType.Video)
      identity.set("single-segment", true);
//    identity.set("check-imperfect-timestamp", true);
//    identity.set("check-imperfect-offset", true);
    
    Pad srcPad = queue.getSrcPads().get(0);
    bin.addPad(new GhostPad(srcPad.getName(), srcPad));
    
    gnlComposition.connect(new Element.PAD_ADDED() {

      @Override
      public void padAdded(Element source, Pad pad) {
        //TODO to debug
        logger.debug("new pad added {}.{} (caps: {}): ", new String[] {
          source.getName(), pad.getName(), pad.getCaps().toString()
        });

        logger.debug("link {}.{} -> {}.{} with result {}", new String[] {
          source.getName(),
          pad.getName(),
          decodebin.getName(),
          decodebin.getSinkPads().get(0).getName(),
          pad.link(decodebin.getSinkPads().get(0)).toString()
        });
      }
    });
    
    gnlComposition.connect(new Element.NO_MORE_PADS() {

      @Override
      public void noMorePads(Element element) {
        if (!decodebin.getSinkPads().get(0).isLinked()) {
          logger.error(decodebin.getName() + " has no peer!");
          // TODO doesn't working?!
          getBin().sendEvent(new EOSEvent());
          
        }
      }
    });
    
    decodebin.connect(new Element.PAD_ADDED() {

      @Override
      public void padAdded(Element element, Pad pad) {
        logger.debug("link {}.{} -> {}.{} with result {}, {}", new String[] {
          element.getName(),
          pad.getName(),
          identity.getName(),
          identity.getSinkPads().get(0).getName(),
          pad.link(identity.getSinkPads().get(0)).toString(),
          pad.getCaps().toString()
        });
      }
    });
  }
  
  void addFileSource(String filePath, long mediaStartMillis, long mediaDurationMillis) {
    
    Bin gnlsource = (Bin) ElementFactory.make("gnlfilesource", null);
    gnlComposition.add(gnlsource);
        
    gnlsource.set("location", filePath);
    gnlsource.set("caps", caps);
    gnlsource.set("start", TimeUnit.MILLISECONDS.toNanos(maxLengthMillis));
    gnlsource.set("duration", TimeUnit.MILLISECONDS.toNanos(mediaDurationMillis));
    gnlsource.set("media-start", TimeUnit.MILLISECONDS.toNanos(mediaStartMillis));
    gnlsource.set("media-duration", TimeUnit.MILLISECONDS.toNanos(mediaDurationMillis));
    
    maxLengthMillis += mediaDurationMillis;
  }
  
  public Pad getSrcPad() {
    return getBin().getSrcPads().get(0);
  }
  
  public Bin getBin() {
    return bin;
  }
  
  public long getLengthMilliseconds() {
    return maxLengthMillis;
  }
  
  public SourceType getSourceType() {
    return type;
  }
}
