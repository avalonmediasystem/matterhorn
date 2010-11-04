/*
 
 JobTicketAttachmentImpl.java
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
import ch.ethz.replay.core.api.common.job.JobTicket;
import ch.ethz.replay.ui.common.util.Utils;
import ch.ethz.replay.ui.scheduler.JobTicketAttachment;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Entity(name = "JobTicketAttachment")
public class JobTicketAttachmentImpl extends AbstractAttachment<JobTicket>
        implements JobTicketAttachment {

    @Column(nullable = false, unique = true)
    private String ticketId;

    @Column(nullable = false)
    private String ticketName;

    //

    public JobTicketAttachmentImpl() {
    }

    public JobTicketAttachmentImpl(JobTicket ticket) {
        setContent(ticket);
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getTicketName() {
        return ticketName;
    }

    public void setContent(JobTicket ticket) {
        super.setContent(ticket);
        ticketId = ticket.getIdentifier();
        ticketName = ticket.getName();
    }

    public String getFilename() {
        return JobTicket.FILENAME;
    }

    public MimeType getContentType() {
        return MimeTypes.XML;
    }

    public String getType() {
        return JobTicketAttachment.TYPE_JOB_TICKET;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobTicketAttachment)) return false;

        JobTicketAttachment that = (JobTicketAttachment) o;
        return Utils.equals(ticketId, that.getTicketId());
    }

    public int hashCode() {
        return ticketId != null ? ticketId.hashCode() : 0;
    }
}
