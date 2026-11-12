package com.yisy.modules.doramcu.unified;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Maps between {@link Dnd5eCharacter} and {@link UnifiedCharacter}.
 * <p>
 * D&D 5e uses 6 abilities (STR/DEX/CON/INT/WIS/CHA) with values 1-20.
 * TECH is derived from INT for D&D characters.
 * </p>
 */
@Component
public class Dnd5eCharacterMapper implements CharacterMapper<Dnd5eCharacter> {

    @Override
    public GameType getGameType() {
        return GameType.DND_5E;
    }

    @Override
    public UnifiedCharacter toUnified(Dnd5eCharacter dnd) {
        if (dnd == null) return null;

        Map<String, Integer> abilities = new HashMap<>();
        abilities.put("STR", dnd.getStrScore());
        abilities.put("DEX", dnd.getDexScore());
        abilities.put("CON", dnd.getConScore());
        abilities.put("INT", dnd.getIntScore());
        abilities.put("WIS", dnd.getWisScore());
        abilities.put("CHA", dnd.getChaScore());
        abilities.put("TECH", dnd.getIntScore());

        List<UnifiedCharacter.SkillEntry> skills = dnd.getSkills().stream()
                .map(s -> new SimpleSkillEntry(s.getSkillName(), s.getProficiencyBonus() > 0 ? 1 : 0))
                .collect(Collectors.toList());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("characterClass", dnd.getCharacterClass());
        metadata.put("background", dnd.getBackground());
        metadata.put("armorClass", dnd.getArmorClass());
        metadata.put("speed", dnd.getSpeed());
        metadata.put("proficiencyBonus", dnd.getProficiencyBonus());
        metadata.put("hitDice", dnd.getHitDice());
        metadata.put("deathSaveSuccesses", dnd.getDeathSaveSuccesses());
        metadata.put("deathSaveFailures", dnd.getDeathSaveFailures());
        metadata.put("tempHitPoints", dnd.getTempHitPoints());
        metadata.put("exhaustionLevel", dnd.getExhaustionLevel());
        metadata.put("inspiration", dnd.isInspiration());

        return new AbstractUnifiedCharacter(
                dnd.getId(),
                dnd.getName(),
                20 + dnd.getLevel(), // approximate age
                dnd.getRace(),
                abilities,
                dnd.getHitPoints(),
                dnd.getMaxHitPoints(),
                dnd.getExperiencePoints(),
                dnd.getLevel(),
                skills,
                dnd.getEquipment(),
                "HEALTHY",
                dnd.getHitPoints() > 0,
                GameType.DND_5E,
                metadata
        );
    }

    @Override
    public Dnd5eCharacter fromUnified(UnifiedCharacter unified) {
        Dnd5eCharacter dnd = new Dnd5eCharacter();
        dnd.setName(unified.getName());
        dnd.setRace(unified.getRace() != null ? unified.getRace() : "Human");
        dnd.setCharacterClass("Fighter");
        dnd.setLevel(unified.getLevel());
        dnd.setStrScore(unified.getAbility("STR"));
        dnd.setDexScore(unified.getAbility("DEX"));
        dnd.setConScore(unified.getAbility("CON"));
        dnd.setIntScore(unified.getAbility("INT"));
        dnd.setWisScore(unified.getAbility("WIS"));
        dnd.setChaScore(unified.getAbility("CHA"));
        dnd.setHitPoints(unified.getHitPoints());
        dnd.setMaxHitPoints(unified.getMaxHitPoints());
        dnd.setExperiencePoints(unified.getExperiencePoints());
        dnd.setEquipment(unified.getEquipment());
        return dnd;
    }

    @Override
    public int normalizeAbilityScore(int sourceValue, GameType sourceType, GameType targetType) {
        // Same as Earth2026 since both use 1-20 range
        if (sourceType == GameType.DND_5E && targetType == GameType.DND_5E) {
            return sourceValue;
        }
        if (sourceType == GameType.DND_5E) {
            if (targetType == GameType.TRAVELLER) {
                return 2 + Math.min(10, Math.max(0, (sourceValue - 1) * 10 / 19));
            }
            if (targetType == GameType.CAPTAINS_LOG) {
                return Math.min(5, Math.max(0, (sourceValue - 1) / 4));
            }
        }
        if (targetType == GameType.DND_5E) {
            if (sourceType == GameType.TRAVELLER) {
                return 3 + (sourceValue - 2) * 16 / 10;
            }
            if (sourceType == GameType.CAPTAINS_LOG) {
                return 1 + sourceValue * 19 / 5;
            }
        }
        return sourceValue;
    }

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
