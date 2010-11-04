/*

 DublinCoreController.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 29, 2008

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
import ch.ethz.replay.ui.common.web.dublincore.DublinCoreCommand;
import ch.ethz.replay.ui.common.web.dublincore.DublinCoreCommandValidator;
import ch.ethz.replay.ui.scheduler.Recording;
import ch.ethz.replay.ui.scheduler.impl.util.RecordingValidator;
import ch.ethz.replay.ui.scheduler.web.extension.RecordingExtensionImpl;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the Dublin Core form.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Controller
@SessionAttributes("dublincore")
public class DublinCoreController extends AbstractDomainController {

    public static final String VIEW = "dublincore";

    private String defaultLanguage;
    private String[] supportedLanguages;

    @Autowired(required = true)
    private RecordingValidator recVal;

    @Resource
    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    /**
     * Set the supported languages as a list of 2-letter ISO codes.
     */
    @Resource
    public void setSupportedLanguages(List<String> languages) {
        supportedLanguages = languages.toArray(new String[languages.size()]);
    }

    // Inter controller communcication

    /**
     * Argument: DublinCore
     */
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView cf_call() {
        CallArg arg = getCallArgument(CallArg.class);
        DublinCoreCommand dcc = arg.isUseDefaultLanguage()
                ? new DublinCoreCommand(defaultLanguage, arg.getDublinCore())
                : new DublinCoreCommand(arg.getDublinCore());
        return populate(dcc, null, true);
    }

    //

    /**
     * Setup the form.
     *
     * @param id the id of the recording whose Dublin Core shall be displayed
     */
    @RequestMapping(method = RequestMethod.GET, params = "id")
    public ModelAndView view(@RequestParam("id") Long id,
                             @RequestParam(value = "edit", required = false) String edit) {
        // get Dublin Core attachment
        Recording rec = schedule.getRecording(id);
        DublinCoreCatalog dc = rec.getDublinCore();
        DublinCoreCommand dcc = dc != null
                ? new DublinCoreCommand(defaultLanguage, dc)
                : new DublinCoreCommand(defaultLanguage);
        // create model
        return populate(dcc, rec, dc == null || Boolean.parseBoolean(edit));
    }

    /**
     * Add a new language set to the DublinCore.
     *
     * @param language iso code of the language to add
     */
    @RequestMapping(value = "addlang", method = RequestMethod.POST)
    public ModelAndView addLanguage(@RequestParam("lang") String language,
                                    @ModelAttribute("dublincore") DublinCoreCommand dcc,
                                    BindingResult result) {
        dcc.addLanguageSet(language);
        return populate(dcc, null, true);
    }

    /**
     * Remove a language set.
     *
     * @param language iso code of the language to remove
     */
    @RequestMapping(value = "removelang", method = RequestMethod.POST)
    public ModelAndView removeLanguage(@RequestParam("lang") String language,
                                       @ModelAttribute("dublincore") DublinCoreCommand dcc,
                                       BindingResult result) {
        dcc.removeLanguageSet(language);
        return populate(dcc, null, true);
    }

    /**
     * Save entered form data.
     */
    @RequestMapping(value = "save", method = RequestMethod.POST)
    public Object save(@ModelAttribute("dublincore") DublinCoreCommand dcc,
                       BindingResult result,
                       SessionStatus status) {
        DublinCoreCommandValidator.INSTANCE.validate(dcc, result);
        if (result.hasErrors()) {
            // create model
            return populate(dcc, null, true);
        } else {
            // ok. finish conversation
            status.setComplete();
            dcc.sync();
            return back(dcc.getDublinCore());
        }
    }

    @RequestMapping(value = "cancel", method = RequestMethod.GET)
    public String cancel(SessionStatus status) {
        status.setComplete();
        return back();
    }

    //

    /**
     * Populates the model with the DublinCoreCommand, the supported languages and some editing information.
     *
     * @param recording may be null, needed to verify if the Dublin Core can be edited
     * @param shallEdit shall the Dublin Core be edited if possible?
     */
    private ModelAndView populate(DublinCoreCommand dcc, Recording recording, boolean shallEdit) {
        ModelAndView model = new ModelAndView(VIEW);
        if (dcc != null) model.addObject("dublincore", dcc);
        //
        boolean editable = true;
        if (recording != null) {
            editable = new RecordingExtensionImpl(recording, recVal).isEditable();
        }
        model.addObject("edit", shallEdit && editable);
        model.addObject("editable", editable);
        // populate languages
        List languages = new ArrayList();
        for (String lang : supportedLanguages) {
            if (dcc == null || !dcc.hasLanguage(lang)) languages.add(lang);
        }
        model.addObject("languages", languages);
        return model;
    }

    /**
     * Argument object for calling DublinCoreController.
     */
    public static class CallArg {
        private DublinCore dublinCore;
        private boolean useDefaultLanguage = false;

        public CallArg(DublinCore dublinCore, boolean useDefaultLanguage) {
            this.dublinCore = dublinCore;
            this.useDefaultLanguage = useDefaultLanguage;
        }

        /**
         * Do not use a default language.
         */
        public CallArg(DublinCore dublinCore) {
            this.dublinCore = dublinCore;
        }

        public DublinCore getDublinCore() {
            return dublinCore;
        }

        public boolean isUseDefaultLanguage() {
            return useDefaultLanguage;
        }
    }
}