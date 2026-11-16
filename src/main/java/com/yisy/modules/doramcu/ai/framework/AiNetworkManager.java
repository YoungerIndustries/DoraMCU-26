package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.eve.model.AiCharacter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Unified AI network manager that provides a single interface for managing
 * all AI entities (ships, stations, vehicles, facilities) and their relationships.
 * <p>
 * This service consolidates the network management functionality from
 * AiNetworkService and AiEntityRegistrationService into a single interface.
 * </p>
 * <p>
 * Usage:
 * <pre>
 * // Get network status for an empire
 * NetworkStatus status = networkManager.getNetworkStatus(empireName);
 *
 * // Register a new AI entity
 * networkManager.registerEntity(aiCharacter);
 *
 * // Get network topology
 * Map&lt;CharacterType, List&lt;AiCharacter&gt;&gt; topology = networkManager.getTopology(empireName);
 * </pre>
 * </p>
 */
public interface AiNetworkManager {

    /**
     * Returns a summary of the AI network for a given empire.
     *
     * @param empireName the empire name
     * @return the network status summary
     */
    NetworkStatus getNetworkStatus(String empireName);

    /**
     * Returns all active AI entities for an empire.
     *
     * @param empireName the empire name
     * @return list of active AI characters
     */
    List<AiCharacter> getActiveEntities(String empireName);

    /**
     * Returns all AI entities (active and inactive) for an empire.
     *
     * @param empireName the empire name
     * @return list of all AI characters
     */
    List<AiCharacter> getAllEntities(String empireName);

    /**
     * Returns the network topology grouped by entity type.
     *
     * @param empireName the empire name
     * @return map of character type to list of AI characters
     */
    Map<AiCharacter.CharacterType, List<AiCharacter>> getTopology(String empireName);

    /**
     * Registers a new AI entity in the network.
     *
     * @param aiCharacter the AI character to register
     */
    void registerEntity(AiCharacter aiCharacter);

    /**
     * Deactivates an AI entity in the network.
     *
     * @param entityId the entity ID
     * @return true if the entity was found and deactivated
     */
    boolean deactivateEntity(Long entityId);

    /**
     * Finds an AI character by entity ID.
     *
     * @param entityId the entity ID
     * @return the AI character if found
     */
    Optional<AiCharacter> findEntity(Long entityId);

    /**
     * Checks if an entity is connected to the AI network.
     *
     * @param entityId the entity ID
     * @return true if the entity has an active AI character
     */
    boolean isConnected(String entityId);

    /**
     * Returns statistics about all AI networks.
     *
     * @return list of network status summaries for all empires
     */
    List<NetworkStatus> getAllNetworksStatus();

    /**
     * Summary of the AI network status for an empire.
     */
    class NetworkStatus {
        private final String empireName;
        private final int totalActiveEntities;
        private final int totalInactiveEntities;
        private final int activeShips;
        private final int activeStations;
        private final int activeVehicles;
        private final int activeFacilities;

        public NetworkStatus(String empireName, int totalActiveEntities, int totalInactiveEntities,
                             int activeShips, int activeStations, int activeVehicles, int activeFacilities) {
            this.empireName = empireName;
            this.totalActiveEntities = totalActiveEntities;
            this.totalInactiveEntities = totalInactiveEntities;
            this.activeShips = activeShips;
            this.activeStations = activeStations;
            this.activeVehicles = activeVehicles;
            this.activeFacilities = activeFacilities;
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

        public int getTotalEntities() {
            return totalActiveEntities + totalInactiveEntities;
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

        @Override
        public String toString() {
            return "NetworkStatus{" +
                    "empire='" + empireName + '\'' +
                    ", active=" + totalActiveEntities +
                    ", inactive=" + totalInactiveEntities +
                    '}';
        }
    }
}
