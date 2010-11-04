/*
 
 AttachmentDao.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 9 11, 2008

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

import ch.ethz.replay.core.api.common.job.JobTicket;
import ch.ethz.replay.ui.common.util.dao.TransactionalGenericHibernateDao;
import ch.ethz.replay.ui.scheduler.Attachment;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;

/**
 * Provides access to all {@link ch.ethz.replay.ui.scheduler.Attachment} implementations of package <code>impl</code>.
 * 
 * 
 */
public class AttachmentDao extends TransactionalGenericHibernateDao<Attachment, Long> {

  /**
   * Supports {@link ch.ethz.replay.core.api.common.job.JobTicket}s.
   */
  @Transactional(readOnly = true)
  public List<Attachment> findBy(final Object filter) {
    if (filter instanceof JobTicket) {
      final JobTicket f = (JobTicket) filter;
      return getHibernateTemplate().executeFind(new HibernateCallback() {

        public Object doInHibernate(Session session) throws HibernateException, SQLException {
          return session.createQuery("from JobTicketAttachment a where a.ticketId = :id")
                  .setString("id", f.getIdentifier()).list();
        }
      });
    }
    throw new IllegalArgumentException(filter + " is not supported");
  }
}
