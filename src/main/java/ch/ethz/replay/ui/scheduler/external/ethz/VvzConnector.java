/*
 
 VvzConnector.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 9 15, 2008

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
import ch.ethz.replay.core.api.common.metadata.dublincore.DublinCoreValue;
import ch.ethz.replay.core.common.bundle.dublincore.ETHZDublinCore;
import ch.ethz.replay.core.common.util.CollectionSupport;
import ch.ethz.replay.core.common.util.StringSupport;
import static ch.ethz.replay.core.common.util.StringSupport.isEmpty;
import static ch.ethz.replay.core.common.util.StringSupport.notEmpty;
import ch.ethz.replay.ui.common.util.ReplayBuilder;
import ch.ethz.replay.ui.common.util.dao.GenericDaoException;
import ch.ethz.replay.ui.scheduler.*;
import ch.ethz.replay.ui.scheduler.external.ethz.soapvvz.*;
import ch.ethz.replay.ui.scheduler.impl.DeviceTypeImpl;
import ch.ethz.replay.ui.scheduler.impl.EventImpl;
import ch.ethz.replay.ui.scheduler.impl.RecordingImpl;
import ch.ethz.replay.ui.scheduler.impl.RecordingSeriesImpl;
import ch.ethz.replay.ui.scheduler.impl.persistence.DeviceTypeDao;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service to access the VVZ database.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Repository
public class VvzConnector extends HibernateDaoSupport {

    private static final Logger Log = Logger.getLogger(VvzConnector.class);

    /**
     * Maps VVZ values, 1 = Monday, 2, Thursday to Java
     */
    private static final int[] WEEKDAYS = {-1,
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
            Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY};

    private Schedule schedule;
    private LehrveranstaltungDAO lehrveranstaltungDAO;
    private SuchindexDAO suchindexDAO;
    private SemesterDAO semesterDAO;
    private MStudiengangDAO studiengangDAO;
    private MRegionDAO regionDAO;
    private DeviceTypeDao deviceTypeDao;
    private Map<String, String> dublinCoreDefaults = new HashMap<String, String>();

    private static final Locale EN = Locale.ENGLISH;
    private static final Locale DE = Locale.GERMAN;

    // --

    @Transactional(readOnly = true)
    public List<Suchindex> query(String query) {
        return suchindexDAO.findBySummary(query);
    }

    /**
     * Creates a lecture course for a given Lehrveranstaltung.
     */
    @Transactional(readOnly = true)
    public LectureCourse fromLehrveranstaltungID(Integer id) {
        // Get Lehrveranstaltung
        Lehrveranstaltung lehrveranstaltung = lehrveranstaltungDAO.get(id);
        // Get Semester
        Semester semester = semesterDAO.get(lehrveranstaltung.getSemkez());
        if (semester == null) {
            throw new GenericDaoException("databaseOutdated",
                    "No semester information can be found in the VVZ mirror database - " +
                            "cannot calculate room usage times. Please update the VVZ mirror database from " +
                            "the SOAP service.");
        }

        // Room schedule
        RecordingSeriesImpl series = (RecordingSeriesImpl) createSchedule(semester, lehrveranstaltung);

        // Series Dublin Core
        if (series.isNew())
            series.setDublinCore(createSeriesDC(series, lehrveranstaltung));

        // Episode Dublin Cores
        for (Recording rec : series.getRecordings()) {
            if (((RecordingImpl) rec).isNew())
                rec.setDublinCore(createEpisodeDC(rec, lehrveranstaltung));
        }

        // todo Use of RecordingImpl() constructor is prohibited: Find a better solution on the API level
        LectureCourse course = new LectureCourse(lehrveranstaltung.getLerneinheit().getCode(), semester, series,
                new RecordingImpl(), ReplayBuilder.createDublinCoreCatalog());

        return course;
    }

    private DublinCoreCatalog createEpisodeDC(Recording rec, Lehrveranstaltung lv) {
        ETHZDublinCore dc = createDublinCore();
        // [dcterms:identifier]
        dc.setDCIdentifier(rec.getBundleId().getFullName());
        // [dcterms:audience] & dcterms.contributor
        setAudienceContributor(lv, dc);
        // [dcterms:available]
        //   todo
        // [dcterms:created]
        dc.setCreated(rec.getStartDate());
        // [dcterms:creator]
        for (Lehrveranstalter doz : sort(lv.getLehrveranstalter())) {
            dc.addCreator(doz.getVorname(), doz.getName());
        }
        // [dcterms:isPartOf]
        if (rec.getSeries() != null && rec.getSeries().getDublinCore() != null) {
            dc.setIsPartOf(((ETHZDublinCore) rec.getSeries().getDublinCore()).getDCIdentifier());
        }
        // [dcterms:isReplacedBy]
        //   not set here
        // [dcterms:issued]
        dc.setIssued(new DateTime(rec.getStartDate().getTime()).plusDays(1).toDate());
        // [dcterms:language]
        dc.setLanguage(new Locale(lv.getLehrsprache()).getISO3Language());
        // [dcterms:spatial]
        dc.addSpatial(rec.getLocation().getName());
        // [dcterms:spatial]
        String region = ((EventImpl) rec.getAssociatedEvent()).getBelegung().getRegion();
        if (notEmpty(region)) {
            MRegion r = regionDAO.get(region);
            if (r != null) {
                dc.addSpatial(r.getNameDe(), DE);
                dc.addSpatial(r.getNameEn(), EN);
            } else {
                dc.addSpatial(region, DE);
                dc.addSpatial(region, EN);
            }
        }
        // [dcterms:subject]
        //   todo SHIS
        // [dcterms:temporal]
        dc.setTemporal(rec.getStartDate(), rec.getEndDate());
        // [dcterms:title]
        //   original title gets no language code
        dc.setOriginalTitle(lv.getLerneinheit().getTitel());
        if (StringSupport.notEmpty(lv.getLerneinheit().getTitelEnglisch()))
            dc.setEnglishTitle(lv.getLerneinheit().getTitelEnglisch());
        // [ethz:EventNumber]
        dc.setEventNumber(lv.getLerneinheit().getCode());

        // Defaults

        if (dublinCoreDefaults.containsKey("episode.type"))
            dc.setTypeCombined(dublinCoreDefaults.get("episode.type").trim());
        if (dublinCoreDefaults.containsKey("episode.advertised"))
            dc.setAdvertised(Boolean.parseBoolean(dublinCoreDefaults.get("episode.advertised").trim()));
        // [dcterms:license]
        dc.setLicenseDE(ETHZDublinCore.CREATIVE_COMMONS_LICENSE);
        dc.setLicenseEN(ETHZDublinCore.CREATIVE_COMMONS_LICENSE);

        return dc;
    }

    /**
     * Handle the series Dublin Core document.
     */
    private DublinCoreCatalog createSeriesDC(RecordingSeries series, Lehrveranstaltung lv) {
        ETHZDublinCore dc = createDublinCore();
        // [dcterms:identifier]
        dc.setDCIdentifier(createSeriesID(lv));
        // [dcterms:audience & dcterms.contributor]
        setAudienceContributor(lv, dc);
        // [dcterms:available]
        try {
            dc.setAvailable(new DateTime(series.getFirst().getStartDate().getTime()).plusDays(1).toDate());
        } catch (NullPointerException ignore) {
        }
        // [dcterms:created]
        try {
            dc.setCreated(series.getFirst().getStartDate(), series.getLast().getEndDate());
        } catch (NullPointerException ignore) {
        }
        // [dcterms:creator]
        for (Lehrveranstalter doz : sort(lv.getLehrveranstalter())) {
            dc.addCreator(doz.getVorname(), doz.getName());
        }
        // [dcterms:description]
        if (!fillInDescription(dc, lv.getLerneinheit().getInhalt(), lv.getLerneinheit().getInhaltEnglisch())) {
            fillInDescription(dc, lv.getLerneinheit().getDiplomaSupplement(), lv.getLerneinheit().getDiplomaSupplementEnglisch());
        }
        // [dcterms:isReplacedBy]
        // [dcterms:issued]
        if (series.getRecordings().size() > 0)
            dc.setIssued(new DateTime(series.getFirst().getStartDate().getTime()).plusDays(1).toDate());
        // [dcterms:language]
        dc.setLanguage(new Locale(lv.getLehrsprache()).getISO3Language());
        // [dcterms:modified]
        // todo
        // [dcterms:replaces]
        // todo
        // [dcterms:subject]
        // todo SHIS
        // [dcterms:temporal]
        // todo spec says to name this period with Semkez. What about english? What about a language attribute?
        if (series.getRecordings().size() > 0)
            dc.setTemporal(series.getFirst().getStartDate(), series.getLast().getEndDate());
        // [dcterms:title]
        dc.setOriginalTitle(lv.getLerneinheit().getTitel());
        if (StringSupport.notEmpty(lv.getLerneinheit().getTitelEnglisch()))
            dc.setEnglishTitle(lv.getLerneinheit().getTitelEnglisch());
        // [ethz:EventNumber]
        dc.setEventNumber(lv.getLerneinheit().getCode());

        // Defaults

        if (dublinCoreDefaults.containsKey("series.type"))
            dc.setTypeCombined(dublinCoreDefaults.get("series.type").trim());
        if (dublinCoreDefaults.containsKey("series.advertised"))
            dc.setAdvertised(Boolean.parseBoolean(dublinCoreDefaults.get("series.advertised").trim()));
        // [dcterms:license]
        dc.setLicenseDE(ETHZDublinCore.CREATIVE_COMMONS_LICENSE);
        dc.setLicenseEN(ETHZDublinCore.CREATIVE_COMMONS_LICENSE);
        // [dcterms:spatial]
        dc.addSpatial("Zürich, Schweiz", DE);
        dc.addSpatial("Zurich, Switzerland", EN);

        return dc;
    }

    private String createSeriesID(Lehrveranstaltung lv) {
        // Lerneinheit codes are not unique over time so we add the semkez to it
        return lv.getLerneinheit().getCode() + "-" + lv.getSemkez();
    }

    /**
     * Create a new ETHZ Dublin Core.
     */
    private ETHZDublinCore createDublinCore() {
        ETHZDublinCore dc;
        try {
            dc = (ETHZDublinCore) ReplayBuilder.createDublinCoreCatalog();
        } catch (ClassCastException e) {
            throw new RuntimeException("Only ETHZDublinCore implementations are supported");
        }
        return dc;
    }

    private void setAudienceContributor(Lehrveranstaltung lv, ETHZDublinCore dc) {
        for (Veranstalter v : lv.getVeranstalter()) {
            MStudiengang sg = studiengangDAO.get(v.getLeitzahl());
            if (sg != null) {
                // dcterms:audience
                if (notEmpty(sg.getTypDe())) dc.addAudience(sg.getTypDe(), DE);
                if (notEmpty(sg.getTypEn())) dc.addAudience(sg.getTypEn(), EN);
                // dcterms:contributor
                if (notEmpty(sg.getDepartementDe())) dc.addContributor(sg.getDepartementDe(), DE);
                if (notEmpty(sg.getDepartementEn())) dc.addContributor(sg.getDepartementEn(), EN);
            }
        }
    }

    /**
     * Sort lehrveranstalter. Hauptverantwortlicher first, then all others in alphabetical order.
     */
    private List<Lehrveranstalter> sort(Set<Lehrveranstalter> lehrveranstalter) {
        List<Lehrveranstalter> l = new ArrayList<Lehrveranstalter>(lehrveranstalter);
        Collections.sort(l, LehrveranstalterComparator.Obj);
        return l;
    }

    private boolean fillInDescription(ETHZDublinCore dc, String original, String english) {
        if (isEmpty(original) && isEmpty(english)) {
            return false;
        }
        if (notEmpty(original)) {
            dc.setOriginalDescription(original);
            if (notEmpty(english))
                dc.setEnglishDescription(english);
        } else {
            // No "original" field is present so use the english translation
            dc.setOriginalDescription(english);
        }
        return true;
    }

    /**
     * Create the schedule for a given Lehrveranstaltung.
     */
    private RecordingSeries createSchedule(Semester semester, Lehrveranstaltung lv) {
        // First try to find an already existing series, then, if none exists create a new one
        RecordingSeries series = schedule.getOrCreateRecordingSeries(createSeriesID(lv));

        //EventSeries eventSeries = schedule.getOrCreateEventSeries(lv.getLerneinheit().getCode());
        //series.setAssociatedEventSeries(eventSeries);

        // End of semester
//        Calendar semesterEnd = Calendar.getInstance();
//        semesterEnd.setTime(semester.getSemesterende());
//        moveToEndOfDay(semesterEnd);

        // Iterate rooms, where this lecture course takes place
        for (Belegung belegung : lv.getBelegung()) {
            // Calculate start time of very first lecture in the semester
            Calendar lectureStartTime = Calendar.getInstance();
            lectureStartTime.setTime(belegung.getDatumVon());
            lectureStartTime.add(Calendar.WEEK_OF_YEAR, belegung.getWochendeltaBeginn());
            moveTo(lectureStartTime, belegung.getUhrzeitVon(), belegung.getWochentag());
            if (lectureStartTime.getTime().before(semester.getSemesterbeginn())) {
                // If semester doesn't start at the first weekday, go to the next week
                lectureStartTime.add(Calendar.WEEK_OF_YEAR, 1);
            }

            // Calculate end time of very first lecture in the semester
            Calendar lectureEndTime = (Calendar) lectureStartTime.clone();
            moveTo(lectureEndTime, belegung.getUhrzeitBis(), belegung.getWochentag());

            // Calculate actual end of semester
            Calendar lectureSemesterEnd = Calendar.getInstance();
            lectureSemesterEnd.setTime(belegung.getDatumBis());
            if (belegung.getWochendeltaEnde() > 0) {
                lectureSemesterEnd.add(Calendar.WEEK_OF_YEAR, belegung.getWochendeltaEnde() * -1);
            }

            // Calculate all lecture dates and create a recording for each
            while (!lectureStartTime.after(lectureSemesterEnd)) {
                // Create new recording
                Location location = schedule.getOrCreateLocation(toRecordingLocation(belegung.getLocation()));
                final Recording recording = schedule.newRecording(location, lectureStartTime.getTime(), lectureEndTime.getTime());
                // Check if there is already a recording for the same location and time.
                Recording existing = CollectionSupport.find(series.getRecordings(), new CollectionSupport.Predicate<Recording>() {
                    public boolean evaluate(Recording rec, int index) {
                        int timeDelta = 10 * 60000;
                        // Detect equality of recordings
                        return rec.getLocation().equals(recording.getLocation()) &&
                                Math.abs(rec.getStartDate().getTime() - recording.getStartDate().getTime()) <= timeDelta &&
                                Math.abs(rec.getEndDate().getTime() - recording.getEndDate().getTime()) <= timeDelta;
                    }
                });
                if (existing == null) {
                    setDeviceDefaults(recording);
                    recording.associateWith(new EventImpl(belegung));
                    // Add recording
                    series.addRecording(recording);
                } else {
                    existing.associateWith(new EventImpl(belegung));
                }

                // On to next week
                lectureStartTime.add(Calendar.WEEK_OF_YEAR, 1);
                lectureEndTime.add(Calendar.WEEK_OF_YEAR, 1);
            }
        }

        return series;
    }

    /**
     * Set default devices for a recording.
     */
    private void setDeviceDefaults(Recording r) {
        if (dublinCoreDefaults.containsKey("recording.device")) {
            Set<DeviceType> deviceTypes = new HashSet<DeviceType>();
            for (String device : dublinCoreDefaults.get("recording.device").split(",")) {
                deviceTypes.addAll(deviceTypeDao.findByExample(new DeviceTypeImpl(device.trim())));
            }
            r.setDevices(deviceTypes);
        }
    }

    /**
     * Delegates to {@link ch.ethz.replay.ui.scheduler.external.ethz.soapvvz.SuchindexDAO#isOnlyLectures()}.
     */
    public boolean isOnlyLectures() {
        return suchindexDAO.isOnlyLectures();
    }

    /**
     * Converts a location string to a location string for recordings, which means that
     * all spaces and other non-alphanumerics are being stripped off.
     * +
     */
    private String toRecordingLocation(String location) {
        return location.replaceAll("\\W", "");
    }

    /**
     * @param timeOfDay local time
     * @param weekday 1 = monday
     */
    private void moveTo(Calendar cal, LocalTime timeOfDay, int weekday) {
        cal.set(Calendar.DAY_OF_WEEK, WEEKDAYS[weekday]);
        cal.set(Calendar.HOUR_OF_DAY, timeOfDay.getHourOfDay());
        cal.set(Calendar.MINUTE, timeOfDay.getMinuteOfHour());
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Moves the calender to the last moment of the day, i.e. 24h
     */
    private void moveToEndOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 24);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Find the current or upcoming semester, or return <code>null</code> if none can be found.
     */
    public Semester findCurrentOrUpcomingSemester(Date date) {
        List rs = getHibernateTemplate().find("from Semester where semesterende >= ? order by semesterbeginn asc", date);
        if (rs.size() > 0)
            return (Semester) rs.get(0);
        else
            return null;
    }

    // --

    public void setSuchindexDAO(SuchindexDAO suchindexDAO) {
        this.suchindexDAO = suchindexDAO;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public void setSemesterDAO(SemesterDAO semesterDAO) {
        this.semesterDAO = semesterDAO;
    }

    public void setLehrveranstaltungDAO(LehrveranstaltungDAO lehrveranstaltungDAO) {
        this.lehrveranstaltungDAO = lehrveranstaltungDAO;
    }

    public void setDublinCoreDefaults(Map<String, String> dublinCoreDefaults) {
        this.dublinCoreDefaults = dublinCoreDefaults;
    }

    public void setStudiengangDAO(MStudiengangDAO studiengangDAO) {
        this.studiengangDAO = studiengangDAO;
    }

    public void setRegionDAO(MRegionDAO regionDAO) {
        this.regionDAO = regionDAO;
    }

    public void setDeviceTypeDao(DeviceTypeDao deviceTypeDao) {
        this.deviceTypeDao = deviceTypeDao;
    }

    //

    private static class LehrveranstalterComparator implements Comparator<Lehrveranstalter> {

        public static final LehrveranstalterComparator Obj = new LehrveranstalterComparator();

        public int compare(Lehrveranstalter o1, Lehrveranstalter o2) {
            if (!(o1.isHauptverantwortlicher() ^ o2.isHauptverantwortlicher())) {
                int r = o1.getName().compareTo(o2.getName());
                if (r == 0)
                    return o1.getVorname().compareTo(o2.getVorname());
                else
                    return r;
            }
            if (o1.isHauptverantwortlicher())
                return -1;
            return 1;
        }
    }
}
