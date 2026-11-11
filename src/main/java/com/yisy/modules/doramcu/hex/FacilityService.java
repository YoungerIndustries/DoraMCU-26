package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.hex.dungeon.FacilityModuleTemplate;
import com.yisy.modules.doramcu.hex.dungeon.FacilityModuleTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for managing facility module templates and facility assembly.
 * <p>
 * Provides CRUD operations for module templates, template browsing and filtering,
 * and facility creation from templates. Also provides pre-population of starter
 * module templates for immediate use.
 * </p>
 */
@Service
public class FacilityService {

    private final FacilityModuleTemplateRepository templateRepository;

    /**
     * Creates a new FacilityService.
     *
     * @param templateRepository the JPA repository for module templates
     */
    public FacilityService(FacilityModuleTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    // ---- Module Template CRUD ----

    public FacilityModuleTemplate createModuleTemplate(FacilityModuleTemplate template) {
        return templateRepository.save(template);
    }

    public FacilityModuleTemplate updateModuleTemplate(FacilityModuleTemplate template) {
        if (!templateRepository.existsById(template.getId())) {
            throw new IllegalArgumentException("Template not found: " + template.getId());
        }
        return templateRepository.save(template);
    }

    public void deleteModuleTemplate(Long id) {
        templateRepository.deleteById(id);
    }

    public List<FacilityModuleTemplate> getModuleTemplates() {
        return templateRepository.findAll();
    }

    public Optional<FacilityModuleTemplate> getModuleTemplate(Long id) {
        return templateRepository.findById(id);
    }

    public List<FacilityModuleTemplate> getModulesByType(FacilityModuleTemplate.FacilityModuleType type) {
        return templateRepository.findByType(type);
    }

    public List<FacilityModuleTemplate> getModulesByTag(String tag) {
        return templateRepository.findByTagContaining(tag);
    }

    public List<FacilityModuleTemplate> getModulesByDifficulty(int maxDifficulty) {
        return templateRepository.findByDifficultyLessThanEqual(maxDifficulty);
    }

    public List<FacilityModuleTemplate> getModulesOrderedByDifficulty() {
        return templateRepository.findAllOrderByDifficultyAsc();
    }

    // ---- Facility Assembly ----

    /**
     * Creates a BuildingHull from facility module templates.
     * <p>
     * Each template becomes a floor in the building. Templates are added in order,
     * and vertical connections (stairs/elevators) are created between consecutive floors.
     * </p>
     *
     * @param name the facility name
     * @param templateIds list of template IDs to use (in floor order)
     * @return the assembled BuildingHull
     */
    public BuildingHull createFacility(String name, List<Long> templateIds) {
        List<FacilityModuleTemplate> templates = templateRepository.findAllById(templateIds);
        if (templates.size() != templateIds.size()) {
            throw new IllegalArgumentException("One or more templates not found");
        }

        BuildingHull building = new BuildingHull(name);

        // Instantiate all floors
        List<String> floorIds = new ArrayList<>();
        for (int i = 0; i < templates.size(); i++) {
            FacilityModuleTemplate template = templates.get(i);
            HullModule floor = template.toHullModule();
            String floorId = "floor_" + i;
            building.addFloor(floor);
            building.registerFloor(floorId, floor);
            floorIds.add(floorId);
        }

        // Connect consecutive floors
        for (int i = 0; i < floorIds.size() - 1; i++) {
            String fromFloorId = floorIds.get(i);
            String toFloorId = floorIds.get(i + 1);
            HullModule fromFloor = building.getFloorById(fromFloorId);
            HullModule toFloor = building.getFloorById(toFloorId);

            // Find connection points from templates
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

                M3space fromCell = fromFloor.getCell(fx, fy, fz);
                M3space toCell = toFloor.getCell(tx, ty, tz);

                if (fromCell != null && toCell != null) {
                    building.connectFloors(fromFloorId, fromCell, toFloorId, toCell);
                }
            }
        }

        return building;
    }

    // ---- Pre-population ----

