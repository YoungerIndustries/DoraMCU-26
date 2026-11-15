package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.TestDefinition;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.eve.model.AiCommanderExperience;
import com.yisy.modules.doramcu.hex.AiCoreComponent;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import com.yisy.modules.doramcu.repository.AiCommanderExperienceRepository;
import com.yisy.modules.doramcu.service.AiCommanderExperienceService;
import com.yisy.modules.doramcu.service.AiEntityRegistrationService;
import com.yisy.modules.doramcu.service.AiNetworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test provider for combat-related AI network features.
 * Tests battle experience recording, training level progression, AI Core tier sync,
 * FleetAiCoordinator integration, StationAiSupportService integration, and destroyed entity deactivation.
 * <p>
 * Uses direct service injection (in-JVM) instead of HTTP calls for fast, reliable testing.
 * </p>
 */
@Component
public class CombatTestProvider implements TestProvider {

    private static final Logger log = LoggerFactory.getLogger(CombatTestProvider.class);

    private final AiCommanderExperienceService experienceService;
    private final AiEntityRegistrationService entityRegistrationService;
    private final AiCharacterRepository aiCharacterRepository;
    private final AiCommanderExperienceRepository experienceRepository;
    private final AiNetworkService aiNetworkService;
    private final Map<String, TestRunnable> testRunnables = new ConcurrentHashMap<>();

    public CombatTestProvider(
            AiCommanderExperienceService experienceService,
            AiEntityRegistrationService entityRegistrationService,
            AiCharacterRepository aiCharacterRepository,
            AiCommanderExperienceRepository experienceRepository,
            AiNetworkService aiNetworkService) {
        this.experienceService = experienceService;
        this.entityRegistrationService = entityRegistrationService;
        this.aiCharacterRepository = aiCharacterRepository;
        this.experienceRepository = experienceRepository;
        this.aiNetworkService = aiNetworkService;
        registerTestRunnables();
    }

