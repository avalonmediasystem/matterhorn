/*
 * Copyright (c) 1999 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 */

package org.w3c.dom.smil;

import org.w3c.dom.DOMException;

/**
 */
public interface ElementTimeControl {
  /**
   *  Causes this element to begin the local timeline (subject to sync 
   * constraints).  
   * @return  <code>true</code> if the method call was successful and the 
   *   element was begun. <code>false</code> if the method call failed. 
   *   Possible reasons for failure include:  The element doesn't support the 
   *   <code>beginElement</code> method. (the <code>beginEvent</code> 
   *   attribute is not set to <code>"none"</code>)  The element is already 
   *   active and can't be restart when it is active. (the 
   *   <code>restart</code> attribute is set to <code>"whenNotActive"</code>) 
   *    The element is active or has been active and can't be restart. (the 
   *   <code>restart</code> attribute is set to <code>"never"</code>). 
   * @exception DOMException
   *    SYNTAX_ERR: The element was not defined with the appropriate syntax 
   *   to allow <code>beginElement</code> calls. 
   */
  public boolean            beginElement()
                                         throws DOMException;
  /**
   *  Causes this element to end the local timeline (subject to sync 
   * constraints). 
   * @return  <code>true</code> if the method call was successful and the 
   *   element was ended. <code>false</code> if method call failed. Possible 
   *   reasons for failure include:  The element doesn't support the 
   *   <code>endElement</code> method. (the <code>endEvent</code> attribute 
   *   is not set to <code>"none"</code>)  The element is not active. 
   * @exception DOMException
   *    SYNTAX_ERR: The element was not defined with the appropriate syntax 
   *   to allow <code>endElement</code> calls. 
   */
  public boolean            endElement()
                                       throws DOMException;
}

