package org.matterhorn.sidebyside.api;

import org.opencastproject.mediapackage.Track;

/**
 * API for a service that, given a certain set of tracks from a Mediapackage, merges them into a single video 
 */
public interface SideBySideService {
  
  /**
   * Processes the input parameters to create the video composition 
   */
  Track process();
}
