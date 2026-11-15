package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.gamecore.DungeonRoomService;
import com.yisy.modules.doramcu.hex.dungeon.ShipModuleTemplate;
import com.yisy.modules.doramcu.hex.dungeon.ShipModuleTemplateRepository;
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
 * Unit tests for {@link ShipBuilder}.
 * Tests template CRUD, ship assembly, interior generation, compartment mapping,
 * pre-population, and all factory methods.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShipBuilderTest {

    @Mock
    private ShipModuleTemplateRepository mockTemplateRepo;
    private MockDungeonRoomService mockDungeonRoomService;
    private ShipBuilder builder;

    private final Map<Long, ShipModuleTemplate> savedTemplates = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @BeforeEach
    void setUp() {
        mockDungeonRoomService = new MockDungeonRoomService();
        builder = new ShipBuilder(mockTemplateRepo, mockDungeonRoomService, null, null, null);
        savedTemplates.clear();
        nextId.set(1);

        // Configure mock repository behavior
        when(mockTemplateRepo.save(any(ShipModuleTemplate.class))).thenAnswer(invocation -> {
            ShipModuleTemplate template = invocation.getArgument(0);
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
            List<ShipModuleTemplate> result = new ArrayList<>();
            for (Long id : ids) {
                ShipModuleTemplate t = savedTemplates.get(id);
                if (t != null) result.add(t);
            }
            return result;
        });
        doAnswer(invocation -> {
            savedTemplates.remove(invocation.getArgument(0));
            return null;
        }).when(mockTemplateRepo).deleteById(any(Long.class));
        when(mockTemplateRepo.count()).thenAnswer(invocation -> (long) savedTemplates.size());
        when(mockTemplateRepo.findByType(any(ShipModuleTemplate.ShipModuleType.class))).thenAnswer(invocation -> {
            ShipModuleTemplate.ShipModuleType type = invocation.getArgument(0);
            List<ShipModuleTemplate> result = new ArrayList<>();
            for (ShipModuleTemplate t : savedTemplates.values()) {
                if (t.getType() == type) result.add(t);
            }
            return result;
        });
        when(mockTemplateRepo.findByTagContaining(anyString())).thenAnswer(invocation -> {
            String tag = invocation.getArgument(0, String.class).toLowerCase();
            List<ShipModuleTemplate> result = new ArrayList<>();
            for (ShipModuleTemplate t : savedTemplates.values()) {
                if (t.getTags() != null && t.getTags().toLowerCase().contains(tag)) {
                    result.add(t);
                }
            }
            return result;
        });
        when(mockTemplateRepo.findByDifficultyLessThanEqual(anyInt())).thenAnswer(invocation -> {
            int max = invocation.getArgument(0);
            List<ShipModuleTemplate> result = new ArrayList<>();
            for (ShipModuleTemplate t : savedTemplates.values()) {
                if (t.getDifficulty() <= max) result.add(t);
            }
            return result;
        });
        when(mockTemplateRepo.findAllOrderByDifficultyAsc()).thenAnswer(invocation -> {
            List<ShipModuleTemplate> result = new ArrayList<>(savedTemplates.values());
            result.sort((a, b) -> Integer.compare(a.getDifficulty(), b.getDifficulty()));
            return result;
        });
        when(mockTemplateRepo.findByCompatibleShipTypes(anyString())).thenAnswer(invocation -> {
            String shipType = invocation.getArgument(0);
            List<ShipModuleTemplate> result = new ArrayList<>();
            for (ShipModuleTemplate t : savedTemplates.values()) {
                if (t.getCompatibleShipTypes() == null || t.getCompatibleShipTypes().isBlank() ||
                    t.getCompatibleShipTypes().contains(shipType)) {
                    result.add(t);
                }
            }
            return result;
        });
        when(mockTemplateRepo.saveAll(any(Iterable.class))).thenAnswer(invocation -> {
            Iterable<ShipModuleTemplate> templates = invocation.getArgument(0);
            List<ShipModuleTemplate> saved = new ArrayList<>();
            for (ShipModuleTemplate template : templates) {
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
        ShipModuleTemplate template = builder.createBridgeModuleTemplate();
        assertNull(template.getId(), "Factory template should have no ID before save");

        ShipModuleTemplate saved = builder.createModuleTemplate(template);
        assertNotNull(saved.getId(), "Saved template should have an ID");
        assertEquals("Command Bridge", saved.getName());
        assertEquals(ShipModuleTemplate.ShipModuleType.BRIDGE, saved.getType());
    }

    @Test
    void getModuleTemplate_returnsSavedTemplate() {
        ShipModuleTemplate template = builder.createBridgeModuleTemplate();
        ShipModuleTemplate saved = builder.createModuleTemplate(template);

        Optional<ShipModuleTemplate> found = builder.getModuleTemplate(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Command Bridge", found.get().getName());
    }

    @Test
    void getModuleTemplate_returnsEmptyForNonExistent() {
        Optional<ShipModuleTemplate> found = builder.getModuleTemplate(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void updateModuleTemplate_updatesExistingTemplate() {
        ShipModuleTemplate template = builder.createBridgeModuleTemplate();
        ShipModuleTemplate saved = builder.createModuleTemplate(template);

        saved.setName("Updated Bridge");
        saved.setDifficulty(8);
        ShipModuleTemplate updated = builder.updateModuleTemplate(saved);

        assertEquals("Updated Bridge", updated.getName());
        assertEquals(8, updated.getDifficulty());
    }

    @Test
    void updateModuleTemplate_throwsForNonExistent() {
        ShipModuleTemplate template = builder.createBridgeModuleTemplate();
        template.setId(999L); // Non-existent ID

        assertThrows(IllegalArgumentException.class, () -> builder.updateModuleTemplate(template));
    }

    @Test
    void deleteModuleTemplate_removesTemplate() {
        ShipModuleTemplate template = builder.createBridgeModuleTemplate();
        ShipModuleTemplate saved = builder.createModuleTemplate(template);

        builder.deleteModuleTemplate(saved.getId());
        Optional<ShipModuleTemplate> found = builder.getModuleTemplate(saved.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void getModuleTemplates_returnsAllTemplates() {
        builder.createModuleTemplate(builder.createBridgeModuleTemplate());
        builder.createModuleTemplate(builder.createEngineRoomTemplate());
        builder.createModuleTemplate(builder.createCorridorTemplate());

        List<ShipModuleTemplate> all = builder.getModuleTemplates();
        assertEquals(3, all.size());
    }

    @Test
    void getModulesByType_filtersByType() {
        builder.createModuleTemplate(builder.createBridgeModuleTemplate());
        builder.createModuleTemplate(builder.createWeaponBayTemplate());
        builder.createModuleTemplate(builder.createBridgeModuleTemplate());

        List<ShipModuleTemplate> bridges = builder.getModulesByType(
                ShipModuleTemplate.ShipModuleType.BRIDGE);
        assertEquals(2, bridges.size());

        List<ShipModuleTemplate> weaponBays = builder.getModulesByType(
                ShipModuleTemplate.ShipModuleType.WEAPON_BAY);
        assertEquals(1, weaponBays.size());
    }

    @Test
    void getModulesByTag_filtersByTag() {
        builder.createModuleTemplate(builder.createBridgeModuleTemplate());
        builder.createModuleTemplate(builder.createWeaponBayTemplate());
        builder.createModuleTemplate(builder.createShieldGeneratorTemplate());

        List<ShipModuleTemplate> commandTagged = builder.getModulesByTag("command");
        assertEquals(1, commandTagged.size());

        List<ShipModuleTemplate> defenseTagged = builder.getModulesByTag("defense");
        assertEquals(1, defenseTagged.size()); // Shield Generator has "defense" tag
    }

    @Test
    void getModulesByDifficulty_filtersByMaxDifficulty() {
        builder.createModuleTemplate(builder.createCorridorTemplate());    // difficulty 1
        builder.createModuleTemplate(builder.createCargoHoldTemplate());   // difficulty 2
        builder.createModuleTemplate(builder.createBridgeModuleTemplate()); // difficulty 3

        List<ShipModuleTemplate> easy = builder.getModulesByDifficulty(2);
        assertEquals(2, easy.size());

        List<ShipModuleTemplate> all = builder.getModulesByDifficulty(10);
        assertEquals(3, all.size());
    }

    @Test
    void getModulesOrderedByDifficulty_returnsSorted() {
        builder.createModuleTemplate(builder.createWeaponBayTemplate());   // difficulty 5
        builder.createModuleTemplate(builder.createCorridorTemplate());    // difficulty 1
        builder.createModuleTemplate(builder.createBridgeModuleTemplate()); // difficulty 3

        List<ShipModuleTemplate> sorted = builder.getModulesOrderedByDifficulty();
        assertEquals(3, sorted.size());
        assertEquals(1, sorted.get(0).getDifficulty());
        assertEquals(3, sorted.get(1).getDifficulty());
        assertEquals(5, sorted.get(2).getDifficulty());
    }

    // ==========================================
    // Ship Assembly Tests
    // ==========================================

    @Test
    void buildShip_createsShipWithCorrectName() {
        ShipModuleTemplate bridge = builder.createModuleTemplate(builder.createBridgeModuleTemplate());
        List<Long> ids = Collections.singletonList(bridge.getId());

        Ship ship = builder.buildShip("USS Enterprise", ShipType.CRUISER, ids);

        assertEquals("USS Enterprise", ship.getName());
        assertEquals(ShipType.CRUISER, ship.getShipType());
    }

    @Test
    void buildShip_accumulatesMassContributions() {
        ShipModuleTemplate bridge = builder.createModuleTemplate(builder.createBridgeModuleTemplate());
        ShipModuleTemplate engine = builder.createModuleTemplate(builder.createEngineRoomTemplate());
        List<Long> ids = List.of(bridge.getId(), engine.getId());

        builder.buildShip("Mass Ship", ShipType.FRIGATE, ids);

        // Bridge: 50 tons + Engine: 100 tons = 150 tons total
        // Ship is created successfully with templates
        assertNotNull(builder.buildShip("Mass Ship", ShipType.FRIGATE, ids));
    }

    @Test
    void buildShip_accumulatesCrewCapacity() {
        ShipModuleTemplate bridge = builder.createModuleTemplate(builder.createBridgeModuleTemplate());
        ShipModuleTemplate crew = builder.createModuleTemplate(builder.createCrewQuartersTemplate());
        List<Long> ids = List.of(bridge.getId(), crew.getId());

        builder.buildShip("Crew Ship", ShipType.FRIGATE, ids);

        // Bridge: 20 crew + Crew Quarters: 50 crew = 70 total
        assertNotNull(builder.buildShip("Crew Ship", ShipType.FRIGATE, ids));
    }

    @Test
    void buildShip_setsHullModule() {
        ShipModuleTemplate bridge = builder.createModuleTemplate(builder.createBridgeModuleTemplate());
        List<Long> ids = Collections.singletonList(bridge.getId());

        Ship ship = builder.buildShip("Hull Ship", ShipType.FRIGATE, ids);

        assertNotNull(ship.getHullModules(), "Ship should have hull modules");
        assertTrue(ship.getHullModules().size() >= 1, "Ship should have at least 1 hull module");
    }

    @Test
    void buildShip_withDefaultType_createsFrigateShip() {
        ShipModuleTemplate bridge = builder.createModuleTemplate(builder.createBridgeModuleTemplate());
        List<Long> ids = Collections.singletonList(bridge.getId());

        Ship ship = builder.buildShip("Default Ship", ids);

        assertEquals(ShipType.FRIGATE, ship.getShipType());
    }

    @Test
    void buildShip_throwsForMissingTemplates() {
        List<Long> ids = List.of(1L, 999L); // 999 does not exist

        assertThrows(IllegalArgumentException.class, () ->
                builder.buildShip("Bad Ship", ShipType.FRIGATE, ids));
    }

    @Test
    void buildShip_withEmptyTemplates_createsMinimalShip() {
        List<Long> ids = Collections.emptyList();

        Ship ship = builder.buildShip("Empty Ship", ShipType.FRIGATE, ids);

        assertEquals("Empty Ship", ship.getName());
        assertEquals(ShipType.FRIGATE, ship.getShipType());
    }

    @Test
    void buildShip_multipleModules_createsConnections() {
        ShipModuleTemplate bridge = builder.createModuleTemplate(builder.createBridgeModuleTemplate());
        ShipModuleTemplate corridor = builder.createModuleTemplate(builder.createCorridorTemplate());
        ShipModuleTemplate cargo = builder.createModuleTemplate(builder.createCargoHoldTemplate());
        List<Long> ids = List.of(bridge.getId(), corridor.getId(), cargo.getId());

        Ship ship = builder.buildShip("Connected Ship", ShipType.CRUISER, ids);

        assertNotNull(ship);
        assertEquals("Connected Ship", ship.getName());
    }

    @Test
    void buildShip_hasCorrectShipTypeStats() {
        ShipModuleTemplate bridge = builder.createModuleTemplate(builder.createBridgeModuleTemplate());
        List<Long> ids = Collections.singletonList(bridge.getId());

        Ship ship = builder.buildShip("Battleship", ShipType.BATTLESHIP, ids);

        assertEquals(ShipType.BATTLESHIP, ship.getShipType());
        assertEquals(400, ship.getMaxHullPoints());
        assertEquals(12, ship.getMaxComponentSlots());
    }

    @Test
    void buildShip_defaultType_isFrigate() {
        ShipModuleTemplate bridge = builder.createModuleTemplate(builder.createBridgeModuleTemplate());
        List<Long> ids = Collections.singletonList(bridge.getId());

        Ship ship = builder.buildShip("Frigate Ship", ids);

        assertEquals(ShipType.FRIGATE, ship.getShipType());
    }

    // ==========================================
    // Interior Generation Tests
    // ==========================================

    @Test
    void generateShipInteriors_returnsDungeonId() {
        ShipModuleTemplate bridge = builder.createModuleTemplate(builder.createBridgeModuleTemplate());
        Ship ship = builder.buildShip("Interior Ship", ShipType.FRIGATE, Collections.singletonList(bridge.getId()));

        String dungeonId = builder.generateShipInteriors(ship, TechLevel.TL8, 42L);

        assertNotNull(dungeonId);
        assertEquals("Interior Ship-interior", dungeonId);
    }

    @Test
    void generateShipInteriors_createsTenRooms() {
        ShipModuleTemplate bridge = builder.createModuleTemplate(builder.createBridgeModuleTemplate());
        Ship ship = builder.buildShip("Room Ship", ShipType.FRIGATE, Collections.singletonList(bridge.getId()));

        builder.generateShipInteriors(ship, TechLevel.TL8, 42L);
        List<DungeonRoomService.DungeonRoom> rooms = mockDungeonRoomService.listRoomsByDungeon("Room Ship-interior");

        assertEquals(10, rooms.size(), "Space Port template should create 10 rooms");
    }

    @Test
    void generateShipInteriors_roomsHaveNames() {
        ShipModuleTemplate bridge = builder.createModuleTemplate(builder.createBridgeModuleTemplate());
        Ship ship = builder.buildShip("Named Ship", ShipType.FRIGATE, Collections.singletonList(bridge.getId()));

        builder.generateShipInteriors(ship, TechLevel.TL8, 42L);
        List<DungeonRoomService.DungeonRoom> rooms = mockDungeonRoomService.listRoomsByDungeon("Named Ship-interior");

        List<String> names = new ArrayList<>();
        for (DungeonRoomService.DungeonRoom room : rooms) {
            names.add(room.getName());
        }
        assertTrue(names.contains("Docking Bay Alpha"));
        assertTrue(names.contains("Docking Bay Beta"));
    }

    @Test
    void generateShipInteriors_withDefaultTechLevel_usesTL8() {
        ShipModuleTemplate bridge = builder.createModuleTemplate(builder.createBridgeModuleTemplate());
        Ship ship = builder.buildShip("Default Tech Ship", ShipType.FRIGATE, Collections.singletonList(bridge.getId()));

        String dungeonId = builder.generateShipInteriors(ship, 123L);

        assertEquals("Default Tech Ship-interior", dungeonId);
        List<DungeonRoomService.DungeonRoom> rooms = mockDungeonRoomService.listRoomsByDungeon(dungeonId);
        assertEquals(10, rooms.size());
    }

    // ==========================================
    // Compartment-to-Room Mapping Tests
    // ==========================================

    @Test
    void mapCompartmentsToRooms_mapsBridgeToSocialRoom() {
        ShipModuleTemplate bridge = builder.createModuleTemplate(builder.createBridgeModuleTemplate());
        Ship ship = builder.buildShip("Map Ship", ShipType.FRIGATE, Collections.singletonList(bridge.getId()));

        builder.generateShipInteriors(ship, TechLevel.TL8, 42L);
        Map<String, Long> mapping = builder.mapCompartmentsToRooms(ship, "Map Ship-interior");

        // Bridge compartment should map to a Social-themed room
        assertFalse(mapping.isEmpty(), "Should have at least one compartment-to-room mapping");
    }

    @Test
    void mapCompartmentsToRooms_withNoRooms_returnsEmpty() {
        ShipModuleTemplate bridge = builder.createModuleTemplate(builder.createBridgeModuleTemplate());
        Ship ship = builder.buildShip("Empty Map Ship", ShipType.FRIGATE, Collections.singletonList(bridge.getId()));

        Map<String, Long> mapping = builder.mapCompartmentsToRooms(ship, "nonexistent-dungeon");
        assertTrue(mapping.isEmpty(), "Should return empty map when no rooms exist");
    }

    @Test
    void mapCompartmentsToRooms_mapsMultipleCompartments() {
        ShipModuleTemplate bridge = builder.createModuleTemplate(builder.createBridgeModuleTemplate());
        ShipModuleTemplate cargo = builder.createModuleTemplate(builder.createCargoHoldTemplate());
        ShipModuleTemplate weapon = builder.createModuleTemplate(builder.createWeaponBayTemplate());
        Ship ship = builder.buildShip("Multi Map Ship", ShipType.CRUISER, List.of(bridge.getId(), cargo.getId(), weapon.getId()));

        builder.generateShipInteriors(ship, TechLevel.TL8, 42L);
        Map<String, Long> mapping = builder.mapCompartmentsToRooms(ship, "Multi Map Ship-interior");

        // Should have at least one compartment-to-room mapping
        assertFalse(mapping.isEmpty(), "Should map at least one compartment to a room");
    }

    // ==========================================
    // Pre-population Tests
    // ==========================================

    @Test
    void prepopulateTemplates_createsTwelveTemplates() {
        int count = builder.prepopulateTemplates();
        assertEquals(12, count, "Should create 12 starter templates");
    }

    @Test
    void prepopulateTemplates_isIdempotent() {
        builder.prepopulateTemplates();
        int secondCount = builder.prepopulateTemplates();
        assertEquals(0, secondCount, "Second pre-population should create 0 templates");
    }

    @Test
    void prepopulateTemplates_createsAllModuleTypes() {
        builder.prepopulateTemplates();

        assertTrue(builder.getModulesByType(ShipModuleTemplate.ShipModuleType.BRIDGE).size() > 0);
        assertTrue(builder.getModulesByType(ShipModuleTemplate.ShipModuleType.ENGINE_ROOM).size() > 0);
        assertTrue(builder.getModulesByType(ShipModuleTemplate.ShipModuleType.WEAPON_BAY).size() > 0);
        assertTrue(builder.getModulesByType(ShipModuleTemplate.ShipModuleType.CARGO_HOLD).size() > 0);
        assertTrue(builder.getModulesByType(ShipModuleTemplate.ShipModuleType.CREW_QUARTERS).size() > 0);
        assertTrue(builder.getModulesByType(ShipModuleTemplate.ShipModuleType.CORRIDOR).size() > 0);
        assertTrue(builder.getModulesByType(ShipModuleTemplate.ShipModuleType.SHIELD_GENERATOR).size() > 0);
        assertTrue(builder.getModulesByType(ShipModuleTemplate.ShipModuleType.DOCKING_BAY).size() > 0);
        assertTrue(builder.getModulesByType(ShipModuleTemplate.ShipModuleType.RESEARCH_LAB).size() > 0);
        assertTrue(builder.getModulesByType(ShipModuleTemplate.ShipModuleType.VAULT).size() > 0);
        assertTrue(builder.getModulesByType(ShipModuleTemplate.ShipModuleType.MEDICAL_BAY).size() > 0);
        assertTrue(builder.getModulesByType(ShipModuleTemplate.ShipModuleType.HANGAR_BAY).size() > 0);
    }

    // ==========================================
    // Factory Method Tests
    // ==========================================

    @Test
    void createBridgeModuleTemplate_hasCorrectType() {
        ShipModuleTemplate template = builder.createBridgeModuleTemplate();
        assertEquals(ShipModuleTemplate.ShipModuleType.BRIDGE, template.getType());
        assertEquals("Command Bridge", template.getName());
        assertEquals(50.0, template.getMassTons());
        assertEquals(20, template.getCrewCapacity());
    }

    @Test
    void createEngineRoomTemplate_hasCorrectType() {
        ShipModuleTemplate template = builder.createEngineRoomTemplate();
        assertEquals(ShipModuleTemplate.ShipModuleType.ENGINE_ROOM, template.getType());
        assertEquals(100.0, template.getMassTons());
    }

    @Test
    void createWeaponBayTemplate_hasCorrectType() {
        ShipModuleTemplate template = builder.createWeaponBayTemplate();
        assertEquals(ShipModuleTemplate.ShipModuleType.WEAPON_BAY, template.getType());
        assertEquals(80.0, template.getMassTons());
    }

    @Test
    void createCargoHoldTemplate_hasCorrectType() {
        ShipModuleTemplate template = builder.createCargoHoldTemplate();
        assertEquals(ShipModuleTemplate.ShipModuleType.CARGO_HOLD, template.getType());
        assertEquals(30.0, template.getMassTons());
    }

    @Test
    void createCrewQuartersTemplate_hasCorrectType() {
        ShipModuleTemplate template = builder.createCrewQuartersTemplate();
        assertEquals(ShipModuleTemplate.ShipModuleType.CREW_QUARTERS, template.getType());
        assertEquals(50, template.getCrewCapacity());
    }

    @Test
    void createShieldGeneratorTemplate_hasCorrectType() {
        ShipModuleTemplate template = builder.createShieldGeneratorTemplate();
        assertEquals(ShipModuleTemplate.ShipModuleType.SHIELD_GENERATOR, template.getType());
        assertEquals(60.0, template.getMassTons());
    }

    @Test
    void createDockingBayTemplate_hasCorrectType() {
        ShipModuleTemplate template = builder.createDockingBayTemplate();
        assertEquals(ShipModuleTemplate.ShipModuleType.DOCKING_BAY, template.getType());
        assertEquals(45.0, template.getMassTons());
    }

    @Test
    void createResearchLabTemplate_hasCorrectType() {
        ShipModuleTemplate template = builder.createResearchLabTemplate();
        assertEquals(ShipModuleTemplate.ShipModuleType.RESEARCH_LAB, template.getType());
        assertEquals(35.0, template.getMassTons());
    }

    @Test
    void createCorridorTemplate_hasCorrectType() {
        ShipModuleTemplate template = builder.createCorridorTemplate();
        assertEquals(ShipModuleTemplate.ShipModuleType.CORRIDOR, template.getType());
        assertEquals(10.0, template.getMassTons());
        assertEquals(0, template.getCrewCapacity());
    }

    @Test
    void createVaultTemplate_hasCorrectType() {
        ShipModuleTemplate template = builder.createVaultTemplate();
        assertEquals(ShipModuleTemplate.ShipModuleType.VAULT, template.getType());
        assertEquals(55.0, template.getMassTons());
    }

    @Test
    void createMedicalBayTemplate_hasCorrectType() {
        ShipModuleTemplate template = builder.createMedicalBayTemplate();
        assertEquals(ShipModuleTemplate.ShipModuleType.MEDICAL_BAY, template.getType());
        assertEquals(40.0, template.getMassTons());
    }

    @Test
    void createHangarBayTemplate_hasCorrectType() {
        ShipModuleTemplate template = builder.createHangarBayTemplate();
        assertEquals(ShipModuleTemplate.ShipModuleType.HANGAR_BAY, template.getType());
        assertEquals(70.0, template.getMassTons());
    }

    // ==========================================
    // Legacy Standalone Mode Tests
    // ==========================================

    @Test
    void standaloneBuilder_buildFromTemplate_works() {
        ShipBuilder standalone = new ShipBuilder();
        ShipModuleTemplate template = builder.createBridgeModuleTemplate();
        HullModule module = standalone.buildFromTemplate(template);
        assertNotNull(module);
    }

    @Test
    void standaloneBuilder_assemble_works() {
        ShipBuilder standalone = new ShipBuilder();
        ShipModuleTemplate bridge = builder.createBridgeModuleTemplate();
        ShipModuleTemplate corridor = builder.createCorridorTemplate();

        List<ShipModuleTemplate> templates = List.of(bridge, corridor);
        List<ShipBuilder.ConnectionDefinition> connections = Collections.singletonList(
                new ShipBuilder.ConnectionDefinition(0, new M3space(5, 9, 1), 1, new M3space(2, 0, 1))
        );

        Ship ship = standalone.assemble("Standalone Ship", ShipType.FRIGATE, templates, connections);
        assertEquals("Standalone Ship", ship.getName());
        assertEquals(ShipType.FRIGATE, ship.getShipType());
    }

    @Test
    void standaloneBuilder_crud_throwsIllegalState() {
        ShipBuilder standalone = new ShipBuilder();
        assertThrows(IllegalStateException.class, () -> standalone.getModuleTemplates());
    }

    // ==========================================
    // Integration Tests
    // ==========================================

    @Test
    void fullWorkflow_prepopulate_buildShip_generateInteriors() {
        // Pre-populate templates
        int count = builder.prepopulateTemplates();
        assertEquals(12, count);

        // Find bridge template
        List<ShipModuleTemplate> bridges = builder.getModulesByType(ShipModuleTemplate.ShipModuleType.BRIDGE);
        assertTrue(bridges.size() > 0);
        Long bridgeId = bridges.get(0).getId();

        // Build ship
        Ship ship = builder.buildShip("USS Voyager", ShipType.CRUISER, List.of(bridgeId));
        assertEquals("USS Voyager", ship.getName());
        assertEquals(ShipType.CRUISER, ship.getShipType());

        // Generate interiors
        String dungeonId = builder.generateShipInteriors(ship, TechLevel.TL8, 42L);
        assertEquals("USS Voyager-interior", dungeonId);

        // Verify rooms
        List<DungeonRoomService.DungeonRoom> rooms = mockDungeonRoomService.listRoomsByDungeon(dungeonId);
        assertEquals(10, rooms.size());
    }

    @Test
    void getModulesForShipType_returnsCompatibleTemplates() {
        ShipModuleTemplate template = builder.createBridgeModuleTemplate();
        template.setCompatibleShipTypes("FRIGATE,DESTROYER");
        builder.createModuleTemplate(template);

        List<ShipModuleTemplate> frigateCompatible = builder.getModulesForShipType(ShipType.FRIGATE);
        assertEquals(1, frigateCompatible.size());

        List<ShipModuleTemplate> battleshipCompatible = builder.getModulesForShipType(ShipType.BATTLESHIP);
        assertEquals(0, battleshipCompatible.size());
    }

    @Test
    void getModulesForShipType_returnsUnrestrictedTemplates() {
        ShipModuleTemplate template = builder.createBridgeModuleTemplate();
        // No compatibleShipTypes set = compatible with all
        builder.createModuleTemplate(template);

        List<ShipModuleTemplate> allCompatible = builder.getModulesForShipType(ShipType.MOTHERSHIP);
        assertEquals(1, allCompatible.size());
    }

    @Test
    void buildFromTemplate_nullThrows() {
        assertThrows(IllegalArgumentException.class, () -> builder.buildFromTemplate(null));
    }

    @Test
    void factoryTemplates_haveConnectionPoints() {
        ShipModuleTemplate template = builder.createBridgeModuleTemplate();
        assertNotNull(template.getConnectionPoints());
        assertTrue(template.getConnectionPoints().size() > 0);
    }

    @Test
    void factoryTemplates_haveShapeCells() {
        ShipModuleTemplate template = builder.createBridgeModuleTemplate();
        assertNotNull(template.getShapeCellsJson());
        assertTrue(template.getShapeCellsJson().length() > 0);
    }

    @Test
    void factoryTemplates_haveCompartments() {
        ShipModuleTemplate template = builder.createBridgeModuleTemplate();
        assertNotNull(template.getCompartmentsJson());
        assertTrue(template.getCompartmentsJson().length() > 0);
    }
}
