/*
 * Copyright (c) 1999 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 */

package org.w3c.dom.smil;

import org.w3c.dom.DOMException;

/**
 *  This interface defines a time container with semantics based upon par, but 
 * with the additional constraint that only one child element may play at a 
 * time. 
 */
public interface ElementExclusiveTimeContainer extends ElementTimeContainer {
  /**
   *  Controls the end of the container. 
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getEndSync();
  public void               setEndSync(String endSync)
                                                throws DOMException;
}

