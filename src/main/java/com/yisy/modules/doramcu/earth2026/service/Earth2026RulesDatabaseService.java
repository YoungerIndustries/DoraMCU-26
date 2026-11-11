package com.yisy.modules.doramcu.earth2026.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.earth2026.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Loads and provides access to the Earth 2026 rules database from JSON.
 * <p>
 * This service loads species definitions, name databases, galaxy/system/planet rules
 * from the Earth 2026 rules JSON database. It provides methods for:
 * </p>
 * <ul>
 *   <li>Looking up species by ID or game module</li>
 *   <li>Generating AI character names from species name databases</li>
 *   <li>Accessing galaxy, system, and planet creation rules</li>
 * </ul>
 * <p>
 * Games can pass their species ID to Earth 2026 when requesting a planet or moon,
 * so AI character names match the requesting game's lore.
 * </p>
 */
@Service
public class Earth2026RulesDatabaseService {

    private static final Logger log = LoggerFactory.getLogger(Earth2026RulesDatabaseService.class);
    private static final Random random = new Random();

    private final ObjectMapper objectMapper;
    private final String databasePath;

    private List<E26Species> species;
    private E26GalaxyRules galaxyRules;
    private E26SystemRules systemRules;
    private E26PlanetRules planetRules;

    public Earth2026RulesDatabaseService() {
        this.objectMapper = new ObjectMapper();
        this.databasePath = "docs/earth2026-rules-db/earth2026-rules.json";
    }

    /**
     * Constructor with custom path for testing.
     */
    public Earth2026RulesDatabaseService(String databasePath) {
        this.objectMapper = new ObjectMapper();
        this.databasePath = databasePath;
    }

    @PostConstruct
    public void loadDatabase() {
        try {
            File file = new File(databasePath);
            if (!file.exists()) {
                log.warn("Earth 2026 rules database file not found: {}", databasePath);
                species = new ArrayList<>();
                galaxyRules = new E26GalaxyRules();
                systemRules = new E26SystemRules();
                planetRules = new E26PlanetRules();
                return;
            }

            JsonNode root = objectMapper.readTree(file);
            loadSpecies(root);
            loadGalaxyRules(root);
            loadSystemRules(root);
            loadPlanetRules(root);

            log.info("Loaded Earth 2026 rules database: {} species, galaxy/system/planet rules", species.size());
        } catch (IOException e) {
            log.error("Failed to load Earth 2026 rules database from {}", databasePath, e);
            species = new ArrayList<>();
            galaxyRules = new E26GalaxyRules();
            systemRules = new E26SystemRules();
            planetRules = new E26PlanetRules();
        }
    }

