package com.yisy.modules.doramcu.hex.dungeon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link DungeonModuleTemplate} entities.
 * <p>
 * Provides queries for browsing, filtering, and searching dungeon module templates
 * by type, tags, and difficulty.
 * </p>
 */
@Repository
public interface DungeonModuleTemplateRepository extends JpaRepository<DungeonModuleTemplate, Long> {

    /**
     * Finds all templates of a given dungeon room type.
     *
     * @param type the dungeon room type
     * @return list of matching templates
     */
    List<DungeonModuleTemplate> findByType(DungeonRoom type);

    /**
     * Finds templates whose name contains the given string (case-insensitive).
     *
     * @param name the name substring to search for
     * @return list of matching templates
     */
    List<DungeonModuleTemplate> findByNameContainingIgnoreCase(String name);

    /**
     * Finds templates whose tags contain the given tag (case-insensitive).
     *
     * @param tag the tag to search for
     * @return list of matching templates
     */
    @Query("SELECT d FROM DungeonModuleTemplate d WHERE LOWER(d.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<DungeonModuleTemplate> findByTagContaining(@Param("tag") String tag);

    /**
     * Finds templates with difficulty less than or equal to the given value.
     *
     * @param maxDifficulty the maximum difficulty
     * @return list of matching templates
     */
    List<DungeonModuleTemplate> findByDifficultyLessThanEqual(int maxDifficulty);

    /**
     * Finds templates matching both type and a tag.
     *
     * @param type the dungeon room type
     * @param tag the tag to search for
     * @return list of matching templates
     */
    @Query("SELECT d FROM DungeonModuleTemplate d WHERE d.type = :type AND LOWER(d.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<DungeonModuleTemplate> findByTypeAndTagContaining(@Param("type") DungeonRoom type, @Param("tag") String tag);

    /**
     * Finds templates ordered by difficulty ascending.
     *
     * @return all templates sorted by difficulty
     */
    @Query("SELECT d FROM DungeonModuleTemplate d ORDER BY d.difficulty ASC")
    List<DungeonModuleTemplate> findAllOrderByDifficultyAsc();
}
