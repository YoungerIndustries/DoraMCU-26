package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.gamecore.DungeonRoomService;
import com.yisy.modules.doramcu.hex.dungeon.GroundVehicleModuleTemplate;
import com.yisy.modules.doramcu.hex.dungeon.GroundVehicleModuleTemplateRepository;
import com.yisy.modules.doramcu.hex.planet.BuildingInteriorGenerator;
import com.yisy.modules.doramcu.hex.spi.AiEntityRegistration;
import com.yisy.modules.doramcu.hex.spi.AssetRegistrySpi;
import com.yisy.modules.doramcu.hex.Skin;
import com.yisy.modules.doramcu.hex.spi.SkinLookupSpi;
import com.yisy.modules.doramcu.hex.SkinType;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for building ground combat vehicles by combining 3D hull modules with interior dungeon rooms.
 * <p>
 * The GroundVehicleBuilder orchestrates two dungeon creation paradigms:
 * </p>
 * <ol>
 *   <li><b>Exterior Hull (3D Template Method)</b> — Uses {@link GroundVehicleModuleTemplate} instances
 *       to create the vehicle's 3D structure. Each template becomes a module section with
 *       {@link HullModule} instances, {@link Compartment} objects for named functional areas,
 *       and connection points for inter-module doorways.</li>
 *   <li><b>Interior Rooms (Graph-Based Method)</b> — Uses {@link BuildingInteriorGenerator}
 *       to create explorable interior rooms with bidirectional connections, traps, treasures,
 *       and encounters for large vehicles.</li>
 * </ol>
 * <p>
 * The bridge between the two paradigms is {@link CompartmentType}:
 * </p>
 * <ul>
 *   <li>Exterior hull → {@link Compartment} objects with {@link CompartmentType} (TURRET, ENGINE_BAY, etc.)</li>
 *   <li>Interior rooms → {@link DungeonRoomService.DungeonRoom} objects with theme (Social, Combat, Storage, etc.)</li>
 *   <li>Mapping: {@link CompartmentType#fromString(String)} provides fuzzy mapping between domain-specific types</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 * // Create vehicle module templates
 * GroundVehicleModuleTemplate tank = builder.createTankTemplate();
 * GroundVehicleModuleTemplate apc = builder.createAPCTemplate();
 *
 * // Build a vehicle from templates
 * GroundVehicle vehicle = builder.buildVehicle("M1 Abrams",
 *     Arrays.asList(tank.getId()));
 *
 * // Generate interior rooms for the vehicle
 * String dungeonId = builder.generateVehicleInteriors(vehicle, TechLevel.TL8);
 * </pre>
 */
@Service
public class GroundVehicleBuilder {

    private final GroundVehicleModuleTemplateRepository templateRepository;
    private final DungeonRoomService dungeonRoomService;
    private final TechLevelService techLevelService;
    private final AiEntityRegistration aiEntityRegistrationService;
    private final AssetRegistrySpi AssetRegistrySpi;
    private final SkinLookupSpi SkinLookupSpi;

    private final AtomicLong nextId = new AtomicLong(1);

    /**
     * Creates a new GroundVehicleBuilder with Spring-managed dependencies.
     *
     * @param templateRepository the JPA repository for ground vehicle module templates
     * @param dungeonRoomService the service for creating interior dungeon rooms
     * @param techLevelService the service for tech-level-aware module lookup (may be null)
     * @param aiEntityRegistrationService the service for AI entity registration
     * @param AssetRegistrySpi the central asset registry for skin assignment
     * @param SkinLookupSpi the service for skin management
     */
    public GroundVehicleBuilder(GroundVehicleModuleTemplateRepository templateRepository,
                                DungeonRoomService dungeonRoomService,
                                TechLevelService techLevelService,
                                AiEntityRegistration aiEntityRegistrationService,
                                AssetRegistrySpi AssetRegistrySpi,
                                SkinLookupSpi SkinLookupSpi) {
        this.templateRepository = templateRepository;
        this.dungeonRoomService = dungeonRoomService;
        this.techLevelService = techLevelService;
        this.aiEntityRegistrationService = aiEntityRegistrationService;
        this.AssetRegistrySpi = AssetRegistrySpi;
        this.SkinLookupSpi = SkinLookupSpi;
    }

    /**
     * Creates a new GroundVehicleBuilder without repository (for legacy/standalone use).
     * <p>
     * In this mode, template CRUD operations are not available.
     * </p>
     */
    public GroundVehicleBuilder() {
        this.templateRepository = null;
        this.dungeonRoomService = null;
        this.techLevelService = null;
        this.aiEntityRegistrationService = null;
        this.AssetRegistrySpi = null;
        this.SkinLookupSpi = null;
    }

    // ==========================================
    // Ground Vehicle Module Template CRUD
    // ==========================================

    /**
     * Creates a new ground vehicle module template and persists it.
     *
     * @param template the template to create
     * @return the saved template
     * @throws IllegalStateException if repository is not available
     */
    public GroundVehicleModuleTemplate createModuleTemplate(GroundVehicleModuleTemplate template) {
        assertRepository();
        return templateRepository.save(template);
    }

    /**
     * Updates an existing ground vehicle module template.
     *
     * @param template the template to update
     * @return the saved template
     * @throws IllegalArgumentException if the template ID does not exist
     * @throws IllegalStateException if repository is not available
     */
    public GroundVehicleModuleTemplate updateModuleTemplate(GroundVehicleModuleTemplate template) {
        assertRepository();
        if (!templateRepository.existsById(template.getId())) {
            throw new IllegalArgumentException("Template not found: " + template.getId());
        }
        return templateRepository.save(template);
    }

    /**
     * Deletes a ground vehicle module template by ID.
     *
     * @param id the template ID
     * @throws IllegalStateException if repository is not available
     */
    public void deleteModuleTemplate(Long id) {
        assertRepository();
        templateRepository.deleteById(id);
    }

    /**
     * Returns all ground vehicle module templates.
     *
     * @return list of all templates
     * @throws IllegalStateException if repository is not available
     */
    public List<GroundVehicleModuleTemplate> getModuleTemplates() {
        assertRepository();
        return templateRepository.findAll();
    }

    /**
     * Returns a ground vehicle module template by ID.
     *
     * @param id the template ID
     * @return the template or empty if not found
     * @throws IllegalStateException if repository is not available
     */
    public Optional<GroundVehicleModuleTemplate> getModuleTemplate(Long id) {
        assertRepository();
        return templateRepository.findById(id);
    }

    /**
     * Returns templates filtered by module type.
     *
     * @param type the ground vehicle module type
     * @return list of matching templates
     * @throws IllegalStateException if repository is not available
     */
    public List<GroundVehicleModuleTemplate> getModulesByType(GroundVehicleModuleTemplate.GroundVehicleModuleType type) {
        assertRepository();
        return templateRepository.findByType(type);
    }

    /**
     * Returns templates filtered by tag.
     *
     * @param tag the tag to search for
     * @return list of matching templates
     * @throws IllegalStateException if repository is not available
     */
    public List<GroundVehicleModuleTemplate> getModulesByTag(String tag) {
        assertRepository();
        return templateRepository.findByTagContaining(tag);
    }

    /**
     * Returns templates compatible with the given terrain type.
     *
     * @param terrain the terrain type name
     * @return list of compatible templates
     * @throws IllegalStateException if repository is not available
     */
    public List<GroundVehicleModuleTemplate> getModulesForTerrain(String terrain) {
        assertRepository();
        return templateRepository.findByCompatibleTerrainContaining(terrain);
    }

    /**
     * Returns templates with armor rating less than or equal to the given value.
     *
     * @param maxArmor the maximum armor rating
     * @return list of matching templates
     * @throws IllegalStateException if repository is not available
     */
    public List<GroundVehicleModuleTemplate> getModulesByArmor(int maxArmor) {
        assertRepository();
        return templateRepository.findByArmorRatingLessThanEqual(maxArmor);
    }

    /**
     * Returns templates with difficulty less than or equal to the given value.
     *
     * @param maxDifficulty the maximum difficulty
     * @return list of matching templates
     * @throws IllegalStateException if repository is not available
     */
    public List<GroundVehicleModuleTemplate> getModulesByDifficulty(int maxDifficulty) {
        assertRepository();
        return templateRepository.findByDifficultyLessThanEqual(maxDifficulty);
    }

    /**
     * Returns all templates ordered by firepower descending.
     *
     * @return list of templates sorted by firepower
     * @throws IllegalStateException if repository is not available
     */
    public List<GroundVehicleModuleTemplate> getModulesOrderedByFirepower() {
        assertRepository();
        return templateRepository.findAllOrderByFirepowerDesc();
    }

    /**
     * Returns all templates ordered by difficulty ascending.
     *
     * @return list of templates sorted by difficulty
     * @throws IllegalStateException if repository is not available
     */
    public List<GroundVehicleModuleTemplate> getModulesOrderedByDifficulty() {
        assertRepository();
        return templateRepository.findAllOrderByDifficultyAsc();
    }

    /**
     * Returns templates matching the given infrastructure requirement.
     *
     * @param infrastructure the infrastructure requirement (NONE, ROAD, RAIL, WATER, AIRPORT)
     * @return list of matching templates
     * @throws IllegalStateException if repository is not available
     */
    public List<GroundVehicleModuleTemplate> getModulesByInfrastructure(String infrastructure) {
        assertRepository();
        return templateRepository.findByInfrastructureRequirement(infrastructure);
    }

    /**
     * Returns templates with passenger capacity greater than or equal to the given value.
     *
     * @param minPassengers the minimum passenger capacity
     * @return list of matching templates
     * @throws IllegalStateException if repository is not available
     */
    public List<GroundVehicleModuleTemplate> getModulesByPassengerCapacity(int minPassengers) {
        assertRepository();
        return templateRepository.findByPassengerCapacityGreaterThanEqual(minPassengers);
    }

    /**
     * Returns templates with cargo capacity greater than or equal to the given value.
     *
     * @param minCargo the minimum cargo capacity
     * @return list of matching templates
     * @throws IllegalStateException if repository is not available
     */
    public List<GroundVehicleModuleTemplate> getModulesByCargoCapacity(int minCargo) {
        assertRepository();
        return templateRepository.findByCargoCapacityGreaterThanEqual(minCargo);
    }

    /**
     * Returns civilian transport vehicle templates (non-combat).
     *
     * @return list of transport vehicle templates
     * @throws IllegalStateException if repository is not available
     */
    public List<GroundVehicleModuleTemplate> getTransportVehicles() {
        assertRepository();
        List<GroundVehicleModuleTemplate.GroundVehicleModuleType> transportTypes = Arrays.asList(
                GroundVehicleModuleTemplate.GroundVehicleModuleType.BUS,
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TRAIN,
                GroundVehicleModuleTemplate.GroundVehicleModuleType.CAR,
                GroundVehicleModuleTemplate.GroundVehicleModuleType.TRUCK,
                GroundVehicleModuleTemplate.GroundVehicleModuleType.FERRY,
                GroundVehicleModuleTemplate.GroundVehicleModuleType.AIRCRAFT
        );
        return templateRepository.findTransportVehicles(transportTypes);
    }

    /**
     * Returns templates whose name contains the given string (case-insensitive).
     *
     * @param name the name substring to search for
     * @return list of matching templates
     * @throws IllegalStateException if repository is not available
     */
    public List<GroundVehicleModuleTemplate> getModulesByName(String name) {
        assertRepository();
        return templateRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Returns templates matching both type and a tag.
     *
     * @param type the ground vehicle module type
     * @param tag the tag to search for
     * @return list of matching templates
     * @throws IllegalStateException if repository is not available
     */
    public List<GroundVehicleModuleTemplate> getModulesByTypeAndTag(
            GroundVehicleModuleTemplate.GroundVehicleModuleType type, String tag) {
        assertRepository();
        return templateRepository.findByTypeAndTagContaining(type, tag);
    }

    private void assertRepository() {
        if (templateRepository == null) {
            throw new IllegalStateException("GroundVehicleBuilder not initialized with repository; use Spring-managed constructor");
        }
    }

    // ==========================================
    // Vehicle Assembly from Templates
    // ==========================================

    /**
     * Builds a complete GroundVehicle from ground vehicle module templates.
     * <p>
     * Each template becomes a module section in the vehicle. Templates are processed
     * in order, and connection points are used to create inter-module doorways.
     * The vehicle's armor, firepower, speed, and crew capacity are calculated from
     * the module contributions.
     * </p>
     *
     * @param name the vehicle name
     * @param templateIds list of template IDs to use (in module order)
     * @return the assembled GroundVehicle
     * @throws IllegalArgumentException if one or more templates are not found
     * @throws IllegalStateException if repository is not available
     */
    public GroundVehicle buildVehicle(String name, List<Long> templateIds) {
        assertRepository();
        List<GroundVehicleModuleTemplate> templates = templateRepository.findAllById(templateIds);
        if (templates.size() != templateIds.size()) {
            throw new IllegalArgumentException("One or more templates not found");
        }

        // Determine type from the first non-CORRIDOR template
        GroundVehicleModuleTemplate.GroundVehicleModuleType type = GroundVehicleModuleTemplate.GroundVehicleModuleType.EMPTY;
        for (GroundVehicleModuleTemplate t : templates) {
            if (t.getType() != GroundVehicleModuleTemplate.GroundVehicleModuleType.CORRIDOR &&
                t.getType() != GroundVehicleModuleTemplate.GroundVehicleModuleType.EMPTY) {
                type = t.getType();
                break;
            }
        }

        // Create the vehicle
        String id = "GV-" + nextId.getAndIncrement();
        GroundVehicle vehicle = new GroundVehicle(id, type);
        vehicle.setName(name);

        // Calculate total contributions from templates
        int totalArmor = 0;
        int totalFirepower = 0;
        int maxSpeed = 0;
        int totalCrew = 0;
        double totalFuelConsumption = 0;
        int maxDetectionRange = 0;
        int totalSupplyCost = 0;
        int totalPassengerCapacity = 0;
        int totalCargoCapacity = 0;
        int totalConstructionCost = 0;
        int totalMaintenanceCost = 0;
        String infrastructureReq = null;
        Set<String> terrainSet = new HashSet<>();

        for (GroundVehicleModuleTemplate template : templates) {
            HullModule module = template.toHullModule();
            vehicle.addHullModule(module);

            // Accumulate combat contributions
            totalArmor += template.getArmorRating();
            totalFirepower += template.getFirepower();
            maxSpeed = Math.max(maxSpeed, template.getSpeedKmh());
            totalCrew += template.getCrewCapacity();
            totalFuelConsumption += template.getFuelConsumption();
            maxDetectionRange = Math.max(maxDetectionRange, template.getDetectionRange());
            totalSupplyCost += template.getSupplyCost();

            // Accumulate transport contributions
            totalPassengerCapacity += template.getPassengerCapacity();
            totalCargoCapacity += template.getCargoCapacity();
            totalConstructionCost += template.getConstructionCost();
            totalMaintenanceCost += template.getMaintenanceCost();

            // Use the first non-null infrastructure requirement
            if (infrastructureReq == null && template.getInfrastructureRequirement() != null) {
                infrastructureReq = template.getInfrastructureRequirement();
            }

            // Collect terrain compatibility
            if (template.getCompatibleTerrain() != null && !template.getCompatibleTerrain().isBlank()) {
                for (String t : template.getCompatibleTerrain().split(",")) {
                    terrainSet.add(t.trim().toUpperCase());
                }
            }
        }

        // Apply accumulated combat stats
        vehicle.setArmorRating(totalArmor);
        vehicle.setFirepower(totalFirepower);
        vehicle.setSpeedKmh(maxSpeed);
        vehicle.setCrewCapacity(totalCrew);
        vehicle.setFuelConsumption(totalFuelConsumption);
        vehicle.setMaxFuel(totalFuelConsumption * 10); // 10 turns of fuel
        vehicle.setFuelLevel(totalFuelConsumption * 10); // Start full
        vehicle.setDetectionRange(maxDetectionRange);
        vehicle.setSupplyCost(totalSupplyCost);

        // Apply accumulated transport stats
        vehicle.setPassengerCapacity(totalPassengerCapacity);
        vehicle.setCargoCapacity(totalCargoCapacity);
        vehicle.setConstructionCost(totalConstructionCost);
        vehicle.setMaintenanceCost(totalMaintenanceCost);
        if (infrastructureReq != null) {
            vehicle.setInfrastructureRequirement(infrastructureReq);
        }

        // Set terrain compatibility
        if (!terrainSet.isEmpty()) {
            vehicle.setCompatibleTerrain(String.join(",", terrainSet));
        }

        // Install AI Core for network connection
        vehicle.setAiCore(new AiCoreComponent());

        // Register as AI character
        registerVehicleAsAi(vehicle, name);

        // Register with asset registry and assign default skin
        registerVehicle(vehicle);

        return vehicle;
    }

    /**
     * Registers a ground vehicle with the asset registry and assigns a default skin.
     */
    private void registerVehicle(GroundVehicle vehicle) {
        if (AssetRegistrySpi == null) {
            return;
        }
        // GroundVehicle uses String IDs; extract numeric part for registry
        String idStr = vehicle.getId();
        int id;
        try {
            id = Integer.parseInt(idStr.replace("GV-", ""));
        } catch (NumberFormatException e) {
            id = idStr.hashCode();
        }
        AssetRegistrySpi.register(SkinType.GROUND_VEHICLE, id, vehicle);
        if (SkinLookupSpi != null && vehicle.getSkin() == null) {
            Skin defaultSkin = SkinLookupSpi.getSkin("military-vehicle");
            if (defaultSkin != null) {
                vehicle.setSkin(defaultSkin);
            }
        }
    }

    /**
     * Registers a ground vehicle as an AI character in the AI character repository.
     * No-op if the AI entity registration service is not available.
     */
    private void registerVehicleAsAi(GroundVehicle vehicle, String vehicleName) {
        if (aiEntityRegistrationService != null && vehicle != null) {
            try {
                aiEntityRegistrationService.registerVehicle(vehicle, vehicleName);
            } catch (Exception e) {
                // Silently ignore if registration fails
            }
        }
    }

    /**
     * Builds a GroundVehicle from a single template.
     *
     * @param name the vehicle name
     * @param templateId the template ID
     * @return the assembled GroundVehicle
     */
    public GroundVehicle buildVehicle(String name, Long templateId) {
        return buildVehicle(name, Collections.singletonList(templateId));
    }

    // ==========================================
    // Interior Room Generation
    // ==========================================

    /**
     * Generates interior dungeon rooms for a ground vehicle.
     * <p>
     * Uses {@link BuildingInteriorGenerator} to create explorable interior rooms
     * for large vehicles (tanks, APCs, aircraft). The rooms are tech-level aware:
     * higher tech levels produce sci-fi themed rooms.
     * </p>
     *
     * @param vehicle the vehicle to generate interiors for
     * @param techLevel the technology level (determines room names and features)
     * @param seed random seed for reproducible generation
     * @return the dungeon ID for the generated interior rooms
     */
    public String generateVehicleInteriors(GroundVehicle vehicle, TechLevel techLevel, long seed) {
        if (dungeonRoomService == null) {
            throw new IllegalStateException("GroundVehicleBuilder not initialized with dungeonRoomService");
        }
        String dungeonId = vehicle.getName() + "-interior";

        BuildingInteriorGenerator generator = new BuildingInteriorGenerator(
                dungeonRoomService, techLevelService, seed
        );

        // Use Warehouse template for vehicle interiors (loading dock, storage, office pattern)
        return generator.generateWarehouse(dungeonId, techLevel);
    }

    /**
     * Generates interior dungeon rooms with default tech level (TL8 for vehicles).
     *
     * @param vehicle the vehicle to generate interiors for
     * @param seed random seed for reproducible generation
     * @return the dungeon ID for the generated interior rooms
     */
    public String generateVehicleInteriors(GroundVehicle vehicle, long seed) {
        return generateVehicleInteriors(vehicle, TechLevel.TL8, seed);
    }

    /**
     * Generates interior dungeon rooms with a random seed.
     *
     * @param vehicle the vehicle to generate interiors for
     * @param techLevel the technology level
     * @return the dungeon ID for the generated interior rooms
     */
    public String generateVehicleInteriors(GroundVehicle vehicle, TechLevel techLevel) {
        return generateVehicleInteriors(vehicle, techLevel, new Random().nextLong());
    }

    // ==========================================
    // Compartment-to-Room Mapping
    // ==========================================

    /**
     * Maps hull compartments to interior dungeon rooms by matching compartment types
     * with room themes.
     * <p>
     * This creates a bridge between the 3D hull model and the graph-based room model,
     * allowing navigation between compartment cells and dungeon rooms.
     * </p>
     *
     * @param vehicle the vehicle with compartments
     * @param dungeonId the dungeon ID of the interior rooms
     * @return a map of compartment names to their matched dungeon room IDs
     */
    public Map<String, Long> mapCompartmentsToRooms(GroundVehicle vehicle, String dungeonId) {
        if (dungeonRoomService == null) {
            return Collections.emptyMap();
        }
        Map<String, Long> mapping = new HashMap<>();
        List<DungeonRoomService.DungeonRoom> rooms = dungeonRoomService.listRoomsByDungeon(dungeonId);

        // Iterate over hull modules and their compartments
        for (HullModule module : vehicle.getHullModules()) {
            for (Compartment compartment : module.getCompartments()) {
                String compartmentName = compartment.getName();
                CompartmentType type = compartment.getType();

                // Find a matching room by theme
                for (DungeonRoomService.DungeonRoom room : rooms) {
                    String theme = room.getTheme();
                    if (themeMatchesType(theme, type)) {
                        mapping.put(compartmentName, room.getId());
                        break;
                    }
                }
            }
        }

        return mapping;
    }

    /**
     * Checks if a room theme matches a compartment type.
     *
     * @param theme the room theme (e.g., "Social", "Combat", "Storage")
     * @param type the compartment type
     * @return true if the theme matches the type
     */
    private boolean themeMatchesType(String theme, CompartmentType type) {
        if (theme == null || type == null) return false;

        return switch (theme.toLowerCase()) {
            case "social" -> type == CompartmentType.CREW ||
                             type == CompartmentType.CREW_HATCH ||
                             type == CompartmentType.TRANSIT;
            case "combat" -> type == CompartmentType.WEAPONS ||
                             type == CompartmentType.DEFENSE ||
                             type == CompartmentType.TURRET ||
                             type == CompartmentType.LAUNCHER;
            case "storage" -> type == CompartmentType.CARGO ||
                              type == CompartmentType.VAULT ||
                              type == CompartmentType.FUEL_TANK;
            case "crafting" -> type == CompartmentType.ENGINE ||
                               type == CompartmentType.ENGINE_BAY ||
                               type == CompartmentType.RESEARCH ||
                               type == CompartmentType.SENSOR_ARRAY;
            default -> false;
        };
    }

    // ==========================================
    // Pre-population of Vehicle Templates
    // ==========================================

    /**
     * Pre-populates the database with starter ground vehicle module templates.
     * <p>
     * Creates templates for all ground vehicle module types (combat and civilian transport)
     * with default dimensions, compartments, and connection points. Called once during initialization.
     * </p>
     *
     * @return the number of templates created
     */
    public int prepopulateTemplates() {
        assertRepository();
        if (templateRepository.count() > 0) {
            return 0;
        }

        List<GroundVehicleModuleTemplate> templates = new ArrayList<>();

        // Core combat vehicles
        templates.add(createTankTemplate());
        templates.add(createAPCTemplate());
        templates.add(createIFVTemplate());
        templates.add(createArtilleryTemplate());
        templates.add(createMissileLauncherTemplate());

        // Air combat vehicles
        templates.add(createFighterJetTemplate());
        templates.add(createBomberTemplate());
        templates.add(createHelicopterTemplate());

        // Support vehicles
        templates.add(createTransportShipTemplate());
        templates.add(createCorridorTemplate());

        // Civilian transport vehicles
        templates.add(createBusTemplate());
        templates.add(createTrainTemplate());
        templates.add(createCarTemplate());
        templates.add(createTruckTemplate());
        templates.add(createFerryTemplate());
        templates.add(createAircraftTemplate());

        templateRepository.saveAll(templates);
        return templates.size();
    }

    // ==========================================
    // Factory Methods for Vehicle Templates
    // ==========================================

    /**
     * Creates a main battle tank template.
     *
     * @return the tank template
     */
    public GroundVehicleModuleTemplate createTankTemplate() {
        GroundVehicleModuleTemplate template = new GroundVehicleModuleTemplate();
        template.setName("Main Battle Tank");
        template.setType(GroundVehicleModuleTemplate.GroundVehicleModuleType.TANK);
        template.setDescription("Heavy armored assault vehicle with large caliber main gun");
        template.setWidth(8);
        template.setHeight(3);
        template.setDepth(3);
        template.setShapeCellsJson("[\"0,0,0\",\"1,0,0\",\"2,0,0\",\"3,0,0\",\"4,0,0\",\"5,0,0\",\"6,0,0\",\"7,0,0\",\"0,1,0\",\"1,1,0\",\"2,1,0\",\"3,1,0\",\"4,1,0\",\"5,1,0\",\"6,1,0\",\"7,1,0\"]");
        template.setCompartmentsJson("[{\"name\":\"Main Turret\",\"type\":\"TURRET\",\"cells\":[\"0,0,0\",\"1,0,0\",\"2,0,0\"]},{\"name\":\"Crew Compartment\",\"type\":\"CREW\",\"cells\":[\"3,0,0\",\"4,0,0\"]},{\"name\":\"Engine Bay\",\"type\":\"ENGINE_BAY\",\"cells\":[\"5,0,0\",\"6,0,0\",\"7,0,0\"]},{\"name\":\"Ammo Storage\",\"type\":\"CARGO\",\"cells\":[\"3,1,0\",\"4,1,0\"]},{\"name\":\"Fuel Tank\",\"type\":\"FUEL_TANK\",\"cells\":[\"5,1,0\",\"6,1,0\",\"7,1,0\"]}]");
        template.setConnectionPointsJson("[{\"x\":4,\"y\":0,\"z\":0,\"direction\":\"front\"}]");
        template.setArmorRating(35);
        template.setFirepower(25);
        template.setSpeedKmh(67);
        template.setCrewCapacity(4);
        template.setFuelConsumption(2.5);
        template.setDetectionRange(3);
        template.setCompatibleTerrain("OPEN,URBAN,DESERT");
        template.setSupplyCost(3);
        template.setDifficulty(5);
        template.setTags("tank,heavy,armor,assault,ground");
        return template;
    }

    /**
     * Creates an armored personnel carrier template.
     *
     * @return the APC template
     */
    public GroundVehicleModuleTemplate createAPCTemplate() {
        GroundVehicleModuleTemplate template = new GroundVehicleModuleTemplate();
        template.setName("Armored Personnel Carrier");
        template.setType(GroundVehicleModuleTemplate.GroundVehicleModuleType.APC);
        template.setDescription("Troop transport with light armor and machine gun");
        template.setWidth(6);
        template.setHeight(3);
        template.setDepth(3);
        template.setShapeCellsJson("[\"0,0,0\",\"1,0,0\",\"2,0,0\",\"3,0,0\",\"4,0,0\",\"5,0,0\",\"0,1,0\",\"1,1,0\",\"2,1,0\",\"3,1,0\",\"4,1,0\",\"5,1,0\"]");
        template.setCompartmentsJson("[{\"name\":\"Crew Hatch\",\"type\":\"CREW_HATCH\",\"cells\":[\"0,0,0\"]},{\"name\":\"Crew Compartment\",\"type\":\"CREW\",\"cells\":[\"1,0,0\",\"2,0,0\"]},{\"name\":\"Troop Bay\",\"type\":\"CARGO\",\"cells\":[\"3,0,0\",\"4,0,0\",\"5,0,0\"]},{\"name\":\"Engine Bay\",\"type\":\"ENGINE_BAY\",\"cells\":[\"0,1,0\",\"1,1,0\"]},{\"name\":\"Machine Gun\",\"type\":\"WEAPONS\",\"cells\":[\"2,1,0\"]}]");
        template.setConnectionPointsJson("[{\"x\":3,\"y\":0,\"z\":0,\"direction\":\"front\"}]");
        template.setArmorRating(15);
        template.setFirepower(8);
        template.setSpeedKmh(65);
        template.setCrewCapacity(11);
        template.setFuelConsumption(2.0);
        template.setDetectionRange(2);
        template.setCompatibleTerrain("OPEN,URBAN,JUNGLE");
        template.setSupplyCost(2);
        template.setDifficulty(3);
        template.setTags("apc,troop,transport,armored");
        return template;
    }

    /**
     * Creates an infantry fighting vehicle template.
     *
     * @return the IFV template
     */
    public GroundVehicleModuleTemplate createIFVTemplate() {
        GroundVehicleModuleTemplate template = new GroundVehicleModuleTemplate();
        template.setName("Infantry Fighting Vehicle");
        template.setType(GroundVehicleModuleTemplate.GroundVehicleModuleType.IFV);
        template.setDescription("APC with integrated weapon system and auto-cannon");
        template.setWidth(7);
        template.setHeight(3);
        template.setDepth(3);
        template.setShapeCellsJson("[\"0,0,0\",\"1,0,0\",\"2,0,0\",\"3,0,0\",\"4,0,0\",\"5,0,0\",\"6,0,0\",\"0,1,0\",\"1,1,0\",\"2,1,0\",\"3,1,0\",\"4,1,0\",\"5,1,0\",\"6,1,0\"]");
        template.setCompartmentsJson("[{\"name\":\"Turret\",\"type\":\"TURRET\",\"cells\":[\"0,0,0\",\"1,0,0\"]},{\"name\":\"Crew Compartment\",\"type\":\"CREW\",\"cells\":[\"2,0,0\",\"3,0,0\"]},{\"name\":\"Troop Bay\",\"type\":\"CARGO\",\"cells\":[\"4,0,0\",\"5,0,0\",\"6,0,0\"]},{\"name\":\"Engine Bay\",\"type\":\"ENGINE_BAY\",\"cells\":[\"0,1,0\",\"1,1,0\"]},{\"name\":\"Ammo Storage\",\"type\":\"CARGO\",\"cells\":[\"2,1,0\"]}]");
        template.setConnectionPointsJson("[{\"x\":3,\"y\":0,\"z\":0,\"direction\":\"front\"}]");
        template.setArmorRating(20);
        template.setFirepower(15);
        template.setSpeedKmh(65);
        template.setCrewCapacity(9);
        template.setFuelConsumption(2.2);
        template.setDetectionRange(3);
        template.setCompatibleTerrain("OPEN,URBAN,JUNGLE,DESERT");
        template.setSupplyCost(2);
        template.setDifficulty(4);
        template.setTags("ifv,infantry,fighting,auto-cannon");
        return template;
    }

    /**
     * Creates a self-propelled artillery template.
     *
     * @return the artillery template
     */
    public GroundVehicleModuleTemplate createArtilleryTemplate() {
        GroundVehicleModuleTemplate template = new GroundVehicleModuleTemplate();
        template.setName("Self-Propelled Artillery");
        template.setType(GroundVehicleModuleTemplate.GroundVehicleModuleType.ARTILLERY);
        template.setDescription("Mobile howitzer for long-range bombardment support");
        template.setWidth(8);
        template.setHeight(3);
        template.setDepth(3);
        template.setShapeCellsJson("[\"0,0,0\",\"1,0,0\",\"2,0,0\",\"3,0,0\",\"4,0,0\",\"5,0,0\",\"6,0,0\",\"7,0,0\",\"0,1,0\",\"1,1,0\",\"2,1,0\",\"3,1,0\",\"4,1,0\",\"5,1,0\",\"6,1,0\",\"7,1,0\"]");
        template.setCompartmentsJson("[{\"name\":\"Howitzer\",\"type\":\"LAUNCHER\",\"cells\":[\"0,0,0\",\"1,0,0\",\"2,0,0\"]},{\"name\":\"Crew Compartment\",\"type\":\"CREW\",\"cells\":[\"3,0,0\",\"4,0,0\"]},{\"name\":\"Ammo Storage\",\"type\":\"CARGO\",\"cells\":[\"5,0,0\",\"6,0,0\"]},{\"name\":\"Engine Bay\",\"type\":\"ENGINE_BAY\",\"cells\":[\"0,1,0\",\"1,1,0\",\"2,1,0\"]},{\"name\":\"Fuel Tank\",\"type\":\"FUEL_TANK\",\"cells\":[\"5,1,0\",\"6,1,0\",\"7,1,0\"]}]");
        template.setConnectionPointsJson("[{\"x\":4,\"y\":0,\"z\":0,\"direction\":\"front\"}]");
        template.setArmorRating(10);
        template.setFirepower(40);
        template.setSpeedKmh(50);
        template.setCrewCapacity(5);
        template.setFuelConsumption(2.0);
        template.setDetectionRange(5);
        template.setCompatibleTerrain("OPEN,DESERT");
        template.setSupplyCost(3);
        template.setDifficulty(5);
        template.setTags("artillery,howitzer,bombardment,long-range");
        return template;
    }

    /**
     * Creates a missile launcher template.
     *
     * @return the missile launcher template
     */
    public GroundVehicleModuleTemplate createMissileLauncherTemplate() {
        GroundVehicleModuleTemplate template = new GroundVehicleModuleTemplate();
        template.setName("Strategic Missile Launcher");
        template.setType(GroundVehicleModuleTemplate.GroundVehicleModuleType.MISSILE_LAUNCHER);
        template.setDescription("Long-range strategic strike platform");
        template.setWidth(10);
        template.setHeight(3);
        template.setDepth(3);
        template.setShapeCellsJson("[\"0,0,0\",\"1,0,0\",\"2,0,0\",\"3,0,0\",\"4,0,0\",\"5,0,0\",\"6,0,0\",\"7,0,0\",\"8,0,0\",\"9,0,0\",\"0,1,0\",\"1,1,0\",\"2,1,0\",\"3,1,0\",\"4,1,0\",\"5,1,0\",\"6,1,0\",\"7,1,0\",\"8,1,0\",\"9,1,0\"]");
        template.setCompartmentsJson("[{\"name\":\"Missile Racks\",\"type\":\"LAUNCHER\",\"cells\":[\"0,0,0\",\"1,0,0\",\"2,0,0\",\"3,0,0\",\"4,0,0\"]},{\"name\":\"Fire Control\",\"type\":\"SENSOR_ARRAY\",\"cells\":[\"5,0,0\",\"6,0,0\"]},{\"name\":\"Crew Compartment\",\"type\":\"CREW\",\"cells\":[\"7,0,0\",\"8,0,0\"]},{\"name\":\"Engine Bay\",\"type\":\"ENGINE_BAY\",\"cells\":[\"0,1,0\",\"1,1,0\",\"2,1,0\"]},{\"name\":\"Fuel Tank\",\"type\":\"FUEL_TANK\",\"cells\":[\"7,1,0\",\"8,1,0\",\"9,1,0\"]}]");
        template.setConnectionPointsJson("[{\"x\":5,\"y\":0,\"z\":0,\"direction\":\"front\"}]");
        template.setArmorRating(8);
        template.setFirepower(60);
        template.setSpeedKmh(40);
        template.setCrewCapacity(6);
        template.setFuelConsumption(2.5);
        template.setDetectionRange(8);
        template.setCompatibleTerrain("OPEN,DESERT");
        template.setSupplyCost(4);
        template.setDifficulty(7);
        template.setTags("missile,strategic,launcher,long-range");
        return template;
    }

    /**
     * Creates a fighter jet template.
     *
     * @return the fighter jet template
     */
    public GroundVehicleModuleTemplate createFighterJetTemplate() {
        GroundVehicleModuleTemplate template = new GroundVehicleModuleTemplate();
        template.setName("Fighter Jet");
        template.setType(GroundVehicleModuleTemplate.GroundVehicleModuleType.FIGHTER_JET);
        template.setDescription("Fast, maneuverable air superiority fighter");
        template.setWidth(6);
        template.setHeight(2);
        template.setDepth(3);
        template.setShapeCellsJson("[\"0,0,0\",\"1,0,0\",\"2,0,0\",\"3,0,0\",\"4,0,0\",\"5,0,0\",\"0,1,0\",\"1,1,0\",\"2,1,0\",\"3,1,0\",\"4,1,0\",\"5,1,0\"]");
        template.setCompartmentsJson("[{\"name\":\"Cockpit\",\"type\":\"CREW\",\"cells\":[\"0,0,0\",\"1,0,0\"]},{\"name\":\"Weapon Bay\",\"type\":\"WEAPONS\",\"cells\":[\"2,0,0\",\"3,0,0\"]},{\"name\":\"Engine\",\"type\":\"ENGINE_BAY\",\"cells\":[\"4,0,0\",\"5,0,0\"]},{\"name\":\"Fuel Tank\",\"type\":\"FUEL_TANK\",\"cells\":[\"2,1,0\",\"3,1,0\"]},{\"name\":\"Avionics\",\"type\":\"SENSOR_ARRAY\",\"cells\":[\"0,1,0\"]}]");
        template.setConnectionPointsJson("[{\"x\":3,\"y\":0,\"z\":0,\"direction\":\"front\"}]");
        template.setArmorRating(5);
        template.setFirepower(30);
        template.setSpeedKmh(2400);
        template.setCrewCapacity(1);
        template.setFuelConsumption(5.0);
        template.setDetectionRange(10);
        template.setCompatibleTerrain("AIR");
        template.setSupplyCost(4);
        template.setDifficulty(6);
        template.setTags("fighter,jet,air,fast");
        return template;
    }

    /**
     * Creates a bomber template.
     *
     * @return the bomber template
     */
    public GroundVehicleModuleTemplate createBomberTemplate() {
        GroundVehicleModuleTemplate template = new GroundVehicleModuleTemplate();
        template.setName("Strategic Bomber");
        template.setType(GroundVehicleModuleTemplate.GroundVehicleModuleType.BOMBER);
        template.setDescription("Heavy payload, long range strategic bomber");
        template.setWidth(10);
        template.setHeight(3);
        template.setDepth(4);
        template.setShapeCellsJson("[\"0,0,0\",\"1,0,0\",\"2,0,0\",\"3,0,0\",\"4,0,0\",\"5,0,0\",\"6,0,0\",\"7,0,0\",\"8,0,0\",\"9,0,0\",\"0,1,0\",\"1,1,0\",\"2,1,0\",\"3,1,0\",\"4,1,0\",\"5,1,0\",\"6,1,0\",\"7,1,0\",\"8,1,0\",\"9,1,0\"]");
        template.setCompartmentsJson("[{\"name\":\"Cockpit\",\"type\":\"CREW\",\"cells\":[\"0,0,0\",\"1,0,0\"]},{\"name\":\"Bomb Bay\",\"type\":\"CARGO\",\"cells\":[\"2,0,0\",\"3,0,0\",\"4,0,0\",\"5,0,0\"]},{\"name\":\"Engines\",\"type\":\"ENGINE_BAY\",\"cells\":[\"6,0,0\",\"7,0,0\",\"8,0,0\",\"9,0,0\"]},{\"name\":\"Fuel Tanks\",\"type\":\"FUEL_TANK\",\"cells\":[\"2,1,0\",\"3,1,0\",\"4,1,0\"]},{\"name\":\"Avionics\",\"type\":\"SENSOR_ARRAY\",\"cells\":[\"0,1,0\"]}]");
        template.setConnectionPointsJson("[{\"x\":5,\"y\":0,\"z\":0,\"direction\":\"front\"}]");
        template.setArmorRating(10);
        template.setFirepower(50);
        template.setSpeedKmh(1000);
        template.setCrewCapacity(4);
        template.setFuelConsumption(8.0);
        template.setDetectionRange(12);
        template.setCompatibleTerrain("AIR");
        template.setSupplyCost(5);
        template.setDifficulty(7);
        template.setTags("bomber,strategic,heavy,long-range");
        return template;
    }

    /**
     * Creates a helicopter template.
     *
     * @return the helicopter template
     */
    public GroundVehicleModuleTemplate createHelicopterTemplate() {
        GroundVehicleModuleTemplate template = new GroundVehicleModuleTemplate();
        template.setName("Attack Helicopter");
        template.setType(GroundVehicleModuleTemplate.GroundVehicleModuleType.HELICOPTER);
        template.setDescription("Rotary wing attack and transport helicopter");
        template.setWidth(6);
        template.setHeight(2);
        template.setDepth(3);
        template.setShapeCellsJson("[\"0,0,0\",\"1,0,0\",\"2,0,0\",\"3,0,0\",\"4,0,0\",\"5,0,0\",\"0,1,0\",\"1,1,0\",\"2,1,0\",\"3,1,0\",\"4,1,0\",\"5,1,0\"]");
        template.setCompartmentsJson("[{\"name\":\"Cockpit\",\"type\":\"CREW\",\"cells\":[\"0,0,0\",\"1,0,0\"]},{\"name\":\"Weapon Stations\",\"type\":\"WEAPONS\",\"cells\":[\"2,0,0\",\"3,0,0\"]},{\"name\":\"Engine\",\"type\":\"ENGINE_BAY\",\"cells\":[\"4,0,0\",\"5,0,0\"]},{\"name\":\"Fuel Tank\",\"type\":\"FUEL_TANK\",\"cells\":[\"4,1,0\",\"5,1,0\"]},{\"name\":\"Avionics\",\"type\":\"SENSOR_ARRAY\",\"cells\":[\"0,1,0\"]}]");
        template.setConnectionPointsJson("[{\"x\":3,\"y\":0,\"z\":0,\"direction\":\"front\"}]");
        template.setArmorRating(8);
        template.setFirepower(20);
        template.setSpeedKmh(300);
        template.setCrewCapacity(3);
        template.setFuelConsumption(3.0);
        template.setDetectionRange(6);
        template.setCompatibleTerrain("AIR,URBAN");
        template.setSupplyCost(3);
        template.setDifficulty(5);
        template.setTags("helicopter,attack,rotary,versatile");
        return template;
    }

    /**
     * Creates a transport ship template.
     *
     * @return the transport ship template
     */
    public GroundVehicleModuleTemplate createTransportShipTemplate() {
        GroundVehicleModuleTemplate template = new GroundVehicleModuleTemplate();
        template.setName("Amphibious Transport Ship");
        template.setType(GroundVehicleModuleTemplate.GroundVehicleModuleType.TRANSPORT_SHIP);
        template.setDescription("Water-borne vehicle and troop transport");
        template.setWidth(12);
        template.setHeight(4);
        template.setDepth(4);
        template.setShapeCellsJson("[\"0,0,0\",\"1,0,0\",\"2,0,0\",\"3,0,0\",\"4,0,0\",\"5,0,0\",\"6,0,0\",\"7,0,0\",\"8,0,0\",\"9,0,0\",\"10,0,0\",\"11,0,0\",\"0,1,0\",\"1,1,0\",\"2,1,0\",\"3,1,0\",\"4,1,0\",\"5,1,0\",\"6,1,0\",\"7,1,0\",\"8,1,0\",\"9,1,0\",\"10,1,0\",\"11,1,0\"]");
        template.setCompartmentsJson("[{\"name\":\"Bridge\",\"type\":\"COMMAND\",\"cells\":[\"0,0,0\",\"1,0,0\"]},{\"name\":\"Vehicle Bay\",\"type\":\"CARGO\",\"cells\":[\"2,0,0\",\"3,0,0\",\"4,0,0\",\"5,0,0\",\"6,0,0\",\"7,0,0\"]},{\"name\":\"Troop Quarters\",\"type\":\"CREW\",\"cells\":[\"8,0,0\",\"9,0,0\"]},{\"name\":\"Engine Room\",\"type\":\"ENGINE_BAY\",\"cells\":[\"10,0,0\",\"11,0,0\"]},{\"name\":\"Fuel Tanks\",\"type\":\"FUEL_TANK\",\"cells\":[\"10,1,0\",\"11,1,0\"]}]");
        template.setConnectionPointsJson("[{\"x\":6,\"y\":0,\"z\":0,\"direction\":\"front\"}]");
        template.setArmorRating(25);
        template.setFirepower(10);
        template.setSpeedKmh(30);
        template.setCrewCapacity(50);
        template.setFuelConsumption(4.0);
        template.setDetectionRange(4);
        template.setCompatibleTerrain("WATER,COASTAL");
        template.setSupplyCost(4);
        template.setDifficulty(5);
        template.setTags("transport,ship,amphibious,water");
        return template;
    }

    /**
     * Creates a corridor template for connecting vehicle modules.
     *
     * @return the corridor template
     */
    public GroundVehicleModuleTemplate createCorridorTemplate() {
        GroundVehicleModuleTemplate template = new GroundVehicleModuleTemplate();
        template.setName("Vehicle Corridor");
        template.setType(GroundVehicleModuleTemplate.GroundVehicleModuleType.CORRIDOR);
        template.setDescription("Connecting section for large multi-module vehicles");
        template.setWidth(4);
        template.setHeight(2);
        template.setDepth(2);
        template.setShapeCellsJson("[\"0,0,0\",\"1,0,0\",\"2,0,0\",\"3,0,0\",\"0,1,0\",\"1,1,0\",\"2,1,0\",\"3,1,0\"]");
        template.setCompartmentsJson("[{\"name\":\"Passage\",\"type\":\"TRANSIT\",\"cells\":[\"0,0,0\",\"1,0,0\",\"2,0,0\",\"3,0,0\"]}]");
        template.setConnectionPointsJson("[{\"x\":0,\"y\":0,\"z\":0,\"direction\":\"left\"},{\"x\":3,\"y\":0,\"z\":0,\"direction\":\"right\"}]");
        template.setArmorRating(5);
        template.setFirepower(0);
        template.setSpeedKmh(0);
        template.setCrewCapacity(0);
        template.setFuelConsumption(0.1);
        template.setDetectionRange(0);
        template.setCompatibleTerrain("OPEN,URBAN,DESERT,JUNGLE,MOUNTAIN,WATER");
        template.setSupplyCost(0);
        template.setDifficulty(1);
        template.setTags("corridor,connecting,passage");
        return template;
    }

    // ==========================================
    // Civilian Transport Vehicle Factory Methods
    // ==========================================

    /**
     * Creates a passenger bus template.
     *
     * @return the bus template
     */
    public GroundVehicleModuleTemplate createBusTemplate() {
        GroundVehicleModuleTemplate template = new GroundVehicleModuleTemplate();
        template.setName("Passenger Bus");
        template.setType(GroundVehicleModuleTemplate.GroundVehicleModuleType.BUS);
        template.setDescription("Road passenger transport vehicle for city and intercity travel");
        template.setWidth(6);
        template.setHeight(3);
        template.setDepth(3);
        template.setShapeCellsJson("[\"0,0,0\",\"1,0,0\",\"2,0,0\",\"3,0,0\",\"4,0,0\",\"5,0,0\",\"0,1,0\",\"1,1,0\",\"2,1,0\",\"3,1,0\",\"4,1,0\",\"5,1,0\"]");
        template.setCompartmentsJson("[{\"name\":\"Driver Cabin\",\"type\":\"CREW\",\"cells\":[\"0,0,0\"]},{\"name\":\"Passenger Seats\",\"type\":\"CARGO\",\"cells\":[\"1,0,0\",\"2,0,0\",\"3,0,0\",\"4,0,0\"]},{\"name\":\"Rear Entrance\",\"type\":\"CREW_HATCH\",\"cells\":[\"5,0,0\"]},{\"name\":\"Engine Bay\",\"type\":\"ENGINE_BAY\",\"cells\":[\"0,1,0\",\"1,1,0\"]},{\"name\":\"Fuel Tank\",\"type\":\"FUEL_TANK\",\"cells\":[\"4,1,0\",\"5,1,0\"]}]");
        template.setConnectionPointsJson("[{\"x\":3,\"y\":0,\"z\":0,\"direction\":\"front\"}]");
        template.setArmorRating(3);
        template.setFirepower(0);
        template.setSpeedKmh(100);
        template.setCrewCapacity(2);
        template.setFuelConsumption(1.5);
        template.setDetectionRange(1);
        template.setCompatibleTerrain("OPEN,URBAN");
        template.setSupplyCost(1);
        template.setPassengerCapacity(30);
        template.setCargoCapacity(0);
        template.setConstructionCost(80);
        template.setMaintenanceCost(4);
        template.setInfrastructureRequirement("ROAD");
        template.setDifficulty(1);
        template.setTags("bus,passenger,road,transport,civilian");
        return template;
    }

    /**
     * Creates a train template (freight or passenger).
     *
     * @return the train template
     */
    public GroundVehicleModuleTemplate createTrainTemplate() {
        GroundVehicleModuleTemplate template = new GroundVehicleModuleTemplate();
        template.setName("Passenger Train");
        template.setType(GroundVehicleModuleTemplate.GroundVehicleModuleType.TRAIN);
        template.setDescription("Rail transport for high-capacity passenger and freight movement");
        template.setWidth(10);
        template.setHeight(4);
        template.setDepth(4);
        template.setShapeCellsJson("[\"0,0,0\",\"1,0,0\",\"2,0,0\",\"3,0,0\",\"4,0,0\",\"5,0,0\",\"6,0,0\",\"7,0,0\",\"8,0,0\",\"9,0,0\",\"0,1,0\",\"1,1,0\",\"2,1,0\",\"3,1,0\",\"4,1,0\",\"5,1,0\",\"6,1,0\",\"7,1,0\",\"8,1,0\",\"9,1,0\"]");
        template.setCompartmentsJson("[{\"name\":\"Locomotive\",\"type\":\"ENGINE_BAY\",\"cells\":[\"0,0,0\",\"1,0,0\",\"2,0,0\"]},{\"name\":\"Driver Cabin\",\"type\":\"CREW\",\"cells\":[\"0,1,0\"]},{\"name\":\"Passenger Car 1\",\"type\":\"CARGO\",\"cells\":[\"3,0,0\",\"4,0,0\",\"5,0,0\"]},{\"name\":\"Passenger Car 2\",\"type\":\"CARGO\",\"cells\":[\"6,0,0\",\"7,0,0\",\"8,0,0\"]},{\"name\":\"Rear Coupler\",\"type\":\"TRANSIT\",\"cells\":[\"9,0,0\"]},{\"name\":\"Fuel Tank\",\"type\":\"FUEL_TANK\",\"cells\":[\"3,1,0\",\"4,1,0\"]}]");
        template.setConnectionPointsJson("[{\"x\":0,\"y\":0,\"z\":0,\"direction\":\"left\"},{\"x\":9,\"y\":0,\"z\":0,\"direction\":\"right\"}]");
        template.setArmorRating(10);
        template.setFirepower(0);
        template.setSpeedKmh(200);
        template.setCrewCapacity(3);
        template.setFuelConsumption(3.0);
        template.setDetectionRange(2);
        template.setCompatibleTerrain("OPEN,URBAN");
        template.setSupplyCost(2);
        template.setPassengerCapacity(80);
        template.setCargoCapacity(100);
        template.setConstructionCost(400);
        template.setMaintenanceCost(12);
        template.setInfrastructureRequirement("RAIL");
        template.setDifficulty(3);
        template.setTags("train,rail,passenger,freight,high-capacity");
        return template;
    }

    /**
     * Creates a car template (personal/commercial light vehicle).
     *
     * @return the car template
     */
    public GroundVehicleModuleTemplate createCarTemplate() {
        GroundVehicleModuleTemplate template = new GroundVehicleModuleTemplate();
        template.setName("Personal Car");
        template.setType(GroundVehicleModuleTemplate.GroundVehicleModuleType.CAR);
        template.setDescription("Light personal or commercial road vehicle");
        template.setWidth(4);
        template.setHeight(2);
        template.setDepth(2);
        template.setShapeCellsJson("[\"0,0,0\",\"1,0,0\",\"2,0,0\",\"3,0,0\",\"0,1,0\",\"1,1,0\",\"2,1,0\",\"3,1,0\"]");
        template.setCompartmentsJson("[{\"name\":\"Driver Seat\",\"type\":\"CREW\",\"cells\":[\"0,0,0\"]},{\"name\":\"Passenger Seats\",\"type\":\"CARGO\",\"cells\":[\"1,0,0\",\"2,0,0\"]},{\"name\":\"Trunk\",\"type\":\"CARGO\",\"cells\":[\"3,0,0\"]},{\"name\":\"Engine\",\"type\":\"ENGINE_BAY\",\"cells\":[\"0,1,0\"]},{\"name\":\"Fuel Tank\",\"type\":\"FUEL_TANK\",\"cells\":[\"3,1,0\"]}]");
        template.setConnectionPointsJson("[{\"x\":2,\"y\":0,\"z\":0,\"direction\":\"front\"}]");
        template.setArmorRating(1);
        template.setFirepower(0);
        template.setSpeedKmh(180);
        template.setCrewCapacity(1);
        template.setFuelConsumption(0.5);
        template.setDetectionRange(1);
        template.setCompatibleTerrain("OPEN,URBAN");
        template.setSupplyCost(0);
        template.setPassengerCapacity(4);
        template.setCargoCapacity(5);
        template.setConstructionCost(30);
        template.setMaintenanceCost(1);
        template.setInfrastructureRequirement("ROAD");
        template.setDifficulty(1);
        template.setTags("car,personal,light,road,civilian");
        return template;
    }

    /**
     * Creates a cargo truck template.
     *
     * @return the truck template
     */
    public GroundVehicleModuleTemplate createTruckTemplate() {
        GroundVehicleModuleTemplate template = new GroundVehicleModuleTemplate();
        template.setName("Cargo Truck");
        template.setType(GroundVehicleModuleTemplate.GroundVehicleModuleType.TRUCK);
        template.setDescription("Heavy road freight transport vehicle");
        template.setWidth(7);
        template.setHeight(3);
        template.setDepth(3);
        template.setShapeCellsJson("[\"0,0,0\",\"1,0,0\",\"2,0,0\",\"3,0,0\",\"4,0,0\",\"5,0,0\",\"6,0,0\",\"0,1,0\",\"1,1,0\",\"2,1,0\",\"3,1,0\",\"4,1,0\",\"5,1,0\",\"6,1,0\"]");
        template.setCompartmentsJson("[{\"name\":\"Cab\",\"type\":\"CREW\",\"cells\":[\"0,0,0\",\"1,0,0\"]},{\"name\":\"Cargo Bay\",\"type\":\"CARGO\",\"cells\":[\"2,0,0\",\"3,0,0\",\"4,0,0\",\"5,0,0\",\"6,0,0\"]},{\"name\":\"Engine\",\"type\":\"ENGINE_BAY\",\"cells\":[\"0,1,0\",\"1,1,0\"]},{\"name\":\"Fuel Tank\",\"type\":\"FUEL_TANK\",\"cells\":[\"5,1,0\",\"6,1,0\"]}]");
        template.setConnectionPointsJson("[{\"x\":3,\"y\":0,\"z\":0,\"direction\":\"front\"}]");
        template.setArmorRating(5);
        template.setFirepower(0);
        template.setSpeedKmh(90);
        template.setCrewCapacity(2);
        template.setFuelConsumption(2.0);
        template.setDetectionRange(1);
        template.setCompatibleTerrain("OPEN,URBAN,DESERT");
        template.setSupplyCost(1);
        template.setPassengerCapacity(0);
        template.setCargoCapacity(20);
        template.setConstructionCost(100);
        template.setMaintenanceCost(5);
        template.setInfrastructureRequirement("ROAD");
        template.setDifficulty(2);
        template.setTags("truck,cargo,freight,road,heavy");
        return template;
    }

    /**
     * Creates a ferry template (water transport).
     *
     * @return the ferry template
     */
    public GroundVehicleModuleTemplate createFerryTemplate() {
        GroundVehicleModuleTemplate template = new GroundVehicleModuleTemplate();
        template.setName("Passenger Ferry");
        template.setType(GroundVehicleModuleTemplate.GroundVehicleModuleType.FERRY);
        template.setDescription("Water transport for passengers and vehicles");
        template.setWidth(10);
        template.setHeight(4);
        template.setDepth(4);
        template.setShapeCellsJson("[\"0,0,0\",\"1,0,0\",\"2,0,0\",\"3,0,0\",\"4,0,0\",\"5,0,0\",\"6,0,0\",\"7,0,0\",\"8,0,0\",\"9,0,0\",\"0,1,0\",\"1,1,0\",\"2,1,0\",\"3,1,0\",\"4,1,0\",\"5,1,0\",\"6,1,0\",\"7,1,0\",\"8,1,0\",\"9,1,0\"]");
        template.setCompartmentsJson("[{\"name\":\"Bridge\",\"type\":\"COMMAND\",\"cells\":[\"0,0,0\",\"1,0,0\"]},{\"name\":\"Passenger Deck\",\"type\":\"CARGO\",\"cells\":[\"2,0,0\",\"3,0,0\",\"4,0,0\",\"5,0,0\"]},{\"name\":\"Vehicle Deck\",\"type\":\"CARGO\",\"cells\":[\"6,0,0\",\"7,0,0\",\"8,0,0\"]},{\"name\":\"Engine Room\",\"type\":\"ENGINE_BAY\",\"cells\":[\"9,0,0\"]},{\"name\":\"Fuel Tanks\",\"type\":\"FUEL_TANK\",\"cells\":[\"8,1,0\",\"9,1,0\"]},{\"name\":\"Lifeboats\",\"type\":\"DEFENSE\",\"cells\":[\"0,1,0\",\"1,1,0\"]}]");
        template.setConnectionPointsJson("[{\"x\":5,\"y\":0,\"z\":0,\"direction\":\"front\"}]");
        template.setArmorRating(8);
        template.setFirepower(0);
        template.setSpeedKmh(30);
        template.setCrewCapacity(5);
        template.setFuelConsumption(3.5);
        template.setDetectionRange(3);
        template.setCompatibleTerrain("WATER,COASTAL");
        template.setSupplyCost(2);
        template.setPassengerCapacity(50);
        template.setCargoCapacity(10);
        template.setConstructionCost(250);
        template.setMaintenanceCost(8);
        template.setInfrastructureRequirement("WATER");
        template.setDifficulty(3);
        template.setTags("ferry,water,passenger,vehicle,coastal");
        return template;
    }

    /**
     * Creates an aircraft template (passenger/cargo plane).
     *
     * @return the aircraft template
     */
    public GroundVehicleModuleTemplate createAircraftTemplate() {
        GroundVehicleModuleTemplate template = new GroundVehicleModuleTemplate();
        template.setName("Passenger Aircraft");
        template.setType(GroundVehicleModuleTemplate.GroundVehicleModuleType.AIRCRAFT);
        template.setDescription("Air transport for passengers and cargo");
        template.setWidth(10);
        template.setHeight(3);
        template.setDepth(4);
        template.setShapeCellsJson("[\"0,0,0\",\"1,0,0\",\"2,0,0\",\"3,0,0\",\"4,0,0\",\"5,0,0\",\"6,0,0\",\"7,0,0\",\"8,0,0\",\"9,0,0\",\"0,1,0\",\"1,1,0\",\"2,1,0\",\"3,1,0\",\"4,1,0\",\"5,1,0\",\"6,1,0\",\"7,1,0\",\"8,1,0\",\"9,1,0\"]");
        template.setCompartmentsJson("[{\"name\":\"Cockpit\",\"type\":\"CREW\",\"cells\":[\"0,0,0\",\"1,0,0\"]},{\"name\":\"Passenger Cabin\",\"type\":\"CARGO\",\"cells\":[\"2,0,0\",\"3,0,0\",\"4,0,0\",\"5,0,0\",\"6,0,0\"]},{\"name\":\"Cargo Hold\",\"type\":\"CARGO\",\"cells\":[\"7,0,0\",\"8,0,0\"]},{\"name\":\"Engines\",\"type\":\"ENGINE_BAY\",\"cells\":[\"9,0,0\"]},{\"name\":\"Fuel Tanks\",\"type\":\"FUEL_TANK\",\"cells\":[\"7,1,0\",\"8,1,0\",\"9,1,0\"]},{\"name\":\"Avionics\",\"type\":\"SENSOR_ARRAY\",\"cells\":[\"0,1,0\"]}]");
        template.setConnectionPointsJson("[{\"x\":5,\"y\":0,\"z\":0,\"direction\":\"front\"}]");
        template.setArmorRating(5);
        template.setFirepower(0);
        template.setSpeedKmh(800);
        template.setCrewCapacity(4);
        template.setFuelConsumption(6.0);
        template.setDetectionRange(8);
        template.setCompatibleTerrain("AIR");
        template.setSupplyCost(3);
        template.setPassengerCapacity(10);
        template.setCargoCapacity(10);
        template.setConstructionCost(800);
        template.setMaintenanceCost(25);
        template.setInfrastructureRequirement("AIRPORT");
        template.setDifficulty(5);
        template.setTags("aircraft,passenger,cargo,air,fast");
        return template;
    }
}