    private void loadSpecies(JsonNode root) {
        species = new ArrayList<>();
        JsonNode node = root.get("species");
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                try {
                    E26Species sp = objectMapper.treeToValue(item, E26Species.class);
                    species.add(sp);
                    log.debug("Parsed species: {}", sp.getId());
                } catch (Exception e) {
                    log.error("Failed to parse species: {}", item, e);
                }
            }
        }
        log.info("Loaded {} species", species.size());
    }

    private void loadGalaxyRules(JsonNode root) {
        JsonNode node = root.get("galaxyRules");
        if (node != null) {
            try {
                galaxyRules = objectMapper.treeToValue(node, E26GalaxyRules.class);
            } catch (Exception e) {
                log.error("Failed to parse galaxy rules", e);
                galaxyRules = new E26GalaxyRules();
            }
        }
    }

    private void loadSystemRules(JsonNode root) {
        JsonNode node = root.get("systemRules");
        if (node != null) {
            try {
                systemRules = objectMapper.treeToValue(node, E26SystemRules.class);
            } catch (Exception e) {
                log.error("Failed to parse system rules", e);
                systemRules = new E26SystemRules();
            }
        }
    }

    private void loadPlanetRules(JsonNode root) {
        JsonNode node = root.get("planetRules");
        if (node != null) {
            try {
                planetRules = objectMapper.treeToValue(node, E26PlanetRules.class);
            } catch (Exception e) {
                log.error("Failed to parse planet rules", e);
                planetRules = new E26PlanetRules();
            }
        }
    }

    // ==========================================
    // Species methods
    // ==========================================

    /**
     * Get all species from the rules database.
     */
    public List<E26Species> getAllSpecies() {
        return species;
    }

    /**
     * Get a species by ID (case-insensitive).
     */
    public Optional<E26Species> getSpecies(String id) {
        return species.stream()
                .filter(sp -> sp.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    /**
     * Get species by game module (e.g., "TRAVELLER", "CAPTAINS_LOG", "BABYLON5").
     */
    public List<E26Species> getSpeciesByGameModule(String gameModule) {
        return species.stream()
                .filter(sp -> gameModule.equalsIgnoreCase(sp.getGameModule()))
                .toList();
    }

    /**
     * Get species by homeworld (case-insensitive).
     */
    public List<E26Species> getSpeciesByHomeworld(String homeworld) {
        return species.stream()
                .filter(sp -> homeworld.equalsIgnoreCase(sp.getHomeworld()))
                .toList();
    }

    // ==========================================
    // AI Character Name Generation
    // ==========================================

    /**
     * Generate an AI character name for a given species.
     * <p>
     * Uses the species' name database to pick a random first name, last name,
     * and title appropriate for the role.
     * </p>
     *
     * @param speciesId the species ID
     * @param role      the role (e.g., "Leader", "Governor", "Captain")
     * @return a generated name with title
     */
    public String generateAiCharacterName(String speciesId, String role) {
        E26Species sp = this.species.stream()
                .filter(s -> s.getId().equalsIgnoreCase(speciesId))
                .findFirst()
                .orElse(null);

        if (sp == null || sp.getNameDatabase() == null) {
            // Fallback to Human
            sp = this.species.stream()
                    .filter(s -> "HUMAN".equalsIgnoreCase(s.getId()))
                    .findFirst()
                    .orElse(null);
        }

        if (sp == null || sp.getNameDatabase() == null) {
            return "Unknown AI (" + role + ")";
        }

        E26NameDatabase db = sp.getNameDatabase();

        // Pick first name (random gender)
        String firstName;
        List<String> maleNames = db.getMaleFirstNames();
        List<String> femaleNames = db.getFemaleFirstNames();

        if (maleNames != null && !maleNames.isEmpty() && random.nextBoolean()) {
            firstName = maleNames.get(random.nextInt(maleNames.size()));
        } else if (femaleNames != null && !femaleNames.isEmpty()) {
            firstName = femaleNames.get(random.nextInt(femaleNames.size()));
        } else if (maleNames != null && !maleNames.isEmpty()) {
            firstName = maleNames.get(random.nextInt(maleNames.size()));
        } else {
            firstName = "Unknown";
        }

        // Pick last name
        String lastName = "";
        if (db.getLastNames() != null && !db.getLastNames().isEmpty()) {
            lastName = " " + db.getLastNames().get(random.nextInt(db.getLastNames().size()));
        }

        // Pick title based on role
        String title = "";
        List<String> titles;
        if ("Leader".equalsIgnoreCase(role) || "President".equalsIgnoreCase(role) || "Emperor".equalsIgnoreCase(role)) {
            titles = db.getAiLeaderTitles();
        } else if ("Governor".equalsIgnoreCase(role) || "Mayor".equalsIgnoreCase(role)) {
            titles = db.getAiGovernorTitles();
        } else if ("Captain".equalsIgnoreCase(role) || "Commander".equalsIgnoreCase(role)) {
            titles = db.getAiCaptainTitles();
        } else {
            titles = db.getAiLeaderTitles();
        }

        if (titles != null && !titles.isEmpty()) {
            title = titles.get(random.nextInt(titles.size()));
        }

        return title + " " + firstName + lastName;
    }

    /**
     * Generate an AI character name for a given species with a specific role title.
     *
     * @param speciesId the species ID
     * @param roleTitle the specific role title to use
     * @return a generated name with the specified title
     */
    public String generateAiCharacterNameWithTitle(String speciesId, String roleTitle) {
        E26Species sp = this.species.stream()
                .filter(s -> s.getId().equalsIgnoreCase(speciesId))
                .findFirst()
                .orElse(null);

        if (sp == null || sp.getNameDatabase() == null) {
            sp = this.species.stream()
                    .filter(s -> "HUMAN".equalsIgnoreCase(s.getId()))
                    .findFirst()
                    .orElse(null);
        }

        if (sp == null || sp.getNameDatabase() == null) {
            return roleTitle + " Unknown";
        }

        E26NameDatabase db = sp.getNameDatabase();

        // Pick first name
        String firstName;
        List<String> maleNames = db.getMaleFirstNames();
        List<String> femaleNames = db.getFemaleFirstNames();

        if (maleNames != null && !maleNames.isEmpty() && random.nextBoolean()) {
            firstName = maleNames.get(random.nextInt(maleNames.size()));
        } else if (femaleNames != null && !femaleNames.isEmpty()) {
            firstName = femaleNames.get(random.nextInt(femaleNames.size()));
        } else if (maleNames != null && !maleNames.isEmpty()) {
            firstName = maleNames.get(random.nextInt(maleNames.size()));
        } else {
            firstName = "Unknown";
        }

        // Pick last name
        String lastName = "";
        if (db.getLastNames() != null && !db.getLastNames().isEmpty()) {
            lastName = " " + db.getLastNames().get(random.nextInt(db.getLastNames().size()));
        }

        return roleTitle + " " + firstName + lastName;
    }

    // ==========================================
    // Galaxy Rules methods
    // ==========================================

    /**
     * Get the galaxy creation rules.
     */
    public E26GalaxyRules getGalaxyRules() {
        return galaxyRules;
    }

    /**
     * Get the default galaxy seed value.
     */
    public long getDefaultGalaxySeed() {
        return galaxyRules != null ? galaxyRules.getDefaultGalaxySeed() : 42L;
    }

    /**
     * Get the Void galaxy seed value.
     */
    public long getVoidGalaxySeed() {
        return galaxyRules != null ? galaxyRules.getVoidGalaxySeed() : 54321L;
    }

    /**
     * Get the Earth galaxy seed value.
     */
    public long getEarthGalaxySeed() {
        return galaxyRules != null ? galaxyRules.getEarthGalaxySeed() : 12345L;
    }

    // ==========================================
    // System Rules methods
    // ==========================================

    /**
     * Get the star system creation rules.
     */
    public E26SystemRules getSystemRules() {
        return systemRules;
    }

    /**
     * Get all star types.
     */
    public List<E26SystemRules.E26StarType> getStarTypes() {
        return systemRules != null ? systemRules.getStarTypes() : List.of();
    }

    /**
     * Get all planet types.
     */
    public List<E26SystemRules.E26PlanetType> getPlanetTypes() {
        return systemRules != null ? systemRules.getPlanetTypes() : List.of();
    }

    // ==========================================
    // Planet Rules methods
    // ==========================================

    /**
     * Get the planet creation rules.
     */
    public E26PlanetRules getPlanetRules() {
        return planetRules;
    }

    /**
     * Get all government types.
     */
    public List<E26PlanetRules.E26GovernmentType> getGovernmentTypes() {
        return planetRules != null ? planetRules.getGovernmentTypes() : List.of();
    }

    /**
     * Get all world modifiers.
     */
    public List<E26PlanetRules.E26WorldModifier> getWorldModifiers() {
        return planetRules != null ? planetRules.getWorldModifiers() : List.of();
    }
}
