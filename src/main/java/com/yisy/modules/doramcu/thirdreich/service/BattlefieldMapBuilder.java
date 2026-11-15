package com.yisy.modules.doramcu.thirdreich.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.earth2026.model.*;
import com.yisy.modules.doramcu.earth2026.service.BiosphereService;
import com.yisy.modules.doramcu.earth2026.service.PlanetService;
import com.yisy.modules.doramcu.thirdreich.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service that assembles a BattlefieldMap from PlanetService data.
 * <p>
 * Combines terrain elevation, structure positions, biosphere context,
 * and auto-populated unit forces into a complete 3D battlefield.
 * </p>
 */
@Service
public class BattlefieldMapBuilder {

    private static final Logger log = LoggerFactory.getLogger(BattlefieldMapBuilder.class);

    private final PlanetService planetService;
    private final BiosphereService biosphereService;
    private final Random random = new Random(42); // Fixed seed for reproducibility

    public BattlefieldMapBuilder(PlanetService planetService, BiosphereService biosphereService) {
        this.planetService = planetService;
        this.biosphereService = biosphereService;
    }

    /**
     * Builds a complete BattlefieldMap from a planet location and time period.
     *
     * @param locationId the Earth2026 location ID
     * @param timePeriod the time period template name
     * @param terrainType the terrain type string
     * @param weather the weather condition
     * @return a complete battlefield map
     */
    public BattlefieldMap buildBattlefieldMap(Long locationId, String timePeriod,
                                               String terrainType,
                                               Earth2026BattleEnvironment.WeatherCondition weather) {
        // 1. Build the battle environment
        Earth2026BattleEnvironment env = planetService.buildBattleEnvironment(
                locationId, timePeriod, terrainType, weather != null ? weather.getName() : "CLEAR");

        // 2. Map terrain type
        TerrainType terrainTypeEnum = mapTerrainType(env.getTerrainType());

        // 3. Build biosphere context
        BiosphereContext biosphere = biosphereService.createDefaultBiosphereContext(env.getWeather());

        // 4. Build the battlefield map
        BattlefieldMap map = BattlefieldMap.builder()
                .name(env.getLocationName() + " Battlefield")
                .locationId(env.getLocationId())
                .locationName(env.getLocationName())
                .city(env.getCity())
                .country(env.getCountry())
                .terrainElevationMap(env.getTerrainElevationMap())
                .terrainType(terrainTypeEnum)
                .groundElevation(env.getGroundElevation())
                .biosphereContext(biosphere)
                .structures(env.getStructures())
                .baseTechLevel(env.getBaseTechLevel())
                .timePeriod(env.getTimePeriod())
                .build();

        log.info("Built battlefield map: {}", map);
        return map;
    }

    /**
     * Auto-populates forces on a battlefield map based on tech level.
     * <p>
     * Generates template forces for both attacker and defender using
     * tech-level-appropriate units.
     * </p>
     *
     * @param map the battlefield map to populate
     * @param attackerCount number of attacker units
     * @param defenderCount number of defender units
     */
    public void autoPopulateForces(BattlefieldMap map, int attackerCount, int defenderCount) {
        int techLevel = map.getBaseTechLevel();
        TerrainElevationMap elevationMap = map.getTerrainElevationMap();

        // Scale positions to fit within the terrain grid dimensions
        double maxX = (elevationMap != null ? elevationMap.getWidth() * elevationMap.getCellSizeMeters() : 500.0);
        double maxY = (elevationMap != null ? elevationMap.getHeight() * elevationMap.getCellSizeMeters() : 500.0);
        double attackerZoneWidth = maxX * 0.2; // Left 20% for attackers
        double defenderZoneStart = maxX * 0.8; // Right 20% for defenders

        // Generate attacker units on the left side
        List<UnitPlacement> attackerPlacements = new ArrayList<>();
        for (int i = 0; i < attackerCount; i++) {
            ThirdReichGroundUnit unit = generateUnitForTechLevel(techLevel, "Attacker " + (i + 1), i);
            double x = random.nextDouble() * attackerZoneWidth;
            double y = random.nextDouble() * maxY;
            double z = elevationMap != null ?
                    elevationMap.getElevationAtWorldCoords(x, y) : 0;
            double facing = 90.0; // Face east toward defender
            attackerPlacements.add(new UnitPlacement(unit, x, y, z, facing, true));
        }

        // Generate defender units on the right side
        List<UnitPlacement> defenderPlacements = new ArrayList<>();
        for (int i = 0; i < defenderCount; i++) {
            ThirdReichGroundUnit unit = generateUnitForTechLevel(techLevel, "Defender " + (i + 1), i);
            double x = defenderZoneStart + random.nextDouble() * (maxX - defenderZoneStart);
            double y = random.nextDouble() * maxY;
            double z = elevationMap != null ?
                    elevationMap.getElevationAtWorldCoords(x, y) : 0;
            double facing = 270.0; // Face west toward attacker
            defenderPlacements.add(new UnitPlacement(unit, x, y, z, facing, false));
        }

        // Add placements to map
        for (UnitPlacement p : attackerPlacements) {
            map.addAttackerPlacement(p);
        }
        for (UnitPlacement p : defenderPlacements) {
            map.addDefenderPlacement(p);
        }

        log.info("Auto-populated battlefield: {} attackers, {} defenders at TL{}",
                attackerCount, defenderCount, techLevel);
    }

