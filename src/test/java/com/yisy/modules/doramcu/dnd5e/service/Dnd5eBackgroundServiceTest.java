package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eBackground;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterBackground;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eBackgroundRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterBackgroundRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Dnd5eBackgroundService Tests")
class Dnd5eBackgroundServiceTest {

    private Dnd5eBackgroundRepository backgroundRepository;
    private Dnd5eCharacterBackgroundRepository characterBackgroundRepository;
    private Dnd5eCharacterRepository characterRepository;
    private Dnd5eBackgroundService service;

    private Dnd5eCharacter thorin;
    private Dnd5eBackground acolyte;
    private Dnd5eBackground criminal;
    private Dnd5eBackground sage;

    @BeforeEach
    void setUp() {
        backgroundRepository = mock(Dnd5eBackgroundRepository.class);
        characterBackgroundRepository = mock(Dnd5eCharacterBackgroundRepository.class);
        characterRepository = mock(Dnd5eCharacterRepository.class);
        service = new Dnd5eBackgroundService(backgroundRepository, characterBackgroundRepository, characterRepository);

        thorin = new Dnd5eCharacter();
        thorin.setId(1L);
        thorin.setName("Thorin");
        thorin.setRace("Dwarf");
        thorin.setCharacterClass("Fighter");
        thorin.setLevel(1);
        thorin.setStrScore(16);
        thorin.setDexScore(12);
        thorin.setConScore(14);
        thorin.setIntScore(10);
        thorin.setWisScore(10);
        thorin.setChaScore(12);
        thorin.setProficiencyBonus(2);
        thorin.setHitPoints(12);
        thorin.setMaxHitPoints(12);

        acolyte = new Dnd5eBackground();
        acolyte.setId(1L);
        acolyte.setName("Acolyte");
        acolyte.setDescription("Devoted to worship.");
        acolyte.setCategory("Common");
        acolyte.setSkillProficiencies("Insight, Religion");
        acolyte.setToolProficiencies("Herbalism kit");
        acolyte.setFeatureSlots("Sheltered: Temple network | Missionary: Find work");
        acolyte.setFeature("Sheltered");
        acolyte.setFeatureDescription("Free shelter at temples.");
        acolyte.setEquipment("Holy symbol, Prayer book");
        acolyte.setActive(true);

        criminal = new Dnd5eBackground();
        criminal.setId(2L);
        criminal.setName("Criminal");
        criminal.setDescription("Experienced criminal.");
        criminal.setCategory("Common");
        criminal.setSkillProficiencies("Deception, Stealth");
        criminal.setToolProficiencies("Gaming set, Thief's tools");
        criminal.setFeatureSlots("Criminal Contact: Network | Crime Boss: Organization");
        criminal.setFeature("Criminal Contact");
        criminal.setFeatureDescription("Network of contacts.");
        criminal.setEquipment("Crowbar, Dark clothing");
        criminal.setActive(true);

        sage = new Dnd5eBackground();
        sage.setId(3L);
        sage.setName("Sage");
        sage.setDescription("Scholar of the natural world.");
        sage.setCategory("Common");
        sage.setSkillProficiencies("Arcana, History");
        sage.setToolProficiencies("Two languages");
        sage.setFeatureSlots("Researcher: Libraries | Savant: Knowledge");
        sage.setFeature("Researcher");
        sage.setFeatureDescription("Access to libraries.");
        sage.setEquipment("Ink, Quill, Note");
        sage.setActive(true);
    }

    @Nested
    @DisplayName("createBackground")
    class CreateBackground {

        @Test
        @DisplayName("should create a new background")
        void shouldCreateNewBackground() {
            when(backgroundRepository.findByName("Custom")).thenReturn(Optional.empty());
            Dnd5eBackground saved = new Dnd5eBackground();
            saved.setId(1L);
            saved.setName("Custom");
            saved.setCategory("Common");
            saved.setActive(true);
            when(backgroundRepository.save(any(Dnd5eBackground.class))).thenReturn(saved);

            Dnd5eBackground bg = service.createBackground("Custom", "A custom background.", "Common",
                    "Perception, Survival", "Navigator's tools",
                    "Explorer: Find shelter | Guide: Lead the way",
                    "Explorer", "You can find shelter in the wild.",
                    "Staff, Bedroll");

            assertEquals(Long.valueOf(1L), bg.getId());
            assertEquals("Custom", bg.getName());
            assertEquals("Common", bg.getCategory());
            assertTrue(bg.isActive());
            verify(backgroundRepository).save(any(Dnd5eBackground.class));
        }

