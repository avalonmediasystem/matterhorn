/*
 
 VvzConnectorTest.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Oct 10, 2008

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

package ch.ethz.replay.ui.scheduler.external.ethz;

import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Ignore;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * Test cases for the {@link VvzConnector}.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/ch/ethz/replay/ui/scheduler/applicationContext.xml"})
public class VvzConnectorTest {

    @Resource
    private VvzConnector vvzConnector;

    // TESTS

    @Test
    public void testConnector() {
        // todo
        /*
        List<LectureCourse> courses = vvzConnector.findBy(new VvzFilter("dietz", "2008W"));
        for (LectureCourse course : courses) {
            System.out.println(course.getRecordings().size());
        }
        */
    }
}
