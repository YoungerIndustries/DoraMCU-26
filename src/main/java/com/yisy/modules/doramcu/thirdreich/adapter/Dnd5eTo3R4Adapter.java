package com.yisy.modules.doramcu.thirdreich.adapter;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eMonster;
import com.yisy.modules.doramcu.thirdreich.commander.AICommander;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichBattleResult;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichUnitType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for D&amp;D 5e monsters and creatures.
 * <p>
 * Converts D&amp;D 5e monsters to 3R4 ground units using Challenge Rating,
 * ability scores, and monster type to determine 3R4 combat stats.
 * This enables D&amp;D siege equipment, monsters, and adventurers to participate
 * in cross-module ground battles alongside sci-fi units.
 * </p>
 * <p>
 * Stat mapping:
 * <ul>
 *   <li>Strength: Monster HP directly maps to 3R4 strength</li>
 *   <li>Attack: Derived from CR and STR/DEX scores</li>
 *   <li>Defense: Derived from AC and CON score</li>
 *   <li>Mobility: Derived from speed (walk/fly/swim)</li>
 *   <li>Morale: Derived from alignment and CR (higher CR = more resolute)</li>
 * </ul>
 * </p>
 * <p>
 * Unit type mapping:
 * <ul>
 *   <li>Dragon, Giant, Gargantuan monsters → TANK (heavy siege units)</li>
 *   <li>Construct → ARTILLERY (stationary or slow heavy hitters)</li>
 *   <li>Fey, Small/Tiny fast monsters → RECON (scouting roles)</li>
 *   <li>Humanoid with high speed → MECHANIZED</li>
 *   <li>Default → INFANTRY</li>
 * </ul>
 * </p>
 */
@Component
public class Dnd5eTo3R4Adapter implements GroundCombatAdapter {

