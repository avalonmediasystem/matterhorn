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

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth.provider.ConsumerAuthentication;
import org.springframework.security.oauth.provider.token.OAuthAccessProviderToken;

import javax.servlet.http.HttpServletRequest;

/**
 * Callback interface for handing authentication details that are used when an authenticated request for a protected
 * resource is received.
 */
public class OAuthAuthenticationHandler implements
        org.springframework.security.oauth.provider.OAuthAuthenticationHandler {

  /**
   * {@inheritDoc}
   * 
   * @see org.springframework.security.oauth.provider.OAuthAuthenticationHandler#createAuthentication(javax.servlet.http.HttpServletRequest,
   *      org.springframework.security.oauth.provider.ConsumerAuthentication,
   *      org.springframework.security.oauth.provider.token.OAuthAccessProviderToken)
   */
  @Override
  public Authentication createAuthentication(HttpServletRequest request, ConsumerAuthentication authentication,
          OAuthAccessProviderToken authToken) {
    // return the consumer authentication to replace the user authentication
    return authentication;
  }

}
