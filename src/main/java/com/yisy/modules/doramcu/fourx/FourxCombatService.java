package com.yisy.modules.doramcu.fourx;

import java.util.List;
import java.util.Map;

/**
 * Shared interface for the Combat 4X pillar.
 * <p>
 * Implementing services handle tactical combat including ship-to-ship battles,
 * fleet engagements, and combat results. Each game module provides its own
 * implementation with theme-specific ship types and combat rules.
 * </p>
 */
public interface FourxCombatService {

    /**
     * Initiate a combat engagement between two fleets.
     *
     * @param attackerFleetId the attacker fleet ID
     * @param defenderFleetId the defender fleet ID
     * @return combat result details
     */
    Map<String, Object> initiateCombat(String attackerFleetId, String defenderFleetId);

    /**
     * Execute a single combat round.
     *
     * @param combatId the combat session ID
     * @return round results
     */
    Map<String, Object> executeCombatRound(String combatId);

    /**
     * Get the current status of a combat session.
     *
     * @param combatId the combat session ID
     * @return combat status
     */
    Map<String, Object> getCombatStatus(String combatId);

    /**
     * Get available ship types for a faction.
     *
     * @param factionId the faction ID
     * @return list of ship type descriptions
     */
    List<Map<String, Object>> getShipTypes(String factionId);

    /**
     * Create a new fleet for a faction.
     *
     * @param factionId  the owning faction ID
     * @param fleetName  the fleet name
     * @param shipTypeId the ship type ID
     * @param count      the number of ships
     * @return the created fleet details
     */
    Map<String, Object> createFleet(String factionId, String fleetName, String shipTypeId, int count);

    /**
     * Get all fleets for a faction.
     *
     * @param factionId the faction ID
     * @return list of fleet details
     */
    List<Map<String, Object>> getFleets(String factionId);

    /**
     * Calculate combat power of a fleet.
     *
     * @param fleetId the fleet ID
     * @return combat power score
     */
    int calculateCombatPower(String fleetId);

    /**
     * Get a list of all active combats.
     *
     * @return list of active combat IDs
     */
    List<String> getActiveCombats();
}
