package com.yisy.modules.doramcu.star_trek_4x.exploration;

import com.yisy.modules.doramcu.captainslog.model.StarTrekSpecies;
import com.yisy.modules.doramcu.star_trek_4x.model.StarSystem;
import com.yisy.modules.doramcu.star_trek_4x.service.StarTrekLoreDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Service for generating first contact scenarios.
 * <p>
 * Uses Captain's Log NPC tables and Star Trek species data to create
 * realistic first contact situations when exploring new systems.
 * </p>
 */
@Service
public class FirstContactService {

    private static final Logger log = LoggerFactory.getLogger(FirstContactService.class);
    private static final Random random = new Random();

    private final StarTrekLoreDatabase starTrekLoreDatabase;

    public FirstContactService(StarTrekLoreDatabase starTrekLoreDatabase) {
        this.starTrekLoreDatabase = starTrekLoreDatabase;
    }

    /**
     * Generate a first contact scenario for a discovered system.
     *
     * @param system the discovered system
     * @return first contact scenario details
     */
    public Map<String, Object> generateFirstContactScenario(StarSystem system) {
        log.info("Generating first contact scenario for system: {}", system.getName());

        // Determine if first contact occurs
        int contactChance = calculateContactChance(system);
        if (random.nextInt(100) >= contactChance) {
            return Map.of(
                    "occurred", false,
                    "systemName", system.getName(),
                    "reason", "No intelligent life detected"
            );
        }

        // Select species for contact
        StarTrekSpecies species = selectSpecies(system);

        // Determine contact type
        String contactType = determineContactType(species);

        // Generate scenario details
        String scenario = generateScenarioDescription(species, contactType, system);

        return Map.of(
                "occurred", true,
                "systemName", system.getName(),
                "species", species.getDisplayName(),
                "speciesDescription", species.getDescription(),
                "contactType", contactType,
                "scenario", scenario,
                "diplomacyModifier", species.getSocialDM(),
                "hostilityLevel", calculateHostilityLevel(species)
        );
    }

    /**
     * Calculate the chance of first contact based on system properties.
     */
    private int calculateContactChance(StarSystem system) {
        int baseChance = 30;

        // Higher population increases chance
        if (system.getUwpPopulation() != null) {
            baseChance += system.getUwpPopulation().charAt(0) - '0';
        }

        // Native species increase chance
        if (system.getNativeSpecies() != null && !system.getNativeSpecies().isEmpty()) {
            baseChance += system.getNativeSpecies().size() * 10;
        }

        // Warp deposits attract attention
        if (system.isHasWarpDeposits()) {
            baseChance += 15;
        }

        return Math.min(90, baseChance);
    }

    /**
     * Select a species from the enum based on system alignment.
     */
    private StarTrekSpecies selectSpecies(StarSystem system) {
        StarTrekSpecies[] allSpecies = StarTrekSpecies.values();
        
        // Try to match by alignment
        for (StarTrekSpecies s : allSpecies) {
            if (system.getAlignment() != null &&
                s.name().equalsIgnoreCase(system.getAlignment().name())) {
                return s;
            }
        }
        
        return allSpecies[random.nextInt(allSpecies.length)];
    }

    /**
     * Determine the type of first contact.
     */
    private String determineContactType(StarTrekSpecies species) {
        int roll = random.nextInt(100);

        if (species.getSocialDM() > 5) {
            return roll < 70 ? "FRIENDLY" : roll < 90 ? "NEUTRAL" : "HOSTILE";
        } else if (species.getSocialDM() < -5) {
            return roll < 30 ? "HOSTILE" : roll < 60 ? "NEUTRAL" : "FRIENDLY";
        } else {
            return roll < 40 ? "FRIENDLY" : roll < 70 ? "NEUTRAL" : "HOSTILE";
        }
    }

    /**
     * Generate a scenario description.
     */
    private String generateScenarioDescription(StarTrekSpecies species, String contactType, StarSystem system) {
        String[] friendlyScenarios = {
            "The " + species.getDisplayName() + " greet your vessel with open arms, offering trade and cultural exchange.",
            "A peaceful delegation from the " + species.getDisplayName() + " approaches, seeking diplomatic relations.",
            "The " + species.getDisplayName() + " have been monitoring your approach and welcome you to their system."
        };

        String[] neutralScenarios = {
            "The " + species.getDisplayName() + " observe your vessel with curiosity but maintain distance.",
            "A cautious " + species.getDisplayName() + " envoy requests a formal meeting to discuss your presence.",
            "The " + species.getDisplayName() + " are aware of your arrival but remain watchful."
        };

        String[] hostileScenarios = {
            "The " + species.getDisplayName() + " intercept your vessel with weapons charged, demanding your purpose.",
            "Hostile " + species.getDisplayName() + " warships emerge from the system, blocking your path.",
            "The " + species.getDisplayName() + " consider your presence an invasion and prepare for conflict."
        };

        List<String> scenarios;
        switch (contactType) {
            case "FRIENDLY" -> scenarios = List.of(friendlyScenarios);
            case "HOSTILE" -> scenarios = List.of(hostileScenarios);
            default -> scenarios = List.of(neutralScenarios);
        }

        return scenarios.get(random.nextInt(scenarios.size()));
    }

    /**
     * Calculate hostility level based on species combat DM.
     */
    private String calculateHostilityLevel(StarTrekSpecies species) {
        int combatDm = species.getCombatDM();
        if (combatDm > 5) return "HIGH";
        if (combatDm > 0) return "MEDIUM";
        if (combatDm > -5) return "LOW";
        return "MINIMAL";
    }

    /**
     * Get all available species for first contact.
     *
     * @return list of species
     */
    public List<StarTrekSpecies> getAvailableSpecies() {
        return List.of(StarTrekSpecies.values());
    }
}
