package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for Earth 2026 Family Member entities.
 */
@Repository
public interface Earth2026FamilyMemberRepository extends JpaRepository<Earth2026FamilyMember, Long> {

    /**
     * Find members by family ID.
     */
    List<Earth2026FamilyMember> findByFamilyId(Long familyId);

    /**
     * Find active members by family ID.
     */
    List<Earth2026FamilyMember> findByFamilyIdAndActiveTrue(Long familyId);

    /**
     * Find members by character ID.
     */
    List<Earth2026FamilyMember> findByCharacterId(Long characterId);

    /**
     * Find members by NPC ID.
     */
    List<Earth2026FamilyMember> findByNpcId(Long npcId);

    /**
     * Find members by relationship type.
     */
    List<Earth2026FamilyMember> findByRelationshipType(Earth2026FamilyMember.RelationshipType relationshipType);

    /**
     * Find active members by character ID.
     */
    List<Earth2026FamilyMember> findByCharacterIdAndActiveTrue(Long characterId);
}
