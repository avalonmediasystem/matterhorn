/*
 
 EventSeries.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Oct 2, 2008

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

import java.util.Set;

/**
 * Used for grouping events like a {@link RecordingSeries} does with {@link Recording Recordings}.
 * Everything said about {@link Event}s is also true for an <code>EventSeries</code>.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public interface EventSeries {

    /**
     * Returns the internal id. This is id does not have any business meanings.
     */
    Long getId();

    /**
     * Returns the business id of the series as it is provided by the external data source.
     *
     * @return the series id or null
     */
    String getSeriesId();

    /**
     * Sets the business id of the series as it si provided by the external data source.
     */
    void setSeriesId(String seriesId);

    /**
     * Returns the associated recording series.
     */
    RecordingSeries getAssociatedRecordingSeries();

    /**
     * Returns the events. 
     */
    Set<Event> getEvents();

    /**
     * Adds an event to this series.
     */
    void addEvent(Event event);
}
