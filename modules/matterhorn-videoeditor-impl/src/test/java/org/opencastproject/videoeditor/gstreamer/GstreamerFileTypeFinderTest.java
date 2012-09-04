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

import java.io.FileNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.opencastproject.videoeditor.gstreamer.exceptions.PipelineBuildException;

/**
 *
 * @author wsmirnow
 */
public class GstreamerFileTypeFinderTest extends GstreamerAbstractTest {
  
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
      typeFinder = new GstreamerFileTypeFinder(audioFilePath);
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
      typeFinder = new GstreamerFileTypeFinder(audioFilePath);
      Assert.assertTrue("GstreamerTypeFinder should find audio caps!", typeFinder.isAudioFile());
      Assert.assertFalse("GstreamerTypeFinder should not find video caps!", typeFinder.isVideoFile());
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
      typeFinder = new GstreamerFileTypeFinder(videoFilePath);
      Assert.assertFalse("GstreamerTypeFinder should not find audio caps!", typeFinder.isAudioFile());
      Assert.assertTrue("GstreamerTypeFinder should find video caps!", typeFinder.isVideoFile());
    } catch (FileNotFoundException ex) {
      Assert.fail("GstreamerFileTypeFinder should not throw a FileNotFoundException");
    } catch (PipelineBuildException ex) {
      Assert.fail("GstreamerFileTypeFinder should not throw a PipelineBuildException");
    }
    typeFinder = null;
  }
  
  @Test
  public void testIsMuxedFile() {
    
    GstreamerFileTypeFinder typeFinder;
    
    try {
      typeFinder = new GstreamerFileTypeFinder(muxedFilePath);
      Assert.assertTrue("GstreamerTypeFinder should find audio caps!", typeFinder.isAudioFile());
      Assert.assertTrue("GstreamerTypeFinder should find video caps!", typeFinder.isVideoFile());
    } catch (FileNotFoundException ex) {
      Assert.fail("GstreamerFileTypeFinder should not throw a FileNotFoundException");
    } catch (PipelineBuildException ex) {
      Assert.fail("GstreamerFileTypeFinder should not throw a PipelineBuildException");
    }
    typeFinder = null;
  }
}
