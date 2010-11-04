/*
 
 Schedule.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 9 29, 2008

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

package ch.ethz.replay.ui.scheduler;

import java.io.Serializable;
import java.util.List;
import java.util.Date;

/**
 * The Schedule manages all scheduled recordings and is the main service for clients to deal with. Therefore it should
 * be implemented as a singleton.
 * 
 * 
 */
public interface Schedule {

  /**
   * Puts a recording on the schedule. Also use this method to save updates to already scheduled recordings. todo
   * Exception if not valid
   * 
   * @return the scheduled recording
   */
  Recording schedule(Recording recording);

  /**
   * Put a whole series of recordings on the schedule. Also use this method to save updates to already scheduled
   * recordings. todo Exception
   * 
   * @return the scheduled series
   */
  RecordingSeries schedule(RecordingSeries series);

  /**
   * Removes the recording from the schedule. todo Exception
   * 
   * @param id
   *          the recording id
   */
  void removeRecording(Long id);

  /**
   * Removes a whole series of recordings from the schedule. todo Exception
   * 
   * @param id
   *          the series id
   */
  void removeSeries(Long id);

  /**
   * Removes a recording from its associated series if it has one. Otherwise a call to this method will be gently
   * ignored.
   * 
   * @param recording
   *          the recording to detach
   */
  void removeRecordingFromSeries(Recording recording);

  /**
   * Returns a recording by its id.
   * 
   * @param id
   *          the recording id
   * @return the recording or null
   */
  Recording getRecording(Long id);

  /**
   * Finds all scheduled recordings.
   */
  List<Recording> findAllRecordings();

  /**
   * Finds recordings by the given example sorted in ascending order by their start date.
   */
  List<Recording> findRecordings(Recording example);

  /**
   * Returns a series by id.
   * 
   * @param id
   *          the series id
   * @return the series or null
   */
  RecordingSeries getRecordingSeries(Long id);

  /**
   * Finds recordings by filter.
   */
  List<Recording> findRecordings(RecordingFilter filter);

  /**
   * Finds all visible recording series.
   */
  List<RecordingSeries> findAllRecordingSeries();

  /**
   * Saves a series. This method - in contrast to {@link #schedule(RecordingSeries)} - only makes the series persistent,
   * so it can be referred to it later on. Neither the series, nor the containing recordings will be scheduled.
   * Contained recording will <em>not</em> be saved.
   */
  void saveRecordingSeries(RecordingSeries series);

  /**
   * Hides a series from being displayed. The series will not be deleted, just flagged. Note that only the series itself
   * will be hidden, the contained recordings are not affected.
   */
  void hideRecordingSeries(RecordingSeries series);

  RecordingSeries findRecordingSeriesBySeriesId(String seriesId);

  /**
   * Returns the date when the schedule for a certain location was last modified.
   * 
   * @return the date or null, if there is no schedule for that location
   */
  Date getLastModificationDateOfSchedule(Location location);

  // Location

  Location getLocation(Long id);

  /**
   * Finds locations by their name. The matching is done case insensitive.
   */
  List<Location> findLocationsByName(String name);

  /**
   * Finds a location by its name. The matching is done case insensitive.
   */
  Location findLocationByName(String name);

  /**
   * Returns a list of all scheduled locations.
   */
  List<Location> findAllScheduledLocations();

  // Attachment

  /**
   * Returns an attachment by its ID or null if there is none.
   */
  Attachment getAttachment(Long id);

  <T extends Attachment> List<T> findAttachmentByType(Class<T> type);

  // Person

  /**
   * Returns a person by its ID or null if none can be found.
   */
  Person getPerson(Long id);

  /**
   * Saves a person.
   * 
   * @return the person
   */
  Person savePerson(Person person);

  /**
   * Removes a person from the schedule. This affects all scheduled recordings referencing this person.
   */
  void removePerson(Long id);

  /**
   * Returns a list of all locally stored persons.
   */
  List<Person> findAllPersons();

  /**
   * Returns a list of persons matching the example. All connected person data sources will be queried.
   */
  List<Person> findPersons(Person example);

  /**
   * Returns a list of persons matching the given name. All connected person data sources will be queried.
   */
  List<Person> findPersonsByName(String name);

  // DeviceType

  /**
   * Returns a list of all known and available device types.
   */
  List<DeviceType> getAvailableDeviceTypes();

  DeviceType getDeviceType(Long id);

  /**
   * Register a new device type.
   * 
   * @return the registered device type
   */
  DeviceType registerDeviceType(DeviceType device);

  // Factory methods

  /**
   * Create a new recording.
   */
  Recording newRecording(Location location, Date startDate, Date endDate);

  /**
   * Get or create a new recording series.
   */
  RecordingSeries getOrCreateRecordingSeries(String seriesID);

  /**
   * Depending on the <code>content</code>'s type, different attachments will be returned, e.g. if <code>content</code>
   * is a {@link ch.ethz.replay.core.api.common.bundle.DublinCoreCatalog} you'll get a
   * {@link ch.ethz.replay.ui.scheduler.DublinCoreAttachment}.
   */
  Attachment newAttachment(Serializable content);

  /**
   * Use this method to create local persons, not bound to any external data source like an LDAP directory.
   * 
   * @param preferredEmailAddress
   *          may be null
   */
  Person newPerson(String givenName, String familyName, String preferredEmailAddress);

  Person newPerson();

  /**
   * Creates and returns a new device type.
   */
  DeviceType getOrCreateDeviceType(String name);

  Location getOrCreateLocation(String name);
}
