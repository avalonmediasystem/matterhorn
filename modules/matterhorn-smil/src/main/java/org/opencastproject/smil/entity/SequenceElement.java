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

@XmlRootElement(name = "seq")
/**
 * SequenceElement representing a &lt;seq &rt; element
 */
public class SequenceElement extends SmilElement {

  private List<ParallelElement> elements;

  public SequenceElement() {
    this.elements = new LinkedList<ParallelElement>();
  }

  @XmlElement(name = "par")
  public List<ParallelElement> getElements() {
    return elements;
  }

  public void setElements(List<ParallelElement> elements) {
    this.elements = elements;
  }

  /**
   * get a parallelElement by a given id
   * 
   * @param elementId the id of the element to retrieve
   * @return the element if it exists, <code>null</code> else
   */
  public ParallelElement getParallel(String elementId) {
    for (ParallelElement p : this.elements) {
      if (p.getId().equals(elementId)) {
        return p;
      }
    }
    return null;
  }

  /**
   * add a ParallelElement
   * 
   * @param p the ParallelElement to add
   */
  public void addParallel(ParallelElement p) {
    this.elements.add(p);
  }

  /**
   * remove a ParallelElement
   * 
   * @param e the element to remove
   */
  public void removeParallel(ParallelElement e) {
    for (ParallelElement elem : this.elements) {
      if (elem.equals(e)) {
        this.elements.remove(elem);
      }
    }
  }

}
