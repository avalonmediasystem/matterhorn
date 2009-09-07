/**
 *  Copyright 2009 The Regents of the University of California
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
package org.opencastproject.workflow.endpoint;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * TODO: Comment me!
 *
 */
@XmlType(name="workflow-instances", namespace="http://workflow.opencastproject.org/")
@XmlRootElement(name="workflow-instances", namespace="http://workflow.opencastproject.org/")
@XmlAccessorType(XmlAccessType.FIELD)
public class WorkflowInstanceJaxbImplList {
  @XmlElement(name="workflow-instance")
  protected List<WorkflowInstanceJaxbImpl> instance;
  public List<WorkflowInstanceJaxbImpl> getWorkflowInstance() {
    if (instance == null) {
      instance = new ArrayList<WorkflowInstanceJaxbImpl>();
    }
    return instance;
  }
}
