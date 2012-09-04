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
import junit.framework.Assert;
import org.gstreamer.Bin;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.junit.Test;
import org.opencastproject.videoeditor.gstreamer.GstreamerAbstractTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wsmirnow
 */
public class FileSourceBinTest extends GstreamerAbstractTest {
  
  /** The logging instance */
  private static final Logger logger = LoggerFactory.getLogger(FileSourceBinTest.class);
  
  /**
   * Test functionality of FileSourceBin class.
   */
  @Test
  public void testFileSourceBin() {
    
    Pipeline pipeline = new Pipeline();
    
    FileSourceBin audioFileSourceBin = new FileSourceBin(audioFilePath, Caps.fromString("audio/x-raw-int; audio/x-raw-float"));
    Assert.assertNotNull(audioFileSourceBin);
    Bin audioBin = audioFileSourceBin.getBin();
    Assert.assertNotNull(audioBin);
    audioBin.setName("audioBin");
    
    Element audioSink = ElementFactory.make("fakesink", null);
    pipeline.addMany(audioBin, audioSink);
    Assert.assertTrue(audioBin.link(audioSink));
    audioSink.set("sync", true);
    
    FileSourceBin videoFileSourceBin = new FileSourceBin(videoFilePath, Caps.fromString("video/x-raw-yuv; video/x-raw-rgb"));
    Assert.assertNotNull(videoFileSourceBin);
    Bin videoBin = videoFileSourceBin.getBin();
    Assert.assertNotNull(videoBin);
    videoBin.setName("videoBin");
    
    Element videoSink = ElementFactory.make("fakesink", null);
    pipeline.addMany(videoBin, videoSink);
    Assert.assertTrue(videoBin.link(videoSink));
    videoSink.set("sync", true);
    
    pipeline.play();
    logger.info("Wait {} sec to test pipeline state is running...", Long.toString(WAIT_SEC));
    try {
      Thread.sleep(TimeUnit.SECONDS.toMillis(WAIT_SEC));
    } catch (InterruptedException ex) {
      logger.warn("Test interrupted!");
    }
    
    State state = pipeline.getState(TimeUnit.SECONDS.toNanos(WAIT_SEC));
        
    pipeline.setState(State.NULL);
    pipeline = null;
    audioFileSourceBin = null;
    
    Assert.assertEquals(State.PLAYING, state);
  }
}
