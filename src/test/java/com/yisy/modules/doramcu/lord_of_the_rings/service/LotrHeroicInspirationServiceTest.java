package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrHeroicInspiration;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrHeroicInspirationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("LotrHeroicInspirationService Tests")
class LotrHeroicInspirationServiceTest {

    private LotrHeroicInspirationRepository inspirationRepository;
    private LotrHeroicInspirationService inspirationService;

    @BeforeEach
    void setUp() {
        inspirationRepository = mock(LotrHeroicInspirationRepository.class);
        inspirationService = new LotrHeroicInspirationService(inspirationRepository);
    }

    // ---- findAll ----

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("should return all inspiration records")
        void findAll_shouldReturnAllRecords() {
            // Arrange
            LotrHeroicInspiration hi = new LotrHeroicInspiration();
            hi.setId(1L);
            hi.setCharacterId(100L);
            when(inspirationRepository.findAll()).thenReturn(List.of(hi));

            // Act
            List<LotrHeroicInspiration> result = inspirationService.findAll();

            // Assert
            assertEquals(1, result.size());
            assertEquals(100L, result.get(0).getCharacterId());
        }

        @Test
        @DisplayName("should return empty list when no records")
        void findAll_shouldReturnEmptyList() {
            // Arrange
            when(inspirationRepository.findAll()).thenReturn(List.of());

            // Act
            List<LotrHeroicInspiration> result = inspirationService.findAll();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- findByCharacterId ----

    @Nested
    @DisplayName("findByCharacterId Tests")
    class FindByCharacterIdTests {

        @Test
        @DisplayName("should return inspiration for character")
        void findByCharacterId_shouldReturnInspiration() {
            // Arrange
            LotrHeroicInspiration hi = new LotrHeroicInspiration();
            hi.setId(1L);
            hi.setCharacterId(100L);
            hi.setActive(true);
            when(inspirationRepository.findAll()).thenReturn(List.of(hi));

            // Act
            Optional<LotrHeroicInspiration> result = inspirationService.findByCharacterId(100L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(100L, result.get().getCharacterId());
        }

        @Test
        @DisplayName("should return empty when character not found")
        void findByCharacterId_shouldReturnEmpty() {
            // Arrange
            when(inspirationRepository.findAll()).thenReturn(List.of());

            // Act
            Optional<LotrHeroicInspiration> result = inspirationService.findByCharacterId(999L);

            // Assert
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should return empty for inactive inspiration")
        void findByCharacterId_shouldReturnEmptyForInactive() {
            // Arrange
            LotrHeroicInspiration hi = new LotrHeroicInspiration();
            hi.setId(1L);
            hi.setCharacterId(100L);
            hi.setActive(false);
            when(inspirationRepository.findAll()).thenReturn(List.of(hi));

            // Act
            Optional<LotrHeroicInspiration> result = inspirationService.findByCharacterId(100L);

            // Assert
            assertFalse(result.isPresent());
        }
    }

    // ---- findById ----

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("should return inspiration when found")
        void findById_shouldReturnInspiration() {
            // Arrange
            LotrHeroicInspiration hi = new LotrHeroicInspiration();
            hi.setId(1L);
            when(inspirationRepository.findById(1L)).thenReturn(Optional.of(hi));

            // Act
            Optional<LotrHeroicInspiration> result = inspirationService.findById(1L);

            // Assert
            assertTrue(result.isPresent());
        }

        @Test
        @DisplayName("should return empty when not found")
        void findById_shouldReturnEmpty() {
            // Arrange
            when(inspirationRepository.findById(99L)).thenReturn(Optional.empty());

            // Act
            Optional<LotrHeroicInspiration> result = inspirationService.findById(99L);

            // Assert
            assertFalse(result.isPresent());
        }
    }

    // ---- createOrUpdate ----

    @Nested
    @DisplayName("createOrUpdate Tests")
    class CreateOrUpdateTests {

        @Test
        @DisplayName("should create new inspiration for character")
        void createOrUpdate_shouldCreateNew() {
            // Arrange
            when(inspirationRepository.findAll()).thenReturn(List.of());
            when(inspirationRepository.save(any(LotrHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrHeroicInspiration result = inspirationService.createOrUpdate(100L, 5);

            // Assert
            assertNotNull(result);
            assertEquals(100L, result.getCharacterId());
            assertEquals(0, result.getPoints());
            assertEquals(5, result.getMaxPoints());
            assertTrue(result.isActive());
            verify(inspirationRepository).save(result);
        }

        @Test
        @DisplayName("should update existing inspiration")
        void createOrUpdate_shouldUpdateExisting() {
            // Arrange
            LotrHeroicInspiration existing = new LotrHeroicInspiration();
            existing.setId(1L);
            existing.setCharacterId(100L);
            existing.setPoints(2);
            existing.setMaxPoints(3);
            existing.setActive(true);
            when(inspirationRepository.findAll()).thenReturn(List.of(existing));
            when(inspirationRepository.save(any(LotrHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrHeroicInspiration result = inspirationService.createOrUpdate(100L, 5);

            // Assert
            assertEquals(5, result.getMaxPoints());
            assertEquals(2, result.getPoints()); // points preserved
        }
    }

    // ---- awardPoints ----

    @Nested
    @DisplayName("awardPoints Tests")
    class AwardPointsTests {

        @Test
        @DisplayName("should award points up to max")
        void awardPoints_shouldAwardPoints() {
            // Arrange
            LotrHeroicInspiration hi = new LotrHeroicInspiration();
            hi.setId(1L);
            hi.setCharacterId(100L);
            hi.setPoints(1);
            hi.setMaxPoints(5);
            hi.setActive(true);
            when(inspirationRepository.findAll()).thenReturn(List.of(hi));
            when(inspirationRepository.save(any(LotrHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            String result = inspirationService.awardPoints(100L, 2);

            // Assert
            assertTrue(result.contains("Awarded"));
            assertTrue(result.contains("3/5"));
        }

        @Test
        @DisplayName("should cap points at max")
        void awardPoints_shouldCapAtMax() {
            // Arrange
            LotrHeroicInspiration hi = new LotrHeroicInspiration();
            hi.setId(1L);
            hi.setCharacterId(100L);
            hi.setPoints(4);
            hi.setMaxPoints(5);
            hi.setActive(true);
            when(inspirationRepository.findAll()).thenReturn(List.of(hi));
            when(inspirationRepository.save(any(LotrHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            String result = inspirationService.awardPoints(100L, 3);

            // Assert
            // Only 1 point awarded (capped at 5)
            assertTrue(result.contains("1/5"));
        }
    }

    // ---- spendPoints ----

    @Nested
    @DisplayName("spendPoints Tests")
    class SpendPointsTests {

        @Test
        @DisplayName("should spend points")
        void spendPoints_shouldSpendPoints() {
            // Arrange
            LotrHeroicInspiration hi = new LotrHeroicInspiration();
            hi.setId(1L);
            hi.setCharacterId(100L);
            hi.setPoints(3);
            hi.setMaxPoints(5);
            hi.setActive(true);
            when(inspirationRepository.findAll()).thenReturn(List.of(hi));
            when(inspirationRepository.save(any(LotrHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            String result = inspirationService.spendPoints(100L, 1);

            // Assert
            assertTrue(result.contains("Spent"));
            assertTrue(result.contains("2/5"));
        }

        @Test
        @DisplayName("should return error when not enough points")
        void spendPoints_shouldReturnError() {
            // Arrange
            LotrHeroicInspiration hi = new LotrHeroicInspiration();
            hi.setId(1L);
            hi.setCharacterId(100L);
            hi.setPoints(1);
            hi.setMaxPoints(5);
            hi.setActive(true);
            when(inspirationRepository.findAll()).thenReturn(List.of(hi));

            // Act
            String result = inspirationService.spendPoints(100L, 2);

            // Assert
            assertTrue(result.contains("Not enough"));
        }
    }

    // ---- resetPoints ----

    @Nested
    @DisplayName("resetPoints Tests")
    class ResetPointsTests {

        @Test
        @DisplayName("should reset points to zero")
        void resetPoints_shouldResetPoints() {
            // Arrange
            LotrHeroicInspiration hi = new LotrHeroicInspiration();
            hi.setId(1L);
            hi.setCharacterId(100L);
            hi.setPoints(3);
            hi.setMaxPoints(5);
            hi.setActive(true);
            when(inspirationRepository.findAll()).thenReturn(List.of(hi));
            when(inspirationRepository.save(any(LotrHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            String result = inspirationService.resetPoints(100L);

            // Assert
            assertTrue(result.contains("Reset"));
            assertTrue(result.contains("3"));
        }
    }

    // ---- deleteInspiration ----

    @Nested
    @DisplayName("deleteInspiration Tests")
    class DeleteInspirationTests {

        @Test
        @DisplayName("should delete inspiration")
        void deleteInspiration_shouldDelete() {
            // Arrange
            when(inspirationRepository.existsById(1L)).thenReturn(true);

            // Act
            inspirationService.deleteInspiration(1L);

            // Assert
            verify(inspirationRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw when not found")
        void deleteInspiration_shouldThrowWhenNotFound() {
            // Arrange
            when(inspirationRepository.existsById(99L)).thenReturn(false);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> inspirationService.deleteInspiration(99L));
        }
    }

    // ---- deactivateInspiration ----

    @Nested
    @DisplayName("deactivateInspiration Tests")
    class DeactivateInspirationTests {

        @Test
        @DisplayName("should deactivate inspiration")
        void deactivateInspiration_shouldDeactivate() {
            // Arrange
            LotrHeroicInspiration hi = new LotrHeroicInspiration();
            hi.setId(1L);
            hi.setActive(true);
            when(inspirationRepository.findById(1L)).thenReturn(Optional.of(hi));
            when(inspirationRepository.save(any(LotrHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            inspirationService.deactivateInspiration(1L);

            // Assert
            assertFalse(hi.isActive());
            verify(inspirationRepository).save(hi);
        }

        @Test
        @DisplayName("should throw when not found")
        void deactivateInspiration_shouldThrowWhenNotFound() {
            // Arrange
            when(inspirationRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> inspirationService.deactivateInspiration(99L));
        }
    }
}
