/*
 * Copyright (c) 1999 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 */

package org.w3c.dom.smil;

import org.w3c.dom.DOMException;

/**
 *  This interface represents the SMIL <code>animate</code> element, the SMIL 
 * <code>animateColor</code> element and the SMIL <code>animateMotion</code> 
 * element. 
 */
public interface SMILAnimateElement extends ElementAnimation, SMILElement {
  /**
   *  A semicolon-separated list of time values used to control the pacing of 
   * the animation. 
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public TimeList           getKeyTimes();
  public void               setKeyTimes(TimeList keyTimes)
                                       throws DOMException;
  /**
   *  A set of Bezier control points associated with the keyTimes list. 
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public TimeList           getKeySplines();
  public void               setKeySplines(TimeList keySplines)
                                       throws DOMException;
}

