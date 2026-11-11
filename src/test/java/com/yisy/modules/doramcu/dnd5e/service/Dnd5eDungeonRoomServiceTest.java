package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eDungeonRoom;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eDungeonRoomRepository;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eDungeonRoomService.TrapCheckResult;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eDungeonRoomService.TreasureResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Dnd5eDungeonRoomService Tests")
class Dnd5eDungeonRoomServiceTest {

    @Mock
    Dnd5eDungeonRoomRepository roomRepository;

    Dnd5eDungeonRoomService service;

    @BeforeEach
    void setUp() {
        // Stub count() to return > 0 so initializeRoomDatabase skips population
        when(roomRepository.count()).thenReturn(1L);
        service = new Dnd5eDungeonRoomService(roomRepository);
    }

    private Dnd5eDungeonRoom createRoom() {
        Dnd5eDungeonRoom room = new Dnd5eDungeonRoom();
        room.setId(1L);
        room.setName("Test Room");
        room.setDescription("A test room");
        room.setDungeonId("test_dungeon");
        room.setTheme("Combat");
        room.setSize("Medium");
        room.setMinLevel(1);
        room.setMaxLevel(5);
        room.setActive(true);
        room.setDiscovered(false);
        return room;
    }

    private Dnd5eDungeonRoom createTrapRoom() {
        Dnd5eDungeonRoom room = createRoom();
        room.setName("Trap Room");
        room.setTheme("Trap");
        room.setTrap("Poison darts");
        room.setTrapDc(15);
        room.setTrapDamage("poison");
        room.setTrapDamageExpr("2d4");
        return room;
    }

    private Dnd5eDungeonRoom createTreasureRoom() {
        Dnd5eDungeonRoom room = createRoom();
        room.setName("Treasure Room");
        room.setTheme("Treasure");
        room.setTreasure("Gold coins and gems");
        room.setGoldPieces(500);
        room.setMagicRarity("Rare");
        return room;
    }

    @Nested
    @DisplayName("createRoom Tests")
    class CreateRoom {
        @Test
        @DisplayName("should create and save a room")
        void shouldCreateAndSaveRoom() {
            Dnd5eDungeonRoom room = createRoom();
            when(roomRepository.save(room)).thenReturn(room);

            Dnd5eDungeonRoom result = service.createRoom(room);

            assertThat(result.getName()).isEqualTo("Test Room");
            verify(roomRepository).save(room);
        }
    }

    @Nested
    @DisplayName("getRoom Tests")
    class GetRoom {
        @Test
        @DisplayName("should return room when found")
        void shouldReturnRoomWhenFound() {
            Dnd5eDungeonRoom room = createRoom();
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

            Optional<Dnd5eDungeonRoom> result = service.getRoom(1L);

            assertThat(result.isPresent()).isTrue();
            assertThat(result.get().getName()).isEqualTo("Test Room");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            when(roomRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<Dnd5eDungeonRoom> result = service.getRoom(99L);

            assertThat(result.isPresent()).isFalse();
        }
    }

