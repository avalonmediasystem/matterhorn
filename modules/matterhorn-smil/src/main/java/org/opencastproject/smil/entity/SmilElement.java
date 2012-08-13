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

import org.opencastproject.mediapackage.identifier.IdBuilder;
import org.opencastproject.mediapackage.identifier.UUIDIdBuilderImpl;

public abstract class SmilElement {

  private String id;
  
  private Smil smil;

  /** id builder, for internal use only */
  private static final IdBuilder idBuilder = new UUIDIdBuilderImpl();

  public SmilElement() {
    setId(idBuilder.createNew().compact());
  }

  @XmlAttribute
  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Smil getSmil() {
    return smil;
  }

  public void setSmil(Smil smil) {
    this.smil = smil;
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
