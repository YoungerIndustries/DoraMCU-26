package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterToolProficiency;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCraftingTool;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterToolProficiencyRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCraftingToolRepository;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eToolProficiencyService.ToolCheckResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Dnd5eToolProficiencyService Tests")
class Dnd5eToolProficiencyServiceTest {

    private Dnd5eCharacterToolProficiencyRepository proficiencyRepository;
    private Dnd5eCharacterRepository characterRepository;
    private Dnd5eCraftingToolRepository toolRepository;
    private Dnd5eToolProficiencyService service;

    private Dnd5eCharacter rogue;
    private Dnd5eCharacter fighter;
    private Dnd5eCraftingTool herbalismKit;
    private Dnd5eCraftingTool smithsHammer;

    @BeforeEach
    void setUp() {
        proficiencyRepository = mock(Dnd5eCharacterToolProficiencyRepository.class);
        characterRepository = mock(Dnd5eCharacterRepository.class);
        toolRepository = mock(Dnd5eCraftingToolRepository.class);
        service = new Dnd5eToolProficiencyService(proficiencyRepository, characterRepository, toolRepository);

        // Rogue with high DEX, WIS
        rogue = new Dnd5eCharacter();
        rogue.setId(1L);
        rogue.setName("Shadow");
        rogue.setRace("Halfling");
        rogue.setCharacterClass("Rogue");
        rogue.setLevel(4);
        rogue.setStrScore(10);
        rogue.setDexScore(16);
        rogue.setConScore(12);
        rogue.setIntScore(12);
        rogue.setWisScore(14);
        rogue.setChaScore(10);
        rogue.setProficiencyBonus(2);
        rogue.setHitPoints(20);
        rogue.setMaxHitPoints(20);

        // Fighter with high STR
        fighter = new Dnd5eCharacter();
        fighter.setId(2L);
        fighter.setName("Thorin");
        fighter.setRace("Dwarf");
        fighter.setCharacterClass("Fighter");
        fighter.setLevel(6);
        fighter.setStrScore(16);
        fighter.setDexScore(12);
        fighter.setConScore(14);
        fighter.setIntScore(10);
        fighter.setWisScore(10);
        fighter.setChaScore(8);
        fighter.setProficiencyBonus(3);
        fighter.setHitPoints(30);
        fighter.setMaxHitPoints(30);

        // Herbalism Kit (WIS)
        herbalismKit = new Dnd5eCraftingTool();
        herbalismKit.setId(1L);
        herbalismKit.setName("Herbalism Kit");
        herbalismKit.setDescription("Plant identification and preparation");
        herbalismKit.setAssociatedAbility("WIS");
        herbalismKit.setToolType("Artisan");
        herbalismKit.setCraftableItems("Potions of healing, antitoxins, poisons");
        herbalismKit.setCombatUse("Identify and use medicinal plants");
        herbalismKit.setExplorationUse("Forage for food and medicine");
        herbalismKit.setUseAction("Prepare healing salves");

        // Smith's Hammer (STR)
        smithsHammer = new Dnd5eCraftingTool();
        smithsHammer.setId(2L);
        smithsHammer.setName("Smith's Hammer");
        smithsHammer.setDescription("Metal forging and shaping");
        smithsHammer.setAssociatedAbility("STR");
        smithsHammer.setToolType("Artisan");
        smithsHammer.setCraftableItems("Weapons, armor, tools, metal fittings");
        smithsHammer.setCombatUse("Repair weapons or armor in the field");
        smithsHammer.setExplorationUse("Forge simple tools or weapons");
        smithsHammer.setUseAction("Shape metal for repairs");
    }

    // ---- listAllTools ----

    @Nested
    @DisplayName("listAllTools Tests")
    class ListAllToolsTests {

        @Test
        @DisplayName("should return all tools")
        void listAllTools_shouldReturnAll() {
            when(toolRepository.findAll()).thenReturn(List.of(herbalismKit, smithsHammer));

            List<Dnd5eCraftingTool> result = service.listAllTools();

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("should return empty list when no tools")
        void listAllTools_shouldReturnEmpty() {
            when(toolRepository.findAll()).thenReturn(List.of());

            List<Dnd5eCraftingTool> result = service.listAllTools();

            assertTrue(result.isEmpty());
        }
    }

