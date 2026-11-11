package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.dto.HeroicInspirationRerollResponse;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eHeroicInspiration;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eHeroicInspirationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Dnd5eHeroicInspirationService Tests")
class Dnd5eHeroicInspirationServiceTest {

    private Dnd5eHeroicInspirationRepository inspirationRepository;
    private Dnd5eCharacterRepository characterRepository;
    private Dnd5eHeroicInspirationService service;

    private Dnd5eCharacter humanCharacter;
    private Dnd5eCharacter elfCharacter;
    private Dnd5eHeroicInspiration inspiration;

    @BeforeEach
    void setUp() {
        inspirationRepository = mock(Dnd5eHeroicInspirationRepository.class);
        characterRepository = mock(Dnd5eCharacterRepository.class);
        service = new Dnd5eHeroicInspirationService(inspirationRepository, characterRepository);

        // Human character — gets Heroic Inspiration after long rest
        humanCharacter = new Dnd5eCharacter();
        humanCharacter.setId(1L);
        humanCharacter.setName("Human Hero");
        humanCharacter.setRace("Human");
        humanCharacter.setCharacterClass("Fighter");
        humanCharacter.setLevel(1);
        humanCharacter.setStrScore(16);
        humanCharacter.setDexScore(12);
        humanCharacter.setConScore(14);
        humanCharacter.setIntScore(10);
        humanCharacter.setWisScore(10);
        humanCharacter.setChaScore(12);
        humanCharacter.setProficiencyBonus(2);
        humanCharacter.setHitPoints(10);
        humanCharacter.setMaxHitPoints(10);
        humanCharacter.setEquipment("Longsword, Shield");

        // Elf character — no Heroic Inspiration by default
        elfCharacter = new Dnd5eCharacter();
        elfCharacter.setId(2L);
        elfCharacter.setName("Elf Mage");
        elfCharacter.setRace("Elf");
        elfCharacter.setCharacterClass("Wizard");
        elfCharacter.setLevel(3);
        elfCharacter.setStrScore(8);
        elfCharacter.setDexScore(14);
        elfCharacter.setConScore(12);
        elfCharacter.setIntScore(16);
        elfCharacter.setWisScore(12);
        elfCharacter.setChaScore(10);
        elfCharacter.setProficiencyBonus(2);
        elfCharacter.setHitPoints(18);
        elfCharacter.setMaxHitPoints(18);
        elfCharacter.setEquipment("Wizard's Spellbook, Calligraphy Set");

        // Pre-existing inspiration record
        inspiration = new Dnd5eHeroicInspiration();
        inspiration.setId(1L);
        inspiration.setCharacterId(1L);
        inspiration.setCharacterName("Human Hero");
        inspiration.setPointsAvailable(1);
        inspiration.setMaxPoints(1);
        inspiration.setSource("Human");
        inspiration.setUsed(false);
    }

    // ---- getOrCreateInspiration ----

    @Nested
    @DisplayName("getOrCreateInspiration Tests")
    class GetOrCreateInspirationTests {

