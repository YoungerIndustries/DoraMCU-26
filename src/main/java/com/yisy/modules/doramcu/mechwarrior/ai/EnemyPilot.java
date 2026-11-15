package com.yisy.modules.doramcu.mechwarrior.ai;

import com.yisy.modules.doramcu.mechwarrior.model.MechChassis;
import com.yisy.modules.doramcu.mechwarrior.model.MechPilot;
import com.yisy.modules.doramcu.mechwarrior.model.MechWeapon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents an AI-controlled enemy pilot with a pre-configured mech loadout.
 * <p>
 * Enemy pilots have scaled skills based on difficulty level and use predefined
 * loadouts appropriate for their role (scout, assault, sniper, brawler, support).
 * </p>
 */
public class EnemyPilot {

    public enum EnemyRole {
        SCOUT,      // Light mech, fast, energy weapons
        ASSAULT,    // Heavy/Assault mech, heavy ballistic weapons
        SNIPER,     // Medium/Heavy mech, long-range weapons
        BRAWLER,    // Medium mech, close-range energy weapons
        SUPPORT     // Medium mech, missile weapons
    }

    private final MechPilot pilot;
    private final MechChassis chassis;
    private final List<MechWeapon> weapons;
    private final EnemyRole role;
    private final int difficultyLevel; // 1-5, scales skills and damage

    public EnemyPilot(String name, EnemyRole role, int difficultyLevel) {
        this.role = role;
        this.difficultyLevel = Math.max(1, Math.min(5, difficultyLevel));
        this.pilot = createPilot(name, difficultyLevel);
        this.chassis = createChassis(role, difficultyLevel);
        this.weapons = createLoadout(role, difficultyLevel);
    }

    /**
     * Create a pilot with skills scaled by difficulty.
     */
    private MechPilot createPilot(String name, int difficulty) {
        MechPilot pilot = new MechPilot(name);
        pilot.setId("enemy-" + pilot.getId());

        // Scale skills based on difficulty (1-5 maps to skill levels 1-5)
        int baseSkill = difficulty;

        // Role-specific skill bonuses
        switch (role) {
            case SCOUT:
                pilot.setEnergySkill(Math.min(6, baseSkill + 1));
                pilot.setEvasionSkill(Math.min(6, baseSkill + 1));
                break;
            case ASSAULT:
                pilot.setBallisticSkill(Math.min(6, baseSkill + 1));
                pilot.setShieldingSkill(Math.min(6, baseSkill));
                break;
            case SNIPER:
                pilot.setBallisticSkill(Math.min(6, baseSkill + 1));
                pilot.setMissileSkill(Math.min(6, baseSkill));
                break;
            case BRAWLER:
                pilot.setEnergySkill(Math.min(6, baseSkill + 1));
                pilot.setEvasionSkill(Math.min(6, baseSkill));
                break;
            case SUPPORT:
                pilot.setMissileSkill(Math.min(6, baseSkill + 1));
                pilot.setHeatManagementSkill(Math.min(6, baseSkill));
                break;
        }

        // Set remaining skills to base
        pilot.setBallisticSkill(Math.max(pilot.getBallisticSkill(), Math.max(1, baseSkill - 1)));
        pilot.setEnergySkill(Math.max(pilot.getEnergySkill(), Math.max(1, baseSkill - 1)));
        pilot.setMissileSkill(Math.max(pilot.getMissileSkill(), Math.max(1, baseSkill - 1)));
        pilot.setShieldingSkill(Math.max(pilot.getShieldingSkill(), Math.max(1, baseSkill - 1)));
        pilot.setHeatManagementSkill(Math.max(pilot.getHeatManagementSkill(), Math.max(1, baseSkill - 1)));
        pilot.setEvasionSkill(Math.max(pilot.getEvasionSkill(), Math.max(1, baseSkill - 1)));

        // Set experience level based on difficulty
        switch (difficulty) {
            case 1: pilot.setExperienceLevel(MechPilot.ExperienceLevel.GREEN); break;
            case 2: pilot.setExperienceLevel(MechPilot.ExperienceLevel.ROOKIE); break;
            case 3: pilot.setExperienceLevel(MechPilot.ExperienceLevel.VETERAN); break;
            case 4: pilot.setExperienceLevel(MechPilot.ExperienceLevel.ELITE); break;
            case 5: pilot.setExperienceLevel(MechPilot.ExperienceLevel.LEGEND); break;
        }

        return pilot;
    }

    /**
     * Create a chassis appropriate for the role.
     */
    private MechChassis createChassis(EnemyRole role, int difficulty) {
        MechChassis.MechClass mechClass;
        int tonnage;
        String name;
        String designation;

        switch (role) {
            case SCOUT:
                mechClass = MechChassis.MechClass.LIGHT;
                tonnage = 25 + (difficulty * 2);
                name = "Commando";
                designation = "CMD-" + difficulty + "A";
                break;
            case ASSAULT:
                mechClass = MechChassis.MechClass.ASSAULT;
                tonnage = 85 + (difficulty * 3);
                name = "Atlas";
                designation = "ATL-" + difficulty + "D";
                break;
            case SNIPER:
                mechClass = MechChassis.MechClass.HEAVY;
                tonnage = 65 + (difficulty * 2);
                name = "Griffin";
                designation = "GRF-" + difficulty + "S";
                break;
            case BRAWLER:
                mechClass = MechChassis.MechClass.MEDIUM;
                tonnage = 45 + (difficulty * 2);
                name = "Cyclops";
                designation = "CYC-" + difficulty + "S";
                break;
            case SUPPORT:
                mechClass = MechChassis.MechClass.MEDIUM;
                tonnage = 40 + (difficulty * 2);
                name = "Phoenix Hawk";
                designation = "PHX-" + difficulty + "H";
                break;
            default:
                mechClass = MechChassis.MechClass.MEDIUM;
                tonnage = 50;
                name = "Generic";
                designation = "GEN-00";
        }

        MechChassis chassis = new MechChassis(name, designation, mechClass, tonnage);
        chassis.setMaxSpeed(66.0 + (role == EnemyRole.SCOUT ? 36.0 : 0));
        chassis.setJumpJets(role == EnemyRole.SCOUT ? 6 : 4);
        chassis.setHardpointEnergy(2 + difficulty);
        chassis.setHardpointBallistic(2 + difficulty);
        chassis.setHardpointMissile(1 + (difficulty / 2));
        return chassis;
    }

