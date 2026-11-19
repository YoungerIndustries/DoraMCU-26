package com.yisy.modules.doramcu.earth2026.survival;

import org.springframework.stereotype.Service;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CharacterRepository;
import com.yisy.modules.doramcu.earth2026.survival.dto.ScavengeResultDto;
import com.yisy.modules.doramcu.earth2026.survival.dto.SurvivalStatusDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Core survival service for Earth2026 characters.
 * <p>
 * Manages survival state (hunger, thirst, fatigue, temperature, exposure),
 * stat decay, environmental effects, and survival actions (eat, drink, rest).
 * </p>
 * <p>
 * Key mechanics:
 * - Hunger decays by 2 per tick (1 real-world minute)
 * - Thirst decays by 3 per tick (faster than hunger)
 * - Fatigue increases by 1 per tick when active
 * - Body temperature drifts toward ambient temperature
 * - Exposure increases in extreme weather conditions
 * - At critical levels, character takes damage to hit points
 * </p>
 */
@Service("survivalService_earth2026_survival_SurvivalService")
public class SurvivalService {

    private static final Logger log = LoggerFactory.getLogger(SurvivalService.class);

    // Stat decay rates per tick (1 tick = 1 real-world minute)
    private static final int HUNGER_DECAY = 2;
    private static final int THIRST_DECAY = 3;
    private static final int FATIGUE_INCREASE = 1;

    // Temperature drift rate (Ã‚Â°C per tick toward ambient)
    private static final double TEMPERATURE_DRIFT = 0.01;

    // Normal body temperature range
    private static final double NORMAL_TEMP_MIN = 36.1;
    private static final double NORMAL_TEMP_MAX = 37.2;

    // Damage per tick at critical levels
    private static final int STARVATION_DAMAGE = 5;
    private static final int DEHYDRATION_DAMAGE = 5;
    private static final int EXHAUSTION_DAMAGE = 3;
    private static final int EXPOSURE_DAMAGE = 5;

    private final SurvivalStateRepository stateRepository;
    private final Earth2026CharacterRepository characterRepository;
    private final ResourceService resourceService;

    public SurvivalService(SurvivalStateRepository stateRepository,
                           Earth2026CharacterRepository characterRepository,
                           ResourceService resourceService) {
        this.stateRepository = stateRepository;
        this.characterRepository = characterRepository;
        this.resourceService = resourceService;
    }

    // ==================== State Management ====================

    /**
     * Get or create the survival state for a character.
     * Creates a default state (full hunger/thirst, zero fatigue) if none exists.
     *
     * @param characterId the character ID
     * @return the survival state
     */
    @Transactional
    public SurvivalState getOrCreateState(Long characterId) {
        return stateRepository.findByCharacterId(characterId).orElseGet(() -> {
            SurvivalState state = new SurvivalState(characterId);
            state.setHunger(100);
            state.setThirst(100);
            state.setFatigue(0);
            state.setBodyTemperature(37.0);
            state.setExposure(0);
            state.setActive(true);
            state.setLastTick(LocalDateTime.now());
            SurvivalState saved = stateRepository.save(state);
            log.info("Created default survival state for character {}", characterId);
            return saved;
        });
    }

    /**
     * Get the survival state for a character.
     *
     * @param characterId the character ID
     * @return the survival state or null if not found
     */
    @Transactional(readOnly = true)
    public SurvivalState getState(Long characterId) {
        return stateRepository.findByCharacterId(characterId).orElse(null);
    }

    /**
     * Get the survival status as a DTO for a character.
     *
     * @param characterId the character ID
     * @return the survival status DTO
     */
    @Transactional(readOnly = true)
    public SurvivalStatusDto getStatus(Long characterId) {
        SurvivalState state = getOrCreateState(characterId);
        return toDto(state);
    }

    // ==================== Survival Actions ====================

    /**
     * Eat a food resource to restore hunger.
     *
     * @param characterId the character ID
     * @param resourceName the name of the food resource
     * @return the updated survival status
     */
    @Transactional
    public SurvivalStatusDto eat(Long characterId, String resourceName) {
        SurvivalState state = getOrCreateState(characterId);
        SurvivalResource resource = resourceService.getResourceByName(resourceName);

        if (!resource.isConsumable() && !"FOOD".equals(resource.getCategory())) {
            throw new IllegalArgumentException("Cannot eat: " + resourceName);
        }

        // Apply food effects
        state.setHunger(state.getHunger() + resource.getHungerValue());
        state.setThirst(state.getThirst() + resource.getThirstValue());
        if (resource.getFatigueReduction() > 0) {
            state.setFatigue(state.getFatigue() - resource.getFatigueReduction());
        }
        if (resource.getHealthValue() > 0) {
            applyHealthEffect(characterId, resource.getHealthValue());
        }
        state.setUpdatedAt(LocalDateTime.now());
        stateRepository.save(state);

        log.info("Character {} ate {}, hunger: {} -> {}",
                characterId, resourceName, state.getHunger() - resource.getHungerValue(), state.getHunger());
        return toDto(state);
    }

