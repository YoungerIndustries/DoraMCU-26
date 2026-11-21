package com.yisy.modules.doramcu.service.galaxy;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.gamecore.GameModule;
import com.yisy.modules.doramcu.gamecore.GameModuleRegistry;
import com.yisy.modules.doramcu.gamecore.MechanicRegistry;
import com.yisy.modules.doramcu.gamecore.MechanicType;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.hex.GalaxyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing galaxy templates and their associated mechanics.
 * <p>
 * This service provides methods to list available templates, get template configuration,
 * and determine which mechanics are installed for each template. The EARTH_2026_TEMPLATE
 * serves as the default template with all mechanics enabled.
 * </p>
 * <p>
 * When a player creates a new game, this service is used to:
 * <ol>
 *   <li>Get the template configuration (default: EARTH_2026_TEMPLATE)</li>
 *   <li>Determine which mechanics are available from registered GameModules</li>
 *   <li>Provide the template config for galaxy generation</li>
 * </ol>
 * </p>
 */
@Service
public class GalaxyTemplateService {

    private static final Logger log = LoggerFactory.getLogger(GalaxyTemplateService.class);

    private final GameModuleRegistry gameModuleRegistry;
    private final MechanicRegistry mechanicRegistry;

    public GalaxyTemplateService(GameModuleRegistry gameModuleRegistry, MechanicRegistry mechanicRegistry) {
        this.gameModuleRegistry = gameModuleRegistry;
        this.mechanicRegistry = mechanicRegistry;
    }

    /**
     * List all available galaxy templates with their installed mechanics.
     *
     * @return list of template info objects
     */
    public List<TemplateInfo> listAvailableTemplates() {
        List<TemplateInfo> templates = new ArrayList<>();
        for (GalaxyTemplate template : GalaxyTemplate.getAllTemplates()) {
            Set<MechanicType> mechanics = getMechanicsForTemplate(template);
            templates.add(new TemplateInfo(
                    template,
                    mechanics,
                    getModulesForTemplate(template)
            ));
        }
        return templates;
    }

    /**
     * Get the default template (EARTH_2026_TEMPLATE).
     *
     * @return the default template
     */
    public GalaxyTemplate getDefaultTemplate() {
        return GalaxyTemplate.EARTH_2026_TEMPLATE;
    }

    /**
     * Get template configuration by name.
     *
     * @param templateName the template name
     * @return the template info, or null if not found
     */
    public TemplateInfo getTemplateInfo(String templateName) {
        try {
            GalaxyTemplate template = GalaxyTemplate.findByName(templateName);
            return new TemplateInfo(
                    template,
                    getMechanicsForTemplate(template),
                    getModulesForTemplate(template)
            );
        } catch (IllegalArgumentException e) {
            log.warn("Template not found: {}", templateName);
            return null;
        }
    }

    /**
     * Get the generation config for a template.
     *
     * @param template the template
     * @return the generation config
     */
    public GalaxyGenerationConfig getTemplateConfig(GalaxyTemplate template) {
        return template.getConfig();
    }

    /**
     * Get all mechanics available from registered GameModules for a template.
     * For EARTH_2026_TEMPLATE, returns all available mechanics.
     * For other templates, returns mechanics based on the game type.
     *
     * @param template the template
     * @return set of mechanic types
     */
    public Set<MechanicType> getMechanicsForTemplate(GalaxyTemplate template) {
        // All templates currently return all available mechanics
        return mechanicRegistry.getAllMechanics();
    }

    /**
     * Get all modules that provide mechanics for a template.
     *
     * @param template the template
     * @return list of module names
     */
    public List<String> getModulesForTemplate(GalaxyTemplate template) {
        if (template == GalaxyTemplate.EARTH_2026_TEMPLATE) {
            // Earth 2026 template uses all modules
            return gameModuleRegistry.getAllModules().stream()
                    .map(GameModule::getModuleName)
                    .collect(Collectors.toList());
        }
        // For other templates, filter by game type
        String gameType = template.getConfig().getGameType();
        if (gameType == null || gameType.isEmpty()) {
            return Collections.emptyList();
        }
        return gameModuleRegistry.getAllModules().stream()
                .filter(m -> m.getModuleName().toLowerCase().contains(gameType.toLowerCase()))
                .map(GameModule::getModuleName)
                .collect(Collectors.toList());
    }

    /**
     * Information about a galaxy template and its installed mechanics.
     */
    public static class TemplateInfo {
        private final GalaxyTemplate template;
        private final Set<MechanicType> mechanics;
        private final List<String> modules;

        public TemplateInfo(GalaxyTemplate template, Set<MechanicType> mechanics, List<String> modules) {
            this.template = template;
            this.mechanics = mechanics;
            this.modules = modules;
        }

        public GalaxyTemplate getTemplate() {
            return template;
        }

        public String getTemplateName() {
            return template.name();
        }

        public String getDisplayName() {
            return template.getDisplayName();
        }

        public String getDescription() {
            return template.getDescription();
        }

        public GalaxyGenerationConfig getConfig() {
            return template.getConfig();
        }

        public Set<MechanicType> getMechanics() {
            return Collections.unmodifiableSet(mechanics);
        }

        public List<String> getModules() {
            return Collections.unmodifiableList(modules);
        }
    }
}
