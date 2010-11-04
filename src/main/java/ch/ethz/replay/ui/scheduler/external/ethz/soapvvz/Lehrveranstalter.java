/*
 
 Lehrveranstalter.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 09 13, 2009

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

import javax.persistence.*;

/**
 * Represents a WsLehrveranstalter SOAP object.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Entity
public class Lehrveranstalter {

    @Id
    private Integer id;

    @Column(name = "doz_id")
    private Integer dozId;
    private String name;
    private String vorname;
    private Boolean hauptverantwortlicher;
    private Integer typ;

    @ManyToOne()
    @JoinColumn(name = "lv_id")
    private Lehrveranstaltung lehrveranstaltung;

    public Integer getId() {
        return id;
    }

    public Integer getDozId() {
        return dozId;
    }

    public String getName() {
        return name;
    }

    public String getVorname() {
        return vorname;
    }

    public Boolean isHauptverantwortlicher() {
        return hauptverantwortlicher;
    }

    public Integer getTyp() {
        return typ;
    }

    public Lehrveranstaltung getLehrveranstaltung() {
        return lehrveranstaltung;
    }

    // --

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lehrveranstalter)) return false;

        Lehrveranstalter that = (Lehrveranstalter) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
