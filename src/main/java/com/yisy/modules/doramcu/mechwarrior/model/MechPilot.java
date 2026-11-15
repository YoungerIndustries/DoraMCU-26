package com.yisy.modules.doramcu.mechwarrior.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a MechWarrior pilot with skills, traits, and experience.
 * <p>
 * Pilots have six skill categories: Ballistic, Energy, Missile (weapon skills)
 * and Shielding, Heat Management, Evasion (piloting skills).
 * Pilots gain experience from missions and can develop traits.
 * </p>
 */
public class MechPilot {

    public enum PilotSkill {
        BALLISTIC,      // Weapon skill: ballistic weapons
        ENERGY,         // Weapon skill: energy weapons
        MISSILE,        // Weapon skill: missile weapons
        SHIELDING,      // Piloting skill: damage reduction
        HEAT_MANAGEMENT, // Piloting skill: heat dissipation
        EVASION         // Piloting skill: dodge and stability
    }

    public enum PilotTrait {
        // Combat traits
        VETERAN,           // Combat veteran with bonus accuracy
        SCOUT,             // Fast and agile pilot
        BRAWLER,           // Close combat specialist
        SNIPER,            // Long-range specialist
        HEAT_MASTER,       // Exceptional heat management
        IRON_WILL,         // Resistant to stress and shock

        // Mech-specific traits
        LIGHT_MECH_PILOT,  // Bonus with light mechs
        MEDIUM_MECH_PILOT, // Bonus with medium mechs
        HEAVY_MECH_PILOT,  // Bonus with heavy mechs
        ASSAULT_PILOT,     // Bonus with assault mechs

        // Special traits
        QUICK_REFLEXES,    // Faster reaction time
        STEADY_HANDS,      // Improved accuracy
        MECHANIC,          // Can repair mechs faster
        LEADER             // Bonus to squad performance
    }

    public enum ExperienceLevel {
        GREEN(0),        // New recruit
        ROOKIE(100),     // Some experience
        VETERAN(500),    // Battle-tested
        ELITE(1500),     // Highly skilled
        LEGEND(5000);    // Legendary warrior

        private final int requiredPoints;

        ExperienceLevel(int requiredPoints) {
            this.requiredPoints = requiredPoints;
        }

        public int getRequiredPoints() {
            return requiredPoints;
        }

        public ExperienceLevel getNextLevel() {
            ExperienceLevel[] values = ExperienceLevel.values();
            int ordinal = this.ordinal();
            if (ordinal < values.length - 1) {
                return values[ordinal + 1];
            }
            return null;
        }
    }

    private String id;
    private String name;
    private ExperienceLevel experienceLevel;
    private int experiencePoints;
    private int stressLevel;          // 0-100, affects performance
    private boolean broken;           // pilot is broken under pressure
    private List<PilotSkill> skills;  // skill levels (1-6 per skill)
    private List<PilotTrait> traits;  // acquired traits
    private int wins;
    private int losses;
    private int kills;
    private int mechsDestroyed;
    private String biography;

    // Skill levels (1-6 for each skill)
    private int ballisticSkill;
    private int energySkill;
    private int missileSkill;
    private int shieldingSkill;
    private int heatManagementSkill;
    private int evasionSkill;

    public MechPilot() {
    }

    public MechPilot(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.experienceLevel = ExperienceLevel.GREEN;
        this.experiencePoints = 0;
        this.stressLevel = 0;
        this.broken = false;
        this.skills = new ArrayList<>();
        this.traits = new ArrayList<>();
        this.wins = 0;
        this.losses = 0;
        this.kills = 0;
        this.mechsDestroyed = 0;
        this.ballisticSkill = 1;
        this.energySkill = 1;
        this.missileSkill = 1;
        this.shieldingSkill = 1;
        this.heatManagementSkill = 1;
        this.evasionSkill = 1;
    }

    /**
     * Gain experience points and potentially level up.
     */
    public void gainExperience(int points) {
        experiencePoints += points;

        // Check for level up
        ExperienceLevel nextLevel = experienceLevel.getNextLevel();
        if (nextLevel != null && experiencePoints >= nextLevel.getRequiredPoints()) {
            experienceLevel = nextLevel;
        }
    }

    /**
     * Increase a specific skill.
     */
    public void increaseSkill(PilotSkill skill, int amount) {
        int newLevel = getSkillLevel(skill) + amount;
        newLevel = Math.min(6, Math.max(1, newLevel)); // Cap at 1-6

        switch (skill) {
            case BALLISTIC -> ballisticSkill = newLevel;
            case ENERGY -> energySkill = newLevel;
            case MISSILE -> missileSkill = newLevel;
            case SHIELDING -> shieldingSkill = newLevel;
            case HEAT_MANAGEMENT -> heatManagementSkill = newLevel;
            case EVASION -> evasionSkill = newLevel;
        }
    }

