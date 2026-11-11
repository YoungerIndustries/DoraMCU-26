package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eAdventureHook;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eAdventureHookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Dnd5eAdventureHookService Tests")
class Dnd5eAdventureHookServiceTest {

    @Mock
    private Dnd5eAdventureHookRepository hookRepository;

    private Dnd5eAdventureHookService service;

    private Dnd5eAdventureHook sampleHook;

    @BeforeEach
    void setUp() {
        // Stub count() to return > 0 so initializeHookDatabase skips population
        when(hookRepository.count()).thenReturn(1L);
        service = new Dnd5eAdventureHookService(hookRepository);
        sampleHook = createHook(1L, "Test Hook", "Personal", "Horror", "Immediate",
                "Soldier", null, null, "Test narrative", "Test DM notes", 1, 10);
    }

    // ==================== createHook ====================

    @Nested
    @DisplayName("createHook")
    class CreateHook {

        @Test
        @DisplayName("should create and save a hook")
        void shouldCreateAndSaveHook() {
            when(hookRepository.save(any())).thenReturn(sampleHook);

            Dnd5eAdventureHook created = service.createHook(sampleHook);

            assertThat(created).isNotNull();
            assertThat(created.getName()).isEqualTo("Test Hook");
            verify(hookRepository).save(sampleHook);
        }

        @Test
        @DisplayName("should create hook with all fields")
        void shouldCreateHookWithAllFields() {
            Dnd5eAdventureHook hook = createHook(1L, "Full Hook", "Combat", "Sandbox", "Immediate",
                    "Soldier", "Dragonborn", "Fighter", "Full narrative", "Full DM notes", 5, 15);
            when(hookRepository.save(hook)).thenReturn(hook);

            Dnd5eAdventureHook created = service.createHook(hook);

            assertThat(created.getType()).isEqualTo("Combat");
            assertThat(created.getTheme()).isEqualTo("Sandbox");
            assertThat(created.getUrgency()).isEqualTo("Immediate");
            assertThat(created.getTargetBackground()).isEqualTo("Soldier");
            assertThat(created.getTargetRace()).isEqualTo("Dragonborn");
            assertThat(created.getTargetClass()).isEqualTo("Fighter");
        }
    }

    // ==================== getHook ====================

    @Nested
    @DisplayName("getHook")
    class GetHook {

        @Test
        @DisplayName("should return hook when found")
        void shouldReturnHookWhenFound() {
            when(hookRepository.findById(1L)).thenReturn(Optional.of(sampleHook));

            Optional<Dnd5eAdventureHook> found = service.getHook(1L);

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Test Hook");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            when(hookRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Dnd5eAdventureHook> found = service.getHook(999L);

            assertThat(found).isEmpty();
        }
    }

    // ==================== getHookByName ====================

    @Nested
    @DisplayName("getHookByName")
    class GetHookByName {

        @Test
        @DisplayName("should return hook when found by name")
        void shouldReturnHookWhenFoundByName() {
            when(hookRepository.findByName("Test Hook")).thenReturn(Optional.of(sampleHook));

            Optional<Dnd5eAdventureHook> found = service.getHookByName("Test Hook");

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Test Hook");
        }

        @Test
        @DisplayName("should return empty when not found by name")
        void shouldReturnEmptyWhenNotFoundByName() {
            when(hookRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

            Optional<Dnd5eAdventureHook> found = service.getHookByName("Nonexistent");

            assertThat(found).isEmpty();
        }
    }

    // ==================== listHooks ====================

    @Nested
    @DisplayName("listHooks")
    class ListHooks {

        @Test
        @DisplayName("should return all hooks")
        void shouldReturnAllHooks() {
            List<Dnd5eAdventureHook> hooks = Arrays.asList(sampleHook);
            when(hookRepository.findAll()).thenReturn(hooks);

            List<Dnd5eAdventureHook> result = service.listHooks();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Test Hook");
        }

