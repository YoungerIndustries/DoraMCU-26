package com.yisy.modules.doramcu.hex.dungeon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link FacilityModuleTemplate} entities.
 */
@Repository
public interface FacilityModuleTemplateRepository extends JpaRepository<FacilityModuleTemplate, Long> {

    List<FacilityModuleTemplate> findByType(FacilityModuleTemplate.FacilityModuleType type);

    List<FacilityModuleTemplate> findByNameContainingIgnoreCase(String name);

    @Query("SELECT f FROM FacilityModuleTemplate f WHERE LOWER(f.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<FacilityModuleTemplate> findByTagContaining(@Param("tag") String tag);

    List<FacilityModuleTemplate> findByDifficultyLessThanEqual(int maxDifficulty);

    @Query("SELECT f FROM FacilityModuleTemplate f WHERE f.type = :type AND LOWER(f.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<FacilityModuleTemplate> findByTypeAndTagContaining(
            @Param("type") FacilityModuleTemplate.FacilityModuleType type,
            @Param("tag") String tag);

    @Query("SELECT f FROM FacilityModuleTemplate f ORDER BY f.difficulty ASC")
    List<FacilityModuleTemplate> findAllOrderByDifficultyAsc();
}
