package com.yisy.modules.doramcu.fourx;

import java.util.List;
import java.util.Map;

/**
 * Interface for 4X combat mechanics.
 * Implemented by game-specific combat services (B5, CaptainsLog, etc.).
 */
public interface FourxCombatService {

    Map<String, Object> initiateCombat(String attackerFleetId, String defenderFleetId);

    Map<String, Object> executeCombatRound(String combatId);

    Map<String, Object> getCombatStatus(String combatId);

    List<Map<String, Object>> getShipTypes(String factionId);

    Map<String, Object> createFleet(String factionId, String fleetName, String shipTypeId, int count);

    List<Map<String, Object>> getFleets(String factionId);

    int calculateCombatPower(String fleetId);

    List<String> getActiveCombats();
}
