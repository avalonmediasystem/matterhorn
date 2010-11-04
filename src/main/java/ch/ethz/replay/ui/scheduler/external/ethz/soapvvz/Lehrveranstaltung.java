/*
 
 Lehrveranstaltung.java
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
import java.util.*;

/**
 * Represents a WsLehrveranstaltung SOAP object.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Entity
public class Lehrveranstaltung {

    @Id
    private Integer id;

    @Column(name = "findet_statt")
    private Integer findetStatt;
    private String lehrsprache;
    private Integer periodizitaet;

    @Column(name = "semester_bezug")
    private Integer semesterBezug;
    private String semkez;

    @Column(name = "service_typ")
    private Integer serviceTyp;

    private String titel;
    private String typ;

    @ManyToOne
    @JoinColumn(name = "le_id")
    private Lerneinheit lerneinheit;

    @OneToMany(mappedBy = "lehrveranstaltung")
    private Set<Lehrveranstalter> lehrveranstalter = new HashSet<Lehrveranstalter>();

    @OneToMany(mappedBy = "lehrveranstaltung")
    private Set<Veranstalter> veranstalter = new HashSet<Veranstalter>();

    @OneToMany(mappedBy = "lehrveranstaltung")
    private Set<Belegung> belegung = new HashSet<Belegung>();

    // --

    public enum Typ {

        SelbststaendigeArbeit {
            public String getCode() {
                return "A";
            }},
        Diplomarbeit {
            public String getCode() {
                return "D";
            }},
        VorlesungMitUebung {
            public String getCode() {
                return "G";
            }},
        Kolloquium {
            public String getCode() {
                return "K";
            }},
        Praktikum {
            public String getCode() {
                return "P";
            }},
        Repetitorium {
            public String getCode() {
                return "R";
            }},
        Seminar {
            public String getCode() {
                return "S";
            }},
        Uebung {
            public String getCode() {
                return "U";
            }},
        Vorlesung {
            public String getCode() {
                return "V";
            }};

        public abstract String getCode();

        /**
         * @throws IllegalArgumentException if code is unknown
         */
        public static Typ fromCode(String code) {
            for (Typ t : values()) {
                if (t.getCode().equals(code)) return t;
            }
            throw new IllegalArgumentException("Unknown code " + code);
        }
    }

    public enum SemesterBezug {

        Kein, Ganz, Haelfte1, Haelfte2;

        public SemesterBezug fromCode(Integer code) {
            if (code >= 0 && code <= values().length) return values()[code];
            throw new IllegalArgumentException("Code must be between 0 and " + values().length);
        }
    }

    // --

    public Integer getId() {
        return id;
    }

    public Integer getFindetStatt() {
        return findetStatt;
    }

    public String getLehrsprache() {
        return lehrsprache;
    }

    public Integer getPeriodizitaet() {
        return periodizitaet;
    }

    public Integer getSemesterBezug() {
        return semesterBezug;
    }

    public String getSemkez() {
        return semkez;
    }

    public Integer getServiceTyp() {
        return serviceTyp;
    }

    public String getTitel() {
        return titel;
    }

    public Typ getTyp() {
        return Typ.fromCode(typ);
    }

    public Lerneinheit getLerneinheit() {
        return lerneinheit;
    }

    public Set<Lehrveranstalter> getLehrveranstalter() {
        return Collections.unmodifiableSet(lehrveranstalter);
    }

    public Set<Veranstalter> getVeranstalter() {
        return Collections.unmodifiableSet(veranstalter);
    }

    public Set<Belegung> getBelegung() {
        return Collections.unmodifiableSet(belegung);
    }

    // --

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lehrveranstaltung)) return false;

        Lehrveranstaltung that = (Lehrveranstaltung) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
