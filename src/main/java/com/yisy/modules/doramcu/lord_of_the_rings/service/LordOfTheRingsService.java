package com.yisy.modules.doramcu.lord_of_the_rings.service;
import com.yisy.modules.doramcu.sfb.service.SfbDiplomacyService.FactionProfile;
import com.yisy.modules.doramcu.sfb.service.SfbDiplomacyService.RelationType;
import com.yisy.modules.doramcu.sfb.service.SfbEconomyService.FactionEconomy;
import com.yisy.modules.doramcu.sfb.service.SfbEconomyService.ResourceType;
import com.yisy.modules.doramcu.lord_of_the_rings.model.FactionLeader;
import com.yisy.modules.doramcu.lord_of_the_rings.model.MilitaryBranch;
import com.yisy.modules.doramcu.model.players.Player;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.lord_of_the_rings.model.Faction;
import com.yisy.modules.doramcu.lord_of_the_rings.model.Lore;
import com.yisy.modules.doramcu.lord_of_the_rings.model.Scenario;
import com.yisy.modules.doramcu.lord_of_the_rings.model.SpecialAbility;
import com.yisy.modules.doramcu.lord_of_the_rings.model.Unit;
import com.yisy.modules.doramcu.lord_of_the_rings.model.UnitStats;
import com.yisy.modules.doramcu.lord_of_the_rings.model.UnitVariant;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Service for loading and querying the Lord of the Rings rules database.
 * <p>
 * Loads all JSON data files from the {@code lord-of-the-rings-rules/} classpath resource
 * and provides typed access to units, factions, scenarios, special abilities, combat tables, and lore.
 * </p>
 */
@Service
public class LordOfTheRingsService {

    private static final String RESOURCE_PREFIX = "lord-of-the-rings-rules/";

    private final ObjectMapper objectMapper;
    
    // Lazy-loaded data sets using AtomicReference for thread-safe initialization
    private final AtomicReference<List<Unit>> groundUnitsRef = new AtomicReference<>();
    private final AtomicReference<List<Unit>> starshipsRef = new AtomicReference<>();
    private final AtomicReference<List<Unit>> groundVehiclesRef = new AtomicReference<>();
    private final AtomicReference<List<Unit>> charactersRef = new AtomicReference<>();
    private final AtomicReference<List<Faction>> factionsRef = new AtomicReference<>();
    private final AtomicReference<List<Scenario>> scenariosRef = new AtomicReference<>();
    private final AtomicReference<List<SpecialAbility>> specialAbilitiesRef = new AtomicReference<>();
    private final AtomicReference<List<Lore>> loreRef = new AtomicReference<>();
    private final AtomicReference<Map<String, Map<String, String>>> groundCombatTableRef = new AtomicReference<>();
    private final AtomicReference<Map<String, Map<String, String>>> spaceCombatTableRef = new AtomicReference<>();
    private final AtomicReference<Map<String, String>> combatOutcomesRef = new AtomicReference<>();
    
    // Cached derived data (cleared when clearCache() is called)
    private final ConcurrentHashMap<String, List<Unit>> unitsByFactionCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<Scenario>> scenariosByEraCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<Scenario>> scenariosByFactionCache = new ConcurrentHashMap<>();

    public LordOfTheRingsService() {
        this(new ObjectMapper());
    }

