/*
 
 Location.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Aug 26, 2008

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
 * Describes the location where a {@link Recording} can be scheduled.
 * 
 * 
 */
public interface Location {

  /**
   * Returns the internal id. This id is a surrogate identifier and does not have any business meanings.
   */
  Long getId();

  /**
   * Returns the unique name of the location, which acts as a natural identifier.
   * <p/>
   * <strong>Note:</strong> Location names may be subject to localization, e.g. "Audimax" should be translated into
   * "Lecture Hall" for english users. Use this name as a fallback or when you do not intend to localize location names.
   */
  String getName();

  /**
   * Returns a list of devices available for recordings at this location. Returns an empty list if no devices are
   * present there. Recordings cannot be scheduled at device-less locations.
   */
  Set<DeviceType> getDevices();
}
