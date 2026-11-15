package com.yisy.modules.doramcu.earth2026.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Connection;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Organization;
import com.yisy.modules.doramcu.earth2026.model.Earth2026OrgMember;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026OrgMemberRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * Service for Earth 2026 Organizations.
 * Provides CRUD operations for organizations and members,
 * as well as loyalty management and hierarchy operations.
 */
@Service
public class Earth2026OrganizationService {

    private final Earth2026OrganizationRepository organizationRepository;
    private final Earth2026OrgMemberRepository memberRepository;

    @Autowired
    public Earth2026OrganizationService(Earth2026OrganizationRepository organizationRepository,
                                         Earth2026OrgMemberRepository memberRepository) {
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
    }

    // --- Organization CRUD Operations ---

    /**
     * Find all organizations.
     */
    public List<Earth2026Organization> findAll() {
        return organizationRepository.findAll();
    }

    /**
     * Find all active organizations.
     */
    public List<Earth2026Organization> findActiveOrganizations() {
        return organizationRepository.findByActiveTrue();
    }

    /**
     * Find organization by ID.
     */
    public Optional<Earth2026Organization> findById(Long id) {
        return organizationRepository.findById(id);
    }

    /**
     * Find organizations by name (case-insensitive partial match).
     */
    public List<Earth2026Organization> findByName(String name) {
        return organizationRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Find organizations by type.
     */
    public List<Earth2026Organization> findByType(Earth2026Organization.OrganizationType orgType) {
        return organizationRepository.findByOrgType(orgType);
    }

    /**
     * Find active organizations by type.
     */
    public List<Earth2026Organization> findActiveByType(Earth2026Organization.OrganizationType orgType) {
        return organizationRepository.findByOrgTypeAndActiveTrue(orgType);
    }

    /**
     * Find organizations by home structure ID.
     */
    public List<Earth2026Organization> findByHomeStructureId(Long homeStructureId) {
        return organizationRepository.findByHomeStructureId(homeStructureId);
    }

    /**
     * Find organizations led by a specific entity.
     */
    public List<Earth2026Organization> findByLeader(
            Earth2026Connection.EntityType leaderType, Long leaderId) {
        return organizationRepository.findByLeaderTypeAndLeaderId(leaderType, leaderId);
    }

    /**
     * Save an organization.
     */
    public Earth2026Organization saveOrganization(Earth2026Organization organization) {
        return organizationRepository.save(organization);
    }

    /**
     * Delete an organization by ID.
     */
    public void deleteOrganization(Long id) {
        organizationRepository.deleteById(id);
    }

    // --- Member Operations ---

    /**
     * Find all members of an organization.
     */
    public List<Earth2026OrgMember> findMembersByOrganizationId(Long organizationId) {
        return memberRepository.findByOrganizationId(organizationId);
    }

    /**
     * Find active members of an organization.
     */
    public List<Earth2026OrgMember> findActiveMembersByOrganizationId(Long organizationId) {
        return memberRepository.findByOrganizationIdAndActiveTrue(organizationId);
    }

    /**
     * Find organization memberships for an entity.
     */
    public List<Earth2026OrgMember> findMembersByEntity(
            Earth2026Connection.EntityType memberType, Long memberId) {
        return memberRepository.findByMemberTypeAndMemberId(memberType, memberId);
    }

    /**
     * Find active organization memberships for an entity.
     */
    public List<Earth2026OrgMember> findActiveMembersByEntity(
            Earth2026Connection.EntityType memberType, Long memberId) {
        return memberRepository.findByMemberTypeAndMemberIdAndActiveTrue(memberType, memberId);
    }

    /**
     * Add a member to an organization.
     * @param organizationId the organization ID
     * @param memberType type of the member entity
     * @param memberId ID of the member entity
     * @param role role within the organization (optional)
     * @return the created member
     */
    public Optional<Earth2026OrgMember> addMember(Long organizationId,
                                                    Earth2026Connection.EntityType memberType,
                                                    Long memberId, String role) {
        return organizationRepository.findById(organizationId).map(organization -> {
            Earth2026OrgMember member = new Earth2026OrgMember();
            member.setOrganization(organization);
            member.setMemberType(memberType);
            member.setMemberId(memberId);
            member.setRole(role);
            member.setRank(1);
            member.setLoyalty(50);
            member.setActive(true);
            return memberRepository.save(member);
        });
    }

    /**
     * Remove a member from an organization.
     * @param memberId the member ID
     * @return true if the member was removed
     */
    public boolean removeMember(Long memberId) {
        Optional<Earth2026OrgMember> optional = memberRepository.findById(memberId);
        if (optional.isPresent()) {
            Earth2026OrgMember member = optional.get();
            member.setActive(false);
            memberRepository.save(member);
            return true;
        }
        return false;
    }

    // --- Loyalty Management ---

    /**
     * Increase the loyalty of a member.
     * @param memberId the member ID
     * @param amount the amount to increase (clamped to 0-100)
     * @return the updated member
     */
    public Optional<Earth2026OrgMember> increaseLoyalty(Long memberId, int amount) {
        return memberRepository.findById(memberId).map(member -> {
            member.increaseLoyalty(amount);
            return memberRepository.save(member);
        });
    }

    /**
     * Decrease the loyalty of a member.
     * @param memberId the member ID
     * @param amount the amount to decrease (clamped to 0-100)
     * @return the updated member
     */
    public Optional<Earth2026OrgMember> decreaseLoyalty(Long memberId, int amount) {
        return memberRepository.findById(memberId).map(member -> {
            member.decreaseLoyalty(amount);
            return memberRepository.save(member);
        });
    }

    // --- Hierarchy Operations ---

    /**
     * Promote a member by increasing rank.
     * @param memberId the member ID
     * @param levels number of levels to promote
     * @return the updated member
     */
    public Optional<Earth2026OrgMember> promoteMember(Long memberId, int levels) {
        return memberRepository.findById(memberId).map(member -> {
            member.promote(levels);
            return memberRepository.save(member);
        });
    }

    /**
     * Demote a member by decreasing rank.
     * @param memberId the member ID
     * @param levels number of levels to demote
     * @return the updated member
     */
    public Optional<Earth2026OrgMember> demoteMember(Long memberId, int levels) {
        return memberRepository.findById(memberId).map(member -> {
            member.demote(levels);
            return memberRepository.save(member);
        });
    }

    // --- Organization Events ---

    /**
     * Simulate an organization meeting, increasing all members' loyalty.
     * @param organizationId the organization ID
     * @param loyaltyIncrease the amount to increase each member's loyalty
     * @return the updated organization
     */
    public Optional<Earth2026Organization> organizationMeeting(Long organizationId, int loyaltyIncrease) {
        return organizationRepository.findById(organizationId).map(organization -> {
            List<Earth2026OrgMember> members = memberRepository.findByOrganizationIdAndActiveTrue(organizationId);
            for (Earth2026OrgMember member : members) {
                member.increaseLoyalty(loyaltyIncrease);
                memberRepository.save(member);
            }
            // Meeting also boosts reputation slightly
            organization.setReputation(Math.min(100, organization.getReputation() + 1));
            return organizationRepository.save(organization);
        });
    }

    /**
     * Get the number of active members in an organization.
     */
    public int getActiveMemberCount(Long organizationId) {
        return memberRepository.findByOrganizationIdAndActiveTrue(organizationId).size();
    }

    /**
     * Check if an entity is a member of an organization.
     */
    public boolean isMember(Long organizationId,
                            Earth2026Connection.EntityType memberType, Long memberId) {
        List<Earth2026OrgMember> members = memberRepository.findByOrganizationIdAndActiveTrue(organizationId);
        return members.stream().anyMatch(m ->
                m.getMemberType() == memberType && m.getMemberId().equals(memberId));
    }
}
