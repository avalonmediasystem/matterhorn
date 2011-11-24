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

package org.opencastproject.clipshow.impl;

import org.opencastproject.clipshow.endpoint.ClipshowInfo;
import org.opencastproject.clipshow.impl.ClipshowVote.Type;
import org.opencastproject.util.NotFoundException;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.NoPermissionException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.spi.PersistenceProvider;

public final class ClipshowServiceImpl implements ManagedServiceFactory {

  /** Log facility */
  private static final Logger logger = LoggerFactory.getLogger(ClipshowServiceImpl.class);

  /** The name of the persistence unit for this class */
  public static final String PERSISTENCE_UNIT = "org.opencastproject.clipshow.impl.ClipshowServiceImpl";

  /** The JPA provider */
  protected PersistenceProvider persistenceProvider;

  /** The persistence properties */
  @SuppressWarnings("unchecked")
  protected Map persistenceProperties;

  /** The factory used to generate the entity manager */
  protected EntityManagerFactory emf = null;

  public void setPersistenceProvider(PersistenceProvider persistence) {
    this.persistenceProvider = persistence;
  }

  public void setPersistenceProperties(Map properties) {
    this.persistenceProperties = properties;
  }

  public void activate(ComponentContext cc) {
    emf = persistenceProvider.createEntityManagerFactory(PERSISTENCE_UNIT, persistenceProperties);
  }

  public void deactivate() {
    if (emf != null) {
      emf.close();
    }
  }

  /**
   * Creates a clipshow and adds it to the database for a given mediapackage.
   * 
   * @param clips
   *          The clips comprising the clipshow.
   * @param mediapackageId
   *          The mediapackage's ID.
   * @param userId
   *          The user's ID.
   * @throws IllegalArgumentException
   */
  public void createClipshow(String name, List<Clip> clips, String mediapackageId, String userId) {
    if (StringUtils.isBlank(name) || clips == null || clips.size() == 0 
        || StringUtils.isBlank(mediapackageId) || StringUtils.isBlank(userId)) {
      throw new IllegalArgumentException("Bad Parameters, unable to add clipshow.");
    }

    //TODO:  Verify that the user has permissions to the mediapackage...
    EntityManager em = emf.createEntityManager();
    ClipshowUser author = getUserFromDB(userId, em);
    em.getTransaction().begin();
    Clipshow clipshow = new Clipshow(name, author, mediapackageId, clips);
    em.persist(clipshow);
    em.getTransaction().commit();
    em.close();
  }

  /**
   * Retrieves a clipshow if the user is in the allowed user list
   * @param clipshowId The id of the clipshow
   * @param userId The user's ID
   * @return The clipshow
   * @throws IllegalArgumentException
   * @throws NotFoundException 
   * @throws NoPermissionException 
   */
  public Clipshow getClipshow(String clipshowId, String userId) throws NoPermissionException, NotFoundException {
    if (StringUtils.isBlank(clipshowId) || StringUtils.isBlank(userId)) {
      throw new IllegalArgumentException("Bad Parameters, unable to get clipshow.");
    }

    EntityManager em = emf.createEntityManager();
    Clipshow show = getClipshowFromDB(clipshowId, em);
    ClipshowUser user = getUserFromDB(userId, em);
    if (show.userAllowed(user)) {
      return show;
    } else {
      //TODO: Change this exception type?  And a better error message
  	  throw new NoPermissionException("TODO: Change this exception type?  And a better error message");
    }
  }

  /**
   * Removes a clipshow from the database.
   * 
   * @param clipshowId
   *          The ID of the clipshow to remove.
   * @param userId
   *          The user's ID.
   * @throws IllegalArgumentException
   * @throws NotFoundException 
   * @throws NoPermissionException 
   */
  public void deleteClipshow(String clipshowId, String userId) throws NotFoundException, NoPermissionException {
    if (StringUtils.isBlank(clipshowId) || StringUtils.isBlank(userId)) {
      throw new IllegalArgumentException("Bad Parameters, unable to get clipshow.");
    }

    EntityManager em = emf.createEntityManager();
    Clipshow show = getClipshowFromDB(clipshowId, em);
    ClipshowUser user = getUserFromDB(userId, em);
    if (show.getAuthor().equals(user)) {
      em.getTransaction().begin();
      for (ClipshowSeries s : show.getSeries()) {
        s.removeClipshow(show);
      }
      user.removeAuthoredClipshow(show);
      user.removeMemberOfClipshow(show);
      em.remove(show);
      em.getTransaction().commit();
      em.close();
    } else {
      //TODO: Change this exception type?  And a better error message
      throw new NoPermissionException("TODO: Change this exception type?  And a better error message");
    }
  }

