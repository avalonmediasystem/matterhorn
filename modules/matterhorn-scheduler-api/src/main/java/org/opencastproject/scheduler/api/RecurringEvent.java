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

/**
 * Recurring Event
 *
 */
public interface RecurringEvent {

  /**
   * @return This recurring event's Id.
   */
  String getRecurringEventId();

  /**
   * @param rEventId
   */
  void setRecurringEventId(String rEventId);

  /**
   * @return The recurrence rule, {@link http://www.ietf.org/rfc/rfc2445.txt section 4.8.5}
   */
  String getRecurrence();

  void setRecurrence(String recurrence);

  /**
   * @return the start date for the recurrence
   */
  Date getRecurrenceStart();
  
  void setRecurrenceStart(Date start);

  /**
   * @return the stop date for the recurrence
   */
  Date getRecurrenceStop();
  
  void setRecurrenceStop(Date start);

  /**
   * @return The list of metadata for this recurring event
   */
  List<Metadata> getMetadata();

  /**
   * @param metadata
   */
  void setMetadata(List<Metadata> metadata);

  /**
   * @return Get the list of events that are children of this recurring event.  If the events have been modified since
   * they were first created, the events may not match the recurrence rule.
   */
  List<Event> getEvents();

  /**
   * @param key
   * @return True if a specific metadata key exists in the metadataTable
   */
  boolean containsKey(String key);

  /**
   * Update a specific metadata item
   * @param data
   */
  void updateMetadata(Metadata data);

  /**
   * Add new metadata to this recurring event's metadata list.
   * @param m
   */
  void addMetadata(Metadata m);
  
  /**
   * Remove a specific metadata field from this recurring event's metadata list.
   * @param m
   */
  void removeMetadata(Metadata m);
  
}