    // ---- getToolByName ----

    @Nested
    @DisplayName("getToolByName Tests")
    class GetToolByNameTests {

        @Test
        @DisplayName("should return tool by name")
        void getToolByName_shouldReturnTool() {
            when(toolRepository.findByName("Herbalism Kit")).thenReturn(Optional.of(herbalismKit));

            Dnd5eCraftingTool result = service.getToolByName("Herbalism Kit");

            assertNotNull(result);
            assertEquals("Herbalism Kit", result.getName());
            assertEquals("WIS", result.getAssociatedAbility());
        }

        @Test
        @DisplayName("should throw when tool not found")
        void getToolByName_shouldThrowWhenNotFound() {
            when(toolRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> service.getToolByName("Nonexistent"));
        }
    }

    // ---- listToolsByType ----

    @Nested
    @DisplayName("listToolsByType Tests")
    class ListToolsByTypeTests {

        @Test
        @DisplayName("should return tools by type")
        void listToolsByType_shouldReturnTools() {
            when(toolRepository.findByToolType("Artisan")).thenReturn(List.of(herbalismKit, smithsHammer));

            List<Dnd5eCraftingTool> result = service.listToolsByType("Artisan");

            assertEquals(2, result.size());
        }
    }

    // ---- listToolsByAbility ----

    @Nested
    @DisplayName("listToolsByAbility Tests")
    class ListToolsByAbilityTests {

        @Test
        @DisplayName("should return tools by ability")
        void listToolsByAbility_shouldReturnTools() {
            when(toolRepository.findByAssociatedAbility("WIS")).thenReturn(List.of(herbalismKit));

            List<Dnd5eCraftingTool> result = service.listToolsByAbility("WIS");

            assertEquals(1, result.size());
            assertEquals("Herbalism Kit", result.get(0).getName());
        }
    }

    // ---- addProficiency ----

    @Nested
    @DisplayName("addProficiency Tests")
    class AddProficiencyTests {

        @Test
        @DisplayName("should add a tool proficiency")
        void addProficiency_shouldAdd() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(rogue));
            when(toolRepository.findByName("Herbalism Kit")).thenReturn(Optional.of(herbalismKit));
            when(proficiencyRepository.existsByCharacterIdAndToolName(1L, "Herbalism Kit")).thenReturn(false);
            when(proficiencyRepository.save(any(Dnd5eCharacterToolProficiency.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Dnd5eCharacterToolProficiency result = service.addProficiency(1L, "Herbalism Kit", 4);

            assertNotNull(result);
            assertEquals(1L, result.getCharacterId());
            assertEquals("Shadow", result.getCharacterName());
            assertEquals("Herbalism Kit", result.getToolName());
            assertEquals(4, result.getLevelAcquired());
            assertTrue(result.isActive());
            assertFalse(result.isExpertise());
        }

        @Test
        @DisplayName("should throw when character not found")
        void addProficiency_shouldThrowWhenCharacterNotFound() {
            when(characterRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> service.addProficiency(99L, "Herbalism Kit", 1));
        }

        @Test
        @DisplayName("should throw when tool not found")
        void addProficiency_shouldThrowWhenToolNotFound() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(rogue));
            when(toolRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> service.addProficiency(1L, "Nonexistent", 1));
        }

        @Test
        @DisplayName("should throw when already proficient")
        void addProficiency_shouldThrowWhenAlreadyProficient() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(rogue));
            when(toolRepository.findByName("Herbalism Kit")).thenReturn(Optional.of(herbalismKit));
            when(proficiencyRepository.existsByCharacterIdAndToolName(1L, "Herbalism Kit")).thenReturn(true);

