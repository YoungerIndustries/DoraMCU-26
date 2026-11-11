package com.yisy.modules.doramcu.thirdreich.adapter;

import com.yisy.modules.doramcu.thirdreich.commander.AICommander;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichBattleResult;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichUnitType;
import com.yisy.modules.doramcu.traveller.MarineUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for Traveller ground units (MarineUnit).
 * <p>
 * Converts Traveller marine units to 3R4 ground units and maps
 * Traveller characters to AI commanders.
 * </p>
 */
public class TravellerTo3R4Adapter implements GroundCombatAdapter {

    private static final String SOURCE_GAME = "Traveller";

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
        if (gameUnit instanceof MarineUnit) {
            return convertMarineUnit((MarineUnit) gameUnit);
        }
        return null;
    }

    private ThirdReichGroundUnit convertMarineUnit(MarineUnit marineUnit) {
        String id = "traveller-marine-" + System.nanoTime();
        ThirdReichGroundUnit unit = new ThirdReichGroundUnit(id, marineUnit.getName(), ThirdReichUnitType.INFANTRY);

        // Map marine size to strength
        int size = marineUnit.getSize();
        unit.setStrength(marineUnit.getRemainingMarines() * 10);
        unit.setMaxStrength(size * 10);

        // Map equipment level to attack/defense
        int attack = 2;
        int defense = 2;
        switch (marineUnit.getEquipment()) {
            case LIGHT:
                attack = 1;
                defense = 1;
                break;
            case STANDARD:
                attack = 2;
                defense = 2;
                break;
            case HEAVY:
                attack = 3;
                defense = 3;
                break;
        }
        unit.setAttackValue(attack);
        unit.setDefenseValue(defense);

        // Map morale
        int morale = 100;
        switch (marineUnit.getMorale()) {
            case POOR:
                morale = 40;
                break;
            case REGULAR:
                morale = 70;
                break;
            case GOOD:
                morale = 85;
                break;
            case ELITE:
                morale = 100;
                break;
        }
        unit.setMorale(morale);
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
        // Traveller characters can be mapped via stats
        if (gameCharacter instanceof Map) {
            Map<String, Object> stats = (Map<String, Object>) gameCharacter;
            String name = (String) stats.getOrDefault("name", "Unknown");
            String id = "traveller-cmd-" + System.nanoTime();
            AICommander commander = new AICommander(id, name);
            commander.setSourceGame(SOURCE_GAME);

            // Map Traveller skills to leadership
            if (stats.containsKey("leadership")) {
                commander.setLeadership(((Number) stats.get("leadership")).intValue());
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
