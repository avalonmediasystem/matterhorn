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

import org.opencastproject.analysis.api.MediaAnalysisException;
import org.opencastproject.analysis.api.MediaAnalysisServiceSupport;
import org.opencastproject.analysis.speech.sphinx4.S4SpeechRecognizerThreaded;
import org.opencastproject.composer.api.ComposerService;
import org.opencastproject.mediapackage.Catalog;
import org.opencastproject.mediapackage.MediaPackageElement;
import org.opencastproject.mediapackage.MediaPackageElementBuilderFactory;
import org.opencastproject.mediapackage.MediaPackageElements;
import org.opencastproject.mediapackage.MediaPackageReferenceImpl;
import org.opencastproject.mediapackage.Track;
import org.opencastproject.metadata.mpeg7.AudioSegment;
import org.opencastproject.metadata.mpeg7.AudioVisual;
import org.opencastproject.metadata.mpeg7.MediaTime;
import org.opencastproject.metadata.mpeg7.MediaTimeImpl;
import org.opencastproject.metadata.mpeg7.Mpeg7Catalog;
import org.opencastproject.metadata.mpeg7.Mpeg7CatalogImpl;
import org.opencastproject.metadata.mpeg7.Mpeg7CatalogService;
import org.opencastproject.metadata.mpeg7.TemporalDecomposition;
import org.opencastproject.remote.api.Job;
import org.opencastproject.remote.api.Receipt;
import org.opencastproject.remote.api.RemoteServiceManager;
import org.opencastproject.remote.api.Receipt.Status;
import org.opencastproject.workspace.api.Workspace;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

/**
 * Media analysis service that takes takes an audio track and returns text as extracted from that track.
 */
public class SpeechAnalyzer extends MediaAnalysisServiceSupport {

  /** Receipt type */
  public static final String JOB_TYPE = "org.opencastproject.analysis.speech";

  /** Resulting collection in the working file repository */
  public static final String COLLECTION_ID = "speechtext";

  /** The configuration key for setting the number of worker threads */
  public static final String CONFIG_THREADS = "spechanalyzer.threads";

  /** The default worker thread pool size to use if no configuration is specified */
  public static final int DEFAULT_THREADS = 1;

  /** Name of the sphinx4 configuration file */
  public static final String SPHINX4_CONFIG = "sphinx4-config.xml";

