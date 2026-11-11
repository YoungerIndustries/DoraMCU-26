package com.yisy.modules.doramcu.earth2026.model;

/**
 * Predefined Earth time period templates for game scenarios.
 * <p>
 * Each template defines a historical era with its corresponding tech level,
 * typical terrain types, available structure types, and weather conditions.
 * Games can request a time period template from Earth 2026's PlanetService
 * to build battle environments or scenarios appropriate to that era.
 * </p>
 * <p>
 * For example, Third Reich can request the WWII template (TL4) to get
 * European theater maps with fortifications, cities, and weather data
 * appropriate for 1939-1945 battles.
 * </p>
 */
public enum TimePeriodTemplate {

    /** Ancient era — Bronze Age civilizations, empires, and warfare. */
    ANCIENT("Ancient", TechLevel.TL1,
            "Bronze Age civilizations, early empires",
            "-3000 to 1450 CE",
            "Siege warfare, phalanx combat, chariot battles",
            "Pyramids, ziggurats, temples, city walls, aqueducts, forums"),

    /** Medieval era — castles, knights, feudalism, crusades. */
    MEDIEVAL("Medieval", TechLevel.TL2,
            "Castles, knights, feudalism, crusades",
            "600 to 1450 CE",
            "Siege warfare, jousting, castle defense, mounted combat",
            "Castles, cathedrals, villages, marketplaces, monasteries, keeps"),

    /** Renaissance era — gunpowder, exploration, early science. */
    RENAISSANCE("Renaissance", TechLevel.TL3,
            "Gunpowder weapons, exploration, early science",
            "1450 to 1800 CE",
            "Naval warfare, musket combat, early artillery",
            "Fortresses, harbors, trading posts, palaces, universities"),

    /** Industrial era — steam power, colonialism, early modern warfare. */
    INDUSTRIAL("Industrial", TechLevel.TL4,
            "Steam power, mass production, colonial warfare",
            "1800 to 1914 CE",
            "Trench warfare, naval battles, colonial campaigns",
            "Factories, railroads, telegraph stations, barracks, docks"),

    /** World War II era — tanks, aircraft, radar, early nuclear. */
    WWII("World War II", TechLevel.TL4,
            "Tanks, aircraft, radar, early nuclear weapons",
            "1939 to 1945 CE",
            "Blitzkrieg, island hopping, D-Day, Pacific campaigns",
            "Bunkers, airfields, ports, cities, bridges, command centers, U-boat pens"),

    /** Cold War era — nuclear deterrence, proxy wars, space race. */
    COLD_WAR("Cold War", TechLevel.TL5,
            "Nuclear deterrence, proxy wars, space race",
            "1945 to 1991 CE",
            "Proxy conflicts, espionage, nuclear standoff",
            "Silos, bases, listening posts, space stations, underground facilities"),

    /** Modern era — information age, globalization, special ops. */
    MODERN("Modern", TechLevel.TL5,
            "Information age, globalization, special operations",
            "1991 to 2026 CE",
            "Counter-terrorism, cyber warfare, drone operations",
            "Skyscrapers, data centers, military bases, airports, hospitals"),

    /** Near Future — AI, nanotechnology, climate change. */
    NEAR_FUTURE("Near Future", TechLevel.TL6,
            "AI systems, nanotechnology, climate adaptation",
            "2026 to 2100 CE",
            "AI-assisted combat, drone swarms, cyber warfare",
            "Smart cities, orbital elevators, fusion plants, research labs"),

    /** Space Age — orbital colonies, interplanetary travel. */
    SPACE_AGE("Space Age", TechLevel.TL7,
            "Orbital colonies, interplanetary travel begins",
            "2100 to 2300 CE",
            "Orbital defense, asteroid mining, Mars colonization",
            "Space stations, lunar bases, orbital shipyards, terraforming plants"),

    /** Interplanetary — solar system civilization. */
    INTERPLANETARY("Interplanetary", TechLevel.TL8,
            "Solar system civilization, FTL communications",
            "2300 to 2500 CE",
            "Planetary defense, asteroid warfare, colony conflicts",
            "Mars cities, Jupiter stations, asteroid habitats, FTL relays"),

    /** Early Stellar — interstellar travel begins. */
    EARLY_STELLAR("Early Stellar", TechLevel.TL10,
            "Interstellar travel via Jump Drive",
            "2500 to 3000 CE",
            "First contact, colony wars, trade route conflicts",
            "Jump gates, space ports, terraforming worlds, star forges"),

    /** Average Stellar — galaxy-spanning civilization. */
    AVERAGE_STELLAR("Average Stellar", TechLevel.TL13,
            "Galaxy-spanning civilization, Imperium-level tech",
            "3000+ CE",
            "Imperial wars, pirate conflicts, alien diplomacy",
            "Dyson swarms, planet crackers, hypernet hubs, AI temples");

    private final String name;
    private final TechLevel baseTechLevel;
    private final String description;
    private final String dateRange;
    private final String typicalConflicts;
    private final String typicalStructures;

    TimePeriodTemplate(String name, TechLevel baseTechLevel, String description,
                       String dateRange, String typicalConflicts, String typicalStructures) {
        this.name = name;
        this.baseTechLevel = baseTechLevel;
        this.description = description;
        this.dateRange = dateRange;
        this.typicalConflicts = typicalConflicts;
        this.typicalStructures = typicalStructures;
    }

    /** Returns the display name (e.g., "World War II", "Modern"). */
    public String getName() {
        return name;
    }

    /** Returns the base tech level for this time period. */
    public TechLevel getBaseTechLevel() {
        return baseTechLevel;
    }

    /** Returns a description of this time period. */
    public String getDescription() {
        return description;
    }

    /** Returns the historical date range (e.g., "1939 to 1945 CE"). */
    public String getDateRange() {
        return dateRange;
    }

    /** Returns the typical types of conflicts in this era. */
    public String getTypicalConflicts() {
        return typicalConflicts;
    }

    /** Returns the typical structures found in this era. */
    public String getTypicalStructures() {
        return typicalStructures;
    }

    /**
     * Parse a comma-separated list of structure names into an array.
     */
    public String[] getStructureTypes() {
        return typicalStructures.split(",\\s*");
    }

    /**
     * Parse a comma-separated list of conflict types into an array.
     */
    public String[] getConflictTypes() {
        return typicalConflicts.split(",\\s*");
    }

    /**
     * Find a TimePeriodTemplate by name (case-insensitive).
     *
     * @param name the template name
     * @return the matching template
     * @throws IllegalArgumentException if not found
     */
    public static TimePeriodTemplate fromName(String name) {
        for (TimePeriodTemplate tpl : values()) {
            if (tpl.name.equalsIgnoreCase(name)) {
                return tpl;
            }
        }
        throw new IllegalArgumentException("Unknown time period template: " + name);
    }

    /**
     * Find a TimePeriodTemplate by tech level.
     * Returns the template whose base tech level matches.
     *
     * @param tl the tech level
     * @return the matching template, or null if no exact match
     */
    public static TimePeriodTemplate fromTechLevel(TechLevel tl) {
        for (TimePeriodTemplate tpl : values()) {
            if (tpl.baseTechLevel == tl) {
                return tpl;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name + " (" + baseTechLevel + ")";
    }
}
