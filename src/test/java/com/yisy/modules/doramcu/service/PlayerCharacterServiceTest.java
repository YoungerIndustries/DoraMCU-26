package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCrewMember;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCrewMemberRepository;
import com.yisy.modules.doramcu.dto.PlayerCharacterEntryDto;
import com.yisy.modules.doramcu.model.CharacterRole;
import com.yisy.modules.doramcu.model.CharacterType;
import com.yisy.modules.doramcu.model.PlayerCharacterRegistry;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.PlayerCharacterRegistryRepository;
import com.yisy.modules.doramcu.repository.UserRepository;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("PlayerCharacterService")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlayerCharacterServiceTest {

    @Mock
    private PlayerCharacterRegistryRepository registryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CaptainLogCrewMemberRepository crewMemberRepository;

    private PlayerCharacterService service;

    private User player;

    @BeforeEach
    void setUp() {
        service = new PlayerCharacterService(registryRepository, userRepository, crewMemberRepository);
        player = new User();
        player.setId(1L);
        player.setUsername("testuser");
        player.setPassword("password");
        player.setGalaxyName("Milky Way");
    }

    private TravellerCharacter createTravellerCharacter() {
        TravellerCharacter character = new TravellerCharacter(1, "Alex");
        return character;
    }

    private CaptainLogCrewMember createCrewMember() {
        CaptainLogCrewMember crew = new CaptainLogCrewMember();
        crew.setId(10L);
        crew.setName("Borg");
        crew.setRole("Science Officer");
        crew.setGalaxyName("Milky Way");
        return crew;
    }

    private PlayerCharacterRegistry createEntry() {
        PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
        entry.setId(100L);
        entry.setPlayer(player);
        entry.setCharacterType(CharacterType.TRAVELLER);
        entry.setCharacterId(1L);
        entry.setCharacterName("Alex");
        entry.setCharacterRole(CharacterRole.COMMANDER);
        entry.setGalaxyName("Milky Way");
        entry.setAiControlled(false);
        return entry;
    }

    // ---- registerTravellerCharacter ----

    @Nested
    @DisplayName("registerTravellerCharacter")
    class RegisterTravellerCharacter {

        @Test
        @DisplayName("throws when player is null")
        void throwsWhenPlayerNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.registerTravellerCharacter(null, createTravellerCharacter(), CharacterRole.COMMANDER));
        }

        @Test
        @DisplayName("throws when character is null")
        void throwsWhenCharacterNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.registerTravellerCharacter(player, null, CharacterRole.COMMANDER));
        }

        @Test
        @DisplayName("throws when role is null")
        void throwsWhenRoleNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.registerTravellerCharacter(player, createTravellerCharacter(), null));
        }

        @Test
        @DisplayName("registers character with int ID converted to Long")
        void registersWithIntId() {
            // TravellerCharacter uses int id (value 1), not null
            when(registryRepository.existsByPlayerAndCharacterTypeAndCharacterId(
                    player, CharacterType.TRAVELLER, 1L)).thenReturn(false);

            TravellerCharacter character = createTravellerCharacter();
            PlayerCharacterRegistry saved = createEntry();
            when(registryRepository.save(any(PlayerCharacterRegistry.class))).thenReturn(saved);

            PlayerCharacterRegistry result = service.registerTravellerCharacter(player, character, CharacterRole.COMMANDER);

            assertNotNull(result);
            assertEquals(1L, result.getCharacterId());
        }

        @Test
        @DisplayName("throws when already registered")
        void throwsWhenAlreadyRegistered() {
            when(registryRepository.existsByPlayerAndCharacterTypeAndCharacterId(
                    player, CharacterType.TRAVELLER, 1L)).thenReturn(true);

            TravellerCharacter character = createTravellerCharacter();
            assertThrows(IllegalStateException.class,
                    () -> service.registerTravellerCharacter(player, character, CharacterRole.COMMANDER));
        }

        @Test
        @DisplayName("registers new character")
        void registersNewCharacter() {
            when(registryRepository.existsByPlayerAndCharacterTypeAndCharacterId(
                    player, CharacterType.TRAVELLER, 1L)).thenReturn(false);

            TravellerCharacter character = createTravellerCharacter();
            PlayerCharacterRegistry saved = createEntry();
            when(registryRepository.save(any(PlayerCharacterRegistry.class))).thenReturn(saved);

            PlayerCharacterRegistry result = service.registerTravellerCharacter(player, character, CharacterRole.COMMANDER);

            assertNotNull(result);
            assertEquals(CharacterType.TRAVELLER, result.getCharacterType());
            assertEquals("Alex", result.getCharacterName());
            verify(registryRepository).save(any(PlayerCharacterRegistry.class));
        }
    }

    // ---- registerCrewMember ----

    @Nested
    @DisplayName("registerCrewMember")
    class RegisterCrewMember {

        @Test
        @DisplayName("throws when crew is null")
        void throwsWhenCrewNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.registerCrewMember(player, null, CharacterRole.CREW));
        }

        @Test
        @DisplayName("throws when crew has no ID")
        void throwsWhenCrewNoId() {
            CaptainLogCrewMember crew = new CaptainLogCrewMember("Borg", "Science", CaptainLogCharacter.Discipline.SCIENCE);
            assertThrows(IllegalArgumentException.class,
                    () -> service.registerCrewMember(player, crew, CharacterRole.CREW));
        }

        @Test
        @DisplayName("registers new crew member")
        void registersNewCrew() {
            CaptainLogCrewMember crew = createCrewMember();
            when(registryRepository.existsByPlayerAndCharacterTypeAndCharacterId(
                    player, CharacterType.AI_CREW, 10L)).thenReturn(false);

            PlayerCharacterRegistry saved = createEntry();
            saved.setCharacterType(CharacterType.AI_CREW);
            saved.setCharacterName("Borg");
            saved.setAiControlled(true);
            when(registryRepository.save(any(PlayerCharacterRegistry.class))).thenReturn(saved);

            PlayerCharacterRegistry result = service.registerCrewMember(player, crew, CharacterRole.CREW);

            assertNotNull(result);
            assertEquals(CharacterType.AI_CREW, result.getCharacterType());
            assertEquals("Borg", result.getCharacterName());
            assertTrue(result.isAiControlled());
        }
    }

    // ---- registerEncounteredAi ----

    @Nested
    @DisplayName("registerEncounteredAi")
    class RegisterEncounteredAi {

        @Test
        @DisplayName("throws when character name is blank")
        void throwsWhenNameBlank() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.registerEncounteredAi(player, "  ", "Milky Way"));
        }

        @Test
        @DisplayName("throws when galaxy is blank")
        void throwsWhenGalaxyBlank() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.registerEncounteredAi(player, "NPC", ""));
        }

        @Test
        @DisplayName("registers encountered AI")
        void registersEncountered() {
            PlayerCharacterRegistry saved = createEntry();
            saved.setCharacterRole(CharacterRole.ENCOUNTERED);
            when(registryRepository.save(any(PlayerCharacterRegistry.class))).thenReturn(saved);

            PlayerCharacterRegistry result = service.registerEncounteredAi(player, "Mysterious NPC", "Andromeda");

            assertNotNull(result);
            assertEquals(CharacterRole.ENCOUNTERED, result.getCharacterRole());
        }
    }

    // ---- getAllCharacters ----

    @Nested
    @DisplayName("getAllCharacters")
    class GetAllCharacters {

        @Test
        @DisplayName("throws when player is null")
        void throwsWhenPlayerNull() {
            assertThrows(IllegalArgumentException.class, () -> service.getAllCharacters(null));
        }

        @Test
        @DisplayName("returns empty list when no entries")
        void returnsEmptyWhenNone() {
            when(registryRepository.findByPlayer(player)).thenReturn(List.of());
            assertTrue(service.getAllCharacters(player).isEmpty());
        }

        @Test
        @DisplayName("returns all entries")
        void returnsAllEntries() {
            PlayerCharacterRegistry entry = createEntry();
            when(registryRepository.findByPlayer(player)).thenReturn(List.of(entry));

            List<PlayerCharacterEntryDto> result = service.getAllCharacters(player);

            assertEquals(1, result.size());
            assertEquals("Alex", result.get(0).getCharacterName());
        }
    }

    // ---- promoteToCrew ----

    @Nested
    @DisplayName("promoteToCrew")
    class PromoteToCrew {

        @Test
        @DisplayName("throws when entry not found")
        void throwsWhenNotFound() {
            when(registryRepository.findById(999L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> service.promoteToCrew(999L));
        }

        @Test
        @DisplayName("promotes to CREW role")
        void promotesToCrew() {
            PlayerCharacterRegistry entry = createEntry();
            entry.setCharacterRole(CharacterRole.OWNED);
            when(registryRepository.findById(100L)).thenReturn(Optional.of(entry));
            when(registryRepository.save(any())).thenReturn(entry);

            PlayerCharacterEntryDto result = service.promoteToCrew(100L);

            assertEquals(CharacterRole.CREW, result.getCharacterRole());
            assertTrue(result.isAiControlled());
        }
    }

    // ---- demoteFromCrew ----

    @Nested
    @DisplayName("demoteFromCrew")
    class DemoteFromCrew {

        @Test
        @DisplayName("demotes to OWNED role")
        void demotesToOwned() {
            PlayerCharacterRegistry entry = createEntry();
            entry.setCharacterRole(CharacterRole.CREW);
            when(registryRepository.findById(100L)).thenReturn(Optional.of(entry));
            when(registryRepository.save(any())).thenReturn(entry);

            PlayerCharacterEntryDto result = service.demoteFromCrew(100L);

            assertEquals(CharacterRole.OWNED, result.getCharacterRole());
            assertFalse(result.isAiControlled());
        }
    }

    // ---- setCommander ----

    @Nested
    @DisplayName("setCommander")
    class SetCommander {

        @Test
        @DisplayName("sets commander role")
        void setsCommander() {
            PlayerCharacterRegistry entry = createEntry();
            entry.setCharacterRole(CharacterRole.OWNED);
            when(registryRepository.findById(100L)).thenReturn(Optional.of(entry));
            when(registryRepository.findByPlayerAndCharacterRole(player, CharacterRole.COMMANDER))
                    .thenReturn(List.of());
            when(registryRepository.save(any())).thenReturn(entry);

            PlayerCharacterEntryDto result = service.setCommander(100L);

            assertEquals(CharacterRole.COMMANDER, result.getCharacterRole());
            assertFalse(result.isAiControlled());
        }
    }

    // ---- moveCrewToGalaxy ----

    @Nested
    @DisplayName("moveCrewToGalaxy")
    class MoveCrewToGalaxy {

        @Test
        @DisplayName("throws when player is null")
        void throwsWhenPlayerNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.moveCrewToGalaxy(null, "Milky Way", "Andromeda"));
        }

        @Test
        @DisplayName("returns 0 when no crew in galaxy")
        void returnsZeroWhenNoCrew() {
            when(registryRepository.findByPlayerAndCharacterRoleAndGalaxyName(
                    player, CharacterRole.CREW, "Milky Way")).thenReturn(List.of());

            int moved = service.moveCrewToGalaxy(player, "Milky Way", "Andromeda");
            assertEquals(0, moved);
        }

        @Test
        @DisplayName("moves crew to new galaxy")
        void movesCrew() {
            PlayerCharacterRegistry crewEntry = createEntry();
            crewEntry.setCharacterRole(CharacterRole.CREW);
            crewEntry.setCharacterType(CharacterType.AI_CREW);
            crewEntry.setGalaxyName("Milky Way");

            when(registryRepository.findByPlayerAndCharacterRoleAndGalaxyName(
                    player, CharacterRole.CREW, "Milky Way")).thenReturn(List.of(crewEntry));
            when(registryRepository.save(any())).thenReturn(crewEntry);

            CaptainLogCrewMember crew = createCrewMember();
            when(crewMemberRepository.findById(1L)).thenReturn(Optional.of(crew));

            int moved = service.moveCrewToGalaxy(player, "Milky Way", "Andromeda");

            assertEquals(1, moved);
            assertEquals("Andromeda", crewEntry.getGalaxyName());
            assertEquals("WORMHOLE_GATE", crewEntry.getHomeLocationType());
        }
    }

    // ---- getEntry ----

    @Nested
    @DisplayName("getEntry")
    class GetEntry {

        @Test
        @DisplayName("returns empty when not found")
        void returnsEmptyWhenNotFound() {
            when(registryRepository.findById(999L)).thenReturn(Optional.empty());
            assertTrue(service.getEntry(999L).isEmpty());
        }

        @Test
        @DisplayName("returns entry when found")
        void returnsEntryWhenFound() {
            PlayerCharacterRegistry entry = createEntry();
            when(registryRepository.findById(100L)).thenReturn(Optional.of(entry));

            Optional<PlayerCharacterEntryDto> result = service.getEntry(100L);

            assertTrue(result.isPresent());
            assertEquals("Alex", result.get().getCharacterName());
        }
    }
}
