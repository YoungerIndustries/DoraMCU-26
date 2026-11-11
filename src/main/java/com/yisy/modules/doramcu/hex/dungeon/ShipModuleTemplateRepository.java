package com.yisy.modules.doramcu.hex.dungeon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link ShipModuleTemplate} entities.
 * <p>
 * Provides queries for browsing, filtering, and searching ship module templates
 * by type, tags, compatibility, and difficulty.
 * </p>
 */
@Repository
public interface ShipModuleTemplateRepository extends JpaRepository<ShipModuleTemplate, Long> {

    /**
     * Finds all templates of a given ship module type.
     *
     * @param type the ship module type
     * @return list of matching templates
     */
    List<ShipModuleTemplate> findByType(ShipModuleTemplate.ShipModuleType type);

    /**
     * Finds templates whose name contains the given string (case-insensitive).
     *
     * @param name the name substring to search for
     * @return list of matching templates
     */
    List<ShipModuleTemplate> findByNameContainingIgnoreCase(String name);

    /**
     * Finds templates whose tags contain the given tag (case-insensitive).
     *
     * @param tag the tag to search for
     * @return list of matching templates
     */
    @Query("SELECT s FROM ShipModuleTemplate s WHERE LOWER(s.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<ShipModuleTemplate> findByTagContaining(@Param("tag") String tag);

    /**
     * Finds templates with difficulty less than or equal to the given value.
     *
     * @param maxDifficulty the maximum difficulty
     * @return list of matching templates
     */
    List<ShipModuleTemplate> findByDifficultyLessThanEqual(int maxDifficulty);

    /**
     * Finds templates matching both type and a tag.
     *
     * @param type the ship module type
     * @param tag the tag to search for
     * @return list of matching templates
     */
    @Query("SELECT s FROM ShipModuleTemplate s WHERE s.type = :type AND LOWER(s.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<ShipModuleTemplate> findByTypeAndTagContaining(
            @Param("type") ShipModuleTemplate.ShipModuleType type,
            @Param("tag") String tag);

    /**
     * Finds templates ordered by difficulty ascending.
     *
     * @return all templates sorted by difficulty
     */
    @Query("SELECT s FROM ShipModuleTemplate s ORDER BY s.difficulty ASC")
    List<ShipModuleTemplate> findAllOrderByDifficultyAsc();

    /**
     * Finds templates compatible with the given ship type string.
     *
     * @param shipType the ship type name
     * @return list of compatible templates (including those with no restriction)
     */
    @Query("SELECT s FROM ShipModuleTemplate s WHERE s.compatibleShipTypes IS NULL OR s.compatibleShipTypes = '' OR s.compatibleShipTypes LIKE %:shipType%")
    List<ShipModuleTemplate> findByCompatibleShipTypes(@Param("shipType") String shipType);
}
