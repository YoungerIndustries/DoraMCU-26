package com.yisy.modules.doramcu.lord_of_the_rings.integration;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates galaxy content for the Lord of the Rings module.
 * Creates star systems, planets, and celestial bodies based on Tolkien's cosmology.
 */
@Component
public class LotrGalaxyGenerator {

    private static final String[] CONSTELLATIONS = {
            "Valacirca", "Menelmacar", "Remmirath", "Thelassiel",
            "Mornifel", "Aldarion's Stars", "Silmaril Star"
    };

    private static final String[] VALAR_PLANETS = {
            "Manwë", "Varda", "Ulmo", "Aulë", "Yavanna",
            "Mandos", "Tilion", "Nienna", "Irmo", "Melian"
    };

    private static final String[] REALMS = {
            "Shire", "Rivendell", "Lothlórien", "Gondor", "Rohan",
            "Mordor", "Isengard", "Moria", "Númenor", "Harad",
            "Rhûn", "Forodwaith", "Mirkwood", "Fangorn"
    };

    private final Random random = new Random();

    /**
     * Generates a list of star system names based on LOTR constellations.
     */
    public List<String> generateStarSystems(int count) {
        List<String> systems = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            systems.add(CONSTELLATIONS[i % CONSTELLATIONS.length] + " Sector " + (i + 1));
        }
        return systems;
    }

    /**
     * Generates a list of planet names based on Valar.
     */
    public List<String> generatePlanets(int count) {
        List<String> planets = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            planets.add(VALAR_PLANETS[i % VALAR_PLANETS.length]);
        }
        return planets;
    }

    /**
     * Generates a list of realm names for the galaxy.
     */
    public List<String> generateRealms(int count) {
        List<String> realms = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            realms.add(REALMS[i % REALMS.length]);
        }
        return realms;
    }

    /**
     * Generates a random celestial body description.
     */
    public String generateCelestialDescription(String name) {
        String[] types = {"star system", "nebula", "binary star", "gas giant", "ocean world", "mining world", "forest world"};
        String type = types[random.nextInt(types.length)];
        return name + " is a " + type + " in the Arda galaxy, blessed by the Valar.";
    }
}
