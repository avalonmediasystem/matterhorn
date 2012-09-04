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
package org.opencastproject.workflow.handler;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.opencastproject.ingest.api.IngestService;
import org.opencastproject.job.api.JobContext;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.mediapackage.Track;
import org.opencastproject.workflow.api.AbstractWorkflowOperationHandler;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowOperationException;
import org.opencastproject.workflow.api.WorkflowOperationResult;
import org.opencastproject.workflow.api.WorkflowOperationResult.Action;
import org.opencastproject.workspace.api.Workspace;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * workflow operation handler for generating the waveform from a wave file
 */
public class WaveformWorkflowOperationHandler extends AbstractWorkflowOperationHandler {

  /**
   * a logger
   */
  private static final Logger logger = LoggerFactory
      .getLogger(WaveformWorkflowOperationHandler.class);

  /**
   * the workspace
   */
  private Workspace workspace;
  /**
   * the ingestservice
   */
  private IngestService ingestService;
  /**
   * the flavor string for the audio source
   */
  private static final String audioFlavorString = "audio/waveform";
  /**
   * the resulting MediaPackageElementFlavor
   */
  private static final MediaPackageElementFlavor audioFlavor = MediaPackageElementFlavor
      .parseFlavor(audioFlavorString);
  /**
   * the flavor string for the resulting image
   */
  private static final String waveformFlavorString = "image/waveform";
  /**
   * the resulting MediaPackageElementFlavor
   */
  private static final MediaPackageElementFlavor waveformFlavor = MediaPackageElementFlavor
      .parseFlavor(waveformFlavorString);

  public void activate(ComponentContext cc) {
    super.activate(cc);
    logger.info("activating waveform workflow operation handler");
  }

