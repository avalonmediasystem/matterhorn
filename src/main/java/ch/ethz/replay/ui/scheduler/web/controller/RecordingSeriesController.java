/*
 
 RecordingSeriesController.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 12 02, 2008

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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;
import ch.ethz.replay.ui.common.util.ReplayBuilder;
import ch.ethz.replay.ui.common.web.spring.flow.ControllerCall;
import ch.ethz.replay.ui.common.web.spring.flow.Back;
import ch.ethz.replay.ui.scheduler.RecordingSeries;
import ch.ethz.replay.ui.scheduler.impl.RecordingSeriesImpl;
import ch.ethz.replay.core.api.common.bundle.DublinCoreCatalog;
import ch.ethz.replay.core.common.bundle.dublincore.ETHZDublinCore;

import java.util.List;

/**
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Controller
public class RecordingSeriesController extends AbstractDomainController {

    public static final String VIEW = "recordingseries";

    @RequestMapping
    public String cf_back() {
        return redirectTo(RecordingSeriesController.class, "list");
    }

    // --------------------------------------------------------------------------------------------

    @ModelAttribute("series")
    public List<RecordingSeries> populateSeries() {
        return schedule.findAllRecordingSeries();
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Provides a list of all series.
     */
    @RequestMapping
    public String list() {
        return VIEW;
    }

    /**
     * Create a new series.
     */
    @RequestMapping
    public String create() {
        final ETHZDublinCoreCommand dcc = new ETHZDublinCoreCommand((ETHZDublinCore) ReplayBuilder.createDublinCoreCatalog());
        return call(DublinCoreNewController.class,
                new DublinCoreNewController.Arg(dcc, "series", false, false),
                new Back() {
                    public void apply() {
                        RecordingSeries series = new RecordingSeriesImpl(dcc.getDCIdentifier());
                        series.setDublinCore(dcc.getDublinCore());
                        schedule.saveRecordingSeries(series);
                    }
                });
    }

    @RequestMapping
    public String edit(@RequestParam("id") Long id) {
        final RecordingSeries series = schedule.getRecordingSeries(id);
        final ETHZDublinCoreCommand dcc = new ETHZDublinCoreCommand((ETHZDublinCore) series.getDublinCore());
        return call(DublinCoreNewController.class,
                new DublinCoreNewController.Arg(dcc, "series", false, false),
                new Back() {
                    public void apply() {
                        series.setDublinCore(dcc.getDublinCore());
                        schedule.saveRecordingSeries(series);
                    }
                });
    }

    @RequestMapping
    public String remove(@RequestParam("id") Long id) {
        schedule.hideRecordingSeries(schedule.getRecordingSeries(id));
        return VIEW;
    }
}