    /**
     * Pre-populates the database with basic starter facility module templates.
     *
     * @return number of templates created
     */
    public int prepopulateTemplates() {
        if (templateRepository.count() > 0) {
            return 0;
        }

        List<FacilityModuleTemplate> templates = new ArrayList<>();
        templates.add(createMiningShaftTemplate());
        templates.add(createPowerGeneratorTemplate());
        templates.add(createResearchLabTemplate());
        templates.add(createWarehouseTemplate());
        templates.add(createCommandCenterTemplate());
        templates.add(createCorridorTemplate());

        templateRepository.saveAll(templates);
        return templates.size();
    }

    private FacilityModuleTemplate createMiningShaftTemplate() {
        FacilityModuleTemplate template = new FacilityModuleTemplate();
        template.setName("Mining Shaft");
        template.setDescription("Underground resource extraction facility.");
        template.setType(FacilityModuleTemplate.FacilityModuleType.MINING_SHAFT);
        template.setWidth(8);
        template.setHeight(8);
        template.setDepth(3);
        template.setDifficulty(3);
        template.setTags("mining,extraction,resources,minerals");
        template.setMineralBonus(10);
        template.setWorkerCapacity(20);

        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 3; z++) {
                    cells.add(new M3space(x, y, z));
                }
            }
        }
        template.setShapeCellsJson(FacilityModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(
                "[{\"name\":\"Mining Shaft\",\"type\":\"MINING_SHAFT\",\"cells\":[" +
                String.join(",", cells.stream().map(c -> "\"" + c.getX() + "," + c.getY() + "," + c.getZ() + "\"").toArray(String[]::new)) +
                "]}"
        );
        template.setConnectionPointsJson("[{\"x\":4,\"y\":0,\"z\":1,\"direction\":\"UP\"},{\"x\":4,\"y\":7,\"z\":1,\"direction\":\"DOWN\"}]");
        return template;
    }

    private FacilityModuleTemplate createPowerGeneratorTemplate() {
        FacilityModuleTemplate template = new FacilityModuleTemplate();
        template.setName("Power Generator");
        template.setDescription("Energy production facility for the colony.");
        template.setType(FacilityModuleTemplate.FacilityModuleType.POWER_GENERATOR);
        template.setWidth(8);
        template.setHeight(8);
        template.setDepth(3);
        template.setDifficulty(4);
        template.setTags("power,energy,reactor,generation");
        template.setEnergyBonus(15);
        template.setWorkerCapacity(10);

        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 3; z++) {
                    cells.add(new M3space(x, y, z));
                }
            }
        }
        template.setShapeCellsJson(FacilityModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(
                "[{\"name\":\"Power Generator\",\"type\":\"POWER_GENERATOR\",\"cells\":[" +
                String.join(",", cells.stream().map(c -> "\"" + c.getX() + "," + c.getY() + "," + c.getZ() + "\"").toArray(String[]::new)) +
                "]}"
        );
        template.setConnectionPointsJson("[{\"x\":4,\"y\":0,\"z\":1,\"direction\":\"UP\"},{\"x\":4,\"y\":7,\"z\":1,\"direction\":\"DOWN\"}]");
        return template;
    }

    private FacilityModuleTemplate createResearchLabTemplate() {
        FacilityModuleTemplate template = new FacilityModuleTemplate();
        template.setName("Research Laboratory");
        template.setDescription("Scientific research and development facility.");
        template.setType(FacilityModuleTemplate.FacilityModuleType.RESEARCH_LAB);
        template.setWidth(8);
        template.setHeight(8);
        template.setDepth(3);
        template.setDifficulty(5);
        template.setTags("research,science,lab,development");
        template.setResearchBonus(12);
        template.setWorkerCapacity(15);

        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 3; z++) {
                    cells.add(new M3space(x, y, z));
                }
            }
        }
        template.setShapeCellsJson(FacilityModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(
                "[{\"name\":\"Research Lab\",\"type\":\"RESEARCH\",\"cells\":[" +
                String.join(",", cells.stream().map(c -> "\"" + c.getX() + "," + c.getY() + "," + c.getZ() + "\"").toArray(String[]::new)) +
                "]}"
        );
        template.setConnectionPointsJson("[{\"x\":4,\"y\":0,\"z\":1,\"direction\":\"UP\"},{\"x\":4,\"y\":7,\"z\":1,\"direction\":\"DOWN\"}]");
        return template;
    }

    private FacilityModuleTemplate createWarehouseTemplate() {
        FacilityModuleTemplate template = new FacilityModuleTemplate();
        template.setName("Warehouse");
        template.setDescription("Large storage facility for resources and supplies.");
        template.setType(FacilityModuleTemplate.FacilityModuleType.WAREHOUSE);
        template.setWidth(10);
        template.setHeight(10);
        template.setDepth(3);
        template.setDifficulty(2);
        template.setTags("storage,warehouse,supplies");
        template.setMineralBonus(5);
        template.setWorkerCapacity(5);

        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 3; z++) {
                    cells.add(new M3space(x, y, z));
                }
            }
        }
        template.setShapeCellsJson(FacilityModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(
                "[{\"name\":\"Warehouse\",\"type\":\"WAREHOUSE\",\"cells\":[" +
                String.join(",", cells.stream().map(c -> "\"" + c.getX() + "," + c.getY() + "," + c.getZ() + "\"").toArray(String[]::new)) +
                "]}"
        );
        template.setConnectionPointsJson("[{\"x\":5,\"y\":0,\"z\":1,\"direction\":\"UP\"},{\"x\":5,\"y\":9,\"z\":1,\"direction\":\"DOWN\"}]");
        return template;
    }

    private FacilityModuleTemplate createCommandCenterTemplate() {
        FacilityModuleTemplate template = new FacilityModuleTemplate();
        template.setName("Command Center");
        template.setDescription("Facility command and control center.");
        template.setType(FacilityModuleTemplate.FacilityModuleType.COMMAND_CENTER);
        template.setWidth(8);
        template.setHeight(8);
        template.setDepth(3);
        template.setDifficulty(3);
        template.setTags("command,control,operations");
        template.setDefenseBonus(5);
        template.setWorkerCapacity(10);

        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 3; z++) {
                    cells.add(new M3space(x, y, z));
                }
            }
        }
        template.setShapeCellsJson(FacilityModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(
                "[{\"name\":\"Command Center\",\"type\":\"COMMAND\",\"cells\":[" +
                String.join(",", cells.stream().map(c -> "\"" + c.getX() + "," + c.getY() + "," + c.getZ() + "\"").toArray(String[]::new)) +
                "]}"
        );
        template.setConnectionPointsJson("[{\"x\":4,\"y\":0,\"z\":1,\"direction\":\"UP\"},{\"x\":4,\"y\":7,\"z\":1,\"direction\":\"DOWN\"}]");
        return template;
    }

    private FacilityModuleTemplate createCorridorTemplate() {
        FacilityModuleTemplate template = new FacilityModuleTemplate();
        template.setName("Corridor");
        template.setDescription("Passage connecting facility sections.");
        template.setType(FacilityModuleTemplate.FacilityModuleType.CORRIDOR);
        template.setWidth(4);
        template.setHeight(4);
        template.setDepth(2);
        template.setDifficulty(1);
        template.setTags("passage,connection,transit");
        template.setWorkerCapacity(0);

        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 2; z++) {
                    cells.add(new M3space(x, y, z));
                }
            }
        }
        template.setShapeCellsJson(FacilityModuleTemplate.serializeCells(cells));
        template.setCompartmentsJson(
                "[{\"name\":\"Corridor\",\"type\":\"TRANSIT\",\"cells\":[" +
                String.join(",", cells.stream().map(c -> "\"" + c.getX() + "," + c.getY() + "," + c.getZ() + "\"").toArray(String[]::new)) +
                "]}"
        );
        template.setConnectionPointsJson("[{\"x\":2,\"y\":0,\"z\":1,\"direction\":\"UP\"},{\"x\":2,\"y\":3,\"z\":1,\"direction\":\"DOWN\"}]");
        return template;
    }
}
