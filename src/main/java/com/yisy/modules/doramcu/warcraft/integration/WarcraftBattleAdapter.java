package com.yisy.modules.doramcu.warcraft.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Warcraft battle adapter — provides Warcraft units to universal battles.
 */
@Component
public class WarcraftBattleAdapter implements ModuleBattleAdapter {

    private static final Map<String, String> UNIT_TYPES = new HashMap<>();
    private static final Map<String, GenericUnitType> GENERIC_MAP = new HashMap<>();
    private static final Map<String, String> MODEL_PATHS = new HashMap<>();
    private static final Map<String, String> TEXTURE_PATHS = new HashMap<>();

    static {
        UNIT_TYPES.put("HUMAN_FOOTMAN", "Human Footman");
        GENERIC_MAP.put("HUMAN_FOOTMAN", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("HUMAN_FOOTMAN", "/models/warcraft/humanfootman.obj");
        TEXTURE_PATHS.put("HUMAN_FOOTMAN", "/textures/warcraft/humanfootman.png");

        UNIT_TYPES.put("ORC_GRUNT", "Orc Grunt");
        GENERIC_MAP.put("ORC_GRUNT", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("ORC_GRUNT", "/models/warcraft/orcgrunt.obj");
        TEXTURE_PATHS.put("ORC_GRUNT", "/textures/warcraft/orcgrunt.png");

        UNIT_TYPES.put("UNDEAD_SKELETAL", "Skeletal Warrior");
        GENERIC_MAP.put("UNDEAD_SKELETAL", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("UNDEAD_SKELETAL", "/models/warcraft/skeletal.obj");
        TEXTURE_PATHS.put("UNDEAD_SKELETAL", "/textures/warcraft/skeletal.png");

        UNIT_TYPES.put("NIGHT_ELF_DRUID", "Night Elf Druid");
        GENERIC_MAP.put("NIGHT_ELF_DRUID", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("NIGHT_ELF_DRUID", "/models/warcraft/druid.obj");
        TEXTURE_PATHS.put("NIGHT_ELF_DRUID", "/textures/warcraft/druid.png");

        UNIT_TYPES.put("MAGE", "Mage");
        GENERIC_MAP.put("MAGE", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("MAGE", "/models/warcraft/mage.obj");
        TEXTURE_PATHS.put("MAGE", "/textures/warcraft/mage.png");

        UNIT_TYPES.put("WARLOCK", "Warlock");
        GENERIC_MAP.put("WARLOCK", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("WARLOCK", "/models/warcraft/warlock.obj");
        TEXTURE_PATHS.put("WARLOCK", "/textures/warcraft/warlock.png");

        UNIT_TYPES.put("PALADIN", "Paladin");
        GENERIC_MAP.put("PALADIN", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("PALADIN", "/models/warcraft/paladin.obj");
        TEXTURE_PATHS.put("PALADIN", "/textures/warcraft/paladin.png");

        UNIT_TYPES.put("DEATH_KNIGHT", "Death Knight");
        GENERIC_MAP.put("DEATH_KNIGHT", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("DEATH_KNIGHT", "/models/warcraft/deathknight.obj");
        TEXTURE_PATHS.put("DEATH_KNIGHT", "/textures/warcraft/deathknight.png");

        UNIT_TYPES.put("GOLEM", "Golem");
        GENERIC_MAP.put("GOLEM", GenericUnitType.ARMORED);
        MODEL_PATHS.put("GOLEM", "/models/warcraft/golem.obj");
        TEXTURE_PATHS.put("GOLEM", "/textures/warcraft/golem.png");

        UNIT_TYPES.put("HYENA", "Hyena");
        GENERIC_MAP.put("HYENA", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("HYENA", "/models/warcraft/hyena.obj");
        TEXTURE_PATHS.put("HYENA", "/textures/warcraft/hyena.png");

        UNIT_TYPES.put("WOLF", "Wolf");
        GENERIC_MAP.put("WOLF", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("WOLF", "/models/warcraft/wolf.obj");
        TEXTURE_PATHS.put("WOLF", "/textures/warcraft/wolf.png");

        UNIT_TYPES.put("WISP", "Wisp");
        GENERIC_MAP.put("WISP", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("WISP", "/models/warcraft/wisp.obj");
        TEXTURE_PATHS.put("WISP", "/textures/warcraft/wisp.png");
    }

    @Override
    public String getModuleId() { return "Warcraft"; }

    @Override
    public String getModuleName() { return "Warcraft"; }

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
            case "alliance": return "#FFD700";
            case "horde": return "#8B4513";
            case "undead": return "#808080";
            case "night_elf": return "#00FF00";
            case "burning_legion": return "#FF0000";
            default: return "#808080";
        }
    }
}
