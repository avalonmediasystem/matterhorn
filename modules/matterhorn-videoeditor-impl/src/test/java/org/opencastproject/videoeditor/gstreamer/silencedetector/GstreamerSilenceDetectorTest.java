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
package org.opencastproject.videoeditor.gstreamer.silencedetector;

import java.util.List;
import java.util.Properties;
import org.gstreamer.ClockTime;
import org.junit.Assert;
import org.junit.Test;
import org.opencastproject.videoeditor.api.MediaSegment;
import org.opencastproject.videoeditor.api.ProcessFailedException;
import org.opencastproject.videoeditor.gstreamer.GstreamerAbstractTest;
import org.opencastproject.videoeditor.gstreamer.exceptions.PipelineBuildException;
import org.opencastproject.videoeditor.impl.VideoEditorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wsmirnow
 */
public class GstreamerSilenceDetectorTest extends GstreamerAbstractTest {
  
  private static final Logger logger = LoggerFactory.getLogger(GstreamerSilenceDetectorTest.class);
  
  @Test
  public void detectorTest() {
//    audioFilePath = "/home/wsmirnow/Videos/Sintel.mp4";
    logger.info("segmenting audio file '{}'...", audioFilePath);
    try {
      GstreamerSilenceDetector silenceDetector = new GstreamerSilenceDetector(new Properties(), audioFilePath);
      Assert.assertNull(silenceDetector.getMediaSegments());
      
      silenceDetector.detect();
      
      List<MediaSegment> segments = silenceDetector.getMediaSegments();
      Assert.assertNotNull(segments);
      Assert.assertTrue(segments.size() > 0);
      
      logger.info("segments found:");
      for (MediaSegment segment : segments) {
        Assert.assertTrue(segment.getSegmentStart() < segment.getSegmentStop());
        logger.info("{} ({}) - {} ({})", new String[] {
          ClockTime.fromMillis(segment.getSegmentStart()).toString(),
          Long.toString(segment.getSegmentStart()),
          ClockTime.fromMillis(segment.getSegmentStop()).toString(),
          Long.toString(segment.getSegmentStop())
        });
      }
      
    } catch (ProcessFailedException ex) {
      Assert.fail();
    } catch (PipelineBuildException ex) {
      Assert.fail();
    }
  }
  
  @Test
  public void detectorSingleSegmentTest() {
    logger.info("segmenting audio file '{}' with minimum silence length of 30 sec...", audioFilePath);
    
    Properties properties = new Properties();
    properties.setProperty(VideoEditorProperties.SILENCE_MIN_LENGTH, "30");
//    properties.setProperty(VideoEditorProperties.SILENCE_THRESHOLD_DB, "-75");
    
    try {
      GstreamerSilenceDetector silenceDetector = new GstreamerSilenceDetector(properties, audioFilePath);
      Assert.assertNull(silenceDetector.getMediaSegments());
      
      silenceDetector.detect();
      
      List<MediaSegment> segments = silenceDetector.getMediaSegments();
      Assert.assertNotNull(segments);
      Assert.assertTrue(segments.size() == 1);
      
      Assert.assertTrue(segments.get(0).getSegmentStart() < segments.get(0).getSegmentStop());
      logger.info("segments found:");
      logger.info("{} ({}) - {} ({})", new String[] {
        ClockTime.fromMillis(segments.get(0).getSegmentStart()).toString(),
        Long.toString(segments.get(0).getSegmentStart()),
        ClockTime.fromMillis(segments.get(0).getSegmentStop()).toString(),
        Long.toString(segments.get(0).getSegmentStop())
      });
            
    } catch (ProcessFailedException ex) {
      Assert.fail();
    } catch (PipelineBuildException ex) {
      Assert.fail();
    }
  }
  
  @Test
  public void detectorFailTest() {
    logger.info("segmenting video only file '{}' should fail...", videoFilePath);
    try {
      GstreamerSilenceDetector silenceDetector = new GstreamerSilenceDetector(new Properties(), videoFilePath);
      Assert.assertNull(silenceDetector.getMediaSegments());
      
      silenceDetector.detect();
      Assert.fail();
      
    } catch (ProcessFailedException ex) {
      logger.debug(ex.getMessage());
    } catch (PipelineBuildException ex) {
      Assert.fail();
    }
  }
}
