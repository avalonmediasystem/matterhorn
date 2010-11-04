/*

 RecordingSeries.java
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

import ch.ethz.replay.ui.scheduler.EventSeries;
import ch.ethz.replay.ui.scheduler.Recording;
import ch.ethz.replay.ui.scheduler.RecordingSeries;
import ch.ethz.replay.ui.scheduler.DublinCoreAttachment;
import ch.ethz.replay.core.api.common.bundle.DublinCoreCatalog;
import static ch.ethz.replay.core.api.common.metadata.dublincore.DublinCore.PROPERTY_TITLE;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * Implementation notes:
 * <ul>
 * <li>Saving does not cascade to contained recordings.</li>
 * <li>Dublin Core must be set</li>
 * </ul>
 *
 * todo
 * Class does not provide an equals/hashcode implementation so it cannot be used safely in a detached state.
 * Think about a business 
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Entity(name = "RecordingSeries")
public class RecordingSeriesImpl extends BaseEntity
        implements RecordingSeries {

    @Column(unique = true, nullable = false, updatable = false)
    private String seriesId;

    @OneToMany(targetEntity = RecordingImpl.class,
            mappedBy = "series")
    private Set<Recording> recordings = new HashSet<Recording>();

    @OneToOne(targetEntity = DublinCoreAttachmentImpl.class)
    @JoinColumn(name = "DublinCoreAttachmentId", nullable = false)
    @Cascade(CascadeType.ALL)
    private DublinCoreAttachment dublinCoreAttachment = new DublinCoreAttachmentImpl();

    @OneToOne
    @JoinColumn(name = "EventSeriesId")
    @Cascade(CascadeType.ALL)
    private EventSeriesImpl eventSeries;

    private String title;

    private boolean visible = true;

    /**
     * Will be updated automatically via {@link ch.ethz.replay.ui.common.util.hibernate.AutoTimestampOnUpdateListener}
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastUpdated;

    //

    /**
     * FOR INTERNAL USE ONLY!
     */
    public RecordingSeriesImpl() {
    }

    /**
     * Create a new series.
     */
    public RecordingSeriesImpl(String seriesId) {
        if (seriesId == null)
            throw new IllegalArgumentException("Series ID may not be null");
        this.seriesId = seriesId;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public Set<Recording> getRecordings() {
        return recordings;
    }

    public void addRecording(Recording recording) {
        if (recording.getSeries() != null) {
            throw new IllegalArgumentException("Recording is already part of another series. Please remove it first");
        }
        recordings.add(recording);
        ((RecordingImpl) recording).setSeries(this);
    }

    public void removeRecording(Recording recording) {
        recordings.remove(recording);
        ((RecordingImpl) recording).setSeries(null);
    }

    public boolean containsRecording(Recording recording) {
        return recordings.contains(recording);
    }

    public Recording getFirst() {
        Recording first = null;
        for (Recording r : recordings) {
            if (first == null || r.getStartDate().before(first.getStartDate()))
                first = r;
        }
        return first;
    }

    public Recording getLast() {
        Recording last = null;
        for (Recording r : recordings) {
            if (last == null || r.getEndDate().after(last.getEndDate()))
                last = r;
        }
        return last;
    }

    public int getBygoneCount() {
        int count = 0;
        for (Recording r : recordings) {
            if (r.isBygone()) count++;
        }
        return count;
    }

    public boolean isBygone() {
        return getBygoneCount() == recordings.size();
    }

    public EventSeries getAssociatedEventSeries() {
        return eventSeries;
    }

    public void setAssociatedEventSeries(EventSeries eventSeries) {
        if (eventSeries == null)
            throw new IllegalArgumentException("Event series must not be null");
        this.eventSeries = (EventSeriesImpl) eventSeries;
        this.eventSeries.setRecordingSeries(this);
    }

    public boolean hasAssociatedEventSeries() {
        return eventSeries != null;
    }

    public EventSeries removeAssociatedEventSeries() {
        this.eventSeries.setRecordingSeries(null);
        EventSeries r = this.eventSeries;
        this.eventSeries = null;
        return r;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        throw new UnsupportedOperationException("Title attribute cannot be set directly. It is fetched from " +
                "a contained Dublin Core metadata catalog");
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public int hashCode() {
        return seriesId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        // Use series ID because it's defined to be non-null and unique
        return (this == o) ||
                ((o instanceof RecordingSeriesImpl) && (seriesId.equals(((RecordingSeriesImpl) o).getSeriesId())));
    }
}