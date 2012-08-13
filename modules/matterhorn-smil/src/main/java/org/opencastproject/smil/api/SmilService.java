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
package org.opencastproject.smil.api;

import org.opencastproject.smil.entity.MediaElement;
import org.opencastproject.smil.entity.ParallelElement;
import org.opencastproject.smil.entity.Smil;
import org.opencastproject.util.NotFoundException;

public interface SmilService {

  /**
   * create a new SMIL document based on the workflowID
   * 
   * @param workflowId the worklfowId
   * @return a new SMIL document
   * @throws SmilException if an error occurs
   */
  Smil createNewSmil(long workflowId) throws SmilException, NotFoundException;

  /**
   * retrieve a SMIL document based on the workflowID
   * 
   * @param workflowId the worklfowId
   * @return the SMIL document
   * @throws SmilException if an error occurs
   * @throws NotFoundException if the SMIL document could not be found
   */
  Smil getSmil(long worklfowId) throws SmilException, NotFoundException;

  /**
   * add a media element to a specified element
   * 
   * @param workflowId the id of the workflow the SMIL document belongs to
   * @param e the element to add
   * @param elementId the id of the element the media element should be added to
   * @return the updated SMIL document
   * @throws SmilException if an error occurs
   * @throws NotFoundException if the SMIL document could not be found
   */
  Smil addMediaElement(long workflowId, MediaElement e, String elementId) throws SmilException,
      NotFoundException;

  /**
   * 
   * @param workflowId the id of the workflow the SMIL document belongs to
   * @param p the parallelElement to add
   * @return the updated SMIL document
   * @throws SmilException if an error occurs
   * @throws NotFoundException if the SMIL document could not be found
   */
  Smil addParallelElement(long workflowId, ParallelElement p) throws SmilException,
      NotFoundException;

}