  @Override
  public WorkflowOperationResult start(WorkflowInstance workflowInstance, JobContext context)
      throws WorkflowOperationException {
    MediaPackage mediaPackage = workflowInstance.getMediaPackage();

    logger.debug("source-flavor{}", workflowInstance.getConfiguration("source-flavor"));
    logger.info("generating waveform png form mediapackage {}", mediaPackage.getIdentifier()
        .compact());

    Track[] tracks = mediaPackage.getTracks(audioFlavor);
    if (tracks.length == 0) {
      logger
          .info("Skipping Waveform generation because no wave file is present in the mediapackage");
      return createResult(Action.SKIP);
    }

    try {
      logger.debug("tracks: {}", Arrays.toString(tracks));
      File waveFile = workspace.get(tracks[0].getURI());
      logger.debug("wave file: {}", waveFile);
      Wave wave = new Wave(waveFile);
      logger.debug("format: {}", wave.getWaveHeader().getFormat());
      // AudioInputStream in = AudioSystem.getAudioInputStream(waveFile);
      FileInputStream in = new FileInputStream(waveFile);
      // ignore first 44 bytes because that's the header of the wave file
      in.read(new byte[44]);
      // size of chunk that should be loaded
      int chunkSize = 16 * 1024 * 1024;
      // for signed signals, the middle is 0 (-1 ~ 1)
      double middleLine = 0;
      // magnifier
      int magnifier = 1000;
      // middle line
      int middle = 0;
      // x position in waveform
      int xPos = 0;
      // number of bytes per sample (should be 2)
      int bytePerSample = wave.getWaveHeader().getBitsPerSample() / 8;
      // number of samples in wave file
      int numSamples = in.available() / bytePerSample;

      /************************************/
      int height = 500;
      int width = 5000;

      middle = height / 2;

      // render wave form image
      BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

      // set default white background
      Graphics2D graphics = bufferedImage.createGraphics();
      graphics.setPaint(new Color(255, 255, 255));
      graphics.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
      /************************************/

      // number of samples per time frame
      int numSamplePerTimeFrame = numSamples / width;
      // array for byte reading
      byte[] bytes = getPart(in, chunkSize);

      while (bytes.length > 0) {
        double[] nAmplitudes = getNormalizedAmplitudes(bytes, wave.getWaveHeader());

        // number of painted Samples in this Chunk
        int numSamplePaintedChunk = nAmplitudes.length / numSamplePerTimeFrame;

        // positive scaled amplitudes
        int[] scaledPosAmplitudes = new int[numSamplePaintedChunk];
        // negative scaled amplitudes
        int[] scaledNegAmplitudes = new int[numSamplePaintedChunk];

        // width scaling
        for (int i = 0; i < numSamplePaintedChunk; i++) {
          double sumPosAmplitude = 0;
          double sumNegAmplitude = 0;
          int startSample = i * numSamplePerTimeFrame;
          for (int j = 0; j < numSamplePerTimeFrame; j++) {
            double a = nAmplitudes[startSample + j];
            if (a > middleLine) {
              sumPosAmplitude += (a - middleLine);
            } else {
              sumNegAmplitude += (a - middleLine);
            }
          }

          int scaledPosAmplitude = (int) (sumPosAmplitude / numSamplePerTimeFrame * magnifier + middle);
          int scaledNegAmplitude = (int) (sumNegAmplitude / numSamplePerTimeFrame * magnifier + middle);

          scaledPosAmplitudes[i] = scaledPosAmplitude;
          scaledNegAmplitudes[i] = scaledNegAmplitude;
        }

        // draw waveform pixel by pixel
        for (int i = 0; i < scaledNegAmplitudes.length; i++) {
          for (int j = scaledNegAmplitudes[i]; j < scaledPosAmplitudes[i]; j++) {
            int y = height - j; // j from -ve to +ve, i.e. draw from top to bottom
            if (y < 0) {
              y = 0;
            } else if (y >= height) {
              y = height - 1;
            }
            bufferedImage.setRGB(xPos, y, 0);
          }
          xPos++;
          if (xPos >= width) {
            break;
          }
        }

        bytes = getPart(in, chunkSize);
      }

      // finally save the image to file
      logger.debug("putting bufferedImage in ByteArrayOutputstream");
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      ImageIO.write(bufferedImage, "png", os);
      InputStream is = new ByteArrayInputStream(os.toByteArray());
      logger.debug("adding waveform as an attachment to mediapackage");
      mediaPackage = ingestService.addAttachment(is, "waveform.png", waveformFlavor, mediaPackage);

      // ImageIO.write(bufferedImage, "png", new File("/home/markus/test.png"));

    } catch (Exception e) {
      logger.error(e.getMessage());
      throw new WorkflowOperationException(e);
    }
    return createResult(mediaPackage, Action.CONTINUE);
  }

  /**
   * read a part of a wave file and return it in a byte array
   * 
   * @param in the AudioInputstream
   * @param size the size to read
   * @param frameSize the frame size
   * @return a byte array containing the data
   * @throws IOException if an IO Exception occurs
   */
  private byte[] getPart(InputStream in, int size) throws IOException {
    size = Math.min(size, in.available());
    logger.debug("available: {}", in.available());
    logger.debug("reading:   {}", size);
    byte[] result = new byte[size];
    in.read(result);
    return result;
  }

  /**
   * get the normalized amplitudes from given wave data
   * 
   * @param data the wave data
   * @param waveHeader the header of the wave file
   * @return an array containing the normalized amplitudes
   */
  private double[] getNormalizedAmplitudes(byte[] data, WaveHeader waveHeader) {
    double[] normalizedAmplitudes = null;
    if (normalizedAmplitudes == null) {

      boolean signed = true;

      // usually 8bit is unsigned
      if (waveHeader.getBitsPerSample() == 8) {
        signed = false;
      }

      short[] amplitudes = getSampleAmplitudes(data, waveHeader);
      int numSamples = amplitudes.length;
      int maxAmplitude = 1 << (waveHeader.getBitsPerSample() - 1);

      if (!signed) { // one more bit for unsigned value
        maxAmplitude <<= 1;
      }

      normalizedAmplitudes = new double[numSamples];
      for (int i = 0; i < numSamples; i++) {
        normalizedAmplitudes[i] = (double) amplitudes[i] / maxAmplitude;
      }
    }
    return normalizedAmplitudes;
  }

