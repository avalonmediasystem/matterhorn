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
package org.opencastproject.remotetest.server;

import org.opencastproject.remotetest.Main;

import junit.framework.Assert;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.ParameterStyle;
import net.oauth.client.OAuthClient;
import net.oauth.client.OAuthResponseMessage;
import net.oauth.client.httpclient4.HttpClient4;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Tests the functionality of a remote workflow service rest endpoint
 */
public class OAuthTest {
  private static final Logger logger = LoggerFactory.getLogger(OAuthTest.class);

  public static final String CONSUMER_KEY = "consumerkey";

  public static final String CONSUMER_SECRET = "consumersecret";
  
  @BeforeClass
  public static void setupClass() throws Exception {
    logger.info("Running " + OAuthTest.class.getName());
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    logger.info("Finished " + OAuthTest.class.getName());
  }
  
  @Before
  public void setup() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testOAuthRequest() throws Exception {
    // Construct and signa message
    OAuthMessage oauthMessage = new OAuthMessage(OAuthMessage.GET, Main.BASE_URL + "/oauth/index.html", null);
    oauthMessage.addParameter(OAuth.OAUTH_CONSUMER_KEY, CONSUMER_KEY);
    oauthMessage.addParameter(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.HMAC_SHA1);
    oauthMessage.addParameter(OAuth.OAUTH_NONCE, UUID.randomUUID().toString());
    oauthMessage.addParameter(OAuth.OAUTH_TIMESTAMP, Long.toString(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())));
    OAuthConsumer consumer = new OAuthConsumer(null, CONSUMER_KEY, CONSUMER_SECRET, null);
    OAuthAccessor accessor = new OAuthAccessor(consumer);
    
    oauthMessage.sign(accessor);

    // Get the response
    OAuthClient client = new OAuthClient(new HttpClient4());
    OAuthResponseMessage response = (OAuthResponseMessage)client.invoke(oauthMessage, ParameterStyle.QUERY_STRING);
    
    // Make sure we got what we wanted
    Assert.assertEquals(HttpStatus.SC_OK, response.getHttpResponse().getStatusCode());
  }

}
