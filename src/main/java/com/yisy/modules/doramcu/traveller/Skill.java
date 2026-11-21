package com.yisy.modules.doramcu.traveller;

/**
 * Represents a character skill in the Traveller system.
 */
public class Skill {

    public enum SkillCategory {
        COMBAT, LEADERSHIP, TECHNICAL, SCIENTIFIC, SURVIVAL, SOCIAL, OTHER,
        NAVIGATION, STEALTH
    }

    public enum SkillType {
        CAR, COMPUTERS, ELECTRONICS, GRAV, GUN_LIGHT, GUN_HEAVY,
        ADMINISTRATION, EDUCATION, LAW, PSYCHOLOGY, SCIENCE, STREETWISE,
        ANIMAL, BIOLOGY, MEDIC, SURVIVAL, ZERO_G,
        ARMOR, BOAT, JUMP, PILOT_AIRSCREW, PILOT_FIGHTER, PILOT_HELIX, PILOT_JET, PILOT_ROTORCRAFT, PILOT_STARSHIP,
        ACROBATICS, ATHLETICS, CLOSE_COMBAT, DANCING, DRIVING, GAMBLE, HANDBALL,
        BARGAIN, BRIBE, CHARM, DECEPTION, INTIMIDATION, SINGING,
        CODE_BREAKING, CRYPTOGRAPHY, DETECTIVE, FORENSICS, HACKER, INVESTIGATION,
        ARTIST, MUSICIAN, PERFORMANCE,
        BIG_GUN, BIG_LASER, MISSILE, ROCKET,
        ADMIN, EDUCATION_SKILL, LAW_SKILL, PSYCHOLOGY_SKILL,
        NONE
    }

    private SkillType type;
    private int level;

    public Skill() {
        this.type = SkillType.NONE;
        this.level = 0;
    }

    public Skill(SkillType type, int level) {
        this.type = type;
        this.level = level;
    }

    public SkillType getType() {
        return type;
    }

    public void setType(SkillType type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return type.name().replace('_', ' ');
    }

    public String getDescription() {
        return getName() + " skill (level " + level + ")";
    }

    public SkillCategory getCategory() {
        return SkillCategory.OTHER;
    }

    // Static constants for special contract skills
    public static final Skill ENTREPRENEUR = new Skill(SkillType.STREETWISE, 1);
    public static final Skill DIPLOMACY = new Skill(SkillType.PSYCHOLOGY, 1);
    public static final Skill ADMINISTRATION = new Skill(SkillType.ADMINISTRATION, 1);
    public static final Skill FIREARMS = new Skill(SkillType.GUN_LIGHT, 1);
    public static final Skill MELEE = new Skill(SkillType.CLOSE_COMBAT, 1);
    public static final Skill NAVIGATION = new Skill(SkillType.PILOT_STARSHIP, 1);
    public static final Skill STEALTH = new Skill(SkillType.STREETWISE, 1);
    public static final Skill SURVIVAL = new Skill(SkillType.SURVIVAL, 1);
    public static final Skill LEADERSHIP = new Skill(SkillType.ADMINISTRATION, 1);
    public static final Skill TACTICS = new Skill(SkillType.GUN_HEAVY, 1);
    public static final Skill COMPUTERS = new Skill(SkillType.COMPUTERS, 1);
    public static final Skill ELECTRONICS = new Skill(SkillType.ELECTRONICS, 1);
    public static final Skill ASTRONOMY = new Skill(SkillType.SCIENCE, 1);
    public static final Skill LANGUAGE = new Skill(SkillType.EDUCATION, 1);
    public static final Skill PSYCHOLOGY = new Skill(SkillType.PSYCHOLOGY, 1);

    @Override
    public String toString() {
        return type + " " + level;
    }
}
