package ch.ethz.replay.ui.scheduler.impl.persistence;

import ch.ethz.replay.ui.scheduler.Location;
import ch.ethz.replay.ui.scheduler.impl.DeviceTypeImpl;
import ch.ethz.replay.ui.scheduler.impl.SimpleLocation;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/ch/ethz/replay/ui/scheduler/applicationContext.xml"})
public class LocationDaoTest {

    @Resource
    private LocationDao locationDao;

    @Resource
    private DeviceTypeDao deviceTypeDao;

    // TESTS

    @Test
    @Transactional // necessary to access lazy collections
    public void testDao() {
        Location room = new SimpleLocation("MBZ2");
        room.getDevices().add(new DeviceTypeImpl("video"));
        locationDao.save(room);
        //
        assertEquals("video", locationDao.get(room.getId()).getDevices().iterator().next().getName());
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    @Transactional
    public void testUnique() {
        Location room1 = new SimpleLocation("room1");
        locationDao.save(room1);
        assertNotNull(room1.getId());
        Location room2 = new SimpleLocation("room1");
        locationDao.save(room2);
    }
}