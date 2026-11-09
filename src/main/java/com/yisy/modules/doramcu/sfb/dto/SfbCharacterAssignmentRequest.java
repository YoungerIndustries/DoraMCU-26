package com.yisy.modules.doramcu.sfb.dto;

import com.yisy.modules.doramcu.sfb.model.SfbCrewStationType;
import com.yisy.modules.doramcu.sfb.model.SfbRole;

import java.util.List;
import java.util.Map;

/**
 * Request to assign a character to an SFB battle.
 */
public class SfbCharacterAssignmentRequest {
    private String battleId;
    private Long characterId;
    private SfbRole role;
    private String shipId;
    private SfbCrewStationType station;

    public String getBattleId() { return battleId; }
    public void setBattleId(String battleId) { this.battleId = battleId; }
    public Long getCharacterId() { return characterId; }
    public void setCharacterId(Long characterId) { this.characterId = characterId; }
    public SfbRole getRole() { return role; }
    public void setRole(SfbRole role) { this.role = role; }
    public String getShipId() { return shipId; }
    public void setShipId(String shipId) { this.shipId = shipId; }
    public SfbCrewStationType getStation() { return station; }
    public void setStation(SfbCrewStationType station) { this.station = station; }
}
