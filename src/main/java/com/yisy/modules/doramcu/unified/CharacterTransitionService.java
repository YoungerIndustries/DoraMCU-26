package com.yisy.modules.doramcu.unified;

import com.yisy.modules.doramcu.shared.model.SharedCharacter;
import com.yisy.modules.doramcu.shared.service.SharedCharacterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that handles character conversion when moving between game systems.
 * <p>
 * This service uses {@link CharacterMapper} adapters to convert characters
 * from one game system to another, normalizing ability scores, skills, and
 * equipment during the transition.
 * </p>
 * <p>
 * The workflow is:
 * <ol>
 *   <li>Convert source character to {@link UnifiedCharacter} using source mapper</li>
 *   <li>Normalize ability scores to target system's range</li>
 *   <li>Convert unified character to target game-specific character using target mapper</li>
 * </ol>
 * </p>
 */
@Service
public class CharacterTransitionService {

    private static final Logger log = LoggerFactory.getLogger(CharacterTransitionService.class);

    private final List<CharacterMapper<?>> mappers;
    private final SharedCharacterService sharedCharacterService;

    public CharacterTransitionService(
            List<CharacterMapper<?>> mappers,
            SharedCharacterService sharedCharacterService) {
        this.mappers = mappers;
        this.sharedCharacterService = sharedCharacterService;
    }

    /**
     * Finds the mapper for a given game type.
     *
     * @param gameType the game type
     * @return the mapper, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> CharacterMapper<T> getMapper(GameType gameType) {
        return (CharacterMapper<T>) mappers.stream()
                .filter(m -> m.getGameType() == gameType)
                .findFirst()
                .orElse(null);
    }

    /**
     * Converts a character from one game system to another.
     * <p>
     * This is the core method that handles cross-game character transitions.
     * It converts the source character to a unified representation, normalizes
     * the ability scores, and then converts to the target game system.
     * </p>
     *
     * @param sourceCharacter the source game-specific character
     * @param sourceType      the source game type
     * @param targetType      the target game type
     * @return the target game-specific character
     * @param <S> the source character type
     * @param <T> the target character type
     */
    @SuppressWarnings("unchecked")
    public <S, T> T transitionCharacter(S sourceCharacter, GameType sourceType, GameType targetType) {
        if (sourceType == targetType) {
            return (T) sourceCharacter;
        }

        log.info("Transitioning character from {} to {}", sourceType, targetType);

        // Step 1: Convert source to unified
        CharacterMapper<S> sourceMapper = getMapper(sourceType);
        if (sourceMapper == null) {
            throw new IllegalArgumentException("No mapper found for source game type: " + sourceType);
        }

        UnifiedCharacter unified = sourceMapper.toUnified(sourceCharacter);
        if (unified == null) {
            throw new IllegalArgumentException("Failed to convert source character to unified representation");
        }

        // Step 2: Normalize ability scores
        Map<String, Integer> normalizedAbilities = new HashMap<>();
        for (Map.Entry<String, Integer> entry : unified.getAbilities().entrySet()) {
            int normalized = sourceMapper.normalizeAbilityScore(
                    entry.getValue(), sourceType, targetType);
            normalizedAbilities.put(entry.getKey(), normalized);
        }

        // Step 3: Create new unified character with normalized abilities
        UnifiedCharacter normalizedUnified = createNormalizedUnified(unified, normalizedAbilities, targetType);

        // Step 4: Convert to target game-specific character
        CharacterMapper<T> targetMapper = getMapper(targetType);
        if (targetMapper == null) {
            throw new IllegalArgumentException("No mapper found for target game type: " + targetType);
        }

        T targetCharacter = targetMapper.fromUnified(normalizedUnified);
        log.info("Character transitioned from {} to {}: {}", sourceType, targetType, targetCharacter);

        return targetCharacter;
    }

    /**
     * Creates a new unified character with normalized abilities.
     */
    private UnifiedCharacter createNormalizedUnified(
            UnifiedCharacter original,
            Map<String, Integer> normalizedAbilities,
            GameType targetType) {
        return new AbstractUnifiedCharacter(
                original.getId(),
                original.getName(),
                original.getAge(),
                original.getRace(),
                normalizedAbilities,
                original.getHitPoints(),
                original.getMaxHitPoints(),
                original.getExperiencePoints(),
                original.getLevel(),
                original.getSkills(),
                original.getEquipment(),
                original.getStatus(),
                original.isAlive(),
                targetType,
                original.getMetadata()
        );
    }

    /**
     * Creates a SharedCharacter from any game-specific character.
     * <p>
     * This method imports a character from any game system into the shared
     * character system, enabling cross-game travel.
     * </p>
     *
     * @param gameCharacter the game-specific character
     * @param gameType      the game type
     * @return the shared character
     * @param <T> the game-specific character type
     */
    public <T> SharedCharacter importToShared(T gameCharacter, GameType gameType) {
        CharacterMapper<T> mapper = getMapper(gameType);
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper found for game type: " + gameType);
        }

        UnifiedCharacter unified = mapper.toUnified(gameCharacter);
        if (unified == null) {
            throw new IllegalArgumentException("Failed to convert character to unified representation");
        }

        // Create shared character from unified data
        SharedCharacter shared = new SharedCharacter(unified.getName(), gameType);
        shared.setStrength(unified.getAbility("STR"));
        shared.setDexterity(unified.getAbility("DEX"));
        shared.setConstitution(unified.getAbility("CON"));
        shared.setIntelligence(unified.getAbility("INT"));
        shared.setWisdom(unified.getAbility("WIS"));
        shared.setCharisma(unified.getAbility("CHA"));
        shared.setTechnical(unified.getAbility("TECH"));
        shared.setAge(unified.getAge());
        shared.setRace(unified.getRace());

        com.yisy.modules.doramcu.shared.dto.CreateSharedCharacterRequest request =
                new com.yisy.modules.doramcu.shared.dto.CreateSharedCharacterRequest();
        request.setName(shared.getName());
        request.setOriginSystem(shared.getOriginSystem());
        request.setStrength(shared.getStrength());
        request.setDexterity(shared.getDexterity());
        request.setConstitution(shared.getConstitution());
        request.setIntelligence(shared.getIntelligence());
        request.setWisdom(shared.getWisdom());
        request.setCharisma(shared.getCharisma());
        request.setTechnical(shared.getTechnical());
        request.setAge(shared.getAge());
        request.setRace(shared.getRace());
        return sharedCharacterService.createSharedCharacter(request);
    }

    /**
     * Lists all available game types that have character mappers.
     *
     * @return the list of game types
     */
    public List<GameType> getAvailableGameTypes() {
        return mappers.stream()
                .map(CharacterMapper::getGameType)
                .collect(Collectors.toList());
    }
}
