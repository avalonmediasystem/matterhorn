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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * An Event has a unique ID, a relation to the recurring event from which it was created and a set of metadata. Even the
 * start- and end-time is stored in the set of metadata, with the keys "timeStart" and "timeEnd" as long value converted
 * to string. Resources and Attendees are store in the metadata too, as
 */

@NamedQueries({
        @NamedQuery(name = "Event.getAll", query = "SELECT e FROM Event e"),
        @NamedQuery(name = "Event.getByDate", query = "SELECT e FROM Event e where e.startDate > :rangeIn and e.stopDate < :rangeOut") })
@XmlRootElement(name = "event")
@XmlAccessorType(XmlAccessType.NONE)
@Entity(name = "Event")
@Access(AccessType.FIELD)
@Table(name = "SCHED_EVENT")
public class EventImpl extends AbstractEvent implements Event {

  public EventImpl() {
  }

  private static final Logger logger = LoggerFactory.getLogger(Event.class);

  @XmlID
  @Id
  @GeneratedValue
  @Column(name = "ID", length = 128)
  protected String eventId;

  @Transient
  @XmlTransient
  RecurringEvent recurringEvent = null;

  @Column
  @Temporal(TemporalType.TIMESTAMP)
  @XmlElement(name = "start")
  protected Date startDate;

  @Column
  @Temporal(TemporalType.TIMESTAMP)
  @XmlElement(name = "stop")
  protected Date stopDate;

  // FIXME: Do we really need a join table here? How about a composite key (event id + metadata key) in the metadata
  // table?
  @XmlElementWrapper(name = "metadataList")
  @XmlElement(name = "metadata")
  @OneToMany(fetch = FetchType.EAGER, targetEntity = MetadataImpl.class, cascade = CascadeType.ALL)
  @JoinTable(name = "SCHED_EVENT_METADATA", joinColumns = { @JoinColumn(name = "EVENT_ID") }, inverseJoinColumns = { @JoinColumn(name = "METADATA_ID") })
  protected List<MetadataImpl> metadata = new LinkedList<MetadataImpl>();

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.opencastproject.scheduler.api.Event#updateMetadata()
   */
  @Override
  public void updateMetadata(Metadata data) {
    for (MetadataImpl md : metadata) {
      if (md.getKey().equals(data.getKey())) {
        md.setValue(data.getValue());
        return;
      }
    }
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.opencastproject.scheduler.api.Event#getEventId()
   */
  @Override
  public String getEventId() {
    return eventId;
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.opencastproject.scheduler.api.Event#setEventId()
   */
  @Override
  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.opencastproject.scheduler.api.Event#getRecurringEvent()
   */
  @Override
  public RecurringEvent getRecurringEvent() {
    return recurringEvent;
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.opencastproject.scheduler.api.Event#setRecurringEvent()
   */
  @Override
  public void setRecurringEvent(RecurringEvent recurringEvent) {
    this.recurringEvent = recurringEvent;
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.opencastproject.scheduler.api.Event#getMetadata()
   */
  @Override
  public List<Metadata> getMetadata() {
    List<Metadata> list = new LinkedList<Metadata>();
    for (Metadata m : metadata)
      list.add(m);
    return list;
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.opencastproject.scheduler.api.Event#setMetadata()
   */
  @Override
  public void setMetadata(List<Metadata> metadata) {
    this.metadata = new LinkedList<MetadataImpl>();
    for (Metadata m : metadata)
      this.metadata.add((MetadataImpl) m);
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.opencastproject.scheduler.api.Event#getRecurringEventId()
   */
  @Override
  public Metadata findMetadata(String key) {
    for (Metadata m : metadata) {
      if (m.getKey().equals(key))
        return m;
    }
    return null;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("EventImpl: ");
    result.append(this.eventId);
    if (recurringEvent != null)
      result.append(", Recurring Event: ").append(recurringEvent);
    else
      result.append(", no recuring event");
    for (Metadata data : metadata) {
      result.append(", ").append(data);
    }
    return result.toString();
  }

  /**
   * valueOf function is called by JAXB to bind values. This function calls the ScheduleEvent factory.
   * 
   * @param xmlString
   *          string representation of an event.
   * @return instantiated event SchdeulerEventJaxbImpl.
   */
  public static EventImpl valueOf(String xmlString) throws Exception {
    return SchedulerBuilder.getInstance().parseEvent(xmlString);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Event))
      return false;
    Event e = (Event) o;
    if (e.getEventId() != this.getEventId())
      return false;
    for (Metadata m : metadata) {
      if (!e.containsKey(m.getKey()) || (!e.getValue(m.getKey()).equals(m.getValue())))
        return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return this.getEventId().hashCode();
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.opencastproject.scheduler.api.Event#addMetadata()
   */
  @Override
  public void addMetadata(Metadata m) {
    this.metadata.add((MetadataImpl) m);
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.opencastproject.scheduler.api.Event#removeMetadata()
   */
  @Override
  public void removeMetadata(Metadata m) {
    this.metadata.remove(m);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.scheduler.api.Event#getStartDate()
   */
  @Override
  public Date getStartDate() {
    return stopDate;
  }

  /**
   * @param startDate
   *          the startDate to set
   */
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.scheduler.api.Event#getStopDate()
   */
  @Override
  public Date getStopDate() {
    return stopDate;
  }

  /**
   * @param stopDate
   *          the stopDate to set
   */
  public void setStopDate(Date stopDate) {
    this.stopDate = stopDate;
  }

}
