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

import net.fortuna.ical4j.model.ValidationException;

import org.opencastproject.util.NotFoundException;
import org.opencastproject.workflow.api.WorkflowDefinition;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public interface SchedulerService {

  /** The metadata key used to store the workflow identifier in an event's metadata */
  String WORKFLOW_INSTANCE_ID_KEY = "org.opencastproject.workflow.id";

  /** The metadata key used to store the workflow definition in an event's metadata */
  String WORKFLOW_DEFINITION_ID_KEY = "org.opencastproject.workflow.definition";

  /** The schedule workflow operation identifier */
  String SCHEDULE_OPERATION_ID = "schedule";

  /** The workflow operation property that stores the event start time, as milliseconds since 1970 */
  String WORKFLOW_OPERATION_KEY_SCHEDULE_START = "schedule.start";

  /** The workflow operation property that stores the event stop time, as milliseconds since 1970 */
  String WORKFLOW_OPERATION_KEY_SCHEDULE_STOP = "schedule.stop";

  /** The workflow operation property that stores the event location */
  String WORKFLOW_OPERATION_KEY_SCHEDULE_LOCATION = "schedule.location";

  WorkflowDefinition getPreProcessingWorkflowDefinition() throws IllegalStateException;

  /**
   * Persist an event
   * 
   * @param event
   *          the event to add
   * 
   * @return The event that has been persisted
   */
  Event addEvent(Event event) throws SchedulerException;

  /**
   * Persist a recurring event
   * 
   * @param RecurringEvent
   *          e
   * @return The recurring event that has been persisted
   */
  void addRecurringEvent(Event recurrence) throws SchedulerException;

  /**
   * Gets an event by its identifier
   * 
   * @param eventId
   *          the event identifier
   * @return An event that matches eventId
   * @throws IllegalArgumentException
   *           if the eventId is null
   * @throws IllegalStateException
   *           if the entity manager factory is not available
   * @throws NotFoundException
   *           if no event with this identifier exists
   */
  Event getEvent(Long eventId) throws NotFoundException;

  /**
   * @param filter
   * @return List of events that match the supplied filter, or all events if no filter is supplied
   */
  List<Event> getEvents(SchedulerFilter filter);

  /**
   * @return A list of all events
   */
  List<Event> getAllEvents();

  /**
   * @return List of all events that start after the current time.
   */
  List<Event> getUpcomingEvents();

  /**
   * @param list
   * @return The list of events in a list of events that occur after the current time.
   */
  List<Event> getUpcomingEvents(List<Event> list);

  /**
   * @param Long
   *          the eventId of the event to be removed.
   * @throws NotFoundException
   *           If the eventId cannot be found.
   */
  void removeEvent(Long eventID) throws NotFoundException;

  /**
   * Updates an event.
   * 
   * @param e
   *          The event
   * @throws NotFoundException
   *           if the event hasn't previously been saved
   * @throws SchedulerException
   *           if the event's persistent representation can not be updated
   */
  void updateEvent(Event e) throws NotFoundException, SchedulerException;

  /**
   * Updates an event.
   * 
   * @param e
   *          The event
   * @param updateWorkflow
   *          Whether to also update the associated workflow for this event
   * @throws SchedulerException
   *           if the scheduled event can not be persisted
   * @throws NotFoundException
   *           if this event hasn't previously been saved
   */
  void updateEvent(Event e, boolean updateWorkflow) throws NotFoundException, SchedulerException;

  /**
   * Updates an event.
   * 
   * @param e
   *          The event
   * @param updateWorkflow
   *          Whether to also update the associated workflow for this event
   * @param updateWithEmptyValues
   *          Overwrite stored event's fields with null if provided event's fields are null
   * @throws SchedulerException
   *           if the scheduled event can not be persisted
   * @throws NotFoundException
   *           if this event hasn't previously been saved
   */
  void updateEvent(Event e, boolean updateWorkflow, boolean updateWithEmptyValues) throws NotFoundException,
          SchedulerException;

  /**
   * Updates each event with an id in the list with the passed event.
   * 
   * @param eventIdList
   *          List of event ids.
   * @param e
   *          Event containing metadata to be updated.
   */
  void updateEvents(List<Long> eventIdList, Event e) throws NotFoundException, SchedulerException;

  /**
   * Updates each event in the list with the passed event.
   * 
   * @param eventList
   *          List of event ids.
   * @param e
   *          Event containing metadata to be updated.
   * @param updateWithEmptyValues
   *          if the passed event contains empty values, overwrite the existing event with them.
   */
  void updateEvents(List<Event> eventList, Event e, boolean updateWithEmptyValues) throws NotFoundException,
          SchedulerException;

  /**
   * @param device
   * @param startDate
   * @param endDate
   * @return A list of events that conflict with the start, or end dates of provided event.
   */
  List<Event> findConflictingEvents(String device, Date startDate, Date endDate);

  /**
   * @param device
   * @param rrule
   * @param duration
   * @return A list of events that conflict with the start, or end dates of provided event.
   */
  List<Event> findConflictingEvents(String device, String deviceTZ, String rrule, Date startDate, Date endDate, Long duration)
          throws ParseException, ValidationException;

  /**
   * 
   * @param captureAgentID
   *          The name of the capture agent
   * @return An iCalendar containing all of the events for the specified capture agent.
   */

  String getCalendarForCaptureAgent(String captureAgentID);

  /**
   * 
   * @param eventID
   * @return The DublinCore metadata document of an event
   * @throws NotFoundException
   */
  String getDublinCoreMetadata(Long eventID) throws NotFoundException;

  /**
   * 
   * @param eventID
   * @return
   * @throws NotFoundException
   */
  String getCaptureAgentMetadata(Long eventID) throws NotFoundException;

  /**
   * @return An empty Event
   */
  Event getNewEvent();

  /**
   * resolves the appropriate Filter for the Capture Agent
   * 
   * @param captureAgentID
   *          The ID as provided by the capture agent
   * @return the Filter for this capture Agent.
   */
  SchedulerFilter getFilterForCaptureAgent(String captureAgentID);

  /**
   * Gets the last modified date for a capture agent's calendar. If no events exist for this capture agent, this method
   * returns null.
   * 
   * @param captureAgentId
   *          the identifier for a capture agent. this maps to the {@link Event#getDevice()} method.
   * @return the date the schedule for this capture agent was last changed, or null if there is no capture agent with
   *         this id, or there are no events for this capture agent.
   * @throws SchedulerException
   *           if the scheduling database is unavailable
   */
  Date getScheduleLastModified(String captureAgentId) throws SchedulerException;
}
