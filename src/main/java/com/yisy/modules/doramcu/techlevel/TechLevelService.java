package com.yisy.modules.doramcu.techlevel;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.model.PlanetBuildingModuleEntity;
import com.yisy.modules.doramcu.repository.PlanetBuildingModuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Service for querying planet building modules by tech level and category.
 * <p>
 * Acts as the bridge between the planet builder and the module database,
 * filtering available modules based on a planet's assigned tech level.
 * </p>
 */
@Service
public class TechLevelService {

    private static final Logger log = LoggerFactory.getLogger(TechLevelService.class);

    private final PlanetBuildingModuleRepository moduleRepository;

    public TechLevelService(PlanetBuildingModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    /**
     * Get all building modules available for a given tech level.
     *
     * @param techLevel the technology level
     * @return list of modules available at this tech level
     */
    public List<PlanetBuildingModuleEntity> getModulesByTechLevel(TechLevel techLevel) {
        if (techLevel == null) {
            return Collections.emptyList();
        }
        return moduleRepository.findByTechLevel(techLevel);
    }

    /**
     * Get building modules for a specific tech level and category.
     *
     * @param techLevel the technology level
     * @param category  the module category
     * @return list of modules matching both criteria
     */
    public List<PlanetBuildingModuleEntity> getModulesByTechLevelAndCategory(TechLevel techLevel, ModuleCategory category) {
        if (techLevel == null || category == null) {
            return Collections.emptyList();
        }
        return moduleRepository.findByTechLevelAndCategory(techLevel, category);
    }

    /**
     * Get all modules in a given category across all tech levels.
     *
     * @param category the module category
     * @return list of modules in this category
     */
    public List<PlanetBuildingModuleEntity> getModulesByCategory(ModuleCategory category) {
        if (category == null) {
            return Collections.emptyList();
        }
        return moduleRepository.findByCategory(category);
    }

    /**
     * Get modules within a tech level range (inclusive).
     *
     * @param startLevel the minimum tech level
     * @param endLevel   the maximum tech level
     * @return list of modules within the range
     */
    public List<PlanetBuildingModuleEntity> getModulesByTechLevelRange(TechLevel startLevel, TechLevel endLevel) {
        if (startLevel == null || endLevel == null) {
            return Collections.emptyList();
        }
        return moduleRepository.findByTechLevelBetween(startLevel, endLevel);
    }

    /**
     * Check if any modules exist for a given tech level.
     *
     * @param techLevel the technology level
     * @return true if modules exist for this tech level
     */
    public boolean hasModulesForTechLevel(TechLevel techLevel) {
        if (techLevel == null) {
            return false;
        }
        return moduleRepository.existsByTechLevel(techLevel);
    }

    /**
     * Count the number of modules for a given tech level.
     *
     * @param techLevel the technology level
     * @return number of modules
     */
    public long countModulesByTechLevel(TechLevel techLevel) {
        if (techLevel == null) {
            return 0;
        }
        return moduleRepository.countByTechLevel(techLevel);
    }

    /**
     * Count the number of modules for a given tech level and category.
     *
     * @param techLevel the technology level
     * @param category  the module category
     * @return number of modules
     */
    public long countModulesByTechLevelAndCategory(TechLevel techLevel, ModuleCategory category) {
        if (techLevel == null || category == null) {
            return 0;
        }
        return moduleRepository.countByTechLevelAndCategory(techLevel, category);
    }
}
