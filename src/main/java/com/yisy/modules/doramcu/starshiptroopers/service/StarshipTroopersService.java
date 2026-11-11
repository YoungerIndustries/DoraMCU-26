package com.yisy.modules.doramcu.starshiptroopers.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.starshiptroopers.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for loading and querying the Starship Troopers rules database.
 * <p>
 * Loads all JSON data files from the {@code starship-troopers-rules/} classpath resource
 * and provides typed access to units, scenarios, combat rules, and combat tables.
 * </p>
 */
public class StarshipTroopersService {

    private static final String RESOURCE_PREFIX = "";

    private final ObjectMapper objectMapper;
    private final List<Unit> terranUnits;
    private final List<Unit> arachnidUnits;
    private final List<Unit> humanoidUnits;
    private final List<Scenario> scenarios;
    private final List<SpecialEquipment> specialEquipment;
    private final Map<String, Map<String, String>> terranCombatTable;
    private final Map<String, Map<String, String>> alienCombatTable;
    private final List<String> oddsRatios;
    private final List<String> terranTurnPhases;
    private final List<String> arachnidTurnPhases;
    private final Map<String, String> combatOutcomes;

    public StarshipTroopersService() {
        this(new ObjectMapper());
    }

    StarshipTroopersService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.terranUnits = loadTerranUnits();
        this.arachnidUnits = loadArachnidUnits();
        this.humanoidUnits = loadHumanoidUnits();
        this.scenarios = loadScenarios();
        this.specialEquipment = loadSpecialEquipment();
        this.terranCombatTable = loadTerranCombatTable();
        this.alienCombatTable = loadAlienCombatTable();
        this.oddsRatios = loadOddsRatios();
        this.terranTurnPhases = loadTerranTurnPhases();
        this.arachnidTurnPhases = loadArachnidTurnPhases();
        this.combatOutcomes = loadCombatOutcomes();
    }

    // ==================== Unit Accessors ====================

    public List<Unit> getTerranUnits() {
        return Collections.unmodifiableList(terranUnits);
    }

    public List<Unit> getArachnidUnits() {
        return Collections.unmodifiableList(arachnidUnits);
    }

    public List<Unit> getHumanoidUnits() {
        return Collections.unmodifiableList(humanoidUnits);
    }

    public Optional<Unit> getTerranUnitById(String id) {
        return terranUnits.stream()
                .filter(u -> u.id().equals(id))
                .findFirst();
    }

    public Optional<Unit> getArachnidUnitById(String id) {
        return arachnidUnits.stream()
                .filter(u -> u.id().equals(id))
                .findFirst();
    }

    public Optional<Unit> getHumanoidUnitById(String id) {
        return humanoidUnits.stream()
                .filter(u -> u.id().equals(id))
                .findFirst();
    }

    /**
     * Returns all unit IDs defined across all factions.
     */
    public Set<String> getAllUnitIds() {
        Set<String> ids = new HashSet<>();
        terranUnits.forEach(u -> ids.add(u.id()));
        arachnidUnits.forEach(u -> ids.add(u.id()));
        humanoidUnits.forEach(u -> ids.add(u.id()));
        return ids;
    }

    /**
     * Returns all combat-capable Terran units (attack strength > 0).
     */
    public List<Unit> getTerranCombatUnits() {
        return terranUnits.stream()
                .filter(Unit::isCombatUnit)
                .collect(Collectors.toList());
    }

    /**
     * Returns all Arachnid units with a given victory point value.
     */
    public List<Unit> getArachnidUnitsByVictoryPoints(int vp) {
        return arachnidUnits.stream()
                .filter(u -> u.victoryPoints() != null && u.victoryPoints() == vp)
                .collect(Collectors.toList());
    }

    // ==================== Scenarios ====================

    public List<Scenario> getScenarios() {
        return Collections.unmodifiableList(scenarios);
    }

    public Optional<Scenario> getScenarioById(int id) {
        return scenarios.stream()
                .filter(s -> s.id() == id)
                .findFirst();
    }

    /**
     * Returns scenarios sorted by difficulty level.
     */
    public List<Scenario> getScenariosByDifficulty() {
        List<String> difficultyOrder = List.of("Introductory", "Easy", "Medium", "Hard", "Very Hard", "Expert");
        return scenarios.stream()
                .sorted(Comparator.comparingInt(s -> {
                    int idx = difficultyOrder.indexOf(s.difficulty());
                    return idx >= 0 ? idx : difficultyOrder.size();
                }))
                .collect(Collectors.toList());
    }

    // ==================== Special Equipment ====================

    public List<SpecialEquipment> getSpecialEquipment() {
        return Collections.unmodifiableList(specialEquipment);
    }

    public Optional<SpecialEquipment> getEquipmentById(String id) {
        return specialEquipment.stream()
                .filter(e -> e.id().equals(id))
                .findFirst();
    }

    public List<SpecialEquipment> getExplosiveEquipment() {
        return specialEquipment.stream()
                .filter(SpecialEquipment::isExplosive)
                .collect(Collectors.toList());
    }

    // ==================== Combat Tables ====================

    public Map<String, Map<String, String>> getTerranCombatTable() {
        return Collections.unmodifiableMap(terranCombatTable);
    }

    public Map<String, Map<String, String>> getAlienCombatTable() {
        return Collections.unmodifiableMap(alienCombatTable);
    }

    public List<String> getOddsRatios() {
        return Collections.unmodifiableList(oddsRatios);
    }

    /**
     * Resolves a combat roll using the Terran attack table.
     *
     * @param oddsRatio the attack-to-defense odds ratio (e.g. "2:1")
     * @param dieRoll   the die roll result (1-6)
     * @return the combat outcome code (M, D, W, K, H)
     */
    public String resolveTerranCombat(String oddsRatio, int dieRoll) {
        Map<String, String> row = terranCombatTable.get(oddsRatio);
        if (row == null)
            throw new IllegalArgumentException("Unknown odds ratio: " + oddsRatio);
        String outcome = row.get(String.valueOf(dieRoll));
        if (outcome == null)
            throw new IllegalArgumentException("Invalid die roll: " + dieRoll);
        return outcome;
    }

    /**
     * Resolves a combat roll using the Alien attack table.
     */
    public String resolveAlienCombat(String oddsRatio, int dieRoll) {
        Map<String, String> row = alienCombatTable.get(oddsRatio);
        if (row == null)
            throw new IllegalArgumentException("Unknown odds ratio: " + oddsRatio);
        String outcome = row.get(String.valueOf(dieRoll));
        if (outcome == null)
            throw new IllegalArgumentException("Invalid die roll: " + dieRoll);
        return outcome;
    }

    /**
     * Calculates the odds ratio from attacker and defender strength.
     * Rounds down to the nearest standard odds category.
     */
    public String calculateOddsRatio(int attackStrength, int defenseStrength) {
        if (defenseStrength <= 0)
            return "7:1+";
        double ratio = (double) attackStrength / defenseStrength;

        // Standard odds categories in order
        double[] thresholds = {0.167, 0.2, 0.25, 0.333, 0.5, 2.0, 3.0, 4.0, 5.0, 6.0};
        String[] labels = {"1:6", "1:5", "1:4", "1:3", "1:2", "2:1", "3:1", "4:1", "5:1", "6:1"};

        for (int i = thresholds.length - 1; i >= 0; i--) {
            if (ratio >= thresholds[i]) {
                return labels[i];
            }
        }
        if (ratio >= 7.0)
            return "7:1+";
        return "1:6";
    }

    // ==================== Turn Sequence ====================

    public List<String> getTerranTurnPhases() {
        return Collections.unmodifiableList(terranTurnPhases);
    }

    public List<String> getArachnidTurnPhases() {
        return Collections.unmodifiableList(arachnidTurnPhases);
    }

    public Map<String, String> getCombatOutcomes() {
        return Collections.unmodifiableMap(combatOutcomes);
    }

    // ==================== JSON Loading Methods ====================

    private JsonNode loadJson(String filename) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (is == null)
                throw new IllegalStateException("Resource not found: " + filename);
            return objectMapper.readTree(is);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + filename, e);
        }
    }

    private List<Unit> loadTerranUnits() {
        JsonNode root = loadJson(RESOURCE_PREFIX + "terran-units.json");
        return parseUnits(root.path("unit_types"));
    }

    private List<Unit> loadArachnidUnits() {
        JsonNode root = loadJson(RESOURCE_PREFIX + "arachnid-units.json");
        return parseUnits(root.path("unit_types"));
    }

    private List<Unit> loadHumanoidUnits() {
        JsonNode root = loadJson(RESOURCE_PREFIX + "humanoid-units.json");
        return parseUnits(root.path("unit_types"));
    }

    private List<Unit> parseUnits(JsonNode nodes) {
        List<Unit> units = new ArrayList<>();
        for (JsonNode node : nodes) {
            List<String> abilities = parseStringList(node.path("abilities"));
            List<String> equipment = parseStringList(node.path("equipment"));
            List<UnitVariant> variants = parseVariants(node.path("variants"));
            units.add(new Unit(
                    node.path("id").asText(),
                    node.path("name").asText(),
                    node.path("type").asText(),
                    node.path("attack_strength").asInt(),
                    node.path("defense_strength").asInt(),
                    node.path("movement_allowance").asInt(),
                    node.path("description").asText(""),
                    abilities,
                    node.has("victory_points") ? node.path("victory_points").asInt() : null,
                    node.has("counters_per_scenario") ? node.path("counters_per_scenario").asInt() : null,
                    node.has("range") ? node.path("range").asInt() : null,
                    equipment,
                    variants,
                    node.has("special") ? node.path("special").asText("") : null
            ));
        }
        return units;
    }

    private List<UnitVariant> parseVariants(JsonNode nodes) {
        List<UnitVariant> variants = new ArrayList<>();
        if (nodes.isMissingNode() || nodes.isNull()) return variants;
        for (JsonNode node : nodes) {
            variants.add(new UnitVariant(
                    node.path("name").asText(""),
                    node.has("color") ? node.path("color").asText("") : null,
                    node.path("description").asText(""),
                    node.has("attack_strength") ? node.path("attack_strength").asInt() : null
            ));
        }
        return variants;
    }

    private List<String> parseStringList(JsonNode nodes) {
        List<String> list = new ArrayList<>();
        if (nodes.isMissingNode() || nodes.isNull()) return list;
        for (JsonNode node : nodes) {
            list.add(node.asText());
        }
        return list;
    }

    private List<Scenario> loadScenarios() {
        JsonNode root = loadJson(RESOURCE_PREFIX + "scenarios.json");
        List<Scenario> scenarios = new ArrayList<>();
        for (JsonNode node : root.path("scenarios")) {
            scenarios.add(new Scenario(
                    node.path("id").asInt(),
                    node.path("name").asText(),
                    node.path("description").asText(""),
                    node.path("difficulty").asText(""),
                    node.path("turns").asInt(),
                    parseStringList(node.path("special_rules")),
                    parseForceMap(node.path("terran_forces")),
                    parseForceMap(node.path("arachnid_forces")),
                    parseForceMap(node.path("humanoid_forces")),
                    parseVictoryConditions(node.path("victory_conditions"))
            ));
        }
        return scenarios;
    }

    private Map<String, Integer> parseForceMap(JsonNode node) {
        Map<String, Integer> map = new LinkedHashMap<>();
        if (node.isMissingNode() || node.isNull()) return map;
        node.fields().forEachRemaining(entry -> map.put(entry.getKey(), entry.getValue().asInt()));
        return map;
    }

    private VictoryConditions parseVictoryConditions(JsonNode node) {
        return new VictoryConditions(
                node.path("terran_victory").asText(""),
                node.path("arachnid_victory").asText("")
        );
    }

    private List<SpecialEquipment> loadSpecialEquipment() {
        JsonNode root = loadJson(RESOURCE_PREFIX + "special-equipment.json");
        List<SpecialEquipment> equipment = new ArrayList<>();
        for (JsonNode node : root.path("special_weapons_and_equipment").path("equipment")) {
            equipment.add(new SpecialEquipment(
                    node.path("id").asText(),
                    node.path("name").asText(),
                    node.path("type").asText(),
                    node.path("attack_strength").asInt(),
                    node.path("max_per_unit").asInt(),
                    node.has("tax_strength") ? node.path("tax_strength").asInt() : null,
                    node.path("range").asInt(),
                    parseStringList(node.path("special_effects")),
                    parseStringList(node.path("restrictions")),
                    node.path("description").asText("")
            ));
        }
        return equipment;
    }

    private Map<String, Map<String, String>> loadTerranCombatTable() {
        return loadCombatTable(RESOURCE_PREFIX + "combat-tables.json", "terran_attack_table");
    }

    private Map<String, Map<String, String>> loadAlienCombatTable() {
        return loadCombatTable(RESOURCE_PREFIX + "combat-tables.json", "alien_attack_table");
    }

    private Map<String, Map<String, String>> loadCombatTable(String filename, String tableName) {
        JsonNode root = loadJson(filename);
        JsonNode table = root.path(tableName).path("table");
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        table.fields().forEachRemaining(oddsEntry -> {
            Map<String, String> outcomes = new LinkedHashMap<>();
            oddsEntry.getValue().fields().forEachRemaining(dieEntry ->
                    outcomes.put(dieEntry.getKey(), dieEntry.getValue().asText()));
            result.put(oddsEntry.getKey(), outcomes);
        });
        return result;
    }

    private List<String> loadOddsRatios() {
        JsonNode root = loadJson(RESOURCE_PREFIX + "combat-tables.json");
        List<String> ratios = new ArrayList<>();
        for (JsonNode node : root.path("combat_tables").path("odds_ratios")) {
            ratios.add(node.asText());
        }
        return ratios;
    }

    private List<String> loadTerranTurnPhases() {
        JsonNode root = loadJson(RESOURCE_PREFIX + "combat-rules.json");
        List<String> phases = new ArrayList<>();
        for (JsonNode node : root.path("turn_sequence").path("terran_turn").path("phases")) {
            phases.add(node.path("name").asText());
        }
        return phases;
    }

    private List<String> loadArachnidTurnPhases() {
        JsonNode root = loadJson(RESOURCE_PREFIX + "combat-rules.json");
        List<String> phases = new ArrayList<>();
        for (JsonNode node : root.path("turn_sequence").path("arachnid_turn").path("phases")) {
            phases.add(node.path("name").asText());
        }
        return phases;
    }

    private Map<String, String> loadCombatOutcomes() {
        JsonNode root = loadJson(RESOURCE_PREFIX + "combat-rules.json");
        JsonNode outcomes = root.path("combat_resolution").path("outcomes");
        Map<String, String> map = new LinkedHashMap<>();
        outcomes.fields().forEachRemaining(entry -> map.put(entry.getKey(), entry.getValue().asText()));
        return map;
    }
}
