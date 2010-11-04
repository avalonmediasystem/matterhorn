/*

 EmailAddressEditor.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 27, 2008

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

import ch.ethz.replay.ui.scheduler.impl.EmailAddressImpl;

import java.beans.PropertyEditorSupport;
import java.util.Arrays;
import java.util.List;

/**
 * Converts strings into {@link ch.ethz.replay.core.api.common.vcard.EmailAddress}es and vice versa.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class EmailAddressEditor extends PropertyEditorSupport {

    public String getAsText() {
        List list = (List) getValue();
        if (list != null && list.size() > 0) {
            return ((EmailAddressImpl) list.get(0)).getAddress();
        }
        return null;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        setValue(Arrays.asList(new EmailAddressImpl(text)));
    }
}
