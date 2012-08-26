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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.gstreamer.Bus;
import org.gstreamer.Element;
import org.gstreamer.Gst;
import org.gstreamer.GstObject;
import org.gstreamer.Pad;
import org.gstreamer.PadDirection;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.lowlevel.MainLoop;
import org.opencastproject.videoeditor.gstreamer.exceptions.PipelineBuildException;

/**
 * Find the file media type (audio, image, video).
 */
public class GstreamerFileTypeFinder {
  
  /**
   * Type find gstreamer pipeline
   */
  private Pipeline pipeline;
  
  /**
   * List with all found caps
   */
  private List<String> caps = Collections.synchronizedList(new LinkedList<String>());
  
  private final MainLoop mainLoop = new MainLoop();
  
  /**
   * Find the file media type
   * 
   * @param filePath absolute path to the media file
   * @throws FileNotFoundException thrown if given file not exist or is not a regular file
   * @throws PipelineBuildException thrown if pipeline can't build
   */
  public GstreamerFileTypeFinder(String filePath) 
          throws FileNotFoundException, PipelineBuildException {
    
    
    File sourceFile = new File(filePath);
    if (!sourceFile.exists() || !sourceFile.isFile())
      throw new FileNotFoundException();
    
    Gst.setUseDefaultContext(true);
    Gst.init();
    
    pipeline = Pipeline.launch(String.format(
            GstreamerElements.FILESRC + " location=\"%s\" ! "
            + GstreamerElements.DECODEBIN + " name=dec", 
            sourceFile.getAbsolutePath()));
    
    Element decodebin = pipeline.getElementByName("dec");
    if (decodebin == null) 
      throw new PipelineBuildException();
    
    decodebin.connect(new Element.PAD_ADDED() {

      public void padAdded(Element element, Pad pad) {
        if (pad.getDirection() == PadDirection.SRC) {
          caps.add(pad.getCaps().toString());
        }
      }
    });
    
    decodebin.connect(new Element.NO_MORE_PADS() {

      public void noMorePads(Element element) {
//        Gst.quit();
        mainLoop.quit();
      }
    });
    
    pipeline.getBus().connect(new Bus.ERROR() {

      @Override
      public void errorMessage(GstObject source, int code, String message) {
//        Gst.quit();
        mainLoop.quit();
      }
    });
    
    pipeline.getBus().connect(new Bus.EOS() {

      @Override
      public void endOfStream(GstObject source) {
//        Gst.quit();
        mainLoop.quit();
      }
    });
    
    pipeline.getBus().connect(new Bus.STATE_CHANGED() {

      public void stateChanged(GstObject source, State old, State current, State pending) {
//        System.out.println(String.format("source: %s, old: %s, current: %s, pending: %s", source, old, current, pending));
        if (source == pipeline && current == State.PLAYING) {
//          Gst.quit();
          mainLoop.quit();
        }
      }
    });
    
    pipeline.play();
    
//    Gst.main();
    mainLoop.run();
    
    pipeline.stop();
    pipeline = null;
    
//    Gst.deinit();
  }

  /**
   * Returns true if mediafile has a video stream.
   * @return true if mediafile has a audio stream
   */
  public boolean isAudioFile() {
    for (String c : caps) {
      if (c.startsWith("audio/"))
        return true;
    }
    return false;
  }
  
  /**
   * Returns true if mediafile is an image.
   * @return true if mediafile is an image
   */
  public boolean isImageFile() {
    for (String c : caps) {
      if (c.startsWith("image/"))
        return true;
    }
    return false;
  }
  
  /**
   * Returns true if mediafile has a video stream.
   * @return true if mediafile has a video stream.
   */
  public boolean isVideoFile() {
    for (String c : caps) {
      if (c.startsWith("video/"))
        return true;
    }
    return false;
  }
}
