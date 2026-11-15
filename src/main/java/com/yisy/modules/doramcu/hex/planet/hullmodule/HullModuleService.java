package com.yisy.modules.doramcu.hex.planet.hullmodule;

import com.yisy.modules.doramcu.hex.planet.BuildingInteriorGenerator;
import com.yisy.modules.doramcu.hex.planet.MockDungeonRoomService;
import com.yisy.modules.doramcu.hex.planet.TerrainType;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.hex.planet.hullmodule.dto.HullModuleBlueprintDto;
import com.yisy.modules.doramcu.hex.planet.hullmodule.dto.HullModuleDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing HullModule construction, blueprints, and economy integration.
 * <p>
 * This service provides:
 * <ul>
 *   <li>Building construction using predefined types or player blueprints</li>
 *   <li>Blueprint creation, publishing, and licensing</li>
 *   <li>Dungeon interior generation via BuildingInteriorGenerator</li>
 *   <li>Economy integration: cost calculation, maintenance tracking</li>
 *   <li>Module queries: by planet, owner, type, category</li>
 * </ul>
 * </p>
 */
@Service
public class HullModuleService {

    private static final Logger log = LoggerFactory.getLogger(HullModuleService.class);

    private final BuildingInteriorGenerator interiorGenerator;
    private final Map<Long, HullModule> modules = new HashMap<>();
    private final Map<Long, HullModuleBlueprint> blueprints = new HashMap<>();
    private long nextModuleId = 1;
    private long nextBlueprintId = 1;

    public HullModuleService() {
        this.interiorGenerator = new BuildingInteriorGenerator(
                new MockDungeonRoomService(), 42L
        );
    }

    /**
     * Build a new HullModule using a predefined type.
     */
    public HullModuleDto buildModule(String name, HullModuleType moduleType, Long planetId,
                                      int hexX, int hexY, int hexZ, String ownerId,
                                      TerrainType terrain) {
        // Validate terrain
        if (!moduleType.canBuildOn(terrain)) {
            throw new IllegalArgumentException(
                    "Cannot build " + moduleType.getDisplayName() + " on " + terrain +
                    ". Required: " + moduleType.getRequiredTerrain());
        }

        HullModule module = new HullModule(name, moduleType, planetId, hexX, hexY, hexZ, ownerId);
        module.setId(nextModuleId++);

        // Generate dungeon interior
        String dungeonId = generateDungeonForModule(module);
        module.setDungeonId(dungeonId);

        modules.put(module.getId(), module);
        log.info("Built module: {} at ({},{},{}) on planet {}", name, hexX, hexY, hexZ, planetId);

        return toDto(module);
    }

    /**
     * Build a new HullModule without terrain validation (for planet generation).
     * Used by DefaultPlanetSurfaceBuilder during initial planet surface generation
     * where terrain types may not match the required terrain exactly.
     */
    public HullModuleDto buildModuleWithoutValidation(String name, HullModuleType moduleType,
                                                       Long planetId, int hexX, int hexY, int hexZ,
                                                       String ownerId) {
        HullModule module = new HullModule(name, moduleType, planetId, hexX, hexY, hexZ, ownerId);
        module.setId(nextModuleId++);

        // Generate dungeon interior
        String dungeonId = generateDungeonForModule(module);
        module.setDungeonId(dungeonId);

        modules.put(module.getId(), module);
        log.info("Built module (no validation): {} at ({},{},{}) on planet {}", name, hexX, hexY, hexZ, planetId);

        return toDto(module);
    }

    /**
     * Build a new HullModule using a player-created blueprint.
     */
    public HullModuleDto buildFromBlueprint(String name, Long blueprintId, Long planetId,
                                             int hexX, int hexY, int hexZ, String ownerId,
                                             TerrainType terrain) {
        HullModuleBlueprint blueprint = blueprints.get(blueprintId);
        if (blueprint == null) {
            throw new IllegalArgumentException("Blueprint not found: " + blueprintId);
        }

        if (!blueprint.isActive()) {
            throw new IllegalArgumentException("Blueprint is not active: " + blueprint.getBlueprintCode());
        }

        // For custom blueprints, use CUSTOM type with blueprint's characteristics
        HullModule module = new HullModule(name, HullModuleType.CUSTOM, planetId, hexX, hexY, hexZ, ownerId);
        module.setId(nextModuleId++);
        module.setBlueprint(blueprint);
        module.setFloors(blueprint.getFloors());
        module.setBasementLevels(blueprint.getBasementLevels());
        module.setConstructionCost(blueprint.getConstructionCost());
        module.setMaintenanceCost(blueprint.getMaintenanceCost());
        module.setTotalCapacity(blueprint.getTotalCapacity());
        module.setInputCargo(new HashMap<>(blueprint.getInputCargo()));
        module.setOutputCargo(new HashMap<>(blueprint.getOutputCargo()));

        // Generate dungeon interior
        String dungeonId = generateDungeonForModule(module);
        module.setDungeonId(dungeonId);

        // Increment blueprint usage
        blueprint.incrementUsageCount();

        modules.put(module.getId(), module);
        log.info("Built module from blueprint: {} at ({},{},{}) on planet {}",
                name, hexX, hexY, hexZ, planetId);

        return toDto(module);
    }

