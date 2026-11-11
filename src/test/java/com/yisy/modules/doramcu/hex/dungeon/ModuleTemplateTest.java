package com.yisy.modules.doramcu.hex.dungeon;

import com.yisy.modules.doramcu.hex.Compartment;
import com.yisy.modules.doramcu.hex.CompartmentType;
import com.yisy.modules.doramcu.hex.HullModule;
import com.yisy.modules.doramcu.hex.M3space;
import com.yisy.modules.doramcu.hex.ShapeDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ModuleTemplate Base Class Tests")
class ModuleTemplateTest {

    /** Concrete subclass for testing abstract ModuleTemplate methods. */
    private static class TestTemplate extends ModuleTemplate {
        void exposeOnCreate() { onCreate(); }
        void exposeOnUpdate() { onUpdate(); }
    }

    @Nested
    @DisplayName("Basic Properties")
    class BasicPropertiesTests {

        @Test
        @DisplayName("should set and get name")
        void name_shouldWork() {
            TestTemplate t = new TestTemplate();
            t.setName("Test Room");
            assertEquals("Test Room", t.getName());
        }

        @Test
        @DisplayName("should set and get description")
        void description_shouldWork() {
            TestTemplate t = new TestTemplate();
            t.setDescription("A test room");
            assertEquals("A test room", t.getDescription());
        }

        @Test
        @DisplayName("should set and get dimensions")
        void dimensions_shouldWork() {
            TestTemplate t = new TestTemplate();
            t.setWidth(10);
            t.setHeight(10);
            t.setDepth(10);
            assertEquals(10, t.getWidth());
            assertEquals(10, t.getHeight());
            assertEquals(10, t.getDepth());
        }

        @Test
        @DisplayName("should default difficulty to 1")
        void difficulty_shouldDefaultToOne() {
            TestTemplate t = new TestTemplate();
            assertEquals(1, t.getDifficulty());
        }

        @Test
        @DisplayName("should clamp difficulty to 1-10 range")
        void difficulty_shouldClamp() {
            TestTemplate t = new TestTemplate();
            t.setDifficulty(0);
            assertEquals(1, t.getDifficulty());
            t.setDifficulty(100);
            assertEquals(10, t.getDifficulty());
            t.setDifficulty(5);
            assertEquals(5, t.getDifficulty());
        }

        @Test
        @DisplayName("should set and get tags")
        void tags_shouldWork() {
            TestTemplate t = new TestTemplate();
            t.setTags("corridor,transit,common");
            assertEquals("corridor,transit,common", t.getTags());
        }

        @Test
        @DisplayName("should set and get ID")
        void id_shouldWork() {
            TestTemplate t = new TestTemplate();
            t.setId(42L);
            assertEquals(42L, t.getId());
        }
    }

    @Nested
    @DisplayName("Lifecycle Callbacks")
    class LifecycleTests {

        @Test
        @DisplayName("should set createdAt and updatedAt on persist")
        void onCreate_shouldSetTimestamps() {
            TestTemplate t = new TestTemplate();
            t.exposeOnCreate();
            assertNotNull(t.getCreatedAt());
            assertNotNull(t.getUpdatedAt());
        }

