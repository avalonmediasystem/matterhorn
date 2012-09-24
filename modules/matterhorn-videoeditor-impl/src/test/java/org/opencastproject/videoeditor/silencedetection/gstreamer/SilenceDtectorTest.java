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
package org.opencastproject.videoeditor.silencedetection.gstreamer;

import java.util.Properties;
import org.gstreamer.ClockTime;
import org.junit.Assert;
import org.junit.Test;
import org.opencastproject.videoeditor.api.ProcessFailedException;
import org.opencastproject.videoeditor.gstreamer.GstreamerAbstractTest;
import org.opencastproject.videoeditor.gstreamer.exceptions.PipelineBuildException;
import org.opencastproject.videoeditor.silencedetection.api.MediaSegment;
import org.opencastproject.videoeditor.silencedetection.api.MediaSegments;
import org.opencastproject.videoeditor.silencedetection.impl.SilenceDetectionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wsmirnow
 */
public class SilenceDtectorTest extends GstreamerAbstractTest {
  
  private static final Logger logger = LoggerFactory.getLogger(SilenceDtectorTest.class);
  
  @Test
  public void detectorTest() {
    logger.info("segmenting audio file '{}'...", audioFilePath);
    try {
      GstreamerSilenceDetector silenceDetector = new GstreamerSilenceDetector(new Properties(), "track-1", audioFilePath);
      Assert.assertNull(silenceDetector.getMediaSegments());
      
      silenceDetector.detect();
      
      MediaSegments segments = silenceDetector.getMediaSegments();
      Assert.assertNotNull(segments);
      Assert.assertTrue(segments.getMediaSegments().size() > 0);
      
      logger.info("segments found:");
      for (MediaSegment segment : segments.getMediaSegments()) {
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
    properties.setProperty(SilenceDetectionProperties.SILENCE_MIN_LENGTH, "30");
//    properties.setProperty(VideoEditorProperties.SILENCE_THRESHOLD_DB, "-75");
    
    try {
      GstreamerSilenceDetector silenceDetector = new GstreamerSilenceDetector(properties, "track-1", audioFilePath);
      Assert.assertNull(silenceDetector.getMediaSegments());
      
      silenceDetector.detect();
      
      MediaSegments segments = silenceDetector.getMediaSegments();
      Assert.assertNotNull(segments);
      Assert.assertTrue(segments.getMediaSegments().size() == 1);
      
      MediaSegment segment = segments.getMediaSegments().get(0);
      Assert.assertTrue(segment.getSegmentStart() < segment.getSegmentStop());
      logger.info("segments found:");
      logger.info("{} ({}) - {} ({})", new String[] {
        ClockTime.fromMillis(segment.getSegmentStart()).toString(),
        Long.toString(segment.getSegmentStart()),
        ClockTime.fromMillis(segment.getSegmentStop()).toString(),
        Long.toString(segment.getSegmentStop())
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
      GstreamerSilenceDetector silenceDetector = new GstreamerSilenceDetector(new Properties(), "track-1", videoFilePath);
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
