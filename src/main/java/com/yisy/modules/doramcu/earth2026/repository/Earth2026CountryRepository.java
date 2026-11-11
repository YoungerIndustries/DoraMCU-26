package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Country;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Earth2026CountryRepository extends JpaRepository<Earth2026Country, Long> {
    List<Earth2026Country> findByPlanetId(Long planetId);
    List<Earth2026Country> findByPlanetIdAndActiveTrue(Long planetId);
    Page<Earth2026Country> findByPlanetIdAndActiveTrue(Long planetId, Pageable pageable);
    long countByPlanetIdAndActiveTrue(Long planetId);
    Earth2026Country findByIsoCode(String isoCode);
    List<Earth2026Country> findByContinent(String continent);
}