    LordOfTheRingsService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        // Lazy loading: data is loaded on first access, not at construction time
    }
    
    // ==================== Lazy Loading Helpers ====================
    
    private List<Unit> getGroundUnitsLazy() {
        List<Unit> units = groundUnitsRef.get();
        if (units == null) {
            synchronized (groundUnitsRef) {
                units = groundUnitsRef.get();
                if (units == null) {
                    units = loadGroundUnits();
                    groundUnitsRef.set(units);
                }
            }
        }
        return units;
    }
    
    private List<Unit> getStarshipsLazy() {
        List<Unit> units = starshipsRef.get();
        if (units == null) {
            synchronized (starshipsRef) {
                units = starshipsRef.get();
                if (units == null) {
                    units = loadStarships();
                    starshipsRef.set(units);
                }
            }
        }
        return units;
    }
    
    private List<Unit> getGroundVehiclesLazy() {
        List<Unit> units = groundVehiclesRef.get();
        if (units == null) {
            synchronized (groundVehiclesRef) {
                units = groundVehiclesRef.get();
                if (units == null) {
                    units = loadGroundVehicles();
                    groundVehiclesRef.set(units);
                }
            }
        }
        return units;
    }
    
    private List<Unit> getCharactersLazy() {
        List<Unit> units = charactersRef.get();
        if (units == null) {
            synchronized (charactersRef) {
                units = charactersRef.get();
                if (units == null) {
                    units = loadCharacters();
                    charactersRef.set(units);
                }
            }
        }
        return units;
    }
    
    private List<Faction> getFactionsLazy() {
        List<Faction> factions = factionsRef.get();
        if (factions == null) {
            synchronized (factionsRef) {
                factions = factionsRef.get();
                if (factions == null) {
                    factions = loadFactions();
                    factionsRef.set(factions);
                }
            }
        }
        return factions;
    }
    
    private List<Scenario> getScenariosLazy() {
        List<Scenario> scenarios = scenariosRef.get();
        if (scenarios == null) {
            synchronized (scenariosRef) {
                scenarios = scenariosRef.get();
                if (scenarios == null) {
                    scenarios = loadScenarios();
                    scenariosRef.set(scenarios);
                }
            }
        }
        return scenarios;
    }
    
    private List<SpecialAbility> getSpecialAbilitiesLazy() {
        List<SpecialAbility> abilities = specialAbilitiesRef.get();
        if (abilities == null) {
            synchronized (specialAbilitiesRef) {
                abilities = specialAbilitiesRef.get();
                if (abilities == null) {
                    abilities = loadSpecialAbilities();
                    specialAbilitiesRef.set(abilities);
                }
            }
        }
        return abilities;
    }
    
    private List<Lore> getLoreLazy() {
        List<Lore> lore = loreRef.get();
        if (lore == null) {
            synchronized (loreRef) {
                lore = loreRef.get();
                if (lore == null) {
                    lore = loadLore();
                    loreRef.set(lore);
                }
            }
        }
        return lore;
    }
    
    private Map<String, Map<String, String>> getGroundCombatTableLazy() {
        Map<String, Map<String, String>> table = groundCombatTableRef.get();
        if (table == null) {
            synchronized (groundCombatTableRef) {
                table = groundCombatTableRef.get();
                if (table == null) {
                    table = loadCombatTable(RESOURCE_PREFIX + "combat-tables.json", "ground_combat_table");
                    groundCombatTableRef.set(table);
                }
            }
        }
        return table;
    }
    
    private Map<String, Map<String, String>> getSpaceCombatTableLazy() {
        Map<String, Map<String, String>> table = spaceCombatTableRef.get();
        if (table == null) {
            synchronized (spaceCombatTableRef) {
                table = spaceCombatTableRef.get();
                if (table == null) {
                    table = loadCombatTable(RESOURCE_PREFIX + "combat-tables.json", "space_combat_table");
                    spaceCombatTableRef.set(table);
                }
            }
        }
        return table;
    }
    
    private Map<String, String> getCombatOutcomesLazy() {
        Map<String, String> outcomes = combatOutcomesRef.get();
        if (outcomes == null) {
            synchronized (combatOutcomesRef) {
                outcomes = combatOutcomesRef.get();
                if (outcomes == null) {
                    outcomes = loadCombatOutcomes();
                    combatOutcomesRef.set(outcomes);
                }
            }
        }
        return outcomes;
    }
    
    /**
     * Clears all cached data, forcing reload on next access.
     */
    public void clearCache() {
        groundUnitsRef.set(null);
        starshipsRef.set(null);
        groundVehiclesRef.set(null);
        charactersRef.set(null);
        factionsRef.set(null);
        scenariosRef.set(null);
        specialAbilitiesRef.set(null);
        loreRef.set(null);
        groundCombatTableRef.set(null);
        spaceCombatTableRef.set(null);
        combatOutcomesRef.set(null);
        unitsByFactionCache.clear();
        scenariosByEraCache.clear();
        scenariosByFactionCache.clear();
    }

    // ==================== Unit Accessors ====================

    public List<Unit> getGroundUnits() {
        return Collections.unmodifiableList(getGroundUnitsLazy());
    }

    public List<Unit> getStarships() {
        return Collections.unmodifiableList(getStarshipsLazy());
    }

    public List<Unit> getGroundVehicles() {
        return Collections.unmodifiableList(getGroundVehiclesLazy());
    }

    public List<Unit> getCharacters() {
        return Collections.unmodifiableList(getCharactersLazy());
    }

    /**
     * Returns all units across all categories.
     */
    public List<Unit> getAllUnits() {
        List<Unit> all = new ArrayList<>();
        all.addAll(getGroundUnitsLazy());
        all.addAll(getStarshipsLazy());
        all.addAll(getGroundVehiclesLazy());
        all.addAll(getCharactersLazy());
        return Collections.unmodifiableList(all);
    }

    public Optional<Unit> getUnitById(String id) {
        return getAllUnits().stream()
                .filter(u -> u.id().equals(id))
                .findFirst();
    }

    /**
     * Returns units belonging to a specific faction.
     */
    public List<Unit> getUnitsByFaction(String factionId) {
        return unitsByFactionCache.computeIfAbsent(factionId, key ->
            getAllUnits().stream()
                .filter(u -> key.equals(u.faction()))
                .collect(Collectors.toList())
        );
    }

    /**
     * Returns all combat-capable units (attack > 0).
     */
    public List<Unit> getCombatUnits() {
        return getAllUnits().stream()
                .filter(Unit::isCombatUnit)
                .collect(Collectors.toList());
    }

    /**
     * Returns Ring-bearer characters (ringResistance > 0).
     */
    public List<Unit> getRingBearers() {
        return getCharactersLazy().stream()
                .filter(u -> u.abilities() != null && u.abilities().stream().anyMatch(a -> a.contains("Ring")))
                .collect(Collectors.toList());
    }

    // ==================== Faction Accessors ====================

    public List<Faction> getFactions() {
        return Collections.unmodifiableList(getFactionsLazy());
    }

    public Optional<Faction> getFactionById(String id) {
        return getFactionsLazy().stream()
                .filter(f -> f.id().equals(id))
                .findFirst();
    }

    /**
     * Returns factions allied with the given faction.
     */
    public List<Faction> getAlliesOf(String factionId) {
        Optional<Faction> faction = getFactionById(factionId);
        if (faction.isEmpty()) return Collections.emptyList();
        List<String> allyIds = faction.get().allies();
        return getFactionsLazy().stream()
                .filter(f -> allyIds.contains(f.name()) || allyIds.contains(f.id()))
                .collect(Collectors.toList());
    }

    // ==================== Scenarios ====================

    public List<Scenario> getScenarios() {
        return Collections.unmodifiableList(getScenariosLazy());
    }

    public Optional<Scenario> getScenarioById(String id) {
        return getScenariosLazy().stream()
                .filter(s -> s.id().equals(id))
                .findFirst();
    }

    /**
     * Returns scenarios sorted by difficulty level.
     */
    public List<Scenario> getScenariosByDifficulty() {
        List<String> difficultyOrder = List.of("Introductory", "Easy", "Medium", "Hard", "Very Hard", "Expert");
        return getScenariosLazy().stream()
                .sorted(Comparator.comparingInt(s -> {
                    int idx = difficultyOrder.indexOf(s.difficulty());
                    return idx >= 0 ? idx : difficultyOrder.size();
                }))
                .collect(Collectors.toList());
    }

    /**
     * Returns scenarios filtered by era keyword.
     */
    public List<Scenario> getScenariosByEra(String eraKeyword) {
        return scenariosByEraCache.computeIfAbsent(eraKeyword.toLowerCase(), key ->
            getScenariosLazy().stream()
                .filter(s -> s.era().toLowerCase().contains(key))
                .collect(Collectors.toList())
        );
    }

    /**
     * Returns scenarios filtered by faction keyword.
     */
    public List<Scenario> getScenariosByFaction(String factionKeyword) {
        return scenariosByFactionCache.computeIfAbsent(factionKeyword.toLowerCase(), key ->
            getScenariosLazy().stream()
                .filter(s -> s.player1Faction().toLowerCase().contains(key)
                        || s.player2Faction().toLowerCase().contains(key))
                .collect(Collectors.toList())
        );
    }

    // ==================== Special Abilities ====================

    public List<SpecialAbility> getSpecialAbilities() {
        return Collections.unmodifiableList(getSpecialAbilitiesLazy());
    }

    public Optional<SpecialAbility> getAbilityById(String id) {
        return getSpecialAbilitiesLazy().stream()
                .filter(a -> a.id().equals(id))
                .findFirst();
    }

    /**
     * Returns Ring powers (One Ring and Three Elven Rings).
     */
    public List<SpecialAbility> getRingPowers() {
        return getSpecialAbilitiesLazy().stream()
                .filter(a -> a.category().contains("Ring"))
                .collect(Collectors.toList());
    }

    /**
     * Returns wizard abilities.
     */
    public List<SpecialAbility> getWizardAbilities() {
        return getSpecialAbilitiesLazy().stream()
                .filter(a -> a.category().contains("Wizard"))
                .collect(Collectors.toList());
    }

    /**
     * Returns racial traits.
     */
    public List<SpecialAbility> getRacialTraits() {
        return getSpecialAbilitiesLazy().stream()
                .filter(a -> a.category().contains("Racial"))
                .collect(Collectors.toList());
    }

    /**
     * Returns all unit keywords.
     */
    public List<SpecialAbility> getUnitKeywords() {
        return getSpecialAbilitiesLazy().stream()
                .filter(SpecialAbility::isUnitKeyword)
                .collect(Collectors.toList());
    }

    // ==================== Lore ====================

    public List<Lore> getLore() {
        return Collections.unmodifiableList(getLoreLazy());
    }

    public List<Lore> getLoreByCategory(String category) {
        return getLoreLazy().stream()
                .filter(l -> category.equals(l.category()))
                .collect(Collectors.toList());
    }

    /**
     * Returns all realms.
     */
    public List<Lore> getRealms() {
        return getLoreByCategory("realm");
    }

    /**
     * Returns all constellations.
     */
    public List<Lore> getConstellations() {
        return getLoreByCategory("constellation");
    }

    /**
     * Returns all Valar planets.
     */
    public List<Lore> getValarPlanets() {
        return getLoreByCategory("valar_planet");
    }

    /**
     * Returns all species.
     */
    public List<Lore> getSpecies() {
        return getLoreByCategory("species");
    }

    /**
     * Returns all key events.
     */
    public List<Lore> getEvents() {
        return getLoreByCategory("event");
    }

    /**
     * Returns Ring-related lore.
     */
    public List<Lore> getRingInfo() {
        return getLoreByCategory("ring");
    }

    /**
     * Returns all artifacts.
     */
    public List<Lore> getArtifacts() {
        return getLoreByCategory("artifact");
    }

    // ==================== Combat Tables ====================

    public Map<String, Map<String, String>> getGroundCombatTable() {
        return Collections.unmodifiableMap(getGroundCombatTableLazy());
    }

    public Map<String, Map<String, String>> getSpaceCombatTable() {
        return Collections.unmodifiableMap(getSpaceCombatTableLazy());
    }

    public Map<String, String> getCombatOutcomes() {
        return Collections.unmodifiableMap(getCombatOutcomesLazy());
    }

    /**
     * Resolves a ground combat roll using the ground combat table.
     */
    public String resolveGroundCombat(String oddsRatio, int dieRoll) {
        Map<String, String> row = getGroundCombatTableLazy().get(oddsRatio);
        if (row == null)
            throw new IllegalArgumentException("Unknown odds ratio: " + oddsRatio);
        String outcome = row.get(String.valueOf(dieRoll));
        if (outcome == null)
            throw new IllegalArgumentException("Invalid die roll: " + dieRoll);
        return outcome;
    }

    /**
     * Resolves a space combat roll using the space combat table.
     */
    public String resolveSpaceCombat(String oddsRatio, int dieRoll) {
        Map<String, String> row = getSpaceCombatTableLazy().get(oddsRatio);
        if (row == null)
            throw new IllegalArgumentException("Unknown odds ratio: " + oddsRatio);
        String outcome = row.get(String.valueOf(dieRoll));
        if (outcome == null)
            throw new IllegalArgumentException("Invalid die roll: " + dieRoll);
        return outcome;
    }

    /**
     * Calculates the odds ratio from attacker and defender strength.
     */
    public String calculateOddsRatio(int attackStrength, int defenseStrength) {
        if (defenseStrength <= 0)
            return "7:1+";
        double ratio = (double) attackStrength / defenseStrength;

        double[] thresholds = {0.167, 0.2, 0.25, 0.333, 0.5, 2.0, 3.0, 4.0, 5.0, 6.0};
        String[] labels = {"1:6", "1:5", "1:4", "1:3", "1:2", "2:1", "3:1", "4:1", "5:1", "6:1"};

        for (int i = thresholds.length - 1; i >= 0; i--) {
            if (ratio >= thresholds[i]) {
                return labels[i];
            }
        }
        return "1:6";
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

    private List<Unit> loadGroundUnits() {
        return parseUnits("ground_units");
    }

    private List<Unit> loadStarships() {
        return parseUnits("starships");
    }

    private List<Unit> loadGroundVehicles() {
        return parseUnits("ground_vehicles");
    }

    private List<Unit> loadCharacters() {
        return parseUnits("characters");
    }

    private List<Unit> parseUnits(String category) {
        JsonNode root = loadJson(RESOURCE_PREFIX + "units.json");
        JsonNode nodes = root.path("units").path(category);
        List<Unit> units = new ArrayList<>();
        for (JsonNode node : nodes) {
            UnitStats stats = parseStats(node.path("stats"));
            List<String> weapons = parseStringList(node.path("weapons"));
            List<String> abilities = parseStringList(node.path("abilities"));
            List<UnitVariant> variants = parseVariants(node.path("variants"));
            units.add(new Unit(
                    node.path("id").asText(),
                    node.path("name").asText(),
                    node.path("faction").asText(""),
                    node.path("type").asText(""),
                    node.path("species").asText(""),
                    node.path("description").asText(""),
                    stats,
                    weapons,
                    abilities,
                    variants
            ));
        }
        return units;
    }

    private UnitStats parseStats(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) return null;
        return new UnitStats(
                node.path("attack").asInt(0),
                node.path("defense").asInt(0),
                node.path("hull").asInt(0),
                node.path("shields").asInt(0),
                node.path("speed").asInt(0),
                node.has("wounds") ? node.path("wounds").asInt() : null,
                node.has("agility") ? node.path("agility").asInt() : null,
                node.has("force_power") ? node.path("force_power").asInt() : null
        );
    }

    private List<UnitVariant> parseVariants(JsonNode nodes) {
        List<UnitVariant> variants = new ArrayList<>();
        if (nodes.isMissingNode() || nodes.isNull()) return variants;
        for (JsonNode node : nodes) {
            variants.add(new UnitVariant(
                    node.path("name").asText(""),
                    node.has("specialty") ? node.path("specialty").asText("") : null
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

    private List<Faction> loadFactions() {
        JsonNode root = loadJson(RESOURCE_PREFIX + "factions.json");
        List<Faction> factions = new ArrayList<>();
        for (JsonNode node : root.path("factions")) {
            List<MilitaryBranch> branches = new ArrayList<>();
            if (!node.path("military_branches").isMissingNode()) {
                for (JsonNode mb : node.path("military_branches")) {
                    branches.add(new MilitaryBranch(
                            mb.path("name").asText(""),
                            mb.path("description").asText(""),
                            parseStringList(mb.path("unit_types"))
                    ));
                }
            }
            List<FactionLeader> leaders = new ArrayList<>();
            if (!node.path("key_leaders").isMissingNode()) {
                for (JsonNode fl : node.path("key_leaders")) {
                    leaders.add(new FactionLeader(
                            fl.path("name").asText(""),
                            fl.path("role").asText("")
                    ));
                }
            }
            factions.add(new Faction(
                    node.path("id").asText(),
                    node.path("name").asText(),
                    node.path("era").asText(""),
                    node.path("ideology").asText(""),
                    node.path("description").asText(""),
                    branches,
                    leaders,
                    parseStringList(node.path("strengths")),
                    parseStringList(node.path("weaknesses")),
                    node.has("home_world") ? node.path("home_world").asText("") : null,
                    parseStringList(node.path("colors")),
                    parseStringList(node.path("allies")),
                    parseStringList(node.path("enemies"))
            ));
        }
        return factions;
    }

    private List<Scenario> loadScenarios() {
        JsonNode root = loadJson(RESOURCE_PREFIX + "scenarios.json");
        List<Scenario> scenarios = new ArrayList<>();
        for (JsonNode node : root.path("scenarios")) {
            // Parse setup forces - LOTR uses player_1_forces and player_2_forces
            JsonNode setup = node.path("setup");
            List<String> player1Forces = new ArrayList<>();
            List<String> player2Forces = new ArrayList<>();
            if (!setup.isMissingNode()) {
                // Try LOTR-specific keys first
                player1Forces = parseStringList(setup.path("player_1_forces"));
                player2Forces = parseStringList(setup.path("player_2_forces"));
                // Fallback to Star Wars keys
                if (player1Forces.isEmpty()) {
                    player1Forces = parseStringList(setup.path("rebel_forces"));
                    if (player1Forces.isEmpty()) player1Forces = parseStringList(setup.path("republic_forces"));
                }
                if (player2Forces.isEmpty()) {
                    player2Forces = parseStringList(setup.path("imperial_forces"));
                    if (player2Forces.isEmpty()) player2Forces = parseStringList(setup.path("separatist_forces"));
                }
            }

            // Parse objectives
            JsonNode objective = node.path("objective");
            String player1Objective = "";
            String player2Objective = "";
            if (!objective.isMissingNode()) {
                player1Objective = objective.path("player_1").asText("");
                player2Objective = objective.path("player_2").asText("");
                // Fallback to Star Wars keys
                if (player1Objective.isEmpty()) player1Objective = objective.path("rebel").asText("");
                if (player2Objective.isEmpty()) player2Objective = objective.path("imperial").asText("");
            }

            // Parse victory conditions
            JsonNode victoryConditions = node.path("victory_conditions");
            Map<String, String> vicMap = new LinkedHashMap<>();
            if (!victoryConditions.isMissingNode()) {
                victoryConditions.fields().forEachRemaining(entry ->
                        vicMap.put(entry.getKey(), entry.getValue().asText()));
            }

            // Parse factions
            JsonNode factions = node.path("factions");
            String player1Faction = "";
            String player2Faction = "";
            if (!factions.isMissingNode()) {
                player1Faction = factions.path("player_1").asText("");
                player2Faction = factions.path("player_2").asText("");
            }

            // Parse backward compatible forces map
            Map<String, Integer> forces = new LinkedHashMap<>();
            if (!node.path("forces").isMissingNode()) {
                node.path("forces").fields().forEachRemaining(entry ->
                        forces.put(entry.getKey(), entry.getValue().asInt()));
            }

            scenarios.add(new Scenario(
                    node.path("id").asText(),
                    node.path("name").asText(),
                    node.path("description").asText(""),
                    node.path("era").asText(""),
                    node.path("location").asText(""),
                    node.path("difficulty").asText(""),
                    node.path("turns").asInt(6),
                    parseStringList(node.path("special_rules")),
                    forces,
                    node.has("victory_condition") ? node.path("victory_condition").asText("") : null,
                    node.has("defeat_condition") ? node.path("defeat_condition").asText("") : null,
                    node.path("map").asText(""),
                    node.path("players").asInt(2),
                    node.path("source_game").asText(""),
                    player1Faction,
                    player2Faction,
                    player1Objective,
                    player2Objective,
                    player1Forces,
                    player2Forces,
                    vicMap,
                    node.path("playtime").asText("")
            ));
        }
        return scenarios;
    }

    private List<SpecialAbility> loadSpecialAbilities() {
        JsonNode root = loadJson(RESOURCE_PREFIX + "special-abilities.json");
        List<SpecialAbility> abilities = new ArrayList<>();

        // Navigate to the nested structure: special_abilities.*
        JsonNode specialAbilitiesNode = root.path("special_abilities");
        if (specialAbilitiesNode.isMissingNode()) {
            return loadSpecialAbilitiesFlat(root);
        }

        // Ring powers: special_abilities.ring_powers
        JsonNode ringPowers = specialAbilitiesNode.path("ring_powers");
        if (!ringPowers.isMissingNode()) {
            // One Ring
            JsonNode oneRing = ringPowers.path("the_one_ring");
            if (!oneRing.isMissingNode()) {
                JsonNode effects = oneRing.path("effects");
                if (!effects.isMissingNode() && effects.isArray()) {
                    int idx = 0;
                    for (JsonNode effect : effects) {
                        abilities.add(new SpecialAbility(
                                "one_ring_" + idx++,
                                effect.path("name").asText("One Ring Power"),
                                "Ring Power",
                                "One Ring",
                                effect.path("description").asText(""),
                                "",
                                effect.path("effect").asText(""),
                                "",
                                List.of(),
                                "Ring Power"
                        ));
                    }
                }
            }
            // Three Elven Rings
            JsonNode threeRings = ringPowers.path("the_three_elven_rings");
            if (!threeRings.isMissingNode() && threeRings.isArray()) {
                for (JsonNode ring : threeRings) {
                    String ringName = ring.path("name").asText("");
                    JsonNode effects = ring.path("effects");
                    if (!effects.isMissingNode() && effects.isArray()) {
                        int idx = 0;
                        for (JsonNode effect : effects) {
                            abilities.add(new SpecialAbility(
                                    ringName.toLowerCase().replace(" ", "_") + "_" + idx++,
                                    effect.path("name").asText(ringName + " Power"),
                                    "Ring Power",
                                    ringName,
                                    effect.path("description").asText(""),
                                    "",
                                    effect.path("effect").asText(""),
                                    "",
                                    List.of(),
                                    "Ring Power"
                            ));
                        }
                    }
                }
            }
        }

        // Wizard abilities: special_abilities.wizard_abilities
        JsonNode wizardAbilities = specialAbilitiesNode.path("wizard_abilities");
        if (!wizardAbilities.isMissingNode() && wizardAbilities.isArray()) {
            for (JsonNode wizard : wizardAbilities) {
                String wizardName = wizard.path("name").asText("");
                JsonNode abilitiesNode = wizard.path("abilities");
                if (!abilitiesNode.isMissingNode() && abilitiesNode.isArray()) {
                    int idx = 0;
                    for (JsonNode abl : abilitiesNode) {
                        abilities.add(new SpecialAbility(
                                wizardName.toLowerCase().replace(" ", "_") + "_" + idx++,
                                abl.path("name").asText(wizardName + " Ability"),
                                "Wizard Ability",
                                wizardName,
                                abl.path("description").asText(""),
                                "",
                                abl.path("effect").asText(""),
                                "",
                                List.of(wizardName),
                                "Wizard Ability"
                        ));
                        idx++;
                    }
                }
            }
        }

        // Racial traits: special_abilities.racial_traits
        JsonNode racialTraits = specialAbilitiesNode.path("racial_traits");
        if (!racialTraits.isMissingNode() && racialTraits.isArray()) {
            for (JsonNode race : racialTraits) {
                String raceName = race.path("race").asText("");
                JsonNode traits = race.path("traits");
                if (!traits.isMissingNode() && traits.isArray()) {
                    int idx = 0;
                    for (JsonNode trait : traits) {
                        abilities.add(new SpecialAbility(
                                "racial_" + raceName.toLowerCase().replace(" ", "_") + "_" + idx++,
                                trait.path("name").asText(raceName + " Trait"),
                                "Racial Trait",
                                raceName,
                                trait.path("description").asText(""),
                                "",
                                trait.path("effect").asText(""),
                                "",
                                List.of(raceName),
                                "Racial Trait"
                        ));
                        idx++;
                    }
                }
            }
        }

        // Unit keywords: special_abilities.unit_keywords
        JsonNode keywordsNode = specialAbilitiesNode.path("unit_keywords");
        if (!keywordsNode.isMissingNode() && keywordsNode.isArray()) {
            for (JsonNode node : keywordsNode) {
                String name = node.path("name").asText();
                abilities.add(new SpecialAbility(
                        "keyword_" + name.toLowerCase().replace(" ", "_"),
                        name,
                        "Keyword",
                        "Neutral",
                        "",
                        "",
                        node.path("effect").asText(""),
                        "",
                        List.of(),
                        "Keyword"
                ));
            }
        }

        return abilities;
    }

    /**
     * Fallback parser for flat JSON structure.
     */
    private List<SpecialAbility> loadSpecialAbilitiesFlat(JsonNode root) {
        List<SpecialAbility> abilities = new ArrayList<>();
        // Simple fallback - try to parse any array at root level
        root.fields().forEachRemaining(entry -> {
            if (entry.getValue().isArray()) {
                int idx = 0;
                for (JsonNode node : entry.getValue()) {
                    abilities.add(new SpecialAbility(
                            entry.getKey() + "_" + idx++,
                            node.path("name").asText(""),
                            entry.getKey(),
                            "Neutral",
                            node.path("description").asText(""),
                            "",
                            "",
                            "",
                            List.of(),
                            ""
                    ));
                }
            }
        });
        return abilities;
    }

    private List<Lore> loadLore() {
        JsonNode root = loadJson(RESOURCE_PREFIX + "lore.json");
        List<Lore> lore = new ArrayList<>();

        // Navigate to the nested structure: lore.*
        JsonNode loreNode = root.path("lore");
        if (loreNode.isMissingNode()) {
            return loadLoreFlat(root);
        }

        // Realms: lore.realms[]
        if (!loreNode.path("realms").isMissingNode()) {
            for (JsonNode node : loreNode.path("realms")) {
                String name = node.path("name").asText();
                lore.add(new Lore(
                        "realm",
                        name.toLowerCase().replace(" ", "_"),
                        name,
                        node.path("description").asText(""),
                        List.of(),
                        node.path("region").asText(""),
                        "",
                        "",
                        parseStringList(node.path("notable_locations")),
                        parseStringList(node.path("inhabitants")),
                        node.path("significance").asText(""),
                        List.of(),
                        "",
                        "",
                        List.of(),
                        "",
                        Map.of()
                ));
            }
        }

        // Constellations: lore.constellations[]
        if (!loreNode.path("constellations").isMissingNode()) {
            for (JsonNode node : loreNode.path("constellations")) {
                String name = node.path("name").asText();
                lore.add(new Lore(
                        "constellation",
                        node.path("id").asText(),
                        name,
                        node.path("description").asText(""),
                        parseStringList(node.path("notable_features")),
                        node.path("real_world_equivalent").asText(""),
                        "",
                        "",
                        List.of(),
                        List.of(),
                        node.path("galaxy_role").asText(""),
                        List.of(),
                        "",
                        "",
                        List.of(),
                        "",
                        Map.of()
                ));
            }
        }

        // Valar Planets: lore.valar_planets[]
        if (!loreNode.path("valar_planets").isMissingNode()) {
            for (JsonNode node : loreNode.path("valar_planets")) {
                String name = node.path("name").asText();
                lore.add(new Lore(
                        "valar_planet",
                        node.path("id").asText(),
                        name,
                        node.path("description").asText(""),
                        parseStringList(node.path("notable_features")),
                        node.path("vala").asText(""),
                        "",
                        node.path("domain").asText(""),
                        List.of(),
                        List.of(),
                        node.path("galaxy_role").asText(""),
                        List.of(),
                        "",
                        "",
                        List.of(),
                        "",
                        Map.of()
                ));
            }
        }

        // Species: lore.species[]
        if (!loreNode.path("species").isMissingNode()) {
            for (JsonNode node : loreNode.path("species")) {
                String name = node.path("name").asText();
                lore.add(new Lore(
                        "species",
                        name.toLowerCase().replace(" ", "_"),
                        name,
                        node.path("description").asText(""),
                        List.of(),
                        "",
                        "",
                        node.path("homeworld").asText(""),
                        List.of(),
                        List.of(),
                        "",
                        List.of(),
                        "",
                        "",
                        parseStringList(node.path("notable_members")),
                        "",
                        Map.of()
                ));
            }
        }

        // Key Events: lore.key_events[]
        if (!loreNode.path("key_events").isMissingNode()) {
            for (JsonNode node : loreNode.path("key_events")) {
                String name = node.path("name").asText();
                lore.add(new Lore(
                        "event",
                        name.toLowerCase().replace(" ", "_"),
                        name,
                        node.path("description").asText(""),
                        List.of(),
                        "",
                        "",
                        "",
                        List.of(),
                        List.of(),
                        "",
                        List.of(),
                        "",
                        "",
                        List.of(),
                        node.path("date").asText(""),
                        Map.of()
                ));
            }
        }

        // The One Ring: lore.the_one_ring
        JsonNode oneRing = loreNode.path("the_one_ring");
        if (!oneRing.isMissingNode()) {
            Map<String, Object> ringData = new LinkedHashMap<>();
            ringData.put("description", oneRing.path("description").asText(""));
            ringData.put("creator", oneRing.path("creator").asText(""));
            ringData.put("inscription", oneRing.path("inscription").asText(""));
            lore.add(new Lore(
                    "ring",
                    "the_one_ring",
                    "The One Ring",
                    oneRing.path("description").asText(""),
                    List.of(),
                    "",
                    "",
                    "",
                    List.of(),
                    List.of(),
                    "",
                    List.of(),
                    "",
                    "",
                    List.of(),
                    "",
                    ringData
            ));
        }

        // The Three Elven Rings: lore.the_three_elven_rings
        JsonNode threeRings = loreNode.path("the_three_elven_rings");
        if (!threeRings.isMissingNode() && threeRings.isArray()) {
            for (JsonNode ring : threeRings) {
                String ringName = ring.path("name").asText("");
                Map<String, Object> ringData = new LinkedHashMap<>();
                ringData.put("description", ring.path("description").asText(""));
                ringData.put("bearer", ring.path("bearer").asText(""));
                ringData.put("element", ring.path("element").asText(""));
                lore.add(new Lore(
                        "ring",
                        ringName.toLowerCase().replace(" ", "_"),
                        ringName,
                        ring.path("description").asText(""),
                        List.of(),
                        "",
                        "",
                        ring.path("element").asText(""),
                        List.of(),
                        List.of(),
                        "",
                        List.of(),
                        "",
                        "",
                        List.of(ring.path("bearer").asText("")),
                        "",
                        ringData
                ));
            }
        }

        // Artifacts: lore.artifacts[]
        if (!loreNode.path("artifacts").isMissingNode()) {
            for (JsonNode node : loreNode.path("artifacts")) {
                String name = node.path("name").asText();
                lore.add(new Lore(
                        "artifact",
                        name.toLowerCase().replace(" ", "_"),
                        name,
                        node.path("description").asText(""),
                        List.of(),
                        "",
                        "",
                        "",
                        List.of(),
                        List.of(),
                        node.path("significance").asText(""),
                        List.of(),
                        "",
                        "",
                        List.of(),
                        "",
                        Map.of()
                ));
            }
        }

        return lore;
    }

    /**
     * Fallback parser for flat JSON structure.
     */
    private List<Lore> loadLoreFlat(JsonNode root) {
        List<Lore> lore = new ArrayList<>();
        if (!root.path("realms").isMissingNode()) {
            for (JsonNode node : root.path("realms")) {
                lore.add(new Lore(
                        "realm",
                        node.path("id").asText(),
                        node.path("name").asText(),
                        node.path("description").asText(""),
                        parseStringList(node.path("details")),
                        "", "", "", List.of(), List.of(), "",
                        List.of(), "", "", List.of(), "", Map.of()
                ));
            }
        }
        return lore;
    }

    private Map<String, Map<String, String>> loadCombatTable(String filename, String tableName) {
        try {
            JsonNode root = loadJson(filename);
            JsonNode table = root.path(tableName).path("table");
            if (table.isMissingNode() || table.isNull()) {
                return Collections.emptyMap();
            }
            Map<String, Map<String, String>> result = new LinkedHashMap<>();
            table.fields().forEachRemaining(oddsEntry -> {
                Map<String, String> outcomes = new LinkedHashMap<>();
                oddsEntry.getValue().fields().forEachRemaining(dieEntry ->
                        outcomes.put(dieEntry.getKey(), dieEntry.getValue().asText()));
                result.put(oddsEntry.getKey(), outcomes);
            });
            return result;
        } catch (IllegalStateException e) {
            return Collections.emptyMap();
        }
    }

    private Map<String, String> loadCombatOutcomes() {
        try {
            JsonNode root = loadJson(RESOURCE_PREFIX + "combat-rules.json");
            JsonNode outcomes = root.path("ground_combat").path("attack_rules");
            Map<String, String> map = new LinkedHashMap<>();
            if (!outcomes.isMissingNode()) {
                outcomes.fields().forEachRemaining(entry -> map.put(entry.getKey(), entry.getValue().asText()));
            }
            return map;
        } catch (IllegalStateException e) {
            return Collections.emptyMap();
        }
    }
}
