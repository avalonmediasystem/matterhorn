/*
 * Copyright (c) 1999 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 */

package org.w3c.dom.smil;

import org.w3c.dom.DOMException;

/**
 *  Defines a block of content control. See the switch element definition in 
 * SMIL 1.0. 
 */
public interface SMILSwitchElement extends SMILElement {
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getTitle();
  public void               setTitle(String title)
                                       throws DOMException;
}

