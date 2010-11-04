/*
 
 EventSeriesImpl.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Oct 13, 2008

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

import ch.ethz.replay.ui.scheduler.Event;
import ch.ethz.replay.ui.scheduler.EventSeries;
import ch.ethz.replay.ui.scheduler.RecordingSeries;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.util.Set;

/**
 * Todo implement
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Entity(name = "EventSeries")
public class EventSeriesImpl extends BaseEntity
        implements EventSeries {

    private String seriesId;

    @OneToOne(targetEntity = RecordingSeriesImpl.class, mappedBy = "eventSeries")
    private RecordingSeries recordingSeries;

    public String getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    public RecordingSeries getAssociatedRecordingSeries() {
        return recordingSeries;
    }

    /**
     * Sets the recording series, null clears the association.
     */
    public void setRecordingSeries(RecordingSeries recordingSeries) {
        this.recordingSeries = recordingSeries;
    }

    /**
     * Todo implement
     */
    public Set<Event> getEvents() {
        return null;
    }

    /**
     * Todo implement
     */
    public void addEvent(Event event) {
    }
}
