package com.yisy.modules.doramcu.unified;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Maps between {@link CaptainLogCharacter} and {@link UnifiedCharacter}.
 * <p>
 * Captain's Log uses 6 attributes (FIT/TEC/STR/REA/INS/PRE) with values 0-5 (ranks).
 * The mapping to unified stats is:
 * <ul>
 *   <li>FIT -> DEX</li>
 *   <li>TEC -> TECH</li>
 *   <li>STR -> STR</li>
 *   <li>REA -> INT</li>
 *   <li>INS -> WIS</li>
 *   <li>PRE -> CHA</li>
 *   <li>CONstitution -> CON</li>
 * </ul>
 * </p>
 */
@Component
public class CaptainLogCharacterMapper implements CharacterMapper<CaptainLogCharacter> {

    @Override
    public GameType getGameType() {
        return GameType.CAPTAINS_LOG;
    }

    @Override
    public UnifiedCharacter toUnified(CaptainLogCharacter cl) {
        if (cl == null) return null;

        Map<String, Integer> abilities = new HashMap<>();
        abilities.put("STR", cl.getStrength());
        abilities.put("DEX", cl.getFitness());
        abilities.put("CON", cl.getConstitution());
        abilities.put("INT", cl.getReason());
        abilities.put("WIS", cl.getInsight());
        abilities.put("CHA", cl.getPresence());
        abilities.put("TECH", cl.getTechnical());

        List<UnifiedCharacter.SkillEntry> skills = cl.getFocuses().stream()
                .map(f -> new SimpleSkillEntry(f.getName(), 1))
                .collect(Collectors.toList());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("rank", cl.getRank().name());
        metadata.put("assignment", cl.getAssignment().name());
        metadata.put("environment", cl.getEnvironment().name());
        metadata.put("outlook", cl.getOutlook().name());
        metadata.put("education", cl.getEducation().name());
        metadata.put("trait1", cl.getTrait1());
        metadata.put("trait2", cl.getTrait2());
        metadata.put("trait3", cl.getTrait3());
        metadata.put("command", cl.getCommand());
        metadata.put("leadership", cl.getLeadership());
        metadata.put("science", cl.getScience());
        metadata.put("security", cl.getSecurity());
        metadata.put("medicine", cl.getMedicine());
        metadata.put("operations", cl.getOperations());
        metadata.put("careerEvents", cl.getCareerEvents());
        metadata.put("missionsCompleted", cl.getMissionsCompleted());
        metadata.put("injuryState", cl.getInjuryState().name());

        return new AbstractUnifiedCharacter(
                cl.getId(),
                cl.getName(),
                25, // default age
                "Human",
                abilities,
                cl.getConstitution(),
                cl.getConstitution(),
                cl.getExperiencePoints(),
                cl.getMissionsCompleted(),
                skills,
                cl.getEquipment(),
                cl.getInjuryState().name(),
                cl.getInjuryState() != CaptainLogCharacter.InjuryState.DOWN,
                GameType.CAPTAINS_LOG,
                metadata
        );
    }

    @Override
    public CaptainLogCharacter fromUnified(UnifiedCharacter unified) {
        CaptainLogCharacter cl = new CaptainLogCharacter();
        cl.setName(unified.getName());
        cl.setStrength(unified.getAbility("STR"));
        cl.setFitness(unified.getAbility("DEX"));
        cl.setConstitution(unified.getAbility("CON"));
        cl.setReason(unified.getAbility("INT"));
        cl.setInsight(unified.getAbility("WIS"));
        cl.setPresence(unified.getAbility("CHA"));
        cl.setTechnical(unified.getAbility("TECH"));
        cl.setEquipment(unified.getEquipment());
        return cl;
    }

    @Override
    public int normalizeAbilityScore(int sourceValue, GameType sourceType, GameType targetType) {
        if (sourceType == GameType.CAPTAINS_LOG && targetType == GameType.CAPTAINS_LOG) {
            return sourceValue;
        }
        if (sourceType == GameType.CAPTAINS_LOG) {
            // Convert from 0-5 to target range
            if (targetType == GameType.EARTH_2026 || targetType == GameType.DND_5E) {
                return 1 + sourceValue * 19 / 5;
            }
            if (targetType == GameType.TRAVELLER) {
                return 2 + sourceValue * 10 / 5;
            }
        }
        if (targetType == GameType.CAPTAINS_LOG) {
            // Convert from source range to 0-5
            if (sourceType == GameType.EARTH_2026 || sourceType == GameType.DND_5E) {
                return Math.min(5, Math.max(0, (sourceValue - 1) / 4));
            }
            if (sourceType == GameType.TRAVELLER) {
                return Math.min(5, Math.max(0, (sourceValue - 2) / 2));
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
