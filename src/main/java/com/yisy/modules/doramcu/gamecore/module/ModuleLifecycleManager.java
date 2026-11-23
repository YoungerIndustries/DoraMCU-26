package com.yisy.modules.doramcu.gamecore.module;

import com.yisy.modules.doramcu.gamecore.GameModule;
import com.yisy.modules.doramcu.gamecore.GameModuleRegistry;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.MechanicRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Central orchestrator for module lifecycle operations.
 * <p>
 * Manages the complete lifecycle of dynamically loaded modules:
 * loading, reloading, unloading, enabling, disabling, and health checks.
 * Each module runs in an isolated {@link ModuleClassLoader} and {@link ModuleContext},
 * providing crash containment and true hot-swap capability.
 * </p>
 */
@Component
public class ModuleLifecycleManager {

    private static final Logger log = LoggerFactory.getLogger(ModuleLifecycleManager.class);

    private final ApplicationContext applicationContext;
    private final GameModuleRegistry gameModuleRegistry;
    private final MechanicRegistry mechanicRegistry;

    @Value("${doramcu.modules.dir:/opt/doramcu/modules/}")
    private String modulesDir;

    /** Map of GameType to ModuleInfo for all dynamically loaded modules */
    private final Map<GameType, ModuleInfo> moduleMap = new ConcurrentHashMap<>();

    /** Per-module locks to prevent concurrent reloads */
    private final Map<GameType, ReentrantLock> locks = new ConcurrentHashMap<>();

    public ModuleLifecycleManager(ApplicationContext applicationContext,
                                   GameModuleRegistry gameModuleRegistry,
                                   MechanicRegistry mechanicRegistry) {
        this.applicationContext = applicationContext;
        this.gameModuleRegistry = gameModuleRegistry;
        this.mechanicRegistry = mechanicRegistry;
    }

    @PostConstruct
    public void init() {
        log.info("ModuleLifecycleManager initialized. Modules directory: {}", modulesDir);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down ModuleLifecycleManager, unloading all dynamic modules");
        moduleMap.keySet().forEach(this::unloadModule);
    }

