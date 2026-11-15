package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a member of an Earth 2026 organization.
 * <p>
 * This is a join entity that links any entity type to an organization with
 * role, rank, and loyalty tracking.
 * </p>
 * <p>
 * Key mechanics:
 * - Organization: Reference to the parent organization
 * - Member Type: Any entity type (Character, NPC, AI Character, Companion, Monster)
 * - Member ID: ID of the member entity
 * - Role: Free-form role name (e.g., "Handler", "Field Agent", "Double Agent")
 * - Rank: 1-10, hierarchical position
 * - Loyalty: 0-100, loyalty to the organization
 * </p>
 */
@Entity
@Table(name = "e26_org_members", indexes = {
        @Index(name = "idx_e26_org_member_org", columnList = "organizationId"),
        @Index(name = "idx_e26_org_member_member", columnList = "memberType,memberId"),
        @Index(name = "idx_e26_org_member_active", columnList = "active")
})
public class Earth2026OrgMember {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Reference to the parent organization.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Earth2026Organization organization;

    /**
     * Organization ID for quick queries (denormalized from organization reference).
     */
    @Column(name = "organization_id", insertable = false, updatable = false)
    private Long organizationId;

    /**
     * Type of the member entity.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", nullable = false, length = 20)
    private Earth2026Connection.EntityType memberType;

    /**
     * ID of the member entity.
     */
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    /**
     * Role within the organization (e.g., "Handler", "Field Agent", "Double Agent").
     */
    @Column(length = 50)
    private String role;

    /**
     * Rank within the organization (1-10). Higher means more senior.
     */
    @Column(name = "rank")
    private int rank = 1;

    /**
     * Loyalty to the organization (0-100). Higher means more loyal.
     */
    @Column(name = "loyalty")
    private int loyalty = 50;

    /**
     * Whether this membership is active.
     */
    @Column(nullable = false)
    private boolean active = true;

    public Earth2026OrgMember() {
    }

    public Earth2026OrgMember(Earth2026Organization organization,
                               Earth2026Connection.EntityType memberType, Long memberId) {
        this.organization = organization;
        this.memberType = memberType;
        this.memberId = memberId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Earth2026Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Earth2026Organization organization) {
        this.organization = organization;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public Earth2026Connection.EntityType getMemberType() {
        return memberType;
    }

    public void setMemberType(Earth2026Connection.EntityType memberType) {
        this.memberType = memberType;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = Math.max(1, Math.min(10, rank));
    }

    public int getLoyalty() {
        return loyalty;
    }

    public void setLoyalty(int loyalty) {
        this.loyalty = Math.max(0, Math.min(100, loyalty));
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Increases the loyalty score.
     */
    public void increaseLoyalty(int amount) {
        this.loyalty = Math.min(100, this.loyalty + amount);
    }

    /**
     * Decreases the loyalty score.
     */
    public void decreaseLoyalty(int amount) {
        this.loyalty = Math.max(0, this.loyalty - amount);
    }

    /**
     * Promote the member by increasing rank.
     */
    public void promote(int levels) {
        this.rank = Math.min(10, this.rank + levels);
    }

    /**
     * Demote the member by decreasing rank.
     */
    public void demote(int levels) {
        this.rank = Math.max(1, this.rank - levels);
    }

    @Override
    public String toString() {
        return "Earth2026OrgMember{id=" + id + ", orgId=" + organizationId +
                ", member=" + memberType + ":" + memberId +
                ", role=" + role + ", rank=" + rank + ", loyalty=" + loyalty + "}";
    }
}
