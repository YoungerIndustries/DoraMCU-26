package com.yisy.modules.doramcu.service.ai;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.model.misc.CharacterType;


import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Provides a unified view of the AI network â€” all entities (ships, stations,
 * vehicles, facilities) connected to the AI network as AI characters.
 * <p>
 * The AI network represents the web of computer modules across an empire's
 * assets. Each entity's AI Core is registered as an AI character in the
 * repository, and this service aggregates the network status across all types.
 * </p>
 * <p>
 * Usage:
 * <pre>
 * // Get overall network status for an empire
 * AiNetworkStatus status = aiNetworkService.getNetworkStatus("Federation");
 *
 * // Get all active entities in the network
 * List&lt;AiCharacter&gt; entities = aiNetworkService.getActiveNetworkEntities("Federation");
 *
 * // Get network topology (grouped by type)
 * Map&lt;AiCharacter.CharacterType, List&lt;AiCharacter&gt;&gt; topology =
 *     aiNetworkService.getNetworkTopology("Federation");
 * </pre>
 * </p>
 */
@Service
public class AiNetworkService {

    private final AiCharacterRepository aiCharacterRepository;
    private final AiEntityRegistrationService entityRegistrationService;

    public AiNetworkService(AiCharacterRepository aiCharacterRepository,
                            AiEntityRegistrationService entityRegistrationService) {
        this.aiCharacterRepository = aiCharacterRepository;
        this.entityRegistrationService = entityRegistrationService;
    }

    // --- Network Status ---

    /**
     * Returns a summary of the AI network for a given empire.
     *
     * @param empireName the empire name
     * @return the network status summary
     */
    @Transactional(readOnly = true)
    public AiNetworkStatus getNetworkStatus(String empireName) {
        List<AiCharacter> allEntities = aiCharacterRepository
                .findByCharacterTypeInAndEmpireNameAndActiveTrue(
                        List.of(
                                AiCharacter.CharacterType.SHIP,
                                AiCharacter.CharacterType.STATION,
                                AiCharacter.CharacterType.VEHICLE,
                                AiCharacter.CharacterType.FACILITY
                        ), empireName);

        List<AiCharacter> inactiveEntities = aiCharacterRepository
                .findByCharacterTypeInAndEmpireNameAndActiveFalse(
                        List.of(
                                AiCharacter.CharacterType.SHIP,
                                AiCharacter.CharacterType.STATION,
                                AiCharacter.CharacterType.VEHICLE,
                                AiCharacter.CharacterType.FACILITY
                        ), empireName);

        long shipCount = allEntities.stream()
                .filter(e -> e.getCharacterType() == AiCharacter.CharacterType.SHIP).count();
        long stationCount = allEntities.stream()
                .filter(e -> e.getCharacterType() == AiCharacter.CharacterType.STATION).count();
        long vehicleCount = allEntities.stream()
                .filter(e -> e.getCharacterType() == AiCharacter.CharacterType.VEHICLE).count();
        long facilityCount = allEntities.stream()
                .filter(e -> e.getCharacterType() == AiCharacter.CharacterType.FACILITY).count();

        long inactiveShipCount = inactiveEntities.stream()
                .filter(e -> e.getCharacterType() == AiCharacter.CharacterType.SHIP).count();
        long inactiveStationCount = inactiveEntities.stream()
                .filter(e -> e.getCharacterType() == AiCharacter.CharacterType.STATION).count();
        long inactiveVehicleCount = inactiveEntities.stream()
                .filter(e -> e.getCharacterType() == AiCharacter.CharacterType.VEHICLE).count();
        long inactiveFacilityCount = inactiveEntities.stream()
                .filter(e -> e.getCharacterType() == AiCharacter.CharacterType.FACILITY).count();

        return new AiNetworkStatus(
                empireName,
                allEntities.size(),
                inactiveEntities.size(),
                (int) shipCount,
                (int) stationCount,
                (int) vehicleCount,
                (int) facilityCount,
                (int) inactiveShipCount,
                (int) inactiveStationCount,
                (int) inactiveVehicleCount,
                (int) inactiveFacilityCount
        );
    }

    // --- Network Entities ---

    /**
     * Returns all active entity-linked AI characters for an empire.
     *
     * @param empireName the empire name
     * @return list of active network entities
     */
    @Transactional(readOnly = true)
    public List<AiCharacter> getActiveNetworkEntities(String empireName) {
        return entityRegistrationService.findAllEntityAiCharactersByEmpire(empireName);
    }

    /**
     * Returns all entity-linked AI characters (active and inactive) for an empire.
     *
     * @param empireName the empire name
     * @return list of all network entities
     */
    @Transactional(readOnly = true)
    public List<AiCharacter> getAllNetworkEntities(String empireName) {
        List<AiCharacter> active = entityRegistrationService.findAllEntityAiCharactersByEmpire(empireName);

        List<AiCharacter> inactive = aiCharacterRepository
                .findByCharacterTypeInAndEmpireNameAndActiveFalse(
                        List.of(
                                AiCharacter.CharacterType.SHIP,
                                AiCharacter.CharacterType.STATION,
                                AiCharacter.CharacterType.VEHICLE,
                                AiCharacter.CharacterType.FACILITY
                        ), empireName);

        List<AiCharacter> all = new ArrayList<>(active);
        all.addAll(inactive);
        return all;
    }

    // --- Network Topology ---

