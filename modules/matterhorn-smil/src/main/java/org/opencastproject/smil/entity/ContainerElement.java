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

public abstract class ContainerElement extends SmilElement {
  
  protected List<SmilElement> elements;
  
  public ContainerElement() {
    setElements(new LinkedList<SmilElement>());
  }

  public abstract List<SmilElement> getElements();

  public void setElements(List<SmilElement> elements) {
    this.elements = elements;
  }
  
  public void addElement(SmilElement e) {
    elements.add(e);
  }

  public SmilElement getElement(String id) {
    SmilElement e = null;

    for (SmilElement elem : elements) {
      if (elem.getId().equals(id)) {
        return elem;
      }
    }

    return e;
  }

  public void removeElement(String id) {
    elements.remove(getElement(id));
  }

}
