package com.yisy.modules.doramcu.captainslog.model;

/**
 * Star Trek species from the Captain's Log Solo RPG and Star Trek canon.
 * <p>
 * Each species has unique traits affecting combat, skills, trade, and social interactions.
 * These traits mirror the Traveller {@code AlienRace} stat model for consistency
 * across game systems in The Void galaxy AI character creation.
 * </p>
 * <p>
 * Species are derived from Star Trek canon and the Captain's Log Solo RPG rulebook.
 * </p>
 */
public enum StarTrekSpecies {

    /**
     * Humans — from Earth or human colonies.
     * Versatile and adaptable, humans excel in many fields.
     * +1 Presence, +1 Reason.
     */
    HUMAN("Human", "From Earth or a human colony. Versatile and adaptable, humans excel in many fields.",
            0, 1, 0, 0, 0),

    /**
     * Vulcans — logical and disciplined.
     * Rich tradition of scientific and scholarly pursuit.
     * +2 Reason (represented as +1 Social DM for logic-based diplomacy).
     */
    VULCAN("Vulcan", "Logical and disciplined. Vulcans have a rich tradition of scientific and scholarly pursuit.",
            0, 1, 0, 0, 0),

    /**
     * Klingons — fierce warriors who value honor above all.
     * A Klingon's strength is legendary.
     * +2 Strength (represented as +1 Combat DM).
     */
    KLINGON("Klingon", "Fierce warriors who value honor above all. A Klingon's strength is legendary.",
            1, -1, 0, 1, 0),

    /**
     * Andorians — honor-bound warriors from a cold world.
     * Tough and resilient.
     * +1 Fitness, +1 Strength (represented as +1 Combat DM).
     */
    ANDORIAN("Andorian", "Honor-bound warriors from a cold world. Andorians are tough and resilient.",
            1, 0, 0, 1, 0),

    /**
     * Betazoids — empathic communicators with a deep understanding of others.
     * Natural diplomats.
     * +2 Presence (represented as +2 Social DM).
     */
    BETAZOID("Betazoid", "Empathic communicators with a deep understanding of others. Natural diplomats.",
            0, 2, 0, 0, 0),

    /**
     * Denobulans — master healers and scientists.
     * Among the greatest medical minds in the galaxy.
     * +1 Technical, +1 Insight (represented as +1 Navigation DM for science).
     */
    DENOBUAN("Denobulan", "Master healers and scientists. Denobulans are among the greatest medical minds.",
            0, 0, 1, 0, 0),

    /**
     * Ferengi — profit-driven traders with sharp minds for business and technology.
     * +2 Technical (represented as +2 Trade Bonus).
     */
    FERENGI("Ferengi", "Profit-driven traders with sharp minds for business and technology.",
            -1, 0, 0, 0, 3),

    /**
     * Trill — symbiotic beings with combined wisdom of past hosts.
     * Incredibly knowledgeable.
     * +2 Insight (represented as +1 Social DM for wisdom, +1 Navigation DM for knowledge).
     */
    TRILL("Trill", "Symbiotic beings with combined wisdom of past hosts. Incredibly knowledgeable.",
            0, 1, 1, 0, 0),

    /**
     * Bajorans — spiritual and resilient.
     * Have endured much and emerged stronger.
     * +1 Insight, +1 Presence (represented as +1 Social DM).
     */
    BAJORAN("Bajoran", "Spiritual and resilient. Bajorans have endured much and emerged stronger.",
            0, 1, 0, 0, 0);

    private final String displayName;
    private final String description;

    /**
     * Combat die modifier (DM) for melee and firearms.
     */
    private final int combatDM;

    /**
     * Social die modifier for interaction and diplomacy.
     */
    private final int socialDM;

    /**
     * Navigation die modifier for piloting, science, and navigation.
     */
    private final int navigationDM;

    /**
     * Armor bonus (extra armor class).
     */
    private final int armorBonus;

    /**
     * Trade profit bonus percentage.
     */
    private final int tradeBonusPercent;

    StarTrekSpecies(String displayName, String description, int combatDM, int socialDM,
                    int navigationDM, int armorBonus, int tradeBonusPercent) {
        this.displayName = displayName;
        this.description = description;
        this.combatDM = combatDM;
        this.socialDM = socialDM;
        this.navigationDM = navigationDM;
        this.armorBonus = armorBonus;
        this.tradeBonusPercent = tradeBonusPercent;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public int getCombatDM() { return combatDM; }
    public int getSocialDM() { return socialDM; }
    public int getNavigationDM() { return navigationDM; }
    public int getArmorBonus() { return armorBonus; }
    public int getTradeBonusPercent() { return tradeBonusPercent; }

    @Override
    public String toString() {
        return displayName;
    }
}
