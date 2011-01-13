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

import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a container of {@link SpeechResult}s, i.e. a list of words.
 */
public class SpeechResultImpl implements SpeechResult {

  /** Serial Version UID */
  private static final long serialVersionUID = 8122780357903911965L;

  /** Decimal format for debugging */
  private static DecimalFormat decimalFormat = new DecimalFormat("#.###");

  /** A list containing the words */
  private List<SpeechResultWord> list = new LinkedList<SpeechResultWord>();

  /** The particular sequence number */
  private int sequenceNumber;

  /** Duration of the sequence in nanoseconds */
  private long duration;

  /** Confidence of the word path [0, 1] i.e. how accurate is the recognition */
  private double confidence;

  /**
   * @param word
   *          Word
   * @param startPosition
   *          Start position in [ms]
   * @param endPosition
   *          End position in [ms]
   */
  public void addWord(String word, long startPosition, long endPosition) {
    list.add(new SpeechResultWord(word, startPosition, endPosition));
  }

  /**
   * @see ch.ethz.replay.core.indexer.plugin.asr.SpeechResult#addWord(ch.ethz.replay.core.indexer.plugin.asr.SpeechResultWord)
   */
  public void addWord(SpeechResultWord word) {
    list.add(word);
  }

  /**
   * @see ch.ethz.replay.core.indexer.plugin.asr.SpeechResult#getWords()
   */
  public List<SpeechResultWord> getWords() {
    return list;
  }

  /**
   * Reverse of the list of words
   */
  public void reverseList() {
    Collections.reverse(list);
  }

  /**
   * Empty list
   */
  public void clearList() {
    list.clear();
  }

  /**
   * @see ch.ethz.replay.core.indexer.plugin.asr.SpeechResult#getDuration()
   */
  public long getDuration() {
    return duration;
  }

  /**
   * @see ch.ethz.replay.core.indexer.plugin.asr.SpeechResult#setDuration(long)
   */
  public void setDuration(long duration) {
    this.duration = duration;
  }

  /**
   * @see ch.ethz.replay.core.indexer.plugin.asr.SpeechResult#getSequenceNumber()
   */
  public int getSequenceNumber() {
    return this.sequenceNumber;
  }

  /**
   * @see ch.ethz.replay.core.indexer.plugin.asr.SpeechResult#setSequenceNumber(int)
   */
  public void setSequenceNumber(int number) {
    this.sequenceNumber = number;
  }

  /**
   * @see ch.ethz.replay.core.indexer.plugin.asr.SpeechResult#getConfidence()
   */
  public double getConfidence() {
    return confidence;
  }

  /**
   * @see ch.ethz.replay.core.indexer.plugin.asr.SpeechResult#setConfidence(int)
   */
  public void setConfidence(double confidence) {
    this.confidence = confidence;
  }

  /**
   * Compares {@link SpeechResult} according to their sequence number
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(SpeechResult o) {
    return new Integer(getSequenceNumber()).compareTo(o.getSequenceNumber());
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringWriter buffer = new StringWriter();
    boolean isFirst = true;
    for (SpeechResultWord word : list) {
      if (!isFirst) {
        buffer.append(" ");
      }
      isFirst = false;
      buffer.append(word.getWord());
    }
    buffer.append(" (" + decimalFormat.format(confidence) + ")");
    return buffer.toString();
  }
}