  /**
   * Returns the list of clipshows for a given mediapackage ID. This method should only return those clipshows for which
   * the user has permissions
   * 
   * @param mediapackageId
   *          The ID of the mediapackage.
   * @param userId The user's ID
   * @return A List of clipshows.
   * @throws IllegalArgumentException
   */
  public List<ClipshowInfo> listClipshows(String mediapackageId, String userId) {
    if (StringUtils.isBlank(mediapackageId) || StringUtils.isBlank(userId)) {
      throw new IllegalArgumentException("Bad parameters, unable to list clipshows");
    }
    LinkedList<ClipshowInfo> list = new LinkedList<ClipshowInfo>();

    EntityManager em = emf.createEntityManager();
    ClipshowUser user = getUserFromDB(userId, em);
    Query q = em.createNamedQuery("clipshow.mediapackage");
    q.setParameter("mpid", mediapackageId);
    List<Clipshow> results = (List<Clipshow>) q.getResultList();
    for (Clipshow c : results) {
      if (c.userAllowed(user)) {
        ClipshowInfo info = new ClipshowInfo();
        info.setId(c.getId());
        info.setAuthor(c.getAuthor().getDisplayName());
        info.setTitle(c.getTitle());
        Map<ClipshowVote.Type, Integer> votes = countVotes(c.getVoters());
        info.setFunny(votes.get(ClipshowVote.Type.FUNNY));
        info.setGood(votes.get(ClipshowVote.Type.GOOD));
        info.setDislike(votes.get(ClipshowVote.Type.DISLIKE));
        list.add(info);
      }
    }
    return list;
  }

  public void addUserToClipshow(String newUserId, String clipshowId, String userId) throws NotFoundException, NoPermissionException {
    if (StringUtils.isBlank(newUserId) || StringUtils.isBlank(clipshowId) || StringUtils.isBlank(userId)) {
      throw new IllegalArgumentException("Bad parameters, unable to add user to clipshow.");
    }

    EntityManager em = emf.createEntityManager();
    Clipshow clipshow = getClipshowFromDB(clipshowId, em);
    ClipshowUser user = getUserFromDB(userId, em);
    ClipshowUser newUser = getUserFromDB(newUserId, em);
    if (clipshow.getAuthor().equals(user)) {
      em.getTransaction().begin();
      clipshow.addAllowedUser(newUser);
      em.getTransaction().commit();

    } else {
      throw new NoPermissionException("Clipshow user cannot be added, userId parameter must be clipshow author's id");
    }
  }

  /**
   * Adds a clipshow series to the database.
   * 
   * @param clipshowSeriesName
   *          The name of the clipshow series.
   * @param userId
   *          The user's ID.
   */
  public void createSeries(String clipshowSeriesName, String userId) {
    if (StringUtils.isBlank(clipshowSeriesName) || StringUtils.isBlank(userId)) {
      throw new IllegalArgumentException("Bad Parameters, unable to create series.");
    }

    EntityManager em = emf.createEntityManager();
    ClipshowUser author = getUserFromDB(userId, em);
    em.getTransaction().begin();
    ClipshowSeries series = new ClipshowSeries(clipshowSeriesName, author);
    em.persist(series);
    em.getTransaction().commit();
    em.close();
  }

