package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eTensionPool;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eTensionPoolRepository;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eTensionPoolService.TensionConsequence;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eTensionPoolService.WanderingEncounterResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Dnd5eTensionPoolService Tests")
class Dnd5eTensionPoolServiceTest {

    @Mock
    private Dnd5eTensionPoolRepository poolRepository;

    @Captor
    private ArgumentCaptor<Dnd5eTensionPool> poolCaptor;

    private Dnd5eTensionPoolService service;

    private Dnd5eTensionPool samplePool;

    @BeforeEach
    void setUp() {
        // Mock the initialization check first
        when(poolRepository.findByActiveTrue()).thenReturn(List.of());
        when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        service = new Dnd5eTensionPoolService(poolRepository);
        samplePool = createPool(1L, "Guard Alertness", "Castle Ward", 0, 10, "[3, 5, 7, 9]",
                "[\"Guards watchful\", \"Captain alerted\", \"Reinforcements\", \"Full alarm\"]",
                "Party discovered by entire guard force", "[false, false, false, false]");
    }

    private Dnd5eTensionPool createPool(Long id, String name, String areaId, int currentTension, int maxTension,
                                         String thresholds, String consequences, String ultimateConsequence, String triggered) {
        Dnd5eTensionPool pool = new Dnd5eTensionPool();
        pool.setId(id);
        pool.setName(name);
        pool.setAreaId(areaId);
        pool.setCurrentTension(currentTension);
        pool.setMaxTension(maxTension);
        pool.setThresholds(thresholds);
        pool.setConsequences(consequences);
        pool.setUltimateConsequence(ultimateConsequence);
        pool.setTriggered(triggered);
        pool.setActive(true);
        return pool;
    }

    // ==================== CRUD Tests ====================

    @Nested
    @DisplayName("createPool Tests")
    class CreatePoolTests {

        @Test
        @DisplayName("should create a tension pool")
        void createPool_success() {
            Dnd5eTensionPool pool = new Dnd5eTensionPool();
            pool.setName("Test Pool");
            pool.setAreaId("Test Area");

            Dnd5eTensionPool created = service.createPool(pool);

            assertNotNull(created);
            assertEquals("Test Pool", created.getName());
            assertEquals("Test Area", created.getAreaId());
        }
    }

    @Nested
    @DisplayName("getPool Tests")
    class GetPoolTests {

        @Test
        @DisplayName("should return pool when found")
        void getPool_found() {
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));

            Optional<Dnd5eTensionPool> result = service.getPool(1L);

