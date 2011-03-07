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

import junit.framework.TestCase;

import org.jboss.security.xacml.factories.RequestResponseContextFactory;
import org.jboss.security.xacml.interfaces.PolicyDecisionPoint;
import org.jboss.security.xacml.interfaces.RequestContext;
import org.jboss.security.xacml.interfaces.ResponseContext;

import java.io.InputStream;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Utility class for the JBossXACML Tests
 * 
 * @author Anil.Saldhana
 */
public class XacmlTestUtil {
  // Enable for request trace
  private static boolean debug = "true".equals(System.getProperty("debug", "false"));

  /**
   * Get the decision from the PDP
   * 
   * @param pdp
   * @param requestFileLoc
   *          a file where the xacml request is stored
   * @return
   * @throws Exception
   */
  public static int getDecision(PolicyDecisionPoint pdp, String requestFileLoc) throws Exception {
    ClassLoader tcl = Thread.currentThread().getContextClassLoader();
    InputStream is = tcl.getResourceAsStream(requestFileLoc);
    RequestContext request = RequestResponseContextFactory.createRequestCtx();
    request.readRequest(is);
    ResponseContext response = pdp.evaluate(request);
    if (response == null)
      throw new RuntimeException("Response is null");
    return response.getDecision();
  }

  /**
   * Get the decision from the PDP
   * 
   * @param pdp
   * @param request
   *          RequestContext containing the request
   * @return
   * @throws Exception
   */
  public static int getDecision(PolicyDecisionPoint pdp, RequestContext request) throws Exception {
    ResponseContext response = pdp.evaluate(request);
    if (debug)
      response.marshall(System.out);
    TestCase.assertNotNull("Response is not null", response);
    return response.getDecision();
  }

  /**
   * Get a Group with the passed rolename
   * 
   * @param roleName
   *          rolename which will be placed as a principal
   * @return
   */
  public static Group getRoleGroup(final String roleName) {
    return new Group() {

      private Vector<Principal> vect = new Vector<Principal>();

      public boolean addMember(final Principal principal) {
        return vect.add(principal);
      }

      public boolean isMember(Principal principal) {
        return vect.contains(principal);
      }

      public Enumeration<Principal> members() {
        vect.add(new Principal() {

          public String getName() {
            return roleName;
          }
        });
        return vect.elements();
      }

      public boolean removeMember(Principal principal) {
        return vect.remove(principal);
      }

      public String getName() {
        return "ROLES";
      }
    };
  }

}