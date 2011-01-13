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
import java.util.List;

public interface SpeechResult extends Serializable, Comparable<SpeechResult> {

  /**
   * @param word
   *          Adds a {@link SpeechResultWord}
   */
  public void addWord(SpeechResultWord word);

  /**
   * @return A List of words
   */
  List<SpeechResultWord> getWords();

  /**
   * @return Sequence number to determine the sequence of the original speech parts
   */
  int getSequenceNumber();

  /**
   * @param number
   *          Sequence number to determine the sequence of the original speech parts
   */
  void setSequenceNumber(int number);

  /**
   * @param duration
   *          in nanoseconds
   */
  void setDuration(long duration);

  /**
   * @return duration in nanoseconds
   */
  long getDuration();

  /**
   * @param confidence
   *          of the result [0, 1]
   */
  void setConfidence(double probability);

  /**
   * @return confidence of the result [0, 1]
   */
  double getConfidence();

}
