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
  private Element defaultSource = null;
  private Element converter = null;
  
  /** Bin's max duration in millisecond */
  private long maxLength = 0L;
  
  public GStreamerSourceBin(SourceType type) throws UnknownSourceTypeException {
    this.type = type;
    
    bin = new Bin();
    
    switch(type) {
      case Audio: 
        converter = ElementFactory.make("audioconvert", null);
        break;
      case Video: 
        converter = ElementFactory.make("ffmpegcolorspace", null);
        break;
      default:
        throw new UnknownSourceTypeException(type);
    }
    bin.add(converter);
    
    gnlComposition = (Bin) ElementFactory.make("gnlcomposition", null);
    bin.add(gnlComposition);
    gnlComposition.connect(new Element.PAD_ADDED() {

      @Override
      public void padAdded(Element source, Pad pad) {
        if (pad.link(converter.getSinkPads().get(0)) != PadLinkReturn.OK) {
          logger.error("pad not linked: {}.{} -> {}.{}", new String[] {
                  source.getName(),
                  pad.getName(),
                  converter.getName(),
                  converter.getSinkPads().get(0).getName()
          });
        }
      }
    });
    
    switch(type) {
      case Audio: 
        defaultSource = ElementFactory.make("audiotestsrc", null);
        defaultSource.set("volume", 0.0f);
        break;
      case Video: 
        defaultSource = ElementFactory.make("videotestsrc", null);
        break;
      default: 
        throw new UnknownSourceTypeException(type);
    }
    
    gnlComposition.add(defaultSource);
    defaultSource.set("priority", -1);
  }
  
  public void addFileSource(String filePath, long start, long mediaStart, long mediaStop) 
          throws CanNotAddElementException {
    
    Element filesource = ElementFactory.make("gnlfilesource", null);
    if (!gnlComposition.add(filesource)) {
      throw new CanNotAddElementException(gnlComposition, filesource);
    }
    filesource.set("location", filePath);
    filesource.set("start", start * VideoEditorPipeline.GST_MILLI_SECOND);
    filesource.set("duration", (mediaStop - mediaStart) * VideoEditorPipeline.GST_MILLI_SECOND);
    filesource.set("media-start", mediaStart * VideoEditorPipeline.GST_MILLI_SECOND);
    filesource.set("media-stop", mediaStop * VideoEditorPipeline.GST_MILLI_SECOND);
    
    long length = start + (mediaStop - mediaStart);
    if (length > maxLength) {
      maxLength = length;
      defaultSource.set("start", 0L);
      defaultSource.set("duration", length * VideoEditorPipeline.GST_MILLI_SECOND);
    }
  }
  
  public Pad getSrcPad() {
    return converter.getSrcPads().get(0);
  }
  
  public Element getSinkElement() {
    return converter;
  }
  
  public Bin getBin() {
    return bin;
  }
  
  public SourceType getType() {
    return type;
  }
  
  public long getLengthMillisecond() {
    return maxLength;
  }
}
