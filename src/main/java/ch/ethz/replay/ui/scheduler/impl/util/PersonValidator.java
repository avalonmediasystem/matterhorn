/*

 PersonValidator.java
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

package ch.ethz.replay.ui.scheduler.impl.util;

import ch.ethz.replay.ui.scheduler.Person;
import ch.ethz.replay.core.api.common.vcard.EmailAddress;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates a {@link ch.ethz.replay.ui.scheduler.Person}.
 * 
 * 
 */
public class PersonValidator implements Validator {

  public boolean supports(Class clazz) {
    return Person.class.isAssignableFrom(clazz);
  }

  public void validate(Object target, Errors errors) {
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "familyName", "empty");
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "givenName", "empty");
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "emailAddresses", "empty");
    // todo validate email
    Person p = (Person) target;
    for (EmailAddress address : p.getEmailAddresses()) {
      if (!address.getAddress().contains("@"))
        errors.rejectValue("emailAddresses", "empty");
    }
  }
}
