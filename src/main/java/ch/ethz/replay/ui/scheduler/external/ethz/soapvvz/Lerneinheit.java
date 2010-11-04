/*
 
 Lerneinheit.java
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
import javax.persistence.OneToMany;
import javax.persistence.Column;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Represents a WsLerneinheit SOAP object.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Entity
public class Lerneinheit {
    @Id
    private Integer id;

    @Column(name = "lk_einheit_id")
    private Integer LKEinheitID;
    private String code;
    private String semkez;

    @Column(name = "diploma_supplement")
    private String diplomaSupplement;

    @Column(name = "diploma_supplement_en")
    private String diplomaSupplementEnglisch;

    private String inhalt;

    @Column(name = "inhalt_en")
    private String inhaltEnglisch;

    private String titel;

    @Column(name = "titel_en")
    private String titelEnglisch;

    @OneToMany(mappedBy = "lerneinheit")
    private Set<Lehrveranstaltung> lehrveranstaltungen = new HashSet<Lehrveranstaltung>();

    // --

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLKEinheitID() {
        return LKEinheitID;
    }

    public void setLKEinheitID(Integer LKEinheitID) {
        this.LKEinheitID = LKEinheitID;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSemkez() {
        return semkez;
    }

    public void setSemkez(String semkez) {
        this.semkez = semkez;
    }

    public String getDiplomaSupplement() {
        return diplomaSupplement;
    }

    public void setDiplomaSupplement(String diplomaSupplement) {
        this.diplomaSupplement = diplomaSupplement;
    }

    public String getDiplomaSupplementEnglisch() {
        return diplomaSupplementEnglisch;
    }

    public void setDiplomaSupplementEnglisch(String diplomaSupplementEnglisch) {
        this.diplomaSupplementEnglisch = diplomaSupplementEnglisch;
    }

    public String getInhalt() {
        return inhalt;
    }

    public void setInhalt(String inhalt) {
        this.inhalt = inhalt;
    }

    public String getInhaltEnglisch() {
        return inhaltEnglisch;
    }

    public void setInhaltEnglisch(String inhaltEnglisch) {
        this.inhaltEnglisch = inhaltEnglisch;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public String getTitelEnglisch() {
        return titelEnglisch;
    }

    public void setTitelEnglisch(String titelEnglisch) {
        this.titelEnglisch = titelEnglisch;
    }

    public Set<Lehrveranstaltung> getLehrveranstaltungen() {
        return lehrveranstaltungen;
    }

    /**
     * Return only Lehrveranstaltungen of a certain type.
     */
    public Set<Lehrveranstaltung> getLehrveranstaltungen(Lehrveranstaltung.Typ typ) {
        Set<Lehrveranstaltung> rs = new HashSet<Lehrveranstaltung>();
        for (Lehrveranstaltung lv : lehrveranstaltungen) {
            if (lv.getTyp() == typ) {
                rs.add(lv);
            }
        }
        return rs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lerneinheit)) return false;

        Lerneinheit that = (Lerneinheit) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
