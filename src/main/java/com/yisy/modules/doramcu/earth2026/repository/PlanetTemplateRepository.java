package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.PlanetTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for querying planet templates.
 */
@Repository
public interface PlanetTemplateRepository extends JpaRepository<PlanetTemplate, Long> {

    /**
     * Find template by name.
     */
    Optional<PlanetTemplate> findByName(String name);

    /**
     * Find templates by type.
     */
    List<PlanetTemplate> findByTemplateType(String templateType);

    /**
     * Find active templates.
     */
    List<PlanetTemplate> findByActiveTrue();

    /**
     * Find active templates by type.
     */
    List<PlanetTemplate> findByTemplateTypeAndActiveTrue(String templateType);
}
