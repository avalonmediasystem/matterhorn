/*

 TestBootStrap.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 19, 2008

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

package ch.ethz.replay.ui.scheduler.web;

import ch.ethz.replay.ui.scheduler.Recording;
import ch.ethz.replay.ui.scheduler.Schedule;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Insert some test data into the application so that you have something to play with.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class TestBootStrap extends HttpServlet {

    private static final Logger Log = Logger.getLogger(TestBootStrap.class);

    public void init(ServletConfig servletConfig) throws ServletException {
        if (!Boolean.valueOf((String) servletConfig.getServletContext().getAttribute("createTestRecords"))) {
            return;
        }
        //
        Schedule schedule = (Schedule) servletConfig.getServletContext().getAttribute("schedule");
        if (schedule == null) {
            throw new IllegalStateException("No schedule configured.");
        }
        //
        TestHelper th = new TestHelper(schedule);
        Log.info("Create some test recordings");
        for (int i = 0; i < 10; i++) {
            Recording rec = th.createRandomRecording();
            schedule.schedule(rec);
        }
    }
}
