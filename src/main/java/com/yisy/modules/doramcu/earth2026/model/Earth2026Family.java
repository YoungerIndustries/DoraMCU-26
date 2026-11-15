package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a family in Earth 2026.
 * <p>
 * Families are groups of related characters and NPCs who share a household,
 * wealth, and reputation. This replaces the comma-separated family strings
 * in E26Npc with proper entity relationships.
 * </p>
 * <p>
 * Key mechanics:
 * - Name & Description: Family identity and background
 * - Members: List of family members with relationship types and bond scores
 * - Home Structure: The family's primary residence
 * - Family Wealth: Economic standing of the family
 * - Family Reputation: Social standing in the community
 * - Family Traits: Shared bonuses or characteristics
 * </p>
 */
@Entity
@Table(name = "e26_families", indexes = {
        @Index(name = "idx_e26_family_name", columnList = "name"),
        @Index(name = "idx_e26_family_home", columnList = "homeStructureId")
})
public class Earth2026Family {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the family (e.g., "Smith Family", "House of Chen").
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Description of the family's history and background.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Reference to the family's home structure.
     */
    @Column(name = "home_structure_id")
    private Long homeStructureId;

    /**
     * Family wealth level (1-100).
     */
    @Column(name = "family_wealth")
    private int familyWealth = 50;

    /**
     * Family reputation in the community (1-100).
     */
    @Column(name = "family_reputation")
    private int familyReputation = 50;

    /**
     * JSON array of family traits (e.g., ["Military Family", "Wealthy", "Influential"]).
     */
    @Column(name = "family_traits", columnDefinition = "TEXT")
    private String familyTraits = "[]";

    /**
     * Whether the family is active (not disbanded).
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * List of family members.
     */
    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Earth2026FamilyMember> members = new ArrayList<>();

    public Earth2026Family() {
    }

    public Earth2026Family(String name) {
        this.name = name;
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

    public Long getHomeStructureId() {
        return homeStructureId;
    }

    public void setHomeStructureId(Long homeStructureId) {
        this.homeStructureId = homeStructureId;
    }

    public int getFamilyWealth() {
        return familyWealth;
    }

    public void setFamilyWealth(int familyWealth) {
        this.familyWealth = Math.max(1, Math.min(100, familyWealth));
    }

    public int getFamilyReputation() {
        return familyReputation;
    }

    public void setFamilyReputation(int familyReputation) {
        this.familyReputation = Math.max(1, Math.min(100, familyReputation));
    }

    public String getFamilyTraits() {
        return familyTraits;
    }

    public void setFamilyTraits(String familyTraits) {
        this.familyTraits = familyTraits;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Earth2026FamilyMember> getMembers() {
        return members;
    }

    public void setMembers(List<Earth2026FamilyMember> members) {
        this.members = members;
    }

    /**
     * Adds a member to the family.
     */
    public void addMember(Earth2026FamilyMember member) {
        members.add(member);
        member.setFamily(this);
    }

    /**
     * Removes a member from the family.
     */
    public void removeMember(Earth2026FamilyMember member) {
        members.remove(member);
        member.setFamily(null);
    }

    @Override
    public String toString() {
        return "Earth2026Family{id=" + id + ", name='" + name + "', members=" + members.size() +
                ", wealth=" + familyWealth + ", reputation=" + familyReputation + "}";
    }
}
