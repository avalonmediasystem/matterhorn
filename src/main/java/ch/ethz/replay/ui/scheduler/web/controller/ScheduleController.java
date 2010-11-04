/*

 ScheduleController.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 21, 2008

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

package ch.ethz.replay.ui.scheduler.web.controller;

import ch.ethz.replay.core.api.common.bundle.DublinCoreCatalog;
import ch.ethz.replay.core.api.common.metadata.dublincore.DublinCore;
import ch.ethz.replay.core.common.bundle.dublincore.ETHZDublinCore;
import ch.ethz.replay.ui.common.web.spring.flow.ControllerCall;
import ch.ethz.replay.ui.common.web.propertyeditors.SimplePropertyEditorSupport;
import ch.ethz.replay.ui.common.util.Extender;
import ch.ethz.replay.ui.scheduler.Recording;
import ch.ethz.replay.ui.scheduler.RecordingFilter;
import ch.ethz.replay.ui.scheduler.RecordingSeries;
import ch.ethz.replay.ui.scheduler.MetadataTagged;
import ch.ethz.replay.ui.scheduler.impl.RecordingImpl;
import ch.ethz.replay.ui.scheduler.impl.RecordingSeriesImpl;
import ch.ethz.replay.ui.scheduler.impl.util.RecordingValidator;
import ch.ethz.replay.ui.scheduler.web.extension.RecordingExtensionImpl;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;

/**
 * To display the schedule.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Controller
@RequestMapping
public class ScheduleController extends AbstractDomainController {

    public static final String VIEW = "schedule";

    @Autowired(required = true)
    private RecordingValidator recVal;

    @InitBinder
    public void initBinder(WebDataBinder binder, final Locale locale) {
        binder.registerCustomEditor(Date.class,
                new CustomDateEditor(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale), true));
        binder.registerCustomEditor(RecordingSeries.class, new SimplePropertyEditorSupport<RecordingSeries>() {
            protected String toString(RecordingSeries o) {
                return o.getTitle();
            }

            protected RecordingSeries toObject(String v) {
                Long id = Long.parseLong(v);
                return id != -1 ? schedule.getRecordingSeries(id) : null;
            }
        });
    }

    // --------------------------------------------------------------------------------------------

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView cf_back() {
        ControllerCall call = getReturnedCall();
        if (call.returnsFrom(DublinCoreNewController.class) && call.hasReturnValue()) {
            ETHZDublinCoreCommand dcc = call.getReturnValue(ETHZDublinCoreCommand.class);
            if (call.hasSaveState(MetadataTagged.class)) {
                MetadataTagged m = call.getSaveState(MetadataTagged.class);
                m.setDublinCore(dcc.getDublinCore());
                if (m instanceof Recording)
                    schedule.schedule((Recording) m);
                else if (m instanceof RecordingSeries) {
                    RecordingSeries r = schedule.getRecordingSeries(((RecordingSeriesImpl) m).getId());
                    r.setDublinCore(dcc.getDublinCore());
                    schedule.saveRecordingSeries(r);
                }
            }
        }
        return list();
    }

    // --------------------------------------------------------------------------------------------

    @ModelAttribute("series")
    public List<RecordingSeries> populateRecordingSeries() {
        return schedule.findAllRecordingSeries();
    }

    // --------------------------------------------------------------------------------------------

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView list() {
        ScheduleFilterCommand filter = getOrCreateScheduleFilterCommand();
        return createModelAndView(filter);
    }

    /**
     * List recordings filtered by current settings.
     */
    @RequestMapping(method = RequestMethod.GET, params = "location")
    public ModelAndView list(@RequestParam(value = "location") String name) {
        // Always use a fresh ScheduleFilterCommand
        ScheduleFilterCommand filter = new ScheduleFilterCommand(new Date());
        filter.setLocationName(name);
        return createModelAndView(filter);
    }

    /**
     * List recordings filtered by entered filter data.
     */
    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView  list(@ModelAttribute("scheduleFilter") ScheduleFilterCommand filter) {
        // store for subsequent requests
        scheduleSession.setScheduleFilterCommand(filter);
        return createModelAndView(filter);
    }

    /**
     * Delete a recording by id.
     */
    @RequestMapping("delete")
    public ModelAndView delete(@RequestParam("id") Long id) {
        schedule.removeRecording(id);
        return createModelAndView(getOrCreateScheduleFilterCommand());
    }

    @RequestMapping("editperson")
    public String editPerson(@RequestParam("id") Long id) {
        return call(EditPersonController.class, id);
    }

    /**
     * @param id the Recording ID
     */
    @RequestMapping("editdublincore")
    public String editDublinCore(@RequestParam("id") Long id) {
        Recording rec = schedule.getRecording(id);
        ETHZDublinCore dc = (ETHZDublinCore) rec.getDublinCore();
        return call(DublinCoreNewController.class,
                new DublinCoreNewController.Arg(new ETHZDublinCoreCommand(dc), "episode", true, false),
                rec);
    }

    /**
     * @param id the series ID
     */
    @RequestMapping("editseriesdublincore")
    public String editSeriesDublinCore(@RequestParam("id") Long id) {
        RecordingSeries series = schedule.getRecordingSeries(id);
        ETHZDublinCore dc = (ETHZDublinCore) series.getDublinCore();
        return call(DublinCoreNewController.class,
                new DublinCoreNewController.Arg(new ETHZDublinCoreCommand(dc), "series", false, false),
                series);
    }
    
    @RequestMapping("filterbyseries")                                           
    public ModelAndView filterBySeries(@RequestParam("id") Long id) {
        ScheduleFilterCommand filter = getOrCreateScheduleFilterCommand();
        filter.setSeries(schedule.getRecordingSeries(id));
        return createModelAndView(filter);
    }

    // --------------------------------------------------------------------------------------------

    private ModelAndView createModelAndView(ScheduleFilterCommand filter) {
        ModelAndView mv = new ModelAndView(VIEW);
        mv.addObject("recordings",
                new Extender<Recording>(RecordingExtensionImpl.class){
                    @Override
                    protected Object newExtensionFor(Recording recording) {
                        return new RecordingExtensionImpl(recording, recVal);
                    }
                }.override(schedule.findRecordings(
                        new RecordingFilter(filter.getLocationName(),
                                null,
                                filter.getStartingAfter(),
                                null,
                                filter.getEndingAfter(),
                                filter.getFreeSearch(),
                                null,
                                filter.getSeriesId(),
                                null))));
        mv.addObject("scheduleFilter", filter);
        return mv;
    }

    public ScheduleFilterCommand getOrCreateScheduleFilterCommand() {
        if (scheduleSession.getScheduleFilterCommand() == null) {
            scheduleSession.setScheduleFilterCommand(new ScheduleFilterCommand(new Date()));
        }
        return scheduleSession.getScheduleFilterCommand();
    }
}
