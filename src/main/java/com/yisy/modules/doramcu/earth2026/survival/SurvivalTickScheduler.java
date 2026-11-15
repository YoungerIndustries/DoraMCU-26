package com.yisy.modules.doramcu.earth2026.survival;

import com.yisy.modules.doramcu.earth2026.service.BiosphereService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled task that processes survival state for all active characters.
 * <p>
 * Runs every minute to update hunger, thirst, fatigue, temperature, and exposure
 * for all characters in survival mode. Uses the BiosphereService to get current
 * environmental conditions.
 * </p>
 */
@Component("earth2026SurvivalTickScheduler")
public class SurvivalTickScheduler {

    private static final Logger log = LoggerFactory.getLogger(SurvivalTickScheduler.class);

    private final SurvivalService survivalService;
    private final SurvivalStateRepository stateRepository;
    private final BiosphereService biosphereService;

    public SurvivalTickScheduler(SurvivalService survivalService,
                                  SurvivalStateRepository stateRepository,
                                  BiosphereService biosphereService) {
        this.survivalService = survivalService;
        this.stateRepository = stateRepository;
        this.biosphereService = biosphereService;
    }

    /**
     * Process survival tick for all active characters.
     * Runs every minute.
     */
    @Scheduled(fixedRate = 60000)
    public void processSurvivalTick() {
        List<SurvivalState> activeStates = stateRepository.findByIsActiveTrue();
        if (activeStates.isEmpty()) {
            return;
        }

        // Get current environmental conditions
        double ambientTemperature = 20.0;
        boolean isNight = false;
        int weatherSeverity = 0;

        try {
            // Use default biosphere context for environmental data
            var context = biosphereService.createDefaultBiosphereContext(null);
            ambientTemperature = context.getTemperature();
            isNight = context.isNight();

            // Calculate weather severity from weather condition
            if (context.getWeather() != null) {
                weatherSeverity = context.getWeather().getVisibilityModifier() * -10;
                weatherSeverity = Math.max(0, Math.min(100, weatherSeverity));
            }
        } catch (Exception e) {
            log.warn("Failed to get biosphere context, using defaults", e);
        }

        // Process each active character
        int processed = 0;
        for (SurvivalState state : activeStates) {
            try {
                survivalService.processTick(state, ambientTemperature, isNight, weatherSeverity);
                processed++;
            } catch (Exception e) {
                log.error("Failed to process survival tick for character {}", state.getCharacterId(), e);
            }
        }

        if (processed > 0) {
            log.debug("Processed survival tick for {} active characters (temp={}, night={}, weather={})",
                    processed, ambientTemperature, isNight, weatherSeverity);
        }
    }
}
