package com.yisy.modules.doramcu.hex.planet.schedule;

import com.yisy.modules.doramcu.hex.planet.Station;
import com.yisy.modules.doramcu.hex.planet.SurfaceVehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages vehicle timetables: creating, assigning, and processing schedules.
 * <p>
 * The service:
 * <ul>
 *   <li>Creates timetables from station lists</li>
 *   <li>Assigns timetables to vehicles</li>
 *   <li>Processes timetable each turn: vehicle moves toward next station</li>
 *   <li>Handles waiting at stations</li>
 * </ul>
 * </p>
 */
@Service
public class TimetableService {

    private static final Logger log = LoggerFactory.getLogger(TimetableService.class);

    /** Timetable ID -> timetable */
    private final Map<String, Timetable> timetables;

    /** Vehicle ID -> timetable ID */
    private final Map<String, String> vehicleTimetables;

    /** Vehicle ID -> current stop index */
    private final Map<String, Integer> vehicleStopIndices;

    /** Vehicle ID -> remaining wait turns */
    private final Map<String, Integer> vehicleWaitTurns;

    public TimetableService() {
        this.timetables = new ConcurrentHashMap<>();
        this.vehicleTimetables = new ConcurrentHashMap<>();
        this.vehicleStopIndices = new ConcurrentHashMap<>();
        this.vehicleWaitTurns = new ConcurrentHashMap<>();
    }

    /**
     * Creates a timetable from a list of station IDs.
     *
     * @param name the timetable name
     * @param stationIds ordered list of station IDs
     * @param waitTurns wait duration at each stop
     * @param loop whether the timetable loops
     * @return the created timetable
     */
    public Timetable createTimetable(String name, List<String> stationIds,
                                      int waitTurns, boolean loop) {
        List<TimetableStop> stops = new ArrayList<>();
        int departureTurn = 0;
        for (String stationId : stationIds) {
            stops.add(new TimetableStop(stationId, departureTurn, waitTurns));
            departureTurn += waitTurns + 1; // +1 for travel turn
        }

        Timetable timetable = new Timetable(name, stops, loop);
        timetables.put(timetable.getId(), timetable);
        log.info("Created timetable {}: {}", timetable.getId(), name);
        return timetable;
    }

    /**
     * Assigns a timetable to a vehicle.
     *
     * @param vehicle the vehicle
     * @param timetable the timetable
     */
    public void assignTimetable(SurfaceVehicle vehicle, Timetable timetable) {
        vehicleTimetables.put(vehicle.getId(), timetable.getId());
        vehicleStopIndices.put(vehicle.getId(), 0);
        vehicleWaitTurns.put(vehicle.getId(), 0);

        // Set the route from timetable stops
        List<String> stationIds = new ArrayList<>();
        for (TimetableStop stop : timetable.getStops()) {
            stationIds.add(stop.getStationId());
        }
        vehicle.setRoute(stationIds);
        vehicle.setActive(true);

        log.info("Assigned timetable {} to vehicle {}", timetable.getId(), vehicle.getId());
    }

    /**
     * Processes a turn for a vehicle following its timetable.
     *
     * @param vehicle the vehicle
     * @param stationMap map of all stations
     * @return the station reached, or null if still en route
     */
    public Station processTurn(SurfaceVehicle vehicle, Map<String, Station> stationMap) {
        String timetableId = vehicleTimetables.get(vehicle.getId());
        if (timetableId == null) {
            // No timetable, use default route processing
            return vehicle.processTurn(stationMap);
        }

        Timetable timetable = timetables.get(timetableId);
        if (timetable == null) {
            log.warn("Timetable {} not found for vehicle {}", timetableId, vehicle.getId());
            return null;
        }

        int stopIndex = vehicleStopIndices.getOrDefault(vehicle.getId(), 0);
        int waitTurns = vehicleWaitTurns.getOrDefault(vehicle.getId(), 0);

        // If waiting, decrement wait counter
        if (waitTurns > 0) {
            vehicleWaitTurns.put(vehicle.getId(), waitTurns - 1);
            return null;
        }

        // Get target station
        TimetableStop stop = timetable.getStop(stopIndex);
        Station target = stationMap.get(stop.getStationId());
        if (target == null) {
            log.warn("Station {} not found in timetable {}", stop.getStationId(), timetable.getId());
            return null;
        }

        // Move toward target
        boolean reached = vehicle.move(target.getPosition());
        if (reached) {
            // Set wait time for next departure
            vehicleWaitTurns.put(vehicle.getId(), stop.getWaitTurns());

            // Advance to next stop
            int nextIndex = timetable.getNextStopIndex(stopIndex);
            if (nextIndex < 0) {
                // End of non-looping timetable
                vehicle.setActive(false);
                vehicleTimetables.remove(vehicle.getId());
                vehicleStopIndices.remove(vehicle.getId());
                vehicleWaitTurns.remove(vehicle.getId());
                log.info("Vehicle {} completed timetable {}", vehicle.getId(), timetable.getId());
                return target;
            }
            vehicleStopIndices.put(vehicle.getId(), nextIndex);

            // Update route index
            vehicle.setCurrentRouteIndex(nextIndex);
            return target;
        }

        return null;
    }

    /**
     * Removes a timetable from a vehicle.
     *
     * @param vehicleId the vehicle ID
     */
    public void removeTimetable(String vehicleId) {
        vehicleTimetables.remove(vehicleId);
        vehicleStopIndices.remove(vehicleId);
        vehicleWaitTurns.remove(vehicleId);
    }

    /**
     * Gets the timetable for a vehicle.
     *
     * @param vehicleId the vehicle ID
     * @return the timetable, or null if not assigned
     */
    public Timetable getVehicleTimetable(String vehicleId) {
        String timetableId = vehicleTimetables.get(vehicleId);
        if (timetableId == null) return null;
        return timetables.get(timetableId);
    }

    /**
     * Gets a timetable by ID.
     *
     * @param timetableId the timetable ID
     * @return the timetable, or null if not found
     */
    public Timetable getTimetable(String timetableId) {
        return timetables.get(timetableId);
    }

    /**
     * Gets all timetables.
     *
     * @return list of all timetables
     */
    public List<Timetable> getAllTimetables() {
        return new ArrayList<>(timetables.values());
    }
}
