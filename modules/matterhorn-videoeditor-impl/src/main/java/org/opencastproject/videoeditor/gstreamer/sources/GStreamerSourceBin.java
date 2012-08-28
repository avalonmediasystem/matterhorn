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
import org.gstreamer.PadLinkReturn;
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
public class GStreamerSourceBin {
  
  public static enum SourceType {
    Null, Audio, Image, Video
  }
  
  /** The logging instance */
  private static final Logger logger = LoggerFactory.getLogger(VideoEditorPipeline.class);
  
  private final SourceType type;
  private final Bin bin;
  private final Bin gnlComposition;
  private final Element converter;
  private final Element identity;
  private final Caps caps;
  private final GhostPad ghostPad;
  
  /** Bin's max duration in millisecond */
  private long maxLengthMillis = 0L;
  
  private int sources = Integer.MAX_VALUE;
  
  public GStreamerSourceBin(SourceType type) throws UnknownSourceTypeException, PipelineBuildException {
    this.type = type;
    
    bin = new Bin();
    gnlComposition = (Bin) ElementFactory.make("gnlcomposition", null);
    identity = ElementFactory.make("identity", null);
    
    switch(type) {
      case Audio: 
        converter = ElementFactory.make("audioconvert", null);
        caps = Caps.fromString("audio/x-raw-int; audio/x-raw-float");
        break;
      case Video: 
        converter = ElementFactory.make("ffmpegcolorspace", null);
        caps = Caps.fromString("video/x-raw-yuv; video/x-raw-rgb");
        break;
      default:
        throw new UnknownSourceTypeException(type);
    }
    
    bin.addMany(gnlComposition, converter, identity);
    if (!Element.linkMany(converter, identity)) {
      throw new PipelineBuildException();
    }
    
    if (type == SourceType.Video)
      identity.set("single-segment", true);
    identity.set("check-imperfect-timestamp", true);
    identity.set("check-imperfect-offset", true);
    
    ghostPad = new GhostPad(identity.getSrcPads().get(0).getName(), identity.getSrcPads().get(0));
    bin.addPad(ghostPad);
    
    gnlComposition.connect(new Element.PAD_ADDED() {

      @Override
      public void padAdded(Element source, Pad pad) {
        //TODO to debug
        logger.info("new pad added {}.{} (cpas: {}): ", new String[] {
          source.getName(), pad.getName(), pad.getCaps().toString()
        });

        if (pad.acceptCaps(caps)) {
          PadLinkReturn plr = pad.link(converter.getSinkPads().get(0));
          if (plr != PadLinkReturn.OK) {
            logger.warn("pad link {}.{} -> {}.{} with status {}", new String[] {
              source.getName(),
              pad.getName(),
              converter.getName(),
              converter.getSrcPads().get(0).getName(),
              plr.toString()
            });
          }
        }
      }
    });
    
    gnlComposition.connect(new Element.NO_MORE_PADS() {

      @Override
      public void noMorePads(Element element) {
        if (!converter.getSinkPads().get(0).isLinked()) {
          logger.error("Source element has no peer!");
          converter.sendEvent(new EOSEvent());
        }
      }
    });
  }
  
  public void addFileSource(String filePath, long mediaStartMillis, long mediaDurationMillis) {
    
    Element filesource = ElementFactory.make("gnlfilesource", null);
    gnlComposition.add(filesource);
    
    filesource.set("location", filePath);
    filesource.set("start", TimeUnit.MILLISECONDS.toNanos(maxLengthMillis));
    filesource.set("duration", TimeUnit.MILLISECONDS.toNanos(mediaDurationMillis));
    filesource.set("media-start", TimeUnit.MILLISECONDS.toNanos(mediaStartMillis));
    filesource.set("media-duration", TimeUnit.MILLISECONDS.toNanos(mediaDurationMillis));
    filesource.set("caps", caps);
//    filesource.set("priority", --sources);
    
    maxLengthMillis += mediaDurationMillis;
  }
  
  public Pad getSrcPad() {
    return ghostPad;
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
