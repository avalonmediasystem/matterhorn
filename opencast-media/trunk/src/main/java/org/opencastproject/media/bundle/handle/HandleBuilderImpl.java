/**
 *  Copyright 2009 Opencast Project (http://www.opencastproject.org)
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

package org.opencastproject.media.bundle.handle;

import org.opencastproject.util.UrlSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * Default implementation of a handle builder. Note that this implementation
 * is for your convenience only, since all it does is creating unique
 * identifiers wrapped into the handle syntax using a globally invalid value
 * of <code>00000</code> for the naming authority and cannot update, resolve
 * or delete these handles.
 * <p>
 * To really use the benefits of handles, you need to either use the soap based
 * implementation {@link SOAPHandleBuilder} which connects to a real handle
 * server, or provide your own solution, e. g. an embedded handle server or
 * the like.
 * </p>
 * 
 * @author Tobias Wunden <tobias.wunden@id.ethz.ch>
 * @version $Id$
 */
public class HandleBuilderImpl implements HandleBuilder {
	
	/** The protocol prefix for a complete handle */
	private final static String PROTOCOL_PREFIX = Handle.PROTOCOL + "://";
	
	/** Konfiguration key for the handle authority */
	private final static String OPT_HANDLE_AUTHORITY = "handle.authority";

	/** Konfiguration key for the handle namespace */
	private final static String OPT_HANDLE_NAMESPACE = "handle.namespace";
	
	/** The default handle url */
	private final static String DEFAULT_URL = "http://localhost";

	/** Handle naming authority */
	private String namingAuthority = "10.0000";

	/** Namespace for handles */
	private String namespace = null;

	/** The default url */
	private URL defaultURL = null;

	/** the logging facility provided by log4j */
	private final static Logger log_ = LoggerFactory.getLogger(HandleBuilderImpl.class);

	/**
	 * Creates a new handle builder.
	 */
	public HandleBuilderImpl() {
		try {
			defaultURL = new URL(DEFAULT_URL);

			// Naming authority
			String authority = System.getProperty(OPT_HANDLE_AUTHORITY);
			if (authority != null && !"".equals(authority)) {
				namingAuthority = authority.trim();
				if (!namingAuthority.startsWith("10."))
					namingAuthority = "10." + namingAuthority;
			}

			// Namespace
			String ns = System.getProperty(OPT_HANDLE_NAMESPACE);
			if (ns != null && !"".equals(ns)) {
				namespace = ns.trim();
			}

		} catch (MalformedURLException e) {
			log_.error("Unable to create default " + DEFAULT_URL + " url for handle builder");
		}
	}

	/**
	 * @see org.opencastproject.media.bundle.handle.HandleBuilder#createNew()
	 */
	public Handle createNew() throws HandleException {
		if (defaultURL == null)
			throw new HandleException("Default url is malformed");
		return createNew(defaultURL);
	}

	/**
	 * @see org.opencastproject.media.bundle.handle.HandleBuilder#createNew(java.net.URL)
	 */
	public Handle createNew(URL url) throws HandleException {
		if (url == null)
			throw new IllegalArgumentException("Argument url must not be null");
		String localName = UUID.randomUUID().toString();
		if (namespace != null) {
			if (namespace.endsWith("/"))
				localName = UrlSupport.concat(namespace, localName);
			else
				localName = namespace + localName;
		}
		
		return new HandleImpl(namingAuthority, localName, url, this);
	}

	/**
	 * @see org.opencastproject.media.bundle.handle.HandleBuilder#fromValue(java.lang.String)
	 */
	public Handle fromValue(String value) throws HandleException {
		if (value == null)
			throw new IllegalArgumentException("Unable to create handle from null string");

		// Strip off handle protocol hdl://
		if (value.startsWith(PROTOCOL_PREFIX)) {
			value = value.substring(PROTOCOL_PREFIX.length());
		}
	
		// Separate naming authority from the local name
		int sep = value.indexOf('/');
		String[] parts = new String[2];
		if (sep < 0) {
			parts[0] = namingAuthority;
			parts[1] = value;
		} else {
			parts[0] = value.substring(0, sep);
			parts[1] = value.substring(sep + 1);
		}
		
		// First part must be of type long
		try {
			Float.parseFloat(parts[0]);
		} catch (NumberFormatException e) {
			throw new HandleException("Handle " + value + " is malformed");
		}
		
		return new HandleImpl(parts[0], parts[1], this);
	}

	/**
	 * This implementation throws an {@link IllegalArgumentException}, since
	 * it cannot resolve handles.
	 * 
	 * @see org.opencastproject.media.bundle.handle.HandleBuilder#resolve(org.opencastproject.media.bundle.handle.Handle)
	 */
	public URL resolve(Handle handle) throws HandleException {
		if (handle == null)
			throw new IllegalArgumentException("Cannot resolve null handle");
		// TODO: Try to get url of store, then resolve
		throw new IllegalStateException("This implementation cannot resolve handles");
	}

	/**
	 * This implementation always returns <code>true</code> without actually
	 * updating anything.
	 * 
	 * @see org.opencastproject.media.bundle.handle.HandleBuilder#update(org.opencastproject.media.bundle.handle.Handle, java.net.URL)
	 */
	public boolean update(Handle handle, URL url) throws HandleException {
		if (handle == null)
			throw new IllegalArgumentException("Cannot update null handle");
		if (handle == null)
			throw new IllegalArgumentException("Cannot update handle to null");
		return true;
	}

	/**
	 * This implementation always returns <code>true</code> without actually
	 * deleting anything.
	 * @see org.opencastproject.media.bundle.handle.HandleBuilder#delete(org.opencastproject.media.bundle.handle.Handle)
	 */
	public boolean delete(Handle handle) throws HandleException {
		if (handle == null)
			throw new IllegalArgumentException("Cannot delete null handle");
		return true;
	}

}