package com.yisy.modules.doramcu.star_trek_4x.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Star Trek lore database that provides species, technologies, ship classes,
 * and mission templates for the 4x game.
 * <p>
 * Loads data from docs/star-trek-4x-rules-db/*.json files.
 * </p>
 */
@Service
public class StarTrekLoreDatabase {

    private static final Logger log = LoggerFactory.getLogger(StarTrekLoreDatabase.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<JsonNode> species = new ArrayList<>();
    private final List<JsonNode> technologies = new ArrayList<>();
    private final List<JsonNode> starSystems = new ArrayList<>();
    private final List<JsonNode> missionTemplates = new ArrayList<>();

    public StarTrekLoreDatabase() {
        loadLore();
    }

    private void loadLore() {
        loadJsonArray("species.json", species);
        loadJsonArray("technologies.json", technologies);
        loadJsonArray("star-systems.json", starSystems);
        loadJsonArray("mission-templates.json", missionTemplates);
        log.info("Star Trek lore loaded: {} species, {} technologies, {} star systems, {} mission templates",
                species.size(), technologies.size(), starSystems.size(), missionTemplates.size());
    }

    private void loadJsonArray(String file, List<JsonNode> target) {
        try {
            Path path = Paths.get("docs/star-trek-4x-rules-db", file);
            if (Files.exists(path)) {
                JsonNode node = objectMapper.readTree(path.toFile());
                if (node.isArray()) {
                    for (JsonNode item : node) {
                        target.add(item);
                    }
                }
            }
        } catch (IOException e) {
            log.warn("Failed to load Star Trek lore: {}", file, e);
        }
    }

    /**
     * Get all species.
     */
    public List<JsonNode> getSpecies() {
        return Collections.unmodifiableList(species);
    }

    /**
     * Get species by ID.
     */
    public Optional<JsonNode> getSpecies(String id) {
        return species.stream()
                .filter(s -> s.has("id") && s.get("id").asText().equals(id))
                .findFirst();
    }

    /**
     * Get all technologies.
     */
    public List<JsonNode> getTechnologies() {
        return Collections.unmodifiableList(technologies);
    }

    /**
     * Get technology by ID.
     */
    public Optional<JsonNode> getTechnology(String id) {
        return technologies.stream()
                .filter(t -> t.has("id") && t.get("id").asText().equals(id))
                .findFirst();
    }

    /**
     * Get all star systems.
     */
    public List<JsonNode> getStarSystems() {
        return Collections.unmodifiableList(starSystems);
    }

    /**
     * Get star system by ID.
     */
    public Optional<JsonNode> getStarSystem(String id) {
        return starSystems.stream()
                .filter(s -> s.has("id") && s.get("id").asText().equals(id))
                .findFirst();
    }

    /**
     * Get all mission templates.
     */
    public List<JsonNode> getMissionTemplates() {
        return Collections.unmodifiableList(missionTemplates);
    }

    /**
     * Get mission template by type.
     */
    public Optional<JsonNode> getMissionTemplate(String type) {
        return missionTemplates.stream()
                .filter(m -> m.has("type") && m.get("type").asText().equals(type))
                .findFirst();
    }

    /**
     * Get a random species.
     */
    public JsonNode getRandomSpecies() {
        if (species.isEmpty()) return null;
        return species.get(new Random().nextInt(species.size()));
    }

    /**
     * Get a random technology.
     */
    public JsonNode getRandomTechnology() {
        if (technologies.isEmpty()) return null;
        return technologies.get(new Random().nextInt(technologies.size()));
    }
}
