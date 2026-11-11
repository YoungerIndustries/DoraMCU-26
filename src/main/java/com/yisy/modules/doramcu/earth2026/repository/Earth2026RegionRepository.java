package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Earth2026RegionRepository extends JpaRepository<Earth2026Region, Long> {
    List<Earth2026Region> findByCountryId(Long countryId);
    List<Earth2026Region> findByCountryIdAndActiveTrue(Long countryId);
    Page<Earth2026Region> findByCountryIdAndActiveTrue(Long countryId, Pageable pageable);
    long countByCountryIdAndActiveTrue(Long countryId);
}
