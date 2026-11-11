package com.yisy.modules.doramcu.sfb.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * UWP-like world profiles for SFB planets.
 * <p>
 * Each world has an 8-character profile describing planet size, atmosphere,
 * hydrographics, temperature, government, population, and trade codes.
 * </p>
 */
public class SfbWorldProfile {

    private final int planetSize;
    private final int atmosphere;
    private final int hydrographics;
    private final int temperature;
    private final SfbGovernmentType government;
    private final int population;
    private final int production;
    private final int influence;
    private final List<SfbTradeCode> tradeCodes = new ArrayList<>();

    private SfbWorldProfile(int planetSize, int atmosphere, int hydrographics, int temperature,
                            SfbGovernmentType government, int population, int production,
                            int influence, List<SfbTradeCode> tradeCodes) {
        this.planetSize = planetSize;
        this.atmosphere = atmosphere;
        this.hydrographics = hydrographics;
        this.temperature = temperature;
        this.government = government;
        this.population = population;
        this.production = production;
        this.influence = influence;
        this.tradeCodes.addAll(tradeCodes);
    }

    public int getPlanetSize() { return planetSize; }
    public int getAtmosphere() { return atmosphere; }
    public int getHydrographics() { return hydrographics; }
    public int getTemperature() { return temperature; }
    public SfbGovernmentType getGovernment() { return government; }
    public int getPopulation() { return population; }
    public int getProduction() { return production; }
    public int getInfluence() { return influence; }
    public List<SfbTradeCode> getTradeCodes() { return tradeCodes; }

    /**
     * Parses a world profile string.
     */
    public static SfbWorldProfile parse(String profile) {
        if (profile == null || profile.length() < 8) {
            throw new IllegalArgumentException("Invalid world profile: " + profile);
        }

        int planetSize = Character.getNumericValue(profile.charAt(0));
        int atmosphere = Character.getNumericValue(profile.charAt(1));
        int hydrographics = Character.getNumericValue(profile.charAt(2));
        int temperature = Character.getNumericValue(profile.charAt(3));

        SfbGovernmentType government = SfbGovernmentType.fromCode(profile.charAt(4));
        int population = Character.getNumericValue(profile.charAt(5));
        int production = Character.getNumericValue(profile.charAt(6));
        int influence = Character.getNumericValue(profile.charAt(7));

        List<SfbTradeCode> tradeCodes = deriveTradeCodes(planetSize, atmosphere, hydrographics, temperature, government, population);

        return new SfbWorldProfile(planetSize, atmosphere, hydrographics, temperature,
                government, population, production, influence, tradeCodes);
    }

    private static List<SfbTradeCode> deriveTradeCodes(int size, int atmosphere, int hydro, int temp,
                                                        SfbGovernmentType gov, int pop) {
        List<SfbTradeCode> codes = new ArrayList<>();
        Random rand = new Random();

        if (atmosphere >= 3 && atmosphere <= 7) codes.add(SfbTradeCode.NORMAL);
        if (hydro >= 4 && hydro <= 7) codes.add(SfbTradeCode.WATER_WORLD);
        if (temp >= 4 && temp <= 7) codes.add(SfbTradeCode.TEMPERATE);
        if (pop >= 7) codes.add(SfbTradeCode.POPULOUS);
        if (pop == 0) codes.add(SfbTradeCode.UNINHABITED);
        if (size >= 8) codes.add(SfbTradeCode.GIANT);
        if (gov == SfbGovernmentType.FEDERATION) codes.add(SfbTradeCode.FEDERATION);
        if (gov == SfbGovernmentType.KLINGON) codes.add(SfbTradeCode.KLINGON);
        if (gov == SfbGovernmentType.ROMULAN) codes.add(SfbTradeCode.ROMULAN);

        if (rand.nextInt(100) < 20) codes.add(SfbTradeCode.ANCIENT_RUINS);
        if (rand.nextInt(100) < 15) codes.add(SfbTradeCode.ANOMALOUS);

        return codes;
    }

    /**
     * Government types for SFB worlds.
     */
    public enum SfbGovernmentType {
        NONE('0', "None"),
        BARBARIC('1', "Barbaric"),
        TRIBAL('2', "Tribal"),
        FEUDAL('3', "Feudal"),
        PATRIARCHY('4', "Patriarchy"),
        DEMOCRACY('5', "Democracy"),
        AUTHORITY('6', "Authority"),
        TOTALITARIAN('7', "Totalitarian"),
        THEOCRACY('8', "Theocracy"),
        COMMUNISM('9', "Communism"),
        CORPORATE('A', "Corporate"),
        FEDERATION('F', "Federation"),
        KLINGON('K', "Klingon Empire"),
        ROMULAN('R', "Romulan Star Empire"),
        ANDORIAN('A', "Andorian Empire"),
        BAJORAN('B', "Bajoran Theocracy");

        private final char code;
        private final String name;

        SfbGovernmentType(char code, String name) {
            this.code = code;
            this.name = name;
        }

        public char getCode() { return code; }
        public String getName() { return name; }

        public static SfbGovernmentType fromCode(char code) {
            for (SfbGovernmentType type : values()) {
                if (type.code == code) return type;
            }
            return DEMOCRACY;
        }
    }

    /**
     * Trade codes for SFB worlds.
     */
    public enum SfbTradeCode {
        NORMAL("Normal"),
        WATER_WORLD("Water World"),
        TEMPERATE("Temperate"),
        POPULOUS("Populous"),
        UNINHABITED("Uninhabited"),
        GIANT("Gas Giant"),
        FEDERATION("Federation Space"),
        KLINGON("Klingon Space"),
        ROMULAN("Romulan Space"),
        ANCIENT_RUINS("Ancient Ruins"),
        ANOMALOUS("Anomalous Readings"),
        DILITHIUM("Dilithium Deposits"),
        WARP_TRADE("Warp Trade Route"),
        MILITARY("Military Presence"),
        SCIENCE("Science Outpost"),
        HAZARD("Hazardous"),
        FIRST_CONTACT("First Contact Zone");

        private final String name;

        SfbTradeCode(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
