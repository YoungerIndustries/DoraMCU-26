package com.yisy.modules.doramcu.sfb.dto;

import java.util.Map;

/**
 * Client -> Server action request for SFB combat.
 */
public class SfbActionRequest {
    public enum ActionType {
        ENERGY_ALLOCATE, MOVE, FIRE, SPECIAL_SYSTEM
    }

    private String scenarioId;
    private String shipId;
    private ActionType type;
    private Map<String, Object> parameters;

    public String getScenarioId() { return scenarioId; }
    public void setScenarioId(String scenarioId) { this.scenarioId = scenarioId; }
    public String getShipId() { return shipId; }
    public void setShipId(String shipId) { this.shipId = shipId; }
    public ActionType getType() { return type; }
    public void setType(ActionType type) { this.type = type; }
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
}
