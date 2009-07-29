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

package org.opencastproject.media.bundle;

import org.opencastproject.util.Checksum;
import org.opencastproject.util.MimeType;
import org.opencastproject.util.UnknownFileTypeException;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Implementation of an audiovisual track.
 * 
 * @author Tobias Wunden <tobias.wunden@id.ethz.ch>
 * @version $Id$
 */
public class AudioVisualTrackImpl extends TrackImpl implements AudioVisualTrack {

  /** Serial version UID */
  private static final long serialVersionUID = 4221572792652579165L;

  /**
   * Creates a new audiovisual track.
   * 
   * @param flavor
   *          the track flavor
   * @param file
   *          the file
   * @param checksum
   *          the file's checksum
   * @throws IOException
   *           if the track file cannot be accessed
   * @throws UnknownFileTypeException
   *           if the file is of an unknown file type
   * @throws NoSuchAlgorithmException
   *           if the track's checksum cannot be computed
   */
  protected AudioVisualTrackImpl(BundleElementFlavor flavor, File file,
      Checksum checksum) throws IOException, UnknownFileTypeException,
      NoSuchAlgorithmException {
    super(flavor, file, checksum);
  }

  /**
   * Creates a new audiovisual track.
   * 
   * @param flavor
   *          the track flavor
   * @param mimeType
   *          the file's mime type
   * @param file
   *          the file
   * @param checksum
   *          the file's checksum
   * @throws IOException
   *           if the track file cannot be accessed
   * @throws UnknownFileTypeException
   *           if the file is of an unknown file type
   */
  protected AudioVisualTrackImpl(BundleElementFlavor flavor, MimeType mimeType,
      File file, Checksum checksum) throws IOException,
      UnknownFileTypeException {
    super(flavor, mimeType, file, checksum);
  }

  /**
   * Creates a new audiovisual track.
   * 
   * @param flavor
   *          the track flavor
   * @param mimeType
   *          the file's mime type
   * @param file
   *          the file
   * @throws IOException
   *           if the track file cannot be accessed
   * @throws NoSuchAlgorithmException
   *           if the track's checksum cannot be computed
   */
  protected AudioVisualTrackImpl(BundleElementFlavor flavor, MimeType mimeType,
      File file) throws IOException, NoSuchAlgorithmException {
    super(flavor, mimeType, file);
  }

  /**
   * Creates a new audiovisual track.
   * 
   * @param id
   *          the track identifier within the bundle
   * @param flavor
   *          the track flavor
   * @param mimeType
   *          the file's mime type
   * @param file
   *          the file
   * @param checksum
   *          the file's checksum
   * @throws IOException
   *           if the track file cannot be accessed
   */
  protected AudioVisualTrackImpl(String id, BundleElementFlavor flavor,
      MimeType mimeType, File file, Checksum checksum) throws IOException {
    super(id, flavor, mimeType, file, checksum);
  }

  /**
   * Creates a new audiovisual track for the given file and track type.
   * 
   * @param flavor
   *          the track flavor
   * @param track
   *          the file
   * @throws IOException
   *           if the track file cannot be accessed
   * @throws UnknownFileTypeException
   *           if the file is of an unknown file type
   * @throws NoSuchAlgorithmException
   *           if the track's checksum cannot be computed
   */
  protected AudioVisualTrackImpl(BundleElementFlavor flavor, File track)
      throws IOException, UnknownFileTypeException, NoSuchAlgorithmException {
    super(flavor, track);
  }

  /**
   * Reads a track from the specified file and returns it encapsulated in a
   * {@link AudioVisualTrack} object.
   * 
   * @param file
   *          the track file
   * @return the track object
   * @throws IOException
   *           if reading the manifest file fails
   * @throws UnknownFileTypeException
   *           if the manifest file is of an unknown file type
   * @throws NoSuchAlgorithmException
   *           if the md5 checksum cannot be computed
   */
  public static AudioVisualTrackImpl fromFile(File file) throws IOException,
      UnknownFileTypeException, NoSuchAlgorithmException {
    return new AudioVisualTrackImpl(AudioVisualTrack.FLAVOR, file);
  }

  /**
   * Reads a track from the specified file and returns it encapsulated in a
   * {@link AudioVisualTrack} object.
   * 
   * @param file
   *          the track file
   * @param mimeType
   *          the track's mime type
   * @param checksum
   *          the file checksum
   * @return the track object
   * @throws IOException
   *           if reading the track file fails
   * @throws UnknownFileTypeException
   *           if the track file is of an unknown file type
   * @throws NoSuchAlgorithmException
   *           if the md5 checksum cannot be computed
   */
  public static AudioVisualTrackImpl fromFile(File file, MimeType mimeType,
      Checksum checksum) throws IOException, UnknownFileTypeException,
      NoSuchAlgorithmException {
    return new AudioVisualTrackImpl(AudioVisualTrack.FLAVOR, mimeType, file,
        checksum);
  }

  /**
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    AudioVisualTrackImpl t = null;
    try {
      t = new AudioVisualTrackImpl(flavor, mimeType, new File(path, fileName),
          checksum);
      t.duration = duration;
      t.audioSettings = (AudioSettings) audioSettings.clone();
      t.videoSettings = (VideoSettings) videoSettings.clone();
    } catch (Exception e) {
      throw new IllegalStateException("Illegal state while cloning track: " + t);
    }
    return super.clone();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer("audiovisual track (");
    if (hasAudio() && audioSettings.getMetadata().getEncoderLibrary() != null) {
      buf.append(audioSettings.getMetadata().getEncoderLibrary());
    } else {
      buf.append(getMimeType());
    }
    buf.append(")");
    return buf.toString().toLowerCase();
  }

}