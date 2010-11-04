/*

 PersonImpl.java
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

package ch.ethz.replay.ui.scheduler.impl;

import ch.ethz.replay.core.api.common.vcard.EmailAddress;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.IndexColumn;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of {@link ch.ethz.replay.ui.scheduler.Person}
 * with support for only a subset of the properties.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Entity(name = "Person")
public class PersonImpl extends AbstractPerson {

    // todo make configurable
    private static final String PROD_ID = "-//ETH Zurich//REPLAY//DE";

    @Column(nullable = false)
    private String familyName;

    @Column(nullable = false)
    private String givenName;

    private String honorificPrefixes;

    @CollectionOfElements(targetElement = EmailAddressImpl.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "person_id")
    @IndexColumn(name = "email_idx")
    private List emailAddresses = new ArrayList();

    //

    public PersonImpl() {
    }

    public PersonImpl(String givenName, String familyName) {
        this.familyName = familyName;
        this.givenName = givenName;
    }

    public PersonImpl(String givenName, String familyName, String preferredEmailAddress) {
        this.familyName = familyName;
        this.givenName = givenName;
        emailAddresses.add(new EmailAddressImpl(preferredEmailAddress, true));
    }

    @Override
    public String getFormattedName() {
        StringBuilder b = new StringBuilder();
        if (StringUtils.hasText(honorificPrefixes)) b.append(honorificPrefixes).append(" ");
        if (StringUtils.hasText(givenName)) b.append(givenName).append(" ");
        if (StringUtils.hasText(familyName)) b.append(familyName);
        return b.toString();
    }

    @Override
    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    @Override
    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    @Override
    public String getHonorificPrefixes() {
        return honorificPrefixes;
    }

    public void setHonorificPrefixes(String honorificPrefixes) {
        this.honorificPrefixes = honorificPrefixes;
    }

    @Override
    public List<EmailAddress> getEmailAddresses() {
        return emailAddresses;
    }

    @Override
    public EmailAddress getPreferredEmailAddress() {
        EmailAddress e = null;
        for (Object o : emailAddresses) {
            e = (EmailAddress) o;
            if (e.isPreferred()) return e;
        }
        return e;
    }

    public void setEmailAddresses(List emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public void addEmailAddress(EmailAddress address) {
        emailAddresses.add(address);
    }

    public boolean isModifiable() {
        return true;
    }

    @Override
    public String getProdId() {
        return PROD_ID;
    }
}
