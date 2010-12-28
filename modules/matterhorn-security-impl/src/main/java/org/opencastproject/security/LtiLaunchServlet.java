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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A simple redirect servlet to accept an LTI login via POST.  The actual authentication happens in LtiProcessingFilter.
 */
public class LtiLaunchServlet extends HttpServlet {
  /** The serialization uid */
  private static final long serialVersionUID = 6138043870346176520L;

  /** The URI to send to the client on successful POST */
  private static final String REDIRECT_URI = "/engage/ui/";
  
  /**
   * {@inheritDoc}
   *
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // We must return a 200 for some oauth client libraries to accept this as a valid response
    resp.setHeader("Location", REDIRECT_URI);
    resp.getWriter().write("<a href=\"" + REDIRECT_URI + "\">continue...</a>");
  }

  
}
