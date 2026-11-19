package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrEncounter;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrEncounterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("LotrEncounterService Tests")
class LotrEncounterServiceTest {

    private LotrEncounterRepository repository;
    private LotrEncounterService encounterService;

    @BeforeEach
    void setUp() {
        repository = mock(LotrEncounterRepository.class);
        encounterService = new LotrEncounterService(repository);
    }

    // ---- createEncounter ----

    @Nested
    @DisplayName("createEncounter Tests")
    class CreateEncounterTests {

        @Test
        @DisplayName("should create an encounter")
        void createEncounter_shouldCreateEncounter() {
            // Arrange
            when(repository.save(any(LotrEncounter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrEncounter encounter = encounterService.createEncounter(
                    "Orc Ambush", "Orcs attack the fellowship", 3, 4);

            // Assert
            assertNotNull(encounter);
            assertEquals("Orc Ambush", encounter.getName());
            assertEquals("Orcs attack the fellowship", encounter.getDescription());
            assertEquals(3, encounter.getPartyLevel());
            assertEquals(4, encounter.getPartySize());
            assertEquals("Medium", encounter.getDifficulty());
            assertTrue(encounter.isActive());
            verify(repository).save(encounter);
        }
    }

    // ---- findById ----

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("should return encounter by id")
        void findById_shouldReturnEncounter() {
            // Arrange
            LotrEncounter encounter = createEncounter(1L, "Orc Ambush");
            when(repository.findById(1L)).thenReturn(Optional.of(encounter));

            // Act
            LotrEncounter result = encounterService.findById(1L);

            // Assert
            assertEquals("Orc Ambush", result.getName());
        }

        @Test
        @DisplayName("should throw when encounter not found")
        void findById_shouldThrowWhenNotFound() {
            // Arrange
            when(repository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> encounterService.findById(999L));
            assertTrue(ex.getMessage().contains("Encounter not found"));
        }
    }

    // ---- findAll ----

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("should return all encounters")
        void findAll_shouldReturnAll() {
            // Arrange
            List<LotrEncounter> encounters = List.of(
                    createEncounter(1L, "Orc Ambush"),
                    createEncounter(2L, "Balrog Fight")
            );
            when(repository.findAll()).thenReturn(encounters);

            // Act
            List<LotrEncounter> result = encounterService.findAll();

            // Assert
            assertEquals(2, result.size());
        }
    }

    // ---- findActive ----

    @Nested
    @DisplayName("findActive Tests")
    class FindActiveTests {

        @Test
        @DisplayName("should return active encounters")
        void findActive_shouldReturnActive() {
            // Arrange
            LotrEncounter encounter = createEncounter(1L, "Active Encounter");
            encounter.setActive(true);
            when(repository.findByActiveTrue()).thenReturn(List.of(encounter));

            // Act
            List<LotrEncounter> result = encounterService.findActive();

            // Assert
            assertEquals(1, result.size());
        }
    }

    // ---- findByPartyLevel ----

    @Nested
    @DisplayName("findByPartyLevel Tests")
    class FindByPartyLevelTests {

        @Test
        @DisplayName("should return encounters by party level")
        void findByPartyLevel_shouldReturnEncounters() {
            // Arrange
            List<LotrEncounter> encounters = List.of(
                    createEncounter(1L, "Level 3 Encounter")
            );
            when(repository.findByPartyLevel(3)).thenReturn(encounters);

            // Act
            List<LotrEncounter> result = encounterService.findByPartyLevel(3);

            // Assert
            assertEquals(1, result.size());
        }
    }

    // ---- resolveEncounter ----

    @Nested
    @DisplayName("resolveEncounter Tests")
    class ResolveEncounterTests {

        @Test
        @DisplayName("should resolve an encounter")
        void resolveEncounter_shouldResolve() {
            // Arrange
            LotrEncounter encounter = createEncounter(1L, "Orc Ambush");
            encounter.setActive(true);
            when(repository.findById(1L)).thenReturn(Optional.of(encounter));
            when(repository.save(any(LotrEncounter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            encounterService.resolveEncounter(1L);

            // Assert
            assertFalse(encounter.isActive());
            verify(repository).save(encounter);
        }
    }

    // ---- Helper methods ----

    private LotrEncounter createEncounter(Long id, String name) {
        LotrEncounter encounter = new LotrEncounter();
        encounter.setId(id);
        encounter.setName(name);
        encounter.setDescription("Test encounter");
        encounter.setPartyLevel(3);
        encounter.setPartySize(4);
        encounter.setDifficulty("Medium");
        encounter.setActive(true);
        return encounter;
    }
}