  /**
   * Retrieves a clipshow series from the database
   * @param clipshowSeriesId The id of the series to fetch
   * @param userId The user's id.
   * @return The series
   * @throws IllegalArgumentException
   * @throws NotFoundException 
   * @throws NoPermissionException 
   */
  public ClipshowSeries getSeries(String clipshowSeriesId, String userId) throws NoPermissionException, NotFoundException {
    if (StringUtils.isBlank(clipshowSeriesId) || StringUtils.isBlank(userId)) {
      throw new IllegalArgumentException("Bad Parameters, unable to get series.");
    }

    EntityManager em = emf.createEntityManager();
    ClipshowUser user = getUserFromDB(userId, em);
    ClipshowSeries series = getSeriesFromDB(clipshowSeriesId, em);
    if (series.userAllowed(user)) {
      return series;
    } else {
      //TODO: Change this exception type?  And a better error message
      throw new NoPermissionException("TODO: Change this exception type?  And a better error message");
    }
  }

  /**
   * Removes a clipshow series from the database
   * @param seriesId The id of the series to remove
   * @param userId The user's id.
   * 
   * @throws IllegalArgumentException
   * @throws NotFoundException 
   * @throws NoPermissionException 
   */
  public void deleteSeries(String seriesId, String userId) throws NotFoundException, NoPermissionException {
    if (StringUtils.isBlank(seriesId) || StringUtils.isBlank(userId)) {
      throw new IllegalArgumentException("Bad Parameters, unable to delete clipshow series");
    }

    EntityManager em = emf.createEntityManager();
    em.getTransaction().begin();
    ClipshowUser user = getUserFromDB(userId, em);
    ClipshowSeries series = getSeriesFromDB(seriesId, em);
    if (series.getAuthor().equals(user)) {
      for (Clipshow c : series.getClipshows()) {
        series.removeClipshow(c);
        em.remove(series);
        em.getTransaction().commit();
      }
    } else {
      //TODO: Change this exception type?  And a better error message
      throw new NoPermissionException("TODO: Change this exception type?  And a better error message");
    }
  }

  /**
   * Returns the list of clipshow series in the system.
   * @param userId The user's ID
   * @return A HashMap of <Id, <SeriesName, AuthorName>>
   */
  public List<ClipshowInfo> listSeries(String userId) {
    LinkedList<ClipshowInfo> list = new LinkedList<ClipshowInfo>();
    EntityManager em = emf.createEntityManager();
    ClipshowUser user = getUserFromDB(userId, em);
    List<ClipshowSeries> entries = (List<ClipshowSeries>) em.createNamedQuery("clipshow-series.all").getResultList();
    for (ClipshowSeries s : entries) {
      if (s.userAllowed(user)) {
        ClipshowInfo info = new ClipshowInfo();
        info.setId(s.getId());
        info.setAuthor(s.getAuthor().getDisplayName());
        info.setTitle(s.getTitle());
        //TODO:  Fill in the votes here
        list.add(info);
      }
    }
    em.close();
    return list;
  }

  /**
   * Adds a clipshow to a series.
   * 
   * @param clipshowId
   *          The clipshow's ID.
   * @param seriesId
   *          The series' ID.
   * @param userId
   *          The user's ID.
   * @throws IllegalArgumentException
   * @throws NotFoundException 
   * @throws NoPermissionException 
   */
  public void addClipshowToSeries(String clipshowId, String seriesId, String userId) throws NoPermissionException, NotFoundException {
    if (StringUtils.isBlank(clipshowId) || StringUtils.isBlank(seriesId) || StringUtils.isBlank(userId)) {
      throw new IllegalArgumentException("Bad Parameters, unable to add clipshow to series.");
    }

    EntityManager em = emf.createEntityManager();
    ClipshowUser user = getUserFromDB(userId, em);
    em.getTransaction().begin();
    ClipshowSeries series = getSeriesFromDB(seriesId, em);
    Clipshow clipshow = getClipshowFromDB(clipshowId, em);
    if (series.getAuthor().equals(user) && clipshow.userAllowed(user)) {
      series.addClipshow(clipshow);
      em.persist(series);
      em.persist(clipshow);
      em.getTransaction().commit();
      em.close();
    } else {
      //TODO: Change this exception type?  And a better error message
      throw new NoPermissionException("TODO: Change this exception type?  And a better error message");
    }
  }