    private static final String SOURCE_GAME = "Dnd5e";

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
        if (gameUnit instanceof Dnd5eMonster) {
            return convertMonster((Dnd5eMonster) gameUnit);
        } else if (gameUnit instanceof Map) {
            return convertGenericUnit((Map<String, Object>) gameUnit);
        }
        return null;
    }

    /**
     * Converts a D&amp;D 5e Monster to a 3R4 ground unit.
     */
    private ThirdReichGroundUnit convertMonster(Dnd5eMonster monster) {
        String id = "dnd-monster-" + monster.getId();
        ThirdReichUnitType unitType = mapMonsterToUnitType(monster);
        ThirdReichGroundUnit unit = new ThirdReichGroundUnit(id, monster.getName(), unitType);

        // Strength: HP maps directly to 3R4 strength
        int hp = monster.getHp();
        unit.setStrength(Math.max(1, hp));
        unit.setMaxStrength(Math.max(1, hp));

        // Attack: derived from CR and ability scores
        int attack = calculateAttackValue(monster);
        unit.setAttackValue(attack);

        // Defense: derived from AC and CON
        int defense = calculateDefenseValue(monster);
        unit.setDefenseValue(defense);

        // Mobility: derived from speed
        int mobility = calculateMobility(monster);
        unit.setMobility(mobility);

        // Supply cost: based on CR
        int supplyCost = Math.max(1, (int) (monster.getCr() / 2));
        unit.setSupplyCost(supplyCost);

        // Morale: derived from alignment and CR
        int morale = calculateMorale(monster);
        unit.setMorale(morale);

        // Terrain bonus: flying creatures get bonus on open terrain
        if (monster.getFlySpeed() > 0) {
            unit.setTerrainBonus(1);
        }

        unit.setSourceGame(SOURCE_GAME);
        return unit;
    }

    /**
     * Converts a generic map-based unit description.
     */
    private ThirdReichGroundUnit convertGenericUnit(Map<String, Object> unitData) {
        String name = (String) unitData.getOrDefault("name", "Unknown Creature");
        String id = "dnd-unit-" + System.nanoTime();

        ThirdReichUnitType unitType = ThirdReichUnitType.INFANTRY;
        String type = (String) unitData.getOrDefault("type", "INFANTRY");
        switch (type.toUpperCase()) {
            case "TANK":
            case "SIEGE":
            case "DRAGON":
            case "GIANT":
                unitType = ThirdReichUnitType.TANK;
                break;
            case "ARTILLERY":
            case "CONSTRUCT":
                unitType = ThirdReichUnitType.ARTILLERY;
                break;
            case "RECON":
            case "SCOUT":
            case "FEY":
                unitType = ThirdReichUnitType.RECON;
                break;
            case "MECHANIZED":
            case "RIDER":
                unitType = ThirdReichUnitType.MECHANIZED;
                break;
            case "AIR_SUPPORT":
            case "FLYING":
                unitType = ThirdReichUnitType.AIR_SUPPORT;
                break;
        }

        ThirdReichGroundUnit unit = new ThirdReichGroundUnit(id, name, unitType);

        if (unitData.containsKey("strength")) {
            unit.setStrength(((Number) unitData.get("strength")).intValue());
        }
        if (unitData.containsKey("maxStrength")) {
            unit.setMaxStrength(((Number) unitData.get("maxStrength")).intValue());
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
        if (unitData.containsKey("mobility")) {
            unit.setMobility(((Number) unitData.get("mobility")).intValue());
        }
        unit.setSourceGame(SOURCE_GAME);
        return unit;
    }

    /**
     * Maps D&amp;D monster type and size to 3R4 unit type.
     */
    private ThirdReichUnitType mapMonsterToUnitType(Dnd5eMonster monster) {
        String monsterType = monster.getType().toUpperCase();
        String size = monster.getSize().toUpperCase();
        double cr = monster.getCr();

        // Dragons and giants are always heavy siege units
        if (monsterType.equals("DRAGON") || monsterType.equals("GIANT")) {
            return ThirdReichUnitType.TANK;
        }

        // Constructs are stationary heavy hitters
        if (monsterType.equals("CONSTRUCT")) {
            return ThirdReichUnitType.ARTILLERY;
        }

        // Gargantuan creatures are heavy
        if (size.equals("GARGANTUAN")) {
            return ThirdReichUnitType.TANK;
        }

        // Huge creatures with high CR are heavy
        if (size.equals("HUGE") && cr >= 5) {
            return ThirdReichUnitType.TANK;
        }

        // Fast small/tiny creatures are scouts
        if ((size.equals("TINY") || size.equals("SMALL")) && monster.getSpeed() >= 40) {
            return ThirdReichUnitType.RECON;
        }

        // Fey are often scouts
        if (monsterType.equals("FEY")) {
            return ThirdReichUnitType.RECON;
        }

        // Flying creatures with high speed are air support
        if (monster.getFlySpeed() > 0 && monster.getFlySpeed() >= 50) {
            return ThirdReichUnitType.AIR_SUPPORT;
        }

        // High CR humanoids with armor are mechanized
        if (monsterType.equals("HUMANOID") && monster.getAc() >= 16) {
            return ThirdReichUnitType.MECHANIZED;
        }

        // Default to infantry
        return ThirdReichUnitType.INFANTRY;
    }

    /**
     * Calculates attack value from CR and ability scores.
     * Maps to 3R4 attack range (1-10).
     */
    private int calculateAttackValue(Dnd5eMonster monster) {
        double cr = monster.getCr();
        int str = monster.getStr();
        int dex = monster.getDex();

        // Base attack from CR
        int baseAttack = Math.max(1, (int) (cr / 2));

        // Bonus from primary combat ability (STR for melee, DEX for ranged)
        int primaryAbility = Math.max(str, dex);
        int abilityBonus = Math.max(0, (primaryAbility - 10) / 2);

        int attack = baseAttack + abilityBonus / 2;
        return Math.min(10, Math.max(1, attack));
    }

    /**
     * Calculates defense value from AC and CON.
     * Maps to 3R4 defense range (1-10).
     */
    private int calculateDefenseValue(Dnd5eMonster monster) {
        int ac = monster.getAc();
        int con = monster.getCon();

        // AC 10 = base defense 1, each +2 AC = +1 defense
        int baseDefense = Math.max(1, (ac - 8) / 2);

        // CON bonus for durability
        int conBonus = Math.max(0, (con - 10) / 4);

        int defense = baseDefense + conBonus;
        return Math.min(10, Math.max(1, defense));
    }

    /**
     * Maps monster speed to 3R4 mobility scale (1-4).
     */
    private int calculateMobility(Dnd5eMonster monster) {
        int speed = monster.getSpeed();
        int flySpeed = monster.getFlySpeed();

        // Use fly speed if available and higher
        int effectiveSpeed = Math.max(speed, flySpeed);

        if (effectiveSpeed >= 60) return 4;
        if (effectiveSpeed >= 50) return 3;
        if (effectiveSpeed >= 30) return 2;
        return 1;
    }

    /**
     * Calculates morale from alignment and CR.
     * Lawful and high-CR creatures have higher morale.
     */
    private int calculateMorale(Dnd5eMonster monster) {
        String alignment = monster.getAlignment().toLowerCase();
        double cr = monster.getCr();

        // Base morale from CR
        int baseMorale = Math.min(80, 40 + (int) (cr * 3));

        // Alignment modifiers
        if (alignment.contains("lawful")) {
            baseMorale += 10;
        } else if (alignment.contains("chaotic")) {
            baseMorale -= 5;
        }

        // Undead and constructs have very high morale (no fear)
        String type = monster.getType().toUpperCase();
        if (type.equals("UNDEAD") || type.equals("CONSTRUCT")) {
            baseMorale = Math.max(baseMorale, 90);
        }

        // Elementals are very resolute
        if (type.equals("ELEMENTAL")) {
            baseMorale = Math.max(baseMorale, 85);
        }

        return Math.max(0, Math.min(100, baseMorale));
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

        // Add D&amp;D-specific fields
        map.put("dnd5eBattle", true);
        map.put("sourceGame", SOURCE_GAME);
        return map;
    }

    @Override
    public AICommander toCommander(Object gameCharacter) {
        if (gameCharacter instanceof Map) {
            Map<String, Object> stats = (Map<String, Object>) gameCharacter;
            String name = (String) stats.getOrDefault("name", "Unknown Commander");
            String id = "dnd-cmd-" + System.nanoTime();
            AICommander commander = new AICommander(id, name);
            commander.setSourceGame(SOURCE_GAME);

            // Map D&amp;D ability scores to leadership
            // WIS → tactical awareness, CHA → leadership
            int leadership = 5;
            if (stats.containsKey("wisdom")) {
                int wis = ((Number) stats.get("wisdom")).intValue();
                leadership = Math.max(1, Math.min(10, (wis + 5) / 3));
            }
            commander.setLeadership(leadership);

            // Map alignment to tactical preference
            String alignment = (String) stats.getOrDefault("alignment", "neutral");
            if (alignment.toLowerCase().contains("chaotic")) {
                commander.setTacticalPreference(AICommander.TacticalPreference.AGGRESSIVE);
            } else if (alignment.toLowerCase().contains("lawful")) {
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
