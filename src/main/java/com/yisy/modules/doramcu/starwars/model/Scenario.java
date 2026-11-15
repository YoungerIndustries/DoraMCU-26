package com.yisy.modules.doramcu.starwars.model;

import java.util.List;
import java.util.Map;

/**
 * Represents a Star Wars battle scenario (e.g., Battle of Yavin, Battle of Hoth).
 * Supports rich data like setup forces, objectives, victory conditions, and playtime.
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
    String rebelObjective,
    String imperialObjective,
    List<String> rebelForces,
    List<String> imperialForces,
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
        if (rebelObjective == null) rebelObjective = "";
        if (imperialObjective == null) imperialObjective = "";
        if (rebelForces == null) rebelForces = List.of();
        if (imperialForces == null) imperialForces = List.of();
        if (victoryConditions == null) victoryConditions = Map.of();
        if (playtime == null) playtime = "";
    }
}
