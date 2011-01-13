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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.AudioInputStream;

public class JobManager {

  /** the logging facility provided by log4j */
  private static Logger logger = LoggerFactory.getLogger(JobManager.class.getName());

  /** Contains a queue with the available speech recognizers */
  private BlockingQueue<SpeechRecognizer> availableRecognizers = new LinkedBlockingQueue<SpeechRecognizer>();

  /** Latch that enables waiting for all threads to be finished */
  private CountDownLatch countDownLatch;

  /** Number of Jobs */
  private int numJobs = 0;

  /** Sequence number to sort results according to their occurrence */
  private int sequenceNumber = 0;

  /** Set to store the results */
  private SortedSet<SpeechResult> results = Collections.synchronizedSortedSet(new TreeSet<SpeechResult>());

  /**
   * Initializes the JobManager
   * 
   * @param numJobs
   *          Number of Jobs
   */
  public JobManager(int numJobs) {
    this.numJobs = numJobs;
    countDownLatch = new CountDownLatch(numJobs);
  }

  /**
   * Adds {@link SpeechRecognizer} to the queue with available recognizers
   * 
   * @param sr
   *          {@link SpeechRecognizer}
   */
  public void addSpeechRecognizer(SpeechRecognizer sr) {
    availableRecognizers.add(sr);
  }

  /**
   * Starts a new thread ( {@link JobThread} ) by taking a free {@link SpeechRecognizer} <br/>
   * This method is blocking.
   * 
   * @param ais
   */
  public void doJob(AudioInputStream ais) {
    try {
      SpeechRecognizer sr = availableRecognizers.take(); // blocking
      Thread thread = new JobThread(sr, ais, sequenceNumber++);
      thread.start();

    } catch (InterruptedException e) {
      // should not come here
      logger.warn("There was a problem with one job", e);
    } catch (IOException e) {
      // should not come here
      logger.warn("There was a problem with one job", e);
    }
  }

  /**
   * @return The list with the speech result sorted acoording to their occurrence
   */
  public SortedSet<SpeechResult> getResults() {
    return results;
  }

  /**
   * Waits until all are finshed (blocking)
   */
  public void waitFor() {
    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      logger.error("InterruptedException (should not come to here)", e);
    }
  }

  /**
   * Thread that starts the sphinx4 {@link SpeechRecognizer} which can be remote or local.
   */
  private class JobThread extends Thread {

    /** The speech recognizer */
    private SpeechRecognizer sr;

    /** The sequence number of the audio part */
    private int sequenceNr;

    /** The audio stream in binary to apply speech recognition */
    private byte[] binaryData;

    /** The audio stream's audio format */
    private SerializableAudioFormat audioFormat;

    /**
     * @param sr
     *          The speech recognizer that uses the {@link AudioInputStream}
     * @param ais
     *          The audio data
     * @param sequenceNr
     *          The sequence number of the audio part
     * @throws IOException
     */
    public JobThread(SpeechRecognizer sr, AudioInputStream ais, int sequenceNr) throws IOException {
      this.sr = sr;
      this.sequenceNr = sequenceNr;

      binaryData = AudioToolkit.streamToByteArray(ais);
      audioFormat = new SerializableAudioFormat(ais.getFormat());
    }

    /**
     * @see java.lang.Thread#run()
     */
    public void run() {
      boolean error;
      do {
        try {
          error = false;
          tryRun();
        } catch (IOException e) {
          logger.error("IOException", e);
          error = true;
        }

        // if error get new recognizer
        if (error) {
          try {
            sr = availableRecognizers.take();
          } catch (InterruptedException e) {
            logger.error("InterruptedException", e);
          }
        }

      } while (error);
    }

    /**
     * @throws IOException
     * @throws RemoteException
     */
    private void tryRun() throws IOException, RemoteException {
      SpeechResult result = sr.doRecognize(binaryData, audioFormat);
      result.setSequenceNumber(sequenceNr);
      result.setDuration(AudioToolkit.getDuration(binaryData.length, audioFormat.getAudioFormat()));
      logger.debug("[" + (sequenceNr + 1) + "/" + numJobs + "] " + result + " ("
              + (long) (result.getDuration() / 10E5) + " [ms])");
      //
      // store result
      results.add(result);
      //
      // offer service again
      availableRecognizers.offer(sr);
      countDownLatch.countDown();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      return obj.hashCode() == hashCode();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      return sequenceNr;
    }
  }

}
