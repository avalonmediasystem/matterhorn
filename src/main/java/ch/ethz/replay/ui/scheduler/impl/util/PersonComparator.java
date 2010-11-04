/*
 
 PersonComparator.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 9 9, 2008

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

import java.util.Comparator;

/**
 * To compare and sort {@link Person}s.
 * 
 * 
 */
public class PersonComparator implements Comparator<Person> {

  private int ascending = 1;

  /**
   * Creates a comparator for an ascending sort.
   */
  public PersonComparator() {
  }

  /**
   * Creates for an ascending or descending sort.
   */
  public PersonComparator(boolean ascending) {
    this.ascending = ascending ? 1 : -1;
  }

  public int compare(Person p1, Person p2) {
    int c = unNull(p1.getFamilyName()).compareToIgnoreCase(unNull(p2.getFamilyName()));
    if (c == 0) {
      c = unNull(p1.getGivenName()).compareToIgnoreCase(unNull(p2.getGivenName()));
      if (c == 0) {
        EmailAddress email1 = p1.getEmailAddress();
        EmailAddress email2 = p1.getEmailAddress();
        if (email1 == null && email2 == null)
          c = 0;
        else if (email1 == null)
          c = 1;
        else if (email2 == null)
          c = -1;
        else
          c = unNull(email1.getAddress()).compareToIgnoreCase(unNull(email2.getAddress()));
      }
    }
    return c * ascending;
  }

  private String unNull(String s) {
    if (s != null)
      return s;
    return "";
  }
}
