package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Connection;
import com.yisy.modules.doramcu.earth2026.model.Earth2026OrgMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for Earth 2026 Organization Member entities.
 */
@Repository
public interface Earth2026OrgMemberRepository extends JpaRepository<Earth2026OrgMember, Long> {

    /**
     * Find members by organization ID.
     */
    List<Earth2026OrgMember> findByOrganizationId(Long organizationId);

    /**
     * Find active members by organization ID.
     */
    List<Earth2026OrgMember> findByOrganizationIdAndActiveTrue(Long organizationId);

    /**
     * Find members by member entity.
     */
    List<Earth2026OrgMember> findByMemberTypeAndMemberId(
            Earth2026Connection.EntityType memberType, Long memberId);

    /**
     * Find active members by member entity.
     */
    List<Earth2026OrgMember> findByMemberTypeAndMemberIdAndActiveTrue(
            Earth2026Connection.EntityType memberType, Long memberId);

    /**
     * Find members by role.
     */
    List<Earth2026OrgMember> findByRoleContainingIgnoreCase(String role);

    /**
     * Find active members by organization and role.
     */
    List<Earth2026OrgMember> findByOrganizationIdAndRoleContainingIgnoreCaseAndActiveTrue(
            Long organizationId, String role);
}