        @Test
        @DisplayName("should throw if background already exists")
        void shouldThrowIfAlreadyExists() {
            when(backgroundRepository.findByName("Acolyte")).thenReturn(Optional.of(acolyte));

            assertThrows(IllegalArgumentException.class, () ->
                    service.createBackground("Acolyte", "Desc", "Common", "Skill1", null, null, "Feat", null, null));
        }

        @Test
        @DisplayName("should default category to Common")
        void shouldDefaultCategoryToCommon() {
            when(backgroundRepository.findByName("Custom")).thenReturn(Optional.empty());
            when(backgroundRepository.save(any(Dnd5eBackground.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eBackground bg = service.createBackground("Custom", "Desc", null,
                    "Skill1", null, null, "Feat", null, null);
            assertEquals("Common", bg.getCategory());
        }
    }

    @Nested
    @DisplayName("getBackground")
    class GetBackground {

        @Test
        @DisplayName("should get background by ID")
        void shouldGetById() {
            when(backgroundRepository.findById(1L)).thenReturn(Optional.of(acolyte));
            assertEquals("Acolyte", service.getBackground(1L).getName());
        }

        @Test
        @DisplayName("should throw if not found")
        void shouldThrowIfNotFound() {
            when(backgroundRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> service.getBackground(99L));
        }
    }

    @Nested
    @DisplayName("getBackgroundByName")
    class GetBackgroundByName {

        @Test
        @DisplayName("should get background by name")
        void shouldGetByName() {
            when(backgroundRepository.findByName("Acolyte")).thenReturn(Optional.of(acolyte));
            assertEquals("Acolyte", service.getBackgroundByName("Acolyte").getName());
        }

        @Test
        @DisplayName("should throw if not found")
        void shouldThrowIfNotFound() {
            when(backgroundRepository.findByName("Nonexistent")).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> service.getBackgroundByName("Nonexistent"));
        }
    }

    @Nested
    @DisplayName("listBackgrounds")
    class ListBackgrounds {

        @Test
        @DisplayName("should list all active backgrounds")
        void shouldListAllActive() {
            when(backgroundRepository.findByActiveTrue()).thenReturn(List.of(acolyte, criminal, sage));
            assertEquals(3, service.listBackgrounds().size());
        }

        @Test
        @DisplayName("should return empty list if none")
        void shouldReturnEmpty() {
            when(backgroundRepository.findByActiveTrue()).thenReturn(List.of());
            assertTrue(service.listBackgrounds().isEmpty());
        }
    }

    @Nested
    @DisplayName("listAllBackgrounds")
    class ListAllBackgrounds {

        @Test
        @DisplayName("should list all backgrounds including inactive")
        void shouldListAll() {
            when(backgroundRepository.findAll()).thenReturn(List.of(acolyte, criminal));
            assertEquals(2, service.listAllBackgrounds().size());
        }
    }

    @Nested
    @DisplayName("listBackgroundsByCategory")
    class ListByCategory {

        @Test
        @DisplayName("should filter by category")
        void shouldFilterByCategory() {
            when(backgroundRepository.findByCategoryAndActiveTrue("Common")).thenReturn(List.of(acolyte, sage));
            List<Dnd5eBackground> result = service.listBackgroundsByCategory("Common");
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(b -> b.getCategory().equals("Common")));
        }
    }

    @Nested
    @DisplayName("listBackgroundsBySkill")
    class ListBySkill {

        @Test
        @DisplayName("should find backgrounds by skill proficiency")
        void shouldFindBySkill() {
            when(backgroundRepository.findBySkillProficiency("Insight")).thenReturn(List.of(acolyte));
            assertEquals(1, service.listBackgroundsBySkill("Insight").size());
        }
    }

    @Nested
    @DisplayName("searchBackgrounds")
    class SearchBackgrounds {

        @Test
        @DisplayName("should search by keyword")
        void shouldSearchByKeyword() {
            when(backgroundRepository.searchByKeyword("worship")).thenReturn(List.of(acolyte));
            assertEquals(1, service.searchBackgrounds("worship").size());
        }

        @Test
        @DisplayName("should return empty if no match")
        void shouldReturnEmptyIfNoMatch() {
            when(backgroundRepository.searchByKeyword("nonexistent")).thenReturn(List.of());
            assertTrue(service.searchBackgrounds("nonexistent").isEmpty());
        }
    }

