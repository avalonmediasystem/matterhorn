package ch.ethz.replay.ui.scheduler.impl.persistence;

import ch.ethz.replay.ui.scheduler.*;
import ch.ethz.replay.ui.scheduler.impl.*;
import ch.ethz.replay.ui.common.util.Utils;
import ch.ethz.replay.ui.common.util.ReplayBuilder;
import ch.ethz.replay.core.api.common.bundle.DublinCoreCatalog;
import static ch.ethz.replay.core.api.common.metadata.dublincore.DublinCore.*;
import ch.ethz.replay.core.api.exception.MetadataException;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext.xml" })
public class RecordingDaoTest {

  @Resource(name = "recordingDao")
  private RecordingDao recordingDao;

  @Resource(name = "attachmentDao")
  private AttachmentDao attachmentDao;

  @Resource(name = "personDao")
  private PersonDao personDao;

  @Resource(name = "recordingSeriesDao")
  private RecordingSeriesDaoImpl recordingSeriesDao;

  // TESTS

  @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
  public void testNullConstraint() {
    Recording r = new RecordingImpl();
    recordingDao.save(r);
  }

  @Test
  public void testDao() throws MetadataException {
    DublinCoreCatalog dc = ReplayBuilder.createDublinCoreCatalog();
    Recording recording = new RecordingImpl();
    Location room = new SimpleLocation("HGZ1");
    recording.setLocation(room);
    recording.setStartDate(Utils.createDate(2008, 10, 10, 12, 0, 0));
    recording.setEndDate(Utils.createDate(2008, 10, 10, 13, 0, 0));
    recording.setDublinCore(dc);
    assertTrue(((BaseEntity) room).isNew());
    recordingDao.save(recording);
    //
    assertEquals(1, recordingDao.count());
    //
    assertEquals(1, recordingDao.getAllScheduledLocations().size());
    //
    dc.set(PROPERTY_TITLE, "Aurelien");
    dc.set(PROPERTY_CREATOR, "Louis Aragon");
    recording.setDublinCore(dc);
    assertEquals("Aurelien", recording.getTitle());
    recordingDao.save(recording);
    assertEquals("Aurelien", ((DublinCoreAttachment) attachmentDao.get(recording.getAttachment().getId()))
            .getContent().getFirst(PROPERTY_TITLE));
    assertEquals(1, recordingDao.findBy(new RecordingFilter().setFreeText("Aurelien")).size());
    assertEquals(1, recordingDao.findBy(new RecordingFilter().setFreeText("aragon")).size());
  }

  @Test
  @Transactional
  public void testSeries() {
    Recording recording = new RecordingImpl();
    Location room = new SimpleLocation("EMZ3");
    RecordingSeries series = new RecordingSeriesImpl();
    series.setDublinCore(ReplayBuilder.createDublinCoreCatalog());
    recording.setLocation(room);
    recording.setStartDate(Utils.createDate(2008, 10, 10, 12, 0, 0));
    recording.setEndDate(Utils.createDate(2008, 10, 10, 13, 0, 0));
    recording.setDublinCore(ReplayBuilder.createDublinCoreCatalog());
    series.addRecording(recording);
    recordingSeriesDao.save(series);
    // Must be still one, because RecordingSeries does not cascade save to Recordings
    assertEquals(1, recordingDao.findAll().size());
    //
    // Save to do the identity test...
    recordingDao.save(recording);
    assertEquals(recording.getId(), recordingDao.get(recording.getId()).getSeries().getRecordings().iterator().next()
            .getId());
    // ... and now delete it again, otherwise you can't do the following test because of
    // org.hibernate.ObjectDeletedException: deleted object would be re-saved by cascade
    // (remove deleted object from associations): [ch.ethz.replay.ui.scheduler.impl.RecordingSeriesImpl#1]
    recordingDao.delete(recording);
    //
    // Deletion of a series has to delete its recordings also
    recordingSeriesDao.delete(series);
    assertEquals(0, recordingSeriesDao.findAll().size());
    assertEquals(1, recordingDao.findAll().size());
  }

  @Test
  @Transactional
  public void testContactPersons() {
    Recording recording = new RecordingImpl();
    Location room = new SimpleLocation("ABM1");
    recording.setLocation(room);
    recording.setStartDate(Utils.createDate(2008, 10, 10, 12, 0, 0));
    recording.setEndDate(Utils.createDate(2008, 10, 10, 13, 0, 0));
    recording.setDublinCore(ReplayBuilder.createDublinCoreCatalog());
    Person person = new PersonImpl("Dieter", "Mueller");
    recording.getContactPersons().add(person);
    recordingDao.save(recording);
    //
    assertEquals(1, recordingDao.get(recording.getId()).getContactPersons().size());
    assertEquals("Dieter", recordingDao.get(recording.getId()).getContactPersons().iterator().next().getGivenName());
    // Flush the session, otherwise the next query will not succeed
    recordingDao.flush();
    assertEquals(1, recordingDao.findBy(new RecordingFilter().setFreeText("mueller")).size());
    //
    assertEquals(1, recordingDao.get(recording.getId()).getContactPersons().size());
    //
    recordingDao.removeContactPersonFromRecordings(person);
    assertNotNull(personDao.get(person.getId()));
    assertEquals(0, recordingDao.findBy(new RecordingFilter().setFreeText("mueller")).size());
  }

  @Test
  public void testVarious() {
    Recording recording = new RecordingImpl();
    recording.setTitle("A recording");
    Location room = new SimpleLocation("ABM1");
    recording.setLocation(room);
    recording.setStartDate(Utils.createDate(2008, 10, 10, 12, 0, 0));
    recording.setEndDate(Utils.createDate(2008, 10, 10, 13, 0, 0));
    recording.setDublinCore(ReplayBuilder.createDublinCoreCatalog());
    save(recording);
    Recording recording2 = recordingDao.get(recording.getId());
    assertTrue(recording != recording2);
    assertEquals(recording.getId(), recording2.getId());
    recording.setTitle("Blubb");
    save(recording);
    assertEquals(recording.getId(), recording2.getId());
    assertEquals("Blubb", recordingDao.get(recording.getId()).getTitle());
  }

  @Transactional
  public void save(Recording rec) {
    recordingDao.save(rec);
  }
}