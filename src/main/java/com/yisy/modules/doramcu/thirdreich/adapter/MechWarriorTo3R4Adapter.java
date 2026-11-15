package com.yisy.modules.doramcu.thirdreich.adapter;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.mechwarrior.engine.MechWarriorMech;
import com.yisy.modules.doramcu.mechwarrior.model.MechChassis;
import com.yisy.modules.doramcu.mechwarrior.model.MechPilot;
import com.yisy.modules.doramcu.mechwarrior.model.MechWeapon;
import com.yisy.modules.doramcu.thirdreich.commander.AICommander;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichBattleResult;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichUnitType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for MechWarrior BattleMechs.
 * <p>
 * Converts MechWarrior mechs to 3R4 ground units using the MECH unit type,
 * mapping chassis tonnage, weapons, and pilot skills to 3R4 combat stats.
 * Mech pilots are mapped to AI commanders using their experience level and skills.
 * </p>
 * <p>
 * Stat mapping:
 * <ul>
 *   <li>Strength: Derived from chassis tonnage and remaining armor/structure</li>
 *   <li>Attack: Sum of weapon damage scaled by pilot weapon skills</li>
 *   <li>Defense: Chassis armor points scaled by pilot shielding skill</li>
 *   <li>Mobility: Chassis max speed mapped to 3R4 mobility scale</li>
 *   <li>Morale: Derived from pilot stress level and experience</li>
 * </ul>
 * </p>
 */
@Component
public class MechWarriorTo3R4Adapter implements GroundCombatAdapter {

