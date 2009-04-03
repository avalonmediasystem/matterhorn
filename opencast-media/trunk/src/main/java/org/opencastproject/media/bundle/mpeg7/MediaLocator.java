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

package org.opencastproject.media.bundle.mpeg7;

import org.opencastproject.media.bundle.XmlElement;

import java.net.URI;

/**
 * The media locator tells where the audio/video track is located.
 * 
 * <pre>
 * <complexType name="MediaLocatorType">
 *   <sequence>
 *       <choice minOccurs="0">
 *           <element name="MediaUri" type="anyURI"/>
 *           <element name="InlineMedia" type="mpeg7:InlineMediaType"/>
 *       </choice>
 *       <element name="StreamID" type="nonNegativeInteger" minOccurs="0"/>
 *   </sequence>
 * </complexType>
 * </pre>
 * 
 * @author Tobias Wunden <tobias.wunden@id.ethz.ch>
 * @version $Id$
 */
public interface MediaLocator extends XmlElement {

	/**
	 * Returns the media uri of the track.
	 * 
	 * @return the media uri
	 */
	URI getMediaURI();
	
}