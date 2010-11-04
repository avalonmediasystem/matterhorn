package ch.ethz.replay.ui.scheduler.impl.persistence;

import ch.ethz.replay.ui.scheduler.Attachment;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * 
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/ch/ethz/replay/ui/scheduler/applicationContext.xml" })
public class AttachmentDaoTest {

  @Resource(name = "attachmentDao")
  private AttachmentDao dao;

  // TESTS

  @Test
  public void testDao() {
    Attachment a = new DocumentAttachmentImpl("Attachment Content", "generic", MimeTypes.TEXT);
    dao.save(a);
    assertNotNull(dao.get(a.getId()));
    System.out.println(CollectionUtils.first(dao.findAll()).getContent().getClass().getName());
    assertTrue(dao.get(a.getId()) instanceof DocumentAttachment);
    assertEquals("Attachment Content", dao.get(a.getId()).getContent());
  }

  @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
  public void testNullConstraint() {
    DublinCoreAttachment d = new DublinCoreAttachmentImpl();
    dao.save(d);
  }
}