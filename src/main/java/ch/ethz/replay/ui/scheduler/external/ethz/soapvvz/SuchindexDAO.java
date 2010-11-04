/*
 
 SuchindexDAO.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 09 15, 2009

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

package ch.ethz.replay.ui.scheduler.external.ethz.soapvvz;

import static ch.ethz.replay.core.common.util.Tool.cast;
import ch.ethz.replay.ui.common.util.dao.TransactionalGenericHibernateDao;
import org.springframework.util.StringUtils;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.hibernate.Session;
import org.hibernate.HibernateException;
import org.hibernate.Criteria;
import org.hibernate.criterion.*;
import static org.hibernate.criterion.Restrictions.ilike;
import static org.hibernate.criterion.Restrictions.*;

import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

/**
 * DAO to access {@link Suchindex}.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class SuchindexDAO extends TransactionalGenericHibernateDao<Suchindex, Integer> {

    private boolean onlyLectures = true;

    /**
     * Searches the Suchindex by the summary column. Each of the query terms is joined by a boolean or operation.
     * The resulting list also contains Suchindex's with the same LV_ID as the found one.
     */
    public List<Suchindex> findBySummary(final String query) {
        return cast(getHibernateTemplate().executeFind(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                // Criteria query that is equivalent to:
                // select * from suchindex s
                // where s.lv_id in (select x.lv_id from suchindex x where summary like '%Kossmann%');
                DetachedCriteria sq = DetachedCriteria.forClass(getEntityClass(), "si");
                for (String qp : query.split("\\s+")) {
                    sq.add(like("summary", qp.toLowerCase(), MatchMode.ANYWHERE));
                }
                if (onlyLectures)
                    sq.add(or(eq("lvTyp", Lehrveranstaltung.Typ.Vorlesung.getCode()),
                            eq("lvTyp", Lehrveranstaltung.Typ.VorlesungMitUebung.getCode())));
                sq.setProjection(Property.forName("si.lvId"));

                Criteria c = session.createCriteria(getEntityClass(), "so")
                        .add(Subqueries.propertyIn("so.lvId", sq))
                        .addOrder(Order.asc("so.titel"))
                        .addOrder(Order.asc("so.name"))
                        .addOrder(Order.asc("so.vorname"))
                        .addOrder(Order.asc("so.gebaeude"));
                return c.list();
            }
        }));
    }

    // --

    /**
     * Decide if only lectures (i.e. Lehrveranstaltung is of type Vorlesung) shall be searched.
     * Defaults to true.
     */
    public void setOnlyLectures(boolean onlyLectures) {
        this.onlyLectures = onlyLectures;
    }

    public boolean isOnlyLectures() {
        return onlyLectures;
    }
}