    /**
     * Get the current level of a specific skill.
     */
    public int getSkillLevel(PilotSkill skill) {
        return switch (skill) {
            case BALLISTIC -> ballisticSkill;
            case ENERGY -> energySkill;
            case MISSILE -> missileSkill;
            case SHIELDING -> shieldingSkill;
            case HEAT_MANAGEMENT -> heatManagementSkill;
            case EVASION -> evasionSkill;
        };
    }

    /**
     * Add stress to the pilot.
     * If stress reaches 100, the pilot becomes broken.
     */
    public void addStress(int amount) {
        stressLevel = Math.min(100, stressLevel + amount);
        if (stressLevel >= 100) {
            broken = true;
        }
    }

    /**
     * Reduce stress (recovery).
     */
    public void reduceStress(int amount) {
        stressLevel = Math.max(0, stressLevel - amount);
        if (stressLevel < 50) {
            broken = false;
        }
    }

    /**
     * Add a trait to the pilot.
     */
    public void addTrait(PilotTrait trait) {
        if (!traits.contains(trait)) {
            traits.add(trait);
        }
    }

    /**
     * Check if the pilot has a specific trait.
     */
    public boolean hasTrait(PilotTrait trait) {
        return traits.contains(trait);
    }

    /**
     * Get accuracy bonus based on weapon skill.
     */
    public double getAccuracyBonus(MechWeapon.WeaponType weaponType) {
        int skill = switch (weaponType) {
            case ENERGY -> energySkill;
            case BALLISTIC -> ballisticSkill;
            case MISSILE -> missileSkill;
        };
        // Each skill level above 1 gives +5% accuracy
        return (skill - 1) * 0.05;
    }

    /**
     * Get heat reduction bonus based on heat management skill.
     */
    public double getHeatReductionBonus() {
        // Each skill level above 1 gives +5% heat reduction
        return (heatManagementSkill - 1) * 0.05;
    }

    /**
     * Get damage reduction bonus based on shielding skill.
     */
    public double getDamageReductionBonus() {
        // Each skill level above 1 gives +3% damage reduction
        return (shieldingSkill - 1) * 0.03;
    }

    /**
     * Get stability bonus based on evasion skill.
     */
    public double getStabilityBonus() {
        // Each skill level above 1 gives +5% stability
        return (evasionSkill - 1) * 0.05;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(ExperienceLevel experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public int getExperiencePoints() {
        return experiencePoints;
    }

    public void setExperiencePoints(int experiencePoints) {
        this.experiencePoints = experiencePoints;
    }

    public int getStressLevel() {
        return stressLevel;
    }

    public void setStressLevel(int stressLevel) {
        this.stressLevel = stressLevel;
    }

    public boolean isBroken() {
        return broken;
    }

    public void setBroken(boolean broken) {
        this.broken = broken;
    }

    public List<PilotSkill> getSkills() {
        return skills;
    }

    public void setSkills(List<PilotSkill> skills) {
        this.skills = skills;
    }

    public List<PilotTrait> getTraits() {
        return traits;
    }

    public void setTraits(List<PilotTrait> traits) {
        this.traits = traits;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getMechsDestroyed() {
        return mechsDestroyed;
    }

    public void setMechsDestroyed(int mechsDestroyed) {
        this.mechsDestroyed = mechsDestroyed;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public int getBallisticSkill() {
        return ballisticSkill;
    }

    public void setBallisticSkill(int ballisticSkill) {
        this.ballisticSkill = ballisticSkill;
    }

    public int getEnergySkill() {
        return energySkill;
    }

    public void setEnergySkill(int energySkill) {
        this.energySkill = energySkill;
    }

    public int getMissileSkill() {
        return missileSkill;
    }

    public void setMissileSkill(int missileSkill) {
        this.missileSkill = missileSkill;
    }

    public int getShieldingSkill() {
        return shieldingSkill;
    }

    public void setShieldingSkill(int shieldingSkill) {
        this.shieldingSkill = shieldingSkill;
    }

    public int getHeatManagementSkill() {
        return heatManagementSkill;
    }

    public void setHeatManagementSkill(int heatManagementSkill) {
        this.heatManagementSkill = heatManagementSkill;
    }

    public int getEvasionSkill() {
        return evasionSkill;
    }

    public void setEvasionSkill(int evasionSkill) {
        this.evasionSkill = evasionSkill;
    }

}
