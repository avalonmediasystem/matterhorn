/*
 
 JobTicketImporter.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 9 10, 2008

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

import ch.ethz.replay.core.api.common.job.JobTicket;
import ch.ethz.replay.core.common.job.JobTicketImpl;
import ch.ethz.replay.ui.common.util.CollectionUtils;
import ch.ethz.replay.ui.common.util.RethrowException;
import ch.ethz.replay.ui.scheduler.impl.JobTicketAttachmentImpl;
import ch.ethz.replay.ui.scheduler.impl.persistence.AttachmentDao;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Imports JobTickets from file to database.
 * 
 * 
 */
public class JobTicketImporter {

  private static final Logger Log = Logger.getLogger(JobTicketImporter.class);

  private File dir;

  private AttachmentDao attachmentDao;

  public void setDir(File dir) {
    if (!dir.exists() || !dir.isDirectory())
      throw new IllegalArgumentException(dir.getAbsolutePath() + " is not a directory or does not exist");
    this.dir = dir;
  }

  public void setAttachmentDao(AttachmentDao attachmentDao) {
    this.attachmentDao = attachmentDao;
  }

  //

  public void importToDatabase() {
    if (Log.isDebugEnabled())
      Log.debug("Importing job tickets from " + dir.getAbsolutePath());
    Collection<JobTicket> tickets = loadTickets(dir);
    for (JobTicket j : tickets) {
      JobTicketAttachmentImpl ticketAttachment = (JobTicketAttachmentImpl) CollectionUtils.first(attachmentDao
              .findBy(j));
      if (ticketAttachment != null) {
        Log.info("Job ticket " + j.getIdentifier() + " updated in database");
        ticketAttachment.setContent(j);
        attachmentDao.save(ticketAttachment);
      } else {
        Log.info("Created new job ticket in database: " + j.getIdentifier());
        attachmentDao.save(new JobTicketAttachmentImpl(j));
      }
    }
  }

  private Collection<JobTicket> loadTickets(File dir) {
    Collection<JobTicket> routeCards = new ArrayList<JobTicket>();
    for (File file : dir.listFiles()) {
      if (file.isFile()) {
        try {
          JobTicket ticket = JobTicketImpl.fromFile(file);
          routeCards.add(ticket);
          if (Log.isDebugEnabled())
            Log.debug("Loaded job ticket " + ticket.getIdentifier());
        } catch (ParserConfigurationException e) {
          throw new RethrowException(e);
        } catch (SAXException e) {
          throw new RethrowException(e);
        } catch (IOException e) {
          throw new RethrowException(e);
        }
      }
    }
    return routeCards;
  }
}
