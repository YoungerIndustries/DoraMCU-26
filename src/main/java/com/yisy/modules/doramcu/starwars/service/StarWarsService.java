package com.yisy.modules.doramcu.starwars.service;
import com.yisy.modules.doramcu.model.players.Player;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.starwars.model.Faction;
import com.yisy.modules.doramcu.starwars.model.FactionLeader;
import com.yisy.modules.doramcu.starwars.model.Lore;
import com.yisy.modules.doramcu.starwars.model.MilitaryBranch;
import com.yisy.modules.doramcu.starwars.model.Scenario;
import com.yisy.modules.doramcu.starwars.model.SpecialAbility;
import com.yisy.modules.doramcu.starwars.model.Unit;
import com.yisy.modules.doramcu.starwars.model.UnitStats;
import com.yisy.modules.doramcu.starwars.model.UnitVariant;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Service for loading and querying the Star Wars rules database.
 * <p>
 * Loads all JSON data files from the {@code star-wars-rules/} classpath resource
 * and provides typed access to units, factions, scenarios, special abilities, combat tables, and lore.
 * </p>
 */
@Service
public class StarWarsService {

    private static final String RESOURCE_PREFIX = "star-wars-rules/";

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

    public StarWarsService() {
        this(new ObjectMapper());
    }

