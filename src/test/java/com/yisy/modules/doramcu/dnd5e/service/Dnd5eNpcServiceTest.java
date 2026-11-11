package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eNpc;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eNpcRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Dnd5eNpcServiceTest {

    @Mock
    private Dnd5eNpcRepository npcRepository;

    @InjectMocks
    private Dnd5eNpcService npcService;

    private Dnd5eNpc createTestNpc() {
        Dnd5eNpc npc = new Dnd5eNpc();
        npc.setId(1L);
        npc.setName("Test NPC");
        npc.setNpcType("Humanoid");
        npc.setRace("Human");
        npc.setNpcClass("Fighter");
        npc.setLevel(5);
        npc.setChallengeRating(3);
        npc.setDescription("A test NPC for testing.");
        npc.setMotive("To be tested.");
        npc.setHitPoints(50);
        npc.setArmorClass(15);
        npc.setGoldPieces(100);
        npc.setDispositionScore(0);
        npc.setActive(true);
        return npc;
    }

    // ==================== createNpc ====================

    @Nested
    @DisplayName("createNpc Tests")
    class CreateNpcTests {

        @Test
        @DisplayName("should create an NPC")
        void shouldCreateANpc() {
            Dnd5eNpc npc = createTestNpc();
            when(npcRepository.save(any())).thenReturn(npc);

            Dnd5eNpc result = npcService.createNpc(npc);

            assertEquals("Test NPC", result.getName());
            verify(npcRepository).save(npc);
        }

        @Test
        @DisplayName("should create a villain NPC")
        void shouldCreateAVillainNpc() {
            Dnd5eNpc npc = createTestNpc();
            npc.setVillain(true);
            npc.setDispositionScore(-10);
            when(npcRepository.save(any())).thenReturn(npc);

            Dnd5eNpc result = npcService.createNpc(npc);

            assertTrue(result.isVillain());
            assertEquals("Hostile", result.getDisposition());
        }

        @Test
        @DisplayName("should create an ally NPC")
        void shouldCreateAnAllyNpc() {
            Dnd5eNpc npc = createTestNpc();
            npc.setAlly(true);
            npc.setDispositionScore(8);
            when(npcRepository.save(any())).thenReturn(npc);

            Dnd5eNpc result = npcService.createNpc(npc);

            assertTrue(result.isAlly());
            assertEquals("Helpful", result.getDisposition());
        }
    }

    // ==================== getNpc ====================

    @Nested
    @DisplayName("getNpc Tests")
    class GetNpcTests {

        @Test
        @DisplayName("should return NPC when found")
        void shouldReturnNpcWhenFound() {
            Dnd5eNpc npc = createTestNpc();
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));

            Optional<Dnd5eNpc> result = npcService.getNpc(1L);

            assertTrue(result.isPresent());
            assertEquals("Test NPC", result.get().getName());
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            when(npcRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Dnd5eNpc> result = npcService.getNpc(999L);

            assertFalse(result.isPresent());
        }
    }

    // ==================== listNpcs ====================

    @Nested
    @DisplayName("listNpcs Tests")
    class ListNpcsTests {

        @Test
        @DisplayName("should list all active NPCs")
        void shouldListAllActiveNpcs() {
            List<Dnd5eNpc> npcs = Arrays.asList(createTestNpc());
            when(npcRepository.findByActiveTrue()).thenReturn(npcs);

            List<Dnd5eNpc> result = npcService.listNpcs();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("should return empty list when no NPCs")
        void shouldReturnEmptyListWhenNoNpcs() {
            when(npcRepository.findByActiveTrue()).thenReturn(Collections.emptyList());

            List<Dnd5eNpc> result = npcService.listNpcs();

            assertTrue(result.isEmpty());
        }
    }

    // ==================== listNpcsByType ====================

    @Nested
    @DisplayName("listNpcsByType Tests")
    class ListNpcsByTypeTests {

        @Test
        @DisplayName("should list NPCs by type")
        void shouldListNpcsByType() {
            List<Dnd5eNpc> npcs = Arrays.asList(createTestNpc());
            when(npcRepository.findByNpcTypeAndActiveTrue("Humanoid")).thenReturn(npcs);

            List<Dnd5eNpc> result = npcService.listNpcsByType("Humanoid");

            assertEquals(1, result.size());
        }
    }

    // ==================== listNpcsByDisposition ====================

    @Nested
    @DisplayName("listNpcsByDisposition Tests")
    class ListNpcsByDispositionTests {

        @Test
        @DisplayName("should list NPCs by disposition")
        void shouldListNpcsByDisposition() {
            List<Dnd5eNpc> npcs = Arrays.asList(createTestNpc());
            when(npcRepository.findByDispositionAndActiveTrue("Indifferent")).thenReturn(npcs);

            List<Dnd5eNpc> result = npcService.listNpcsByDisposition("Indifferent");

            assertEquals(1, result.size());
        }
    }

    // ==================== listNpcsByClass ====================

    @Nested
    @DisplayName("listNpcsByClass Tests")
    class ListNpcsByClassTests {

        @Test
        @DisplayName("should list NPCs by class")
        void shouldListNpcsByClass() {
            List<Dnd5eNpc> npcs = Arrays.asList(createTestNpc());
            when(npcRepository.findByClass("Fighter")).thenReturn(npcs);

            List<Dnd5eNpc> result = npcService.listNpcsByClass("Fighter");

            assertEquals(1, result.size());
        }
    }

    // ==================== listNpcsByRace ====================

    @Nested
    @DisplayName("listNpcsByRace Tests")
    class ListNpcsByRaceTests {

        @Test
        @DisplayName("should list NPCs by race")
        void shouldListNpcsByRace() {
            List<Dnd5eNpc> npcs = Arrays.asList(createTestNpc());
            when(npcRepository.findByRace("Human")).thenReturn(npcs);

            List<Dnd5eNpc> result = npcService.listNpcsByRace("Human");

            assertEquals(1, result.size());
        }
    }

    // ==================== listNpcsByCrRange ====================

    @Nested
    @DisplayName("listNpcsByCrRange Tests")
    class ListNpcsByCrRangeTests {

        @Test
        @DisplayName("should list NPCs by CR range")
        void shouldListNpcsByCrRange() {
            List<Dnd5eNpc> npcs = Arrays.asList(createTestNpc());
            when(npcRepository.findByCrRange(1, 5)).thenReturn(npcs);

            List<Dnd5eNpc> result = npcService.listNpcsByCrRange(1, 5);

            assertEquals(1, result.size());
        }
    }

    // ==================== listVillains ====================

    @Nested
    @DisplayName("listVillains Tests")
    class ListVillainsTests {

        @Test
        @DisplayName("should list villain NPCs")
        void shouldListVillainNpcs() {
            Dnd5eNpc villain = createTestNpc();
            villain.setVillain(true);
            when(npcRepository.findByIsVillainTrueAndActiveTrue()).thenReturn(Arrays.asList(villain));

            List<Dnd5eNpc> result = npcService.listVillains();

            assertEquals(1, result.size());
            assertTrue(result.get(0).isVillain());
        }
    }

    // ==================== listAllies ====================

    @Nested
    @DisplayName("listAllies Tests")
    class ListAlliesTests {

        @Test
        @DisplayName("should list ally NPCs")
        void shouldListAllyNpcs() {
            Dnd5eNpc ally = createTestNpc();
            ally.setAlly(true);
            when(npcRepository.findByIsAllyTrueAndActiveTrue()).thenReturn(Arrays.asList(ally));

            List<Dnd5eNpc> result = npcService.listAllies();

            assertEquals(1, result.size());
            assertTrue(result.get(0).isAlly());
        }
    }

    // ==================== searchNpcs ====================

    @Nested
    @DisplayName("searchNpcs Tests")
    class SearchNpcsTests {

        @Test
        @DisplayName("should search NPCs by keyword")
        void shouldSearchNpcsByKeyword() {
            List<Dnd5eNpc> npcs = Arrays.asList(createTestNpc());
            when(npcRepository.searchByKeyword("Test")).thenReturn(npcs);

            List<Dnd5eNpc> result = npcService.searchNpcs("Test");

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("should return empty when no match")
        void shouldReturnEmptyWhenNoMatch() {
            when(npcRepository.searchByKeyword("Nonexistent")).thenReturn(Collections.emptyList());

            List<Dnd5eNpc> result = npcService.searchNpcs("Nonexistent");

            assertTrue(result.isEmpty());
        }
    }

    // ==================== deleteNpc ====================

    @Nested
    @DisplayName("deleteNpc Tests")
    class DeleteNpcTests {

        @Test
        @DisplayName("should deactivate NPC")
        void shouldDeactivateNpc() {
            Dnd5eNpc npc = createTestNpc();
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));
            when(npcRepository.save(any())).thenReturn(npc);

            npcService.deleteNpc(1L);

            assertFalse(npc.isActive());
            verify(npcRepository).save(npc);
        }

        @Test
        @DisplayName("should do nothing when NPC not found")
        void shouldDoNothingWhenNpcNotFound() {
            when(npcRepository.findById(999L)).thenReturn(Optional.empty());

            npcService.deleteNpc(999L);

            // findById called for 999L (not found), no save for this ID
            verify(npcRepository).findById(999L);
        }
    }

    // ==================== changeDisposition ====================

    @Nested
    @DisplayName("changeDisposition Tests")
    class ChangeDispositionTests {

        @Test
        @DisplayName("should increase disposition score")
        void shouldIncreaseDispositionScore() {
            Dnd5eNpc npc = createTestNpc();
            npc.setDispositionScore(0);
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));
            when(npcRepository.save(any())).thenReturn(npc);

            Dnd5eNpc result = npcService.changeDisposition(1L, 3);

            assertNotNull(result);
        }

        @Test
        @DisplayName("should decrease disposition score")
        void shouldDecreaseDispositionScore() {
            Dnd5eNpc npc = createTestNpc();
            npc.setDispositionScore(0);
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));
            when(npcRepository.save(any())).thenReturn(npc);

            Dnd5eNpc result = npcService.changeDisposition(1L, -3);

            assertNotNull(result);
        }

        @Test
        @DisplayName("should clamp disposition to max 10")
        void shouldClampDispositionToMax10() {
            Dnd5eNpc npc = createTestNpc();
            npc.setDispositionScore(8);
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));
            when(npcRepository.save(any())).thenReturn(npc);

            npcService.changeDisposition(1L, 5);

            assertEquals(10, npc.getDispositionScore());
            assertEquals("Helpful", npc.getDisposition());
        }

        @Test
        @DisplayName("should clamp disposition to min -10")
        void shouldClampDispositionToMin10() {
            Dnd5eNpc npc = createTestNpc();
            npc.setDispositionScore(-8);
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));
            when(npcRepository.save(any())).thenReturn(npc);

            npcService.changeDisposition(1L, -5);

            assertEquals(-10, npc.getDispositionScore());
            assertEquals("Hostile", npc.getDisposition());
        }

        @Test
        @DisplayName("should return null when NPC not found")
        void shouldReturnNullWhenNpcNotFound() {
            when(npcRepository.findById(999L)).thenReturn(Optional.empty());

            Dnd5eNpc result = npcService.changeDisposition(999L, 1);

            assertNull(result);
        }

        @Test
        @DisplayName("should update disposition string from Indifferent to Friendly")
        void shouldUpdateDispositionStringToFriendly() {
            Dnd5eNpc npc = createTestNpc();
            npc.setDispositionScore(0);
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));
            when(npcRepository.save(any())).thenReturn(npc);

            npcService.changeDisposition(1L, 3);

            assertEquals("Friendly", npc.getDisposition());
        }

        @Test
        @DisplayName("should update disposition string from Indifferent to Unfriendly")
        void shouldUpdateDispositionStringToUnfriendly() {
            Dnd5eNpc npc = createTestNpc();
            npc.setDispositionScore(0);
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));
            when(npcRepository.save(any())).thenReturn(npc);

            npcService.changeDisposition(1L, -3);

            assertEquals("Unfriendly", npc.getDisposition());
        }
    }

    // ==================== setDispositionScore ====================

    @Nested
    @DisplayName("setDispositionScore Tests")
    class SetDispositionScoreTests {

        @Test
        @DisplayName("should set disposition score")
        void shouldSetDispositionScore() {
            Dnd5eNpc npc = createTestNpc();
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));
            when(npcRepository.save(any())).thenReturn(npc);

            Dnd5eNpc result = npcService.setDispositionScore(1L, 5);

            assertNotNull(result);
            assertEquals("Friendly", npc.getDisposition());
        }

        @Test
        @DisplayName("should return null when NPC not found")
        void shouldReturnNullWhenNpcNotFound() {
            when(npcRepository.findById(999L)).thenReturn(Optional.empty());

            Dnd5eNpc result = npcService.setDispositionScore(999L, 5);

            assertNull(result);
        }
    }

    // ==================== getNpcsByDispositionScoreAtLeast ====================

    @Nested
    @DisplayName("getNpcsByDispositionScoreAtLeast Tests")
    class GetNpcsByDispositionScoreAtLeastTests {

        @Test
        @DisplayName("should return NPCs with score at least min")
        void shouldReturnNpcsWithScoreAtLeastMin() {
            List<Dnd5eNpc> npcs = Arrays.asList(createTestNpc());
            when(npcRepository.findByDispositionScoreAtLeast(5)).thenReturn(npcs);

            List<Dnd5eNpc> result = npcService.getNpcsByDispositionScoreAtLeast(5);

            assertEquals(1, result.size());
        }
    }

    // ==================== getNpcsByDispositionScoreAtMost ====================

    @Nested
    @DisplayName("getNpcsByDispositionScoreAtMost Tests")
    class GetNpcsByDispositionScoreAtMostTests {

        @Test
        @DisplayName("should return NPCs with score at most max")
        void shouldReturnNpcsWithScoreAtMostMax() {
            List<Dnd5eNpc> npcs = Arrays.asList(createTestNpc());
            when(npcRepository.findByDispositionScoreAtMost(-5)).thenReturn(npcs);

            List<Dnd5eNpc> result = npcService.getNpcsByDispositionScoreAtMost(-5);

            assertEquals(1, result.size());
        }
    }

    // ==================== addAlly ====================

    @Nested
    @DisplayName("addAlly Tests")
    class AddAllyTests {

        @Test
        @DisplayName("should add ally to NPC")
        void shouldAddAllyToNpc() {
            Dnd5eNpc npc = createTestNpc();
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));
            when(npcRepository.save(any())).thenReturn(npc);
            when(npcRepository.findByNameContainingIgnoreCase("AllyName")).thenReturn(Collections.emptyList());

            Dnd5eNpc result = npcService.addAlly(1L, "AllyName");

            assertNotNull(result);
            assertEquals("AllyName", npc.getAllies());
        }

        @Test
        @DisplayName("should append ally to existing allies")
        void shouldAppendAllyToExistingAllies() {
            Dnd5eNpc npc = createTestNpc();
            npc.setAllies("ExistingAlly");
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));
            when(npcRepository.save(any())).thenReturn(npc);
            when(npcRepository.findByNameContainingIgnoreCase("NewAlly")).thenReturn(Collections.emptyList());

            npcService.addAlly(1L, "NewAlly");

            assertEquals("ExistingAlly,NewAlly", npc.getAllies());
        }

        @Test
        @DisplayName("should not duplicate ally")
        void shouldNotDuplicateAlly() {
            Dnd5eNpc npc = createTestNpc();
            npc.setAllies("ExistingAlly");
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));
            when(npcRepository.save(any())).thenReturn(npc);

            npcService.addAlly(1L, "ExistingAlly");

            assertEquals("ExistingAlly", npc.getAllies());
        }

        @Test
        @DisplayName("should return null when NPC not found")
        void shouldReturnNullWhenNpcNotFound() {
            when(npcRepository.findById(999L)).thenReturn(Optional.empty());

            Dnd5eNpc result = npcService.addAlly(999L, "AllyName");

            assertNull(result);
        }

        @Test
        @DisplayName("should make relationship bidirectional")
        void shouldMakeRelationshipBidirectional() {
            Dnd5eNpc npc = createTestNpc();
            Dnd5eNpc ally = new Dnd5eNpc();
            ally.setId(2L);
            ally.setName("AllyName");
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));
            when(npcRepository.findByNameContainingIgnoreCase("AllyName")).thenReturn(Arrays.asList(ally));
            when(npcRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            npcService.addAlly(1L, "AllyName");

            assertEquals("AllyName", npc.getAllies());
            assertEquals("Test NPC", ally.getAllies());
        }
    }

    // ==================== addEnemy ====================

    @Nested
    @DisplayName("addEnemy Tests")
    class AddEnemyTests {

        @Test
        @DisplayName("should add enemy to NPC")
        void shouldAddEnemyToNpc() {
            Dnd5eNpc npc = createTestNpc();
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));
            when(npcRepository.save(any())).thenReturn(npc);
            when(npcRepository.findByNameContainingIgnoreCase("EnemyName")).thenReturn(Collections.emptyList());

            Dnd5eNpc result = npcService.addEnemy(1L, "EnemyName");

            assertNotNull(result);
            assertEquals("EnemyName", npc.getEnemies());
        }

        @Test
        @DisplayName("should return null when NPC not found")
        void shouldReturnNullWhenNpcNotFound() {
            when(npcRepository.findById(999L)).thenReturn(Optional.empty());

            Dnd5eNpc result = npcService.addEnemy(999L, "EnemyName");

            assertNull(result);
        }
    }

    // ==================== addFamily ====================

    @Nested
    @DisplayName("addFamily Tests")
    class AddFamilyTests {

        @Test
        @DisplayName("should add family to NPC")
        void shouldAddFamilyToNpc() {
            Dnd5eNpc npc = createTestNpc();
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));
            when(npcRepository.save(any())).thenReturn(npc);

            Dnd5eNpc result = npcService.addFamily(1L, "FamilyName");

            assertNotNull(result);
            assertEquals("FamilyName", npc.getFamily());
        }

        @Test
        @DisplayName("should append family to existing family")
        void shouldAppendFamilyToExistingFamily() {
            Dnd5eNpc npc = createTestNpc();
            npc.setFamily("ExistingFamily");
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));
            when(npcRepository.save(any())).thenReturn(npc);

            npcService.addFamily(1L, "NewFamily");

            assertEquals("ExistingFamily,NewFamily", npc.getFamily());
        }

        @Test
        @DisplayName("should return null when NPC not found")
        void shouldReturnNullWhenNpcNotFound() {
            when(npcRepository.findById(999L)).thenReturn(Optional.empty());

            Dnd5eNpc result = npcService.addFamily(999L, "FamilyName");

            assertNull(result);
        }
    }

    // ==================== getNpcsWithAllies ====================

    @Nested
    @DisplayName("getNpcsWithAllies Tests")
    class GetNpcsWithAlliesTests {

        @Test
        @DisplayName("should return NPCs with allies")
        void shouldReturnNpcsWithAllies() {
            List<Dnd5eNpc> npcs = Arrays.asList(createTestNpc());
            when(npcRepository.findAllies()).thenReturn(npcs);

            List<Dnd5eNpc> result = npcService.getNpcsWithAllies();

            assertEquals(1, result.size());
        }
    }

    // ==================== getNpcsWithEnemies ====================

    @Nested
    @DisplayName("getNpcsWithEnemies Tests")
    class GetNpcsWithEnemiesTests {

        @Test
        @DisplayName("should return NPCs with enemies")
        void shouldReturnNpcsWithEnemies() {
            List<Dnd5eNpc> npcs = Arrays.asList(createTestNpc());
            when(npcRepository.FindEnemies()).thenReturn(npcs);

            List<Dnd5eNpc> result = npcService.getNpcsWithEnemies();

            assertEquals(1, result.size());
        }
    }

    // ==================== addGold ====================

    @Nested
    @DisplayName("addGold Tests")
    class AddGoldTests {

        @Test
        @DisplayName("should add gold to NPC")
        void shouldAddGoldToNpc() {
            Dnd5eNpc npc = createTestNpc();
            npc.setGoldPieces(100);
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));
            when(npcRepository.save(any())).thenReturn(npc);

            Dnd5eNpc result = npcService.addGold(1L, 50);

            assertNotNull(result);
            assertEquals(150, npc.getGoldPieces());
        }

        @Test
        @DisplayName("should return null when NPC not found")
        void shouldReturnNullWhenNpcNotFound() {
            when(npcRepository.findById(999L)).thenReturn(Optional.empty());

            Dnd5eNpc result = npcService.addGold(999L, 50);

            assertNull(result);
        }
    }

    // ==================== removeGold ====================

    @Nested
    @DisplayName("removeGold Tests")
    class RemoveGoldTests {

        @Test
        @DisplayName("should remove gold from NPC")
        void shouldRemoveGoldFromNpc() {
            Dnd5eNpc npc = createTestNpc();
            npc.setGoldPieces(100);
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));
            when(npcRepository.save(any())).thenReturn(npc);

            Dnd5eNpc result = npcService.removeGold(1L, 30);

            assertNotNull(result);
            assertEquals(70, npc.getGoldPieces());
        }

        @Test
        @DisplayName("should not go below 0 gold")
        void shouldNotGoBelow0Gold() {
            Dnd5eNpc npc = createTestNpc();
            npc.setGoldPieces(100);
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));
            when(npcRepository.save(any())).thenReturn(npc);

            npcService.removeGold(1L, 200);

            assertEquals(0, npc.getGoldPieces());
        }

        @Test
        @DisplayName("should return null when NPC not found")
        void shouldReturnNullWhenNpcNotFound() {
            when(npcRepository.findById(999L)).thenReturn(Optional.empty());

            Dnd5eNpc result = npcService.removeGold(999L, 50);

            assertNull(result);
        }
    }

    // ==================== setHitPoints ====================

    @Nested
    @DisplayName("setHitPoints Tests")
    class SetHitPointsTests {

        @Test
        @DisplayName("should set NPC hit points")
        void shouldSetNpcHitPoints() {
            Dnd5eNpc npc = createTestNpc();
            when(npcRepository.findById(1L)).thenReturn(Optional.of(npc));
            when(npcRepository.save(any())).thenReturn(npc);

            Dnd5eNpc result = npcService.setHitPoints(1L, 30);

            assertNotNull(result);
            assertEquals(30, npc.getHitPoints());
        }

        @Test
        @DisplayName("should return null when NPC not found")
        void shouldReturnNullWhenNpcNotFound() {
            when(npcRepository.findById(999L)).thenReturn(Optional.empty());

            Dnd5eNpc result = npcService.setHitPoints(999L, 30);

            assertNull(result);
        }
    }

    // ==================== getDispositionValues ====================

    @Nested
    @DisplayName("getDispositionValues Tests")
    class GetDispositionValuesTests {

        @Test
        @DisplayName("should return all disposition values")
        void shouldReturnAllDispositionValues() {
            List<String> result = npcService.getDispositionValues();

            assertEquals(5, result.size());
            assertTrue(result.contains("Hostile"));
            assertTrue(result.contains("Unfriendly"));
            assertTrue(result.contains("Indifferent"));
            assertTrue(result.contains("Friendly"));
            assertTrue(result.contains("Helpful"));
        }
    }

    // ==================== getDispositionForScore ====================

    @Nested
    @DisplayName("getDispositionForScore Tests")
    class GetDispositionForScoreTests {

        @Test
        @DisplayName("should return Hostile for -10")
        void shouldReturnHostileForMinus10() {
            assertEquals("Hostile", npcService.getDispositionForScore(-10));
        }

        @Test
        @DisplayName("should return Hostile for -6")
        void shouldReturnHostileForMinus6() {
            assertEquals("Hostile", npcService.getDispositionForScore(-6));
        }

        @Test
        @DisplayName("should return Unfriendly for -5")
        void shouldReturnUnfriendlyForMinus5() {
            assertEquals("Unfriendly", npcService.getDispositionForScore(-5));
        }

        @Test
        @DisplayName("should return Unfriendly for -2")
        void shouldReturnUnfriendlyForMinus2() {
            assertEquals("Unfriendly", npcService.getDispositionForScore(-2));
        }

        @Test
        @DisplayName("should return Indifferent for -1")
        void shouldReturnIndifferentForMinus1() {
            assertEquals("Indifferent", npcService.getDispositionForScore(-1));
        }

        @Test
        @DisplayName("should return Indifferent for 0")
        void shouldReturnIndifferentFor0() {
            assertEquals("Indifferent", npcService.getDispositionForScore(0));
        }

        @Test
        @DisplayName("should return Indifferent for 1")
        void shouldReturnIndifferentFor1() {
            assertEquals("Indifferent", npcService.getDispositionForScore(1));
        }

        @Test
        @DisplayName("should return Friendly for 2")
        void shouldReturnFriendlyFor2() {
            assertEquals("Friendly", npcService.getDispositionForScore(2));
        }

        @Test
        @DisplayName("should return Friendly for 5")
        void shouldReturnFriendlyFor5() {
            assertEquals("Friendly", npcService.getDispositionForScore(5));
        }

        @Test
        @DisplayName("should return Helpful for 6")
        void shouldReturnHelpfulFor6() {
            assertEquals("Helpful", npcService.getDispositionForScore(6));
        }

        @Test
        @DisplayName("should return Helpful for 10")
        void shouldReturnHelpfulFor10() {
            assertEquals("Helpful", npcService.getDispositionForScore(10));
        }

        @Test
        @DisplayName("should clamp score below -10")
        void shouldClampScoreBelowMinus10() {
            assertEquals("Hostile", npcService.getDispositionForScore(-20));
        }

        @Test
        @DisplayName("should clamp score above 10")
        void shouldClampScoreAbove10() {
            assertEquals("Helpful", npcService.getDispositionForScore(20));
        }
    }

    // ==================== Model setters ====================

    @Nested
    @DisplayName("Model setter Tests")
    class ModelSetterTests {

        @Test
        @DisplayName("should clamp level to minimum 1")
        void shouldClampLevelToMinimum1() {
            Dnd5eNpc npc = createTestNpc();
            npc.setLevel(0);
            assertEquals(1, npc.getLevel());
        }

        @Test
        @DisplayName("should clamp CR to 0-30")
        void shouldClampCrTo0To30() {
            Dnd5eNpc npc = createTestNpc();
            npc.setChallengeRating(-1);
            assertEquals(0, npc.getChallengeRating());
            npc.setChallengeRating(50);
            assertEquals(30, npc.getChallengeRating());
        }

        @Test
        @DisplayName("should clamp hit points to minimum 0")
        void shouldClampHitPointsToMinimum0() {
            Dnd5eNpc npc = createTestNpc();
            npc.setHitPoints(-10);
            assertEquals(0, npc.getHitPoints());
        }

        @Test
        @DisplayName("should clamp gold to minimum 0")
        void shouldClampGoldToMinimum0() {
            Dnd5eNpc npc = createTestNpc();
            npc.setGoldPieces(-50);
            assertEquals(0, npc.getGoldPieces());
        }

        @Test
        @DisplayName("should auto-update disposition on score set")
        void shouldAutoUpdateDispositionOnScoreSet() {
            Dnd5eNpc npc = createTestNpc();
            npc.setDispositionScore(-7);
            assertEquals("Hostile", npc.getDisposition());
            npc.setDispositionScore(3);
            assertEquals("Friendly", npc.getDisposition());
            npc.setDispositionScore(7);
            assertEquals("Helpful", npc.getDisposition());
        }
    }

    // ==================== initializeNpcDatabase ====================

    @Nested
    @DisplayName("initializeNpcDatabase Tests")
    class InitializeNpcDatabaseTests {

        @Test
        @DisplayName("should skip if already populated")
        void shouldSkipIfAlreadyPopulated() {
            Dnd5eNpcRepository freshRepo = mock(Dnd5eNpcRepository.class);
            when(freshRepo.count()).thenReturn(10L);

            new Dnd5eNpcService(freshRepo);

            verify(freshRepo, never()).save(any());
        }

        @Test
        @DisplayName("should populate if missing")
        void shouldPopulateIfMissing() {
            Dnd5eNpcRepository freshRepo = mock(Dnd5eNpcRepository.class);
            when(freshRepo.count()).thenReturn(0L);
            when(freshRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            new Dnd5eNpcService(freshRepo);

            verify(freshRepo, atLeast(12)).save(any());
        }
    }
}
