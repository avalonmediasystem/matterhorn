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

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "clipshow_series")
@XmlRootElement(name = "clipshow-series")
@NamedQueries({
  @NamedQuery(name = "clipshow-series.all", query = "SELECT c FROM ClipshowSeries c")
})
public class ClipshowSeries implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  @XmlElement(name = "id")
  private Long id;

  @Column(name = "title")
  @XmlElement(name = "title")
  private String title;

  @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
  @JoinColumn(name = "author", nullable = false)
  @XmlElement(name = "author")
  private ClipshowUser author;

  @ManyToMany
  @JoinTable(name = "clipshow_series_mappings")
  @XmlElement(name = "shows")
  private Set<Clipshow> shows;

  @ManyToMany(mappedBy = "memberOfSeriesSet", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @XmlTransient //This could be a very large number of users
  private Set<ClipshowUser> allowedUsers;

  public ClipshowSeries() {
    setClipshows(new LinkedHashSet<Clipshow>());
    setAllowedUsers(new LinkedHashSet<ClipshowUser>());
  }
  
  public ClipshowSeries(String name, ClipshowUser author) {
    this();
    setTitle(name);
    setAuthor(author);
  }

  public Long getId() {
    return id;
  }

  public void setTitle(String t) {
    title = t;
  }

  public String getTitle() {
    return title;
  }

  public void setAuthor(ClipshowUser a) {
    //Just in case we ever change the author for some reason, remove the old author from the allowed users
    if (author != null) {
      removeAllowedUser(author);
      author.removeAuthoredSeries(this);
    }
    author = a;
    addAllowedUser(author);
    author.addAuthoredSeries(this);
  }

  public ClipshowUser getAuthor() {
    return author;
  }

  public void setClipshows(Set<Clipshow> set) {
    if (set != null) {
      shows = set;
    } else {
      shows = new LinkedHashSet<Clipshow>();
    }
  }

  public Set<Clipshow> getClipshows() {
    return shows;
  }

  public void addClipshow(Clipshow clipshow) {
    shows.add(clipshow);
    clipshow.addSeries(this);
  }

  public void removeClipshow(Clipshow clipshow) {
    shows.remove(clipshow);
    clipshow.removeSeries(this);
  }

  public Boolean containsClipshow(Clipshow c) {
    return shows.contains(c);
  }

  public void setAllowedUsers(Set<ClipshowUser> users) {
    if (users != null) {
      allowedUsers = users;
    } else {
      allowedUsers = new LinkedHashSet<ClipshowUser>();
    }
    addAllowedUser(author);
  }

  public Set<ClipshowUser> getAllowedUsers() {
    return allowedUsers;
  }

  public void addAllowedUser(ClipshowUser user) {
    allowedUsers.add(user);
  }

  public void removeAllowedUser(ClipshowUser user) {
    allowedUsers.remove(user);
  }

  public Boolean userAllowed(ClipshowUser user) {
    return allowedUsers.contains(user);
  }
}