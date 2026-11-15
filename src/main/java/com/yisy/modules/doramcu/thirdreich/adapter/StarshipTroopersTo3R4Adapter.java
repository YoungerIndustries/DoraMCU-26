package com.yisy.modules.doramcu.thirdreich.adapter;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.starshiptroopers.model.Unit;
import com.yisy.modules.doramcu.thirdreich.commander.AICommander;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichBattleResult;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichUnitType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for Starship Troopers units (Terran Mobile Infantry and Arachnids).
 * <p>
 * Converts Starship Troopers units to 3R4 ground units using the JSON-based
 * unit definitions from the starship-troopers-rules module. Both Terran and
 * Arachnid units are supported, enabling cross-module battles between
 * Federation Soldiers and Bug forces.
 * </p>
 * <p>
 * Stat mapping (from JSON unit definitions):
 * <ul>
 *   <li>Strength: Derived from attack_strength + defense_strength combined</li>
 *   <li>Attack: Directly from attack_strength, scaled to 3R4 range</li>
 *   <li>Defense: Directly from defense_strength, scaled to 3R4 range</li>
 *   <li>Mobility: Derived from movement_allowance</li>
 *   <li>Morale: Based on unit type (elite units have higher morale)</li>
 * </ul>
 * </p>
 * <p>
 * Unit type mapping:
 * <ul>
 *   <li>Infantry Squad, Infantry → INFANTRY</li>
 *   <li>Officer, Command → ENGINEER (leadership role)</li>
 *   <li>Reconnaissance → RECON</li>
 *   <li>Engineer → ENGINEER</li>
 *   <li>Vehicle, Transport → MECHANIZED</li>
 *   <li>Heavy Weapon → ARTILLERY</li>
 *   <li>Siege → TANK</li>
 *   <li>Fortification → ANTI_AIR (stationary defense)</li>
 * </ul>
 * </p>
 */
@Component
public class StarshipTroopersTo3R4Adapter implements GroundCombatAdapter {

    private static final String SOURCE_GAME = "StarshipTroopers";

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
     * Converts a Starship Troopers Unit record to a 3R4 ground unit.
     */
    private ThirdReichGroundUnit convertUnit(Unit unit) {
        String id = "st-unit-" + unit.id() + "-" + System.nanoTime();
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

        // Mobility: map movement allowance to 3R4 scale (1-4)
        int mobility = calculateMobility(unit.movementAllowance());
        thirdReichUnit.setMobility(mobility);

        // Supply cost: based on unit complexity
        int supplyCost = Math.max(1, (unit.attackStrength() + unit.defenseStrength()) / 4);
        thirdReichUnit.setSupplyCost(supplyCost);

        // Morale: based on unit type and role
        int morale = calculateMorale(unit);
        thirdReichUnit.setMorale(morale);

        // Terrain bonus for tunneling units
        if (unit.abilities() != null) {
            for (String ability : unit.abilities()) {
                if (ability.toLowerCase().contains("tunnel")) {
                    thirdReichUnit.setTerrainBonus(1);
                    break;
                }
            }
        }

        thirdReichUnit.setSourceGame(SOURCE_GAME);
        return thirdReichUnit;
    }

    /**
     * Maps Starship Troopers Unit record to 3R4 unit type.
     */
    private ThirdReichUnitType mapUnitToUnitType(Unit unit) {
        // Check specific unit IDs for special cases
        if (unit.id().equals("crawler")) {
            return ThirdReichUnitType.TANK;
        }
        if (unit.id().equals("brain_bug") || unit.id().equals("queen")) {
            return ThirdReichUnitType.ARTILLERY; // Stationary command units
        }
        if (unit.id().equals("strong_point")) {
            return ThirdReichUnitType.ANTI_AIR; // Stationary defense
        }
        if (unit.id().equals("decoy")) {
            return ThirdReichUnitType.RECON; // Deception unit
        }

        // Map by type string
        switch (unit.type().toUpperCase()) {
            case "INFANTRY":
            case "INFANTRY SQUAD":
            case "HEAVY INFANTRY":
            case "ELITE INFANTRY":
                return ThirdReichUnitType.INFANTRY;

            case "OFFICER":
            case "COMMAND":
                return ThirdReichUnitType.ENGINEER;

            case "RECONNAISSANCE":
            case "SCOUT":
                return ThirdReichUnitType.RECON;

            case "ENGINEER":
                return ThirdReichUnitType.ENGINEER;

            case "VEHICLE":
            case "TRANSPORT":
                return ThirdReichUnitType.MECHANIZED;

            case "HEAVY WEAPON":
                return ThirdReichUnitType.ARTILLERY;

            case "SIEGE":
                return ThirdReichUnitType.TANK;

            case "FORTIFICATION":
                return ThirdReichUnitType.ANTI_AIR;

            case "SUPPORT":
            case "EQUIPMENT":
            case "DECEPTION":
                return ThirdReichUnitType.RECON;

            default:
                return ThirdReichUnitType.INFANTRY;
        }
    }