  /**
   * Removes a clipshow from a series. This method removes the clipshow series itself if it is removing the last
   * clipshow from the series.
   * 
   * @param clipshowId
   *          The clipshow's ID.
   * @param seriesId
   *          The series' ID.
   * @param userId
   *          The user's ID.
   * @throws IllegalArgumentException
   * @throws NotFoundException 
   * @throws NoPermissionException 
   */
  public void removeClipshowFromSeries(String clipshowId, String seriesId, String userId) throws NoPermissionException, NotFoundException {
    if (StringUtils.isBlank(clipshowId) || StringUtils.isBlank(seriesId) || StringUtils.isBlank(userId)) {
      throw new IllegalArgumentException("Bad Parameters, unable to add clipshow to series.");
    }

    EntityManager em = emf.createEntityManager();
    ClipshowUser user = getUserFromDB(userId, em);
    em.getTransaction().begin();
    ClipshowSeries series = getSeriesFromDB(seriesId, em);
    Clipshow clipshow = getClipshowFromDB(clipshowId, em);
    if (series.getAuthor().equals(user) && clipshow.userAllowed(user)) {
      series.removeClipshow(clipshow);
      em.persist(series);
      em.persist(clipshow);
      em.getTransaction().commit();
      em.close();
    } else {
      //TODO: Change this exception type?  And a better error message
      throw new NoPermissionException("TODO: Change this exception type?  And a better error message");
    }
  }

  /**
   * Adds a vote for a clipshow for a given user.
   * 
   * @param userId
   *          The user's ID.
   * @param type
   *          A comma separated list of types to vote for.  See {@link ClipshowVote.Type} for more details.
   * @param clipshowId
   *          The clipshow's ID.
   * @throws IllegalArgumentException
   * @throws NotFoundException 
   * @throws NoPermissionException  
   */
  public void voteForClipshow(String userId, String types, String clipshowId) throws NoPermissionException, NotFoundException {
    if (StringUtils.isBlank(userId) || StringUtils.isBlank(clipshowId)) {
      throw new IllegalArgumentException("Bad parameters, unable to vote for clipshow.");
    }

    EntityManager em = emf.createEntityManager();
    ClipshowUser user = getUserFromDB(userId, em);
    Clipshow clipshow = getClipshowFromDB(clipshowId, em);
    String[] typeArray = types.split(",");
    //TODO:  What is the type is blank?
    if (clipshow.userAllowed(user)) {
      Query q = em.createNamedQuery("vote");
      q.setParameter("user", user);
      q.setParameter("clipshow", clipshow);
      ClipshowVote v = null;
      em.getTransaction().begin();
      try {
        v = (ClipshowVote) q.getSingleResult();
      } catch (NoResultException e) {
        v = new ClipshowVote(user, clipshow);
      }

      v.setGood(false);
      v.setFunny(false);
      v.setDislike(false);

      for (String type : typeArray) {
        if (Type.GOOD.toString().equals(type)) {
          v.setGood(true);
        } else if (Type.FUNNY.toString().equals(type)) {
          v.setFunny(true);
        } else if (Type.DISLIKE.toString().equals(type)) {
          v.setDislike(true);
          break;  //If it's a dislike, break since that overrides everything else
        } else if (Type.NEUTRAL.toString().equals(type)) {
          v.setGood(false);
          v.setFunny(false);
          v.setDislike(false);
          break;
        }
      }
      clipshow.addVoter(v);
      em.persist(v);
      em.getTransaction().commit();
      em.close();
    } else {
      //TODO: Change this exception type?  And a better error message
      throw new NoPermissionException("TODO: Change this exception type?  And a better error message");
    }
  }

  /**
   * Returns the number of votes for a given clipshow
   * 
   * @param clipshowId The ID of the clipshow
   * @return A map of vote type to type count.
   * @throws IllegalArgumentException
   * @throws NotFoundException  
   */
  public Map<ClipshowVote.Type, Integer> getVotesForClipshow(String clipshowId) throws NotFoundException {
    if (StringUtils.isBlank(clipshowId)) {
      throw new IllegalArgumentException("Bad parameters, unable to get clipshow vote count");
    }

    EntityManager em = emf.createEntityManager();
    Clipshow clipshow = getClipshowFromDB(clipshowId, em);
    
    return countVotes(clipshow.getVoters());
  }

