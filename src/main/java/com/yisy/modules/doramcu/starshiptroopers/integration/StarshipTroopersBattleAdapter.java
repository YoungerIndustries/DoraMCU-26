package com.yisy.modules.doramcu.starshiptroopers.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Starship Troopers battle adapter — provides ST units to universal battles.
 */
@Component
public class StarshipTroopersBattleAdapter implements ModuleBattleAdapter {

    private static final Map<String, String> UNIT_TYPES = new HashMap<>();
    private static final Map<String, GenericUnitType> GENERIC_MAP = new HashMap<>();
    private static final Map<String, String> MODEL_PATHS = new HashMap<>();
    private static final Map<String, String> TEXTURE_PATHS = new HashMap<>();

    static {
        UNIT_TYPES.put("MOBILE_INFANTRY", "Mobile Infantry");
        GENERIC_MAP.put("MOBILE_INFANTRY", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("MOBILE_INFANTRY", "/models/st/mobile_infantry.obj");
        TEXTURE_PATHS.put("MOBILE_INFANTRY", "/textures/st/mobile_infantry.png");

        UNIT_TYPES.put("POWER_ARMOR", "Power Armor");
        GENERIC_MAP.put("POWER_ARMOR", GenericUnitType.ARMORED);
        MODEL_PATHS.put("POWER_ARMOR", "/models/st/power_armor.obj");
        TEXTURE_PATHS.put("POWER_ARMOR", "/textures/st/power_armor.png");

        UNIT_TYPES.put("OFFICER", "Officer");
        GENERIC_MAP.put("OFFICER", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("OFFICER", "/models/st/officer.obj");
        TEXTURE_PATHS.put("OFFICER", "/textures/st/officer.png");

        UNIT_TYPES.put("BUG_WARrior", "Bug Warrior");
        GENERIC_MAP.put("BUG_WARrior", GenericUnitType.ARMORED);
        MODEL_PATHS.put("BUG_WARrior", "/models/st/bug_warrior.obj");
        TEXTURE_PATHS.put("BUG_WARrior", "/textures/st/bug_warrior.png");

        UNIT_TYPES.put("BUG_PLASMA", "Bug Plasma");
        GENERIC_MAP.put("BUG_PLASMA", GenericUnitType.ARTILLERY);
        MODEL_PATHS.put("BUG_PLASMA", "/models/st/bug_plasma.obj");
        TEXTURE_PATHS.put("BUG_PLASMA", "/textures/st/bug_plasma.png");

        UNIT_TYPES.put("DROP_SHIP", "Drop Ship");
        GENERIC_MAP.put("DROP_SHIP", GenericUnitType.SPACECRAFT);
        MODEL_PATHS.put("DROP_SHIP", "/models/st/drop_ship.obj");
        TEXTURE_PATHS.put("DROP_SHIP", "/textures/st/drop_ship.png");
    }

    @Override
    public String getModuleId() { return "StarshipTroopers"; }

    @Override
    public String getModuleName() { return "Starship Troopers"; }

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
            case "federation": return "#0000FF";
            case "bugs": return "#00FF00";
            default: return "#808080";
        }
    }
}
