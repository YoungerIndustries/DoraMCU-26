package com.yisy.modules.doramcu.hex.planet.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Defines a timetable for a vehicle route.
 * <p>
 * A timetable consists of ordered stops at stations with departure times
 * and wait durations. Vehicles follow the timetable cyclically.
 * </p>
 */
public class Timetable {

    private final String id;
    private final String name;
    private final List<TimetableStop> stops;
    private final boolean loop;

    /**
     * Creates a new timetable.
     *
     * @param name display name for the timetable
     * @param stops ordered list of stops
     * @param loop whether the timetable loops back to the first stop
     */
    public Timetable(String name, List<TimetableStop> stops, boolean loop) {
        this.id = UUID.randomUUID().toString();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Timetable name must not be empty");
        }
        if (stops == null || stops.isEmpty()) {
            throw new IllegalArgumentException("Timetable must have at least one stop");
        }
        this.name = name;
        this.stops = new ArrayList<>(stops);
        this.loop = loop;
    }

    /** @return the unique timetable ID */
    public String getId() {
        return id;
    }

    /** @return the timetable name */
    public String getName() {
        return name;
    }

    /** @return the ordered list of stops */
    public List<TimetableStop> getStops() {
        return Collections.unmodifiableList(stops);
    }

    /** @return true if the timetable loops back to the first stop */
    public boolean isLoop() {
        return loop;
    }

    /** @return the number of stops */
    public int getStopCount() {
        return stops.size();
    }

    /**
     * Gets the stop at the given index.
     *
     * @param index the stop index
     * @return the timetable stop
     */
    public TimetableStop getStop(int index) {
        if (index < 0 || index >= stops.size()) {
            throw new IndexOutOfBoundsException("Stop index " + index + " out of range [0, " + stops.size() + ")");
        }
        return stops.get(index);
    }

    /**
     * Gets the next stop index after the given index.
     * If the timetable loops, wraps around to 0.
     *
     * @param currentIndex the current stop index
     * @return the next stop index, or -1 if at the end and not looping
     */
    public int getNextStopIndex(int currentIndex) {
        int next = currentIndex + 1;
        if (next >= stops.size()) {
            return loop ? 0 : -1;
        }
        return next;
    }

    /**
     * Gets the station ID at the given index.
     *
     * @param index the stop index
     * @return the station ID
     */
    public String getStationIdAt(int index) {
        return getStop(index).getStationId();
    }

    @Override
    public String toString() {
        return String.format("Timetable{id=%s, name=%s, stops=%d, loop=%s}",
                id, name, stops.size(), loop);
    }
}
