package com.yisy.modules.doramcu.traveller;

/**
 * Skills available to Traveller characters.
 */
public enum Skill {
    // Combat skills
    MELEE(SkillCategory.COMBAT),
    FIREARMS(SkillCategory.COMBAT),
    GUNNERY(SkillCategory.COMBAT),
    SKIRMISH(SkillCategory.COMBAT),
    GUERRILLA(SkillCategory.COMBAT),
    MISSILE(SkillCategory.COMBAT),
    LASER(SkillCategory.COMBAT),

    // Leadership skills
    LEADERSHIP(SkillCategory.LEADERSHIP),
    TACTICS(SkillCategory.LEADERSHIP),

    // Technical skills
    COMPUTERS(SkillCategory.TECHNICAL),
    ELECTRONICS(SkillCategory.TECHNICAL),
    CARMECH(SkillCategory.TECHNICAL),
    ELEMECH(SkillCategory.TECHNICAL),
    FORGERY(SkillCategory.TECHNICAL),

    // Navigation skills
    NAVIGATION(SkillCategory.NAVIGATION),
    ASTRONOMY(SkillCategory.NAVIGATION),
    STARSHIPS(SkillCategory.NAVIGATION),
    GRAVITIC(SkillCategory.NAVIGATION),
    PILOT_ASTRO(SkillCategory.NAVIGATION),
    PILOT_FIGHTER(SkillCategory.NAVIGATION),
    PILOT_HEAVY(SkillCategory.NAVIGATION),
    PILOT_LIGHT(SkillCategory.NAVIGATION),

    // Scientific skills
    BIOLOGY(SkillCategory.SCIENTIFIC),
    CHEMISTRY(SkillCategory.SCIENTIFIC),
    GEOLOGY(SkillCategory.SCIENTIFIC),
    MATHEMATICS(SkillCategory.SCIENTIFIC),
    PHYSICS(SkillCategory.SCIENTIFIC),
    MEDICAL(SkillCategory.SCIENTIFIC),
    MEDIC(SkillCategory.SCIENTIFIC),

    // Survival skills
    SURVIVAL(SkillCategory.SURVIVAL),
    JOCKEY(SkillCategory.SURVIVAL),
    STOWAGE(SkillCategory.SURVIVAL),
    ZERO_G(SkillCategory.SURVIVAL),
    ACROBATICS(SkillCategory.SURVIVAL),
    ATHLETICS(SkillCategory.SURVIVAL),

    // Stealth skills
    STEALTH(SkillCategory.STEALTH),
    PERCEPTION(SkillCategory.STEALTH),

    // Social skills
    LANGUAGE(SkillCategory.SOCIAL),
    PSYCHOLOGY(SkillCategory.SOCIAL),
    GAMBLE(SkillCategory.SOCIAL),
    STREETWISE(SkillCategory.SOCIAL),
    ADMINISTRATION(SkillCategory.SOCIAL),
    APPRAISAL(SkillCategory.SOCIAL),
    CRIMINAL(SkillCategory.SOCIAL),
    DIPLOMACY(SkillCategory.SOCIAL),
    ENTREPRENEUR(SkillCategory.SOCIAL),
    INTIMIDATION(SkillCategory.SOCIAL),
    PERFORMANCE(SkillCategory.SOCIAL),
    SCAM(SkillCategory.SOCIAL),
    ANIMAL_HANDLING(SkillCategory.SOCIAL),
    ARTISAN(SkillCategory.SOCIAL),
    SOCIOLOGY(SkillCategory.SOCIAL);

    private final SkillCategory category;

    Skill(SkillCategory category) {
        this.category = category;
    }

    /**
     * Returns the category of this skill.
     *
     * @return the skill category
     */
    public SkillCategory getCategory() {
        return category;
    }

    /**
     * Returns the display name of this skill (lowercase enum name converted to readable form).
     *
     * @return the display name
     */
    public String getName() {
        return name().replace('_', ' ').toLowerCase();
    }

    /**
     * Returns a short description of this skill.
     *
     * @return the description
     */
    public String getDescription() {
        return getName() + " (" + category.name().toLowerCase() + ")";
    }

    /**
     * Categories for grouping skills.
     */
    public enum SkillCategory {
        /** Combat and weapons skills */
        COMBAT,
        /** Leadership and tactics skills */
        LEADERSHIP,
        /** Technical and engineering skills */
        TECHNICAL,
        /** Navigation and piloting skills */
        NAVIGATION,
        /** Scientific and medical skills */
        SCIENTIFIC,
        /** Survival and physical skills */
        SURVIVAL,
        /** Stealth and perception skills */
        STEALTH,
        /** Social and interpersonal skills */
        SOCIAL
    }
}
