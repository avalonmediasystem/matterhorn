/*

 PersonCommand.java
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

package ch.ethz.replay.ui.scheduler.web.controller;

import ch.ethz.replay.core.api.common.vcard.EmailAddress;
import ch.ethz.replay.ui.scheduler.impl.PersonImpl;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.List;

/**
 * Command class to take and validate person data.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class PersonCommand implements Validator {

    private String emailAddress;

    private PersonImpl person;

    public PersonCommand(PersonImpl person) {
        this.person = person;
    }

    public PersonCommand() {
        this.person = new PersonImpl();
    }

    public void copyFrom(PersonImpl person) {
        List<EmailAddress> addresses = person.getEmailAddresses();
        if (addresses != null && addresses.size() > 0) {
            if (addresses != null && addresses.size() > 0) {
                emailAddress = addresses.get(0).getAddress();
            }
        }
    }

    public String getTitle() {
        return person.getHonorificPrefixes();
    }

    public void setTitle(String title) {
        person.setHonorificPrefixes(title);
    }

    public String getFamilyName() {
        return person.getFamilyName();
    }

    public void setFamilyName(String familyName) {
        person.setFamilyName(familyName);
    }

    public String getGivenName() {
        return person.getGivenName();
    }

    public void setGivenName(String givenName) {
        person.setGivenName(givenName);
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public PersonImpl getPerson() {
        return person;
    }

    public boolean supports(Class clazz) {
        return this.getClass().isAssignableFrom(clazz);
    }

    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "familyName", "empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "givenName", "empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "emailAddress", "empty");
    }
}

