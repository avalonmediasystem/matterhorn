/*
 
 MetadataTagged.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 12 01, 2008

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

import ch.ethz.replay.core.api.common.bundle.DublinCoreCatalog;

/**
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public interface MetadataTagged {

    /**
     * Returns the - optional - title of this recording. A good idea may be to use information
     * from a propably associated meta data attachment such as Dublin Core.
     *
     * @return the title or null
     */
    String getTitle();

    /**
     * Sets the title of this recording. A recording must not have a title.
     */
    void setTitle(String title);

    /**
     * Sets the Dublin Core metadata.
     */
    void setDublinCore(DublinCoreCatalog dublinCore);

    /**
     * Returns the Dublin Core or null if not yet set.
     */
    DublinCoreCatalog getDublinCore();

    /**
     * Returns the Dublin Core Attachment.
     */
    DublinCoreAttachment getDublinCoreAttachment();
}
