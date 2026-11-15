package com.yisy.modules.doramcu.daoc.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Dark Age of Camelot battle adapter — provides DaOC units to universal battles.
 */
@Component
public class DaocBattleAdapter implements ModuleBattleAdapter {

    private static final Map<String, String> UNIT_TYPES = new HashMap<>();
    private static final Map<String, GenericUnitType> GENERIC_MAP = new HashMap<>();
    private static final Map<String, String> MODEL_PATHS = new HashMap<>();
    private static final Map<String, String> TEXTURE_PATHS = new HashMap<>();

    static {
        UNIT_TYPES.put("MARTYR", "Martyr");
        GENERIC_MAP.put("MARTYR", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("MARTYR", "/models/daoc/martyr.obj");
        TEXTURE_PATHS.put("MARTYR", "/textures/daoc/martyr.png");

        UNIT_TYPES.put("CHAMPION", "Champion");
        GENERIC_MAP.put("CHAMPION", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("CHAMPION", "/models/daoc/champion.obj");
        TEXTURE_PATHS.put("CHAMPION", "/textures/daoc/champion.png");

        UNIT_TYPES.put("WARRIOR", "Warrior");
        GENERIC_MAP.put("WARRIOR", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("WARRIOR", "/models/daoc/warrior.obj");
        TEXTURE_PATHS.put("WARRIOR", "/textures/daoc/warrior.png");

        UNIT_TYPES.put("MAGE", "Mage");
        GENERIC_MAP.put("MAGE", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("MAGE", "/models/daoc/mage.obj");
        TEXTURE_PATHS.put("MAGE", "/textures/daoc/mage.png");

        UNIT_TYPES.put("RANGER", "Ranger");
        GENERIC_MAP.put("RANGER", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("RANGER", "/models/daoc/ranger.obj");
        TEXTURE_PATHS.put("RANGER", "/textures/daoc/ranger.png");

        UNIT_TYPES.put("CLERIC", "Cleric");
        GENERIC_MAP.put("CLERIC", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("CLERIC", "/models/daoc/cleric.obj");
        TEXTURE_PATHS.put("CLERIC", "/textures/daoc/cleric.png");

        UNIT_TYPES.put("BARD", "Bard");
        GENERIC_MAP.put("BARD", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("BARD", "/models/daoc/bard.obj");
        TEXTURE_PATHS.put("BARD", "/textures/daoc/bard.png");

        UNIT_TYPES.put("NECROMANCER", "Necromancer");
        GENERIC_MAP.put("NECROMANCER", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("NECROMANCER", "/models/daoc/necromancer.obj");
        TEXTURE_PATHS.put("NECROMANCER", "/textures/daoc/necromancer.png");

        UNIT_TYPES.put("DRUID", "Druid");
        GENERIC_MAP.put("DRUID", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("DRUID", "/models/daoc/druid.obj");
        TEXTURE_PATHS.put("DRUID", "/textures/daoc/druid.png");

        UNIT_TYPES.put("ASSASSIN", "Assassin");
        GENERIC_MAP.put("ASSASSIN", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("ASSASSIN", "/models/daoc/assassin.obj");
        TEXTURE_PATHS.put("ASSASSIN", "/textures/daoc/assassin.png");
    }

    @Override
    public String getModuleId() { return "DaOC"; }

    @Override
    public String getModuleName() { return "Dark Age of Camelot"; }

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
            case "albion": return "#FFD700";
            case "midgard": return "#00FF00";
            case "hibernia": return "#FF0000";
            default: return "#808080";
        }
    }
}
