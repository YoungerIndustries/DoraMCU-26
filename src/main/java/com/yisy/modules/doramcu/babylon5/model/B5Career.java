package com.yisy.modules.doramcu.babylon5.model;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a Babylon 5 career path for character creation.
 * <p>
 * Careers define the skills, attribute bonuses, and advancement opportunities
 * available to a character during their lifepath. Each career has a skill table
 * from which skills are randomly selected during character advancement.
 * </p>
 * <p>
 * Based on the Babylon 5 universe, including Earth Force, Narn military,
 * Centauri nobility, Minbari warrior castes, and more.
 * </p>
 */
public enum B5Career {

    EARTH_FORCE("Earth Force", "Earth Alliance military service",
            "Earth Alliance",
            Arrays.asList("ST+1", "DX+1"),
            Arrays.asList(B5Skill.MELEE, B5Skill.FIREARMS, B5Skill.SKIRMISH, B5Skill.TACTICS,
                    B5Skill.LEADERSHIP, B5Skill.STARSHIPS, B5Skill.NAVIGATION, B5Skill.MEDICAL,
                    B5Skill.ELECTRONICS, B5Skill.PSYCHOLOGY),
            true),

    NARN_MILITARY("Narn Military", "Narn Regent's Guard service",
            "Narn",
            Arrays.asList("ST+1", "EN+1"),
            Arrays.asList(B5Skill.MELEE, B5Skill.FIREARMS, B5Skill.GUERRILLA, B5Skill.SKIRMISH,
                    B5Skill.TACTICS, B5Skill.SURVIVAL, B5Skill.STARSHIPS, B5Skill.MEDICAL,
                    B5Skill.HAND_TO_HAND, B5Skill.PSYCHOLOGY),
            true),

    CENTAURI_NOBILITY("Centauri Nobility", "Centauri Republic political service",
            "Centauri",
            Arrays.asList("SO+1", "IN+1"),
            Arrays.asList(B5Skill.DIPLOMACY, B5Skill.POLITICS, B5Skill.PSYCHOLOGY, B5Skill.LANGUAGE,
                    B5Skill.MELEE, B5Skill.FIREARMS, B5Skill.LEADERSHIP, B5Skill.TACTICS,
                    B5Skill.BARKEEPING, B5Skill.STEALTH),
            true),

    MINBARI_WARRIOR("Minbari Warrior", "Minbari Warrior Caste service",
            "Minbari",
            Arrays.asList("ST+1", "DX+1"),
            Arrays.asList(B5Skill.MELEE, B5Skill.FIREARMS, B5Skill.SKIRMISH, B5Skill.TACTICS,
                    B5Skill.LEADERSHIP, B5Skill.STARSHIPS, B5Skill.NAVIGATION, B5Skill.MEDICAL,
                    B5Skill.HAND_TO_HAND, B5Skill.PSYCHOLOGY),
            true),

    MINBARI_TECHNICAL("Minbari Technical", "Minbari Technical Caste service",
            "Minbari",
            Arrays.asList("IN+1", "ED+1"),
            Arrays.asList(B5Skill.ELECTRONICS, B5Skill.COMPUTERS, B5Skill.ENGINEERING, B5Skill.ASTRONOMY,
                    B5Skill.PHYSICS, B5Skill.CHEMISTRY, B5Skill.NAVIGATION, B5Skill.MEDICAL,
                    B5Skill.STARSHIPS, B5Skill.ANCIENT_TECH),
            true),

    INDEPENDENT_MERCHANT("Independent Merchant", "Interstellar trade and commerce",
            "Independent",
            Arrays.asList("SO+1", "IN+1"),
            Arrays.asList(B5Skill.NAVIGATION, B5Skill.STARSHIPS, B5Skill.PILOTING, B5Skill.LANGUAGE,
                    B5Skill.PSYCHOLOGY, B5Skill.COMPUTERS, B5Skill.ELECTRONICS, B5Skill.MEDICAL,
                    B5Skill.DIPLOMACY, B5Skill.FIREARMS),
            true),

    INDEPENDENT_ADVENTURER("Independent Adventurer", "Independent exploration and adventure",
            "Independent",
            Arrays.asList("DX+1", "ST+1"),
            Arrays.asList(B5Skill.SURVIVAL, B5Skill.STEALTH, B5Skill.FIREARMS, B5Skill.MELEE,
                    B5Skill.PILOTING, B5Skill.NAVIGATION, B5Skill.MEDICAL, B5Skill.PSYCHOLOGY,
                    B5Skill.LANGUAGE, B5Skill.GUERRILLA),
            true),