    StarWarsService(ObjectMapper objectMapper) {
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
     * Useful for memory management in long-running applications.
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
     * Returns Force-sensitive characters (forcePower > 0).
     */
    public List<Unit> getForceUsers() {
        return getCharactersLazy().stream()
                .filter(u -> u.stats() != null && u.stats().forcePower() != null && u.stats().forcePower() > 0)
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

    public List<SpecialAbility> getLightSidePowers() {
        return getSpecialAbilitiesLazy().stream()
                .filter(SpecialAbility::isLightSide)
                .collect(Collectors.toList());
    }

    public List<SpecialAbility> getDarkSidePowers() {
        return getSpecialAbilitiesLazy().stream()
                .filter(SpecialAbility::isDarkSide)
                .collect(Collectors.toList());
    }

    /**
     * Returns all leader abilities.
     */
    public List<SpecialAbility> getLeaderAbilities() {
        return getSpecialAbilitiesLazy().stream()
                .filter(SpecialAbility::isLeaderAbility)
                .collect(Collectors.toList());
    }

    /**
     * Returns leader abilities for a specific faction.
     */
    public List<SpecialAbility> getLeaderAbilitiesByFaction(String faction) {
        return getLeaderAbilities().stream()
                .filter(a -> a.side().equalsIgnoreCase(faction))
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
     * Returns all planets.
     */
    public List<Lore> getPlanets() {
        return getLoreByCategory("planet");
    }

    /**
     * Returns all eras.
     */
    public List<Lore> getEras() {
        return getLoreByCategory("era");
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
     * Returns Force-related lore (abilities, philosophy, midichlorians).
     */
    public List<Lore> getForceInfo() {
        return getLoreByCategory("force");
    }

    /**
     * Returns lightsaber information.
     */
    public List<Lore> getLightsaberInfo() {
        return getLoreByCategory("lightsaber");
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
        // ratio >= 7.0 is unreachable — already caught by ratio >= 6.0 in the loop above
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
                    node.path("era").asText(""),
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
            // Parse setup forces
            JsonNode setup = node.path("setup");
            List<String> rebelForces = new ArrayList<>();
            List<String> imperialForces = new ArrayList<>();
            if (!setup.isMissingNode()) {
                rebelForces = parseStringList(setup.path("rebel_forces"));
                imperialForces = parseStringList(setup.path("imperial_forces"));
                // Try generic force keys if rebel/imperial not found
                if (rebelForces.isEmpty()) {
                    JsonNode repForces = setup.path("republic_forces");
                    if (!repForces.isMissingNode()) rebelForces = parseStringList(repForces);
                }
                if (imperialForces.isEmpty()) {
                    JsonNode sepForces = setup.path("separatist_forces");
                    if (!sepForces.isMissingNode()) imperialForces = parseStringList(sepForces);
                }
                // Han Solo scenario
                if (rebelForces.isEmpty()) {
                    JsonNode hanForces = setup.path("han_solo_forces");
                    if (!hanForces.isMissingNode()) rebelForces = parseStringList(hanForces);
                }
                // Resistance scenario
                if (rebelForces.isEmpty()) {
                    JsonNode resForces = setup.path("resistance_forces");
                    if (!resForces.isMissingNode()) rebelForces = parseStringList(resForces);
                }
                if (imperialForces.isEmpty()) {
                    JsonNode foForces = setup.path("first_order_forces");
                    if (!foForces.isMissingNode()) imperialForces = parseStringList(foForces);
                }
                // Naboo scenario
                if (rebelForces.isEmpty()) {
                    JsonNode nabForces = setup.path("naboo_forces");
                    if (!nabForces.isMissingNode()) rebelForces = parseStringList(nabForces);
                }
                if (imperialForces.isEmpty()) {
                    JsonNode tfForces = setup.path("trade_federation_forces");
                    if (!tfForces.isMissingNode()) imperialForces = parseStringList(tfForces);
                }
                // Obi-Wan scenario
                if (rebelForces.isEmpty()) {
                    JsonNode owForces = setup.path("obi_wan_forces");
                    if (!owForces.isMissingNode()) rebelForces = parseStringList(owForces);
                }
                if (imperialForces.isEmpty()) {
                    JsonNode dvForces = setup.path("darth_vader_forces");
                    if (!dvForces.isMissingNode()) imperialForces = parseStringList(dvForces);
                }
            }

            // Parse objectives
            JsonNode objective = node.path("objective");
            String rebelObjective = "";
            String imperialObjective = "";
            if (!objective.isMissingNode()) {
                rebelObjective = objective.path("rebel").asText("");
                imperialObjective = objective.path("imperial").asText("");
                // Try other objective keys
                if (rebelObjective.isEmpty()) {
                    rebelObjective = objective.path("republic").asText("");
                }
                if (rebelObjective.isEmpty()) {
                    rebelObjective = objective.path("han_solo").asText("");
                }
                if (rebelObjective.isEmpty()) {
                    rebelObjective = objective.path("resistance").asText("");
                }
                if (rebelObjective.isEmpty()) {
                    rebelObjective = objective.path("naboo").asText("");
                }
                if (rebelObjective.isEmpty()) {
                    rebelObjective = objective.path("obi_wan").asText("");
                }
                if (imperialObjective.isEmpty()) {
                    imperialObjective = objective.path("separatist").asText("");
                }
                if (imperialObjective.isEmpty()) {
                    imperialObjective = objective.path("first_order").asText("");
                }
                if (imperialObjective.isEmpty()) {
                    imperialObjective = objective.path("trade_federation").asText("");
                }
                if (imperialObjective.isEmpty()) {
                    imperialObjective = objective.path("darth_vader").asText("");
                }
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
                    rebelObjective,
                    imperialObjective,
                    rebelForces,
                    imperialForces,
                    vicMap,
                    node.path("playtime").asText("")
            ));
        }
        return scenarios;
    }

    private List<SpecialAbility> loadSpecialAbilities() {
        JsonNode root = loadJson(RESOURCE_PREFIX + "special-abilities.json");
        List<SpecialAbility> abilities = new ArrayList<>();

        // Navigate to the nested structure: special_abilities.force_powers
        JsonNode specialAbilitiesNode = root.path("special_abilities");
        if (specialAbilitiesNode.isMissingNode()) {
            // Fallback: try flat structure
            return loadSpecialAbilitiesFlat(root);
        }

        JsonNode forcePowers = specialAbilitiesNode.path("force_powers");

        // Light Side powers: special_abilities.force_powers.light_side_powers[]
        if (!forcePowers.path("light_side_powers").isMissingNode()) {
            int idx = 0;
            for (JsonNode node : forcePowers.path("light_side_powers")) {
                String id = node.path("id").asText("");
                if (id.isEmpty()) id = "light_force_" + idx++;
                String rankStr = node.path("rank").isTextual()
                        ? node.path("rank").asText()
                        : String.valueOf(node.path("rank").asInt(1));
                abilities.add(new SpecialAbility(
                        id,
                        node.path("name").asText(),
                        "Force Power",
                        "Light Side",
                        node.path("description").asText(""),
                        node.path("cost").asText(""),
                        node.path("effect").asText(""),
                        rankStr,
                        parseStringList(node.path("users")),
                        node.path("type").asText("Force Power")
                ));
            }
        }

        // Dark Side powers: special_abilities.force_powers.dark_side_powers[]
        if (!forcePowers.path("dark_side_powers").isMissingNode()) {
            int idx = 0;
            for (JsonNode node : forcePowers.path("dark_side_powers")) {
                String id = node.path("id").asText("");
                if (id.isEmpty()) id = "dark_force_" + idx++;
                String rankStr = node.path("rank").isTextual()
                        ? node.path("rank").asText()
                        : String.valueOf(node.path("rank").asInt(1));
                abilities.add(new SpecialAbility(
                        id,
                        node.path("name").asText(),
                        "Force Power",
                        "Dark Side",
                        node.path("description").asText(""),
                        node.path("cost").asText(""),
                        node.path("effect").asText(""),
                        rankStr,
                        parseStringList(node.path("users")),
                        node.path("type").asText("Force Power")
                ));
            }
        }

        // Leader abilities: special_abilities.leader_abilities.<faction>[]
        JsonNode leaderAbilitiesNode = specialAbilitiesNode.path("leader_abilities");
        if (!leaderAbilitiesNode.isMissingNode()) {
            leaderAbilitiesNode.fields().forEachRemaining(factionEntry -> {
                String faction = factionEntry.getKey();
                JsonNode leaders = factionEntry.getValue();
                if (leaders.isArray()) {
                    int leaderIdx = 0;
                    for (JsonNode leaderNode : leaders) {
                        String leaderName = leaderNode.path("leader").asText("");
                        JsonNode ablNode = leaderNode.path("abilities");
                        if (!ablNode.isMissingNode() && ablNode.isArray()) {
                            int ablIdx = 0;
                            for (JsonNode abl : ablNode) {
                                String id = leaderName.toLowerCase().replace(" ", "_")
                                        + "_" + abl.path("name").asText("").toLowerCase().replace(" ", "_");
                                id = id.replaceAll("[^a-z0-9_]", "_");
                                abilities.add(new SpecialAbility(
                                        id,
                                        abl.path("name").asText(),
                                        "Leader Ability",
                                        faction,
                                        "",
                                        "",
                                        abl.path("effect").asText(""),
                                        "",
                                        List.of(leaderName),
                                        abl.path("type").asText("")
                                ));
                                ablIdx++;
                            }
                        }
                        leaderIdx++;
                    }
                }
            });
        }

        // Unit keywords: special_abilities.unit_keywords.keywords[]
        JsonNode keywordsNode = specialAbilitiesNode.path("unit_keywords").path("keywords");
        if (!keywordsNode.isMissingNode() && keywordsNode.isArray()) {
            int idx = 0;
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
                idx++;
            }
        }

        return abilities;
    }

    /**
     * Fallback parser for flat JSON structure (backward compatibility).
     */
    private List<SpecialAbility> loadSpecialAbilitiesFlat(JsonNode root) {
        List<SpecialAbility> abilities = new ArrayList<>();

        if (!root.path("light_side_powers").isMissingNode()) {
            for (JsonNode node : root.path("light_side_powers")) {
                abilities.add(new SpecialAbility(
                        node.path("id").asText(),
                        node.path("name").asText(),
                        node.path("category").asText("Force Power"),
                        "Light Side",
                        node.path("description").asText(""),
                        "",
                        "",
                        String.valueOf(node.path("rank").asInt(1)),
                        List.of(),
                        ""
                ));
            }
        }

        if (!root.path("dark_side_powers").isMissingNode()) {
            for (JsonNode node : root.path("dark_side_powers")) {
                abilities.add(new SpecialAbility(
                        node.path("id").asText(),
                        node.path("name").asText(),
                        node.path("category").asText("Force Power"),
                        "Dark Side",
                        node.path("description").asText(""),
                        "",
                        "",
                        String.valueOf(node.path("rank").asInt(1)),
                        List.of(),
                        ""
                ));
            }
        }

        if (!root.path("leader_abilities").isMissingNode()) {
            for (JsonNode node : root.path("leader_abilities")) {
                abilities.add(new SpecialAbility(
                        node.path("id").asText(),
                        node.path("name").asText(),
                        node.path("category").asText("Leader Ability"),
                        node.has("side") ? node.path("side").asText("") : "Neutral",
                        node.path("description").asText(""),
                        "",
                        "",
                        String.valueOf(node.path("rank").asInt(1)),
                        List.of(),
                        ""
                ));
            }
        }

        return abilities;
    }

    private List<Lore> loadLore() {
        JsonNode root = loadJson(RESOURCE_PREFIX + "lore.json");
        List<Lore> lore = new ArrayList<>();

        // Navigate to the nested structure: lore.*
        JsonNode loreNode = root.path("lore");
        if (loreNode.isMissingNode()) {
            // Fallback: try flat structure
            return loadLoreFlat(root);
        }

        // Planets: lore.planets[]
        if (!loreNode.path("planets").isMissingNode()) {
            for (JsonNode node : loreNode.path("planets")) {
                String name = node.path("name").asText();
                lore.add(new Lore(
                        "planet",
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

        // Eras: lore.eras[]
        if (!loreNode.path("eras").isMissingNode()) {
            for (JsonNode node : loreNode.path("eras")) {
                String name = node.path("name").asText();
                lore.add(new Lore(
                        "era",
                        name.toLowerCase().replace(" ", "_"),
                        name,
                        node.path("description").asText(""),
                        List.of(),
                        "",
                        node.path("period").asText(""),
                        "",
                        List.of(),
                        List.of(),
                        "",
                        parseStringList(node.path("key_events")),
                        node.path("jedi_status").asText(""),
                        node.path("sith_status").asText(""),
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

        // The Force: lore.the_force
        JsonNode theForce = loreNode.path("the_force");
        if (!theForce.isMissingNode()) {
            // Force abilities
            JsonNode forceAbilities = theForce.path("force_abilities");
            if (!forceAbilities.isMissingNode()) {
                Map<String, Object> forceData = new LinkedHashMap<>();
                forceAbilities.fields().forEachRemaining(entry ->
                        forceData.put(entry.getKey(), entry.getValue().asText()));
                lore.add(new Lore(
                        "force",
                        "force_abilities",
                        "Force Abilities",
                        theForce.path("description").asText(""),
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
                        forceData
                ));
            }

            // Midichlorians
            JsonNode midichlorians = theForce.path("midichlorians");
            if (!midichlorians.isMissingNode()) {
                Map<String, Object> midiData = new LinkedHashMap<>();
                midiData.put("description", midichlorians.path("description").asText(""));
                JsonNode counts = midichlorians.path("notable_counts");
                if (!counts.isMissingNode()) {
                    Map<String, Integer> countMap = new LinkedHashMap<>();
                    counts.fields().forEachRemaining(entry ->
                            countMap.put(entry.getKey(), entry.getValue().asInt()));
                    midiData.put("notable_counts", countMap);
                }
                lore.add(new Lore(
                        "force",
                        "midichlorians",
                        "Midichlorians",
                        midichlorians.path("description").asText(""),
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
                        midiData
                ));
            }

            // Light Side philosophy
            JsonNode lightSide = theForce.path("philosophy").path("light_side");
            if (!lightSide.isMissingNode()) {
                Map<String, Object> lightData = new LinkedHashMap<>();
                lightData.put("tenets", parseStringList(lightSide.path("tenets")));
                lightData.put("practices", parseStringList(lightSide.path("practices")));
                lore.add(new Lore(
                        "force",
                        "light_side_philosophy",
                        "Light Side Philosophy",
                        lightSide.path("description").asText(""),
                        parseStringList(lightSide.path("tenets")),
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
                        lightData
                ));
            }

            // Dark Side philosophy
            JsonNode darkSide = theForce.path("philosophy").path("dark_side");
            if (!darkSide.isMissingNode()) {
                Map<String, Object> darkData = new LinkedHashMap<>();
                darkData.put("tenets", parseStringList(darkSide.path("tenets")));
                darkData.put("practices", parseStringList(darkSide.path("practices")));
                darkData.put("warning", darkSide.path("warning").asText(""));
                lore.add(new Lore(
                        "force",
                        "dark_side_philosophy",
                        "Dark Side Philosophy",
                        darkSide.path("description").asText(""),
                        parseStringList(darkSide.path("tenets")),
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
                        darkData
                ));
            }
        }

        // Lightsabers: lore.lightsabers
        JsonNode lightsabers = loreNode.path("lightsabers");
        if (!lightsabers.isMissingNode()) {
            Map<String, Object> lsData = new LinkedHashMap<>();
            lsData.put("description", lightsabers.path("description").asText(""));

            // Kyber crystal colors
            JsonNode colors = lightsabers.path("kyber_crystals").path("colors");
            if (!colors.isMissingNode()) {
                Map<String, String> colorMap = new LinkedHashMap<>();
                colors.fields().forEachRemaining(entry ->
                        colorMap.put(entry.getKey(), entry.getValue().asText()));
                lsData.put("colors", colorMap);
            }

            // Darksaber
            JsonNode darksaber = lightsabers.path("darksaber");
            if (!darksaber.isMissingNode()) {
                Map<String, Object> dsData = new LinkedHashMap<>();
                dsData.put("description", darksaber.path("description").asText(""));
                dsData.put("notable_wielders", parseStringList(darksaber.path("notable_wielders")));
                lsData.put("darksaber", dsData);
            }

            lore.add(new Lore(
                    "lightsaber",
                    "lightsabers",
                    "Lightsabers",
                    lightsabers.path("description").asText(""),
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
                    lsData
            ));
        }

        return lore;
    }

    /**
     * Fallback parser for flat JSON structure (backward compatibility).
     */
    private List<Lore> loadLoreFlat(JsonNode root) {
        List<Lore> lore = new ArrayList<>();

        if (!root.path("planets").isMissingNode()) {
            for (JsonNode node : root.path("planets")) {
                lore.add(new Lore(
                        "planet",
                        node.path("id").asText(),
                        node.path("name").asText(),
                        node.path("description").asText(""),
                        parseStringList(node.path("details")),
                        "", "", "", List.of(), List.of(), "",
                        List.of(), "", "", List.of(), "", Map.of()
                ));
            }
        }

        if (!root.path("eras").isMissingNode()) {
            for (JsonNode node : root.path("eras")) {
                lore.add(new Lore(
                        "era",
                        node.path("id").asText(),
                        node.path("name").asText(),
                        node.path("description").asText(""),
                        parseStringList(node.path("details")),
                        "", "", "", List.of(), List.of(), "",
                        List.of(), "", "", List.of(), "", Map.of()
                ));
            }
        }

        if (!root.path("species").isMissingNode()) {
            for (JsonNode node : root.path("species")) {
                lore.add(new Lore(
                        "species",
                        node.path("id").asText(),
                        node.path("name").asText(),
                        node.path("description").asText(""),
                        parseStringList(node.path("details")),
                        "", "", "", List.of(), List.of(), "",
                        List.of(), "", "", List.of(), "", Map.of()
                ));
            }
        }

        if (!root.path("events").isMissingNode()) {
            for (JsonNode node : root.path("events")) {
                lore.add(new Lore(
                        "event",
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
            // Combat table may not exist; return empty map
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
