package com.yisy.modules.doramcu.hex.planet.schedule;

/**
 * Represents a single stop in a vehicle timetable.
 * <p>
 * Each stop defines:
 * <ul>
 *   <li>The station ID where the vehicle stops</li>
 *   <li>The departure time (turn number)</li>
 *   <li>The wait duration in turns</li>
 * </ul>
 * </p>
 */
public class TimetableStop {

    private final String stationId;
    private final int departureTurn;
    private final int waitTurns;

    /**
     * Creates a timetable stop.
     *
     * @param stationId the station ID
     * @param departureTurn the turn number when the vehicle departs
     * @param waitTurns how many turns the vehicle waits at this station
     */
    public TimetableStop(String stationId, int departureTurn, int waitTurns) {
        if (stationId == null || stationId.isEmpty()) {
            throw new IllegalArgumentException("Station ID must not be empty");
        }
        if (departureTurn < 0) {
            throw new IllegalArgumentException("Departure turn must be non-negative");
        }
        if (waitTurns < 0) {
            throw new IllegalArgumentException("Wait turns must be non-negative");
        }
        this.stationId = stationId;
        this.departureTurn = departureTurn;
        this.waitTurns = waitTurns;
    }

    /** @return the station ID */
    public String getStationId() {
        return stationId;
    }

    /** @return the departure turn number */
    public int getDepartureTurn() {
        return departureTurn;
    }

    /** @return the wait duration in turns */
    public int getWaitTurns() {
        return waitTurns;
    }

    @Override
    public String toString() {
        return String.format("TimetableStop{station=%s, depart=%d, wait=%d}",
                stationId, departureTurn, waitTurns);
    }
}
