package com.yisy.modules.doramcu.warhammer40k.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Warhammer 40K battle adapter — provides Warhammer 40K units to universal battles.
 */
@Component
public class WarhammerBattleAdapter implements ModuleBattleAdapter {

    private static final Map<String, String> UNIT_TYPES = new HashMap<>();
    private static final Map<String, GenericUnitType> GENERIC_MAP = new HashMap<>();
    private static final Map<String, String> MODEL_PATHS = new HashMap<>();
    private static final Map<String, String> TEXTURE_PATHS = new HashMap<>();

    static {
        UNIT_TYPES.put("SPACE_MARINE", "Space Marine");
        GENERIC_MAP.put("SPACE_MARINE", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("SPACE_MARINE", "/models/warhammer40k/spacemarine.obj");
        TEXTURE_PATHS.put("SPACE_MARINE", "/textures/warhammer40k/spacemarine.png");

        UNIT_TYPES.put("ORK", "Ork Boy");
        GENERIC_MAP.put("ORK", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("ORK", "/models/warhammer40k/ork.obj");
        TEXTURE_PATHS.put("ORK", "/textures/warhammer40k/ork.png");

        UNIT_TYPES.put("CHAOS_MARINE", "Chaos Marine");
        GENERIC_MAP.put("CHAOS_MARINE", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("CHAOS_MARINE", "/models/warhammer40k/chaosmarine.obj");
        TEXTURE_PATHS.put("CHAOS_MARINE", "/textures/warhammer40k/chaosmarine.png");

        UNIT_TYPES.put("NECRON", "Necron Warrior");
        GENERIC_MAP.put("NECRON", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("NECRON", "/models/warhammer40k/necron.obj");
        TEXTURE_PATHS.put("NECRON", "/textures/warhammer40k/necron.png");

        UNIT_TYPES.put("ELDAR", "Eldar Guardian");
        GENERIC_MAP.put("ELDAR", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("ELDAR", "/models/warhammer40k/eldar.obj");
        TEXTURE_PATHS.put("ELDAR", "/textures/warhammer40k/eldar.png");

        UNIT_TYPES.put("TYRANID", "Tyranid Termagant");
        GENERIC_MAP.put("TYRANID", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("TYRANID", "/models/warhammer40k/tyranid.obj");
        TEXTURE_PATHS.put("TYRANID", "/textures/warhammer40k/tyranid.png");

        UNIT_TYPES.put("IMPERIAL_GUARD", "Imperial Guard");
        GENERIC_MAP.put("IMPERIAL_GUARD", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("IMPERIAL_GUARD", "/models/warhammer40k/imperialguard.obj");
        TEXTURE_PATHS.put("IMPERIAL_GUARD", "/textures/warhammer40k/imperialguard.png");

        UNIT_TYPES.put("DREADNOUGHT", "Dreadnought");
        GENERIC_MAP.put("DREADNOUGHT", GenericUnitType.ARMORED);
        MODEL_PATHS.put("DREADNOUGHT", "/models/warhammer40k/dreadnought.obj");
        TEXTURE_PATHS.put("DREADNOUGHT", "/textures/warhammer40k/dreadnought.png");

        UNIT_TYPES.put("BATTLE_TANK", "Baneblade Tank");
        GENERIC_MAP.put("BATTLE_TANK", GenericUnitType.ARMORED);
        MODEL_PATHS.put("BATTLE_TANK", "/models/warhammer40k/baneblade.obj");
        TEXTURE_PATHS.put("BATTLE_TANK", "/textures/warhammer40k/baneblade.png");

        UNIT_TYPES.put("DROP_POD", "Drop Pod");
        GENERIC_MAP.put("DROP_POD", GenericUnitType.AIRCRAFT);
        MODEL_PATHS.put("DROP_POD", "/models/warhammer40k/droppod.obj");
        TEXTURE_PATHS.put("DROP_POD", "/textures/warhammer40k/droppod.png");
    }

    @Override
    public String getModuleId() { return "Warhammer40K"; }

    @Override
    public String getModuleName() { return "Warhammer 40K"; }

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
            case "imperial": return "#FFD700";
            case "chaos": return "#800080";
            case "ork": return "#00FF00";
            case "necron": return "#00FF00";
            case "eldar": return "#FF00FF";
            case "tyranid": return "#006400";
            case "space_marine": return "#0000FF";
            default: return "#808080";
        }
    }
}
