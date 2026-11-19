package com.yisy.modules.doramcu.star_trek_4x.integration;
import com.yisy.modules.doramcu.techlevel.TechLevel;


import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Star Trek 4X battle adapter.
 */
@Component("starTrekBattleAdapter_star_trek_4x_integration_StarTrekBattleAdapter")
    @Component
public class StarTrekBattleAdapter implements ModuleBattleAdapter {

    private static final Map<String, String> UNIT_TYPES = new HashMap<>();
    private static final Map<String, GenericUnitType> GENERIC_MAP = new HashMap<>();
    private static final Map<String, String> MODEL_PATHS = new HashMap<>();
    private static final Map<String, String> TEXTURE_PATHS = new HashMap<>();

    static {
        UNIT_TYPES.put("PHASER_RIFLEMAN", "Phaser Rifleman");
        GENERIC_MAP.put("PHASER_RIFLEMAN", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("PHASER_RIFLEMAN", "/models/startrek/phaser_rifleman.obj");
        TEXTURE_PATHS.put("PHASER_RIFLEMAN", "/textures/startrek/phaser_rifleman.png");

        UNIT_TYPES.put("ENGINEER", "Engineer");
        GENERIC_MAP.put("ENGINEER", GenericUnitType.ENGINEER);
        MODEL_PATHS.put("ENGINEER", "/models/startrek/engineer.obj");
        TEXTURE_PATHS.put("ENGINEER", "/textures/startrek/engineer.png");

        UNIT_TYPES.put("ENTERPRISE", "Enterprise Class");
        GENERIC_MAP.put("ENTERPRISE", GenericUnitType.SPACECRAFT);
        MODEL_PATHS.put("ENTERPRISE", "/models/startrek/enterprise.obj");
        TEXTURE_PATHS.put("ENTERPRISE", "/textures/startrek/enterprise.png");

        UNIT_TYPES.put("DEFIANT", "Defiant Class");
        GENERIC_MAP.put("DEFIANT", GenericUnitType.SPACECRAFT);
        MODEL_PATHS.put("DEFIANT", "/models/startrek/defiant.obj");
        TEXTURE_PATHS.put("DEFIANT", "/textures/startrek/defiant.png");
    }

    @Override
    public String getModuleId() { return "StarTrek4x"; }

    @Override
    public String getModuleName() { return "Star Trek 4X"; }

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
            case "federation": return "#FF8C00";
            case "klingon": return "#8B0000";
            case "romulan": return "#4B0082";
            default: return "#808080";
        }
    }
}
