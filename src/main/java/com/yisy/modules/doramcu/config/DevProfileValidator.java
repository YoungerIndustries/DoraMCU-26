package com.yisy.modules.doramcu.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Validates application configuration at startup and warns about potentially
 * dangerous settings that should not be used in production.
 * <p>
 * This component checks for:
 * <ul>
 *   <li>Circular references enabled (should be false in production)</li>
 *   <li>Bean definition overriding enabled (should be false in production)</li>
 *   <li>Dev profile active without explicit opt-in</li>
 *   <li>Flyway disabled in production (data loss risk)</li>
 * </ul>
 * </p>
 */
@Component
@ConditionalOnProperty(name = "doramcu.development", havingValue = "true", matchIfMissing = false)
public class DevProfileValidator {

    private static final Logger log = LoggerFactory.getLogger(DevProfileValidator.class);

    private final Environment environment;

    public DevProfileValidator(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validateDevProfile() {
        log.info("========================================");
        log.info("DoraMCU Development Mode Active");
        log.info("========================================");
        log.info("This configuration is for DEVELOPMENT ONLY.");
        log.info("Do NOT use in production or staging environments.");
        log.info("");

        String[] activeProfiles = environment.getActiveProfiles();
        log.info("Active profiles: {}", String.join(", ", activeProfiles));

        // Check for dangerous settings
        boolean circularRefs = Boolean.parseBoolean(
                environment.getProperty("spring.main.allow-circular-references", "false"));
        boolean beanOverriding = Boolean.parseBoolean(
                environment.getProperty("spring.main.allow-bean-definition-overriding", "false"));
        boolean flywayEnabled = Boolean.parseBoolean(
                environment.getProperty("spring.flyway.enabled", "true"));

        if (circularRefs) {
            log.warn("WARNING: Circular references are enabled!");
            log.warn("This can hide architectural problems. Fix the dependencies instead.");
        }

        if (beanOverriding) {
            log.warn("WARNING: Bean definition overriding is enabled!");
            log.warn("This can cause unexpected behavior. Remove duplicate beans instead.");
        }

        if (!flywayEnabled) {
            log.warn("WARNING: Flyway migrations are disabled!");
            log.warn("Schema changes will not be tracked. Enable Flyway for production.");
        }

        log.info("========================================");
    }
}
