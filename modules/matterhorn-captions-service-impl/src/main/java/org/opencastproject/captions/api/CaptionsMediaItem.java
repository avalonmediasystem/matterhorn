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

package org.opencastproject.captions.api;

import org.opencastproject.media.mediapackage.MediaPackage;

import java.net.URI;

public interface CaptionsMediaItem {
  public String getWorkflowId();
  public MediaPackage getMediaPackage();
  public String getMediaPackageId();
  public String getTitle();
  public URI getMediaURI();
  /**
   * @param captionType the caption type string from {@link CaptionsService#CAPTIONS_TYPE_TIMETEXT}
   * @return the url OR null if none found
   */
  public URI getCaptionsURI(String captionType);
}

