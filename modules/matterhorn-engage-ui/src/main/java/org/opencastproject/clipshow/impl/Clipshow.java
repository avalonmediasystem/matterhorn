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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "clipshows")
@XmlRootElement(name = "clipshow")
@XmlAccessorOrder(XmlAccessOrder.UNDEFINED)
@XmlAccessorType(XmlAccessType.NONE)
@NamedQueries({
  @NamedQuery(name = "clipshow", query = "SELECT c FROM Clipshow c where c.id = :id"),
  @NamedQuery(name = "clipshow.mediapackage", query = "SELECT c FROM Clipshow c where c.mediapackageId = :mpid")
})
public class Clipshow implements Serializable, Comparable<Clipshow> {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  @XmlElement(name = "id")
  private Long id;

  @Column(name = "title")
  @XmlElement(name = "title")
  private String title;

  @Column(name = "mediapackage_id")
  @XmlElement(name = "mediapackageId")
  private String mediapackageId;

  @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
  @JoinColumn(name = "author", nullable = false)
  @XmlElement(name = "author")
  private ClipshowUser author;

  @ManyToMany(mappedBy = "memberOfClipshowSet", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private Set<ClipshowUser> allowedUsers;

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "clipshow")
  @XmlElement(name = "clips")
  private List<Clip> clips;

  @ManyToMany(mappedBy = "shows")
  private Set<ClipshowSeries> series;

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "clipshow_votes")
  private Set<ClipshowVote> votes;

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "clipshow_tags")
  @XmlElement(name = "tags")
  private Set<ClipshowTag> tags;

  @XmlTransient
  @Column(name = "public")
  private Boolean publicClipshow = true;

  public Clipshow() { 
    setVotes(new LinkedHashSet<ClipshowVote>());
    setSeries(new LinkedHashSet<ClipshowSeries>());
    setClips(new LinkedList<Clip>());
    setAllowedUsers(new LinkedHashSet<ClipshowUser>());
    setTags(new LinkedHashSet<ClipshowTag>());
  }

  public Clipshow(String name, ClipshowUser author, String mediapackageId, List<Clip> clips) {
    this();
    setTitle(name);
    setAuthor(author);
    setMediapackageId(mediapackageId);
    setClips(clips);
  }

  public void setMediapackageId(String mpId) {
    mediapackageId = mpId;
  }

  public String getMediapackageId() {
    return mediapackageId;
  }

  public void setClips(List<Clip> list) {
    if (list != null) {
      clips = list;
    } else {
      clips = new LinkedList<Clip>();
    }
  }

  public List<Clip> getClips() {
    return clips;
  }

  public void setSeries(Set<ClipshowSeries> s) {
    if (s != null) {
      series = s;
    } else {
      series = new LinkedHashSet<ClipshowSeries>();
    }
  }

  public Set<ClipshowSeries> getSeries() {
    return series;
  }

  public void addSeries(ClipshowSeries s) {
    series.add(s);
  }

  public void removeSeries(ClipshowSeries s) {
    series.remove(s);
  }

  public Boolean memberOfSeries(ClipshowSeries s) {
    return series.contains(s);
  }

  public void setAuthor(ClipshowUser a) {
    //Just in case we ever change the author for some reason, remove the old author from the allowed users
    if (author != null) {
      removeAllowedUser(author);
      author.removeAuthoredClipshow(this);
    }
    author = a;
    addAllowedUser(author);
    author.addAuthoredClipshow(this);
  }
  
  public ClipshowUser getAuthor() {
    return author;
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
    if (isPublicClipshow()) {
      return true;
    }
    return allowedUsers.contains(user);
  }
  
  public void setVotes(Set<ClipshowVote> v) {
    votes = v;
  }

  public Set<ClipshowVote> getVoters() {
    return votes;
  }

  public void addVoter(ClipshowVote v) {
    votes.add(v);
  }

  public void removeVoter(ClipshowVote v) {
    votes.remove(v);
  }

  public void setTitle(String t) {
    title = t;
  }

  public String getTitle() {
    return title;
  }

  public Long getId() {
    return id;
  }

  public void setTags(Set<ClipshowTag> newTags) {
    tags = newTags;
  }

  public Set<ClipshowTag> getTags() {
    return tags;
  }

  public void addTag(ClipshowTag tag) {
    removeTag(tag); //Remove the old one so that the new one definitely replaces it
    tags.add(tag);
  }

  public void removeTag(ClipshowTag tag) {
    tags.remove(tag);
  }

  public Boolean getPublicClipshow() {
    return publicClipshow;
  }

  public void setPublicClipshow(Boolean publicClipshow) {
    this.publicClipshow = publicClipshow;
  }

  public boolean isPublicClipshow() {
    return publicClipshow.booleanValue();
  }

  @Override
  public int compareTo(Clipshow o) {
    if (o.getVoters().size() < this.getVoters().size()) {
      return 1;
    } else if (o.getVoters().size() > this.getVoters().size()) {
      return -1;
    } else {
      return 0;
    }
  }
}
