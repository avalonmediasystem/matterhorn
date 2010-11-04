/*

 Person.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 8, 2008

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

import ch.ethz.replay.core.api.common.vcard.VCard;
import ch.ethz.replay.core.api.common.vcard.EmailAddress;

import java.util.List;

/**
 * {@link Recording Recordings} have associated persons, which may be the speaker or just someone who wants to
 * get notified about the state of a recording, etc.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public interface Person extends External, VCard, Comparable<Person> {

    /**
     * Returns the internal id. This is id does not have any business meanings.
     */
    Long getId();

    void setGivenName(String name);

    void setFamilyName(String name);

    void setHonorificPrefixes(String prefixes);
    
    void setEmailAddresses(List<EmailAddress> emailAddresses);

    void addEmailAddress(EmailAddress emailAddress);
}
