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
package edu.indiana.dlib.avalon;

import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowListener;
import org.opencastproject.util.UrlSupport;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A simple workflow listener implementation suitable for monitoring a workflow's state changes.
 */
public class HydrantWorkflowListener implements WorkflowListener {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(HydrantWorkflowListener.class);

  private static String hydrantUrl = "http://localhost:3000/";

  public HydrantWorkflowListener() {
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowListener#operationChanged(org.opencastproject.workflow.api.WorkflowInstance)
   */
  @Override
  public void operationChanged(WorkflowInstance workflow) {
    synchronized (this) {
      logger.trace("Operation changed - pinging Hydrant");
      String pid = workflow.getTitle();
      long id = workflow.getId();
      pingHydrant(pid, id);      
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowListener#stateChanged(org.opencastproject.workflow.api.WorkflowInstance)
   */
  @Override
  public void stateChanged(WorkflowInstance workflow) {
    synchronized (this) {
      logger.debug("No-op");
    }
  }

  private void pingHydrant(String pid, long workflowId) {
        try {
                String url = UrlSupport.concat(new String[] { hydrantUrl, "master_file", pid });
                MultiThreadedHttpConnectionManager mgr = new MultiThreadedHttpConnectionManager();
                HttpClient client = new HttpClient(mgr);

                PutMethod put = new PutMethod(url);

                Part[] parts = {
                        new StringPart("workflow_id", String.valueOf(workflowId)),
                };
                put.setRequestEntity(
                        new MultipartRequestEntity(parts, put.getParams())
                );
                int status = client.executeMethod(put);
                logger.debug("Got status: " + status);
                logger.trace("Got response body: " + put.getResponseBodyAsString());
        } catch (IOException e) {
                logger.debug("Exception pinging Hydrant: " + e.getCause());
        }
  }

}
