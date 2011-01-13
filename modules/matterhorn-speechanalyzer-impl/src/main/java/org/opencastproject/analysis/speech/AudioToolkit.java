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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat.Encoding;

import org.tritonus.share.sampled.TConversionTool;

public class AudioToolkit {

  /**
   * @param numBytes
   *          Number of bytes
   * @param af
   *          The AudioFormat
   * @return length in nanoseconds
   */
  public static long getDuration(long numBytes, AudioFormat af) {
    return (long) (numBytes / (af.getSampleRate() * (af.getSampleSizeInBits() / 8) * af.getChannels()) * 1000000000);
  }

  /**
   * Currently not used, for debugging
   * 
   * @param af
   * @return
   */
  public static String[] showAudioFormat(AudioFormat af) {
    String[] buffer = new String[7];
    buffer[0] = "Encoding:   " + af.getEncoding();
    buffer[1] = "Samplerate: " + af.getSampleRate() + " Hz";
    buffer[2] = "Samplebits: " + af.getSampleSizeInBits() + " bit";
    buffer[3] = "Channels:   " + (af.getChannels() == 1 ? "mono" : "stereo");
    buffer[4] = "Framerate:  " + af.getFrameRate();
    buffer[5] = "Framesize:  " + af.getFrameSize() + " bytes/frame";
    buffer[6] = "Endian:     " + (af.isBigEndian() ? "big-endian" : "little-endian");
    return buffer;
  }

  /**
   * Converts a stream into an {@link AudioInputStream}
   * 
   * @param ais
   *          AudioInputStream
   * @return Audio data as byteArray
   * @throws IOException
   */
  public static byte[] streamToByteArray(AudioInputStream ais) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    int bytesRead = 0;
    while (bytesRead >= 0) {
      byte[] bytes = new byte[ais.available()];
      bytesRead = ais.read(bytes);
      baos.write(bytes);
    }
    baos.close();
    return baos.toByteArray();
  }

  /**
   * Converts bytes to unsigned integers
   * 
   * @param bytes
   *          Audio data
   * @param numBytes
   *          Length of audio data
   * @param af
   *          The format the audio is represented
   * @return
   */
  public static int[] bytesToInt(byte[] bytes, int numBytes, AudioFormat af) {
    int[] toInt = new int[numBytes * 8 / af.getSampleSizeInBits()];
    if (af.getEncoding().equals(Encoding.PCM_UNSIGNED)) {
      byte[] toIntegrateTmp = new byte[bytes.length];
      TConversionTool.convertSign8(bytes, 0, toIntegrateTmp, 0, bytes.length / af.getSampleSizeInBits() / 8);
      bytes = toIntegrateTmp;
    } else if (af.getEncoding().equals(Encoding.ALAW)) {
      byte[] toIntegrateTmp = new byte[bytes.length];
      if (af.getSampleSizeInBits() == 8) {
        TConversionTool.alaw2pcm8(bytes, 0, toIntegrateTmp, 0, bytes.length / af.getSampleSizeInBits() / 8, af
                .isBigEndian());
      } else {
        TConversionTool.alaw2pcm16(bytes, 0, toIntegrateTmp, 0, bytes.length / af.getSampleSizeInBits() / 8, af
                .isBigEndian());
      }
      bytes = toIntegrateTmp;
    } else if (af.getEncoding().equals(Encoding.ULAW)) {
      byte[] toIntegrateTmp = new byte[bytes.length];
      if (af.getSampleSizeInBits() == 8) {
        TConversionTool.ulaw2pcm8(bytes, 0, toIntegrateTmp, 0, bytes.length / af.getSampleSizeInBits() / 8, af
                .isBigEndian());
      } else {
        TConversionTool.ulaw2pcm16(bytes, 0, toIntegrateTmp, 0, bytes.length / af.getSampleSizeInBits() / 8, af
                .isBigEndian());
      }
      bytes = toIntegrateTmp;
    }
    int newIndex = 0;

    for (int i = 0; i < numBytes; i += af.getSampleSizeInBits() / 8) {
      int value = 0;

      switch (af.getSampleSizeInBits()) {
      case 8:
        value = bytes[i];
        break;
      case 16:
        value = TConversionTool.bytesToInt16(bytes, i, af.isBigEndian());
        break;
      case 24:
        value = TConversionTool.bytesToInt24(bytes, i, af.isBigEndian());
        break;
      case 32:
        value = TConversionTool.bytesToInt32(bytes, i, af.isBigEndian());
        break;
      default:
        throw new RuntimeException("audio has unsupported samplesize: " + af.getSampleSizeInBits());
      }
      toInt[newIndex] = Math.abs(value);
      newIndex++;
    }

    return toInt;
  }

}
