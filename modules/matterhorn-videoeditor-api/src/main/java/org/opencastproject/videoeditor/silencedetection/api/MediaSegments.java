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

package org.opencastproject.videoeditor.silencedetection.api;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author wsmirnow
 */
@XmlRootElement(name = "media-segments")
@XmlAccessorType(XmlAccessType.NONE)
public class MediaSegments {

  @XmlElement(name = "trackId", required = true)
  private String trackId;
  
  @XmlElement(name = "filePath")
  private String filePath;
  
  @XmlElementWrapper(name = "segments", required = true, nillable = false)
  @XmlElement(name = "segment")
  private List<MediaSegment> mediaSegments;
  
  public MediaSegments() {
    this(null, null, null);
  }
  
  public MediaSegments(String trackId, String filePath, List<MediaSegment> mediaSegments) {
    this.trackId = trackId;
    this.filePath = filePath;
    this.mediaSegments = mediaSegments;
  }
  
  public List<MediaSegment> getMediaSegments() {
    return mediaSegments;
  }
  
  public String getTrackId() {
    return trackId;
  }
  
  public String getFilePath() {
    return filePath;
  }
  
  public String toXml() throws JAXBException {
    StringWriter sw = new StringWriter();
    JAXBContext jctx = JAXBContext.newInstance(MediaSegments.class);
    Marshaller mediaSegmentsMarshaller = jctx.createMarshaller();
    mediaSegmentsMarshaller.marshal(this, sw);
    return sw.toString();
  }
  
  public static MediaSegments fromXml(String mediaSegmentsXml) throws JAXBException {
    StringReader sr = new StringReader(mediaSegmentsXml);
    JAXBContext jctx = JAXBContext.newInstance(MediaSegments.class);
    Unmarshaller unmarshaller = jctx.createUnmarshaller();
    MediaSegments mediaSegments = (MediaSegments) unmarshaller.unmarshal(sr);
    sr.close();
    return mediaSegments;
  }
}