    /**
     * Create a new player blueprint.
     */
    public HullModuleBlueprintDto createBlueprint(String blueprintCode, String name,
                                                   String description, String creatorId,
                                                   HullModuleType.Category category,
                                                   int constructionCost, int maintenanceCost,
                                                   int floors, int basementLevels,
                                                   int totalCapacity,
                                                   Map<String, Integer> inputCargo,
                                                   Map<String, Integer> outputCargo,
                                                   int techLevelRequired) {
        // Check for duplicate code
        for (HullModuleBlueprint bp : blueprints.values()) {
            if (bp.getBlueprintCode().equals(blueprintCode)) {
                throw new IllegalArgumentException("Blueprint code already exists: " + blueprintCode);
            }
        }

        HullModuleBlueprint blueprint = new HullModuleBlueprint(
                blueprintCode, name, creatorId, category);
        blueprint.setId(nextBlueprintId++);
        blueprint.setDescription(description);
        blueprint.setConstructionCost(constructionCost);
        blueprint.setMaintenanceCost(maintenanceCost);
        blueprint.setFloors(floors);
        blueprint.setBasementLevels(basementLevels);
        blueprint.setTotalCapacity(totalCapacity);
        blueprint.setInputCargo(inputCargo != null ? inputCargo : new HashMap<>());
        blueprint.setOutputCargo(outputCargo != null ? outputCargo : new HashMap<>());
        blueprint.setTechLevelRequired(techLevelRequired);

        blueprints.put(blueprint.getId(), blueprint);
        log.info("Created blueprint: {} by {}", blueprintCode, creatorId);

        return toBlueprintDto(blueprint);
    }

    /**
     * Publish a blueprint for public use.
     */
    public HullModuleBlueprintDto publishBlueprint(Long blueprintId, Integer licenseFee) {
        HullModuleBlueprint blueprint = blueprints.get(blueprintId);
        if (blueprint == null) {
            throw new IllegalArgumentException("Blueprint not found: " + blueprintId);
        }

        blueprint.setPublished(true);
        blueprint.setLicenseFee(licenseFee);
        blueprint.setUpdatedAt(LocalDateTime.now());

        log.info("Published blueprint: {} with license fee: {}",
                blueprint.getBlueprintCode(), licenseFee);

        return toBlueprintDto(blueprint);
    }