    /**
     * Calculates morale based on Unit record properties.
     */
    private int calculateMorale(Unit unit) {
        String type = unit.type().toUpperCase();
        String unitId = unit.id();

        // Command units have highest morale
        if (type.equals("COMMAND") || type.equals("OFFICER")) {
            return 95;
        }

        // Elite units
        if (type.equals("ELITE INFANTRY")) {
            return 90;
        }

        // Brain bugs and queens are protected
        if (unitId.equals("brain_bug") || unitId.equals("queen")) {
            return 100;
        }

        // Royal warriors are elite
        if (unitId.equals("royal_warrior")) {
            return 95;
        }

        // Crawler bugs are heavily armored
        if (unitId.equals("crawler")) {
            return 85;
        }

        // Regular infantry
        if (type.contains("INFANTRY")) {
            return 80;
        }

        // Engineers
        if (type.equals("ENGINEER")) {
            return 75;
        }

        // Recon
        if (type.equals("RECONNAISSANCE")) {
            return 70;
        }

        // Workers and support have lower morale
        if (type.equals("SUPPORT") || type.equals("EQUIPMENT")) {
            return 40;
        }

        // Default
        return 65;
    }

    /**
     * Converts a Starship Troopers unit (from JSON) to a 3R4 ground unit.
     * <p>
     * The unit data map is expected to contain:
     * <ul>
     *   <li>id: Unit identifier (e.g., "marauder", "warrior")</li>
     *   <li>name: Display name (e.g., "Marauder", "Warrior Bug")</li>
     *   <li>type: Unit type string (e.g., "Infantry Squad", "Heavy Weapon")</li>
     *   <li>attack_strength: Attack power (0-10)</li>
     *   <li>defense_strength: Defense power (0-10)</li>
     *   <li>movement_allowance: Movement points per turn</li>
     *   <li>description: Unit description</li>
     *   <li>abilities: List of special abilities</li>
     *   <li>counters_per_scenario: Number of unit counters available</li>
     * </ul>
     * </p>
     */
    @SuppressWarnings("unchecked")
    private ThirdReichGroundUnit convertGenericUnit(Map<String, Object> unitData) {
        String name = (String) unitData.getOrDefault("name", "Unknown Unit");
        String unitId = (String) unitData.getOrDefault("id", "unknown");
        String id = "st-unit-" + unitId + "-" + System.nanoTime();

        // Determine 3R4 unit type from ST unit type
        ThirdReichUnitType unitType = mapToUnitType(unitData);

        ThirdReichGroundUnit unit = new ThirdReichGroundUnit(id, name, unitType);

        // Read stats from JSON data
        int attackStrength = getIntValue(unitData, "attack_strength", 1);
        int defenseStrength = getIntValue(unitData, "defense_strength", 1);
        int movementAllowance = getIntValue(unitData, "movement_allowance", 3);

        // Strength: combined attack + defense as a proxy for HP
        int strength = Math.max(1, attackStrength + defenseStrength);
        unit.setStrength(strength);
        unit.setMaxStrength(strength * 2);

        // Attack: scale from 0-10 range to 3R4 1-10 range
        int attack = Math.min(10, Math.max(1, attackStrength));
        unit.setAttackValue(attack);

        // Defense: scale from 0-10 range to 3R4 1-10 range
        int defense = Math.min(10, Math.max(1, defenseStrength));
        unit.setDefenseValue(defense);

        // Mobility: map movement allowance to 3R4 scale (1-4)
        int mobility = calculateMobility(movementAllowance);
        unit.setMobility(mobility);

        // Supply cost: based on unit complexity
        int supplyCost = Math.max(1, (attackStrength + defenseStrength) / 4);
        unit.setSupplyCost(supplyCost);

        // Morale: based on unit type and role
        int morale = calculateMorale(unitData);
        unit.setMorale(morale);

        // Terrain bonus for tunneling units
        List<String> abilities = (List<String>) unitData.getOrDefault("abilities", List.of());
        for (String ability : abilities) {
            if (ability.toLowerCase().contains("tunnel")) {
                unit.setTerrainBonus(1);
                break;
            }
        }

        unit.setSourceGame(SOURCE_GAME);
        return unit;
    }

