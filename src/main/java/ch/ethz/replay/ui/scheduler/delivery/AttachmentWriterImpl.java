/*

 AttachmentWriterImpl.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 30, 2008

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

package ch.ethz.replay.ui.scheduler.delivery;

import ch.ethz.replay.core.api.common.MimeType;
import ch.ethz.replay.core.api.common.MimeTypes;
import ch.ethz.replay.core.common.bundle.dublincore.DublinCoreCatalogImpl;
import ch.ethz.replay.core.common.job.JobTicketImpl;
import static ch.ethz.replay.ui.common.web.Header.RES_CONTENT_DISPOSITION;
import ch.ethz.replay.ui.scheduler.Attachment;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class AttachmentWriterImpl implements AttachmentWriter {

    private static final String ATTACHMENT_ENCODING = "UTF-8";
    private static final String ATTACHMENT_DEFAULT_CONTENT_TYPE = MimeTypes.TEXT.asString();

    public boolean write(Attachment attachment, HttpServletResponse response) throws IOException {
        // Response header
        MimeType contentType = attachment.getContentType();
        response.setCharacterEncoding(ATTACHMENT_ENCODING);
        response.setContentType(contentType != null ? contentType.asString() : ATTACHMENT_DEFAULT_CONTENT_TYPE);
        if (attachment.getFilename() != null)
            response.setHeader(RES_CONTENT_DISPOSITION, "attachment; filename=" + attachment.getFilename());
        //
        Object content = attachment.getContent();
        if (content instanceof DublinCoreCatalogImpl) {
            DublinCoreCatalogImpl catalog = (DublinCoreCatalogImpl) content;
            try {
                catalog.save(response.getOutputStream());
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            } catch (TransformerException e) {
                throw new RuntimeException(e);
            }
        } else if (content instanceof JobTicketImpl) {
            JobTicketImpl jobTicket = (JobTicketImpl) content;
            try {
                jobTicket.save(response.getOutputStream());
            } catch (TransformerException e) {
                throw new RuntimeException(e);
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException(attachment + " is not supported yet");
        }
        return true;
    }
}
