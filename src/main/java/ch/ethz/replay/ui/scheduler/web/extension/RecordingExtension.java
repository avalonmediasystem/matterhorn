/*

 RecordingExtension.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 23, 2008

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

package ch.ethz.replay.ui.scheduler.web.extension;

import java.util.Map;

/**
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public interface RecordingExtension {

    boolean isRecording();

    boolean isEditable();

    boolean isValid();

    /**
     * Pass a {@link java.util.Date} to the map to query, if this date is a different day than
     * the day of the recording.
     */
    Map getOtherDay();

    /** Monday == 0 */
    int getWeekDay();
}
