package com.yisy.modules.doramcu.babylon5.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.babylon5.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Loads and provides access to the Babylon 5 universe database from JSON.
 * Follows the SFB pattern: loads JSON via ObjectMapper from file path.
 */
@Service
public class Babylon5DatabaseService {

    private static final Logger log = LoggerFactory.getLogger(Babylon5DatabaseService.class);

    private final ObjectMapper objectMapper;
    private final String databasePath;

    private List<B5Race> races;
    private List<B5Character> characters;
    private List<B5Storyline> storylines;
    private List<B5Dialogue> dialogues;
    private List<B5Location> locations;

    public Babylon5DatabaseService() {
        this.objectMapper = new ObjectMapper();
        this.databasePath = "docs/babylon5-db/babylon5-universe.json";
    }

    /**
     * Constructor with custom path for testing.
     */
    public Babylon5DatabaseService(String databasePath) {
        this.objectMapper = new ObjectMapper();
        this.databasePath = databasePath;
    }

    @PostConstruct
    public void loadDatabase() {
        try {
            File file = new File(databasePath);
            if (!file.exists()) {
                log.warn("Babylon 5 database file not found: {}", databasePath);
                races = new ArrayList<>();
                characters = new ArrayList<>();
                storylines = new ArrayList<>();
                dialogues = new ArrayList<>();
                locations = new ArrayList<>();
                return;
            }

            JsonNode root = objectMapper.readTree(file);
            loadRaces(root);
            loadCharacters(root);
            loadStorylines(root);
            loadDialogues(root);
            loadLocations(root);

            log.info("Loaded Babylon 5 database: {} races, {} characters, {} storylines, {} dialogues, {} locations",
                    races.size(), characters.size(), storylines.size(), dialogues.size(), locations.size());
        } catch (IOException e) {
            log.error("Failed to load Babylon 5 database from {}", databasePath, e);
            races = new ArrayList<>();
            characters = new ArrayList<>();
            storylines = new ArrayList<>();
            dialogues = new ArrayList<>();
            locations = new ArrayList<>();
        }
    }

    private void loadRaces(JsonNode root) {
        races = new ArrayList<>();
        JsonNode node = root.get("races");
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                try {
                    B5Race race = objectMapper.treeToValue(item, B5Race.class);
                    races.add(race);
                    log.debug("Parsed race: {}", race.getId());
                } catch (Exception e) {
                    log.error("Failed to parse race: {}", item, e);
                }
            }
        }
        log.info("Loaded {} races", races.size());
    }

    private void loadCharacters(JsonNode root) {
        characters = new ArrayList<>();
        JsonNode node = root.get("characters");
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                try {
                    B5Character ch = objectMapper.treeToValue(item, B5Character.class);
                    characters.add(ch);
                    log.debug("Parsed character: {}", ch.getId());
                } catch (Exception e) {
                    log.error("Failed to parse character: {}", item, e);
                }
            }
        }
        log.info("Loaded {} characters", characters.size());
    }

    private void loadStorylines(JsonNode root) {
        storylines = new ArrayList<>();
        JsonNode node = root.get("storylines");
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                try {
                    storylines.add(objectMapper.treeToValue(item, B5Storyline.class));
                } catch (Exception e) {
                    log.warn("Failed to parse storyline: {}", item, e);
                }
            }
        }
    }

    private void loadDialogues(JsonNode root) {
        dialogues = new ArrayList<>();
        JsonNode node = root.get("dialogues");
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                try {
                    dialogues.add(objectMapper.treeToValue(item, B5Dialogue.class));
                } catch (Exception e) {
                    log.warn("Failed to parse dialogue: {}", item, e);
                }
            }
        }
    }

    private void loadLocations(JsonNode root) {
        locations = new ArrayList<>();
        JsonNode node = root.get("locations");
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                try {
                    locations.add(objectMapper.treeToValue(item, B5Location.class));
                } catch (Exception e) {
                    log.warn("Failed to parse location: {}", item, e);
                }
            }
        }
    }

    // ==========================================
    // Race methods
    // ==========================================

    public List<B5Race> getAllRaces() {
        return races;
    }

    public Optional<B5Race> getRace(String id) {
        return races.stream()
                .filter(race -> race.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    // ==========================================
    // Character methods
    // ==========================================

    public List<B5Character> getAllCharacters() {
        return characters;
    }

    public Optional<B5Character> getCharacter(String id) {
        return characters.stream()
                .filter(ch -> ch.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    public List<B5Character> getCharactersBySpecies(String speciesId) {
        return characters.stream()
                .filter(ch -> speciesId.equalsIgnoreCase(ch.getSpecies()))
                .toList();
    }

    // ==========================================
    // Storyline methods
    // ==========================================

    public List<B5Storyline> getAllStorylines() {
        return storylines;
    }

    public Optional<B5Storyline> getStoryline(String id) {
        return storylines.stream()
                .filter(sl -> sl.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    // ==========================================
    // Dialogue methods
    // ==========================================

    public List<B5Dialogue> getAllDialogues() {
        return dialogues;
    }

    // ==========================================
    // Location methods
    // ==========================================

    public List<B5Location> getAllLocations() {
        return locations;
    }

    public Optional<B5Location> getLocation(String id) {
        return locations.stream()
                .filter(loc -> loc.getId().equalsIgnoreCase(id))
                .findFirst();
    }
}
