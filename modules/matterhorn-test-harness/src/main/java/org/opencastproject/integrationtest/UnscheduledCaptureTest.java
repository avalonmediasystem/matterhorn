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
package org.opencastproject.integrationtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.sun.jersey.api.client.ClientResponse;

import org.json.simple.JSONObject;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathConstants;

/**
 * Integration test for unscheduled capture
 * 
 * @author jamiehodge
 * 
 */

public class UnscheduledCaptureTest {
  public static String recordingId;

  @Test
  public void testUnscheduledCapture() throws Exception {

    //Only run this test if -DDoCaptureTests=true is passed
    if (System.getProperty("DoCaptureTests") == null || !System.getProperty("DoCaptureTests").equals("true")) {
      return;
    }

    // Agent Registered (Capture Admin Agents)
    ClientResponse response = CaptureAdminResources.agents();
    assertEquals("Response code (agents):", 200, response.getStatus());
    Document xml = Utils.parseXml(response.getEntity(String.class));
    assertTrue("Agent included? (agents):", Utils.xPathExists(xml, "//ns1:agent-state-update[name=\'" + IntegrationTests.AGENT + "\']"));

    // Agent Registered (Capture Admin Agent)
    response = CaptureAdminResources.agent(IntegrationTests.AGENT);
    assertEquals("Response code (agent):", 200, response.getStatus());
    xml = Utils.parseXml(response.getEntity(String.class));
    assertTrue("Agent included? (agent):", Utils.xPathExists(xml, "//ns2:agent-state-update[name=\'" + IntegrationTests.AGENT + "\']"));

    // Agent idle (State)
    response = StateResources.getState();
    assertEquals("Response code (getState):", 200, response.getStatus());
    assertEquals("Agent idle? (getState):", "idle", response.getEntity(String.class));

    // Get initial recording count (Admin Proxy)
    response = AdminResources.countRecordings();
    assertEquals("Response code (countRecordings):", 200, response.getStatus());
    JSONObject initialRecordingCount = Utils.parseJson(response.getEntity(String.class));
    // System.out.println("Initial: " + initialRecordingCount);

    // Start capture (Capture)
    response = CaptureResources.startCaptureGet();
    assertEquals("Response code (startCapture):", 200, response.getStatus());

    // Get capture ID (Capture)
    recordingId = CaptureResources.captureId(response);

    // Confirm recording started (State)
    int retries = 0;
    int timeout = 60;
    while (retries < timeout) {
      Thread.sleep(1000);

      // Check capture agent status
      response = StateResources.getState();
      assertEquals("Response code (workflow instance):", 200, response.getStatus());
      if (response.getEntity(String.class).equals("capturing")) {
        break;
      }

      retries++;
    }

    if (retries == timeout) {
      fail("State Service failed to reflect that recording had started.");
    }

    // Confirm recording started (Capture Admin)
    retries = 0;
    timeout = 10;
    while (retries < timeout) {
      Thread.sleep(1000);

      // Check capture agent status
      response = CaptureAdminResources.agents();
      assertEquals("Response code (agents):", 200, response.getStatus());
      xml = Utils.parseXml(response.getEntity(String.class));
      if (Utils.xPath(xml, "//ns1:agent-state-update[name=\'" + IntegrationTests.AGENT + "\']/state", XPathConstants.STRING).equals("capturing")) {
        break;
      }

      retries++;
    }

    if (retries == timeout) {
      fail("Capture Admin failed to reflect that recording had started.");
    }

    // Get capturing recording count (Admin Proxy)
    response = AdminResources.countRecordings();
    assertEquals("Response code (countRecordings):", 200, response.getStatus());
    JSONObject capturingRecordingCount = Utils.parseJson(response.getEntity(String.class));
    // System.out.println("Recording Started: " + capturingRecordingCount);

    // Compare total recording count
    assertEquals("Total recording count the same (schedule to capture):", (Long) initialRecordingCount.get("total") + 1, capturingRecordingCount.get("total"));
    // Compare capturing recording count
    assertEquals("Capture recording count increased by one:", (Long) initialRecordingCount.get("capturing") + 1, capturingRecordingCount.get("capturing"));

    // Stop capture (Capture)
    response = CaptureResources.stopCapturePost(recordingId);
    assertEquals("Response code (stopCapturePost):", 200, response.getStatus());

    // Confirm recording stopped (State)
    retries = 0;
    timeout = 10;
    while (retries < timeout) {
      Thread.sleep(1000);

      // Check capture agent status
      response = StateResources.getState();
      assertEquals("Response code (workflow instance):", 200, response.getStatus());
      if (response.getEntity(String.class).equals("idle")) {
        break;
      }

      retries++;
    }

    if (retries == timeout) {
      fail("State Service failed to reflect that recording had stopped.");
    }

    // Confirm recording stopped (Capture Admin)
    retries = 0;
    while (retries < timeout) {
      Thread.sleep(1000);

      // Check capture agent status
      response = CaptureAdminResources.agents();
      assertEquals("Response code (agents):", 200, response.getStatus());
      xml = Utils.parseXml(response.getEntity(String.class));
      if (Utils.xPath(xml, "//ns1:agent-state-update[name=\'" + IntegrationTests.AGENT + "\']/state", XPathConstants.STRING).equals("idle")) {
        break;
      }

      retries++;
    }

    if (retries == timeout) {
      fail("Capture Admin Service failed to reflect that recording had stopped.");
    }

    // Get processing recording count (Admin Proxy)
    response = AdminResources.countRecordings();
    assertEquals("Response code (countRecordings):", 200, response.getStatus());
    JSONObject processingRecordingCount = Utils.parseJson(response.getEntity(String.class));
    System.out.println("Process Recording: " + processingRecordingCount);

    // Compare total recording count
    assertEquals("Total recording count the same (capture to process):", (Long) capturingRecordingCount.get("total"), processingRecordingCount.get("total"));
    // Compare capturing recording count
    assertEquals("Capture recording count decreased by one:", (Long) capturingRecordingCount.get("capturing") - 1, processingRecordingCount.get("capturing"));
    // Compare processing recording count
    assertEquals("Process recording count increased by one:", (Long) capturingRecordingCount.get("processing") + 1, processingRecordingCount.get("processing"));

    // TODO Confirm recording indexed

    Thread.sleep(15000);

    // Get finished recording count (Admin Proxy)
    response = AdminResources.countRecordings();
    assertEquals("Response code (countRecordings):", 200, response.getStatus());
    JSONObject finishedRecordingCount = Utils.parseJson(response.getEntity(String.class));
    System.out.println("Finished Recording: " + finishedRecordingCount);

    // Compare total recording count
    assertEquals("Total recording count the same (process to finish):", (Long) processingRecordingCount.get("total"), finishedRecordingCount.get("total"));
    // Compare processing recording count
    assertEquals("Process recording count decreased by one:", (Long) processingRecordingCount.get("processing") - 1, finishedRecordingCount.get("processing"));
    // Compare finished recording count
    assertEquals("Finished recording count increased by one:", (Long) processingRecordingCount.get("finished") + 1, finishedRecordingCount.get("finished"));

  }
}
