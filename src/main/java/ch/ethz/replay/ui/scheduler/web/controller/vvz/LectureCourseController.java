/*
 
 LectureCourseController.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Sep 17, 2008

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

import ch.ethz.replay.core.api.common.metadata.dublincore.DublinCore;
import ch.ethz.replay.core.common.bundle.dublincore.ETHZDublinCore;
import ch.ethz.replay.core.common.util.CollectionSupport;
import static ch.ethz.replay.core.common.util.CollectionSupport.map;
import static ch.ethz.replay.core.common.util.CollectionSupport.filter;
import ch.ethz.replay.ui.common.util.*;
import ch.ethz.replay.ui.scheduler.Recording;
import ch.ethz.replay.ui.scheduler.external.ethz.LectureCourse;
import ch.ethz.replay.ui.scheduler.impl.util.RecordingsValidator;
import ch.ethz.replay.ui.scheduler.web.controller.*;
import ch.ethz.replay.ui.scheduler.web.extension.RecordingExtensionImpl;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Edit a lecture course to prepare for scheduling. This is custom controller satisfying ETH Zurich needs.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Controller
@SessionAttributes({
        LectureCourseController.S_COURSE,
        LectureCourseController.S_SCHEDULE,
        LectureCourseController.S_RECORDINGS,
        LectureCourseController.S_DCD})
public class LectureCourseController
        extends AbstractDomainController {

    public static final String VIEW = "vvz/lecturecourse";

    // Session attributes

    public static final String S_COURSE = "course";
    public static final String S_SCHEDULE = "scheduleRecordings";
    public static final String S_RECORDINGS = "recordings";
    public static final String S_DCD = "dcd";

    //

    @Autowired(required = true)
    private RecordingsValidator recsVal;

    // --

    @RequestMapping("cf_redirect")
    public ModelAndView cf_redirect() {
        LectureCourse course = getRedirectArgument(LectureCourse.class);
        return new ModelAndView(VIEW)
                .addObject(S_COURSE, course)
                .addObject(S_RECORDINGS, extendRecordings(course))
                .addObject(S_DCD, new ETHZDublinCoreCommand((ETHZDublinCore) course.getDublinCoreDefaults()));
    }

    @RequestMapping("cf_back")
    public String cf_back() {
        return VIEW;
    }

    // --

    /**
     * Edit the recording defaults
     */
    @RequestMapping(value = "editrecordingdefaults")
    public String editRecordingDefaults(@ModelAttribute(S_COURSE) LectureCourse course) {
        return call(RecordingController.class,
                new RecordingController.Arg(course.getRecordingDefaults(),
                false, "startDate", "endDate", "dublinCore", "seriesDublinCore"));
    }

    /**
     * Edit a certain recording.
     */
    @RequestMapping("edit_r")
    public String editRecording(@RequestParam("id") Integer nr,
                                @ModelAttribute(S_COURSE) LectureCourse course,
                                @ModelAttribute(S_RECORDINGS) List<Recording> recordings) {
        Recording selected = recordings.get(nr);
        return call(RecordingController.class,
                new RecordingController.Arg(selected, true, "seriesDublinCore"));
    }

    /**
     * Edit the Dublin Core defaults template.
     */
    @RequestMapping("edit_dcd")
    public String editDublinCoreDefaults(@ModelAttribute(S_DCD) ETHZDublinCoreCommand dcc) {
        return call(DublinCoreNewController.class,
                new DublinCoreNewController.Arg(dcc,"episode-defaults", true, true));
    }

    /**
     * Resets all Dublin Cores to the default by deleting them.
     */
    @RequestMapping("reset_dcs")
    public String resetDublinCores(@ModelAttribute(S_DCD) ETHZDublinCoreCommand dcc,
                                   @ModelAttribute(S_RECORDINGS) List<Recording> recordings) {
        for (Recording r : recordings) {
            dcc.syncAssigned((ETHZDublinCore) r.getDublinCore());
        }
        return VIEW;
    }

    /**
     * Reset an event's Dublin Core.
     *
     * @param nr the number of the recording as supplied to the JSP
     */
    @RequestMapping("reset_dc")
    public String resetDublinCore(@RequestParam("id") int nr,
                                  @ModelAttribute(S_DCD) ETHZDublinCoreCommand dcc,
                                  @ModelAttribute(S_RECORDINGS) List<Recording> recordings) {
        dcc.syncAssigned((ETHZDublinCore) recordings.get(nr).getDublinCore());
        return VIEW;
    }

    /**
     * Reset a recording to its defaults.
     */
    @RequestMapping("reset_r")
    public String resetRecording(@RequestParam("id") Integer nr,
                                 @ModelAttribute(S_COURSE) LectureCourse course,
                                 @ModelAttribute(S_RECORDINGS) List<Recording> recordings) {
        resetToDefaults(course.getRecordingDefaults(), recordings.get(nr));
        return VIEW;
    }

    /**
     * Reset all recordings to defaults.
     */
    @RequestMapping("reset_rs")
    public String resetRecordings(@ModelAttribute(S_COURSE) LectureCourse course) {
        Recording defaults = course.getRecordingDefaults();
        for (Recording rec : course.getRecordings()) {
            resetToDefaults(defaults, rec);
        }
        return VIEW;
    }

    /**
     * Edit a concrete lecture's Dublin Core.
     *
     * @param nr the number of the recording as supplied to the JSP
     */
    @RequestMapping("edit_dc")
    public String editDublinCore(@RequestParam("id") Integer nr,
                                 @ModelAttribute(S_RECORDINGS) List<Recording> recordings,
                                 @ModelAttribute(S_COURSE) LectureCourse course) {
        Recording rec = recordings.get(nr);
        DublinCore dc = rec.getDublinCore();
        if (dc == null) {
            throw new RuntimeException("Recording does not have a Dublin Core");
        }
        return call(DublinCoreNewController.class,
                new DublinCoreNewController.Arg(new ETHZDublinCoreCommand((ETHZDublinCore) rec.getDublinCore()), "episode", true, false),
                rec);
    }

    @RequestMapping("edit_sdc")
    public String editSeriesDublinCore(@ModelAttribute(S_COURSE) LectureCourse course) {
        return call(DublinCoreNewController.class,
                new DublinCoreNewController.Arg(new ETHZDublinCoreCommand((ETHZDublinCore) course.getSeries().getDublinCore()), "series", false, false),
                course.getSeries());
    }

    /**
     * Schedules all events as recordings.
     */
    @RequestMapping(value = "schedule")
    public Object schedule(@ModelAttribute(S_COURSE) LectureCourse course,
                           @ModelAttribute(S_RECORDINGS) List<Recording> recordings,
                           @RequestParam("selected") final Set<Integer> selected, // Cool! Arrays can be converted to sets by default
                           SessionStatus status) {
        // Filter recordings to schedule
        List<Recording> scheduled = map(filter(recordings, new CollectionSupport.Predicate<Recording>() {
            public boolean evaluate(Recording recording, int index) {
                return selected.contains(index);
            }
        }), new CollectionSupport.Mapper<Recording, Recording>() {
            public Recording map(Recording rec) {
                return Extender.reduceAll(rec);
            }
        });
        // Validate
        Errors result = recsVal.validate(scheduled);
        if (result.hasErrors() || scheduled.isEmpty()) {
            for (Object e : result.getAllErrors()) System.out.println("[ERROR] " + e);
            return new ModelAndView(VIEW, "selected", set2Map(selected));
        } else {
            for (Recording r : scheduled) {
                beforeSchedule(r, course);
                schedule.schedule(r);
            }
            // End conversation
            status.setComplete();
            return redirectTo(VvzController.class, "finish");
        }
    }

    /**
     * Prepare recording to get scheduled.
     *
     * @param recording the recording to prepare
     * @param course the course this recording is derived from
     */
    private void beforeSchedule(Recording recording, LectureCourse course) {
        // If recording does not have it's own Dublin Core use the defaults
        if (recording.getDublinCore() == null) {
            recording.setDublinCore(course.getDublinCoreDefaults());
        }

        // QUICK HACK: add an offset of -5/+5min to each recording
        Calendar c = Calendar.getInstance();
        c.setTime(recording.getStartDate());
        c.add(Calendar.MINUTE, -5);
        recording.setStartDate(c.getTime());
        c.setTime(recording.getEndDate());
        c.add(Calendar.MINUTE, 5);
        recording.setEndDate(c.getTime());
        // /QUICK HACK
    }

    // --

    /**
     * Convert a set to a map with its values being key and value.
     */
    private Map set2Map(Set set) {
        Map map = new HashMap(set.size());
        for (Object e : set) {
            map.put(e, e);
        }
        return map;
    }

    /**
     * Reset recording <code>rec</code> to the defaults provided by <code>defaults</code>.
     */
    private void resetToDefaults(Recording defaults, Recording rec) {
        if (defaults.getLocation() != null) rec.setLocation(defaults.getLocation());
        if (!defaults.getDevices().isEmpty()) rec.setDevices(defaults.getDevices());
        if (!defaults.getContactPersons().isEmpty()) rec.setContactPersons(defaults.getContactPersons());
        if (defaults.getJobTicket() != null) rec.setJobTicket(defaults.getJobTicket());
    }

    private List<Recording> extendRecordings(final LectureCourse course) {
        return new Extender<Recording>(RecDifferentExtImpl.class) {
            @Override
            protected Object newExtensionFor(Recording object) {
                return new RecDifferentExtImpl(object, course.getRecordingDefaults());
            }
        }.override(new Extender<Recording>(RecordingExtensionImpl.class) {
            @Override
            protected Object newExtensionFor(Recording object) {
                return new RecordingExtensionImpl(object, recsVal.getRecordingValidator());
            }
        }.override(course.getRecordingsGrouped()));
    }

    // --

    public interface RecDifferentExt {

        boolean isDifferent();
    }

    public static class RecDifferentExtImpl
            extends ExtensionSupport<Recording>
            implements RecDifferentExt {

        private Recording defaults;

        public RecDifferentExtImpl(Recording object, Recording defaults) {
            super(object);
            this.defaults = defaults;
        }

        public boolean isDifferent() {
            boolean equals = true;
            if (defaults.getLocation() != null) {
                equals &= Utils.equals(object.getLocation(), defaults.getLocation());
            }
            if (defaults.getJobTicket() != null) {
                equals &= Utils.equals(object.getJobTicket(), defaults.getJobTicket());
            }
            if (defaults.getDevices().size() > 0) {
                equals &= Utils.equals(object.getDevices(), defaults.getDevices());
            }
            if (defaults.getContactPersons().size() > 0) {
                equals &= Utils.equals(object.getContactPersons(), defaults.getContactPersons());
            }
            return !equals;
        }
    }
}
