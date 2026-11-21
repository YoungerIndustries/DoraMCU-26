package com.yisy.modules.doramcu.traveller;

import java.util.Arrays;
import java.util.List;

/**
 * Careers available in Classic Traveller character creation.
 * <p>
 * Each career represents a professional path a character can follow during
 * their adult life. Careers determine skill acquisition, wealth accumulation,
 * and retirement benefits. Characters typically serve 2–4 careers before
 * retiring to adventure.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 1: Characters & Combat (1981).
 * </p>
 */
public enum Career {

    /**
     * Soldier — military ground forces.
     */
    SOLDIER("Soldier", "Ground combat and military operations",
            Arrays.asList("Melee", "Firearms", "Skirmish", "Guerrilla",
                    "Leadership", "Tactics", "Survival", "Navigation",
                    "Medic", "Psychology")),

    /**
     * Explorer — planetary exploration and survey.
     */
    EXPLORER("Explorer", "Planetary survey and exploration",
            Arrays.asList("Astronomy", "Geology", "Biology", "Chemistry",
                    "Physics", "Navigation", "Survival", "Medic",
                    "Electronics", "Computers")),

    /**
     * Gunner — naval weapons specialist.
     */
    GUNNER("Gunner", "Naval weapons operations",
            Arrays.asList("Missile", "Laser", "Electronics", "Computers",
                    "Navigation", "Tactics", "Psychology", "Medic")),

    /**
     * Pilot — starship and atmospheric pilot.
     */
    PILOT("Pilot", "Starship and atmospheric flight",
            Arrays.asList("Pilot Starship", "Grav", "Navigation", "Electronics",
                    "Computers", "Astronomy", "Psychology", "Medic")),

    /**
     * Merchant — interstellar trade and commerce.
     */
    MERCHANT("Merchant", "Interstellar trade and commerce",
            Arrays.asList("Navigation", "Pilot Starship", "Grav", "Language",
                    "Psychology", "Computers", "Electronics", "Medic")),

    /**
     * Scout — deep space exploration and patrol.
     */
    SCOUT("Scout", "Deep space exploration and patrol",
            Arrays.asList("Pilot Starship", "Grav", "Navigation", "Astronomy",
                    "Electronics", "Computers", "Survival", "Medic",
                    "Psychology", "Tactics")),

    /**
     * Spy — intelligence and covert operations.
     */
    SPY("Spy", "Intelligence and covert operations",
            Arrays.asList("Stealth", "Psychology", "Language", "Forgery",
                    "Computers", "Electronics", "Melee", "Firearms",
                    "Skirmish", "Guerrilla")),

    /**
     * Noble — aristocratic service and politics.
     */
    NOBLE("Noble", "Government, diplomacy, and administration",
            Arrays.asList("Language", "Psychology", "Leadership", "Tactics",
                    "Melee", "Firearms", "Skirmish", "Guerrilla"));

    private final String name;
    private final String description;
    private final List<String> skillTable;

    Career(String name, String description, List<String> skillTable) {
        this.name = name;
        this.description = description;
        this.skillTable = skillTable;
    }

    /**
     * Returns the name of the career.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the career.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the skill table for this career as skill names.
     *
     * @return the list of skill names
     */
    public List<String> getSkillTable() {
        return skillTable;
    }

    /**
     * Returns the display name of the career.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return name;
    }

    /**
     * Returns true if this is a military career (ground or naval).
     *
     * @return true if military
     */
    public boolean isMilitaryCareer() {
        return this == SOLDIER || this == GUNNER || this == PILOT;
    }

    /**
     * Returns true if this is a navy career (ship-based).
     *
     * @return true if navy
     */
    public boolean isNavyCareer() {
        return this == GUNNER || this == PILOT;
    }

    /**
     * Returns true if this is an army career (ground-based).
     *
     * @return true if army
     */
    public boolean isArmyCareer() {
        return this == SOLDIER;
    }
}
