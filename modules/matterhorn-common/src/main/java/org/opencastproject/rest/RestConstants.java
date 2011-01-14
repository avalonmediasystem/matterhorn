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
package org.opencastproject.rest;

/**
 * Constant definitions used to define and consume Matterhorn REST services.
 */
public interface RestConstants {

  /** The service property indicating the type of service. This is an arbitrary ID, not necessarily a java interface. */
  String SERVICE_TYPE_PROPERTY = "opencast.service.type";

  /** The service property indicating the URL path that the service is attempting to claim */
  String SERVICE_PATH_PROPERTY = "opencast.service.path";

  /** The service property indicating that this service should be registered in the remote service registry */
  String SERVICE_JOBPRODUCER_PROPERTY = "opencast.service.jobproducer";

  /** The ID by which this http context is known by the extended http service */
  String HTTP_CONTEXT_ID = "opencast.httpcontext";
  
}
