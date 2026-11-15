package com.yisy.modules.doramcu.dnd5e.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DnD5e battle adapter — provides DnD units to universal battles.
 */
@Component
public class Dnd5eBattleAdapter implements ModuleBattleAdapter {

    private static final Map<String, String> UNIT_TYPES = new HashMap<>();
    private static final Map<String, GenericUnitType> GENERIC_MAP = new HashMap<>();
    private static final Map<String, String> MODEL_PATHS = new HashMap<>();
    private static final Map<String, String> TEXTURE_PATHS = new HashMap<>();

    static {
        UNIT_TYPES.put("FIGHTER", "Fighter");
        GENERIC_MAP.put("FIGHTER", GenericUnitType.ARMORED);
        MODEL_PATHS.put("FIGHTER", "/models/dnd5e/fighter.obj");
        TEXTURE_PATHS.put("FIGHTER", "/textures/dnd5e/fighter.png");

        UNIT_TYPES.put("WIZARD", "Wizard");
        GENERIC_MAP.put("WIZARD", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("WIZARD", "/models/dnd5e/wizard.obj");
        TEXTURE_PATHS.put("WIZARD", "/textures/dnd5e/wizard.png");

        UNIT_TYPES.put("ROGUE", "Rogue");
        GENERIC_MAP.put("ROGUE", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("ROGUE", "/models/dnd5e/rogue.obj");
        TEXTURE_PATHS.put("ROGUE", "/textures/dnd5e/rogue.png");

        UNIT_TYPES.put("CLERIC", "Cleric");
        GENERIC_MAP.put("CLERIC", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("CLERIC", "/models/dnd5e/cleric.obj");
        TEXTURE_PATHS.put("CLERIC", "/textures/dnd5e/cleric.png");

        UNIT_TYPES.put("DRAGON", "Dragon");
        GENERIC_MAP.put("DRAGON", GenericUnitType.AIRCRAFT);
        MODEL_PATHS.put("DRAGON", "/models/dnd5e/dragon.obj");
        TEXTURE_PATHS.put("DRAGON", "/textures/dnd5e/dragon.png");

        UNIT_TYPES.put("GOLEM", "Golem");
        GENERIC_MAP.put("GOLEM", GenericUnitType.ARMORED);
        MODEL_PATHS.put("GOLEM", "/models/dnd5e/golem.obj");
        TEXTURE_PATHS.put("GOLEM", "/textures/dnd5e/golem.png");
    }

    @Override
    public String getModuleId() { return "Dnd5e"; }

    @Override
    public String getModuleName() { return "Dungeons & Dragons 5e"; }

    @Override
    public Map<String, String> getAvailableUnitTypes() { return Collections.unmodifiableMap(UNIT_TYPES); }

    @Override
    public UniversalBattleUnit createUnit(String unitId, String unitName, String moduleUnitType, String faction, int techLevel) {
        GenericUnitType genericType = GENERIC_MAP.getOrDefault(moduleUnitType, GenericUnitType.INFANTRY);
        int scale = Math.max(1, techLevel / 2);
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
            case "good": return "#FFFF00";
            case "evil": return "#800080";
            case "neutral": return "#808080";
            default: return "#808080";
        }
    }
}
