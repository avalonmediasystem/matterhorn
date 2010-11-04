/*

 DeviceType.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created Oct 2, 2008

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

import java.util.Set;

/**
 * A device which can be used to produce a recording.
 * 
 * 
 */
public interface DeviceType {

  /**
   * Returns the internal id. This is id does not have any business meanings.
   */
  Long getId();

  /**
   * Returns the unique device name.
   */
  String getName();

  /**
   * Sets the device name. Must be unique in the system's context.
   */
  void setName(String name);

  /**
   * Returns an optional description of the device.
   */
  String getDescription();

  /**
   * Set the description.
   */
  void setDescription(String description);

  /**
   * Returns the capabilities of this device.
   */
  Set<String> getCapabilities();

  /**
   * Sets the capabilities of this device.
   */
  void setCapabilities(Set<String> capabilities);
}
