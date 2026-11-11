package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eAdventureHook;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eModule;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eModuleRepository;
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
class Dnd5eModuleServiceTest {

    @Mock
    private Dnd5eModuleRepository moduleRepository;

    @Mock
    private Dnd5eAdventureHookService adventureHookService;

    @Mock
    private Dnd5eDungeonRoomService dungeonRoomService;

    @Mock
    private Dnd5eNpcService npcService;

    @Mock
    private Dnd5eEncounterService encounterService;

    @Mock
    private Dnd5eVillainService villainService;

    @Mock
    private Dnd5eBastionService bastionService;

    @Mock
    private Dnd5eTensionPoolService tensionPoolService;

    @Mock
    private Dnd5eGrowingMagicItemService growingItemService;

    @Mock
    private Dnd5eHorrorService horrorService;

    @Mock
    private Dnd5eFeatService featService;

    @Mock
    private Dnd5eCraftingService craftingService;

    @Mock
    private Dnd5ePsionService psionService;

    @InjectMocks
    private Dnd5eModuleService moduleService;

    private Dnd5eModule createTestModule() {
        Dnd5eModule module = new Dnd5eModule();
        module.setId(1L);
        module.setName("Test Module");
        module.setTagline("A test module");
        module.setDescription("A test module for testing.");
        module.setTheme("Horror");
        module.setMinLevel(1);
        module.setMaxLevel(5);
        module.setEstimatedSessions(4);
        module.setStatus("Draft");
        module.setActive(true);
        return module;
    }

    // ==================== createModule ====================

    @Nested
    @DisplayName("createModule Tests")
    class CreateModuleTests {

        @Test
        @DisplayName("should create a module with all fields")
        void shouldCreateModuleWithAllFields() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.createModule("Test Module", "A test module",
                    "A test module for testing.", "Horror", 1, 5, 4);

            assertEquals("Test Module", result.getName());
            assertEquals("Horror", result.getTheme());
            assertEquals(1, result.getMinLevel());
            assertEquals(5, result.getMaxLevel());
            assertEquals(4, result.getEstimatedSessions());
            assertEquals("Draft", result.getStatus());
            verify(moduleRepository).save(any());
        }

        @Test
        @DisplayName("should create a module with default status Draft")
        void shouldCreateModuleWithDefaultStatus() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.createModule("Test Module", null,
                    "Description", "Sandbox", 3, 7, 6);

