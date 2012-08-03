/*
 * Copyright (c) 1999 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 */

package org.w3c.dom.smil;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 *  This interface define the set of animation extensions.  The  attributes 
 * will go in a XLink interface. 
 */
public interface ElementAnimation extends ElementTime, ElementTimeControl {
  /**
   *  This attribute specifies the target element to be animated. If no 
   * <code>href</code> and <code>targetElement</code> are specified in the 
   * animation document, the default value of this attribute is the first 
   * element ancestor. If a <code>href</code> is not <code>null</code>, 
   * setting this attribute has no effect. 
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public Element            getTargetElement();
  public void               setTargetElement(Element targetElement)
                                       throws DOMException;
  /**
   *  This attribute specifies an  reference to the target element to be 
   * animated. 
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getHref();
  public void               setHref(String href)
                                       throws DOMException;
}