    /**
     * Load a module from a JAR file.
     *
     * @param jarPath path to the module JAR
     * @return the ModuleInfo if loaded successfully, or empty if failed
     */
    public Optional<ModuleInfo> loadModule(Path jarPath) {
        if (!Files.exists(jarPath)) {
            log.error("Module JAR not found: {}", jarPath);
            return Optional.empty();
        }

        // Parse manifest
        Optional<ModuleManifest> manifestOpt = ModuleManifest.fromJar(jarPath);
        if (manifestOpt.isEmpty()) {
            log.error("Invalid or missing module manifest in: {}", jarPath);
            return Optional.empty();
        }

        ModuleManifest manifest = manifestOpt.get();
        GameType gameType = manifest.getGameType();

        // Skip modules already loaded by the main Spring context
        if (gameModuleRegistry.getModule(gameType) != null && !moduleMap.containsKey(gameType)) {
            log.info("Module {} already loaded by main Spring context, skipping dynamic load", gameType);
            return Optional.empty();
        }

        // Check for duplicate game type
        if (moduleMap.containsKey(gameType)) {
            ModuleInfo existing = moduleMap.get(gameType);
            if (existing.getVersion() != null && manifest.compareVersion(existing.getVersion()) <= 0) {
                log.warn("Module {} already loaded with version {} (new version {}). Skipping.",
                        gameType, existing.getVersion(), manifest.getVersion());
                return Optional.of(existing);
            }
            log.info("Reloading module {} (version {} -> {})", gameType, existing.getVersion(), manifest.getVersion());
            return Optional.of(reloadModule(gameType, jarPath));
        }

        ReentrantLock lock = locks.computeIfAbsent(gameType, k -> new ReentrantLock());
        lock.lock();
        try {
            log.info("Loading module: {} ({}) from {}", manifest.getName(), gameType, jarPath);

            // Create classloader
            ModuleClassLoader classLoader = new ModuleClassLoader(jarPath.toUri().toURL());

            // Create module info
            ModuleInfo info = new ModuleInfo();
            info.setGameType(gameType);
            info.setVersion(manifest.getVersion());
            info.setName(manifest.getName());
            info.setJarPath(jarPath);
            info.setEnabled(true);
            info.setHealth(ModuleHealthStatus.LOADING);
            info.setClassLoader(classLoader);
            moduleMap.put(gameType, info);

            // Determine the module package from the main class
            String mainClass = manifest.getMainClass();
            String modulePackage;
            if (mainClass != null && !mainClass.isEmpty()) {
                int lastDot = mainClass.lastIndexOf('.');
                modulePackage = lastDot > 0 ? mainClass.substring(0, lastDot) : "";
            } else {
                // Derive package from game type
                modulePackage = "com.yisy.modules.doramcu." + gameType.name().toLowerCase().replace("_", "");
            }

            try {
                // Create and refresh module context
                ModuleContext moduleContext = new ModuleContext(applicationContext, modulePackage, classLoader);
                moduleContext.refresh();

                // Get the GameModule bean
                GameModule gameModule = moduleContext.getGameModule();
                if (gameModule == null) {
                    log.error("No GameModule bean found in module context for {} (dependencies may be missing from JAR)", gameType);
                    moduleContext.close();
                    classLoader.close();
                    moduleMap.remove(gameType);
                    return Optional.empty();
                }

                if (gameModule != null) {
                    info.setGameModule(gameModule);
                    info.setSpringContext(moduleContext.getContext());

                    // Register in the game module registry
                    gameModuleRegistry.registerModule(gameModule);

                    // Index in mechanic registry
                    mechanicRegistry.rebuildIndex();

                    info.setHealth(ModuleHealthStatus.HEALTHY);
                    info.setLastReloadTime(System.currentTimeMillis());

                    log.info("Module loaded successfully: {} ({}) version {} with {} beans",
                            manifest.getName(), gameType, manifest.getVersion(),
                            moduleContext.getContext().getBeanDefinitionCount());
                } else {
                    log.error("No GameModule bean found for module {}", gameType);
                    moduleContext.close();
                    classLoader.close();
                    moduleMap.remove(gameType);
                    return Optional.empty();
                }

            } catch (Exception e) {
                log.error("Failed to load module {}: {}", gameType, e.getMessage(), e);
                info.setHealth(ModuleHealthStatus.CRASHED);
                throw e;
            }

            return Optional.of(info);

        } catch (Exception e) {
            log.error("Failed to load module from {}: {}", jarPath, e.getMessage(), e);
            moduleMap.remove(gameType);
            return Optional.empty();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Reload an existing module with a new JAR file.
     *
     * @param gameType the game type to reload
     * @param newJarPath path to the new JAR file
     * @return the updated ModuleInfo
     */
    public ModuleInfo reloadModule(GameType gameType, Path newJarPath) {
        ReentrantLock lock = locks.computeIfAbsent(gameType, k -> new ReentrantLock());
        lock.lock();
        try {
            log.info("Reloading module: {} from {}", gameType, newJarPath);

            ModuleInfo existing = moduleMap.get(gameType);
            if (existing != null) {
                existing.setHealth(ModuleHealthStatus.RELOADING);

                // Unregister old module
                gameModuleRegistry.unregisterModule(gameType);

                // Close old context
                if (existing.getSpringContext() != null) {
                    try {
                        if (existing.getSpringContext() instanceof ConfigurableApplicationContext cac) {
                            cac.close();
                        }
                    } catch (Exception e) {
                        log.warn("Error closing old context for {}: {}", gameType, e.getMessage());
                    }
                }

                // Close old classloader
                if (existing.getClassLoader() instanceof Closeable closeable) {
                    try {
                        closeable.close();
                    } catch (IOException e) {
                        log.warn("Error closing old classloader for {}: {}", gameType, e.getMessage());
                    }
                }
            }

            // Remove from map and load fresh
            moduleMap.remove(gameType);
            Optional<ModuleInfo> loaded = loadModule(newJarPath);
            return loaded.orElse(null);

        } finally {
            lock.unlock();
        }
    }

    /**
     * Reload an existing module from the same JAR path (refresh only).
     *
     * @param gameType the game type to reload
     * @return the updated ModuleInfo
     */
    public ModuleInfo reloadModule(GameType gameType) {
        ModuleInfo existing = moduleMap.get(gameType);
        if (existing == null) {
            log.warn("Module {} is not loaded dynamically, cannot reload", gameType);
            return null;
        }
        return reloadModule(gameType, existing.getJarPath());
    }

    /**
     * Unload a module completely.
     *
     * @param gameType the game type to unload
     * @return true if unloaded successfully
     */
    public boolean unloadModule(GameType gameType) {
        ReentrantLock lock = locks.computeIfAbsent(gameType, k -> new ReentrantLock());
        lock.lock();
        try {
            log.info("Unloading module: {}", gameType);

            ModuleInfo info = moduleMap.remove(gameType);
            if (info == null) {
                log.warn("Module {} is not loaded dynamically", gameType);
                return false;
            }

            // Unregister from registry
            gameModuleRegistry.unregisterModule(gameType);

            // Close context
            if (info.getSpringContext() instanceof ConfigurableApplicationContext cac) {
                try {
                    cac.close();
                } catch (Exception e) {
                    log.warn("Error closing context for {}: {}", gameType, e.getMessage());
                }
            }

            // Close classloader
            if (info.getClassLoader() instanceof Closeable closeable) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    log.warn("Error closing classloader for {}: {}", gameType, e.getMessage());
                }
            }

            // Dereference for GC
            info.setSpringContext(null);
            info.setClassLoader(null);
            info.setGameModule(null);
            info.setHealth(ModuleHealthStatus.UNLOADED);

            // Rebuild mechanic index
            mechanicRegistry.rebuildIndex();

            log.info("Module unloaded: {}", gameType);
            return true;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Enable a module.
     *
     * @param gameType the game type to enable
     * @return the updated ModuleInfo
     */
    public ModuleInfo enableModule(GameType gameType) {
        ModuleInfo info = moduleMap.get(gameType);
        if (info == null) {
            log.warn("Module {} is not loaded dynamically", gameType);
            return null;
        }
        info.setEnabled(true);
        info.setHealth(ModuleHealthStatus.HEALTHY);
        log.info("Module enabled: {}", gameType);
        return info;
    }

    /**
     * Disable a module.
     *
     * @param gameType the game type to disable
     * @return the updated ModuleInfo
     */
    public ModuleInfo disableModule(GameType gameType) {
        ModuleInfo info = moduleMap.get(gameType);
        if (info == null) {
            log.warn("Module {} is not loaded dynamically", gameType);
            return null;
        }
        info.setEnabled(false);
        info.setHealth(ModuleHealthStatus.DISABLED);
        log.info("Module disabled: {}", gameType);
        return info;
    }

    /**
     * Check the health of a specific module.
     *
     * @param gameType the game type to check
     * @return the ModuleInfo with current health status
     */
    public ModuleInfo healthCheck(GameType gameType) {
        ModuleInfo info = moduleMap.get(gameType);
        if (info == null) {
            return null;
        }

        boolean active = info.getSpringContext() instanceof ConfigurableApplicationContext cac && cac.isActive();
        if (active) {
            if (info.getHealth() == ModuleHealthStatus.CRASHED) {
                info.setHealth(ModuleHealthStatus.HEALTHY);
                log.info("Module {} recovered from crashed state", gameType);
            }
        } else if (info.getHealth() == ModuleHealthStatus.HEALTHY) {
            info.setHealth(ModuleHealthStatus.CRASHED);
            log.error("Module {} context is not active", gameType);
        }

        return info;
    }

    /**
     * Get module info by game type.
     */
    public ModuleInfo getModuleInfo(GameType gameType) {
        return moduleMap.get(gameType);
    }

    /**
     * Get all module info.
     */
    public Map<GameType, ModuleInfo> getAllModuleInfo() {
        return Map.copyOf(moduleMap);
    }

    /**
     * Check if a module is loaded dynamically.
     */
    public boolean isModuleLoaded(GameType gameType) {
        return moduleMap.containsKey(gameType);
    }

    /**
     * Get the modules directory path.
     */
    public String getModulesDir() {
        return modulesDir;
    }
}
