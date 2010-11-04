/*

 DetailPersonController.java
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Delivers datail information about a certain person.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Controller
@RequestMapping
public class DetailPersonController extends AbstractDomainController {

    /**
     * @param id the person's id
     * @param ajax called via ajax
     */
    @RequestMapping(method = RequestMethod.GET)
    public String setup(@RequestParam(value = "id")Long id,
                        @RequestParam(value = "ajax", required = false)Boolean ajax,
                        Model model) {
        Person person = id != null
                ? schedule.getPerson(id)
                : schedule.newPerson();
        model.addAttribute("person", person);
        return ajax == null ? "person/detail" : "person/detailAjax";
    }
}