    VREE_MERCHANT("Vree Merchant", "Vree trading corporation service",
            "Vree",
            Arrays.asList("SO+2", "IN+1"),
            Arrays.asList(B5Skill.DIPLOMACY, B5Skill.POLITICS, B5Skill.LANGUAGE, B5Skill.COMPUTERS,
                    B5Skill.NAVIGATION, B5Skill.STARSHIPS, B5Skill.PSYCHOLOGY, B5Skill.MEDICAL,
                    B5Skill.ELECTRONICS, B5Skill.FIREARMS),
            true),

    HYACH_PEACEKEEPER("Hyach Peacekeeper", "Hyach defensive service",
            "Hyach",
            Arrays.asList("ST+1", "EN+1"),
            Arrays.asList(B5Skill.MELEE, B5Skill.FIREARMS, B5Skill.SKIRMISH, B5Skill.TACTICS,
                    B5Skill.SURVIVAL, B5Skill.MEDICAL, B5Skill.STARSHIPS, B5Skill.NAVIGATION,
                    B5Skill.HAND_TO_HAND, B5Skill.PSYCHOLOGY),
            true),

    BRAKIRI_SPIRITUAL("Brakiri Spiritual", "Brakiri spiritual service",
            "Brakiri",
            Arrays.asList("SO+1", "IN+1"),
            Arrays.asList(B5Skill.PSYCHOLOGY, B5Skill.MEDICAL, B5Skill.LANGUAGE, B5Skill.DIPLOMACY,
                    B5Skill.POLITICS, B5Skill.SURVIVAL, B5Skill.FIRST_AID, B5Skill.STEALTH,
                    B5Skill.PSIONICS, B5Skill.BARKEEPING),
            true),

    PAKMARA_RESISTANCE("Pak'ma'ra Resistance", "Pak'ma'ra guerrilla service",
            "Pak'ma'ra",
            Arrays.asList("DX+1", "EN+1"),
            Arrays.asList(B5Skill.GUERRILLA, B5Skill.STEALTH, B5Skill.FIREARMS, B5Skill.SKIRMISH,
                    B5Skill.SURVIVAL, B5Skill.MEDICAL, B5Skill.INTRUSION_COUNTER, B5Skill.PSYCHOLOGY,
                    B5Skill.ELECTRONICS, B5Skill.NAVIGATION),
            true),

    GOBLYN_SCAVENGER("Goblyn Scavenger", "Goblyn salvage and scavenging",
            "Goblyn",
            Arrays.asList("DX+1", "IN+1"),
            Arrays.asList(B5Skill.ELECTRONICS, B5Skill.ENGINEERING, B5Skill.COMPUTERS, B5Skill.STEALTH,
                    B5Skill.SURVIVAL, B5Skill.MEDICAL, B5Skill.PILOTING, B5Skill.NAVIGATION,
                    B5Skill.ANCIENT_TECH, B5Skill.FIREARMS),
            true),

    MARKAB_MERCENARY("Markab Mercenary", "Markab mercenary service",
            "Markab",
            Arrays.asList("DX+1", "ST+1"),
            Arrays.asList(B5Skill.FIREARMS, B5Skill.MELEE, B5Skill.SKIRMISH, B5Skill.TACTICS,
                    B5Skill.PILOTING, B5Skill.STARSHIPS, B5Skill.NAVIGATION, B5Skill.MEDICAL,
                    B5Skill.ELECTRONICS, B5Skill.PSYCHOLOGY),
            true);

    private final String name;
    private final String description;
    private final String faction;
    private final List<String> attributeBonuses;
    private final List<B5Skill> skillTable;
    private final boolean allRaces;

    B5Career(String name, String description, String faction,
             List<String> attributeBonuses, List<B5Skill> skillTable, boolean allRaces) {
        this.name = name;
        this.description = description;
        this.faction = faction;
        this.attributeBonuses = attributeBonuses;
        this.skillTable = skillTable;
        this.allRaces = allRaces;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getFaction() {
        return faction;
    }

    public List<String> getAttributeBonuses() {
        return attributeBonuses;
    }

    /**
     * Returns the skill table for this career.
     * <p>
     * When a character gains a skill during a career term, one skill
     * is randomly selected from this table.
     * </p>
     *
     * @return the list of possible skills
     */
    public List<B5Skill> getSkillTable() {
        return skillTable;
    }

    public boolean isAllRaces() {
        return allRaces;
    }

    /**
     * Returns true if this is a military career.
     */
    public boolean isMilitaryCareer() {
        return this == EARTH_FORCE || this == NARN_MILITARY || this == MINBARI_WARRIOR ||
               this == HYACH_PEACEKEEPER || this == MARKAB_MERCENARY;
    }

    /**
     * Returns true if this is a civilian career.
     */
    public boolean isCivilianCareer() {
        return !isMilitaryCareer();
    }
}
