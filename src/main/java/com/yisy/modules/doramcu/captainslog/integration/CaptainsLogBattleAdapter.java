package com.yisy.modules.doramcu.captainslog.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Captain's Log battle adapter — provides CL units to universal battles.
 */
@Component
public class CaptainsLogBattleAdapter implements ModuleBattleAdapter {

    private static final Map<String, String> UNIT_TYPES = new HashMap<>();
    private static final Map<String, GenericUnitType> GENERIC_MAP = new HashMap<>();
    private static final Map<String, String> MODEL_PATHS = new HashMap<>();
    private static final Map<String, String> TEXTURE_PATHS = new HashMap<>();

    static {
        UNIT_TYPES.put("SECURITY_OFFICER", "Security Officer");
        GENERIC_MAP.put("SECURITY_OFFICER", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("SECURITY_OFFICER", "/models/captainslog/security.obj");
        TEXTURE_PATHS.put("SECURITY_OFFICER", "/textures/captainslog/security.png");

        UNIT_TYPES.put("SCOUT", "Scout");
        GENERIC_MAP.put("SCOUT", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("SCOUT", "/models/captainslog/scout.obj");
        TEXTURE_PATHS.put("SCOUT", "/textures/captainslog/scout.png");

        UNIT_TYPES.put("FRIGATE", "Frigate");
        GENERIC_MAP.put("FRIGATE", GenericUnitType.SPACECRAFT);
        MODEL_PATHS.put("FRIGATE", "/models/captainslog/frigate.obj");
        TEXTURE_PATHS.put("FRIGATE", "/textures/captainslog/frigate.png");

        UNIT_TYPES.put("DESTROYER", "Destroyer");
        GENERIC_MAP.put("DESTROYER", GenericUnitType.SPACECRAFT);
        MODEL_PATHS.put("DESTROYER", "/models/captainslog/destroyer.obj");
        TEXTURE_PATHS.put("DESTROYER", "/textures/captainslog/destroyer.png");

        UNIT_TYPES.put("CRUISER", "Cruiser");
        GENERIC_MAP.put("CRUISER", GenericUnitType.SPACECRAFT);
        MODEL_PATHS.put("CRUISER", "/models/captainslog/cruiser.obj");
        TEXTURE_PATHS.put("CRUISER", "/textures/captainslog/cruiser.png");
    }

    @Override
    public String getModuleId() { return "CaptainsLog"; }

    @Override
    public String getModuleName() { return "Captain's Log"; }

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
            case "federation": return "#FF8C00";
            case "klingon": return "#8B0000";
            case "romulan": return "#4B0082";
            default: return "#808080";
        }
    }
}
