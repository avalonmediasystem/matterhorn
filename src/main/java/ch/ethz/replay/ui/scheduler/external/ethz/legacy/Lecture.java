/*
 * Other Meta Data can be imported
 */

package ch.ethz.replay.ui.scheduler.external.ethz.legacy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Represents a lecture course. This is a cleaned-up legacy class.
 */
public class Lecture {

    private String semkez;
    private String code;
    private String title;
    private String titleEn;

    private List<Speaker> speakers = new ArrayList<Speaker>();
    private List<Room> rooms = new ArrayList<Room>();

    public Lecture(String code, String semkez, String title, String titleEn) {
        this.code = code;
        this.title = title;
        this.titleEn = titleEn;
        this.semkez = semkez;
    }

    public String getCode() {
        return code;
    }

    public String getSemkez() {
        return semkez;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public List<Speaker> getSpeakers() {
        return speakers;
    }

    public List<Room> getRooms() {
        return rooms;
    }
}
