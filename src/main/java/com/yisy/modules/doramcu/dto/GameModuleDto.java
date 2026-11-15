package com.yisy.modules.doramcu.dto;

import com.yisy.modules.doramcu.gamecore.MechanicType;

import java.util.Set;
import java.util.List;

/**
 * DTO representing a game module's metadata for REST API responses.
 */
public class GameModuleDto {

    private String id;
    private String name;
    private boolean enabled;
    private Set<String> mechanics;
    private List<String> availableServices;

    public GameModuleDto() {
    }

    public GameModuleDto(String id, String name, boolean enabled, Set<String> mechanics, List<String> availableServices) {
        this.id = id;
        this.name = name;
        this.enabled = enabled;
        this.mechanics = mechanics;
        this.availableServices = availableServices;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<String> getMechanics() {
        return mechanics;
    }

    public void setMechanics(Set<String> mechanics) {
        this.mechanics = mechanics;
    }

    public List<String> getAvailableServices() {
        return availableServices;
    }

    public void setAvailableServices(List<String> availableServices) {
        this.availableServices = availableServices;
    }
}
