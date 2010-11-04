/*
 
 LdapBackedPersonDao.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 10 9, 2008

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

import ch.ethz.replay.ui.scheduler.Person;
import ch.ethz.replay.ui.scheduler.impl.persistence.PersonDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Useful if you want to transparently connect
 * an additional LDAP directory to {@link ch.ethz.replay.ui.scheduler.impl.ScheduleImpl}.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class LdapBackedPersonDao implements PersonDao {

    private PersonDao localDao;

    private LdapPersonDao ldapDao;

    private boolean queryLdapOnFindAll = true;

    /**
     * Sets the local person DAO used by the default Scheduler implementation.
     */
    public void setLocalPersonDao(PersonDao localDao) {
        this.localDao = localDao;
    }

    /**
     * Sets the LDAP DAO.
     */
    public void setLdapPersonDao(LdapPersonDao ldapDao) {
        this.ldapDao = ldapDao;
    }

    /**
     * Default is true.
     */
    public void setQueryLdapOnFindAll(boolean queryLdapOnFindAll) {
        this.queryLdapOnFindAll = queryLdapOnFindAll;
    }

    //

    /**
     * Queries <em>only</em> the Person DAO.
     */
    public Person get(Long id) {
        return localDao.get(id);
    }

    /**
     * Queries both local and LDAP DAO and returns the merged result.
     * The resulting list is in natural order.
     */
    public List<Person> findBy(Object filter) {
        List<Person> result = new ArrayList<Person>();
        result.addAll(localDao.findBy(filter));
        result.addAll(ldapDao.findBy(filter));
        Collections.sort(result);
        return result;
    }

    /**
     * Queries both local and LDAP DAO and returns the merged result.
     * The resulting list is in natural order.
     */
    public List<Person> findByExample(Person example, String... excludeProperties) {
        List<Person> result = new ArrayList<Person>();
        result.addAll(localDao.findByExample(example, excludeProperties));
        result.addAll(ldapDao.findByExample(example, excludeProperties));
        Collections.sort(result);
        return result;
    }

    /**
     * Queries both local and LDAP DAO and returns the merged result.
     * The resulting list is in natural order.
     */
    public List<Person> findAll() {
        if (queryLdapOnFindAll) {
            List<Person> result = new ArrayList<Person>();
            result.addAll(localDao.findAll());
            result.addAll(ldapDao.findAll());
            Collections.sort(result);
            return result;
        } else {
            return localDao.findAll();
        }
    }

    /**
     * Saves a person to the local database.
     */
    public Person save(Person person) {
        if (person instanceof LdapPerson) {
            // Need to check if already stored in local DB
            List<Person> local = localDao.findByExample(person);
            if (local.size() == 1) {
                return local.iterator().next();
            } else if (local.size() > 1) {
                throw new RuntimeException("More than one persons found in DB matching the given example. " +
                        "This should not happen.");
            }
        }
        // Ok. Can save
        return localDao.save(person);
    }

    /**
     * Not yet implemented.
     */
    public Person merge(Person object) {
        // Todo
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Deletes a person from the local database.
     */
    public void delete(Person person) {
        localDao.delete(person);
    }

    public boolean isWritable() {
        return localDao.isWritable();
    }

    /**
     * Flushes the local DAO.
     */
    public void flush() {
        localDao.flush();
    }
}
