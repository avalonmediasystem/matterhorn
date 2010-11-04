/*
 
 Semester.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 09 15, 2009

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
import java.util.Date;

/**
 * Represents a WsSemester SOAP object.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Entity
public class Semester {

    @Id
    private String semkez;

    private Date semesterbeginn;
    private Date semesterende;
    private Date semestermitte;
    private Date semesterintervallende;

    public String getSemkez() {
        return semkez;
    }

    public Date getSemesterbeginn() {
        return semesterbeginn;
    }

    public Date getSemesterende() {
        return semesterende;
    }

    public Date getSemestermitte() {
        return semestermitte;
    }

    public Date getSemesterintervallende() {
        return semesterintervallende;
    }

    // --
   
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Semester)) return false;

        Semester semester = (Semester) o;

        if (!semkez.equals(semester.semkez)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return semkez.hashCode();
    }

    @Override
    public String toString() {
        return semkez + " " + semesterbeginn + " - " + semesterende;
    }
}
