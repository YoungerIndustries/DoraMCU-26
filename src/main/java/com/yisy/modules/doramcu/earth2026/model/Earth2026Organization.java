package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a universal organization in the Earth 2026 world.
 * <p>
 * This replaces the narrow Family concept with a polymorphic group that can represent:
 * - Families, espionage rings, merchant guilds, criminal syndicates
 * - Military units, cults, government agencies, research labs, parties
 * </p>
 * <p>
 * Key mechanics:
 * - Organization Type: Defines the nature of the group
 * - Leader: Any entity type can lead an organization
 * - Hierarchy Level: 1=flat, 2=supervisor, 3=corporate, etc.
 * - Reputation: 1-100, social standing
 * - Wealth: 1-100, economic power
 * - Secrecy Level: 0-100, how hidden is this organization?
 * - Members: List of Earth2026OrgMember entities
 * </p>
 */
@Entity
@Table(name = "e26_organizations", indexes = {
        @Index(name = "idx_e26_organization_type", columnList = "orgType"),
        @Index(name = "idx_e26_organization_leader", columnList = "leaderType,leaderId"),
        @Index(name = "idx_e26_organization_active", columnList = "active")
})
public class Earth2026Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the organization.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Description of the organization.
     */
    @Column(length = 2000)
    private String description;

    /**
     * Type of organization: Family, Espionage Ring, Merchant Guild, etc.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "org_type", nullable = false, length = 20)
    private OrganizationType orgType;

    /**
     * Type of the leader entity.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "leader_type", length = 20)
    private Earth2026Connection.EntityType leaderType;

    /**
     * ID of the leader entity.
     */
    @Column(name = "leader_id")
    private Long leaderId;

    /**
     * Hierarchy level: 1=flat, 2=supervisor, 3=corporate, etc.
     */
    @Column(name = "hierarchy_level")
    private int hierarchyLevel = 1;

    /**
     * Reputation of the organization (1-100).
     */
    @Column(name = "reputation")
    private int reputation = 50;

    /**
     * Wealth of the organization (1-100).
     */
    @Column(name = "wealth")
    private int wealth = 50;

    /**
     * Secrecy level (0-100). 0=public, 100=highly classified.
     */
    @Column(name = "secrecy_level")
    private int secrecyLevel = 0;

    /**
     * Reference to a home structure (building, base, etc.).
     */
    @Column(name = "home_structure_id")
    private Long homeStructureId;

    /**
     * Whether this organization is active.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Members of this organization.
     */
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Earth2026OrgMember> members = new ArrayList<>();

    /**
     * Types of organizations.
     */
    public enum OrganizationType {
        /** Family group. */
        FAMILY,
        /** Espionage ring or spy network. */
        ESPIONAGE_RING,
        /** Merchant guild or trade association. */
        MERCHANT_GUILD,
        /** Criminal syndicate or mafia. */
        CRIMINAL_SYNDICATE,
        /** Military unit or armed force. */
        MILITARY_UNIT,
        /** Cult or religious group. */
        CULT,
        /** Government agency or department. */
        GOVERNMENT_AGENCY,
        /** Research laboratory or think tank. */
        RESEARCH_LAB,
        /** Adventure party or quest group. */
        PARTY,
        /** Other custom organization type. */
        OTHER
    }

    public Earth2026Organization() {
    }

    public Earth2026Organization(String name, OrganizationType orgType) {
        this.name = name;
        this.orgType = orgType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OrganizationType getOrgType() {
        return orgType;
    }

    public void setOrgType(OrganizationType orgType) {
        this.orgType = orgType;
    }

    public Earth2026Connection.EntityType getLeaderType() {
        return leaderType;
    }

    public void setLeaderType(Earth2026Connection.EntityType leaderType) {
        this.leaderType = leaderType;
    }

    public Long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Long leaderId) {
        this.leaderId = leaderId;
    }

    public int getHierarchyLevel() {
        return hierarchyLevel;
    }

    public void setHierarchyLevel(int hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = Math.max(1, Math.min(100, reputation));
    }

    public int getWealth() {
        return wealth;
    }

    public void setWealth(int wealth) {
        this.wealth = Math.max(1, Math.min(100, wealth));
    }

    public int getSecrecyLevel() {
        return secrecyLevel;
    }

    public void setSecrecyLevel(int secrecyLevel) {
        this.secrecyLevel = Math.max(0, Math.min(100, secrecyLevel));
    }

    public Long getHomeStructureId() {
        return homeStructureId;
    }

    public void setHomeStructureId(Long homeStructureId) {
        this.homeStructureId = homeStructureId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Earth2026OrgMember> getMembers() {
        return members;
    }

    public void setMembers(List<Earth2026OrgMember> members) {
        this.members = members;
    }

    /**
     * Add a member to this organization.
     */
    public void addMember(Earth2026OrgMember member) {
        members.add(member);
        member.setOrganization(this);
    }

    /**
     * Remove a member from this organization.
     */
    public void removeMember(Earth2026OrgMember member) {
        members.remove(member);
        member.setOrganization(null);
    }

    @Override
    public String toString() {
        return "Earth2026Organization{id=" + id + ", name='" + name + "', type=" + orgType +
                ", reputation=" + reputation + ", wealth=" + wealth + ", members=" + members.size() + "}";
    }
}
