/*

 AbstractAttachment.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 28, 2008

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

import ch.ethz.replay.ui.common.util.RethrowException;
import ch.ethz.replay.ui.common.util.hibernate.PrePersistAware;
import ch.ethz.replay.ui.scheduler.Attachment;
import org.hibernate.annotations.Type;
import org.apache.log4j.Logger;

import javax.persistence.*;
import java.io.*;
import java.net.URI;

/**
 * <em>Note:</em> Remote attachments aren't currently supported.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Entity(name = "Attachment")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractAttachment<T extends Serializable> extends BaseEntity
        implements Attachment<T>, PrePersistAware {

    private static final Logger Log = Logger.getLogger(AbstractAttachment.class);

    private boolean remote = false;

    @Type(type = "ch.ethz.replay.ui.common.util.hibernate.URIType")
    private URI uri;

    @Transient
    private Serializable content;

    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, name = "content")
    @Type(type = "org.hibernate.type.BinaryType")
    private byte[] contentStore;

    private String summary;

    //

    protected AbstractAttachment() {
    }

    public boolean isRemote() {
        return remote;
    }

    public URI getUri() {
        return uri;
    }

    public String getSummary() {
        return summary;
    }

    protected void setSummary(String summary) {
        this.summary = summary;
    }

    public T getContent() {
        if (content == null && contentStore != null) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(contentStore);
                ObjectInputStream ois = new ObjectInputStream(bais);
                content = (Serializable) ois.readObject();
            } catch (Exception e) {
                throw new RethrowException(e);
            }
        }
        return (T) content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public void onPrePersist() {
        // IMPORTANT: Access contentStore to ensure it is loaded, because otherwise
        // it stays null and so it'll get lost on saving the object!
        getContent();
        if (Log.isDebugEnabled())
            Log.debug("Pre persist on " + this + ". content = " + content);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(content);
            contentStore = baos.toByteArray();
        } catch (IOException e) {
            throw new RethrowException(e);
        }
    }
}
