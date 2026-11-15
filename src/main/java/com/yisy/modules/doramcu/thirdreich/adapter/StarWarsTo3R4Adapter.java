package com.yisy.modules.doramcu.thirdreich.adapter;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.starwars.model.Unit;
import com.yisy.modules.doramcu.thirdreich.commander.AICommander;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichBattleResult;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichUnitType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for Star Wars units (ground troops, vehicles, and characters).
 * <p>
 * Converts Star Wars units to 3R4 ground units using the JSON-based
 * unit definitions from the star-wars-rules module. All Star Wars factions
 * are supported, enabling cross-module battles between Star Wars forces
 * and other game systems.
 * </p>
 * <p>
 * Stat mapping (from JSON unit definitions):
 * <ul>
 *   <li>Strength: Derived from attack + defense combined</li>
 *   <li>Attack: Directly from attack stat, scaled to 3R4 range</li>
 *   <li>Defense: Directly from defense stat, scaled to 3R4 range</li>
 *   <li>Mobility: Derived from speed stat</li>
 *   <li>Morale: Based on unit type and faction</li>
 * </ul>
 * </p>
 */
@Component
public class StarWarsTo3R4Adapter implements GroundCombatAdapter {

    private static final String SOURCE_GAME = "StarWars";

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
        if (gameUnit instanceof Unit) {
            return convertUnit((Unit) gameUnit);
        } else if (gameUnit instanceof Map) {
            return convertGenericUnit((Map<String, Object>) gameUnit);
        }
        return null;
    }

    /**
     * Converts a Star Wars Unit record to a 3R4 ground unit.
     */
    private ThirdReichGroundUnit convertUnit(Unit unit) {
        String id = "sw-unit-" + unit.id() + "-" + System.nanoTime();
        ThirdReichUnitType unitType = mapUnitToUnitType(unit);
        ThirdReichGroundUnit thirdReichUnit = new ThirdReichGroundUnit(id, unit.name(), unitType);

        // Strength: combined attack + defense as a proxy for HP
        int strength = Math.max(1, unit.attackStrength() + unit.defenseStrength());
        thirdReichUnit.setStrength(strength);
        thirdReichUnit.setMaxStrength(strength * 2);

        // Attack: scale from 0-10 range to 3R4 1-10 range
        int attack = Math.min(10, Math.max(1, unit.attackStrength()));
        thirdReichUnit.setAttackValue(attack);

        // Defense: scale from 0-10 range to 3R4 1-10 range
        int defense = Math.min(10, Math.max(1, unit.defenseStrength()));
        thirdReichUnit.setDefenseValue(defense);

        // Mobility: map speed to 3R4 scale (1-4)
        int mobility = calculateMobility(unit.stats() != null ? unit.stats().speed() : 6);
        thirdReichUnit.setMobility(mobility);

        // Supply cost: based on unit complexity
        int supplyCost = Math.max(1, (unit.attackStrength() + unit.defenseStrength()) / 4);
        thirdReichUnit.setSupplyCost(supplyCost);

        // Morale: based on unit type and faction
        int morale = calculateMorale(unit);
        thirdReichUnit.setMorale(morale);

        // Terrain bonus for vehicles
        if (unit.isStarship() || "walker".equals(unit.type()) || "tank".equals(unit.type())) {
            thirdReichUnit.setTerrainBonus(1);
        }

        thirdReichUnit.setSourceGame(SOURCE_GAME);
        return thirdReichUnit;
    }

    /**
     * Maps Star Wars Unit record to 3R4 unit type.
     */
    private ThirdReichUnitType mapUnitToUnitType(Unit unit) {
        // Check specific unit IDs for special cases
        if (unit.id().equals("at_at_walker") || unit.id().equals("at_at_rebel")) {
            return ThirdReichUnitType.TANK;
        }
        if (unit.id().equals("at_st_walker")) {
            return ThirdReichUnitType.MECHANIZED;
        }
        if (unit.id().equals("devastator")) {
            return ThirdReichUnitType.TANK;
        }
        if (unit.id().equals("rebel_artillery")) {
            return ThirdReichUnitType.ARTILLERY;
        }
        if (unit.id().equals("speeder_bike") || unit.id().equals("ta17_jedi_speeder")) {
            return ThirdReichUnitType.RECON;
        }

        // Map by type string
        switch (unit.type().toLowerCase()) {
            case "infantry":
            case "elite_infantry":
            case "droid_infantry":
            case "elite_droid":
                return ThirdReichUnitType.INFANTRY;

            case "commander":
                return ThirdReichUnitType.ENGINEER;

            case "walker":
            case "tank":
                return ThirdReichUnitType.TANK;

            case "speeder":
                return ThirdReichUnitType.RECON;

            case "heavy_weapons":
                return ThirdReichUnitType.ARTILLERY;

            case "creature":
                return ThirdReichUnitType.INFANTRY;

            case "starfighter":
            case "freighter":
            case "capital_ship":
            case "space_station":
                return ThirdReichUnitType.ANTI_AIR; // Space units mapped to AA

            default:
                return ThirdReichUnitType.INFANTRY;
        }
    }

    /**
     * Calculates morale based on Unit record properties.
     */
    private int calculateMorale(Unit unit) {
        String type = unit.type().toLowerCase();
        String faction = unit.faction() != null ? unit.faction().toLowerCase() : "";

        // Jedi and Force users have highest morale
        if (unit.stats() != null && unit.stats().forcePower() != null && unit.stats().forcePower() > 0) {
            return 95;
        }

        // Elite units
        if (type.contains("elite")) {
            return 90;
        }

        // Commander units
        if (type.equals("commander")) {
            return 95;
        }

        // Imperial units have high discipline
        if (faction.contains("empire") || faction.contains("first_order")) {
            if (type.contains("infantry")) return 80;
            if (type.contains("droid")) return 60;
            return 75;
        }

        // Rebel units have high morale from idealism
        if (faction.contains("rebel") || faction.contains("resistance")) {
            return 85;
        }

        // Republic clone troops
        if (faction.contains("republic")) {
            return 80;
        }

        // Droid armies have lower morale
        if (type.contains("droid")) {
            return 50;
        }

        // Scum and villainy
        if (faction.contains("scum") || faction.contains("villainy")) {
            return 60;
        }

        // Default
        return 70;
    }

    /**
     * Converts a generic Star Wars unit (from JSON) to a 3R4 ground unit.
     */
    @SuppressWarnings("unchecked")
    private ThirdReichGroundUnit convertGenericUnit(Map<String, Object> unitData) {
        String name = (String) unitData.getOrDefault("name", "Unknown Unit");
        String unitId = (String) unitData.getOrDefault("id", "unknown");
        String id = "sw-unit-" + unitId + "-" + System.nanoTime();

        ThirdReichUnitType unitType = mapToUnitType(unitData);
        ThirdReichGroundUnit unit = new ThirdReichGroundUnit(id, name, unitType);

        int attackStrength = getIntValue(unitData, "attack", 1);
        int defenseStrength = getIntValue(unitData, "defense", 1);
        int speed = getIntValue(unitData, "speed", 6);

        int strength = Math.max(1, attackStrength + defenseStrength);
        unit.setStrength(strength);
        unit.setMaxStrength(strength * 2);

        unit.setAttackValue(Math.min(10, Math.max(1, attackStrength)));
        unit.setDefenseValue(Math.min(10, Math.max(1, defenseStrength)));
        unit.setMobility(calculateMobility(speed));
        unit.setSupplyCost(Math.max(1, (attackStrength + defenseStrength) / 4));
        unit.setMorale(calculateMorale(unitData));

        unit.setSourceGame(SOURCE_GAME);
        return unit;
    }

    @SuppressWarnings("unchecked")
    private ThirdReichUnitType mapToUnitType(Map<String, Object> unitData) {
        String type = (String) unitData.getOrDefault("type", "");
        String unitId = (String) unitData.getOrDefault("id", "");

        if (unitId.equals("at_at_walker") || unitId.equals("devastator")) {
            return ThirdReichUnitType.TANK;
        }
        if (unitId.equals("speeder_bike")) {
            return ThirdReichUnitType.RECON;
        }

        switch (type.toUpperCase()) {
            case "INFANTRY":
            case "ELITE_INFANTRY":
            case "DROID_INFANTRY":
                return ThirdReichUnitType.INFANTRY;
            case "COMMANDER":
                return ThirdReichUnitType.ENGINEER;
            case "WALKER":
            case "TANK":
                return ThirdReichUnitType.TANK;
            case "SPEEDER":
                return ThirdReichUnitType.RECON;
            case "HEAVY_WEAPONS":
                return ThirdReichUnitType.ARTILLERY;
            default:
                return ThirdReichUnitType.INFANTRY;
        }
    }

    private int calculateMobility(int speed) {
        if (speed >= 10) return 4;
        if (speed >= 7) return 3;
        if (speed >= 4) return 2;
        return 1;
    }

    @SuppressWarnings("unchecked")
    private int calculateMorale(Map<String, Object> unitData) {
        String type = (String) unitData.getOrDefault("type", "");
        String faction = (String) unitData.getOrDefault("faction", "");

        if (type.toLowerCase().contains("elite")) return 90;
        if (type.equals("commander")) return 95;
        if (faction != null && faction.toLowerCase().contains("empire")) return 80;
        if (faction != null && faction.toLowerCase().contains("rebel")) return 85;
        if (type.toLowerCase().contains("droid")) return 50;

        return 70;
    }

    private int getIntValue(Map<String, Object> data, String key, int defaultValue) {
        if (!data.containsKey(key)) return defaultValue;
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
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
        map.put("starWarsBattle", true);
        map.put("sourceGame", SOURCE_GAME);
        return map;
    }

    @Override
    public AICommander toCommander(Object gameCharacter) {
        if (gameCharacter instanceof Unit) {
            Unit unit = (Unit) gameCharacter;
            String id = "sw-cmd-" + unit.id() + "-" + System.nanoTime();
            AICommander commander = new AICommander(id, unit.name());
            commander.setSourceGame(SOURCE_GAME);

            // Map leadership from attack/force stats
            int leadership = 5;
            if (unit.stats() != null) {
                int attack = unit.stats().attack();
                int forcePower = unit.stats().forcePower() != null ? unit.stats().forcePower() : 0;
                leadership = Math.max(1, Math.min(10, attack / 2 + forcePower / 3 + 2));
            }
            commander.setLeadership(leadership);

            // Map faction to tactical preference
            String faction = unit.faction() != null ? unit.faction().toLowerCase() : "";
            if (faction.contains("empire") || faction.contains("first_order")) {
                commander.setTacticalPreference(AICommander.TacticalPreference.AGGRESSIVE);
            } else if (faction.contains("rebel") || faction.contains("resistance")) {
                commander.setTacticalPreference(AICommander.TacticalPreference.BALANCED);
            } else if (faction.contains("republic")) {
                commander.setTacticalPreference(AICommander.TacticalPreference.BALANCED);
            } else if (faction.contains("separatist")) {
                commander.setTacticalPreference(AICommander.TacticalPreference.AGGRESSIVE);
            }

            return commander;
        } else if (gameCharacter instanceof Map) {
            Map<String, Object> stats = (Map<String, Object>) gameCharacter;
            String name = (String) stats.getOrDefault("name", "Unknown Commander");
            String id = "sw-cmd-" + System.nanoTime();
            AICommander commander = new AICommander(id, name);
            commander.setSourceGame(SOURCE_GAME);

            int leadership = 5;
            if (stats.containsKey("attack")) {
                int attack = ((Number) stats.get("attack")).intValue();
                leadership = Math.max(1, Math.min(10, attack / 2 + 2));
            }
            commander.setLeadership(leadership);

            String faction = (String) stats.getOrDefault("faction", "");
            if (faction != null && faction.toLowerCase().contains("empire")) {
                commander.setTacticalPreference(AICommander.TacticalPreference.AGGRESSIVE);
            } else if (faction != null && faction.toLowerCase().contains("rebel")) {
                commander.setTacticalPreference(AICommander.TacticalPreference.BALANCED);
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