        @Test
        @DisplayName("should return empty list when no hooks")
        void shouldReturnEmptyListWhenNoHooks() {
            when(hookRepository.findAll()).thenReturn(Collections.emptyList());

            List<Dnd5eAdventureHook> result = service.listHooks();

            assertThat(result).isEmpty();
        }
    }

    // ==================== listActiveHooks ====================

    @Nested
    @DisplayName("listActiveHooks")
    class ListActiveHooks {

        @Test
        @DisplayName("should return only active hooks")
        void shouldReturnOnlyActiveHooks() {
            when(hookRepository.findByActiveTrue()).thenReturn(Arrays.asList(sampleHook));

            List<Dnd5eAdventureHook> result = service.listActiveHooks();

            assertThat(result).hasSize(1);
            verify(hookRepository).findByActiveTrue();
        }
    }

    // ==================== listHooksByType ====================

    @Nested
    @DisplayName("listHooksByType")
    class ListHooksByType {

        @Test
        @DisplayName("should return hooks filtered by type")
        void shouldReturnHooksFilteredByType() {
            when(hookRepository.findByTypeAndActiveTrue("Personal")).thenReturn(Arrays.asList(sampleHook));

            List<Dnd5eAdventureHook> result = service.listHooksByType("Personal");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getType()).isEqualTo("Personal");
        }
    }

    // ==================== listHooksByTheme ====================

    @Nested
    @DisplayName("listHooksByTheme")
    class ListHooksByTheme {

