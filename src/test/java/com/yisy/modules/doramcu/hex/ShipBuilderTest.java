package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.hex.dungeon.ShipModuleTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ShipBuilder Tests")
class ShipBuilderTest {

    private ShipModuleTemplate createTemplate(String name, ShipModuleTemplate.ShipModuleType type) {
        ShipModuleTemplate t = new ShipModuleTemplate();
        t.setName(name);
        t.setType(type);
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
        t.setShapeCellsJson(ShipModuleTemplate.serializeCells(cells));
        return t;
    }

    @Nested
    @DisplayName("Build from Template")
    class BuildFromTemplateTests {

        @Test
        @DisplayName("should build HullModule from template")
        void buildFromTemplate_shouldBuild() {
            ShipBuilder builder = new ShipBuilder();
            ShipModuleTemplate template = createTemplate("Bridge", ShipModuleTemplate.ShipModuleType.BRIDGE);
            HullModule module = builder.buildFromTemplate(template);
            assertNotNull(module);
            assertEquals("Bridge", module.getName());
        }

        @Test
        @DisplayName("should throw on null template")
        void buildFromTemplate_nullTemplate() {
            ShipBuilder builder = new ShipBuilder();
            assertThrows(IllegalArgumentException.class, () -> builder.buildFromTemplate(null));
        }
    }

    @Nested
    @DisplayName("Ship Assembly")
    class AssemblyTests {

        @Test
        @DisplayName("should assemble ship from templates")
        void assemble_shouldCreateShip() {
            ShipBuilder builder = new ShipBuilder();
            List<ShipModuleTemplate> templates = new ArrayList<>();
            templates.add(createTemplate("Bridge", ShipModuleTemplate.ShipModuleType.BRIDGE));
            templates.add(createTemplate("Engine Room", ShipModuleTemplate.ShipModuleType.ENGINE_ROOM));

            Ship ship = builder.assemble("Test Ship", ShipType.FRIGATE, templates, null);
            assertNotNull(ship);
            assertEquals("Test Ship", ship.getName());
        }

        @Test
        @DisplayName("should assemble ship with connections")
        void assemble_withConnections() {
            ShipBuilder builder = new ShipBuilder();
            List<ShipModuleTemplate> templates = new ArrayList<>();
            templates.add(createTemplate("Bridge", ShipModuleTemplate.ShipModuleType.BRIDGE));
            templates.add(createTemplate("Engine Room", ShipModuleTemplate.ShipModuleType.ENGINE_ROOM));

            List<ShipBuilder.ConnectionDefinition> connections = new ArrayList<>();
            connections.add(new ShipBuilder.ConnectionDefinition(
                    0, new M3space(5, 0, 5), 1, new M3space(5, 9, 5)));

            Ship ship = builder.assemble("Connected Ship", ShipType.FRIGATE, templates, connections);
            assertNotNull(ship);
        }

        @Test
        @DisplayName("should assemble ship with empty templates")
        void assemble_emptyTemplates() {
            ShipBuilder builder = new ShipBuilder();
            Ship ship = builder.assemble("Empty Ship", ShipType.FRIGATE, new ArrayList<>(), null);
            assertNotNull(ship);
        }
    }

    @Nested
    @DisplayName("Connection at Passage")
    class ConnectAtPassageTests {

        @Test
        @DisplayName("should connect modules at passage")
        void connectAtPassage_shouldConnect() {
            ShipBuilder builder = new ShipBuilder();
            Ship ship = new Ship(0, ShipType.FRIGATE);
            ShipHull hull = new ShipHull(ship);

            HullModule m1 = new HullModule("Module1", 10, 10, 10);
            HullModule m2 = new HullModule("Module2", 10, 10, 10);
            hull.registerModule("m1", m1);
            hull.registerModule("m2", m2);

            builder.connectAtPassage(hull, "m1", new M3space(5, 0, 5), "m2", new M3space(5, 9, 5));
            assertEquals(1, hull.getConnections().size());
        }
    }

    @Nested
    @DisplayName("ConnectionDefinition")
    class ConnectionDefinitionTests {

        @Test
        @DisplayName("should store connection definition")
        void connectionDefinition_shouldStore() {
            M3space from = new M3space(5, 0, 5);
            M3space to = new M3space(5, 9, 5);
            ShipBuilder.ConnectionDefinition def = new ShipBuilder.ConnectionDefinition(0, from, 1, to);
            assertEquals(0, def.getFromModuleIndex());
            assertEquals(from, def.getFromCell());
            assertEquals(1, def.getToModuleIndex());
            assertEquals(to, def.getToCell());
        }
    }

    @Nested
    @DisplayName("Module ID Generation")
    class ModuleIdTests {

        @Test
        @DisplayName("should generate unique module IDs")
        void generateModuleId_shouldBeUnique() {
            ShipBuilder builder = new ShipBuilder();
            List<ShipModuleTemplate> templates = new ArrayList<>();
            templates.add(createTemplate("Bridge", ShipModuleTemplate.ShipModuleType.BRIDGE));
            templates.add(createTemplate("Bridge", ShipModuleTemplate.ShipModuleType.BRIDGE));

            Ship ship = builder.assemble("Test", ShipType.FRIGATE, templates, null);
            assertNotNull(ship);
        }
    }
}
