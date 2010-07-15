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

import org.osgi.service.cm.ConfigurationException;

import java.util.Dictionary;
import java.util.List;

public interface SchedulerService {
  
  public SingleEvent addEvent(SingleEvent e);
  
  public RecurringEvent addRecurringEvent(RecurringEvent e);
  
  public SingleEvent getEvent(String eventID);

  public RecurringEvent getRecurringEvent(String recurringEventID);
  
  public SingleEvent [] getEvents (SchedulerFilter filter);
  
  public SingleEvent [] getAllEvents ();
  
  public RecurringEvent [] getAllRecurringEvents();  
  
  public SingleEvent [] getUpcomingEvents();
  
  public List<? extends SingleEvent> getUpcomingEvents (List<? extends SingleEvent> list);
  
  /**
   * {@inheritDoc}
   * @see org.opencastproject.scheduler.impl.SchedulerServiceImpl#removeEvent(java.lang.String)
   */
  public boolean removeEvent(String eventID);
  
  public boolean removeRecurringEvent(String rEventID);
  
  public boolean updateEvent(Event e); 
  
  public boolean updateRecurringEvent(RecurringEvent e);  
  
  public SingleEvent[] findConflictingEvents (SingleEvent e);
  
  public SingleEvent[] findConflictingEvents (RecurringEvent rEvent) throws IncompleteDataException;
  
  /**
   * {@inheritDoc}
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  public void updated(Dictionary properties) throws ConfigurationException;
  
  /**
   * {@inheritDoc}
   * @see org.opencastproject.scheduler.api.SchedulerService#getCalendarForCaptureAgent(java.lang.String)
   */
  public String getCalendarForCaptureAgent(String captureAgentID);
  
  /**
   * {@inheritDoc}
   * @see org.opencastproject.scheduler.api.SchedulerService#getDublinCoreMetadata(java.lang.String)
   */
  public String getDublinCoreMetadata (String eventID);
  
  /**
   * {@inheritDoc}
   * @see org.opencastproject.scheduler.api.SchedulerService#getDublinCoreMetadata(java.lang.String)
   */
  public String getCaptureAgentMetadata (String eventID); 

  public SchedulerFilter getNewSchedulerFilter ();
  
  public SingleEvent getNewEvent ();
}
