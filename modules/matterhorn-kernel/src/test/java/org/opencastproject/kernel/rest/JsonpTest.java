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

import org.opencastproject.kernel.rest.JsonpFilter.HttpServletResponseContentWrapper;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;

public class JsonpTest {

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
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      EasyMock.expect(originalResponse.getWriter()).andReturn(printWriter).anyTimes();
      // ByteArrayServletOutputStream out = new ByteArrayServletOutputStream();
      // EasyMock.expect(originalResponse.getOutputStream()).andReturn(out).anyTimes();
      EasyMock.expect(originalResponse.getCharacterEncoding()).andReturn("UTF-8").anyTimes();
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

      // Our string writer should now contain the wrapped content;
      Assert.assertEquals("Content wrapped?", jsonpCallback + "(" + SERVLET_OUTPUT + ");", stringWriter.getBuffer()
              .toString());
    }

    System.out.println("Total wrap time = " + totalTime);
  }

  private static final String SERVLET_OUTPUT = "<html><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>"
          + "<div>Some content</div><div>Some content</div><div>Some content</div><div>Some content</div>";

}
