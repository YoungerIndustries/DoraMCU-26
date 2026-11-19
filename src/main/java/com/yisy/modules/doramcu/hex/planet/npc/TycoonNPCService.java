package com.yisy.modules.doramcu.hex.planet.npc;

import com.yisy.modules.doramcu.hex.Hex3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates and manages NPCs for the tycoon world.
 * <p>
 * The service:
 * <ul>
 *   <li>Generates driver NPCs for each active vehicle</li>
 *   <li>Generates worker NPCs for each active industry</li>
 *   <li>Generates staff NPCs for each station</li>
 *   <li>Updates NPC positions based on vehicle movement</li>
 * </ul>
 * </p>
 */
@Service
public class TycoonNPCService {

    private static final Logger log = LoggerFactory.getLogger(TycoonNPCService.class);

    /** All NPCs in the world */
    private final Map<String, TycoonNPC> npcs;

    /** Name pools for generating NPC names */
    private static final String[] FIRST_NAMES = {
            "James", "Mary", "John", "Patricia", "Robert", "Jennifer",
            "Michael", "Linda", "William", "Elizabeth", "David", "Barbara",
            "Richard", "Susan", "Joseph", "Jessica", "Thomas", "Sarah",
            "Charles", "Karen", "Christopher", "Lisa", "Daniel", "Nancy"
    };

    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia",
            "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez",
            "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore",
            "Jackson", "Martin", "Lee", "Perez", "Thompson", "White"
    };

    public TycoonNPCService() {
        this.npcs = new ConcurrentHashMap<>();
    }

    /**
     * Generates all NPCs for the current game state.
     *
     * @param vehicles list of all vehicles
     * @param industries list of all industries
     * @param stations list of all stations
     */
    public void generateAllNpcs(List<SurfaceVehicle> vehicles,
                                 List<Industry> industries,
                                 List<Station> stations) {
        // Clear existing NPCs
        npcs.clear();

        // Generate vehicle drivers
        for (SurfaceVehicle vehicle : vehicles) {
            if (vehicle.isActive()) {
                generateVehicleDriver(vehicle);
            }
        }

        // Generate factory workers
        for (Industry industry : industries) {
            if (industry.isActive()) {
                generateFactoryWorkers(industry);
            }
        }

        // Generate station staff
        for (Station station : stations) {
            generateStationStaff(station);
        }

        log.info("Generated {} NPCs: {} drivers, {} workers, {} staff",
                npcs.size(),
                countByType(NpcType.TRAIN_DRIVER, NpcType.BUS_DRIVER, NpcType.TRUCK_DRIVER,
                           NpcType.SHIP_CAPTAIN, NpcType.PILOT),
                countByType(NpcType.FACTORY_WORKER),
                countByType(NpcType.STATION_MASTER, NpcType.CARGO_HANDLER));
    }

    /**
     * Generates a driver NPC for a vehicle.
     */
    private void generateVehicleDriver(SurfaceVehicle vehicle) {
        NpcType type = getDriverType(vehicle.getType());
        String name = generateName();
        TycoonNPC npc = new TycoonNPC(type, name, vehicle.getId(), vehicle.getCurrentPosition());
        npcs.put(npc.getId(), npc);
    }

    /**
     * Generates worker NPCs for an industry.
     */
    private void generateFactoryWorkers(Industry industry) {
        IndustryType type = industry.getType();
        int workerCount;
        switch (type.getLevel()) {
            case PRIMARY: workerCount = 3; break;
            case SECONDARY: workerCount = 5; break;
            case TERTIARY: workerCount = 8; break;
            default: workerCount = 3; break;
        }

        for (int i = 0; i < workerCount; i++) {
            String name = generateName();
            TycoonNPC npc = new TycoonNPC(NpcType.FACTORY_WORKER, name,
                    industry.getId(), industry.getLocation());
            npcs.put(npc.getId(), npc);
        }
    }

    /**
     * Generates staff NPCs for a station.
     */
    private void generateStationStaff(Station station) {
        // Station master
        String masterName = generateName();
        TycoonNPC master = new TycoonNPC(NpcType.STATION_MASTER, masterName,
                station.getId(), station.getPosition());
        npcs.put(master.getId(), master);

        // Cargo handlers based on cargo volume
        int cargoHandlers = Math.max(1, station.getTotalCargoWaiting() / 20);
        for (int i = 0; i < cargoHandlers; i++) {
            String name = generateName();
            TycoonNPC handler = new TycoonNPC(NpcType.CARGO_HANDLER, name,
                    station.getId(), station.getPosition());
            npcs.put(handler.getId(), handler);
        }
    }

    /**
     * Updates NPC positions based on current vehicle positions.
     */
    public void updateNpcPositions(List<SurfaceVehicle> vehicles) {
        for (SurfaceVehicle vehicle : vehicles) {
            // Find driver NPC for this vehicle
            for (TycoonNPC npc : npcs.values()) {
                if (vehicle.getId().equals(npc.getEntityId()) &&
                    isDriverType(npc.getType())) {
                    npc.setPosition(vehicle.getCurrentPosition());
                    break;
                }
            }
        }
    }

    /**
     * Gets all NPCs.
     *
     * @return unmodifiable list of all NPCs
     */
    public List<TycoonNPC> getAllNpcs() {
        return new ArrayList<>(npcs.values());
    }

    /**
     * Gets NPCs by entity ID.
     *
     * @param entityId the entity ID
     * @return list of NPCs associated with the entity
     */
    public List<TycoonNPC> getNpcsByEntity(String entityId) {
        List<TycoonNPC> result = new ArrayList<>();
        for (TycoonNPC npc : npcs.values()) {
            if (entityId.equals(npc.getEntityId())) {
                result.add(npc);
            }
        }
        return result;
    }

    /**
     * Gets NPCs by type.
     *
     * @param type the NPC type
     * @return list of NPCs of the given type
     */
    public List<TycoonNPC> getNpcsByType(NpcType type) {
        List<TycoonNPC> result = new ArrayList<>();
        for (TycoonNPC npc : npcs.values()) {
            if (npc.getType() == type) {
                result.add(npc);
            }
        }
        return result;
    }

    /**
     * Gets the driver type for a vehicle type.
     */
    private NpcType getDriverType(VehicleType vehicleType) {
        switch (vehicleType) {
            case FREIGHT_TRAIN:
            case PASSENGER_TRAIN:
                return NpcType.TRAIN_DRIVER;
            case BUS:
                return NpcType.BUS_DRIVER;
            case TRUCK:
            case EXPRESS_TRUCK:
                return NpcType.TRUCK_DRIVER;
            case CARGO_SHIP:
            case PASSENGER_FERRY:
                return NpcType.SHIP_CAPTAIN;
            case AIRCRAFT:
                return NpcType.PILOT;
            default:
                return NpcType.TRUCK_DRIVER;
        }
    }

    /**
     * Checks if an NPC type is a driver type.
     */
    private boolean isDriverType(NpcType type) {
        return type == NpcType.TRAIN_DRIVER || type == NpcType.BUS_DRIVER ||
               type == NpcType.TRUCK_DRIVER || type == NpcType.SHIP_CAPTAIN ||
               type == NpcType.PILOT;
    }

    /**
     * Counts NPCs of the given types.
     */
    private int countByType(NpcType... types) {
        int count = 0;
        for (NpcType type : types) {
            for (TycoonNPC npc : npcs.values()) {
                if (npc.getType() == type) count++;
            }
        }
        return count;
    }

    /**
     * Generates a random name.
     */
    private String generateName() {
        Random random = new Random();
        String first = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String last = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        return first + " " + last;
    }
}
