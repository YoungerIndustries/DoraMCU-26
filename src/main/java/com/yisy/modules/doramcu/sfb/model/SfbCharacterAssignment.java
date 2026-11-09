package com.yisy.modules.doramcu.sfb.model;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;

import java.util.Map;

/**
 * Links a Captain's Log character to an SFB battle.
 * <p>
 * Tracks role (Admiral/Captain/Crew), assigned ship (for Captain/Crew),
 * assigned station (for Crew), and active modifiers.
 * </p>
 */
public class SfbCharacterAssignment {

    private Long characterId;
    private String characterName;
    private String battleId;
    private SfbRole role;
    private String shipId; // required for CAPTAIN and CREW
    private SfbCrewStationType station; // required for CREW
    private int side; // 0 or 1, for two-player battles
    private boolean active;
    private CaptainLogCharacter character;
    private Map<String, Integer> modifiers; // attribute/discipline -> bonus value

    public SfbCharacterAssignment() {
    }

    public SfbCharacterAssignment(Long characterId, String battleId, SfbRole role) {
        this.characterId = characterId;
        this.battleId = battleId;
        this.role = role;
    }

    public Long getCharacterId() { return characterId; }
    public void setCharacterId(Long characterId) { this.characterId = characterId; }

    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName; }

    public String getBattleId() { return battleId; }
    public void setBattleId(String battleId) { this.battleId = battleId; }

    public int getSide() { return side; }
    public void setSide(int side) { this.side = side; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public SfbRole getRole() { return role; }
    public void setRole(SfbRole role) { this.role = role; }

    public String getShipId() { return shipId; }
    public void setShipId(String shipId) { this.shipId = shipId; }

    public SfbCrewStationType getStation() { return station; }
    public void setStation(SfbCrewStationType station) { this.station = station; }

    public CaptainLogCharacter getCharacter() { return character; }
    public void setCharacter(CaptainLogCharacter character) { this.character = character; }

    public Map<String, Integer> getModifiers() { return modifiers; }
    public void setModifiers(Map<String, Integer> modifiers) { this.modifiers = modifiers; }

    /**
     * Validate the assignment.
     * - ADMIRAL: no ship required
     * - CAPTAIN: ship required
     * - CREW: ship and station required
     */
    public boolean isValid() {
        if (character == null) return false;
        if (character.getInjuryState() == CaptainLogCharacter.InjuryState.DOWN) return false;

        switch (role) {
            case ADMIRAL:
                return true;
            case CAPTAIN:
                return shipId != null && !shipId.isEmpty();
            case CREW:
                return shipId != null && !shipId.isEmpty() && station != null;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return "SfbCharacterAssignment{character=" + characterId +
                ", role=" + role + ", ship=" + shipId +
                ", station=" + station + "}";
    }
}
