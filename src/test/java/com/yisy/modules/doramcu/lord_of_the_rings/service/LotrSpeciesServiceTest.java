package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrSpecies;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrSpeciesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("LotrSpeciesService Tests")
class LotrSpeciesServiceTest {

    private LotrSpeciesRepository speciesRepository;
    private LotrSpeciesService speciesService;

    @BeforeEach
    void setUp() {
        speciesRepository = mock(LotrSpeciesRepository.class);
        speciesService = new LotrSpeciesService(speciesRepository);
    }

    // ---- findAll ----

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("should return all species")
        void findAll_shouldReturnAllSpecies() {
            // Arrange
            LotrSpecies species = new LotrSpecies();
            species.setId(1L);
            species.setName("Elf");
            when(speciesRepository.findAll()).thenReturn(List.of(species));

            // Act
            List<LotrSpecies> result = speciesService.findAll();

            // Assert
            assertEquals(1, result.size());
            assertEquals("Elf", result.get(0).getName());
        }

        @Test
        @DisplayName("should return empty list when no species")
        void findAll_shouldReturnEmptyList() {
            // Arrange
            when(speciesRepository.findAll()).thenReturn(List.of());

            // Act
            List<LotrSpecies> result = speciesService.findAll();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- findActiveSpecies ----

    @Nested
    @DisplayName("findActiveSpecies Tests")
    class FindActiveSpeciesTests {

        @Test
        @DisplayName("should return only active species")
        void findActiveSpecies_shouldReturnOnlyActive() {
            // Arrange
            LotrSpecies active = new LotrSpecies();
            active.setId(1L);
            active.setName("Elf");
            active.setActive(true);

            LotrSpecies inactive = new LotrSpecies();
            inactive.setId(2L);
            inactive.setName("Extinct Species");
            inactive.setActive(false);

            when(speciesRepository.findAll()).thenReturn(List.of(active, inactive));

            // Act
            List<LotrSpecies> result = speciesService.findActiveSpecies();

            // Assert
            assertEquals(1, result.size());
            assertEquals("Elf", result.get(0).getName());
        }
    }

    // ---- findById ----

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("should return species when found")
        void findById_shouldReturnSpecies() {
            // Arrange
            LotrSpecies species = new LotrSpecies();
            species.setId(1L);
            species.setName("Dwarf");
            when(speciesRepository.findById(1L)).thenReturn(Optional.of(species));

            // Act
            Optional<LotrSpecies> result = speciesService.findById(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("Dwarf", result.get().getName());
        }

        @Test
        @DisplayName("should return empty when not found")
        void findById_shouldReturnEmpty() {
            // Arrange
            when(speciesRepository.findById(99L)).thenReturn(Optional.empty());

            // Act
            Optional<LotrSpecies> result = speciesService.findById(99L);

            // Assert
            assertFalse(result.isPresent());
        }
    }

    // ---- findByName ----

    @Nested
    @DisplayName("findByName Tests")
    class FindByNameTests {

        @Test
        @DisplayName("should find species by partial name")
        void findByName_shouldFindPartialMatch() {
            // Arrange
            LotrSpecies species = new LotrSpecies();
            species.setId(1L);
            species.setName("Hobbit");
            when(speciesRepository.findAll()).thenReturn(List.of(species));

            // Act
            List<LotrSpecies> result = speciesService.findByName("hob");

            // Assert
            assertEquals(1, result.size());
            assertEquals("Hobbit", result.get(0).getName());
        }

        @Test
        @DisplayName("should be case insensitive")
        void findByName_shouldBeCaseInsensitive() {
            // Arrange
            LotrSpecies species = new LotrSpecies();
            species.setId(1L);
            species.setName("Maia");
            when(speciesRepository.findAll()).thenReturn(List.of(species));

            // Act
            List<LotrSpecies> result = speciesService.findByName("MAI");

            // Assert
            assertEquals(1, result.size());
        }
    }

    // ---- createSpecies ----

    @Nested
    @DisplayName("createSpecies Tests")
    class CreateSpeciesTests {

        @Test
        @DisplayName("should create a new species")
        void createSpecies_shouldCreateSpecies() {
            // Arrange
            when(speciesRepository.save(any(LotrSpecies.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrSpecies result = speciesService.createSpecies(
                    "Elf", "Graceful and long-lived", "Medium", 30, true,
                    "Adolescence at 100", "5-6 feet tall", "100-160 pounds", "+2 Dex, +1 Cha");

            // Assert
            assertNotNull(result);
            assertEquals("Elf", result.getName());
            assertEquals("Medium", result.getSize());
            assertEquals(30, result.getSpeed());
            assertTrue(result.isDarkvision());
            assertEquals("+2 Dex, +1 Cha", result.getAbilityIncrease());
            assertTrue(result.isActive());
            verify(speciesRepository).save(result);
        }

        @Test
        @DisplayName("should create a species without darkvision")
        void createSpecies_shouldCreateSpeciesWithoutDarkvision() {
            // Arrange
            when(speciesRepository.save(any(LotrSpecies.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrSpecies result = speciesService.createSpecies(
                    "Hobbit", "Small and unassuming", "Small", 25, false,
                    "Adolescence at 7", "2.5-3.5 feet tall", "30-100 pounds", "+2 Dex");

            // Assert
            assertEquals("Hobbit", result.getName());
            assertEquals("Small", result.getSize());
            assertFalse(result.isDarkvision());
        }
    }

    // ---- updateSpecies ----

    @Nested
    @DisplayName("updateSpecies Tests")
    class UpdateSpeciesTests {

        @Test
        @DisplayName("should update an existing species")
        void updateSpecies_shouldUpdateSpecies() {
            // Arrange
            LotrSpecies existing = new LotrSpecies();
            existing.setId(1L);
            existing.setName("Elf");
            when(speciesRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(speciesRepository.save(any(LotrSpecies.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrSpecies result = speciesService.updateSpecies(
                    1L, "Updated Elf", "Updated description", "Medium", 35, true,
                    "Ageless", "Tall", "Light", "+3 Dex");

            // Assert
            assertEquals("Updated Elf", result.getName());
            assertEquals(35, result.getSpeed());
            assertEquals("+3 Dex", result.getAbilityIncrease());
            verify(speciesRepository).save(result);
        }

        @Test
        @DisplayName("should throw when species not found")
        void updateSpecies_shouldThrowWhenNotFound() {
            // Arrange
            when(speciesRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    speciesService.updateSpecies(99L, "Name", "Desc", "Medium", 30, false, "age", "height", "weight", "ability"));
        }
    }

    // ---- deleteSpecies ----

    @Nested
    @DisplayName("deleteSpecies Tests")
    class DeleteSpeciesTests {

        @Test
        @DisplayName("should delete an existing species")
        void deleteSpecies_shouldDeleteSpecies() {
            // Arrange
            when(speciesRepository.existsById(1L)).thenReturn(true);

            // Act
            speciesService.deleteSpecies(1L);

            // Assert
            verify(speciesRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw when species not found")
        void deleteSpecies_shouldThrowWhenNotFound() {
            // Arrange
            when(speciesRepository.existsById(99L)).thenReturn(false);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> speciesService.deleteSpecies(99L));
        }
    }

    // ---- initializeSpeciesDatabase ----

    @Nested
    @DisplayName("initializeSpeciesDatabase Tests")
    class InitializeSpeciesDatabaseTests {

        @Test
        @DisplayName("should skip if database already has species")
        void initializeSpeciesDatabase_shouldSkipIfNotEmpty() {
            // Arrange
            LotrSpecies species = new LotrSpecies();
            species.setId(1L);
            when(speciesRepository.findAll()).thenReturn(List.of(species));

            // Act
            speciesService.initializeSpeciesDatabase();

            // Assert
            verify(speciesRepository, never()).save(any());
        }

        @Test
        @DisplayName("should create 7 LOTR species when empty")
        void initializeSpeciesDatabase_shouldCreateSpecies() {
            // Arrange
            when(speciesRepository.findAll()).thenReturn(List.of());
            when(speciesRepository.save(any(LotrSpecies.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            speciesService.initializeSpeciesDatabase();

            // Assert
            // 7 species: Elf, Man, Dwarf, Hobbit, Ent, Maia, Orc
            verify(speciesRepository, times(7)).save(any(LotrSpecies.class));
        }
    }
}
