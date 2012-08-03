/*
 * Copyright (c) 1999 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 */

package org.w3c.dom.smil;

import org.w3c.dom.DOMException;

/**
 *  Declares media content. 
 */
public interface SMILMediaElement extends ElementTime, SMILElement {
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getAbstractAttr();
  public void               setAbstractAttr(String abstractAttr)
                                                throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getAlt();
  public void               setAlt(String alt)
                                                throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getAuthor();
  public void               setAuthor(String author)
                                                throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getClipBegin();
  public void               setClipBegin(String clipBegin)
                                                throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getClipEnd();
  public void               setClipEnd(String clipEnd)
                                                throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getCopyright();
  public void               setCopyright(String copyright)
                                                throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getLongdesc();
  public void               setLongdesc(String longdesc)
                                                throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getSrc();
  public void               setSrc(String src)
                                                throws DOMException;
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
  public String             getType();
  public void               setType(String type)
                                                throws DOMException;
}

