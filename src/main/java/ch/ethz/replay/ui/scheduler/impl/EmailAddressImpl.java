/*

 EmailAddressImpl.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 10, 2008

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

import ch.ethz.replay.core.api.common.vcard.EmailAddress;
import ch.ethz.replay.ui.common.util.Utils;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Simple implementation of {@link ch.ethz.replay.core.api.common.vcard.EmailAddress} storing
 * only addresses of type {@link #INTERNET}, with "address" and "preferred" properties.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Embeddable
public class EmailAddressImpl implements EmailAddress, Serializable {

    private String type = INTERNET;

    @Column(nullable = false, unique = true)
    private String address;

    private boolean preferred;

    //

    /**
     * Hibernate only
     */
    EmailAddressImpl() {
    }

    public EmailAddressImpl(String address) {
        this.address = address;
    }

    public EmailAddressImpl(String address, boolean preferred) {
        this.address = address;
        this.preferred = preferred;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isPreferred() {
        return preferred;
    }

    public void setPreferred(boolean preferred) {
        this.preferred = preferred;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailAddress)) return false;

        EmailAddress that = (EmailAddress) o;
        return Utils.equals(address, that.getAddress())
                && preferred == this.isPreferred();
    }

    public int hashCode() {
        int result;
        result = address != null ? address.hashCode() : 0;
        result = 31 * result + (preferred ? 1 : 0);
        return result;
    }
}
