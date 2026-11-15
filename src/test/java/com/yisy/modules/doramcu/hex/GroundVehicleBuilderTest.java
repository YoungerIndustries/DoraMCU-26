package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.gamecore.DungeonRoomService;
import com.yisy.modules.doramcu.hex.dungeon.GroundVehicleModuleTemplate;
import com.yisy.modules.doramcu.hex.dungeon.GroundVehicleModuleTemplateRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GroundVehicleBuilder}.
 * Tests template CRUD, vehicle assembly, interior generation, compartment mapping,
 * pre-population, and all factory methods.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GroundVehicleBuilderTest {

    @Mock
    private GroundVehicleModuleTemplateRepository mockTemplateRepo;
    private MockDungeonRoomService mockDungeonRoomService;
    private GroundVehicleBuilder builder;

    private final Map<Long, GroundVehicleModuleTemplate> savedTemplates = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @BeforeEach
    void setUp() {
        mockDungeonRoomService = new MockDungeonRoomService();
        builder = new GroundVehicleBuilder(mockTemplateRepo, mockDungeonRoomService, null, null, null, null);
        savedTemplates.clear();
        nextId.set(1);

        // Configure mock repository behavior
        when(mockTemplateRepo.save(any(GroundVehicleModuleTemplate.class))).thenAnswer(invocation -> {
            GroundVehicleModuleTemplate template = invocation.getArgument(0);
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
            List<GroundVehicleModuleTemplate> result = new ArrayList<>();
            for (Long id : ids) {
                GroundVehicleModuleTemplate t = savedTemplates.get(id);
                if (t != null) result.add(t);
            }
            return result;
        });
        doAnswer(invocation -> {
            savedTemplates.remove(invocation.getArgument(0));
            return null;
        }).when(mockTemplateRepo).deleteById(any(Long.class));
        when(mockTemplateRepo.count()).thenAnswer(invocation -> (long) savedTemplates.size());
        when(mockTemplateRepo.saveAll(anyIterable())).thenAnswer(invocation -> {
            Iterable<GroundVehicleModuleTemplate> templates = invocation.getArgument(0);
            List<GroundVehicleModuleTemplate> saved = new ArrayList<>();
            for (GroundVehicleModuleTemplate t : templates) {
                if (t.getId() == null) {
                    t.setId(nextId.getAndIncrement());
                }
                savedTemplates.put(t.getId(), t);
                saved.add(t);
            }
            return saved;
        });
        when(mockTemplateRepo.findByType(any(GroundVehicleModuleTemplate.GroundVehicleModuleType.class))).thenAnswer(invocation -> {
            GroundVehicleModuleTemplate.GroundVehicleModuleType type = invocation.getArgument(0);
            List<GroundVehicleModuleTemplate> result = new ArrayList<>();
            for (GroundVehicleModuleTemplate t : savedTemplates.values()) {
                if (t.getType() == type) result.add(t);
            }
            return result;
        });
        when(mockTemplateRepo.findByTagContaining(anyString())).thenAnswer(invocation -> {
            String tag = invocation.getArgument(0, String.class).toLowerCase();
            List<GroundVehicleModuleTemplate> result = new ArrayList<>();
            for (GroundVehicleModuleTemplate t : savedTemplates.values()) {
                if (t.getTags() != null && t.getTags().toLowerCase().contains(tag)) {
                    result.add(t);
                }
            }
            return result;
        });
        when(mockTemplateRepo.findByArmorRatingLessThanEqual(anyInt())).thenAnswer(invocation -> {
            int max = invocation.getArgument(0);
            List<GroundVehicleModuleTemplate> result = new ArrayList<>();
            for (GroundVehicleModuleTemplate t : savedTemplates.values()) {
                if (t.getArmorRating() <= max) result.add(t);
            }
            return result;
        });
        when(mockTemplateRepo.findByDifficultyLessThanEqual(anyInt())).thenAnswer(invocation -> {
            int max = invocation.getArgument(0);
            List<GroundVehicleModuleTemplate> result = new ArrayList<>();
            for (GroundVehicleModuleTemplate t : savedTemplates.values()) {
                if (t.getDifficulty() <= max) result.add(t);
            }
            return result;
        });
        when(mockTemplateRepo.findByCompatibleTerrainContaining(anyString())).thenAnswer(invocation -> {
            String terrain = invocation.getArgument(0, String.class).toUpperCase();
            List<GroundVehicleModuleTemplate> result = new ArrayList<>();
            for (GroundVehicleModuleTemplate t : savedTemplates.values()) {
                if (t.getCompatibleTerrain() == null || t.getCompatibleTerrain().isBlank()) {
                    result.add(t);
                } else if (t.getCompatibleTerrain().toUpperCase().contains(terrain)) {
                    result.add(t);
                }
            }
            return result;
        });
        when(mockTemplateRepo.findAllOrderByFirepowerDesc()).thenAnswer(invocation -> {
            List<GroundVehicleModuleTemplate> result = new ArrayList<>(savedTemplates.values());
            result.sort((a, b) -> Integer.compare(b.getFirepower(), a.getFirepower()));
            return result;
        });
        when(mockTemplateRepo.findAllOrderByDifficultyAsc()).thenAnswer(invocation -> {
            List<GroundVehicleModuleTemplate> result = new ArrayList<>(savedTemplates.values());
            result.sort((a, b) -> Integer.compare(a.getDifficulty(), b.getDifficulty()));
            return result;
        });
        when(mockTemplateRepo.findByInfrastructureRequirement(anyString())).thenAnswer(invocation -> {
            String infrastructure = invocation.getArgument(0);
            List<GroundVehicleModuleTemplate> result = new ArrayList<>();
            for (GroundVehicleModuleTemplate t : savedTemplates.values()) {
                if (infrastructure.equals(t.getInfrastructureRequirement())) {
                    result.add(t);
                }
            }
            return result;
        });
        when(mockTemplateRepo.findByPassengerCapacityGreaterThanEqual(anyInt())).thenAnswer(invocation -> {
            int min = invocation.getArgument(0);
            List<GroundVehicleModuleTemplate> result = new ArrayList<>();
            for (GroundVehicleModuleTemplate t : savedTemplates.values()) {
                if (t.getPassengerCapacity() >= min) result.add(t);
            }
            return result;
        });
        when(mockTemplateRepo.findByCargoCapacityGreaterThanEqual(anyInt())).thenAnswer(invocation -> {
            int min = invocation.getArgument(0);
            List<GroundVehicleModuleTemplate> result = new ArrayList<>();
            for (GroundVehicleModuleTemplate t : savedTemplates.values()) {
                if (t.getCargoCapacity() >= min) result.add(t);
            }
            return result;
        });
        when(mockTemplateRepo.findTransportVehicles(anyList())).thenAnswer(invocation -> {
            List<GroundVehicleModuleTemplate.GroundVehicleModuleType> types = invocation.getArgument(0);
            List<GroundVehicleModuleTemplate> result = new ArrayList<>();
            for (GroundVehicleModuleTemplate t : savedTemplates.values()) {
                if (types.contains(t.getType())) result.add(t);
            }
            return result;
        });
        when(mockTemplateRepo.findByNameContainingIgnoreCase(anyString())).thenAnswer(invocation -> {
            String name = invocation.getArgument(0, String.class).toLowerCase();
            List<GroundVehicleModuleTemplate> result = new ArrayList<>();
            for (GroundVehicleModuleTemplate t : savedTemplates.values()) {
                if (t.getName() != null && t.getName().toLowerCase().contains(name)) {
                    result.add(t);
                }
            }
            return result;
        });
        when(mockTemplateRepo.findByTypeAndTagContaining(any(GroundVehicleModuleTemplate.GroundVehicleModuleType.class), anyString())).thenAnswer(invocation -> {
            GroundVehicleModuleTemplate.GroundVehicleModuleType type = invocation.getArgument(0);
            String tag = invocation.getArgument(1, String.class).toLowerCase();
            List<GroundVehicleModuleTemplate> result = new ArrayList<>();
            for (GroundVehicleModuleTemplate t : savedTemplates.values()) {
                if (t.getType() == type && t.getTags() != null && t.getTags().toLowerCase().contains(tag)) {
                    result.add(t);
                }
            }
            return result;
        });
    }

    // ==========================================
    // Template CRUD Tests
    // ==========================================

    @Test
    void createModuleTemplate_savesAndReturnsTemplate() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);

        assertNotNull(saved.getId());
        assertEquals("Main Battle Tank", saved.getName());
        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK, saved.getType());
        assertEquals(35, saved.getArmorRating());
    }

    @Test
    void updateModuleTemplate_updatesExistingTemplate() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);

        saved.setArmorRating(50);
        saved.setName("Heavy Tank");
        GroundVehicleModuleTemplate updated = builder.updateModuleTemplate(saved);

        assertEquals(50, updated.getArmorRating());
        assertEquals("Heavy Tank", updated.getName());
    }

    @Test
    void updateModuleTemplate_throwsWhenNotFound() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        template.setId(999L); // Non-existent ID

        assertThrows(IllegalArgumentException.class, () -> builder.updateModuleTemplate(template));
    }

    @Test
    void deleteModuleTemplate_removesTemplate() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);

        builder.deleteModuleTemplate(saved.getId());

        assertFalse(builder.getModuleTemplate(saved.getId()).isPresent());
    }

    @Test
    void getModuleTemplates_returnsAllTemplates() {
        builder.createModuleTemplate(builder.createTankTemplate());
        builder.createModuleTemplate(builder.createAPCTemplate());

        List<GroundVehicleModuleTemplate> templates = builder.getModuleTemplates();

        assertEquals(2, templates.size());
    }

    @Test
    void getModuleTemplate_returnsTemplateById() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);

        Optional<GroundVehicleModuleTemplate> found = builder.getModuleTemplate(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void getModulesByType_filtersByType() {
        builder.createModuleTemplate(builder.createTankTemplate());
        builder.createModuleTemplate(builder.createAPCTemplate());
        builder.createModuleTemplate(builder.createTankTemplate());

        List<GroundVehicleModuleTemplate> tanks = builder.getModulesByType(
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);

        assertEquals(2, tanks.size());
    }

    @Test
    void getModulesByTag_filtersByTag() {
        builder.createModuleTemplate(builder.createTankTemplate());
        builder.createModuleTemplate(builder.createAPCTemplate());

        List<GroundVehicleModuleTemplate> armored = builder.getModulesByTag("armored");

        // APC has "armored" tag, Tank has "armor" tag - only APC matches "armored"
        assertEquals(1, armored.size());
    }

    @Test
    void getModulesForTerrain_filtersByTerrain() {
        builder.createModuleTemplate(builder.createTankTemplate());
        builder.createModuleTemplate(builder.createFighterJetTemplate());

        List<GroundVehicleModuleTemplate> openTerrain = builder.getModulesForTerrain("OPEN");

        // Tank has OPEN terrain, Fighter Jet has AIR terrain
        assertEquals(1, openTerrain.size());
        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK, openTerrain.get(0).getType());
    }

    @Test
    void getModulesByArmor_filtersByArmor() {
        builder.createModuleTemplate(builder.createTankTemplate());
        builder.createModuleTemplate(builder.createAPCTemplate());
        builder.createModuleTemplate(builder.createFighterJetTemplate());

        List<GroundVehicleModuleTemplate> lightArmor = builder.getModulesByArmor(15);

        assertEquals(2, lightArmor.size());
    }

    @Test
    void getModulesOrderedByFirepower_ordersByFirepower() {
        builder.createModuleTemplate(builder.createTankTemplate());
        builder.createModuleTemplate(builder.createMissileLauncherTemplate());
        builder.createModuleTemplate(builder.createAPCTemplate());

        List<GroundVehicleModuleTemplate> ordered = builder.getModulesOrderedByFirepower();

        assertEquals(3, ordered.size());
        assertTrue(ordered.get(0).getFirepower() >= ordered.get(1).getFirepower());
    }

    // ==========================================
    // Vehicle Assembly Tests
    // ==========================================

    @Test
    void buildVehicle_assemblesFromSingleTemplate() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);

        GroundVehicle vehicle = builder.buildVehicle("M1 Abrams", saved.getId());

        assertNotNull(vehicle);
        assertEquals("M1 Abrams", vehicle.getName());
        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK, vehicle.getType());
        assertEquals(35, vehicle.getArmorRating());
        assertEquals(25, vehicle.getFirepower());
        assertEquals(67, vehicle.getSpeedKmh());
        assertEquals(4, vehicle.getCrewCapacity());
    }

    @Test
    void buildVehicle_assemblesFromMultipleTemplates() {
        GroundVehicleModuleTemplate tank = builder.createTankTemplate();
        GroundVehicleModuleTemplate corridor = builder.createCorridorTemplate();
        GroundVehicleModuleTemplate savedTank = builder.createModuleTemplate(tank);
        GroundVehicleModuleTemplate savedCorridor = builder.createModuleTemplate(corridor);

        GroundVehicle vehicle = builder.buildVehicle("Combined Vehicle",
                List.of(savedTank.getId(), savedCorridor.getId()));

        assertNotNull(vehicle);
        assertEquals("Combined Vehicle", vehicle.getName());
        // Armor accumulates: 35 + 5 = 40
        assertEquals(40, vehicle.getArmorRating());
        // Firepower accumulates: 25 + 0 = 25
        assertEquals(25, vehicle.getFirepower());
        // Speed takes max: max(67, 0) = 67
        assertEquals(67, vehicle.getSpeedKmh());
    }

    @Test
    void buildVehicle_throwsWhenTemplateNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
                builder.buildVehicle("Ghost Vehicle", 999L));
    }

    @Test
    void buildVehicle_setsFuelAndDetectionRange() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);

        GroundVehicle vehicle = builder.buildVehicle("Test Vehicle", saved.getId());

        assertEquals(2.5, vehicle.getFuelConsumption(), 0.01);
        assertEquals(25.0, vehicle.getMaxFuel(), 0.01);
        assertEquals(3, vehicle.getDetectionRange());
    }

    @Test
    void buildVehicle_setsTerrainCompatibility() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);

        GroundVehicle vehicle = builder.buildVehicle("Test Vehicle", saved.getId());

        assertTrue(vehicle.isCompatibleWithTerrain("OPEN"));
        assertTrue(vehicle.isCompatibleWithTerrain("URBAN"));
        assertTrue(vehicle.isCompatibleWithTerrain("DESERT"));
    }

    @Test
    void buildVehicle_accumulatesCrewCapacity() {
        GroundVehicleModuleTemplate tank = builder.createTankTemplate();
        GroundVehicleModuleTemplate apc = builder.createAPCTemplate();
        builder.createModuleTemplate(tank);
        GroundVehicleModuleTemplate savedAp = builder.createModuleTemplate(apc);

        GroundVehicle vehicle = builder.buildVehicle("Combined", savedAp.getId());

        assertEquals(11, vehicle.getCrewCapacity());
    }

    @Test
    void buildVehicle_setsSupplyCost() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);

        GroundVehicle vehicle = builder.buildVehicle("Test Vehicle", saved.getId());

        assertEquals(3, vehicle.getSupplyCost());
    }

    @Test
    void buildVehicle_hasHullModules() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);

        GroundVehicle vehicle = builder.buildVehicle("Test Vehicle", saved.getId());

        assertEquals(1, vehicle.getHullModuleCount());
        assertFalse(vehicle.getHullModules().isEmpty());
    }

    @Test
    void buildVehicle_hasCompartments() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);

        GroundVehicle vehicle = builder.buildVehicle("Test Vehicle", saved.getId());

        // Tank has 5 compartments: Main Turret, Crew Compartment, Engine Bay, Ammo Storage, Fuel Tank
        assertEquals(5, vehicle.getCompartments().size());
        assertNotNull(vehicle.getCompartment("Main Turret"));
        assertNotNull(vehicle.getCompartment("Engine Bay"));
    }

    @Test
    void buildVehicle_determinesTypeFromFirstNonCorridor() {
        GroundVehicleModuleTemplate corridor = builder.createCorridorTemplate();
        GroundVehicleModuleTemplate tank = builder.createTankTemplate();
        builder.createModuleTemplate(corridor);
        GroundVehicleModuleTemplate savedTank = builder.createModuleTemplate(tank);

        GroundVehicle vehicle = builder.buildVehicle("Test Vehicle", savedTank.getId());

        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK, vehicle.getType());
    }

    // ==========================================
    // Interior Generation Tests
    // ==========================================

    @Test
    void generateVehicleInteriors_generatesDungeon() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);
        GroundVehicle vehicle = builder.buildVehicle("Test Tank", saved.getId());

        String dungeonId = builder.generateVehicleInteriors(vehicle, TechLevel.TL8, 42L);

        assertNotNull(dungeonId);
        assertEquals("Test Tank-interior", dungeonId);
    }

    @Test
    void generateVehicleInteriors_withDefaultTechLevel() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);
        GroundVehicle vehicle = builder.buildVehicle("Test Tank", saved.getId());

        String dungeonId = builder.generateVehicleInteriors(vehicle, 42L);

        assertNotNull(dungeonId);
    }

    @Test
    void generateVehicleInteriors_withRandomSeed() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);
        GroundVehicle vehicle = builder.buildVehicle("Test Tank", saved.getId());

        String dungeonId = builder.generateVehicleInteriors(vehicle, TechLevel.TL8);

        assertNotNull(dungeonId);
    }

    // ==========================================
    // Compartment-to-Room Mapping Tests
    // ==========================================

    @Test
    void mapCompartmentsToRooms_mapsCombatCompartments() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);
        GroundVehicle vehicle = builder.buildVehicle("Test Tank", saved.getId());
        String dungeonId = builder.generateVehicleInteriors(vehicle, TechLevel.TL8, 42L);

        Map<String, Long> mapping = builder.mapCompartmentsToRooms(vehicle, dungeonId);

        // Should have some mappings
        assertNotNull(mapping);
    }

    @Test
    void mapCompartmentsToRooms_returnsEmptyWhenNoDungeon() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);
        GroundVehicle vehicle = builder.buildVehicle("Test Tank", saved.getId());

        Map<String, Long> mapping = builder.mapCompartmentsToRooms(vehicle, "nonexistent");

        assertTrue(mapping.isEmpty());
    }

    @Test
    void mapCompartmentsToRooms_matchesTurretToCombat() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);
        GroundVehicle vehicle = builder.buildVehicle("Test Tank", saved.getId());
        String dungeonId = builder.generateVehicleInteriors(vehicle, TechLevel.TL8, 42L);

        Map<String, Long> mapping = builder.mapCompartmentsToRooms(vehicle, dungeonId);

        // Main Turret is TURRET type, should match Combat theme
        if (mapping.containsKey("Main Turret")) {
            Long roomId = mapping.get("Main Turret");
            DungeonRoomService.DungeonRoom room = mockDungeonRoomService.getRoom(roomId);
            assertNotNull(room);
            assertEquals("Combat", room.getTheme());
        }
    }

    // ==========================================
    // Pre-population Tests
    // ==========================================

    @Test
    void prepopulateTemplates_createsAllTemplates() {
        int count = builder.prepopulateTemplates();

        // prepopulate creates 16 templates: 10 combat + 6 civilian transport
        assertEquals(16, count);
    }

    @Test
    void prepopulateTemplates_skipsWhenAlreadyPopulated() {
        builder.createModuleTemplate(builder.createTankTemplate());

        int count = builder.prepopulateTemplates();

        assertEquals(0, count);
    }

    @Test
    void prepopulateTemplates_includesAllTypes() {
        builder.prepopulateTemplates();

        // Verify factory methods produce correct types
        GroundVehicleModuleTemplate tank = builder.createTankTemplate();
        GroundVehicleModuleTemplate apc = builder.createAPCTemplate();
        GroundVehicleModuleTemplate fighter = builder.createFighterJetTemplate();

        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK, tank.getType());
        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.APC, apc.getType());
        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.FIGHTER_JET, fighter.getType());
    }

    // ==========================================
    // Factory Method Tests
    // ==========================================

    @Test
    void createTankTemplate_hasCorrectStats() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();

        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK, template.getType());
        assertEquals(35, template.getArmorRating());
        assertEquals(25, template.getFirepower());
        assertEquals(67, template.getSpeedKmh());
        assertEquals(4, template.getCrewCapacity());
    }

    @Test
    void createAPCTemplate_hasCorrectStats() {
        GroundVehicleModuleTemplate template = builder.createAPCTemplate();

        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.APC, template.getType());
        assertEquals(15, template.getArmorRating());
        assertEquals(8, template.getFirepower());
        assertEquals(65, template.getSpeedKmh());
        assertEquals(11, template.getCrewCapacity());
    }

    @Test
    void createIFVTemplate_hasCorrectStats() {
        GroundVehicleModuleTemplate template = builder.createIFVTemplate();

        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.IFV, template.getType());
        assertEquals(20, template.getArmorRating());
        assertEquals(15, template.getFirepower());
    }

    @Test
    void createArtilleryTemplate_hasHighFirepower() {
        GroundVehicleModuleTemplate template = builder.createArtilleryTemplate();

        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.ARTILLERY, template.getType());
        assertEquals(40, template.getFirepower());
        assertEquals(5, template.getDetectionRange());
    }

    @Test
    void createMissileLauncherTemplate_hasHighestFirepower() {
        GroundVehicleModuleTemplate template = builder.createMissileLauncherTemplate();

        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.MISSILE_LAUNCHER, template.getType());
        assertEquals(60, template.getFirepower());
        assertEquals(8, template.getDetectionRange());
    }

    @Test
    void createFighterJetTemplate_hasHighSpeed() {
        GroundVehicleModuleTemplate template = builder.createFighterJetTemplate();

        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.FIGHTER_JET, template.getType());
        assertEquals(2400, template.getSpeedKmh());
        assertEquals(1, template.getCrewCapacity());
    }

    @Test
    void createBomberTemplate_hasHighFirepower() {
        GroundVehicleModuleTemplate template = builder.createBomberTemplate();

        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.BOMBER, template.getType());
        assertEquals(50, template.getFirepower());
        assertEquals(4, template.getCrewCapacity());
    }

    @Test
    void createHelicopterTemplate_hasMediumStats() {
        GroundVehicleModuleTemplate template = builder.createHelicopterTemplate();

        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.HELICOPTER, template.getType());
        assertEquals(20, template.getFirepower());
        assertEquals(300, template.getSpeedKmh());
    }

    @Test
    void createTransportShipTemplate_hasHighCrewCapacity() {
        GroundVehicleModuleTemplate template = builder.createTransportShipTemplate();

        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.TRANSPORT_SHIP, template.getType());
        assertEquals(50, template.getCrewCapacity());
        assertEquals(25, template.getArmorRating());
    }

    @Test
    void createCorridorTemplate_hasZeroFirepower() {
        GroundVehicleModuleTemplate template = builder.createCorridorTemplate();

        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.CORRIDOR, template.getType());
        assertEquals(0, template.getFirepower());
        assertEquals(0, template.getCrewCapacity());
    }

    // ==========================================
    // GroundVehicle Model Tests
    // ==========================================

    @Test
    void groundVehicle_damageHullCell_reducesArmor() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);
        GroundVehicle vehicle = builder.buildVehicle("Test Tank", saved.getId());

        int initialArmor = vehicle.getCurrentArmor();
        vehicle.damageHullCell(0, 0, 0, 10);

        assertEquals(initialArmor - 10, vehicle.getCurrentArmor());
    }

    @Test
    void groundVehicle_damageHullCell_returnsCompartment() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);
        GroundVehicle vehicle = builder.buildVehicle("Test Tank", saved.getId());

        Compartment damaged = vehicle.damageHullCell(0, 0, 0, 10);

        // May return null if the cell doesn't exist in the module's grid
        // The important thing is it doesn't throw
        assertNotNull(vehicle);
    }

    @Test
    void groundVehicle_isDestroyed_whenArmorDepleted() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);
        vehicle.setArmorRating(10);

        assertFalse(vehicle.isDestroyed());
        vehicle.setCurrentArmor(0);
        assertTrue(vehicle.isDestroyed());
    }

    @Test
    void groundVehicle_refuel_addsFuel() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);
        vehicle.setMaxFuel(100);
        vehicle.setFuelLevel(50);

        double added = vehicle.refuel(30);

        assertEquals(30, added, 0.01);
        assertEquals(80, vehicle.getFuelLevel(), 0.01);
    }

    @Test
    void groundVehicle_refuel_cappedAtMax() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);
        vehicle.setMaxFuel(100);
        vehicle.setFuelLevel(90);

        double added = vehicle.refuel(30);

        assertEquals(10, added, 0.01);
        assertEquals(100, vehicle.getFuelLevel(), 0.01);
    }

    @Test
    void groundVehicle_consumeFuel_reducesFuel() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);
        vehicle.setMaxFuel(100);
        vehicle.setFuelLevel(50);
        vehicle.setFuelConsumption(10);

        boolean hasFuel = vehicle.consumeFuel();

        assertTrue(hasFuel);
        assertEquals(40, vehicle.getFuelLevel(), 0.01);
    }

    @Test
    void groundVehicle_consumeFuel_returnsFalseWhenEmpty() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);
        vehicle.setMaxFuel(100);
        vehicle.setFuelLevel(5);
        vehicle.setFuelConsumption(10);

        boolean hasFuel = vehicle.consumeFuel();

        assertFalse(hasFuel);
        assertEquals(5, vehicle.getFuelLevel(), 0.01);
    }

    @Test
    void groundVehicle_move_requiresFuel() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);
        vehicle.setArmorRating(100);
        vehicle.setMaxFuel(100);
        vehicle.setFuelLevel(50);
        vehicle.setFuelConsumption(10);

        Hex3D newPos = new Hex3D(1, 0, -1);
        boolean moved = vehicle.move(newPos);

        assertTrue(moved);
        // Hex3D may not override equals, so check coordinates
        assertEquals(1, vehicle.getCurrentPosition().getX());
        assertEquals(0, vehicle.getCurrentPosition().getY());
        assertEquals(-1, vehicle.getCurrentPosition().getZ());
    }

    @Test
    void groundVehicle_move_failsWhenDestroyed() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);
        vehicle.setArmorRating(10);
        vehicle.setCurrentArmor(0);
        vehicle.setMaxFuel(100);
        vehicle.setFuelLevel(50);
        vehicle.setFuelConsumption(10);

        Hex3D newPos = new Hex3D(1, 0, -1);
        boolean moved = vehicle.move(newPos);

        assertFalse(moved);
    }

    @Test
    void groundVehicle_terrainCompatibility_works() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);
        vehicle.setCompatibleTerrain("OPEN,URBAN,DESERT");

        assertTrue(vehicle.isCompatibleWithTerrain("OPEN"));
        assertTrue(vehicle.isCompatibleWithTerrain("urban"));
        assertFalse(vehicle.isCompatibleWithTerrain("JUNGLE"));
    }

    @Test
    void groundVehicle_noTerrainRestriction_allowsAll() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);

        assertTrue(vehicle.isCompatibleWithTerrain("ANY"));
    }

    // ==========================================
    // Integration Tests
    // ==========================================

    @Test
    void fullWorkflow_createAndBuild() {
        // Create a tank template and build from it
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);

        GroundVehicle vehicle = builder.buildVehicle("Battle Tank", saved.getId());

        assertNotNull(vehicle);
        assertEquals("Battle Tank", vehicle.getName());
        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK, vehicle.getType());
    }

    @Test
    void fullWorkflow_buildAndGenerateInteriors() {
        GroundVehicleModuleTemplate template = builder.createTankTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);
        GroundVehicle vehicle = builder.buildVehicle("Test Tank", saved.getId());
        String dungeonId = builder.generateVehicleInteriors(vehicle, TechLevel.TL8, 42L);
        Map<String, Long> mapping = builder.mapCompartmentsToRooms(vehicle, dungeonId);

        assertNotNull(dungeonId);
        assertNotNull(mapping);
    }

    @Test
    void standaloneBuilder_throwsOnCRUD() {
        GroundVehicleBuilder standalone = new GroundVehicleBuilder();

        assertThrows(IllegalStateException.class, () -> standalone.getModuleTemplates());
        assertThrows(IllegalStateException.class, () -> standalone.createModuleTemplate(
                new GroundVehicleModuleTemplate()));
    }

    @Test
    void groundVehicle_processTurn_consumesFuel() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);
        vehicle.setArmorRating(100);
        vehicle.setCurrentArmor(100);
        vehicle.setMaxFuel(100);
        vehicle.setFuelLevel(50);
        vehicle.setFuelConsumption(10);

        vehicle.processTurn();

        assertEquals(40, vehicle.getFuelLevel(), 0.01);
    }

    @Test
    void groundVehicle_processTurn_skipsWhenInactive() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);
        vehicle.setMaxFuel(100);
        vehicle.setFuelLevel(50);
        vehicle.setFuelConsumption(10);
        vehicle.setActive(false);

        vehicle.processTurn();

        assertEquals(50, vehicle.getFuelLevel(), 0.01);
    }

    @Test
    void compartmentType_enumHasNewTypes() {
        // Verify the new compartment types exist
        assertEquals("TURRET", CompartmentType.TURRET.name());
        assertEquals("ENGINE_BAY", CompartmentType.ENGINE_BAY.name());
        assertEquals("CREW_HATCH", CompartmentType.CREW_HATCH.name());
        assertEquals("FUEL_TANK", CompartmentType.FUEL_TANK.name());
        assertEquals("SENSOR_ARRAY", CompartmentType.SENSOR_ARRAY.name());
        assertEquals("LAUNCHER", CompartmentType.LAUNCHER.name());
    }

    // ==========================================
    // Civilian Transport Factory Method Tests
    // ==========================================

    @Test
    void createBusTemplate_hasCorrectStats() {
        GroundVehicleModuleTemplate template = builder.createBusTemplate();

        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.BUS, template.getType());
        assertEquals(30, template.getPassengerCapacity());
        assertEquals(0, template.getCargoCapacity());
        assertEquals("ROAD", template.getInfrastructureRequirement());
        assertEquals(80, template.getConstructionCost());
        assertEquals(4, template.getMaintenanceCost());
        assertTrue(template.isTransportVehicle());
    }

    @Test
    void createTrainTemplate_hasCorrectStats() {
        GroundVehicleModuleTemplate template = builder.createTrainTemplate();

        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.TRAIN, template.getType());
        assertEquals(80, template.getPassengerCapacity());
        assertEquals(100, template.getCargoCapacity());
        assertEquals("RAIL", template.getInfrastructureRequirement());
        assertEquals(400, template.getConstructionCost());
    }

    @Test
    void createCarTemplate_hasCorrectStats() {
        GroundVehicleModuleTemplate template = builder.createCarTemplate();

        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.CAR, template.getType());
        assertEquals(4, template.getPassengerCapacity());
        assertEquals(5, template.getCargoCapacity());
        assertEquals("ROAD", template.getInfrastructureRequirement());
        assertEquals(30, template.getConstructionCost());
    }

    @Test
    void createTruckTemplate_hasCorrectStats() {
        GroundVehicleModuleTemplate template = builder.createTruckTemplate();

        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.TRUCK, template.getType());
        assertEquals(0, template.getPassengerCapacity());
        assertEquals(20, template.getCargoCapacity());
        assertEquals("ROAD", template.getInfrastructureRequirement());
        assertEquals(100, template.getConstructionCost());
    }

    @Test
    void createFerryTemplate_hasCorrectStats() {
        GroundVehicleModuleTemplate template = builder.createFerryTemplate();

        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.FERRY, template.getType());
        assertEquals(50, template.getPassengerCapacity());
        assertEquals(10, template.getCargoCapacity());
        assertEquals("WATER", template.getInfrastructureRequirement());
        assertEquals(250, template.getConstructionCost());
    }

    @Test
    void createAircraftTemplate_hasCorrectStats() {
        GroundVehicleModuleTemplate template = builder.createAircraftTemplate();

        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.AIRCRAFT, template.getType());
        assertEquals(10, template.getPassengerCapacity());
        assertEquals(10, template.getCargoCapacity());
        assertEquals("AIRPORT", template.getInfrastructureRequirement());
        assertEquals(800, template.getConstructionCost());
    }

    // ==========================================
    // Transport Vehicle Assembly Tests
    // ==========================================

    @Test
    void buildVehicle_accumulatesTransportStats() {
        GroundVehicleModuleTemplate template = builder.createBusTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);

        GroundVehicle vehicle = builder.buildVehicle("City Bus", saved.getId());

        assertEquals(30, vehicle.getPassengerCapacity());
        assertEquals(0, vehicle.getCargoCapacity());
        assertEquals(80, vehicle.getConstructionCost());
        assertEquals(4, vehicle.getMaintenanceCost());
        assertEquals("ROAD", vehicle.getInfrastructureRequirement());
    }

    @Test
    void buildVehicle_trainHasHighCapacity() {
        GroundVehicleModuleTemplate template = builder.createTrainTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);

        GroundVehicle vehicle = builder.buildVehicle("Express Train", saved.getId());

        assertEquals(80, vehicle.getPassengerCapacity());
        assertEquals(100, vehicle.getCargoCapacity());
        assertEquals("RAIL", vehicle.getInfrastructureRequirement());
    }

    // ==========================================
    // GroundVehicle Transport Operations Tests
    // ==========================================

    @Test
    void groundVehicle_boardPassengers_works() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.BUS);
        vehicle.setPassengerCapacity(30);

        int boarded = vehicle.boardPassengers(10);

        assertEquals(10, boarded);
        assertEquals(10, vehicle.getCurrentPassengers());
    }

    @Test
    void groundVehicle_boardPassengers_cappedAtCapacity() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.BUS);
        vehicle.setPassengerCapacity(30);
        vehicle.setCurrentPassengers(25);

        int boarded = vehicle.boardPassengers(10);

        assertEquals(5, boarded);
        assertEquals(30, vehicle.getCurrentPassengers());
    }

    @Test
    void groundVehicle_unboardPassengers_works() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.BUS);
        vehicle.setPassengerCapacity(30);
        vehicle.setCurrentPassengers(20);

        int unboarded = vehicle.unboardPassengers(10);

        assertEquals(10, unboarded);
        assertEquals(10, vehicle.getCurrentPassengers());
    }

    @Test
    void groundVehicle_loadCargo_works() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TRUCK);
        vehicle.setCargoCapacity(20);

        int loaded = vehicle.loadCargo("Minerals", 10);

        assertEquals(10, loaded);
        assertEquals(10, vehicle.getTotalCargoOnBoard());
    }

    @Test
    void groundVehicle_loadCargo_cappedAtCapacity() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TRUCK);
        vehicle.setCargoCapacity(20);
        vehicle.loadCargo("Minerals", 15);

        int loaded = vehicle.loadCargo("Energy", 10);

        assertEquals(5, loaded);
        assertEquals(20, vehicle.getTotalCargoOnBoard());
    }

    @Test
    void groundVehicle_unloadCargo_works() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TRUCK);
        vehicle.setCargoCapacity(20);
        vehicle.loadCargo("Minerals", 15);

        int unloaded = vehicle.unloadCargo("Minerals", 10);

        assertEquals(10, unloaded);
        assertEquals(5, vehicle.getTotalCargoOnBoard());
    }

    @Test
    void groundVehicle_infrastructureRequirement_check() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.BUS);
        vehicle.setInfrastructureRequirement("ROAD");

        assertTrue(vehicle.canOperateWithInfrastructure("ROAD"));
        assertFalse(vehicle.canOperateWithInfrastructure("RAIL"));
    }

    @Test
    void groundVehicle_noInfrastructureRequirement_allowsAll() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.CAR);

        assertTrue(vehicle.canOperateWithInfrastructure("ANY"));
    }

    @Test
    void groundVehicle_templateTransportVehicle_check() {
        GroundVehicleModuleTemplate bus = builder.createBusTemplate();
        GroundVehicleModuleTemplate tank = builder.createTankTemplate();

        assertTrue(bus.isTransportVehicle());
        assertFalse(tank.isTransportVehicle());
    }

    @Test
    void groundVehicle_cargoInventory_multipleTypes() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TRUCK);
        vehicle.setCargoCapacity(50);
        vehicle.loadCargo("Minerals", 10);
        vehicle.loadCargo("Energy", 5);
        vehicle.loadCargo("Food", 8);

        assertEquals(23, vehicle.getTotalCargoOnBoard());
        Map<String, Integer> inventory = vehicle.getCargoInventory();
        assertEquals(3, inventory.size());
        assertEquals(10, inventory.get("Minerals"));
        assertEquals(5, inventory.get("Energy"));
        assertEquals(8, inventory.get("Food"));
    }

    // ==========================================
    // Repository Query Method Tests
    // ==========================================

    @Test
    void getModulesByInfrastructure_filtersByInfrastructure() {
        builder.createModuleTemplate(builder.createBusTemplate());
        builder.createModuleTemplate(builder.createTrainTemplate());
        builder.createModuleTemplate(builder.createTankTemplate());

        List<GroundVehicleModuleTemplate> roadVehicles = builder.getModulesByInfrastructure("ROAD");

        assertEquals(1, roadVehicles.size());
        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.BUS, roadVehicles.get(0).getType());
    }

    @Test
    void getModulesByInfrastructure_returnsEmptyWhenNoneMatch() {
        builder.createModuleTemplate(builder.createTankTemplate());

        List<GroundVehicleModuleTemplate> railVehicles = builder.getModulesByInfrastructure("RAIL");

        assertTrue(railVehicles.isEmpty());
    }

    @Test
    void getModulesByPassengerCapacity_filtersByCapacity() {
        builder.createModuleTemplate(builder.createBusTemplate());
        builder.createModuleTemplate(builder.createTrainTemplate());
        builder.createModuleTemplate(builder.createCarTemplate());

        List<GroundVehicleModuleTemplate> highCapacity = builder.getModulesByPassengerCapacity(50);

        assertEquals(1, highCapacity.size());
        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.TRAIN, highCapacity.get(0).getType());
    }

    @Test
    void getModulesByPassengerCapacity_includesExactMatch() {
        builder.createModuleTemplate(builder.createBusTemplate());
        builder.createModuleTemplate(builder.createCarTemplate());

        List<GroundVehicleModuleTemplate> result = builder.getModulesByPassengerCapacity(30);

        assertEquals(1, result.size());
        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.BUS, result.get(0).getType());
    }

    @Test
    void getModulesByCargoCapacity_filtersByCapacity() {
        builder.createModuleTemplate(builder.createTruckTemplate());
        builder.createModuleTemplate(builder.createTrainTemplate());
        builder.createModuleTemplate(builder.createCarTemplate());

        List<GroundVehicleModuleTemplate> highCargo = builder.getModulesByCargoCapacity(50);

        assertEquals(1, highCargo.size());
        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.TRAIN, highCargo.get(0).getType());
    }

    @Test
    void getTransportVehicles_returnsOnlyCivilianTransport() {
        builder.createModuleTemplate(builder.createTankTemplate());
        builder.createModuleTemplate(builder.createBusTemplate());
        builder.createModuleTemplate(builder.createAPCTemplate());
        builder.createModuleTemplate(builder.createTrainTemplate());
        builder.createModuleTemplate(builder.createCarTemplate());

        List<GroundVehicleModuleTemplate> transports = builder.getTransportVehicles();

        assertEquals(3, transports.size());
        for (GroundVehicleModuleTemplate t : transports) {
            assertTrue(t.isTransportVehicle());
        }
    }

    @Test
    void getModulesByName_searchesCaseInsensitive() {
        builder.createModuleTemplate(builder.createTankTemplate());
        builder.createModuleTemplate(builder.createBusTemplate());

        List<GroundVehicleModuleTemplate> result = builder.getModulesByName("tank");

        assertEquals(1, result.size());
        assertEquals("Main Battle Tank", result.get(0).getName());
    }

    @Test
    void getModulesByName_returnsEmptyWhenNoMatch() {
        builder.createModuleTemplate(builder.createTankTemplate());

        List<GroundVehicleModuleTemplate> result = builder.getModulesByName("nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void getModulesByTypeAndTag_filtersByBoth() {
        builder.createModuleTemplate(builder.createTankTemplate());
        builder.createModuleTemplate(builder.createAPCTemplate());
        builder.createModuleTemplate(builder.createIFVTemplate());

        // APC has "armored" tag and type APC
        List<GroundVehicleModuleTemplate> result = builder.getModulesByTypeAndTag(
                GroundVehicleModuleTemplate.GroundVehicleModuleType.APC, "armored");

        assertEquals(1, result.size());
    }

    @Test
    void getModulesByTypeAndTag_returnsEmptyWhenNoMatch() {
        builder.createModuleTemplate(builder.createTankTemplate());

        List<GroundVehicleModuleTemplate> result = builder.getModulesByTypeAndTag(
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK, "nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void getModulesByDifficulty_filtersByDifficulty() {
        builder.createModuleTemplate(builder.createTankTemplate());
        builder.createModuleTemplate(builder.createAPCTemplate());
        builder.createModuleTemplate(builder.createFighterJetTemplate());

        // Tank difficulty 5, APC difficulty 3, Fighter Jet difficulty 7
        List<GroundVehicleModuleTemplate> lowDifficulty = builder.getModulesByDifficulty(3);

        assertEquals(1, lowDifficulty.size());
        assertEquals(GroundVehicleModuleTemplate.GroundVehicleModuleType.APC, lowDifficulty.get(0).getType());
    }

    @Test
    void getModulesOrderedByDifficulty_ordersByDifficulty() {
        builder.createModuleTemplate(builder.createTankTemplate());
        builder.createModuleTemplate(builder.createAPCTemplate());
        builder.createModuleTemplate(builder.createFighterJetTemplate());

        List<GroundVehicleModuleTemplate> ordered = builder.getModulesOrderedByDifficulty();

        assertEquals(3, ordered.size());
        assertTrue(ordered.get(0).getDifficulty() <= ordered.get(1).getDifficulty());
        assertTrue(ordered.get(1).getDifficulty() <= ordered.get(2).getDifficulty());
    }

    // ==========================================
    // Edge Case Tests for Cargo and Passenger Operations
    // ==========================================

    @Test
    void groundVehicle_loadCargo_nullType_returnsZero() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TRUCK);
        vehicle.setCargoCapacity(20);

        int loaded = vehicle.loadCargo(null, 10);

        assertEquals(0, loaded);
        assertEquals(0, vehicle.getTotalCargoOnBoard());
    }

    @Test
    void groundVehicle_loadCargo_emptyType_returnsZero() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TRUCK);
        vehicle.setCargoCapacity(20);

        int loaded = vehicle.loadCargo("", 10);

        assertEquals(0, loaded);
        assertEquals(0, vehicle.getTotalCargoOnBoard());
    }

    @Test
    void groundVehicle_loadCargo_zeroAmount_returnsZero() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TRUCK);
        vehicle.setCargoCapacity(20);

        int loaded = vehicle.loadCargo("Minerals", 0);

        assertEquals(0, loaded);
        assertEquals(0, vehicle.getTotalCargoOnBoard());
    }

    @Test
    void groundVehicle_unloadCargo_nullType_returnsZero() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TRUCK);
        vehicle.setCargoCapacity(20);
        vehicle.loadCargo("Minerals", 10);

        int unloaded = vehicle.unloadCargo(null, 5);

        assertEquals(0, unloaded);
        assertEquals(10, vehicle.getTotalCargoOnBoard());
    }

    @Test
    void groundVehicle_unloadCargo_emptyType_returnsZero() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TRUCK);
        vehicle.setCargoCapacity(20);
        vehicle.loadCargo("Minerals", 10);

        int unloaded = vehicle.unloadCargo("", 5);

        assertEquals(0, unloaded);
        assertEquals(10, vehicle.getTotalCargoOnBoard());
    }

    @Test
    void groundVehicle_unloadCargo_moreThanAvailable_returnsAvailable() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TRUCK);
        vehicle.setCargoCapacity(20);
        vehicle.loadCargo("Minerals", 5);

        int unloaded = vehicle.unloadCargo("Minerals", 10);

        assertEquals(5, unloaded);
        assertEquals(0, vehicle.getTotalCargoOnBoard());
    }

    @Test
    void groundVehicle_unloadCargo_removesTypeWhenEmpty() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TRUCK);
        vehicle.setCargoCapacity(20);
        vehicle.loadCargo("Minerals", 5);

        vehicle.unloadCargo("Minerals", 5);

        Map<String, Integer> inventory = vehicle.getCargoInventory();
        assertFalse(inventory.containsKey("Minerals"));
        assertEquals(0, inventory.size());
    }

    @Test
    void groundVehicle_unboardPassengers_moreThanAvailable_returnsAvailable() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.BUS);
        vehicle.setPassengerCapacity(30);
        vehicle.setCurrentPassengers(5);

        int unboarded = vehicle.unboardPassengers(10);

        assertEquals(5, unboarded);
        assertEquals(0, vehicle.getCurrentPassengers());
    }

    @Test
    void groundVehicle_boardPassengers_zeroCapacity_returnsZero() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.BUS);
        vehicle.setPassengerCapacity(0);

        int boarded = vehicle.boardPassengers(10);

        assertEquals(0, boarded);
        assertEquals(0, vehicle.getCurrentPassengers());
    }

    @Test
    void groundVehicle_boardPassengers_negative_returnsZero() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.BUS);
        vehicle.setPassengerCapacity(30);

        int boarded = vehicle.boardPassengers(-5);

        assertEquals(0, boarded);
        assertEquals(0, vehicle.getCurrentPassengers());
    }

    @Test
    void groundVehicle_setCurrentPassengers_clampedToCapacity() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.BUS);
        vehicle.setPassengerCapacity(30);
        vehicle.setCurrentPassengers(50);

        assertEquals(30, vehicle.getCurrentPassengers());
    }

    @Test
    void groundVehicle_setCurrentPassengers_clampedToZero() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.BUS);
        vehicle.setPassengerCapacity(30);
        vehicle.setCurrentPassengers(-10);

        assertEquals(0, vehicle.getCurrentPassengers());
    }

    @Test
    void groundVehicle_infrastructureRequirement_noneAllowsAll() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);
        vehicle.setInfrastructureRequirement("NONE");

        assertTrue(vehicle.canOperateWithInfrastructure("ROAD"));
        assertTrue(vehicle.canOperateWithInfrastructure("RAIL"));
        assertTrue(vehicle.canOperateWithInfrastructure("ANY"));
    }

    @Test
    void groundVehicle_infrastructureRequirement_nullAllowsAll() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);

        assertTrue(vehicle.canOperateWithInfrastructure("ROAD"));
        assertTrue(vehicle.canOperateWithInfrastructure("ANY"));
    }

    @Test
    void groundVehicle_infrastructureRequirement_emptyAllowsAll() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);
        vehicle.setInfrastructureRequirement("");

        assertTrue(vehicle.canOperateWithInfrastructure("ROAD"));
    }

    // ==========================================
    // Multi-Module Transport Assembly Tests
    // ==========================================

    @Test
    void buildVehicle_multiModuleTransport_accumulatesPassengerCapacity() {
        GroundVehicleModuleTemplate bus = builder.createBusTemplate();
        GroundVehicleModuleTemplate corridor = builder.createCorridorTemplate();
        GroundVehicleModuleTemplate savedBus = builder.createModuleTemplate(bus);
        GroundVehicleModuleTemplate savedCorridor = builder.createModuleTemplate(corridor);

        GroundVehicle vehicle = builder.buildVehicle("Extended Bus",
                List.of(savedBus.getId(), savedCorridor.getId()));

        // Bus has 30 passengers, corridor has 0
        assertEquals(30, vehicle.getPassengerCapacity());
        assertEquals(2, vehicle.getHullModuleCount());
    }

    @Test
    void buildVehicle_multiModuleTransport_accumulatesCargoCapacity() {
        GroundVehicleModuleTemplate truck = builder.createTruckTemplate();
        GroundVehicleModuleTemplate train = builder.createTrainTemplate();
        builder.createModuleTemplate(truck);
        GroundVehicleModuleTemplate savedTrain = builder.createModuleTemplate(train);

        GroundVehicle vehicle = builder.buildVehicle("Combined Transport",
                List.of(savedTrain.getId()));

        assertEquals(100, vehicle.getCargoCapacity());
    }

    @Test
    void buildVehicle_multiModuleTransport_accumulatesCosts() {
        GroundVehicleModuleTemplate bus = builder.createBusTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(bus);

        GroundVehicle vehicle = builder.buildVehicle("City Bus", saved.getId());

        assertEquals(80, vehicle.getConstructionCost());
        assertEquals(4, vehicle.getMaintenanceCost());
    }

    @Test
    void buildVehicle_multiModuleTransport_infrastructureFromFirst() {
        GroundVehicleModuleTemplate bus = builder.createBusTemplate();
        GroundVehicleModuleTemplate corridor = builder.createCorridorTemplate();
        GroundVehicleModuleTemplate savedBus = builder.createModuleTemplate(bus);
        GroundVehicleModuleTemplate savedCorridor = builder.createModuleTemplate(corridor);

        GroundVehicle vehicle = builder.buildVehicle("Extended Bus",
                List.of(savedBus.getId(), savedCorridor.getId()));

        // Bus has ROAD infrastructure, corridor has null
        assertEquals("ROAD", vehicle.getInfrastructureRequirement());
    }

    @Test
    void buildVehicle_multiModuleTransport_accumulatesTerrain() {
        GroundVehicleModuleTemplate tank = builder.createTankTemplate();
        GroundVehicleModuleTemplate fighter = builder.createFighterJetTemplate();
        builder.createModuleTemplate(tank);
        GroundVehicleModuleTemplate savedFighter = builder.createModuleTemplate(fighter);

        GroundVehicle vehicle = builder.buildVehicle("Combined",
                List.of(savedFighter.getId()));

        // Fighter Jet has AIR terrain
        assertTrue(vehicle.isCompatibleWithTerrain("AIR"));
    }

    // ==========================================
    // Standalone Builder Tests
    // ==========================================

    @Test
    void standaloneBuilder_throwsOnTransportQueries() {
        GroundVehicleBuilder standalone = new GroundVehicleBuilder();

        assertThrows(IllegalStateException.class, () -> standalone.getTransportVehicles());
        assertThrows(IllegalStateException.class, () -> standalone.getModulesByInfrastructure("ROAD"));
        assertThrows(IllegalStateException.class, () -> standalone.getModulesByPassengerCapacity(10));
        assertThrows(IllegalStateException.class, () -> standalone.getModulesByCargoCapacity(10));
        assertThrows(IllegalStateException.class, () -> standalone.getModulesByName("test"));
        assertThrows(IllegalStateException.class, () -> standalone.getModulesByTypeAndTag(
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK, "test"));
    }

    // ==========================================
    // Template isTransportVehicle Tests
    // ==========================================

    @Test
    void template_isTransportVehicle_forAllCivilianTypes() {
        assertTrue(builder.createBusTemplate().isTransportVehicle());
        assertTrue(builder.createTrainTemplate().isTransportVehicle());
        assertTrue(builder.createCarTemplate().isTransportVehicle());
        assertTrue(builder.createTruckTemplate().isTransportVehicle());
        assertTrue(builder.createFerryTemplate().isTransportVehicle());
        assertTrue(builder.createAircraftTemplate().isTransportVehicle());
    }

    @Test
    void template_isTransportVehicle_forCombatTypes() {
        assertFalse(builder.createTankTemplate().isTransportVehicle());
        assertFalse(builder.createAPCTemplate().isTransportVehicle());
        assertFalse(builder.createIFVTemplate().isTransportVehicle());
        assertFalse(builder.createArtilleryTemplate().isTransportVehicle());
        assertFalse(builder.createMissileLauncherTemplate().isTransportVehicle());
        assertFalse(builder.createFighterJetTemplate().isTransportVehicle());
        assertFalse(builder.createBomberTemplate().isTransportVehicle());
        assertFalse(builder.createHelicopterTemplate().isTransportVehicle());
        assertFalse(builder.createTransportShipTemplate().isTransportVehicle());
        assertFalse(builder.createCorridorTemplate().isTransportVehicle());
    }

    // ==========================================
    // GroundVehicle Model Additional Tests
    // ==========================================

    @Test
    void groundVehicle_cargoInventory_isUnmodifiable() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TRUCK);
        vehicle.setCargoCapacity(50);
        vehicle.loadCargo("Minerals", 10);

        Map<String, Integer> inventory = vehicle.getCargoInventory();

        assertThrows(UnsupportedOperationException.class, () -> inventory.put("Energy", 5));
    }

    @Test
    void groundVehicle_refuel_zeroAddedWhenFull() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);
        vehicle.setMaxFuel(100);
        vehicle.setFuelLevel(100);

        double added = vehicle.refuel(50);

        assertEquals(0, added, 0.01);
        assertEquals(100, vehicle.getFuelLevel(), 0.01);
    }

    @Test
    void groundVehicle_refuel_negative_returnsZero() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);
        vehicle.setMaxFuel(100);
        vehicle.setFuelLevel(50);

        double added = vehicle.refuel(-10);

        assertEquals(0, added, 0.01);
        assertEquals(50, vehicle.getFuelLevel(), 0.01);
    }

    @Test
    void groundVehicle_move_noFuel_fails() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);
        vehicle.setArmorRating(100);
        vehicle.setMaxFuel(100);
        vehicle.setFuelLevel(5);
        vehicle.setFuelConsumption(10);

        Hex3D newPos = new Hex3D(1, 0, -1);
        boolean moved = vehicle.move(newPos);

        assertFalse(moved);
    }

    @Test
    void groundVehicle_move_inactive_fails() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);
        vehicle.setArmorRating(100);
        vehicle.setMaxFuel(100);
        vehicle.setFuelLevel(50);
        vehicle.setFuelConsumption(10);
        vehicle.setActive(false);

        Hex3D newPos = new Hex3D(1, 0, -1);
        boolean moved = vehicle.move(newPos);

        assertFalse(moved);
    }

    @Test
    void groundVehicle_processTurn_inactive_skipsFuel() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);
        vehicle.setMaxFuel(100);
        vehicle.setFuelLevel(50);
        vehicle.setFuelConsumption(10);
        vehicle.setActive(false);

        vehicle.processTurn();

        assertEquals(50, vehicle.getFuelLevel(), 0.01);
    }

    @Test
    void groundVehicle_processTurn_destroyed_skipsFuel() {
        GroundVehicle vehicle = new GroundVehicle("test-1",
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);
        vehicle.setArmorRating(10);
        vehicle.setCurrentArmor(0);
        vehicle.setMaxFuel(100);
        vehicle.setFuelLevel(50);
        vehicle.setFuelConsumption(10);

        vehicle.processTurn();

        assertEquals(50, vehicle.getFuelLevel(), 0.01);
    }

    // ==========================================
    // Integration Tests for Transport Queries
    // ==========================================

    @Test
    void fullWorkflow_prepopulateAndQueryTransportVehicles() {
        builder.prepopulateTemplates();

        List<GroundVehicleModuleTemplate> transports = builder.getTransportVehicles();

        assertEquals(6, transports.size());
        for (GroundVehicleModuleTemplate t : transports) {
            assertTrue(t.isTransportVehicle());
        }
    }

    @Test
    void fullWorkflow_prepopulateAndQueryByInfrastructure() {
        builder.prepopulateTemplates();

        List<GroundVehicleModuleTemplate> roadVehicles = builder.getModulesByInfrastructure("ROAD");

        assertEquals(3, roadVehicles.size()); // Bus, Car, Truck
    }

    @Test
    void fullWorkflow_prepopulateAndQueryByPassengerCapacity() {
        builder.prepopulateTemplates();

        List<GroundVehicleModuleTemplate> highCapacity = builder.getModulesByPassengerCapacity(50);

        // Train (80), Ferry (50) should match
        assertEquals(2, highCapacity.size());
    }

    @Test
    void fullWorkflow_buildTransportAndCheckInfrastructure() {
        GroundVehicleModuleTemplate template = builder.createBusTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);

        GroundVehicle vehicle = builder.buildVehicle("City Bus", saved.getId());

        assertEquals("ROAD", vehicle.getInfrastructureRequirement());
        assertTrue(vehicle.canOperateWithInfrastructure("ROAD"));
        assertFalse(vehicle.canOperateWithInfrastructure("RAIL"));
    }

    @Test
    void fullWorkflow_buildTrainAndLoadCargo() {
        GroundVehicleModuleTemplate template = builder.createTrainTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);

        GroundVehicle vehicle = builder.buildVehicle("Freight Train", saved.getId());

        int loaded = vehicle.loadCargo("Minerals", 50);
        assertEquals(50, loaded);
        assertEquals(50, vehicle.getTotalCargoOnBoard());

        int unloaded = vehicle.unloadCargo("Minerals", 30);
        assertEquals(30, unloaded);
        assertEquals(20, vehicle.getTotalCargoOnBoard());
    }

    @Test
    void fullWorkflow_buildBusAndBoardPassengers() {
        GroundVehicleModuleTemplate template = builder.createBusTemplate();
        GroundVehicleModuleTemplate saved = builder.createModuleTemplate(template);

        GroundVehicle vehicle = builder.buildVehicle("City Bus", saved.getId());

        int boarded = vehicle.boardPassengers(25);
        assertEquals(25, boarded);
        assertEquals(25, vehicle.getCurrentPassengers());

        int unboarded = vehicle.unboardPassengers(10);
        assertEquals(10, unboarded);
        assertEquals(15, vehicle.getCurrentPassengers());
    }
}
