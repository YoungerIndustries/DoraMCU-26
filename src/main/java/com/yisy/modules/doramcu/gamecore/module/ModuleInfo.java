package com.yisy.modules.doramcu.gamecore.module;

import com.yisy.modules.doramcu.gamecore.GameModule;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.springframework.context.ApplicationContext;

import java.nio.file.Path;

/**
 * Holds metadata and runtime state for a dynamically loaded module.
 * <p>
 * Each module JAR loaded from the modules directory gets a {@code ModuleInfo}
 * instance that tracks the module's version, classloader, Spring child context,
 * enabled state, and health status.
 * </p>
 */
public class ModuleInfo {

    private GameType gameType;
    private String version;
    private String name;
    private Path jarPath;
    private boolean enabled;
    private ModuleHealthStatus health;
    private long lastReloadTime;
    private ClassLoader classLoader;
    private ApplicationContext springContext;
    private GameModule gameModule;

    public ModuleInfo() {
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Path getJarPath() {
        return jarPath;
    }

    public void setJarPath(Path jarPath) {
        this.jarPath = jarPath;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ModuleHealthStatus getHealth() {
        return health;
    }

    public void setHealth(ModuleHealthStatus health) {
        this.health = health;
    }

    public long getLastReloadTime() {
        return lastReloadTime;
    }

    public void setLastReloadTime(long lastReloadTime) {
        this.lastReloadTime = lastReloadTime;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ApplicationContext getSpringContext() {
        return springContext;
    }

    public void setSpringContext(ApplicationContext springContext) {
        this.springContext = springContext;
    }

    public GameModule getGameModule() {
        return gameModule;
    }

    public void setGameModule(GameModule gameModule) {
        this.gameModule = gameModule;
    }

    @Override
    public String toString() {
        return "ModuleInfo{" +
                "gameType=" + gameType +
                ", version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", enabled=" + enabled +
                ", health=" + health +
                '}';
    }
}
