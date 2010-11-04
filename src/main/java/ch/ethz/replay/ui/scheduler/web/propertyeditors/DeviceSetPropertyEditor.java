/*

 DeviceSetPropertyEditor.java
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

package ch.ethz.replay.ui.scheduler.web.propertyeditors;

import ch.ethz.replay.ui.scheduler.DeviceType;
import ch.ethz.replay.ui.scheduler.impl.DeviceTypeImpl;
import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.util.HashSet;
import java.util.Set;

/**
 * Converts strings into a set of {@link ch.ethz.replay.ui.scheduler.DeviceType}s and vice versa.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 * @deprecated
 */
public class DeviceSetPropertyEditor extends PropertyEditorSupport {

    /**
     * Default separator for splitting a String: a comma (",")
     */
    public static final String DEFAULT_SEPARATOR = ",";

    private final String separator;

    private final boolean emptyArrayAsNull;

    public DeviceSetPropertyEditor(String separator, boolean emptyArrayAsNull) {
        this.separator = separator;
        this.emptyArrayAsNull = emptyArrayAsNull;
    }

    public DeviceSetPropertyEditor(boolean emptyArrayAsNull) {
        this.separator = DEFAULT_SEPARATOR;
        this.emptyArrayAsNull = emptyArrayAsNull;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        String[] array = StringUtils.delimitedListToStringArray(text, this.separator);
        if (this.emptyArrayAsNull && array.length == 0) {
            setValue(null);
        } else {
            Set devices = new HashSet();
            for (String s : array) devices.add(new DeviceTypeImpl(s));
            setValue(devices);
        }
    }

    public String getAsText() {
        Set<DeviceType> deviceTypes = (Set<DeviceType>) getValue();
        if (deviceTypes != null) {
            StringBuilder b = new StringBuilder();
            for (DeviceType d : deviceTypes) {
                if (b.length() > 0) b.append(this.separator);
                b.append(d.getName());
            }
            return b.toString();
        }
        return null;
    }
}
