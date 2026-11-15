package com.yisy.modules.doramcu.warhammer40k.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.warhammer40k.model.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Service for loading and querying the Warhammer 40K rules database.
 * <p>
 * Loads all JSON data files from the {@code warhammer40k-rules/} classpath resource
 * and provides typed access to units, factions, scenarios, and lore.
 * </p>
 */
@Service
public class Warhammer40kService {

    private static final String RESOURCE_PREFIX = "warhammer40k-rules/";

    private final ObjectMapper objectMapper;
    
    private final AtomicReference<List<Unit>> unitsRef = new AtomicReference<>();
    private final AtomicReference<List<Faction>> factionsRef = new AtomicReference<>();
    private final AtomicReference<List<Scenario>> scenariosRef = new AtomicReference<>();
    private final AtomicReference<Lore> loreRef = new AtomicReference<>();
    private final AtomicReference<Map<String, Object>> combatRulesRef = new AtomicReference<>();

    public Warhammer40kService() {
        this.objectMapper = new ObjectMapper();
    }

    public List<Unit> getUnits() {
        List<Unit> units = unitsRef.get();
        if (units == null) {
            synchronized (unitsRef) {
                units = unitsRef.get();
                if (units == null) {
                    units = loadUnits();
                    unitsRef.set(units);
                }
            }
        }
        return Collections.unmodifiableList(units);
    }

    public List<Unit> getUnitsByFaction(String factionId) {
        return getUnits().stream()
                .filter(u -> factionId.equals(u.faction()))
                .collect(Collectors.toList());
    }

    public Optional<Unit> getUnit(String unitId) {
        return getUnits().stream()
                .filter(u -> unitId.equals(u.id()))
                .findFirst();
    }

    public List<Unit> getInfantry() {
        return getUnits().stream()
                .filter(Unit::isInfantry)
                .collect(Collectors.toList());
    }

    public List<Unit> getVehicles() {
        return getUnits().stream()
                .filter(Unit::isVehicle)
                .collect(Collectors.toList());
    }

    public List<Faction> getFactions() {
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
        return Collections.unmodifiableList(factions);
    }

    public Optional<Faction> getFaction(String factionId) {
        return getFactions().stream()
                .filter(f -> factionId.equals(f.id()))
                .findFirst();
    }

    public List<Scenario> getScenarios() {
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
        return Collections.unmodifiableList(scenarios);
    }

    public Optional<Scenario> getScenario(String scenarioId) {
        return getScenarios().stream()
                .filter(s -> scenarioId.equals(s.id()))
                .findFirst();
    }

