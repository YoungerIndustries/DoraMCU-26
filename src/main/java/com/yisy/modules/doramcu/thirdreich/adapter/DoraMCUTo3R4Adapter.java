package com.yisy.modules.doramcu.thirdreich.adapter;

import com.yisy.modules.doramcu.hex.ArmyUnit;
import com.yisy.modules.doramcu.hex.ArmyUnitType;
import com.yisy.modules.doramcu.hex.UnifiedGroundUnit;
import com.yisy.modules.doramcu.thirdreich.commander.AICommander;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichBattleResult;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichUnitType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for DoraMCU 4X ground units (ArmyUnit, UnifiedGroundUnit).
 * <p>
 * Converts DoraMCU army units to 3R4 ground units using the existing
 * ArmyUnitType mapping.
 * </p>
 */
public class DoraMCUTo3R4Adapter implements GroundCombatAdapter {

    private static final String SOURCE_GAME = "DoraMCU_4X";

    @Override
    public List<ThirdReichGroundUnit> to3R4Units(List<?> gameUnits) {
        List<ThirdReichGroundUnit> units = new ArrayList<>();
        for (Object unit : gameUnits) {
            ThirdReichGroundUnit converted = to3R4Unit(unit);
            if (converted != null) {
                units.add(converted);
            }
        }
        return units;
    }

    @Override
    public ThirdReichGroundUnit to3R4Unit(Object gameUnit) {
        if (gameUnit instanceof ArmyUnit) {
            return convertArmyUnit((ArmyUnit) gameUnit);
        } else if (gameUnit instanceof UnifiedGroundUnit) {
            return convertUnifiedUnit((UnifiedGroundUnit) gameUnit);
        }
        return null;
    }

    private ThirdReichGroundUnit convertArmyUnit(ArmyUnit armyUnit) {
        ThirdReichUnitType unitType = mapUnitType(armyUnit.getUnitType());
        String id = "doramcu-army-" + armyUnit.getId();
        ThirdReichGroundUnit unit = new ThirdReichGroundUnit(id, armyUnit.getName(), unitType);
        unit.setStrength(armyUnit.getCurrentStrength());
        unit.setMaxStrength(armyUnit.getMaxStrength());
        unit.setMorale(armyUnit.getMorale());
        unit.setAttackValue(armyUnit.getUnitType().getOffense());
        unit.setDefenseValue(armyUnit.getUnitType().getDefense());
        unit.setSourceGame(SOURCE_GAME);
        return unit;
    }

    private ThirdReichGroundUnit convertUnifiedUnit(UnifiedGroundUnit unifiedUnit) {
        ThirdReichUnitType unitType = mapUnitType(unifiedUnit.getUnitType());
        String id = "doramcu-unified-" + unifiedUnit.getId();
        ThirdReichGroundUnit unit = new ThirdReichGroundUnit(id, unifiedUnit.getName(), unitType);
        unit.setStrength(unifiedUnit.getCurrentStrength());
        unit.setMaxStrength(unifiedUnit.getMaxStrength());
        unit.setMorale(unifiedUnit.getMoraleValue());
        unit.setAttackValue(unifiedUnit.getUnitType().getOffense());
        unit.setDefenseValue(unifiedUnit.getUnitType().getDefense());
        unit.setSourceGame(SOURCE_GAME);
        return unit;
    }

    private ThirdReichUnitType mapUnitType(ArmyUnitType armyType) {
        switch (armyType) {
            case INFANTRY:
                return ThirdReichUnitType.INFANTRY;
            case MECHANIZED:
                return ThirdReichUnitType.MECHANIZED;
            case TANK_DIVISION:
                return ThirdReichUnitType.TANK;
            case ARTILLERY:
                return ThirdReichUnitType.ARTILLERY;
            case MISSILE_BATTERY:
                return ThirdReichUnitType.ARTILLERY;
            case AIR_SUPPORT:
                return ThirdReichUnitType.AIR_SUPPORT;
            case ENGINEER:
                return ThirdReichUnitType.ENGINEER;
            case SPECIAL_FORCES:
                return ThirdReichUnitType.RECON;
            case MARINES:
                return ThirdReichUnitType.INFANTRY;
            default:
                return ThirdReichUnitType.INFANTRY;
        }
    }

    @Override
    public Map<String, Object> mapResult(ThirdReichBattleResult result) {
        Map<String, Object> map = new HashMap<>();
        map.put("battleId", result.getBattleId());
        map.put("outcome", result.getOutcome().name());
        map.put("attackerCasualties", result.getAttackerCasualties());
        map.put("defenderCasualties", result.getDefenderCasualties());
        map.put("attackerUnitsLost", result.getAttackerUnitsLost());
        map.put("defenderUnitsLost", result.getDefenderUnitsLost());
        map.put("roundsFought", result.getRoundsFought());
        map.put("narrative", result.getNarrativeSummary());
        map.put("events", result.getEvents());
        return map;
    }

    @Override
    public AICommander toCommander(Object gameCharacter) {
        // DoraMCU doesn't have a character model yet; return null
        return null;
    }

    @Override
    public String getSourceGame() {
        return SOURCE_GAME;
    }
}
