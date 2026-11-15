package com.yisy.modules.doramcu.earth2026.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Family;
import com.yisy.modules.doramcu.earth2026.model.Earth2026FamilyMember;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026FamilyMemberRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026FamilyRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * Service for Earth 2026 Families.
 * Provides CRUD operations for families and family members,
 * as well as bond management and member operations.
 */
@Service
public class Earth2026FamilyService {

    private final Earth2026FamilyRepository familyRepository;
    private final Earth2026FamilyMemberRepository memberRepository;

    @Autowired
    public Earth2026FamilyService(Earth2026FamilyRepository familyRepository,
                                   Earth2026FamilyMemberRepository memberRepository) {
        this.familyRepository = familyRepository;
        this.memberRepository = memberRepository;
    }

    // --- Family CRUD Operations ---

    /**
     * Find all families.
     */
    public List<Earth2026Family> findAll() {
        return familyRepository.findAll();
    }

    /**
     * Find all active families.
     */
    public List<Earth2026Family> findActiveFamilies() {
        return familyRepository.findByActiveTrue();
    }

    /**
     * Find family by ID.
     */
    public Optional<Earth2026Family> findById(Long id) {
        return familyRepository.findById(id);
    }

    /**
     * Find families by name (case-insensitive partial match).
     */
    public List<Earth2026Family> findByName(String name) {
        return familyRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Find families by home structure ID.
     */
    public List<Earth2026Family> findByHomeStructureId(Long homeStructureId) {
        return familyRepository.findByHomeStructureId(homeStructureId);
    }

    /**
     * Save a family.
     */
    public Earth2026Family saveFamily(Earth2026Family family) {
        return familyRepository.save(family);
    }

    /**
     * Delete a family by ID.
     */
    public void deleteFamily(Long id) {
        familyRepository.deleteById(id);
    }

    // --- Family Member Operations ---

    /**
     * Find all members of a family.
     */
    public List<Earth2026FamilyMember> findMembersByFamilyId(Long familyId) {
        return memberRepository.findByFamilyId(familyId);
    }

    /**
     * Find active members of a family.
     */
    public List<Earth2026FamilyMember> findActiveMembersByFamilyId(Long familyId) {
        return memberRepository.findByFamilyIdAndActiveTrue(familyId);
    }

    /**
     * Find family memberships for a character.
     */
    public List<Earth2026FamilyMember> findMembersByCharacterId(Long characterId) {
        return memberRepository.findByCharacterId(characterId);
    }

    /**
     * Find active family memberships for a character.
     */
    public List<Earth2026FamilyMember> findActiveMembersByCharacterId(Long characterId) {
        return memberRepository.findByCharacterIdAndActiveTrue(characterId);
    }

    /**
     * Find family memberships for an NPC.
     */
    public List<Earth2026FamilyMember> findMembersByNpcId(Long npcId) {
        return memberRepository.findByNpcId(npcId);
    }

    /**
     * Add a member to a family.
     * @param familyId the family ID
     * @param characterId the character ID (optional, null for NPC-only)
     * @param npcId the NPC ID (optional, null for character-only)
     * @param relationshipType the relationship type
     * @return the created family member
     */
    public Optional<Earth2026FamilyMember> addMember(Long familyId, Long characterId, Long npcId,
                                                      Earth2026FamilyMember.RelationshipType relationshipType) {
        return familyRepository.findById(familyId).map(family -> {
            Earth2026FamilyMember member = new Earth2026FamilyMember();
            member.setFamily(family);
            member.setCharacterId(characterId);
            member.setNpcId(npcId);
            member.setRelationshipType(relationshipType);
            member.setBondScore(50);
            member.setActive(true);
            return memberRepository.save(member);
        });
    }

    /**
     * Remove a member from a family.
     * @param memberId the member ID
     * @return true if the member was removed
     */
    public boolean removeMember(Long memberId) {
        Optional<Earth2026FamilyMember> optional = memberRepository.findById(memberId);
        if (optional.isPresent()) {
            Earth2026FamilyMember member = optional.get();
            member.setActive(false);
            memberRepository.save(member);
            return true;
        }
        return false;
    }

    // --- Bond Management ---

    /**
     * Increase the bond score for a family member.
     * @param memberId the member ID
     * @param amount the amount to increase (clamped to 0-100)
     * @return the updated member
     */
    public Optional<Earth2026FamilyMember> increaseBond(Long memberId, int amount) {
        return memberRepository.findById(memberId).map(member -> {
            member.increaseBond(amount);
            return memberRepository.save(member);
        });
    }

    /**
     * Decrease the bond score for a family member.
     * @param memberId the member ID
     * @param amount the amount to decrease (clamped to 0-100)
     * @return the updated member
     */
    public Optional<Earth2026FamilyMember> decreaseBond(Long memberId, int amount) {
        return memberRepository.findById(memberId).map(member -> {
            member.decreaseBond(amount);
            return memberRepository.save(member);
        });
    }

    // --- Family Events ---

    /**
     * Simulate a family gathering event, increasing all members' bond scores.
     * @param familyId the family ID
     * @param bondIncrease the amount to increase each member's bond
     * @return the updated family
     */
    public Optional<Earth2026Family> familyGathering(Long familyId, int bondIncrease) {
        return familyRepository.findById(familyId).map(family -> {
            List<Earth2026FamilyMember> members = memberRepository.findByFamilyIdAndActiveTrue(familyId);
            for (Earth2026FamilyMember member : members) {
                member.increaseBond(bondIncrease);
                memberRepository.save(member);
            }
            // Family gathering also boosts reputation slightly
            family.setFamilyReputation(Math.min(100, family.getFamilyReputation() + 2));
            return familyRepository.save(family);
        });
    }
}
