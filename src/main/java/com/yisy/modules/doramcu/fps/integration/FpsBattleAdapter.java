package com.yisy.modules.doramcu.fps.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * FPS battle adapter — provides FPS units to universal battles.
 */
@Component
public class FpsBattleAdapter implements ModuleBattleAdapter {

    private static final Map<String, String> UNIT_TYPES = new HashMap<>();
    private static final Map<String, GenericUnitType> GENERIC_MAP = new HashMap<>();
    private static final Map<String, String> MODEL_PATHS = new HashMap<>();
    private static final Map<String, String> TEXTURE_PATHS = new HashMap<>();

    static {
        UNIT_TYPES.put("ASSAULT_RIFLE", "Assault Rifle");
        GENERIC_MAP.put("ASSAULT_RIFLE", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("ASSAULT_RIFLE", "/models/fps/assault_rifle.obj");
        TEXTURE_PATHS.put("ASSAULT_RIFLE", "/textures/fps/assault_rifle.png");

        UNIT_TYPES.put("SNIPER", "Sniper");
        GENERIC_MAP.put("SNIPER", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("SNIPER", "/models/fps/sniper.obj");
        TEXTURE_PATHS.put("SNIPER", "/textures/fps/sniper.png");

        UNIT_TYPES.put("HEAVY_GUNNER", "Heavy Gunner");
        GENERIC_MAP.put("HEAVY_GUNNER", GenericUnitType.ARMORED);
        MODEL_PATHS.put("HEAVY_GUNNER", "/models/fps/heavy_gunner.obj");
        TEXTURE_PATHS.put("HEAVY_GUNNER", "/textures/fps/heavy_gunner.png");

        UNIT_TYPES.put("MEDIC", "Medic");
        GENERIC_MAP.put("MEDIC", GenericUnitType.ENGINEER);
        MODEL_PATHS.put("MEDIC", "/models/fps/medic.obj");
        TEXTURE_PATHS.put("MEDIC", "/textures/fps/medic.png");
    }

    @Override
    public String getModuleId() { return "FPS"; }

    @Override
    public String getModuleName() { return "FPS Module"; }

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
            case "alpha": return "#FF0000";
            case "bravo": return "#0000FF";
            default: return "#808080";
        }
    }
}