            assertThrows(IllegalStateException.class,
                    () -> service.addProficiency(1L, "Herbalism Kit", 4));
        }
    }

    // ---- removeProficiency ----

    @Nested
    @DisplayName("removeProficiency Tests")
    class RemoveProficiencyTests {

        @Test
        @DisplayName("should remove a tool proficiency")
        void removeProficiency_shouldRemove() {
            when(proficiencyRepository.existsByCharacterIdAndToolName(1L, "Herbalism Kit")).thenReturn(true);
            doNothing().when(proficiencyRepository).deleteByCharacterIdAndToolName(1L, "Herbalism Kit");

            assertDoesNotThrow(() -> service.removeProficiency(1L, "Herbalism Kit"));
            verify(proficiencyRepository).deleteByCharacterIdAndToolName(1L, "Herbalism Kit");
        }

        @Test
        @DisplayName("should throw when proficiency not found")
        void removeProficiency_shouldThrowWhenNotFound() {
            when(proficiencyRepository.existsByCharacterIdAndToolName(1L, "Herbalism Kit")).thenReturn(false);

            assertThrows(IllegalArgumentException.class,
                    () -> service.removeProficiency(1L, "Herbalism Kit"));
        }
    }

    // ---- listProficiencies ----

    @Nested
    @DisplayName("listProficiencies Tests")
    class ListProficienciesTests {

        @Test
        @DisplayName("should return all proficiencies for a character")
        void listProficiencies_shouldReturnAll() {
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eCharacterToolProficiency prof1 = new Dnd5eCharacterToolProficiency();
            prof1.setToolName("Herbalism Kit");
            Dnd5eCharacterToolProficiency prof2 = new Dnd5eCharacterToolProficiency();
            prof2.setToolName("Smith's Hammer");
            when(proficiencyRepository.findByCharacterIdOrderByCreatedAtDesc(1L))
                    .thenReturn(List.of(prof1, prof2));

            List<Dnd5eCharacterToolProficiency> result = service.listProficiencies(1L);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("should throw when character not found")
        void listProficiencies_shouldThrowWhenNotFound() {
            when(characterRepository.existsById(99L)).thenReturn(false);

            assertThrows(IllegalArgumentException.class, () -> service.listProficiencies(99L));
        }
    }

    // ---- listActiveProficiencies ----

    @Nested
    @DisplayName("listActiveProficiencies Tests")
    class ListActiveProficienciesTests {

        @Test
        @DisplayName("should return only active proficiencies")
        void listActiveProficiencies_shouldReturnActive() {
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eCharacterToolProficiency prof = new Dnd5eCharacterToolProficiency();
            prof.setToolName("Herbalism Kit");
            prof.setActive(true);
            when(proficiencyRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(List.of(prof));

            List<Dnd5eCharacterToolProficiency> result = service.listActiveProficiencies(1L);

            assertEquals(1, result.size());
        }
    }

    // ---- isProficient ----

    @Nested
    @DisplayName("isProficient Tests")
    class IsProficientTests {

        @Test
        @DisplayName("should return true when proficient and active")
        void isProficient_shouldReturnTrue() {
            Dnd5eCharacterToolProficiency prof = new Dnd5eCharacterToolProficiency();
            prof.setActive(true);
            when(proficiencyRepository.findByCharacterIdAndToolName(1L, "Herbalism Kit"))
                    .thenReturn(Optional.of(prof));

            assertTrue(service.isProficient(1L, "Herbalism Kit"));
        }

        @Test
        @DisplayName("should return false when not proficient")
        void isProficient_shouldReturnFalseWhenNotProficient() {
            when(proficiencyRepository.findByCharacterIdAndToolName(1L, "Herbalism Kit"))
                    .thenReturn(Optional.empty());

            assertFalse(service.isProficient(1L, "Herbalism Kit"));
        }

        @Test
        @DisplayName("should return false when proficiency is inactive")
        void isProficient_shouldReturnFalseWhenInactive() {
            Dnd5eCharacterToolProficiency prof = new Dnd5eCharacterToolProficiency();
            prof.setActive(false);
            when(proficiencyRepository.findByCharacterIdAndToolName(1L, "Herbalism Kit"))
                    .thenReturn(Optional.of(prof));

            assertFalse(service.isProficient(1L, "Herbalism Kit"));
        }
    }

    // ---- hasExpertise ----

    @Nested
    @DisplayName("hasExpertise Tests")
    class HasExpertiseTests {

        @Test
        @DisplayName("should return true when has expertise")
        void hasExpertise_shouldReturnTrue() {
            Dnd5eCharacterToolProficiency prof = new Dnd5eCharacterToolProficiency();
            prof.setExpertise(true);
            when(proficiencyRepository.findByCharacterIdAndToolName(1L, "Herbalism Kit"))
                    .thenReturn(Optional.of(prof));

            assertTrue(service.hasExpertise(1L, "Herbalism Kit"));
        }

        @Test
        @DisplayName("should return false when no expertise")
        void hasExpertise_shouldReturnFalse() {
            Dnd5eCharacterToolProficiency prof = new Dnd5eCharacterToolProficiency();
            prof.setExpertise(false);
            when(proficiencyRepository.findByCharacterIdAndToolName(1L, "Herbalism Kit"))
                    .thenReturn(Optional.of(prof));

            assertFalse(service.hasExpertise(1L, "Herbalism Kit"));
        }
    }

    // ---- setExpertise ----

    @Nested
    @DisplayName("setExpertise Tests")
    class SetExpertiseTests {

        @Test
        @DisplayName("should set expertise to true")
        void setExpertise_shouldSetTrue() {
            Dnd5eCharacterToolProficiency prof = new Dnd5eCharacterToolProficiency();
            prof.setExpertise(false);
            when(proficiencyRepository.findByCharacterIdAndToolName(1L, "Herbalism Kit"))
                    .thenReturn(Optional.of(prof));
            when(proficiencyRepository.save(any(Dnd5eCharacterToolProficiency.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Dnd5eCharacterToolProficiency result = service.setExpertise(1L, "Herbalism Kit", true);

            assertTrue(result.isExpertise());
        }

        @Test
        @DisplayName("should set expertise to false")
        void setExpertise_shouldSetFalse() {
            Dnd5eCharacterToolProficiency prof = new Dnd5eCharacterToolProficiency();
            prof.setExpertise(true);
            when(proficiencyRepository.findByCharacterIdAndToolName(1L, "Herbalism Kit"))
                    .thenReturn(Optional.of(prof));
            when(proficiencyRepository.save(any(Dnd5eCharacterToolProficiency.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Dnd5eCharacterToolProficiency result = service.setExpertise(1L, "Herbalism Kit", false);

            assertFalse(result.isExpertise());
        }

        @Test
        @DisplayName("should throw when proficiency not found")
        void setExpertise_shouldThrowWhenNotFound() {
            when(proficiencyRepository.findByCharacterIdAndToolName(1L, "Herbalism Kit"))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> service.setExpertise(1L, "Herbalism Kit", true));
        }
    }

    // ---- toggleActive ----

    @Nested
    @DisplayName("toggleActive Tests")
    class ToggleActiveTests {

        @Test
        @DisplayName("should toggle from active to inactive")
        void toggleActive_shouldDeactivate() {
            Dnd5eCharacterToolProficiency prof = new Dnd5eCharacterToolProficiency();
            prof.setActive(true);
            when(proficiencyRepository.findByCharacterIdAndToolName(1L, "Herbalism Kit"))
                    .thenReturn(Optional.of(prof));
            when(proficiencyRepository.save(any(Dnd5eCharacterToolProficiency.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Dnd5eCharacterToolProficiency result = service.toggleActive(1L, "Herbalism Kit");

            assertFalse(result.isActive());
        }

        @Test
        @DisplayName("should toggle from inactive to active")
        void toggleActive_shouldActivate() {
            Dnd5eCharacterToolProficiency prof = new Dnd5eCharacterToolProficiency();
            prof.setActive(false);
            when(proficiencyRepository.findByCharacterIdAndToolName(1L, "Herbalism Kit"))
                    .thenReturn(Optional.of(prof));
            when(proficiencyRepository.save(any(Dnd5eCharacterToolProficiency.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Dnd5eCharacterToolProficiency result = service.toggleActive(1L, "Herbalism Kit");

            assertTrue(result.isActive());
        }

        @Test
        @DisplayName("should throw when proficiency not found")
        void toggleActive_shouldThrowWhenNotFound() {
            when(proficiencyRepository.findByCharacterIdAndToolName(1L, "Herbalism Kit"))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> service.toggleActive(1L, "Herbalism Kit"));
        }
    }

    // ---- makeToolCheck ----

    @Nested
    @DisplayName("makeToolCheck Tests")
    class MakeToolCheckTests {

        @Test
        @DisplayName("should calculate check without proficiency")
        void makeToolCheck_shouldCalculateWithoutProficiency() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(rogue));
            when(toolRepository.findByName("Herbalism Kit")).thenReturn(Optional.of(herbalismKit));
            when(proficiencyRepository.findByCharacterIdAndToolName(1L, "Herbalism Kit"))
                    .thenReturn(Optional.empty());

            ToolCheckResult result = service.makeToolCheck(1L, "Herbalism Kit", 15);

            // Rogue WIS 14 = +2 modifier, no proficiency
            assertEquals(2, result.getAbilityModifier());
            assertEquals(0, result.getProficiencyBonus());
            assertEquals(2, result.getTotalModifier());
            assertEquals("Herbalism Kit", result.getToolName());
            assertEquals("WIS", result.getAssociatedAbility());
            assertFalse(result.isProficient());
            assertFalse(result.isExpertise());
        }

        @Test
        @DisplayName("should calculate check with proficiency")
        void makeToolCheck_shouldCalculateWithProficiency() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(rogue));
            when(toolRepository.findByName("Herbalism Kit")).thenReturn(Optional.of(herbalismKit));

            Dnd5eCharacterToolProficiency prof = new Dnd5eCharacterToolProficiency();
            prof.setActive(true);
            prof.setExpertise(false);
            when(proficiencyRepository.findByCharacterIdAndToolName(1L, "Herbalism Kit"))
                    .thenReturn(Optional.of(prof));

            ToolCheckResult result = service.makeToolCheck(1L, "Herbalism Kit", 15);

            // Rogue WIS 14 = +2, proficiency bonus = 2
            assertEquals(2, result.getAbilityModifier());
            assertEquals(2, result.getProficiencyBonus());
            assertEquals(4, result.getTotalModifier());
            assertTrue(result.isProficient());
            assertFalse(result.isExpertise());
        }

        @Test
        @DisplayName("should calculate check with expertise (double proficiency)")
        void makeToolCheck_shouldCalculateWithExpertise() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(rogue));
            when(toolRepository.findByName("Herbalism Kit")).thenReturn(Optional.of(herbalismKit));

            Dnd5eCharacterToolProficiency prof = new Dnd5eCharacterToolProficiency();
            prof.setActive(true);
            prof.setExpertise(true);
            when(proficiencyRepository.findByCharacterIdAndToolName(1L, "Herbalism Kit"))
                    .thenReturn(Optional.of(prof));

            ToolCheckResult result = service.makeToolCheck(1L, "Herbalism Kit", 15);

            // Rogue WIS 14 = +2, expertise = 2*2 = 4
            assertEquals(2, result.getAbilityModifier());
            assertEquals(4, result.getProficiencyBonus());
            assertEquals(6, result.getTotalModifier());
            assertTrue(result.isExpertise());
        }

        @Test
        @DisplayName("should include tool mechanical effects in result")
        void makeToolCheck_shouldIncludeToolEffects() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(rogue));
            when(toolRepository.findByName("Herbalism Kit")).thenReturn(Optional.of(herbalismKit));
            when(proficiencyRepository.findByCharacterIdAndToolName(1L, "Herbalism Kit"))
                    .thenReturn(Optional.empty());

            ToolCheckResult result = service.makeToolCheck(1L, "Herbalism Kit", 15);

            assertEquals("Identify and use medicinal plants", result.getCombatUse());
            assertEquals("Forage for food and medicine", result.getExplorationUse());
            assertEquals("Prepare healing salves", result.getUseAction());
        }

        @Test
        @DisplayName("should calculate STR-based tool check")
        void makeToolCheck_shouldCalculateSTRCheck() {
            when(characterRepository.findById(2L)).thenReturn(Optional.of(fighter));
            when(toolRepository.findByName("Smith's Hammer")).thenReturn(Optional.of(smithsHammer));
            when(proficiencyRepository.findByCharacterIdAndToolName(2L, "Smith's Hammer"))
                    .thenReturn(Optional.empty());

            ToolCheckResult result = service.makeToolCheck(2L, "Smith's Hammer", 15);

            // Fighter STR 16 = +3
            assertEquals(3, result.getAbilityModifier());
            assertEquals("STR", result.getAssociatedAbility());
        }

        @Test
        @DisplayName("should throw when character not found")
        void makeToolCheck_shouldThrowWhenCharacterNotFound() {
            when(characterRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> service.makeToolCheck(99L, "Herbalism Kit", 15));
        }

        @Test
        @DisplayName("should throw when tool not found")
        void makeToolCheck_shouldThrowWhenToolNotFound() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(rogue));
            when(toolRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> service.makeToolCheck(1L, "Nonexistent", 15));
        }

        @Test
        @DisplayName("should return correct DC in result")
        void makeToolCheck_shouldReturnDc() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(rogue));
            when(toolRepository.findByName("Herbalism Kit")).thenReturn(Optional.of(herbalismKit));
            when(proficiencyRepository.findByCharacterIdAndToolName(1L, "Herbalism Kit"))
                    .thenReturn(Optional.empty());

            ToolCheckResult result = service.makeToolCheck(1L, "Herbalism Kit", 20);

            assertEquals(20, result.getDc());
        }

        @Test
        @DisplayName("should return roll between 1 and 20")
        void makeToolCheck_shouldReturnValidRoll() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(rogue));
            when(toolRepository.findByName("Herbalism Kit")).thenReturn(Optional.of(herbalismKit));
            when(proficiencyRepository.findByCharacterIdAndToolName(1L, "Herbalism Kit"))
                    .thenReturn(Optional.empty());

            for (int i = 0; i < 20; i++) {
                ToolCheckResult result = service.makeToolCheck(1L, "Herbalism Kit", 10);
                int roll = result.getRoll();
                assertTrue(roll >= 1 && roll <= 20, "Roll should be between 1 and 20, was: " + roll);
            }
        }

        @Test
        @DisplayName("should return result description")
        void makeToolCheck_shouldReturnResultDescription() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(rogue));
            when(toolRepository.findByName("Herbalism Kit")).thenReturn(Optional.of(herbalismKit));
            when(proficiencyRepository.findByCharacterIdAndToolName(1L, "Herbalism Kit"))
                    .thenReturn(Optional.empty());

            ToolCheckResult result = service.makeToolCheck(1L, "Herbalism Kit", 10);

            assertNotNull(result.getResult());
            assertFalse(result.getResult().isEmpty());
        }
    }

    // ---- ToolCheckResult ----

    @Nested
    @DisplayName("ToolCheckResult Tests")
    class ToolCheckResultTests {

        @Test
        @DisplayName("should have all getters working")
        void toolCheckResult_shouldHaveAllGetters() {
            ToolCheckResult result = new ToolCheckResult(
                    15, 2, 3, 5, 20, 15, true, true, false,
                    "Success!", "Herbalism Kit", "WIS",
                    "Combat use", "Exploration use", "Use action"
            );

            assertEquals(15, result.getRoll());
            assertEquals(2, result.getAbilityModifier());
            assertEquals(3, result.getProficiencyBonus());
            assertEquals(5, result.getTotalModifier());
            assertEquals(20, result.getTotal());
            assertEquals(15, result.getDc());
            assertTrue(result.isSuccess());
            assertTrue(result.isProficient());
            assertFalse(result.isExpertise());
            assertEquals("Success!", result.getResult());
            assertEquals("Herbalism Kit", result.getToolName());
            assertEquals("WIS", result.getAssociatedAbility());
            assertEquals("Combat use", result.getCombatUse());
            assertEquals("Exploration use", result.getExplorationUse());
            assertEquals("Use action", result.getUseAction());
        }
    }
}
