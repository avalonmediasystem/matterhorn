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
package org.opencastproject.inspection.impl;

import org.opencastproject.inspection.api.MediaInspectionException;
import org.opencastproject.inspection.api.MediaInspectionService;
import org.opencastproject.inspection.impl.api.AudioStreamMetadata;
import org.opencastproject.inspection.impl.api.MediaAnalyzer;
import org.opencastproject.inspection.impl.api.MediaAnalyzerException;
import org.opencastproject.inspection.impl.api.MediaContainerMetadata;
import org.opencastproject.inspection.impl.api.VideoStreamMetadata;
import org.opencastproject.job.api.Job;
import org.opencastproject.job.api.Job.Status;
import org.opencastproject.job.api.JobProducer;
import org.opencastproject.mediapackage.MediaPackageElement;
import org.opencastproject.mediapackage.MediaPackageElement.Type;
import org.opencastproject.mediapackage.MediaPackageElementBuilder;
import org.opencastproject.mediapackage.MediaPackageElementBuilderFactory;
import org.opencastproject.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.mediapackage.MediaPackageException;
import org.opencastproject.mediapackage.MediaPackageElementParser;
import org.opencastproject.mediapackage.Stream;
import org.opencastproject.mediapackage.Track;
import org.opencastproject.mediapackage.UnsupportedElementException;
import org.opencastproject.mediapackage.track.AudioStreamImpl;
import org.opencastproject.mediapackage.track.TrackImpl;
import org.opencastproject.mediapackage.track.VideoStreamImpl;
import org.opencastproject.serviceregistry.api.ServiceRegistry;
import org.opencastproject.serviceregistry.api.ServiceRegistryException;
import org.opencastproject.serviceregistry.api.ServiceUnavailableException;
import org.opencastproject.util.Checksum;
import org.opencastproject.util.ChecksumType;
import org.opencastproject.util.MimeTypes;
import org.opencastproject.util.NotFoundException;
import org.opencastproject.util.UnknownFileTypeException;
import org.opencastproject.workspace.api.Workspace;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Inspects media via the 3rd party MediaInfo tool by default, and can be configured to use other media analyzers.
 */
public class MediaInspectionServiceImpl implements MediaInspectionService, JobProducer, ManagedService {

  private static final Logger logger = LoggerFactory.getLogger(MediaInspectionServiceImpl.class);

  /** List of available operations on jobs */
  private enum Operation {
    Inspect, Enrich
  };

  /** The inspect job operation name */
  public static final String INSPECT_URL = "inspect";

  protected Workspace workspace;
  protected ServiceRegistry serviceRegistry;
  protected Map<String, Object> analyzerConfig = new ConcurrentHashMap<String, Object>();
  protected MediaPackageElementBuilderFactory elementFactory = MediaPackageElementBuilderFactory.newInstance();

