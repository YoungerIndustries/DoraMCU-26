package com.yisy.modules.doramcu.gamecore;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that wires up delegating services as Spring beans.
 * These services provide fallback implementations for modules that don't
 * implement certain services natively.
 */
@Configuration
public class ServiceDelegationConfig {
    // Delegating services are registered as @Component beans directly:
    // - DelegatingDiceService
    // - DelegatingCombatService
    // - DelegatingEncounterService
    // - DelegatingCharacterService
}