    /**
     * Get all modules on a planet as DTOs.
     */
    public List<HullModuleDto> getModulesByPlanet(Long planetId) {
        return modules.values().stream()
                .filter(m -> m.getPlanetId().equals(planetId))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all module entities on a planet (for internal use by cargo generation).
     */
    public List<HullModule> getModuleEntitiesByPlanet(Long planetId) {
        return modules.values().stream()
                .filter(m -> m.getPlanetId().equals(planetId))
                .collect(Collectors.toList());
    }

    /**
     * Get all modules owned by a player/corporation.
     */
    public List<HullModuleDto> getModulesByOwner(String ownerId) {
        return modules.values().stream()
                .filter(m -> m.getOwnerId().equals(ownerId))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all modules of a specific type.
     */
    public List<HullModuleDto> getModulesByType(HullModuleType moduleType) {
        return modules.values().stream()
                .filter(m -> m.getModuleType() == moduleType)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all modules of a specific category.
     */
    public List<HullModuleDto> getModulesByCategory(HullModuleType.Category category) {
        return modules.values().stream()
                .filter(m -> m.getModuleType().getCategory() == category)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a single module by ID.
     */
    public HullModuleDto getModule(Long moduleId) {
        HullModule module = modules.get(moduleId);
        if (module == null) {
            return null;
        }
        return toDto(module);
    }

    /**
     * Get all published blueprints.
     */
    public List<HullModuleBlueprintDto> getPublishedBlueprints() {
        return blueprints.values().stream()
                .filter(HullModuleBlueprint::isPublished)
                .map(this::toBlueprintDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all blueprints by a creator.
     */
    public List<HullModuleBlueprintDto> getBlueprintsByCreator(String creatorId) {
        return blueprints.values().stream()
                .filter(bp -> bp.getCreatorId().equals(creatorId))
                .map(this::toBlueprintDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all available module types.
     */
    public List<Map<String, Object>> getModuleTypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        for (HullModuleType type : HullModuleType.values()) {
            Map<String, Object> typeMap = new HashMap<>();
            typeMap.put("type", type.name());
            typeMap.put("displayName", type.getDisplayName());
            typeMap.put("category", type.getCategory().name());
            typeMap.put("constructionCost", type.getConstructionCost());
            typeMap.put("maintenanceCost", type.getMaintenanceCost());
            typeMap.put("floors", type.getFloors());
            typeMap.put("basementLevels", type.getBasementLevels());
            typeMap.put("requiredTerrain", type.getRequiredTerrain());
            typeMap.put("inputCargo", type.getInputCargo());
            typeMap.put("outputCargo", type.getOutputCargo());
            typeMap.put("apartmentUnits", type.getApartmentUnits());
            typeMap.put("officeUnits", type.getOfficeUnits());
            typeMap.put("cargoCapacity", type.getCargoCapacity());
            typeMap.put("passengerCapacity", type.getPassengerCapacity());
            types.add(typeMap);
        }
        return types;
    }

    /**
     * Calculate total maintenance cost for all modules on a planet.
     */
    public int calculateTotalMaintenance(Long planetId) {
        return modules.values().stream()
                .filter(m -> m.getPlanetId().equals(planetId) && m.isActive())
                .mapToInt(HullModule::getMaintenanceCost)
                .sum();
    }

    /**
     * Deactivate a module (demolish).
     */
    public HullModuleDto deactivateModule(Long moduleId) {
        HullModule module = modules.get(moduleId);
        if (module == null) {
            throw new IllegalArgumentException("Module not found: " + moduleId);
        }

        module.setActive(false);
        module.setUpdatedAt(LocalDateTime.now());

        log.info("Deactivated module: {}", module.getName());
        return toDto(module);
    }

    /**
     * Update production level for an industry module.
     */
    public HullModuleDto updateProductionLevel(Long moduleId, int productionLevel) {
        HullModule module = modules.get(moduleId);
        if (module == null) {
            throw new IllegalArgumentException("Module not found: " + moduleId);
        }

        module.setProductionLevel(Math.max(0, Math.min(100, productionLevel)));
        module.setUpdatedAt(LocalDateTime.now());

        return toDto(module);
    }

    /**
     * Update occupancy for residential/commercial modules.
     */
    public HullModuleDto updateOccupancy(Long moduleId, int occupiedCapacity) {
        HullModule module = modules.get(moduleId);
        if (module == null) {
            throw new IllegalArgumentException("Module not found: " + moduleId);
        }

        module.setOccupiedCapacity(Math.max(0, Math.min(module.getTotalCapacity(), occupiedCapacity)));
        module.setUpdatedAt(LocalDateTime.now());

        return toDto(module);
    }

    /**
     * Generate dungeon interior for a module.
     */
    private String generateDungeonForModule(HullModule module) {
        String dungeonId = "HM-" + module.getId();
        HullModuleType type = module.getModuleType();

        try {
            // Route to appropriate generator based on type
            switch (type.getCategory()) {
                case RESIDENTIAL:
                    if (type == HullModuleType.APARTMENT_BUILDING) {
                        interiorGenerator.generateApartmentBuilding(dungeonId,
                                TechLevel.TL8);
                    }
                    break;
                case COMMERCIAL:
                    if (type == HullModuleType.YOUNGER_HQ) {
                        interiorGenerator.generateYoungerIndustriesHQ(dungeonId,
                                TechLevel.TL8);
                    }
                    break;
                case INDUSTRY:
                    interiorGenerator.generateFactory(dungeonId,
                            TechLevel.TL8);
                    break;
                default:
                    // Infrastructure modules don't need dungeon interiors
                    break;
            }
        } catch (Exception e) {
            log.warn("Failed to generate dungeon for module {}: {}", module.getName(), e.getMessage());
        }

        return dungeonId;
    }

    /**
     * Convert entity to DTO.
     */
    private HullModuleDto toDto(HullModule module) {
        return new HullModuleDto(
                module.getId(),
                module.getPlanetId(),
                module.getName(),
                module.getModuleType(),
                module.getHexX(), module.getHexY(), module.getHexZ(),
                module.getOwnerId(),
                module.getBlueprint() != null ? module.getBlueprint().getId() : null,
                module.getFloors(),
                module.getBasementLevels(),
                module.getConstructionCost(),
                module.getMaintenanceCost(),
                module.getTotalCapacity(),
                module.getOccupiedCapacity(),
                module.getProductionLevel(),
                module.getInputCargo(),
                module.getOutputCargo(),
                module.getDescription(),
                module.isActive(),
                module.getConstructionProgress(),
                module.getDungeonId(),
                module.getCreatedAt()
        );
    }

    /**
     * Convert blueprint entity to DTO.
     */
    private HullModuleBlueprintDto toBlueprintDto(HullModuleBlueprint blueprint) {
        return new HullModuleBlueprintDto(
                blueprint.getId(),
                blueprint.getBlueprintCode(),
                blueprint.getName(),
                blueprint.getDescription(),
                blueprint.getCreatorId(),
                blueprint.getCategory(),
                blueprint.getConstructionCost(),
                blueprint.getMaintenanceCost(),
                blueprint.getFloors(),
                blueprint.getBasementLevels(),
                blueprint.getTotalCapacity(),
                blueprint.getInputCargo(),
                blueprint.getOutputCargo(),
                blueprint.getTechLevelRequired(),
                blueprint.isPublished(),
                blueprint.getLicenseFee(),
                blueprint.getUsageCount(),
                blueprint.getRoomTemplates(),
                blueprint.isActive(),
                blueprint.getCreatedAt()
        );
    }
}
