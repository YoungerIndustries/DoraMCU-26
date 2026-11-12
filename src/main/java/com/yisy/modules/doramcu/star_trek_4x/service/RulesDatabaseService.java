package com.yisy.modules.doramcu.star_trek_4x.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.captainslog.model.RulesEntry;
import com.yisy.modules.doramcu.captainslog.model.RulesTable;
import com.yisy.modules.doramcu.captainslog.repository.RulesEntryRepository;
import com.yisy.modules.doramcu.captainslog.repository.RulesTableRepository;
import com.yisy.modules.doramcu.earth2026.model.E26GalaxyRules;
import com.yisy.modules.doramcu.earth2026.model.E26PlanetRules;
import com.yisy.modules.doramcu.earth2026.model.E26SystemRules;
import com.yisy.modules.doramcu.earth2026.service.PlanetService;
import com.yisy.modules.doramcu.sfb.model.SfbEmpire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Unified rules database service that merges four data sources:
 * <ol>
 *   <li>SFB JSON rules from docs/sfb-rules-db/*.json</li>
 *   <li>Captain's Log JPA rules from cl_rules_entries and cl_rules_tables</li>
 *   <li>Earth 2026 rules via PlanetService (galaxy, system, planet rules)</li>
 *   <li>Star Trek lore JSON from docs/star-trek-4x-rules-db/*.json</li>
 * </ol>
 * <p>
 * Acts as the single query interface for all game rules, avoiding conflicts
 * between sources by prioritizing: Star Trek lore > SFB > Captain's Log > Earth 2026.
 * </p>
 */
@Service("starTrek4xRulesDatabaseService")
public class RulesDatabaseService {

