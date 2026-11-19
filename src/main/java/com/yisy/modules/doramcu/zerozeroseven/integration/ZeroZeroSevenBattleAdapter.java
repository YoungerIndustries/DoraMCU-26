package com.yisy.modules.doramcu.zerozeroseven.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 007 Espionage battle adapter — provides 007 units to universal battles.
 */
@Component("zeroZeroSevenBattleAdapter_0")
    @Component
public class ZeroZeroSevenBattleAdapter implements ModuleBattleAdapter {

    private static final Map<String, String> UNIT_TYPES = new HashMap<>();
    private static final Map<String, GenericUnitType> GENERIC_MAP = new HashMap<>();
    private static final Map<String, String> MODEL_PATHS = new HashMap<>();
    private static final Map<String, String> TEXTURE_PATHS = new HashMap<>();

    static {
        UNIT_TYPES.put("AGENT", "Secret Agent");
        GENERIC_MAP.put("AGENT", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("AGENT", "/models/007/agent.obj");
        TEXTURE_PATHS.put("AGENT", "/textures/007/agent.png");

        UNIT_TYPES.put("ASSASSIN", "Assassin");
        GENERIC_MAP.put("ASSASSIN", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("ASSASSIN", "/models/007/assassin.obj");
        TEXTURE_PATHS.put("ASSASSIN", "/textures/007/assassin.png");

        UNIT_TYPES.put("FIELD_AGENT", "Field Agent");
        GENERIC_MAP.put("FIELD_AGENT", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("FIELD_AGENT", "/models/007/field_agent.obj");
        TEXTURE_PATHS.put("FIELD_AGENT", "/textures/007/field_agent.png");

        UNIT_TYPES.put("Q_BRANCH", "Q Branch Tech");
        GENERIC_MAP.put("Q_BRANCH", GenericUnitType.ENGINEER);
        MODEL_PATHS.put("Q_BRANCH", "/models/007/q_branch.obj");
        TEXTURE_PATHS.put("Q_BRANCH", "/textures/007/q_branch.png");

        UNIT_TYPES.put("M", "M Commander");
        GENERIC_MAP.put("M", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("M", "/models/007/m_commander.obj");
        TEXTURE_PATHS.put("M", "/textures/007/m_commander.png");
    }

    @Override
    public String getModuleId() { return "ZeroZeroSeven"; }

    @Override
    public String getModuleName() { return "007 Espionage"; }

    @Override
    public Map<String, String> getAvailableUnitTypes() { return Collections.unmodifiableMap(UNIT_TYPES); }

    @Override
    public UniversalBattleUnit createUnit(String unitId, String unitName, String moduleUnitType, String faction, int techLevel) {
        GenericUnitType genericType = GENERIC_MAP.getOrDefault(moduleUnitType, GenericUnitType.SPECIAL_FORCES);
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
            case "mi6": return "#000000";
            case "svr": return "#FF0000";
            case "syndicate": return "#800080";
            default: return "#808080";
        }
    }
}
