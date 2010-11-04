/*

 DublinCoreController.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created Sep 27, 2009

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
import ch.ethz.replay.core.common.bundle.dublincore.ETHZDublinCoreValidator;
import ch.ethz.replay.core.common.bundle.dublincore.DublinCoreValidator;
import ch.ethz.replay.core.common.bundle.dublincore.utils.Temporal;
import ch.ethz.replay.ui.scheduler.Recording;
import ch.ethz.replay.ui.scheduler.web.propertyeditors.DCMIPeriodEditor;
import ch.ethz.replay.ui.scheduler.web.propertyeditors.ISODurationEditor;
import ch.ethz.replay.ui.scheduler.web.propertyeditors.TemporalEditor;
import static ch.ethz.replay.ui.scheduler.web.controller.DublinCoreNewController.*;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.beans.PropertyEditor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Handles the Dublin Core form.
 * <p>
 * {@link DublinCoreNewController.Arg} => {@link ch.ethz.replay.core.common.bundle.dublincore.ETHZDublinCore}
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Controller
@SessionAttributes(S_FORM)
public class DublinCoreNewController extends AbstractDomainController {

    public static final String VIEW = "dublincore_new";

    // Session attributes

    static final String S_FORM = "form";

    @Resource
    private DublinCoreValidator dcSeriesValidator;

    @Resource
    private DublinCoreValidator dcEpisodeValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder, Locale locale) {
        super.initBinder(binder);
        PropertyEditor dateEditor = new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true);
        PropertyEditor temporalEditor = new TemporalEditor(dateEditor, new ISODurationEditor(), new DCMIPeriodEditor(dateEditor));
        binder.registerCustomEditor(Temporal.class, "dc.temporal", temporalEditor);
        binder.registerCustomEditor(Temporal.class, "dc.created", temporalEditor);
        binder.registerCustomEditor(Date.class, "dc.issued", dateEditor);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView cf_call() {
        Arg arg = getCallArgument(Arg.class);
        return new ModelAndView(VIEW, S_FORM, new Form(arg));
    }

    /** Save the edited Dublin Core. */
    @RequestMapping(value = "save", method = RequestMethod.POST)
    public Object save(@ModelAttribute(S_FORM) Form form,
                       BindingResult result, // Needs to follow dcc immediately!
                       SessionStatus status) {
        if (!form.isTemplate()) {
            (form.isEpisode() ? dcEpisodeValidator : dcSeriesValidator).
                    validate(form.getDc().getDublinCore(), result, "dc.dublinCore");
        }
        if (result.hasErrors()) {
            // Create model
            return new ModelAndView(VIEW, S_FORM, form);
        } else {
            // OK. finish conversation
            status.setComplete();
            form.getDc().sync();
            return back(form.getDc());
        }
    }

    /** Cancel editing. */
    @RequestMapping(value = "cancel", method = RequestMethod.GET)
    public String cancel(SessionStatus status) {
        status.setComplete();
        return back();
    }

    // --

    /** Call argument object. */
    public static class Arg {

        ETHZDublinCoreCommand dc;
        String formTitle;
        boolean template;
        boolean episode;

        public Arg(ETHZDublinCoreCommand dc, String formTitle, boolean episode, boolean template) {
            this.dc = dc;
            this.formTitle = formTitle;
            this.template = template;
            this.episode = episode;
        }
    }

    public static class Form {

        private Arg arg;

        public Form(Arg arg) {
            this.arg = arg;
        }

        public ETHZDublinCoreCommand getDc() {
            return arg.dc;
        }

        /** Get the form title. */
        public String getTitle() {
            return arg.formTitle;
        }

        /** Either the Dublin Core is a template or it is concrete. */
        public boolean isConcrete() {
            return !arg.template;
        }

        /** Either the Dublin Core is a template or it is concrete. */
        public boolean isTemplate() {
            return arg.template;
        }

        /** Either the Dublin Core describes an episode or a series. */
        public boolean isEpisode() {
            return arg.episode;
        }

        /** Either the Dublin Core describes an episode or a series. */
        public boolean isSeries() {
            return !arg.episode;
        }
    }
}