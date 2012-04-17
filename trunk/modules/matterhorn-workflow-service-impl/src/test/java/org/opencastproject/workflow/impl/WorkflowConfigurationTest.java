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
package org.opencastproject.workflow.impl;

import org.opencastproject.workflow.api.WorkflowConfiguration;
import org.opencastproject.workflow.api.WorkflowConfigurationImpl;
import org.opencastproject.workflow.api.WorkflowInstanceImpl;
import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.api.WorkflowOperationInstance.OperationState;
import org.opencastproject.workflow.api.WorkflowOperationInstanceImpl;
import org.opencastproject.workflow.api.WorkflowParser;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorkflowConfigurationTest {
  
  @Test
  public void testConfigurationSerialization() throws Exception {
    WorkflowOperationInstanceImpl op = new WorkflowOperationInstanceImpl("op", OperationState.RUNNING);
    Set<WorkflowConfiguration> config = new HashSet<WorkflowConfiguration>();
    config.add(new WorkflowConfigurationImpl("this", "that"));
    op.setConfiguration(config);
    WorkflowInstanceImpl instance = new WorkflowInstanceImpl();
    List<WorkflowOperationInstance> ops = new ArrayList<WorkflowOperationInstance>();
    ops.add(op);
    instance.setOperations(ops);
    String xml = WorkflowParser.toXml(instance);
    Assert.assertTrue(xml.contains("<configurations><configuration key=\"this\">that</configuration></configurations>"));
  }

}
