/*

 RecordingDao.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 8, 2008

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

package ch.ethz.replay.ui.scheduler.impl.persistence;

import ch.ethz.replay.ui.scheduler.Attachment;
import ch.ethz.replay.ui.scheduler.Recording;
import ch.ethz.replay.ui.scheduler.Location;
import ch.ethz.replay.ui.scheduler.Person;
import ch.ethz.replay.ui.common.util.dao.GenericDao;

import java.util.Date;
import java.util.List;

/**
 * Provides access to scheduled recordings.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public interface RecordingDao extends GenericDao<Recording, Long> {

    /**
     * Gets a recording by it's job id. Note that the allocation of a job id is optional for recordings.
     *
     * @return the recording or null if there is no recording with this job id
     */
    Recording getByJobId(String jobId);

    /**
     * Delete a recording from the schedule by it's job id (if present).
     *
     * @param jobId the job id of the recording
     */
    void deleteByJobId(String jobId);

    /**
     * Returns the number of recordings in the schedule.
     */
    long count();

    Date getLastModificationDate(Long locationId);

    /**
     * Removes the given person from all recordings.
     */
    void removeContactPersonFromRecordings(Person person);

    /**
     * Returns a list of all scheduled locations.
     */
    List<Location> getAllScheduledLocations();

    /**
     * Returns a list of all scheduled locations whose names match <code>like</code>.
     */
    List<Location> getAllScheduledLocationsLike(String like);
}
