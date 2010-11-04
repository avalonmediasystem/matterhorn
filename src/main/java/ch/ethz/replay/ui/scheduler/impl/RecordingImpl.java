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

import static ch.ethz.replay.core.api.common.metadata.dublincore.DublinCore.*;
import ch.ethz.replay.core.api.common.bundle.DublinCoreCatalog;
import ch.ethz.replay.core.api.common.handle.Handle;
import ch.ethz.replay.core.common.handle.HandleImpl;
import ch.ethz.replay.ui.common.util.Utils;
import ch.ethz.replay.ui.common.util.ReplayBuilder;
import ch.ethz.replay.ui.scheduler.*;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.*;

/**
 * Implementation of {@link ch.ethz.replay.ui.scheduler.Recording}.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Entity(name = "Recording")
public class RecordingImpl extends BaseEntity
        implements Recording {

    @ManyToOne(targetEntity = RecordingSeriesImpl.class)
    @JoinColumn(name = "RecordingSeriesId")
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    private RecordingSeries series;

    @Column(nullable = false, unique = true)
    private String jobId = ReplayBuilder.createId();

    @Column(nullable = false, unique = true)
    private String bundleId = ReplayBuilder.createHandle().getFullName();

    private String title;

    @ManyToOne(targetEntity = SimpleLocation.class)
    @JoinColumn(name = "LocationId", nullable = false)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    private Location location;

    @Column(nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date startDate;

    @Column(nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date endDate;

    @Column(nullable = false)
    private RecordingStatus status = RecordingStatus.created;

    @OneToOne(targetEntity = DublinCoreAttachmentImpl.class)
    @JoinColumn(name = "DublinCoreAttachmentId", nullable = false)
    @Cascade(CascadeType.ALL)
    private DublinCoreAttachment dublinCoreAttachment = new DublinCoreAttachmentImpl();

    @ManyToOne(targetEntity = JobTicketAttachmentImpl.class)
    @JoinColumn(name = "JobTicketAttachmentId")
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    private JobTicketAttachment jobTicketAttachment;

    @ManyToMany(targetEntity = AbstractPerson.class)                                                                    
    @JoinTable(name = "Recording_Person",
            joinColumns = @JoinColumn(name = "recordingId"),
            inverseJoinColumns = @JoinColumn(name = "personId"))
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    private Set<Person> contactPersons = new TreeSet<Person>();

    @ManyToMany(targetEntity = AbstractAttachment.class)
    @JoinTable(name = "Recording_Attachment",
            joinColumns = @JoinColumn(name = "recordingId"),
            inverseJoinColumns = @JoinColumn(name = "attachmentId"))
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    private Set<Attachment> attachments = new HashSet<Attachment>();

    @ManyToMany(targetEntity = DeviceTypeImpl.class,
            fetch = FetchType.EAGER)
    @JoinTable(name = "Recording_DeviceType",
            joinColumns = @JoinColumn(name = "recordingId"),
            inverseJoinColumns = @JoinColumn(name = "deviceId"))
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    private Set<DeviceType> devices = new HashSet<DeviceType>();

    // todo make persistent
    @Transient
    private Event event;

    /**
     *  Will be updated automatically via {@link ch.ethz.replay.ui.common.util.hibernate.AutoTimestampOnUpdateListener}
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastUpdated;

    /**
     * FOR INTERNAL USE ONLY! DO NOT CALL!
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

    public void attach(DocumentAttachment attachment) {
        attachments.add(attachment);
    }

    public void detach(DocumentAttachment attachment) {
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

    public Event getAssociatedEvent() {
        return event;
    }

    public void associateWith(Event event) {
        this.event = event;
    }

    public String getJobId() {
        return jobId;
    }

    public Handle getBundleId() {
        return ReplayBuilder.createHandle(bundleId);
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
        if (persons == null) this.contactPersons.clear();
        else this.contactPersons = persons;
    }

    public void addContactPerson(Person person) {
        if (person == null) {
            throw new IllegalArgumentException("person is null");
        }
        contactPersons.add(person);
    }

    public Collection<Attachment> getAttachments() {
        Collection<Attachment> combined = new ArrayList<Attachment>(attachments.size() + 2);
        combined.addAll(attachments);
        if (dublinCoreAttachment.getContent() != null) {
            combined.add(dublinCoreAttachment);
        }
        if (jobTicketAttachment != null) {
            combined.add(jobTicketAttachment);
        }
        return combined;
    }

    public DublinCoreAttachment getDublinCoreAttachment() {
        return dublinCoreAttachment;
    }

    public void setDublinCore(DublinCoreCatalog dublinCore) {
        dublinCoreAttachment.setContent(dublinCore);
        title = dublinCore != null
                ? dublinCore.getFirst(PROPERTY_TITLE)
                : null;
    }

    public DublinCoreCatalog getDublinCore() {
        return dublinCoreAttachment.getContent();
    }

    public void setJobTicket(JobTicketAttachment attachment) {
        this.jobTicketAttachment = attachment;
    }

    public JobTicketAttachment getJobTicket() {
        return jobTicketAttachment;
    }

    public Set<DeviceType> getDevices() {
        return devices;
    }

    public void setDevices(Set<DeviceType> devices) {
        if (devices == null) this.devices.clear();
        else this.devices = devices;
    }

    public void addDevice(DeviceType device) {
        if (device == null) {
            throw new IllegalArgumentException("device is null");
        }
        devices.add(device);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecordingImpl)) return false;
        // The job ID is defined to be non null and unique so it's an ideal candidate for equality.
        return jobId.equals(((RecordingImpl) o).getJobId());
    }

    @Override
    public int hashCode() {
        // See equals()
        return jobId.hashCode();
    }
}
