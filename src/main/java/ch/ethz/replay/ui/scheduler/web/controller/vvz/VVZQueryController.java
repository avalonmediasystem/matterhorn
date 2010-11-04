/*
 
 VVZQueryController.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Oct 24, 2008

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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ch.ethz.replay.ui.scheduler.external.ethz.VvzConnector;
import ch.ethz.replay.ui.scheduler.external.ethz.soapvvz.Suchindex;
import ch.ethz.replay.ui.scheduler.external.ethz.soapvvz.Lehrveranstaltung;
import ch.ethz.replay.ui.scheduler.web.controller.AbstractDomainController;
import ch.ethz.replay.ui.scheduler.util.Tuple5;
import ch.ethz.replay.core.common.util.StringSupport;

import java.util.*;

/**
 * Handle the AJAX queries.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Controller
public class VVZQueryController extends AbstractDomainController {

    private VvzConnector vvz;

    @RequestMapping
    public ModelAndView query(@RequestParam(value = "q")String q) {
        return new ModelAndView("vvz/aj_query").addObject("groups", groupByLvID(vvz.query(q)));
    }

    /** (titel, lvID, lvTyp, name, location) */
    private List<MyTyple> groupByLvID(List<Suchindex> xs) {
        Map<Integer, MyTyple> builder =
                new HashMap<Integer, MyTyple>();
        for (Suchindex x : xs) {
            Integer id = x.getLehrveranstaltungId();
            MyTyple t = builder.get(id);
            if (t == null) {
                t = new MyTyple(x.getTitel(), id, x.getLehrveranstaltungTyp(), new HashSet<String>(), new HashSet<String>());
                builder.put(id, t);
            }
            t.get_4().add(x.getCompleteName());
            if (!StringSupport.isEmpty(x.getLocation()))
                t.get_5().add(x.getLocation());
        }
        // Sort list by title
        List<MyTyple> rs = new ArrayList<MyTyple>(builder.values());
        Collections.sort(rs, MyTupComp.OBJ);
        return rs;
    }

    static class MyTupComp implements Comparator<MyTyple> {

        static MyTupComp OBJ = new MyTupComp();

        public int compare(MyTyple o1, MyTyple o2) {
            return o1.get_1().compareTo(o2.get_1());
        }
    }

    /**
     * (titel, lvID, lvTyp, name, location)
     */
    public class MyTyple extends Tuple5<String, Integer, Lehrveranstaltung.Typ, Set<String>, Set<String>> {

        public MyTyple(String _1, Integer _2, Lehrveranstaltung.Typ _3, Set<String> _4, Set<String> _5) {
            super(_1, _2, _3, _4, _5);
        }
    }

    // --

    public void setVvzConnector(VvzConnector vvzConnector) {
        this.vvz = vvzConnector;
    }
}
