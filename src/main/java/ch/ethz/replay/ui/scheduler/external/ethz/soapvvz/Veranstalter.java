/*
 
 Veranstalter.java
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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

/**
 * Represents a WsVeranstalter SOAP object.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Entity
public class Veranstalter {

    @Id
    private Integer id;
    private String leitzahl;
    private String name;

    @ManyToOne
    @JoinColumn(name = "lv_id")
    private Lehrveranstaltung lehrveranstaltung;

    public Integer getId() {
        return id;
    }

    public String getLeitzahl() {
        return leitzahl;
    }

    public String getName() {
        return name;
    }

    public Lehrveranstaltung getLehrveranstaltung() {
        return lehrveranstaltung;
    }

    // --

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Veranstalter)) return false;

        Veranstalter that = (Veranstalter) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        return result;
    }
}
