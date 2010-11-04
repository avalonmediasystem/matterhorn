/*

 Event.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created Aug 26, 2008

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

import ch.ethz.replay.core.api.common.metadata.dublincore.DublinCore;

import java.util.Date;
import java.util.Set;
import java.util.List;

/**
 * An event is something that can be recorded. This interface is designed to connect a recording to
 * the event from which it is derived.
 * <p/>
 * Recordings must not have an associated event, they are completely
 * optional and only for storing extra information. Their use is up to the actual implementation.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 * @see EventSeries
 */
public interface Event {

    /**
     * Returns the internal id. This is id does not have any business meanings.
     */
    Long getId();

    /**
     * Returns the business id of the event as it is provided by the external data source.
     */
    String getEventId();

    /**
     * Returns the series this event belongs to or null.
     */
    EventSeries getSeries();

    /**
     * Returns the associated recording.
     */
    Recording getAssociatedRecording();
}
