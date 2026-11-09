package com.yisy.modules.doramcu.sfb.model;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents an Admiral's fleet-level command.
 * <p>
 * Tracks the Admiral character, fleet-wide bonuses, and affected ships.
 * The Admiral provides fleet-wide bonuses based on Command/Leadership disciplines.
 * </p>
 */
public class SfbFleetCommand {

    private Long admiralCharacterId;
    private CaptainLogCharacter admiral;
    private String battleId;
    private List<String> affectedShipIds;
    private Map<String, Integer> fleetBonuses; // bonus type -> value

    public SfbFleetCommand() {
        this.affectedShipIds = new ArrayList<>();
    }

    public SfbFleetCommand(Long admiralCharacterId, String battleId) {
        this.admiralCharacterId = admiralCharacterId;
        this.battleId = battleId;
        this.affectedShipIds = new ArrayList<>();
    }

    public Long getAdmiralCharacterId() { return admiralCharacterId; }
    public void setAdmiralCharacterId(Long admiralCharacterId) { this.admiralCharacterId = admiralCharacterId; }

    public CaptainLogCharacter getAdmiral() { return admiral; }
    public void setAdmiral(CaptainLogCharacter admiral) { this.admiral = admiral; }

    public String getBattleId() { return battleId; }
    public void setBattleId(String battleId) { this.battleId = battleId; }

    public List<String> getAffectedShipIds() { return affectedShipIds; }
    public void setAffectedShipIds(List<String> affectedShipIds) { this.affectedShipIds = affectedShipIds; }

    public void addAffectedShip(String shipId) {
        if (!affectedShipIds.contains(shipId)) {
            affectedShipIds.add(shipId);
        }
    }

    public Map<String, Integer> getFleetBonuses() { return fleetBonuses; }
    public void setFleetBonuses(Map<String, Integer> fleetBonuses) { this.fleetBonuses = fleetBonuses; }

    /**
     * Get a specific fleet bonus value.
     */
    public int getBonus(String bonusType) {
        return fleetBonuses != null ? fleetBonuses.getOrDefault(bonusType, 0) : 0;
    }

    /**
     * Set a specific fleet bonus value.
     */
    public void setBonus(String bonusType, int value) {
        if (fleetBonuses == null) {
            fleetBonuses = new java.util.HashMap<>();
        }
        fleetBonuses.put(bonusType, value);
    }

    @Override
    public String toString() {
        return "SfbFleetCommand{admiral=" + admiralCharacterId +
                ", battle=" + battleId +
                ", ships=" + affectedShipIds.size() + "}";
    }
}
