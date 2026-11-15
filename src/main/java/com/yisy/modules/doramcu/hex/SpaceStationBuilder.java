package com.yisy.modules.doramcu.hex;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.gamecore.DungeonRoomService;
import com.yisy.modules.doramcu.hex.dungeon.StationModuleTemplate;
import com.yisy.modules.doramcu.hex.dungeon.StationModuleTemplateRepository;
import com.yisy.modules.doramcu.hex.planet.BuildingInteriorGenerator;
import com.yisy.modules.doramcu.hex.spi.AssetRegistrySpi;
import com.yisy.modules.doramcu.hex.Skin;
import com.yisy.modules.doramcu.hex.spi.SkinLookupSpi;
import com.yisy.modules.doramcu.hex.SkinType;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelService;

import java.util.*;

/**
 * Service for building space stations by combining 3D hull modules with interior dungeon rooms.
 * <p>
 * The SpaceStationBuilder orchestrates two dungeon creation paradigms:
 * </p>
 * <ol>
 *   <li><b>Exterior Hull (3D Template Method)</b> â€” Uses {@link StationModuleTemplate} instances
 *       to create the station's 3D structure. Each template becomes a module section with
 *       {@link HullModule} instances, {@link Compartment} objects for named functional areas,
 *       and connection points for inter-module doorways.</li>
 *   <li><b>Interior Rooms (Graph-Based Method)</b> â€” Uses {@link BuildingInteriorGenerator}
 *       to create explorable interior rooms with bidirectional connections, traps, treasures,
 *       and encounters. The Space Port template provides 10 rooms for station interiors.</li>
 * </ol>
 * <p>
 * The bridge between the two paradigms is {@link CompartmentType}:
 * </p>
 * <ul>
 *   <li>Exterior hull â†’ {@link Compartment} objects with {@link CompartmentType} (COMMAND, DOCKING, etc.)</li>
 *   <li>Interior rooms â†’ {@link DungeonRoomService.DungeonRoom} objects with theme (Social, Combat, Storage, etc.)</li>
 *   <li>Mapping: {@link CompartmentType#fromString(String)} provides fuzzy mapping between domain-specific types</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 * // Create station module templates
 * StationModuleTemplate commandModule = builder.createCommandModuleTemplate();
 * StationModuleTemplate dockingRing = builder.createDockingRingTemplate();
 *
 * // Build a station from templates
 * SpaceStation station = builder.buildStation("Babylon-5", SpaceStationType.MILITARY,
 *     Arrays.asList(commandModule.getId(), dockingRing.getId()));
 *
 * // Generate interior rooms for the station
 * String dungeonId = builder.generateStationInteriors(station, TechLevel.TL8);
 * </pre>
 */
@Service
public class SpaceStationBuilder {

    private final StationModuleTemplateRepository templateRepository;
    private final DungeonRoomService dungeonRoomService;
    private final TechLevelService techLevelService;
    private final AssetRegistrySpi AssetRegistrySpi;
    private final SkinLookupSpi SkinLookupSpi;

    /**
     * Creates a new SpaceStationBuilder.
     *
     * @param templateRepository the JPA repository for station module templates
     * @param dungeonRoomService the service for creating interior dungeon rooms
     * @param techLevelService the service for tech-level-aware module lookup (may be null)
     * @param AssetRegistrySpi the central asset registry for skin assignment
     * @param SkinLookupSpi the service for skin management
     */
    public SpaceStationBuilder(StationModuleTemplateRepository templateRepository,
                                DungeonRoomService dungeonRoomService,
                                TechLevelService techLevelService,
                                AssetRegistrySpi AssetRegistrySpi,
                                SkinLookupSpi SkinLookupSpi) {
        this.templateRepository = templateRepository;
        this.dungeonRoomService = dungeonRoomService;
        this.techLevelService = techLevelService;
        this.AssetRegistrySpi = AssetRegistrySpi;
        this.SkinLookupSpi = SkinLookupSpi;
    }

    // ==========================================
    // Station Module Template CRUD
    // ==========================================

    /**
     * Creates a new station module template and persists it.
     *
     * @param template the template to create
     * @return the saved template
     */
    public StationModuleTemplate createModuleTemplate(StationModuleTemplate template) {
        return templateRepository.save(template);
    }

