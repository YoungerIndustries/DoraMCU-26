package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for Earth 2026 Family entities.
 */
@Repository
public interface Earth2026FamilyRepository extends JpaRepository<Earth2026Family, Long> {

    /**
     * Find families by name.
     */
    List<Earth2026Family> findByNameContainingIgnoreCase(String name);

    /**
     * Find active families.
     */
    List<Earth2026Family> findByActiveTrue();

    /**
     * Find families by home structure ID.
     */
    List<Earth2026Family> findByHomeStructureId(Long homeStructureId);
}
