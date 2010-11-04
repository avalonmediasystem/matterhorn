/*
 
 AbstractPerson.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 9 8, 2008

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

import ch.ethz.replay.core.api.common.vcard.Address;
import ch.ethz.replay.core.api.common.vcard.EmailAddress;
import ch.ethz.replay.core.api.common.vcard.GeoPosition;
import ch.ethz.replay.core.api.common.vcard.TelephoneNumber;
import ch.ethz.replay.ui.scheduler.Person;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.Transient;
import javax.persistence.InheritanceType;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.hibernate.annotations.AccessType;

/**
 * Basis for {@link ch.ethz.replay.ui.scheduler.Person} implementations. Provides
 * implementations for <code>equals</code> and <code>hashCode</code> and also
 * implements {@link Comparable}.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
// Implementation notes:
// A table-per-subclass strategy is chosen to ensure unique ids across all Person implementations
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@AccessType("field")
public abstract class AbstractPerson extends BaseEntity
        implements Person, Comparable<Person> {

    @Transient
    private final boolean dummy = false;

    public String getFormattedName() {
        return null;
    }

    public String getFamilyName() {
        return null;
    }

    public String getGivenName() {
        return null;
    }

    public String getAdditionalNames() {
        return null;
    }

    public String getHonorificPrefixes() {
        return null;
    }

    public String getHonorificSuffixes() {
        return null;
    }

    public List<String> getNicknames() {
        return null;
    }

    public Object getPhoto() {
        return null;
    }

    public Date getBirthDate() {
        return null;
    }

    public List<Address> getAddresses() {
        return null;
    }

    public List<TelephoneNumber> getTelephoneNumbers() {
        return null;
    }

    public List<EmailAddress> getEmailAddresses() {
        return null;
    }

    public EmailAddress getPreferredEmailAddress() {
        return null;
    }

    public String getMailer() {
        return null;
    }

    public TimeZone getTimeZone() {
        return null;
    }

    public GeoPosition getGeoPosition() {
        return null;
    }

    public String getTitle() {
        return null;
    }

    public String getRole() {
        return null;
    }

    public Object getLogo() {
        return null;
    }

    public String getAgent() {
        return null;
    }

    public String getOrganization() {
        return null;
    }

    public String getCategories() {
        return null;
    }

    public String getNote() {
        return null;
    }

    public String getProdId() {
        return null;
    }

    public String getRevision() {
        return null;
    }

    /**
     * Returns {@link #getFamilyName()}
     */
    public String getSortString() {
        return getFamilyName();
    }

    public Object getSound() {
        return null;
    }

    public String getUid() {
        return null;
    }

    public URL getUrl() {
        return null;
    }

    /**
     * @return <code>3.0</code>
     */
    public String getVersion() {
        return "3.0";
    }

    public String getAccessClassification() {
        return null;
    }

    public Object getKey() {
        return null;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;

        Person person = (Person) o;
        if (getFamilyName() != null
                ? !getFamilyName().equals(person.getFamilyName())
                : person.getFamilyName() != null) return false;
        if (getGivenName() != null
                ? !getGivenName().equals(person.getGivenName())
                : person.getGivenName() != null) return false;
        if (getHonorificPrefixes() != null
                ? !getHonorificPrefixes().equals(person.getHonorificPrefixes())
                : person.getHonorificPrefixes() != null) return false;
        EmailAddress emailAddress = getPreferredEmailAddress();
        if (emailAddress != null
                ? !emailAddress.equals(person.getPreferredEmailAddress())
                : person.getPreferredEmailAddress() != null) return false;
        return true;
    }

    public int hashCode() {
        int result;
        result = getFamilyName() != null ? getFamilyName().hashCode() : 0;
        result = 31 * result + (getGivenName() != null ? getGivenName().hashCode() : 0);
        result = 31 * result + (getHonorificPrefixes() != null ? getHonorificPrefixes().hashCode() : 0);
        return result;
    }

    public int compareTo(Person p) {
        int result = 0;
        result = _compareTo(getFamilyName(), p.getFamilyName());
        if (result != 0) return result;
        result = _compareTo(getGivenName(), p.getGivenName());
        if (result != 0) return result;
        EmailAddress me = getPreferredEmailAddress();
        EmailAddress other = p.getPreferredEmailAddress();
        if (me == null) return other == null ? 0 : -1;
        if (other == null) return -1;
        String myAddress = me.getAddress();
        if (myAddress == null) return other.getAddress() == null ? 0 : -1;
        return me.getAddress().compareTo(other.getAddress());
    }

    private int _compareTo(Comparable a, Comparable b) {
        if (a == null) return b == null ? 0 : -1;
        if (b == null) return -1;
        return a.compareTo(b);
    }
}
