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
import org.gstreamer.Gst;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.opencastproject.videoeditor.gstreamer.exceptions.PipelineBuildException;

/**
 *
 * @author wsmirnow
 */
public class GstreamerFileTypeFinderTest {
  
  public static final String AUDIO_TEST_FILE_PATH = "target/dependency/audio-1.0.mp3";
  public static final String CAMERA_TEST_FILE_PATH = "target/dependency/camera-1.0.mpg";
  
  @BeforeClass
  public static void setUpClass() throws Exception {
    if (!new File(AUDIO_TEST_FILE_PATH).exists()) {
      throw new Exception("Audio testfile is not exist!");
    }
    if (!new File(CAMERA_TEST_FILE_PATH).exists()) {
      throw new Exception("Video testfile is not exist!");
    }
    Gst.init();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }
  
  @Before
  public void setUp() {
  }
  
  @After
  public void tearDown() {
  }
  
  @Test
  public void testGstreamerFileTypeFinder() {
  
    GstreamerFileTypeFinder typeFinder;
    
    try {
      typeFinder = new GstreamerFileTypeFinder(".");
      Assert.fail("GstreamerFileTypeFinder should throw an FileNotFoundException");
    }  catch (FileNotFoundException ex) {
      // pass
    } catch (PipelineBuildException ex) {
      Assert.fail("GstreamerFileTypeFinder should throw an FileNotFoundException");
    }
    
    typeFinder = null;
    
    try {
      typeFinder = new GstreamerFileTypeFinder("foo");
      Assert.fail("GstreamerFileTypeFinder should throw a FileNotFoundException");
    } catch (FileNotFoundException ex) {
      // pass
    } catch (PipelineBuildException ex) {
      Assert.fail("GstreamerFileTypeFinder should throw a FileNotFoundException");
    }
    
    typeFinder = null;
    
    try {
      typeFinder = new GstreamerFileTypeFinder(AUDIO_TEST_FILE_PATH);
      // pass
    } catch (FileNotFoundException ex) {
      Assert.fail("GstreamerFileTypeFinder should not throw a FileNotFoundException");
    } catch (PipelineBuildException ex) {
      Assert.fail("GstreamerFileTypeFinder should not throw a PipelineBuildException");
    }
    
    typeFinder = null;
  }

  @Test
  public void testIsAudioFile() {
    
    GstreamerFileTypeFinder typeFinder;
    
    try {
      typeFinder = new GstreamerFileTypeFinder(AUDIO_TEST_FILE_PATH);
      Assert.assertTrue("streamerTypeFinder should find audio caps!", typeFinder.isAudioFile());
      Assert.assertFalse("streamerTypeFinder should not find video caps!", typeFinder.isVideoFile());
    } catch (FileNotFoundException ex) {
      Assert.fail("GstreamerFileTypeFinder should not throw a FileNotFoundException");
    } catch (PipelineBuildException ex) {
      Assert.fail("GstreamerFileTypeFinder should not throw a PipelineBuildException");
    }
    typeFinder = null;
  }
  
  @Test
  public void testIsVideoFile() {
    
    GstreamerFileTypeFinder typeFinder;
    
    try {
      typeFinder = new GstreamerFileTypeFinder(CAMERA_TEST_FILE_PATH);
      Assert.assertFalse("streamerTypeFinder should not find audio caps!", typeFinder.isAudioFile());
      Assert.assertTrue("streamerTypeFinder should find video caps!", typeFinder.isVideoFile());
    } catch (FileNotFoundException ex) {
      Assert.fail("GstreamerFileTypeFinder should not throw a FileNotFoundException");
    } catch (PipelineBuildException ex) {
      Assert.fail("GstreamerFileTypeFinder should not throw a PipelineBuildException");
    }
    typeFinder = null;
  }
}
