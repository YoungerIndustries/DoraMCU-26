package com.yisy.modules.doramcu.babylon5.model;

import java.util.Objects;

/**
 * B5 World Profile — a Universal World Profile (UWP) adapted for the Babylon 5 universe.
 * <p>
 * Similar to Traveller's 8-character UWP, this profile describes a B5 planet's
 * key characteristics: planet size, atmosphere, gravity, starport, population,
 * government, law level, and an optional modifier for special conditions.
 * </p>
 * <p>
 * The format is:
 * </p>
 * <pre>
 *   P C A G M T - Mod
 *   │ │ │ │ │ │   │
 *   │ │ │ │ │ │   └ Special modifier (optional, e.g., "Vorlon", "Shadow")
 *   │ │ │ │ │ └──── Government type (0-A)
 *   │ │ │ │ └────── Law level (0-A)
 *   │ │ │ └──────── Population (0-A)
 *   │ │ └────────── Starport class (A-E)
 *   │ └──────────── Atmosphere (0-F)
 *   └────────────── Planet size (0-A)
 * </pre>
 * <p>
 * Example: <code>A544430-Vorlon</code> describes a large world with breathable atmosphere,
 * average population, democratic government, high law, and Vorlon ruins.
 * </p>
 * <p>
 * Government types are themed around the Babylon 5 universe:
 * Earth Alliance democracy, Narn collective, Centauri empire, Minbari theocracy,
 * Vorlon ancient order, Shadow manipulation, etc.
 * </p>
 */
public class B5WorldProfile {

    private final int planetSize;
    private final int atmosphere;
    private final int gravity;
    private final int starport;
    private final int population;
    private final int government;
    private final int lawLevel;
    private final String modifier;
    private final String profileString;

    /**
     * Parses a B5 world profile string.
     *
     * @param profile the profile string (e.g., "A544430-Vorlon", "34565432")
     * @return a new B5WorldProfile
     * @throws IllegalArgumentException if the profile string is invalid
     */
    public static B5WorldProfile parse(String profile) {
        Objects.requireNonNull(profile, "Profile string must not be null");
        profile = profile.trim();

        if (profile.length() < 8) {
            throw new IllegalArgumentException("Profile must be at least 8 characters: " + profile);
        }

        String base = profile.substring(0, 8);
        String mod = null;
        if (profile.length() > 8 && profile.charAt(8) == '-') {
            mod = profile.substring(9);
        }

        int[] values = new int[7];
        for (int i = 0; i < 7; i++) {
            char c = base.charAt(i);
            values[i] = hexToValue(c);
        }

        return new B5WorldProfile(
                values[0], // Planet Size
                values[1], // Atmosphere
                values[2], // Gravity
                values[3], // Starport
                values[4], // Population
                values[5], // Government
                values[6], // Law Level
                mod,
                profile
        );
    }

    private static int hexToValue(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else if (c == 'A' || c == 'a') {
            return 10;
        } else {
            throw new IllegalArgumentException("Invalid profile character: " + c);
        }
    }

    /**
     * Creates a new B5WorldProfile with the given field values.
     *
     * @param planetSize planet size code (0-10)
     * @param atmosphere atmosphere code (0-10)
     * @param gravity gravity code (0-10)
     * @param starport starport class (0-10)
     * @param population population code (0-10)
     * @param government government type (0-10)
     * @param lawLevel law level (0-10)
     * @param modifier special modifier (optional)
     * @param profileString the original profile string
     */
    public B5WorldProfile(int planetSize, int atmosphere, int gravity,
                           int starport, int population, int government,
                           int lawLevel, String modifier, String profileString) {
        validateRange(planetSize, "Planet size");
        validateRange(atmosphere, "Atmosphere");
        validateRange(gravity, "Gravity");
        validateRange(starport, "Starport");
        validateRange(population, "Population");
        validateRange(government, "Government");
        validateRange(lawLevel, "Law level");

        this.planetSize = planetSize;
        this.atmosphere = atmosphere;
        this.gravity = gravity;
        this.starport = starport;
        this.population = population;
        this.government = government;
        this.lawLevel = lawLevel;
        this.modifier = modifier;
        this.profileString = profileString;
    }

