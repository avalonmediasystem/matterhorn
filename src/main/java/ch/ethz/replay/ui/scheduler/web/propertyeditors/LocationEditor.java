/*
 
 LocationEditor.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Oct 8, 2008

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

package ch.ethz.replay.ui.scheduler.web.propertyeditors;

import ch.ethz.replay.ui.scheduler.Location;
import ch.ethz.replay.ui.scheduler.Schedule;
import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;

/**
 * Converts from ID to {@link ch.ethz.replay.ui.scheduler.Location} and vice versa.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class LocationEditor extends PropertyEditorSupport {

    private Schedule schedule;

    public LocationEditor(Schedule schedule) {
        this.schedule = schedule;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.hasText(text)) {
            Location location = schedule.findLocationByName(text);
            if (location == null) {
                location = schedule.getOrCreateLocation(text);
            }
            setValue(location);
        } else {
            setValue(null);
        }
    }

    @Override
    public String getAsText() {
        Location location = (Location) getValue();
        return location != null
                ? location.getName()
                : "";
    }
}
