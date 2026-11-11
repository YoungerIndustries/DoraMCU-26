package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.hex.dungeon.ShipModuleTemplate;
import com.yisy.modules.doramcu.hex.dungeon.ShipModuleTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for managing ship module templates and ship assembly.
 * <p>
 * Provides CRUD operations for module templates, template browsing and filtering,
 * and ship creation from templates. Also provides pre-population of starter
 * module templates for immediate use.
 * </p>
 */
@Service
public class ShipService {

    private final ShipModuleTemplateRepository templateRepository;
    private final ShipBuilder builder;

    /**
     * Creates a new ShipService.
     *
     * @param templateRepository the JPA repository for module templates
     */
    public ShipService(ShipModuleTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
        this.builder = new ShipBuilder();
    }

    // ---- Module Template CRUD ----

    /**
     * Creates and saves a new module template.
     *
     * @param template the template to save
     * @return the saved template
     */
    public ShipModuleTemplate createModuleTemplate(ShipModuleTemplate template) {
        return templateRepository.save(template);
    }

    /**
     * Updates an existing module template.
     *
     * @param template the template with updated data
     * @return the updated template
     */
    public ShipModuleTemplate updateModuleTemplate(ShipModuleTemplate template) {
        if (!templateRepository.existsById(template.getId())) {
            throw new IllegalArgumentException("Template not found: " + template.getId());
        }
        return templateRepository.save(template);
    }

    /**
     * Deletes a module template by ID.
     *
     * @param id the template ID
     */
    public void deleteModuleTemplate(Long id) {
        templateRepository.deleteById(id);
    }

    /**
     * @return all module templates
     */
    public List<ShipModuleTemplate> getModuleTemplates() {
        return templateRepository.findAll();
    }

    /**
     * @param id the template ID
     * @return the template, or null if not found
     */
    public Optional<ShipModuleTemplate> getModuleTemplate(Long id) {
        return templateRepository.findById(id);
    }

    /**
     * Finds templates by ship module type.
     *
     * @param type the module type
     * @return list of matching templates
     */
    public List<ShipModuleTemplate> getModulesByType(ShipModuleTemplate.ShipModuleType type) {
        return templateRepository.findByType(type);
    }

    /**
     * Finds templates by tag.
     *
     * @param tag the tag to search for
     * @return list of matching templates
     */
    public List<ShipModuleTemplate> getModulesByTag(String tag) {
        return templateRepository.findByTagContaining(tag);
    }

    /**
     * Finds templates compatible with the given ship type.
     *
     * @param shipType the ship type
     * @return list of compatible templates
     */
    public List<ShipModuleTemplate> getModulesForShipType(ShipType shipType) {
        return templateRepository.findByCompatibleShipTypes(shipType.name());
    }

    /**
     * Finds templates by difficulty range.
     *
     * @param maxDifficulty the maximum difficulty
     * @return list of matching templates
     */
    public List<ShipModuleTemplate> getModulesByDifficulty(int maxDifficulty) {
        return templateRepository.findByDifficultyLessThanEqual(maxDifficulty);
    }

    /**
     * Finds templates ordered by difficulty.
     *
     * @return all templates sorted by difficulty ascending
     */
    public List<ShipModuleTemplate> getModulesOrderedByDifficulty() {
        return templateRepository.findAllOrderByDifficultyAsc();
    }

    // ---- Ship Assembly ----

    /**
     * Creates a ship from templates and connection definitions.
     *
     * @param name the ship name
     * @param shipType the ship type classification
     * @param templateIds list of template IDs to use
     * @param connections list of connection definitions
     * @return the assembled ship
     */
    public Ship createShip(String name, ShipType shipType,
                           List<Long> templateIds,
                           List<ShipBuilder.ConnectionDefinition> connections) {
        List<ShipModuleTemplate> templates = templateRepository.findAllById(templateIds);
        if (templates.size() != templateIds.size()) {
            throw new IllegalArgumentException("One or more templates not found");
        }
        return builder.assemble(name, shipType, templates, connections);
    }