    private void validateRange(int value, String name) {
        if (value < 0 || value > 10) {
            throw new IllegalArgumentException(name + " must be 0-10, got " + value);
        }
    }

    // ---- Accessors ----

    public int getPlanetSize() {
        return planetSize;
    }

    public int getAtmosphere() {
        return atmosphere;
    }

    public int getGravity() {
        return gravity;
    }

    public int getStarport() {
        return starport;
    }

    public int getPopulation() {
        return population;
    }

    public int getGovernment() {
        return government;
    }

    public int getLawLevel() {
        return lawLevel;
    }

    public String getModifier() {
        return modifier;
    }

    public String getProfileString() {
        return profileString;
    }

    // ---- Derived Properties ----

    /**
     * Checks if the world is habitable by humans without environmental suits.
     *
     * @return true if the world is habitable
     */
    public boolean isHabitable() {
        boolean breathableAtmosphere = (atmosphere >= 1 && atmosphere <= 7) || atmosphere == 10;
        boolean tolerableGravity = gravity >= 1 && gravity <= 8;
        return breathableAtmosphere && tolerableGravity;
    }

    /**
     * Checks if the world has a breathable atmosphere.
     *
     * @return true if the atmosphere is breathable
     */
    public boolean hasBreathableAtmosphere() {
        return (atmosphere >= 1 && atmosphere <= 7) || atmosphere == 10;
    }

    /**
     * Checks if the world has no atmosphere (Vacuum).
     *
     * @return true if atmosphere code is 0
     */
    public boolean isVacuum() {
        return atmosphere == 0;
    }

    /**
     * Checks if the world has a functional starport.
     *
     * @return true if starport code is ≤ 4
     */
    public boolean hasStarport() {
        return starport <= 4;
    }

    /**
     * Estimates the world's population in billions.
     *
     * @return estimated population in billions
     */
    public double getPopulationBillions() {
        switch (population) {
            case 0: return 0;
            case 1: return 0.01;
            case 2: return 0.1;
            case 3: return 0.5;
            case 4: return 1;
            case 5: return 5;
            case 6: return 10;
            case 7: return 25;
            case 8: return 50;
            case 9: return 100;
            case 10: return 200;
            default: return 0;
        }
    }

    /**
     * Returns the government type as a B5-themed string.
     *
     * @return the government type description
     */
    public String getGovernmentType() {
        switch (government) {
            case 0: return "None";
            case 1: return "Earth Alliance Democracy";
            case 2: return "Narn Collective";
            case 3: return "Centauri Empire";
            case 4: return "Minbari Theocracy";
            case 5: return "Drazi Warrior Council";
            case 6: return "Vree Merchant League";
            case 7: return "Brakiri Spiritual Council";
            case 8: return "Vorlon Ancient Order";
            case 9: return "Shadow Manipulation";
            case 10: return "Independent";
            default: return "Unknown";
        }
    }

    /**
     * Returns the law level as a human-readable string.
     *
     * @return the law level description
     */
    public String getLawLevelDescription() {
        switch (lawLevel) {
            case 0: return "None (Anarchy)";
            case 1: return "Very Low";
            case 2: return "Low";
            case 3: return "Moderate";
            case 4: return "Average";
            case 5: return "High";
            case 6: return "Very High";
            case 7: return "Enforced";
            case 8: return "Very Strict";
            case 9: return "Strict";
            case 10: return "Totalitarian";
            default: return "Unknown";
        }
    }

    /**
     * Returns the planet size as a human-readable string.
     *
     * @return the planet size description
     */
    public String getPlanetSizeDescription() {
        switch (planetSize) {
            case 0: return "Small Sub-Terran";
            case 1: return "Sub-Terran";
            case 2: return "High Sub-Terran";
            case 3: return "Terran";
            case 4: return "High Terran";
            case 5: return "Super-Terran";
            case 6: return "High Super-Terran";
            case 7: return "Gas Giant";
            case 8: return "High Gas Giant";
            case 9: return "Very High Gas Giant";
            case 10: return "Gigantic Gas Giant";
            default: return "Unknown";
        }
    }

