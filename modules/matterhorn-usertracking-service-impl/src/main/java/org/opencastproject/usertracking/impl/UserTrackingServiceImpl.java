/**
 *  Copyright 2009, 2010 The Regents of the University of California
 *  Licensed under the Educational Community License, Version 2.0
 *  (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *  http://www.osedu.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */
package org.opencastproject.usertracking.impl;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.spi.PersistenceProvider;

import org.opencastproject.usertracking.api.UserTrackingService;
import org.opencastproject.usertracking.api.Footprint;
import org.opencastproject.usertracking.api.FootprintList;
import org.opencastproject.usertracking.api.Report;
import org.opencastproject.usertracking.api.ReportItem;
import org.opencastproject.search.api.SearchService;
import org.opencastproject.usertracking.endpoint.FootprintImpl;
import org.opencastproject.usertracking.endpoint.FootprintsListImpl;
import org.opencastproject.usertracking.endpoint.ReportImpl;
import org.opencastproject.usertracking.endpoint.ReportItemImpl;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of org.opencastproject.usertracking.api.UserTrackingService
 * 
 * @see org.opencastproject.usertracking.api.UserTrackingService
 */
public class UserTrackingServiceImpl implements UserTrackingService {

  public static final String FOOTPRINT_KEY = "FOOTPRINT";

  private static final Logger logger = LoggerFactory.getLogger(UserTrackingServiceImpl.class);

  private SearchService searchService;

  /**
   * @param persistenceProvider
   *          the persistenceProvider to set
   */
  public void setPersistenceProvider(PersistenceProvider persistenceProvider) {
    this.persistenceProvider = persistenceProvider;
  }

  @SuppressWarnings("unchecked")
  protected Map persistenceProperties;

  /**
   * @param persistenceProperties
   *          the persistenceProperties to set
   */
  @SuppressWarnings("unchecked")
  public void setPersistenceProperties(Map persistenceProperties) {
    this.persistenceProperties = persistenceProperties;
  }

  /** The factory used to generate the entity manager */
  protected EntityManagerFactory emf = null;

  /**
   * The JPA provider
   */
  protected PersistenceProvider persistenceProvider;

  /**
   * This method will be called, when the bundle gets unloaded from OSGI
   */
  public void deactivate() {
  }

  /**
   * This method will be called, when the bundle gets loaded from OSGI
   * 
   * @param componentContext
   *          The ComponetnContext of the OSGI bundle
   */
  public void activate(ComponentContext componentContext) {
    logger.debug("activation started.");
    emf = persistenceProvider.createEntityManagerFactory("org.opencastproject.usertracking", persistenceProperties);
  }

  public void destroy() {
    emf.close();
  }

  public int getViews(String mediapackageId) {
    EntityManager em = null;
    try {
      em = emf.createEntityManager();
      Query q = em.createNamedQuery("countSessionsOfMediapackage");
      q.setParameter("mediapackageId", mediapackageId);
      return ((Long) q.getSingleResult()).intValue();
    } finally {
      em.close();
    }
  }
  
  private int getDistinctEpisodeIdTotal(Calendar calBegin, Calendar calEnd, EntityManager em) {
    Query q = em.createNamedQuery("findDistinctEpisodeIdTotalByIntervall");
    q.setParameter("begin", calBegin, TemporalType.TIMESTAMP);
    q.setParameter("end", calEnd, TemporalType.TIMESTAMP);
    return ((Long) q.getSingleResult()).intValue();
  }

  public Report getReport(int offset, int limit, EntityManager em) {
    Report report = new ReportImpl();
    report.setLimit(limit);
    report.setOffset(offset);

    Query q = em.createNamedQuery("countSessionsGroupByMediapackage");
    q.setFirstResult(offset);
    q.setMaxResults(limit);

    @SuppressWarnings("unchecked")
    List<Object[]> result = q.getResultList();
    ReportItem item;

    for (Object[] a : result) {
      item = new ReportItemImpl();
      item.setEpisodeId((String) a[0]);
      item.setViews((Long) a[1]);
      item.setPlayed((Long) a[2]);
      report.add(item);
    }

    return report;
  }

  public Report getReport(String from, String to, int offset, int limit, EntityManager em) {
    Report report = new ReportImpl();
    report.setLimit(limit);
    report.setOffset(offset);

    int year = Integer.parseInt(from.substring(0, 4));
    int month = Integer.parseInt(from.substring(4, 6)) - 1;
    int date = Integer.parseInt(from.substring(6, 8));
    Calendar calBegin = new GregorianCalendar();
    calBegin.set(year, month, date, 0, 0);

    year = Integer.parseInt(to.substring(0, 4));
    month = Integer.parseInt(to.substring(4, 6)) - 1;
    date = Integer.parseInt(to.substring(6, 8));
    Calendar calEnd = new GregorianCalendar();
    calEnd.set(year, month, date, 23, 59);

    report.setTotal(getDistinctEpisodeIdTotal(calBegin, calEnd, em));
    Query q = em.createNamedQuery("countSessionsGroupByMediapackageByIntervall");
    q.setParameter("begin", calBegin, TemporalType.TIMESTAMP);
    q.setParameter("end", calEnd, TemporalType.TIMESTAMP);
    q.setFirstResult(offset);
    q.setMaxResults(limit);

    @SuppressWarnings("unchecked")
    List<Object[]> result = q.getResultList();
    ReportItem item;

    for (Object[] a : result) {
      item = new ReportItemImpl();
      item.setEpisodeId((String) a[0]);
      item.setViews((Long) a[1]);
      item.setPlayed((Long) a[2]);
      report.add(item);
    }

    return report;
  }

  public FootprintList getFootprints(String mediapackageId, String userId) {
    FootprintList result = new FootprintsListImpl();
    EntityManager em = emf.createEntityManager();
    Query q = em.createNamedQuery("findAnnotationsByKeyAndMediapackageIdOrderByOutpointDESC");
    q.setParameter("key", FOOTPRINT_KEY);
    q.setParameter("mediapackageId", mediapackageId);
    Collection<FootprintImpl> annotations = q.getResultList();

    int[] resultArray = new int[1];
    boolean first = true;

    for (FootprintImpl a : annotations) {
      if (first) {
        // Get one more item than the known outpoint to append a footprint of 0 views at the end of the result set
        resultArray = new int[a.getOutpoint() + 1];
        first = false;
      }
      for (int i = a.getInpoint(); i < a.getOutpoint(); i++) {
        resultArray[i]++;
      }
    }

    FootprintList list = new FootprintsListImpl();
    int current, last = -1;
    int lastPositionAdded = -1;
    for (int i = 0; i < resultArray.length; i++) {
      current = resultArray[i];
      if (last != current) {
        Footprint footprint = new FootprintImpl();
        footprint.setPosition(i);
        footprint.setViews(current);
        list.add(footprint);
        lastPositionAdded = i;
      }
      last = current;
    }

    return list;
  }

  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }
}