    private static final String SOURCE_GAME = "MechWarrior";

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
        if (gameUnit instanceof MechWarriorMech) {
            return convertMech((MechWarriorMech) gameUnit);
        } else if (gameUnit instanceof MechChassis) {
            return convertChassis((MechChassis) gameUnit);
        } else if (gameUnit instanceof Map) {
            return convertGenericUnit((Map<String, Object>) gameUnit);
        }
        return null;
    }

    /**
     * Converts a full MechWarriorMech instance (with pilot, chassis, weapons, and current state).
     */
    private ThirdReichGroundUnit convertMech(MechWarriorMech mech) {
        MechChassis chassis = mech.getChassis();
        MechPilot pilot = mech.getPilot();
        String id = "mw-mech-" + chassis.getDesignation() + "-" + System.nanoTime();

        // Determine 3R4 unit type based on mech class
        ThirdReichUnitType unitType = mapMechClassToUnitType(chassis);
        ThirdReichGroundUnit unit = new ThirdReichGroundUnit(id, chassis.getName(), unitType);

        // Strength: based on remaining armor and structure as percentage of max
        int totalArmor = chassis.getArmorPoints();
        int totalStructure = chassis.getStructurePoints();
        int maxHp = totalArmor + totalStructure;
        int currentHp = calculateCurrentHp(mech);
        unit.setStrength(Math.max(1, currentHp));
        unit.setMaxStrength(Math.max(1, maxHp));

        // Attack: sum of weapon damage scaled by pilot skills
        int attack = calculateAttackValue(mech, pilot);
        unit.setAttackValue(attack);

        // Defense: based on armor and pilot shielding skill
        int defense = calculateDefenseValue(chassis, pilot);
        unit.setDefenseValue(defense);

        // Mobility: based on chassis speed
        int mobility = calculateMobility(chassis);
        unit.setMobility(mobility);

        // Supply cost: based on tonnage
        int supplyCost = Math.max(1, chassis.getTonnage() / 20);
        unit.setSupplyCost(supplyCost);

        // Morale: derived from pilot stress and experience
        int morale = calculateMorale(pilot);
        unit.setMorale(morale);

        // Commander: pilot becomes the commander
        AICommander commander = toCommander(pilot);
        if (commander != null) {
            unit.setCommanderId(commander.getId());
        }

        unit.setSourceGame(SOURCE_GAME);
        return unit;
    }

    /**
     * Converts a chassis definition without a pilot (for unit catalog purposes).
     */
    private ThirdReichGroundUnit convertChassis(MechChassis chassis) {
        String id = "mw-chassis-" + chassis.getDesignation() + "-" + System.nanoTime();
        ThirdReichUnitType unitType = mapMechClassToUnitType(chassis);
        ThirdReichGroundUnit unit = new ThirdReichGroundUnit(id, chassis.getName(), unitType);

        int totalArmor = chassis.getArmorPoints();
        int totalStructure = chassis.getStructurePoints();
        unit.setStrength(totalArmor + totalStructure);
        unit.setMaxStrength(totalArmor + totalStructure);

        // Default attack based on hardpoints
        int hardpoints = chassis.getHardpointEnergy() + chassis.getHardpointBallistic() + chassis.getHardpointMissile();
        unit.setAttackValue(Math.max(1, hardpoints));

        // Default defense based on armor
        unit.setDefenseValue(Math.max(1, totalArmor / 10));

        // Mobility from speed
        unit.setMobility(calculateMobility(chassis));

        // Supply from tonnage
        unit.setSupplyCost(Math.max(1, chassis.getTonnage() / 20));

        unit.setMorale(70);
        unit.setSourceGame(SOURCE_GAME);
        return unit;
    }

    /**
     * Converts a generic map-based unit description.
     */
    private ThirdReichGroundUnit convertGenericUnit(Map<String, Object> unitData) {
        String name = (String) unitData.getOrDefault("name", "Unknown Mech");
        String id = "mw-unit-" + System.nanoTime();

        ThirdReichUnitType unitType = ThirdReichUnitType.MECH;
        if (unitData.containsKey("type")) {
            String type = (String) unitData.get("type");
            switch (type.toUpperCase()) {
                case "LIGHT":
                case "SCOUT":
                    unitType = ThirdReichUnitType.RECON;
                    break;
                case "HEAVY":
                case "ASSAULT":
                    unitType = ThirdReichUnitType.MECH;
                    break;
                case "ARTILLERY":
                case "BOMBARDMENT":
                    unitType = ThirdReichUnitType.ARTILLERY;
                    break;
            }
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
     * Maps Mech class to 3R4 unit type.
     * Light mechs map to RECON for scouting roles; all others map to MECH.
     */
    private ThirdReichUnitType mapMechClassToUnitType(MechChassis chassis) {
        switch (chassis.getMechClass()) {
            case LIGHT:
                return ThirdReichUnitType.RECON;
            case MEDIUM:
            case HEAVY:
            case ASSAULT:
            default:
                return ThirdReichUnitType.MECH;
        }
    }

    /**
     * Calculates current HP from mech component states.
     */
    private int calculateCurrentHp(MechWarriorMech mech) {
        int currentHp = 0;
        for (var component : mech.getAllComponents().values()) {
            currentHp += component.getArmorCurrent() + component.getStructureCurrent();
        }
        return Math.max(1, currentHp);
    }

    /**
     * Calculates attack value from weapons and pilot skills.
     */
    private int calculateAttackValue(MechWarriorMech mech, MechPilot pilot) {
        int totalDamage = 0;
        for (MechWeapon weapon : mech.getAllWeapons()) {
            totalDamage += weapon.getDamage();
        }

        // Scale damage to 3R4 attack range (1-10)
        int baseAttack = Math.max(1, totalDamage / 10);

        // Apply pilot skill bonus
        double skillBonus = 0;
        skillBonus += pilot.getAccuracyBonus(MechWeapon.WeaponType.BALLISTIC);
        skillBonus += pilot.getAccuracyBonus(MechWeapon.WeaponType.ENERGY);
        skillBonus += pilot.getAccuracyBonus(MechWeapon.WeaponType.MISSILE);
        int skillAttack = (int) (baseAttack * (1.0 + skillBonus));

        return Math.min(10, Math.max(1, skillAttack));
    }

    /**
     * Calculates defense value from armor and pilot shielding skill.
     */
    private int calculateDefenseValue(MechChassis chassis, MechPilot pilot) {
        int baseDefense = Math.max(1, chassis.getArmorPoints() / 20);
        double shieldingBonus = pilot.getDamageReductionBonus();
        int defense = (int) (baseDefense * (1.0 + shieldingBonus));
        return Math.min(10, Math.max(1, defense));
    }

    /**
     * Maps chassis speed to 3R4 mobility scale (1-4).
     */
    private int calculateMobility(MechChassis chassis) {
        double speed = chassis.getMaxSpeed();
        if (speed >= 120) return 4;
        if (speed >= 90) return 3;
        if (speed >= 60) return 2;
        return 1;
    }

    /**
     * Calculates morale from pilot stress and experience.
     */
    private int calculateMorale(MechPilot pilot) {
        // Base morale from experience level
        int baseMorale;
        switch (pilot.getExperienceLevel()) {
            case GREEN:
                baseMorale = 50;
                break;
            case ROOKIE:
                baseMorale = 65;
                break;
            case VETERAN:
                baseMorale = 80;
                break;
            case ELITE:
                baseMorale = 90;
                break;
            case LEGEND:
                baseMorale = 100;
                break;
            default:
                baseMorale = 70;
        }

        // Reduce by stress
        int morale = baseMorale - pilot.getStressLevel() / 2;

        // Broken pilots have very low morale
        if (pilot.isBroken()) {
            morale = Math.min(morale, 20);
        }

        return Math.max(0, Math.min(100, morale));
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

        // Add MechWarrior-specific fields
        map.put("mechWarriorBattle", true);
        map.put("sourceGame", SOURCE_GAME);
        return map;
    }

    @Override
    public AICommander toCommander(Object gameCharacter) {
        if (gameCharacter instanceof MechPilot) {
            return convertPilotToCommander((MechPilot) gameCharacter);
        } else if (gameCharacter instanceof Map) {
            Map<String, Object> stats = (Map<String, Object>) gameCharacter;
            String name = (String) stats.getOrDefault("name", "Unknown Pilot");
            String id = "mw-cmd-" + System.nanoTime();
            AICommander commander = new AICommander(id, name);
            commander.setSourceGame(SOURCE_GAME);

            if (stats.containsKey("leadership")) {
                commander.setLeadership(((Number) stats.get("leadership")).intValue());
            }
            return commander;
        }
        return null;
    }

    /**
     * Converts a MechPilot to an AI Commander.
     * <p>
     * Leadership is derived from the pilot's experience level and traits.
     * Tactical preference is derived from the pilot's traits and skills.
     * </p>
     */
    private AICommander convertPilotToCommander(MechPilot pilot) {
        String id = "mw-cmd-" + pilot.getId();
        AICommander commander = new AICommander(id, pilot.getName());
        commander.setSourceGame(SOURCE_GAME);

        // Leadership from experience level
        int leadership;
        switch (pilot.getExperienceLevel()) {
            case GREEN:
                leadership = 3;
                break;
            case ROOKIE:
                leadership = 5;
                break;
            case VETERAN:
                leadership = 7;
                break;
            case ELITE:
                leadership = 9;
                break;
            case LEGEND:
                leadership = 10;
                break;
            default:
                leadership = 5;
        }

        // Bonus for Leader trait
        if (pilot.hasTrait(MechPilot.PilotTrait.LEADER)) {
            leadership = Math.min(10, leadership + 1);
        }

        // Bonus for Veteran trait
        if (pilot.hasTrait(MechPilot.PilotTrait.VETERAN)) {
            leadership = Math.min(10, leadership + 1);
        }

        commander.setLeadership(leadership);

        // Tactical preference from traits
        if (pilot.hasTrait(MechPilot.PilotTrait.BRAWLER)) {
            commander.setTacticalPreference(AICommander.TacticalPreference.AGGRESSIVE);
        } else if (pilot.hasTrait(MechPilot.PilotTrait.SNIPER)) {
            commander.setTacticalPreference(AICommander.TacticalPreference.DEFENSIVE);
        } else if (pilot.hasTrait(MechPilot.PilotTrait.SCOUT)) {
            commander.setTacticalPreference(AICommander.TacticalPreference.BALANCED);
        }

        return commander;
    }

    @Override
    public String getSourceGame() {
        return SOURCE_GAME;
    }
}