  /**
   * get the sample amplitudes from given data
   * 
   * @param data the wave data
   * @param waveHeader the wave header containing some additional info
   * @return an array containing the sample amplitudes
   */
  private short[] getSampleAmplitudes(byte[] data, WaveHeader waveHeader) {
    int bytePerSample = waveHeader.getBitsPerSample() / 8;
    int numSamples = data.length / bytePerSample;
    short[] amplitudes = new short[numSamples];

    int pointer = 0;
    for (int i = 0; i < numSamples; i++) {
      short amplitude = 0;
      for (int byteNumber = 0; byteNumber < bytePerSample; byteNumber++) {
        // little endian
        amplitude |= (short) ((data[pointer++] & 0xFF) << (byteNumber * 8));
      }
      amplitudes[i] = amplitude;
    }

    return amplitudes;
  }

  /**
   * Wave class encapsulates the wave data in a waveheader
   */
  class Wave {

    private WaveHeader waveHeader;
    private byte[] data; // little endian

    /**
     * Constructor
     * 
     */
    public Wave() {
      this.waveHeader = new WaveHeader();
      this.data = new byte[0];
    }

    /**
     * Constructor
     * 
     * @param filename Wave file
     */
    public Wave(String filename) {
      this(new File(filename));
    }

    /**
     * constructor
     * 
     * @param f the wav file
     */
    public Wave(File f) {
      try {
        InputStream inputStream = new FileInputStream(f);
        initWaveWithInputStream(inputStream);
        inputStream.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    /**
     * Constructor
     * 
     * @param inputStream Wave file input stream
     */
    public Wave(InputStream inputStream) {
      initWaveWithInputStream(inputStream);
    }

    /**
     * Constructor
     * 
     * @param WaveHeader waveHeader
     * @param byte[] data
     */
    public Wave(WaveHeader waveHeader, byte[] data) {
      this.waveHeader = waveHeader;
      this.data = data;
    }

    /**
     * init the object with an input stream
     * 
     * @param inputStream the inputstream to initialize from
     */
    private void initWaveWithInputStream(InputStream inputStream) {
      // reads the first 44 bytes for header
      waveHeader = new WaveHeader(inputStream);

      if (waveHeader.isValid()) {
        // load data
        try {
          data = new byte[inputStream.available()];
          inputStream.read(data);
        } catch (IOException e) {
          e.printStackTrace();
        }
        // end load data
      } else {
        logger.error("Invalid Wave Header");
      }
    }

    /**
     * Get the wave header
     * 
     * @return waveHeader
     */
    public WaveHeader getWaveHeader() {
      return waveHeader;
    }

  }

  /**
   * Class for encapsulating all information for the wave file
   */
  class WaveHeader {

    public static final String RIFF_HEADER = "RIFF";
    public static final String WAVE_HEADER = "WAVE";
    public static final String FMT_HEADER = "fmt ";
    public static final String DATA_HEADER = "data";
    public static final int HEADER_BYTE_LENGTH = 44; // 44 bytes for header

    private boolean valid;
    private String chunkId; // 4 bytes
    private long chunkSize; // unsigned 4 bytes, little endian
    private String format; // 4 bytes
    private String subChunk1Id; // 4 bytes
    private long subChunk1Size; // unsigned 4 bytes, little endian
    private int audioFormat; // unsigned 2 bytes, little endian
    private int channels; // unsigned 2 bytes, little endian
    private long sampleRate; // unsigned 4 bytes, little endian
    private long byteRate; // unsigned 4 bytes, little endian
    private int blockAlign; // unsigned 2 bytes, little endian
    private int bitsPerSample; // unsigned 2 bytes, little endian
    private String subChunk2Id; // 4 bytes
    private long subChunk2Size; // unsigned 4 bytes, little endian

    public WaveHeader() {
      // init a 8k 16bit mono wav
      chunkSize = 36;
      subChunk1Size = 16;
      audioFormat = 1;
      channels = 1;
      sampleRate = 8000;
      byteRate = 16000;
      blockAlign = 2;
      bitsPerSample = 16;
      subChunk2Size = 0;
      valid = true;
    }

    public WaveHeader(InputStream inputStream) {
      valid = loadHeader(inputStream);
    }

    private boolean loadHeader(InputStream inputStream) {

      byte[] headerBuffer = new byte[HEADER_BYTE_LENGTH];
      try {
        inputStream.read(headerBuffer);

        // read header
        int pointer = 0;
        chunkId = new String(new byte[] { headerBuffer[pointer++], headerBuffer[pointer++],
            headerBuffer[pointer++], headerBuffer[pointer++] });
        // little endian
        chunkSize = (long) (headerBuffer[pointer++] & 0xff)
            | (long) (headerBuffer[pointer++] & 0xff) << 8
            | (long) (headerBuffer[pointer++] & 0xff) << 16
            | (long) (headerBuffer[pointer++] & 0xff << 24);
        format = new String(new byte[] { headerBuffer[pointer++], headerBuffer[pointer++],
            headerBuffer[pointer++], headerBuffer[pointer++] });
        subChunk1Id = new String(new byte[] { headerBuffer[pointer++], headerBuffer[pointer++],
            headerBuffer[pointer++], headerBuffer[pointer++] });
        subChunk1Size = (long) (headerBuffer[pointer++] & 0xff)
            | (long) (headerBuffer[pointer++] & 0xff) << 8
            | (long) (headerBuffer[pointer++] & 0xff) << 16
            | (long) (headerBuffer[pointer++] & 0xff) << 24;
        audioFormat = (int) ((headerBuffer[pointer++] & 0xff) | (headerBuffer[pointer++] & 0xff) << 8);
        channels = (int) ((headerBuffer[pointer++] & 0xff) | (headerBuffer[pointer++] & 0xff) << 8);
        sampleRate = (long) (headerBuffer[pointer++] & 0xff)
            | (long) (headerBuffer[pointer++] & 0xff) << 8
            | (long) (headerBuffer[pointer++] & 0xff) << 16
            | (long) (headerBuffer[pointer++] & 0xff) << 24;
        byteRate = (long) (headerBuffer[pointer++] & 0xff)
            | (long) (headerBuffer[pointer++] & 0xff) << 8
            | (long) (headerBuffer[pointer++] & 0xff) << 16
            | (long) (headerBuffer[pointer++] & 0xff) << 24;
        blockAlign = (int) ((headerBuffer[pointer++] & 0xff) | (headerBuffer[pointer++] & 0xff) << 8);
        bitsPerSample = (int) ((headerBuffer[pointer++] & 0xff) | (headerBuffer[pointer++] & 0xff) << 8);
        subChunk2Id = new String(new byte[] { headerBuffer[pointer++], headerBuffer[pointer++],
            headerBuffer[pointer++], headerBuffer[pointer++] });
        subChunk2Size = (long) (headerBuffer[pointer++] & 0xff)
            | (long) (headerBuffer[pointer++] & 0xff) << 8
            | (long) (headerBuffer[pointer++] & 0xff) << 16
            | (long) (headerBuffer[pointer++] & 0xff) << 24;
        // end read header

        // the inputStream should be closed outside this method

        // dis.close();

      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }

      if (bitsPerSample != 8 && bitsPerSample != 16) {
        System.err.println("WaveHeader: only supports bitsPerSample 8 or 16");
        return false;
      }

      // check the format is support
      if (chunkId.toUpperCase().equals(RIFF_HEADER) && format.toUpperCase().equals(WAVE_HEADER)
          && audioFormat == 1) {
        return true;
      } else {
        System.err.println("WaveHeader: Unsupported header format");
      }

      return false;
    }

    public boolean isValid() {
      return valid;
    }

    public String getChunkId() {
      return chunkId;
    }

    public long getChunkSize() {
      return chunkSize;
    }

    public String getFormat() {
      return format;
    }

    public String getSubChunk1Id() {
      return subChunk1Id;
    }

    public long getSubChunk1Size() {
      return subChunk1Size;
    }

    public int getAudioFormat() {
      return audioFormat;
    }

    public int getChannels() {
      return channels;
    }

    public int getSampleRate() {
      return (int) sampleRate;
    }

    public int getByteRate() {
      return (int) byteRate;
    }

    public int getBlockAlign() {
      return blockAlign;
    }

    public int getBitsPerSample() {
      return bitsPerSample;
    }

    public String getSubChunk2Id() {
      return subChunk2Id;
    }

    public long getSubChunk2Size() {
      return subChunk2Size;
    }

    public void setSampleRate(int sampleRate) {
      int newSubChunk2Size = (int) (this.subChunk2Size * sampleRate / this.sampleRate);
      // if num bytes for each sample is even, the size of newSubChunk2Size also needed to be in
      // even number
      if ((bitsPerSample / 8) % 2 == 0) {
        if (newSubChunk2Size % 2 != 0) {
          newSubChunk2Size++;
        }
      }

      this.sampleRate = sampleRate;
      this.byteRate = sampleRate * bitsPerSample / 8;
      this.chunkSize = newSubChunk2Size + 36;
      this.subChunk2Size = newSubChunk2Size;
    }

    public void setChunkId(String chunkId) {
      this.chunkId = chunkId;
    }

    public void setChunkSize(long chunkSize) {
      this.chunkSize = chunkSize;
    }

    public void setFormat(String format) {
      this.format = format;
    }

    public void setSubChunk1Id(String subChunk1Id) {
      this.subChunk1Id = subChunk1Id;
    }

    public void setSubChunk1Size(long subChunk1Size) {
      this.subChunk1Size = subChunk1Size;
    }

    public void setAudioFormat(int audioFormat) {
      this.audioFormat = audioFormat;
    }

    public void setChannels(int channels) {
      this.channels = channels;
    }

    public void setByteRate(long byteRate) {
      this.byteRate = byteRate;
    }

    public void setBlockAlign(int blockAlign) {
      this.blockAlign = blockAlign;
    }

    public void setBitsPerSample(int bitsPerSample) {
      this.bitsPerSample = bitsPerSample;
    }

    public void setSubChunk2Id(String subChunk2Id) {
      this.subChunk2Id = subChunk2Id;
    }

    public void setSubChunk2Size(long subChunk2Size) {
      this.subChunk2Size = subChunk2Size;
    }

    public String toString() {

      StringBuffer sb = new StringBuffer();
      sb.append("chunkId: " + chunkId);
      sb.append("\n");
      sb.append("chunkSize: " + chunkSize);
      sb.append("\n");
      sb.append("format: " + format);
      sb.append("\n");
      sb.append("subChunk1Id: " + subChunk1Id);
      sb.append("\n");
      sb.append("subChunk1Size: " + subChunk1Size);
      sb.append("\n");
      sb.append("audioFormat: " + audioFormat);
      sb.append("\n");
      sb.append("channels: " + channels);
      sb.append("\n");
      sb.append("sampleRate: " + sampleRate);
      sb.append("\n");
      sb.append("byteRate: " + byteRate);
      sb.append("\n");
      sb.append("blockAlign: " + blockAlign);
      sb.append("\n");
      sb.append("bitsPerSample: " + bitsPerSample);
      sb.append("\n");
      sb.append("subChunk2Id: " + subChunk2Id);
      sb.append("\n");
      sb.append("subChunk2Size: " + subChunk2Size);
      return sb.toString();
    }
  }

  /**
   * set workspace
   * 
   * @param workspace the workspace
   */
  protected void setWorkspace(Workspace workspace) {
    this.workspace = workspace;
  }

  /**
   * set the ingestservice
   * 
   * @param ingestService the ingestservice
   */
  protected void setIngestService(IngestService ingestService) {
    this.ingestService = ingestService;
  }
}
