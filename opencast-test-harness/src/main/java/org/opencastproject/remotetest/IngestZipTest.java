/**
 *  Copyright 2009 The Regents of the University of California
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
package org.opencastproject.remotetest;

import static org.opencastproject.remotetest.AllRemoteTests.BASE_URL;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Posts a zip file to the ingest service
 */
public class IngestZipTest {
  HttpClient client;

  @Before
  public void setup() throws Exception {
    client = new DefaultHttpClient();
  }

  @After
  public void teardown() throws Exception {
    client.getConnectionManager().shutdown();
  }

  @Test
  public void testIngestZip() throws Exception {
    byte[] bytesToPost = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("ingest.zip"));
    HttpPost post = new HttpPost(BASE_URL + "/ingest/rest/addZippedMediaPackage");
    post.setEntity(new ByteArrayEntity(bytesToPost));
    HttpResponse response = client.execute(post);
    Assert.assertEquals(200, response.getStatusLine().getStatusCode());    
  }
}
