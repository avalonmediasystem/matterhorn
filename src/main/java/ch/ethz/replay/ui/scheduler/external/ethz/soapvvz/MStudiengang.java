/*
 
 MStudiengang.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Oct 13, 2009

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
import javax.persistence.Column;
import javax.persistence.Id;

/**
 * Mapping table to get additional metadata.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Entity(name = "m_studiengang")
public class MStudiengang {

    @Id
    private String leitzahl;

    @Column(name = "departement_de")
    private String departementDe;

    @Column(name = "departement_en")
    private String departementEn;

    @Column(name = "typ_de")
    private String typDe;

    @Column(name = "typ_en")
    private String typEn;

    public String getLeitzahl() {
        return leitzahl;
    }

    public String getDepartementDe() {
        return departementDe;
    }

    public String getDepartementEn() {
        return departementEn;
    }

    public String getTypDe() {
        return typDe;
    }

    public String getTypEn() {
        return typEn;
    }
}
