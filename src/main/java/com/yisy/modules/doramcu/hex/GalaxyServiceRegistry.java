package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.fourx.FourxCharacterService;
import com.yisy.modules.doramcu.fourx.FourxCombatService;
import com.yisy.modules.doramcu.fourx.FourxDiplomacyService;
import com.yisy.modules.doramcu.fourx.FourxEconomyService;
import com.yisy.modules.doramcu.fourx.FourxExplorationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Registry that maps {@link GalaxyTemplate} instances to their available
 * 4X service implementations.
 * <p>
 * Each galaxy template (e.g., BABYLON5, STAR_TREK, SFB) can have a set of
 * {@code Fourx*Service} implementations that provide the 4X pillars:
 * diplomacy, exploration, economy, character creation, and combat.
 * </p>
 * <p>
 * The registry is populated at startup by Spring beans that implement
 * {@link GalaxyServiceRegistry.ServiceProvider}. Services are discovered
 * automatically and associated with the template they support.
 * </p>
 * <p>
 * Usage:
 * <pre>
 * Set&lt;Object&gt; services = GalaxyServiceRegistry.getServices(GalaxyTemplate.BABYLON5);
 * FourxDiplomacyService diplomacy = GalaxyServiceRegistry.getService(
 *     GalaxyTemplate.BABYLON5, FourxDiplomacyService.class);
 * </pre>
 * </p>
 */
@Component
public class GalaxyServiceRegistry {

    private static final Logger log = LoggerFactory.getLogger(GalaxyServiceRegistry.class);

    /**
     * Maps GalaxyTemplate → list of 4X service beans.
     */
    private static final Map<GalaxyTemplate, List<Object>> serviceMap = new EnumMap<>(GalaxyTemplate.class);

    /**
     * Functional interface for Spring beans that want to register themselves
     * with a specific GalaxyTemplate.
     * <p>
     * Implement this interface in a {@code Fourx*Service} bean to automatically
     * register it with the registry.
     * </p>
     */
    @FunctionalInterface
    public interface ServiceProvider {
        /**
         * Returns the galaxy template this service supports.
         *
         * @return the supported template
         */
        GalaxyTemplate getSupportedTemplate();
    }

    /**
     * Initializes the registry by discovering all ServiceProvider beans.
     * <p>
     * Called automatically by Spring after context initialization.
     * </p>
     *
     * @param providers all beans that implement ServiceProvider
     */
    public void init(List<ServiceProvider> providers) {
        for (ServiceProvider provider : providers) {
            GalaxyTemplate template = provider.getSupportedTemplate();
            if (template != null) {
                serviceMap.computeIfAbsent(template, k -> new ArrayList<>()).add(provider);
                log.info("Registered 4X service '{}' for template {}", provider.getClass().getSimpleName(), template.name());
            }
        }
        log.info("GalaxyServiceRegistry initialized with {} templates", serviceMap.size());
    }

    /**
     * Returns all 4X services registered for the given template.
     *
     * @param template the galaxy template
     * @return an unmodifiable list of services, or an empty list if none
     */
    public static Set<Object> getServices(GalaxyTemplate template) {
        List<Object> services = serviceMap.get(template);
        return services != null ? Collections.unmodifiableSet(new HashSet<>(services)) : Collections.emptySet();
    }

    /**
     * Returns a specific 4X service for the given template.
     *
     * @param template the galaxy template
     * @param serviceClass the service interface class
     * @param <T> the service type
     * @return the service instance, or null if not registered
     */
    public static <T> T getService(GalaxyTemplate template, Class<T> serviceClass) {
        for (Object service : getServices(template)) {
            if (serviceClass.isInstance(service)) {
                return serviceClass.cast(service);
            }
        }
        return null;
    }

    /**
     * Returns the diplomacy service for the given template, if available.
     *
     * @param template the galaxy template
     * @return the diplomacy service or null
     */
    public static FourxDiplomacyService getDiplomacyService(GalaxyTemplate template) {
        return getService(template, FourxDiplomacyService.class);
    }

    /**
     * Returns the exploration service for the given template, if available.
     *
     * @param template the galaxy template
     * @return the exploration service or null
     */
    public static FourxExplorationService getExplorationService(GalaxyTemplate template) {
        return getService(template, FourxExplorationService.class);
    }

    /**
     * Returns the economy service for the given template, if available.
     *
     * @param template the galaxy template
     * @return the economy service or null
     */
    public static FourxEconomyService getEconomyService(GalaxyTemplate template) {
        return getService(template, FourxEconomyService.class);
    }

    /**
     * Returns the character service for the given template, if available.
     *
     * @param template the galaxy template
     * @return the character service or null
     */
    public static FourxCharacterService getCharacterService(GalaxyTemplate template) {
        return getService(template, FourxCharacterService.class);
    }

    /**
     * Returns the combat service for the given template, if available.
     *
     * @param template the galaxy template
     * @return the combat service or null
     */
    public static FourxCombatService getCombatService(GalaxyTemplate template) {
        return getService(template, FourxCombatService.class);
    }

    /**
     * Returns all templates that have at least one registered service.
     *
     * @return a set of templates with registered services
     */
    public static Set<GalaxyTemplate> getAvailableTemplates() {
        return Collections.unmodifiableSet(serviceMap.keySet());
    }

    /**
     * Returns the number of services registered for the given template.
     *
     * @param template the galaxy template
     * @return the number of registered services
     */
    public static int getServiceCount(GalaxyTemplate template) {
        List<Object> services = serviceMap.get(template);
        return services != null ? services.size() : 0;
    }
}
