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
package org.opencastproject.analysis.speech.sphinx4;

import org.opencastproject.analysis.api.MediaAnalysisException;
import org.opencastproject.analysis.speech.SerializableAudioFormat;
import org.opencastproject.analysis.speech.SpeechRecognizer;
import org.opencastproject.analysis.speech.SpeechResult;

import edu.cmu.sphinx.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Wrapper for {@link S4SpeechRecognizer} that loads the vocabulary in a separate thread to overcome a problem with the
 * {@link Timer}.
 */
public class S4SpeechRecognizerThreaded implements SpeechRecognizer {

  /** the logging facility provided by log4j */
  private static final Logger logger = LoggerFactory.getLogger(S4SpeechRecognizerThreaded.class.getName());

  /** the ASR provider */
  private SpeechRecognizer recognizer = null;

  /**
   * @param configURL
   *          URL to the sphinx4 config File
   * @param languageModel
   *          path to the language model (*.DMP)
   * @throws MediaAnalysisException
   */
  public S4SpeechRecognizerThreaded(URL configURL, URL languageModel) throws MediaAnalysisException {
    newRecognizer(configURL, languageModel);
  }

  /**
   * forwards the request to the {@link SpeechRecognizer}
   * 
   * @see ch.ethz.replay.core.indexer.plugin.asr.SpeechRecognizer#doRecognize(byte[],
   *      ch.ethz.replay.core.indexer.plugin.asr.SerializableAudioFormat)
   */
  public SpeechResult doRecognize(byte[] speechData, SerializableAudioFormat audioFormat) throws MediaAnalysisException {
    return recognizer.doRecognize(speechData, audioFormat);
  }

  /**
   * Starts the recognizer in a separate thread to overcome threading problems discussed in <a href
   * ="https://sourceforge.net/forum/message.php?msg_id=4989028"> here</a>
   * 
   * @param config
   * @param languageModel
   */
  private void newRecognizer(final URL config, final URL languageModel) {
    Thread thread = new Thread(new Runnable() {

      public void run() {
        try {
          recognizer = new S4SpeechRecognizer(config, languageModel);
        } catch (MediaAnalysisException e) {
          logger.error("Problems loading " + S4SpeechRecognizer.class.getName(), e);
        }
      }
    });
    thread.start();
    try {
      thread.join();
    } catch (InterruptedException e) {
      new MediaAnalysisException(e.getMessage());
    }
  }

}
