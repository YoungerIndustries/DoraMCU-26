package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.YoungerOffice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying Younger Industries branch offices.
 */
@Repository
public interface YoungerOfficeRepository extends JpaRepository<YoungerOffice, Long> {

    /**
     * Find all offices on a specific planet.
     */
    List<YoungerOffice> findByPlanetId(Long planetId);

    /**
     * Find office at a specific location.
     */
    YoungerOffice findByLocationId(Long locationId);

    /**
     * Find offices in a specific city.
     */
    List<YoungerOffice> findByCity(String city);

    /**
     * Find active offices on a specific planet.
     */
    List<YoungerOffice> findByPlanetIdAndActiveTrue(Long planetId);

    /**
     * Count offices on a specific planet.
     */
    long countByPlanetId(Long planetId);
}
