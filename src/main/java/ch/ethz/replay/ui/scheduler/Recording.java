/*

 Recording.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 8, 2008

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

import ch.ethz.replay.core.api.common.handle.Handle;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * Recording is the central interface of the domain model. It describes everything around a recording.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public interface Recording extends MetadataTagged {

    /**
     * Returns the internal id. This id is surrogate key and does not have any business meanings.
     * Its sole purpose is to store and manage recordings.
     */
    Long getId();

    /**
     * Returns the series if this recording belongs to one.
     *
     * @return the series or null
     */
    RecordingSeries getSeries();

    /**
     * Checks if the recording is part of a series.
     */
    boolean isPartOfSeries();

    /**
     * Returns the event that will be recorded by this recording-
     * <p/>
     * If a recording is created for an event that comes e.g. from
     * an external event data base it may be useful to keep this link.
     *
     * @return the associated event or null
     */
    Event getAssociatedEvent();

    /**
     * Associates a recording with an event.
     * <p/>
     * If a recording is created for an event that comes e.g. from
     * an external event data base it may be useful to keep this link.
     */
    void associateWith(Event event);

    /**
     * Each recording <em>may</em> be associated with a job id which may be used elsewhere and therefore must
     * conform to the needs of the application where this id is intended to be used.
     * <p/>
     * Currently this id will be used by REPLAY as the id for the job that
     * processes the bundle resulting from this recording.
     *
     * @return the job id or null if not used
     */
    String getJobId();

    /**
     * Return an ID used for the bundle resulting from this recording.
     */
    Handle getBundleId();

    /**
     * Returns the location where the recording takes place.
     *
     * @return the location or null if not yet set
     */
    Location getLocation();

    /**
     * Sets the location.
     */
    void setLocation(Location location);

    /**
     * Returns the start date of the recording.
     */
    Date getStartDate();

    void setStartDate(Date date);

    /**
     * Returns the end date of the recording.
     */
    Date getEndDate();

    void setEndDate(Date date);

    /**
     * Checks if the recording lies completely in the past.
     */
    boolean isBygone();

    RecordingStatus getStatus();

    void setStatus(RecordingStatus status);

    /**
     * Returns people responsible for this recording.
     *
     * @return a set of people or an empty set
     */
    Set<Person> getContactPersons();

    void setContactPersons(Set<Person> persons);

    /**
     * Adds a new contact person.
     */
    void addContactPerson(Person person);

    /**
     * Returns all attachments.
     *
     * @return the attachments or an empty list
     */
    Collection<Attachment> getAttachments();

    /**
     * Attaches an arbitrary document.
     */
    void attach(DocumentAttachment attachment);

    /**
     * Detaches an attachment.
     */
    void detach(DocumentAttachment attachment);

    // Job Ticket

    /**
     * Attaches a {@link ch.ethz.replay.core.api.common.job.JobTicket}
     * to the recording.
     *
     * @param attachment the job ticket attachment or null to remove
     */
    void setJobTicket(JobTicketAttachment attachment);

    /**
     * Returns the attached job ticket or null, if none is attached.
     */
    JobTicketAttachment getJobTicket();

    /**
     * Returns the devices used to produce this recording.
     *
     * @return a set of devices or an empty set
     */
    Set<DeviceType> getDevices();

    void setDevices(Set<DeviceType> devices);

    void addDevice(DeviceType device);
}
