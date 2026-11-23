package com.yisy.modules.doramcu.dto.game;

import com.yisy.modules.doramcu.gamecore.MechanicType;

import java.util.Set;
import java.util.List;

/**
 * DTO representing a game module's metadata for REST API responses.
 */
public class GameModuleDto {

    private String id;
    private String name;
    private String version;
    private boolean enabled;
    private String health;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }
}