        @Test
        @DisplayName("should return existing inspiration record")
        void getOrCreate_shouldReturnExisting() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(humanCharacter));
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));

            // Act
            Dnd5eHeroicInspiration result = service.getOrCreateInspiration(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getCharacterId());
            assertEquals(1, result.getPointsAvailable());
            assertEquals("Human", result.getSource());
        }

        @Test
        @DisplayName("should auto-create inspiration for Human character")
        void getOrCreate_shouldAutoCreateForHuman() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(humanCharacter));
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.empty());
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> {
                Dnd5eHeroicInspiration saved = inv.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            // Act
            Dnd5eHeroicInspiration result = service.getOrCreateInspiration(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getPointsAvailable());
            assertEquals("Human", result.getSource());
            verify(inspirationRepository).save(any(Dnd5eHeroicInspiration.class));
        }

        @Test
        @DisplayName("should create empty record for non-Human character")
        void getOrCreate_shouldCreateEmptyForNonHuman() {
            // Arrange
            when(characterRepository.findById(2L)).thenReturn(Optional.of(elfCharacter));
            when(inspirationRepository.findByCharacterId(2L)).thenReturn(Optional.empty());
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> {
                Dnd5eHeroicInspiration saved = inv.getArgument(0);
                saved.setId(2L);
                return saved;
            });

            // Act
            Dnd5eHeroicInspiration result = service.getOrCreateInspiration(2L);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getPointsAvailable());
            assertEquals("None", result.getSource());
        }

        @Test
        @DisplayName("should throw when character not found")
        void getOrCreate_shouldThrowWhenCharacterNotFound() {
            // Arrange
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> service.getOrCreateInspiration(999L));
        }
    }

    // ---- awardInspiration ----

    @Nested
    @DisplayName("awardInspiration Tests")
    class AwardInspirationTests {

        @Test
        @DisplayName("should award inspiration to existing record")
        void award_shouldAwardToExisting() {
            // Arrange
            inspiration.setPointsAvailable(0);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(humanCharacter));
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eHeroicInspiration result = service.awardInspiration(1L, "Human", 1);

            // Assert
            assertEquals(1, result.getPointsAvailable());
            assertEquals("Human", result.getSource());
            assertFalse(result.isUsed());
            assertNotNull(result.getLastAwardedAt());
        }

        @Test
        @DisplayName("should cap points at max")
        void award_shouldCapAtMax() {
            // Arrange
            inspiration.setPointsAvailable(0);
            inspiration.setMaxPoints(1);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(humanCharacter));
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act — award 1 point (same as max), so max stays 1
            Dnd5eHeroicInspiration result = service.awardInspiration(1L, "DM Award", 1);

            // Assert
            assertEquals(1, result.getPointsAvailable());
            assertEquals(1, result.getMaxPoints());
        }

        @Test
        @DisplayName("should update max points if source provides more")
        void award_shouldUpdateMaxPoints() {
            // Arrange
            inspiration.setPointsAvailable(0);
            inspiration.setMaxPoints(1);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(humanCharacter));
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eHeroicInspiration result = service.awardInspiration(1L, "Musician feat", 2);

            // Assert
            assertEquals(2, result.getMaxPoints());
            assertEquals(2, result.getPointsAvailable());
        }

        @Test
        @DisplayName("should create new record if none exists")
        void award_shouldCreateNewRecord() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(humanCharacter));
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.empty());
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> {
                Dnd5eHeroicInspiration saved = inv.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            // Act
            Dnd5eHeroicInspiration result = service.awardInspiration(1L, "DM Award", 1);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getPointsAvailable());
            assertEquals("DM Award", result.getSource());
        }

        @Test
        @DisplayName("should throw when character not found")
        void award_shouldThrowWhenCharacterNotFound() {
            // Arrange
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> service.awardInspiration(999L, "DM", 1));
        }
    }

    // ---- rerollWithInspiration ----

    @Nested
    @DisplayName("rerollWithInspiration Tests")
    class RerollWithInspirationTests {

        @Test
        @DisplayName("should consume inspiration and return response")
        void reroll_shouldConsumeAndReturnResponse() {
            // Arrange
            inspiration.setPointsAvailable(1);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(humanCharacter));
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            HeroicInspirationRerollResponse response = service.rerollWithInspiration(
                    1L, "Attack roll vs dragon", 5, 20);

            // Assert
            assertNotNull(response);
            assertEquals(1L, response.getCharacterId());
            assertEquals("Human Hero", response.getCharacterName());
            assertEquals("Attack roll vs dragon", response.getDescription());
            assertEquals(5, response.getOriginalRoll());
            assertTrue(response.getRerollValue() >= 1 && response.getRerollValue() <= 20);
            assertTrue(response.getFinalValue() >= 5); // at least the original
            assertEquals(0, response.getPointsRemaining());
            assertEquals("Human", response.getSource());
        }

        @Test
        @DisplayName("should keep higher of original and reroll")
        void reroll_shouldKeepHigherValue() {
            // Arrange
            inspiration.setPointsAvailable(1);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(humanCharacter));
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act — original roll is 20 (max), reroll can't improve
            HeroicInspirationRerollResponse response = service.rerollWithInspiration(
                    1L, "Saving throw", 20, 20);

            // Assert
            assertEquals(20, response.getOriginalRoll());
            assertEquals(20, response.getFinalValue());
            assertFalse(response.isImproved());
        }

        @Test
        @DisplayName("should mark inspiration as used")
        void reroll_shouldMarkAsUsed() {
            // Arrange
            inspiration.setPointsAvailable(1);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(humanCharacter));
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            service.rerollWithInspiration(1L, "Test", 10, 20);

            // Assert
            assertTrue(inspiration.isUsed());
            assertEquals(0, inspiration.getPointsAvailable());
            assertEquals("Test", inspiration.getLastRerollDescription());
            assertEquals(10, inspiration.getLastOriginalRoll().intValue());
        }

        @Test
        @DisplayName("should support different dice sizes")
        void reroll_shouldSupportDifferentDiceSizes() {
            // Arrange
            inspiration.setPointsAvailable(1);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(humanCharacter));
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act — d6 damage roll
            HeroicInspirationRerollResponse response = service.rerollWithInspiration(
                    1L, "Damage roll", 2, 6);

            // Assert
            assertTrue(response.getRerollValue() >= 1 && response.getRerollValue() <= 6);
        }

        @Test
        @DisplayName("should throw when no points available")
        void reroll_shouldThrowWhenNoPoints() {
            // Arrange
            inspiration.setPointsAvailable(0);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(humanCharacter));
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> service.rerollWithInspiration(1L, "Test", 10, 20));
        }

        @Test
        @DisplayName("should throw when no inspiration record exists")
        void reroll_shouldThrowWhenNoRecord() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(humanCharacter));
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> service.rerollWithInspiration(1L, "Test", 10, 20));
        }

        @Test
        @DisplayName("should throw when character not found")
        void reroll_shouldThrowWhenCharacterNotFound() {
            // Arrange
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> service.rerollWithInspiration(999L, "Test", 10, 20));
        }
    }

    // ---- hasAvailableInspiration ----

    @Nested
    @DisplayName("hasAvailableInspiration Tests")
    class HasAvailableInspirationTests {

        @Test
        @DisplayName("should return true when points available")
        void hasAvailable_shouldReturnTrue() {
            // Arrange
            inspiration.setPointsAvailable(1);
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));

            // Act
            boolean result = service.hasAvailableInspiration(1L);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("should return false when no points")
        void hasAvailable_shouldReturnFalse() {
            // Arrange
            inspiration.setPointsAvailable(0);
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));

            // Act
            boolean result = service.hasAvailableInspiration(1L);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("should return false when no record")
        void hasAvailable_shouldReturnFalseWhenNoRecord() {
            // Arrange
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.empty());

            // Act
            boolean result = service.hasAvailableInspiration(1L);

            // Assert
            assertFalse(result);
        }
    }

    // ---- getInspiration ----

    @Nested
    @DisplayName("getInspiration Tests")
    class GetInspirationTests {

        @Test
        @DisplayName("should return inspiration record")
        void get_shouldReturnRecord() {
            // Arrange
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));

            // Act
            Dnd5eHeroicInspiration result = service.getInspiration(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getCharacterId());
        }

        @Test
        @DisplayName("should throw when no record")
        void get_shouldThrowWhenNotFound() {
            // Arrange
            when(inspirationRepository.findByCharacterId(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> service.getInspiration(999L));
        }
    }

    // ---- listAllInspiration ----

    @Nested
    @DisplayName("listAllInspiration Tests")
    class ListAllInspirationTests {

        @Test
        @DisplayName("should return all records")
        void listAll_shouldReturnAll() {
            // Arrange
            when(inspirationRepository.findAll()).thenReturn(List.of(inspiration));

            // Act
            List<Dnd5eHeroicInspiration> result = service.listAllInspiration();

            // Assert
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("should return empty list when no records")
        void listAll_shouldReturnEmpty() {
            // Arrange
            when(inspirationRepository.findAll()).thenReturn(List.of());

            // Act
            List<Dnd5eHeroicInspiration> result = service.listAllInspiration();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- listAvailableInspiration ----

    @Nested
    @DisplayName("listAvailableInspiration Tests")
    class ListAvailableInspirationTests {

        @Test
        @DisplayName("should return unused inspiration records")
        void listAvailable_shouldReturnUnused() {
            // Arrange
            inspiration.setUsed(false);
            when(inspirationRepository.findByIsUsedFalse()).thenReturn(List.of(inspiration));

            // Act
            List<Dnd5eHeroicInspiration> result = service.listAvailableInspiration();

            // Assert
            assertEquals(1, result.size());
            assertFalse(result.get(0).isUsed());
        }
    }

    // ---- listInspirationBySource ----

    @Nested
    @DisplayName("listInspirationBySource Tests")
    class ListInspirationBySourceTests {

        @Test
        @DisplayName("should return records filtered by source")
        void listBySource_shouldFilterBySource() {
            // Arrange
            when(inspirationRepository.findBySource("Human")).thenReturn(List.of(inspiration));

            // Act
            List<Dnd5eHeroicInspiration> result = service.listInspirationBySource("Human");

            // Assert
            assertEquals(1, result.size());
            assertEquals("Human", result.get(0).getSource());
        }
    }

    // ---- resetInspiration ----

    @Nested
    @DisplayName("resetInspiration Tests")
    class ResetInspirationTests {

        @Test
        @DisplayName("should reset Human character to 1 point")
        void reset_shouldResetHuman() {
            // Arrange
            inspiration.setPointsAvailable(0);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(humanCharacter));
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eHeroicInspiration result = service.resetInspiration(1L);

            // Assert
            assertEquals(1, result.getPointsAvailable());
            assertEquals("Human", result.getSource());
            assertFalse(result.isUsed());
        }

        @Test
        @DisplayName("should reset non-Human with Musician feat to 1 point")
        void reset_shouldResetMusician() {
            // Arrange
            elfCharacter.setEquipment("Musician, Wizard's Spellbook");
            Dnd5eHeroicInspiration elfInspiration = new Dnd5eHeroicInspiration();
            elfInspiration.setId(2L);
            elfInspiration.setCharacterId(2L);
            elfInspiration.setCharacterName("Elf Mage");
            elfInspiration.setPointsAvailable(0);
            elfInspiration.setMaxPoints(0);
            elfInspiration.setSource("None");

            when(characterRepository.findById(2L)).thenReturn(Optional.of(elfCharacter));
            when(inspirationRepository.findByCharacterId(2L)).thenReturn(Optional.of(elfInspiration));
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eHeroicInspiration result = service.resetInspiration(2L);

            // Assert
            assertEquals(1, result.getPointsAvailable());
            assertEquals("Musician feat", result.getSource());
        }

        @Test
        @DisplayName("should reset non-Human without Musician to 0 points")
        void reset_shouldResetNonHumanNoMusician() {
            // Arrange
            elfCharacter.setEquipment("Wizard's Spellbook");
            Dnd5eHeroicInspiration elfInspiration = new Dnd5eHeroicInspiration();
            elfInspiration.setId(2L);
            elfInspiration.setCharacterId(2L);
            elfInspiration.setCharacterName("Elf Mage");
            elfInspiration.setPointsAvailable(0);
            elfInspiration.setMaxPoints(0);
            elfInspiration.setSource("None");

            when(characterRepository.findById(2L)).thenReturn(Optional.of(elfCharacter));
            when(inspirationRepository.findByCharacterId(2L)).thenReturn(Optional.of(elfInspiration));
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eHeroicInspiration result = service.resetInspiration(2L);

            // Assert
            assertEquals(0, result.getPointsAvailable());
            assertEquals("None", result.getSource());
        }

        @Test
        @DisplayName("should throw when character not found")
        void reset_shouldThrowWhenCharacterNotFound() {
            // Arrange
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> service.resetInspiration(999L));
        }

        @Test
        @DisplayName("should throw when no inspiration record")
        void reset_shouldThrowWhenNoRecord() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(humanCharacter));
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> service.resetInspiration(1L));
        }
    }

    // ---- setInspirationPoints ----

    @Nested
    @DisplayName("setInspirationPoints Tests")
    class SetInspirationPointsTests {

        @Test
        @DisplayName("should set points")
        void setPoints_shouldSetPoints() {
            // Arrange
            inspiration.setMaxPoints(2);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(humanCharacter));
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eHeroicInspiration result = service.setInspirationPoints(1L, 2, "DM Adjustment");

            // Assert
            assertEquals(2, result.getPointsAvailable());
            assertEquals("DM Adjustment", result.getSource());
        }

        @Test
        @DisplayName("should clamp points to max")
        void setPoints_shouldClampToMax() {
            // Arrange
            inspiration.setMaxPoints(1);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(humanCharacter));
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eHeroicInspiration result = service.setInspirationPoints(1L, 10, "DM");

            // Assert
            assertEquals(1, result.getPointsAvailable());
        }

        @Test
        @DisplayName("should clamp negative points to 0")
        void setPoints_shouldClampNegative() {
            // Arrange
            inspiration.setMaxPoints(1);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(humanCharacter));
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eHeroicInspiration result = service.setInspirationPoints(1L, -5, "DM");

            // Assert
            assertEquals(0, result.getPointsAvailable());
        }

        @Test
        @DisplayName("should not update source when null")
        void setPoints_shouldNotUpdateSourceWhenNull() {
            // Arrange
            inspiration.setSource("Human");
            inspiration.setMaxPoints(2);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(humanCharacter));
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            service.setInspirationPoints(1L, 1, null);

            // Assert
            assertEquals("Human", inspiration.getSource());
        }
    }

    // ---- setMaxPoints ----

    @Nested
    @DisplayName("setMaxPoints Tests")
    class SetMaxPointsTests {

        @Test
        @DisplayName("should set max points")
        void setMax_shouldSetMax() {
            // Arrange
            inspiration.setPointsAvailable(1);
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eHeroicInspiration result = service.setMaxPoints(1L, 3);

            // Assert
            assertEquals(3, result.getMaxPoints());
            assertEquals(1, result.getPointsAvailable()); // unchanged, already below max
        }

        @Test
        @DisplayName("should reduce available points if below new max")
        void setMax_shouldReduceAvailableIfAboveNewMax() {
            // Arrange
            inspiration.setPointsAvailable(3);
            inspiration.setMaxPoints(5);
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eHeroicInspiration result = service.setMaxPoints(1L, 2);

            // Assert
            assertEquals(2, result.getMaxPoints());
            assertEquals(2, result.getPointsAvailable()); // clamped down
        }

        @Test
        @DisplayName("should clamp negative max to 0")
        void setMax_shouldClampNegative() {
            // Arrange
            when(inspirationRepository.findByCharacterId(1L)).thenReturn(Optional.of(inspiration));
            when(inspirationRepository.save(any(Dnd5eHeroicInspiration.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eHeroicInspiration result = service.setMaxPoints(1L, -5);

            // Assert
            assertEquals(0, result.getMaxPoints());
            assertEquals(0, result.getPointsAvailable());
        }

        @Test
        @DisplayName("should throw when no record")
        void setMax_shouldThrowWhenNotFound() {
            // Arrange
            when(inspirationRepository.findByCharacterId(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> service.setMaxPoints(999L, 1));
        }
    }
}
