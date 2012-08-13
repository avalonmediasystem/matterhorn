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

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "par")
public class ParallelElement extends SmilElement {

  private List<MediaElement> elements;

  public ParallelElement() {
    this.elements = new LinkedList<MediaElement>();
  }

  @XmlElement(name = "ref")
  public List<MediaElement> getElements() {
    return elements;
  }

  public void setElements(List<MediaElement> elements) {
    this.elements = elements;
  }

  public void addElement(MediaElement e) {
    this.elements.add(e);
  }

  public void removeElement(MediaElement e) {
    for (MediaElement elem : this.elements) {
      if (elem.equals(e)) {
        this.elements.remove(elem);
      }
    }
  }

}
