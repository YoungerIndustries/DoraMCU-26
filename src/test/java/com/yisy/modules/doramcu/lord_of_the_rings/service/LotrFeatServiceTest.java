package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrFeat;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrFeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("LotrFeatService Tests")
class LotrFeatServiceTest {

    private LotrFeatRepository featRepository;
    private LotrFeatService featService;

    @BeforeEach
    void setUp() {
        featRepository = mock(LotrFeatRepository.class);
        featService = new LotrFeatService(featRepository);
    }

    // ---- findAll ----

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("should return all feats")
        void findAll_shouldReturnAllFeats() {
            // Arrange
            LotrFeat feat = new LotrFeat();
            feat.setId(1L);
            feat.setName("Ring Resistance");
            when(featRepository.findAll()).thenReturn(List.of(feat));

            // Act
            List<LotrFeat> result = featService.findAll();

            // Assert
            assertEquals(1, result.size());
            assertEquals("Ring Resistance", result.get(0).getName());
        }

        @Test
        @DisplayName("should return empty list when no feats")
        void findAll_shouldReturnEmptyList() {
            // Arrange
            when(featRepository.findAll()).thenReturn(List.of());

            // Act
            List<LotrFeat> result = featService.findAll();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- findActiveFeats ----

    @Nested
    @DisplayName("findActiveFeats Tests")
    class FindActiveFeatsTests {

        @Test
        @DisplayName("should return only active feats")
        void findActiveFeats_shouldReturnOnlyActive() {
            // Arrange
            LotrFeat active = new LotrFeat();
            active.setId(1L);
            active.setName("Ring Resistance");
            active.setActive(true);

            LotrFeat inactive = new LotrFeat();
            inactive.setId(2L);
            inactive.setName("Old Feat");
            inactive.setActive(false);

            when(featRepository.findAll()).thenReturn(List.of(active, inactive));

            // Act
            List<LotrFeat> result = featService.findActiveFeats();

            // Assert
            assertEquals(1, result.size());
            assertEquals("Ring Resistance", result.get(0).getName());
        }
    }

    // ---- findById ----

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("should return feat when found")
        void findById_shouldReturnFeat() {
            // Arrange
            LotrFeat feat = new LotrFeat();
            feat.setId(1L);
            feat.setName("Ent Speech");
            when(featRepository.findById(1L)).thenReturn(Optional.of(feat));

            // Act
            Optional<LotrFeat> result = featService.findById(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("Ent Speech", result.get().getName());
        }

        @Test
        @DisplayName("should return empty when not found")
        void findById_shouldReturnEmpty() {
            // Arrange
            when(featRepository.findById(99L)).thenReturn(Optional.empty());

            // Act
            Optional<LotrFeat> result = featService.findById(99L);

            // Assert
            assertFalse(result.isPresent());
        }
    }

    // ---- findByName ----

    @Nested
    @DisplayName("findByName Tests")
    class FindByNameTests {

        @Test
        @DisplayName("should find feat by partial name")
        void findByName_shouldFindPartialMatch() {
            // Arrange
            LotrFeat feat = new LotrFeat();
            feat.setId(1L);
            feat.setName("Elven Sight");
            when(featRepository.findAll()).thenReturn(List.of(feat));

            // Act
            List<LotrFeat> result = featService.findByName("elven");

            // Assert
            assertEquals(1, result.size());
            assertEquals("Elven Sight", result.get(0).getName());
        }

        @Test
        @DisplayName("should be case insensitive")
        void findByName_shouldBeCaseInsensitive() {
            // Arrange
            LotrFeat feat = new LotrFeat();
            feat.setId(1L);
            feat.setName("Dwarven Toughness");
            when(featRepository.findAll()).thenReturn(List.of(feat));

            // Act
            List<LotrFeat> result = featService.findByName("DWAR");

            // Assert
            assertEquals(1, result.size());
        }
    }

    // ---- createFeat ----

    @Nested
    @DisplayName("createFeat Tests")
    class CreateFeatTests {

        @Test
        @DisplayName("should create a new feat")
        void createFeat_shouldCreateFeat() {
            // Arrange
            when(featRepository.save(any(LotrFeat.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrFeat result = featService.createFeat(
                    "Ring Resistance",
                    "Advantage on saves against corruption and charm effects.",
                    "None", "None");

            // Assert
            assertNotNull(result);
            assertEquals("Ring Resistance", result.getName());
            assertEquals("None", result.getPrerequisite());
            assertEquals("None", result.getAbilityIncrease());
            assertTrue(result.isActive());
            verify(featRepository).save(result);
        }

        @Test
        @DisplayName("should create a feat with ability increase")
        void createFeat_shouldCreateFeatWithAbilityIncrease() {
            // Arrange
            when(featRepository.save(any(LotrFeat.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrFeat result = featService.createFeat(
                    "Master Archer",
                    "+2 to attack rolls with ranged weapons.",
                    "Ranger or Scout calling", "+1 Dex");

            // Assert
            assertEquals("Master Archer", result.getName());
            assertEquals("+1 Dex", result.getAbilityIncrease());
        }
    }

    // ---- updateFeat ----

    @Nested
    @DisplayName("updateFeat Tests")
    class UpdateFeatTests {

        @Test
        @DisplayName("should update an existing feat")
        void updateFeat_shouldUpdateFeat() {
            // Arrange
            LotrFeat existing = new LotrFeat();
            existing.setId(1L);
            existing.setName("Ring Resistance");
            when(featRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(featRepository.save(any(LotrFeat.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrFeat result = featService.updateFeat(
                    1L, "Updated Feat", "Updated description", "New prereq", "+2 Str");

            // Assert
            assertEquals("Updated Feat", result.getName());
            assertEquals("+2 Str", result.getAbilityIncrease());
            verify(featRepository).save(result);
        }

        @Test
        @DisplayName("should throw when feat not found")
        void updateFeat_shouldThrowWhenNotFound() {
            // Arrange
            when(featRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    featService.updateFeat(99L, "Name", "Desc", "prereq", "ability"));
        }
    }

    // ---- deleteFeat ----

    @Nested
    @DisplayName("deleteFeat Tests")
    class DeleteFeatTests {

        @Test
        @DisplayName("should delete an existing feat")
        void deleteFeat_shouldDeleteFeat() {
            // Arrange
            when(featRepository.existsById(1L)).thenReturn(true);

            // Act
            featService.deleteFeat(1L);

            // Assert
            verify(featRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw when feat not found")
        void deleteFeat_shouldThrowWhenNotFound() {
            // Arrange
            when(featRepository.existsById(99L)).thenReturn(false);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> featService.deleteFeat(99L));
        }
    }

    // ---- initializeFeatDatabase ----

    @Nested
    @DisplayName("initializeFeatDatabase Tests")
    class InitializeFeatDatabaseTests {

        @Test
        @DisplayName("should skip if database already has feats")
        void initializeFeatDatabase_shouldSkipIfNotEmpty() {
            // Arrange
            LotrFeat feat = new LotrFeat();
            feat.setId(1L);
            when(featRepository.findAll()).thenReturn(List.of(feat));

            // Act
            featService.initializeFeatDatabase();

            // Assert
            verify(featRepository, never()).save(any());
        }

        @Test
        @DisplayName("should create 12 LOTR feats when empty")
        void initializeFeatDatabase_shouldCreateFeats() {
            // Arrange
            when(featRepository.findAll()).thenReturn(List.of());
            when(featRepository.save(any(LotrFeat.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            featService.initializeFeatDatabase();

            // Assert
            // 12 feats: Ring Resistance, Ent Speech, Elven Sight, Dwarven Toughness,
            // Hobbit Luck, Mark of the Ring, Warden of the West, Master Archer,
            // Wizard's Ward, Shield of the Valar, Beast Speaker, Shadow Walker
            verify(featRepository, times(12)).save(any(LotrFeat.class));
        }
    }
}