    /**
     * Drink a water resource to restore thirst.
     *
     * @param characterId the character ID
     * @param resourceName the name of the water resource
     * @return the updated survival status
     */
    @Transactional
    public SurvivalStatusDto drink(Long characterId, String resourceName) {
        SurvivalState state = getOrCreateState(characterId);
        SurvivalResource resource = resourceService.getResourceByName(resourceName);

        if (!resource.isConsumable() && !"WATER".equals(resource.getCategory())) {
            throw new IllegalArgumentException("Cannot drink: " + resourceName);
        }

        // Apply water effects
        state.setThirst(state.getThirst() + resource.getThirstValue());
        if (resource.getHealthValue() > 0) {
            applyHealthEffect(characterId, resource.getHealthValue());
        } else if (resource.getHealthValue() < 0) {
            applyHealthEffect(characterId, resource.getHealthValue());
        }
        state.setUpdatedAt(LocalDateTime.now());
        stateRepository.save(state);

        log.info("Character {} drank {}, thirst: {} -> {}",
                characterId, resourceName, state.getThirst() - resource.getThirstValue(), state.getThirst());
        return toDto(state);
    }

    /**
     * Rest to reduce fatigue. Resting also slightly restores hunger and thirst.
     *
     * @param characterId the character ID
     * @param hours number of hours to rest
     * @return the updated survival status
     */
    @Transactional
    public SurvivalStatusDto rest(Long characterId, int hours) {
        SurvivalState state = getOrCreateState(characterId);

        // Fatigue reduction: 10 per hour of rest
        int fatigueReduction = Math.min(state.getFatigue(), hours * 10);
        state.setFatigue(state.getFatigue() - fatigueReduction);

        // Hunger/thirst still decay during rest, but slower
        int hungerDecay = Math.max(1, (int) (HUNGER_DECAY * hours * 0.5));
        int thirstDecay = Math.max(1, (int) (THIRST_DECAY * hours * 0.5));
        state.setHunger(state.getHunger() - hungerDecay);
        state.setThirst(state.getThirst() - thirstDecay);

        // Body temperature normalizes during rest
        if (state.getBodyTemperature() < NORMAL_TEMP_MIN) {
            state.setBodyTemperature(Math.min(NORMAL_TEMP_MAX, state.getBodyTemperature() + 0.1 * hours));
        } else if (state.getBodyTemperature() > NORMAL_TEMP_MAX) {
            state.setBodyTemperature(Math.max(NORMAL_TEMP_MIN, state.getBodyTemperature() - 0.1 * hours));
        }

        // Exposure reduces during rest (assuming shelter)
        state.setExposure(Math.max(0, state.getExposure() - hours * 5));

        state.setLastTick(LocalDateTime.now());
        state.setUpdatedAt(LocalDateTime.now());
        stateRepository.save(state);

        log.info("Character {} rested for {} hours, fatigue: {} -> {}",
                characterId, hours, state.getFatigue() + fatigueReduction, state.getFatigue());
        return toDto(state);
    }

    /**
     * Scavenge for resources at a location.
     *
     * @param characterId the character ID
     * @param location the location name (optional)
     * @return the scavenge result
     */
    @Transactional
    public ScavengeResultDto scavenge(Long characterId, String location) {
        SurvivalState state = getOrCreateState(characterId);

        // Check if character is too exhausted to scavenge
        if (state.isExhausted()) {
            ScavengeResultDto result = new ScavengeResultDto();
            result.setSuccess(false);
            result.setMessage("Character is too exhausted to scavenge. Rest first.");
            return result;
        }

        List<SurvivalResource> found = resourceService.scavenge(characterId, location);

        // Apply fatigue cost
        int fatigueCost = Math.max(1, 5);
        state.setFatigue(state.getFatigue() + fatigueCost);
        state.setUpdatedAt(LocalDateTime.now());
        stateRepository.save(state);

        ScavengeResultDto result = new ScavengeResultDto();
        result.setSuccess(true);
        result.setMessage("Found " + found.size() + " items while scavenging.");
        result.setResourcesFound(found);
        result.setTotalItems(found.size());
        result.setLocation(location != null ? location : "Current location");
        return result;
    }