        @Test
        @DisplayName("should update updatedAt on update")
        void onUpdate_shouldSetUpdatedAt() {
            TestTemplate t = new TestTemplate();
            t.exposeOnCreate();
            var created = t.getUpdatedAt();
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
            t.exposeOnUpdate();
            assertNotEquals(created, t.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Cell Coordinate Parsing")
    class CellParsingTests {

        @Test
        @DisplayName("should parse valid cell coordinates")
        void parseCellCoordinates_shouldParse() {
            String json = "[\"0,0,0\",\"1,0,0\",\"2,1,3\"]";
            Set<M3space> cells = ModuleTemplate.parseCellCoordinates(json);
            assertEquals(3, cells.size());
            assertTrue(cells.contains(new M3space(0, 0, 0)));
            assertTrue(cells.contains(new M3space(1, 0, 0)));
            assertTrue(cells.contains(new M3space(2, 1, 3)));
        }

        @Test
        @DisplayName("should handle empty JSON array")
        void parseCellCoordinates_emptyArray() {
            String json = "[]";
            Set<M3space> cells = ModuleTemplate.parseCellCoordinates(json);
            assertTrue(cells.isEmpty());
        }

        @Test
        @DisplayName("should skip invalid coordinates")
        void parseCellCoordinates_shouldSkipInvalid() {
            String json = "[\"0,0,0\",\"abc\",\"1,0,0\"]";
            Set<M3space> cells = ModuleTemplate.parseCellCoordinates(json);
            assertEquals(2, cells.size());
        }

        @Test
        @DisplayName("should handle JSON without brackets")
        void parseCellCoordinates_noBrackets() {
            String json = "\"0,0,0\",\"1,0,0\"";
            Set<M3space> cells = ModuleTemplate.parseCellCoordinates(json);
            assertEquals(2, cells.size());
        }
    }

    @Nested
    @DisplayName("Cell Serialization")
    class CellSerializationTests {

        @Test
        @DisplayName("should serialize cells to JSON")
        void serializeCells_shouldSerialize() {
            Set<M3space> cells = new HashSet<>();
            cells.add(new M3space(0, 0, 0));
            cells.add(new M3space(1, 0, 0));
            String json = ModuleTemplate.serializeCells(cells);
            assertTrue(json.contains("\"0,0,0\""));
            assertTrue(json.contains("\"1,0,0\""));
            assertTrue(json.startsWith("["));
            assertTrue(json.endsWith("]"));
        }

        @Test
        @DisplayName("should return empty array for null cells")
        void serializeCells_nullCells() {
            assertEquals("[]", ModuleTemplate.serializeCells(null));
        }

        @Test
        @DisplayName("should return empty array for empty cells")
        void serializeCells_emptyCells() {
            assertEquals("[]", ModuleTemplate.serializeCells(new HashSet<>()));
        }

        @Test
        @DisplayName("should round-trip serialize and parse")
        void serializeAndParse_shouldRoundTrip() {
            Set<M3space> original = new HashSet<>();
            original.add(new M3space(0, 0, 0));
            original.add(new M3space(5, 3, 2));
            original.add(new M3space(9, 9, 9));

            String json = ModuleTemplate.serializeCells(original);
            Set<M3space> parsed = ModuleTemplate.parseCellCoordinates(json);

            assertEquals(original.size(), parsed.size());
            assertTrue(parsed.containsAll(original));
        }
    }

    @Nested
    @DisplayName("Compartment Type Mapping")
    class CompartmentTypeMappingTests {

        @Test
        @DisplayName("should map standard CompartmentType names directly")
        void mapToCompartmentType_standardNames() {
            assertEquals(CompartmentType.COMMAND, ModuleTemplate.mapToCompartmentType("COMMAND"));
            assertEquals(CompartmentType.CARGO, ModuleTemplate.mapToCompartmentType("CARGO"));
            assertEquals(CompartmentType.ENGINE, ModuleTemplate.mapToCompartmentType("ENGINE"));
        }

        @Test
        @DisplayName("should map dungeon room types to CompartmentType")
        void mapToCompartmentType_dungeonTypes() {
            // Dungeon types that are now direct CompartmentType values return themselves
            assertEquals(CompartmentType.THRONES_ROOM, ModuleTemplate.mapToCompartmentType("THRONES_ROOM"));
            assertEquals(CompartmentType.TREASURE_VAULT, ModuleTemplate.mapToCompartmentType("TREASURE_VAULT"));
            assertEquals(CompartmentType.LIBRARY, ModuleTemplate.mapToCompartmentType("LIBRARY"));
            assertEquals(CompartmentType.PRISON, ModuleTemplate.mapToCompartmentType("PRISON"));
            assertEquals(CompartmentType.ARMORY, ModuleTemplate.mapToCompartmentType("ARMORY"));
            assertEquals(CompartmentType.TRAP_ROOM, ModuleTemplate.mapToCompartmentType("TRAP_ROOM"));
            // Aliases that are NOT direct enum values still map via switch
            assertEquals(CompartmentType.COMMAND, ModuleTemplate.mapToCompartmentType("BOSS_CHAMBER"));
            assertEquals(CompartmentType.CARGO, ModuleTemplate.mapToCompartmentType("STORAGE"));
            assertEquals(CompartmentType.TRANSIT, ModuleTemplate.mapToCompartmentType("CORRIDOR"));
        }

        @Test
        @DisplayName("should map unknown types to EMPTY")
        void mapToCompartmentType_unknown() {
            assertEquals(CompartmentType.EMPTY, ModuleTemplate.mapToCompartmentType("UNKNOWN_TYPE"));
        }

        @Test
        @DisplayName("should handle case insensitivity")
        void mapToCompartmentType_caseInsensitive() {
            assertEquals(CompartmentType.COMMAND, ModuleTemplate.mapToCompartmentType("command"));
            // treasure_vault is now a direct enum value, so it returns itself
            assertEquals(CompartmentType.TREASURE_VAULT, ModuleTemplate.mapToCompartmentType("treasure_vault"));
        }
    }

    @Nested
    @DisplayName("Connection Points Parsing")
    class ConnectionPointsTests {

        @Test
        @DisplayName("should parse connection points JSON")
        void getConnectionPoints_shouldParse() {
            TestTemplate t = new TestTemplate();
            t.setConnectionPointsJson("[{\"x\":4,\"y\":0,\"z\":1,\"direction\":\"UP\"},{\"x\":4,\"y\":7,\"z\":1,\"direction\":\"DOWN\"}]");
            List<Map<String, Object>> points = t.getConnectionPoints();
            assertEquals(2, points.size());
            assertEquals(4, points.get(0).get("x"));
            assertEquals(0, points.get(0).get("y"));
            assertEquals("UP", points.get(0).get("direction"));
        }

        @Test
        @DisplayName("should return empty list for null connection points")
        void getConnectionPoints_null() {
            TestTemplate t = new TestTemplate();
            assertTrue(t.getConnectionPoints().isEmpty());
        }

        @Test
        @DisplayName("should return empty list for blank connection points")
        void getConnectionPoints_blank() {
            TestTemplate t = new TestTemplate();
            t.setConnectionPointsJson("  ");
            assertTrue(t.getConnectionPoints().isEmpty());
        }
    }

    @Nested
    @DisplayName("Shape Definition Conversion")
    class ShapeDefinitionTests {

        @Test
        @DisplayName("should convert to ShapeDefinition")
        void toShapeDefinition_shouldConvert() {
            TestTemplate t = new TestTemplate();
            t.setShapeCellsJson("[\"0,0,0\",\"1,0,0\",\"2,0,0\"]");
            ShapeDefinition shape = t.toShapeDefinition();
            assertNotNull(shape);
        }

        @Test
        @DisplayName("should throw when shapeCellsJson is null")
        void toShapeDefinition_nullJson() {
            TestTemplate t = new TestTemplate();
            assertThrows(IllegalArgumentException.class, t::toShapeDefinition);
        }

        @Test
        @DisplayName("should throw when shapeCellsJson is blank")
        void toShapeDefinition_blankJson() {
            TestTemplate t = new TestTemplate();
            t.setShapeCellsJson("  ");
            assertThrows(IllegalArgumentException.class, t::toShapeDefinition);
        }
    }

    @Nested
    @DisplayName("HullModule Conversion")
    class HullModuleConversionTests {

        @Test
        @DisplayName("should create HullModule from template")
        void toHullModule_shouldCreate() {
            TestTemplate t = new TestTemplate();
            t.setName("Test Module");
            t.setWidth(10);
            t.setHeight(10);
            t.setDepth(10);
            t.setShapeCellsJson("[\"0,0,0\",\"1,0,0\",\"2,0,0\"]");
            HullModule module = t.toHullModule();
            assertNotNull(module);
            assertEquals("Test Module", module.getName());
        }

        @Test
        @DisplayName("should add compartments from JSON")
        void toHullModule_shouldAddCompartments() {
            TestTemplate t = new TestTemplate();
            t.setName("Test Module");
            t.setWidth(10);
            t.setHeight(10);
            t.setDepth(10);
            t.setShapeCellsJson("[\"0,0,0\",\"1,0,0\",\"2,0,0\"]");
            t.setCompartmentsJson("[{\"name\":\"Bridge\",\"type\":\"COMMAND\",\"cells\":[\"0,0,0\"]}]");
            HullModule module = t.toHullModule();
            List<Compartment> compartments = module.getCompartments();
            assertEquals(1, compartments.size());
            assertEquals("Bridge", compartments.get(0).getName());
            assertEquals(CompartmentType.COMMAND, compartments.get(0).getType());
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("should produce meaningful string representation")
        void toString_shouldProduceOutput() {
            TestTemplate t = new TestTemplate();
            t.setId(1L);
            t.setName("Test Room");
            t.setWidth(10);
            t.setHeight(10);
            t.setDepth(10);
            t.setDifficulty(5);
            String s = t.toString();
            assertTrue(s.contains("Test Room"));
            assertTrue(s.contains("10x10x10"));
            assertTrue(s.contains("difficulty=5"));
        }
    }
}