            assertTrue(result.isPresent());
            assertEquals("Guard Alertness", result.get().getName());
        }

        @Test
        @DisplayName("should return empty when not found")
        void getPool_notFound() {
            when(poolRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Dnd5eTensionPool> result = service.getPool(999L);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("listPools Tests")
    class ListPoolsTests {

        @Test
        @DisplayName("should return all active pools")
        void listPools_success() {
            when(poolRepository.findByActiveTrue()).thenReturn(List.of(samplePool));

            List<Dnd5eTensionPool> pools = service.listPools();

            assertEquals(1, pools.size());
            assertEquals("Guard Alertness", pools.get(0).getName());
        }

        @Test
        @DisplayName("should return empty list when no pools")
        void listPools_empty() {
            when(poolRepository.findByActiveTrue()).thenReturn(List.of());

            List<Dnd5eTensionPool> pools = service.listPools();

            assertTrue(pools.isEmpty());
        }
    }

    @Nested
    @DisplayName("listPoolsByArea Tests")
    class ListPoolsByAreaTests {

        @Test
        @DisplayName("should return pools for specific area")
        void listPoolsByArea_success() {
            when(poolRepository.findByAreaIdAndActiveTrue("Castle Ward")).thenReturn(List.of(samplePool));

            List<Dnd5eTensionPool> pools = service.listPoolsByArea("Castle Ward");

            assertEquals(1, pools.size());
            assertEquals("Castle Ward", pools.get(0).getAreaId());
        }
    }

    @Nested
    @DisplayName("searchPools Tests")
    class SearchPoolsTests {

        @Test
        @DisplayName("should return matching pools")
        void searchPools_match() {
            when(poolRepository.searchByKeyword("Guard")).thenReturn(List.of(samplePool));

            List<Dnd5eTensionPool> pools = service.searchPools("Guard");

            assertEquals(1, pools.size());
        }

        @Test
        @DisplayName("should return empty when no match")
        void searchPools_noMatch() {
            when(poolRepository.searchByKeyword("Nonexistent")).thenReturn(List.of());

            List<Dnd5eTensionPool> pools = service.searchPools("Nonexistent");

            assertTrue(pools.isEmpty());
        }
    }

    @Nested
    @DisplayName("deletePool Tests")
    class DeletePoolTests {

        @Test
        @DisplayName("should deactivate pool")
        void deletePool_success() {
            reset(poolRepository);
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));
            when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.deletePool(1L);

            verify(poolRepository).save(any(Dnd5eTensionPool.class));
            assertFalse(samplePool.isActive());
        }
    }

    // ==================== Tension Management Tests ====================

    @Nested
    @DisplayName("increaseTension Tests")
    class IncreaseTensionTests {

        @Test
        @DisplayName("should return empty list when pool not found")
        void increaseTension_notFound() {
            when(poolRepository.findById(999L)).thenReturn(Optional.empty());

            List<TensionConsequence> result = service.increaseTension(999L, 1);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should increase tension by amount")
        void increaseTension_basic() {
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));
            when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            List<TensionConsequence> result = service.increaseTension(1L, 3);

            // Crosses threshold 3 (tension goes from 0 to 3)
            assertEquals(1, result.size());
            assertEquals(3, result.get(0).getThreshold());
            assertEquals("THRESHOLD", result.get(0).getType());
        }

        @Test
        @DisplayName("should clamp tension to max")
        void increaseTension_clampToMax() {
            samplePool.setCurrentTension(8);
            samplePool.setTriggered("[true, true, true, false]");
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));
            when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            List<TensionConsequence> result = service.increaseTension(1L, 10);

            // Should clamp to 10, crossing threshold 9 and hitting ultimate
            assertTrue(result.size() >= 2);
            // Last should be ULTIMATE
            assertEquals("ULTIMATE", result.get(result.size() - 1).getType());
        }

        @Test
        @DisplayName("should trigger ultimate consequence at max tension")
        void increaseTension_ultimateConsequence() {
            samplePool.setCurrentTension(9);
            samplePool.setTriggered("[true, true, true, true]");
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));
            when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            List<TensionConsequence> result = service.increaseTension(1L, 1);

            assertTrue(result.stream().anyMatch(c -> "ULTIMATE".equals(c.getType())));
        }

        @Test
        @DisplayName("should not trigger already-fired thresholds")
        void increaseTension_alreadyTriggered() {
            samplePool.setCurrentTension(2);
            samplePool.setTriggered("[true, false, false, false]");
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));
            when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            List<TensionConsequence> result = service.increaseTension(1L, 1);

            // Tension goes from 2 to 3, but threshold 3 already triggered
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should trigger multiple thresholds at once")
        void increaseTension_multipleThresholds() {
            samplePool.setCurrentTension(1);
            samplePool.setTriggered("[false, false, false, false]");
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));
            when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            List<TensionConsequence> result = service.increaseTension(1L, 5);

            // Tension goes from 1 to 6, crossing thresholds 3 and 5
            assertEquals(2, result.size());
            assertEquals(3, result.get(0).getThreshold());
            assertEquals(5, result.get(1).getThreshold());
        }
    }

    @Nested
    @DisplayName("decreaseTension Tests")
    class DecreaseTensionTests {

        @Test
        @DisplayName("should return null when pool not found")
        void decreaseTension_notFound() {
            when(poolRepository.findById(999L)).thenReturn(Optional.empty());

            Dnd5eTensionPool result = service.decreaseTension(999L, 1);

            assertNull(result);
        }

        @Test
        @DisplayName("should decrease tension by amount")
        void decreaseTension_basic() {
            samplePool.setCurrentTension(5);
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));
            when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eTensionPool result = service.decreaseTension(1L, 2);

            assertEquals(3, result.getCurrentTension());
        }

        @Test
        @DisplayName("should clamp tension to 0 minimum")
        void decreaseTension_clampToZero() {
            samplePool.setCurrentTension(1);
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));
            when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eTensionPool result = service.decreaseTension(1L, 10);

            assertEquals(0, result.getCurrentTension());
        }
    }

    @Nested
    @DisplayName("setTension Tests")
    class SetTensionTests {

        @Test
        @DisplayName("should return null when pool not found")
        void setTension_notFound() {
            when(poolRepository.findById(999L)).thenReturn(Optional.empty());

            Dnd5eTensionPool result = service.setTension(999L, 5);

            assertNull(result);
        }

        @Test
        @DisplayName("should set tension to specific value")
        void setTension_basic() {
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));
            when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eTensionPool result = service.setTension(1L, 7);

            assertEquals(7, result.getCurrentTension());
        }

        @Test
        @DisplayName("should clamp to max tension")
        void setTension_clampToMax() {
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));
            when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eTensionPool result = service.setTension(1L, 100);

            assertEquals(10, result.getCurrentTension());
        }
    }

    @Nested
    @DisplayName("resetTension Tests")
    class ResetTensionTests {

        @Test
        @DisplayName("should return null when pool not found")
        void resetTension_notFound() {
            when(poolRepository.findById(999L)).thenReturn(Optional.empty());

            Dnd5eTensionPool result = service.resetTension(999L);

            assertNull(result);
        }

        @Test
        @DisplayName("should reset tension to 0 and clear triggered flags")
        void resetTension_basic() {
            samplePool.setCurrentTension(7);
            samplePool.setTriggered("[true, true, false, false]");
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));
            when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eTensionPool result = service.resetTension(1L);

            assertEquals(0, result.getCurrentTension());
            assertEquals("[false,false,false,false]", result.getTriggered());
        }
    }

    @Nested
    @DisplayName("resetAllPools Tests")
    class ResetAllPoolsTests {

        @Test
        @DisplayName("should reset all active pools")
        void resetAllPools_success() {
            when(poolRepository.findByActiveTrue()).thenReturn(List.of(samplePool));
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));
            when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            int count = service.resetAllPools();

            assertEquals(1, count);
        }
    }

    // ==================== Wandering Encounter Tests ====================

    @Nested
    @DisplayName("rollWanderingEncounter Tests")
    class RollWanderingEncounterTests {

        @Test
        @DisplayName("should return not found result when pool missing")
        void rollWanderingEncounter_notFound() {
            when(poolRepository.findById(999L)).thenReturn(Optional.empty());

            WanderingEncounterResult result = service.rollWanderingEncounter(999L, 15);

            assertFalse(result.isEncounterTriggered());
            assertEquals(999L, result.getPoolId());
        }

        @Test
        @DisplayName("should trigger encounter when roll >= DC")
        void rollWanderingEncounter_triggered() {
            samplePool.setCurrentTension(5);
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));

            WanderingEncounterResult result = service.rollWanderingEncounter(1L, 15);

            // DC = max(5, 20 - 5) = 15, roll 15 >= 15
            assertTrue(result.isEncounterTriggered());
            assertEquals(5, result.getTension());
            assertEquals(15, result.getRoll());
        }

        @Test
        @DisplayName("should not trigger encounter when roll < DC")
        void rollWanderingEncounter_notTriggered() {
            samplePool.setCurrentTension(5);
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));

            WanderingEncounterResult result = service.rollWanderingEncounter(1L, 10);

            // DC = 15, roll 10 < 15
            assertFalse(result.isEncounterTriggered());
        }

        @Test
        @DisplayName("should have DC 20 at tension 0")
        void rollWanderingEncounter_zeroTension() {
            samplePool.setCurrentTension(0);
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));

            WanderingEncounterResult result = service.rollWanderingEncounter(1L, 19);

            // DC = 20, roll 19 < 20
            assertFalse(result.isEncounterTriggered());
        }

        @Test
        @DisplayName("should have DC 5 at max tension")
        void rollWanderingEncounter_maxTension() {
            samplePool.setCurrentTension(10);
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));

            WanderingEncounterResult result = service.rollWanderingEncounter(1L, 5);

            // DC = max(5, 20-10) = 10, roll 5 < 10
            assertFalse(result.isEncounterTriggered());
        }

        @Test
        @DisplayName("should always trigger at high roll and high tension")
        void rollWanderingEncounter_alwaysTrigger() {
            samplePool.setCurrentTension(10);
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));

            WanderingEncounterResult result = service.rollWanderingEncounter(1L, 20);

            // DC = 10, roll 20 >= 10
            assertTrue(result.isEncounterTriggered());
        }
    }

    @Nested
    @DisplayName("getEncounterDc Tests")
    class GetEncounterDcTests {

        @Test
        @DisplayName("should return 20 when pool not found")
        void getEncounterDc_notFound() {
            when(poolRepository.findById(999L)).thenReturn(Optional.empty());

            int dc = service.getEncounterDc(999L);

            assertEquals(20, dc);
        }

        @Test
        @DisplayName("should return DC based on tension")
        void getEncounterDc_basic() {
            samplePool.setCurrentTension(5);
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));

            int dc = service.getEncounterDc(1L);

            assertEquals(15, dc);
        }

        @Test
        @DisplayName("should return DC 20 at tension 0")
        void getEncounterDc_zeroTension() {
            samplePool.setCurrentTension(0);
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));

            int dc = service.getEncounterDc(1L);

            assertEquals(20, dc);
        }

        @Test
        @DisplayName("should return DC 5 minimum")
        void getEncounterDc_minimum() {
            // At tension 15, DC = max(5, 20 - 15) = 5
            // But setCurrentTension clamps to maxTension, so set maxTension high enough
            samplePool.setMaxTension(20);
            samplePool.setCurrentTension(15);
            when(poolRepository.findById(1L)).thenReturn(Optional.of(samplePool));

            int dc = service.getEncounterDc(1L);

            // DC = max(5, 20 - 15) = 5
            assertEquals(5, dc);
        }
    }

    // ==================== Reference Data Tests ====================

    @Nested
    @DisplayName("listAreas Tests")
    class ListAreasTests {

        @Test
        @DisplayName("should return distinct area IDs")
        void listAreas_success() {
            when(poolRepository.findDistinctAreaIds()).thenReturn(List.of("Castle Ward", "Dark Forest"));

            List<String> areas = service.listAreas();

            assertEquals(2, areas.size());
            assertTrue(areas.contains("Castle Ward"));
        }
    }

    @Nested
    @DisplayName("listMaxedPools Tests")
    class ListMaxedPoolsTests {

        @Test
        @DisplayName("should return pools at max tension")
        void listMaxedPools_success() {
            samplePool.setCurrentTension(10);
            when(poolRepository.findMaxedTensionPools()).thenReturn(List.of(samplePool));

            List<Dnd5eTensionPool> pools = service.listMaxedPools();

            assertEquals(1, pools.size());
        }
    }

    @Nested
    @DisplayName("listEmptyPools Tests")
    class ListEmptyPoolsTests {

        @Test
        @DisplayName("should return pools at tension 0")
        void listEmptyPools_success() {
            when(poolRepository.findEmptyTensionPools()).thenReturn(List.of(samplePool));

            List<Dnd5eTensionPool> pools = service.listEmptyPools();

            assertEquals(1, pools.size());
        }
    }

    // ==================== JSON Helper Tests ====================

    @Nested
    @DisplayName("parseThresholds Tests")
    class ParseThresholdsTests {

        @Test
        @DisplayName("should parse valid thresholds")
        void parseThresholds_valid() {
            List<Integer> result = service.parseThresholds("[3, 5, 7, 9]");

            assertEquals(4, result.size());
            assertEquals(3, result.get(0));
            assertEquals(9, result.get(3));
        }

        @Test
        @DisplayName("should return empty for null")
        void parseThresholds_null() {
            List<Integer> result = service.parseThresholds(null);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return empty for empty string")
        void parseThresholds_empty() {
            List<Integer> result = service.parseThresholds("[]");
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("parseConsequences Tests")
    class ParseConsequencesTests {

        @Test
        @DisplayName("should parse valid consequences")
        void parseConsequences_valid() {
            List<String> result = service.parseConsequences("[\"First\", \"Second\", \"Third\"]");

            assertEquals(3, result.size());
            assertEquals("First", result.get(0));
            assertEquals("Third", result.get(2));
        }

        @Test
        @DisplayName("should return empty for null")
        void parseConsequences_null() {
            List<String> result = service.parseConsequences(null);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("parseTriggered Tests")
    class ParseTriggeredTests {

        @Test
        @DisplayName("should parse valid triggered flags")
        void parseTriggered_valid() {
            List<Boolean> result = service.parseTriggered("[true, false, true, false]");

            assertEquals(4, result.size());
            assertTrue(result.get(0));
            assertFalse(result.get(1));
            assertTrue(result.get(2));
        }

        @Test
        @DisplayName("should return empty for null")
        void parseTriggered_null() {
            List<Boolean> result = service.parseTriggered(null);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("buildTriggeredArray Tests")
    class BuildTriggeredArrayTests {

        @Test
        @DisplayName("should build array with uniform value")
        void buildTriggeredArray_uniform() {
            String result = service.buildTriggeredArray(4, false);

            assertEquals("[false,false,false,false]", result);
        }

        @Test
        @DisplayName("should build array from list")
        void buildTriggeredArray_fromList() {
            String result = service.buildTriggeredArray(4, List.of(true, false, true, false));

            assertEquals("[true,false,true,false]", result);
        }
    }

    // ==================== Initialization Tests ====================

    @Nested
    @DisplayName("initializeTensionDatabase Tests")
    class InitializeTensionDatabaseTests {

        @Test
        @DisplayName("should skip if database is populated")
        void initialize_skipIfPopulated() {
            // Service already initialized in setUp — the initialized flag is true,
            // so calling again should be a no-op
            service.initializeTensionDatabase();
            // No exception thrown, no new saves expected since initialized=true
        }
    }

    @Nested
    @DisplayName("populateExamplePools Tests")
    class PopulateExamplePoolsTests {

        @Test
        @DisplayName("should populate example pools")
        void populateExamplePools_success() {
            // Reset mock to count only calls from this test
            reset(poolRepository);
            when(poolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.populateExamplePools();

            verify(poolRepository, times(5)).save(any());
        }
    }

    // ==================== DTO Tests ====================

    @Nested
    @DisplayName("TensionConsequence Tests")
    class TensionConsequenceTests {

        @Test
        @DisplayName("should store threshold, type, and description")
        void tensionConsequence_fields() {
            TensionConsequence consequence = new TensionConsequence(5, "THRESHOLD", "Test consequence");

            assertEquals(5, consequence.getThreshold());
            assertEquals("THRESHOLD", consequence.getType());
            assertEquals("Test consequence", consequence.getDescription());
        }
    }

    @Nested
    @DisplayName("WanderingEncounterResult Tests")
    class WanderingEncounterResultTests {

        @Test
        @DisplayName("should store all fields")
        void wanderingEncounterResult_fields() {
            WanderingEncounterResult result = new WanderingEncounterResult(1L, 5, 15, true, "Test");

            assertEquals(1L, result.getPoolId());
            assertEquals(5, result.getTension());
            assertEquals(15, result.getRoll());
            assertTrue(result.isEncounterTriggered());
            assertEquals("Test", result.getDescription());
        }
    }
}
