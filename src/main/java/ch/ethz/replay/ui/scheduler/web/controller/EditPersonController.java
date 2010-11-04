/*

 EditPersonController.java
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

package ch.ethz.replay.ui.scheduler.web.controller;

import ch.ethz.replay.ui.scheduler.Person;
import ch.ethz.replay.ui.scheduler.impl.PersonImpl;
import ch.ethz.replay.ui.scheduler.impl.util.PersonValidator;
import ch.ethz.replay.ui.scheduler.web.propertyeditors.EmailAddressEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Responsible for editing a person's vCard.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Controller
@RequestMapping
@SessionAttributes("person")
public class EditPersonController extends AbstractDomainController {

    public static final String VIEW = "person/edit";
    public static final String VIEW_AJAX = "person/editAjax";

    @Override
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(List.class, "emailAddresses", new EmailAddressEditor());
    }

    // Inter controller communication

    @RequestMapping(value = "call", method = RequestMethod.GET)
    public ModelAndView cf_call() {
        Long id = getCallArgument(Long.class);
        Person person = id != null
                ? schedule.getPerson(id)
                : schedule.newPerson();
        return new ModelAndView(VIEW, "person", person);
    }

    //

    /**
     * Sets up the form.
     *
     * @param id the person's id
     */
    @RequestMapping(method = RequestMethod.GET)
    public String setup(@RequestParam(value = "id", required = false)Long id,
                        @RequestParam(value = "ajax", required = false)Boolean ajax,
                        Model model) {
        Person person = id != null
                ? schedule.getPerson(id)
                : schedule.newPerson();
        model.addAttribute("person", person);
        return ajax == null ? VIEW : VIEW_AJAX;
    }

    /**
     * Process form submission.
     */
    @RequestMapping(method = RequestMethod.POST)
    public String submit(@ModelAttribute("person")PersonImpl person,
                         BindingResult result,
                         SessionStatus status) {
        new PersonValidator().validate(person, result);
        if (result.hasErrors()) {
            return VIEW;
        }
        // everything's fine

        schedule.savePerson(person);
        status.setComplete();

        // dispatch
        if (isCalled()) {
            return back(person);
        } else {
            return redirectTo(ListPersonsController.class, null);
        }
    }
}