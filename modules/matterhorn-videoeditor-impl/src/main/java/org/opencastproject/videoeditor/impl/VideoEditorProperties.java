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

package org.opencastproject.videoeditor.impl;

/**
 *
 * @author wsmirnow
 */
public interface VideoEditorProperties {

  String AUDIO_ENCODER = "audio.encoder";
  String AUDIO_ENCODER_PROPERTIES = "audio.encoder.properties";
  
  String VIDEO_ENCODER = "video.encoder";
  String VIDEO_ENCODER_PROPERTIES = "video.encoder.properties";
  
  String MUX = "mux";
  String MUX_PROPERTIES = "mux.properties";
  
  String OUTPUT_FILE_SUFFIX = "outputfile.suffix";
  String OUTPUT_FILE_EXTENSION = "outputfile.extension";
  
  /** Timespan in seconds before silece cut begin. */
  String SILENCE_PRE_LENGTH = "silence.pre.length";
  /** Minimum length in seconds to accept silence sequence. */
  String SILENCE_MIN_LENGTH = "silence.min.length";
  /** Silence threshold in decibel (e.g. -50 for loud classrooms, -35 for very silent indoor location). */
  String SILENCE_THRESHOLD_DB = "silence.threshold.db";
}
