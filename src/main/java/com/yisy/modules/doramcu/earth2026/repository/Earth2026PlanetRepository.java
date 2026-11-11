package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Planet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Earth2026PlanetRepository extends JpaRepository<Earth2026Planet, Long> {
    Optional<Earth2026Planet> findByName(String name);
    Optional<Earth2026Planet> findByNameAndActiveTrue(String name);
}