  public Map<ClipshowVote.Type, Integer> getVotesForClipshow(String clipshowId, String userId) throws NotFoundException {
    if (StringUtils.isBlank(clipshowId) || StringUtils.isBlank(userId)) {
      throw new IllegalArgumentException("Bad parameters, unable to get clipshow vote counts");
    }

    EntityManager em = emf.createEntityManager();
    Clipshow clipshow = getClipshowFromDB(clipshowId, em);
    ClipshowUser user = getUserFromDB(userId, em);
    Query q = em.createNamedQuery("vote");
    q.setParameter("clipshow", clipshow);
    q.setParameter("user", user);
    return countVotes(q.getResultList());
  }

  private Map<ClipshowVote.Type, Integer> countVotes(Collection<ClipshowVote> votes) {
    HashMap<ClipshowVote.Type, Integer> voteCounts = new HashMap<ClipshowVote.Type, Integer>();
    voteCounts.put(ClipshowVote.Type.FUNNY, 0);
    voteCounts.put(ClipshowVote.Type.GOOD, 0);
    voteCounts.put(ClipshowVote.Type.DISLIKE, 0);
    for (ClipshowVote v : votes) {
      if (v.getDislike()) {
        voteCounts.put(ClipshowVote.Type.DISLIKE, voteCounts.get(ClipshowVote.Type.DISLIKE) + 1);
        continue;
      }
      if (v.getfunny()) {
        voteCounts.put(ClipshowVote.Type.FUNNY, voteCounts.get(ClipshowVote.Type.FUNNY) + 1);
      }
      if (v.getGood()) {
        voteCounts.put(ClipshowVote.Type.GOOD, voteCounts.get(ClipshowVote.Type.GOOD) + 1);
      }
    }
    return voteCounts;
  }

  
  /**
   * Returns the current user's display name
   * @param userId The user's id
   * @return The user's display name
   */
  public String getUserDisplayName(String userId) {
    if (StringUtils.isBlank(userId)) {
      throw new IllegalArgumentException("Bad parameter, unable to get username");
    }

    EntityManager em = emf.createEntityManager();
    ClipshowUser u = getUserFromDB(userId, em);
    em.close();
    return u.getDisplayName();
  }

  /**
   * Changes a user's display name
   * @param userId The user's id
   * @param newName The new display name
   */
  public void changeUsername(String userId, String newName) {
    if (StringUtils.isBlank(userId) || StringUtils.isBlank(newName)) {
      throw new IllegalArgumentException("Bad parameters, unable to change username");
    }

    //TODO:  This needs to be made unique
    EntityManager em = emf.createEntityManager();
    ClipshowUser user = getUserFromDB(userId, em);
    em.getTransaction().begin();
    user.setDisplayName(newName);
    em.getTransaction().commit();
    em.close();
  }

  private ClipshowUser getUserFromDB(String userId, EntityManager em) {
  	ClipshowUser user = null;
  	try {
        user = (ClipshowUser) em.createNamedQuery("user").setParameter("username", userId).getSingleResult();
        logger.debug("User " + userId + " was found in the database");
  	} catch (NoResultException e) {
        logger.debug("User " + userId + " was not found in the database");
        boolean commit = false;
        if (!em.getTransaction().isActive()) {
          em.getTransaction().begin();
          commit = true;
        }
        user = new ClipshowUser(userId);
        em.persist(user);
        if (commit) {
          em.getTransaction().commit();
        }
  	}    
    return user;
  }

  private Clipshow getClipshowFromDB(String clipshowId, EntityManager em) throws NotFoundException {
    Clipshow show = null;
    show = em.find(Clipshow.class, Long.valueOf(clipshowId));
    if (show == null) {
      throw new NotFoundException("No clipshow with that id was found.");
    }
    return show;
  }

  private ClipshowSeries getSeriesFromDB(String seriesId, EntityManager em) throws NotFoundException {
    ClipshowSeries series = null;
    series = em.find(ClipshowSeries.class, Long.valueOf(seriesId));
    if (series == null) {
      throw new NotFoundException("No series with that id was found.");
    }
    return series;
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updated(String pid, Dictionary properties) throws ConfigurationException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleted(String pid) {
    // TODO Auto-generated method stub

  }
}
