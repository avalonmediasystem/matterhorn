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
import java.util.concurrent.TimeUnit;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencastproject.videoeditor.gstreamer.exceptions.CanNotAddElementException;
import org.opencastproject.videoeditor.gstreamer.exceptions.PipelineBuildException;
import org.opencastproject.videoeditor.gstreamer.exceptions.UnknownSourceTypeException;
import org.opencastproject.videoeditor.impl.FileSourceBins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wsmirnow
 */
public class VideoEditorPipelineTest {
  
  public static final String AUDIO_TEST_FILE_PATH = "target/dependency/audio-1.0.mp3";
  public static final String VIDEO_TEST_FILE_PATH = "target/dependency/screen-1.0.mpg";
  
  public static final String AUDIO_OUTPUT_PATH = "target/testoutput/audio.mpg";
  public static final String VIDEO_OUTPUT_PATH = "target/testoutput/screen.mpg";
  public static final String MUX_OUTPUT_PATH = "target/testoutput/mux.mpg";
  
  public static final int WAIT_SEC = 3;
  
  /** The logging instance */
  private static final Logger logger = LoggerFactory.getLogger(VideoEditorPipelineTest.class);
  
  @BeforeClass
  public static void setUpClass() throws Exception {
    if (!new File(AUDIO_TEST_FILE_PATH).exists()) {
      throw new Exception("Audio testfile does not exist!");
    }
    if (!new File(VIDEO_TEST_FILE_PATH).exists()) {
      throw new Exception("Video testfile does not exist!");
    }
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }
  
  @Before
  public void setUp() {
    if (new File(AUDIO_OUTPUT_PATH).exists())  {
      new File(AUDIO_OUTPUT_PATH).delete();
    } else if (!new File(AUDIO_OUTPUT_PATH).getParentFile().exists()) {
      new File(AUDIO_OUTPUT_PATH).getParentFile().mkdir();
    }
    if (new File(VIDEO_OUTPUT_PATH).exists())  {
      new File(VIDEO_OUTPUT_PATH).delete();
    } else if (!new File(VIDEO_OUTPUT_PATH).getParentFile().exists()) {
      new File(VIDEO_OUTPUT_PATH).getParentFile().mkdir();
    }
    
    if (new File(MUX_OUTPUT_PATH).exists())  {
      new File(MUX_OUTPUT_PATH).delete();
    } else if (!new File(MUX_OUTPUT_PATH).getParentFile().exists()) {
      new File(MUX_OUTPUT_PATH).getParentFile().mkdir();
    }
  }
  
  @After
  public void tearDown() {
//    if (new File(AUDIO_OUTPUT_PATH).exists())  {
//      new File(AUDIO_OUTPUT_PATH).delete();
//    }
//    if (new File(VIDEO_OUTPUT_PATH).exists())  {
//      new File(VIDEO_OUTPUT_PATH).delete();
//    }
//    if (new File(MUX_OUTPUT_PATH).exists())  {
//      new File(MUX_OUTPUT_PATH).delete();
//    }
  }

  /**
   * Test of run and stop methods, of class VideoEditorPipeline.
   */
  @Test
  public void testRunStop() {
    try {
      VideoEditorPipeline pipeline = new VideoEditorPipeline(null);
      FileSourceBins sourceBins = new FileSourceBins(new File(MUX_OUTPUT_PATH).getAbsolutePath());
      sourceBins.addFileSource(new File(AUDIO_TEST_FILE_PATH).getAbsolutePath(), 
              TimeUnit.SECONDS.toMillis(122), TimeUnit.SECONDS.toMillis(10));
      sourceBins.addFileSource(new File(AUDIO_TEST_FILE_PATH).getAbsolutePath(), 
              TimeUnit.SECONDS.toMillis(21), TimeUnit.SECONDS.toMillis(26));
      
      sourceBins.addFileSource(new File(VIDEO_TEST_FILE_PATH).getAbsolutePath(), 
              TimeUnit.SECONDS.toMillis(122), TimeUnit.SECONDS.toMillis(10));
      sourceBins.addFileSource(new File(VIDEO_TEST_FILE_PATH).getAbsolutePath(), 
              TimeUnit.SECONDS.toMillis(21), TimeUnit.SECONDS.toMillis(26));
      
      pipeline.addSourceBinsAndCreatePipeline(sourceBins);
      pipeline.addListener();
      
      pipeline.run();
      pipeline.mainLoop();
      Assert.assertTrue(pipeline.stop());
      
    } catch (FileNotFoundException ex) {
      Assert.fail();
    } catch (PipelineBuildException ex) {
      Assert.fail();
    } catch (CanNotAddElementException ex) {
      Assert.fail();
    } catch (UnknownSourceTypeException ex) {
      Assert.fail();
    }
  }
}
