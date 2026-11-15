package com.yisy.modules.doramcu.starwars.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Star Wars battle adapter — provides Star Wars units to universal battles.
 */
@Component
public class StarWarsBattleAdapter implements ModuleBattleAdapter {

    private static final Map<String, String> UNIT_TYPES = new HashMap<>();
    private static final Map<String, GenericUnitType> GENERIC_MAP = new HashMap<>();
    private static final Map<String, String> MODEL_PATHS = new HashMap<>();
    private static final Map<String, String> TEXTURE_PATHS = new HashMap<>();

    static {
        UNIT_TYPES.put("STORMTROOPER", "Stormtrooper");
        GENERIC_MAP.put("STORMTROOPER", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("STORMTROOPER", "/models/starwars/stormtrooper.obj");
        TEXTURE_PATHS.put("STORMTROOPER", "/textures/starwars/stormtrooper.png");

        UNIT_TYPES.put("JEDI", "Jedi");
        GENERIC_MAP.put("JEDI", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("JEDI", "/models/starwars/jedi.obj");
        TEXTURE_PATHS.put("JEDI", "/textures/starwars/jedi.png");

        UNIT_TYPES.put("ATST", "AT-ST");
        GENERIC_MAP.put("ATST", GenericUnitType.ARMORED);
        MODEL_PATHS.put("ATST", "/models/starwars/atst.obj");
        TEXTURE_PATHS.put("ATST", "/textures/starwars/atst.png");

        UNIT_TYPES.put("ATAT", "AT-AT");
        GENERIC_MAP.put("ATAT", GenericUnitType.ARMORED);
        MODEL_PATHS.put("ATAT", "/models/starwars/atat.obj");
        TEXTURE_PATHS.put("ATAT", "/textures/starwars/atat.png");

        UNIT_TYPES.put("X_WING", "X-Wing");
        GENERIC_MAP.put("X_WING", GenericUnitType.SPACECRAFT);
        MODEL_PATHS.put("X_WING", "/models/starwars/xwing.obj");
        TEXTURE_PATHS.put("X_WING", "/textures/starwars/xwing.png");

        UNIT_TYPES.put("TIE_FIGHTER", "TIE Fighter");
        GENERIC_MAP.put("TIE_FIGHTER", GenericUnitType.SPACECRAFT);
        MODEL_PATHS.put("TIE_FIGHTER", "/models/starwars/tiefighter.obj");
        TEXTURE_PATHS.put("TIE_FIGHTER", "/textures/starwars/tiefighter.png");

        UNIT_TYPES.put("DROID", "Battle Droid");
        GENERIC_MAP.put("DROID", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("DROID", "/models/starwars/droid.obj");
        TEXTURE_PATHS.put("DROID", "/textures/starwars/droid.png");

        UNIT_TYPES.put("BOMBER", "Bomber");
        GENERIC_MAP.put("BOMBER", GenericUnitType.ARTILLERY);
        MODEL_PATHS.put("BOMBER", "/models/starwars/bomber.obj");
        TEXTURE_PATHS.put("BOMBER", "/textures/starwars/bomber.png");
    }

    @Override
    public String getModuleId() { return "StarWars"; }

    @Override
    public String getModuleName() { return "Star Wars"; }

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
            case "empire": return "#000000";
            case "republic": return "#0000FF";
            case "rebellion": return "#FF0000";
            case "separatist": return "#FF8C00";
            case "sith": return "#800080";
            default: return "#808080";
        }
    }
}
