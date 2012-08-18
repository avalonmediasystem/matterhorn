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
import org.gstreamer.Gst;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wsmirnow
 */
public class VideoEditorPipelineTest {
  
  public static final String AUDIO_TEST_FILE_PATH = "target/dependency/audio-1.0.mp3";
  public static final String CAMERA_TEST_FILE_PATH = "target/dependency/camera-1.0.mpg";
  public static final int WAIT_SEC = 3;
  
  /** The logging instance */
  private static final Logger logger = LoggerFactory.getLogger(VideoEditorPipelineTest.class);
  
  @BeforeClass
  public static void setUpClass() throws Exception {
    Gst.init();
    if (!new File(AUDIO_TEST_FILE_PATH).exists()) {
      throw new Exception("Audio testfile is not exist!");
    }
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
//    Gst.deinit();
  }
  
  @Before
  public void setUp() {
    
  }
  
  @After
  public void tearDown() {
    
  }

  /**
   * Test of run and stop methods, of class VideoEditorPipeline.
   */
  @Test
  @Ignore
  public void testRunStop() {
    VideoEditorPipeline pipeline = new VideoEditorPipeline(null);
    //pipeline.addAudioSource();
    pipeline.addListener();
    pipeline.run();
    
    try {
      Thread.sleep(WAIT_SEC * 1000);
    } catch (InterruptedException e) { }
    
//    String state = pipeline.getState();
//    boolean stopOK = pipeline.stop();
//    Assert.assertEquals("Pipeline does not start after " + WAIT_SEC + " seconds!", "playing", state);
//    Assert.assertTrue("Pipeline was force stopped!", stopOK);
//    state = pipeline.getState();
//    Assert.assertEquals("Pipeline does not stop!", "null", state);
  }
}
