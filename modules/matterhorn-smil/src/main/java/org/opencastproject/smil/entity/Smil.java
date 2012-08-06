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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import org.opencastproject.mediapackage.MediaPackage;

@XmlRootElement(name = "smil")
@XmlSeeAlso({ SequenceElement.class, MediaElement.class })
public class Smil {

  private BodyElement body;

  private String id;

  private MediaPackage mediaPackage;

  private long workflowId;

  public Smil() {
    body = new BodyElement();
  }

  public void addElementToContainer(SmilElement e, ContainerElement to) {
    to.addElement(e);
  }

  public ContainerElement getContainerElement(String id) {
    ContainerElement e = null;
    e = getContainerElement(id, body);
    return e;
  }

  private ContainerElement getContainerElement(String id, ContainerElement in) {
    if (in.getId().equals(id)) {
      return in;
    }
    SmilElement e = in.getElement(id);
    if (e != null) {
      return (ContainerElement) e;
    } else {
      for (SmilElement elem : in.getElements()) {
        if (elem instanceof ContainerElement) {
          return getContainerElement(id, (ContainerElement) elem);
        }
      }
    }
    return (ContainerElement) e;
  }

  @XmlElement(name = "body", type = BodyElement.class)
  public BodyElement getBody() {
    return body;
  }

  public void setBody(BodyElement body) {
    this.body = body;
  }

  @XmlAttribute
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @XmlTransient
  public MediaPackage getMediaPackage() {
    return mediaPackage;
  }

  public void setMediaPackage(MediaPackage mediaPackage) {
    this.mediaPackage = mediaPackage;
  }

  @XmlAttribute
  public long getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(long workflowId) {
    this.workflowId = workflowId;
  }

}
