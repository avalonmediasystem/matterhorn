/*
 * Other Meta Data can be imported
 */

package ch.ethz.replay.ui.scheduler.external.ethz.legacy;

public class Room {

    private String id;
    private String name;
    private String startTime;
    private String endTime;
    private int weekday;
    private int weekDeltaStart;
    private int weekDeltaEnd;

    public Room(String id, String name, String startTime, String endTime, int weekday,
                int weekDeltaStart, int weekDeltaEnd) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.weekday = weekday;
        this.weekDeltaEnd = weekDeltaEnd;
        this.weekDeltaStart = weekDeltaStart;
    }

    public String getId() {
        return id;
    }

    /**
     * Returns the time of day the lecture starts, e.g. 10:15
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Returns the time of day the lecture end, e.g. 11:30
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * Returns the weekday, the lecture takes place, <code>0</code> is monday.
     */
    public int getWeekday() {
        return weekday;
    }

    public String getName() {
        return name;
    }

    public int getWeekDeltaStart() {
        return weekDeltaStart;
    }

    public int getWeekDeltaEnd() {
        return weekDeltaEnd;
    }
}
