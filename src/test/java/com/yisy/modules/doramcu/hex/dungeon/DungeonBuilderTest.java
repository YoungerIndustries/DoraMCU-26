package com.yisy.modules.doramcu.hex.dungeon;

import com.yisy.modules.doramcu.hex.HullModule;
import com.yisy.modules.doramcu.hex.M3space;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DungeonBuilder Tests")
class DungeonBuilderTest {

    private DungeonModuleTemplate createTemplate(String name, DungeonRoom roomType) {
        DungeonModuleTemplate t = new DungeonModuleTemplate();
        t.setName(name);
        t.setType(roomType);
        t.setWidth(10);
        t.setHeight(10);
        t.setDepth(10);
        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 10; z++) {
                    cells.add(new M3space(x, y, z));
                }
            }
        }
        t.setShapeCellsJson(DungeonModuleTemplate.serializeCells(cells));
        return t;
    }

    // ---- Build from Template ----

    @Nested
    @DisplayName("Build from Template")
    class BuildFromTemplateTests {

        @Test
        @DisplayName("should build HullModule from template")
        void buildFromTemplate_shouldBuild() {
            DungeonBuilder builder = new DungeonBuilder();
            DungeonModuleTemplate template = createTemplate("Entrance", DungeonRoom.CORRIDOR);
            HullModule module = builder.buildFromTemplate(template);
            assertNotNull(module);
            assertEquals("Entrance", module.getName());
        }

        @Test
        @DisplayName("should throw on null template")
        void buildFromTemplate_nullTemplate() {
            DungeonBuilder builder = new DungeonBuilder();
            assertThrows(IllegalArgumentException.class, () -> builder.buildFromTemplate(null));
        }
    }

    // ---- Dungeon Assembly ----

    @Nested
    @DisplayName("Dungeon Assembly")
    class AssemblyTests {

        @Test
        @DisplayName("should assemble dungeon from templates")
        void assemble_shouldCreateDungeon() {
            DungeonBuilder builder = new DungeonBuilder();
            List<DungeonModuleTemplate> templates = new ArrayList<>();
            templates.add(createTemplate("Entrance", DungeonRoom.CORRIDOR));
            templates.add(createTemplate("Treasure Room", DungeonRoom.TREASURE_VAULT));

            Dungeon dungeon = builder.assemble("Test Dungeon", "A test dungeon", templates, null);
            assertNotNull(dungeon);
            assertEquals("Test Dungeon", dungeon.getName());
            assertEquals("A test dungeon", dungeon.getDescription());
            assertEquals(2, dungeon.getRoomCount());
        }

        @Test
        @DisplayName("should assemble dungeon with connections")
        void assemble_withConnections() {
            DungeonBuilder builder = new DungeonBuilder();
            List<DungeonModuleTemplate> templates = new ArrayList<>();
            templates.add(createTemplate("Entrance", DungeonRoom.CORRIDOR));
            templates.add(createTemplate("Treasure Room", DungeonRoom.TREASURE_VAULT));

            List<DungeonBuilder.ConnectionDefinition> connections = new ArrayList<>();
            connections.add(new DungeonBuilder.ConnectionDefinition(
                    0, new M3space(5, 0, 5), 1, new M3space(5, 9, 5)));

            Dungeon dungeon = builder.assemble("Connected Dungeon", "desc", templates, connections);
            assertNotNull(dungeon);
            assertEquals(1, dungeon.getConnections().size());
        }

        @Test
        @DisplayName("should assemble dungeon with empty templates")
        void assemble_emptyTemplates() {
            DungeonBuilder builder = new DungeonBuilder();
            Dungeon dungeon = builder.assemble("Empty Dungeon", "desc", new ArrayList<>(), null);
            assertNotNull(dungeon);
            assertEquals(0, dungeon.getRoomCount());
        }

        @Test
        @DisplayName("should generate unique room IDs")
        void assemble_uniqueRoomIds() {
            DungeonBuilder builder = new DungeonBuilder();
            List<DungeonModuleTemplate> templates = new ArrayList<>();
            templates.add(createTemplate("Corridor", DungeonRoom.CORRIDOR));
            templates.add(createTemplate("Corridor", DungeonRoom.CORRIDOR));

            Dungeon dungeon = builder.assemble("Test", "desc", templates, null);
            assertEquals(2, dungeon.getRoomCount());
            // Both should have IDs based on "corridor" but with different counters
            Set<String> roomIds = new HashSet<>(dungeon.getRooms().keySet());
            assertEquals(2, roomIds.size());
        }

        @Test
        @DisplayName("should handle null connections list")
        void assemble_nullConnections() {
            DungeonBuilder builder = new DungeonBuilder();
            List<DungeonModuleTemplate> templates = new ArrayList<>();
            templates.add(createTemplate("Entrance", DungeonRoom.CORRIDOR));

            Dungeon dungeon = builder.assemble("Test", "desc", templates, null);
            assertNotNull(dungeon);
            assertEquals(1, dungeon.getRoomCount());
            assertEquals(0, dungeon.getConnections().size());
        }
    }

    // ---- Connect at Doorway ----

    @Nested
    @DisplayName("Connect at Doorway")
    class ConnectAtDoorwayTests {

        @Test
        @DisplayName("should connect rooms at doorway")
        void connectAtDoorway_shouldConnect() {
            DungeonBuilder builder = new DungeonBuilder();
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", new HullModule("Entrance", 10, 10, 10));
            dungeon.addRoom("room2", new HullModule("Corridor", 10, 10, 10));

            builder.connectAtDoorway(dungeon, "room1", new M3space(5, 0, 5), "room2", new M3space(5, 9, 5));
            assertEquals(1, dungeon.getConnections().size());
        }
    }

    // ---- ConnectionDefinition ----

    @Nested
    @DisplayName("ConnectionDefinition")
    class ConnectionDefinitionTests {

        @Test
        @DisplayName("should create connection definition")
        void constructor_shouldCreate() {
            DungeonBuilder.ConnectionDefinition def = new DungeonBuilder.ConnectionDefinition(
                    0, new M3space(5, 0, 5), 1, new M3space(5, 9, 5));
            assertEquals(0, def.getFromRoomIndex());
            assertEquals(5, def.getFromCell().getX());
            assertEquals(0, def.getFromCell().getY());
            assertEquals(5, def.getFromCell().getZ());
            assertEquals(1, def.getToRoomIndex());
            assertEquals(5, def.getToCell().getX());
            assertEquals(9, def.getToCell().getY());
            assertEquals(5, def.getToCell().getZ());
        }
    }
}
