package com.yisy.modules.doramcu.hex.dungeon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link StationModuleTemplate} entities.
 */
@Repository
public interface StationModuleTemplateRepository extends JpaRepository<StationModuleTemplate, Long> {

    List<StationModuleTemplate> findByType(StationModuleTemplate.StationModuleType type);

    List<StationModuleTemplate> findByNameContainingIgnoreCase(String name);

    @Query("SELECT s FROM StationModuleTemplate s WHERE LOWER(s.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<StationModuleTemplate> findByTagContaining(@Param("tag") String tag);

    List<StationModuleTemplate> findByDifficultyLessThanEqual(int maxDifficulty);

    @Query("SELECT s FROM StationModuleTemplate s WHERE s.type = :type AND LOWER(s.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<StationModuleTemplate> findByTypeAndTagContaining(
            @Param("type") StationModuleTemplate.StationModuleType type,
            @Param("tag") String tag);

    @Query("SELECT s FROM StationModuleTemplate s ORDER BY s.difficulty ASC")
    List<StationModuleTemplate> findAllOrderByDifficultyAsc();
}
