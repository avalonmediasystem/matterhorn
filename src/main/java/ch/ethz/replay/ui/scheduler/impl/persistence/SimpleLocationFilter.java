/*
 
 SimpleLocationFilter.java
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

/**
 * A filter to search for {@link ch.ethz.replay.ui.scheduler.Location}s
 * 
 * 
 */
public class SimpleLocationFilter {

  private String name;
  private Integer maxResults;
  private boolean exactMatch = false;

  /**
   * Creates a new filter with <code>exactMatch</code> set to false.
   */
  public SimpleLocationFilter() {
  }

  /**
   * Creates a new filter with the given properties and <code>exactMatch</code> set to false.
   */
  public SimpleLocationFilter(String name, Integer maxResults) {
    this.name = name;
    this.maxResults = maxResults;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getMaxResults() {
    return maxResults;
  }

  public void setMaxResults(Integer maxResults) {
    this.maxResults = maxResults;
  }

  public boolean isExactMatch() {
    return exactMatch;
  }

  public void setExactMatch(boolean exactMatch) {
    this.exactMatch = exactMatch;
  }
}
