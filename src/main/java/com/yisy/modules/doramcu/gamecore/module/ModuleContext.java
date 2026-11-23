package com.yisy.modules.doramcu.gamecore.module;

import com.yisy.modules.doramcu.gamecore.GameModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.Set;

/**
 * Wrapper around a module's Spring child {@link GenericApplicationContext}.
 * <p>
 * Each dynamically loaded module runs in its own child context that inherits
 * shared beans (services, registry) from the parent context but isolates
 * module-specific beans. This provides crash containment while allowing
 * shared service access.
 * </p>
 */
public class ModuleContext {

    private static final Logger log = LoggerFactory.getLogger(ModuleContext.class);

    private final GenericApplicationContext context;
    private final String modulePackage;

    /**
     * Create a new module child context.
     *
     * @param parentContext the parent Spring application context
     * @param modulePackage the base package to scan for module beans
     * @param classLoader   the module's isolated classloader
     */
    public ModuleContext(ApplicationContext parentContext, String modulePackage, ClassLoader classLoader) {
        this.modulePackage = modulePackage;

        // Create bean factory with the module's classloader
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.setBeanClassLoader(classLoader);

        // Create child context with parent
        this.context = new GenericApplicationContext(beanFactory);
        context.setParent(parentContext);

        log.debug("Created ModuleContext for package {} with classloader {}", modulePackage, classLoader);
    }

    /**
     * Scan the module's package for beans and refresh the context.
     * <p>
     * This discovers all {@code @Component}, {@code @Service}, {@code @Configuration}
     * classes in the module's package and registers them as beans.
     * </p>
     */
    public void refresh() {
        // Scan for component classes
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(Configuration.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(org.springframework.stereotype.Component.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(org.springframework.stereotype.Service.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(org.springframework.stereotype.Repository.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(org.springframework.stereotype.Controller.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(GameModule.class));

        Set<org.springframework.beans.factory.config.BeanDefinition> beans =
                scanner.findCandidateComponents(modulePackage);

        log.info("Scanning module package {}: found {} bean definitions", modulePackage, beans.size());

        // Register bean definitions
        BeanDefinitionRegistry registry = context;
        for (org.springframework.beans.factory.config.BeanDefinition bd : beans) {
            String beanName = bd.getBeanClassName();
            if (beanName != null) {
                // Use simple class name as bean name
                String simpleName = beanName.substring(beanName.lastIndexOf('.') + 1);
                simpleName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
                registry.registerBeanDefinition(simpleName + "$" + modulePackage.hashCode(), bd);
            }
        }

        // Refresh the context
        context.refresh();
        log.info("ModuleContext refreshed for package {} with {} beans", modulePackage, context.getBeanDefinitionCount());
    }

    /**
     * Get the GameModule bean from this context.
     *
     * @return the GameModule bean, or null if not found
     */
    public GameModule getGameModule() {
        try {
            return context.getBean(GameModule.class);
        } catch (Exception e) {
            log.warn("No GameModule bean found in context for package {}: {}", modulePackage, e.getMessage());
            return null;
        }
    }

    /**
     * Get a bean by type from this context.
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        return context.getBean(type);
    }

    /**
     * Get a bean by name and type from this context.
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name, Class<T> type) {
        return context.getBean(name, type);
    }

    /**
     * Check if the context is currently active.
     */
    public boolean isActive() {
        return context.isActive();
    }

    /**
     * Get the underlying Spring context.
     */
    public GenericApplicationContext getContext() {
        return context;
    }

    /**
     * Get the module package name.
     */
    public String getModulePackage() {
        return modulePackage;
    }

    /**
     * Close the context and destroy all beans.
     */
    public void close() {
        try {
            if (context.isActive()) {
                log.info("Closing ModuleContext for package {}", modulePackage);
                context.close();
            }
        } catch (Exception e) {
            log.error("Error closing ModuleContext for package {}: {}", modulePackage, e.getMessage());
        }
    }
}
