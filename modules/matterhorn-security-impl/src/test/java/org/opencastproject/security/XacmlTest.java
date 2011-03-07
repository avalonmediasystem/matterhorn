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

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.jboss.security.xacml.core.JBossPDP;
import org.jboss.security.xacml.interfaces.PolicyDecisionPoint;
import org.jboss.security.xacml.interfaces.RequestContext;
import org.jboss.security.xacml.interfaces.XACMLConstants;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.security.Principal;
import java.security.acl.Group;

import javax.servlet.http.HttpServletRequest;

/**
 * Experiments with JBoss XACML
 */
public class XacmlTest {
  
  @Test
  public void testJbossPdpFromConfigFiles() throws Exception {
    // Create a new PDP using a sample configuration
    InputStream is = getClass().getResourceAsStream("/org/opencastproject/security/xacml-config.xml");
    PolicyDecisionPoint pdp = new JBossPDP(is);
    IOUtils.closeQuietly(is);

    Principal principal = new Principal() {
      @Override
      public String getName() {
        return "user1";
      }
    };

    Group developerGroup = XacmlTestUtil.getRoleGroup("developer");
    Group nonDeveloperGroup = XacmlTestUtil.getRoleGroup("not a developer");

    // Mock up a request
    String uri = "http://test/developer-guide.html";
    HttpServletRequest req = EasyMock.createNiceMock(HttpServletRequest.class);
    EasyMock.expect(req.getUserPrincipal()).andReturn(principal).anyTimes();
    EasyMock.expect(req.getRequestURI()).andReturn(uri).anyTimes();
    EasyMock.replay(req);
    
    HttpPep pep = new HttpPep();
    
    // Ensure that users in the "developer" group can access the resource at this URI
    RequestContext request = pep.createXACMLRequest(req, principal, developerGroup);
    Assert.assertEquals("Access Allowed?", XACMLConstants.DECISION_PERMIT, XacmlTestUtil.getDecision(pdp, request));
    
    // Ensure that users in the "not a developer" group can not access the resource at this URI
    RequestContext deniedRequest = pep.createXACMLRequest(req, principal, nonDeveloperGroup);
    Assert.assertEquals("Access Allowed?", XACMLConstants.DECISION_DENY, XacmlTestUtil.getDecision(pdp, deniedRequest));
  }
}
