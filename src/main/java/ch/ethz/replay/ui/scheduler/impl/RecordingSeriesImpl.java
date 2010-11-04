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

import ch.ethz.replay.ui.scheduler.Recording;
import ch.ethz.replay.ui.scheduler.RecordingSeries;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Implementation notes:
 * <ul>
 * <li>Saving does not cascade to contained recordings.</li>
 * <li>Dublin Core must be set</li>
 * </ul>
 * 
 * todo Class does not provide an equals/hashcode implementation so it cannot be used safely in a detached state. Think
 * about a business
 * 
 * 
 */
@Entity(name = "RecordingSeries")
public class RecordingSeriesImpl extends BaseEntity implements RecordingSeries {

  @Id
  @GeneratedValue
  @Column(unique = true, nullable = false, updatable = false)
  private String seriesId;

  @OneToMany(targetEntity = RecordingImpl.class, mappedBy = "series")
  private Set<Recording> recordings = new HashSet<Recording>();

  @OneToOne(targetEntity = AttachmentImpl.class)
  @JoinColumn(name = "DublinCoreAttachmentId", nullable = false)
  private AttachmentImpl attachment = new AttachmentImpl();

  private String title;

  /**
   * Will be updated automatically via {@link ch.ethz.replay.ui.common.util.hibernate.AutoTimestampOnUpdateListener}
   */
  @Temporal(value = TemporalType.TIMESTAMP)
  private Date lastUpdated;

  /**
   * @return the lastUpdated
   */
  public Date getLastUpdated() {
    return lastUpdated;
  }
  
  /**
   * @param lastUpdated the lastUpdated to set
   */
  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }
  
  /**
   * No-arg constructor for JPA
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
      if (r.isBygone())
        count++;
    }
    return count;
  }

  public boolean isBygone() {
    return getBygoneCount() == recordings.size();
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public int hashCode() {
    return seriesId.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    // Use series ID because it's defined to be non-null and unique
    return (this == o)
            || ((o instanceof RecordingSeriesImpl) && (seriesId.equals(((RecordingSeriesImpl) o).getSeriesId())));
  }
}