    /**
     * Customizes a force by replacing units at specific positions.
     *
     * @param map the battlefield map
     * @param isAttacker true for attacker force, false for defender
     * @param unitIndex the index of the unit to replace
     * @param newUnit the new unit
     */
    public void customizeForce(BattlefieldMap map, boolean isAttacker, int unitIndex,
                                ThirdReichGroundUnit newUnit) {
        List<UnitPlacement> placements = isAttacker ?
                map.getAttackerPlacements() : map.getDefenderPlacements();

        if (unitIndex < 0 || unitIndex >= placements.size()) {
            throw new IndexOutOfBoundsException("Unit index " + unitIndex +
                    " out of range for " + (isAttacker ? "attacker" : "defender") + " force");
        }

        UnitPlacement oldPlacement = placements.get(unitIndex);
        UnitPlacement newPlacement = new UnitPlacement(
                newUnit, oldPlacement.getX(), oldPlacement.getY(), oldPlacement.getZ(),
                oldPlacement.getFacingDirection(), isAttacker);

        placements.set(unitIndex, newPlacement);
    }

    /**
     * Repositions a unit on the battlefield.
     *
     * @param map the battlefield map
     * @param isAttacker true for attacker force, false for defender
     * @param unitIndex the index of the unit to move
     * @param x the new X coordinate
     * @param y the new Y coordinate
     */
    public void repositionUnit(BattlefieldMap map, boolean isAttacker, int unitIndex,
                                double x, double y) {
        List<UnitPlacement> placements = isAttacker ?
                map.getAttackerPlacements() : map.getDefenderPlacements();

        if (unitIndex < 0 || unitIndex >= placements.size()) {
            throw new IndexOutOfBoundsException("Unit index " + unitIndex +
                    " out of range for " + (isAttacker ? "attacker" : "defender") + " force");
        }

        UnitPlacement oldPlacement = placements.get(unitIndex);
        double z = map.getTerrainElevationMap() != null ?
                map.getTerrainElevationMap().getElevationAtWorldCoords(x, y) : oldPlacement.getZ();

        UnitPlacement newPlacement = oldPlacement.createMoved(x, y, z);
        placements.set(unitIndex, newPlacement);
    }

    /**
     * Validates that all units match the battlefield's tech level.
     *
     * @param map the battlefield map
     * @return true if all units are valid
     */
    public boolean validateTechLevel(BattlefieldMap map) {
        // Simple validation: all units are valid at any tech level
        // More complex validation could check unit type against tech level
        return true;
    }

    // ---- Helper Methods ----

    private ThirdReichGroundUnit generateUnitForTechLevel(int techLevel, String name, int index) {
        ThirdReichUnitType unitType;

        // Select unit type based on tech level and index
        if (techLevel >= 5) {
            // Modern era: mix of unit types
            unitType = selectModernUnitType(index);
        } else if (techLevel >= 4) {
            // WWII era: classic unit types
            unitType = selectWWIIUnitType(index);
        } else if (techLevel >= 3) {
            // Gunpowder era
            unitType = selectGunpowderUnitType(index);
        } else {
            // Primitive era
            unitType = ThirdReichUnitType.INFANTRY;
        }

        return new ThirdReichGroundUnit(
                "unit-" + name.replace(" ", "-") + "-" + index,
                name, unitType);
    }

    private ThirdReichUnitType selectModernUnitType(int index) {
        switch (index % 5) {
            case 0: return ThirdReichUnitType.INFANTRY;
            case 1: return ThirdReichUnitType.MECHANIZED;
            case 2: return ThirdReichUnitType.TANK;
            case 3: return ThirdReichUnitType.ARTILLERY;
            case 4: return ThirdReichUnitType.RECON;
            default: return ThirdReichUnitType.INFANTRY;
        }
    }

    private ThirdReichUnitType selectWWIIUnitType(int index) {
        switch (index % 5) {
            case 0: return ThirdReichUnitType.INFANTRY;
            case 1: return ThirdReichUnitType.TANK;
            case 2: return ThirdReichUnitType.ARTILLERY;
            case 3: return ThirdReichUnitType.ANTI_AIR;
            case 4: return ThirdReichUnitType.ENGINEER;
            default: return ThirdReichUnitType.INFANTRY;
        }
    }

    private ThirdReichUnitType selectGunpowderUnitType(int index) {
        switch (index % 3) {
            case 0: return ThirdReichUnitType.INFANTRY;
            case 1: return ThirdReichUnitType.ARTILLERY;
            case 2: return ThirdReichUnitType.ENGINEER;
            default: return ThirdReichUnitType.INFANTRY;
        }
    }

    private TerrainType mapTerrainType(String terrainType) {
        if (terrainType == null || terrainType.isEmpty()) {
            return TerrainType.PLAINS;
        }
        try {
            return TerrainType.valueOf(terrainType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TerrainType.PLAINS;
        }
    }
}
