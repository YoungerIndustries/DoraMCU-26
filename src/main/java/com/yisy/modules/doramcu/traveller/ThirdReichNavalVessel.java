package com.yisy.modules.doramcu.traveller;

import java.util.Objects;

/**
 * Represents a historical Third Reich naval vessel with combat factors.
 * <p>
 * This class models naval vessels from the Rise and Decline of the Third Reich (3R4) game,
 * including battleships, carriers, submarines, and other warships. Each vessel has
 * historical stats that can be converted to Traveller equivalents via {@link NavalVesselConversion}.
 * </p>
 * <p>
 * Key attributes:
 * </p>
 * <ul>
 *   <li><b>Name</b> — historical vessel name (e.g., "Bismarck", "Yorktown", "U-47")</li>
 *   <li><b>Nationality</b> — the nation that operates the vessel</li>
 *   <li><b>Attack Value</b> — combat factor for attack in CRT resolution</li>
 *   <li><b>Defense Value</b> — combat factor for defense in CRT resolution</li>
 *   <li><b>Year</b> — year the vessel was introduced</li>
 * </ul>
 * <p>
 * Vessels can be converted to Traveller equivalents using {@link NavalVesselConversionService}.
 * </p>
 *
 * @deprecated Use the new {@code com.yisy.modules.doramcu.thirdreich} package for 3R4 ground combat.
 * This class remains for backward compatibility with the Traveller naval conversion layer.
 */
@Deprecated
public class ThirdReichNavalVessel {

    /**
     * Types of Third Reich naval vessels.
     */
    public enum HistoricalVesselType {
        /** Battleship — large capital ship with heavy guns */
        BATTLESHIP("Battleship"),
        /** Aircraft carrier — carries and launches aircraft */
        CARRIER("Aircraft Carrier"),
        /** Heavy cruiser — large cruiser with 8-inch guns */
        HEAVY_CRUISER("Heavy Cruiser"),
        /** Light cruiser — medium cruiser with 6-inch guns */
        LIGHT_CRUISER("Light Cruiser"),
        /** Destroyer — fast escort with torpedoes and AA */
        DESTROYER("Destroyer"),
        /** Submarine — underwater attack vessel */
        SUBMARINE("Submarine"),
        /** Patrol boat — small coastal defense vessel */
        PATROL_BOAT("Patrol Boat"),
        /** Transport — troop and supply transport */
        TRANSPORT("Transport"),
        /** Support ship — repair, supply, or command vessel */
        SUPPORT("Support Ship");

        private final String displayName;

        HistoricalVesselType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    private final String id;
    private String name;
    private String nationality;
    private HistoricalVesselType type;
    private int attackValue;
    private int defenseValue;
    private int year;
    private NavalVesselConversion conversion;

    /**
     * Creates a new Third Reich naval vessel.
     *
     * @param id the unique vessel identifier
     * @param name the vessel's name
     * @param nationality the operating nation
     * @param type the vessel type
     */
    public ThirdReichNavalVessel(String id, String name, String nationality, HistoricalVesselType type) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.nationality = Objects.requireNonNull(nationality, "Nationality must not be null");
        this.type = Objects.requireNonNull(type, "Type must not be null");
        this.attackValue = 0;
        this.defenseValue = 0;
        this.year = 1940;
    }

    // ---- Accessors & Mutators ----

    /** Returns the vessel ID. */
    public String getId() { return id; }

    /** Returns the vessel's name. */
    public String getName() { return name; }

    /** Sets the vessel's name. */
    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Name must not be null");
    }

    /** Returns the operating nation. */
    public String getNationality() { return nationality; }

    /** Sets the operating nation. */
    public void setNationality(String nationality) {
        this.nationality = Objects.requireNonNull(nationality, "Nationality must not be null");
    }

    /** Returns the vessel type. */
    public HistoricalVesselType getType() { return type; }

    /** Sets the vessel type. */
    public void setType(HistoricalVesselType type) {
        this.type = Objects.requireNonNull(type, "Type must not be null");
    }

    /** Returns the attack combat factor. */
    public int getAttackValue() { return attackValue; }

    /** Sets the attack combat factor. */
    public void setAttackValue(int attackValue) {
        this.attackValue = Math.max(0, attackValue);
    }

    /** Returns the defense combat factor. */
    public int getDefenseValue() { return defenseValue; }

    /** Sets the defense combat factor. */
    public void setDefenseValue(int defenseValue) {
        this.defenseValue = Math.max(0, defenseValue);
    }

    /** Returns the year the vessel was introduced. */
    public int getYear() { return year; }

    /** Sets the year the vessel was introduced. */
    public void setYear(int year) {
        this.year = Math.max(1900, year);
    }

    /** Returns the Traveller conversion mapping, or null if not yet converted. */
    public NavalVesselConversion getConversion() { return conversion; }

    /** Sets the Traveller conversion mapping. */
    public void setConversion(NavalVesselConversion conversion) {
        this.conversion = conversion;
    }

    // ---- Utility Methods ----

    /**
     * Returns the total combat strength (attack + defense).
     *
     * @return total combat strength
     */
    public int getCombatStrength() {
        return attackValue + defenseValue;
    }

    /**
     * Returns a summary string of the vessel.
     *
     * @return a formatted summary
     */
    public String getSummary() {
        return String.format("%s (%s, %s) — Atk%d Def%d (%d)",
                name, type.getDisplayName(), nationality,
                attackValue, defenseValue, year);
    }

    /**
     * Checks if this vessel has been converted to a Traveller equivalent.
     *
     * @return true if a conversion exists
     */
    public boolean isConverted() {
        return conversion != null;
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
