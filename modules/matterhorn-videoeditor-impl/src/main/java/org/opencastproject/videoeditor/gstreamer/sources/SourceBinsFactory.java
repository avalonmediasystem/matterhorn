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
package org.opencastproject.videoeditor.gstreamer.sources;

import java.io.FileNotFoundException;
import org.gstreamer.Bin;
import org.opencastproject.videoeditor.gstreamer.GstreamerTypeFinder;
import org.opencastproject.videoeditor.gstreamer.exceptions.PipelineBuildException;
import org.opencastproject.videoeditor.gstreamer.exceptions.UnknownSourceTypeException;

/**
 *
 * @author wsmirnow
 */
public class SourceBinsFactory {

  private String outputFilePath = null;
  private String sourceMHElementID = null;
  private GnonlinSourceBin audioSourceBin = null;
  private GnonlinSourceBin videoSourceBin = null;

  public SourceBinsFactory(String outputFilePath) {
    this.outputFilePath = outputFilePath;
  }

  public void addFileSource(String inputFilePath, long mediaStartMillis, long durationMillis)
          throws FileNotFoundException, UnknownSourceTypeException, PipelineBuildException {

    GstreamerTypeFinder typeFinder;
    typeFinder = new GstreamerTypeFinder(inputFilePath);

    if (typeFinder.isAudioFile()) {
      if (audioSourceBin == null) {
        audioSourceBin = new GnonlinSourceBin(GnonlinSourceBin.SourceType.Audio, typeFinder.getAudioCaps());
      }
      audioSourceBin.addFileSource(inputFilePath, mediaStartMillis, durationMillis);
    }
    
    if (typeFinder.isVideoFile()) {
      if (videoSourceBin == null) {
        videoSourceBin = new GnonlinSourceBin(GnonlinSourceBin.SourceType.Video, typeFinder.getVideoCaps());
      }
      videoSourceBin.addFileSource(inputFilePath, mediaStartMillis, durationMillis);
    }
  }

  public boolean hasAudioSource() {
    return audioSourceBin != null;
  }

  public boolean hasVideoSource() {
    return videoSourceBin != null;
  }
  
  public String getOutputFilePath() {
    return outputFilePath;
  }
  
  public Bin getAudioSourceBin() {
    return audioSourceBin.getBin();
  }
  
  public Bin getVideoSourceBin() {
    return videoSourceBin.getBin();
  }
  
  public void setSourceMHElementID(String sourceMHElementID) {
    this.sourceMHElementID = sourceMHElementID;
  }
  
  public String getSourceMHElementID() {
    return sourceMHElementID;
  }
}