    // ==================== Survival Tick ====================

    /**
     * Process a survival tick for a character.
     * This is called by the SurvivalTickScheduler every minute.
     *
     * @param state the survival state to process
     * @param ambientTemperature the ambient temperature in Celsius
     * @param isNight whether it's currently night
     * @param weatherSeverity weather severity (0-100, higher = more severe)
     */
    @Transactional
    public void processTick(SurvivalState state, double ambientTemperature, boolean isNight, int weatherSeverity) {
        if (!state.isActive()) {
            return;
        }

        // Hunger decay
        state.setHunger(state.getHunger() - HUNGER_DECAY);

        // Thirst decay (faster than hunger)
        state.setThirst(state.getThirst() - THIRST_DECAY);

        // Fatigue increase (faster at night if no shelter)
        int fatigueIncrease = FATIGUE_INCREASE;
        if (isNight) {
            fatigueIncrease += 1;
        }
        state.setFatigue(state.getFatigue() + fatigueIncrease);

        // Temperature drift toward ambient
        double currentTemp = state.getBodyTemperature();
        double tempDiff = ambientTemperature - currentTemp;
        state.setBodyTemperature(currentTemp + tempDiff * TEMPERATURE_DRIFT);

        // Exposure increase in extreme weather
        if (weatherSeverity > 50) {
            int exposureIncrease = (weatherSeverity - 50) / 10;
            state.setExposure(state.getExposure() + exposureIncrease);
        } else {
            // Exposure decreases in mild weather
            state.setExposure(Math.max(0, state.getExposure() - 1));
        }

        // Check critical conditions and apply damage
        Earth2026Character character = characterRepository.findById(state.getCharacterId()).orElse(null);
        if (character != null) {
            int totalDamage = 0;

            if (state.isStarving()) {
                totalDamage += STARVATION_DAMAGE;
            }
            if (state.isDehydrated()) {
                totalDamage += DEHYDRATION_DAMAGE;
            }
            if (state.isExhausted()) {
                totalDamage += EXHAUSTION_DAMAGE;
            }
            if (state.hasCriticalExposure()) {
                totalDamage += EXPOSURE_DAMAGE;
            }
            if (state.hasHypothermia()) {
                totalDamage += EXPOSURE_DAMAGE;
            }
            if (state.hasHeatStroke()) {
                totalDamage += EXPOSURE_DAMAGE;
            }

            if (totalDamage > 0) {
                character.setHitPoints(Math.max(0, character.getHitPoints() - totalDamage));
                characterRepository.save(character);
                log.warn("Character {} took {} survival damage (starving={}, dehydrated={}, exhausted={}, exposure={})",
                        character.getName(), totalDamage,
                        state.isStarving(), state.isDehydrated(), state.isExhausted(), state.getExposure());
            }
        }

        state.setLastTick(LocalDateTime.now());
        state.setUpdatedAt(LocalDateTime.now());
        stateRepository.save(state);
    }

    // ==================== Helpers ====================

    private void applyHealthEffect(Long characterId, int healthValue) {
        if (healthValue == 0) return;
        Earth2026Character character = characterRepository.findById(characterId).orElse(null);
        if (character == null) return;

        int newHp = character.getHitPoints() + healthValue;
        character.setHitPoints(Math.min(character.getMaxHitPoints(), Math.max(0, newHp)));
        characterRepository.save(character);
    }

    private SurvivalStatusDto toDto(SurvivalState state) {
        SurvivalStatusDto dto = new SurvivalStatusDto();
        dto.setCharacterId(state.getCharacterId());
        dto.setHunger(state.getHunger());
        dto.setThirst(state.getThirst());
        dto.setFatigue(state.getFatigue());
        dto.setBodyTemperature(state.getBodyTemperature());
        dto.setExposure(state.getExposure());
        dto.setStarving(state.isStarving());
        dto.setDehydrated(state.isDehydrated());
        dto.setExhausted(state.isExhausted());
        dto.setHypothermia(state.hasHypothermia());
        dto.setHeatStroke(state.hasHeatStroke());
        dto.setCriticalExposure(state.hasCriticalExposure());
        dto.setCritical(state.isCritical());
        dto.setActive(state.isActive());
        dto.setLastTick(state.getLastTick());
        return dto;
    }
}
