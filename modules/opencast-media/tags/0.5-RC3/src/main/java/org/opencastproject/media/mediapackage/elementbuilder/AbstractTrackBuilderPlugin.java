/**
 *  Copyright 2009 The Regents of the University of California
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

package org.opencastproject.media.mediapackage.elementbuilder;

import org.opencastproject.media.mediapackage.MediaPackageElement;
import org.opencastproject.media.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.media.mediapackage.MediaPackageReferenceImpl;
import org.opencastproject.media.mediapackage.MediaPackageSerializer;
import org.opencastproject.media.mediapackage.Track;
import org.opencastproject.media.mediapackage.UnsupportedElementException;
import org.opencastproject.media.mediapackage.track.AudioStreamImpl;
import org.opencastproject.media.mediapackage.track.TrackImpl;
import org.opencastproject.media.mediapackage.track.VideoStreamImpl;
import org.opencastproject.util.Checksum;
import org.opencastproject.util.MimeType;
import org.opencastproject.util.MimeTypes;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;

/**
 * Abstract base class for the various track builders.
 */
public abstract class AbstractTrackBuilderPlugin extends AbstractElementBuilderPlugin {

  /**
   * Creates a new instance of an abstract track builder plugin.
   * 
   * @throws IllegalStateException
   *           in case of not being able to initialize
   */
  protected AbstractTrackBuilderPlugin() throws IllegalStateException {
  }

  /**
   * Creates a track object from the given url. The method is called when the plugin reads the track information from
   * the manifest.
   * 
   * @param id
   *          the track id
   * @param uri
   *          the track location
   */
  protected abstract TrackImpl trackFromManifest(String id, URI uri);

  /**
   * @see org.opencastproject.media.mediapackage.elementbuilder.MediaPackageElementBuilderPlugin#newElement(org.opencastproject.media.mediapackage.MediaPackageElement.Type
   *      ,org.opencastproject.media.mediapackage.MediaPackageElementFlavor)
   */
  public MediaPackageElement newElement(MediaPackageElement.Type type, MediaPackageElementFlavor flavor)
          throws IOException {
    throw new IllegalStateException("Unable to create track from scratch");
  }

  /**
   * @see org.opencastproject.media.mediapackage.elementbuilder.MediaPackageElementBuilderPlugin#elementFromManifest(org.w3c.dom.Node,
   *      org.opencastproject.media.mediapackage.MediaPackageSerializer)
   */
  public MediaPackageElement elementFromManifest(Node elementNode, MediaPackageSerializer serializer)
          throws UnsupportedElementException {

    String id = null;
    MimeType mimeType = null;
    String reference = null;
    URI url = null;
    long size = -1;
    Checksum checksum = null;

    try {
      // id
      id = (String) xpath.evaluate("@id", elementNode, XPathConstants.STRING);

      // url
      url = serializer.resolvePath(xpath.evaluate("url/text()", elementNode).trim());

      // reference
      reference = (String) xpath.evaluate("@ref", elementNode, XPathConstants.STRING);

      // size
      String trackSize = xpath.evaluate("size/text()", elementNode).trim();
      if (!"".equals(trackSize))
        size = Long.parseLong(trackSize);

      // checksum
      String checksumValue = (String) xpath.evaluate("checksum/text()", elementNode, XPathConstants.STRING);
      String checksumType = (String) xpath.evaluate("checksum/@type", elementNode, XPathConstants.STRING);
      if (checksumValue != null && !checksumValue.equals("") && checksumType != null)
        checksum = Checksum.create(checksumType.trim(), checksumValue.trim());

      // mimetype
      String mimeTypeValue = (String) xpath.evaluate("mimetype/text()", elementNode, XPathConstants.STRING);
      if (mimeTypeValue != null && !mimeTypeValue.equals(""))
        mimeType = MimeTypes.parseMimeType(mimeTypeValue);

      //
      // Build the track

      TrackImpl track = trackFromManifest(id, url);
      if (id != null && !id.equals(""))
        track.setIdentifier(id);

      // Add url
      track.setURI(url);

      // Add reference
      if (reference != null && !reference.equals(""))
        track.referTo(MediaPackageReferenceImpl.fromString(reference));

      // Set size
      if (size > 0)
        track.setSize(size);

      // Set checksum
      if (checksum != null)
        track.setChecksum(checksum);

      // Set mimetpye
      if (mimeType != null)
        track.setMimeType(mimeType);

      // description
      String description = (String) xpath.evaluate("description/text()", elementNode, XPathConstants.STRING);
      if (description != null && !description.trim().equals(""))
        track.setElementDescription(description.trim());

      // tags
      NodeList tagNodes = (NodeList) xpath.evaluate("tags/tag", elementNode, XPathConstants.NODESET);
      for (int i = 0; i < tagNodes.getLength(); i++) {
        track.addTag(tagNodes.item(i).getTextContent());
      }

      // duration
      try {
        String strDuration = (String) xpath.evaluate("duration/text()", elementNode, XPathConstants.STRING);
        if (strDuration != null && !strDuration.equals("")) {
          long duration = Long.parseLong(strDuration.trim());
          track.setDuration(duration);
        }
      } catch (NumberFormatException e) {
        throw new UnsupportedElementException("Duration of track " + url + " is malformatted");
      }

      // audio settings
      Node audioSettingsNode = (Node) xpath.evaluate("audio", elementNode, XPathConstants.NODE);
      if (audioSettingsNode != null && audioSettingsNode.hasChildNodes()) {
        try {
          AudioStreamImpl as = AudioStreamImpl.fromManifest(createStreamID(track), audioSettingsNode, xpath);
          track.addStream(as);
        } catch (IllegalStateException e) {
          throw new UnsupportedElementException("Illegal state encountered while reading audio settings from " + url + ": "
                  + e.getMessage());
        } catch (XPathException e) {
          throw new UnsupportedElementException("Error while parsing audio settings from " + url + ": " + e.getMessage());
        }
      }

      // video settings
      Node videoSettingsNode = (Node) xpath.evaluate("video", elementNode, XPathConstants.NODE);
      if (videoSettingsNode != null && videoSettingsNode.hasChildNodes()) {
        try {
          VideoStreamImpl vs = VideoStreamImpl.fromManifest(createStreamID(track), videoSettingsNode, xpath);
          track.addStream(vs);
        } catch (IllegalStateException e) {
          throw new UnsupportedElementException("Illegal state encountered while reading video settings from " + url + ": "
                  + e.getMessage());
        } catch (XPathException e) {
          throw new UnsupportedElementException("Error while parsing video settings from " + url + ": " + e.getMessage());
        }
      }

      return track;
    } catch (XPathExpressionException e) {
      throw new UnsupportedElementException("Error while reading track information from manifest: " + e.getMessage());
    } catch (NoSuchAlgorithmException e) {
      throw new UnsupportedElementException("Unsupported digest algorithm: " + e.getMessage());
    } catch (URISyntaxException e) {
      throw new UnsupportedElementException("Error while reading presenter track " + url + ": " + e.getMessage());
    }
  }

  private String createStreamID(Track track) {
    return "stream-" + (track.getStreams().length + 1);
  }

}
