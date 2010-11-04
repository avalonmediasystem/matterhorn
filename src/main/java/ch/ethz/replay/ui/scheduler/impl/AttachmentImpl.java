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

import org.opencastproject.util.MimeType;
import org.opencastproject.util.MimeTypes;

import ch.ethz.replay.ui.scheduler.Attachment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;

/**
 * <em>Note:</em> Remote attachments aren't currently supported.
 * 
 * 
 */
@Entity(name = "Attachment")
@Inheritance(strategy = InheritanceType.JOINED)
public class AttachmentImpl extends BaseEntity implements Attachment {

  /** The content of the attachment */
  @Lob
  @Column(nullable = false, name = "content")
  protected String contentStore;

  /** The summary for the attachment */
  @Column
  private String summary;

  /** The filename of the attachment */
  @Column
  private String filename;

  /** The type of attachment */
  @Column
  private String type;

  /** The mime type of the attachment */
  @Column
  protected String mimeType;

  /**
   * {@inheritDoc}
   *
   * @see ch.ethz.replay.ui.scheduler.Attachment#getType()
   */
  @Override
  public String getType() {
    return type;
  }
  
  /**
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.ethz.replay.ui.scheduler.Attachment#getFilename()
   */
  @Override
  public String getFilename() {
    return filename;
  }
  
  /**
   * @param filename the filename to set
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }
  
  /**
   * @param mimeType the mimeType to set
   */
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }
  
  /**
   * @return the mimeType
   */
  public String getMimeType() {
    return mimeType;
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.ethz.replay.ui.scheduler.Attachment#getContentType()
   */
  @Override
  public MimeType getContentType() {
    if (mimeType == null) {
      return null;
    } else {
      return MimeTypes.parseMimeType(this.mimeType);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.ethz.replay.ui.scheduler.Attachment#getContent()
   */
  public String getContent() {
    return contentStore;
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see ch.ethz.replay.ui.scheduler.Attachment#setContent(java.lang.String)
   */
  public void setContent(String content) {
    this.contentStore = content;
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see ch.ethz.replay.ui.scheduler.Attachment#getSummary()
   */
  public String getSummary() {
    return summary;
  }

  /**
   * Sets the attachment summary
   * 
   * @param summary
   *          the summary
   */
  protected void setSummary(String summary) {
    this.summary = summary;
  }

}