        @Test
        @DisplayName("should return hooks filtered by theme")
        void shouldReturnHooksFilteredByTheme() {
            when(hookRepository.findByThemeAndActiveTrue("Horror")).thenReturn(Arrays.asList(sampleHook));

            List<Dnd5eAdventureHook> result = service.listHooksByTheme("Horror");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTheme()).isEqualTo("Horror");
        }
    }

    // ==================== listHooksByUrgency ====================

    @Nested
    @DisplayName("listHooksByUrgency")
    class ListHooksByUrgency {

        @Test
        @DisplayName("should return hooks filtered by urgency")
        void shouldReturnHooksFilteredByUrgency() {
            when(hookRepository.findByUrgencyAndActiveTrue("Immediate")).thenReturn(Arrays.asList(sampleHook));

            List<Dnd5eAdventureHook> result = service.listHooksByUrgency("Immediate");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUrgency()).isEqualTo("Immediate");
        }
    }

    // ==================== searchHooks ====================

    @Nested
    @DisplayName("searchHooks")
    class SearchHooks {

        @Test
        @DisplayName("should return hooks matching keyword")
        void shouldReturnHooksMatchingKeyword() {
            when(hookRepository.searchByKeyword("dragon")).thenReturn(Arrays.asList(sampleHook));

            List<Dnd5eAdventureHook> result = service.searchHooks("dragon");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty list when no matches")
        void shouldReturnEmptyListWhenNoMatches() {
            when(hookRepository.searchByKeyword("nonexistent")).thenReturn(Collections.emptyList());

            List<Dnd5eAdventureHook> result = service.searchHooks("nonexistent");

            assertThat(result).isEmpty();
        }
    }

    // ==================== updateHook ====================

    @Nested
    @DisplayName("updateHook")
    class UpdateHook {

        @Test
        @DisplayName("should update hook fields")
        void shouldUpdateHookFields() {
            when(hookRepository.findById(1L)).thenReturn(Optional.of(sampleHook));
            when(hookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eAdventureHook updates = new Dnd5eAdventureHook();
            updates.setName("Updated Hook");
            updates.setType("Combat");

            Dnd5eAdventureHook updated = service.updateHook(1L, updates);

            assertThat(updated.getName()).isEqualTo("Updated Hook");
            assertThat(updated.getType()).isEqualTo("Combat");
        }

        @Test
        @DisplayName("should throw when hook not found")
        void shouldThrowWhenHookNotFound() {
            when(hookRepository.findById(999L)).thenReturn(Optional.empty());

            Dnd5eAdventureHook updates = new Dnd5eAdventureHook();
            updates.setName("Updated");

            assertThatThrownBy(() -> service.updateHook(999L, updates))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not found");
        }
    }

    // ==================== deleteHook ====================

    @Nested
    @DisplayName("deleteHook")
    class DeleteHook {

        @Test
        @DisplayName("should delete hook and return true")
        void shouldDeleteHookAndReturnTrue() {
            when(hookRepository.findById(1L)).thenReturn(Optional.of(sampleHook));
            doNothing().when(hookRepository).delete(sampleHook);

            boolean deleted = service.deleteHook(1L);

            assertThat(deleted).isTrue();
            verify(hookRepository).delete(sampleHook);
        }

        @Test
        @DisplayName("should return false when hook not found")
        void shouldReturnFalseWhenHookNotFound() {
            when(hookRepository.findById(999L)).thenReturn(Optional.empty());

            boolean deleted = service.deleteHook(999L);

            assertThat(deleted).isFalse();
        }
    }

    // ==================== toggleHookActive ====================

    @Nested
    @DisplayName("toggleHookActive")
    class ToggleHookActive {

        @Test
        @DisplayName("should toggle active state from true to false")
        void shouldToggleActiveStateFromTrueToFalse() {
            sampleHook.setActive(true);
            when(hookRepository.findById(1L)).thenReturn(Optional.of(sampleHook));
            when(hookRepository.save(sampleHook)).thenReturn(sampleHook);

            service.toggleHookActive(1L);

            assertThat(sampleHook.isActive()).isFalse();
            verify(hookRepository).save(sampleHook);
        }

        @Test
        @DisplayName("should toggle active state from false to true")
        void shouldToggleActiveStateFromFalseToTrue() {
            sampleHook.setActive(false);
            when(hookRepository.findById(1L)).thenReturn(Optional.of(sampleHook));
            when(hookRepository.save(sampleHook)).thenReturn(sampleHook);

            service.toggleHookActive(1L);

            assertThat(sampleHook.isActive()).isTrue();
        }
    }

    // ==================== generateHooksForCharacter ====================

    @Nested
    @DisplayName("generateHooksForCharacter")
    class GenerateHooksForCharacter {

        @Test
        @DisplayName("should generate hooks matching character profile")
        void shouldGenerateHooksMatchingCharacterProfile() {
            Dnd5eCharacter character = createCharacter("Test", "Elf", "Wizard", "Sage", 5);
            List<Dnd5eAdventureHook> hooks = Arrays.asList(sampleHook);
            when(hookRepository.findByCharacterProfile(5, "Sage", "Elf", "Wizard")).thenReturn(hooks);

            List<Dnd5eAdventureHook> result = service.generateHooksForCharacter(character);

            assertThat(result).hasSize(1);
            verify(hookRepository).findByCharacterProfile(5, "Sage", "Elf", "Wizard");
        }

        @Test
        @DisplayName("should handle null background")
        void shouldHandleNullBackground() {
            Dnd5eCharacter character = createCharacter("Test", "Human", "Fighter", null, 1);
            when(hookRepository.findByCharacterProfile(1, "", "Human", "Fighter"))
                    .thenReturn(Collections.emptyList());

            List<Dnd5eAdventureHook> result = service.generateHooksForCharacter(character);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty list when no matches")
        void shouldReturnEmptyListWhenNoMatches() {
            Dnd5eCharacter character = createCharacter("Test", "Tiefling", "Warlock", "Charlatan", 3);
            when(hookRepository.findByCharacterProfile(3, "Charlatan", "Tiefling", "Warlock"))
                    .thenReturn(Collections.emptyList());

            List<Dnd5eAdventureHook> result = service.generateHooksForCharacter(character);

            assertThat(result).isEmpty();
        }
    }

    // ==================== generateHooksForCharacterWithTheme ====================

    @Nested
    @DisplayName("generateHooksForCharacterWithTheme")
    class GenerateHooksForCharacterWithTheme {

        @Test
        @DisplayName("should generate hooks matching character profile and theme")
        void shouldGenerateHooksMatchingCharacterProfileAndTheme() {
            Dnd5eCharacter character = createCharacter("Test", "Elf", "Wizard", "Sage", 5);
            List<Dnd5eAdventureHook> hooks = Arrays.asList(sampleHook);
            when(hookRepository.findByCharacterProfileAndTheme(5, "Sage", "Elf", "Wizard", "Horror"))
                    .thenReturn(hooks);

            List<Dnd5eAdventureHook> result = service.generateHooksForCharacterWithTheme(character, "Horror");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty when no themed matches")
        void shouldReturnEmptyWhenNoThemedMatches() {
            Dnd5eCharacter character = createCharacter("Test", "Human", "Cleric", "Acolyte", 1);
            when(hookRepository.findByCharacterProfileAndTheme(1, "Acolyte", "Human", "Cleric", "Steampunk"))
                    .thenReturn(Collections.emptyList());

            List<Dnd5eAdventureHook> result = service.generateHooksForCharacterWithTheme(character, "Steampunk");

            assertThat(result).isEmpty();
        }
    }

    // ==================== generateHooksByLevel ====================

    @Nested
    @DisplayName("generateHooksByLevel")
    class GenerateHooksByLevel {

        @Test
        @DisplayName("should generate hooks for a specific level")
        void shouldGenerateHooksForSpecificLevel() {
            when(hookRepository.findByCharacterLevel(5)).thenReturn(Arrays.asList(sampleHook));

            List<Dnd5eAdventureHook> result = service.generateHooksByLevel(5);

            assertThat(result).hasSize(1);
            verify(hookRepository).findByCharacterLevel(5);
        }

        @Test
        @DisplayName("should return empty for level with no hooks")
        void shouldReturnEmptyForLevelWithNoHooks() {
            when(hookRepository.findByCharacterLevel(20)).thenReturn(Collections.emptyList());

            List<Dnd5eAdventureHook> result = service.generateHooksByLevel(20);

            assertThat(result).isEmpty();
        }
    }

    // ==================== generateHooksByBackground ====================

    @Nested
    @DisplayName("generateHooksByBackground")
    class GenerateHooksByBackground {

        @Test
        @DisplayName("should generate hooks for a specific background")
        void shouldGenerateHooksForSpecificBackground() {
            when(hookRepository.findByBackgroundOrAny("Soldier")).thenReturn(Arrays.asList(sampleHook));

            List<Dnd5eAdventureHook> result = service.generateHooksByBackground("Soldier");

            assertThat(result).hasSize(1);
        }
    }

    // ==================== generateHooksByRace ====================

    @Nested
    @DisplayName("generateHooksByRace")
    class GenerateHooksByRace {

        @Test
        @DisplayName("should generate hooks for a specific race")
        void shouldGenerateHooksForSpecificRace() {
            when(hookRepository.findByRaceOrAny("Elf")).thenReturn(Arrays.asList(sampleHook));

            List<Dnd5eAdventureHook> result = service.generateHooksByRace("Elf");

            assertThat(result).hasSize(1);
        }
    }

    // ==================== generateHooksByClass ====================

    @Nested
    @DisplayName("generateHooksByClass")
    class GenerateHooksByClass {

        @Test
        @DisplayName("should generate hooks for a specific class")
        void shouldGenerateHooksForSpecificClass() {
            when(hookRepository.findByClassOrAny("Wizard")).thenReturn(Arrays.asList(sampleHook));

            List<Dnd5eAdventureHook> result = service.generateHooksByClass("Wizard");

            assertThat(result).hasSize(1);
        }
    }

    // ==================== generateRandomHook ====================

    @Nested
    @DisplayName("generateRandomHook")
    class GenerateRandomHook {

        @Test
        @DisplayName("should return a random hook from active hooks")
        void shouldReturnRandomHookFromActiveHooks() {
            List<Dnd5eAdventureHook> hooks = Arrays.asList(sampleHook);
            when(hookRepository.findByActiveTrue()).thenReturn(hooks);

            Optional<Dnd5eAdventureHook> result = service.generateRandomHook();

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("should return empty when no active hooks")
        void shouldReturnEmptyWhenNoActiveHooks() {
            when(hookRepository.findByActiveTrue()).thenReturn(Collections.emptyList());

            Optional<Dnd5eAdventureHook> result = service.generateRandomHook();

            assertThat(result).isEmpty();
        }
    }

    // ==================== generateRandomHookByType ====================

    @Nested
    @DisplayName("generateRandomHookByType")
    class GenerateRandomHookByType {

        @Test
        @DisplayName("should return a random hook of the specified type")
        void shouldReturnRandomHookOfSpecifiedType() {
            when(hookRepository.findByTypeAndActiveTrue("Personal")).thenReturn(Arrays.asList(sampleHook));

            Optional<Dnd5eAdventureHook> result = service.generateRandomHookByType("Personal");

            assertThat(result).isPresent();
            assertThat(result.get().getType()).isEqualTo("Personal");
        }

        @Test
        @DisplayName("should return empty when no hooks of the type")
        void shouldReturnEmptyWhenNoHooksofTheType() {
            when(hookRepository.findByTypeAndActiveTrue("Nonexistent")).thenReturn(Collections.emptyList());

            Optional<Dnd5eAdventureHook> result = service.generateRandomHookByType("Nonexistent");

            assertThat(result).isEmpty();
        }
    }

    // ==================== generateRandomHookByTheme ====================

    @Nested
    @DisplayName("generateRandomHookByTheme")
    class GenerateRandomHookByTheme {

        @Test
        @DisplayName("should return a random hook of the specified theme")
        void shouldReturnRandomHookOfSpecifiedTheme() {
            when(hookRepository.findByThemeAndActiveTrue("Horror")).thenReturn(Arrays.asList(sampleHook));

            Optional<Dnd5eAdventureHook> result = service.generateRandomHookByTheme("Horror");

            assertThat(result).isPresent();
            assertThat(result.get().getTheme()).isEqualTo("Horror");
        }

        @Test
        @DisplayName("should return empty when no hooks of the theme")
        void shouldReturnEmptyWhenNoHooksofTheTheme() {
            when(hookRepository.findByThemeAndActiveTrue("Nonexistent")).thenReturn(Collections.emptyList());

            Optional<Dnd5eAdventureHook> result = service.generateRandomHookByTheme("Nonexistent");

            assertThat(result).isEmpty();
        }
    }

    // ==================== Reference Data ====================

    @Nested
    @DisplayName("getHookTypes")
    class GetHookTypes {

        @Test
        @DisplayName("should return distinct hook types")
        void shouldReturnDistinctHookTypes() {
            when(hookRepository.findDistinctTypes()).thenReturn(Arrays.asList("Personal", "Combat", "Mystery"));

            List<String> types = service.getHookTypes();

            assertThat(types).containsExactlyInAnyOrder("Personal", "Combat", "Mystery");
        }
    }

    @Nested
    @DisplayName("getHookThemes")
    class GetHookThemes {

        @Test
        @DisplayName("should return distinct hook themes")
        void shouldReturnDistinctHookThemes() {
            when(hookRepository.findDistinctThemes()).thenReturn(Arrays.asList("Horror", "Sandbox", "High Magic"));

            List<String> themes = service.getHookThemes();

            assertThat(themes).containsExactlyInAnyOrder("Horror", "Sandbox", "High Magic");
        }
    }

    @Nested
    @DisplayName("getHookUrgencies")
    class GetHookUrgencies {

        @Test
        @DisplayName("should return distinct hook urgencies")
        void shouldReturnDistinctHookUrgencies() {
            when(hookRepository.findDistinctUrgencies()).thenReturn(Arrays.asList("Immediate", "Soon", "Eventual"));

            List<String> urgencies = service.getHookUrgencies();

            assertThat(urgencies).containsExactlyInAnyOrder("Immediate", "Soon", "Eventual");
        }
    }

    // ==================== initializeHookDatabase ====================

    @Nested
    @DisplayName("initializeHookDatabase")
    class InitializeHookDatabase {

        @Test
        @DisplayName("should skip initialization if hooks already populated")
        void shouldSkipInitializationIfHooksAlreadyPopulated() {
            // The service was created in setUp() with count > 0 already mocked by default
            // The initializeHookDatabase should have called count() once and skipped
            verify(hookRepository, atLeast(1)).count();
        }

        @Test
        @DisplayName("should populate hooks if database is empty")
        void shouldPopulateHooksIfDatabaseIsEmpty() {
            // Create a fresh mock repository to avoid interference from @BeforeEach
            Dnd5eAdventureHookRepository freshRepo = mock(Dnd5eAdventureHookRepository.class);
            when(freshRepo.count()).thenReturn(0L);
            when(freshRepo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            new Dnd5eAdventureHookService(freshRepo);

            verify(freshRepo).saveAll(anyList());
        }
    }

    // ==================== listHooksByTargetBackground ====================

    @Nested
    @DisplayName("listHooksByTargetBackground")
    class ListHooksByTargetBackground {

        @Test
        @DisplayName("should return hooks targeting a specific background")
        void shouldReturnHooksTargetingSpecificBackground() {
            when(hookRepository.findByTargetBackground("Soldier")).thenReturn(Arrays.asList(sampleHook));

            List<Dnd5eAdventureHook> result = service.listHooksByTargetBackground("Soldier");

            assertThat(result).hasSize(1);
        }
    }

    // ==================== listHooksByTargetRace ====================

    @Nested
    @DisplayName("listHooksByTargetRace")
    class ListHooksByTargetRace {

        @Test
        @DisplayName("should return hooks targeting a specific race")
        void shouldReturnHooksTargetingSpecificRace() {
            when(hookRepository.findByTargetRace("Elf")).thenReturn(Arrays.asList(sampleHook));

            List<Dnd5eAdventureHook> result = service.listHooksByTargetRace("Elf");

            assertThat(result).hasSize(1);
        }
    }

    // ==================== listHooksByTargetClass ====================

    @Nested
    @DisplayName("listHooksByTargetClass")
    class ListHooksByTargetClass {

        @Test
        @DisplayName("should return hooks targeting a specific class")
        void shouldReturnHooksTargetingSpecificClass() {
            when(hookRepository.findByTargetClass("Wizard")).thenReturn(Arrays.asList(sampleHook));

            List<Dnd5eAdventureHook> result = service.listHooksByTargetClass("Wizard");

            assertThat(result).hasSize(1);
        }
    }

    // ==================== Helper Methods ====================

    private Dnd5eAdventureHook createHook(Long id, String name, String type, String theme, String urgency,
                                          String targetBackground, String targetRace, String targetClass,
                                          String narrative, String dmNotes, int minLevel, int maxLevel) {
        Dnd5eAdventureHook hook = new Dnd5eAdventureHook();
        hook.setId(id);
        hook.setName(name);
        hook.setType(type);
        hook.setTheme(theme);
        hook.setUrgency(urgency);
        hook.setTargetBackground(targetBackground);
        hook.setTargetRace(targetRace);
        hook.setTargetClass(targetClass);
        hook.setNarrative(narrative);
        hook.setDmNotes(dmNotes);
        hook.setMinLevel(minLevel);
        hook.setMaxLevel(maxLevel);
        hook.setActive(true);
        return hook;
    }

    private Dnd5eCharacter createCharacter(String name, String race, String characterClass,
                                           String background, int level) {
        Dnd5eCharacter character = new Dnd5eCharacter();
        character.setName(name);
        character.setRace(race);
        character.setCharacterClass(characterClass);
        character.setBackground(background);
        character.setLevel(level);
        character.setHitPoints(10);
        character.setMaxHitPoints(10);
        return character;
    }
}
