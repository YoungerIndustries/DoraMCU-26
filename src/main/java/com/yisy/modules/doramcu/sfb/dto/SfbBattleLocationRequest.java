package com.yisy.modules.doramcu.sfb.dto;

/**
 * Request to set battle location from galaxy.
 */
public class SfbBattleLocationRequest {
    private String battleId;
    private int galaxyId;
    private int sectorRow;
    private int sectorCol;
    private String systemName;

    public String getBattleId() { return battleId; }
    public void setBattleId(String battleId) { this.battleId = battleId; }
    public int getGalaxyId() { return galaxyId; }
    public void setGalaxyId(int galaxyId) { this.galaxyId = galaxyId; }
    public int getSectorRow() { return sectorRow; }
    public void setSectorRow(int sectorRow) { this.sectorRow = sectorRow; }
    public int getSectorCol() { return sectorCol; }
    public void setSectorCol(int sectorCol) { this.sectorCol = sectorCol; }
    public String getSystemName() { return systemName; }
    public void setSystemName(String systemName) { this.systemName = systemName; }
}
