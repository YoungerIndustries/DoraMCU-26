package com.yisy.modules.doramcu.sfb.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Star Fleet Battleforce battle adapter.
 */
@Component
public class SfbBattleAdapter implements ModuleBattleAdapter {

    private static final Map<String, String> UNIT_TYPES = new HashMap<>();
    private static final Map<String, GenericUnitType> GENERIC_MAP = new HashMap<>();
    private static final Map<String, String> MODEL_PATHS = new HashMap<>();
    private static final Map<String, String> TEXTURE_PATHS = new HashMap<>();

    static {
        UNIT_TYPES.put("SECURITY_TEAM", "Security Team");
        GENERIC_MAP.put("SECURITY_TEAM", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("SECURITY_TEAM", "/models/sfb/security.obj");
        TEXTURE_PATHS.put("SECURITY_TEAM", "/textures/sfb/security.png");

        UNIT_TYPES.put("SCIENCE_OFFICER", "Science Officer");
        GENERIC_MAP.put("SCIENCE_OFFICER", GenericUnitType.ENGINEER);
        MODEL_PATHS.put("SCIENCE_OFFICER", "/models/sfb/science.obj");
        TEXTURE_PATHS.put("SCIENCE_OFFICER", "/textures/sfb/science.png");

        UNIT_TYPES.put("CONSTANT_CLASS", "Constant Class");
        GENERIC_MAP.put("CONSTANT_CLASS", GenericUnitType.SPACECRAFT);
        MODEL_PATHS.put("CONSTANT_CLASS", "/models/sfb/constant.obj");
        TEXTURE_PATHS.put("CONSTANT_CLASS", "/textures/sfb/constant.png");

        UNIT_TYPES.put("DEFiant_CLASS", "Defiant Class");
        GENERIC_MAP.put("DEFiant_CLASS", GenericUnitType.SPACECRAFT);
        MODEL_PATHS.put("DEFiant_CLASS", "/models/sfb/defiant.obj");
        TEXTURE_PATHS.put("DEFiant_CLASS", "/textures/sfb/defiant.png");
    }

    @Override
    public String getModuleId() { return "SFB"; }

    @Override
    public String getModuleName() { return "Star Fleet Battleforce"; }

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
