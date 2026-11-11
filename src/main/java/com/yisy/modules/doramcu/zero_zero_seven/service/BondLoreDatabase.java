package com.yisy.modules.doramcu.zero_zero_seven.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads JSON data from docs/zero-zero-seven-rules-db/ for game rules and content.
 * <p>
 * Provides in-memory access to empires, operations, technologies, and missions data.
 * </p>
 */
@Component
public class BondLoreDatabase {

    private final ObjectMapper objectMapper;
    private final Map<String, JsonNode> dataCache;

    public BondLoreDatabase(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.dataCache = new HashMap<>();
        loadDefaultData();
    }

    /**
     * Load default data into the cache.
     */
    private void loadDefaultData() {
        // Default empire data
        ObjectNode empires = objectMapper.createObjectNode();
        empires.putArray("western").add("United States").add("United Kingdom").add("France")
                .add("West Germany").add("Japan").add("Italy").add("Australia");
        empires.putArray("soviet").add("Soviet Union").add("China").add("East Germany");
        empires.putArray("non_aligned").add("India").add("Brazil");
        dataCache.put("empires", empires);

        // Default operation data
        ObjectNode operations = objectMapper.createObjectNode();
        operations.putObject("sabotage").put("description", "Destroy enemy infrastructure")
                .put("baseDifficulty", 5).put("rewardMultiplier", 10);
        operations.putObject("assassination").put("description", "Eliminate key personnel")
                .put("baseDifficulty", 8).put("rewardMultiplier", 20);
        operations.putObject("intel_gathering").put("description", "Collect intelligence")
                .put("baseDifficulty", 3).put("rewardMultiplier", 15);
        operations.putObject("propaganda").put("description", "Influence public opinion")
                .put("baseDifficulty", 4).put("rewardMultiplier", 8);
        operations.putObject("infiltration").put("description", "Infiltrate enemy organizations")
                .put("baseDifficulty", 6).put("rewardMultiplier", 18);
        dataCache.put("operations", operations);

        // Default technology data
        ObjectNode technologies = objectMapper.createObjectNode();
        technologies.putArray("nuclear").add("Atomic Bomb").add("Hydrogen Bomb").add("Nuclear Submarine");
        technologies.putArray("military").add("Jet Aircraft").add("Missile Systems").add("Radar");
        technologies.putArray("intelligence").add("Cryptography").add("Surveillance").add("Signals Intelligence");
        technologies.putArray("communications").add("Satellite").add("Radio Network").add("Undersea Cable");
        dataCache.put("technologies", technologies);

        // Default mission data
        ObjectNode missions = objectMapper.createObjectNode();
        missions.putObject("recruit").put("description", "Recruit a new agent")
                .put("cost", 10).put("time", 1);
        missions.putObject("establish_network").put("description", "Set up a spy network")
                .put("cost", 20).put("time", 2);
        missions.putObject("upgrade_network").put("description", "Upgrade an existing network")
                .put("cost", 15).put("time", 1);
        dataCache.put("missions", missions);
    }

    /**
     * Get data by key.
     *
     * @param key the data key
     * @return the JSON node
     */
    public JsonNode getData(String key) {
        return dataCache.getOrDefault(key, objectMapper.createObjectNode());
    }

    /**
     * Put data by key.
     *
     * @param key the data key
     * @param data the JSON node
     */
    public void putData(String key, JsonNode data) {
        dataCache.put(key, data);
    }
}
