package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Economy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Earth2026EconomyRepository extends JpaRepository<Earth2026Economy, Long> {
    Optional<Earth2026Economy> findByPlanetId(Long planetId);
}
