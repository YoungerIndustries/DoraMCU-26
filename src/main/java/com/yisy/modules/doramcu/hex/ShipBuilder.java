package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.gamecore.DungeonRoomService;
import com.yisy.modules.doramcu.hex.dungeon.ShipModuleTemplate;
import com.yisy.modules.doramcu.hex.dungeon.ShipModuleTemplateRepository;
import com.yisy.modules.doramcu.hex.planet.BuildingInteriorGenerator;
import com.yisy.modules.doramcu.hex.spi.AssetRegistrySpi;
import com.yisy.modules.doramcu.hex.Skin;
import com.yisy.modules.doramcu.hex.spi.SkinLookupSpi;
import com.yisy.modules.doramcu.hex.SkinType;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for building ships by combining 3D hull modules with interior dungeon rooms.
 * <p>
 * The ShipBuilder orchestrates two dungeon creation paradigms:
 * </p>
 * <ol>
 *   <li><b>Exterior Hull (3D Template Method)</b> — Uses {@link ShipModuleTemplate} instances
 *       to create the ship's 3D structure. Each template becomes a module section with
 *       {@link HullModule} instances, {@link Compartment} objects for named functional areas,
 *       and connection points for inter-module doorways.</li>
 *   <li><b>Interior Rooms (Graph-Based Method)</b> — Uses {@link BuildingInteriorGenerator}
 *       to create explorable interior rooms with bidirectional connections, traps, treasures,
 *       and encounters.</li>
 * </ol>
 * <p>
 * The bridge between the two paradigms is {@link CompartmentType}:
 * </p>
 * <ul>
 *   <li>Exterior hull → {@link Compartment} objects with {@link CompartmentType} (COMMAND, ENGINE, etc.)</li>
 *   <li>Interior rooms → {@link DungeonRoomService.DungeonRoom} objects with theme (Social, Combat, Storage, etc.)</li>
 *   <li>Mapping: {@link CompartmentType#fromString(String)} provides fuzzy mapping between domain-specific types</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 * // Create ship module templates
 * ShipModuleTemplate bridge = builder.createBridgeModuleTemplate();
 * ShipModuleTemplate engine = builder.createEngineRoomTemplate();
 *
 * // Build a ship from templates
 * Ship ship = builder.buildShip("USS Enterprise", ShipType.CRUISER,
 *     Arrays.asList(bridge.getId(), engine.getId()));
 *
 * // Generate interior rooms for the ship
 * String dungeonId = builder.generateShipInteriors(ship, TechLevel.TL8);
 * </pre>
 */
@Service
public class ShipBuilder {

    private final ShipModuleTemplateRepository templateRepository;
    private final DungeonRoomService dungeonRoomService;
    private final TechLevelService techLevelService;
    private final AssetRegistrySpi AssetRegistrySpi;
    private final SkinLookupSpi SkinLookupSpi;

    private int moduleCounter;

    /**
     * Creates a new ShipBuilder with Spring-managed dependencies.
     *
     * @param templateRepository the JPA repository for ship module templates
     * @param dungeonRoomService the service for creating interior dungeon rooms
     * @param techLevelService the service for tech-level-aware module lookup (may be null)
     * @param AssetRegistrySpi the central asset registry for skin assignment
     * @param SkinLookupSpi the service for skin management
     */
    public ShipBuilder(ShipModuleTemplateRepository templateRepository,
                       DungeonRoomService dungeonRoomService,
                       TechLevelService techLevelService,
                       AssetRegistrySpi AssetRegistrySpi,
                       SkinLookupSpi SkinLookupSpi) {
        this.templateRepository = templateRepository;
        this.dungeonRoomService = dungeonRoomService;
        this.techLevelService = techLevelService;
        this.AssetRegistrySpi = AssetRegistrySpi;
        this.SkinLookupSpi = SkinLookupSpi;
        this.moduleCounter = 0;
    }

    /**
     * Creates a new ShipBuilder without repository (for legacy/standalone use).
     * <p>
     * In this mode, template CRUD operations are not available.
     * </p>
     */
    public ShipBuilder() {
        this.templateRepository = null;
        this.dungeonRoomService = null;
        this.techLevelService = null;
        this.AssetRegistrySpi = null;
        this.SkinLookupSpi = null;
        this.moduleCounter = 0;
    }

    // ==========================================
    // Ship Module Template CRUD
    // ==========================================

    /**
     * Creates a new ship module template and persists it.
     *
     * @param template the template to create
     * @return the saved template
     * @throws IllegalStateException if repository is not available
     */
    public ShipModuleTemplate createModuleTemplate(ShipModuleTemplate template) {
        assertRepository();
        return templateRepository.save(template);
    }

    /**
     * Updates an existing ship module template.
     *
     * @param template the template to update
     * @return the saved template
     * @throws IllegalArgumentException if the template ID does not exist
     * @throws IllegalStateException if repository is not available
     */
    public ShipModuleTemplate updateModuleTemplate(ShipModuleTemplate template) {
        assertRepository();
        if (!templateRepository.existsById(template.getId())) {
            throw new IllegalArgumentException("Template not found: " + template.getId());
        }
        return templateRepository.save(template);
    }

    /**
     * Deletes a ship module template by ID.
     *
     * @param id the template ID
     * @throws IllegalStateException if repository is not available
     */
    public void deleteModuleTemplate(Long id) {
        assertRepository();
        templateRepository.deleteById(id);
    }

    /**
     * Returns all ship module templates.
     *
     * @return list of all templates
     * @throws IllegalStateException if repository is not available
     */
    public List<ShipModuleTemplate> getModuleTemplates() {
        assertRepository();
        return templateRepository.findAll();
    }

    /**
     * Returns a ship module template by ID.
     *
     * @param id the template ID
     * @return the template or empty if not found
     * @throws IllegalStateException if repository is not available
     */
    public Optional<ShipModuleTemplate> getModuleTemplate(Long id) {
        assertRepository();
        return templateRepository.findById(id);
    }

    /**
     * Returns templates filtered by module type.
     *
     * @param type the ship module type
     * @return list of matching templates
     * @throws IllegalStateException if repository is not available
     */
    public List<ShipModuleTemplate> getModulesByType(ShipModuleTemplate.ShipModuleType type) {
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
    public List<ShipModuleTemplate> getModulesByTag(String tag) {
        assertRepository();
        return templateRepository.findByTagContaining(tag);
    }

    /**
     * Returns templates compatible with the given ship type.
     *
     * @param shipType the ship type
     * @return list of compatible templates
     * @throws IllegalStateException if repository is not available
     */
    public List<ShipModuleTemplate> getModulesForShipType(ShipType shipType) {
        assertRepository();
        return templateRepository.findByCompatibleShipTypes(shipType.name());
    }

    /**
     * Returns templates with difficulty less than or equal to the given value.
     *
     * @param maxDifficulty the maximum difficulty
     * @return list of matching templates
     * @throws IllegalStateException if repository is not available
     */
    public List<ShipModuleTemplate> getModulesByDifficulty(int maxDifficulty) {
        assertRepository();
        return templateRepository.findByDifficultyLessThanEqual(maxDifficulty);
    }

    /**
     * Returns all templates ordered by difficulty ascending.
     *
     * @return list of templates sorted by difficulty
     * @throws IllegalStateException if repository is not available
     */
    public List<ShipModuleTemplate> getModulesOrderedByDifficulty() {
        assertRepository();
        return templateRepository.findAllOrderByDifficultyAsc();
    }

    private void assertRepository() {
        if (templateRepository == null) {
            throw new IllegalStateException("ShipBuilder not initialized with repository; use Spring-managed constructor");
        }
    }

    // ==========================================
    // Ship Assembly from Templates
    // ==========================================

    /**
     * Builds a complete Ship from ship module templates.
     * <p>
     * Each template becomes a module section in the ship. Templates are processed
     * in order, and connection points are used to create inter-module doorways.
     * The ship's mass, crew capacity, and power consumption are calculated from
     * the module contributions.
     * </p>
     *
     * @param name the ship name
     * @param shipType the ship type (determines base stats)
     * @param templateIds list of template IDs to use (in module order)
     * @return the assembled Ship
     * @throws IllegalArgumentException if one or more templates are not found
     */
    public Ship buildShip(String name, ShipType shipType, List<Long> templateIds) {
        List<ShipModuleTemplate> templates = templateRepository.findAllById(templateIds);
        if (templates.size() != templateIds.size()) {
            throw new IllegalArgumentException("One or more templates not found");
        }

        // Create the ship
        Ship ship = new Ship(
                templateIds.isEmpty() ? 0 : templateIds.get(0).intValue(),
                shipType
        );
        ship.setName(name);

        // Calculate total contributions from templates
        double totalMass = 0;
        int totalCrew = 0;
        double totalPower = 0;

        // Build the 3D hull structure
        BuildingHull hull = new BuildingHull(name);
        List<String> moduleIds = new ArrayList<>();

        for (int i = 0; i < templates.size(); i++) {
            ShipModuleTemplate template = templates.get(i);
            HullModule module = template.toHullModule();
            String moduleId = "module_" + i;

            hull.addFloor(module);
            hull.registerFloor(moduleId, module);
            moduleIds.add(moduleId);

            // Accumulate contributions
            totalMass += template.getMassTons();
            totalCrew += template.getCrewCapacity();
            totalPower += template.getPowerConsumption();
        }

        // Connect consecutive modules via connection points
        for (int i = 0; i < moduleIds.size() - 1; i++) {
            String fromModuleId = moduleIds.get(i);
            String toModuleId = moduleIds.get(i + 1);
            HullModule fromModule = hull.getFloorById(fromModuleId);
            HullModule toModule = hull.getFloorById(toModuleId);

            List<Map<String, Object>> fromPoints = templates.get(i).getConnectionPoints();
            List<Map<String, Object>> toPoints = templates.get(i + 1).getConnectionPoints();

            if (!fromPoints.isEmpty() && !toPoints.isEmpty()) {
                Map<String, Object> fromPoint = fromPoints.get(0);
                Map<String, Object> toPoint = toPoints.get(0);

                int fx = (int) fromPoint.getOrDefault("x", 0);
                int fy = (int) fromPoint.getOrDefault("y", 0);
                int fz = (int) fromPoint.getOrDefault("z", 0);
                int tx = (int) toPoint.getOrDefault("x", 0);
                int ty = (int) toPoint.getOrDefault("y", 0);
                int tz = (int) toPoint.getOrDefault("z", 0);

                M3space fromCell = fromModule.getCell(fx, fy, fz);
                M3space toCell = toModule.getCell(tx, ty, tz);

                if (fromCell != null && toCell != null) {
                    hull.connectFloors(fromModuleId, fromCell, toModuleId, toCell);
                }
            }
        }

        // Set the hull module on the ship
        if (!moduleIds.isEmpty()) {
            String firstModuleId = moduleIds.get(0);
            HullModule firstModule = hull.getFloorById(firstModuleId);
            if (firstModule != null) {
                List<HullModule> modules = new ArrayList<>();
                modules.add(firstModule);
                ship.setHullModules(modules);
            }
        }

        // Install AI Core for network connection
        ship.setAiCore(new AiCoreComponent());

        // Register with asset registry and assign default skin
        registerShip(ship);

        return ship;
    }

    /**
     * Builds a Ship with default FRIGATE type.
     *
     * @param name the ship name
     * @param templateIds list of template IDs
     * @return the assembled Ship
     */
    public Ship buildShip(String name, List<Long> templateIds) {
        return buildShip(name, ShipType.FRIGATE, templateIds);
    }

    // ==========================================
    // Interior Room Generation
    // ==========================================

    /**
     * Generates interior dungeon rooms for a ship.
     * <p>
     * Uses {@link BuildingInteriorGenerator} with the Space Port template to create
     * 10 explorable rooms with connections, traps, treasures, and encounters.
     * The rooms are tech-level aware: higher tech levels produce sci-fi themed rooms.
     * </p>
     *
     * @param ship the ship to generate interiors for
     * @param techLevel the technology level (determines room names and features)
     * @param seed random seed for reproducible generation
     * @return the dungeon ID for the generated interior rooms
     */
    public String generateShipInteriors(Ship ship, TechLevel techLevel, long seed) {
        if (dungeonRoomService == null) {
            throw new IllegalStateException("ShipBuilder not initialized with dungeonRoomService");
        }
        String dungeonId = ship.getName() + "-interior";

        BuildingInteriorGenerator generator = new BuildingInteriorGenerator(
                dungeonRoomService, techLevelService, seed
        );

        // Use the Space Port template which provides 10 rooms suitable for a ship interior
        return generator.generateSpacePort(dungeonId, techLevel);
    }

    /**
     * Generates interior dungeon rooms with default tech level (TL8 for ships).
     *
     * @param ship the ship to generate interiors for
     * @param seed random seed for reproducible generation
     * @return the dungeon ID for the generated interior rooms
     */
    public String generateShipInteriors(Ship ship, long seed) {
        return generateShipInteriors(ship, TechLevel.TL8, seed);
    }

    /**
     * Generates interior dungeon rooms with a random seed.
     *
     * @param ship the ship to generate interiors for
     * @param techLevel the technology level
     * @return the dungeon ID for the generated interior rooms
     */
    public String generateShipInteriors(Ship ship, TechLevel techLevel) {
        return generateShipInteriors(ship, techLevel, new Random().nextLong());
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
     * @param ship the ship with compartments
     * @param dungeonId the dungeon ID of the interior rooms
     * @return a map of compartment names to their matched dungeon room IDs
     */
    public Map<String, Long> mapCompartmentsToRooms(Ship ship, String dungeonId) {
        if (dungeonRoomService == null) {
            return Collections.emptyMap();
        }
        Map<String, Long> mapping = new HashMap<>();
        List<DungeonRoomService.DungeonRoom> rooms = dungeonRoomService.listRoomsByDungeon(dungeonId);

        // Iterate over hull modules and their compartments
        for (HullModule module : ship.getHullModules()) {
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
            case "social" -> type == CompartmentType.COMMAND ||
                             type == CompartmentType.CREW ||
                             type == CompartmentType.TRANSIT;
            case "combat" -> type == CompartmentType.WEAPONS ||
                             type == CompartmentType.DEFENSE;
            case "storage" -> type == CompartmentType.CARGO ||
                              type == CompartmentType.VAULT;
            case "crafting" -> type == CompartmentType.RESEARCH ||
                               type == CompartmentType.ENGINE;
            default -> false;
        };
    }

    // ==========================================
    // Pre-population of Ship Templates
    // ==========================================

    /**
     * Pre-populates the database with starter ship module templates.
     * <p>
     * Creates templates for all 13 ship module types with default dimensions,
     * compartments, and connection points. Called once during initialization.
     * </p>
     *
     * @return the number of templates created
     */
    public int prepopulateTemplates() {
        if (templateRepository.count() > 0) {
            return 0;
        }

        List<ShipModuleTemplate> templates = new ArrayList<>();

        // Core modules
        templates.add(createBridgeModuleTemplate());
        templates.add(createEngineRoomTemplate());
        templates.add(createWeaponBayTemplate());
        templates.add(createCargoHoldTemplate());
        templates.add(createCrewQuartersTemplate());
        templates.add(createCorridorTemplate());

        // Support modules
        templates.add(createShieldGeneratorTemplate());
        templates.add(createDockingBayTemplate());
        templates.add(createResearchLabTemplate());
        templates.add(createVaultTemplate());
        templates.add(createMedicalBayTemplate());
        templates.add(createHangarBayTemplate());

        templateRepository.saveAll(templates);
        return templates.size();
    }

    // ==========================================
    // Template Factory Methods
    // ==========================================

    /**
     * Creates a Bridge Module template.
     * <p>10×10×3 grid with command center compartment.</p>
     */
    public ShipModuleTemplate createBridgeModuleTemplate() {
        ShipModuleTemplate template = new ShipModuleTemplate();
        template.setName("Command Bridge");
        template.setDescription("The ship's command and control center with navigation and tactical stations.");
        template.setType(ShipModuleTemplate.ShipModuleType.BRIDGE);
        template.setWidth(10);
        template.setHeight(10);
        template.setDepth(3);
        template.setDifficulty(3);
        template.setTags("command,bridge,tactical,navigation");
        template.setMassTons(50.0);
        template.setCrewCapacity(20);
        template.setPowerConsumption(100.0);

        Set<M3space> cells = generateRectCells(10, 10, 3);
        template.setShapeCellsJson(ShipModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Bridge", "COMMAND", cells));
        template.setConnectionPointsJson(
                "[{\"x\":5,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":5,\"y\":9,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates an Engine Room template.
     * <p>10×10×4 grid with engine compartment.</p>
     */
    public ShipModuleTemplate createEngineRoomTemplate() {
        ShipModuleTemplate template = new ShipModuleTemplate();
        template.setName("Engine Room");
        template.setDescription("Power generation and propulsion systems.");
        template.setType(ShipModuleTemplate.ShipModuleType.ENGINE_ROOM);
        template.setWidth(10);
        template.setHeight(10);
        template.setDepth(4);
        template.setDifficulty(4);
        template.setTags("engine,power,propulsion,reactor");
        template.setMassTons(100.0);
        template.setCrewCapacity(15);
        template.setPowerConsumption(50.0);

        Set<M3space> cells = generateRectCells(10, 10, 4);
        template.setShapeCellsJson(ShipModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Engine Room", "ENGINE", cells));
        template.setConnectionPointsJson(
                "[{\"x\":5,\"y\":0,\"z\":2,\"direction\":\"FORWARD\"},{\"x\":5,\"y\":9,\"z\":2,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Weapon Bay template.
     * <p>10×8×3 grid with weapons compartment.</p>
     */
    public ShipModuleTemplate createWeaponBayTemplate() {
        ShipModuleTemplate template = new ShipModuleTemplate();
        template.setName("Weapon Bay");
        template.setDescription("Missile bays and turret mounts for offensive operations.");
        template.setType(ShipModuleTemplate.ShipModuleType.WEAPON_BAY);
        template.setWidth(10);
        template.setHeight(8);
        template.setDepth(3);
        template.setDifficulty(5);
        template.setTags("weapons,missiles,turrets,offensive");
        template.setMassTons(80.0);
        template.setCrewCapacity(10);
        template.setPowerConsumption(200.0);

        Set<M3space> cells = generateRectCells(10, 8, 3);
        template.setShapeCellsJson(ShipModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Weapon Bay", "WEAPONS", cells));
        template.setConnectionPointsJson(
                "[{\"x\":5,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":5,\"y\":7,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Cargo Hold template.
     * <p>10×10×3 grid with cargo storage compartment.</p>
     */
    public ShipModuleTemplate createCargoHoldTemplate() {
        ShipModuleTemplate template = new ShipModuleTemplate();
        template.setName("Cargo Hold");
        template.setDescription("Large storage area for cargo, supplies, and raw materials.");
        template.setType(ShipModuleTemplate.ShipModuleType.CARGO_HOLD);
        template.setWidth(10);
        template.setHeight(10);
        template.setDepth(3);
        template.setDifficulty(2);
        template.setTags("cargo,storage,supplies");
        template.setMassTons(30.0);
        template.setCrewCapacity(5);
        template.setPowerConsumption(20.0);

        Set<M3space> cells = generateRectCells(10, 10, 3);
        template.setShapeCellsJson(ShipModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Cargo Hold", "CARGO", cells));
        template.setConnectionPointsJson(
                "[{\"x\":5,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":5,\"y\":9,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Crew Quarters template.
     * <p>10×10×3 grid with crew living compartment.</p>
     */
    public ShipModuleTemplate createCrewQuartersTemplate() {
        ShipModuleTemplate template = new ShipModuleTemplate();
        template.setName("Crew Quarters");
        template.setDescription("Living quarters, mess halls, and medical facilities for crew.");
        template.setType(ShipModuleTemplate.ShipModuleType.CREW_QUARTERS);
        template.setWidth(10);
        template.setHeight(10);
        template.setDepth(3);
        template.setDifficulty(2);
        template.setTags("crew,quarters,medical,living");
        template.setMassTons(40.0);
        template.setCrewCapacity(50);
        template.setPowerConsumption(60.0);

        Set<M3space> cells = generateRectCells(10, 10, 3);
        template.setShapeCellsJson(ShipModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Crew Quarters", "CREW", cells));
        template.setConnectionPointsJson(
                "[{\"x\":5,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":5,\"y\":9,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Shield Generator template.
     * <p>8×8×3 grid with shield generator compartment.</p>
     */
    public ShipModuleTemplate createShieldGeneratorTemplate() {
        ShipModuleTemplate template = new ShipModuleTemplate();
        template.setName("Shield Generator");
        template.setDescription("Shield generators and defensive systems.");
        template.setType(ShipModuleTemplate.ShipModuleType.SHIELD_GENERATOR);
        template.setWidth(8);
        template.setHeight(8);
        template.setDepth(3);
        template.setDifficulty(4);
        template.setTags("shield,defense,generator,protection");
        template.setMassTons(60.0);
        template.setCrewCapacity(8);
        template.setPowerConsumption(150.0);

        Set<M3space> cells = generateRectCells(8, 8, 3);
        template.setShapeCellsJson(ShipModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Shield Generator", "DEFENSE", cells));
        template.setConnectionPointsJson(
                "[{\"x\":4,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":4,\"y\":7,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Docking Bay template.
     * <p>10×10×3 grid with docking compartment.</p>
     */
    public ShipModuleTemplate createDockingBayTemplate() {
        ShipModuleTemplate template = new ShipModuleTemplate();
        template.setName("Docking Bay");
        template.setDescription("Docking bays, airlocks, and ship maintenance facilities.");
        template.setType(ShipModuleTemplate.ShipModuleType.DOCKING_BAY);
        template.setWidth(10);
        template.setHeight(10);
        template.setDepth(3);
        template.setDifficulty(3);
        template.setTags("docking,airlock,maintenance,shuttle");
        template.setMassTons(45.0);
        template.setCrewCapacity(10);
        template.setPowerConsumption(40.0);

        Set<M3space> cells = generateRectCells(10, 10, 3);
        template.setShapeCellsJson(ShipModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Docking Bay", "DOCKING", cells));
        template.setConnectionPointsJson(
                "[{\"x\":5,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":5,\"y\":9,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Research Lab template.
     * <p>8×8×3 grid with research compartment.</p>
     */
    public ShipModuleTemplate createResearchLabTemplate() {
        ShipModuleTemplate template = new ShipModuleTemplate();
        template.setName("Research Lab");
        template.setDescription("Scientific instruments, laboratories, and research equipment.");
        template.setType(ShipModuleTemplate.ShipModuleType.RESEARCH_LAB);
        template.setWidth(8);
        template.setHeight(8);
        template.setDepth(3);
        template.setDifficulty(5);
        template.setTags("research,science,lab,experiment");
        template.setMassTons(35.0);
        template.setCrewCapacity(15);
        template.setPowerConsumption(80.0);

        Set<M3space> cells = generateRectCells(8, 8, 3);
        template.setShapeCellsJson(ShipModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Research Lab", "RESEARCH", cells));
        template.setConnectionPointsJson(
                "[{\"x\":4,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":4,\"y\":7,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Corridor template.
     * <p>4×4×2 grid with transit compartment for connecting sections.</p>
     */
    public ShipModuleTemplate createCorridorTemplate() {
        ShipModuleTemplate template = new ShipModuleTemplate();
        template.setName("Corridor");
        template.setDescription("Passage connecting ship modules.");
        template.setType(ShipModuleTemplate.ShipModuleType.CORRIDOR);
        template.setWidth(4);
        template.setHeight(4);
        template.setDepth(2);
        template.setDifficulty(1);
        template.setTags("passage,connection,transit");
        template.setMassTons(10.0);
        template.setCrewCapacity(0);
        template.setPowerConsumption(5.0);

        Set<M3space> cells = generateRectCells(4, 4, 2);
        template.setShapeCellsJson(ShipModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Corridor", "TRANSIT", cells));
        template.setConnectionPointsJson(
                "[{\"x\":2,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":2,\"y\":3,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Vault template.
     * <p>6×6×3 grid with reinforced storage compartment.</p>
     */
    public ShipModuleTemplate createVaultTemplate() {
        ShipModuleTemplate template = new ShipModuleTemplate();
        template.setName("Vault");
        template.setDescription("Reinforced storage for hazardous or valuable materials.");
        template.setType(ShipModuleTemplate.ShipModuleType.VAULT);
        template.setWidth(6);
        template.setHeight(6);
        template.setDepth(3);
        template.setDifficulty(4);
        template.setTags("vault,secure,storage,hazardous");
        template.setMassTons(55.0);
        template.setCrewCapacity(5);
        template.setPowerConsumption(30.0);

        Set<M3space> cells = generateRectCells(6, 6, 3);
        template.setShapeCellsJson(ShipModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Vault", "VAULT", cells));
        template.setConnectionPointsJson(
                "[{\"x\":3,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":3,\"y\":5,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Medical Bay template.
     * <p>8×8×3 grid with medical compartment.</p>
     */
    public ShipModuleTemplate createMedicalBayTemplate() {
        ShipModuleTemplate template = new ShipModuleTemplate();
        template.setName("Medical Bay");
        template.setDescription("Medical bay and repair facilities for crew health.");
        template.setType(ShipModuleTemplate.ShipModuleType.MEDICAL_BAY);
        template.setWidth(8);
        template.setHeight(8);
        template.setDepth(3);
        template.setDifficulty(3);
        template.setTags("medical,health,repair,infirmary");
        template.setMassTons(40.0);
        template.setCrewCapacity(12);
        template.setPowerConsumption(70.0);

        Set<M3space> cells = generateRectCells(8, 8, 3);
        template.setShapeCellsJson(ShipModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Medical Bay", "CREW", cells));
        template.setConnectionPointsJson(
                "[{\"x\":4,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":4,\"y\":7,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Hangar Bay template.
     * <p>12×10×4 grid with hangar compartment for shuttles and fighters.</p>
     */
    public ShipModuleTemplate createHangarBayTemplate() {
        ShipModuleTemplate template = new ShipModuleTemplate();
        template.setName("Hangar Bay");
        template.setDescription("Hangar for shuttles and fighters.");
        template.setType(ShipModuleTemplate.ShipModuleType.HANGAR_BAY);
        template.setWidth(12);
        template.setHeight(10);
        template.setDepth(4);
        template.setDifficulty(4);
        template.setTags("hangar,shuttle,fighter,aircraft");
        template.setMassTons(70.0);
        template.setCrewCapacity(20);
        template.setPowerConsumption(90.0);

        Set<M3space> cells = generateRectCells(12, 10, 4);
        template.setShapeCellsJson(ShipModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Hangar Bay", "DOCKING", cells));
        template.setConnectionPointsJson(
                "[{\"x\":6,\"y\":0,\"z\":2,\"direction\":\"FORWARD\"},{\"x\":6,\"y\":9,\"z\":2,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    // ==========================================
    // Helper Methods
    // ==========================================

    /**
     * Generates a rectangular set of M3space cells.
     *
     * @param width the width (X axis)
     * @param height the height (Y axis)
     * @param depth the depth (Z axis)
     * @return set of M3space cells
     */
    private Set<M3space> generateRectCells(int width, int height, int depth) {
        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    cells.add(new M3space(x, y, z));
                }
            }
        }
        return cells;
    }

    /**
     * Builds a compartment JSON string for a single compartment spanning all cells.
     *
     * @param name the compartment name
     * @param type the compartment type string
     * @param cells the set of cells
     * @return the JSON string
     */
    private String buildCompartmentJson(String name, String type, Set<M3space> cells) {
        StringBuilder sb = new StringBuilder();
        sb.append("[{\"name\":\"").append(name).append("\",\"type\":\"").append(type).append("\",\"cells\":[");
        boolean first = true;
        for (M3space cell : cells) {
            if (!first) sb.append(",");
            sb.append("\"").append(cell.getX()).append(",")
              .append(cell.getY()).append(",")
              .append(cell.getZ()).append("\"");
            first = false;
        }
        sb.append("]}]");
        return sb.toString();
    }

    /**
     * Builds a single module from a template, instantiating a new {@link HullModule}.
     *
     * @param template the module template to instantiate
     * @return a new HullModule instance
     */
    public HullModule buildFromTemplate(ShipModuleTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("Template must not be null");
        }
        return template.toHullModule();
    }

    /**
     * Assembles a ship from templates and connection definitions.
     * <p>
     * Each template is instantiated as a separate module. Connections define how modules
     * are linked at passage cells. The ship's ShipHull is configured with graph-based
     * connections for pathfinding.
     * </p>
     *
     * @param shipName the name of the ship
     * @param shipType the ship type classification
     * @param templates list of module templates to instantiate
     * @param connections list of connection definitions
     * @return the assembled ship
     */
    public Ship assemble(String shipName, ShipType shipType,
                         List<ShipModuleTemplate> templates,
                         List<ConnectionDefinition> connections) {
        Ship ship = new Ship(0, shipType);
        ship.setName(shipName);
        ShipHull hull = new ShipHull(ship);

        // Instantiate all modules
        Map<Integer, String> indexToModuleId = new HashMap<>();
        for (int i = 0; i < templates.size(); i++) {
            ShipModuleTemplate template = templates.get(i);
            HullModule module = buildFromTemplate(template);
            String moduleId = generateModuleId(template.getName());
            hull.registerModule(moduleId, module);

            // Add to hull modules list
            hull.getHullModules(); // ensure list is initialized
            // We need to set the modules directly
            List<HullModule> modules = new ArrayList<>(hull.getHullModules());
            modules.add(module);
            hull.setHullModules(modules);

            indexToModuleId.put(i, moduleId);
        }

        // Create connections
        if (connections != null) {
            for (ConnectionDefinition conn : connections) {
                String fromModuleId = indexToModuleId.get(conn.fromModuleIndex);
                String toModuleId = indexToModuleId.get(conn.toModuleIndex);
                if (fromModuleId != null && toModuleId != null) {
                    hull.connectModules(fromModuleId, conn.fromCell, toModuleId, conn.toCell);
                }
            }
        }

        // Install AI Core for network connection
        ship.setAiCore(new AiCoreComponent());

        // Register with asset registry and assign default skin
        registerShip(ship);

        return ship;
    }

    /**
     * Connects two modules at passage cells within an existing ship hull.
     *
     * @param hull the ship hull to modify
     * @param fromModuleId the source module ID
     * @param fromCell the passage cell in the source module
     * @param toModuleId the target module ID
     * @param toCell the passage cell in the target module
     */
    public void connectAtPassage(ShipHull hull, String fromModuleId, M3space fromCell,
                                  String toModuleId, M3space toCell) {
        hull.connectModules(fromModuleId, fromCell, toModuleId, toCell);
    }

    /**
     * Generates a unique module ID based on the template name.
     *
     * @param templateName the template name
     * @return a unique module ID
     */
    private String generateModuleId(String templateName) {
        moduleCounter++;
        return templateName.toLowerCase().replace(" ", "_") + "_" + moduleCounter;
    }

    // ==========================================
    // Asset Registration
    // ==========================================

    /**
     * Registers a ship with the asset registry and assigns a default skin.
     */
    private void registerShip(Ship ship) {
        if (AssetRegistrySpi == null) {
            return;
        }
        int id = ship.getId();
        AssetRegistrySpi.register(SkinType.SHIP, id, ship);
        // Assign default sci-fi ship skin if available
        if (SkinLookupSpi != null && ship.getSkin() == null) {
            Skin defaultSkin = SkinLookupSpi.getSkin("sci-fi-ship");
            if (defaultSkin != null) {
                ship.setSkin(defaultSkin);
            }
        }
    }

    /**
     * Defines a connection between two modules by template index.
     * <p>
     * Used during assembly to specify which modules connect at which cells.
     * </p>
     */
    public static class ConnectionDefinition {
        private final int fromModuleIndex;
        private final M3space fromCell;
        private final int toModuleIndex;
        private final M3space toCell;

        /**
         * Creates a connection definition.
         *
         * @param fromModuleIndex index of the source module in the templates list
         * @param fromCell the passage cell in the source module
         * @param toModuleIndex index of the target module in the templates list
         * @param toCell the passage cell in the target module
         */
        public ConnectionDefinition(int fromModuleIndex, M3space fromCell,
                                     int toModuleIndex, M3space toCell) {
            this.fromModuleIndex = fromModuleIndex;
            this.fromCell = fromCell;
            this.toModuleIndex = toModuleIndex;
            this.toCell = toCell;
        }

        public int getFromModuleIndex() { return fromModuleIndex; }
        public M3space getFromCell() { return fromCell; }
        public int getToModuleIndex() { return toModuleIndex; }
        public M3space getToCell() { return toCell; }
    }
}