    /**
     * Returns the network topology grouped by entity type.
     *
     * @param empireName the empire name
     * @return map of character type to list of active AI characters
     */
    @Transactional(readOnly = true)
    public Map<AiCharacter.CharacterType, List<AiCharacter>> getNetworkTopology(String empireName) {
        List<AiCharacter> allEntities = getActiveNetworkEntities(empireName);
        return allEntities.stream()
                .collect(Collectors.groupingBy(AiCharacter::getCharacterType));
    }

    // --- Network Connectivity ---

    /**
     * Checks if a ship is connected to the AI network (has an active AI character).
     *
     * @param shipId the ship ID
     * @return true if the ship has an active AI character
     */
    @Transactional(readOnly = true)
    public boolean isShipConnected(int shipId) {
        return entityRegistrationService.findShipAiCharacter(shipId)
                .map(AiCharacter::isActive)
                .orElse(false);
    }

    /**
     * Checks if a station is connected to the AI network (has an active AI character).
     *
     * @param stationId the station ID
     * @return true if the station has an active AI character
     */
    @Transactional(readOnly = true)
    public boolean isStationConnected(int stationId) {
        return entityRegistrationService.findStationAiCharacter(stationId)
                .map(AiCharacter::isActive)
                .orElse(false);
    }

    /**
     * Checks if a vehicle is connected to the AI network (has an active AI character).
     *
     * @param vehicleId the vehicle ID string
     * @return true if the vehicle has an active AI character
     */
    @Transactional(readOnly = true)
    public boolean isVehicleConnected(String vehicleId) {
        return entityRegistrationService.findVehicleAiCharacter(vehicleId)
                .map(AiCharacter::isActive)
                .orElse(false);
    }

    /**
     * Checks if a facility is connected to the AI network (has an active AI character).
     *
     * @param facilityId the facility ID
     * @return true if the facility has an active AI character
     */
    @Transactional(readOnly = true)
    public boolean isFacilityConnected(int facilityId) {
        return entityRegistrationService.findFacilityAiCharacter(facilityId)
                .map(AiCharacter::isActive)
                .orElse(false);
    }

    // --- Network Statistics ---

    /**
     * Returns statistics about the AI network for all empires.
     *
     * @return list of network status summaries for all empires
     */
    @Transactional(readOnly = true)
    public List<AiNetworkStatus> getAllNetworksStatus() {
        // Find all distinct empire names with entity-linked AI characters
        List<AiCharacter> allEntityCharacters = aiCharacterRepository
                .findByCharacterTypeIn(
                        List.of(
                                AiCharacter.CharacterType.SHIP,
                                AiCharacter.CharacterType.STATION,
                                AiCharacter.CharacterType.VEHICLE,
                                AiCharacter.CharacterType.FACILITY
                        ));

        Set<String> empireNames = allEntityCharacters.stream()
                .map(AiCharacter::getEmpireName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return empireNames.stream()
                .map(this::getNetworkStatus)
                .collect(Collectors.toList());
    }

    /**
     * Summary of the AI network status for an empire.
     */
    public static class AiNetworkStatus {
        private final String empireName;
        private final int totalActiveEntities;
        private final int totalInactiveEntities;
        private final int activeShips;
        private final int activeStations;
        private final int activeVehicles;
        private final int activeFacilities;
        private final int inactiveShips;
        private final int inactiveStations;
        private final int inactiveVehicles;
        private final int inactiveFacilities;

        public AiNetworkStatus(String empireName, int totalActiveEntities, int totalInactiveEntities,
                               int activeShips, int activeStations, int activeVehicles, int activeFacilities,
                               int inactiveShips, int inactiveStations, int inactiveVehicles, int inactiveFacilities) {
            this.empireName = empireName;
            this.totalActiveEntities = totalActiveEntities;
            this.totalInactiveEntities = totalInactiveEntities;
            this.activeShips = activeShips;
            this.activeStations = activeStations;
            this.activeVehicles = activeVehicles;
            this.activeFacilities = activeFacilities;
            this.inactiveShips = inactiveShips;
            this.inactiveStations = inactiveStations;
            this.inactiveVehicles = inactiveVehicles;
            this.inactiveFacilities = inactiveFacilities;
        }

        public String getEmpireName() {
            return empireName;
        }

        public int getTotalActiveEntities() {
            return totalActiveEntities;
        }

        public int getTotalInactiveEntities() {
            return totalInactiveEntities;
        }

        public int getActiveShips() {
            return activeShips;
        }

        public int getActiveStations() {
            return activeStations;
        }

        public int getActiveVehicles() {
            return activeVehicles;
        }

        public int getActiveFacilities() {
            return activeFacilities;
        }

        public int getInactiveShips() {
            return inactiveShips;
        }

        public int getInactiveStations() {
            return inactiveStations;
        }

        public int getInactiveVehicles() {
            return inactiveVehicles;
        }

        public int getInactiveFacilities() {
            return inactiveFacilities;
        }

        public int getTotalEntities() {
            return totalActiveEntities + totalInactiveEntities;
        }

        @Override
        public String toString() {
            return "AiNetworkStatus{" +
                    "empire='" + empireName + '\'' +
                    ", active=" + totalActiveEntities +
                    ", inactive=" + totalInactiveEntities +
                    ", ships=" + activeShips +
                    ", stations=" + activeStations +
                    ", vehicles=" + activeVehicles +
                    ", facilities=" + activeFacilities +
                    '}';
        }
    }
}
