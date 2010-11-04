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
package org.opencastproject.scheduler.api;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Event provides methods and properties belonging to single events. It contains {@link Metadata), as well as JAXB and
 * JPA Annotations.
 */
public interface Event {

  /**
   * @return The recurring event that this event belongs to, or null if there is no associated recurring event.
   */
  RecurringEvent getRecurringEvent();

  /**
   * @param recurringEvent
   *          The recurring event that this event belongs to
   */
  void setRecurringEvent(RecurringEvent recurringEvent);

  /**
   * Gets the start date for this event.
   * 
   * @return the start date
   */
  Date getStartDate();

  void setStartDate(Date date);
  
  /**
   * Gets the stop date for this event.
   * 
   * @return the stop date
   */
  Date getStopDate();

  void setStopDate(Date date);
  
  /**
   * Update a specific metadata field in the Event.
   * 
   * @param data
   */
  void updateMetadata(Metadata data);

  /**
   * @return This events Id.
   */
  String getEventId();

  /**
   * @param eventId
   */
  void setEventId(String eventId);

  /**
   * @return List containing this event's metadata
   */
  List<Metadata> getMetadata();

  /**
   * @param metadata
   */
  void setMetadata(List<Metadata> metadata);

  /**
   * @param key
   *          The name of a specific metadata field
   * @return The value of a specific metadata field in the metadataTable
   */
  String getValue(String key);

  /**
   * @return Set of all metadata keys
   */
  Set<String> getKeySet();

  /**
   * @param key
   * @return True if a specific metadata key exists in the metadataTable
   */
  boolean containsKey(String key);

  /**
   * @param key
   * @return A specific metadata field in the metadata list for this event (not metadataTable)
   */
  Metadata findMetadata(String key);

  /**
   * Add new metadata to this event's metadata list.
   * 
   * @param m
   */
  void addMetadata(Metadata m);

  /**
   * Remove a specific metadata field from this event's metadata list.
   * 
   * @param m
   */
  void removeMetadata(Metadata m);

}
