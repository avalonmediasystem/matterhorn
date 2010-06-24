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
package org.opencastproject.workspace.api;

import org.opencastproject.util.NotFoundException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Provides efficient access java.io.File objects from potentially remote URIs. This helper service prevents different
 * service implementations running in the same osgi container from downloading remote files multiple times.
 * 
 * Additionally, when the system is configured to use shared storage, this performance gain is also achieved across
 * distributed osgi containers. The methods from WorkingFileRepository are also available as a convenience to clients.
 */
public interface Workspace {

  /**
   * Gets a locally cached {@link File} for the given URI.
   * 
   * @param uri
   * @return The locally cached file
   * @throws NotFoundException
   *           if the file does not exist
   * @throws IOException
   *           if reading the file from the workspace fails
   */
  File get(URI uri) throws NotFoundException, IOException;

  /**
   * Store the data stream under the given media package and element IDs, specifying a filename.
   * 
   * @param mediaPackageID
   * @param mediaPackageElementID
   * @param fileName
   * @param in
   * @throws IOException
   *           if writing the data to the workspace fails
   */
  URI put(String mediaPackageID, String mediaPackageElementID, String fileName, InputStream in) throws IOException;

  /**
   * Stores the data stream in the given collection, overwriting any data with the same collection id and file name.
   * 
   * @param collectionId
   *          The collection to use for storing this data
   * @param fileName
   *          the filename to use in the collection.
   * @param in
   *          the inputstream
   * @return the URI of the stored data
   * @throws IOException
   *           if writing the data to the workspace fails
   */
  URI putInCollection(String collectionId, String fileName, InputStream in) throws IOException;

  /**
   * Gets the URIs of the members of this collection
   * 
   * @param collectionId
   *          the collection identifier
   * @return the URIs for each member of the collection
   * @throws IOException
   *           if reading the collection contents fails
   */
  URI[] getCollectionContents(String collectionId) throws IOException;

  /**
   * Removes a file from a collection
   * 
   * @param collectionId
   *          the collection identifier
   * @param fileName
   *          the filename to remove
   * @throws NotFoundException
   *           if there was not file stored under this combination of mediapackage and element IDs.
   * @throws IOException
   *           if deleting the data from the workspace fails
   */
  void deleteFromCollection(String collectionId, String fileName) throws NotFoundException, IOException;

  /**
   * Delete the file stored at the given media package and element IDs.
   * 
   * @param mediaPackageID
   * @param mediaPackageElementID
   * @throws NotFoundException
   *           if there was not file stored under this combination of mediapackage and element IDs.
   * @throws IOException
   *           if deleting the data from the workspace fails
   */
  void delete(String mediaPackageID, String mediaPackageElementID) throws NotFoundException, IOException;

  /**
   * Get the URL for a file stored under the given media package and element IDs. MediaPackages may reference elements
   * that are not yet stored in the working file repository, so this method will return a URI even if the file is not
   * yet stored.
   * 
   * @param mediaPackageID
   * @param mediaPackageElementID
   * @return the URI to the file
   */
  URI getURI(String mediaPackageID, String mediaPackageElementID);

  /**
   * Get the URL for a file stored under the given collection.
   * 
   * @param collectionID
   *          the collection id
   * @param fileName
   *          the file name
   * @return the file's uri
   */
  URI getCollectionURI(String collectionID, String fileName);

}
