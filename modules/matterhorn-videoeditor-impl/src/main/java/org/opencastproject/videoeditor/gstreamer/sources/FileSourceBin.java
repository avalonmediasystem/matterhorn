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
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.GhostPad;
import org.gstreamer.Pad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wsmirnow
 */
class FileSourceBin {
  
  /** The logging instance */
  private static final Logger logger = LoggerFactory.getLogger(FileSourceBin.class);

  private final Bin bin;
  
  FileSourceBin(String filePath, final Caps caps) {

    bin = new Bin();
    
    final Element filesource = ElementFactory.make("filesrc", null);
    final Element decodebin = ElementFactory.make("decodebin", null);
    final Element capsfilter = ElementFactory.make("capsfilter", null);
    
    bin.addMany(filesource, decodebin, capsfilter);
    
    filesource.set("location", filePath);
    capsfilter.setCaps(caps);
    
    filesource.link(decodebin);
        
    decodebin.connect(new Element.PAD_ADDED() {

      @Override
      public void padAdded(Element element, Pad pad) {
        if (pad.acceptCaps(caps)) {
          pad.link(capsfilter.getSinkPads().get(0));
          logger.debug("pad linked {}.{} -> {}", new String[] {
            element.getName(), pad.getName(), capsfilter.getName()
          });
        } else {
          Element fakesink = ElementFactory.make("fakesink", null);
          bin.add(fakesink);
          pad.link(fakesink.getSinkPads().get(0));
          fakesink.syncStateWithParent();
          logger.debug("pad linked {}.{} -> {}", new String[] {
            element.getName(), pad.getName(), fakesink.getName()
          });
        }
      }
    });
    
    Pad srcPad = capsfilter.getSrcPads().get(0);
    GhostPad ghostSrcPad = new GhostPad(srcPad.getName(), srcPad);
    bin.addPad(ghostSrcPad);
  }
  
  Bin getBin() {
    return bin;
  }
}
