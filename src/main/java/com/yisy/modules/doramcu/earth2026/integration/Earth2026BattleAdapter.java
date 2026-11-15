package com.yisy.modules.doramcu.earth2026.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Earth2026 battle adapter — provides Earth2026 units to universal battles.
 */
@Component
public class Earth2026BattleAdapter implements ModuleBattleAdapter {

    private static final Map<String, String> UNIT_TYPES = new HashMap<>();
    private static final Map<String, GenericUnitType> GENERIC_MAP = new HashMap<>();
    private static final Map<String, String> MODEL_PATHS = new HashMap<>();
    private static final Map<String, String> TEXTURE_PATHS = new HashMap<>();

    static {
        UNIT_TYPES.put("INFANTRY", "Infantry");
        GENERIC_MAP.put("INFANTRY", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("INFANTRY", "/models/earth2026/infantry.obj");
        TEXTURE_PATHS.put("INFANTRY", "/textures/earth2026/infantry.png");

        UNIT_TYPES.put("MECHANIZED", "Mechanized");
        GENERIC_MAP.put("MECHANIZED", GenericUnitType.ARMORED);
        MODEL_PATHS.put("MECHANIZED", "/models/earth2026/mechanized.obj");
        TEXTURE_PATHS.put("MECHANIZED", "/textures/earth2026/mechanized.png");

        UNIT_TYPES.put("TANK", "Tank");
        GENERIC_MAP.put("TANK", GenericUnitType.ARMORED);
        MODEL_PATHS.put("TANK", "/models/earth2026/tank.obj");
        TEXTURE_PATHS.put("TANK", "/textures/earth2026/tank.png");

        UNIT_TYPES.put("ARTILLERY", "Artillery");
        GENERIC_MAP.put("ARTILLERY", GenericUnitType.ARTILLERY);
        MODEL_PATHS.put("ARTILLERY", "/models/earth2026/artillery.obj");
        TEXTURE_PATHS.put("ARTILLERY", "/textures/earth2026/artillery.png");

        UNIT_TYPES.put("AIRCRAFT", "Aircraft");
        GENERIC_MAP.put("AIRCRAFT", GenericUnitType.AIRCRAFT);
        MODEL_PATHS.put("AIRCRAFT", "/models/earth2026/aircraft.obj");
        TEXTURE_PATHS.put("AIRCRAFT", "/textures/earth2026/aircraft.png");

        UNIT_TYPES.put("SPECIAL_FORCES", "Special Forces");
        GENERIC_MAP.put("SPECIAL_FORCES", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("SPECIAL_FORCES", "/models/earth2026/special_forces.obj");
        TEXTURE_PATHS.put("SPECIAL_FORCES", "/textures/earth2026/special_forces.png");

        UNIT_TYPES.put("ENGINEER", "Engineer");
        GENERIC_MAP.put("ENGINEER", GenericUnitType.ENGINEER);
        MODEL_PATHS.put("ENGINEER", "/models/earth2026/engineer.obj");
        TEXTURE_PATHS.put("ENGINEER", "/textures/earth2026/engineer.png");
    }

    @Override
    public String getModuleId() { return "Earth2026"; }

    @Override
    public String getModuleName() { return "Earth 2026"; }

    @Override
    public Map<String, String> getAvailableUnitTypes() { return Collections.unmodifiableMap(UNIT_TYPES); }

    @Override
    public UniversalBattleUnit createUnit(String unitId, String unitName, String moduleUnitType, String faction, int techLevel) {
        GenericUnitType genericType = GENERIC_MAP.getOrDefault(moduleUnitType, GenericUnitType.INFANTRY);
        int scale = Math.max(1, techLevel / 3);
        return UniversalBattleUnit.builder(unitId, unitName, getModuleId(), moduleUnitType, genericType)
                .faction(faction)
                .attackValue(genericType.getBaseAttack() * scale)
                .defenseValue(genericType.getBaseDefense() * scale)
                .model3dPath(MODEL_PATHS.getOrDefault(moduleUnitType, null))
                .texturePath(TEXTURE_PATHS.getOrDefault(moduleUnitType, null))
                .build();
    }

    @Override
    public List<UniversalBattleUnit> createUnits(List<String> unitIds, String moduleUnitType, String faction, int techLevel) {
        return unitIds.stream()
                .map(unitId -> createUnit(unitId, moduleUnitType + "-" + unitId, moduleUnitType, faction, techLevel))
                .collect(Collectors.toList());
    }

    @Override
    public String getModelPath(String moduleUnitType) { return MODEL_PATHS.get(moduleUnitType); }

    @Override
    public String getTexturePath(String moduleUnitType) { return TEXTURE_PATHS.get(moduleUnitType); }

    @Override
    public Map<String, Integer> calculateCombatStats(UniversalBattleUnit unit, int terrainBonus, double commanderBonus) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("attack", unit.getAttackValue() + (int) (unit.getAttackValue() * commanderBonus));
        stats.put("defense", unit.getDefenseValue() + terrainBonus);
        stats.put("morale", Math.min(100, unit.getMorale() + (int) (commanderBonus * 10)));
        return stats;
    }

    @Override
    public String getFactionColor(String faction) {
        if (faction == null) return "#808080";
        switch (faction.toLowerCase()) {
            case "us": return "#0000FF";
            case "ru": return "#FF0000";
            case "cn": return "#FF8C00";
            case "eu": return "#000080";
            default: return "#808080";
        }
    }
}
