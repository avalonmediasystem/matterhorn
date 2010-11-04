/*
 
 RecordingFilter.java
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

package ch.ethz.replay.ui.scheduler;

import ch.ethz.replay.ui.scheduler.RecordingStatus;

import java.util.Date;

/**
 * This class defines a set of criteria to search for {@link Recording}s.
 * 
 * 
 */
public class RecordingFilter {

  private String locationName;
  private Long locationId;
  private Date startingAfter;
  private Date endingBefore;
  private Date endingAfter;
  private String freeText;
  private RecordingStatus status;
  private Long seriesId;
  private String jobId;

  public RecordingFilter() {
  }

  /**
   * Creates a new filter.
   * 
   * @param locationName
   *          you can either set <code>locationName</code> or <code>locationId</code>
   * @param seriesId
   * @param jobId
   */
  public RecordingFilter(String locationName, Long locationId, Date startingAfter, Date endingBefore, Date endingAfter,
          String freeText, RecordingStatus status, Long seriesId, String jobId) {
    if (locationName != null && locationId != null) {
      throw new IllegalArgumentException("You cannot set both locationName and locationId");
    }
    this.locationName = locationName;
    this.locationId = locationId;
    this.startingAfter = startingAfter;
    this.endingBefore = endingBefore;
    this.endingAfter = endingAfter;
    this.freeText = freeText;
    this.status = status;
    this.seriesId = seriesId;
    this.jobId = jobId;
  }

  public String getLocationName() {
    return locationName;
  }

  public RecordingFilter setLocationName(String locationName) {
    this.locationName = locationName;
    return this;
  }

  public Long getLocationId() {
    return locationId;
  }

  public RecordingFilter setLocationId(Long locationId) {
    this.locationId = locationId;
    return this;
  }

  public Date getStartingAfter() {
    return startingAfter;
  }

  public RecordingFilter setStartingAfter(Date startingAfter) {
    this.startingAfter = startingAfter;
    return this;
  }

  public Date getEndingBefore() {
    return endingBefore;
  }

  public RecordingFilter setEndingBefore(Date endingBefore) {
    this.endingBefore = endingBefore;
    return this;
  }

  public Date getEndingAfter() {
    return endingAfter;
  }

  public RecordingFilter setEndingAfter(Date endingAfter) {
    this.endingAfter = endingAfter;
    return this;
  }

  public String getFreeText() {
    return freeText;
  }

  public RecordingFilter setFreeText(String freeText) {
    this.freeText = freeText;
    return this;
  }

  public RecordingStatus getStatus() {
    return status;
  }

  public RecordingFilter setStatus(RecordingStatus status) {
    this.status = status;
    return this;
  }

  public Long getSeriesId() {
    return seriesId;
  }

  public void setSeriesId(Long seriesId) {
    this.seriesId = seriesId;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }
}
