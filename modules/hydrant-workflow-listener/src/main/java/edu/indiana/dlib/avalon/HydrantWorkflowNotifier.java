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

import org.opencastproject.serviceregistry.api.ServiceRegistry;
import org.opencastproject.workflow.api.WorkflowService;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HydrantWorkflowNotifier {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(HydrantWorkflowNotifier.class);

  /** The workflow service */
  private WorkflowService workflowService;

  /** The opencast service registry */
  private ServiceRegistry serviceRegistry;

  /** The listener that pings Hydrant when an operation changed **/
  private HydrantWorkflowListener listener;

  /** The configuration key for the Avalon pingback URL **/
  private static final String urlConfigKey = "org.avalonmediasystem.avalon.url";

  public HydrantWorkflowNotifier() {
  }

  /**
   * OSGI callback for activating this component
   * 
   * @param cc
   *          the osgi component context
   */
  protected void activate(ComponentContext cc) {
    logger.info("HydrantWorkflowNotifier started.");

    String hydrantUrl = null;
    // Get the configured hydrant server URL
    if (cc != null) {
      hydrantUrl = StringUtils.trimToNull(cc.getBundleContext().getProperty(urlConfigKey));
      if (hydrantUrl == null)
        logger.warn("Avalon pingback url was not set (" + urlConfigKey + ")");
      else
        logger.info("Avalon pingback url is {}", hydrantUrl);
    }

    listener = new HydrantWorkflowListener(hydrantUrl);
    workflowService.addWorkflowListener(listener);
  }

  /**
   * Callback from OSGi on service deactivation.
   */
  public void deactivate() {
    workflowService.removeWorkflowListener(listener);
  }

  /**
   * Sets the service registry
   * 
   * @param serviceRegistry
   *          the serviceRegistry to set
   */
  public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }

  public void setWorkflowService(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  protected ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }

}
