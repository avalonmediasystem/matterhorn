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
package org.opencastproject.workflow.impl;

import org.opencastproject.workflow.api.WorkflowService;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WorkflowServiceImplTest {
  private WorkflowService service = null;
  @Before
  public void setup() {
    service = new WorkflowServiceImpl();
    WorkflowDefinitionImpl workflowDefinition = new WorkflowDefinitionImpl();
    workflowDefinition.setId("123");
    workflowDefinition.setTitle("test title");
    workflowDefinition.setDescription("dest description");
    service.registerWorkflowDefinition(workflowDefinition);
  }

  @After
  public void teardown() {
    service = null;
  }
  
  @Test
  public void testGetWorkflowDefinition() {
    Assert.assertEquals("test title", service.getWorkflowDefinition("123").getTitle());
  }
}