  public void activate() {
    analyzerConfig.put(MediaInfoAnalyzer.MEDIAINFO_BINARY_CONFIG, MediaInfoAnalyzer.MEDIAINFO_BINARY_DEFAULT);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @Override
  @SuppressWarnings("rawtypes")
  public void updated(Dictionary properties) throws ConfigurationException {
    if (properties == null)
      return;
    String path = StringUtils.trimToNull((String) properties.get(MediaInfoAnalyzer.MEDIAINFO_BINARY_CONFIG));
    if (path != null) {
      analyzerConfig.put(MediaInfoAnalyzer.MEDIAINFO_BINARY_CONFIG, path);
      logger.info("Setting the path to mediainfo to " + path);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.job.api.JobProducer#startJob(org.opencastproject.job.api.Job, java.lang.String,
   *      java.util.List)
   */
  @Override
  public void startJob(Job job, String operation, List<String> arguments) throws ServiceRegistryException {
    Operation op = null;
    try {
      op = Operation.valueOf(operation);

      switch (op) {
        case Inspect:
          URI uri = URI.create(arguments.get(0));
          inspect(job, uri);
          break;
        case Enrich:
          MediaPackageElement element = MediaPackageElementParser.getFromXml(arguments.get(0));
          boolean overwrite = Boolean.parseBoolean(arguments.get(1));
          enrich(job, element, overwrite);
          break;
        default:
          throw new IllegalStateException("Don't know how to handle operation '" + operation + "'");
      }
    } catch (IllegalArgumentException e) {
      throw new ServiceRegistryException("This service can't handle operations of type '" + op + "'");
    } catch (IndexOutOfBoundsException e) {
      throw new ServiceRegistryException("This argument list for operation '" + op + "' does not meet expectations");
    } catch (Exception e) {
      throw new ServiceRegistryException("Error handling operation '" + op + "'");
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.job.api.JobProducer#getJob(long)
   */
  public Job getJob(long id) throws NotFoundException, ServiceRegistryException {
    return serviceRegistry.getJob(id);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.job.api.JobProducer#getJobType()
   */
  @Override
  public String getJobType() {
    return JOB_TYPE;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.job.api.JobProducer#countJobs(org.opencastproject.job.api.Job.Status)
   */
  public long countJobs(Status status) throws ServiceRegistryException {
    if (status == null)
      throw new IllegalArgumentException("status must not be null");
    return serviceRegistry.count(JOB_TYPE, status);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.job.api.JobProducer#countJobs(org.opencastproject.job.api.Job.Status, java.lang.String)
   */
  public long countJobs(Status status, String host) throws ServiceRegistryException {
    if (status == null)
      throw new IllegalArgumentException("status must not be null");
    if (host == null)
      throw new IllegalArgumentException("host must not be null");
    return serviceRegistry.count(JOB_TYPE, status, host);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.inspection.api.MediaInspectionService#inspect(java.net.URI)
   */
  public Job inspect(URI uri) throws MediaInspectionException {
    try {
      return serviceRegistry.createJob(JOB_TYPE, Operation.Inspect.toString(), Arrays.asList(uri.toString()));
    } catch (ServiceUnavailableException e) {
      throw new MediaInspectionException("No service of type '" + JOB_TYPE + "' available", e);
    } catch (ServiceRegistryException e) {
      throw new MediaInspectionException(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.inspection.api.MediaInspectionService#enrich(org.opencastproject.mediapackage.MediaPackageElement,
   *      boolean)
   */
  @Override
  public Job enrich(final MediaPackageElement element, final boolean override) throws MediaInspectionException,
          MediaPackageException {
    try {
      return serviceRegistry.createJob(JOB_TYPE, Operation.Enrich.toString(),
              Arrays.asList(MediaPackageElementParser.getAsXml(element), Boolean.toString(override)));
    } catch (ServiceUnavailableException e) {
      throw new MediaInspectionException("No service of type '" + JOB_TYPE + "' available", e);
    } catch (ServiceRegistryException e) {
      throw new MediaInspectionException(e);
    }
  }

  /**
   * Inspects the element that is passed in as uri.
   * 
   * @param job
   *          the associated job
   * @param trackURI
   *          the elemtn uri
   * @return the inspected track
   * @throws MediaInspectionException
   *           if inspection fails
   */
  private Track inspect(Job job, URI trackURI) throws MediaInspectionException {
    logger.debug("inspect(" + trackURI + ") called, using workspace " + workspace);

    try {
      // Get the file from the URL (runtime exception if invalid)
      File file = null;
      try {
        file = workspace.get(trackURI);
      } catch (NotFoundException notFound) {
        throw new MediaInspectionException("Unable to find resource " + trackURI, notFound);
      } catch (IOException ioe) {
        throw new MediaInspectionException("Error reading " + trackURI + " from workspace", ioe);
      }

      // Make sure the file has an extension. Otherwise, tools like ffmpeg will not work.
      // TODO: Try to guess the extension from the container's metadata
      if ("".equals(FilenameUtils.getExtension(file.getName()))) {
        throw new MediaInspectionException("Can not inspect files without a filename extension");
      }

      MediaContainerMetadata metadata = getFileMetadata(file);
      if (metadata == null) {
        throw new MediaInspectionException("Media analyzer returned no metadata from " + file);
      } else {
        MediaPackageElementBuilder elementBuilder = MediaPackageElementBuilderFactory.newInstance().newElementBuilder();
        TrackImpl track;
        MediaPackageElement element;
        try {
          element = elementBuilder.elementFromURI(trackURI, Type.Track, null);
        } catch (UnsupportedElementException e) {
          throw new MediaInspectionException("Unable to create track element from " + file, e);
        }
        track = (TrackImpl) element;

        // Duration
        if (metadata.getDuration() != null)
          track.setDuration(metadata.getDuration());

        // Checksum
        try {
          track.setChecksum(Checksum.create(ChecksumType.DEFAULT_TYPE, file));
        } catch (IOException e) {
          throw new MediaInspectionException("Unable to read " + file, e);
        }

        // Mimetype
        try {
          track.setMimeType(MimeTypes.fromURL(file.toURI().toURL()));
        } catch (Exception e) {
          logger.info("Unable to find mimetype for {}", file.getAbsolutePath());
        }

        // Audio metadata
        try {
          addAudioStreamMetadata(track, metadata);
        } catch (Exception e) {
          throw new MediaInspectionException("Unable to extract audio metadata from " + file, e);
        }

        // Videometadata
        try {
          addVideoStreamMetadata(track, metadata);
        } catch (Exception e) {
          throw new MediaInspectionException("Unable to extract video metadata from " + file, e);
        }

        job.setPayload(MediaPackageElementParser.getAsXml(track));
        job.setStatus(Status.FINISHED);
        updateJob(job);
        return track;
      }
    } catch (Exception e) {
      logger.warn("Error inspecting " + trackURI, e);
      try {
        job.setStatus(Status.FAILED);
        updateJob(job);
      } catch (Exception failureToFail) {
        logger.warn("Unable to update job to failed state", failureToFail);
      }
      if (e instanceof MediaInspectionException) {
        throw (MediaInspectionException) e;
      } else {
        throw new MediaInspectionException(e);
      }
    }
  }

  /**
   * Enriches the given element's mediapackage.
   * 
   * @param job
   *          the associated job
   * @param element
   *          the element to enrich
   * @param override
   *          <code>true</code> to override existing metadata
   * @return the enriched element
   * @throws MediaInspectionException
   *           if enriching fails
   */
  private MediaPackageElement enrich(Job job, MediaPackageElement element, boolean override)
          throws MediaInspectionException {
    if (element instanceof Track) {
      final Track originalTrack = (Track) element;
      return enrichTrack(originalTrack, override, job);
    } else {
      return enrichElement(element, override, job);
    }
  }

  /**
   * Enriches the track's metadata and can be executed in an asynchronous way.
   * 
   * @param originalTrack
   *          the original track
   * @param override
   *          <code>true</code> to override existing metadata
   * @param job
   *          the job
   * @return the media package element
   * @throws MediaInspectionException
   */
  private MediaPackageElement enrichTrack(final Track originalTrack, final boolean override, final Job job)
          throws MediaInspectionException {

    try {
      URI originalTrackUrl = originalTrack.getURI();
      MediaPackageElementFlavor flavor = originalTrack.getFlavor();
      logger.debug("enrich(" + originalTrackUrl + ") called");

      // Get the file from the URL
      File file = null;
      try {
        file = workspace.get(originalTrackUrl);
      } catch (NotFoundException e) {
        throw new MediaInspectionException("File " + file + " was not found and can therefore not be inspected", e);
      } catch (IOException e) {
        throw new MediaInspectionException("Error accessing " + file, e);
      }

      // Make sure the file has an extension. Otherwise, tools like ffmpeg will not work.
      // TODO: Try to guess the extension from the container's metadata
      if (StringUtils.trimToNull(FilenameUtils.getExtension(file.getName())) == null) {
        throw new MediaInspectionException("Element " + file + " has no file extension");
      }

      MediaContainerMetadata metadata = getFileMetadata(file);
      if (metadata == null) {
        throw new MediaInspectionException("Unable to acquire media metadata for " + originalTrackUrl);
      } else if (metadata.getAudioStreamMetadata().size() == 0 && metadata.getVideoStreamMetadata().size() == 0) {
        throw new MediaInspectionException("File at " + originalTrackUrl + " does not seem to be a/v media");
      } else {
        TrackImpl track = null;
        try {
          track = (TrackImpl) MediaPackageElementBuilderFactory.newInstance().newElementBuilder()
                  .elementFromURI(originalTrackUrl, Type.Track, flavor);
        } catch (UnsupportedElementException e) {
          throw new MediaInspectionException("Unable to create track element from " + file, e);
        }

        // init the new track with old
        track.setChecksum(originalTrack.getChecksum());
        track.setDuration(originalTrack.getDuration());
        track.setElementDescription(originalTrack.getElementDescription());
        track.setFlavor(flavor);
        track.setIdentifier(originalTrack.getIdentifier());
        track.setMimeType(originalTrack.getMimeType());
        track.setReference(originalTrack.getReference());
        track.setSize(originalTrack.getSize());
        track.setURI(originalTrackUrl);
        for (String tag : originalTrack.getTags()) {
          track.addTag(tag);
        }

        // enrich the new track with basic info
        if (track.getDuration() == -1L || override)
          track.setDuration(metadata.getDuration());
        if (track.getChecksum() == null || override) {
          try {
            track.setChecksum(Checksum.create(ChecksumType.DEFAULT_TYPE, file));
          } catch (IOException e) {
            throw new MediaInspectionException("Unable to read " + file, e);
          }
        }

        // Add the mime type if it's not already present
        if (track.getMimeType() == null || override) {
          try {
            track.setMimeType(MimeTypes.fromURI(track.getURI()));
          } catch (UnknownFileTypeException e) {
            logger.info("Unable to detect the mimetype for track {} at {}", track.getIdentifier(), track.getURI());
          }
        }

        // find all streams
        Dictionary<String, Stream> streamsId2Stream = new Hashtable<String, Stream>();
        for (Stream stream : originalTrack.getStreams()) {
          streamsId2Stream.put(stream.getIdentifier(), stream);
        }

        // audio list
        try {
          addAudioStreamMetadata(track, metadata);
        } catch (Exception e) {
          throw new MediaInspectionException("Unable to extract audio metadata from " + file, e);
        }

        // video list
        try {
          addVideoStreamMetadata(track, metadata);
        } catch (Exception e) {
          throw new MediaInspectionException("Unable to extract video metadata from " + file, e);
        }

        job.setPayload(MediaPackageElementParser.getAsXml(track));
        job.setStatus(Status.FINISHED);
        updateJob(job);

        logger.info("Successfully inspected track {}", track);
        return track;
      }
    } catch (Exception e) {
      logger.warn("Error enriching track " + originalTrack, e);
      try {
        job.setStatus(Status.FAILED);
        updateJob(job);
      } catch (Exception failureToFail) {
        logger.warn("Unable to update job to failed state", failureToFail);
      }
      if (e instanceof MediaInspectionException) {
        throw (MediaInspectionException) e;
      } else {
        throw new MediaInspectionException(e);
      }
    }
  }

  /**
   * Enriches the media package element metadata such as the mimetype, the file size etc.
   * 
   * @param element
   *          the media package element
   * @param override
   *          <code>true</code> to overwrite existing metadata
   * @param job
   *          the associated job
   * @return the callable
   * @throws MediaInspectionException
   *           if enriching fails
   */
  private MediaPackageElement enrichElement(final MediaPackageElement element, final boolean override, final Job job)
          throws MediaInspectionException {
    try {
      File file;
      try {
        file = workspace.get(element.getURI());
      } catch (NotFoundException e) {
        throw new MediaInspectionException("Unable to find " + element.getURI() + " in the workspace", e);
      } catch (IOException e) {
        throw new MediaInspectionException("Error accessing " + element.getURI() + " in the workspace", e);
      }

      // Checksum
      if (element.getChecksum() == null || override) {
        try {
          element.setChecksum(Checksum.create(ChecksumType.DEFAULT_TYPE, file));
        } catch (IOException e) {
          throw new MediaInspectionException("Error generating checksum for " + element.getURI(), e);
        }
      }

      // Mimetype
      if (element.getMimeType() == null || override) {
        try {
          element.setMimeType(MimeTypes.fromURI(file.toURI()));
        } catch (UnknownFileTypeException e) {
          logger.info("unable to determine the mime type for {}", file.getName());
        }
      }

      job.setPayload(MediaPackageElementParser.getAsXml(element));
      job.setStatus(Status.FINISHED);
      updateJob(job);
      logger.info("Successfully inspected element {}", element);

      return element;
    } catch (Exception e) {
      logger.warn("Error enriching element " + element, e);
      try {
        job.setStatus(Status.FAILED);
        updateJob(job);
      } catch (Exception failureToFail) {
        logger.warn("Unable to update job to failed state", failureToFail);
      }
      if (e instanceof MediaInspectionException) {
        throw (MediaInspectionException) e;
      } else {
        throw new MediaInspectionException(e);
      }
    }
  }

  /**
   * Adds the video related metadata to the track.
   * 
   * @param track
   *          the track
   * @param metadata
   *          the container metadata
   * @throws Exception
   *           Media analysis is fragile, and may throw any kind of runtime exceptions due to inconsistencies in the
   *           media's metadata
   */
  protected Track addVideoStreamMetadata(TrackImpl track, MediaContainerMetadata metadata) throws Exception {
    List<VideoStreamMetadata> videoList = metadata.getVideoStreamMetadata();
    if (videoList != null && !videoList.isEmpty()) {
      for (int i = 0; i < videoList.size(); i++) {
        VideoStreamImpl video = new VideoStreamImpl("video-" + (i + 1));
        VideoStreamMetadata v = videoList.get(i);
        video.setBitRate(v.getBitRate());
        video.setFormat(v.getFormat());
        video.setFormatVersion(v.getFormatVersion());
        video.setFrameHeight(v.getFrameHeight());
        video.setFrameRate(v.getFrameRate());
        video.setFrameWidth(v.getFrameWidth());
        video.setScanOrder(v.getScanOrder());
        video.setScanType(v.getScanType());
        // TODO: retain the original video metadata
        track.addStream(video);
      }
    }
    return track;
  }

  /**
   * Adds the audio related metadata to the track.
   * 
   * @param track
   *          the track
   * @param metadata
   *          the container metadata
   * @throws Exception
   *           Media analysis is fragile, and may throw any kind of runtime exceptions due to inconsistencies in the
   *           media's metadata
   */
  protected Track addAudioStreamMetadata(TrackImpl track, MediaContainerMetadata metadata) throws Exception {
    List<AudioStreamMetadata> audioList = metadata.getAudioStreamMetadata();
    if (audioList != null && !audioList.isEmpty()) {
      for (int i = 0; i < audioList.size(); i++) {
        AudioStreamImpl audio = new AudioStreamImpl("audio-" + (i + 1));
        AudioStreamMetadata a = audioList.get(i);
        audio.setBitRate(a.getBitRate());
        audio.setChannels(a.getChannels());
        audio.setFormat(a.getFormat());
        audio.setFormatVersion(a.getFormatVersion());
        audio.setBitDepth(a.getResolution());
        audio.setSamplingRate(a.getSamplingRate());
        // TODO: retain the original audio metadata
        track.addStream(audio);
      }
    }
    return track;
  }

  /**
   * Asks the media analyzer to extract the file's metadata.
   * 
   * @param file
   *          the file
   * @return the file container metadata
   * @throws MediaInspectionException
   *           if metadata extraction fails
   */
  private MediaContainerMetadata getFileMetadata(File file) throws MediaInspectionException {
    if (file == null) {
      throw new IllegalArgumentException("file to analyze cannot be null");
    }
    MediaContainerMetadata metadata = null;
    try {
      MediaAnalyzer analyzer = new MediaInfoAnalyzer();
      analyzer.setConfig(analyzerConfig);
      metadata = analyzer.analyze(file);
    } catch (MediaAnalyzerException e) {
      throw new MediaInspectionException(e);
    }
    return metadata;
  }

  /**
   * Updates the job in the service registry. The exceptions that are possibly been thrown are wrapped in a
   * {@link MediaInspectionException}.
   * 
   * @param job
   *          the job to update
   * @throws MediaInspectionException
   *           the exception that is being thrown
   */
  private void updateJob(Job job) throws MediaInspectionException {
    try {
      serviceRegistry.updateJob(job);
    } catch (NotFoundException notFound) {
      throw new MediaInspectionException("Unable to find job " + job, notFound);
    } catch (ServiceUnavailableException e) {
      throw new MediaInspectionException("No service of type '" + JOB_TYPE + "' available", e);
    } catch (ServiceRegistryException serviceRegException) {
      throw new MediaInspectionException("Unable to update job '" + job + "' in service registry", serviceRegException);
    }
  }

  protected void setWorkspace(Workspace workspace) {
    logger.debug("setting " + workspace);
    this.workspace = workspace;
  }

  protected void setServiceRegistry(ServiceRegistry jobManager) {
    this.serviceRegistry = jobManager;
  }

}
