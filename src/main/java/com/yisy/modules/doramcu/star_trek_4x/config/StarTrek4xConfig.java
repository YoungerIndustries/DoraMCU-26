package com.yisy.modules.doramcu.star_trek_4x.config;

import com.yisy.modules.doramcu.earth2026.service.PlanetService;
import com.yisy.modules.doramcu.service.AiCommanderExperienceService;
import com.yisy.modules.doramcu.thirdreich.service.ThirdReichGroundBattleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Configuration for the Star Trek 4x package.
 * <p>
 * Injects PlanetService (from earth2026) and ThirdReichGroundBattleService (from thirdreich)
 * to enable planet creation and ground combat integration.
 * </p>
 */
@Configuration
public class StarTrek4xConfig {

    private static final Logger log = LoggerFactory.getLogger(StarTrek4xConfig.class);

    /**
     * Create the Third Reich ground battle service with PlanetService integration.
     * This enables Earth 2026 battle environments for Star Trek ground combat.
     */
    @Bean
    @DependsOn("planetService")
    public ThirdReichGroundBattleService thirdReichGroundBattleService(PlanetService planetService,
                                                                          AiCommanderExperienceService experienceService) {
        log.info("Initializing ThirdReichGroundBattleService with PlanetService for Star Trek 4x integration");
        return new ThirdReichGroundBattleService(planetService, experienceService);
    }

    /**
     * Expose PlanetService for use by Star Trek 4x services.
     * PlanetService provides planet lookup, battle environments, tech levels, species, and rules database.
     */
    @Bean
    public PlanetService planetService(PlanetService planetService) {
        log.info("PlanetService available for Star Trek 4x planet creation and rules database access");
        return planetService;
    }
}
