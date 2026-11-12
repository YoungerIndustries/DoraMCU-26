package com.yisy.modules.doramcu.unified;

import com.yisy.modules.doramcu.traveller.AbilityScore;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Maps between {@link TravellerCharacter} and {@link UnifiedCharacter}.
 * <p>
 * Traveller uses 7 abilities (DEX/ST/END/INT/EDU/SOC/APP) with values 2-12 (2D6).
 * The unified model uses 7-core stats (STR/DEX/CON/INT/WIS/CHA/TECH).
 * </p>
 * <p>
 * Mapping:
 * <ul>
 *   <li>DEX -> DEX</li>
 *   <li>ST -> STR</li>
 *   <li>END -> CON</li>
 *   <li>INT -> INT</li>
 *   <li>EDU -> TECH</li>
 *   <li>SOC -> WIS</li>
 *   <li>APP -> CHA</li>
 * </ul>
 * </p>
 */
@Component
public class TravellerCharacterMapper implements CharacterMapper<TravellerCharacter> {

    @Override
    public GameType getGameType() {
        return GameType.TRAVELLER;
    }

    @Override
    public UnifiedCharacter toUnified(TravellerCharacter traveller) {
        if (traveller == null) return null;

        Map<String, Integer> abilities = new HashMap<>();
        abilities.put("STR", traveller.getAbility(AbilityScore.ST));
        abilities.put("DEX", traveller.getAbility(AbilityScore.DEX));
        abilities.put("CON", traveller.getAbility(AbilityScore.END));
        abilities.put("INT", traveller.getAbility(AbilityScore.INT));
        abilities.put("TECH", traveller.getAbility(AbilityScore.EDU));
        abilities.put("WIS", traveller.getAbility(AbilityScore.SOC));
        abilities.put("CHA", traveller.getAbility(AbilityScore.APP));

        List<UnifiedCharacter.SkillEntry> skills = traveller.getSkills().stream()
                .map(skill -> new SimpleSkillEntry(skill.name(), 1))
                .collect(Collectors.toList());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("wealth", traveller.getWealth());
        metadata.put("status", traveller.getStatus().name());
        metadata.put("race", traveller.getRace().name());
        metadata.put("primaryCareer", traveller.getPrimaryCareer() != null ? traveller.getPrimaryCareer().name() : null);

        return new AbstractUnifiedCharacter(
                (long) traveller.getId(),
                traveller.getName(),
                traveller.getAge(),
                traveller.getRace().getDisplayName(),
                abilities,
                0, // Traveller doesn't have explicit HP in the character model
                0,
                0,
                0,
                skills,
                null,
                traveller.getStatus().name(),
                traveller.isAlive(),
                GameType.TRAVELLER,
                metadata
        );
    }

    @Override
    public TravellerCharacter fromUnified(UnifiedCharacter unified) {
        // This is a simplified conversion — a full implementation would use CharacterService
        throw new UnsupportedOperationException("Creating TravellerCharacter from UnifiedCharacter requires CharacterService");
    }

    @Override
    public int normalizeAbilityScore(int sourceValue, GameType sourceType, GameType targetType) {
        // Traveller: 2-12 range
        // D&D/Earth2026: 1-20 range
        // Captain's Log: 0-5 range
        if (sourceType == GameType.TRAVELLER && targetType == GameType.TRAVELLER) {
            return sourceValue;
        }
        if (sourceType == GameType.TRAVELLER) {
            // Convert from 2-12 to target range
            if (targetType == GameType.EARTH_2026 || targetType == GameType.DND_5E) {
                // Map 2-12 to 3-18 (standard D&D range)
                return 3 + (sourceValue - 2) * 16 / 10;
            }
            if (targetType == GameType.CAPTAINS_LOG) {
                // Map 2-12 to 0-5
                return Math.min(5, Math.max(0, (sourceValue - 2) / 2));
            }
        }
        if (targetType == GameType.TRAVELLER) {
            // Convert from source range to 2-12
            if (sourceType == GameType.EARTH_2026 || sourceType == GameType.DND_5E) {
                // Map 1-20 to 2-12
                return 2 + Math.min(10, Math.max(0, (sourceValue - 1) * 10 / 19));
            }
            if (sourceType == GameType.CAPTAINS_LOG) {
                // Map 0-5 to 2-12
                return 2 + sourceValue * 10 / 5;
            }
        }
        return sourceValue;
    }

    /**
     * Simple implementation of SkillEntry.
     */
    private static class SimpleSkillEntry implements UnifiedCharacter.SkillEntry {
        private final String name;
        private final int level;

        SimpleSkillEntry(String name, int level) {
            this.name = name;
            this.level = level;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getLevel() {
            return level;
        }
    }
}
