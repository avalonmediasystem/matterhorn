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
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pad;
import org.gstreamer.PadLinkReturn;
import org.opencastproject.videoeditor.gstreamer.VideoEditorPipeline;
import org.opencastproject.videoeditor.gstreamer.exceptions.CanNotAddElementException;
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
  
  private SourceType type;
  private Bin bin = null;
  private Bin gnlComposition = null;
  private Element converter = null;
  private Element identity = null;
  private Element queue = null;
  private Element rate = null;
  
  /** Bin's max duration in millisecond */
  private long maxLengthMillis = 0L;
  
  private int sources = Integer.MAX_VALUE;
  
  public GStreamerSourceBin(SourceType type) throws UnknownSourceTypeException {
    this.type = type;
    
    bin = new Bin();
    
    switch(type) {
      case Audio: 
        converter = ElementFactory.make("audioconvert", null);
        rate = ElementFactory.make("audiorate", null);
        break;
      case Video: 
        converter = ElementFactory.make("ffmpegcolorspace", null);
        rate = ElementFactory.make("videorate", null);
        break;
      default:
        throw new UnknownSourceTypeException(type);
    }
    identity = ElementFactory.make("identity", null);
    queue = ElementFactory.make("queue", null);
    bin.addMany(converter, identity, queue, rate);
    Element.linkMany(converter, identity, queue, rate);
    identity.set("single-segment", true);
    identity.set("check-imperfect-timestamp", true);
    identity.set("check-imperfect-offset", true);
    
    gnlComposition = (Bin) ElementFactory.make("gnlcomposition", null);
    bin.add(gnlComposition);
    gnlComposition.connect(new Element.PAD_ADDED() {

      @Override
      public void padAdded(Element source, Pad pad) {
        logger.info("new pad added with caps: " + pad.getCaps().toString());

        if (converter.getSinkPads().get(0).acceptCaps(pad.getCaps())) {
          
          PadLinkReturn ret = pad.link(converter.getSinkPads().get(0));
          if (ret != PadLinkReturn.OK) {
            logger.error("pad not linked {}.{} -> {}.{}", new String[] {
                    source.getName(),
                    pad.getName(),
                    converter.getName(),
                    converter.getSinkPads().get(0).getName()
            });
          } else {
            logger.info("pad linked {}.{} -> {}.{} with caps {}", new String[] {
              source.getName(),
              pad.getName(),
              converter.getName(),
              converter.getSinkPads().get(0).getName(),
              pad.getCaps().toString()
            });
          }
        } else {
          logger.info("peer does not accept caps {}.{} {}.{}", new String[] {
              source.getName(),
              pad.getName(),
              converter.getName(),
              converter.getSinkPads().get(0).getName()
          });
        }
      }
    });
  }
  
  public void addFileSource(String filePath, long mediaStartMillis, long mediaDurationMillis) 
          throws CanNotAddElementException {
    
    Element filesource = ElementFactory.make("gnlfilesource", null);
    if (!gnlComposition.add(filesource)) {
      throw new CanNotAddElementException(gnlComposition, filesource);
    }
    
    filesource.set("location", filePath);
    filesource.set("start", TimeUnit.MILLISECONDS.toNanos(maxLengthMillis));
    filesource.set("duration", TimeUnit.MILLISECONDS.toNanos(mediaDurationMillis));
    filesource.set("media-start", TimeUnit.MILLISECONDS.toNanos(mediaStartMillis));
    filesource.set("media-duration", TimeUnit.MILLISECONDS.toNanos(mediaDurationMillis));
//    filesource.set("priority", --sources);
    
    maxLengthMillis += mediaDurationMillis;
  }
  
  public Pad getSrcPad() {
    return rate.getSrcPads().get(0);
  }
  
  public Element getSinkElement() {
    return rate;
  }
  
  public Bin getBin() {
    return bin;
  }
  
  public SourceType getType() {
    return type;
  }
  
  public long getLengthMillisecond() {
    return maxLengthMillis;
  }
  
  public SourceType getSourceType() {
    return type;
  }
}
