/*

 BaseEntity.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 28, 2008

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

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

/**
 * Base class for entities using database generated ids and optimistic locking via a version column.
 * 
 * 
 */
@MappedSuperclass
public abstract class BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  protected Long id;

  @Version
  protected int versionNr;

  /**
   * Returns the id of the entity. Is null prior to make persistent.
   * 
   * @return the database id or null if unsaved
   * @see #isNew()
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }
  
  /**
   * Checks if this entity is new, which means that it is not persisent yet.
   * 
   * @see #getId()
   */
  public boolean isNew() {
    return id == null;
  }
}
