/*

 RecordingDaoImpl.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 10, 2008

 Copyright (c) 2007 ETH Zurich, Switzerland

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
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
import ch.ethz.replay.ui.scheduler.Person;
import ch.ethz.replay.ui.scheduler.Recording;
import ch.ethz.replay.ui.scheduler.RecordingFilter;
import ch.ethz.replay.ui.scheduler.impl.Deletion;
import ch.ethz.replay.ui.scheduler.impl.RecordingImpl;
import ch.ethz.replay.core.common.util.Tool;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Provides access to {@link ch.ethz.replay.ui.scheduler.impl.RecordingImpl}.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class RecordingDaoImpl extends TransactionalGenericHibernateDao<Recording, Long>
        implements RecordingDao {

    @Transactional(readOnly = true)
    public long count() {
        Long result = (Long) getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createQuery("select count(r) from Recording r").uniqueResult();
            }
        });
        return result != null ? result : 0;
    }

    @Transactional(readOnly = true)
    public long countByUpdatedAfter(final String locationName, final Date date) {
        if (date == null) throw new IllegalArgumentException("date is null");
        Integer result = (Integer) getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria c = session.createCriteria(RecordingImpl.class)
                        .setProjection(Projections.rowCount());
                if (locationName != null)
                    c.createCriteria("location").add(Restrictions.eq("name", locationName));
                c.add(Restrictions.gt("lastUpdated", date));
                // curious ... unlike a count(*) query using HQL which returns a Long
                // a criteria query returns an Integer value
                return c.uniqueResult();
            }
        });
        // no changes made on existing recordings, no recordings added
        if (result == 0 && locationName != null) {
            // check if recordings were deleted for the given room
            result = ((Long) getHibernateTemplate().execute(new HibernateCallback() {

                public Object doInHibernate(Session session) throws HibernateException, SQLException {
                    return session.createQuery("select count(*) from Deletion where lastUpdated > :date")
                            .setDate("date", date).uniqueResult();
                }
            })).intValue();
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Date getLastModificationDate(final Long locationId) {
        Date lastUpdated = (Date) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createSQLQuery("select max(u) from ( " +
                        "select max(r.lastupdated) as u from recording r where r.locationid = :lid " +
                        "UNION " +
                        "select max(s.lastupdated) as u from recording r left join recordingseries s on r.recordingseriesid = s.id where r.locationid = :lid "+
                        "UNION " +
                        "select max(d.lastupdated) as u from deletion d where d.locationid = :lid " +
                        ") s").setLong("lid", locationId).uniqueResult();
            }
        });
        return lastUpdated;
    }

    @Transactional
    public void removeContactPersonFromRecordings(final Person person) {
        getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createSQLQuery("delete from Recording_Person r where r.PersonId = :id")
                        .setLong("id", person.getId()).executeUpdate();
            }
        });
    }

    /**
     * Support {@link RecordingFilter}.
     * <p/>
     * {@link RecordingFilter#getFreeText()} is searched in the summary of document and Dublin Core attachments,
     * the family name and given name of contact persons and the recording's title.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Recording> findBy(Object filter) {
        if (filter instanceof RecordingFilter) {
            final RecordingFilter f = (RecordingFilter) filter;
            return Tool.cast(getHibernateTemplate().execute(new HibernateCallback() {

                public Object doInHibernate(Session session) throws HibernateException, SQLException {
                    Criteria c = session.createCriteria(getEntityClass())
                            .addOrder(Order.asc("startDate"))
                            .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
                    if (f.getLocationName() != null)
                        c.createCriteria("location").add(Restrictions.ilike("name", f.getLocationName(), MatchMode.EXACT));
                    if (f.getLocationId() != null)
                        c.createCriteria("location").add(Restrictions.idEq(f.getLocationId()));
                    if (f.getStartingAfter() != null)
                        c.add(Restrictions.gt("startDate", f.getStartingAfter()));
                    if (f.getEndingBefore() != null)
                        c.add(Restrictions.lt("endDate", f.getEndingBefore()));
                    if (f.getEndingAfter() != null)
                        c.add(Restrictions.gt("endDate", f.getEndingAfter()));
                    if (f.getStatus() != null)
                        c.add(Restrictions.eq("status", f.getStatus()));
                    if (f.getSeriesId() != null)
                        c.createAlias("series", "s", CriteriaSpecification.LEFT_JOIN)
                                .add(Restrictions.eq("s.id", f.getSeriesId()));
                    if (f.getJobId() != null)
                        c.add(Restrictions.eq("jobId", f.getJobId()));
                    if (f.getFreeText() != null) {
//                        c.createAlias("attachments", "a", CriteriaSpecification.LEFT_JOIN);
//                        c.createAlias("dublinCoreAttachment", "d", CriteriaSpecification.LEFT_JOIN);
//                        c.createAlias("contactPersons", "p", CriteriaSpecification.LEFT_JOIN);
                        c.createAlias("attachments", "a", CriteriaSpecification.LEFT_JOIN);
                        c.createAlias("dublinCoreAttachment", "d", CriteriaSpecification.LEFT_JOIN);
                        c.createAlias("contactPersons", "p", CriteriaSpecification.LEFT_JOIN);
                        c.add(Restrictions.disjunction()
                                .add(Expression.ilike("title", f.getFreeText(), MatchMode.ANYWHERE))
                                .add(Expression.ilike("a.summary", f.getFreeText(), MatchMode.ANYWHERE))
                                .add(Expression.ilike("d.summary", f.getFreeText(), MatchMode.ANYWHERE))
                                .add(Expression.ilike("p.familyName", f.getFreeText(), MatchMode.ANYWHERE))
                                .add(Expression.ilike("p.givenName", f.getFreeText(), MatchMode.ANYWHERE))
                        );
                    }
                    return c.list();
                }
            }));
        }
        throw new IllegalArgumentException("Unsupported filter " + filter);
    }

    @Transactional(readOnly = true)
    public Recording getByJobId(final String jobId) {
        return (Recording) getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createQuery("from Recording where jobId = :j")
                        .setString("j", jobId).uniqueResult();
            }
        });
    }

    @Override
    @Transactional
    public void delete(Recording recording) {
        _deleteRecording(recording);
    }

    @Transactional
    public void deleteByJobId(final String jobId) {
        Recording rec = getByJobId(jobId);
        if (rec != null) _deleteRecording(rec);
    }

    /**
     * Actual implemention of recording deletion.
     */
    private void _deleteRecording(Recording recording) {
        getHibernateTemplate().delete(recording);
        getHibernateTemplate().saveOrUpdate(new Deletion(recording.getLocation().getId()));
    }

    // for internal purposes only

    @Transactional(readOnly = true)
    public List<Location> getAllScheduledLocations() {
        return getHibernateTemplate().executeFind(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createQuery("select distinct r.location from Recording r " +
                        "order by r.location.name asc").list();
            }
        });
    }

    @Transactional(readOnly = true)
    public List<Location> getAllScheduledLocationsLike(String like) {
        final String likeLower = "%" + like.toLowerCase() + "%";
        return getHibernateTemplate().executeFind(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createQuery("select distinct r.location from Recording r " +
                        "where lower(r.location.name) like :location " +
                        "order by r.location.name asc")
                        .setString("location", likeLower).list();
            }
        });
    }
}
