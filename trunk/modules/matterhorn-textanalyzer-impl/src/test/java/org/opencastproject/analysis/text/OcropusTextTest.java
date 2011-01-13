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
package org.opencastproject.analysis.text;

import static org.junit.Assert.assertEquals;

import org.opencastproject.textanalyzer.impl.ocropus.OcropusLine;

import org.junit.Before;
import org.junit.Test;

import java.awt.Rectangle;

/**
 * Test case for class {@link OcropusLine}.
 */
public class OcropusTextTest {

  /** The text item */
  protected OcropusLine textItem = null;
  
  /** The text */
  protected String text = "Hello world";
  
  /** Top boundary coordinate */
  protected int top = 10;

  /** Left boundary coordinate */
  protected int left = 20;

  /** Boundary width */
  protected int width = 110;

  /** Boundary height */
  protected int height = 60;

  /** Text boundaries */
  protected Rectangle textBoundaries = new Rectangle(left, top, width, height);

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    textItem = new OcropusLine(text, textBoundaries);
  }

  /**
   * Test method for {@link org.opencastproject.textanalyzer.impl.ocropus.OcropusLine#getText()}.
   */
  @Test
  public void testGetText() {
    assertEquals(text, textItem.getText());
  }

  /**
   * Test method for {@link org.opencastproject.textanalyzer.impl.ocropus.OcropusLine#getBoundaries()}.
   */
  @Test
  public void testGetBoundaries() {
    assertEquals(textBoundaries, textItem.getBoundaries());
  }

}
