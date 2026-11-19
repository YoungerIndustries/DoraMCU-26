package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCombat;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrCombatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("LotrCombatService Tests")
class LotrCombatServiceTest {

    private LotrCombatRepository combatRepository;
    private LotrCombatService combatService;

    @BeforeEach
    void setUp() {
        combatRepository = mock(LotrCombatRepository.class);
        combatService = new LotrCombatService(combatRepository);
    }

    // ---- createCombat ----

    @Nested
    @DisplayName("createCombat Tests")
    class CreateCombatTests {

        @Test
        @DisplayName("should create a combat encounter")
        void createCombat_shouldCreateCombat() {
            // Arrange
            when(combatRepository.save(any(LotrCombat.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCombat combat = combatService.createCombat("Orc Ambush", "Helm's Deep");

            // Assert
            assertNotNull(combat);
            assertEquals("Orc Ambush", combat.getEncounterName());
            assertEquals("Helm's Deep", combat.getLocation());
            verify(combatRepository).save(combat);
        }
    }

    // ---- findById ----

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("should return combat by id")
        void findById_shouldReturnCombat() {
            // Arrange
            LotrCombat combat = new LotrCombat("Orc Ambush");
            combat.setId(1L);
            when(combatRepository.findById(1L)).thenReturn(Optional.of(combat));

            // Act
            LotrCombat result = combatService.findById(1L);

            // Assert
            assertEquals("Orc Ambush", result.getEncounterName());
        }

        @Test
        @DisplayName("should throw when combat not found")
        void findById_shouldThrowWhenNotFound() {
            // Arrange
            when(combatRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> combatService.findById(999L));
            assertTrue(ex.getMessage().contains("Combat not found"));
        }
    }

    // ---- findAll ----

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("should return all combats")
        void findAll_shouldReturnAll() {
            // Arrange
            List<LotrCombat> combats = List.of(
                    new LotrCombat("Orc Ambush"),
                    new LotrCombat("Balrog Fight")
            );
            when(combatRepository.findAll()).thenReturn(combats);

            // Act
            List<LotrCombat> result = combatService.findAll();

            // Assert
            assertEquals(2, result.size());
        }
    }

    // ---- findActive ----

    @Nested
    @DisplayName("findActive Tests")
    class FindActiveTests {

        @Test
        @DisplayName("should return active combats")
        void findActive_shouldReturnActive() {
            // Arrange
            LotrCombat combat = new LotrCombat("Active Combat");
            combat.setStatus("ACTIVE");
            when(combatRepository.findByStatus("ACTIVE")).thenReturn(List.of(combat));

            // Act
            List<LotrCombat> result = combatService.findActive();

            // Assert
            assertEquals(1, result.size());
        }
    }

    // ---- endCombat ----

    @Nested
    @DisplayName("endCombat Tests")
    class EndCombatTests {

        @Test
        @DisplayName("should end a combat")
        void endCombat_shouldEndCombat() {
            // Arrange
            LotrCombat combat = new LotrCombat("Orc Ambush");
            combat.setId(1L);
            combat.setStatus("ACTIVE");
            when(combatRepository.findById(1L)).thenReturn(Optional.of(combat));
            when(combatRepository.save(any(LotrCombat.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            combatService.endCombat(1L);

            // Assert
            assertEquals("ENDED", combat.getStatus());
            verify(combatRepository).save(combat);
        }
    }

    // ---- nextRound ----

    @Nested
    @DisplayName("nextRound Tests")
    class NextRoundTests {

        @Test
        @DisplayName("should increment the current round")
        void nextRound_shouldIncrementRound() {
            // Arrange
            LotrCombat combat = new LotrCombat("Orc Ambush");
            combat.setId(1L);
            combat.setCurrentRound(1);
            when(combatRepository.findById(1L)).thenReturn(Optional.of(combat));
            when(combatRepository.save(any(LotrCombat.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            combatService.nextRound(1L);

            // Assert
            assertEquals(2, combat.getCurrentRound());
            verify(combatRepository).save(combat);
        }
    }
}
