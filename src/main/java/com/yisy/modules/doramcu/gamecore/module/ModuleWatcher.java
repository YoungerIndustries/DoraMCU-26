package com.yisy.modules.doramcu.gamecore.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Monitors the modules directory for JAR file changes using {@link WatchService}.
 * <p>
 * Detects CREATE, MODIFY, and DELETE events for JAR files and triggers
 * the appropriate action in {@link ModuleLifecycleManager}:
 * <ul>
 *   <li>CREATE → load new module</li>
 *   <li>MODIFY → reload existing module</li>
 *   <li>DELETE → unload module</li>
 * </ul>
 * A debounce mechanism (500ms) prevents rapid reload loops during file copy.
 * </p>
 */
@Component
public class ModuleWatcher {

    private static final Logger log = LoggerFactory.getLogger(ModuleWatcher.class);

    private final ModuleLifecycleManager lifecycleManager;

    @Value("${doramcu.modules.watch-enabled:true}")
    private boolean watchEnabled;

    @Value("${doramcu.modules.dir:/opt/doramcu/modules/}")
    private String modulesDir;

    // Package-private setters for testing
    void setWatchEnabled(boolean watchEnabled) {
        this.watchEnabled = watchEnabled;
    }

    void setModulesDir(String modulesDir) {
        this.modulesDir = modulesDir;
    }

    private WatchService watchService;
    private ScheduledExecutorService debounceExecutor;
    private volatile boolean running = false;

    public ModuleWatcher(ModuleLifecycleManager lifecycleManager) {
        this.lifecycleManager = lifecycleManager;
    }

    @PostConstruct
    public void start() {
        if (!watchEnabled) {
            log.info("ModuleWatcher disabled by configuration");
            return;
        }

        Path path = Paths.get(modulesDir);
        if (!java.nio.file.Files.exists(path)) {
            log.warn("Modules directory does not exist: {}. ModuleWatcher will not start.", modulesDir);
            return;
        }

        try {
            watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService,
                    java.nio.file.StandardWatchEventKinds.ENTRY_CREATE,
                    java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY,
                    java.nio.file.StandardWatchEventKinds.ENTRY_DELETE);

            debounceExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "module-watcher-debounce");
                t.setDaemon(true);
                return t;
            });

            running = true;
            Thread watchThread = new Thread(this::watchLoop, "module-watcher");
            watchThread.setDaemon(true);
            watchThread.start();

            log.info("ModuleWatcher started, monitoring: {}", modulesDir);
        } catch (IOException e) {
            log.error("Failed to start ModuleWatcher: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                log.warn("Error closing WatchService: {}", e.getMessage());
            }
        }
        if (debounceExecutor != null) {
            debounceExecutor.shutdownNow();
        }
        log.info("ModuleWatcher stopped");
    }

    private void watchLoop() {
        while (running) {
            try {
                WatchKey key = watchService.poll(1, TimeUnit.SECONDS);
                if (key == null) continue;

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == java.nio.file.StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                    Path fileName = pathEvent.context();
                    String name = fileName.toString().toLowerCase();

                    if (!name.endsWith(".jar")) {
                        continue;
                    }

                    Path fullPath = ((Path) key.watchable()).resolve(fileName);

                    // Debounce to prevent rapid reload loops
                    debounceExecutor.schedule(() -> {
                        try {
                            handleEvent(kind, fullPath);
                        } catch (Exception e) {
                            log.error("Error handling module event: {}", e.getMessage(), e);
                        }
                    }, 500, TimeUnit.MILLISECONDS);
                }

                if (!key.reset()) {
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void handleEvent(WatchEvent.Kind<?> kind, Path jarPath) {
        log.info("Module event: {} on {}", kind.name(), jarPath);

        if (kind == java.nio.file.StandardWatchEventKinds.ENTRY_CREATE) {
            lifecycleManager.loadModule(jarPath);
        } else if (kind == java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY) {
            handleModify(jarPath);
        } else if (kind == java.nio.file.StandardWatchEventKinds.ENTRY_DELETE) {
            handleDelete(jarPath);
        }
    }

    private void handleModify(Path jarPath) {
        // Check if the JAR still exists (it might have been replaced)
        if (!java.nio.file.Files.exists(jarPath)) {
            return;
        }

        // Try to parse the manifest to find the game type
        java.util.Optional<ModuleManifest> manifestOpt = ModuleManifest.fromJar(jarPath);
        if (manifestOpt.isPresent()) {
            var manifest = manifestOpt.get();
            lifecycleManager.reloadModule(manifest.getGameType(), jarPath);
        }
    }

    private void handleDelete(Path jarPath) {
        // Try to parse the manifest from the existing module info
        java.util.Optional<ModuleManifest> manifestOpt = ModuleManifest.fromJar(jarPath);
        if (manifestOpt.isPresent()) {
            var manifest = manifestOpt.get();
            lifecycleManager.unloadModule(manifest.getGameType());
        } else {
            // Try to find the module by JAR path
            for (var entry : lifecycleManager.getAllModuleInfo().entrySet()) {
                if (entry.getValue().getJarPath() != null &&
                    entry.getValue().getJarPath().toString().equals(jarPath.toString())) {
                    lifecycleManager.unloadModule(entry.getKey());
                    break;
                }
            }
        }
    }
}
