package com.yisy.modules.doramcu.sfb.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a character assignment in SFB.
 */
public class SfbCharacterAssignment {
    public static final String HELM = "HELM";
    public static final String TACTICAL = "TACTICAL";
    public static final String ENGINEERING = "ENGINEERING";
    public static final String SENSORS = "SENSORS";
    public static final String MEDICAL = "MEDICAL";

    private Long characterId;
    private String aiCharacterId;
    private String shipId;
    private SfbCrewStationType station;
    private SfbRole role;
    private int side;
    private int shieldAllocation;
    private int weaponAllocation;
    private int specialSystemAllocation;
    private int ewAllocation;
    private int[] shieldReinforcement;
    private Map<String, Integer> modifiers;
    private String battleId;
    private String characterName;
    private boolean active;

    public SfbCharacterAssignment() {
        this.modifiers = new HashMap<>();
    }

    public SfbCharacterAssignment(Long characterId, String shipId, SfbCrewStationType station) {
        this.characterId = characterId;
        this.shipId = shipId;
        this.station = station;
        this.modifiers = new HashMap<>();
        this.active = true;
    }

    public Long getCharacterId() { return characterId; }
    public void setCharacterId(Long characterId) { this.characterId = characterId; }
    public String getAiCharacterId() { return aiCharacterId; }
    public void setAiCharacterId(String aiCharacterId) { this.aiCharacterId = aiCharacterId; }
    public String getShipId() { return shipId; }
    public void setShipId(String shipId) { this.shipId = shipId; }
    public SfbCrewStationType getStation() { return station; }
    public void setStation(SfbCrewStationType station) { this.station = station; }
    public SfbRole getRole() { return role; }
    public void setRole(SfbRole role) { this.role = role; }
    public int getSide() { return side; }
    public void setSide(int side) { this.side = side; }
    public int getShieldAllocation() { return shieldAllocation; }
    public void setShieldAllocation(int shieldAllocation) { this.shieldAllocation = shieldAllocation; }
    public int getWeaponAllocation() { return weaponAllocation; }
    public void setWeaponAllocation(int weaponAllocation) { this.weaponAllocation = weaponAllocation; }
    public int getSpecialSystemAllocation() { return specialSystemAllocation; }
    public void setSpecialSystemAllocation(int specialSystemAllocation) { this.specialSystemAllocation = specialSystemAllocation; }
    public int getEwAllocation() { return ewAllocation; }
    public void setEwAllocation(int ewAllocation) { this.ewAllocation = ewAllocation; }
    public int[] getShieldReinforcement() { return shieldReinforcement; }
    public void setShieldReinforcement(int[] shieldReinforcement) { this.shieldReinforcement = shieldReinforcement; }
    public Map<String, Integer> getModifiers() { return modifiers; }
    public void setModifiers(Map<String, Integer> modifiers) { this.modifiers = modifiers; }
    public String getBattleId() { return battleId; }
    public void setBattleId(String battleId) { this.battleId = battleId; }
    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
