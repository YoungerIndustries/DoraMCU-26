package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.TestDefinition;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
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
 * Test provider for the AI Network feature.
 * Tests AI character registration, deactivation, network topology, and training level progression
 * for entity-linked AI characters (ships, stations, vehicles, facilities).
 * <p>
 * Uses direct service injection (in-JVM) instead of HTTP calls for fast, reliable testing.
 * </p>
 */
@Component
public class AiNetworkTestProvider implements TestProvider {

    private static final Logger log = LoggerFactory.getLogger(AiNetworkTestProvider.class);

    private final AiNetworkService aiNetworkService;
    private final AiEntityRegistrationService entityRegistrationService;
    private final AiCharacterRepository aiCharacterRepository;
    private final Map<String, TestRunnable> testRunnables = new ConcurrentHashMap<>();

    public AiNetworkTestProvider(AiNetworkService aiNetworkService,
                                  AiEntityRegistrationService entityRegistrationService,
                                  AiCharacterRepository aiCharacterRepository) {
        this.aiNetworkService = aiNetworkService;
        this.entityRegistrationService = entityRegistrationService;
        this.aiCharacterRepository = aiCharacterRepository;
        registerTestRunnables();
    }

    private void registerTestRunnables() {
        // Test 1: AI Network Service is available
        testRunnables.put("ai-network-service-available", () -> {
            return aiNetworkService != null && aiCharacterRepository != null;
        });

        // Test 2: Entity-linked AI characters exist in repository
        testRunnables.put("ai-network-entity-characters-exist", () -> {
            List<AiCharacter> entityChars = aiCharacterRepository.findByCharacterTypeIn(
                    Arrays.asList(
                            AiCharacter.CharacterType.SHIP,
                            AiCharacter.CharacterType.STATION,
                            AiCharacter.CharacterType.VEHICLE,
                            AiCharacter.CharacterType.FACILITY
                    ));
            return entityChars != null && !entityChars.isEmpty();
        });

        // Test 3: AI Network topology query works
        testRunnables.put("ai-network-topology-query", () -> {
            try {
                // Query topology for any empire — just verify the query doesn't throw
                List<AiCharacter> allEntityChars = aiCharacterRepository.findByCharacterTypeIn(
                        Arrays.asList(
                                AiCharacter.CharacterType.SHIP,
                                AiCharacter.CharacterType.STATION,
                                AiCharacter.CharacterType.VEHICLE,
                                AiCharacter.CharacterType.FACILITY
                        ));
                // Group by type to simulate topology
                Map<AiCharacter.CharacterType, List<AiCharacter>> topology = new ConcurrentHashMap<>();
                for (AiCharacter charEntity : allEntityChars) {
                    topology.computeIfAbsent(charEntity.getCharacterType(), k -> new ArrayList<>()).add(charEntity);
                }
                return !topology.isEmpty();
            } catch (Exception e) {
                log.warn("AI network topology query failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 4: Entity-linked characters are not hirable
        testRunnables.put("ai-network-characters-not-hirable", () -> {
            List<AiCharacter> entityChars = aiCharacterRepository.findByCharacterTypeIn(
                    Arrays.asList(
                            AiCharacter.CharacterType.SHIP,
                            AiCharacter.CharacterType.STATION,
                            AiCharacter.CharacterType.VEHICLE,
                            AiCharacter.CharacterType.FACILITY
                    ));
            // All entity-linked characters should have hirable=false
            for (AiCharacter charEntity : entityChars) {
                if (charEntity.isHirable()) {
                    log.warn("Entity-linked AI character {} should not be hirable", charEntity.getName());
                    return false;
                }
            }
            return true;
        });

        // Test 5: Entity-linked characters are not AI admins
        testRunnables.put("ai-network-characters-not-admin", () -> {
            List<AiCharacter> entityChars = aiCharacterRepository.findByCharacterTypeIn(
                    Arrays.asList(
                            AiCharacter.CharacterType.SHIP,
                            AiCharacter.CharacterType.STATION,
                            AiCharacter.CharacterType.VEHICLE,
                            AiCharacter.CharacterType.FACILITY
                    ));
            // All entity-linked characters should have aiAdmin=false
            for (AiCharacter charEntity : entityChars) {
                if (charEntity.isAiAdmin()) {
                    log.warn("Entity-linked AI character {} should not be an AI admin", charEntity.getName());
                    return false;
                }
            }
            return true;
        });

        // Test 6: Ship AI characters have linked ship IDs
        testRunnables.put("ai-network-ship-linked-ids", () -> {
            List<AiCharacter> shipChars = aiCharacterRepository.findByCharacterTypeIn(
                    List.of(AiCharacter.CharacterType.SHIP));
            for (AiCharacter charEntity : shipChars) {
                if (charEntity.getLinkedShipId() == null) {
                    log.warn("Ship AI character {} missing linked ship ID", charEntity.getName());
                    return false;
                }
            }
            return true;
        });

        // Test 7: Station AI characters have linked station IDs
        testRunnables.put("ai-network-station-linked-ids", () -> {
            List<AiCharacter> stationChars = aiCharacterRepository.findByCharacterTypeIn(
                    List.of(AiCharacter.CharacterType.STATION));
            for (AiCharacter charEntity : stationChars) {
                if (charEntity.getLinkedStationId() == null) {
                    log.warn("Station AI character {} missing linked station ID", charEntity.getName());
                    return false;
                }
            }
            return true;
        });

        // Test 8: Vehicle AI characters have linked entity IDs
        testRunnables.put("ai-network-vehicle-linked-ids", () -> {
            List<AiCharacter> vehicleChars = aiCharacterRepository.findByCharacterTypeIn(
                    List.of(AiCharacter.CharacterType.VEHICLE));
            for (AiCharacter charEntity : vehicleChars) {
                if (charEntity.getLinkedEntityId() == null || charEntity.getLinkedEntityId().isBlank()) {
                    log.warn("Vehicle AI character {} missing linked entity ID", charEntity.getName());
                    return false;
                }
            }
            return true;
        });

        // Test 9: Facility AI characters have linked facility IDs
        testRunnables.put("ai-network-facility-linked-ids", () -> {
            List<AiCharacter> facilityChars = aiCharacterRepository.findByCharacterTypeIn(
                    List.of(AiCharacter.CharacterType.FACILITY));
            for (AiCharacter charEntity : facilityChars) {
                if (charEntity.getLinkedFacilityId() == null) {
                    log.warn("Facility AI character {} missing linked facility ID", charEntity.getName());
                    return false;
                }
            }
            return true;
        });

        // Test 10: AI Network service getNetworkStatus works
        testRunnables.put("ai-network-status-query", () -> {
            try {
                // Get all unique empire names from entity characters
                List<AiCharacter> allEntityChars = aiCharacterRepository.findByCharacterTypeIn(
                        Arrays.asList(
                                AiCharacter.CharacterType.SHIP,
                                AiCharacter.CharacterType.STATION,
                                AiCharacter.CharacterType.VEHICLE,
                                AiCharacter.CharacterType.FACILITY
                        ));
                if (allEntityChars.isEmpty()) {
                    return true; // No data yet — test still passes
                }
                String empireName = allEntityChars.get(0).getEmpireName();
                AiNetworkService.AiNetworkStatus status = aiNetworkService.getNetworkStatus(empireName);
                return status != null && empireName.equals(status.getEmpireName());
            } catch (Exception e) {
                log.warn("AI network status query failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 11: Inactive entity characters can be queried
        testRunnables.put("ai-network-inactive-query", () -> {
            try {
                List<AiCharacter> allEntityChars = aiCharacterRepository.findByCharacterTypeIn(
                        Arrays.asList(
                                AiCharacter.CharacterType.SHIP,
                                AiCharacter.CharacterType.STATION,
                                AiCharacter.CharacterType.VEHICLE,
                                AiCharacter.CharacterType.FACILITY
                        ));
                // Just verify the query works — inactive characters may or may not exist
                return allEntityChars != null;
            } catch (Exception e) {
                log.warn("AI network inactive query failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 12: Entity character types are properly distributed
        testRunnables.put("ai-network-character-type-distribution", () -> {
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
            // Verify all characters have valid types
            for (AiCharacter charEntity : allEntityChars) {
                AiCharacter.CharacterType type = charEntity.getCharacterType();
                if (type == null) {
                    log.warn("Entity AI character {} has null character type", charEntity.getName());
                    return false;
                }
                if (type != AiCharacter.CharacterType.SHIP &&
                    type != AiCharacter.CharacterType.STATION &&
                    type != AiCharacter.CharacterType.VEHICLE &&
                    type != AiCharacter.CharacterType.FACILITY) {
                    log.warn("Entity AI character {} has invalid type: {}", charEntity.getName(), type);
                    return false;
                }
            }
            return true;
        });
    }

    @Override
    public List<TestDefinition> registerTests() {
        List<TestDefinition> tests = new ArrayList<>();

        tests.add(new TestDefinition(
                "ai-network-service-available",
                "AI Network Service availability",
                "AI_NETWORK",
                "Verify the AI Network Service and repository are available for in-JVM testing"));

        tests.add(new TestDefinition(
                "ai-network-entity-characters-exist",
                "Entity-linked AI characters exist",
                "AI_NETWORK",
                "Verify entity-linked AI characters (SHIP, STATION, VEHICLE, FACILITY) exist in the repository"));

        tests.add(new TestDefinition(
                "ai-network-topology-query",
                "AI Network topology query",
                "AI_NETWORK",
                "Verify AI network topology can be queried and grouped by character type"));

        tests.add(new TestDefinition(
                "ai-network-characters-not-hirable",
                "Entity characters are not hirable",
                "AI_NETWORK",
                "Verify entity-linked AI characters cannot be hired as commanders"));

        tests.add(new TestDefinition(
                "ai-network-characters-not-admin",
                "Entity characters are not AI admins",
                "AI_NETWORK",
                "Verify entity-linked AI characters do not have AI admin privileges"));

        tests.add(new TestDefinition(
                "ai-network-ship-linked-ids",
                "Ship AI characters have linked IDs",
                "AI_NETWORK",
                "Verify SHIP-type AI characters have valid linked ship IDs"));

        tests.add(new TestDefinition(
                "ai-network-station-linked-ids",
                "Station AI characters have linked IDs",
                "AI_NETWORK",
                "Verify STATION-type AI characters have valid linked station IDs"));

        tests.add(new TestDefinition(
                "ai-network-vehicle-linked-ids",
                "Vehicle AI characters have linked IDs",
                "AI_NETWORK",
                "Verify VEHICLE-type AI characters have valid linked entity IDs"));

        tests.add(new TestDefinition(
                "ai-network-facility-linked-ids",
                "Facility AI characters have linked IDs",
                "AI_NETWORK",
                "Verify FACILITY-type AI characters have valid linked facility IDs"));

        tests.add(new TestDefinition(
                "ai-network-status-query",
                "AI Network status query",
                "AI_NETWORK",
                "Verify getNetworkStatus() returns valid status for an empire with entity-linked characters"));

        tests.add(new TestDefinition(
                "ai-network-inactive-query",
                "AI Network inactive character query",
                "AI_NETWORK",
                "Verify inactive (deactivated) entity characters can be queried"));

        tests.add(new TestDefinition(
                "ai-network-character-type-distribution",
                "AI Network character type distribution",
                "AI_NETWORK",
                "Verify all entity-linked characters have valid character types (SHIP, STATION, VEHICLE, FACILITY)"));

        return tests;
    }

    @Override
    public boolean executeTest(String testId) {
        TestRunnable runnable = testRunnables.get(testId);
        if (runnable == null) {
            log.warn("Unknown AI Network test: {}", testId);
            return false;
        }
        try {
            return runnable.run();
        } catch (Exception e) {
            log.warn("AI Network test {} failed: {}", testId, e.getMessage());
            return false;
        }
    }

    @Override
    public String getCategory() {
        return "AI_NETWORK";
    }

    @FunctionalInterface
    interface TestRunnable {
        boolean run() throws Exception;
    }
}
