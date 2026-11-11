package com.yisy.modules.doramcu.star_trek_4x.rpg;

import com.yisy.modules.doramcu.star_trek_4x.service.StarTrekLoreDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Service for creating new characters using SFB rules.
 * <p>
 * Creates characters with empire, species, background, and attributes based on SFB rules.
 * </p>
 */
@Service
public class SfbCharacterCreator {

    private static final Logger log = LoggerFactory.getLogger(SfbCharacterCreator.class);
    private static final Random random = new Random();

    private final StarTrekLoreDatabase starTrekLoreDatabase;

    public SfbCharacterCreator(StarTrekLoreDatabase starTrekLoreDatabase) {
        this.starTrekLoreDatabase = starTrekLoreDatabase;
    }

    /**
     * Create a new character using SFB rules.
     *
     * @param name     the character name
     * @param empire   the empire
     * @param species  the species
     * @param division the division
     * @return created character data
     */
    public Map<String, Object> createCharacter(String name, String empire, String species, String division) {
        log.info("Creating SFB character: {}, empire={}, species={}, division={}", name, empire, species, division);

        // Generate attributes based on species and division
        int combat = generateAttribute(division, "COMBAT");
        int diplomacy = generateAttribute(division, "DIPLOMACY");
        int science = generateAttribute(division, "SCIENCE");
        int engineering = generateAttribute(division, "ENGINEERING");
        int medicine = generateAttribute(division, "MEDICINE");

        // Generate background
        String background = generateBackground();

        Map<String, Object> character = Map.of(
                "name", name,
                "empire", empire,
                "species", species,
                "division", division,
                "background", background,
                "attributes", Map.of(
                        "combat", combat,
                        "diplomacy", diplomacy,
                        "science", science,
                        "engineering", engineering,
                        "medicine", medicine
                ),
                "rank", "ENSIGN",
                "stress", 0,
                "created", true
        );

        log.info("Character '{}' created successfully", name);
        return character;
    }

    /**
     * Generate an attribute value based on division.
     */
    private int generateAttribute(String division, String attribute) {
        int base = random.nextInt(6) + 1; // 1-6

        // Division bonus
        if (division != null && division.equalsIgnoreCase(attribute)) {
            base += 2;
        }

        return Math.min(10, base);
    }

    /**
     * Generate a random background.
     */
    private String generateBackground() {
        String[] backgrounds = {
                "Academy Graduate", "Street Smart", "Military Family",
                "Scientific Prodigy", "Diplomatic Heritage", "Engineering Genius",
                "Medical Training", "Combat Veteran", "Explorer", "Trader"
        };
        return backgrounds[random.nextInt(backgrounds.length)];
    }

    /**
     * Get available empires.
     *
     * @return list of empires
     */
    public List<String> getAvailableEmpires() {
        return List.of("Federation", "Klingon Empire", "Romulan Star Empire", "Cardassian Union", "Ferengi Alliance", "Borg Collective");
    }

    /**
     * Get available species.
     *
     * @return list of species names
     */
    public List<String> getAvailableSpecies() {
        return List.of("Human", "Vulcan", "Klingon", "Romulan", "Ferengi", "Betazoid", "Andorian", "Bajoran");
    }

    /**
     * Get available divisions.
     *
     * @return list of divisions
     */
    public List<String> getAvailableDivisions() {
        return List.of("Command", "Science", "Engineering", "Medical", "Security");
    }
}
