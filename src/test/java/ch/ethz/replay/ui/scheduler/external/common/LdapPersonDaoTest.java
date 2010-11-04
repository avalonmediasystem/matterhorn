package ch.ethz.replay.ui.scheduler.external.common;

import ch.ethz.replay.ui.common.util.CollectionUtils;
import ch.ethz.replay.ui.scheduler.HibernateDatabaseTestSupport;
import ch.ethz.replay.ui.scheduler.Person;
import ch.ethz.replay.ui.scheduler.impl.persistence.PersonDao;
import ch.ethz.replay.ui.scheduler.impl.persistence.SimplePersonFilter;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * Unit tests for {@link LdapPersonDao} &amp; Co.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/ch/ethz/replay/ui/scheduler/applicationContext.xml"})
public class LdapPersonDaoTest extends HibernateDatabaseTestSupport {

    @Resource(name = "ldapPersonDao")
    private LdapPersonDao externalDao;

    @Resource(name = "personDao")
    private PersonDao localDao;

    // TESTS

    @Test
    public void testDao() {
        List<Person> persons = externalDao.findBy(new SimplePersonFilter("dietz", 10));
        for (Person p : persons) {
            System.out.println(p.getFormattedName());
        }
        assertTrue(persons.size() > 0);
        //
        persons = externalDao.findBy(new SimplePersonFilter("Silke Dietz", 1));
        assertTrue(persons.size() == 1);
        Person silke = CollectionUtils.first(persons);
        System.out.println("Silke Dietz's email address " + silke.getPreferredEmailAddress().getAddress());
        System.out.println("Silke Dietz's LDAP ID " + ((LdapPerson) silke).getLdapId());
        assertNotNull(externalDao.get(silke.getPreferredEmailAddress().getAddress()));
        //
        localDao.save(silke);
        assertEquals(silke, localDao.get(silke.getId()));
        assertEquals(1, localDao.findAll().size());
    }

    @Test
    public void testLocalStorage() {
        List<Person> persons = externalDao.findBy(new SimplePersonFilter("Silke Dietz", 1));
        assertTrue(persons.size() == 1);
        LdapPerson silke = (LdapPerson) persons.iterator().next();
        localDao.save(silke);
        assertEquals(silke.getId(), localDao.get(silke.getId()).getId());
        assertNull(externalDao.get(silke.getLdapId()).getId());
    }
}