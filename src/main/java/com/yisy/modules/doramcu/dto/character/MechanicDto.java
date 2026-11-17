package com.yisy.modules.doramcu.dto.character;

import java.util.List;

/**
 * DTO representing a mechanic type and which modules provide it.
 */
public class MechanicDto {

    private String type;
    private String displayName;
    private String description;
    private List<String> providingModules;

    public MechanicDto() {
    }

    public MechanicDto(String type, String displayName, String description, List<String> providingModules) {
        this.type = type;
        this.displayName = displayName;
        this.description = description;
        this.providingModules = providingModules;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getProvidingModules() {
        return providingModules;
    }

    public void setProvidingModules(List<String> providingModules) {
        this.providingModules = providingModules;
    }
}