    /**
     * Updates an existing station module template.
     *
     * @param template the template to update
     * @return the saved template
     * @throws IllegalArgumentException if the template ID does not exist
     */
    public StationModuleTemplate updateModuleTemplate(StationModuleTemplate template) {
        if (!templateRepository.existsById(template.getId())) {
            throw new IllegalArgumentException("Template not found: " + template.getId());
        }
        return templateRepository.save(template);
    }

    /**
     * Deletes a station module template by ID.
     *
     * @param id the template ID
     */
    public void deleteModuleTemplate(Long id) {
        templateRepository.deleteById(id);
    }

    /**
     * Returns all station module templates.
     *
     * @return list of all templates
     */
    public List<StationModuleTemplate> getModuleTemplates() {
        return templateRepository.findAll();
    }

    /**
     * Returns a station module template by ID.
     *
     * @param id the template ID
     * @return the template or empty if not found
     */
    public Optional<StationModuleTemplate> getModuleTemplate(Long id) {
        return templateRepository.findById(id);
    }

    /**
     * Returns templates filtered by module type.
     *
     * @param type the station module type
     * @return list of matching templates
     */
    public List<StationModuleTemplate> getModulesByType(StationModuleTemplate.StationModuleType type) {
        return templateRepository.findByType(type);
    }

    /**
     * Returns templates filtered by tag.
     *
     * @param tag the tag to search for
     * @return list of matching templates
     */
    public List<StationModuleTemplate> getModulesByTag(String tag) {
        return templateRepository.findByTagContaining(tag);
    }

    /**
     * Returns templates with difficulty less than or equal to the given value.
     *
     * @param maxDifficulty the maximum difficulty
     * @return list of matching templates
     */
    public List<StationModuleTemplate> getModulesByDifficulty(int maxDifficulty) {
        return templateRepository.findByDifficultyLessThanEqual(maxDifficulty);
    }

    /**
     * Returns all templates ordered by difficulty ascending.
     *
     * @return list of templates sorted by difficulty
     */
    public List<StationModuleTemplate> getModulesOrderedByDifficulty() {
        return templateRepository.findAllOrderByDifficultyAsc();
    }

    // ==========================================
    // Station Assembly from Templates
    // ==========================================