    public Lore getLore() {
        Lore lore = loreRef.get();
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

    public Map<String, Object> getCombatRules() {
        Map<String, Object> rules = combatRulesRef.get();
        if (rules == null) {
            synchronized (combatRulesRef) {
                rules = combatRulesRef.get();
                if (rules == null) {
                    rules = loadCombatRules();
                    combatRulesRef.set(rules);
                }
            }
        }
        return Collections.unmodifiableMap(rules);
    }

    private List<Unit> loadUnits() {
        try {
            JsonNode root = loadJson(RESOURCE_PREFIX + "units.json");
            JsonNode units = root.path("units");
            if (units.isMissingNode() || units.isNull()) {
                return Collections.emptyList();
            }
            List<Unit> result = new ArrayList<>();
            units.forEach(unitNode -> {
                UnitStats stats = mapToValue(unitNode.path("stats"), UnitStats.class);
                List<String> weapons = mapToList(unitNode.path("weapons"));
                List<String> abilities = mapToList(unitNode.path("abilities"));
                List<String> variants = mapToList(unitNode.path("variants"));
                result.add(new Unit(
                        unitNode.path("id").asText(),
                        unitNode.path("name").asText(),
                        unitNode.path("faction").asText(),
                        unitNode.path("type").asText(),
                        unitNode.path("era").asText(),
                        unitNode.path("description").asText(),
                        stats,
                        weapons,
                        abilities,
                        variants
                ));
            });
            return result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<Faction> loadFactions() {
        try {
            JsonNode root = loadJson(RESOURCE_PREFIX + "factions.json");
            JsonNode factions = root.path("factions");
            if (factions.isMissingNode() || factions.isNull()) {
                return Collections.emptyList();
            }
            List<Faction> result = new ArrayList<>();
            factions.forEach(factionNode -> {
                result.add(new Faction(
                        factionNode.path("id").asText(),
                        factionNode.path("name").asText(),
                        factionNode.path("era").asText(),
                        factionNode.path("ideology").asText(),
                        factionNode.path("description").asText(),
                        mapToList(factionNode.path("military_branches")),
                        mapToList(factionNode.path("key_leaders")),
                        mapToList(factionNode.path("strengths")),
                        mapToList(factionNode.path("weaknesses")),
                        factionNode.path("home_world").asText(),
                        mapToList(factionNode.path("allies")),
                        mapToList(factionNode.path("enemies"))
                ));
            });
            return result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Scenario> loadScenarios() {
        try {
            JsonNode root = loadJson(RESOURCE_PREFIX + "scenarios.json");
            JsonNode scenarios = root.path("scenarios");
            if (scenarios.isMissingNode() || scenarios.isNull()) {
                return Collections.emptyList();
            }
            List<Scenario> result = new ArrayList<>();
            scenarios.forEach(scenarioNode -> {
                Map<String, List<String>> units = new LinkedHashMap<>();
                JsonNode unitsNode = scenarioNode.path("units");
                if (!unitsNode.isMissingNode()) {
                    unitsNode.fields().forEachRemaining(entry -> {
                        List<String> unitList = mapToList(entry.getValue());
                        units.put(entry.getKey(), unitList);
                    });
                }
                result.add(new Scenario(
                        scenarioNode.path("id").asText(),
                        scenarioNode.path("name").asText(),
                        scenarioNode.path("description").asText(),
                        mapToList(scenarioNode.path("factions")),
                        scenarioNode.path("difficulty").asInt(),
                        mapToList(scenarioNode.path("objectives")),
                        mapToList(scenarioNode.path("terrain")),
                        units
                ));
            });
            return result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private Lore loadLore() {
        try {
            JsonNode root = loadJson(RESOURCE_PREFIX + "lore.json");
            JsonNode lore = root.path("lore");
            if (lore.isMissingNode()) {
                return null;
            }
            JsonNode universe = lore.path("universe");
            
            Map<String, Map<String, Object>> factions = new LinkedHashMap<>();
            JsonNode factionsNode = lore.path("factions");
            if (!factionsNode.isMissingNode()) {
                factionsNode.fields().forEachRemaining(entry -> {
                    Map<String, Object> factionData = new LinkedHashMap<>();
                    entry.getValue().fields().forEachRemaining(f -> {
                        factionData.put(f.getKey(), f.getValue().isValueNode() ? f.getValue().asText() : f.getValue());
                    });
                    factions.put(entry.getKey(), factionData);
                });
            }

            List<Lore.Era> eras = new ArrayList<>();
            JsonNode erasNode = lore.path("eras");
            if (!erasNode.isMissingNode()) {
                erasNode.forEach(eraNode -> {
                    eras.add(new Lore.Era(
                            eraNode.path("name").asText(),
                            eraNode.path("period").asText(),
                            eraNode.path("description").asText(),
                            mapToList(eraNode.path("key_events"))
                    ));
                });
            }

            return new Lore(
                    universe.path("title").asText(),
                    universe.path("description").asText(),
                    mapToList(universe.path("source_material")),
                    factions,
                    eras
            );
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadCombatRules() {
        try {
            JsonNode root = loadJson(RESOURCE_PREFIX + "combat-rules.json");
            Map<String, Object> result = new LinkedHashMap<>();
            root.fields().forEachRemaining(entry -> {
                result.put(entry.getKey(), objectMapper.convertValue(entry.getValue(), Object.class));
            });
            return result;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private JsonNode loadJson(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: " + path);
            }
            return objectMapper.readTree(is);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + path, e);
        }
    }

    private List<String> mapToList(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        node.forEach(n -> result.add(n.asText()));
        return result;
    }

    private <T> T mapToValue(JsonNode node, Class<T> type) {
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        return objectMapper.convertValue(node, type);
    }
}
