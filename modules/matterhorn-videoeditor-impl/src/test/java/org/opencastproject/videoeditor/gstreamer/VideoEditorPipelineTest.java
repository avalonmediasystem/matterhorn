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
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import junit.framework.Assert;
import org.gstreamer.State;
import org.junit.Ignore;
import org.junit.Test;
import org.opencastproject.videoeditor.gstreamer.exceptions.PipelineBuildException;
import org.opencastproject.videoeditor.gstreamer.exceptions.UnknownSourceTypeException;
import org.opencastproject.videoeditor.gstreamer.sources.SourceBinsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wsmirnow
 */
public class VideoEditorPipelineTest extends GstreamerAbstractTest {
  
  /** The logging instance */
  private static final Logger logger = LoggerFactory.getLogger(VideoEditorPipelineTest.class);
  
//  private String muxedFilePath = "/home/wsmirnow/Videos/Sintel.mp4";
  
  /**
   * Test of run and stop methods, of class VideoEditorPipeline.
   */
  @Test
  public void testRunStopDemuxedSourceFiles() {
    try {
      final VideoEditorPipeline pipeline = new VideoEditorPipeline(new Properties());
      SourceBinsFactory sourceBins = new SourceBinsFactory(new File(outputFilePath).getAbsolutePath());
      sourceBins.addFileSource(new File(audioFilePath).getAbsolutePath(), 
              TimeUnit.SECONDS.toMillis(0), TimeUnit.SECONDS.toMillis(10));
      sourceBins.addFileSource(new File(audioFilePath).getAbsolutePath(), 
              TimeUnit.SECONDS.toMillis(60), TimeUnit.SECONDS.toMillis(10));
      
      sourceBins.addFileSource(new File(videoFilePath).getAbsolutePath(), 
              TimeUnit.SECONDS.toMillis(0), TimeUnit.SECONDS.toMillis(10));
      sourceBins.addFileSource(new File(videoFilePath).getAbsolutePath(), 
              TimeUnit.SECONDS.toMillis(60), TimeUnit.SECONDS.toMillis(10));
      
      pipeline.addSourceBinsAndCreatePipeline(sourceBins);
      pipeline.addListener();
      
      Thread runner = new Thread(new Runnable() {

        @Override
        public void run() {
          pipeline.mainLoop();
        }
      });
      
      runner.start();
      pipeline.run();
      
      try {
        Thread.sleep(TimeUnit.SECONDS.toMillis(WAIT_SEC));
        Assert.assertEquals(State.PLAYING, pipeline.getState(TimeUnit.SECONDS.toMillis(WAIT_SEC)));
        runner.join();
      } catch (InterruptedException ex) {
        logger.warn("Test interrupted!");
      } finally {
        Assert.assertTrue(pipeline.stop());
      }
      
      
    } catch (FileNotFoundException ex) {
      Assert.fail();
    } catch (PipelineBuildException ex) {
      Assert.fail();
    } catch (UnknownSourceTypeException ex) {
      Assert.fail();
    }
  }
  
  /**
   * Test of run and stop methods, of class VideoEditorPipeline.
   */
  @Ignore
  @Test
  public void testRunStopMuxedSourceFile() {
    try {
      final VideoEditorPipeline pipeline = new VideoEditorPipeline(new Properties());
      SourceBinsFactory sourceBins = new SourceBinsFactory(new File(outputFilePath).getAbsolutePath());
      sourceBins.addFileSource(new File(muxedFilePath).getAbsolutePath(), 
              TimeUnit.SECONDS.toMillis(0), TimeUnit.SECONDS.toMillis(10));
      sourceBins.addFileSource(new File(muxedFilePath).getAbsolutePath(), 
              TimeUnit.SECONDS.toMillis(122), TimeUnit.SECONDS.toMillis(10));
      
      pipeline.addSourceBinsAndCreatePipeline(sourceBins);
      pipeline.addListener();
      
      Thread runner = new Thread(new Runnable() {

        @Override
        public void run() {
          pipeline.mainLoop();
        }
      });
      
      runner.start();
      pipeline.run();
      
      try {
        Thread.sleep(TimeUnit.SECONDS.toMillis(WAIT_SEC));
        Assert.assertEquals(State.PLAYING, pipeline.getState(TimeUnit.SECONDS.toMillis(WAIT_SEC)));
        runner.join();
      } catch (InterruptedException ex) {
        logger.warn("Test interrupted!");
      } finally {
        Assert.assertTrue(pipeline.stop());
      }
      
      
    } catch (FileNotFoundException ex) {
      Assert.fail();
    } catch (PipelineBuildException ex) {
      Assert.fail();
    } catch (UnknownSourceTypeException ex) {
      Assert.fail();
    }
  }
}
