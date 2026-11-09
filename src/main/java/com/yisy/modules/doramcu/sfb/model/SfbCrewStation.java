package com.yisy.modules.doramcu.sfb.model;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;

import java.util.Map;

/**
 * Represents a crew member's station assignment on an SFB ship.
 * <p>
 * Tracks character, station type, and performance modifiers.
 * Character discipline bonuses apply to that system's operations.
 * </p>
 */
public class SfbCrewStation {

    private Long characterId;
    private CaptainLogCharacter character;
    private String shipId;
    private SfbCrewStationType stationType;
    private Map<String, Integer> performanceModifiers; // modifier type -> value

    public SfbCrewStation() {
    }

    public SfbCrewStation(Long characterId, String shipId, SfbCrewStationType stationType) {
        this.characterId = characterId;
        this.shipId = shipId;
        this.stationType = stationType;
    }

    public Long getCharacterId() { return characterId; }
    public void setCharacterId(Long characterId) { this.characterId = characterId; }

    public CaptainLogCharacter getCharacter() { return character; }
    public void setCharacter(CaptainLogCharacter character) { this.character = character; }

    public String getShipId() { return shipId; }
    public void setShipId(String shipId) { this.shipId = shipId; }

    public SfbCrewStationType getStationType() { return stationType; }
    public void setStationType(SfbCrewStationType stationType) { this.stationType = stationType; }

    public Map<String, Integer> getPerformanceModifiers() { return performanceModifiers; }
    public void setPerformanceModifiers(Map<String, Integer> performanceModifiers) {
        this.performanceModifiers = performanceModifiers;
    }

    /**
     * Get a specific performance modifier value.
     */
    public int getModifier(String modifierType) {
        return performanceModifiers != null ?
                performanceModifiers.getOrDefault(modifierType, 0) : 0;
    }

    /**
     * Set a specific performance modifier value.
     */
    public void setModifier(String modifierType, int value) {
        if (performanceModifiers == null) {
            performanceModifiers = new java.util.HashMap<>();
        }
        performanceModifiers.put(modifierType, value);
    }

    /**
     * Validate the station assignment.
     */
    public boolean isValid() {
        if (character == null) return false;
        if (character.getInjuryState() == CaptainLogCharacter.InjuryState.DOWN) return false;
        return shipId != null && !shipId.isEmpty() && stationType != null;
    }

    @Override
    public String toString() {
        return "SfbCrewStation{character=" + characterId +
                ", ship=" + shipId +
                ", station=" + stationType + "}";
    }
}
