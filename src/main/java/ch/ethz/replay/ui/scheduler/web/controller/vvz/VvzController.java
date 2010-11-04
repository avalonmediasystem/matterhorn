/*

 CalendarController.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 10, 2008

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

package ch.ethz.replay.ui.scheduler.web.controller.vvz;

import static ch.ethz.replay.core.api.common.metadata.dublincore.DublinCore.PROPERTY_TITLE;
import ch.ethz.replay.core.common.util.ForwardException;
import ch.ethz.replay.ui.common.util.dao.GenericDaoException;
import ch.ethz.replay.ui.common.util.dao.NotReachableException;
import ch.ethz.replay.ui.common.web.SessionGetter;
import ch.ethz.replay.ui.scheduler.external.ethz.LectureCourse;
import ch.ethz.replay.ui.scheduler.external.ethz.VvzConnector;
import ch.ethz.replay.ui.scheduler.external.ethz.soapvvz.Semester;
import ch.ethz.replay.ui.scheduler.web.controller.AbstractDomainController;
import ch.ethz.replay.ui.scheduler.web.controller.vvz.LectureCourseController;
import ch.ethz.replay.ui.scheduler.web.controller.ScheduleController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;

import java.util.Comparator;
import java.util.Date;

/**
 * This controller is responsible for querying the VVZ database at ETHZ.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Controller
@SessionAttributes({"lectureCourses", "vvzFilter", "semesters"})
public class VvzController extends AbstractDomainController {

    private static final Logger log = Logger.getLogger(VvzController.class);

    public static final String VIEW = "vvz/vvz";
    private static boolean configured = false;

    private VvzConnector vvz;

    /**
     * Semesters are only fetched once per conversation to reduce database access.
     */
    @ModelAttribute("semester")
    public Semester populateSemesters(WebRequest request) {
        return new SessionGetter<Semester>("semester", request) {
            @Override
            public Semester create() {
                try {
                    return vvz.findCurrentOrUpcomingSemester(new Date());
                } catch (NotReachableException e) {
                    return null;
                }
            }
        }.emptyAsNull().get();
    }

    @ModelAttribute("onlyLectures")
    public Boolean populateOnlyLectures() {
        return vvz.isOnlyLectures();
    }

    // --

    /** Just show the page. */
    @RequestMapping(method = RequestMethod.GET)
    public String index() {
        return VIEW;
    }

    /**
     * Select a Lehrveranstaltung to create a {@link ch.ethz.replay.ui.scheduler.external.ethz.LectureCourse} from
     * and edit.
     */
    @RequestMapping(method = RequestMethod.GET, value = "select")
    public Object select(@RequestParam("lv_id") int lehrveranstaltungID) {
        try {
            return redirect(LectureCourseController.class, vvz.fromLehrveranstaltungID(lehrveranstaltungID));
        } catch (Exception e) {
            return translateException(e);
        }
    }

    private ModelAndView translateException(Exception e) {
        if (e instanceof NotReachableException) {
            log.error("VVZ database is not reacheable");
            return new ModelAndView(VIEW, "error", "error.vvz.notReachable");
        }
        if (e instanceof GenericDaoException) {
            log.error("DAO error " + ((GenericDaoException) e).getType());
            return new ModelAndView(VIEW, "error", "error.vvz." + ((GenericDaoException) e).getType());
        }
        throw new ForwardException(e);
    }

    /**
     * To be called by the LectureCourseController at the end of a successful scheduling to
     * do some cleanup.
     */
    @RequestMapping(method = RequestMethod.GET, value = "finish")
    public String finishScheduling(SessionStatus status) {
        status.setComplete();
        return redirectTo(ScheduleController.class, "list");
    }

    // --

    /**
     * Sorts lecture courses ascending by title, putting empty course at the end.
     */
    public static class LectureCourseTitleComparator implements Comparator<LectureCourse> {

        public static final LectureCourseTitleComparator INSTANCE = new LectureCourseTitleComparator();

        public int compare(LectureCourse o1, LectureCourse o2) {
            int r1 = o1.getRecordings().size();
            int r2 = o2.getRecordings().size();
            if (r1 > 0 && r2 > 0 || r1 == 0 && r2 == 0)
                return o1.getDublinCoreDefaults().getFirst(PROPERTY_TITLE).compareTo(o2.getDublinCoreDefaults().getFirst(PROPERTY_TITLE));
            return r2 - r1;
        }
    }

    // --

    public void setVvzConnector(VvzConnector vvzConnector) {
        this.vvz = vvzConnector;
        configured = vvzConnector != null;
    }

    /**
     * Returns true if configured with a {@link ch.ethz.replay.ui.scheduler.external.ethz.VvzConnector}.
     */
    public static boolean isConfigured() {
        return configured;
    }
}