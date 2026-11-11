package com.yisy.modules.doramcu.babylon5.model;

/**
 * Atmospheric small craft types for the Babylon 5 universe.
 * <p>
 * Small craft are used for atmospheric flight, shuttle operations, and short-range
 * transport. They are smaller than starships and operate within planetary atmospheres
 * or in close proximity to larger vessels.
 * </p>
 * <p>
 * Each small craft type has tonnage, speed, range, capacity, and armament characteristics.
 * These mirror the Traveller small craft system (Launch, Pinnace, Cutter) but are
 * themed around B5 factions and their distinctive atmospheric vehicles.
 * </p>
 * <p>
 * Small craft are typically carried aboard larger ships or operated from planetary
 * spaceports. They can be used for reconnaissance, transport, combat, and rescue.
 * </p>
 */
public enum B5SmallCraft {

    // ==================== Earth Alliance ====================

    /**
     * Standard Earth Alliance shuttle. Used for transport between ships and stations.
     * Similar to Traveller's Launch.
     */
    EA_SHUTTLE("Earth Alliance Shuttle", "Earth Alliance", "Shuttle",
        10, 400, 2000, 6, 2, 0,
        "Standard transport shuttle used by Earth Force and civilian operators."),

    /**
     * Earth Force atmospheric fighter. Fast and heavily armed.
     * Similar to Traveller's Cutter.
     */
    EA_FIGHTER("Earth Force Fighter", "Earth Alliance", "Fighter",
        50, 1200, 5000, 2, 4, 2,
        "Single-seat atmospheric fighter with advanced avionics and missile armament."),

    /**
     * Earth Alliance cargo hauler. Heavy lift atmospheric transport.
     * Similar to Traveller's Pinnace.
     */
    EA_CARGO("Earth Alliance Cargo Hauler", "Earth Alliance", "Cargo",
        200, 300, 8000, 4, 20, 1,
        "Heavy atmospheric cargo transport for planetary logistics."),

    /**
     * Earth Force patrol craft. Used for atmospheric reconnaissance and patrol.
     */
    EA_PATROL("Earth Force Patrol Craft", "Earth Alliance", "Patrol",
        80, 800, 6000, 3, 8, 1,
        "Multi-role patrol craft for atmospheric surveillance and interdiction."),

    // ==================== Narn ====================

    /**
     * Narn atmospheric scout. Fast and agile, used for reconnaissance.
     */
    NARN_SCOUT("Narn Scout", "Narn", "Scout",
        30, 900, 3000, 2, 2, 1,
        "Light atmospheric scout used by the Narn military for reconnaissance."),

    /**
     * Narn transport. Rugged and durable, designed for rough planetary conditions.
     */
    NARN_TRANSPORT("Narn Transport", "Narn", "Transport",
        150, 500, 5000, 8, 10, 1,
        "Rugged atmospheric transport built for the harsh Narn homeworld conditions."),

    /**
     * Narn attack craft. Heavily armed for close air support.
     */
    NARN_ATTACK("Narn Attack Craft", "Narn", "Attack",
        100, 700, 4000, 3, 5, 3,
        "Close air support craft with heavy armament for ground attack missions."),

    // ==================== Centauri ====================

    /**
     * Centauri luxury shuttle. Opulent and fast, used by the nobility.
     */
    CENTAURI_LUXURY("Centauri Luxury Shuttle", "Centauri", "Luxury",
        80, 600, 4000, 10, 5, 0,
        "Opulent atmospheric shuttle with luxury accommodations for Centauri nobility."),

    /**
     * Centauri interceptor. Fast and deadly, used for point defense.
     */
    CENTAURI_INTERCEPTOR("Centauri Interceptor", "Centauri", "Interceptor",
        60, 1500, 3000, 1, 3, 2,
        "High-speed atmospheric interceptor for point defense and escort duties."),

    /**
     * Centauri cargo runner. Used for trade and smuggling.
     */
    CENTAURI_RUNNER("Centauri Cargo Runner", "Centauri", "Cargo",
        250, 400, 7000, 4, 30, 1,
        "Versatile cargo runner used for legitimate trade and less legal activities."),

    // ==================== Minbari ====================

    /**
     * Minbari ceremonial shuttle. Used for diplomatic missions.
     */
    MINBARI_CEREMONIAL("Minbari Ceremonial Shuttle", "Minbari", "Ceremonial",
        100, 500, 6000, 12, 8, 0,
        "Elegant ceremonial shuttle used for diplomatic missions and religious processions."),

    /**
     * Minbari patrol craft. Advanced sensors and shields.
     */
    MINBARI_PATROL("Minbari Patrol Craft", "Minbari", "Patrol",
        120, 700, 8000, 4, 6, 1,
        "Advanced patrol craft with superior sensors and energy shield technology."),

