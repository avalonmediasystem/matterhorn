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
package org.opencastproject.capture.impl;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opencastproject.capture.api.AgentState;

public class StatusServiceImplTest {
  private CaptureAgentImpl service = null;

  @Before
  public void setup() {
    service = new CaptureAgentImpl();
    service.activate(null);
    Assert.assertNotNull(service);
  }

  @After
  public void teardown() {
    service = null;
  }

  @Test
  public void testStartup() {
    Assert.assertEquals(AgentState.IDLE, service.getAgentState());
  }

  @Test
  public void testStart() {
    service.startCapture();
    Assert.assertEquals(AgentState.CAPTURING, service.getAgentState());
  }

  @Test
  public void testStop() {
    service.startCapture();
    Assert.assertEquals(AgentState.CAPTURING, service.getAgentState());
    service.stopCapture();
    Assert.assertEquals(AgentState.UPLOADING, service.getAgentState());
  }

  @Test
  public void testHalt() {
    service.startCapture();
    Assert.assertEquals(AgentState.CAPTURING, service.getAgentState());
    service.stopCapture();
    Assert.assertEquals(AgentState.UPLOADING, service.getAgentState());
    service.stopCapture();
    Assert.assertEquals(AgentState.IDLE, service.getAgentState());
  }
}
