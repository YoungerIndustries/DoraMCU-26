package com.yisy.modules.doramcu.hex.dungeon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link GroundVehicleModuleTemplate} entities.
 * <p>
 * Provides queries for browsing, filtering, and searching ground vehicle module templates
 * by type, tags, terrain compatibility, armor rating, and difficulty.
 * </p>
 */
@Repository
public interface GroundVehicleModuleTemplateRepository extends JpaRepository<GroundVehicleModuleTemplate, Long> {

    /**
     * Finds all templates of a given ground vehicle module type.
     *
     * @param type the ground vehicle module type
     * @return list of matching templates
     */
    List<GroundVehicleModuleTemplate> findByType(GroundVehicleModuleTemplate.GroundVehicleModuleType type);

    /**
     * Finds templates whose name contains the given string (case-insensitive).
     *
     * @param name the name substring to search for
     * @return list of matching templates
     */
    List<GroundVehicleModuleTemplate> findByNameContainingIgnoreCase(String name);

    /**
     * Finds templates whose tags contain the given tag (case-insensitive).
     *
     * @param tag the tag to search for
     * @return list of matching templates
     */
    @Query("SELECT g FROM GroundVehicleModuleTemplate g WHERE LOWER(g.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<GroundVehicleModuleTemplate> findByTagContaining(@Param("tag") String tag);

    /**
     * Finds templates with armor rating less than or equal to the given value.
     *
     * @param maxArmor the maximum armor rating
     * @return list of matching templates
     */
    List<GroundVehicleModuleTemplate> findByArmorRatingLessThanEqual(int maxArmor);

    /**
     * Finds templates with difficulty less than or equal to the given value.
     *
     * @param maxDifficulty the maximum difficulty
     * @return list of matching templates
     */
    List<GroundVehicleModuleTemplate> findByDifficultyLessThanEqual(int maxDifficulty);

    /**
     * Finds templates compatible with the given terrain type.
     *
     * @param terrain the terrain type name
     * @return list of compatible templates (including those with no restriction)
     */
    @Query("SELECT g FROM GroundVehicleModuleTemplate g WHERE g.compatibleTerrain IS NULL OR g.compatibleTerrain = '' OR g.compatibleTerrain LIKE %:terrain%")
    List<GroundVehicleModuleTemplate> findByCompatibleTerrainContaining(@Param("terrain") String terrain);

    /**
     * Finds templates matching both type and a tag.
     *
     * @param type the ground vehicle module type
     * @param tag the tag to search for
     * @return list of matching templates
     */
    @Query("SELECT g FROM GroundVehicleModuleTemplate g WHERE g.type = :type AND LOWER(g.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<GroundVehicleModuleTemplate> findByTypeAndTagContaining(
            @Param("type") GroundVehicleModuleTemplate.GroundVehicleModuleType type,
            @Param("tag") String tag);

    /**
     * Finds templates ordered by firepower descending.
     *
     * @return all templates sorted by firepower
     */
    @Query("SELECT g FROM GroundVehicleModuleTemplate g ORDER BY g.firepower DESC")
    List<GroundVehicleModuleTemplate> findAllOrderByFirepowerDesc();

    /**
     * Finds templates ordered by difficulty ascending.
     *
     * @return all templates sorted by difficulty
     */
    @Query("SELECT g FROM GroundVehicleModuleTemplate g ORDER BY g.difficulty ASC")
    List<GroundVehicleModuleTemplate> findAllOrderByDifficultyAsc();

    /**
     * Finds templates matching the given infrastructure requirement.
     *
     * @param infrastructure the infrastructure requirement (NONE, ROAD, RAIL, WATER, AIRPORT)
     * @return list of matching templates
     */
    List<GroundVehicleModuleTemplate> findByInfrastructureRequirement(String infrastructure);

    /**
     * Finds templates with passenger capacity greater than or equal to the given value.
     *
     * @param minPassengers the minimum passenger capacity
     * @return list of matching templates
     */
    List<GroundVehicleModuleTemplate> findByPassengerCapacityGreaterThanEqual(int minPassengers);

    /**
     * Finds templates with cargo capacity greater than or equal to the given value.
     *
     * @param minCargo the minimum cargo capacity
     * @return list of matching templates
     */
    List<GroundVehicleModuleTemplate> findByCargoCapacityGreaterThanEqual(int minCargo);

    /**
     * Finds civilian transport vehicle templates (non-combat).
     *
     * @return list of transport vehicle templates
     */
    @Query("SELECT g FROM GroundVehicleModuleTemplate g WHERE g.type IN " +
            "(:transportTypes)")
    List<GroundVehicleModuleTemplate> findTransportVehicles(
            @Param("transportTypes") List<GroundVehicleModuleTemplate.GroundVehicleModuleType> transportTypes);
}
