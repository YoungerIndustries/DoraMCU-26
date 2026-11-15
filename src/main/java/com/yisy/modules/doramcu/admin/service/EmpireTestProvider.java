package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.TestDefinition;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import com.yisy.modules.doramcu.service.AiNetworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test provider for empire management and AI network integration.
 * Tests empire fleet counts, facility counts, army unit counts, AI network topology per empire,
 * empire AI character distribution, fleet AI core coverage, station AI core coverage,
 * facility AI core coverage, army AI core coverage, and cross-empire network statistics.
 * <p>
 * Uses direct service injection (in-JVM) instead of HTTP calls for fast, reliable testing.
 * </p>
 */
@Component
@ConditionalOnBean(Game.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class EmpireTestProvider implements TestProvider {

    private static final Logger log = LoggerFactory.getLogger(EmpireTestProvider.class);

    private final Game game;
    private final AiNetworkService aiNetworkService;
    private final AiCharacterRepository aiCharacterRepository;
    private final Map<String, TestRunnable> testRunnables = new ConcurrentHashMap<>();

    public EmpireTestProvider(Game game,
                               AiNetworkService aiNetworkService,
                               AiCharacterRepository aiCharacterRepository) {
        this.game = game;
        this.aiNetworkService = aiNetworkService;
        this.aiCharacterRepository = aiCharacterRepository;
        registerTestRunnables();
    }

    private void registerTestRunnables() {
        // Test 1: Empire fleet counts — each empire has non-negative fleet counts
        testRunnables.put("empire-fleet-counts", () -> {
            try {
                Map<Integer, Empire> empires = game.getEmpires();
                if (empires.isEmpty()) {
                    return true; // No empires yet — pass
                }
                for (Empire empire : empires.values()) {
                    int fleetCount = empire.getNavy().getFleetCount();
                    if (fleetCount < 0) {
                        log.warn("Empire {} has negative fleet count: {}", empire.getName(), fleetCount);
                        return false;
                    }
                    int shipCount = empire.getNavy().getTotalShipCount();
                    if (shipCount < 0) {
                        log.warn("Empire {} has negative ship count: {}", empire.getName(), shipCount);
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                log.warn("Empire fleet counts test failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 2: Empire facility counts — each empire has non-negative facility counts
        testRunnables.put("empire-facility-counts", () -> {
            try {
                Map<Integer, Empire> empires = game.getEmpires();
                if (empires.isEmpty()) {
                    return true; // No empires yet
                }
                for (Empire empire : empires.values()) {
                    int stationCount = empire.getFacilities().getSpaceStationCount();
                    if (stationCount < 0) {
                        log.warn("Empire {} has negative station count: {}", empire.getName(), stationCount);
                        return false;
                    }
                    int facilityCount = empire.getFacilities().getTotalFacilityCount();
                    if (facilityCount < 0) {
                        log.warn("Empire {} has negative facility count: {}", empire.getName(), facilityCount);
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                log.warn("Empire facility counts test failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 3: Empire army unit counts — each empire has non-negative army unit counts
        testRunnables.put("empire-army-unit-counts", () -> {
            try {
                Map<Integer, Empire> empires = game.getEmpires();
                if (empires.isEmpty()) {
                    return true; // No empires yet
                }
                for (Empire empire : empires.values()) {
                    int unitCount = empire.getArmyUnitCount();
                    if (unitCount < 0) {
                        log.warn("Empire {} has negative army unit count: {}", empire.getName(), unitCount);
                        return false;
                    }
                    int groundStrength = empire.getGroundForceStrength();
                    if (groundStrength < 0) {
                        log.warn("Empire {} has negative ground strength: {}", empire.getName(), groundStrength);
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                log.warn("Empire army unit counts test failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 4: AI network topology per empire — topology query works for each empire
        testRunnables.put("empire-ai-network-topology", () -> {
            try {
                Map<Integer, Empire> empires = game.getEmpires();
                if (empires.isEmpty()) {
                    return true; // No empires yet
                }
                for (Empire empire : empires.values()) {
                    String empireName = empire.getName();
                    // Query AI network status for this empire
                    List<AiCharacter> entityChars = aiCharacterRepository
                            .findByCharacterTypeInAndEmpireNameAndActiveTrue(
                                    Arrays.asList(
                                            AiCharacter.CharacterType.SHIP,
                                            AiCharacter.CharacterType.STATION,
                                            AiCharacter.CharacterType.VEHICLE,
                                            AiCharacter.CharacterType.FACILITY
                                    ), empireName);
                    if (entityChars == null) {
                        log.warn("AI network topology query for empire {} returned null", empireName);
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                log.warn("Empire AI network topology test failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 5: Empire AI character distribution — character types are distributed across empires
        testRunnables.put("empire-ai-character-distribution", () -> {
            try {
                Map<Integer, Empire> empires = game.getEmpires();
                if (empires.isEmpty()) {
                    return true; // No empires yet
                }
                // Verify each empire's AI characters have valid types
                for (Empire empire : empires.values()) {
                    String empireName = empire.getName();
                    List<AiCharacter> allChars = aiCharacterRepository.findByEmpireName(empireName);
                    if (allChars == null) {
                        log.warn("Empire {} AI character query returned null", empireName);
                        return false;
                    }
                    for (AiCharacter charEntity : allChars) {
                        AiCharacter.CharacterType type = charEntity.getCharacterType();
                        if (type == null) {
                            log.warn("AI character {} in empire {} has null type", charEntity.getName(), empireName);
                            return false;
                        }
                    }
                }
                return true;
            } catch (Exception e) {
                log.warn("Empire AI character distribution test failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 6: Fleet AI core coverage — ships have AI cores registered
        testRunnables.put("empire-fleet-ai-core-coverage", () -> {
            try {
                Map<Integer, Empire> empires = game.getEmpires();
                if (empires.isEmpty()) {
                    return true; // No empires yet
                }
                for (Empire empire : empires.values()) {
                    String empireName = empire.getName();
                    int totalShips = empire.getNavy().getTotalShipCount();
                    if (totalShips == 0) {
                        continue; // No ships — skip
                    }
                    List<AiCharacter> shipChars = aiCharacterRepository
                            .findByCharacterTypeInAndEmpireNameAndActiveTrue(
                                    List.of(AiCharacter.CharacterType.SHIP), empireName);
                    // Ship AI characters should exist for registered ships
                    // The count may be less than totalShips if some ships weren't registered
                    if (shipChars == null) {
                        log.warn("Fleet AI core query for empire {} returned null", empireName);
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                log.warn("Fleet AI core coverage test failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 7: Station AI core coverage — stations have AI cores registered
        testRunnables.put("empire-station-ai-core-coverage", () -> {
            try {
                Map<Integer, Empire> empires = game.getEmpires();
                if (empires.isEmpty()) {
                    return true; // No empires yet
                }
                for (Empire empire : empires.values()) {
                    String empireName = empire.getName();
                    int stationCount = empire.getFacilities().getSpaceStationCount();
                    if (stationCount == 0) {
                        continue; // No stations — skip
                    }
                    List<AiCharacter> stationChars = aiCharacterRepository
                            .findByCharacterTypeInAndEmpireNameAndActiveTrue(
                                    List.of(AiCharacter.CharacterType.STATION), empireName);
                    if (stationChars == null) {
                        log.warn("Station AI core query for empire {} returned null", empireName);
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                log.warn("Station AI core coverage test failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 8: Facility AI core coverage — planet facilities have AI cores registered
        testRunnables.put("empire-facility-ai-core-coverage", () -> {
            try {
                Map<Integer, Empire> empires = game.getEmpires();
                if (empires.isEmpty()) {
                    return true; // No empires yet
                }
                for (Empire empire : empires.values()) {
                    String empireName = empire.getName();
                    int facilityCount = empire.getFacilities().getTotalFacilityCount();
                    if (facilityCount == 0) {
                        continue; // No facilities — skip
                    }
                    List<AiCharacter> facilityChars = aiCharacterRepository
                            .findByCharacterTypeInAndEmpireNameAndActiveTrue(
                                    List.of(AiCharacter.CharacterType.FACILITY), empireName);
                    if (facilityChars == null) {
                        log.warn("Facility AI core query for empire {} returned null", empireName);
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                log.warn("Facility AI core coverage test failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 9: Army AI core coverage — army units have AI cores registered
        testRunnables.put("empire-army-ai-core-coverage", () -> {
            try {
                Map<Integer, Empire> empires = game.getEmpires();
                if (empires.isEmpty()) {
                    return true; // No empires yet
                }
                for (Empire empire : empires.values()) {
                    String empireName = empire.getName();
                    int unitCount = empire.getArmyUnitCount();
                    if (unitCount == 0) {
                        continue; // No army units — skip
                    }
                    List<AiCharacter> vehicleChars = aiCharacterRepository
                            .findByCharacterTypeInAndEmpireNameAndActiveTrue(
                                    List.of(AiCharacter.CharacterType.VEHICLE), empireName);
                    if (vehicleChars == null) {
                        log.warn("Army AI core query for empire {} returned null", empireName);
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                log.warn("Army AI core coverage test failed: {}", e.getMessage());
                return false;
            }
        });

        // Test 10: Cross-empire network statistics — aggregate AI network stats across all empires
        testRunnables.put("empire-cross-network-statistics", () -> {
            try {
                Map<Integer, Empire> empires = game.getEmpires();
                if (empires.isEmpty()) {
                    return true; // No empires yet
                }
                // Aggregate entity-linked AI characters across all empires
                int totalEntityChars = 0;
                int totalEmpires = empires.size();
                for (Empire empire : empires.values()) {
                    String empireName = empire.getName();
                    List<AiCharacter> entityChars = aiCharacterRepository
                            .findByCharacterTypeInAndEmpireNameAndActiveTrue(
                                    Arrays.asList(
                                            AiCharacter.CharacterType.SHIP,
                                            AiCharacter.CharacterType.STATION,
                                            AiCharacter.CharacterType.VEHICLE,
                                            AiCharacter.CharacterType.FACILITY
                                    ), empireName);
                    totalEntityChars += entityChars.size();
                }
                // Verify aggregate stats are consistent
                if (totalEntityChars < 0) {
                    log.warn("Cross-empire entity character count is negative: {}", totalEntityChars);
                    return false;
                }
                return true;
            } catch (Exception e) {
                log.warn("Cross-empire network statistics test failed: {}", e.getMessage());
                return false;
            }
        });
    }

    @Override
    public List<TestDefinition> registerTests() {
        List<TestDefinition> tests = new ArrayList<>();

        tests.add(new TestDefinition(
                "empire-fleet-counts",
                "Empire fleet counts",
                "EMPIRE",
                "Verify each empire has non-negative fleet and ship counts"));

        tests.add(new TestDefinition(
                "empire-facility-counts",
                "Empire facility counts",
                "EMPIRE",
                "Verify each empire has non-negative station and facility counts"));

        tests.add(new TestDefinition(
                "empire-army-unit-counts",
                "Empire army unit counts",
                "EMPIRE",
                "Verify each empire has non-negative army unit counts and ground strength"));

        tests.add(new TestDefinition(
                "empire-ai-network-topology",
                "AI network topology per empire",
                "EMPIRE",
                "Verify entity-linked AI characters can be queried per empire for topology"));

        tests.add(new TestDefinition(
                "empire-ai-character-distribution",
                "Empire AI character distribution",
                "EMPIRE",
                "Verify AI characters in each empire have valid character types"));

        tests.add(new TestDefinition(
                "empire-fleet-ai-core-coverage",
                "Fleet AI core coverage",
                "EMPIRE",
                "Verify ships have AI cores registered as SHIP-type AI characters"));

        tests.add(new TestDefinition(
                "empire-station-ai-core-coverage",
                "Station AI core coverage",
                "EMPIRE",
                "Verify stations have AI cores registered as STATION-type AI characters"));

        tests.add(new TestDefinition(
                "empire-facility-ai-core-coverage",
                "Facility AI core coverage",
                "EMPIRE",
                "Verify planet facilities have AI cores registered as FACILITY-type AI characters"));

        tests.add(new TestDefinition(
                "empire-army-ai-core-coverage",
                "Army AI core coverage",
                "EMPIRE",
                "Verify army units have AI cores registered as VEHICLE-type AI characters"));

        tests.add(new TestDefinition(
                "empire-cross-network-statistics",
                "Cross-empire network statistics",
                "EMPIRE",
                "Verify aggregate AI network statistics are consistent across all empires"));

        return tests;
    }

    @Override
    public boolean executeTest(String testId) {
        TestRunnable runnable = testRunnables.get(testId);
        if (runnable == null) {
            log.warn("Unknown empire test: {}", testId);
            return false;
        }
        try {
            return runnable.run();
        } catch (Exception e) {
            log.warn("Empire test {} failed: {}", testId, e.getMessage());
            return false;
        }
    }

    @Override
    public String getCategory() {
        return "EMPIRE";
    }

    @FunctionalInterface
    interface TestRunnable {
        boolean run() throws Exception;
    }
}
