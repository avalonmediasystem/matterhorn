/*
 
 LectureCourse.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 10 10, 2008

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

package ch.ethz.replay.ui.scheduler.external.ethz;

import ch.ethz.replay.core.api.common.bundle.DublinCoreCatalog;
import static ch.ethz.replay.core.common.util.CollectionSupport.getOrCreate;
import static ch.ethz.replay.core.common.util.CollectionSupport.merge;
import ch.ethz.replay.core.common.util.CollectionSupport;
import ch.ethz.replay.ui.scheduler.Person;
import ch.ethz.replay.ui.scheduler.Recording;
import ch.ethz.replay.ui.scheduler.RecordingSeries;
import ch.ethz.replay.ui.scheduler.Location;
import ch.ethz.replay.ui.scheduler.external.ethz.soapvvz.Semester;
import ch.ethz.replay.ui.scheduler.external.ethz.soapvvz.Belegung;

import java.util.*;

/**
 * LectureCourse bundles information about a course fetched from the VVZ database.
 *
 * @see VvzConnector
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class LectureCourse {

    private String courseId;
    private Semester semester;
    private RecordingSeries series;
    private List<Person> contactPersons = new ArrayList<Person>();

    private DublinCoreCatalog dublinCoreDefaults;
    private Recording recordingDefaults;

    /**
     * Bean constructor.
     */
    public LectureCourse() {
    }

    public LectureCourse(String courseId, Semester semester,
                         RecordingSeries series,
                         Recording recordingDefaults, DublinCoreCatalog dublinCoreDefaults) {
        this.courseId = courseId;
        this.semester = semester;
        this.series = series;
        this.recordingDefaults = recordingDefaults;
        this.dublinCoreDefaults = dublinCoreDefaults;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public DublinCoreCatalog getDublinCoreDefaults() {
        return dublinCoreDefaults;
    }

    public void setDublinCoreDefaults(DublinCoreCatalog dublinCoreDefaults) {
        this.dublinCoreDefaults = dublinCoreDefaults;
    }

    public Recording getRecordingDefaults() {
        return recordingDefaults;
    }

    public void setRecordingDefaults(Recording recordingDefaults) {
        this.recordingDefaults = recordingDefaults;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    /**
     * Shorthand for {@link #getSeries()}.{@link RecordingSeries#getRecordings() getRecordings(}.
     */
    public Set<Recording> getRecordings() {
        return Collections.unmodifiableSet(series.getRecordings());
    }

    /**
     * Groups recordings by location ID and sorts after start time ascending.
     */
    public List<Recording> getRecordingsGrouped() {
            Map<Location, List<Recording>> g = new HashMap<Location, List<Recording>>();
            // Group
            for (Recording r :  getSeries().getRecordings()) {
                getOrCreate(g, r.getLocation(), new CollectionSupport.Creator<List<Recording>>() {
                    public List<Recording> create() {
                        return new ArrayList<Recording>();
                    }
                }).add(r);
            }
            // Sort
            for (List<Recording> r : g.values()) {
                Collections.sort(r, RecordingByStartComp.OBJECT);
            }
            // Merge all lists
            return merge(g);

    }

    public List<Person> getContactPersons() {
        return contactPersons;
    }

    public void setContactPersons(List<Person> contactPersons) {
        this.contactPersons = contactPersons;
    }

    public RecordingSeries getSeries() {
        return series;
    }

    private static class RecordingByStartComp implements Comparator<Recording> {

        private static RecordingByStartComp OBJECT = new RecordingByStartComp();

        public int compare(Recording r1, Recording r2) {
            if (r1.getStartDate().before(r2.getStartDate()))
                return -1;
            if (r1.getStartDate().after(r2.getStartDate()))
                return 1;
            return 0;
        }
    }
}
