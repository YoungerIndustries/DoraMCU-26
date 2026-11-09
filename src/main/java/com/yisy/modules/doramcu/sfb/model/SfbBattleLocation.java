package com.yisy.modules.doramcu.sfb.model;

import com.yisy.modules.doramcu.sfb.service.SfbGalaxyIntegration;

/**
 * Links an SFB scenario to a Captain's Log galaxy sector and star system.
 * <p>
 * Battle locations map to sectors and star systems from GalaxyExplorationService.
 * This connects exploration history to tactical combat.
 * </p>
 */
public class SfbBattleLocation {

    private String battleId;
    private int galaxyId;
    private int sectorRow;
    private int sectorCol;
    private String systemName;
    private String sectorName;
    private String terrainType; // e.g., "nebula", "asteroid", "normal"
    private SfbGalaxyIntegration.TerrainEffect terrainEffect;
    private double sensorRangeModifier; // e.g., 0.5 for nebula
    private double collisionRisk; // e.g., 0.1 for asteroid field

    public SfbBattleLocation() {
        this.sensorRangeModifier = 1.0;
        this.collisionRisk = 0.0;
    }

    public SfbBattleLocation(int galaxyId, int sectorRow, int sectorCol, String systemName) {
        this.galaxyId = galaxyId;
        this.sectorRow = sectorRow;
        this.sectorCol = sectorCol;
        this.systemName = systemName;
        this.sensorRangeModifier = 1.0;
        this.collisionRisk = 0.0;
    }

    public int getGalaxyId() { return galaxyId; }
    public void setGalaxyId(int galaxyId) { this.galaxyId = galaxyId; }

    public int getSectorRow() { return sectorRow; }
    public void setSectorRow(int sectorRow) { this.sectorRow = sectorRow; }

    public int getSectorCol() { return sectorCol; }
    public void setSectorCol(int sectorCol) { this.sectorCol = sectorCol; }

    public String getSystemName() { return systemName; }
    public void setSystemName(String systemName) { this.systemName = systemName; }

    public String getSectorName() { return sectorName; }
    public void setSectorName(String sectorName) { this.sectorName = sectorName; }

    public String getTerrainType() { return terrainType; }
    public void setTerrainType(String terrainType) { this.terrainType = terrainType; }

    public String getBattleId() { return battleId; }
    public void setBattleId(String battleId) { this.battleId = battleId; }

    public SfbGalaxyIntegration.TerrainEffect getTerrainEffect() { return terrainEffect; }
    public void setTerrainEffect(SfbGalaxyIntegration.TerrainEffect terrainEffect) { this.terrainEffect = terrainEffect; }

    public double getSensorRangeModifier() { return sensorRangeModifier; }
    public void setSensorRangeModifier(double sensorRangeModifier) { this.sensorRangeModifier = sensorRangeModifier; }

    public double getCollisionRisk() { return collisionRisk; }
    public void setCollisionRisk(double collisionRisk) { this.collisionRisk = collisionRisk; }

    /**
     * Get a unique identifier for this location.
     */
    public String getLocationId() {
        return galaxyId + "-" + sectorRow + "-" + sectorCol;
    }

    @Override
    public String toString() {
        return "SfbBattleLocation{galaxy=" + galaxyId + ", sector=(" + sectorRow + "," + sectorCol + ")" +
                ", system=" + systemName + ", terrain=" + terrainType + "}";
    }
}
