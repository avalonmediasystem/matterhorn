/*
 * Copyright (c) 1999 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 */

package org.w3c.dom.smil;

import org.w3c.dom.DOMException;

/**
 *  Controls the position, size and scaling of media object elements. See the 
 * region element definition in SMIL 1.0. 
 */
public interface SMILRegionElement extends SMILElement {
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getTitle();
  public void               setTitle(String title)
                                       throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getSkipContent();
  public void               setSkipContent(String skipContent)
                                       throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getFit();
  public void               setFit(String fit)
                                       throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getBackgroundColor();
  public void               setBackgroundColor(String backgroundColor)
                                       throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public int                getHeight();
  public void               setHeight(int height)
                                       throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public int                getWidth();
  public void               setWidth(int width)
                                       throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getTop();
  public void               setTop(String top)
                                       throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public int                getZIndex();
  public void               setZIndex(int zIndex)
                                       throws DOMException;
}

