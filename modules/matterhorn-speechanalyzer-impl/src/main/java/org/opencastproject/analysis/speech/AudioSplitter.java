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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * This class splits a given AudioStream into several parts. There is a possibility to detect silences and split at an
 * optimal location (Quick and Dirty version). You can specify a minimal amount of seconds {@link #MIN_SECONDS} and an
 * additional amount of seconds {@link #SECONDS_FOR_SILENCE_CHECK} where the minimal volume level should be located. <br/>
 * 
 * Information from <a
 * href="http://archives.java.sun.com/cgi-bin/wa?A2=ind0010&L=javasound-interest&P=14477">javasound-interest archive</a> <br/>
 * <br/>
 * PCM means that samples are encoded as integer values. Several parameters define a PCM format: -number of bits: how
 * many bits are used for one sample. Widely used are 8, 16, 20, 24, 32 bits. E.g. when you have 16 bit samples, you
 * have 2 bytes for each sample in the byte arrays used in JavaSound. Java has the types byte, short, and int which
 * correspond to 8, 16, 32 bits. <br/>
 * -signed/unsigned: how to interpret samples. Usually, only 8 bit samples occur unsigned, more than 8 bits are usually
 * signed. Though, 8 bit samples may be signed, too. Signed samples are stored using the 2's complement. Note that Java
 * always interprets byte/short/integer as signed, so for using e.g. unsigned byte samples in Java you'd need to convert
 * the unsigned byte to signed first. <br/>
 * -big endian/little endian: this determines the order of bytes for samples > 8 bit. Big endian means that the most
 * significant byte comes first. Java internally uses big endian shorts, ints and longs. For 8 bit samples, endianness
 * is meaningless.
 */
public class AudioSplitter {

  private static final float MIN_SECONDS = 3f;
  
  private static final float SECONDS_FOR_SILENCE_CHECK = 3f;

  private static final int WEIGHT_ADJ = 1;
  
  private static final int WEIGHT = 2;

  /**
   * Splits an Audiofile into Streams
   * 
   * @param file
   * @return
   * @throws UnsupportedAudioFileException
   * @throws IOException
   */
  public static List<AudioInputStream> splitIntoStreams(File file, ProcessObservable observer)
          throws UnsupportedAudioFileException, IOException {
    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
    return splitIntoStreams(audioInputStream, file.length(), observer);

  }

  /**
   * To find a better silence region (silence=0) and not just a peak, a weighted value is used (left and right neighbor
   * in the array). <br/>
   * <br/>
   * 
   * <table border="1">
   * <tr colspan="2">
   * <td>part of the AudioStream</td>
   * </tr>
   * <tr>
   * <td>minBuffer</td>
   * <td>addBuffer: here is the minimum audio level found</td>
   * </tr>
   * </table>
   * 
   * @param audioInputStream
   *          The AudioStream which is to split
   * @param totalNumBytes
   *          The length of the AudioStream
   * @param observer
   *          To get progress information
   * @return
   * @throws IOException
   */
  public static List<AudioInputStream> splitIntoStreams(AudioInputStream audioInputStream, long totalNumBytes,
          ProcessObservable observer) throws IOException {
    List<AudioInputStream> list = new LinkedList<AudioInputStream>();
    AudioFormat format = audioInputStream.getFormat();

    long totBufferSize = (long) (format.getFrameRate() * format.getFrameSize() * (MIN_SECONDS));
    int minBufSize = (totBufferSize > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) totBufferSize);
    int addBufferSize = (int) (format.getFrameRate() * format.getFrameSize() * SECONDS_FOR_SILENCE_CHECK);
    int numBytesRead = 0;
    int numBytesReadPerIteration = 0;
    int totalNumBytesRead = 0;
    byte[] minBuffer = new byte[minBufSize];
    byte[] addBuffer = new byte[addBufferSize];
    boolean lastIteration = false;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    while (numBytesRead != -1) {

      numBytesRead = audioInputStream.read(minBuffer, 0, minBuffer.length);
      numBytesReadPerIteration += numBytesRead;
      baos.write(minBuffer, 0, numBytesRead);

      if (numBytesRead != -1) {
        numBytesRead = audioInputStream.read(addBuffer, 0, addBuffer.length);
        numBytesReadPerIteration += numBytesRead;
      }

      int bufferMinIndex = 0;
      if (numBytesRead == -1) { // empty stream
        lastIteration = true;
      } else if (addBuffer.length != numBytesRead) { // end of stream
        baos.write(addBuffer, 0, numBytesRead);
        lastIteration = true;
      } else if (numBytesRead != -1) {
        int[] bufferInt = AudioToolkit.bytesToInt(addBuffer, numBytesRead, format);
        int[] bufferMap = new int[bufferInt.length];
        for (int i = 0; i < bufferMap.length; i++) {
          bufferMap[i] = WEIGHT_ADJ * bufferInt[Math.max(0, i - 1)] + //
                  WEIGHT * bufferInt[i] + //
                  WEIGHT_ADJ * bufferInt[Math.min(bufferMap.length - 1, i + 1)];
        }
        int minIndex = findMinIndex(bufferMap);
        bufferMinIndex = minIndex * format.getSampleSizeInBits() / 8;
        //
        // to avoid splitting between frames to not mess up the audio
        // encoding
        while (bufferMinIndex % format.getFrameSize() != 0) {
          bufferMinIndex++;
        }
        // write begin of addBuffer
        baos.write(addBuffer, 0, bufferMinIndex);
      }

      totalNumBytesRead += numBytesReadPerIteration;
      observer.setStatusProgress(totalNumBytesRead / totalNumBytes);
      //
      // create AudioInputstream List Entry
      list.add(toAudioStream(audioInputStream.getFormat(), baos));
      //
      // write end of addBuffer
      if (!lastIteration) {
        baos = new ByteArrayOutputStream();
        baos.write(addBuffer, bufferMinIndex, //
                numBytesRead - bufferMinIndex);
      } else {
        break;
      }
    }
    return list;
  }

  /**
   * Converts the byteArray from the {@link ByteArrayOutputStream} to an {@link AudioInputStream}
   * 
   * @param format
   *          The format the stream is encoded
   * @param baos
   *          The stream containing the audio
   * @return
   */
  private static AudioInputStream toAudioStream(AudioFormat format, ByteArrayOutputStream baos) {
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray(), 0, baos.size());
    AudioInputStream ais = new AudioInputStream(bais, format, baos.size());
    return ais;
  }

  /**
   * @param bufferMap
   * @return the index of the int array with the smalles value
   */
  private static int findMinIndex(int[] bufferMap) {
    int minIndex = 0;
    int minValue = Integer.MAX_VALUE;
    for (int i = 0; i < bufferMap.length; i++) {
      if (bufferMap[i] < minValue) {
        minValue = bufferMap[i];
        minIndex = i;
      }
    }
    return minIndex;
  }

}
