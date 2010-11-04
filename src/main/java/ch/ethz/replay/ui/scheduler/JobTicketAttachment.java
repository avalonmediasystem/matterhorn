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

package ch.ethz.replay.ui.scheduler;

import ch.ethz.replay.core.api.common.job.JobTicket;

/**
 * Specialized attachment for {@link ch.ethz.replay.core.api.common.job.JobTicket}s.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public interface JobTicketAttachment extends Attachment<JobTicket> {

    String TYPE_JOB_TICKET = "jobticket";

    /**
     * Returns the id of the contained job ticket.
     *
     * @see ch.ethz.replay.core.api.common.job.JobTicket#getIdentifier
     */
    String getTicketId();

    /**
     * Returns the name of the containd job ticket.
     *
     * @see ch.ethz.replay.core.api.common.job.JobTicket#getName
     */
    String getTicketName();
}
