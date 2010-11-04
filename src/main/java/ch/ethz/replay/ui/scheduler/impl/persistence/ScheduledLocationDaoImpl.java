/*
 
 ScheduledLocationDaoImpl.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 9 1, 2008

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

import ch.ethz.replay.ui.common.util.dao.NotWritableException;
import ch.ethz.replay.ui.scheduler.Location;

import java.util.List;

/**
 * Default implementation of {@link ch.ethz.replay.ui.scheduler.impl.persistence.LocationDao} which delivers only
 * scheduled locations. It does not store locations by itself, instead it accesses the stored recordings
 * to look for scheduled locations.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class ScheduledLocationDaoImpl implements LocationDao {

    private RecordingDao recordingDao;

    /**
     * DAO is based on the {@link ch.ethz.replay.ui.scheduler.impl.persistence.RecordingDao}.
     */
    public void setRecordingDao(RecordingDao recordingDao) {
        this.recordingDao = recordingDao;
    }

    /**
     * Always returns null.
     */
    public Location get(Long id) {
        return null;
    }

    public List<Location> findByExample(Location example, String... excludeProperties) {
        return null;  //todo
    }

    public List<Location> findAll() {
        return recordingDao.getAllScheduledLocations();
    }

    /**
     * Supports:
     * <ul>
     * <li>filter: {@link SimpleLocationFilter}
     * </ul>
     */
    public List<Location> findBy(Object filter) {
        if (filter instanceof SimpleLocationFilter) {
            SimpleLocationFilter f = (SimpleLocationFilter) filter;
            return recordingDao.getAllScheduledLocationsLike(f.getName());
        }
        throw new IllegalArgumentException("Unsupported filter " + filter);
    }

    public Location save(Location object) {
        throw new NotWritableException("read-only source");
    }

    public Location merge(Location object) {
        throw new NotWritableException("read-only source");
    }

    public void delete(Location object) {
        throw new NotWritableException("read-only source");
    }

    public boolean isWritable() {
        return false;
    }

    public void flush() {
        //todo
    }
}