    /**
     * Maps Starship Troopers unit type to 3R4 unit type.
     */
    @SuppressWarnings("unchecked")
    private ThirdReichUnitType mapToUnitType(Map<String, Object> unitData) {
        String type = (String) unitData.getOrDefault("type", "");
        String unitId = (String) unitData.getOrDefault("id", "");
        String faction = (String) unitData.getOrDefault("faction", "");

        // Check specific unit IDs for special cases
        if (unitId.equals("crawler")) {
            return ThirdReichUnitType.TANK;
        }
        if (unitId.equals("brain_bug") || unitId.equals("queen")) {
            return ThirdReichUnitType.ARTILLERY; // Stationary command units
        }
        if (unitId.equals("strong_point")) {
            return ThirdReichUnitType.ANTI_AIR; // Stationary defense
        }
        if (unitId.equals("decoy")) {
            return ThirdReichUnitType.RECON; // Deception unit
        }

        // Map by type string
        switch (type.toUpperCase()) {
            case "INFANTRY":
            case "INFANTRY SQUAD":
            case "HEAVY INFANTRY":
            case "ELITE INFANTRY":
                return ThirdReichUnitType.INFANTRY;

            case "OFFICER":
            case "COMMAND":
                return ThirdReichUnitType.ENGINEER;

            case "RECONNAISSANCE":
            case "SCOUT":
                return ThirdReichUnitType.RECON;

            case "ENGINEER":
                return ThirdReichUnitType.ENGINEER;

            case "VEHICLE":
            case "TRANSPORT":
                return ThirdReichUnitType.MECHANIZED;

            case "HEAVY WEAPON":
                return ThirdReichUnitType.ARTILLERY;

            case "SIEGE":
                return ThirdReichUnitType.TANK;

            case "FORTIFICATION":
                return ThirdReichUnitType.ANTI_AIR;

            case "SUPPORT":
            case "EQUIPMENT":
            case "DECEPTION":
                return ThirdReichUnitType.RECON;

            default:
                return ThirdReichUnitType.INFANTRY;
        }
    }

    /**
     * Maps movement allowance to 3R4 mobility scale (1-4).
     */
    private int calculateMobility(int movementAllowance) {
        if (movementAllowance >= 12) return 4;
        if (movementAllowance >= 9) return 3;
        if (movementAllowance >= 3) return 2;
        return 1;
    }

    /**
     * Calculates morale based on unit type and role.
     */
    @SuppressWarnings("unchecked")
    private int calculateMorale(Map<String, Object> unitData) {
        String type = (String) unitData.getOrDefault("type", "");
        String unitId = (String) unitData.getOrDefault("id", "");
        String faction = (String) unitData.getOrDefault("faction", "");

        // Terran units generally have higher morale
        if (faction != null && faction.toLowerCase().contains("terran")) {
            switch (type.toUpperCase()) {
                case "OFFICER":
                case "COMMAND":
                    return 95;
                case "ELITE INFANTRY":
                    return 90;
                case "INFANTRY SQUAD":
                case "INFANTRY":
                    return 80;
                case "ENGINEER":
                    return 75;
                case "RECONNAISSANCE":
                    return 70;
                default:
                    return 65;
            }
        }

        // Arachnid units
        if (faction != null && faction.toLowerCase().contains("arachnid")) {
            // Brain bugs and queens are protected, high morale
            if (unitId.equals("brain_bug") || unitId.equals("queen")) {
                return 100;
            }
            // Royal warriors are elite
            if (unitId.equals("royal_warrior")) {
                return 95;
            }
            // Crawler bugs are heavily armored
            if (unitId.equals("crawler")) {
                return 85;
            }
            // Regular warriors
            if (type.toUpperCase().contains("INFANTRY")) {
                return 70;
            }
            // Workers have low combat morale
            if (type.toUpperCase().equals("SUPPORT")) {
                return 40;
            }
            return 60;
        }

        // Default morale
        return 65;
    }

    /**
     * Helper to safely extract integer values from unit data.
     */
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

        // Add Starship Troopers-specific fields
        map.put("starshipTroopersBattle", true);
        map.put("sourceGame", SOURCE_GAME);
        return map;
    }

    @Override
    public AICommander toCommander(Object gameCharacter) {
        if (gameCharacter instanceof Map) {
            Map<String, Object> stats = (Map<String, Object>) gameCharacter;
            String name = (String) stats.getOrDefault("name", "Unknown Commander");
            String id = "st-cmd-" + System.nanoTime();
            AICommander commander = new AICommander(id, name);
            commander.setSourceGame(SOURCE_GAME);

            // Map leadership from attack/defense stats
            int leadership = 5;
            if (stats.containsKey("attack_strength")) {
                int attack = ((Number) stats.get("attack_strength")).intValue();
                leadership = Math.max(1, Math.min(10, attack / 2 + 2));
            }
            commander.setLeadership(leadership);

            // Map faction to tactical preference
            String faction = (String) stats.getOrDefault("faction", "");
            if (faction.toLowerCase().contains("terran")) {
                commander.setTacticalPreference(AICommander.TacticalPreference.AGGRESSIVE);
            } else if (faction.toLowerCase().contains("arachnid")) {
                commander.setTacticalPreference(AICommander.TacticalPreference.DEFENSIVE);
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
