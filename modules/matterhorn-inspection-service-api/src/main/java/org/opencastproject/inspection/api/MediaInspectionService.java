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
package org.opencastproject.inspection.api;

import org.opencastproject.media.mediapackage.MediaPackageElement;
import org.opencastproject.receipt.api.Receipt;

import java.net.URI;

/**
 * Anayzes media to determine its technical metadata.
 */
public interface MediaInspectionService {
  /**
   * The namespace distinguishing media inspection receipts from other types
   */
  String RECEIPT_TYPE = "org.opencastproject.inspection";

  /**
   * Inspect a track based on a given uri to the track and put the gathered data into the track
   * 
   * @param uri the uri to a track in a media package
   * @param block whether this operation should block the calling thread, or return the receipt immediately without
   *  the inspected track.
   * @return the updated track OR null if no metadata can be found
   * @throws IllegalStateException if the analyzer cannot be loaded
   * @throws RuntimeException if there is a failure during media package update
   */
  Receipt inspect(URI uri, boolean block);

  /**
   * Equip an existing media package element with automatically generated metadata
   * 
   * @param original
   *          The original media package element that will be inspected
   * @param override
   *          In case of conflict between existing and automatically obtained metadata this switch selects preference.
   *          False..The original metadata will be kept, True..The new metadata will be used.
   * @return the updated track OR null if no metadata found
   * @throws IllegalStateException if the analyzer cannot be loaded
   * @throws RuntimeException if there is a failure during media package update
   */
  Receipt enrich(MediaPackageElement original, boolean override, boolean block);

  /**
   * Gets the receipt with a given ID.  This can be used to retrieve the status and outcome of a non-blocking call to
   * {@link #inspect(URI, boolean)} or {@link #enrich(MediaPackageElement, boolean, boolean)}.
   * @param id The id of the receipt
   * @return The receipt, or null if not found
   */
  Receipt getReceipt(String id);
}
