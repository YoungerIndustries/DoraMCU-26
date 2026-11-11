package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Earth2026JobRepository extends JpaRepository<Earth2026Job, Long> {
    List<Earth2026Job> findByPlanetId(Long planetId);
    List<Earth2026Job> findByPlanetIdAndActiveTrue(Long planetId);
    Page<Earth2026Job> findByPlanetIdAndActiveTrue(Long planetId, Pageable pageable);
    long countByPlanetIdAndActiveTrue(Long planetId);
    List<Earth2026Job> findByCategory(String category);
    List<Earth2026Job> findByPlanetIdAndCategory(Long planetId, String category);
}