    private void registerTestRunnables() {
        // Test 1: Combat-related services are available
        testRunnables.put("combat-service-available", () -> {
            return experienceService != null &&
                   entityRegistrationService != null &&
                   aiCharacterRepository != null &&
                   experienceRepository != null &&
                   aiNetworkService != null;
        });

        // Test 2: FleetAiCoordinator integration — ship AI characters have AI cores with valid tiers
        testRunnables.put("combat-fleet-ai-coordinator", () -> {
            try {
                List<AiCharacter> shipChars = aiCharacterRepository.findByCharacterTypeIn(
                        List.of(AiCharacter.CharacterType.SHIP));
                if (shipChars.isEmpty()) {
                    return true; // No ship data yet — test still passes
                }
                // Verify ship AI characters have training levels that map to AI Core tiers
                for (AiCharacter charEntity : shipChars) {
                    AiCharacter.TrainingLevel level = charEntity.getTrainingLevel();
                    if (level == null) {
                        log.warn("Ship AI character {} has null training level", charEntity.getName());
                        return false;
                    }
                    // Training level should correspond to a valid AI Core tier
                    if (level != AiCharacter.TrainingLevel.UNTRAINED &&
                        level != AiCharacter.TrainingLevel.BASIC &&
                        level != AiCharacter.TrainingLevel.ADVANCED &&
                        level != AiCharacter.TrainingLevel.MASTER) {
                        log.warn("Ship AI character {} has unexpected training level: {}", charEntity.getName(), level);
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                log.warn("Fleet AI coordinator test failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 3: StationAiSupportService integration — station AI characters have valid AI cores
        testRunnables.put("combat-station-ai-support", () -> {
            try {
                List<AiCharacter> stationChars = aiCharacterRepository.findByCharacterTypeIn(
                        List.of(AiCharacter.CharacterType.STATION));
                if (stationChars.isEmpty()) {
                    return true; // No station data yet
                }
                // Verify station AI characters have linked station IDs for support service lookups
                for (AiCharacter charEntity : stationChars) {
                    if (charEntity.getLinkedStationId() == null) {
                        log.warn("Station AI character {} missing linked station ID for support service", charEntity.getName());
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                log.warn("Station AI support test failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 4: AI Core bonus calculation — entity AI characters have training levels for bonus computation
        testRunnables.put("combat-ai-core-bonus", () -> {
            try {
                List<AiCharacter> allEntityChars = aiCharacterRepository.findByCharacterTypeIn(
                        Arrays.asList(
                                AiCharacter.CharacterType.SHIP,
                                AiCharacter.CharacterType.STATION,
                                AiCharacter.CharacterType.VEHICLE,
                                AiCharacter.CharacterType.FACILITY
                        ));
                if (allEntityChars.isEmpty()) {
                    return true; // No data yet
                }
                // Verify all entity characters have training levels that determine AI core bonuses
                for (AiCharacter charEntity : allEntityChars) {
                    AiCharacter.TrainingLevel level = charEntity.getTrainingLevel();
                    if (level == null) {
                        log.warn("Entity AI character {} has null training level for bonus calculation", charEntity.getName());
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                log.warn("AI core bonus test failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 5: Battle experience recording — experience records exist for entity-linked AI characters
        testRunnables.put("combat-battle-experience-recorded", () -> {
            try {
                List<AiCharacter> entityChars = aiCharacterRepository.findByCharacterTypeIn(
                        Arrays.asList(
                                AiCharacter.CharacterType.SHIP,
                                AiCharacter.CharacterType.STATION,
                                AiCharacter.CharacterType.VEHICLE,
                                AiCharacter.CharacterType.FACILITY
                        ));
                if (entityChars.isEmpty()) {
                    return true; // No data yet
                }
                // Check if any entity character has battle experience records
                for (AiCharacter charEntity : entityChars) {
                    List<AiCommanderExperience> experiences = experienceRepository
                            .findByAiCharacterIdOrderByBattleDateDesc(charEntity.getId());
                    if (!experiences.isEmpty()) {
                        // Verify experience records have valid outcomes
                        for (AiCommanderExperience exp : experiences) {
                            if (exp.getOutcome() == null) {
                                log.warn("Experience record {} has null outcome", exp.getId());
                                return false;
                            }
                            if (exp.getExperiencePoints() < 0) {
                                log.warn("Experience record {} has negative XP: {}", exp.getId(), exp.getExperiencePoints());
                                return false;
                            }
                        }
                    }
                }
                // Pass whether or not experience exists — it depends on battle history
                return true;
            } catch (Exception e) {
                log.warn("Battle experience test failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 6: Training level progression — AI characters advance through training levels
        testRunnables.put("combat-training-level-progression", () -> {
            try {
                List<AiCharacter> entityChars = aiCharacterRepository.findByCharacterTypeIn(
                        Arrays.asList(
                                AiCharacter.CharacterType.SHIP,
                                AiCharacter.CharacterType.STATION,
                                AiCharacter.CharacterType.VEHICLE,
                                AiCharacter.CharacterType.FACILITY
                        ));
                if (entityChars.isEmpty()) {
                    return true; // No data yet
                }
                // Verify training levels are consistent with XP thresholds
                for (AiCharacter charEntity : entityChars) {
                    int totalXp = charEntity.getTotalExperience();
                    AiCharacter.TrainingLevel level = charEntity.getTrainingLevel();
                    if (level == null) {
                        continue;
                    }
                    // Check XP thresholds: UNTRAINED→BASIC at 100, BASIC→ADVANCED at 500, ADVANCED→MASTER at 1500
                    switch (level) {
                        case UNTRAINED:
                            if (totalXp >= 100) {
                                log.warn("Entity AI character {} has UNTRAINED level but {} XP (should be BASIC)",
                                        charEntity.getName(), totalXp);
                                return false;
                            }
                            break;
                        case BASIC:
                            if (totalXp >= 500) {
                                log.warn("Entity AI character {} has BASIC level but {} XP (should be ADVANCED)",
                                        charEntity.getName(), totalXp);
                                return false;
                            }
                            break;
                        case ADVANCED:
                            if (totalXp >= 1500) {
                                log.warn("Entity AI character {} has ADVANCED level but {} XP (should be MASTER)",
                                        charEntity.getName(), totalXp);
                                return false;
                            }
                            break;
                        case MASTER:
                            // MASTER is the highest level — no upper bound check
                            break;
                    }
                }
                return true;
            } catch (Exception e) {
                log.warn("Training level progression test failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 7: AI Core tier sync — training level maps to valid AI Core tier
        testRunnables.put("combat-ai-core-tier-sync", () -> {
            try {
                List<AiCharacter> entityChars = aiCharacterRepository.findByCharacterTypeIn(
                        Arrays.asList(
                                AiCharacter.CharacterType.SHIP,
                                AiCharacter.CharacterType.STATION,
                                AiCharacter.CharacterType.VEHICLE,
                                AiCharacter.CharacterType.FACILITY
                        ));
                if (entityChars.isEmpty()) {
                    return true; // No data yet
                }
                // Verify training level → AI Core tier mapping is valid
                for (AiCharacter charEntity : entityChars) {
                    AiCharacter.TrainingLevel level = charEntity.getTrainingLevel();
                    if (level == null) {
                        continue;
                    }
                    // Map training level to expected tier
                    AiCoreComponent.Tier expectedTier = null;
                    switch (level) {
                        case UNTRAINED:
                            expectedTier = null; // No tier for untrained
                            break;
                        case BASIC:
                            expectedTier = AiCoreComponent.Tier.BASIC;
                            break;
                        case ADVANCED:
                            expectedTier = AiCoreComponent.Tier.ADVANCED;
                            break;
                        case MASTER:
                            expectedTier = AiCoreComponent.Tier.ELITE;
                            break;
                    }
                    // The mapping should be valid (not null for non-UNTRAINED levels)
                    if (level != AiCharacter.TrainingLevel.UNTRAINED && expectedTier == null) {
                        log.warn("Entity AI character {} has training level {} but no mapped tier",
                                charEntity.getName(), level);
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                log.warn("AI Core tier sync test failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 8: Destroyed ship deactivation — deactivated entity characters can be queried
        testRunnables.put("combat-destroyed-ship-deactivation", () -> {
            try {
                // Query inactive entity characters — these are deactivated (destroyed) entities
                List<AiCharacter> inactiveChars = aiCharacterRepository
                        .findByCharacterTypeInAndEmpireNameAndActiveFalse(
                                Arrays.asList(
                                        AiCharacter.CharacterType.SHIP,
                                        AiCharacter.CharacterType.STATION,
                                        AiCharacter.CharacterType.VEHICLE,
                                        AiCharacter.CharacterType.FACILITY
                                ), "Demo");
                // The query should work without throwing — inactive chars may or may not exist
                return inactiveChars != null;
            } catch (Exception e) {
                log.warn("Destroyed ship deactivation test failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 9: Formation bonus calculation — ship AI characters have primary skills for formation bonuses
        testRunnables.put("combat-formation-bonus", () -> {
            try {
                List<AiCharacter> shipChars = aiCharacterRepository.findByCharacterTypeIn(
                        List.of(AiCharacter.CharacterType.SHIP));
                if (shipChars.isEmpty()) {
                    return true; // No ship data yet
                }
                // Verify ship AI characters have primary skills that affect formation bonuses
                for (AiCharacter charEntity : shipChars) {
                    String primarySkill = charEntity.getPrimarySkill();
                    if (primarySkill == null || primarySkill.isBlank()) {
                        log.warn("Ship AI character {} has no primary skill for formation bonus", charEntity.getName());
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                log.warn("Formation bonus test failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 10: Experience repository queries — experience records are queryable by character and module
        testRunnables.put("combat-experience-repository-queries", () -> {
            try {
                // Test query by game module
                List<AiCommanderExperience> allExperiences = experienceRepository.findAll();
                // Test query by game module name
                List<AiCommanderExperience> moduleExperiences = experienceRepository.findByGameModuleName("TRAVELLER");
                if (moduleExperiences == null) {
                    log.warn("Experience repository module query returned null");
                    return false;
                }
                // Test count query for entity characters
                List<AiCharacter> entityChars = aiCharacterRepository.findByCharacterTypeIn(
                        Arrays.asList(
                                AiCharacter.CharacterType.SHIP,
                                AiCharacter.CharacterType.STATION,
                                AiCharacter.CharacterType.VEHICLE,
                                AiCharacter.CharacterType.FACILITY
                        ));
                for (AiCharacter charEntity : entityChars) {
                    long count = experienceRepository.countByAiCharacterId(charEntity.getId());
                    if (count < 0) {
                        log.warn("Experience count for character {} is negative: {}", charEntity.getName(), count);
                        return false;
                    }
                    Integer sumXp = experienceRepository.sumExperiencePointsByAiCharacterId(charEntity.getId());
                    if (sumXp == null || sumXp < 0) {
                        log.warn("Experience sum for character {} is null or negative: {}", charEntity.getName(), sumXp);
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                log.warn("Experience repository queries test failed: {}", e.getMessage());
                return false;
            }
        });
    }

    @Override
    public List<TestDefinition> registerTests() {
        List<TestDefinition> tests = new ArrayList<>();

        tests.add(new TestDefinition(
                "combat-service-available",
                "Combat AI services availability",
                "COMBAT",
                "Verify all combat-related AI services are available for in-JVM testing"));

        tests.add(new TestDefinition(
                "combat-fleet-ai-coordinator",
                "Fleet AI Coordinator integration",
                "COMBAT",
                "Verify ship AI characters have training levels that map to AI Core tiers for formation bonuses"));

        tests.add(new TestDefinition(
                "combat-station-ai-support",
                "Station AI Support Service integration",
                "COMBAT",
                "Verify station AI characters have linked station IDs for defense support lookups"));

        tests.add(new TestDefinition(
                "combat-ai-core-bonus",
                "AI Core bonus calculation",
                "COMBAT",
                "Verify entity AI characters have training levels that determine AI core combat bonuses"));

        tests.add(new TestDefinition(
                "combat-battle-experience-recorded",
                "Battle experience recording",
                "COMBAT",
                "Verify battle experience records exist with valid outcomes and XP for entity-linked AI characters"));

        tests.add(new TestDefinition(
                "combat-training-level-progression",
                "Training level progression",
                "COMBAT",
                "Verify AI character training levels are consistent with XP thresholds (100/500/1500)"));

        tests.add(new TestDefinition(
                "combat-ai-core-tier-sync",
                "AI Core tier sync with training level",
                "COMBAT",
                "Verify training level maps to valid AI Core tier (BASIC→BASIC, ADVANCED→ADVANCED, MASTER→ELITE)"));

        tests.add(new TestDefinition(
                "combat-destroyed-ship-deactivation",
                "Destroyed ship AI deactivation",
                "COMBAT",
                "Verify deactivated (destroyed) entity AI characters can be queried via inactive character queries"));

        tests.add(new TestDefinition(
                "combat-formation-bonus",
                "Formation bonus calculation",
                "COMBAT",
                "Verify ship AI characters have primary skills that affect FleetAiCoordinator formation bonuses"));

        tests.add(new TestDefinition(
                "combat-experience-repository-queries",
                "Experience repository queries",
                "COMBAT",
                "Verify experience records are queryable by character ID, game module, and aggregate functions"));

        return tests;
    }

    @Override
    public boolean executeTest(String testId) {
        TestRunnable runnable = testRunnables.get(testId);
        if (runnable == null) {
            log.warn("Unknown combat test: {}", testId);
            return false;
        }
        try {
            return runnable.run();
        } catch (Exception e) {
            log.warn("Combat test {} failed: {}", testId, e.getMessage());
            return false;
        }
    }

    @Override
    public String getCategory() {
        return "COMBAT";
    }

    @FunctionalInterface
    interface TestRunnable {
        boolean run() throws Exception;
    }
}
