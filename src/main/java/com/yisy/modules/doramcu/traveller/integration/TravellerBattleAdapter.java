package com.yisy.modules.doramcu.traveller.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Traveller battle adapter — provides Traveller units to universal battles.
 */
@Component
public class TravellerBattleAdapter implements ModuleBattleAdapter {

    private static final Map<String, String> UNIT_TYPES = new HashMap<>();
    private static final Map<String, GenericUnitType> GENERIC_MAP = new HashMap<>();
    private static final Map<String, String> MODEL_PATHS = new HashMap<>();
    private static final Map<String, String> TEXTURE_PATHS = new HashMap<>();

    static {
        // Infantry units
        UNIT_TYPES.put("MARINE", "Traveller Marine");
        GENERIC_MAP.put("MARINE", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("MARINE", "/models/traveller/marine.obj");
        TEXTURE_PATHS.put("MARINE", "/textures/traveller/marine.png");

        UNIT_TYPES.put("RIFLEMAN", "Rifleman");
        GENERIC_MAP.put("RIFLEMAN", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("RIFLEMAN", "/models/traveller/rifleman.obj");
        TEXTURE_PATHS.put("RIFLEMAN", "/textures/traveller/rifleman.png");

        // Armored units
        UNIT_TYPES.put("ASSAULT_TANK", "Assault Tank");
        GENERIC_MAP.put("ASSAULT_TANK", GenericUnitType.ARMORED);
        MODEL_PATHS.put("ASSAULT_TANK", "/models/traveller/assault_tank.obj");
        TEXTURE_PATHS.put("ASSAULT_TANK", "/textures/traveller/assault_tank.png");

        UNIT_TYPES.put("CIVILIAN_CARRIER", "Civilian Carrier");
        GENERIC_MAP.put("CIVILIAN_CARRIER", GenericUnitType.ARMORED);
        MODEL_PATHS.put("CIVILIAN_CARRIER", "/models/traveller/civilian_carrier.obj");
        TEXTURE_PATHS.put("CIVILIAN_CARRIER", "/textures/traveller/civilian_carrier.png");

        // Artillery
        UNIT_TYPES.put("ARTILLERY", "Artillery");
        GENERIC_MAP.put("ARTILLERY", GenericUnitType.ARTILLERY);
        MODEL_PATHS.put("ARTILLERY", "/models/traveller/artillery.obj");
        TEXTURE_PATHS.put("ARTILLERY", "/textures/traveller/artillery.png");

        // Aircraft
        UNIT_TYPES.put("FIGHTER", "Fighter");
        GENERIC_MAP.put("FIGHTER", GenericUnitType.AIRCRAFT);
        MODEL_PATHS.put("FIGHTER", "/models/traveller/fighter.obj");
        TEXTURE_PATHS.put("FIGHTER", "/textures/traveller/fighter.png");

        // Naval
        UNIT_TYPES.put("DESTROYER", "Destroyer");
        GENERIC_MAP.put("DESTROYER", GenericUnitType.NAVAL);
        MODEL_PATHS.put("DESTROYER", "/models/traveller/destroyer.obj");
        TEXTURE_PATHS.put("DESTROYER", "/textures/traveller/destroyer.png");

        // Spacecraft
        UNIT_TYPES.put("FRIGATE", "Frigate");
        GENERIC_MAP.put("FRIGATE", GenericUnitType.SPACECRAFT);
        MODEL_PATHS.put("FRIGATE", "/models/traveller/frigate.obj");
        TEXTURE_PATHS.put("FRIGATE", "/textures/traveller/frigate.png");

        UNIT_TYPES.put("CRUISER", "Cruiser");
        GENERIC_MAP.put("CRUISER", GenericUnitType.SPACECRAFT);
        MODEL_PATHS.put("CRUISER", "/models/traveller/cruiser.obj");
        TEXTURE_PATHS.put("CRUISER", "/textures/traveller/cruiser.png");

        // Special forces
        UNIT_TYPES.put("COMMANDO", "Commando");
        GENERIC_MAP.put("COMMANDO", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("COMMANDO", "/models/traveller/commando.obj");
        TEXTURE_PATHS.put("COMMANDO", "/textures/traveller/commando.png");

        // Engineer
        UNIT_TYPES.put("ENGINEER", "Engineer");
        GENERIC_MAP.put("ENGINEER", GenericUnitType.ENGINEER);
        MODEL_PATHS.put("ENGINEER", "/models/traveller/engineer.obj");
        TEXTURE_PATHS.put("ENGINEER", "/textures/traveller/engineer.png");
    }

    @Override
    public String getModuleId() {
        return "Traveller";
    }

    @Override
    public String getModuleName() {
        return "Traveller RPG";
    }

    @Override
    public Map<String, String> getAvailableUnitTypes() {
        return Collections.unmodifiableMap(UNIT_TYPES);
    }

    @Override
    public UniversalBattleUnit createUnit(String unitId, String unitName, String moduleUnitType, String faction, int techLevel) {
        GenericUnitType genericType = GENERIC_MAP.getOrDefault(moduleUnitType, GenericUnitType.INFANTRY);

        // Scale stats based on tech level
        int attackScale = Math.max(1, techLevel / 3);
        int defenseScale = Math.max(1, techLevel / 3);

        return UniversalBattleUnit.builder(unitId, unitName, getModuleId(), moduleUnitType, genericType)
                .faction(faction)
                .attackValue(genericType.getBaseAttack() * attackScale)
                .defenseValue(genericType.getBaseDefense() * defenseScale)
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
    public String getModelPath(String moduleUnitType) {
        return MODEL_PATHS.get(moduleUnitType);
    }

    @Override
    public String getTexturePath(String moduleUnitType) {
        return TEXTURE_PATHS.get(moduleUnitType);
    }

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
            case "sol": return "#FF0000";
            case "argentina": return "#FFFF00";
            case "subsector": return "#00FF00";
            default: return "#808080";
        }
    }
}
