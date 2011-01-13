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

import org.opencastproject.analysis.speech.SpeechResult;
import org.opencastproject.analysis.speech.SpeechResultImpl;
import org.opencastproject.analysis.speech.SpeechResultWord;

import java.util.Iterator;

import edu.cmu.sphinx.decoder.search.Token;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.FloatData;
import edu.cmu.sphinx.linguist.dictionary.Word;
import edu.cmu.sphinx.result.Result;

/**
 * Similiar to {@link edu.cmu.sphinx.result.Result}
 */
public class ResultAnalizerSimple {

  /** Stores the result */
  private SpeechResultImpl speechResult;

  /**
   * Converts a {@link Result} to a {@link SpeechResult}
   * 
   * @param result
   * @return
   */
  public SpeechResult processResult(Result result) {
    speechResult = new SpeechResultImpl();
    process(result, false, false);
    speechResult.reverseList();
    return speechResult;
  }

  /**
   * Returns the string of words (with timestamp) for this token.
   * 
   * @param result
   *          Result
   * @param wantFiller
   *          true if we want filler words included, false otherwise
   * @param wordTokenFirst
   *          true if the word tokens come before other types of tokens
   * 
   * @return the string of words
   */
  private void process(Result result, boolean wantFiller, boolean wordTokenFirst) {
    Token token = getBestToken(result);
    if (token != null) {
      if (wordTokenFirst) {
        processTimedWordPath(token, wantFiller);
      } else {
        processTimedWordTokenLastPath(token, wantFiller);
      }
    }
  }

  /**
   * Returns the string of words (with timestamp) for this token. This method assumes that the word tokens come before
   * other types of token.
   * 
   * @param wantFiller
   *          true if we want filler words, false otherwise
   * @return the string of words
   */
  private void processTimedWordPath(Token token, boolean wantFiller) {
    // get to the first emitting token
    while (token != null && !token.isEmitting()) {
      token = token.getPredecessor();
    }

    if (token != null) {
      Data lastWordFirstFeature = token.getData();
      Data lastFeature = lastWordFirstFeature;
      token = token.getPredecessor();

      while (token != null) {
        if (token.isWord()) {
          edu.cmu.sphinx.linguist.dictionary.Word word = token.getWord();
          if (wantFiller || !word.isFiller()) {
            speechResult.addWord(processWord(word, (FloatData) lastFeature, (FloatData) lastWordFirstFeature));
          }
          lastWordFirstFeature = lastFeature;
        }
        Data feature = token.getData();
        if (feature != null) {
          lastFeature = feature;
        }
        token = token.getPredecessor();
      }
    }
  }

  /**
   * Returns the string of words for this token, each with the starting sample number as the timestamp. This method
   * assumes that the word tokens come after the unit and hmm tokens. changed: bug 1109444
   * 
   * @return the string of words, each with the starting sample number
   */
  private void processTimedWordTokenLastPath(Token token, boolean wantFiller) {
    edu.cmu.sphinx.linguist.dictionary.Word word = null;
    Data firstFeature = null;
    Data lastFeature = null;

    // Scan back to the first Word token (which isactually the final
    // word in the result); we can't output it yetbecause we haven't
    // yet seen start/end tokens for it, so save it.
    while (token != null && !token.isWord()) {
      token = token.getPredecessor();
    }
    word = token.getWord();
    token = token.getPredecessor();

    // Now scan back to the next Word token, upatingfirstFeature as we go.
    // When we get there, output the previous word and itstimes, then save
    // this word.
    while (token != null) {
      if (token.isWord()) {
        if (wantFiller || !word.isFiller()) {
          speechResult.addWord(processWord(word, (FloatData) firstFeature, (FloatData) lastFeature));
        }
        word = token.getWord(); // next word
        lastFeature = firstFeature; // lastfeature of that word
      }
      if (token.isEmitting()) {
        firstFeature = token.getData();
        if (lastFeature == null) { // happens only for very last
          // feature oflast word
          lastFeature = firstFeature;
        }
      }
      token = token.getPredecessor();
    }
    // Now output the last word found, which is thefirst word of the
    // utterance (surely it's <s>)
    processWord(word, (FloatData) firstFeature, (FloatData) lastFeature);
  }

  /**
   * Adds the given word into the given string buffer with the start and end times from the given features.
   * 
   * changed: <a href="https://sourceforge.net/tracker/index.php?func=detail&aid=1109444&group_id=1904&atid=101904">
   * bugfix 1109444 </a>
   * 
   * @param word
   *          the word to add
   * @param startFeature
   *          the starting feature
   * @param endFeature
   *          the ending feature
   */
  protected SpeechResultWord processWord(Word word, FloatData startFeature, FloatData endFeature) {
    long startTime = -1L;
    long endTime = -1L;

    // shouldn't be necessary, but we've seen some null ptr exceptions...
    if (startFeature != null) {
      startTime = startFeature.getFirstSampleNumber() * 1000 / startFeature.getSampleRate();
    }
    if (endFeature != null) {
      endTime = endFeature.getFirstSampleNumber() * 1000 / endFeature.getSampleRate();
    }
    SpeechResultWord srWord = new SpeechResultWord(word.getSpelling());
    srWord.setStartPosition(startTime);
    srWord.setEndPosition(endTime);
    srWord.setFiller(word.isFiller());

    return srWord;
  }

  /**
   * Returns the best scoring token in the active set
   * 
   * @param result
   *          Result
   * @return the best scoring token or null
   */
  @SuppressWarnings("unchecked")
  private Token getBestActiveToken(Result result) {
    Token bestToken = null;
    for (Iterator i = result.getActiveTokens().iterator(); i.hasNext();) {
      Token token = (Token) i.next();
      if (bestToken == null || token.getScore() > bestToken.getScore()) {
        bestToken = token;
      }
    }
    return bestToken;
  }

  /**
   * Returns the best scoring final token in the result. A final token is a token that has reached a final state in the
   * current frame.
   * 
   * @param result
   *          Result
   * @return the best scoring final token or null
   */
  @SuppressWarnings("unchecked")
  private Token getBestFinalToken(Result result) {
    Token bestToken = null;
    for (Iterator i = result.getResultTokens().iterator(); i.hasNext();) {
      Token token = (Token) i.next();
      if (bestToken == null || token.getScore() > bestToken.getScore()) {
        bestToken = token;
      }
    }
    return bestToken;
  }

  /**
   * Returns the best scoring token in the result. First, the best final token is retrieved. A final token is one that
   * has reached the final state in the search space. If no final tokens can be found, then the best, non-final token is
   * returned.
   * 
   * @param result
   *          Result
   * @return the best scoring token or null
   */
  private Token getBestToken(Result result) {
    Token bestToken = getBestFinalToken(result);

    if (bestToken == null) {
      bestToken = getBestActiveToken(result);
    }

    return bestToken;
  }

}
