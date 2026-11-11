package com.yisy.modules.doramcu.thirdreich.commander;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Predefined historical commander profiles for the 3R4 ground combat engine.
 * <p>
 * These profiles represent famous WWII commanders with fixed leadership ratings,
 * traits, and tactical preferences based on their historical reputation.
 * </p>
 */
public enum AICommanderProfile {

    /**
     * Erwin Rommel - "The Desert Fox". Master of mobile warfare and deception.
     */
    ROMMEL("rommel", "Erwin Rommel", 9, AICommander.TacticalPreference.AGGRESSIVE,
            Arrays.asList(CommanderTrait.ARMOR_SPECIALIST, CommanderTrait.FLANKER, CommanderTrait.AGGRESSIVE),
            "Master of mobile warfare and deception"),

    /**
     * George Patton - Bold tank commander known for aggressive offensives.
     */
    PATTON("patton", "George Patton", 8, AICommander.TacticalPreference.AGGRESSIVE,
            Arrays.asList(CommanderTrait.ARMOR_SPECIALIST, CommanderTrait.AGGRESSIVE, CommanderTrait.INSPIRATIONAL),
            "Bold tank commander, aggressive offensives"),

    /**
     * Georgy Zhukov - Soviet marshal who orchestrated major counteroffensives.
     */
    ZHUKOV("zhukov", "Georgy Zhukov", 9, AICommander.TacticalPreference.AGGRESSIVE,
            Arrays.asList(CommanderTrait.ARMOR_SPECIALIST, CommanderTrait.ARTILLERY_FOCUS, CommanderTrait.INSPIRATIONAL),
            "Soviet marshal, master of counteroffensive"),

    /**
     * Bernard Montgomery - Cautious British commander, meticulous planner.
     */
    MONTGOMERY("montgomery", "Bernard Montgomery", 8, AICommander.TacticalPreference.DEFENSIVE,
            Arrays.asList(CommanderTrait.CAUTIOUS, CommanderTrait.LOGISTICIAN, CommanderTrait.ARMOR_SPECIALIST),
            "Cautious planner, meticulous preparation"),

    /**
     * Heinz Guderian - Father of Blitzkrieg, tank warfare pioneer.
     */
    GUDERIAN("guderian", "Heinz Guderian", 8, AICommander.TacticalPreference.AGGRESSIVE,
            Arrays.asList(CommanderTrait.ARMOR_SPECIALIST, CommanderTrait.FLANKER, CommanderTrait.AGGRESSIVE),
            "Father of Blitzkrieg, tank warfare pioneer"),

    /**
     * Fyodor Tolbukhin - Soviet commander in Balkans and Crimea.
     */
    TOLBUKHIN("tolbukhin", "Fyodor Tolbukhin", 7, AICommander.TacticalPreference.BALANCED,
            Arrays.asList(CommanderTrait.INFANTRY_EXPERT, CommanderTrait.ARTILLERY_FOCUS),
            "Soviet commander, Balkans and Crimea"),

    /**
     * Omar Bradley - American army group commander, master of logistics.
     */
    BRADLEY("bradley", "Omar Bradley", 7, AICommander.TacticalPreference.BALANCED,
            Arrays.asList(CommanderTrait.LOGISTICIAN, CommanderTrait.INFANTRY_EXPERT, CommanderTrait.CAUTIOUS),
            "American army group commander, logistics master"),

    /**
     * Konstantin Rokossovsky - Soviet hero of Stalingrad and Berlin.
     */
    ROKOSSOVSKY("rokossovsky", "Konstantin Rokossovsky", 9, AICommander.TacticalPreference.AGGRESSIVE,
            Arrays.asList(CommanderTrait.ARMOR_SPECIALIST, CommanderTrait.INSPIRATIONAL, CommanderTrait.FLANKER),
            "Soviet hero of Stalingrad and Berlin"),

    /**
     * Chester Nimitz - Naval commander adapted to ground operations.
     */
    NIMITZ("nimitz", "Chester Nimitz", 8, AICommander.TacticalPreference.BALANCED,
            Arrays.asList(CommanderTrait.CAUTIOUS, CommanderTrait.LOGISTICIAN, CommanderTrait.ARTILLERY_FOCUS),
            "Naval commander, strategic planner"),

    /**
     * Manstein - Brilliant German strategist, master of encirclement.
     */
    MANSTEIN("manstein", "Erich von Manstein", 9, AICommander.TacticalPreference.AGGRESSIVE,
            Arrays.asList(CommanderTrait.FLANKER, CommanderTrait.ARMOR_SPECIALIST, CommanderTrait.AGGRESSIVE),
            "Brilliant strategist, master of encirclement");

    private final String id;
    private final String name;
    private final int leadership;
    private final AICommander.TacticalPreference tacticalPreference;
    private final List<CommanderTrait> traits;
    private final String description;

    AICommanderProfile(String id, String name, int leadership,
                       AICommander.TacticalPreference tacticalPreference,
                       List<CommanderTrait> traits, String description) {
        this.id = id;
        this.name = name;
        this.leadership = leadership;
        this.tacticalPreference = tacticalPreference;
        this.traits = traits;
        this.description = description;
    }

    /** Returns the profile ID. */
    public String getId() {
        return id;
    }

    /** Returns the commander's name. */
    public String getName() {
        return name;
    }

    /** Returns the leadership rating (1-10). */
    public int getLeadership() {
        return leadership;
    }

    /** Returns the tactical preference. */
    public AICommander.TacticalPreference getTacticalPreference() {
        return tacticalPreference;
    }

    /** Returns the list of traits. */
    public List<CommanderTrait> getTraits() {
        return traits;
    }

    /** Returns the description. */
    public String getDescription() {
        return description;
    }

    /**
     * Creates an AICommander instance from this profile.
     *
     * @return a new AICommander with this profile's settings
     */
    public AICommander createCommander() {
        AICommander commander = new AICommander(id, name);
        commander.setLeadership(leadership);
        commander.setTacticalPreference(tacticalPreference);
        for (CommanderTrait trait : traits) {
            commander.addTrait(trait);
        }
        commander.setHistoricalProfile(true);
        return commander;
    }

    /**
     * Returns all available historical profiles.
     */
    public static List<AICommanderProfile> getAllProfiles() {
        return new ArrayList<>(List.of(AICommanderProfile.values()));
    }
}
