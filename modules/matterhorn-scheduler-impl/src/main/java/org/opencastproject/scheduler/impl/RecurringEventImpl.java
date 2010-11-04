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

package org.opencastproject.scheduler.impl;

import org.opencastproject.scheduler.api.Event;
import org.opencastproject.scheduler.api.Metadata;
import org.opencastproject.scheduler.api.RecurringEvent;
import org.opencastproject.scheduler.endpoint.SchedulerBuilder;

import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.RRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

@NamedQueries({ @NamedQuery(name = "RecurringEvent.getAll", query = "SELECT e FROM RecurringEvent e") })
@XmlRootElement(name = "recurringEvent")
@XmlAccessorType(XmlAccessType.NONE)
@Entity(name = "RecurringEvent")
@Table(name = "SCHED_R_EVENT")
@Access(AccessType.FIELD)
public class RecurringEventImpl extends AbstractEvent implements RecurringEvent {
  private static final Logger logger = LoggerFactory.getLogger(RecurringEventImpl.class);

  @XmlID
  @Id
  @GeneratedValue
  @Column(name = "ID", length = 128)
  @XmlElement(name = "recurringEventId")
  protected String rEventId;

  @XmlElement(name = "recurrence")
  @Column(name = "RECURRENCE")
  protected String recurrence;

  @XmlElement(name = "start")
  @Column(name = "START")
  protected Date recurrenceStart;

  @XmlElement(name = "stop")
  @Column(name = "STOP")
  protected Date recurrenceStop;

  @XmlElementWrapper(name = "metadataList")
  @XmlElement(name = "metadata")
  @OneToMany(fetch = FetchType.EAGER, targetEntity = MetadataImpl.class, cascade = CascadeType.ALL)
  @JoinTable(name = "SCHED_R_EVENT_METADATA", joinColumns = { @JoinColumn(name = "REC_EVENT_ID") }, inverseJoinColumns = { @JoinColumn(name = "MD_ID") })
  protected List<MetadataImpl> metadata = null;

  @XmlElementWrapper(name = "events")
  @XmlElement(name = "event")
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinTable(name = "SCHED_R_EVENT_ITEM", joinColumns = { @JoinColumn(name = "REC_EVENT_ID") }, inverseJoinColumns = { @JoinColumn(name = "EVENT_ID") })
  protected List<EventImpl> events = null;

  public RecurringEventImpl() {
  }

  @Override
  public String getRecurringEventId() {
    return rEventId;
  }

  @Override
  public void setRecurringEventId(String rEventId) {
    this.rEventId = rEventId;
  }

  @Override
  public String getRecurrence() {
    return recurrence;
  }

  public void setRecurrence(String recurrence) {
    this.recurrence = recurrence;
  }

  @Override
  public List<Metadata> getMetadata() {
    List<Metadata> list = new LinkedList<Metadata>();
    for (Metadata m : metadata)
      list.add(m);
    return list;
  }

  @Override
  public void setMetadata(List<Metadata> metadata) {
    metadataTable = null;
    this.metadata = new LinkedList<MetadataImpl>();
    for (Metadata m : metadata)
      this.metadata.add((MetadataImpl) m);
  }

  public List<Event> getEvents() {
    List<Event> list = new ArrayList<Event>(events.size());
    for (Event e : events)
      list.add(e);
    return list;
  }

