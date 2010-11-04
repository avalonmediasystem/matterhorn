/*
 
 SimpleLocationDaoImpl.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Aug 26, 2008

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

package ch.ethz.replay.ui.scheduler.impl.persistence;

import ch.ethz.replay.ui.common.util.dao.TransactionalGenericHibernateDao;
import ch.ethz.replay.ui.scheduler.Location;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;

import java.sql.SQLException;
import java.util.List;

/**
 * Provides access to {@link ch.ethz.replay.ui.scheduler.impl.SimpleLocation}s.
 * 
 * 
 */
public class SimpleLocationDaoImpl extends TransactionalGenericHibernateDao<Location, Long> implements LocationDao {

  /**
   * Supports {@link SimpleLocationFilter}.
   */
  @Override
  public List<Location> findBy(Object filter) {
    if (filter instanceof SimpleLocationFilter) {
      final SimpleLocationFilter f = (SimpleLocationFilter) filter;
      return getHibernateTemplate().executeFind(new HibernateCallback() {

        public Object doInHibernate(Session session) throws HibernateException, SQLException {
          return session
                  .createCriteria(getEntityClass())
                  .add(Restrictions.ilike("name", f.getName(), f.isExactMatch() ? MatchMode.EXACT : MatchMode.ANYWHERE))
                  .list();
        }
      });
    }
    throw new IllegalArgumentException("Unsupported filter " + filter);
  }
}
