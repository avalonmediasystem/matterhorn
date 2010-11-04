/*

 ScheduleFilterCommand.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 26, 2008

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

package ch.ethz.replay.ui.scheduler.web.controller;

import ch.ethz.replay.ui.scheduler.RecordingSeries;

import java.util.Date;
import java.util.List;

/**
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class ScheduleFilterCommand {

    private String locationName;
    private Date startingAfter;
    private Date endingAfter;
    private String freeSearch;
    private RecordingSeries series;

    public ScheduleFilterCommand() {
    }

    public ScheduleFilterCommand(Date endingAfter) {
        this.endingAfter = endingAfter;
    }

    public String getFreeSearch() {
        return freeSearch;
    }

    public void setFreeSearch(String freeSearch) {
        this.freeSearch = freeSearch;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Date getStartingAfter() {
        return startingAfter;
    }

    public void setStartingAfter(Date startingAfter) {
        this.startingAfter = startingAfter;
    }

    public Date getEndingAfter() {
        return endingAfter;
    }

    public void setEndingAfter(Date endingAfter) {
        this.endingAfter = endingAfter;
    }

    public RecordingSeries getSeries() {
        return series;
    }

    public void setSeries(RecordingSeries series) {
        this.series = series;
    }

    public Long getSeriesId() {
        return series != null ? series.getId() : null;
    }
}
