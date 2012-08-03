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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlRootElement(name = "smil")
@XmlSeeAlso({ SequenceElement.class, MediaElement.class })
public class Smil {
  
  private BodyElement body;
  
  public Smil() {
    body = new BodyElement();
  }
  
  @XmlElement(name = "body", type = BodyElement.class)
  public BodyElement getBody() {
    return body;
  }

  public void setBody(BodyElement body) {
    this.body = body;
  }
  
}
