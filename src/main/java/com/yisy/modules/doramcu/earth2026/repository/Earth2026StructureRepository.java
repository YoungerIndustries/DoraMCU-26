package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Earth2026StructureRepository extends JpaRepository<Earth2026Structure, Long> {

    /**
     * Find all structures at a specific location.
     */
    List<Earth2026Structure> findByLocationId(Long locationId);

    /**
     * Find all active structures at a specific location.
     */
    List<Earth2026Structure> findByLocationIdAndActiveTrue(Long locationId);

    /**
     * Find all structures of a specific type.
     */
    List<Earth2026Structure> findByStructureType(String structureType);

    /**
     * Find all active structures of a specific type.
     */
    List<Earth2026Structure> findByStructureTypeAndActiveTrue(String structureType);

    /**
     * Find all structures at a location with tech level >= the given value.
     */
    List<Earth2026Structure> findByLocationIdAndTechLevelGreaterThanEqualAndActiveTrue(
            Long locationId, int techLevel);

    /**
     * Find all strategic structures at a location.
     */
    List<Earth2026Structure> findByLocationIdAndIsStrategicTrueAndActiveTrue(Long locationId);

    /**
     * Count structures at a location.
     */
    long countByLocationId(Long locationId);
}
