/*
 
 EventImpl.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Sep 15, 2008

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

import ch.ethz.replay.ui.scheduler.Event;
import ch.ethz.replay.ui.scheduler.EventSeries;
import ch.ethz.replay.ui.scheduler.Recording;
import ch.ethz.replay.ui.scheduler.external.ethz.soapvvz.Belegung;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.Transient;

/**
 * Todo implement
 * 
 * 
 */
@Entity(name = "Event")
public class EventImpl extends BaseEntity implements Event {

  // This is the ID of the Lehrveranstaltung
  @Column(name = "eventid")
  private String eventId;

  @Transient
  private Belegung belegung;

  public EventImpl() {
  }

  public EventImpl(Belegung belegung) {
    setEventId(belegung.getLehrveranstaltung().getId());
    this.belegung = belegung;
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(Integer eventId) {
    this.eventId = Integer.toString(eventId);
  }

  public Belegung getBelegung() {
    return belegung;
  }

  /**
   * Todo implement.
   */
  public EventSeries getSeries() {
    return null;
  }

  /**
   * Todo implement.
   */
  public Recording getAssociatedRecording() {
    return null;
  }
}
