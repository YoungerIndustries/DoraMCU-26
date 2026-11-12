package com.yisy.modules.doramcu.unified;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Maps between {@link Earth2026Character} and {@link UnifiedCharacter}.
 * <p>
 * Earth2026 uses 6 abilities (STR/DEX/CON/INT/WIS/CHA) with values 1-20.
 * TECH is derived from INT for Earth2026 characters.
 * </p>
 */
@Component
public class Earth2026CharacterMapper implements CharacterMapper<Earth2026Character> {

    @Override
    public GameType getGameType() {
        return GameType.EARTH_2026;
    }

    @Override
    public UnifiedCharacter toUnified(Earth2026Character e26) {
        if (e26 == null) return null;

        Map<String, Integer> abilities = new HashMap<>();
        abilities.put("STR", e26.getStrScore());
        abilities.put("DEX", e26.getDexScore());
        abilities.put("CON", e26.getConScore());
        abilities.put("INT", e26.getIntScore());
        abilities.put("WIS", e26.getWisScore());
        abilities.put("CHA", e26.getChaScore());
        abilities.put("TECH", e26.getIntScore()); // TECH derived from INT

        List<UnifiedCharacter.SkillEntry> skills = Collections.emptyList();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("occupation", e26.getOccupation());
        metadata.put("background", e26.getBackground());
        metadata.put("earthLocation", e26.getEarthLocation());
        metadata.put("targetGalaxy", e26.getTargetGalaxy());
        metadata.put("hasTransitioned", e26.isHasTransitioned());
        metadata.put("supernaturalEnabled", e26.isSupernaturalEnabled());
        if (e26.getSanityScore() != null) {
            metadata.put("sanityScore", e26.getSanityScore());
        }

        return new AbstractUnifiedCharacter(
                e26.getId(),
                e26.getName(),
                e26.getAge(),
                "Human",
                abilities,
                e26.getHitPoints(),
                e26.getMaxHitPoints(),
                e26.getExperiencePoints(),
                e26.getLevel(),
                skills,
                e26.getEquipment(),
                "HEALTHY",
                true,
                GameType.EARTH_2026,
                metadata
        );
    }

    @Override
    public Earth2026Character fromUnified(UnifiedCharacter unified) {
        Earth2026Character e26 = new Earth2026Character();
        e26.setName(unified.getName());
        e26.setAge(unified.getAge());
        e26.setStrScore(unified.getAbility("STR"));
        e26.setDexScore(unified.getAbility("DEX"));
        e26.setConScore(unified.getAbility("CON"));
        e26.setIntScore(unified.getAbility("INT"));
        e26.setWisScore(unified.getAbility("WIS"));
        e26.setChaScore(unified.getAbility("CHA"));
        e26.setHitPoints(unified.getHitPoints());
        e26.setMaxHitPoints(unified.getMaxHitPoints());
        e26.setExperiencePoints(unified.getExperiencePoints());
        e26.setLevel(unified.getLevel());
        e26.setEquipment(unified.getEquipment());
        return e26;
    }

    @Override
    public int normalizeAbilityScore(int sourceValue, GameType sourceType, GameType targetType) {
        if (sourceType == GameType.EARTH_2026 && targetType == GameType.EARTH_2026) {
            return sourceValue;
        }
        if (sourceType == GameType.EARTH_2026) {
            if (targetType == GameType.TRAVELLER) {
                return 2 + Math.min(10, Math.max(0, (sourceValue - 1) * 10 / 19));
            }
            if (targetType == GameType.CAPTAINS_LOG) {
                return Math.min(5, Math.max(0, (sourceValue - 1) / 4));
            }
        }
        if (targetType == GameType.EARTH_2026) {
            if (sourceType == GameType.TRAVELLER) {
                return 3 + (sourceValue - 2) * 16 / 10;
            }
            if (sourceType == GameType.CAPTAINS_LOG) {
                return 1 + sourceValue * 19 / 5;
            }
        }
        return sourceValue;
    }
}
