package com.yisy.modules.doramcu.sfb.model;

/**
 * Skills available in SFB character creation.
 * <p>
 * Skills represent specific abilities a character has learned through
 * career service, education, or personal study. Each skill enhances
 * a character's capabilities in a specific area.
 * </p>
 * <p>
 * Skills are organized by career skill tables. When a character gains
 * a skill during a career tour, one is randomly selected from the
 * career's skill table.
 * </p>
 * <p>
 * SFB-themed skills based on the Star Fleet Battles universe, including
 * Starfleet, Klingon Defense Force, Romulan Sentinel, and more.
 * </p>
 */
public enum SfbSkill {

    // Combat skills
    PHASER("Phaser", "Phaser weapon operation and tactics", SkillCategory.COMBAT),
    PHOTON_TORPEDO("Photon Torpedo", "Torpedo weapon operation and targeting", SkillCategory.COMBAT),
    MELEE("Melee", "Unarmed and melee weapon combat", SkillCategory.COMBAT),
    BAT_LETH("Bat'leth", "Klingon bat'leth combat techniques", SkillCategory.COMBAT),
    HAND_TO_HAND("Hand-to-Hand", "Close-quarters combat techniques", SkillCategory.COMBAT),
    SKIRMISH("Skirmish", "Small unit tactics and combat", SkillCategory.COMBAT),

    // Science skills
    ASTROPHYSICS("Astrophysics", "Study of stars, planets, and space phenomena", SkillCategory.SCIENCE),
    BIOLOGY("Biology", "Study of living organisms", SkillCategory.SCIENCE),
    XENO_BIOLOGY("Xenobiology", "Study of alien life forms", SkillCategory.SCIENCE),
    CHEMISTRY("Chemistry", "Study of chemical compounds and reactions", SkillCategory.SCIENCE),
    PHYSICS("Physics", "Study of physical laws and phenomena", SkillCategory.SCIENCE),
    GEOLOGY("Geology", "Study of planetary geology and minerals", SkillCategory.SCIENCE),

    // Engineering skills
    ENGINEERING("Engineering", "Ship and station systems repair", SkillCategory.ENGINEERING),
    WARP_DRIVE("Warp Drive", "Warp drive operation and maintenance", SkillCategory.ENGINEERING),
    SHIELD_SYSTEMS("Shield Systems", "Deflector shield operation and tuning", SkillCategory.ENGINEERING),
    WEAPONS_SYSTEMS("Weapons Systems", "Ship weapons operation and maintenance", SkillCategory.ENGINEERING),
    ELECTRONICS("Electronics", "Repair and operation of electronic devices", SkillCategory.ENGINEERING),
    COMPUTER("Computer", "Computer programming and operation", SkillCategory.ENGINEERING),

    // Medical skills
    MEDICAL("Medical", "Medical treatment and surgery", SkillCategory.MEDICAL),
    FIRST_AID("First Aid", "Emergency medical treatment", SkillCategory.MEDICAL),
    PSYCHIATRY("Psychiatry", "Mental health and behavioral treatment", SkillCategory.MEDICAL),

    // Tactics and leadership
    TACTICS("Tactics", "Strategic planning and battlefield command", SkillCategory.TACTICS),
    LEADERSHIP("Leadership", "Command and motivation of crew", SkillCategory.TACTICS),
    DIPLOMACY("Diplomacy", "Negotiation and political maneuvering", SkillCategory.TACTICS),
    NAVIGATION("Navigation", "Starship and planetary navigation", SkillCategory.TACTICS),

    // Survival skills
    SURVIVAL("Survival", "Living in hostile environments", SkillCategory.SURVIVAL),
    PILOTING("Piloting", "Atmospheric and space flight", SkillCategory.SURVIVAL),
    STARSHIPS("Starships", "Starship operation and systems", SkillCategory.SURVIVAL),

    // Stealth skills
    STEALTH("Stealth", "Moving unseen and avoiding detection", SkillCategory.STEALTH),
    INTRUSION_COUNTER("Intrusion Counter", "Detecting and preventing breaches", SkillCategory.STEALTH),
    FORGERY("Forgery", "Creation of false documents and items", SkillCategory.STEALTH),

    // SFB-specific skills
    KLINGON_HONOR("Klingon Honor", "Klingon combat honor and traditions", SkillCategory.TACTICS),
    ROMULAN_TACTICS("Romulan Tactics", "Romulan military strategies", SkillCategory.TACTICS),
    FEDERATION_PROTOCOL("Federation Protocol", "Federation diplomatic procedures", SkillCategory.TACTICS),
    WARP_THEORY("Warp Theory", "Advanced warp drive theory", SkillCategory.SCIENCE),
    TRILL_KNOWLEDGE("Trill Knowledge", "Symbiont-enhanced knowledge retention", SkillCategory.SCIENCE);

    private final String name;
    private final String description;
    private final SkillCategory category;

    SfbSkill(String name, String description, SkillCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    /**
     * Returns the name of the skill.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the skill.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the category of the skill.
     *
     * @return the category
     */
    public SkillCategory getCategory() {
        return category;
    }

    /**
     * Categories of skills in SFB.
     */
    public enum SkillCategory {
        COMBAT("Combat"),
        SCIENCE("Science"),
        ENGINEERING("Engineering"),
        MEDICAL("Medical"),
        TACTICS("Tactics"),
        LEADERSHIP("Leadership"),
        SURVIVAL("Survival"),
        STEALTH("Stealth");

        private final String name;

        SkillCategory(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
