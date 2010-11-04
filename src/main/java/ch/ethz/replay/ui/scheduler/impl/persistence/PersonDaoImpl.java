/*

 PersonDaoImpl.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 19, 2008

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
import ch.ethz.replay.ui.scheduler.Person;
import ch.ethz.replay.ui.scheduler.impl.PersonImpl;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;

/**
 * <strong>Note:</strong> <code>findBy</code> and <code>findByExample</code> return only instances of
 * {@link ch.ethz.replay.ui.scheduler.impl.PersonImpl}.
 * 
 * 
 */
@Repository
public class PersonDaoImpl extends TransactionalGenericHibernateDao<Person, Long> implements PersonDao {

  /**
   * Supports:
   * <ul>
   * <li>filter: {@link SimplePersonFilter}
   * </ul>
   * <strong>Note:</strong> Only instances of type {@link ch.ethz.replay.ui.scheduler.impl.PersonImpl} will be returned.
   */
  @Transactional(readOnly = true)
  public List<Person> findBy(Object filter) {
    if (filter instanceof SimplePersonFilter) {
      // by filter
      final SimplePersonFilter f = (SimplePersonFilter) filter;
      return (List<Person>) getHibernateTemplate().execute(new HibernateCallback() {

        public Object doInHibernate(Session session) throws HibernateException, SQLException {
          Criteria c = session.createCriteria(PersonImpl.class).addOrder(Order.asc("familyName"))
                  .addOrder(Order.asc("givenName"));
          if (f.getMaxResults() != null)
            c.setMaxResults(f.getMaxResults());
          // todo Hibernate does not support critera queries on components and the email addresses
          // todo are components at the moment, so it is commented out
          // if (f.getEmail() != null)
          // c.createCriteria("emailAddresses").add(Restrictions.eq("address", f.getEmail()));
          if (f.getName() != null)
            c.add(Restrictions.or(Expression.ilike("familyName", f.getName(), MatchMode.ANYWHERE),
                    Expression.ilike("givenName", f.getName(), MatchMode.ANYWHERE)));
          return c.list();
        }
      });
    }
    throw new IllegalArgumentException("Unsupported filter " + filter);
  }

  protected void customizeExampleCriterion(Example example) {
    example.enableLike(MatchMode.ANYWHERE).ignoreCase();
  }
}