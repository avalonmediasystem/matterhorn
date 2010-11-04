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

import ch.ethz.replay.ui.scheduler.DeviceType;
import ch.ethz.replay.ui.scheduler.Schedule;
import ch.ethz.replay.ui.scheduler.impl.JobTicketImporter;
import org.apache.log4j.Logger;

import javax.servlet.*;
import java.io.IOException;
import java.util.List;

/**
 * Set up the application.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class Initializer implements Filter {

    private static final Logger Log = Logger.getLogger(Initializer.class);

    private boolean initialized = false;
    private ServletContext context;

    public void init(FilterConfig filterConfig) throws ServletException {
        context = filterConfig.getServletContext();
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        synchronized (this) {
            if (!initialized) {
                Log.info("initialize application...");
                if (Boolean.valueOf((String) context.getAttribute("createDevices"))) {
                    List<DeviceType> devices = (List<DeviceType>) context.getAttribute("devices");
                    if (devices == null) {
                        throw new IllegalStateException("Please configure a list of devices under the name 'devices' " +
                                "in the servlet context.");
                    }
                    Schedule schedule = (Schedule) context.getAttribute("schedule");
                    for (DeviceType device : devices) {
                        Log.info("Register device " + device.getName());
                        schedule.registerDeviceType(device);
                    }
                }
                JobTicketImporter importer = (JobTicketImporter) context.getAttribute("jobTicketImporter");
                if (importer == null) {
                    throw new IllegalStateException("Please configure a JobTicketImporter under " +
                            "the name 'jobTicketImporter' in the servlet context.");
                }
                importer.importToDatabase();
                initialized = true;
            }
        }
        //
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {
        //todo
    }
}