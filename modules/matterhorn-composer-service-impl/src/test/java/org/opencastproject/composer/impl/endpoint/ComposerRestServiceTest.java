/**
 *  Copyright 2010 The Regents of the University of California
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
package org.opencastproject.composer.impl.endpoint;

import org.opencastproject.composer.api.ComposerService;
import org.opencastproject.composer.api.Receipt;
import org.opencastproject.composer.impl.ReceiptImpl;
import org.opencastproject.media.mediapackage.MediaPackageBuilderFactory;
import org.opencastproject.media.mediapackage.MediaPackageImpl;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;


/**
 * Tests the behavior of the composer rest endpoint, using a mock composer service.
 */
public class ComposerRestServiceTest {
  Receipt receipt;
  MediaPackageImpl mp;
  String audioTrackId;
  String videoTrackId;
  String profileId;
  ComposerRestService restService;
  

  @Before
  public void setup() throws Exception {
    // Set up our arguments and return values
    audioTrackId = "audio";
    videoTrackId = "video";
    profileId = "profile";
    receipt = new ReceiptImpl("123", org.opencastproject.composer.api.Receipt.Status.QUEUED, "encoding_farm_server_456");
    mp = (MediaPackageImpl)MediaPackageBuilderFactory.newInstance().newMediaPackageBuilder().createNew();
        
    // Train a mock composer with some known behavior
    ComposerService composer = EasyMock.createNiceMock(ComposerService.class);
    EasyMock.expect(composer.encode(mp, videoTrackId, audioTrackId, profileId)).andReturn(receipt);
    EasyMock.replay(composer);

    // Set up the rest endpoint
    restService = new ComposerRestService();
    restService.setComposerService(composer);
    restService.activate(null);
  }
  
  @Test
  public void testMissingArguments() throws Exception {
    // Ensure the rest endpoint tests for missing parameters
    Response response = restService.encode(mp, "audio", "video", null);
    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());

    response = restService.encode(mp, "audio", null, "profile");
    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    
    response = restService.encode(mp, null, "video", "profile");
    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());

    response = restService.encode(null, "audio", "video", "profile");
    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }
  
  @Test
  public void testEncode() throws Exception {    
    Response response = restService.encode(mp, audioTrackId, videoTrackId, profileId);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    Assert.assertEquals(receipt, response.getEntity());
  }
}