  /** Path to the default acoustic model */
  public static final String SPHINX4_ACOUSTIC_MODEL = "edu/cmu/sphinx/model/acoustic/TIDIGITS_8gau_13dCep_16k_40mel_130Hz_6800Hz";

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SpeechAnalyzer.class);

  /** Reference to the receipt service */
  private RemoteServiceManager remoteServiceManager = null;

  /**  The mpeg-7 service */
  protected Mpeg7CatalogService mpeg7CatalogService = null;
  
  /** The composer service */
  protected ComposerService composer = null;
  
  /** The workspace to ue when retrieving remote media files */
  protected Workspace workspace = null;

  /** The executor service used to queue and run jobs */
  private ExecutorService executor;

  /** This server's base URL */
  private String serverUrl = null;

  /**
   * Creates a new speech analzer.
   */
  public SpeechAnalyzer() {
    super(MediaPackageElements.TEXTS);
  }

  /**
   * Callback from OSGi DS that will be executed on component activation.
   * 
   * @param cc
   *          the component context
   */
  protected void activate(ComponentContext cc) {
    // set up threading
    int threads = -1;
    String configredThreads = (String) cc.getBundleContext().getProperty(CONFIG_THREADS);
    // try to parse the value as a number. If it fails to parse, there is a config problem so we throw an exception.
    if (configredThreads == null) {
      threads = DEFAULT_THREADS;
    } else {
      threads = Integer.parseInt(configredThreads);
    }
    if (threads < 1) {
      throw new IllegalStateException("The text analyzer needs one or more threads to function.");
    }
    setExecutorThreads(threads);

    // register as a handler for "org.opencastproject.analysis.text" jobs
    serverUrl = (String) cc.getBundleContext().getProperty("org.opencastproject.server.url");
    remoteServiceManager.registerService(JOB_TYPE, serverUrl);
  }

  /**
   * Callback from OSGi DS that will be executed on component deactivation.
   */
  protected void deactivate() {
    remoteServiceManager.unRegisterService(JOB_TYPE, serverUrl);
  }

  /**
   * Separating this from the activate method so it's easier to test
   */
  void setExecutorThreads(int threads) {
    executor = Executors.newFixedThreadPool(threads);
    logger.info("Thread pool size = {}", threads);
  }

  /**
   * Starts text extraction on the audio track and returns a receipt containing the final result in the form of a
   * {@link import org.opencastproject.metadata.mpeg7.Mpeg7Catalog}.
   * 
   * @param element
   *          the element to analyze
   * @param mediapackageId
   *          the media package identifier
   * @param elementId
   *          element identifier
   * @param block
   *          <code>true</code> to make this operation synchronous
   * @return a receipt containing the resulting mpeg-7 catalog
   * @throws MediaAnalysisException
   */
  public Receipt analyze(final MediaPackageElement element, boolean block) throws MediaAnalysisException {
    final RemoteServiceManager rs = remoteServiceManager;
    final Receipt receipt = rs.createReceipt(JOB_TYPE);

    // Make sure that we are looking at a track
    if (!(element instanceof Track)) {
      logger.warn("Speech analyzer can't handle elements other than tracks");
      receipt.setStatus(Status.FAILED);
      rs.updateReceipt(receipt);
      return receipt;
    }

    final Track track = (Track) element;
    final URI trackUri = track.getURI();

    // Check if there is an audio track
    if (!track.hasAudio()) {
      logger.warn("The movie {} doesn't contain an audio track", track);
      receipt.setStatus(Status.FAILED);
      rs.updateReceipt(receipt);
      return receipt;
    }

    Runnable command = new Runnable() {
      @SuppressWarnings("unchecked")
      public void run() {
        receipt.setStatus(Status.RUNNING);
        rs.updateReceipt(receipt);

        Mpeg7CatalogImpl mpeg7 = Mpeg7CatalogImpl.newInstance();

        try {

          logger.info("Starting speech extraction from {}", trackUri);

          File imageFile = workspace.get(trackUri);
          AudioSegment[] segments = analyze(imageFile);

          // Create a temporal decomposition
          MediaTime mediaTime = new MediaTimeImpl(0, 0);
          AudioVisual avContent = mpeg7.addAudioVisualContent(element.getIdentifier(), mediaTime, null);
          TemporalDecomposition<AudioSegment> temporalDecomposition = (TemporalDecomposition<AudioSegment>) avContent
                  .getTemporalDecomposition();

          // Add segments
          for (AudioSegment segment : segments) {
            AudioSegment s = temporalDecomposition.createSegment(segment.getIdentifier());
            s.setMediaTime(mediaTime);
          }

          logger.info("Text extraction of {} finished", track.getURI());

          MediaPackageElement mpeg7Catalog = MediaPackageElementBuilderFactory.newInstance().newElementBuilder()
                  .newElement(Catalog.TYPE, MediaPackageElements.SPEECH);
          URI uri = uploadMpeg7(mpeg7);
          mpeg7Catalog.setURI(uri);
          mpeg7Catalog.setReference(new MediaPackageReferenceImpl(element));

          receipt.setElement(mpeg7Catalog);
          receipt.setStatus(Status.FINISHED);
          rs.updateReceipt(receipt);

          logger.info("Finished text extraction of {}", trackUri);

        } catch (MediaAnalysisException e) {
          receipt.setStatus(Status.FAILED);
          rs.updateReceipt(receipt);
          throw e;
        } catch (Exception e) {
          receipt.setStatus(Status.FAILED);
          rs.updateReceipt(receipt);
          throw new MediaAnalysisException(e);
        }
      }
    };

    Future<?> future = executor.submit(command);
    if (block) {
      try {
        future.get();
      } catch (Exception e) {
        receipt.setStatus(Status.FAILED);
        remoteServiceManager.updateReceipt(receipt);
        throw new MediaAnalysisException(e);
      }
    }
    return receipt;
  }

  /**
   * Stores the mpeg-7 catalog in the working file repository.
   * 
   * @param catalog
   *          the catalog
   * @return the catalog's URI in the working file repository
   * @throws TransformerFactoryConfigurationError
   *           if serializing the catalog to xml fails
   * @throws IOException
   *           if writing the catalog to the working file repository fails
   * @throws ParserConfigurationException
   *           if the xml parser is not set up correctly
   * @throws TransformerException
   *           if creating the xml representation from the dom tree fails
   * @throws URISyntaxException
   *           if the working file repository created an invalid uri
   */
  protected URI uploadMpeg7(Mpeg7Catalog catalog) throws TransformerFactoryConfigurationError,
          TransformerException, ParserConfigurationException, IOException, URISyntaxException {
    InputStream in = mpeg7CatalogService.serialize(catalog);
    return workspace.putInCollection(COLLECTION_ID, UUID.randomUUID().toString(), in);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.remote.api.JobProducer#getJob(java.lang.String)
   */
  public Job getJob(String id) {
    return remoteServiceManager.getJob(id);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.remote.api.JobProducer#countJobs(org.opencastproject.remote.api.Job.Status)
   */
  public long countJobs(Status status) {
    if (status == null)
      throw new IllegalArgumentException("status must not be null");
    return remoteServiceManager.count(JOB_TYPE, status);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.remote.api.JobProducer#countJobs(org.opencastproject.remote.api.Job.Status,
   *      java.lang.String)
   */
  public long countJobs(Status status, String host) {
    if (status == null)
      throw new IllegalArgumentException("status must not be null");
    if (host == null)
      throw new IllegalArgumentException("host must not be null");
    return remoteServiceManager.count(JOB_TYPE, status, host);
  }

  /**
   * Returns the video text element for the given image.
   * 
   * @param audioTrack
   *          the audio track
   * @return the segments found in the track
   * @throws IOException
   *           if accessing the track fails
   * @throws UnsupportedAudioFileException
   */
  protected AudioSegment[] analyze(File audioTrack) throws IOException, UnsupportedAudioFileException {
    List<AudioSegment> segments = new ArrayList<AudioSegment>();

    int numJobs = 1;
    JobManager jobManager = new JobManager(numJobs);
    URL configURL = getClass().getClassLoader().getResource(SPHINX4_CONFIG);
    URL dictionary = getClass().getClassLoader().getResource(SPHINX4_ACOUSTIC_MODEL);
    SpeechRecognizer recognizer = new S4SpeechRecognizerThreaded(configURL, dictionary);
    jobManager.addSpeechRecognizer(recognizer);
    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioTrack.toURI().toURL());
    jobManager.doJob(audioInputStream);
    jobManager.waitFor();
    Set<SpeechResult> results = jobManager.getResults();
    
    // TODO: Create audio segments

    return segments.toArray(new AudioSegment[segments.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.analysis.api.MediaAnalysisService#getAnalysisType()
   */
  @Override
  public String getAnalysisType() {
    return JOB_TYPE;
  }

  /**
   * Sets the receipt service
   * 
   * @param remoteServiceManager
   *          the receipt service
   */
  public void setRemoteServiceManager(RemoteServiceManager remoteServiceManager) {
    this.remoteServiceManager = remoteServiceManager;
  }

  /**
   * Sets the mpeg7CatalogService
   * 
   * @param mpeg7CatalogService
   *          an instance of the mpeg7 catalog service
   */
  public void setMpeg7CatalogService(Mpeg7CatalogService mpeg7CatalogService) {
    this.mpeg7CatalogService = mpeg7CatalogService;
  }

  /**
   * Sets the workspace
   * 
   * @param workspace
   *          an instance of the workspace
   */
  public void setWorkspace(Workspace workspace) {
    this.workspace = workspace;
  }

  /**
   * Sets the composer service.
   * 
   * @param composerService
   */
  public void setComposerService(ComposerService composerService) {
    this.composer = composerService;
  }

}