    @Nested
    @DisplayName("listCategories")
    class ListCategories {

        @Test
        @DisplayName("should return distinct categories")
        void shouldReturnDistinctCategories() {
            acolyte.setCategory("Common");
            criminal.setCategory("Common");
            sage.setCategory("Rare");
            when(backgroundRepository.findByActiveTrue()).thenReturn(List.of(acolyte, criminal, sage));
            List<String> categories = service.listCategories();
            assertEquals(2, categories.size());
            assertTrue(categories.contains("Common"));
            assertTrue(categories.contains("Rare"));
        }
    }

    @Nested
    @DisplayName("deleteBackground")
    class DeleteBackground {

        @Test
        @DisplayName("should delete a background")
        void shouldDelete() {
            when(backgroundRepository.findByName("Acolyte")).thenReturn(Optional.of(acolyte));
            service.deleteBackground("Acolyte");
            verify(backgroundRepository).delete(acolyte);
        }
    }

    @Nested
    @DisplayName("toggleActive")
    class ToggleActive {

        @Test
        @DisplayName("should toggle active state")
        void shouldToggleActive() {
            when(backgroundRepository.findById(1L)).thenReturn(Optional.of(acolyte));
            when(backgroundRepository.save(any(Dnd5eBackground.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eBackground bg = service.toggleActive(1L, false);
            assertFalse(bg.isActive());
        }
    }

    @Nested
    @DisplayName("assignBackgroundToCharacter")
    class AssignBackgroundToCharacter {

        @Test
        @DisplayName("should assign background to character")
        void shouldAssign() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(thorin));
            when(backgroundRepository.findByName("Acolyte")).thenReturn(Optional.of(acolyte));
            when(characterBackgroundRepository.existsByCharacterIdAndBackgroundNameAndActiveTrue(1L, "Acolyte")).thenReturn(false);
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(characterBackgroundRepository.save(any(Dnd5eCharacterBackground.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eCharacterBackground cb = service.assignBackgroundToCharacter(1L, "Acolyte", "Sheltered");

            assertEquals(1L, cb.getCharacterId());
            assertEquals("Acolyte", cb.getBackgroundName());
            assertEquals("Sheltered", cb.getFeatureSelections());
            assertTrue(cb.isActive());
            assertEquals("Acolyte", thorin.getBackground());
        }

        @Test
        @DisplayName("should throw if character not found")
        void shouldThrowIfCharacterNotFound() {
            when(characterRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () ->
                    service.assignBackgroundToCharacter(99L, "Acolyte", null));
        }

        @Test
        @DisplayName("should throw if background not found")
        void shouldThrowIfBackgroundNotFound() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(thorin));
            when(backgroundRepository.findByName("Nonexistent")).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () ->
                    service.assignBackgroundToCharacter(1L, "Nonexistent", null));
        }

        @Test
        @DisplayName("should throw if character already has background")
        void shouldThrowIfAlreadyHas() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(thorin));
            when(backgroundRepository.findByName("Acolyte")).thenReturn(Optional.of(acolyte));
            when(characterBackgroundRepository.existsByCharacterIdAndBackgroundNameAndActiveTrue(1L, "Acolyte")).thenReturn(true);
            assertThrows(IllegalArgumentException.class, () ->
                    service.assignBackgroundToCharacter(1L, "Acolyte", null));
        }

