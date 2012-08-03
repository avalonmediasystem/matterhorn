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
package org.opencastproject.smil.entity;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ref")
public class MediaElement extends SmilElement {

  private String alt;
  private String src;
  private String title;
  private String author;
  private String clipBegin;
  private String clipEnd;
  private String abstractAttr;
  private String copyright;
  private String type;
  private String longdesc;
  private String mhElement;


  public MediaElement() {
    this(null, null, null);
  }

  public MediaElement(Smil smil, String src) {
    this(src, null, null);
  }

  public MediaElement(String src, String clipBegin, String clipEnd) {
    this.src = src;
    this.clipBegin = clipBegin;
    this.clipEnd = clipEnd;
  }

  @XmlAttribute
  public String getLongdesc() {
    return longdesc;
  }

  public void setLongdesc(String longdesc) {
    this.longdesc = longdesc;
  }

  @XmlAttribute
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @XmlAttribute
  public String getAbstractAttr() {
    return this.abstractAttr;
  }

  public void setAbstractAttr(String abstractAttr) {
    this.abstractAttr = abstractAttr;
  }

  @XmlAttribute
  public String getAlt() {
    return this.alt;
  }

  public void setAlt(String alt) {
    this.alt = alt;
  }

  @XmlAttribute
  public String getAuthor() {
    return this.author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  @XmlAttribute
  public String getClipBegin() {
    return this.clipBegin;
  }
  
  public int getClipBeginMS() {
    String tmp = clipBegin.replace("s", "");
    double f = Double.valueOf(tmp);
    
    return (int) (f * 1000d);
  }

  public void setClipBegin(String clipBegin) {
    this.clipBegin = clipBegin;
  }

  @XmlAttribute
  public String getClipEnd() {
    return this.clipEnd;
  }
  
  public int getClipEndMS() {
    String tmp = clipEnd.replace("s", "");
    double f = Double.valueOf(tmp);
    
    return (int) (f * 1000d);
  }

  public void setClipEnd(String clipEnd) {
    this.clipEnd = clipEnd;
  }

  @XmlAttribute
  public String getCopyright() {
    return this.copyright;
  }

  public void setCopyright(String copyright) {
    this.copyright = copyright;
  }

  @XmlAttribute(required = true)
  public String getSrc() {
    return this.src;
  }

  public void setSrc(String src) {
    this.src = src;
  }

  @XmlAttribute
  public String getTitle() {
    return this.title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @XmlAttribute(name = "element",
                namespace = "http://www.opencastproject.org/matterhorn/")
  public String getMhElement() {
    return mhElement;
  }

  public void setMhElement(String mhElement) {
    this.mhElement = mhElement;
  }

  public boolean equals(MediaElement e) {
    return this.getId().equals(e.getId());
  }

  public int hashCode() {
    int hash = 7;
    hash = 31 * hash;
    hash = 31 * hash + (null == getId() ? 0 : getId().hashCode());
    return hash;
  }

}
