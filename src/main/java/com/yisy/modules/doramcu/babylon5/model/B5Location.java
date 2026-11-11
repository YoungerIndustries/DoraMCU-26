package com.yisy.modules.doramcu.babylon5.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a location in the Babylon 5 universe (station, planet, sector, etc.).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class B5Location {

    private String id;
    private String name;
    private String description;
    private String type;
    private String system;
    private String faction;

    public B5Location() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    @Override
    public String toString() {
        return "B5Location{id='" + id + "', name='" + name + "', type='" + type + "'}";
    }
}
