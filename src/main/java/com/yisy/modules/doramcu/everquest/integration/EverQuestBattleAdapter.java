package com.yisy.modules.doramcu.everquest.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * EverQuest battle adapter — provides EverQuest units to universal battles.
 */
@Component
public class EverQuestBattleAdapter implements ModuleBattleAdapter {

    private static final Map<String, String> UNIT_TYPES = new HashMap<>();
    private static final Map<String, GenericUnitType> GENERIC_MAP = new HashMap<>();
    private static final Map<String, String> MODEL_PATHS = new HashMap<>();
    private static final Map<String, String> TEXTURE_PATHS = new HashMap<>();

    static {
        UNIT_TYPES.put("FIGHTER", "Fighter");
        GENERIC_MAP.put("FIGHTER", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("FIGHTER", "/models/everquest/fighter.obj");
        TEXTURE_PATHS.put("FIGHTER", "/textures/everquest/fighter.png");

        UNIT_TYPES.put("MAGE", "Mage");
        GENERIC_MAP.put("MAGE", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("MAGE", "/models/everquest/mage.obj");
        TEXTURE_PATHS.put("MAGE", "/textures/everquest/mage.png");

        UNIT_TYPES.put("ROGUE", "Rogue");
        GENERIC_MAP.put("ROGUE", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("ROGUE", "/models/everquest/rogue.obj");
        TEXTURE_PATHS.put("ROGUE", "/textures/everquest/rogue.png");

        UNIT_TYPES.put("CLERIC", "Cleric");
        GENERIC_MAP.put("CLERIC", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("CLERIC", "/models/everquest/cleric.obj");
        TEXTURE_PATHS.put("CLERIC", "/textures/everquest/cleric.png");

        UNIT_TYPES.put("RANGER", "Ranger");
        GENERIC_MAP.put("RANGER", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("RANGER", "/models/everquest/ranger.obj");
        TEXTURE_PATHS.put("RANGER", "/textures/everquest/ranger.png");

        UNIT_TYPES.put("BARD", "Bard");
        GENERIC_MAP.put("BARD", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("BARD", "/models/everquest/bard.obj");
        TEXTURE_PATHS.put("BARD", "/textures/everquest/bard.png");

        UNIT_TYPES.put("NECROMANCER", "Necromancer");
        GENERIC_MAP.put("NECROMANCER", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("NECROMANCER", "/models/everquest/necromancer.obj");
        TEXTURE_PATHS.put("NECROMANCER", "/textures/everquest/necromancer.png");

        UNIT_TYPES.put("SHAMAN", "Shaman");
        GENERIC_MAP.put("SHAMAN", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("SHAMAN", "/models/everquest/shaman.obj");
        TEXTURE_PATHS.put("SHAMAN", "/textures/everquest/shaman.png");

        UNIT_TYPES.put("PALADIN", "Paladin");
        GENERIC_MAP.put("PALADIN", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("PALADIN", "/models/everquest/paladin.obj");
        TEXTURE_PATHS.put("PALADIN", "/textures/everquest/paladin.png");

        UNIT_TYPES.put("MONSTROSITY", "Monstrosity");
        GENERIC_MAP.put("MONSTROSITY", GenericUnitType.ARMORED);
        MODEL_PATHS.put("MONSTROSITY", "/models/everquest/monstrosity.obj");
        TEXTURE_PATHS.put("MONSTROSITY", "/textures/everquest/monstrosity.png");
    }

    @Override
    public String getModuleId() { return "EverQuest"; }

    @Override
    public String getModuleName() { return "EverQuest"; }

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
            case "erudites": return "#FFD700";
            case "pax": return "#0000FF";
            case "retributions": return "#FF0000";
            case "beasts": return "#00FF00";
            case "undead": return "#808080";
            default: return "#808080";
        }
    }
}
