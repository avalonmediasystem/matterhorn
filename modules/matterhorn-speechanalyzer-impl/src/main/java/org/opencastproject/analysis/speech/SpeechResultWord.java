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

import java.io.Serializable;

/**
 * Represents one word with timestamps.
 */
public class SpeechResultWord implements Serializable {

  /** Serial Version UID needed for {@link Serializable} */
  private static final long serialVersionUID = 3271273602551989553L;

  /** The word */
  private String word = null;

  /** Start and end position of the word in [ms] */
  private long startPosition, endPosition;

  /** Indicator whether it is a filler word or not */
  private boolean filler;

  /**
   * @param word
   *          The word
   */
  public SpeechResultWord(String word) {
    this.word = word;
  }

  /**
   * @param word
   *          The word
   * @param startPosition
   *          Start position of the word occurring
   * @param endPosition
   *          End position of the word occurring
   */
  public SpeechResultWord(String word, long startPosition, long endPosition) {
    this.word = word;
    this.startPosition = startPosition;
    this.endPosition = endPosition;
  }

  /**
   * @return Returns the word.
   */
  public String getWord() {
    return word;
  }

  /**
   * @param word
   *          The word to set.
   */
  public void setWord(String word) {
    this.word = word;
  }

  /**
   * @return start position in [ms]
   */
  public long getStartPosition() {
    return startPosition;
  }

  /**
   * @param startPosition
   *          start position in [ms]
   */
  public void setStartPosition(long startPosition) {
    this.startPosition = startPosition;
  }

  /**
   * @return end position in [ms]
   */
  public long getEndPosition() {
    return endPosition;
  }

  /**
   * @param endPosition
   *          end position in [ms]
   */
  public void setEndPosition(long endPosition) {
    this.endPosition = endPosition;
  }

  /**
   * @return Returns the filler.
   */
  public boolean isFiller() {
    return filler;
  }

  /**
   * @param filler
   *          The filler to set.
   */
  public void setFiller(boolean filler) {
    this.filler = filler;
  }

}