    /**
     * Create a weapon loadout appropriate for the role.
     */
    private List<MechWeapon> createLoadout(EnemyRole role, int difficulty) {
        List<MechWeapon> loadout = new ArrayList<>();
        Random rand = new Random();

        // Damage multiplier based on difficulty
        double damageMult = 0.8 + (difficulty * 0.2); // 1.0x to 1.8x

        switch (role) {
            case SCOUT:
                // Energy weapons: fast cooldown, medium range
                loadout.add(new MechWeapon("Small Laser", MechWeapon.WeaponType.ENERGY,
                        MechWeapon.WeaponSubType.SMALL_LASER,
                        (int) (2 * damageMult), 1, 0.5));
                loadout.add(new MechWeapon("Medium Laser", MechWeapon.WeaponType.ENERGY,
                        MechWeapon.WeaponSubType.MEDIUM_LASER,
                        (int) (5 * damageMult), 3, 1.0));
                loadout.add(new MechWeapon("SRM-2", MechWeapon.WeaponType.MISSILE,
                        MechWeapon.WeaponSubType.SRM_2,
                        (int) (4 * damageMult), 2, 1.5));
                break;

            case ASSAULT:
                // Heavy ballistic weapons: high damage, slow cooldown
                loadout.add(new MechWeapon("AC/20", MechWeapon.WeaponType.BALLISTIC,
                        MechWeapon.WeaponSubType.AC20,
                        (int) (20 * damageMult), 8, 3.0));
                loadout.add(new MechWeapon("Large Laser", MechWeapon.WeaponType.ENERGY,
                        MechWeapon.WeaponSubType.LARGE_LASER,
                        (int) (10 * damageMult), 6, 1.5));
                loadout.add(new MechWeapon("LRM-10", MechWeapon.WeaponType.MISSILE,
                        MechWeapon.WeaponSubType.LRM_10,
                        (int) (15 * damageMult), 5, 2.5));
                loadout.add(new MechWeapon("Medium Laser", MechWeapon.WeaponType.ENERGY,
                        MechWeapon.WeaponSubType.MEDIUM_LASER,
                        (int) (5 * damageMult), 3, 1.0));
                break;

            case SNIPER:
                // Long-range weapons: high damage, long range
                loadout.add(new MechWeapon("AC/10", MechWeapon.WeaponType.BALLISTIC,
                        MechWeapon.WeaponSubType.AC10,
                        (int) (10 * damageMult), 5, 2.0));
                loadout.add(new MechWeapon("ER Large Laser", MechWeapon.WeaponType.ENERGY,
                        MechWeapon.WeaponSubType.ER_PPC,
                        (int) (12 * damageMult), 7, 1.5));
                loadout.add(new MechWeapon("LRM-15", MechWeapon.WeaponType.MISSILE,
                        MechWeapon.WeaponSubType.LRM_15,
                        (int) (20 * damageMult), 6, 2.5));
                break;

            case BRAWLER:
                // Close-range energy weapons: high heat, fast fire
                loadout.add(new MechWeapon("Large Pulse Laser", MechWeapon.WeaponType.ENERGY,
                        MechWeapon.WeaponSubType.LARGE_PULSE_LASER,
                        (int) (12 * damageMult), 7, 1.0));
                loadout.add(new MechWeapon("Medium Pulse Laser", MechWeapon.WeaponType.ENERGY,
                        MechWeapon.WeaponSubType.MEDIUM_PULSE_LASER,
                        (int) (8 * damageMult), 4, 0.8));
                loadout.add(new MechWeapon("SRM-4 Streak", MechWeapon.WeaponType.MISSILE,
                        MechWeapon.WeaponSubType.SRM_4_STREAK,
                        (int) (10 * damageMult), 3, 1.5));
                break;

            case SUPPORT:
                // Missile weapons: area damage, medium range
                loadout.add(new MechWeapon("LRM-10", MechWeapon.WeaponType.MISSILE,
                        MechWeapon.WeaponSubType.LRM_10,
                        (int) (15 * damageMult), 5, 2.5));
                loadout.add(new MechWeapon("SRM-6", MechWeapon.WeaponType.MISSILE,
                        MechWeapon.WeaponSubType.SRM_6,
                        (int) (12 * damageMult), 4, 2.0));
                loadout.add(new MechWeapon("Medium Laser", MechWeapon.WeaponType.ENERGY,
                        MechWeapon.WeaponSubType.MEDIUM_LASER,
                        (int) (5 * damageMult), 3, 1.0));
                break;
        }

        return loadout;
    }

    // Getters

    public MechPilot getPilot() {
        return pilot;
    }

    public MechChassis getChassis() {
        return chassis;
    }

    public List<MechWeapon> getWeapons() {
        return weapons;
    }

    public EnemyRole getRole() {
        return role;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }
}
