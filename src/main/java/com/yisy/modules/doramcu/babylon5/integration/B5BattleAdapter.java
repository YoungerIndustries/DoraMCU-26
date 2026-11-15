package com.yisy.modules.doramcu.babylon5.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Babylon5 battle adapter — provides B5 units to universal battles.
 */
@Component
public class B5BattleAdapter implements ModuleBattleAdapter {

    private static final Map<String, String> UNIT_TYPES = new HashMap<>();
    private static final Map<String, GenericUnitType> GENERIC_MAP = new HashMap<>();
    private static final Map<String, String> MODEL_PATHS = new HashMap<>();
    private static final Map<String, String> TEXTURE_PATHS = new HashMap<>();

    static {
        UNIT_TYPES.put("CENTCOM", "CentCom Soldier");
        GENERIC_MAP.put("CENTCOM", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("CENTCOM", "/models/b5/centcom.obj");
        TEXTURE_PATHS.put("CENTCOM", "/textures/b5/centcom.png");

        UNIT_TYPES.put("NID_AGENT", "NID Agent");
        GENERIC_MAP.put("NID_AGENT", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("NID_AGENT", "/models/b5/nid_agent.obj");
        TEXTURE_PATHS.put("NID_AGENT", "/textures/b5/nid_agent.png");

        UNIT_TYPES.put("MINBARIN_WARRIOR", "Minbarin Warrior");
        GENERIC_MAP.put("MINBARIN_WARRIOR", GenericUnitType.ARMORED);
        MODEL_PATHS.put("MINBARIN_WARRIOR", "/models/b5/minbarin.obj");
        TEXTURE_PATHS.put("MINBARIN_WARRIOR", "/textures/b5/minbarin.png");

        UNIT_TYPES.put("WARSHIP", "Warship");
        GENERIC_MAP.put("WARSHIP", GenericUnitType.SPACECRAFT);
        MODEL_PATHS.put("WARSHIP", "/models/b5/warship.obj");
        TEXTURE_PATHS.put("WARSHIP", "/textures/b5/warship.png");

        UNIT_TYPES.put("DESTROYER", "Destroyer");
        GENERIC_MAP.put("DESTROYER", GenericUnitType.SPACECRAFT);
        MODEL_PATHS.put("DESTROYER", "/models/b5/destroyer.obj");
        TEXTURE_PATHS.put("DESTROYER", "/textures/b5/destroyer.png");
    }

    @Override
    public String getModuleId() { return "Babylon5"; }

    @Override
    public String getModuleName() { return "Babylon 5"; }

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
            case "earth": return "#0000FF";
            case "minbari": return "#FFD700";
            case "centauri": return "#FF0000";
            case "narn": return "#00FF00";
            case "shadow": return "#800080";
            default: return "#808080";
        }
    }
}
