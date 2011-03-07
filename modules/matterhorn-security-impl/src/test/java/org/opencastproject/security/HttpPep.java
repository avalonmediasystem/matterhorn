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
package org.opencastproject.security;

import java.net.URI;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.jboss.security.xacml.core.model.context.ActionType;
import org.jboss.security.xacml.core.model.context.AttributeType;
import org.jboss.security.xacml.core.model.context.EnvironmentType;
import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResourceType;
import org.jboss.security.xacml.core.model.context.SubjectType;
import org.jboss.security.xacml.factories.RequestAttributeFactory;
import org.jboss.security.xacml.factories.RequestResponseContextFactory;
import org.jboss.security.xacml.interfaces.RequestContext;

/**
 * PEP for the web layer
 * 
 * @author Anil Saldhana
 */
public class HttpPep {
  String ACTION_IDENTIFIER = "urn:oasis:names:tc:xacml:1.0:action:action-id";
  String CURRENT_TIME_IDENTIFIER = "urn:oasis:names:tc:xacml:1.0:environment:current-time";
  String RESOURCE_IDENTIFIER = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
  String SUBJECT_IDENTIFIER = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";
  String SUBJECT_ROLE_IDENTIFIER = "urn:oasis:names:tc:xacml:2.0:subject:role";

  @SuppressWarnings("unchecked")
  public RequestContext createXACMLRequest(HttpServletRequest request, Principal principal, Group roleGroup)
          throws Exception {
    RequestContext requestCtx = RequestResponseContextFactory.createRequestCtx();

    // Create a subject type
    SubjectType subject = new SubjectType();
    subject.getAttribute().add(
            RequestAttributeFactory.createStringAttributeType(SUBJECT_IDENTIFIER, "jboss.org", principal.getName()));
    Enumeration<Principal> roles = (Enumeration<Principal>) roleGroup.members();
    while (roles.hasMoreElements()) {
      Principal rolePrincipal = roles.nextElement();
      AttributeType attSubjectID = RequestAttributeFactory.createStringAttributeType(SUBJECT_ROLE_IDENTIFIER,
              "jboss.org", rolePrincipal.getName());
      subject.getAttribute().add(attSubjectID);
    }

    // Create a resource type
    ResourceType resourceType = new ResourceType();
    resourceType.getAttribute().add(
            RequestAttributeFactory.createAnyURIAttributeType(RESOURCE_IDENTIFIER, null,
                    new URI(request.getRequestURI())));

    // Create an action type
    ActionType actionType = new ActionType();
    actionType.getAttribute().add(
            RequestAttributeFactory.createStringAttributeType(ACTION_IDENTIFIER, "jboss.org", "read"));

    // Create an Environment Type (Optional)
    EnvironmentType environmentType = new EnvironmentType();
    environmentType.getAttribute().add(
            RequestAttributeFactory.createDateTimeAttributeType(CURRENT_TIME_IDENTIFIER, null));

    // Create a Request Type
    RequestType requestType = new RequestType();
    requestType.getSubject().add(subject);
    requestType.getResource().add(resourceType);
    requestType.setAction(actionType);
    requestType.setEnvironment(environmentType);

    requestCtx.setRequest(requestType);

    return requestCtx;
  }
}