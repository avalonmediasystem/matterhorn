/*

 Deletion.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Aug 5, 2008

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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 * Helper entity to keep track of the deletion of recordings for a certain location.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Entity(name = "Deletion")
public class Deletion implements Serializable {

    @Id
    private Long locationId;

    // Will be updated via AutoTimestampOnUpdateListener
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastUpdated;

    /**
     * Hibernate only
     */
    Deletion() {
    }

    public Deletion(Long locationId) {
        this.locationId = locationId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }
}
