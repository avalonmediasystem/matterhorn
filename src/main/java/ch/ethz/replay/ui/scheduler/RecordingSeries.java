/*
 
 RecordingSeries.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 9 29, 2008

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
 * {@link Recording Recordings} may be grouped.
 * 
 * 
 */
public interface RecordingSeries {

  /**
   * Returns the internal ID. This id is a surrogate key and does not have any business meanings, it's sole purpose is
   * to store and manage recordings.
   */
  Long getId();

  /**
   * Returns the series ID, which shall be a natural identifier. This may be the ID of an associated lecture series for
   * example.
   */
  String getSeriesId();

  /**
   * Returns the {@link Recording}s grouped by this series.
   */
  Set<Recording> getRecordings();

  /**
   * Adds a new recording to the series. This also sets the dependency on the Recording side. Adding the same recording
   * more than once shall not matter.
   */
  void addRecording(Recording recording);

  /**
   * Removes the recording from the series.
   */
  void removeRecording(Recording recording);

  /**
   * Checks if the recording is part of the series.
   */
  boolean containsRecording(Recording recording);

  /**
   * Returns the event with the earliest start date. May be null if the series does not have any events.
   */
  Recording getFirst();

  /**
   * Returns the event with the latest end date. May be null if the series does not have any events.
   */
  Recording getLast();

  /**
   * Returns the number of recordings that are already bygone.
   * 
   * @see Recording#isBygone()
   */
  int getBygoneCount();

  /**
   * Checks if the whole series (i.e. all recordings) is bygone.
   */
  boolean isBygone();

}
