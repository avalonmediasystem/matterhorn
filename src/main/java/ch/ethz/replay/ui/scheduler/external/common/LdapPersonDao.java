/*
 
 LdapPersonDao.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Sep 2, 2008

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

import ch.ethz.replay.ui.common.util.CollectionUtils;
import ch.ethz.replay.ui.common.util.RethrowException;
import ch.ethz.replay.ui.common.util.dao.DaoException;
import ch.ethz.replay.ui.common.util.dao.GenericDao;
import ch.ethz.replay.ui.common.util.dao.NotReachableException;
import ch.ethz.replay.ui.scheduler.Person;
import ch.ethz.replay.ui.scheduler.impl.EmailAddressImpl;
import ch.ethz.replay.ui.scheduler.impl.persistence.PersonDao;
import ch.ethz.replay.ui.scheduler.impl.persistence.SimplePersonFilter;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.ServiceUnavailableException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.WhitespaceWildcardsFilter;

import javax.naming.directory.SearchControls;
import java.util.Collections;
import java.util.List;

/**
 * DAO to retrieve {@link ch.ethz.replay.ui.scheduler.Person}s
 * from an LDAP directory. Uses {@link ch.ethz.replay.ui.scheduler.external.common.LdapPerson}.
 * <p/>
 * Note that all finder methods try to return locally stored entities, so you'll most likely get
 * a mixture of transient (only in LDAP directory) and detached (already stored locally) Person objects.
 * <p/>
 * Todo Calling finder methods results in two LDAP accesses, if the person is locally stored. The first
 * comes from the search itself, the second is the result of the onPostLoad callback in LdapPerson
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class LdapPersonDao implements GenericDao<Person, String> {

    private final class PersonMapper extends AbstractContextMapper {

        private boolean checkLocal;

        private PersonMapper(boolean checkLocal) {
            this.checkLocal = checkLocal;
        }

        protected Object doMapFromContext(DirContextOperations ctx) {
            LdapPerson person = new LdapPerson(ctx.getStringAttribute(idAttribute), writable);
            if (checkLocal) {
                // Check, if LdapPerson is already stored locally
                List<Person> locallyStored = localPersonDao.findByExample(person);
                if (locallyStored.size() > 0) {
                    return locallyStored.iterator().next();
                }
            }
            person.setFamilyName(ctx.getStringAttribute("sn"));
            person.setGivenName(ctx.getStringAttribute("givenName"));
            person.addEmailAddress(new EmailAddressImpl(ctx.getStringAttribute("mail")));
            return person;
        }
    }

    /**
     * Maps the query result to {@link ch.ethz.replay.ui.scheduler.external.common.LdapPerson}s
     */
    private final AbstractContextMapper PERSON_MAPPER = new PersonMapper(true);

    private final AbstractContextMapper GET_PERSON_MAPPER = new PersonMapper(false);

    private LdapTemplate ldapTemplate;

    private PersonDao localPersonDao;

    private String idAttribute = "dn";

    private boolean writable = false;

    private String objectClassName = "person";

    public LdapPersonDao() {
    }

    public LdapPersonDao(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public void setLocalPersonDao(PersonDao localPersonDao) {
        this.localPersonDao = localPersonDao;
    }

    /**
     * Returns the attribute that identifies objects in the directory. Usually this will be
     * the <code>dn</code> attribute so this is the default.
     */
    public String getIdAttribute() {
        return idAttribute;
    }

    /**
     * Set the id attribute if it is not the <code>dn</code> attribute. Another attribute
     * suitable for identifying objects may be <code>mail</code>.
     */
    public void setIdAttribute(String ldapId) {
        this.idAttribute = ldapId;
    }

    /**
     * Sets a value for the <code>objectclass</code> attribute. The default is <code>person</code>.
     * This value is used in queries.
     */
    public void setObjectClassName(String className) {
        this.objectClassName = className;
    }

    /**
     * Returns the value used for the <code>objectclass</code> attribute in queries.
     */
    public String getObjectClassName() {
        return objectClassName;
    }

    /**
     * Sets if the connected directory is writable or not. The default is <code>false</code>.
     */
    public void setWritable(boolean writable) {
        this.writable = writable;
    }

    /**
     * Returns if the connected directory is writable.
     */
    public boolean isWritable() {
        return writable;
    }

    /**
     * Supports {@link ch.ethz.replay.ui.scheduler.impl.persistence.SimplePersonFilter}.
     * The name is used as <code>cn</code> attribute.
     * <p/>
     * <em>Note:</em> The returned list is in no particular order.
     */
    public List<Person> findBy(Object filter) {
        if (filter instanceof SimplePersonFilter) {
            SimplePersonFilter f = (SimplePersonFilter) filter;
            AndFilter ldapFilter = new AndFilter();
            if (objectClassName != null) {
                ldapFilter.and(new EqualsFilter("objectclass", objectClassName));
            }
            ldapFilter.and(new WhitespaceWildcardsFilter("cn", f.getName()));
            try {
                return ldapTemplate.search("", ldapFilter.encode(), getSearchControls(), PERSON_MAPPER);
            } catch (NamingException e) {
                convertLdapException(e);
            }
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * If a value for the {@link #setObjectClassName(String) objectClass} attribute is set then a query for this
     * class is executed. A sensible value here might be <code>person</code> - which therefore is the default.
     * Whithout an object class name set, an empty list is returned.
     */
    public List<Person> findAll() {
        if (objectClassName != null) {
            try {
                return ldapTemplate.search("",
                        new EqualsFilter("objectClass", objectClassName).encode(),
                        getSearchControls(), PERSON_MAPPER);
            } catch (NamingException e) {
                convertLdapException(e);
            }
        }
        return Collections.emptyList();
    }

    /**
     * This method does not access the local database, so you'll get a transient Person object.
     */
    public Person get(String ldapId) {
        if (ldapId == null) {
            throw new IllegalArgumentException("ldapId is null");
        }
        try {
            if (idAttribute.equals("dn")) {
                return (Person) ldapTemplate.lookup(ldapId, GET_PERSON_MAPPER);
            } else {
                return (Person) CollectionUtils.first(ldapTemplate.search("",
                        new EqualsFilter(idAttribute, ldapId).encode(),
                        SearchControls.SUBTREE_SCOPE, GET_PERSON_MAPPER));
            }
        } catch (NamingException e) {
            return convertLdapException(e);
        }
    }

    public List<Person> findByExample(Person example, String... excludeProperties) {
        // Todo
        throw new UnsupportedOperationException("not yet implemented");
    }

    public Person save(Person object) {
        // Todo
        throw new UnsupportedOperationException("not yet implemented");
    }

    public Person merge(Person object) {
        // Todo
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void delete(Person object) {
        // Todo
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void flush() {
        // Todo
    }

    /**
     * Override to provide your own customized {@link javax.naming.directory.SearchControls}.
     */
    protected SearchControls getSearchControls() {
        return new SearchControls(SearchControls.SUBTREE_SCOPE, 0, 1000, null, false, false);
    }

    private <T> T convertLdapException(NamingException e) throws DaoException, RethrowException {
        if (e instanceof ServiceUnavailableException) {
            throw new NotReachableException("Cannot reach LDAP directory", e);
        } else {
            throw new RethrowException(e);
        }
    }
}