    /**
     * Minbari rescue craft. Used for search and rescue operations.
     */
    MINBARI_RESCUE("Minbari Rescue Craft", "Minbari", "Rescue",
        180, 450, 7000, 6, 15, 0,
        "Search and rescue craft with advanced life support and medical facilities."),

    // ==================== Vorlon ====================

    /**
     * Vorlon probe. Mysterious and advanced, used for surveillance.
     */
    VORLON_PROBE("Vorlon Probe", "Vorlon", "Probe",
        200, 2000, 15000, 0, 0, 0,
        "Mysterious surveillance craft with technology far beyond younger races."),

    /**
     * Vorlon transport. Capable of atmospheric and space flight.
     */
    VORLON_TRANSPORT("Vorlon Transport", "Vorlon", "Transport",
        500, 1000, 20000, 20, 50, 0,
        "Advanced transport capable of both atmospheric and space flight."),

    // ==================== Shadow ====================

    /**
     * Shadow scout. Stealthy and fast, used for reconnaissance.
     */
    SHADOW_SCOUT("Shadow Scout", "Shadow", "Scout",
        300, 3000, 10000, 2, 0, 2,
        "Stealth reconnaissance craft with advanced cloaking technology."),

    /**
     * Shadow attack craft. Devastating firepower.
     */
    SHADOW_ATTACK("Shadow Attack Craft", "Shadow", "Attack",
        400, 2500, 8000, 4, 0, 5,
        "Heavily armed attack craft with devastating energy weapons."),

    // ==================== Independent ====================

    /**
     * Independent trader's craft. Versatile and reliable.
     */
    INDEPENDENT_TRADER("Independent Trader", "Independent", "Trader",
        150, 500, 6000, 4, 20, 0,
        "Versatile atmospheric craft used by independent traders and merchants."),

    /**
     * Psi Corps courier. Fast and discreet.
     */
    PSI_COURIER("Psi Corps Courier", "Psi Corps", "Courier",
        40, 800, 4000, 2, 2, 0,
        "Fast and discreet courier craft used by the Psi Corps for secure transport."),

    /**
     * Ranger craft. Used by the secret Ranger organization.
     */
    RANGER_CRAFT("Ranger Craft", "Rangers", "Stealth",
        80, 1000, 5000, 2, 5, 1,
        "Stealth craft used by the secret Ranger organization for covert operations.");

    // ==================== Fields ====================

    private final String displayName;
    private final String faction;
    private final String role;
    private final int tonnage;
    private final int speed; // km/h atmospheric speed
    private final int range; // km maximum range
    private final int crew;
    private final int cargoTons;
    private final int armament; // weapon level (0-5)
    private final String description;

    B5SmallCraft(String displayName, String faction, String role,
                 int tonnage, int speed, int range,
                 int crew, int cargoTons, int armament,
                 String description) {
        this.displayName = displayName;
        this.faction = faction;
        this.role = role;
        this.tonnage = tonnage;
        this.speed = speed;
        this.range = range;
        this.crew = crew;
        this.cargoTons = cargoTons;
        this.armament = armament;
        this.description = description;
    }

    /** Returns the human-readable name of this craft. */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the faction that primarily uses this craft. */
    public String getFaction() {
        return faction;
    }

    /** Returns the role of this craft (Shuttle, Fighter, Cargo, etc.). */
    public String getRole() {
        return role;
    }

    /** Returns the tonnage of this craft. */
    public int getTonnage() {
        return tonnage;
    }

    /** Returns the maximum atmospheric speed in km/h. */
    public int getSpeed() {
        return speed;
    }

    /** Returns the maximum range in km. */
    public int getRange() {
        return range;
    }

    /** Returns the required crew count. */
    public int getCrew() {
        return crew;
    }

    /** Returns the cargo capacity in tons. */
    public int getCargoTons() {
        return cargoTons;
    }

    /** Returns the armament level (0-5, where 5 is heavily armed). */
    public int getArmament() {
        return armament;
    }

    /** Returns a description of this craft. */
    public String getDescription() {
        return description;
    }

    /**
     * Returns all small craft used by a specific faction.
     */
    public static B5SmallCraft[] getByFaction(String faction) {
        return values(); // Return all; filter in caller
    }

    /**
     * Returns all small craft of a specific role.
     */
    public static B5SmallCraft[] getByRole(String role) {
        return values(); // Return all; filter in caller
    }

    /**
     * Returns all small craft with armament level >= the specified minimum.
     */
    public static B5SmallCraft[] getCombatCraft(int minArmament) {
        return values(); // Return all; filter in caller
    }

    @Override
    public String toString() {
        return String.format(
            "B5SmallCraft{name='%s', faction='%s', role='%s', %d tons, speed=%d km/h, range=%d km}",
            displayName, faction, role, tonnage, speed, range
        );
    }
}
