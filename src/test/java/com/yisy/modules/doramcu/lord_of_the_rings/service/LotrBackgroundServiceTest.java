package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrBackground;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrBackgroundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("LotrBackgroundService Tests")
class LotrBackgroundServiceTest {

    private LotrBackgroundRepository backgroundRepository;
    private LotrBackgroundService backgroundService;

    @BeforeEach
    void setUp() {
        backgroundRepository = mock(LotrBackgroundRepository.class);
        backgroundService = new LotrBackgroundService(backgroundRepository);
    }

    // ---- findAll ----

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("should return all backgrounds")
        void findAll_shouldReturnAllBackgrounds() {
            // Arrange
            LotrBackground bg = new LotrBackground();
            bg.setId(1L);
            bg.setName("Ranger of the North");
            when(backgroundRepository.findAll()).thenReturn(List.of(bg));

            // Act
            List<LotrBackground> result = backgroundService.findAll();

            // Assert
            assertEquals(1, result.size());
            assertEquals("Ranger of the North", result.get(0).getName());
        }

        @Test
        @DisplayName("should return empty list when no backgrounds")
        void findAll_shouldReturnEmptyList() {
            // Arrange
            when(backgroundRepository.findAll()).thenReturn(List.of());

            // Act
            List<LotrBackground> result = backgroundService.findAll();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- findActiveBackgrounds ----

    @Nested
    @DisplayName("findActiveBackgrounds Tests")
    class FindActiveBackgroundsTests {

        @Test
        @DisplayName("should return only active backgrounds")
        void findActiveBackgrounds_shouldReturnOnlyActive() {
            // Arrange
            LotrBackground active = new LotrBackground();
            active.setId(1L);
            active.setName("Ranger of the North");
            active.setActive(true);

            LotrBackground inactive = new LotrBackground();
            inactive.setId(2L);
            inactive.setName("Old Background");
            inactive.setActive(false);

            when(backgroundRepository.findAll()).thenReturn(List.of(active, inactive));

            // Act
            List<LotrBackground> result = backgroundService.findActiveBackgrounds();

            // Assert
            assertEquals(1, result.size());
            assertEquals("Ranger of the North", result.get(0).getName());
        }
    }

    // ---- findById ----

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("should return background when found")
        void findById_shouldReturnBackground() {
            // Arrange
            LotrBackground bg = new LotrBackground();
            bg.setId(1L);
            bg.setName("Steward of Gondor");
            when(backgroundRepository.findById(1L)).thenReturn(Optional.of(bg));

            // Act
            Optional<LotrBackground> result = backgroundService.findById(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("Steward of Gondor", result.get().getName());
        }

        @Test
        @DisplayName("should return empty when not found")
        void findById_shouldReturnEmpty() {
            // Arrange
            when(backgroundRepository.findById(99L)).thenReturn(Optional.empty());

            // Act
            Optional<LotrBackground> result = backgroundService.findById(99L);

            // Assert
            assertFalse(result.isPresent());
        }
    }

    // ---- findByName ----

    @Nested
    @DisplayName("findByName Tests")
    class FindByNameTests {

        @Test
        @DisplayName("should find background by partial name")
        void findByName_shouldFindPartialMatch() {
            // Arrange
            LotrBackground bg = new LotrBackground();
            bg.setId(1L);
            bg.setName("Hobbit of the Shire");
            when(backgroundRepository.findAll()).thenReturn(List.of(bg));

            // Act
            List<LotrBackground> result = backgroundService.findByName("hobbit");

            // Assert
            assertEquals(1, result.size());
            assertEquals("Hobbit of the Shire", result.get(0).getName());
        }

        @Test
        @DisplayName("should be case insensitive")
        void findByName_shouldBeCaseInsensitive() {
            // Arrange
            LotrBackground bg = new LotrBackground();
            bg.setId(1L);
            bg.setName("Elf of Rivendell");
            when(backgroundRepository.findAll()).thenReturn(List.of(bg));

            // Act
            List<LotrBackground> result = backgroundService.findByName("ELF");

            // Assert
            assertEquals(1, result.size());
        }
    }

    // ---- createBackground ----

    @Nested
    @DisplayName("createBackground Tests")
    class CreateBackgroundTests {

        @Test
        @DisplayName("should create a new background")
        void createBackground_shouldCreateBackground() {
            // Arrange
            when(backgroundRepository.save(any(LotrBackground.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrBackground result = backgroundService.createBackground(
                    "Ranger of the North", "A wanderer of the northern lands",
                    "Survival", "Perception", "Navigator's tools", "Sindarin",
                    "Wanderer's Insight", "Advantage on Survival checks in wilderness.");

            // Assert
            assertNotNull(result);
            assertEquals("Ranger of the North", result.getName());
            assertEquals("Survival", result.getSkillProficiency1());
            assertEquals("Perception", result.getSkillProficiency2());
            assertEquals("Navigator's tools", result.getToolProficiency());
            assertEquals("Sindarin", result.getLanguage());
            assertEquals("Wanderer's Insight", result.getFeatureName());
            assertTrue(result.isActive());
            verify(backgroundRepository).save(result);
        }
    }

    // ---- updateBackground ----

    @Nested
    @DisplayName("updateBackground Tests")
    class UpdateBackgroundTests {

        @Test
        @DisplayName("should update an existing background")
        void updateBackground_shouldUpdateBackground() {
            // Arrange
            LotrBackground existing = new LotrBackground();
            existing.setId(1L);
            existing.setName("Ranger of the North");
            when(backgroundRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(backgroundRepository.save(any(LotrBackground.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrBackground result = backgroundService.updateBackground(
                    1L, "Updated Ranger", "Updated description",
                    "Stealth", "Athletics", "Thieves' tools", "Common",
                    "New Feature", "New feature description");

            // Assert
            assertEquals("Updated Ranger", result.getName());
            assertEquals("Stealth", result.getSkillProficiency1());
            verify(backgroundRepository).save(result);
        }

        @Test
        @DisplayName("should throw when background not found")
        void updateBackground_shouldThrowWhenNotFound() {
            // Arrange
            when(backgroundRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    backgroundService.updateBackground(99L, "Name", "Desc", "s1", "s2", "t", "l", "f", "fd"));
        }
    }

    // ---- deleteBackground ----

    @Nested
    @DisplayName("deleteBackground Tests")
    class DeleteBackgroundTests {

        @Test
        @DisplayName("should delete an existing background")
        void deleteBackground_shouldDeleteBackground() {
            // Arrange
            when(backgroundRepository.existsById(1L)).thenReturn(true);

            // Act
            backgroundService.deleteBackground(1L);

            // Assert
            verify(backgroundRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw when background not found")
        void deleteBackground_shouldThrowWhenNotFound() {
            // Arrange
            when(backgroundRepository.existsById(99L)).thenReturn(false);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> backgroundService.deleteBackground(99L));
        }
    }

    // ---- initializeBackgroundDatabase ----

    @Nested
    @DisplayName("initializeBackgroundDatabase Tests")
    class InitializeBackgroundDatabaseTests {

        @Test
        @DisplayName("should skip if database already has backgrounds")
        void initializeBackgroundDatabase_shouldSkipIfNotEmpty() {
            // Arrange
            LotrBackground bg = new LotrBackground();
            bg.setId(1L);
            when(backgroundRepository.findAll()).thenReturn(List.of(bg));

            // Act
            backgroundService.initializeBackgroundDatabase();

            // Assert
            verify(backgroundRepository, never()).save(any());
        }

        @Test
        @DisplayName("should create 8 LOTR backgrounds when empty")
        void initializeBackgroundDatabase_shouldCreateBackgrounds() {
            // Arrange
            when(backgroundRepository.findAll()).thenReturn(List.of());
            when(backgroundRepository.save(any(LotrBackground.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            backgroundService.initializeBackgroundDatabase();

            // Assert
            // 8 backgrounds: Ranger of the North, Steward of Gondor, Hobbit of the Shire,
            // Elf of Rivendell, Dwarf of the Mountains, Rohirrim Rider, Outlaw of the Wild, Scholar of Lore
            verify(backgroundRepository, times(8)).save(any(LotrBackground.class));
        }
    }
}
