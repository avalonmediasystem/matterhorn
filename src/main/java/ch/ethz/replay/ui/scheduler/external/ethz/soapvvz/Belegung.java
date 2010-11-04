/*
 
 Belegung.java
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

import org.joda.time.LocalTime;

import javax.persistence.*;
import java.util.Date;

import ch.ethz.replay.core.common.bundle.dublincore.ETHZMetadataCodec;

/**
 * Represents a WsBelegungsSerieOrt SOAP object.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Entity
public class Belegung {

    @Id
    private Integer id;
    private String areal;
    private String region;

    @Column(name = "datum_von")
    private Date datumVon;

    @Column(name = "datum_bis")
    private Date datumBis;
    private String gebaeude;
    private String geschoss;
    private String raum;

    @Column(name = "ort_typ")
    private Integer ortTyp;

    @Column(name = "ort_id")
    private Integer ortId;

    private Integer periodizitaet;

    @Column(name = "semester_bezugstyp")
    private Integer semesterBezugstyp;
    private String semkez;
    private Integer typ;

    @Column(name = "uhrzeit_von")
    private String uhrzeitVon;

    @Column(name = "uhrzeit_bis")
    private String uhrzeitBis;

    @Column(name = "wochendelta_beginn")
    private Integer wochendeltaBeginn;

    @Column(name = "wochendelta_ende")
    private Integer wochendeltaEnde;

    private Integer wochentag;

    @ManyToOne
    @JoinColumn(name = "lv_id")
    private Lehrveranstaltung lehrveranstaltung;

    // --

    public enum OrtTyp {

        Region, Areal, Gebaeude, ExtGebaeude, Geschoss, Raum, ExtRaum;

        public static OrtTyp fromCode(Integer code) {
            if (code == 1) return Region;
            if (code == 2) return Areal;
            if (code == 3) return Gebaeude;
            if (code == 4) return ExtGebaeude;
            if (code == 5) return Geschoss;
            if (code == 6) return Raum;
            if (code == 7) return ExtRaum;
            throw new IllegalArgumentException("Unknown code " + code);
        }
    }

    // --

    public Integer getId() {
        return id;
    }

    public String getAreal() {
        return areal;
    }

    public String getRegion() {
        return region;
    }

    public Date getDatumVon() {
        return datumVon;
    }

    public Date getDatumBis() {
        return datumBis;
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

    /**
     * Cumulation of "Gebaeude GeschossRaum". May be "" if all spatial information is null.
     */
    public String getLocation() {
        return ETHZMetadataCodec.encodeRoom(getGebaeude(), getGeschoss(), getRaum());
    }

    public OrtTyp getOrtTyp() {
        return OrtTyp.fromCode(ortTyp);
    }

    public Integer getOrtId() {
        return ortId;
    }

    public Integer getPeriodizitaet() {
        return periodizitaet;
    }

    public Integer getSemesterBezugstyp() {
        return semesterBezugstyp;
    }

    public String getSemkez() {
        return semkez;
    }

    public Integer getTyp() {
        return typ;
    }

    public LocalTime getUhrzeitVon() {
        return toLocalTime(uhrzeitVon);
    }

    public LocalTime getUhrzeitBis() {
        return toLocalTime(uhrzeitBis);
    }

    public Integer getWochendeltaBeginn() {
        return wochendeltaBeginn;
    }

    public Integer getWochendeltaEnde() {
        return wochendeltaEnde;
    }

    /**
     * Returns the day of week with Monday being the first and Sunday being the seventh day. This encoding
     * is equal to Jode time's.
     */
    public Integer getWochentag() {
        return wochentag;
    }

    public Lehrveranstaltung getLehrveranstaltung() {
        return lehrveranstaltung;
    }

    private LocalTime toLocalTime(String time) {
        String[] p = time.split(":");
        return new LocalTime(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
    }

    // --

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Belegung)) return false;

        Belegung that = (Belegung) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