    @Nested
    @DisplayName("listRooms Tests")
    class ListRooms {
        @Test
        @DisplayName("should return all rooms")
        void shouldReturnAllRooms() {
            List<Dnd5eDungeonRoom> rooms = Arrays.asList(createRoom());
            when(roomRepository.findAll()).thenReturn(rooms);

            List<Dnd5eDungeonRoom> result = service.listRooms();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty list when no rooms")
        void shouldReturnEmptyList() {
            when(roomRepository.findAll()).thenReturn(Collections.emptyList());

            List<Dnd5eDungeonRoom> result = service.listRooms();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listActiveRooms Tests")
    class ListActiveRooms {
        @Test
        @DisplayName("should return active rooms")
        void shouldReturnActiveRooms() {
            List<Dnd5eDungeonRoom> rooms = Arrays.asList(createRoom());
            when(roomRepository.findByActiveTrue()).thenReturn(rooms);

            List<Dnd5eDungeonRoom> result = service.listActiveRooms();

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("listRoomsByDungeon Tests")
    class ListRoomsByDungeon {
        @Test
        @DisplayName("should return rooms for a dungeon")
        void shouldReturnRoomsForDungeon() {
            List<Dnd5eDungeonRoom> rooms = Arrays.asList(createRoom());
            when(roomRepository.findByDungeonIdAndActiveTrue("test_dungeon")).thenReturn(rooms);

            List<Dnd5eDungeonRoom> result = service.listRoomsByDungeon("test_dungeon");

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("listRoomsByTheme Tests")
    class ListRoomsByTheme {
        @Test
        @DisplayName("should return rooms for a theme")
        void shouldReturnRoomsForTheme() {
            List<Dnd5eDungeonRoom> rooms = Arrays.asList(createRoom());
            when(roomRepository.findByThemeAndActiveTrue("Combat")).thenReturn(rooms);

            List<Dnd5eDungeonRoom> result = service.listRoomsByTheme("Combat");

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("listRoomsByLevel Tests")
    class ListRoomsByLevel {
        @Test
        @DisplayName("should return rooms for a level")
        void shouldReturnRoomsForLevel() {
            List<Dnd5eDungeonRoom> rooms = Arrays.asList(createRoom());
            when(roomRepository.findByCharacterLevel(3)).thenReturn(rooms);

            List<Dnd5eDungeonRoom> result = service.listRoomsByLevel(3);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("listRoomsByDungeonAndLevel Tests")
    class ListRoomsByDungeonAndLevel {
        @Test
        @DisplayName("should return rooms for dungeon and level")
        void shouldReturnRoomsForDungeonAndLevel() {
            List<Dnd5eDungeonRoom> rooms = Arrays.asList(createRoom());
            when(roomRepository.findByDungeonAndLevel("test_dungeon", 3)).thenReturn(rooms);

            List<Dnd5eDungeonRoom> result = service.listRoomsByDungeonAndLevel("test_dungeon", 3);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Discovery Tests")
    class Discovery {
        @Test
        @DisplayName("should return discovered rooms")
        void shouldReturnDiscoveredRooms() {
            when(roomRepository.findByDiscoveredTrue()).thenReturn(Collections.emptyList());
            assertThat(service.listDiscoveredRooms()).isEmpty();
        }

        @Test
        @DisplayName("should return undiscovered rooms")
        void shouldReturnUndiscoveredRooms() {
            when(roomRepository.findByDiscoveredFalse()).thenReturn(Collections.emptyList());
            assertThat(service.listUndiscoveredRooms()).isEmpty();
        }

        @Test
        @DisplayName("should discover a room")
        void shouldDiscoverRoom() {
            Dnd5eDungeonRoom room = createRoom();
            room.setDiscovered(false);
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
            when(roomRepository.save(room)).thenReturn(room);

            Dnd5eDungeonRoom result = service.discoverRoom(1L);

            assertThat(result.isDiscovered()).isTrue();
        }

        @Test
        @DisplayName("should throw when discovering non-existent room")
        void shouldThrowWhenDiscoveringNonExistent() {
            when(roomRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.discoverRoom(99L))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should undiscover a room")
        void shouldUndiscoverRoom() {
            Dnd5eDungeonRoom room = createRoom();
            room.setDiscovered(true);
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

            service.undiscoverRoom(1L);

            assertThat(room.isDiscovered()).isFalse();
            verify(roomRepository).save(room);
        }

        @Test
        @DisplayName("should discover all rooms in dungeon")
        void shouldDiscoverAllInDungeon() {
            Dnd5eDungeonRoom room = createRoom();
            when(roomRepository.findByDungeonId("test_dungeon")).thenReturn(Arrays.asList(room));

            service.discoverAllInDungeon("test_dungeon");

            assertThat(room.isDiscovered()).isTrue();
        }

        @Test
        @DisplayName("should undiscover all rooms in dungeon")
        void shouldUndiscoverAllInDungeon() {
            Dnd5eDungeonRoom room = createRoom();
            room.setDiscovered(true);
            when(roomRepository.findByDungeonId("test_dungeon")).thenReturn(Arrays.asList(room));

            service.undiscoverAllInDungeon("test_dungeon");

            assertThat(room.isDiscovered()).isFalse();
        }
    }

    @Nested
    @DisplayName("Connection Tests")
    class Connections {
        @Test
        @DisplayName("should add bidirectional connection")
        void shouldAddBidirectionalConnection() {
            Dnd5eDungeonRoom room1 = createRoom();
            room1.setId(1L);
            room1.setConnections("[]");
            Dnd5eDungeonRoom room2 = createRoom();
            room2.setId(2L);
            room2.setConnections("[]");

            when(roomRepository.findById(1L)).thenReturn(Optional.of(room1));
            when(roomRepository.findById(2L)).thenReturn(Optional.of(room2));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.addConnection(1L, 2L);

            assertThat(room1.getConnections()).contains("2");
            assertThat(room2.getConnections()).contains("1");
        }

        @Test
        @DisplayName("should throw when room not found")
        void shouldThrowWhenRoomNotFound() {
            when(roomRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addConnection(1L, 2L))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should remove connection")
        void shouldRemoveConnection() {
            Dnd5eDungeonRoom room1 = createRoom();
            room1.setId(1L);
            room1.setConnections("[2]");
            Dnd5eDungeonRoom room2 = createRoom();
            room2.setId(2L);
            room2.setConnections("[1]");

            when(roomRepository.findById(1L)).thenReturn(Optional.of(room1));
            when(roomRepository.findById(2L)).thenReturn(Optional.of(room2));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.removeConnection(1L, 2L);

            assertThat(room1.getConnections()).isEqualTo("[]");
            assertThat(room2.getConnections()).isEqualTo("[]");
        }

        @Test
        @DisplayName("should get connected rooms")
        void shouldGetConnectedRooms() {
            Dnd5eDungeonRoom room1 = createRoom();
            room1.setId(1L);
            room1.setConnections("[2]");
            Dnd5eDungeonRoom room2 = createRoom();
            room2.setId(2L);
            room2.setName("Connected Room");

            when(roomRepository.findById(1L)).thenReturn(Optional.of(room1));
            when(roomRepository.findById(2L)).thenReturn(Optional.of(room2));

            List<Dnd5eDungeonRoom> result = service.getConnectedRooms(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Connected Room");
        }

        @Test
        @DisplayName("should return empty for room with no connections")
        void shouldReturnEmptyForNoConnections() {
            Dnd5eDungeonRoom room = createRoom();
            room.setConnections("[]");
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

            List<Dnd5eDungeonRoom> result = service.getConnectedRooms(1L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Trap Check Tests")
    class TrapCheck {
        @Test
        @DisplayName("should succeed when roll meets DC")
        void shouldSucceedWhenRollMeetsDc() {
            Dnd5eDungeonRoom room = createTrapRoom();
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

            TrapCheckResult result = service.makeTrapCheck(1L, 2, 2, 11);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isCriticalFailure()).isFalse();
            assertThat(result.getDamageMultiplier()).isZero();
            assertThat(result.getDc()).isEqualTo(15);
        }

        @Test
        @DisplayName("should fail when roll below DC")
        void shouldFailWhenRollBelowDc() {
            Dnd5eDungeonRoom room = createTrapRoom();
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

            TrapCheckResult result = service.makeTrapCheck(1L, 0, 0, 5);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getDamageMultiplier()).isEqualTo(1);
            assertThat(result.getDamageType()).isEqualTo("poison");
        }

        @Test
        @DisplayName("should critical fail on natural 1")
        void shouldCriticalFailOnNatural1() {
            Dnd5eDungeonRoom room = createTrapRoom();
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

            TrapCheckResult result = service.makeTrapCheck(1L, 0, 0, 1);

            assertThat(result.isCriticalFailure()).isTrue();
            assertThat(result.getDamageMultiplier()).isEqualTo(2);
        }

        @Test
        @DisplayName("should succeed on natural 20 regardless of DC")
        void shouldSucceedOnNatural20() {
            Dnd5eDungeonRoom room = createTrapRoom();
            room.setTrapDc(30);
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

            TrapCheckResult result = service.makeTrapCheck(1L, 0, 0, 20);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getDamageMultiplier()).isZero();
        }

        @Test
        @DisplayName("should return null DC for room without trap")
        void shouldReturnNullDcForNoTrap() {
            Dnd5eDungeonRoom room = createRoom();
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

            TrapCheckResult result = service.makeTrapCheck(1L, 0, 0, 10);

            assertThat(result.getDc()).isNull();
            assertThat(result.getDamageMultiplier()).isZero();
        }

        @Test
        @DisplayName("should throw when room not found")
        void shouldThrowWhenRoomNotFound() {
            when(roomRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.makeTrapCheck(99L, 0, 0, 10))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should include damage expression")
        void shouldIncludeDamageExpression() {
            Dnd5eDungeonRoom room = createTrapRoom();
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

            TrapCheckResult result = service.makeTrapCheck(1L, 0, 0, 5);

            assertThat(result.getDamageExpr()).isEqualTo("2d4");
        }
    }

    @Nested
    @DisplayName("Treasure Discovery Tests")
    class TreasureDiscovery {
        @Test
        @DisplayName("should return treasure info")
        void shouldReturnTreasureInfo() {
            Dnd5eDungeonRoom room = createTreasureRoom();
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

            TreasureResult result = service.discoverTreasure(1L);

            assertThat(result.getTreasure()).isEqualTo("Gold coins and gems");
            assertThat(result.getGoldPieces()).isEqualTo(500);
            assertThat(result.getMagicRarity()).isEqualTo("Rare");
            assertThat(result.getRoomName()).isEqualTo("Treasure Room");
        }

        @Test
        @DisplayName("should return null treasure for empty room")
        void shouldReturnNullTreasureForEmptyRoom() {
            Dnd5eDungeonRoom room = createRoom();
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

            TreasureResult result = service.discoverTreasure(1L);

            assertThat(result.getTreasure()).isNull();
            assertThat(result.getGoldPieces()).isZero();
        }

        @Test
        @DisplayName("should throw when room not found")
        void shouldThrowWhenRoomNotFound() {
            when(roomRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.discoverTreasure(99L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("updateRoom Tests")
    class UpdateRoom {
        @Test
        @DisplayName("should update room fields")
        void shouldUpdateRoomFields() {
            Dnd5eDungeonRoom existing = createRoom();
            Dnd5eDungeonRoom updates = new Dnd5eDungeonRoom();
            updates.setName("Updated Room");
            updates.setTheme("Trap");

            when(roomRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(roomRepository.save(existing)).thenReturn(existing);

            Dnd5eDungeonRoom result = service.updateRoom(1L, updates);

            assertThat(result.getName()).isEqualTo("Updated Room");
            assertThat(result.getTheme()).isEqualTo("Trap");
        }

        @Test
        @DisplayName("should throw when room not found")
        void shouldThrowWhenNotFound() {
            when(roomRepository.findById(99L)).thenReturn(Optional.empty());

            Dnd5eDungeonRoom updates = new Dnd5eDungeonRoom();
            assertThatThrownBy(() -> service.updateRoom(99L, updates))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("deleteRoom Tests")
    class DeleteRoom {
        @Test
        @DisplayName("should delete room and return true")
        void shouldDeleteRoom() {
            Dnd5eDungeonRoom room = createRoom();
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

            boolean result = service.deleteRoom(1L);

            assertThat(result).isTrue();
            verify(roomRepository).delete(room);
        }

        @Test
        @DisplayName("should return false when not found")
        void shouldReturnFalseWhenNotFound() {
            when(roomRepository.findById(99L)).thenReturn(Optional.empty());

            boolean result = service.deleteRoom(99L);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("toggleRoomActive Tests")
    class ToggleRoomActive {
        @Test
        @DisplayName("should toggle active state")
        void shouldToggleActiveState() {
            Dnd5eDungeonRoom room = createRoom();
            room.setActive(true);
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

            service.toggleRoomActive(1L);

            assertThat(room.isActive()).isFalse();
            verify(roomRepository).save(room);
        }
    }

    @Nested
    @DisplayName("Reference Data Tests")
    class ReferenceData {
        @Test
        @DisplayName("should list dungeon IDs")
        void shouldListDungeonIds() {
            when(roomRepository.findDistinctDungeonIds()).thenReturn(Arrays.asList("dungeon1", "dungeon2"));

            List<String> result = service.listDungeonIds();

            assertThat(result).containsExactlyInAnyOrder("dungeon1", "dungeon2");
        }

        @Test
        @DisplayName("should list themes")
        void shouldListThemes() {
            when(roomRepository.findDistinctThemes()).thenReturn(Arrays.asList("Combat", "Trap", "Treasure"));

            List<String> result = service.listThemes();

            assertThat(result).containsExactlyInAnyOrder("Combat", "Trap", "Treasure");
        }
    }

    @Nested
    @DisplayName("searchRooms Tests")
    class SearchRooms {
        @Test
        @DisplayName("should search by keyword")
        void shouldSearchByKeyword() {
            List<Dnd5eDungeonRoom> rooms = Arrays.asList(createRoom());
            when(roomRepository.searchByKeyword("test")).thenReturn(rooms);

            List<Dnd5eDungeonRoom> result = service.searchRooms("test");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty when no match")
        void shouldReturnEmptyWhenNoMatch() {
            when(roomRepository.searchByKeyword("nonexistent")).thenReturn(Collections.emptyList());

            List<Dnd5eDungeonRoom> result = service.searchRooms("nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("initializeRoomDatabase Tests")
    class InitializeRoomDatabase {
        @Test
        @DisplayName("should skip if database is populated")
        void shouldSkipIfPopulated() {
            // The service was created in setUp() with count > 0 already mocked
            // The initializeRoomDatabase should have called count() once and skipped
            verify(roomRepository, atLeast(1)).count();
        }

        @Test
        @DisplayName("should populate if database is empty")
        void shouldPopulateIfEmpty() {
            // Reset the mock and set count to 0
            reset(roomRepository);
            when(roomRepository.count()).thenReturn(0L);
            when(roomRepository.save(any(Dnd5eDungeonRoom.class))).thenAnswer(inv -> inv.getArgument(0));
            when(roomRepository.findByDungeonId(anyString())).thenAnswer(inv -> Collections.emptyList());

            Dnd5eDungeonRoomService testService = new Dnd5eDungeonRoomService(roomRepository);

            verify(roomRepository, atLeastOnce()).save(any(Dnd5eDungeonRoom.class));
        }
    }

    @Nested
    @DisplayName("listRoomsTraps/Treasure/Encounters Tests")
    class SpecialRoomLists {
        @Test
        @DisplayName("should list rooms with traps")
        void shouldListRoomsTraps() {
            when(roomRepository.findRoomsTraps()).thenReturn(Collections.emptyList());
            assertThat(service.listRoomsTraps()).isEmpty();
        }

        @Test
        @DisplayName("should list rooms with treasure")
        void shouldListRoomsTreasure() {
            when(roomRepository.findRoomsTreasure()).thenReturn(Collections.emptyList());
            assertThat(service.listRoomsTreasure()).isEmpty();
        }

        @Test
        @DisplayName("should list rooms with encounters")
        void shouldListRoomsEncounters() {
            when(roomRepository.findRoomsEncounters()).thenReturn(Collections.emptyList());
            assertThat(service.listRoomsEncounters()).isEmpty();
        }
    }

    @Nested
    @DisplayName("listDiscoveredRoomsByDungeon Tests")
    class DiscoveredByDungeon {
        @Test
        @DisplayName("should list discovered rooms by dungeon")
        void shouldListDiscoveredByDungeon() {
            when(roomRepository.findByDungeonIdAndDiscoveredTrue("test_dungeon")).thenReturn(Collections.emptyList());
            assertThat(service.listDiscoveredRoomsByDungeon("test_dungeon")).isEmpty();
        }

        @Test
        @DisplayName("should list undiscovered rooms by dungeon")
        void shouldListUndiscoveredByDungeon() {
            when(roomRepository.findByDungeonIdAndDiscoveredFalse("test_dungeon")).thenReturn(Collections.emptyList());
            assertThat(service.listUndiscoveredRoomsByDungeon("test_dungeon")).isEmpty();
        }
    }
}
