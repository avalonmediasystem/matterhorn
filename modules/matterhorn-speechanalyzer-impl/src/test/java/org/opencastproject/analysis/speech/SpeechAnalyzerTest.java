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
package org.opencastproject.analysis.speech;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.opencastproject.mediapackage.MediaPackageElementBuilder;
import org.opencastproject.mediapackage.MediaPackageElementBuilderFactory;
import org.opencastproject.mediapackage.MediaPackageElements;
import org.opencastproject.mediapackage.Track;
import org.opencastproject.mediapackage.MediaPackageElement.Type;
import org.opencastproject.metadata.mpeg7.AudioSegment;
import org.opencastproject.metadata.mpeg7.MediaTime;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Test case for the {@link SpeechAnalyzer}.
 */
public class SpeechAnalyzerTest {

  /** the logging facility provided by log4j */
  private static Logger logger = LoggerFactory.getLogger(SpeechAnalyzerTest.class.getName());

  /** Timestamps of the words found in the sample movie */
  private static long[] timeStamps = new long[] { 80, 830, 1620, 2120, 2300, 2740, 3070, 3345, 3485, 3955, 4175, 4415,
      4645, 5065, 5515, 5985, 6165, 7025, 7475, 7805, 8085, 8235, 8825, 9136, 9296, 9696, 11396, 11626, 12096,
      12276, 12436, 12656, 12776, 13036, 13196, 13416, 13716, 13876, 14266, 14416
  };

  /** Words found in the sample movie */
  private static String[] words = new String[] { "one", "heartily", "python", "can", "compress", "these", "fae", "all",
      "smaller", "than", "any", "other", "capture", "device", "allowing", "more", "practical", "everyday", "use",
      "in", "e.", "mail", "am", "the", "internet", "applications", "with", "python", "there's", "no", "need", "to",
      "open", "your", "p.", "c.", "and", "fumble", "with", "connections"
  };

  /** The analyzer */
  protected SpeechAnalyzer analyzer = null;
  
  /** The sample track */
  protected Track audioTrack = null;

  /** The sample track file */
  protected File audioTrackFile = null;

  /** The sample track uri */
  protected String sampleTrackPath = "/python.mpeg";

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    MediaPackageElementBuilder elementBuilder = MediaPackageElementBuilderFactory.newInstance().newElementBuilder();
    
    // Copy the sample movie to a temporary file
    audioTrackFile = File.createTempFile("speechtest", ".mpeg");
    IOUtils.copy(SpeechAnalyzerTest.class.getResourceAsStream(sampleTrackPath), new FileOutputStream(audioTrackFile));
    audioTrack = (Track)elementBuilder.elementFromURI(audioTrackFile.toURI(), Type.Track, MediaPackageElements.PRESENTATION_SOURCE);
    analyzer = new SpeechAnalyzer();
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    if (audioTrackFile != null) {
      FileUtils.deleteQuietly(audioTrackFile);
    }
  }

  /**
   * Tests the actual analysis.
   */
  @Test
  public void testAnalyze() {
    AudioSegment[] segments = null;
    long start = System.currentTimeMillis();
    
    // Do the analysis
    try {
      // TODO: Use the analyze(MediaPackageElement, boolean) signature
      segments = analyzer.analyze(audioTrackFile);
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error accessing sample audio track " + audioTrackFile);
    } catch (UnsupportedAudioFileException e) {
      e.printStackTrace();
      fail("Error processing sample audio track " + audioTrackFile);
    }

    logger.info("Analysis took {} ms", (System.currentTimeMillis() - start));

    // Make sure we found all the segments
    assertEquals(words.length, segments.length);
    
    // Make sure all the segments have been properly created
    for (int i = 0; i < segments.length; i++) {
      AudioSegment segment  = segments[i];
      MediaTime time = segment.getMediaTime();
      assertEquals(timeStamps[i], time.getMediaTimePoint().getTimeInMilliseconds());
      assertEquals(1, segment.getTextAnnotationCount());
      assertEquals(words[i], segment.textAnnotations().next());
    }
  }

}
