/*
 
 LdapPersonDaoProvider.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 9 30, 2008

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

package ch.ethz.replay.ui.scheduler.external.common;

import javax.annotation.Resource;

/**
 * Bridge from unmanaged {@link LdapPerson}s to the Application Context.
 * <p/>
 * This pattern avoids the use of AspectJ weaving to put LdapPerson objects under Spring control.
 * <p/>
 * <strong>Note</strong> that
 * <em>only one</em> Application Context per ClassLoader can be responsible
 * for this provider because the DAO is saved in a static variable.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class LdapPersonDaoProvider {

    private static LdapPersonDao ldapPersonDao;

    @Resource
    public void setLdapPersonDao(LdapPersonDao ldapPersonDao) {
        if (LdapPersonDaoProvider.ldapPersonDao != null) {
            throw new IllegalStateException("The ldapPersonDao is already set. Do you have multiple " +
                    "Application Contexts per Classloader installed?");
        }
        LdapPersonDaoProvider.ldapPersonDao = ldapPersonDao;
    }

    public static LdapPersonDao get() {
        return ldapPersonDao;
    }
}
