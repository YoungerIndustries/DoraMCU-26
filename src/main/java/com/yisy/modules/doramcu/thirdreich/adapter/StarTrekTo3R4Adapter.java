package com.yisy.modules.doramcu.thirdreich.adapter;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCombat;
import com.yisy.modules.doramcu.thirdreich.commander.AICommander;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichBattleResult;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichUnitType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for Star Trek/Captain's Log ground teams.
 * <p>
 * Converts Captain's Log ground combat units to 3R4 ground units and maps
 * Captain's Log characters to AI commanders using their attributes.
 * </p>
 */
public class StarTrekTo3R4Adapter implements GroundCombatAdapter {

    private static final String SOURCE_GAME = "Captains_Log";

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
        if (gameUnit instanceof CaptainLogCombat) {
            return convertCombatUnit((CaptainLogCombat) gameUnit);
        } else if (gameUnit instanceof Map) {
            return convertGenericUnit((Map<String, Object>) gameUnit);
        }
        return null;
    }

    private ThirdReichGroundUnit convertCombatUnit(CaptainLogCombat combat) {
        String id = "strek-combat-" + System.nanoTime();
        ThirdReichGroundUnit unit = new ThirdReichGroundUnit(id, "Starfleet Ground Team", ThirdReichUnitType.INFANTRY);

        // Map Captain's Log combat stats to 3R4
        int hits = combat.getCharacterHits();
        int maxHits = combat.getCharacterMaxHits();
        unit.setStrength(hits);
        unit.setMaxStrength(maxHits);
        unit.setMorale(Math.min(100, hits * 100 / Math.max(1, maxHits)));

        // Starfleet teams are well-equipped infantry
        unit.setAttackValue(3);
        unit.setDefenseValue(3);
        unit.setSourceGame(SOURCE_GAME);
        return unit;
    }

    private ThirdReichGroundUnit convertGenericUnit(Map<String, Object> unitData) {
        String name = (String) unitData.getOrDefault("name", "Unknown Unit");
        String id = "strek-unit-" + System.nanoTime();

        // Determine unit type from data
        ThirdReichUnitType unitType = ThirdReichUnitType.INFANTRY;
        String type = (String) unitData.getOrDefault("type", "INFANTRY");
        switch (type.toUpperCase()) {
            case "TANK":
            case "ARMOR":
                unitType = ThirdReichUnitType.TANK;
                break;
            case "ARTILLERY":
            case "BOMBARDMENT":
                unitType = ThirdReichUnitType.ARTILLERY;
                break;
            case "RECON":
            case "SCOUT":
                unitType = ThirdReichUnitType.RECON;
                break;
            case "AIR_SUPPORT":
            case "AEROSPACE":
                unitType = ThirdReichUnitType.AIR_SUPPORT;
                break;
        }

        ThirdReichGroundUnit unit = new ThirdReichGroundUnit(id, name, unitType);

        if (unitData.containsKey("strength")) {
            unit.setStrength(((Number) unitData.get("strength")).intValue());
        }
        if (unitData.containsKey("morale")) {
            unit.setMorale(((Number) unitData.get("morale")).intValue());
        }
        if (unitData.containsKey("attack")) {
            unit.setAttackValue(((Number) unitData.get("attack")).intValue());
        }
        if (unitData.containsKey("defense")) {
            unit.setDefenseValue(((Number) unitData.get("defense")).intValue());
        }
        unit.setSourceGame(SOURCE_GAME);
        return unit;
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
        // Captain's Log characters are represented as maps with attributes
        if (gameCharacter instanceof Map) {
            Map<String, Object> stats = (Map<String, Object>) gameCharacter;
            String name = (String) stats.getOrDefault("name", "Unknown Officer");
            String id = "strek-cmd-" + System.nanoTime();
            AICommander commander = new AICommander(id, name);
            commander.setSourceGame(SOURCE_GAME);

            // Map Star Trek attributes to leadership
            // FITNESS → physical leadership, EDUCATION → tactical skill
            int leadership = 5;
            if (stats.containsKey("fitness")) {
                int fitness = ((Number) stats.get("fitness")).intValue();
                leadership = Math.max(1, Math.min(10, fitness / 2 + 3));
            }
            commander.setLeadership(leadership);

            // Map aggression from attributes
            if (stats.containsKey("aggression")) {
                int aggression = ((Number) stats.get("aggression")).intValue();
                if (aggression >= 7) {
                    commander.setTacticalPreference(AICommander.TacticalPreference.AGGRESSIVE);
                } else if (aggression <= 3) {
                    commander.setTacticalPreference(AICommander.TacticalPreference.DEFENSIVE);
                }
            }
            return commander;
        }
        return null;
    }

    @Override
    public String getSourceGame() {
        return SOURCE_GAME;
    }
}
