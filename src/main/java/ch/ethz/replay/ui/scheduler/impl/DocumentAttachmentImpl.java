/*
 
 GenericAttachment.java
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
import ch.ethz.replay.ui.common.util.Utils;
import ch.ethz.replay.ui.scheduler.DocumentAttachment;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 * A generic implementation of {@link ch.ethz.replay.ui.scheduler.Attachment}
 * to attach arbitrary objects.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Entity(name = "DocumentAttachment")
public class DocumentAttachmentImpl<T extends Serializable> extends AbstractAttachment<T>
        implements DocumentAttachment<T> {

    private static final String TYPE_GENERIC = "generic";

    @Column(nullable = false)
    private String type = TYPE_GENERIC;

    @Type(type = "ch.ethz.replay.ui.common.util.hibernate.MimeTypeType")
    private MimeType mimeType;

    private String filename;

    //

    DocumentAttachmentImpl() {
    }

    public DocumentAttachmentImpl(T content) {
        setContent(content);
    }

    public DocumentAttachmentImpl(T content, String type, MimeType mimeType) {
        this.type = type;
        setContent(content);
        setContentType(mimeType);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public MimeType getContentType() {
        return mimeType;
    }

    public void setContentType(MimeType mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public void setSummary(String summary) {
        super.setSummary(summary);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentAttachment)) return false;

        DocumentAttachment that = (DocumentAttachment) o;
        if (Utils.equals(getUri(), that.getUri())) return true;
        return Utils.equals(getContent(), that.getContent());
    }

    public int hashCode() {
        int result;
        result = getUri() != null ? getUri().hashCode() : 0;
        result = 31 * result + (getContent() != null ? getContent().hashCode() : 0);
        return result;
    }
}
