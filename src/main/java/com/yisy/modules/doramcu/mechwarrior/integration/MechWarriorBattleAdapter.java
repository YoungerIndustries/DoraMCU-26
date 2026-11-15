package com.yisy.modules.doramcu.mechwarrior.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MechWarrior battle adapter — provides MechWarrior units to universal battles.
 */
@Component
public class MechWarriorBattleAdapter implements ModuleBattleAdapter {

    private static final Map<String, String> UNIT_TYPES = new HashMap<>();
    private static final Map<String, GenericUnitType> GENERIC_MAP = new HashMap<>();
    private static final Map<String, String> MODEL_PATHS = new HashMap<>();
    private static final Map<String, String> TEXTURE_PATHS = new HashMap<>();

    static {
        UNIT_TYPES.put("LIGHT_MECH", "Light Mech");
        GENERIC_MAP.put("LIGHT_MECH", GenericUnitType.ARMORED);
        MODEL_PATHS.put("LIGHT_MECH", "/models/mechwarrior/light_mech.obj");
        TEXTURE_PATHS.put("LIGHT_MECH", "/textures/mechwarrior/light_mech.png");

        UNIT_TYPES.put("MEDIUM_MECH", "Medium Mech");
        GENERIC_MAP.put("MEDIUM_MECH", GenericUnitType.ARMORED);
        MODEL_PATHS.put("MEDIUM_MECH", "/models/mechwarrior/medium_mech.obj");
        TEXTURE_PATHS.put("MEDIUM_MECH", "/textures/mechwarrior/medium_mech.png");

        UNIT_TYPES.put("HEAVY_MECH", "Heavy Mech");
        GENERIC_MAP.put("HEAVY_MECH", GenericUnitType.ARMORED);
        MODEL_PATHS.put("HEAVY_MECH", "/models/mechwarrior/heavy_mech.obj");
        TEXTURE_PATHS.put("HEAVY_MECH", "/textures/mechwarrior/heavy_mech.png");

        UNIT_TYPES.put("ASSAULT_MECH", "Assault Mech");
        GENERIC_MAP.put("ASSAULT_MECH", GenericUnitType.ARMORED);
        MODEL_PATHS.put("ASSAULT_MECH", "/models/mechwarrior/assault_mech.obj");
        TEXTURE_PATHS.put("ASSAULT_MECH", "/textures/mechwarrior/assault_mech.png");

        UNIT_TYPES.put("MECH_PILOT", "Mech Pilot");
        GENERIC_MAP.put("MECH_PILOT", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("MECH_PILOT", "/models/mechwarrior/mech_pilot.obj");
        TEXTURE_PATHS.put("MECH_PILOT", "/textures/mechwarrior/mech_pilot.png");

        UNIT_TYPES.put("COMMAND_MECH", "Command Mech");
        GENERIC_MAP.put("COMMAND_MECH", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("COMMAND_MECH", "/models/mechwarrior/command_mech.obj");
        TEXTURE_PATHS.put("COMMAND_MECH", "/textures/mechwarrior/command_mech.png");
    }

    @Override
    public String getModuleId() { return "MechWarrior"; }

    @Override
    public String getModuleName() { return "MechWarrior"; }

    @Override
    public Map<String, String> getAvailableUnitTypes() { return Collections.unmodifiableMap(UNIT_TYPES); }

    @Override
    public UniversalBattleUnit createUnit(String unitId, String unitName, String moduleUnitType, String faction, int techLevel) {
        GenericUnitType genericType = GENERIC_MAP.getOrDefault(moduleUnitType, GenericUnitType.ARMORED);
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
            case "feds": return "#0000FF";
            case "draconis": return "#FF0000";
            case "lyran": return "#FFD700";
            case "captain": return "#00FF00";
            case "free": return "#808080";
            default: return "#808080";
        }
    }
}