    private static final Logger log = LoggerFactory.getLogger(RulesDatabaseService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RulesEntryRepository rulesEntryRepository;
    private final RulesTableRepository rulesTableRepository;
    private final PlanetService planetService;

    // Cached data from each source
    private final Map<String, JsonNode> sfbRules = new HashMap<>();
    private final Map<String, JsonNode> starTrekLore = new HashMap<>();
    private final List<RulesEntry> captainLogRules = new ArrayList<>();
    private final List<RulesTable> captainLogTables = new ArrayList<>();
    private E26GalaxyRules galaxyRules;
    private E26SystemRules systemRules;
    private E26PlanetRules planetRules;

    public RulesDatabaseService(RulesEntryRepository rulesEntryRepository,
                                 RulesTableRepository rulesTableRepository,
                                 PlanetService planetService) {
        this.rulesEntryRepository = rulesEntryRepository;
        this.rulesTableRepository = rulesTableRepository;
        this.planetService = planetService;
        loadAllRules();
    }

    /**
     * Load all rules from all four sources.
     */
    private void loadAllRules() {
        loadSfbRules();
        loadStarTrekLore();
        loadCaptainLogRules();
        loadEarth2026Rules();
        log.info("Rules database loaded: {} SFB rules, {} Star Trek lore entries, {} Captain's Log rules, {} Captain's Log tables",
                sfbRules.size(), starTrekLore.size(), captainLogRules.size(), captainLogTables.size());
    }

    /**
     * Load SFB rules from docs/sfb-rules-db/*.json.
     */
    private void loadSfbRules() {
        String[] ruleFiles = {"empires.json", "ship-classes.json", "weapons.json", "movement.json",
                "shields.json", "energy-allocation.json"};
        for (String file : ruleFiles) {
            try {
                Path path = Paths.get("docs/sfb-rules-db", file);
                if (Files.exists(path)) {
                    JsonNode node = objectMapper.readTree(path.toFile());
                    sfbRules.put(file, node);
                    log.debug("Loaded SFB rules: {}", file);
                }
            } catch (IOException e) {
                log.warn("Failed to load SFB rules: {}", file, e);
            }
        }
    }

    /**
     * Load Star Trek lore from docs/star-trek-4x-rules-db/*.json.
     */
    private void loadStarTrekLore() {
        String[] loreFiles = {"species.json", "technologies.json", "star-systems.json", "mission-templates.json"};
        for (String file : loreFiles) {
            try {
                Path path = Paths.get("docs/star-trek-4x-rules-db", file);
                if (Files.exists(path)) {
                    JsonNode node = objectMapper.readTree(path.toFile());
                    starTrekLore.put(file, node);
                    log.debug("Loaded Star Trek lore: {}", file);
                }
            } catch (IOException e) {
                log.warn("Failed to load Star Trek lore: {}", file, e);
            }
        }
    }

    /**
     * Load Captain's Log rules from JPA tables.
     */
    private void loadCaptainLogRules() {
        try {
            captainLogRules.addAll(rulesEntryRepository.findAll());
            captainLogTables.addAll(rulesTableRepository.findAll());
            log.debug("Loaded {} Captain's Log rules and {} tables", captainLogRules.size(), captainLogTables.size());
        } catch (Exception e) {
            log.warn("Failed to load Captain's Log rules", e);
        }
    }

    /**
     * Load Earth 2026 rules via PlanetService.
     */
    private void loadEarth2026Rules() {
        try {
            galaxyRules = planetService.getGalaxyRules();
            systemRules = planetService.getSystemRules();
            planetRules = planetService.getPlanetRules();
            log.debug("Loaded Earth 2026 rules");
        } catch (Exception e) {
            log.warn("Failed to load Earth 2026 rules", e);
        }
    }

    // ------------------------------------------------------------------
    // Query methods
    // ------------------------------------------------------------------

    /**
     * Get all SFB empires.
     */
    public List<SfbEmpire> getEmpires() {
        List<SfbEmpire> empires = new ArrayList<>();
        JsonNode empiresNode = sfbRules.get("empires.json");
        if (empiresNode != null && empiresNode.isArray()) {
            for (JsonNode node : empiresNode) {
                SfbEmpire empire = objectMapper.convertValue(node, SfbEmpire.class);
                empires.add(empire);
            }
        }
        return empires;
    }

    /**
     * Get SFB empire by name.
     */
    public Optional<SfbEmpire> getEmpire(String name) {
        return getEmpires().stream()
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Get all ship classes from SFB rules.
     */
    public List<JsonNode> getShipClasses() {
        List<JsonNode> classes = new ArrayList<>();
        JsonNode shipNode = sfbRules.get("ship-classes.json");
        if (shipNode != null && shipNode.isArray()) {
            for (JsonNode node : shipNode) {
                classes.add(node);
            }
        }
        return classes;
    }

    /**
     * Get all weapons from SFB rules.
     */
    public List<JsonNode> getWeapons() {
        List<JsonNode> weapons = new ArrayList<>();
        JsonNode weaponNode = sfbRules.get("weapons.json");
        if (weaponNode != null && weaponNode.isArray()) {
            for (JsonNode node : weaponNode) {
                weapons.add(node);
            }
        }
        return weapons;
    }

    /**
     * Get Star Trek species from lore database.
     */
    public List<JsonNode> getSpecies() {
        List<JsonNode> species = new ArrayList<>();
        JsonNode speciesNode = starTrekLore.get("species.json");
        if (speciesNode != null && speciesNode.isArray()) {
            for (JsonNode node : speciesNode) {
                species.add(node);
            }
        }
        // Also add from Captain's Log species
        captainLogRules.stream()
                .filter(r -> "species".equals(r.getCategory()))
                .forEach(r -> species.add(objectMapper.valueToTree(
                        Map.of("name", r.getTitle(), "description", r.getText(),
                                "category", r.getCategory(), "chapter", r.getChapter()))));
        return species;
    }

    /**
     * Get technologies from Star Trek lore.
     */
    public List<JsonNode> getTechnologies() {
        List<JsonNode> technologies = new ArrayList<>();
        JsonNode techNode = starTrekLore.get("technologies.json");
        if (techNode != null && techNode.isArray()) {
            for (JsonNode node : techNode) {
                technologies.add(node);
            }
        }
        return technologies;
    }

    /**
     * Get mission templates from Star Trek lore.
     */
    public List<JsonNode> getMissionTemplates() {
        List<JsonNode> templates = new ArrayList<>();
        JsonNode templateNode = starTrekLore.get("mission-templates.json");
        if (templateNode != null && templateNode.isArray()) {
            for (JsonNode node : templateNode) {
                templates.add(node);
            }
        }
        return templates;
    }

    /**
     * Get Captain's Log rules by category.
     */
    public List<RulesEntry> getCaptainLogRules(String category) {
        return captainLogRules.stream()
                .filter(r -> category.equals(r.getCategory()))
                .toList();
    }

    /**
     * Get Captain's Log rules table by name.
     */
    public Optional<RulesTable> getCaptainLogTable(String name) {
        return captainLogTables.stream()
                .filter(t -> t.getTableName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Get all Captain's Log rules tables.
     */
    public List<RulesTable> getAllCaptainLogTables() {
        return captainLogTables;
    }

    /**
     * Get Earth 2026 galaxy rules.
     */
    public E26GalaxyRules getGalaxyRules() {
        return galaxyRules;
    }

    /**
     * Get Earth 2026 system rules.
     */
    public E26SystemRules getSystemRules() {
        return systemRules;
    }

    /**
     * Get Earth 2026 planet rules.
     */
    public E26PlanetRules getPlanetRules() {
        return planetRules;
    }

    /**
     * Get all tech levels from Earth 2026.
     */
    public List<com.yisy.modules.doramcu.earth2026.model.TechLevel> getAllTechLevels() {
        if (planetService != null) {
            return planetService.getAllTechLevels();
        }
        return new ArrayList<>();
    }

    /**
     * Query rules from all sources with a keyword.
     * Priority: Star Trek lore > SFB > Captain's Log > Earth 2026.
     */
    public List<Map<String, Object>> searchRules(String keyword) {
        List<Map<String, Object>> results = new ArrayList<>();

        // Search Star Trek lore first
        for (Map.Entry<String, JsonNode> entry : starTrekLore.entrySet()) {
            JsonNode node = entry.getValue();
            if (node.isArray()) {
                for (JsonNode item : node) {
                    if (containsText(item, keyword)) {
                        Map<String, Object> result = objectMapper.convertValue(item, Map.class);
                        result.put("source", "STAR_TREK_LORE");
                        results.add(result);
                    }
                }
            }
        }

        // Search SFB rules
        for (Map.Entry<String, JsonNode> entry : sfbRules.entrySet()) {
            JsonNode node = entry.getValue();
            if (node.isArray()) {
                for (JsonNode item : node) {
                    if (containsText(item, keyword)) {
                        Map<String, Object> result = objectMapper.convertValue(item, Map.class);
                        result.put("source", "SFB");
                        results.add(result);
                    }
                }
            }
        }

        // Search Captain's Log rules
        for (RulesEntry entry : captainLogRules) {
            if (entry.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                (entry.getText() != null && entry.getText().toLowerCase().contains(keyword.toLowerCase()))) {
                Map<String, Object> result = new HashMap<>();
                result.put("name", entry.getTitle());
                result.put("description", entry.getText());
                result.put("category", entry.getCategory());
                result.put("source", "CAPTAINS_LOG");
                results.add(result);
            }
        }

        return results;
    }

    private boolean containsText(JsonNode node, String keyword) {
        try {
            return objectMapper.writeValueAsString(node).toLowerCase().contains(keyword.toLowerCase());
        } catch (Exception e) {
            return node.toString().toLowerCase().contains(keyword.toLowerCase());
        }
    }

    /**
     * Reload all rules from sources.
     */
    public void reload() {
        sfbRules.clear();
        starTrekLore.clear();
        captainLogRules.clear();
        captainLogTables.clear();
        loadAllRules();
    }
}
