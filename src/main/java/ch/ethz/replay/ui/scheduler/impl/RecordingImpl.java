/*

 RecordingImpl.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 10, 2008

 Copyright (c) 2007 ETH Zurich, Switzerland

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

 */

package ch.ethz.replay.ui.scheduler.impl;

import ch.ethz.replay.ui.scheduler.Attachment;
import ch.ethz.replay.ui.scheduler.DeviceType;
import ch.ethz.replay.ui.scheduler.Location;
import ch.ethz.replay.ui.scheduler.Person;
import ch.ethz.replay.ui.scheduler.Recording;
import ch.ethz.replay.ui.scheduler.RecordingSeries;
import ch.ethz.replay.ui.scheduler.RecordingStatus;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Implementation of {@link ch.ethz.replay.ui.scheduler.Recording}.
 * 
 * 
 */
@Entity(name = "Recording")
public class RecordingImpl extends BaseEntity implements Recording {

  @ManyToOne(targetEntity = RecordingSeriesImpl.class)
  @JoinColumn(name = "RecordingSeriesId")
  private RecordingSeries series;

  @Id
  @GeneratedValue
  private String recordingId = null;

  @Column
  protected String title;

  @ManyToOne(targetEntity = SimpleLocation.class)
  @JoinColumn(name = "LocationId", nullable = false)
  private Location location;

  @Column(nullable = false)
  @Temporal(value = TemporalType.TIMESTAMP)
  private Date startDate;

  @Column(nullable = false)
  @Temporal(value = TemporalType.TIMESTAMP)
  private Date endDate;

  @Column(nullable = false)
  private RecordingStatus status = RecordingStatus.created;

  @ManyToMany(targetEntity = PersonImpl.class)
  @JoinTable(name = "Recording_Person", joinColumns = @JoinColumn(name = "recordingId"), inverseJoinColumns = @JoinColumn(name = "personId"))
  private Set<Person> contactPersons = new TreeSet<Person>();

  @ManyToMany(targetEntity = AttachmentImpl.class)
  @JoinTable(name = "Recording_Attachment", joinColumns = @JoinColumn(name = "recordingId"), inverseJoinColumns = @JoinColumn(name = "attachmentId"))
  private Set<Attachment> attachments = new HashSet<Attachment>();

  @ManyToMany(targetEntity = DeviceTypeImpl.class, fetch = FetchType.EAGER)
  @JoinTable(name = "Recording_DeviceType", joinColumns = @JoinColumn(name = "recordingId"), inverseJoinColumns = @JoinColumn(name = "deviceId"))
  private Set<DeviceType> devices = new HashSet<DeviceType>();

  /**
   * Will be updated automatically when persisted
   */
  @Temporal(value = TemporalType.TIMESTAMP)
  protected Date lastUpdated;

  /**
   * No-arg constructor required by JPA
   */
  public RecordingImpl() {
  }

  public RecordingImpl(Location location, Date startDate, Date endDate) {
    if (location == null)
      throw new IllegalArgumentException("Location must not be null");
    if (startDate == null)
      throw new IllegalArgumentException("Start date must not be null");
    if (endDate == null)
      throw new IllegalArgumentException("End date must not be null");

    this.location = location;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public void attach(Attachment attachment) {
    attachments.add(attachment);
  }

  public void detach(Attachment attachment) {
    attachments.remove(attachment);
  }

  public RecordingSeries getSeries() {
    return series;
  }

  void setSeries(RecordingSeries series) {
    this.series = series;
  }

  public boolean isPartOfSeries() {
    return series != null;
  }

  /**
   * @return the recordingId
   */
  public String getRecordingId() {
    return recordingId;
  }

  /**
   * @param recordingId
   *          the recordingId to set
   */
  public void setRecordingId(String recordingId) {
    this.recordingId = recordingId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    if (location == null)
      throw new IllegalArgumentException("Location must not be null");
    this.location = location;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date date) {
    if (date == null)
      throw new IllegalArgumentException("Start date must not be null");
    this.startDate = date;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date date) {
    if (date == null)
      throw new IllegalArgumentException("End date must not be null");
    this.endDate = date;
  }

  public boolean isBygone() {
    return endDate.getTime() < System.currentTimeMillis();
  }

  public RecordingStatus getStatus() {
    return status;
  }

  public void setStatus(RecordingStatus status) {
    this.status.checkTransition(status);
    this.status = status;
  }

  public Set<Person> getContactPersons() {
    return contactPersons;
  }

  public void setContactPersons(Set<Person> persons) {
    if (persons == null)
      this.contactPersons.clear();
    else
      this.contactPersons = persons;
  }

  public void addContactPerson(Person person) {
    if (person == null) {
      throw new IllegalArgumentException("person is null");
    }
    contactPersons.add(person);
  }

  public Collection<Attachment> getAttachments() {
    return attachments;
  }

  public Set<DeviceType> getDevices() {
    return devices;
  }

  public void setDevices(Set<DeviceType> devices) {
    if (devices == null)
      this.devices.clear();
    else
      this.devices = devices;
  }

  public void addDevice(DeviceType device) {
    if (device == null) {
      throw new IllegalArgumentException("device is null");
    }
    devices.add(device);
  }

  /**
   * @return the lastUpdated
   */
  public Date getLastUpdated() {
    return lastUpdated;
  }

  /**
   * @param lastUpdated
   *          the lastUpdated to set
   */
  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof RecordingImpl))
      return false;
    // The job ID is defined to be non null and unique so it's an ideal candidate for equality.
    return recordingId.equals(((RecordingImpl) o).getRecordingId());
  }

  @Override
  public int hashCode() {
    // See equals()
    return recordingId.hashCode();
  }
}
