/*
 * Copyright (c) 1999 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 */

package org.w3c.dom.smil;

import org.w3c.dom.DOMException;

/**
 *  Defines the test attributes interface. See the Test attributes definition 
 * in SMIL 1.0. 
 */
public interface ElementTest {
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getSystemBitrate();
  public void               setSystemBitrate(String systemBitrate)
                                       throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getSystemCaptions();
  public void               setSystemCaptions(String systemCaptions)
                                       throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getSystemLanguage();
  public void               setSystemLanguage(String systemLanguage)
                                       throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getSystemOverdubOrCaption();
  public void               setSystemOverdubOrCaption(String systemOverdubOrCaption)
                                       throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getSystemRequired();
  public void               setSystemRequired(String systemRequired)
                                       throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getSystemScreenSize();
  public void               setSystemScreenSize(String systemScreenSize)
                                       throws DOMException;
  /**
   * @exception DOMException
   *    NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly. 
   */
  public String             getSystemScreenDepth();
  public void               setSystemScreenDepth(String systemScreenDepth)
                                       throws DOMException;
}

