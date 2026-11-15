package com.yisy.modules.doramcu.hex.planet.signals;

import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.planet.SurfaceVehicle;
import com.yisy.modules.doramcu.hex.planet.VehicleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages rail signals for traffic control.
 * <p>
 * The service:
 * <ul>
 *   <li>Places signals on rail tracks</li>
 *   <li>Manages block sections</li>
 *   <li>Prevents head-on collisions</li>
 *   <li>Pathfinding respects signal state</li>
 * </ul>
 * </p>
 */
@Service
public class RailSignalService {

    private static final Logger log = LoggerFactory.getLogger(RailSignalService.class);

    /** Signal ID -> signal */
    private final Map<String, RailSignal> signals;

    /** Block section ID -> list of signal IDs */
    private final Map<String, List<String>> blockSections;

    /** Block section ID -> occupying vehicle ID */
    private final Map<String, String> occupiedSections;

    public RailSignalService() {
        this.signals = new ConcurrentHashMap<>();
        this.blockSections = new ConcurrentHashMap<>();
        this.occupiedSections = new ConcurrentHashMap<>();
    }

    /**
     * Places a signal on a rail track.
     *
     * @param position the hex position
     * @param blockSectionId the block section ID
     * @param ownerCorporationId the owner corporation
     * @return the created signal
     */
    public RailSignal placeSignal(Hex3D position, String blockSectionId, String ownerCorporationId) {
        RailSignal signal = new RailSignal(position, blockSectionId, ownerCorporationId);
        signals.put(signal.getId(), signal);
        blockSections.computeIfAbsent(blockSectionId, k -> new ArrayList<>()).add(signal.getId());
        log.info("Placed signal {} at {} for block {}", signal.getId(), position, blockSectionId);
        return signal;
    }

    /**
     * Checks if a vehicle can enter a block section.
     *
     * @param blockSectionId the block section ID
     * @param vehicleId the vehicle ID
     * @return true if the vehicle can enter
     */
    public boolean canEnterSection(String blockSectionId, String vehicleId) {
        String occupant = occupiedSections.get(blockSectionId);
        if (occupant == null) return true;
        return occupant.equals(vehicleId);
    }

    /**
     * Registers a vehicle entering a block section.
     *
     * @param blockSectionId the block section ID
     * @param vehicleId the vehicle ID
     */
    public void enterSection(String blockSectionId, String vehicleId) {
        occupiedSections.put(blockSectionId, vehicleId);
        // Set signals to red for this section
        List<String> signalIds = blockSections.get(blockSectionId);
        if (signalIds != null) {
            for (String signalId : signalIds) {
                RailSignal signal = signals.get(signalId);
                if (signal != null) {
                    signal.setState(RailSignal.SignalState.RED);
                }
            }
        }
    }

    /**
     * Registers a vehicle leaving a block section.
     *
     * @param blockSectionId the block section ID
     * @param vehicleId the vehicle ID
     */
    public void leaveSection(String blockSectionId, String vehicleId) {
        String occupant = occupiedSections.get(blockSectionId);
        if (occupant != null && occupant.equals(vehicleId)) {
            occupiedSections.remove(blockSectionId);
            // Set signals to green
            List<String> signalIds = blockSections.get(blockSectionId);
            if (signalIds != null) {
                for (String signalId : signalIds) {
                    RailSignal signal = signals.get(signalId);
                    if (signal != null) {
                        signal.setState(RailSignal.SignalState.GREEN);
                    }
                }
            }
        }
    }

    /**
     * Gets a signal by ID.
     *
     * @param signalId the signal ID
     * @return the signal, or null if not found
     */
    public RailSignal getSignal(String signalId) {
        return signals.get(signalId);
    }

    /**
     * Gets all signals.
     *
     * @return list of all signals
     */
    public List<RailSignal> getAllSignals() {
        return new ArrayList<>(signals.values());
    }

    /**
     * Checks if a position has a red signal.
     *
     * @param position the hex position
     * @return true if there's a red signal at this position
     */
    public boolean hasRedSignal(Hex3D position) {
        for (RailSignal signal : signals.values()) {
            if (signal.getPosition().equals(position) && !signal.isClear()) {
                return true;
            }
        }
        return false;
    }
}