    /**
     * Creates a single-module ship from a template.
     *
     * @param name the ship name
     * @param shipType the ship type
     * @param templateId the template ID
     * @return the assembled ship
     */
    public Ship createShipFromTemplate(String name, ShipType shipType, Long templateId) {
        ShipModuleTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));
        Ship ship = new Ship(0, shipType);
        ship.setName(name);
        HullModule module = builder.buildFromTemplate(template);
        ShipHull hull = new ShipHull(ship);
        List<HullModule> modules = new ArrayList<>(hull.getHullModules());
        modules.add(module);
        hull.setHullModules(modules);
        hull.registerModule("module_1", module);
        return ship;
    }

    // ---- Pre-population ----

    /**
     * Pre-populates the database with basic starter ship module templates.
     * <p>
     * Creates templates for: bridge, engine room, weapon bay, cargo hold,
     * crew quarters, and corridor. These are only created if no templates exist yet.
     * </p>
     *
     * @return number of templates created
     */
    public int prepopulateTemplates() {
        if (templateRepository.count() > 0) {
            return 0;  // Already populated
        }

        List<ShipModuleTemplate> templates = new ArrayList<>();

        // Bridge module
        templates.add(createBridgeTemplate());

        // Engine room
        templates.add(createEngineRoomTemplate());

        // Weapon bay
        templates.add(createWeaponBayTemplate());

        // Cargo hold
        templates.add(createCargoHoldTemplate());

        // Crew quarters
        templates.add(createCrewQuartersTemplate());

        // Corridor
        templates.add(createCorridorTemplate());

        templateRepository.saveAll(templates);
        return templates.size();
    }

    private ShipModuleTemplate createBridgeTemplate() {
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

        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 3; z++) {
                    cells.add(new M3space(x, y, z));
                }
            }
        }
        template.setShapeCellsJson(ShipModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(
                "[{\"name\":\"Bridge\",\"type\":\"COMMAND\",\"cells\":[" +
                String.join(",", cells.stream().map(c -> "\"" + c.getX() + "," + c.getY() + "," + c.getZ() + "\"").toArray(String[]::new)) +
                "]}"
        );
        template.setConnectionPointsJson(
                "[{\"x\":5,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":5,\"y\":9,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    private ShipModuleTemplate createEngineRoomTemplate() {
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

        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 4; z++) {
                    cells.add(new M3space(x, y, z));
                }
            }
        }
        template.setShapeCellsJson(ShipModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(
                "[{\"name\":\"Engine Room\",\"type\":\"ENGINE\",\"cells\":[" +
                String.join(",", cells.stream().map(c -> "\"" + c.getX() + "," + c.getY() + "," + c.getZ() + "\"").toArray(String[]::new)) +
                "]}"
        );
        template.setConnectionPointsJson(
                "[{\"x\":5,\"y\":0,\"z\":2,\"direction\":\"FORWARD\"},{\"x\":5,\"y\":9,\"z\":2,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    private ShipModuleTemplate createWeaponBayTemplate() {
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

        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 3; z++) {
                    cells.add(new M3space(x, y, z));
                }
            }
        }
        template.setShapeCellsJson(ShipModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(
                "[{\"name\":\"Weapon Bay\",\"type\":\"WEAPONS\",\"cells\":[" +
                String.join(",", cells.stream().map(c -> "\"" + c.getX() + "," + c.getY() + "," + c.getZ() + "\"").toArray(String[]::new)) +
                "]}"
        );
        template.setConnectionPointsJson(
                "[{\"x\":5,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":5,\"y\":7,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    private ShipModuleTemplate createCargoHoldTemplate() {
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

        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 3; z++) {
                    cells.add(new M3space(x, y, z));
                }
            }
        }
        template.setShapeCellsJson(ShipModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(
                "[{\"name\":\"Cargo Hold\",\"type\":\"CARGO\",\"cells\":[" +
                String.join(",", cells.stream().map(c -> "\"" + c.getX() + "," + c.getY() + "," + c.getZ() + "\"").toArray(String[]::new)) +
                "]}"
        );
        template.setConnectionPointsJson(
                "[{\"x\":5,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":5,\"y\":9,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    private ShipModuleTemplate createCrewQuartersTemplate() {
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

        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 3; z++) {
                    cells.add(new M3space(x, y, z));
                }
            }
        }
        template.setShapeCellsJson(ShipModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(
                "[{\"name\":\"Crew Quarters\",\"type\":\"CREW\",\"cells\":[" +
                String.join(",", cells.stream().map(c -> "\"" + c.getX() + "," + c.getY() + "," + c.getZ() + "\"").toArray(String[]::new)) +
                "]}"
        );
        template.setConnectionPointsJson(
                "[{\"x\":5,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":5,\"y\":9,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }

    private ShipModuleTemplate createCorridorTemplate() {
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

        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 2; z++) {
                    cells.add(new M3space(x, y, z));
                }
            }
        }
        template.setShapeCellsJson(ShipModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(
                "[{\"name\":\"Corridor\",\"type\":\"TRANSIT\",\"cells\":[" +
                String.join(",", cells.stream().map(c -> "\"" + c.getX() + "," + c.getY() + "," + c.getZ() + "\"").toArray(String[]::new)) +
                "]}"
        );
        template.setConnectionPointsJson(
                "[{\"x\":2,\"y\":0,\"z\":1,\"direction\":\"FORWARD\"},{\"x\":2,\"y\":3,\"z\":1,\"direction\":\"AFT\"}]"
        );
        return template;
    }
}