        @Test
        @DisplayName("should assign without feature selections")
        void shouldAssignWithoutFeatures() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(thorin));
            when(backgroundRepository.findByName("Criminal")).thenReturn(Optional.of(criminal));
            when(characterBackgroundRepository.existsByCharacterIdAndBackgroundNameAndActiveTrue(1L, "Criminal")).thenReturn(false);
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(characterBackgroundRepository.save(any(Dnd5eCharacterBackground.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eCharacterBackground cb = service.assignBackgroundToCharacter(1L, "Criminal", null);
            assertNull(cb.getFeatureSelections());
        }
    }

    @Nested
    @DisplayName("removeBackgroundFromCharacter")
    class RemoveBackgroundFromCharacter {

        @Test
        @DisplayName("should remove background from character")
        void shouldRemove() {
            thorin.setBackground("Acolyte");
            Dnd5eCharacterBackground cb = new Dnd5eCharacterBackground();
            cb.setId(1L);
            cb.setCharacterId(1L);
            cb.setBackgroundName("Acolyte");
            cb.setActive(true);

            when(characterRepository.findById(1L)).thenReturn(Optional.of(thorin));
            when(characterBackgroundRepository.findByCharacterIdAndBackgroundNameAndActiveTrue(1L, "Acolyte")).thenReturn(Optional.of(cb));
            when(characterBackgroundRepository.save(any(Dnd5eCharacterBackground.class))).thenAnswer(inv -> inv.getArgument(0));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            service.removeBackgroundFromCharacter(1L, "Acolyte");

            assertFalse(cb.isActive());
            verify(characterRepository).save(thorin);
        }

        @Test
        @DisplayName("should throw if character not found")
        void shouldThrowIfCharacterNotFound() {
            when(characterRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () ->
                    service.removeBackgroundFromCharacter(99L, "Acolyte"));
        }

        @Test
        @DisplayName("should throw if character does not have background")
        void shouldThrowIfNotHas() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(thorin));
            when(characterBackgroundRepository.findByCharacterIdAndBackgroundNameAndActiveTrue(1L, "Acolyte")).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () ->
                    service.removeBackgroundFromCharacter(1L, "Acolyte"));
        }
    }

    @Nested
    @DisplayName("updateFeatureSelections")
    class UpdateFeatureSelections {

        @Test
        @DisplayName("should update feature selections")
        void shouldUpdate() {
            Dnd5eCharacterBackground cb = new Dnd5eCharacterBackground();
            cb.setId(1L);
            cb.setCharacterId(1L);
            cb.setBackgroundName("Acolyte");
            cb.setFeatureSelections("Sheltered");

            when(characterBackgroundRepository.findByCharacterIdAndBackgroundNameAndActiveTrue(1L, "Acolyte")).thenReturn(Optional.of(cb));
            when(characterBackgroundRepository.save(any(Dnd5eCharacterBackground.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eCharacterBackground updated = service.updateFeatureSelections(1L, "Acolyte", "Missionary");
            assertEquals("Missionary", updated.getFeatureSelections());
        }

        @Test
        @DisplayName("should throw if character does not have background")
        void shouldThrowIfNotHas() {
            when(characterBackgroundRepository.findByCharacterIdAndBackgroundNameAndActiveTrue(1L, "Acolyte")).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () ->
                    service.updateFeatureSelections(1L, "Acolyte", "Missionary"));
        }
    }

    @Nested
    @DisplayName("listCharacterBackgrounds")
    class ListCharacterBackgrounds {

        @Test
        @DisplayName("should list all backgrounds for a character")
        void shouldList() {
            Dnd5eCharacterBackground cb = new Dnd5eCharacterBackground();
            cb.setCharacterId(1L);
            cb.setBackgroundName("Acolyte");
            when(characterBackgroundRepository.findByCharacterId(1L)).thenReturn(List.of(cb));

            List<Dnd5eCharacterBackground> result = service.listCharacterBackgrounds(1L);
            assertEquals(1, result.size());
            assertEquals("Acolyte", result.get(0).getBackgroundName());
        }

        @Test
        @DisplayName("should return empty if none")
        void shouldReturnEmpty() {
            when(characterBackgroundRepository.findByCharacterId(1L)).thenReturn(List.of());
            assertTrue(service.listCharacterBackgrounds(1L).isEmpty());
        }
    }

    @Nested
    @DisplayName("getActiveCharacterBackground")
    class GetActiveCharacterBackground {

        @Test
        @DisplayName("should return active background")
        void shouldReturnActive() {
            Dnd5eCharacterBackground cb = new Dnd5eCharacterBackground();
            cb.setCharacterId(1L);
            cb.setBackgroundName("Acolyte");
            cb.setActive(true);
            when(characterBackgroundRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(List.of(cb));

            Dnd5eCharacterBackground result = service.getActiveCharacterBackground(1L);
            assertNotNull(result);
            assertEquals("Acolyte", result.getBackgroundName());
        }

        @Test
        @DisplayName("should return null if no active background")
        void shouldReturnNull() {
            when(characterBackgroundRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(List.of());
            assertNull(service.getActiveCharacterBackground(1L));
        }
    }

    @Nested
    @DisplayName("getSkillProficiencies")
    class GetSkillProficiencies {

        @Test
        @DisplayName("should parse skill proficiencies")
        void shouldParseSkills() {
            when(backgroundRepository.findByName("Acolyte")).thenReturn(Optional.of(acolyte));
            List<String> skills = service.getSkillProficiencies("Acolyte");
            assertEquals(2, skills.size());
            assertTrue(skills.contains("Insight"));
            assertTrue(skills.contains("Religion"));
        }

        @Test
        @DisplayName("should return empty if no skills")
        void shouldReturnEmpty() {
            Dnd5eBackground empty = new Dnd5eBackground();
            empty.setSkillProficiencies("");
            when(backgroundRepository.findByName("Empty")).thenReturn(Optional.of(empty));
            assertTrue(service.getSkillProficiencies("Empty").isEmpty());
        }

        @Test
        @DisplayName("should return empty if null skills")
        void shouldReturnEmptyForNull() {
            Dnd5eBackground empty = new Dnd5eBackground();
            empty.setSkillProficiencies(null);
            when(backgroundRepository.findByName("Null")).thenReturn(Optional.of(empty));
            assertTrue(service.getSkillProficiencies("Null").isEmpty());
        }
    }

    @Nested
    @DisplayName("getToolProficiencies")
    class GetToolProficiencies {

        @Test
        @DisplayName("should parse tool proficiencies")
        void shouldParseTools() {
            when(backgroundRepository.findByName("Criminal")).thenReturn(Optional.of(criminal));
            List<String> tools = service.getToolProficiencies("Criminal");
            assertEquals(2, tools.size());
            assertTrue(tools.contains("Gaming set"));
            assertTrue(tools.contains("Thief's tools"));
        }

        @Test
        @DisplayName("should return empty if no tools")
        void shouldReturnEmpty() {
            Dnd5eBackground empty = new Dnd5eBackground();
            empty.setToolProficiencies(null);
            when(backgroundRepository.findByName("Empty")).thenReturn(Optional.of(empty));
            assertTrue(service.getToolProficiencies("Empty").isEmpty());
        }
    }

    @Nested
    @DisplayName("getFeatureSlotOptions")
    class GetFeatureSlotOptions {

        @Test
        @DisplayName("should parse feature slot options")
        void shouldParseOptions() {
            when(backgroundRepository.findByName("Acolyte")).thenReturn(Optional.of(acolyte));
            List<String> options = service.getFeatureSlotOptions("Acolyte");
            assertEquals(2, options.size());
            assertTrue(options.contains("Temple network"));
            assertTrue(options.contains("Find work"));
        }

        @Test
        @DisplayName("should return empty if no feature slots")
        void shouldReturnEmpty() {
            Dnd5eBackground empty = new Dnd5eBackground();
            empty.setFeatureSlots(null);
            when(backgroundRepository.findByName("Empty")).thenReturn(Optional.of(empty));
            assertTrue(service.getFeatureSlotOptions("Empty").isEmpty());
        }
    }

    @Nested
    @DisplayName("updateBackground")
    class UpdateBackground {

        @Test
        @DisplayName("should update background fields")
        void shouldUpdate() {
            when(backgroundRepository.findById(1L)).thenReturn(Optional.of(acolyte));
            when(backgroundRepository.save(any(Dnd5eBackground.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eBackground updated = service.updateBackground(1L, "New Name", null, null, null, null, null, null, null, null);
            assertEquals("New Name", updated.getName());
        }

        @Test
        @DisplayName("should throw if not found")
        void shouldThrowIfNotFound() {
            when(backgroundRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> service.updateBackground(99L, null, null, null, null, null, null, null, null, null));
        }
    }

    @Nested
    @DisplayName("initializeBackgroundDatabase")
    class InitializeDatabase {

        @Test
        @DisplayName("should skip if already initialized")
        void shouldSkipIfAlreadyInitialized() {
            // First call sets initialized=true internally
            service.initializeBackgroundDatabase();
            // Second call should be a no-op (already initialized)
            service.initializeBackgroundDatabase();
        }

        @Test
        @DisplayName("should skip if enough backgrounds exist")
        void shouldSkipIfEnoughExist() {
            when(backgroundRepository.count()).thenReturn(20L);
            service.initializeBackgroundDatabase();
            // Should not call populate
        }

        @Test
        @DisplayName("should populate if missing")
        void shouldPopulateIfMissing() {
            when(backgroundRepository.count()).thenReturn(0L);
            when(backgroundRepository.existsByName(anyString())).thenReturn(false);
            when(backgroundRepository.save(any(Dnd5eBackground.class))).thenAnswer(inv -> inv.getArgument(0));
            service.initializeBackgroundDatabase();
            // Should not throw
        }
    }
}
