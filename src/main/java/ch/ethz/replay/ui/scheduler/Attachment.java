/*

 Attachment.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 10, 2008

 Copyright (c) 2007 ETH Zurich, Switzerland

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/

package ch.ethz.replay.ui.scheduler;

import ch.ethz.replay.core.api.common.MimeType;

import java.io.Serializable;
import java.net.URI;

/**
 * {@link Recording Recordings} may have attachments to associate specialized and arbitrary data with them.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public interface Attachment<T extends Serializable> {

    /**
     * Returns the internal id. This is id does not have any business meanings.
     */
    Long getId();

    /**
     * Returns the content of the attachment. Remote attachments are free to retrieve the content
     * from the remote location.
     *
     * @return the content or null
     */
    T getContent();

    void setContent(T content);

    /**
     * Returns an optional summary of the attachment's content. This may be used for indexing and searching
     * purposes.
     *
     * @return the summary or null
     */
    String getSummary();

    /**
     * Returns the mime type of the content if known.
     *
     * @return the mime type or null
     */
    MimeType getContentType();

    /**
     * Returns a freely definable type. Please follow general REPLAY rules regarding types.
     *
     * @return a type
     */
    String getType();

    /**
     * Returns the URI where the actual content of the attachment is located.
     * Remote Attachments are forced to have an URI.
     *
     * @return the URI or null
     */
    URI getUri();

    /**
     * Checks if the content of this attachment is remotely located.
     */
    boolean isRemote();

    /**
     * Return a filename suitable for this attachment if it is provided as a download. May be a path as
     * well like e.g. <code>metadata/dublincore.xml</code>.
     *
     * @return a filename or null
     */
    String getFilename();
}
