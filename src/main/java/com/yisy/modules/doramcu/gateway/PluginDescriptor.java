package com.yisy.modules.doramcu.gateway;

import com.yisy.modules.doramcu.unified.GameType;

/**
 * Describes a game plugin that can be loaded through the Plugin Gateway.
 * <p>
 * Each plugin represents a game module (e.g., Captain's Log, Traveller, Babylon 5)
 * that is loaded lazily when a character transitions from Earth 2026 to the
 * plugin's target galaxy.
 * </p>
 */
public class PluginDescriptor {

    private GameType gameType;
    private String displayName;
    private String description;
    private String packageName;
    private boolean requiresGalaxyTransition;
    private String targetGalaxy;
    private boolean enabled;

    public PluginDescriptor() {
    }

    public PluginDescriptor(GameType gameType, String displayName, String description, String packageName) {
        this.gameType = gameType;
        this.displayName = displayName;
        this.description = description;
        this.packageName = packageName;
        this.requiresGalaxyTransition = true;
        this.enabled = false;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
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

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isRequiresGalaxyTransition() {
        return requiresGalaxyTransition;
    }

    public void setRequiresGalaxyTransition(boolean requiresGalaxyTransition) {
        this.requiresGalaxyTransition = requiresGalaxyTransition;
    }

    public String getTargetGalaxy() {
        return targetGalaxy;
    }

    public void setTargetGalaxy(String targetGalaxy) {
        this.targetGalaxy = targetGalaxy;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "PluginDescriptor{" +
                "gameType=" + gameType +
                ", displayName='" + displayName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
