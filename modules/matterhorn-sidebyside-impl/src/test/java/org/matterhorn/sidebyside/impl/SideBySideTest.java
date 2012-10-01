package org.matterhorn.sidebyside.impl;


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Rectangle;


/**
 * Unit tests for the SideBySide service
 */
public class SideBySideTest {

  private static final Logger logger = LoggerFactory.getLogger(SideBySideTest.class);
  
  @Test
  public void proba() {
    Rectangle rect = new Rectangle(200, 17);
    
    logger.info(rect.toString());
  }
  
  
}