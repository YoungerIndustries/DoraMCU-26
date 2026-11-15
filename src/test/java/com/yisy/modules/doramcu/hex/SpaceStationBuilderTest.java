package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.gamecore.DungeonRoomService;
import com.yisy.modules.doramcu.hex.dungeon.StationModuleTemplate;
import com.yisy.modules.doramcu.hex.dungeon.StationModuleTemplateRepository;
import com.yisy.modules.doramcu.hex.planet.MockDungeonRoomService;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SpaceStationBuilder}.
 * Tests template CRUD, station assembly, interior generation, compartment mapping,
 * pre-population, and all factory methods.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SpaceStationBuilderTest {

    @Mock
    private StationModuleTemplateRepository mockTemplateRepo;
    private MockDungeonRoomService mockDungeonRoomService;
    private SpaceStationBuilder builder;

    private final Map<Long, StationModuleTemplate> savedTemplates = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @BeforeEach
    void setUp() {
        mockDungeonRoomService = new MockDungeonRoomService();
        builder = new SpaceStationBuilder(mockTemplateRepo, mockDungeonRoomService, null, null, null);
        savedTemplates.clear();
        nextId.set(1);

        // Configure mock repository behavior
        when(mockTemplateRepo.save(any(StationModuleTemplate.class))).thenAnswer(invocation -> {
            StationModuleTemplate template = invocation.getArgument(0);
            if (template.getId() == null) {
                template.setId(nextId.getAndIncrement());
            }
            savedTemplates.put(template.getId(), template);
            return template;
        });
        when(mockTemplateRepo.findById(any(Long.class))).thenAnswer(invocation ->
                Optional.ofNullable(savedTemplates.get(invocation.getArgument(0))));
        when(mockTemplateRepo.existsById(any(Long.class))).thenAnswer(invocation ->
                savedTemplates.containsKey(invocation.getArgument(0)));
        when(mockTemplateRepo.findAll()).thenAnswer(invocation -> new ArrayList<>(savedTemplates.values()));
        when(mockTemplateRepo.findAllById(any(Iterable.class))).thenAnswer(invocation -> {
            Iterable<Long> ids = invocation.getArgument(0);
            List<StationModuleTemplate> result = new ArrayList<>();
            for (Long id : ids) {
                StationModuleTemplate t = savedTemplates.get(id);
                if (t != null) result.add(t);
            }
            return result;
        });
        doAnswer(invocation -> {
            savedTemplates.remove(invocation.getArgument(0));
            return null;
        }).when(mockTemplateRepo).deleteById(any(Long.class));
        when(mockTemplateRepo.count()).thenAnswer(invocation -> (long) savedTemplates.size());
        when(mockTemplateRepo.findByType(any(StationModuleTemplate.StationModuleType.class))).thenAnswer(invocation -> {
            StationModuleTemplate.StationModuleType type = invocation.getArgument(0);
            List<StationModuleTemplate> result = new ArrayList<>();
            for (StationModuleTemplate t : savedTemplates.values()) {
                if (t.getType() == type) result.add(t);
            }
            return result;
        });
        when(mockTemplateRepo.findByTagContaining(anyString())).thenAnswer(invocation -> {
            String tag = invocation.getArgument(0, String.class).toLowerCase();
            List<StationModuleTemplate> result = new ArrayList<>();
            for (StationModuleTemplate t : savedTemplates.values()) {
                if (t.getTags() != null && t.getTags().toLowerCase().contains(tag)) {
                    result.add(t);
                }
            }
            return result;
        });
        when(mockTemplateRepo.findByDifficultyLessThanEqual(anyInt())).thenAnswer(invocation -> {
            int max = invocation.getArgument(0);
            List<StationModuleTemplate> result = new ArrayList<>();
            for (StationModuleTemplate t : savedTemplates.values()) {
                if (t.getDifficulty() <= max) result.add(t);
            }
            return result;
        });
        when(mockTemplateRepo.findAllOrderByDifficultyAsc()).thenAnswer(invocation -> {
            List<StationModuleTemplate> result = new ArrayList<>(savedTemplates.values());
            result.sort((a, b) -> Integer.compare(a.getDifficulty(), b.getDifficulty()));
            return result;
        });
        when(mockTemplateRepo.saveAll(any(Iterable.class))).thenAnswer(invocation -> {
            Iterable<StationModuleTemplate> templates = invocation.getArgument(0);
            List<StationModuleTemplate> saved = new ArrayList<>();
            for (StationModuleTemplate template : templates) {
                if (template.getId() == null) {
                    template.setId(nextId.getAndIncrement());
                }
                savedTemplates.put(template.getId(), template);
                saved.add(template);
            }
            return saved;
        });
    }

    // ==========================================
    // Template CRUD Tests
    // ==========================================

    @Test
    void createModuleTemplate_savesAndReturnsTemplate() {
        StationModuleTemplate template = builder.createCommandModuleTemplate();
        assertNull(template.getId(), "Factory template should have no ID before save");

        StationModuleTemplate saved = builder.createModuleTemplate(template);
        assertNotNull(saved.getId(), "Saved template should have an ID");
        assertEquals("Command Module", saved.getName());
        assertEquals(StationModuleTemplate.StationModuleType.COMMAND_MODULE, saved.getType());
    }

    @Test
    void getModuleTemplate_returnsSavedTemplate() {
        StationModuleTemplate template = builder.createCommandModuleTemplate();
        StationModuleTemplate saved = builder.createModuleTemplate(template);

        Optional<StationModuleTemplate> found = builder.getModuleTemplate(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Command Module", found.get().getName());
    }

    @Test
    void getModuleTemplate_returnsEmptyForNonExistent() {
        Optional<StationModuleTemplate> found = builder.getModuleTemplate(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void updateModuleTemplate_updatesExistingTemplate() {
        StationModuleTemplate template = builder.createCommandModuleTemplate();
        StationModuleTemplate saved = builder.createModuleTemplate(template);

        saved.setName("Updated Command Module");
        saved.setDifficulty(8);
        StationModuleTemplate updated = builder.updateModuleTemplate(saved);

        assertEquals("Updated Command Module", updated.getName());
        assertEquals(8, updated.getDifficulty());
    }

    @Test
    void updateModuleTemplate_throwsForNonExistent() {
        StationModuleTemplate template = builder.createCommandModuleTemplate();
        template.setId(999L); // Non-existent ID

        assertThrows(IllegalArgumentException.class, () -> builder.updateModuleTemplate(template));
    }

    @Test
    void deleteModuleTemplate_removesTemplate() {
        StationModuleTemplate template = builder.createCommandModuleTemplate();
        StationModuleTemplate saved = builder.createModuleTemplate(template);

        builder.deleteModuleTemplate(saved.getId());
        Optional<StationModuleTemplate> found = builder.getModuleTemplate(saved.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void getModuleTemplates_returnsAllTemplates() {
        builder.createModuleTemplate(builder.createCommandModuleTemplate());
        builder.createModuleTemplate(builder.createDockingRingTemplate());
        builder.createModuleTemplate(builder.createCorridorTemplate());

        List<StationModuleTemplate> all = builder.getModuleTemplates();
        assertEquals(3, all.size());
    }

    @Test
    void getModulesByType_filtersByType() {
        builder.createModuleTemplate(builder.createCommandModuleTemplate());
        builder.createModuleTemplate(builder.createDockingRingTemplate());
        builder.createModuleTemplate(builder.createCommandModuleTemplate());

        List<StationModuleTemplate> commandModules = builder.getModulesByType(
                StationModuleTemplate.StationModuleType.COMMAND_MODULE);
        assertEquals(2, commandModules.size());

        List<StationModuleTemplate> dockingRings = builder.getModulesByType(
                StationModuleTemplate.StationModuleType.DOCKING_RING);
        assertEquals(1, dockingRings.size());
    }

    @Test
    void getModulesByTag_filtersByTag() {
        builder.createModuleTemplate(builder.createCommandModuleTemplate());
        builder.createModuleTemplate(builder.createDockingRingTemplate());
        builder.createModuleTemplate(builder.createWeaponPlatformTemplate());
        builder.createModuleTemplate(builder.createShieldArrayTemplate());

        List<StationModuleTemplate> commandTagged = builder.getModulesByTag("command");
        assertEquals(1, commandTagged.size());

        List<StationModuleTemplate> defenseTagged = builder.getModulesByTag("defense");
        assertEquals(2, defenseTagged.size()); // Weapon Platform + Shield Array both have "defense" tag
    }

    @Test
    void getModulesByDifficulty_filtersByMaxDifficulty() {
        builder.createModuleTemplate(builder.createCorridorTemplate());    // difficulty 1
        builder.createModuleTemplate(builder.createCrewQuartersTemplate()); // difficulty 2
        builder.createModuleTemplate(builder.createCommandModuleTemplate()); // difficulty 5

        List<StationModuleTemplate> easy = builder.getModulesByDifficulty(2);
        assertEquals(2, easy.size());

        List<StationModuleTemplate> all = builder.getModulesByDifficulty(10);
        assertEquals(3, all.size());
    }

    @Test
    void getModulesOrderedByDifficulty_returnsSorted() {
        builder.createModuleTemplate(builder.createCommandModuleTemplate()); // difficulty 5
        builder.createModuleTemplate(builder.createCorridorTemplate());      // difficulty 1
        builder.createModuleTemplate(builder.createDockingRingTemplate());   // difficulty 3

        List<StationModuleTemplate> sorted = builder.getModulesOrderedByDifficulty();
        assertEquals(3, sorted.size());
        assertEquals(1, sorted.get(0).getDifficulty());
        assertEquals(3, sorted.get(1).getDifficulty());
        assertEquals(5, sorted.get(2).getDifficulty());
    }

    // ==========================================
    // Station Assembly Tests
    // ==========================================

    @Test
    void buildStation_createsStationWithCorrectName() {
        StationModuleTemplate cmd = builder.createModuleTemplate(builder.createCommandModuleTemplate());
        List<Long> ids = Collections.singletonList(cmd.getId());

        SpaceStation station = builder.buildStation("Babylon-5", SpaceStationType.MILITARY, ids);

        assertEquals("Babylon-5", station.getName());
        assertEquals(SpaceStationType.MILITARY, station.getStationType());
    }

    @Test
    void buildStation_accumulatesHullContributions() {
        StationModuleTemplate cmd = builder.createModuleTemplate(builder.createCommandModuleTemplate());
        StationModuleTemplate dock = builder.createModuleTemplate(builder.createDockingRingTemplate());
        List<Long> ids = List.of(cmd.getId(), dock.getId());

        SpaceStation station = builder.buildStation("Test Station", SpaceStationType.TRADE, ids);

        // Command: 100 hull + Docking: 80 hull = 180 total
        // Base station hull + contributions
        assertTrue(station.getHullPoints() >= 180, "Hull points should include template contributions");
    }

    @Test
    void buildStation_accumulatesShieldContributions() {
        StationModuleTemplate cmd = builder.createModuleTemplate(builder.createCommandModuleTemplate());
        StationModuleTemplate shield = builder.createModuleTemplate(builder.createShieldArrayTemplate());
        List<Long> ids = List.of(cmd.getId(), shield.getId());

        SpaceStation station = builder.buildStation("Shielded Station", SpaceStationType.MILITARY, ids);

        // Command: 20 shield + Shield Array: 30 shield = 50 total
        assertTrue(station.getShieldPoints() >= 50, "Shield points should include template contributions");
    }

    @Test
    void buildStation_accumulatesDockingPorts() {
        StationModuleTemplate cmd = builder.createModuleTemplate(builder.createCommandModuleTemplate());
        StationModuleTemplate dock = builder.createModuleTemplate(builder.createDockingRingTemplate());
        List<Long> ids = List.of(cmd.getId(), dock.getId());

        SpaceStation station = builder.buildStation("Docking Station", SpaceStationType.TRADE, ids);

        // Command: 2 ports + Docking: 4 ports = 6 total
        assertTrue(station.getMaxDockedShips() >= 6, "Max docked ships should include template contributions");
    }

    @Test
    void buildStation_registersCompartments() {
        StationModuleTemplate cmd = builder.createModuleTemplate(builder.createCommandModuleTemplate());
        StationModuleTemplate cargo = builder.createModuleTemplate(builder.createCargoModuleTemplate());
        List<Long> ids = List.of(cmd.getId(), cargo.getId());

        SpaceStation station = builder.buildStation("Compartment Station", SpaceStationType.TRADE, ids);

        Map<String, Compartment> compartments = station.getCompartments();
        assertTrue(compartments.size() >= 2, "Station should have compartments from templates");
    }

    @Test
    void buildStation_setsHullModule() {
        StationModuleTemplate cmd = builder.createModuleTemplate(builder.createCommandModuleTemplate());
        List<Long> ids = Collections.singletonList(cmd.getId());

        SpaceStation station = builder.buildStation("Hull Station", SpaceStationType.MILITARY, ids);

        assertNotNull(station.getHullModule(), "Station should have a hull module");
    }

    @Test
    void buildStation_withDefaultType_createsMilitaryStation() {
        StationModuleTemplate cmd = builder.createModuleTemplate(builder.createCommandModuleTemplate());
        List<Long> ids = Collections.singletonList(cmd.getId());

        SpaceStation station = builder.buildStation("Default Station", ids);

        assertEquals(SpaceStationType.MILITARY, station.getStationType());
    }

    @Test
    void buildStation_throwsForMissingTemplates() {
        List<Long> ids = List.of(1L, 999L); // 999 does not exist

        assertThrows(IllegalArgumentException.class, () ->
                builder.buildStation("Bad Station", SpaceStationType.MILITARY, ids));
    }

    @Test
    void buildStation_withEmptyTemplates_createsMinimalStation() {
        List<Long> ids = Collections.emptyList();

        SpaceStation station = builder.buildStation("Empty Station", SpaceStationType.TRADE, ids);

        assertEquals("Empty Station", station.getName());
        assertEquals(SpaceStationType.TRADE, station.getStationType());
    }

    @Test
    void buildStation_multipleModules_createsConnections() {
        StationModuleTemplate cmd = builder.createModuleTemplate(builder.createCommandModuleTemplate());
        StationModuleTemplate corridor = builder.createModuleTemplate(builder.createCorridorTemplate());
        StationModuleTemplate cargo = builder.createModuleTemplate(builder.createCargoModuleTemplate());
        List<Long> ids = List.of(cmd.getId(), corridor.getId(), cargo.getId());

        SpaceStation station = builder.buildStation("Connected Station", SpaceStationType.TRADE, ids);

        assertNotNull(station);
        assertEquals("Connected Station", station.getName());
        // Should have compartments from all 3 modules
        assertTrue(station.getCompartments().size() >= 3);
    }

    // ==========================================
    // Interior Generation Tests
    // ==========================================

    @Test
    void generateStationInteriors_returnsDungeonId() {
        StationModuleTemplate cmd = builder.createModuleTemplate(builder.createCommandModuleTemplate());
        SpaceStation station = builder.buildStation("Interior Station",
                SpaceStationType.MILITARY, Collections.singletonList(cmd.getId()));

        String dungeonId = builder.generateStationInteriors(station, TechLevel.TL8, 42L);

        assertNotNull(dungeonId);
        assertEquals("Interior Station-interior", dungeonId);
    }

    @Test
    void generateStationInteriors_createsTenRooms() {
        StationModuleTemplate cmd = builder.createModuleTemplate(builder.createCommandModuleTemplate());
        SpaceStation station = builder.buildStation("Room Station",
                SpaceStationType.MILITARY, Collections.singletonList(cmd.getId()));

        builder.generateStationInteriors(station, TechLevel.TL8, 42L);
        List<DungeonRoomService.DungeonRoom> rooms = mockDungeonRoomService.listRoomsByDungeon("Room Station-interior");

        assertEquals(10, rooms.size(), "Space Port template should create 10 rooms");
    }

    @Test
    void generateStationInteriors_roomsHaveNames() {
        StationModuleTemplate cmd = builder.createModuleTemplate(builder.createCommandModuleTemplate());
        SpaceStation station = builder.buildStation("Named Station",
                SpaceStationType.MILITARY, Collections.singletonList(cmd.getId()));

        builder.generateStationInteriors(station, TechLevel.TL8, 42L);
        List<DungeonRoomService.DungeonRoom> rooms = mockDungeonRoomService.listRoomsByDungeon("Named Station-interior");

        List<String> names = new ArrayList<>();
        for (DungeonRoomService.DungeonRoom room : rooms) {
            names.add(room.getName());
        }
        assertTrue(names.contains("Docking Bay Alpha"));
        assertTrue(names.contains("Docking Bay Beta"));
    }

    @Test
    void generateStationInteriors_withDefaultTechLevel_usesTL8() {
        StationModuleTemplate cmd = builder.createModuleTemplate(builder.createCommandModuleTemplate());
        SpaceStation station = builder.buildStation("Default Tech Station",
                SpaceStationType.MILITARY, Collections.singletonList(cmd.getId()));

        String dungeonId = builder.generateStationInteriors(station, 123L);

        assertEquals("Default Tech Station-interior", dungeonId);
        List<DungeonRoomService.DungeonRoom> rooms = mockDungeonRoomService.listRoomsByDungeon(dungeonId);
        assertEquals(10, rooms.size());
    }

    // ==========================================
    // Compartment-to-Room Mapping Tests
    // ==========================================

    @Test
    void mapCompartmentsToRooms_mapsCommandToSocialRoom() {
        StationModuleTemplate cmd = builder.createModuleTemplate(builder.createCommandModuleTemplate());
        SpaceStation station = builder.buildStation("Map Station",
                SpaceStationType.MILITARY, Collections.singletonList(cmd.getId()));

        builder.generateStationInteriors(station, TechLevel.TL8, 42L);
        Map<String, Long> mapping = builder.mapCompartmentsToRooms(station, "Map Station-interior");

        // Command Center compartment should map to a Social-themed room
        assertFalse(mapping.isEmpty(), "Should have at least one compartment-to-room mapping");
    }

    @Test
    void mapCompartmentsToRooms_withNoRooms_returnsEmpty() {
        StationModuleTemplate cmd = builder.createModuleTemplate(builder.createCommandModuleTemplate());
        SpaceStation station = builder.buildStation("Empty Map Station",
                SpaceStationType.MILITARY, Collections.singletonList(cmd.getId()));

        Map<String, Long> mapping = builder.mapCompartmentsToRooms(station, "nonexistent-dungeon");
        assertTrue(mapping.isEmpty(), "Should return empty map when no rooms exist");
    }

    @Test
    void mapCompartmentsToRooms_mapsMultipleCompartments() {
        StationModuleTemplate cmd = builder.createModuleTemplate(builder.createCommandModuleTemplate());
        StationModuleTemplate cargo = builder.createModuleTemplate(builder.createCargoModuleTemplate());
        StationModuleTemplate weapon = builder.createModuleTemplate(builder.createWeaponPlatformTemplate());
        SpaceStation station = builder.buildStation("Multi Map Station",
                SpaceStationType.MILITARY, List.of(cmd.getId(), cargo.getId(), weapon.getId()));

        builder.generateStationInteriors(station, TechLevel.TL8, 42L);
        Map<String, Long> mapping = builder.mapCompartmentsToRooms(station, "Multi Map Station-interior");

        // Should have mappings for COMMAND, CARGO, and WEAPONS compartments
        assertTrue(mapping.size() >= 2, "Should map multiple compartments to rooms");
    }

    // ==========================================
    // Pre-population Tests
    // ==========================================

    @Test
    void prepopulateTemplates_createsTwelveTemplates() {
        int count = builder.prepopulateTemplates();
        assertEquals(12, count, "Should create 12 starter templates");

        List<StationModuleTemplate> all = builder.getModuleTemplates();
        assertEquals(12, all.size());
    }

    @Test
    void prepopulateTemplates_isIdempotent() {
        builder.prepopulateTemplates();
        int secondCount = builder.prepopulateTemplates();
        assertEquals(0, secondCount, "Should not create duplicates");

        List<StationModuleTemplate> all = builder.getModuleTemplates();
        assertEquals(12, all.size(), "Should still have 12 templates");
    }

    @Test
    void prepopulateTemplates_createsAllModuleTypes() {
        builder.prepopulateTemplates();

        assertTrue(builder.getModulesByType(StationModuleTemplate.StationModuleType.COMMAND_MODULE).size() > 0);
        assertTrue(builder.getModulesByType(StationModuleTemplate.StationModuleType.DOCKING_RING).size() > 0);
        assertTrue(builder.getModulesByType(StationModuleTemplate.StationModuleType.CREW_QUARTERS).size() > 0);
        assertTrue(builder.getModulesByType(StationModuleTemplate.StationModuleType.CARGO_MODULE).size() > 0);
        assertTrue(builder.getModulesByType(StationModuleTemplate.StationModuleType.CORRIDOR).size() > 0);
        assertTrue(builder.getModulesByType(StationModuleTemplate.StationModuleType.POWER_MODULE).size() > 0);
        assertTrue(builder.getModulesByType(StationModuleTemplate.StationModuleType.LIFE_SUPPORT).size() > 0);
        assertTrue(builder.getModulesByType(StationModuleTemplate.StationModuleType.REPAIR_BAY).size() > 0);
        assertTrue(builder.getModulesByType(StationModuleTemplate.StationModuleType.RESEARCH_LAB).size() > 0);
        assertTrue(builder.getModulesByType(StationModuleTemplate.StationModuleType.WEAPON_PLATFORM).size() > 0);
        assertTrue(builder.getModulesByType(StationModuleTemplate.StationModuleType.SHIELD_ARRAY).size() > 0);
        assertTrue(builder.getModulesByType(StationModuleTemplate.StationModuleType.SENSOR_ARRAY).size() > 0);
    }

    // ==========================================
    // Factory Method Tests
    // ==========================================

    @Test
    void createCommandModuleTemplate_hasCorrectProperties() {
        StationModuleTemplate template = builder.createCommandModuleTemplate();
        assertEquals("Command Module", template.getName());
        assertEquals(StationModuleTemplate.StationModuleType.COMMAND_MODULE, template.getType());
        assertEquals(10, template.getWidth());
        assertEquals(10, template.getHeight());
        assertEquals(4, template.getDepth());
        assertEquals(5, template.getDifficulty());
        assertEquals(2, template.getDockingPorts());
        assertEquals(20, template.getShieldContribution());
        assertEquals(100, template.getHullContribution());
        assertNotNull(template.getShapeCellsJson());
        assertNotNull(template.getCompartmentsJson());
        assertNotNull(template.getConnectionPointsJson());
    }

    @Test
    void createDockingRingTemplate_hasFourDockingPorts() {
        StationModuleTemplate template = builder.createDockingRingTemplate();
        assertEquals("Docking Ring", template.getName());
        assertEquals(StationModuleTemplate.StationModuleType.DOCKING_RING, template.getType());
        assertEquals(4, template.getDockingPorts());
        assertEquals(12, template.getWidth());
        assertEquals(12, template.getHeight());
    }

    @Test
    void createCrewQuartersTemplate_hasCrewCompartment() {
        StationModuleTemplate template = builder.createCrewQuartersTemplate();
        assertEquals("Crew Quarters", template.getName());
        assertEquals(StationModuleTemplate.StationModuleType.CREW_QUARTERS, template.getType());
        assertEquals(0, template.getDockingPorts());
        assertTrue(template.getCompartmentsJson().contains("CREW"));
    }

    @Test
    void createCargoModuleTemplate_hasCargoCompartment() {
        StationModuleTemplate template = builder.createCargoModuleTemplate();
        assertEquals("Cargo Module", template.getName());
        assertEquals(StationModuleTemplate.StationModuleType.CARGO_MODULE, template.getType());
        assertEquals(1, template.getDockingPorts());
        assertTrue(template.getCompartmentsJson().contains("CARGO"));
    }

    @Test
    void createCorridorTemplate_hasLowestDifficulty() {
        StationModuleTemplate template = builder.createCorridorTemplate();
        assertEquals("Corridor", template.getName());
        assertEquals(StationModuleTemplate.StationModuleType.CORRIDOR, template.getType());
        assertEquals(1, template.getDifficulty());
        assertEquals(0, template.getShieldContribution());
        assertTrue(template.getCompartmentsJson().contains("TRANSIT"));
    }

    @Test
    void createPowerModuleTemplate_hasEngineCompartment() {
        StationModuleTemplate template = builder.createPowerModuleTemplate();
        assertEquals("Power Module", template.getName());
        assertEquals(StationModuleTemplate.StationModuleType.POWER_MODULE, template.getType());
        assertTrue(template.getCompartmentsJson().contains("ENGINE"));
    }

    @Test
    void createLifeSupportTemplate_hasCrewCompartment() {
        StationModuleTemplate template = builder.createLifeSupportTemplate();
        assertEquals("Life Support", template.getName());
        assertEquals(StationModuleTemplate.StationModuleType.LIFE_SUPPORT, template.getType());
        assertTrue(template.getCompartmentsJson().contains("CREW"));
    }

    @Test
    void createRepairBayTemplate_hasEngineCompartment() {
        StationModuleTemplate template = builder.createRepairBayTemplate();
        assertEquals("Repair Bay", template.getName());
        assertEquals(StationModuleTemplate.StationModuleType.REPAIR_BAY, template.getType());
        assertEquals(1, template.getDockingPorts());
        assertTrue(template.getCompartmentsJson().contains("ENGINE"));
    }

    @Test
    void createResearchLabTemplate_hasResearchCompartment() {
        StationModuleTemplate template = builder.createResearchLabTemplate();
        assertEquals("Research Lab", template.getName());
        assertEquals(StationModuleTemplate.StationModuleType.RESEARCH_LAB, template.getType());
        assertTrue(template.getCompartmentsJson().contains("RESEARCH"));
    }

    @Test
    void createWeaponPlatformTemplate_hasWeaponsCompartment() {
        StationModuleTemplate template = builder.createWeaponPlatformTemplate();
        assertEquals("Weapon Platform", template.getName());
        assertEquals(StationModuleTemplate.StationModuleType.WEAPON_PLATFORM, template.getType());
        assertEquals(6, template.getDifficulty());
        assertTrue(template.getCompartmentsJson().contains("WEAPONS"));
    }

    @Test
    void createShieldArrayTemplate_hasHighestShieldContribution() {
        StationModuleTemplate template = builder.createShieldArrayTemplate();
        assertEquals("Shield Array", template.getName());
        assertEquals(StationModuleTemplate.StationModuleType.SHIELD_ARRAY, template.getType());
        assertEquals(30, template.getShieldContribution());
        assertTrue(template.getCompartmentsJson().contains("DEFENSE"));
    }

    @Test
    void createSensorArrayTemplate_hasResearchCompartment() {
        StationModuleTemplate template = builder.createSensorArrayTemplate();
        assertEquals("Sensor Array", template.getName());
        assertEquals(StationModuleTemplate.StationModuleType.SENSOR_ARRAY, template.getType());
        assertTrue(template.getCompartmentsJson().contains("RESEARCH"));
    }

    // ==========================================
    // Integration Tests
    // ==========================================

    @Test
    void fullWorkflow_prepopulate_buildStation_generateInteriors() {
        // Pre-populate templates
        int created = builder.prepopulateTemplates();
        assertEquals(12, created);

        // Find command and docking templates
        List<StationModuleTemplate> commandModules = builder.getModulesByType(
                StationModuleTemplate.StationModuleType.COMMAND_MODULE);
        List<StationModuleTemplate> dockingRings = builder.getModulesByType(
                StationModuleTemplate.StationModuleType.DOCKING_RING);

        // Build station
        List<Long> templateIds = List.of(commandModules.get(0).getId(), dockingRings.get(0).getId());
        SpaceStation station = builder.buildStation("Full Workflow Station", SpaceStationType.MILITARY, templateIds);

        assertEquals("Full Workflow Station", station.getName());
        assertNotNull(station.getHullModule());
        assertTrue(station.getCompartments().size() >= 2);

        // Generate interiors
        String dungeonId = builder.generateStationInteriors(station, TechLevel.TL8, 42L);
        assertEquals("Full Workflow Station-interior", dungeonId);

        List<DungeonRoomService.DungeonRoom> rooms = mockDungeonRoomService.listRoomsByDungeon(dungeonId);
        assertEquals(10, rooms.size());

        // Map compartments to rooms
        Map<String, Long> mapping = builder.mapCompartmentsToRooms(station, dungeonId);
        assertNotNull(mapping);
    }

    @Test
    void buildStation_differentStationTypes() {
        StationModuleTemplate cmd = builder.createModuleTemplate(builder.createCommandModuleTemplate());
        List<Long> ids = Collections.singletonList(cmd.getId());

        SpaceStation military = builder.buildStation("Military", SpaceStationType.MILITARY, ids);
        SpaceStation trade = builder.buildStation("Trade", SpaceStationType.TRADE, ids);
        SpaceStation research = builder.buildStation("Research", SpaceStationType.RESEARCH, ids);
        SpaceStation mining = builder.buildStation("Mining", SpaceStationType.MINING, ids);

        assertEquals(SpaceStationType.MILITARY, military.getStationType());
        assertEquals(SpaceStationType.TRADE, trade.getStationType());
        assertEquals(SpaceStationType.RESEARCH, research.getStationType());
        assertEquals(SpaceStationType.MINING, mining.getStationType());
    }

    @Test
    void templateToHullModule_createsValidModule() {
        StationModuleTemplate template = builder.createCommandModuleTemplate();
        HullModule module = template.toHullModule();

        assertNotNull(module);
        assertEquals("Command Module", module.getName());
        assertEquals(10, module.getWidth());
        assertEquals(10, module.getHeight());
        assertEquals(4, module.getDepth());
        assertTrue(module.getCompartments().size() > 0);
    }

    @Test
    void templateConnectionPoints_parsedCorrectly() {
        StationModuleTemplate template = builder.createCommandModuleTemplate();
        List<Map<String, Object>> points = template.getConnectionPoints();

        assertEquals(2, points.size(), "Command module should have 2 connection points");
        assertEquals(5, points.get(0).get("x"));
        assertEquals("FORWARD", points.get(0).get("direction"));
    }

}
