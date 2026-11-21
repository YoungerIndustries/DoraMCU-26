package com.yisy.modules.doramcu.lord_of_the_rings.model;
import com.yisy.modules.doramcu.model.players.Player;

import java.util.List;
import java.util.Map;

/**
 * Represents a Lord of the Rings battle scenario (e.g., Battle of Helm's Deep, Pelennor Fields).
 */
public record Scenario(
    String id,
    String name,
    String description,
    String era,
    String location,
    String difficulty,
    int turns,
    List<String> specialRules,
    Map<String, Integer> factionForces,
    String victoryCondition,
    String defeatCondition,
    String map,
    int players,
    String sourceGame,
    String player1Faction,
    String player2Faction,
    String freePeoplesObjective,
    String forcesOfDarknessObjective,
    List<String> freePeoplesForces,
    List<String> forcesOfDarknessForces,
    Map<String, String> victoryConditions,
    String playtime
) {
    public Scenario {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Scenario id must not be blank");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Scenario name must not be blank");
        if (description == null) description = "";
        if (era == null) era = "";
        if (location == null) location = "";
        if (difficulty == null) difficulty = "";
        if (turns <= 0) turns = 6;
        if (specialRules == null) specialRules = List.of();
        if (factionForces == null) factionForces = Map.of();
        if (victoryCondition == null) victoryCondition = "";
        if (defeatCondition == null) defeatCondition = "";
        if (map == null) map = "";
        if (players <= 0) players = 2;
        if (sourceGame == null) sourceGame = "";
        if (player1Faction == null) player1Faction = "";
        if (player2Faction == null) player2Faction = "";
        if (freePeoplesObjective == null) freePeoplesObjective = "";
        if (forcesOfDarknessObjective == null) forcesOfDarknessObjective = "";
        if (freePeoplesForces == null) freePeoplesForces = List.of();
        if (forcesOfDarknessForces == null) forcesOfDarknessForces = List.of();
        if (victoryConditions == null) victoryConditions = Map.of();
        if (playtime == null) playtime = "";
    }
}
