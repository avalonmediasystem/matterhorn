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
package org.opencastproject.series.api;

import org.opencastproject.metadata.dublincore.DublinCoreCatalog;
import org.opencastproject.metadata.dublincore.DublinCoreCatalogList;
import org.opencastproject.security.api.AccessControlList;
import org.opencastproject.util.NotFoundException;

/**
 * Series service API for creating, removing and searching over series.
 * 
 */
public interface SeriesService {

  /**
   * Adds or updates series. IllegalArgumentException is thrown if dc argument is null.
   * 
   * @param dc
   *          {@link DublinCoreCatalog} representing series
   * @return Dublin Core catalog of newly created series or null if series Dublin Core was just updated
   * @throws SeriesException
   *           if adding or updating fails
   */
  DublinCoreCatalog updateSeries(DublinCoreCatalog dc) throws SeriesException;

  /**
   * Updates access control rules for specified series. Not specifying series ID or trying to update series with null
   * value will throw IllegalArgumentException.
   * 
   * @param seriesID
   *          series to be updated
   * @param accessControl
   *          {@link AccessControlList} defining access control rules
   * @return true if ACL was updated and false it if was created
   * @throws NotFoundException
   *           if series with given ID cannot be found
   * @throws SeriesException
   *           if exception occurred
   */
  boolean updateAccessControl(String seriesID, AccessControlList accessControl) throws NotFoundException,
          SeriesException;

  /**
   * Removes series
   * 
   * @param seriesID
   *          ID of the series to be removed
   * @throws SeriesException
   *           if deleting fails
   * @throws NotFoundException
   *           if series with specified ID does not exist
   */
  void deleteSeries(String seriesID) throws SeriesException, NotFoundException;

  /**
   * Returns Dublin core representing series by series ID.
   * 
   * @param seriesID
   *          series to be retrieved
   * @return {@link DublinCoreCatalog} representing series
   * @throws SeriesException
   *           if retrieving fails
   */
  DublinCoreCatalog getSeries(String seriesID) throws SeriesException, NotFoundException;

  /**
   * Returns access control rules for series with given ID.
   * 
   * @param seriesID
   *          ID of the series for which access control rules will be retrieved
   * @return {@link AccessControlList} defining access control rules
   * @throws NotFoundException
   *           if series with given ID cannot be found
   * @throws SeriesException
   *           if exception occurred
   */
  AccessControlList getSeriesAccessControl(String seriesID) throws NotFoundException, SeriesException;

  /**
   * Search over series
   * 
   * @param query
   *          {@link SeriesQuery} representing query
   * @return List of all matching series
   * @throws SeriesException
   *           if query could not be performed
   */
  DublinCoreCatalogList getSeries(SeriesQuery query) throws SeriesException;
  
  int getSeriesCount() throws SeriesException;
}
