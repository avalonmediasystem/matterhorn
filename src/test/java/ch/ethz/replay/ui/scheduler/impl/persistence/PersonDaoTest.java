package ch.ethz.replay.ui.scheduler.impl.persistence;

import ch.ethz.replay.ui.scheduler.Person;
import ch.ethz.replay.ui.scheduler.external.common.LdapPerson;
import ch.ethz.replay.ui.scheduler.impl.PersonImpl;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/ch/ethz/replay/ui/scheduler/applicationContext.xml"})
public class PersonDaoTest {

    @Resource(name = "personDao")
    private PersonDao localDao;

    @Resource(name = "&sessionFactory")
    private LocalSessionFactoryBean sessionFactoryBean;

    @Before
    public void setup() throws Exception {
        sessionFactoryBean.dropDatabaseSchema();
        sessionFactoryBean.createDatabaseSchema();
    }

    // TESTS

    @Test
    public void testDao() {
        Person p1 = new PersonImpl("Donald", "Knuth");
        localDao.save(p1);
        assertEquals(p1, localDao.get(p1.getId()));
        //
        localDao.save(new PersonImpl("Peter", "Licht"));
        List<Person> persons = localDao.findAll();
        assertEquals(2, persons.size());
        //
        assertEquals(1, localDao.findByExample(new PersonImpl("Donald", "Knuth")).size());
        //
        assertEquals(1, localDao.findByExample(new PersonImpl("Donald", "Duck"), "familyName").size());
        //
        assertEquals(1, localDao.findByExample(new PersonImpl("Don", "Duck"), "familyName").size());
        //
        localDao.save(new PersonImpl("Mona", "Lisa"));
        assertEquals(2, localDao.findByExample(new PersonImpl("ONA", "Duck"), "familyName").size());
        //
        assertEquals(0, localDao.findByExample(new PersonImpl("Kurt", "Weill")).size());
        //
        localDao.save(new LdapPerson("silke.dietz@env.ethz.ch"));
        assertEquals(4, localDao.findAll().size());
        //
        assertEquals(3, localDao.findByExample(new PersonImpl()).size());               
        assertEquals(1, localDao.findByExample(new LdapPerson()).size());               
    }
}