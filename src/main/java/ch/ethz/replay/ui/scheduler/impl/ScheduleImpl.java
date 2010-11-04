/*
 
 ScheduleImpl.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Oct 7, 2008

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

import ch.ethz.replay.core.api.common.bundle.DublinCoreCatalog;
import ch.ethz.replay.core.api.common.job.JobTicket;
import ch.ethz.replay.ui.scheduler.*;
import ch.ethz.replay.ui.scheduler.impl.persistence.*;
import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Todo {@link #schedule(ch.ethz.replay.ui.scheduler.Recording)} uses merge() to implement conversation needed by
 * components like the VVZ connection. Check, if this is a suitable strategy, or if it would be better to extend the
 * persistence context by implementing the session-per-conversion pattern, like the open-session-in-view pattern.
 * 
 * 
 */
public class ScheduleImpl implements Schedule {

  private static final Logger Log = Logger.getLogger(ScheduleImpl.class);

  private int maxPersonResultSize = 20;
  private int maxLocationResultSize = 20;

  //

  private RecordingDao recordingDao;
  private AttachmentDao attachmentDao;
  private RecordingSeriesDao recordingSeriesDao;
  private DeviceTypeDaoImpl deviceTypeDao;
  private PersonDao personDao;
  private PersonDao localPersonDao;
  private LocationDao locationDao;

  //

  public void setDeviceTypeDao(DeviceTypeDaoImpl deviceTypeDao) {
    this.deviceTypeDao = deviceTypeDao;
  }

  /**
   * Set the DAO that gives combined access to all person data sources.
   */
  public void setPersonDao(PersonDao personDao) {
    this.personDao = personDao;
  }

  /**
   * Set the DAO that gives access only to the locally stored persons.
   */
  public void setLocalPersonDao(PersonDao localPersonDao) {
    this.localPersonDao = localPersonDao;
  }

  public void setRecordingDao(RecordingDao recordingDao) {
    this.recordingDao = recordingDao;
  }

  public void setAttachmentDao(AttachmentDao attachmentDao) {
    this.attachmentDao = attachmentDao;
  }

  public void setRecordingSeriesDao(RecordingSeriesDao recordingSeriesDao) {
    this.recordingSeriesDao = recordingSeriesDao;
  }

  public void setLocationDao(LocationDao locationDao) {
    this.locationDao = locationDao;
  }

  //

  @Transactional
  public Recording schedule(Recording recording) {
    if (recording.getStatus().isNew()) {
      recording.setStatus(RecordingStatus.scheduled);
    }
    saveRecording(recording);
    return recording;
  }

  @Transactional
  private void saveRecording(Recording recording) {
    // Locations have a unique constraint on their name, so replace them
    Location location = recording.getLocation();
    List<Location> dbLocation = locationDao.findByExample(location);
    if (dbLocation.size() == 1)
      recording.setLocation(dbLocation.get(0));
    recordingDao.save(recording);
  }

  @Transactional
  public RecordingSeries schedule(RecordingSeries series) {
    for (Recording r : series.getRecordings()) {
      if (r.getStatus().isNew()) {
        r.setStatus(RecordingStatus.scheduled);
      }
    }
    recordingSeriesDao.save(series);
    // Persitence events aren't cascaded to the contained recordings
    Set<Recording> recordings = series.getRecordings();
    if (recordings != null) {
      for (Recording recording : recordings) {
        saveRecording(recording);
      }
    }
    return series;
  }

  @Transactional
  public void removeRecording(Long recordingId) {
    Recording recording = recordingDao.get(recordingId);
    if (recording != null) {
      RecordingSeries series = recording.getSeries();
      if (series != null) {
        series = recordingSeriesDao.merge(series);
        series.removeRecording(recording);
      }
      recordingDao.delete(recording);
      recording.setStatus(RecordingStatus.created);
    }
  }

  public List<Recording> findAllRecordings() {
    return recordingDao.findAll();
  }

  @Transactional
  public void removeSeries(Long seriesId) {
    RecordingSeries series = recordingSeriesDao.get(seriesId);
    if (seriesId != null) {
      try {
        for (Recording r : series.getRecordings()) {
          r.setStatus(RecordingStatus.created);
        }
      } catch (RuntimeException e) {
        // todo reset to old status
        throw e;
      }
    }
  }

  @Transactional
  public void removeRecordingFromSeries(Recording recording) {
    RecordingSeries currentSeries = recording.getSeries();
    if (currentSeries != null) {
      // Reattach to session because we want to access a lazily loaded collection
      currentSeries = recordingSeriesDao.merge(currentSeries);
      currentSeries.removeRecording(recording);
    }
  }

  public Recording getRecording(Long id) {
    return recordingDao.get(id);
  }

  public List<Recording> findRecordings(Recording example) {
    return recordingDao.findByExample(example);
  }

  public RecordingSeries getRecordingSeries(Long id) {
    return recordingSeriesDao.get(id);
  }

  public List<Recording> findRecordings(RecordingFilter filter) {
    return recordingDao.findBy(filter);
  }

  public List<RecordingSeries> findAllRecordingSeries() {
    RecordingSeriesImpl example = new RecordingSeriesImpl();
    example.setVisible(true);
    return recordingSeriesDao.findByExample(example);
  }

