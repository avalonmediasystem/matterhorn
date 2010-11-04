package ch.ethz.replay.ui.scheduler;

/**
 * Describes the various process stages a recording can assume. The stages have the following order:
 * <ol>
 * <li>{@link #created}
 * <li>{@link #scheduled}
 * <li>{@link #started}
 * <li>{@link #finished}
 * <li>{@link #ingested}
 * <li>{@link #archived}
 * </ol>
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public enum RecordingStatus {

    /**
     * The recording is created, but not scheduled.
     */
    created,

    /**
     * The recording is scheduled, but has not been started yet.
     */
    scheduled,

    /**
     * The recording has started.
     */
    started,

    /**
     * The recording has finished, so that the recorded data is ready for further processing.
     */
    finished,

    /**
     * The recorded bundle has been ingested by REPLAY:Core.
     */
    ingested,

    /**
     * The recording has been completely processed and put in the archive. This is the final stage.
     */
    archived;

    /**
     * Checks if this status represents a new, unscheduled recording.
     */
    public boolean isNew() {
        return this == RecordingStatus.created;
    }

    /**
     * Checks if this status can pass into <code>newStatus</code>.
     *
     * @throws IllegalArgumentException if transition is not possible
     */
    public void checkTransition(RecordingStatus newStatus) {
        if (Math.abs(ordinal() - newStatus.ordinal()) > 1) {
            throw new IllegalArgumentException("Cannot skip a status");
        }
    }
}
