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

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wsmirnow
 */
public class GstreamerTypeFinderTest extends GstreamerAbstractTest {
  
  /**
   * The logging instance
   */
  private static final Logger logger = LoggerFactory.getLogger(GstreamerTypeFinderTest.class);
  
  @Test
  public void typefinderAudioTest() {
    GstreamerTypeFinder typeFinder = new GstreamerTypeFinder(audioFilePath);
    Assert.assertTrue(typeFinder.isAudioFile());
    Assert.assertFalse(typeFinder.isVideoFile());
    
    logger.info("audiocaps: " + typeFinder.getAudioCaps().toString());
  }
  
  @Test
  public void typefinderVideoTest() {
    GstreamerTypeFinder typeFinder = new GstreamerTypeFinder(videoFilePath);
    Assert.assertTrue(typeFinder.isVideoFile());
    Assert.assertFalse(typeFinder.isAudioFile());
    
    logger.info("videocaps: " + typeFinder.getVideoCaps().toString());
  }
  
  @Test
  public void typefinderMuxedTest() {
    GstreamerTypeFinder typeFinder = new GstreamerTypeFinder(muxedFilePath);
    Assert.assertTrue(typeFinder.isAudioFile());
    Assert.assertTrue(typeFinder.isVideoFile());
    
    logger.info("audiocaps: " + typeFinder.getAudioCaps().toString());
    logger.info("videocaps: " + typeFinder.getVideoCaps().toString());
  }
  
  @Test
  public void typefinderFailTest() {
    GstreamerTypeFinder typeFinder = new GstreamerTypeFinder("foo");
    Assert.assertFalse(typeFinder.isAudioFile());
    Assert.assertFalse(typeFinder.isVideoFile());
  }
}
