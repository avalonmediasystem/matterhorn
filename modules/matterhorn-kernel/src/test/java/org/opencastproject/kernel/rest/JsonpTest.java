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
package org.opencastproject.kernel.rest;

import static org.opencastproject.kernel.rest.JsonpFilter.CHARACTER_ENCODING;

import org.opencastproject.kernel.rest.JsonpFilter.ByteArrayServletOutputStream;
import org.opencastproject.kernel.rest.JsonpFilter.HttpServletResponseContentWrapper;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.Arrays;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;

public class JsonpTest {

  private String SERVLET_OUTPUT = null;

  @Before
  public void setup() throws Exception {
    SERVLET_OUTPUT = IOUtils.toString(getClass().getResourceAsStream("/utf8.json"), CHARACTER_ENCODING);
  }

  @Test
  public void testCallbackSafety() {
    // Some good ones
    Assert.assertTrue(JsonpFilter.SAFE_PATTERN.matcher("GoodCallback").matches());
    Assert.assertTrue(JsonpFilter.SAFE_PATTERN.matcher("GoodCallback1").matches());
    Assert.assertTrue(JsonpFilter.SAFE_PATTERN.matcher("Good1Callback").matches());
    Assert.assertTrue(JsonpFilter.SAFE_PATTERN.matcher("Object.GoodCallback").matches());

    // Some bad ones
    Assert.assertFalse(JsonpFilter.SAFE_PATTERN.matcher("alert(document.cookie)").matches());
  }

  @Test
  public void testPaddingPerformance() throws Exception {

    // Run 1000 request through and record the total time spent wrapping the response
    int numberOfRuns = 1000;
    int totalTime = 0;

    for (int i = 0; i < numberOfRuns; i++) {
      // Mock an 'original' http response
      HttpServletResponse originalResponse = EasyMock.createNiceMock(HttpServletResponse.class);
      ByteArrayServletOutputStream out = new ByteArrayServletOutputStream();
      EasyMock.expect(originalResponse.getOutputStream()).andReturn(out).anyTimes();
      EasyMock.expect(originalResponse.getCharacterEncoding()).andReturn(CHARACTER_ENCODING).anyTimes();
      EasyMock.replay(originalResponse);

      // Wrap the response
      String jsonpCallback = "CallMeBack";
      HttpServletResponseContentWrapper response = new HttpServletResponseContentWrapper(originalResponse,
              jsonpCallback);
      response.enableWrapping = true;

      // Simulate a servlet writing to the wrapped response
      long start = System.currentTimeMillis();

      response.getWriter().write(SERVLET_OUTPUT);

      // Simulate the jsonp filter flushing the wrapped buffer
      response.flushWrapper();

      totalTime += System.currentTimeMillis() - start;

      // Our output stream should now contain the wrapped content;
      byte[] expected = (jsonpCallback + JsonpFilter.OPEN_PARENS + SERVLET_OUTPUT + JsonpFilter.POST_PADDING)
              .getBytes();
      byte[] actual = out.toByteArray();
      Assert.assertTrue("Content wrapped properly?", Arrays.areEqual(expected, actual));
    }

    System.out.println("Total wrap time = " + totalTime);
  }
}
