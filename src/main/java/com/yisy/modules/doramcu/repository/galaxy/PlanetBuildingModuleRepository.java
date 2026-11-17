package com.yisy.modules.doramcu.repository.galaxy;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.hex.dungeon.Dungeon;
import com.yisy.modules.doramcu.model.misc.Planet;


import com.yisy.modules.doramcu.model.galaxy.PlanetBuildingModuleEntity;
import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing planet building module persistence.
 */
@Repository
public interface PlanetBuildingModuleRepository extends JpaRepository<PlanetBuildingModuleEntity, Long> {

    /** Find all modules for a given tech level. */
    List<PlanetBuildingModuleEntity> findByTechLevel(TechLevel techLevel);

    /** Find all modules for a given tech level and category. */
    List<PlanetBuildingModuleEntity> findByTechLevelAndCategory(TechLevel techLevel, ModuleCategory category);

    /** Find all modules for a given category. */
    List<PlanetBuildingModuleEntity> findByCategory(ModuleCategory category);

    /** Find modules by tech level range. */
    List<PlanetBuildingModuleEntity> findByTechLevelBetween(TechLevel startLevel, TechLevel endLevel);

    /** Check if any modules exist for a tech level. */
    boolean existsByTechLevel(TechLevel techLevel);

    /** Count modules for a tech level. */
    long countByTechLevel(TechLevel techLevel);

    /** Count modules for a tech level and category. */
    long countByTechLevelAndCategory(TechLevel techLevel, ModuleCategory category);

    // === Dungeon-specific queries ===

    /** Find monsters by creature type. */
    List<PlanetBuildingModuleEntity> findByCategoryAndCreatureType(ModuleCategory category, String creatureType);

    /** Find magic items by rarity. */
    List<PlanetBuildingModuleEntity> findByCategoryAndRarity(ModuleCategory category, String rarity);

    /** Find traps by damage type. */
    List<PlanetBuildingModuleEntity> findByCategoryAndDamageType(ModuleCategory category, String damageType);

    /** Find spells by school. */
    List<PlanetBuildingModuleEntity> findByCategoryAndSpellSchool(ModuleCategory category, String spellSchool);

    /** Find spells by level. */
    List<PlanetBuildingModuleEntity> findByCategoryAndSpellLevel(ModuleCategory category, Integer spellLevel);

    /** Find monsters by CR range. */
    List<PlanetBuildingModuleEntity> findByCategoryAndCrBetween(ModuleCategory category, Double minCr, Double maxCr);

    /** Find modules by game type. */
    List<PlanetBuildingModuleEntity> findByGameType(String gameType);

    /** Find dungeon objects (all non-planet-building categories) for a tech level. */
    List<PlanetBuildingModuleEntity> findByTechLevelAndCategoryIn(TechLevel techLevel, List<ModuleCategory> categories);

    // === Animal-specific queries ===

    /** Find animals with milk yield greater than a threshold. */
    List<PlanetBuildingModuleEntity> findByCategoryAndMilkYieldGreaterThan(ModuleCategory category, Double milkYield);

    /** Find animals with wool yield greater than a threshold. */
    List<PlanetBuildingModuleEntity> findByCategoryAndWoolYieldGreaterThan(ModuleCategory category, Double woolYield);

    /** Find animals with carrying capacity greater than a threshold. */
    List<PlanetBuildingModuleEntity> findByCategoryAndCarryingCapacityGreaterThan(ModuleCategory category, Integer carryingCapacity);

    /** Find animals with egg rate greater than a threshold. */
    List<PlanetBuildingModuleEntity> findByCategoryAndEggRateGreaterThan(ModuleCategory category, Integer eggRate);

    /** Find animals with meat yield greater than a threshold. */
    List<PlanetBuildingModuleEntity> findByCategoryAndMeatYieldGreaterThan(ModuleCategory category, Double meatYield);
}
