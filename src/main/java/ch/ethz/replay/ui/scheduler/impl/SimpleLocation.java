/*
 
 SimpleLocation.java
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

package ch.ethz.replay.ui.scheduler.impl;

import ch.ethz.replay.ui.scheduler.DeviceType;
import ch.ethz.replay.ui.scheduler.Location;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple locations only have a unique name.
 * 
 * 
 */
@Entity(name = "Location")
public class SimpleLocation extends BaseEntity implements Location {

  @Column(nullable = true, unique = true)
  private String name;

  @ManyToMany(targetEntity = DeviceTypeImpl.class)
  @JoinTable(name = "Location_DeviceType", joinColumns = { @JoinColumn(name = "LocationId") }, inverseJoinColumns = { @JoinColumn(name = "DeviceTypeId") })
  @Cascade({ CascadeType.SAVE_UPDATE })
  private Set<DeviceType> deviceTypes = new HashSet<DeviceType>();

  //

  SimpleLocation() {
  }

  public SimpleLocation(String name) {
    this.name = name;
  }

  public SimpleLocation(String name, Set<DeviceType> deviceTypes) {
    this.name = name;
    this.deviceTypes = deviceTypes;
  }

  public String getName() {
    return name;
  }

  public Set<DeviceType> getDevices() {
    return deviceTypes;
  }

  /**
   * Returns true.
   */
  public boolean isModifiable() {
    return true;
  }

  /**
   * Two Locations are defined equal if they have the same {@linkplain #getName() name}.
   */
  @Override
  public boolean equals(Object o) {
    return (this == o) || ((o instanceof Location) && name.equals(((Location) o).getName()));
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