    /**
     * Builds a complete SpaceStation from station module templates.
     * <p>
     * Each template becomes a module section in the station. Templates are processed
     * in order, and connection points are used to create inter-module doorways.
     * The station's hull and shield points are calculated from the module contributions.
     * </p>
     *
     * @param name the station name
     * @param stationType the station type (determines base stats)
     * @param templateIds list of template IDs to use (in module order)
     * @return the assembled SpaceStation
     * @throws IllegalArgumentException if one or more templates are not found
     */
    public SpaceStation buildStation(String name, SpaceStationType stationType, List<Long> templateIds) {
        List<StationModuleTemplate> templates = templateRepository.findAllById(templateIds);
        if (templates.size() != templateIds.size()) {
            throw new IllegalArgumentException("One or more templates not found");
        }

        // Create the station
        SpaceStation station = new SpaceStation(
                templateIds.isEmpty() ? 0 : templateIds.get(0).intValue(),
                stationType
        );
        station.setName(name);

        // Calculate total contributions from templates
        int totalDockingPorts = 0;
        int totalShieldContribution = 0;
        int totalHullContribution = 0;

        // Build the 3D hull structure
        BuildingHull hull = new BuildingHull(name);
        List<String> moduleIds = new ArrayList<>();

        for (int i = 0; i < templates.size(); i++) {
            StationModuleTemplate template = templates.get(i);
            HullModule module = template.toHullModule();
            String moduleId = "module_" + i;

            hull.addFloor(module);
            hull.registerFloor(moduleId, module);
            moduleIds.add(moduleId);

            // Accumulate contributions
            totalDockingPorts += template.getDockingPorts();
            totalShieldContribution += template.getShieldContribution();
            totalHullContribution += template.getHullContribution();

            // Register compartments in the station
            for (Compartment compartment : module.getCompartments()) {
                station.addCompartment(compartment);
            }
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

        // Apply template contributions to station stats
        if (totalHullContribution > 0) {
            station.setHullPoints(station.getHullPoints() + totalHullContribution);
            station.setCurrentHullPoints(station.getCurrentHullPoints() + totalHullContribution);
        }
        if (totalShieldContribution > 0) {
            station.setShieldPoints(station.getShieldPoints() + totalShieldContribution);
            station.setCurrentShieldPoints(station.getCurrentShieldPoints() + totalShieldContribution);
        }
        if (totalDockingPorts > 0) {
            station.setMaxDockedShips(station.getMaxDockedShips() + totalDockingPorts);
        }

        // Set the hull module on the station
        if (!moduleIds.isEmpty()) {
            String firstModuleId = moduleIds.get(0);
            HullModule firstModule = hull.getFloorById(firstModuleId);
            if (firstModule != null) {
                station.setHullModule(firstModule);
            }
        }

        // Register with asset registry and assign default skin
        registerStation(station);

        return station;
    }

    /**
     * Builds a SpaceStation with default MILITARY type.
     *
     * @param name the station name
     * @param templateIds list of template IDs
     * @return the assembled SpaceStation
     */
    public SpaceStation buildStation(String name, List<Long> templateIds) {
        return buildStation(name, SpaceStationType.MILITARY, templateIds);
    }

    // ==========================================
    // Interior Room Generation
    // ==========================================

    /**
     * Generates interior dungeon rooms for a space station.
     * <p>
     * Uses {@link BuildingInteriorGenerator} with the Space Port template to create
     * 10 explorable rooms with connections, traps, treasures, and encounters.
     * The rooms are tech-level aware: higher tech levels produce sci-fi themed rooms.
     * </p>
     *
     * @param station the space station to generate interiors for
     * @param techLevel the technology level (determines room names and features)
     * @param seed random seed for reproducible generation
     * @return the dungeon ID for the generated interior rooms
     */
    public String generateStationInteriors(SpaceStation station, TechLevel techLevel, long seed) {
        String dungeonId = station.getName() + "-interior";

        BuildingInteriorGenerator generator = new BuildingInteriorGenerator(
                dungeonRoomService, techLevelService, seed
        );

        // Use the Space Port template which provides 10 rooms suitable for a space station
        return generator.generateSpacePort(dungeonId, techLevel);
    }

    /**
     * Generates interior dungeon rooms with default tech level (TL8 for space stations).
     *
     * @param station the space station to generate interiors for
     * @param seed random seed for reproducible generation
     * @return the dungeon ID for the generated interior rooms
     */
    public String generateStationInteriors(SpaceStation station, long seed) {
        return generateStationInteriors(station, TechLevel.TL8, seed);
    }

    /**
     * Generates interior dungeon rooms with a random seed.
     *
     * @param station the space station to generate interiors for
     * @param techLevel the technology level
     * @return the dungeon ID for the generated interior rooms
     */
    public String generateStationInteriors(SpaceStation station, TechLevel techLevel) {
        return generateStationInteriors(station, techLevel, new Random().nextLong());
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
     * @param station the space station with compartments
     * @param dungeonId the dungeon ID of the interior rooms
     * @return a map of compartment names to their matched dungeon room IDs
     */
    public Map<String, Long> mapCompartmentsToRooms(SpaceStation station, String dungeonId) {
        Map<String, Long> mapping = new HashMap<>();
        List<DungeonRoomService.DungeonRoom> rooms = dungeonRoomService.listRoomsByDungeon(dungeonId);

        for (Map.Entry<String, Compartment> entry : station.getCompartments().entrySet()) {
            String compartmentName = entry.getKey();
            Compartment compartment = entry.getValue();
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
            case "crafting" -> type == CompartmentType.RESEARCH;
            default -> false;
        };
    }

    // ==========================================
    // Pre-population of Station Templates
    // ==========================================

    /**
     * Pre-populates the database with starter station module templates.
     * <p>
     * Creates templates for all 13 station module types with default dimensions,
     * compartments, and connection points. Called once during initialization.
     * </p>
     *
     * @return the number of templates created
     */
    public int prepopulateTemplates() {
        if (templateRepository.count() > 0) {
            return 0;
        }

        List<StationModuleTemplate> templates = new ArrayList<>();

        // Core modules
        templates.add(createCommandModuleTemplate());
        templates.add(createDockingRingTemplate());
        templates.add(createCrewQuartersTemplate());
        templates.add(createCargoModuleTemplate());
        templates.add(createCorridorTemplate());

        // Support modules
        templates.add(createPowerModuleTemplate());
        templates.add(createLifeSupportTemplate());
        templates.add(createRepairBayTemplate());
        templates.add(createResearchLabTemplate());

        // Defense modules
        templates.add(createWeaponPlatformTemplate());
        templates.add(createShieldArrayTemplate());
        templates.add(createSensorArrayTemplate());

        templateRepository.saveAll(templates);
        return templates.size();
    }

    // ==========================================
    // Template Factory Methods
    // ==========================================

    /**
     * Creates a Command Module template.
     * <p>10Ã—10Ã—4 grid with command center compartment and 2 docking ports.</p>
     */
    public StationModuleTemplate createCommandModuleTemplate() {
        StationModuleTemplate template = new StationModuleTemplate();
        template.setName("Command Module");
        template.setDescription("Command and control center for station operations.");
        template.setType(StationModuleTemplate.StationModuleType.COMMAND_MODULE);
        template.setWidth(10);
        template.setHeight(10);
        template.setDepth(4);
        template.setDifficulty(5);
        template.setTags("command,control,operations,bridge");
        template.setDockingPorts(2);
        template.setShieldContribution(20);
        template.setHullContribution(100);

        Set<M3space> cells = generateRectCells(10, 10, 4);
        template.setShapeCellsJson(StationModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Command Center", "COMMAND", cells));
        template.setConnectionPointsJson(
                "[{\"x\":5,\"y\":0,\"z\":2,\"direction\":\"FORWARD\"},{\"x\":5,\"y\":9,\"z\":2,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Docking Ring template.
     * <p>12Ã—12Ã—3 grid with docking bay compartment and 4 docking ports.</p>
     */
    public StationModuleTemplate createDockingRingTemplate() {
        StationModuleTemplate template = new StationModuleTemplate();
        template.setName("Docking Ring");
        template.setDescription("Ship berthing ring with multiple docking ports.");
        template.setType(StationModuleTemplate.StationModuleType.DOCKING_RING);
        template.setWidth(12);
        template.setHeight(12);
        template.setDepth(3);
        template.setDifficulty(3);
        template.setTags("docking,berthing,ships,ports");
        template.setDockingPorts(4);
        template.setShieldContribution(10);
        template.setHullContribution(80);

        Set<M3space> cells = generateRectCells(12, 12, 3);
        template.setShapeCellsJson(StationModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Docking Bay", "DOCKING", cells));
        template.setConnectionPointsJson(
                "[{\"x\":6,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":6,\"y\":11,\"z\":1,\"direction\":\"AFT\"}," +
                "{\"x\":0,\"y\":6,\"z\":1,\"direction\":\"PORT\"},{\"x\":11,\"y\":6,\"z\":1,\"direction\":\"STARBOARD\"}]"
        );
        return template;
    }

    /**
     * Creates a Crew Quarters template.
     * <p>8Ã—8Ã—3 grid with crew living compartment.</p>
     */
    public StationModuleTemplate createCrewQuartersTemplate() {
        StationModuleTemplate template = new StationModuleTemplate();
        template.setName("Crew Quarters");
        template.setDescription("Living quarters and amenities for station crew.");
        template.setType(StationModuleTemplate.StationModuleType.CREW_QUARTERS);
        template.setWidth(8);
        template.setHeight(8);
        template.setDepth(3);
        template.setDifficulty(2);
        template.setTags("crew,quarters,living,amenities");
        template.setDockingPorts(0);
        template.setShieldContribution(5);
        template.setHullContribution(50);

        Set<M3space> cells = generateRectCells(8, 8, 3);
        template.setShapeCellsJson(StationModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Crew Quarters", "CREW", cells));
        template.setConnectionPointsJson(
                "[{\"x\":4,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":4,\"y\":7,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Cargo Module template.
     * <p>10Ã—10Ã—3 grid with cargo storage compartment.</p>
     */
    public StationModuleTemplate createCargoModuleTemplate() {
        StationModuleTemplate template = new StationModuleTemplate();
        template.setName("Cargo Module");
        template.setDescription("Cargo storage and transfer section.");
        template.setType(StationModuleTemplate.StationModuleType.CARGO_MODULE);
        template.setWidth(10);
        template.setHeight(10);
        template.setDepth(3);
        template.setDifficulty(2);
        template.setTags("cargo,storage,transfer,warehouse");
        template.setDockingPorts(1);
        template.setShieldContribution(5);
        template.setHullContribution(60);

        Set<M3space> cells = generateRectCells(10, 10, 3);
        template.setShapeCellsJson(StationModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Cargo Hold", "CARGO", cells));
        template.setConnectionPointsJson(
                "[{\"x\":5,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":5,\"y\":9,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Corridor template.
     * <p>4Ã—4Ã—2 grid with transit compartment for connecting sections.</p>
     */
    public StationModuleTemplate createCorridorTemplate() {
        StationModuleTemplate template = new StationModuleTemplate();
        template.setName("Corridor");
        template.setDescription("Passage connecting station sections.");
        template.setType(StationModuleTemplate.StationModuleType.CORRIDOR);
        template.setWidth(4);
        template.setHeight(4);
        template.setDepth(2);
        template.setDifficulty(1);
        template.setTags("passage,connection,transit");
        template.setDockingPorts(0);
        template.setShieldContribution(0);
        template.setHullContribution(20);

        Set<M3space> cells = generateRectCells(4, 4, 2);
        template.setShapeCellsJson(StationModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Corridor", "TRANSIT", cells));
        template.setConnectionPointsJson(
                "[{\"x\":2,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":2,\"y\":3,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Power Module template.
     * <p>8Ã—8Ã—3 grid with power generation compartment.</p>
     */
    public StationModuleTemplate createPowerModuleTemplate() {
        StationModuleTemplate template = new StationModuleTemplate();
        template.setName("Power Module");
        template.setDescription("Energy generation and distribution module.");
        template.setType(StationModuleTemplate.StationModuleType.POWER_MODULE);
        template.setWidth(8);
        template.setHeight(8);
        template.setDepth(3);
        template.setDifficulty(4);
        template.setTags("power,energy,reactor,generation");
        template.setDockingPorts(0);
        template.setShieldContribution(10);
        template.setHullContribution(70);

        Set<M3space> cells = generateRectCells(8, 8, 3);
        template.setShapeCellsJson(StationModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Power Core", "ENGINE", cells));
        template.setConnectionPointsJson(
                "[{\"x\":4,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":4,\"y\":7,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Life Support template.
     * <p>6Ã—6Ã—3 grid with environmental systems compartment.</p>
     */
    public StationModuleTemplate createLifeSupportTemplate() {
        StationModuleTemplate template = new StationModuleTemplate();
        template.setName("Life Support");
        template.setDescription("Environmental control and life support systems.");
        template.setType(StationModuleTemplate.StationModuleType.LIFE_SUPPORT);
        template.setWidth(6);
        template.setHeight(6);
        template.setDepth(3);
        template.setDifficulty(3);
        template.setTags("life-support,environmental,air,water");
        template.setDockingPorts(0);
        template.setShieldContribution(5);
        template.setHullContribution(40);

        Set<M3space> cells = generateRectCells(6, 6, 3);
        template.setShapeCellsJson(StationModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Life Support", "CREW", cells));
        template.setConnectionPointsJson(
                "[{\"x\":3,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":3,\"y\":5,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Repair Bay template.
     * <p>8Ã—8Ã—3 grid with maintenance compartment.</p>
     */
    public StationModuleTemplate createRepairBayTemplate() {
        StationModuleTemplate template = new StationModuleTemplate();
        template.setName("Repair Bay");
        template.setDescription("Maintenance and repair facilities.");
        template.setType(StationModuleTemplate.StationModuleType.REPAIR_BAY);
        template.setWidth(8);
        template.setHeight(8);
        template.setDepth(3);
        template.setDifficulty(3);
        template.setTags("repair,maintenance,engineering,workshop");
        template.setDockingPorts(1);
        template.setShieldContribution(5);
        template.setHullContribution(50);

        Set<M3space> cells = generateRectCells(8, 8, 3);
        template.setShapeCellsJson(StationModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Repair Bay", "ENGINE", cells));
        template.setConnectionPointsJson(
                "[{\"x\":4,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":4,\"y\":7,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Research Lab template.
     * <p>8Ã—8Ã—3 grid with research compartment.</p>
     */
    public StationModuleTemplate createResearchLabTemplate() {
        StationModuleTemplate template = new StationModuleTemplate();
        template.setName("Research Lab");
        template.setDescription("Science laboratory and research facilities.");
        template.setType(StationModuleTemplate.StationModuleType.RESEARCH_LAB);
        template.setWidth(8);
        template.setHeight(8);
        template.setDepth(3);
        template.setDifficulty(5);
        template.setTags("research,science,lab,experiment");
        template.setDockingPorts(0);
        template.setShieldContribution(10);
        template.setHullContribution(50);

        Set<M3space> cells = generateRectCells(8, 8, 3);
        template.setShapeCellsJson(StationModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Research Lab", "RESEARCH", cells));
        template.setConnectionPointsJson(
                "[{\"x\":4,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":4,\"y\":7,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Weapon Platform template.
     * <p>8Ã—8Ã—3 grid with weapons compartment and turret mounts.</p>
     */
    public StationModuleTemplate createWeaponPlatformTemplate() {
        StationModuleTemplate template = new StationModuleTemplate();
        template.setName("Weapon Platform");
        template.setDescription("Weapon platform with turret mounts for station defense.");
        template.setType(StationModuleTemplate.StationModuleType.WEAPON_PLATFORM);
        template.setWidth(8);
        template.setHeight(8);
        template.setDepth(3);
        template.setDifficulty(6);
        template.setTags("weapons,turret,defense,combat");
        template.setDockingPorts(0);
        template.setShieldContribution(15);
        template.setHullContribution(80);

        Set<M3space> cells = generateRectCells(8, 8, 3);
        template.setShapeCellsJson(StationModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Weapon Platform", "WEAPONS", cells));
        template.setConnectionPointsJson(
                "[{\"x\":4,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":4,\"y\":7,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Shield Array template.
     * <p>6Ã—6Ã—3 grid with shield generator compartment.</p>
     */
    public StationModuleTemplate createShieldArrayTemplate() {
        StationModuleTemplate template = new StationModuleTemplate();
        template.setName("Shield Array");
        template.setDescription("Shield generator array for station defense.");
        template.setType(StationModuleTemplate.StationModuleType.SHIELD_ARRAY);
        template.setWidth(6);
        template.setHeight(6);
        template.setDepth(3);
        template.setDifficulty(5);
        template.setTags("shield,defense,generator,protection");
        template.setDockingPorts(0);
        template.setShieldContribution(30);
        template.setHullContribution(50);

        Set<M3space> cells = generateRectCells(6, 6, 3);
        template.setShapeCellsJson(StationModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Shield Array", "DEFENSE", cells));
        template.setConnectionPointsJson(
                "[{\"x\":3,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":3,\"y\":5,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    /**
     * Creates a Sensor Array template.
     * <p>6Ã—6Ã—3 grid with sensor and communications compartment.</p>
     */
    public StationModuleTemplate createSensorArrayTemplate() {
        StationModuleTemplate template = new StationModuleTemplate();
        template.setName("Sensor Array");
        template.setDescription("Sensor and communications array for long-range detection.");
        template.setType(StationModuleTemplate.StationModuleType.SENSOR_ARRAY);
        template.setWidth(6);
        template.setHeight(6);
        template.setDepth(3);
        template.setDifficulty(4);
        template.setTags("sensor,communications,detection,radar");
        template.setDockingPorts(0);
        template.setShieldContribution(10);
        template.setHullContribution(30);

        Set<M3space> cells = generateRectCells(6, 6, 3);
        template.setShapeCellsJson(StationModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(buildCompartmentJson("Sensor Array", "RESEARCH", cells));
        template.setConnectionPointsJson(
                "[{\"x\":3,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":3,\"y\":5,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    // ==========================================
    // Asset Registration
    // ==========================================

    /**
     * Registers a space station with the asset registry and assigns a default skin.
     */
    private void registerStation(SpaceStation station) {
        if (AssetRegistrySpi == null) {
            return;
        }
        int id = station.getId();
        AssetRegistrySpi.register(SkinType.SPACE_STATION, id, station);
        if (SkinLookupSpi != null && station.getSkin() == null) {
            Skin defaultSkin = SkinLookupSpi.getSkin("industrial-station");
            if (defaultSkin != null) {
                station.setSkin(defaultSkin);
            }
        }
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
}
