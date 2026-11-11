package com.yisy.modules.doramcu.fourx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared faction model for the 4X framework.
 * <p>
 * A faction represents a playable or AI-controlled entity in a 4X game.
 * It tracks reputation with other factions, controlled sectors, fleet strength,
 * colony count, research points, and economic output.
 * </p>
 * <p>
 * This is a plain DTO — not a JPA entity. Each game module (B5, SFB, Star Trek 4X)
 * maps its own persistent entities to this shared model.
 * </p>
 */
public class FourxFaction {

    private String id;
    private String name;
    private String description;
    private String gameModule; // "Babylon5", "SFB", "StarTrek4X", etc.

    /** Combat doctrine: OFFENSIVE, DEFENSIVE, BALANCED, GUERRILLA. */
    private String combatStyle;

    /** Default diplomatic stance: HOSTILE, NEUTRAL, FRIENDLY, ALLIED. */
    private DiplomaticStance defaultStance = DiplomaticStance.NEUTRAL;

    /** Reputation scores with other factions (faction name -> score -100 to 100). */
    private Map<String, Integer> reputationMap = new HashMap<>();

    /** List of controlled sector IDs. */
    private List<Integer> controlledSectors;

    /** Total fleet strength. */
    private int fleetStrength;

    /** Total number of colonies. */
    private int colonyCount;

    /** Research points available. */
    private int researchPoints;

    /** Economic output per turn. */
    private int economyOutput;

    public FourxFaction() {
    }

    public FourxFaction(String id, String name, String gameModule) {
        this.id = id;
        this.name = name;
        this.gameModule = gameModule;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGameModule() { return gameModule; }
    public void setGameModule(String gameModule) { this.gameModule = gameModule; }

    public String getCombatStyle() { return combatStyle; }
    public void setCombatStyle(String combatStyle) { this.combatStyle = combatStyle; }

    public DiplomaticStance getDefaultStance() { return defaultStance; }
    public void setDefaultStance(DiplomaticStance defaultStance) { this.defaultStance = defaultStance; }

    public Map<String, Integer> getReputationMap() { return reputationMap; }
    public void setReputationMap(Map<String, Integer> reputationMap) { this.reputationMap = reputationMap; }

    public List<Integer> getControlledSectors() { return controlledSectors; }
    public void setControlledSectors(List<Integer> controlledSectors) { this.controlledSectors = controlledSectors; }

    public int getFleetStrength() { return fleetStrength; }
    public void setFleetStrength(int fleetStrength) { this.fleetStrength = fleetStrength; }

    public int getColonyCount() { return colonyCount; }
    public void setColonyCount(int colonyCount) { this.colonyCount = colonyCount; }

    public int getResearchPoints() { return researchPoints; }
    public void setResearchPoints(int researchPoints) { this.researchPoints = researchPoints; }

    public int getEconomyOutput() { return economyOutput; }
    public void setEconomyOutput(int economyOutput) { this.economyOutput = economyOutput; }

    /** Get reputation with another faction. */
    public int getReputation(String otherFaction) {
        return reputationMap.getOrDefault(otherFaction, 0);
    }

    /** Modify reputation with another faction. */
    public void modifyReputation(String otherFaction, int delta) {
        int current = reputationMap.getOrDefault(otherFaction, 0);
        reputationMap.put(otherFaction, Math.max(-100, Math.min(100, current + delta)));
    }

    /** Check if this faction is at war with another. */
    public boolean isAtWarWith(String otherFaction) {
        return getReputation(otherFaction) < -50;
    }

    /** Check if this faction is allied with another. */
    public boolean isAlliedWith(String otherFaction) {
        return getReputation(otherFaction) > 75;
    }

    public enum DiplomaticStance {
        HOSTILE,
        NEUTRAL,
        FRIENDLY,
        ALLIED
    }
}
