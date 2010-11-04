/*
 
 Suchindex.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 09 14, 2009

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

package ch.ethz.replay.ui.scheduler.external.ethz.soapvvz;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Entity class for the VVZ-Suchindex.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Entity
public class Suchindex {

    @Id
    private Integer id;

    @Column(name = "le_id")
    private Integer leId;

    @Column(name = "lv_id")
    private Integer lvId;

    @Column(name = "lv_typ")
    private String lvTyp;

    private String name;
    private String vorname;
    private String titel;
    private String gebaeude;
    private String geschoss;
    private String raum;
    private String summary;

    // --

    public Integer getLerneinheitId() {
        return leId;
    }

    public String getName() {
        return name;
    }

    public String getVorname() {
        return vorname;
    }

    public String getTitel() {
        return titel;
    }

    public String getGebaeude() {
        return gebaeude;
    }

    public String getGeschoss() {
        return geschoss;
    }

    public String getRaum() {
        return raum;
    }

    public Integer getLehrveranstaltungId() {
        return lvId;
    }

    public Lehrveranstaltung.Typ getLehrveranstaltungTyp() {
        return Lehrveranstaltung.Typ.fromCode(lvTyp);
    }

    /** Name and surname combined. */
    public String getCompleteName() {
        return name + ", " + vorname;
    }

    /**
     * Cumulation of "Gebaeude GeschossRaum". May be "" if all spatial information is null.
     */
    public String getLocation() {
        return Util.roomName(this);
    }
    
    @Override
    public String toString() {
        return "[" + leId + "] " + summary;
    }
}
