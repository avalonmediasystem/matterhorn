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
package org.opencastproject.usertracking.api;

/**
 * Provides user track capabilities to the engage tools, possibly to other services.
 */
public interface UserTrackingService {


  /**
   * Returns the views of a mediapackage
   * 
   * @param mediapackageId
   *          the mediapackeId
   * @return the views
   */
  int getViews(String mediapackageId);

  /**
   * Returns a report
   * 
   * @param from
   *          The from day key
   * @param to
   *          The to day key
   * @param offset
   *          the offset
   * @param limit
   *          the limit
   * @return the report
   */
  Report getReport(String from, String to, int offset, int limit);

  /**
   * Returns a report
   * 
   * @param offset
   *          the offset
   * @param limit
   *          the limit
   * @return the report
   */
  Report getReport(int offset, int limit);

  /**
   * Returns a list of footprints, if a userId is passed only the footprints of that user are returned.
   * 
   * @param mediapackageId
   *          The mediapackageId
   * @param userId
   *          The userId is optional
   * @return the footprintList
   */
  FootprintList getFootprints(String mediapackageId, String userId);


}
