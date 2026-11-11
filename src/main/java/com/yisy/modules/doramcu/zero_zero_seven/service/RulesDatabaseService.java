package com.yisy.modules.doramcu.zero_zero_seven.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

/**
 * Provides access to game rules and data from the lore database.
 */
@Service
public class RulesDatabaseService {

    private final BondLoreDatabase loreDatabase;

    public RulesDatabaseService(BondLoreDatabase loreDatabase) {
        this.loreDatabase = loreDatabase;
    }

    /**
     * Get empire data.
     *
     * @return empire JSON data
     */
    public JsonNode getEmpireData() {
        return loreDatabase.getData("empires");
    }

    /**
     * Get operation data.
     *
     * @return operation JSON data
     */
    public JsonNode getOperationData() {
        return loreDatabase.getData("operations");
    }

    /**
     * Get technology data.
     *
     * @return technology JSON data
     */
    public JsonNode getTechnologyData() {
        return loreDatabase.getData("technologies");
    }

    /**
     * Get mission data.
     *
     * @return mission JSON data
     */
    public JsonNode getMissionData() {
        return loreDatabase.getData("missions");
    }
}
