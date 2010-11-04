/*
 
 DublinCoreAttachmentImpl.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 9 12, 2008

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

package ch.ethz.replay.ui.scheduler.impl;

import ch.ethz.replay.core.api.common.MimeType;
import ch.ethz.replay.core.api.common.MimeTypes;
import ch.ethz.replay.core.api.common.bundle.DublinCoreCatalog;
import ch.ethz.replay.core.common.bundle.dublincore.DublinCoreCatalogImpl;
import ch.ethz.replay.ui.common.util.Utils;
import ch.ethz.replay.ui.scheduler.DublinCoreAttachment;
import org.hibernate.annotations.AccessType;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Entity(name = "DublinCoreAttachment")
@AccessType("field")
public class DublinCoreAttachmentImpl extends AbstractAttachment<DublinCoreCatalog>
        implements DublinCoreAttachment {

    private static final String FILENAME = "metadata/dublincore.xml";

    @Transient
    private final boolean dummy = false;

    public DublinCoreAttachmentImpl() {
    }

    public DublinCoreAttachmentImpl(DublinCoreCatalog dublinCore) {
        setContent(dublinCore);
    }

    public void setContent(DublinCoreCatalog dublinCore) {
        super.setContent(dublinCore);
        if (dublinCore instanceof DublinCoreCatalogImpl) {
            setSummary(((DublinCoreCatalogImpl) dublinCore).toText());
        }
    }

    public String getFilename() {
        return FILENAME;
    }

    public MimeType getContentType() {
        return MimeTypes.XML;
    }

    public String getType() {
        return DublinCoreAttachment.TYPE_DUBLIN_CORE;
    }

    public boolean isUnique() {
        return true;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DublinCoreAttachment)) return false;

        DublinCoreAttachment that = (DublinCoreAttachment) o;
        if (Utils.equals(getUri(), that.getUri())) return true;
        if (Utils.equals(getSummary(), that.getSummary())) return true;
        return Utils.equals(getContent(), that.getContent());
    }

    public int hashCode() {
        int result;
        result = getUri() != null ? getUri().hashCode() : 0;
        result = 31 * result + (getSummary() != null ? getSummary().hashCode() : 0);
        return result;
    }
}
