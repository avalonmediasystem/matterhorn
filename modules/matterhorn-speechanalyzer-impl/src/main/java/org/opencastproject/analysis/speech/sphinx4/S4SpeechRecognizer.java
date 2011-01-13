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
import org.opencastproject.analysis.speech.ProcessObservable;
import org.opencastproject.analysis.speech.SerializableAudioFormat;
import org.opencastproject.analysis.speech.SpeechRecognizer;
import org.opencastproject.analysis.speech.SpeechResult;

import edu.cmu.sphinx.frontend.util.StreamDataSource;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.ConfidenceResult;
import edu.cmu.sphinx.result.ConfidenceScorer;
import edu.cmu.sphinx.result.Path;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat.Encoding;

/**
 * Implements speech recognition using Sphinx 4.
 */
public class S4SpeechRecognizer implements SpeechRecognizer {

  /** To serialize over the network */
  private static final long serialVersionUID = 109509280939227402L;

  /** the logging facility provided by log4j */
  private static final Logger logger = LoggerFactory.getLogger(S4SpeechRecognizer.class.getName());

  /** Enables process progress inspection */
  private ProcessObservable observable = new ProcessObservable();

  /** Converts/Analyzes the speech result */
  private ResultAnalizerSimple analizer = new ResultAnalizerSimple();

  /** The sphinx4 speech recognizer */
  private Recognizer recognizer = null;

  /** The sphinx4 configuration manager to override properties */
  private ConfigurationManager configManager = null;

  /**
   * @param config
   *          URL to the sphinx4 config File
   * @param languageModel
   *          File to the language model
   * @throws MediaAnalysisException
   */
  public S4SpeechRecognizer(URL config, URL languageModel) throws MediaAnalysisException {
    initRecognizer(config, languageModel);
  }

  /**
   * Initializes the sphinx4 recognizer.<br/>
   * In particular it overrides the location of the language model
   * 
   * @param config
   * @param languageModel
   * @throws MediaAnalysisException
   */
  private synchronized void initRecognizer(URL config, URL languageModel) throws MediaAnalysisException {
    try {

      logger.debug("Initializing speech recognizer with {}", config);
      configManager = new ConfigurationManager(config);

      // Programmatically sets the language model
      /*
      if (languageModel != null) {
        Model ltm = (Model)configManager.lookup("tidigitsModel");
        PropertySheet ps = configManager.getPropertySheet("tidigitsModelLoader");
        ps.setRaw(Model.PROP_LOCATION, languageModel.toExternalForm());
        ltm.newProperties(ps);
      }
      */

      // Load the recognizer
      logger.debug("Loading recognizer");
      observable.setStatus("loading recognizer");
      observable.setStatusProgressUndefined();
      recognizer = (Recognizer) configManager.lookup("recognizer");
      recognizer.allocate();
      logger.debug("Recognizer loaded");
      observable.setStatus("loaded recognizer");

    } catch (IOException e) {
      logger.error("IOException", e);
      throw new MediaAnalysisException("IOException: " + e.getMessage());
    } catch (PropertyException e) {
      logger.error("PropertyException", e);
      throw new MediaAnalysisException("PropertyException: " + e.getMessage());
    } catch (InstantiationException e) {
      logger.error("InstantiationException", e);
      throw new MediaAnalysisException("InstantiationException: " + e.getMessage());
    } catch (Exception e) {
      logger.error("Exception", e);
      throw new MediaAnalysisException(e.getClass().getSimpleName() + ": " + e.getMessage());
    }
  }

  /**
   * Applies speech recognition. <br/>
   * Overrides the audio stream properties of the configuration file via {@link ConfigurationManager}
   * 
   * @see ch.ethz.replay.core.indexer.plugin.asr.SpeechRecognizer#doRecognize(byte[],
   *      ch.ethz.replay.core.indexer.plugin.asr.SerializableAudioFormat)
   */
  public synchronized SpeechResult doRecognize(byte[] bytes, SerializableAudioFormat audioFormat)
          throws MediaAnalysisException {
    try {
      logger.debug("recognize: received " + bytes.length + " bytes");
      logger.trace("format: [" + audioFormat + "]");
      AudioFormat format = audioFormat.getAudioFormat();
      AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(bytes), format, bytes.length);
      StreamDataSource reader = (StreamDataSource) configManager.lookup("streamDataSource");
      PropertySheet ps = configManager.getPropertySheet("streamDataSource");
      ps.setRaw(StreamDataSource.PROP_SAMPLE_RATE, String.valueOf((int) format.getSampleRate()));
      ps.setRaw(StreamDataSource.PROP_BITS_PER_SAMPLE, String.valueOf(format.getSampleSizeInBits()));
      ps.setRaw(StreamDataSource.PROP_BIG_ENDIAN_DATA, String.valueOf(format.isBigEndian()));
      ps.setRaw(StreamDataSource.PROP_SIGNED_DATA, String.valueOf(format.getEncoding().equals(Encoding.PCM_SIGNED)));
      reader.newProperties(ps);
      if (format.getEncoding().equals(Encoding.ULAW) || format.getEncoding().equals(Encoding.ALAW)) {
        throw new MediaAnalysisException("unsupported encoding: " + format.getEncoding());
      }
      //
      // Speech recognition
      observable.setStatus("speech recognizing...");
      observable.setStatusProgress(0);

      reader.setInputStream(ais, ais.toString());
      // while (recognizer.recognize() != null)
      // ;
      //
      // get confidence and process result
      Result res = recognizer.recognize();
      SpeechResult sr = analizer.processResult(res);
      try {
        ConfidenceScorer cs = (ConfidenceScorer) configManager.lookup("confidenceScorer");
        ConfidenceResult cr = cs.score(res);
        Path best = cr.getBestHypothesis();
        sr.setConfidence(best.getLogMath().logToLinear((float) best.getConfidence()));

      } catch (Throwable e) {
        logger.warn("error setting confidence: " + e.getMessage() + " in " + sr);
      }
      logger.debug("recognized: " + sr);
      return sr;

    } catch (PropertyException e) {
      logger.error("Sphinx4 PropertyException", e);
      throw new MediaAnalysisException("Sphinx4 PropertyException: " + e.getMessage());

    } catch (InstantiationException e) {
      logger.error("Sphinx4 InstantiationException", e);
      throw new MediaAnalysisException("Sphinx4 InstantiationException: " + e.getMessage());

    }
  }

}
