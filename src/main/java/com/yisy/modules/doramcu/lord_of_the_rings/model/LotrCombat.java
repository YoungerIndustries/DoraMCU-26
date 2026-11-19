package com.yisy.modules.doramcu.lord_of_the_rings.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a Lord of the Rings combat encounter.
 * <p>
 * Tracks initiative order, current round, and a list of combatants.
 * LOTR combat uses d6-based resolution with corruption and hope mechanics.
 * </p>
 */
@Entity
@Table(name = "lotr_combats", indexes = {
        @Index(name = "idx_lotr_combat_status", columnList = "status")
})
public class LotrCombat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "encounter_name", nullable = false, length = 200)
    private String encounterName;

    @Column(name = "current_round", nullable = false)
    private int currentRound = 1;

    @Column(name = "initiative_order", columnDefinition = "JSON")
    private String initiativeOrder;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "corruption_check", nullable = false)
    private boolean corruptionCheck = false;

    @Column(name = "hope_check", nullable = false)
    private boolean hopeCheck = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "combat", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<LotrCombatant> combatants = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public LotrCombat() {
    }

    public LotrCombat(String encounterName) {
        this.encounterName = encounterName;
        this.status = "ACTIVE";
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEncounterName() {
        return encounterName;
    }

    public void setEncounterName(String encounterName) {
        this.encounterName = encounterName;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public String getInitiativeOrder() {
        return initiativeOrder;
    }

    public void setInitiativeOrder(String initiativeOrder) {
        this.initiativeOrder = initiativeOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isCorruptionCheck() {
        return corruptionCheck;
    }

    public void setCorruptionCheck(boolean corruptionCheck) {
        this.corruptionCheck = corruptionCheck;
    }

    public boolean isHopeCheck() {
        return hopeCheck;
    }

    public void setHopeCheck(boolean hopeCheck) {
        this.hopeCheck = hopeCheck;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<LotrCombatant> getCombatants() {
        return combatants;
    }

    public void setCombatants(List<LotrCombatant> combatants) {
        this.combatants = combatants;
    }

    @Override
    public String toString() {
        return "LotrCombat{id=" + id + ", name='" + encounterName + "', round=" + currentRound +
                ", status=" + status + "}";
    }
}
