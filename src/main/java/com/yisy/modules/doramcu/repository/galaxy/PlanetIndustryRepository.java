package com.yisy.modules.doramcu.repository.galaxy;

import com.yisy.modules.doramcu.model.galaxy.PlanetIndustryEntity;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing planet industry persistence.
 */
@Repository
public interface PlanetIndustryRepository extends JpaRepository<PlanetIndustryEntity, Long> {

    /** Find industry by its business ID. */
    Optional<PlanetIndustryEntity> findByIndustryId(String industryId);

    /** Find all industries on a planet. */
    List<PlanetIndustryEntity> findByPlanetId(int planetId);

    /** Find industries on a planet owned by an empire. */
    List<PlanetIndustryEntity> findByPlanetIdAndOwnerEmpireId(int planetId, String ownerEmpireId);

    /** Find industries of a specific type on a planet. */
    List<PlanetIndustryEntity> findByPlanetIdAndIndustryType(int planetId, String industryType);

    /** Delete an industry by its business ID. */
    void deleteByIndustryId(String industryId);

    /** Delete all industries for a planet. */
    void deleteByPlanetId(int planetId);

    /** Count industries on a planet. */
    long countByPlanetId(int planetId);
}