  public RecordingSeries findRecordingSeriesBySeriesId(String seriesId) {
    RecordingSeriesImpl example = new RecordingSeriesImpl(seriesId);
    List<RecordingSeries> r = recordingSeriesDao.findByExample(example);
    if (r.size() > 1)
      throw new RuntimeException("Constraint error: Series ID should be unique");
    return r.size() == 1 ? r.get(0) : null;
  }

  public void saveRecordingSeries(RecordingSeries series) {
    recordingSeriesDao.save(series);
  }

  public void hideRecordingSeries(RecordingSeries series) {
    ((RecordingSeriesImpl) series).setVisible(false);
    recordingSeriesDao.save(series);
  }

  public Date getLastModificationDateOfSchedule(Location location) {
    if (location == null) {
      throw new IllegalArgumentException("Please provide a location");
    }
    return recordingDao.getLastModificationDate(location.getId());
  }

  public Location getLocation(Long id) {
    return locationDao.get(id);
  }

  public List<Location> findLocationsByName(String name) {
    return locationDao.findBy(new SimpleLocationFilter(name, maxLocationResultSize));
  }

  public Location findLocationByName(String name) {
    SimpleLocationFilter filter = new SimpleLocationFilter(name, maxLocationResultSize);
    filter.setExactMatch(true);
    List<Location> result = locationDao.findBy(filter);
    if (result.size() > 1)
      throw new RuntimeException("Constraint error: Location names must be unique");
    return result.size() == 1 ? result.get(0) : null;
  }

  public List<Location> findAllScheduledLocations() {
    return recordingDao.getAllScheduledLocations();
  }

  public Attachment getAttachment(Long id) {
    return attachmentDao.get(id);
  }

  public <T extends Attachment> List<T> findAttachmentByType(Class<T> type) {
    Attachment example;
    if (JobTicketAttachment.class.isAssignableFrom(type)) {
      example = new JobTicketAttachmentImpl();
    } else if (DublinCoreAttachment.class.isAssignableFrom(type)) {
      example = new DublinCoreAttachmentImpl();
    } else {
      example = new DocumentAttachmentImpl();
    }
    return (List<T>) attachmentDao.findByExample(example);
  }

  public Person getPerson(Long id) {
    return personDao.get(id);
  }

  public Person savePerson(Person person) {
    return personDao.save(person);
  }

  @Transactional
  public void removePerson(Long id) {
    Person person = personDao.get(id);
    if (person != null) {
      recordingDao.removeContactPersonFromRecordings(person);
      personDao.delete(person);
    }
  }

  public List<Person> findAllPersons() {
    return localPersonDao.findByExample(new PersonImpl());
  }

  public List<Person> findPersons(Person example) {
    return personDao.findByExample(example);
  }

  /**
   * This implementation returns only the first 20 found persons.
   */
  public List<Person> findPersonsByName(String name) {
    return personDao.findBy(new SimplePersonFilter(name, maxPersonResultSize));
  }

  public List<DeviceType> getAvailableDeviceTypes() {
    return deviceTypeDao.findAll();
  }

  public DeviceType getDeviceType(Long id) {
    return deviceTypeDao.get(id);
  }

  /**
   * Registers a new device type. Already saved type will be gently ignored.
   */
  public DeviceType registerDeviceType(DeviceType device) {
    try {
      return deviceTypeDao.save(device);
    } catch (DataIntegrityViolationException ignore) {
      return device;
    }
  }

  public Recording newRecording(Location location, Date startDate, Date endDate) {
    return new RecordingImpl(location, startDate, endDate);
  }

  /**
   * todo This implementation does not store EventSeries.
   */
  public EventSeries getOrCreateEventSeries(String seriesId) {
    EventSeries series = new EventSeriesImpl();
    series.setSeriesId(seriesId);
    return series;
  }

  public Attachment newAttachment(Serializable content) {
    if (content instanceof DublinCoreCatalog)
      return new DublinCoreAttachmentImpl((DublinCoreCatalog) content);
    if (content instanceof JobTicket)
      return new JobTicketAttachmentImpl((JobTicket) content);
    return new DocumentAttachmentImpl(content);
  }

  public Person newPerson(String givenName, String familyName, String preferredEmailAddress) {
    if (preferredEmailAddress != null)
      return new PersonImpl(givenName, familyName, preferredEmailAddress);
    else
      return new PersonImpl(givenName, familyName);
  }

  public Person newPerson() {
    return new PersonImpl();
  }

  public DeviceType getOrCreateDeviceType(String name) {
    List<DeviceType> r = deviceTypeDao.findByExample(new DeviceTypeImpl(name));
    if (r.size() > 1)
      throw new RuntimeException("Constraint error: Device type names must be unique");
    return r.size() == 1 ? r.get(0) : new DeviceTypeImpl(name);
  }

  /**
   * This implementation tries at first to retrieve a location with the given name from the database. If none can be
   * found it creates a new one.
   */
  public Location getOrCreateLocation(String name) {
    Location location = new SimpleLocation(name);
    List<Location> result = locationDao.findByExample(location);
    switch (result.size()) {
    case 0:
      return location;
    case 1:
      return result.get(0);
    default:
      throw new RuntimeException("Constraint error: Location names must be unique");
    }
  }

  public RecordingSeries getOrCreateRecordingSeries(String seriesID) {
    List<RecordingSeries> r = recordingSeriesDao.findByExample(new RecordingSeriesImpl(seriesID));
    if (r.size() > 1)
      throw new RuntimeException("Constraint error: Series IDs must be unique");
    return r.size() == 1 ? r.get(0) : new RecordingSeriesImpl(seriesID);
  }
}
