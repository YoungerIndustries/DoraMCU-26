package com.yisy.modules.doramcu.lord_of_the_rings.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Lord of the Rings battle adapter — provides LOTR units to universal battles.
 */
@Component
public class LordOfTheRingsBattleAdapter implements ModuleBattleAdapter {

    private static final Map<String, String> UNIT_TYPES = new HashMap<>();
    private static final Map<String, GenericUnitType> GENERIC_MAP = new HashMap<>();
    private static final Map<String, String> MODEL_PATHS = new HashMap<>();
    private static final Map<String, String> TEXTURE_PATHS = new HashMap<>();

    static {
        UNIT_TYPES.put("ELVEN_GUARD", "Elven Guard");
        GENERIC_MAP.put("ELVEN_GUARD", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("ELVEN_GUARD", "/models/lotr/elven_guard.obj");
        TEXTURE_PATHS.put("ELVEN_GUARD", "/textures/lotr/elven_guard.png");

        UNIT_TYPES.put("ROHIRRIM", "Rohirrim Cavalry");
        GENERIC_MAP.put("ROHIRRIM", GenericUnitType.ARMORED);
        MODEL_PATHS.put("ROHIRRIM", "/models/lotr/rohirrim.obj");
        TEXTURE_PATHS.put("ROHIRRIM", "/textures/lotr/rohirrim.png");

        UNIT_TYPES.put("GONDOR_SOLDIER", "Gondor Soldier");
        GENERIC_MAP.put("GONDOR_SOLDIER", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("GONDOR_SOLDIER", "/models/lotr/gondor_soldier.obj");
        TEXTURE_PATHS.put("GONDOR_SOLDIER", "/textures/lotr/gondor_soldier.png");

        UNIT_TYPES.put("ORC", "Orc Warrior");
        GENERIC_MAP.put("ORC", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("ORC", "/models/lotr/orc.obj");
        TEXTURE_PATHS.put("ORC", "/textures/lotr/orc.png");

        UNIT_TYPES.put("URUK_HAI", "Uruk-hai");
        GENERIC_MAP.put("URUK_HAI", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("URUK_HAI", "/models/lotr/uruk_hai.obj");
        TEXTURE_PATHS.put("URUK_HAI", "/textures/lotr/uruk_hai.png");

        UNIT_TYPES.put("WIZARD", "Wizard");
        GENERIC_MAP.put("WIZARD", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("WIZARD", "/models/lotr/wizard.obj");
        TEXTURE_PATHS.put("WIZARD", "/textures/lotr/wizard.png");

        UNIT_TYPES.put("ENT", "Ent");
        GENERIC_MAP.put("ENT", GenericUnitType.ARMORED);
        MODEL_PATHS.put("ENT", "/models/lotr/ent.obj");
        TEXTURE_PATHS.put("ENT", "/textures/lotr/ent.png");

        UNIT_TYPES.put("NAZGUL", "Nazgûl");
        GENERIC_MAP.put("NAZGUL", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("NAZGUL", "/models/lotr/nazgul.obj");
        TEXTURE_PATHS.put("NAZGUL", "/textures/lotr/nazgul.png");

        UNIT_TYPES.put("DWARVEN_WARrior", "Dwarven Warrior");
        GENERIC_MAP.put("DWARVEN_WARRIOR", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("DWARVEN_WARRIOR", "/models/lotr/dwarven_warrior.obj");
        TEXTURE_PATHS.put("DWARVEN_WARRIOR", "/textures/lotr/dwarven_warrior.png");

        UNIT_TYPES.put("BALROG", "Balrog");
        GENERIC_MAP.put("BALROG", GenericUnitType.ARTILLERY);
        MODEL_PATHS.put("BALROG", "/models/lotr/balrog.obj");
        TEXTURE_PATHS.put("BALROG", "/textures/lotr/balrog.png");
    }

    @Override
    public String getModuleId() { return "LordOfTheRings"; }

    @Override
    public String getModuleName() { return "Lord of the Rings"; }

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
            case "free peoples alliance": return "#006400";
            case "gondor": return "#FFFFFF";
            case "rohan": return "#FFFF00";
            case "mordor": return "#000000";
            case "isengard": return "#808080";
            case "nazgul": return "#4B0082";
            case "lothríen": return "#DAA520";
            case "rivendell": return "#4169E1";
            default: return "#808080";
        }
    }
}
