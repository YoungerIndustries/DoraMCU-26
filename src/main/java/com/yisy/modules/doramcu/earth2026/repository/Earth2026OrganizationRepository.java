package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Connection;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for Earth 2026 Organization entities.
 */
@Repository
public interface Earth2026OrganizationRepository extends JpaRepository<Earth2026Organization, Long> {

    /**
     * Find all active organizations.
     */
    List<Earth2026Organization> findByActiveTrue();

    /**
     * Find organizations by type.
     */
    List<Earth2026Organization> findByOrgType(Earth2026Organization.OrganizationType orgType);

    /**
     * Find active organizations by type.
     */
    List<Earth2026Organization> findByOrgTypeAndActiveTrue(Earth2026Organization.OrganizationType orgType);

    /**
     * Find organizations by name (case-insensitive partial match).
     */
    List<Earth2026Organization> findByNameContainingIgnoreCase(String name);

    /**
     * Find organizations by home structure ID.
     */
    List<Earth2026Organization> findByHomeStructureId(Long homeStructureId);

    /**
     * Find organizations by leader.
     */
    List<Earth2026Organization> findByLeaderTypeAndLeaderId(
            Earth2026Connection.EntityType leaderType, Long leaderId);
}