            assertEquals("Draft", result.getStatus());
        }

        @Test
        @DisplayName("should clamp min level to 1-20")
        void shouldClampMinLevel() {
            when(moduleRepository.save(any())).thenAnswer(invocation -> {
                Dnd5eModule m = invocation.getArgument(0);
                return m;
            });

            Dnd5eModule result = moduleService.createModule("Test Module", null,
                    "Description", "Horror", 0, 5, 4);

            assertEquals(1, result.getMinLevel());
        }

        @Test
        @DisplayName("should clamp max level to 1-20")
        void shouldClampMaxLevel() {
            when(moduleRepository.save(any())).thenAnswer(invocation -> {
                Dnd5eModule m = invocation.getArgument(0);
                return m;
            });

            Dnd5eModule result = moduleService.createModule("Test Module", null,
                    "Description", "Horror", 1, 25, 4);

            assertEquals(20, result.getMaxLevel());
        }
    }

    // ==================== getModule ====================

    @Nested
    @DisplayName("getModule Tests")
    class GetModuleTests {

        @Test
        @DisplayName("should return module when found")
        void shouldReturnModuleWhenFound() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));

            Dnd5eModule result = moduleService.getModule(1L);

            assertEquals("Test Module", result.getName());
        }

        @Test
        @DisplayName("should throw when not found")
        void shouldThrowWhenNotFound() {
            when(moduleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(NoSuchElementException.class, () -> moduleService.getModule(99L));
        }
    }

    // ==================== listModules ====================

    @Nested
    @DisplayName("listModules Tests")
    class ListModulesTests {

        @Test
        @DisplayName("should list all active modules")
        void shouldListAllActiveModules() {
            List<Dnd5eModule> modules = Arrays.asList(createTestModule());
            when(moduleRepository.findByActiveTrue()).thenReturn(modules);

            List<Dnd5eModule> result = moduleService.listModules();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("should return empty list when no modules")
        void shouldReturnEmptyListWhenNoModules() {
            when(moduleRepository.findByActiveTrue()).thenReturn(Collections.emptyList());

            List<Dnd5eModule> result = moduleService.listModules();

            assertTrue(result.isEmpty());
        }
    }

    // ==================== listModulesByTheme ====================

    @Nested
    @DisplayName("listModulesByTheme Tests")
    class ListModulesByThemeTests {

        @Test
        @DisplayName("should list modules by theme")
        void shouldListModulesByTheme() {
            List<Dnd5eModule> modules = Arrays.asList(createTestModule());
            when(moduleRepository.findByThemeAndActiveTrue("Horror")).thenReturn(modules);

            List<Dnd5eModule> result = moduleService.listModulesByTheme("Horror");

            assertEquals(1, result.size());
        }
    }

    // ==================== listModulesByStatus ====================

    @Nested
    @DisplayName("listModulesByStatus Tests")
    class ListModulesByStatusTests {

        @Test
        @DisplayName("should list modules by status")
        void shouldListModulesByStatus() {
            List<Dnd5eModule> modules = Arrays.asList(createTestModule());
            when(moduleRepository.findByStatusAndActiveTrue("Draft")).thenReturn(modules);

            List<Dnd5eModule> result = moduleService.listModulesByStatus("Draft");

            assertEquals(1, result.size());
        }
    }

    // ==================== listModulesByPartyLevel ====================

    @Nested
    @DisplayName("listModulesByPartyLevel Tests")
    class ListModulesByPartyLevelTests {

        @Test
        @DisplayName("should list modules by party level")
        void shouldListModulesByPartyLevel() {
            List<Dnd5eModule> modules = Arrays.asList(createTestModule());
            when(moduleRepository.findByPartyLevel(3)).thenReturn(modules);

            List<Dnd5eModule> result = moduleService.listModulesByPartyLevel(3);

            assertEquals(1, result.size());
        }
    }

    // ==================== searchModules ====================

    @Nested
    @DisplayName("searchModules Tests")
    class SearchModulesTests {

        @Test
        @DisplayName("should search modules by keyword")
        void shouldSearchModulesByKeyword() {
            List<Dnd5eModule> modules = Arrays.asList(createTestModule());
            when(moduleRepository.searchByKeyword("test")).thenReturn(modules);

            List<Dnd5eModule> result = moduleService.searchModules("test");

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("should return empty list when no match")
        void shouldReturnEmptyListWhenNoMatch() {
            when(moduleRepository.searchByKeyword("nonexistent")).thenReturn(Collections.emptyList());

            List<Dnd5eModule> result = moduleService.searchModules("nonexistent");

            assertTrue(result.isEmpty());
        }
    }

    // ==================== updateModule ====================

    @Nested
    @DisplayName("updateModule Tests")
    class UpdateModuleTests {

        @Test
        @DisplayName("should update module fields")
        void shouldUpdateModuleFields() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.updateModule(1L, "Updated Name", null, null,
                    "Sandbox", 3, 7, 6, "In Progress", null, null);

            assertEquals("Updated Name", result.getName());
            assertEquals("Sandbox", result.getTheme());
            assertEquals("In Progress", result.getStatus());
        }

        @Test
        @DisplayName("should throw when module not found")
        void shouldThrowWhenModuleNotFound() {
            when(moduleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(NoSuchElementException.class, () -> moduleService.updateModule(99L, null, null, null,
                    null, 1, 5, 4, null, null, null));
        }
    }

    // ==================== deleteModule ====================

    @Nested
    @DisplayName("deleteModule Tests")
    class DeleteModuleTests {

        @Test
        @DisplayName("should soft delete module")
        void shouldSoftDeleteModule() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            moduleService.deleteModule(1L);

            assertFalse(module.isActive());
            verify(moduleRepository).save(module);
        }
    }

    // ==================== Component Linking ====================

    @Nested
    @DisplayName("addHook Tests")
    class AddHookTests {

        @Test
        @DisplayName("should add hook to module")
        void shouldAddHookToModule() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.addHook(1L, "hook1");

            assertTrue(result.getHookIds().contains("hook1"));
        }

        @Test
        @DisplayName("should not add duplicate hook")
        void shouldNotAddDuplicateHook() {
            Dnd5eModule module = createTestModule();
            module.setHookIds("hook1");
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.addHook(1L, "hook1");

            assertEquals("hook1", result.getHookIds());
        }
    }

    @Nested
    @DisplayName("removeHook Tests")
    class RemoveHookTests {

        @Test
        @DisplayName("should remove hook from module")
        void shouldRemoveHookFromModule() {
            Dnd5eModule module = createTestModule();
            module.setHookIds("hook1,hook2");
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.removeHook(1L, "hook1");

            assertEquals("hook2", result.getHookIds());
        }
    }

    @Nested
    @DisplayName("addRoom Tests")
    class AddRoomTests {

        @Test
        @DisplayName("should add room to module")
        void shouldAddRoomToModule() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.addRoom(1L, "room1");

            assertTrue(result.getRoomIds().contains("room1"));
        }
    }

    @Nested
    @DisplayName("addNpc Tests")
    class AddNpcTests {

        @Test
        @DisplayName("should add NPC to module")
        void shouldAddNpcToModule() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.addNpc(1L, "npc1");

            assertTrue(result.getNpcIds().contains("npc1"));
        }
    }

    @Nested
    @DisplayName("addEncounter Tests")
    class AddEncounterTests {

        @Test
        @DisplayName("should add encounter to module")
        void shouldAddEncounterToModule() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.addEncounter(1L, "enc1");

            assertTrue(result.getEncounterIds().contains("enc1"));
        }
    }

    @Nested
    @DisplayName("addVillain Tests")
    class AddVillainTests {

        @Test
        @DisplayName("should add villain to module")
        void shouldAddVillainToModule() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.addVillain(1L, "villain1");

            assertTrue(result.getVillainIds().contains("villain1"));
        }
    }

    @Nested
    @DisplayName("addBastion Tests")
    class AddBastionTests {

        @Test
        @DisplayName("should add bastion to module")
        void shouldAddBastionToModule() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.addBastion(1L, "bastion1");

            assertTrue(result.getBastionIds().contains("bastion1"));
        }
    }

    @Nested
    @DisplayName("addTensionPool Tests")
    class AddTensionPoolTests {

        @Test
        @DisplayName("should add tension pool to module")
        void shouldAddTensionPoolToModule() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.addTensionPool(1L, "tension1");

            assertTrue(result.getTensionPoolIds().contains("tension1"));
        }
    }

    @Nested
    @DisplayName("addGrowingItem Tests")
    class AddGrowingItemTests {

        @Test
        @DisplayName("should add growing item to module")
        void shouldAddGrowingItemToModule() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.addGrowingItem(1L, "item1");

            assertTrue(result.getGrowingItemIds().contains("item1"));
        }
    }

    @Nested
    @DisplayName("addDarkGift Tests")
    class AddDarkGiftTests {

        @Test
        @DisplayName("should add dark gift to module")
        void shouldAddDarkGiftToModule() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.addDarkGift(1L, "gift1");

            assertTrue(result.getDarkGiftIds().contains("gift1"));
        }
    }

    @Nested
    @DisplayName("addFeat Tests")
    class AddFeatTests {

        @Test
        @DisplayName("should add feat to module")
        void shouldAddFeatToModule() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.addFeat(1L, "feat1");

            assertTrue(result.getFeatIds().contains("feat1"));
        }
    }

    @Nested
    @DisplayName("addCraftingRecipe Tests")
    class AddCraftingRecipeTests {

        @Test
        @DisplayName("should add crafting recipe to module")
        void shouldAddCraftingRecipeToModule() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.addCraftingRecipe(1L, "recipe1");

            assertTrue(result.getCraftingRecipeIds().contains("recipe1"));
        }
    }

    @Nested
    @DisplayName("addPsionicPower Tests")
    class AddPsionicPowerTests {

        @Test
        @DisplayName("should add psionic power to module")
        void shouldAddPsionicPowerToModule() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.addPsionicPower(1L, "power1");

            assertTrue(result.getPsionicPowerIds().contains("power1"));
        }
    }

    // ==================== getHookIds ====================

    @Nested
    @DisplayName("getHookIds Tests")
    class GetHookIdsTests {

        @Test
        @DisplayName("should return list of hook IDs")
        void shouldReturnListOfHookIds() {
            Dnd5eModule module = createTestModule();
            module.setHookIds("hook1,hook2,hook3");
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));

            List<String> result = moduleService.getHookIds(1L);

            assertEquals(3, result.size());
            assertTrue(result.contains("hook1"));
        }

        @Test
        @DisplayName("should return empty list when no hooks")
        void shouldReturnEmptyListWhenNoHooks() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));

            List<String> result = moduleService.getHookIds(1L);

            assertTrue(result.isEmpty());
        }
    }

    // ==================== generateSummary ====================

    @Nested
    @DisplayName("generateSummary Tests")
    class GenerateSummaryTests {

        @Test
        @DisplayName("should generate summary with component counts")
        void shouldGenerateSummaryWithComponentCounts() {
            Dnd5eModule module = createTestModule();
            module.setHookIds("hook1,hook2");
            module.setNpcIds("npc1");
            module.setEncounterIds("enc1,enc2,enc3");
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            String summary = moduleService.generateSummary(1L);

            assertTrue(summary.contains("Test Module"));
            assertTrue(summary.contains("Adventure Hooks: 2"));
            assertTrue(summary.contains("NPCs: 1"));
            assertTrue(summary.contains("Encounters: 3"));
            assertTrue(summary.contains("Total Components:"));
            assertTrue(summary.contains("Completion:"));
        }

        @Test
        @DisplayName("should generate summary with 0% completion for empty module")
        void shouldGenerateSummaryWithZeroCompletion() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            String summary = moduleService.generateSummary(1L);

            assertTrue(summary.contains("Completion: 0%"));
        }

        @Test
        @DisplayName("should set summary on module")
        void shouldSetSummaryOnModule() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            moduleService.generateSummary(1L);

            verify(moduleRepository).save(module);
        }
    }

    // ==================== getModuleStatistics ====================

    @Nested
    @DisplayName("getModuleStatistics Tests")
    class GetModuleStatisticsTests {

        @Test
        @DisplayName("should return statistics map")
        void shouldReturnStatisticsMap() {
            Dnd5eModule module = createTestModule();
            module.setHookIds("hook1");
            module.setPlayCount(5);
            module.setRating(4.5);
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));

            Map<String, Object> stats = moduleService.getModuleStatistics(1L);

            assertEquals("Test Module", stats.get("name"));
            assertEquals("Horror", stats.get("theme"));
            assertEquals(5, stats.get("playCount"));
            assertEquals(4.5, stats.get("rating"));
            assertNotNull(stats.get("components"));
        }

        @Test
        @DisplayName("should include total components count")
        void shouldIncludeTotalComponentsCount() {
            Dnd5eModule module = createTestModule();
            module.setHookIds("hook1,hook2");
            module.setNpcIds("npc1");
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));

            Map<String, Object> stats = moduleService.getModuleStatistics(1L);

            assertEquals(3, stats.get("totalComponents"));
        }
    }

    // ==================== incrementPlayCount ====================

    @Nested
    @DisplayName("incrementPlayCount Tests")
    class IncrementPlayCountTests {

        @Test
        @DisplayName("should increment play count")
        void shouldIncrementPlayCount() {
            Dnd5eModule module = createTestModule();
            module.setPlayCount(3);
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.incrementPlayCount(1L);

            assertEquals(4, result.getPlayCount());
        }
    }

    // ==================== setRating ====================

    @Nested
    @DisplayName("setRating Tests")
    class SetRatingTests {

        @Test
        @DisplayName("should set rating")
        void shouldSetRating() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.setRating(1L, 4.5);

            assertEquals(4.5, result.getRating());
        }

        @Test
        @DisplayName("should clamp rating to 0-5")
        void shouldClampRating() {
            Dnd5eModule module = createTestModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(moduleRepository.save(any())).thenReturn(module);

            Dnd5eModule result = moduleService.setRating(1L, 6.0);

            assertEquals(5.0, result.getRating());
        }
    }

    // ==================== validateModule ====================

    @Nested
    @DisplayName("validateModule Tests")
    class ValidateModuleTests {

        @Test
        @DisplayName("should return empty issues for valid module")
        void shouldReturnEmptyIssuesForValidModule() {
            Dnd5eModule module = createTestModule();
            module.setHookIds("hook1");
            module.setNpcIds("npc1");
            module.setEncounterIds("enc1");
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));

            List<String> issues = moduleService.validateModule(1L);

            assertTrue(issues.isEmpty());
        }

        @Test
        @DisplayName("should report missing hooks")
        void shouldReportMissingHooks() {
            Dnd5eModule module = createTestModule();
            module.setNpcIds("npc1");
            module.setEncounterIds("enc1");
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));

            List<String> issues = moduleService.validateModule(1L);

            assertTrue(issues.stream().anyMatch(i -> i.contains("hooks")));
        }

        @Test
        @DisplayName("should report missing NPCs")
        void shouldReportMissingNpcs() {
            Dnd5eModule module = createTestModule();
            module.setHookIds("hook1");
            module.setEncounterIds("enc1");
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));

            List<String> issues = moduleService.validateModule(1L);

            assertTrue(issues.stream().anyMatch(i -> i.contains("NPCs")));
        }

        @Test
        @DisplayName("should report missing encounters")
        void shouldReportMissingEncounters() {
            Dnd5eModule module = createTestModule();
            module.setHookIds("hook1");
            module.setNpcIds("npc1");
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));

            List<String> issues = moduleService.validateModule(1L);

            assertTrue(issues.stream().anyMatch(i -> i.contains("encounters")));
        }
    }

    // ==================== listThemes ====================

    @Nested
    @DisplayName("listThemes Tests")
    class ListThemesTests {

        @Test
        @DisplayName("should return all themes")
        void shouldReturnAllThemes() {
            List<String> themes = moduleService.listThemes();

            assertEquals(6, themes.size());
            assertTrue(themes.contains("Horror"));
            assertTrue(themes.contains("High Magic"));
            assertTrue(themes.contains("Sandbox"));
            assertTrue(themes.contains("Dungeon Crawl"));
            assertTrue(themes.contains("Feywild"));
            assertTrue(themes.contains("Steampunk"));
        }
    }

    // ==================== listStatuses ====================

    @Nested
    @DisplayName("listStatuses Tests")
    class ListStatusesTests {

        @Test
        @DisplayName("should return all statuses")
        void shouldReturnAllStatuses() {
            List<String> statuses = moduleService.listStatuses();

            assertEquals(4, statuses.size());
            assertTrue(statuses.contains("Draft"));
            assertTrue(statuses.contains("In Progress"));
            assertTrue(statuses.contains("Complete"));
            assertTrue(statuses.contains("Archived"));
        }
    }

    // ==================== getThemeSuggestions ====================

    @Nested
    @DisplayName("getThemeSuggestions Tests")
    class GetThemeSuggestionsTests {

        @Test
        @DisplayName("should return suggestions for Horror theme")
        void shouldReturnSuggestionsForHorrorTheme() {
            when(adventureHookService.listHooksByTheme("Horror")).thenReturn(Collections.emptyList());

            Map<String, Object> suggestions = moduleService.getThemeSuggestions("Horror");

            assertEquals("Horror", suggestions.get("theme"));
            assertNotNull(suggestions.get("recommendedComponents"));
        }

        @Test
        @DisplayName("should return suggestions for High Magic theme")
        void shouldReturnSuggestionsForHighMagicTheme() {
            when(adventureHookService.listHooksByTheme("High Magic")).thenReturn(Collections.emptyList());

            Map<String, Object> suggestions = moduleService.getThemeSuggestions("High Magic");

            assertEquals("High Magic", suggestions.get("theme"));
        }

        @Test
        @DisplayName("should return suggestions for Sandbox theme")
        void shouldReturnSuggestionsForSandboxTheme() {
            when(adventureHookService.listHooksByTheme("Sandbox")).thenReturn(Collections.emptyList());

            Map<String, Object> suggestions = moduleService.getThemeSuggestions("Sandbox");

            assertEquals("Sandbox", suggestions.get("theme"));
        }

        @Test
        @DisplayName("should return suggestions for Dungeon Crawl theme")
        void shouldReturnSuggestionsForDungeonCrawlTheme() {
            when(adventureHookService.listHooksByTheme("Dungeon Crawl")).thenReturn(Collections.emptyList());

            Map<String, Object> suggestions = moduleService.getThemeSuggestions("Dungeon Crawl");

            assertEquals("Dungeon Crawl", suggestions.get("theme"));
        }

        @Test
        @DisplayName("should return suggestions for Feywild theme")
        void shouldReturnSuggestionsForFeywildTheme() {
            when(adventureHookService.listHooksByTheme("Feywild")).thenReturn(Collections.emptyList());

            Map<String, Object> suggestions = moduleService.getThemeSuggestions("Feywild");

            assertEquals("Feywild", suggestions.get("theme"));
        }

        @Test
        @DisplayName("should return suggestions for Steampunk theme")
        void shouldReturnSuggestionsForSteampunkTheme() {
            when(adventureHookService.listHooksByTheme("Steampunk")).thenReturn(Collections.emptyList());

            Map<String, Object> suggestions = moduleService.getThemeSuggestions("Steampunk");

            assertEquals("Steampunk", suggestions.get("theme"));
        }

        @Test
        @DisplayName("should return general suggestions for unknown theme")
        void shouldReturnGeneralSuggestionsForUnknownTheme() {
            when(adventureHookService.listHooksByTheme("Unknown")).thenReturn(Collections.emptyList());

            Map<String, Object> suggestions = moduleService.getThemeSuggestions("Unknown");

            assertEquals("Unknown", suggestions.get("theme"));
        }
    }

    // ==================== findMostPlayed ====================

    @Nested
    @DisplayName("findMostPlayed Tests")
    class FindMostPlayedTests {

        @Test
        @DisplayName("should return most played modules")
        void shouldReturnMostPlayedModules() {
            List<Dnd5eModule> modules = Arrays.asList(createTestModule());
            when(moduleRepository.findMostPlayed()).thenReturn(modules);

            List<Dnd5eModule> result = moduleService.findMostPlayed();

            assertEquals(1, result.size());
        }
    }

    // ==================== findTopRated ====================

    @Nested
    @DisplayName("findTopRated Tests")
    class FindTopRatedTests {

        @Test
        @DisplayName("should return top rated modules")
        void shouldReturnTopRatedModules() {
            List<Dnd5eModule> modules = Arrays.asList(createTestModule());
            when(moduleRepository.findByRating(4.0)).thenReturn(modules);

            List<Dnd5eModule> result = moduleService.findTopRated(4.0);

            assertEquals(1, result.size());
        }
    }

    // ==================== populateExampleModules ====================

    @Nested
    @DisplayName("populateExampleModules Tests")
    class PopulateExampleModulesTests {

        @Test
        @DisplayName("should skip if already populated")
        void shouldSkipIfAlreadyPopulated() {
            when(moduleRepository.count()).thenReturn(5L);

            moduleService.populateExampleModules();

            verify(moduleRepository, never()).save(any());
        }

        @Test
        @DisplayName("should populate if empty")
        void shouldPopulateIfEmpty() {
            when(moduleRepository.count()).thenReturn(0L);
            when(moduleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            moduleService.populateExampleModules();

            verify(moduleRepository, times(6)).save(any());
        }

        @Test
        @DisplayName("should create modules for all 6 themes")
        void shouldCreateModulesForAllThemes() {
            when(moduleRepository.count()).thenReturn(0L);
            when(moduleRepository.save(any())).thenAnswer(invocation -> {
                Dnd5eModule m = invocation.getArgument(0);
                m.setId(1L);
                return m;
            });

            moduleService.populateExampleModules();

            // Verify 6 modules were created
            verify(moduleRepository, times(6)).save(any());
        }
    }
}