    /**
     * Returns the atmosphere as a human-readable string.
     *
     * @return the atmosphere description
     */
    public String getAtmosphereDescription() {
        switch (atmosphere) {
            case 0: return "None";
            case 1: return "Trace";
            case 2: return "Thin";
            case 3: return "Very Thin";
            case 4: return "Standard";
            case 5: return "Dense";
            case 6: return "Very Dense";
            case 7: return "Super-Dense";
            case 8: return "None (Too Hot)";
            case 9: return "None (Toxic)";
            case 10: return "None (Unbreathable)";
            default: return "Unknown";
        }
    }

    // ---- Trade Code Derivation ----

    /**
     * Derives the primary trade code for this world based on its profile.
     * B5-themed trade codes reflect the Babylon 5 universe.
     *
     * @return the primary trade code
     */
    public B5TradeCode derivePrimaryTradeCode() {
        if (isVacuum()) {
            return B5TradeCode.VACUUM;
        }

        if (population == 0) {
            return B5TradeCode.NONE;
        }

        // Ecumenopolis: Population 9, Government 1-7
        if (population == 9 && government >= 1 && government <= 7) {
            return B5TradeCode.ECUMENOPOLIS;
        }

        // High Population
        if (population >= 7) {
            return B5TradeCode.HIGH_POPULATION;
        }

        // Colonial: Population ≤ 3
        if (population <= 3) {
            return B5TradeCode.COLONIAL;
        }

        // Agricultural: Population ≥ 4, gravity 3-6
        if (population >= 4 && gravity >= 3 && gravity <= 6) {
            return B5TradeCode.AGRICULTURAL;
        }

        // Poor: No government or no law
        if (government == 0 || lawLevel == 0) {
            return B5TradeCode.POOR;
        }

        // Rich: High government and law
        if (government >= 4 && lawLevel >= 5) {
            return B5TradeCode.RICH;
        }

        return B5TradeCode.LOW_POPULATION;
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        return profileString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        B5WorldProfile that = (B5WorldProfile) o;
        return planetSize == that.planetSize &&
                atmosphere == that.atmosphere &&
                gravity == that.gravity &&
                starport == that.starport &&
                population == that.population &&
                government == that.government &&
                lawLevel == that.lawLevel &&
                Objects.equals(modifier, that.modifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planetSize, atmosphere, gravity, starport,
                population, government, lawLevel, modifier);
    }

    /**
     * B5-themed trade codes derived from world profiles.
     */
    public enum B5TradeCode {
        /**
         * Vacuum world — no atmosphere.
         */
        VACUUM("Va", "Vacuum world"),

        /**
         * No significant trade.
         */
        NONE("Ni", "No trade"),

        /**
         * Ecumenopolis — planet-wide city.
         */
        ECUMENOPOLIS("Ec", "Ecumenopolis"),

        /**
         * High population world.
         */
        HIGH_POPULATION("Hi", "High population"),

        /**
         * Colonial world — low population.
         */
        COLONIAL("Co", "Colonial"),

        /**
         * Agricultural world.
         */
        AGRICULTURAL("Ag", "Agricultural"),

        /**
         * Poor world — low wealth.
         */
        POOR("Po", "Poor"),

        /**
         * Rich world — high wealth.
         */
        RICH("Ri", "Rich"),

        /**
         * Low population world.
         */
        LOW_POPULATION("Lo", "Low population"),

        /**
         * Water world — mostly ocean.
         */
        WATER("Wa", "Water world"),

        /**
         * Barren world — no life.
         */
        BARREN("Ba", "Barren"),

        /**
         * Jungle world — dense vegetation.
         */
        JUNGLE("Ju", "Jungle world"),

        /**
         * Ice world — frozen surface.
         */
        ICE("Ic", "Ice world"),

        /**
         * Desert world — arid surface.
         */
        DESERT("De", "Desert world"),

        /**
         * Vorlon ruins present.
         */
        VORLON("Vo", "Vorlon ruins"),

        /**
         * Shadow facilities present.
         */
        SHADOW("Sh", "Shadow facilities"),

        /**
         * First One artifacts present.
         */
        FIRST_ONE("Fo", "First One artifacts");

        private final String code;
        private final String description;

        B5TradeCode(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return code;
        }
    }
}
