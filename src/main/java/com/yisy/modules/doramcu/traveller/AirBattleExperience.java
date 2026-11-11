package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.List;

/**
 * Experience entry for air battles.
 */
public class AirBattleExperience {
    private String id;
    private String battleId;
    private String airForceName;
    private String nationality;
    private String altitude;
    private int losses;
    private String oddsRatio;
    private String crtResult;
    private List<String> spacecraftNames = new ArrayList<>();
    private String narrative;
    private long timestamp;

    public AirBattleExperience() {}

    public AirBattleExperience(String battleId, String airForceName, String nationality) {
        this.id = "exp-" + System.currentTimeMillis();
        this.battleId = battleId;
        this.airForceName = airForceName;
        this.nationality = nationality;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getBattleId() { return battleId; }
    public String getAirForceName() { return airForceName; }
    public String getNationality() { return nationality; }
    public String getAltitude() { return altitude; }
    public void setAltitude(String altitude) { this.altitude = altitude; }
    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }
    public String getOddsRatio() { return oddsRatio; }
    public void setOddsRatio(String oddsRatio) { this.oddsRatio = oddsRatio; }
    public String getCrtResult() { return crtResult; }
    public void setCrtResult(String crtResult) { this.crtResult = crtResult; }
    public List<String> getSpacecraftNames() { return spacecraftNames; }
    public String getNarrative() { return narrative; }
    public void setNarrative(String narrative) { this.narrative = narrative; }
    public long getTimestamp() { return timestamp; }
}
