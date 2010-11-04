package ch.ethz.replay.integrator.schedule;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext.xml" })
public class VvzDaoTest {

  // TODO

  // private VvzConnector vvzDao;
  //
  // @Resource
  // public void setEventDao(VvzConnector vvzDao) {
  // this.vvzDao = vvzDao;
  // }
  //
  // @Test
  // public void testBasic() {
  // VvzFilter filter = new VvzFilter();
  // filter.setPerson("koppenol");
  // List<Event> events = vvzDao.findBy(filter);
  // for (Event event : events) {
  // System.out.print(event.getDublinCoreDefaults().getTitle()
  // + ": " + event.getParts().size() + " : " + event.getContactPersons().size());
  // if (event.isSeries()) {
  // System.out.println();
  // for (Event partEvent : event.getParts()) {
  // System.out.println("    " + partEvent.getLocationName() + " : " + event.getContactPersons().size());
  // }
  // } else {
  // System.out.println(" : " + event.getLocationName());
  // }
  // }
  //
  // Calendar[] semester = vvzDao.findCurrentOrUpcomingSemester(new GregorianCalendar(2007, 5, 3).getTime());
  // if (semester != null) {
  // System.out.println(semester[0] + " - " + semester[1]);
  // }
  // }
}