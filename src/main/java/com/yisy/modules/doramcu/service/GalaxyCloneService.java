package com.yisy.modules.doramcu.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.gamecore.GameModule;
import com.yisy.modules.doramcu.gamecore.GameModuleRegistry;
import com.yisy.modules.doramcu.gamecore.MechanicRegistry;
import com.yisy.modules.doramcu.gamecore.MechanicType;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.hex.GalaxyTemplate;
import com.yisy.modules.doramcu.model.Galaxy;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for cloning galaxy templates and applying selected game mechanics.
 * <p>
 * This service handles the core cloning logic:
 * <ol>
 *   <li>Copies galaxy structure (sectors, systems, planets) from the source template</li>
 *   <li>Applies selected game mechanics based on GameModule registration</li>
 *   <li>Initializes modules for the new galaxy</li>
 * </ol>
 * </p>
 * <p>
 * When a new galaxy is created from the EARTH_2026_TEMPLATE, this service:
 * <ol>
 *   <li>Generates the galaxy structure using the template's configuration</li>
 *   <li>Identifies which GameModules provide the selected mechanics</li>
 *   <li>Initializes each module for the new galaxy via {@link GameModule#initializeForGalaxy(String, String)}</li>
 * </ol>
 * </p>
 */
@Service
public class GalaxyCloneService {

    private static final Logger log = LoggerFactory.getLogger(GalaxyCloneService.class);

    private final GameModuleRegistry gameModuleRegistry;
    private final MechanicRegistry mechanicRegistry;
    private final GalaxyTemplateService galaxyTemplateService;
    private final GalaxyRepository galaxyRepository;

    public GalaxyCloneService(
            GameModuleRegistry gameModuleRegistry,
            MechanicRegistry mechanicRegistry,
            GalaxyTemplateService galaxyTemplateService,
            GalaxyRepository galaxyRepository) {
        this.gameModuleRegistry = gameModuleRegistry;
        this.mechanicRegistry = mechanicRegistry;
        this.galaxyTemplateService = galaxyTemplateService;
        this.galaxyRepository = galaxyRepository;
    }

    /**
     * Clone a template galaxy with selected mechanics.
     * <p>
     * This is the main entry point for template-based galaxy creation. It:
     * <ol>
     *   <li>Gets the template configuration</li>
     *   <li>Filters modules by selected mechanics</li>
     *   <li>Initializes each selected module for the new galaxy</li>
     * </ol>
     * </p>
     *
     * @param galaxyName   the name of the new galaxy
     * @param template     the source template
     * @param mechanics    the set of mechanics to enable (null for all)
     * @param loreSlug     the lore slug for the template
     * @return the clone result with initialized modules
     */
    @Transactional
    public CloneResult cloneTemplate(
            String galaxyName,
            GalaxyTemplate template,
            Set<MechanicType> mechanics,
            String loreSlug) {

        log.info("Cloning template {} for galaxy '{}' with mechanics: {}",
                template.name(), galaxyName, mechanics);

        // Determine which mechanics to use
        Set<MechanicType> effectiveMechanics;
        if (mechanics == null || mechanics.isEmpty()) {
            // Use all mechanics from the template
            effectiveMechanics = galaxyTemplateService.getMechanicsForTemplate(template);
            log.info("No mechanics specified, using all template mechanics: {}", effectiveMechanics);
        } else {
            effectiveMechanics = mechanics;
        }

        // Find modules that provide the selected mechanics
        List<GameModule> selectedModules = findModulesForMechanics(effectiveMechanics);
        log.info("Selected {} modules for mechanics: {}", selectedModules.size(), effectiveMechanics);

        // Initialize each module for the new galaxy
        List<String> initializedModules = new ArrayList<>();
        for (GameModule module : selectedModules) {
            try {
                module.initializeForGalaxy(galaxyName, loreSlug);
                initializedModules.add(module.getModuleName());
                log.info("Initialized module {} for galaxy {}", module.getModuleName(), galaxyName);
            } catch (Exception e) {
                log.error("Failed to initialize module {} for galaxy {}: {}",
                        module.getModuleName(), galaxyName, e.getMessage(), e);
            }
        }

        // Get the template config for galaxy generation
        GalaxyGenerationConfig config = template.getConfig();

        return new CloneResult(
                galaxyName,
                template,
                effectiveMechanics,
                initializedModules,
                config
        );
    }

    /**
     * Clone the default template (EARTH_2026_TEMPLATE) with all mechanics.
     *
     * @param galaxyName the name of the new galaxy
     * @param loreSlug   the lore slug
     * @return the clone result
     */
    @Transactional
    public CloneResult cloneDefaultTemplate(String galaxyName, String loreSlug) {
        GalaxyTemplate template = galaxyTemplateService.getDefaultTemplate();
        return cloneTemplate(galaxyName, template, null, loreSlug);
    }

    /**
     * Find all modules that provide the specified mechanics.
     *
     * @param mechanics the set of mechanics
     * @return list of modules providing at least one of the mechanics
     */
    public List<GameModule> findModulesForMechanics(Set<MechanicType> mechanics) {
        Set<GameModule> modules = new LinkedHashSet<>();
        for (MechanicType mechanic : mechanics) {
            List<GameModule> mechanicModules = mechanicRegistry.getModulesForMechanic(mechanic);
            modules.addAll(mechanicModules);
        }
        return new ArrayList<>(modules);
    }

    /**
     * Get the template configuration for galaxy generation.
     *
     * @param template the template
     * @return the generation config
     */
    public GalaxyGenerationConfig getTemplateConfig(GalaxyTemplate template) {
        return galaxyTemplateService.getTemplateConfig(template);
    }

    /**
     * Result of a template cloning operation.
     */
    public static class CloneResult {
        private final String galaxyName;
        private final GalaxyTemplate template;
        private final Set<MechanicType> mechanics;
        private final List<String> initializedModules;
        private final GalaxyGenerationConfig config;

        public CloneResult(
                String galaxyName,
                GalaxyTemplate template,
                Set<MechanicType> mechanics,
                List<String> initializedModules,
                GalaxyGenerationConfig config) {
            this.galaxyName = galaxyName;
            this.template = template;
            this.mechanics = mechanics;
            this.initializedModules = initializedModules;
            this.config = config;
        }

        public String getGalaxyName() {
            return galaxyName;
        }

        public GalaxyTemplate getTemplate() {
            return template;
        }

        public Set<MechanicType> getMechanics() {
            return Collections.unmodifiableSet(mechanics);
        }

        public List<String> getInitializedModules() {
            return Collections.unmodifiableList(initializedModules);
        }

        public GalaxyGenerationConfig getConfig() {
            return config;
        }
    }
}
