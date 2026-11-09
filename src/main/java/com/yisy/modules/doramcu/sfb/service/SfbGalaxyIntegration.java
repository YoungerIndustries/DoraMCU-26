package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.sfb.model.SfbBattleLocation;
import com.yisy.modules.doramcu.sfb.model.SfbScenario;

import java.util.*;

/**
 * Connects SFB battles to the Captain's Log galaxy.
 * <p>
 * Uses GalaxyExplorationService to look up battle locations,
 * apply sector-based terrain effects, and track battle outcomes.
 * </p>
 */
public class SfbGalaxyIntegration {

    /**
     * Terrain effects that can be applied based on sector type.
     */
    public enum TerrainEffect {
        NONE("No terrain effects"),
        NEBULA("Nebula: reduced sensor range"),
        ASTEROID_FIELD("Asteroid field: collision risk"),
        ION_STORM("Ion storm: EW interference"),
        GRAVITY_WELL("Gravity well: movement penalty");

        private final String description;

        TerrainEffect(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Simulated galaxy data
    private final Map<String, TerrainEffect> sectorTerrain = new HashMap<>();
    private final Map<String, List<SfbBattleLocation>> battleLocations = new HashMap<>();

    /**
     * Set battle location from galaxy sector.
     */
    public SfbBattleLocation setBattleLocation(String battleId, int galaxyId,
                                                int sectorRow, int sectorCol,
                                                String systemName) {
        SfbBattleLocation location = new SfbBattleLocation();
        location.setBattleId(battleId);
        location.setGalaxyId(galaxyId);
        location.setSectorRow(sectorRow);
        location.setSectorCol(sectorCol);
        location.setSystemName(systemName);

        // Determine terrain effect based on sector
        String sectorKey = sectorRow + "," + sectorCol;
        TerrainEffect terrain = sectorTerrain.getOrDefault(sectorKey, TerrainEffect.NONE);
        location.setTerrainEffect(terrain);

        // Apply terrain effects
        applyTerrainEffects(location);

        // Track battle location
        battleLocations.computeIfAbsent(battleId, k -> new ArrayList<>()).add(location);

        return location;
    }

    /**
     * Apply terrain effects based on sector type.
     */
    public void applyTerrainEffects(SfbBattleLocation location) {
        // Terrain effects are applied during combat via the combat engine
        // This method just sets up the location metadata
    }

    /**
     * Get terrain effect for a sector.
     */
    public TerrainEffect getTerrainEffect(int sectorRow, int sectorCol) {
        String sectorKey = sectorRow + "," + sectorCol;
        return sectorTerrain.getOrDefault(sectorKey, TerrainEffect.NONE);
    }

    /**
     * Set terrain effect for a sector (for setup purposes).
     */
    public void setSectorTerrain(int sectorRow, int sectorCol, TerrainEffect terrain) {
        String sectorKey = sectorRow + "," + sectorCol;
        sectorTerrain.put(sectorKey, terrain);
    }

    /**
     * Get battle location for a battle.
     */
    public SfbBattleLocation getBattleLocation(String battleId) {
        List<SfbBattleLocation> locations = battleLocations.get(battleId);
        if (locations != null && !locations.isEmpty()) {
            return locations.get(0);
        }
        return null;
    }

    /**
     * List battles in a sector.
     */
    public List<String> getBattlesInSector(int sectorRow, int sectorCol) {
        String sectorKey = sectorRow + "," + sectorCol;
        List<String> battles = new ArrayList<>();

        for (Map.Entry<String, List<SfbBattleLocation>> entry : battleLocations.entrySet()) {
            for (SfbBattleLocation location : entry.getValue()) {
                if (location.getSectorRow() == sectorRow && location.getSectorCol() == sectorCol) {
                    battles.add(entry.getKey());
                }
            }
        }

        return battles;
    }

    /**
     * Track battle outcome in galaxy history.
     */
    public void trackBattleOutcome(String battleId, int winningSide, int turnsPlayed) {
        // In production, this would update the galaxy history
        // For now, just store the outcome metadata
    }

    /**
     * Get sensor range modifier based on terrain.
     */
    public int getSensorRangeModifier(SfbBattleLocation location) {
        if (location == null) return 0;

        switch (location.getTerrainEffect()) {
            case NEBULA:
                return -2; // Reduced sensor range in nebula
            case ION_STORM:
                return -1; // Some interference
            default:
                return 0;
        }
    }

    /**
     * Get movement penalty based on terrain.
     */
    public int getMovementPenalty(SfbBattleLocation location) {
        if (location == null) return 0;

        switch (location.getTerrainEffect()) {
            case ASTEROID_FIELD:
                return 1; // Must slow down in asteroid fields
            case GRAVITY_WELL:
                return 2; // Significant movement penalty
            default:
                return 0;
        }
    }
}