  public List<Date> generateDates() throws IllegalStateException {
    LinkedList<Date> generatedDates = new LinkedList<Date>();
    if (recurrence == null) {
      throw new IllegalStateException("Could not generate events because of missing recurrence pattern");
    }
    try {
      Recur recur = new RRule(recurrence).getRecur();
      logger.debug("Recur: {}", recur);
      logger.debug("Recur start: {}", recurrenceStart);
      if (recurrenceStart == null)
        recurrenceStart = new Date(System.currentTimeMillis());
      if (recurrenceStop == null) {
        throw new IllegalStateException("No end date specified for recurring event {}. " + rEventId);
      }

      DateTime seed = new DateTime(recurrenceStart.getTime());
      seed.setUtc(true);
      DateTime period = new DateTime(recurrenceStop.getTime());
      period.setUtc(true);
      DateList dates = recur.getDates(seed, period, Value.DATE_TIME);
      logger.debug("DateList: {}", dates);
      for (Object date : dates) {
        Date d = (Date) date;
        logger.info("Date: {}", d);
        // Adjust for DST, if start of event
        // Create timezone based on CA's reported TZ.
        TimeZone tz = null;
        if (!this.getValue("agentTimeZone").isEmpty()) {
          tz = TimeZone.getTimeZone(this.getValue("agentTimeZone"));
        }
        if (tz == null) { // No timezone was present, assume the serve's local timezone.
          tz = TimeZone.getDefault();
        }
        if (tz.inDaylightTime(seed)) { // Event starts in DST
          if (!tz.inDaylightTime(d)) {// Date not in DST?
            d.setTime(d.getTime() + tz.getDSTSavings()); // Ajust for Fall back one hour
          }
        } else { // Event doesn't start in DST
          if (tz.inDaylightTime(d)) {
            d.setTime(d.getTime() - tz.getDSTSavings()); // Adjust for Spring forward one hour
          }
        }
        generatedDates.add(d);
      }
    } catch (ParseException e) {
      throw new IllegalStateException("Could not parse recurrence " + recurrence);
    }
    return generatedDates;
  }

  public static RecurringEventImpl find(String recurringEventId, EntityManagerFactory emf) {
    logger.debug("loading recurring event with the ID {}", recurringEventId);
    if (recurringEventId == null || emf == null) {
      logger.warn("could not find reccuring event {}. Null Pointer exeption", recurringEventId);
      return null;
    }
    EntityManager em = emf.createEntityManager();
    RecurringEventImpl e = null;
    try {
      e = em.find(RecurringEventImpl.class, recurringEventId);
    } finally {
      em.close();
    }
    return e;
  }

  @Override
  public String toString() {
    String result = "Recurring Event " + rEventId + ", pattern: " + recurrence + ", generated events: "
            + System.getProperty("line.separator");
    for (Event e : events)
      result += e.toString() + System.getProperty("line.separator");
    return result;
  }

  @Override
  public void updateMetadata(Metadata data) {
    if (containsKey(data.getKey())) {
      for (Metadata olddata : getMetadata()) {
        if (olddata.getKey().equals(data.getKey())) {
          olddata.setValue(data.getValue());
          break;
        }
      }
    } else {
      this.metadata.add((MetadataImpl) data);
    }
    metadataTable = null;
  }

  /**
   * valueOf function is called by JAXB to bind values. This function calls the ScheduleEvent factory.
   * 
   * @param xmlString
   *          string representation of an event.
   * @return instantiated event SchdeulerEventJaxbImpl.
   */
  public static RecurringEventImpl valueOf(String xmlString) throws Exception {
    return SchedulerBuilder.getInstance().parseRecurringEvent(xmlString);
  }

  @Override
  public void addMetadata(Metadata m) {
    this.metadata.add((MetadataImpl) m);
  }

  @Override
  public void removeMetadata(Metadata m) {
    this.metadata.remove(m);
  }

  /**
   * @return the recurrenceStart
   */
  public Date getRecurrenceStart() {
    return recurrenceStart;
  }

  /**
   * @param recurrenceStart
   *          the recurrenceStart to set
   */
  public void setRecurrenceStart(Date recurrenceStart) {
    this.recurrenceStart = recurrenceStart;
  }

  /**
   * @return the recurrenceStop
   */
  public Date getRecurrenceStop() {
    return recurrenceStop;
  }

  /**
   * @param recurrenceStop
   *          the recurrenceStop to set
   */
  public void setRecurrenceStop(Date recurrenceStop) {
    this.recurrenceStop = recurrenceStop;
  }
}
