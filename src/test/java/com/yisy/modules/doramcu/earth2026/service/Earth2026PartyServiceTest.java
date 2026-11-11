package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Party;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CharacterRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026PartyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Earth2026PartyService Tests")
class Earth2026PartyServiceTest {

    private Earth2026PartyRepository partyRepository;
    private Earth2026CharacterRepository characterRepository;
    private Earth2026PartyService partyService;

    @BeforeEach
    void setUp() {
        partyRepository = mock(Earth2026PartyRepository.class);
        characterRepository = mock(Earth2026CharacterRepository.class);
        partyService = new Earth2026PartyService(partyRepository, characterRepository);
    }

    // ---- createParty ----

    @Nested
    @DisplayName("createParty Tests")
    class CreatePartyTests {

        @Test
        @DisplayName("should create party with name, location, and leader")
        void createParty_shouldCreateWithAllFields() {
            // Arrange
            Earth2026Character leader = new Earth2026Character("John Doe", 30, "Detective");
            leader.setId(1L);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(leader));
            when(partyRepository.save(any(Earth2026Party.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Earth2026Party party = partyService.createParty("San Diego Squad", 1L, 1L);

            // Assert
            assertNotNull(party);
            assertEquals("San Diego Squad", party.getName());
            assertEquals(1L, party.getLocationId());
            assertEquals(1L, party.getLeaderId());
            assertEquals(6, party.getMaxMembers());
            assertEquals("active", party.getStatus());
            assertTrue(party.isRecruiting());
            assertNotNull(party.getCreatedAt());
            assertNotNull(party.getUpdatedAt());
            verify(partyRepository).save(party);
        }

        @Test
        @DisplayName("should throw when leader character not found")
        void createParty_shouldThrowWhenLeaderNotFound() {
            // Arrange
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> partyService.createParty("Party", 1L, 999L));
            assertTrue(ex.getMessage().contains("Leader character not found"));
            assertTrue(ex.getMessage().contains("999"));
            verify(partyRepository, never()).save(any());
        }

        @Test
        @DisplayName("should set default max members to 6")
        void createParty_shouldSetDefaultMaxMembers() {
            // Arrange
            Earth2026Character leader = new Earth2026Character("Leader", 25, "Agent");
            leader.setId(1L);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(leader));
            when(partyRepository.save(any(Earth2026Party.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Earth2026Party party = partyService.createParty("Default Party", 1L, 1L);

            // Assert
            assertEquals(6, party.getMaxMembers());
        }

        @Test
        @DisplayName("should set default status to active")
        void createParty_shouldSetDefaultStatus() {
            // Arrange
            Earth2026Character leader = new Earth2026Character("Leader", 25, "Agent");
            leader.setId(1L);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(leader));
            when(partyRepository.save(any(Earth2026Party.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Earth2026Party party = partyService.createParty("Active Party", 1L, 1L);

            // Assert
            assertEquals("active", party.getStatus());
        }
    }

    // ---- getParty ----

    @Nested
    @DisplayName("getParty Tests")
    class GetPartyTests {

        @Test
        @DisplayName("should return party when found")
        void getParty_shouldReturnWhenFound() {
            // Arrange
            Earth2026Party party = new Earth2026Party("Test Party", 1L, 1L);
            party.setId(1L);
            when(partyRepository.findById(1L)).thenReturn(Optional.of(party));

            // Act
            Optional<Earth2026Party> result = partyService.getParty(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("Test Party", result.get().getName());
        }

        @Test
        @DisplayName("should return empty when not found")
        void getParty_shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(partyRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Earth2026Party> result = partyService.getParty(999L);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- getPartiesAtLocation ----

    @Nested
    @DisplayName("getPartiesAtLocation Tests")
    class GetPartiesAtLocationTests {

        @Test
        @DisplayName("should return parties at a specific location")
        void getPartiesAtLocation_shouldReturnParties() {
            // Arrange
            Earth2026Party party1 = new Earth2026Party("Party 1", 1L, 1L);
            party1.setId(1L);
            Earth2026Party party2 = new Earth2026Party("Party 2", 1L, 2L);
            party2.setId(2L);
            when(partyRepository.findByLocationId(1L)).thenReturn(List.of(party1, party2));

            // Act
            List<Earth2026Party> result = partyService.getPartiesAtLocation(1L);

            // Assert
            assertEquals(2, result.size());
            assertEquals("Party 1", result.get(0).getName());
            assertEquals("Party 2", result.get(1).getName());
        }

        @Test
        @DisplayName("should return empty list when no parties at location")
        void getPartiesAtLocation_shouldReturnEmptyList() {
            // Arrange
            when(partyRepository.findByLocationId(999L)).thenReturn(List.of());

            // Act
            List<Earth2026Party> result = partyService.getPartiesAtLocation(999L);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- getRecruitingParties ----

    @Nested
    @DisplayName("getRecruitingParties Tests")
    class GetRecruitingPartiesTests {

        @Test
        @DisplayName("should return recruiting parties")
        void getRecruitingParties_shouldReturnRecruitingParties() {
            // Arrange
            Earth2026Party party = new Earth2026Party("Recruiting", 1L, 1L);
            party.setId(1L);
            party.setRecruiting(true);
            when(partyRepository.findByIsRecruitingTrue()).thenReturn(List.of(party));

            // Act
            List<Earth2026Party> result = partyService.getRecruitingParties();

            // Assert
            assertEquals(1, result.size());
            assertTrue(result.get(0).isRecruiting());
        }

        @Test
        @DisplayName("should return empty list when no recruiting parties")
        void getRecruitingParties_shouldReturnEmptyList() {
            // Arrange
            when(partyRepository.findByIsRecruitingTrue()).thenReturn(List.of());

            // Act
            List<Earth2026Party> result = partyService.getRecruitingParties();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- getActiveParties ----

    @Nested
    @DisplayName("getActiveParties Tests")
    class GetActivePartiesTests {

        @Test
        @DisplayName("should return active parties")
        void getActiveParties_shouldReturnActiveParties() {
            // Arrange
            Earth2026Party party = new Earth2026Party("Active", 1L, 1L);
            party.setId(1L);
            party.setStatus("active");
            when(partyRepository.findByStatus("active")).thenReturn(List.of(party));

            // Act
            List<Earth2026Party> result = partyService.getActiveParties();

            // Assert
            assertEquals(1, result.size());
            assertEquals("active", result.get(0).getStatus());
        }

        @Test
        @DisplayName("should query by status active")
        void getActiveParties_shouldQueryByStatusActive() {
            // Arrange
            when(partyRepository.findByStatus("active")).thenReturn(List.of());

            // Act
            partyService.getActiveParties();

            // Assert
            verify(partyRepository).findByStatus("active");
        }
    }

    // ---- disbandParty ----

    @Nested
    @DisplayName("disbandParty Tests")
    class DisbandPartyTests {

        @Test
        @DisplayName("should disband party and set status to disbanded")
        void disbandParty_shouldDisbandParty() {
            // Arrange
            Earth2026Party party = new Earth2026Party("To Disband", 1L, 1L);
            party.setId(1L);
            party.setStatus("active");
            when(partyRepository.findById(1L)).thenReturn(Optional.of(party));
            when(partyRepository.save(party)).thenReturn(party);

            // Act
            Optional<Earth2026Party> result = partyService.disbandParty(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("disbanded", result.get().getStatus());
            assertNotNull(result.get().getUpdatedAt());
            verify(partyRepository).save(party);
        }

        @Test
        @DisplayName("should return empty when party not found")
        void disbandParty_shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(partyRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Earth2026Party> result = partyService.disbandParty(999L);

            // Assert
            assertTrue(result.isEmpty());
            verify(partyRepository, never()).save(any());
        }
    }

    // ---- setRecruiting ----

    @Nested
    @DisplayName("setRecruiting Tests")
    class SetRecruitingTests {

        @Test
        @DisplayName("should set recruiting to true")
        void setRecruiting_shouldSetToTrue() {
            // Arrange
            Earth2026Party party = new Earth2026Party("Party", 1L, 1L);
            party.setId(1L);
            party.setRecruiting(false);
            when(partyRepository.findById(1L)).thenReturn(Optional.of(party));
            when(partyRepository.save(party)).thenReturn(party);

            // Act
            Optional<Earth2026Party> result = partyService.setRecruiting(1L, true);

            // Assert
            assertTrue(result.isPresent());
            assertTrue(result.get().isRecruiting());
            verify(partyRepository).save(party);
        }

        @Test
        @DisplayName("should set recruiting to false")
        void setRecruiting_shouldSetToFalse() {
            // Arrange
            Earth2026Party party = new Earth2026Party("Party", 1L, 1L);
            party.setId(1L);
            party.setRecruiting(true);
            when(partyRepository.findById(1L)).thenReturn(Optional.of(party));
            when(partyRepository.save(party)).thenReturn(party);

            // Act
            Optional<Earth2026Party> result = partyService.setRecruiting(1L, false);

            // Assert
            assertTrue(result.isPresent());
            assertFalse(result.get().isRecruiting());
        }

        @Test
        @DisplayName("should return empty when party not found")
        void setRecruiting_shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(partyRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Earth2026Party> result = partyService.setRecruiting(999L, true);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- setScenario ----

    @Nested
    @DisplayName("setScenario Tests")
    class SetScenarioTests {

        @Test
        @DisplayName("should set scenario and change status to on_scenario")
        void setScenario_shouldSetScenarioAndStatus() {
            // Arrange
            Earth2026Party party = new Earth2026Party("Party", 1L, 1L);
            party.setId(1L);
            party.setStatus("active");
            when(partyRepository.findById(1L)).thenReturn(Optional.of(party));
            when(partyRepository.save(party)).thenReturn(party);

            // Act
            Optional<Earth2026Party> result = partyService.setScenario(1L, 42L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(42L, result.get().getScenarioId());
            assertEquals("on_scenario", result.get().getStatus());
            verify(partyRepository).save(party);
        }

        @Test
        @DisplayName("should return empty when party not found")
        void setScenario_shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(partyRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Earth2026Party> result = partyService.setScenario(999L, 1L);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- moveParty ----

    @Nested
    @DisplayName("moveParty Tests")
    class MovePartyTests {

        @Test
        @DisplayName("should move party to new location")
        void moveParty_shouldMoveToNewLocation() {
            // Arrange
            Earth2026Party party = new Earth2026Party("Party", 1L, 1L);
            party.setId(1L);
            when(partyRepository.findById(1L)).thenReturn(Optional.of(party));
            when(partyRepository.save(party)).thenReturn(party);

            // Act
            Optional<Earth2026Party> result = partyService.moveParty(1L, 5L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(5L, result.get().getLocationId());
            assertNotNull(result.get().getUpdatedAt());
            verify(partyRepository).save(party);
        }

        @Test
        @DisplayName("should return empty when party not found")
        void moveParty_shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(partyRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Earth2026Party> result = partyService.moveParty(999L, 1L);

            // Assert
            assertTrue(result.isEmpty());
        }
    }
}
