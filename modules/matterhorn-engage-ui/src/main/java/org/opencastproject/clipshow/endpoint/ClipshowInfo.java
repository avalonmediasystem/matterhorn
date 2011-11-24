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
package org.opencastproject.clipshow.endpoint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "clipshow-info-blob", namespace = "http://clipshow.engage.opencastproject.org")
@XmlRootElement(name = "clipshow-info-blob", namespace = "http://clipshow.engage.opencastproject.org")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClipshowInfo implements Comparable<ClipshowInfo> {

  @XmlElement(name = "id")
  private Long id;

  @XmlElement(name = "author")
  private String author;

  @XmlElement(name = "title")
  private String title;

  @XmlElement(name = "good")
  private Integer good = 0;

  private Integer funny = 0;

  private Integer dislike = 0;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }


  public Integer getGood() {
    return good;
  }

  public void setGood(Integer good) {
    this.good = good;
  }

  public Integer getFunny() {
    return funny;
  }

  public void setFunny(Integer funny) {
    this.funny = funny;
  }

  public Integer getDislike() {
    return dislike;
  }

  public void setDislike(Integer dislike) {
    this.dislike = dislike;
  }

  @XmlElement(name = "serverVotes")
  public Integer getVotes() {
    return getGood() + getFunny() - getDislike();
  }

  @Override
  public int compareTo(ClipshowInfo o) {
    if (o.getVotes() < getVotes()) {
      return 1;
    } else if (o.getVotes() > getVotes()) {
      return -1;
    } else {
      return 0;
    }
  }
}
