package com.yisy.modules.doramcu.dnd5e.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a D&D 5e combat encounter.
 * <p>
 * Tracks initiative order, current round, and a list of combatants.
 * </p>
 */
@Entity
@Table(name = "dnd5e_combats", indexes = {
        @Index(name = "idx_dnd5e_combat_status", columnList = "status")
})
public class Dnd5eCombat {

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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "combat", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Dnd5eCombatant> combatants = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Dnd5eCombat() {
    }

    public Dnd5eCombat(String encounterName) {
        this.encounterName = encounterName;
        this.status = "ACTIVE";
    }

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

    public List<Dnd5eCombatant> getCombatants() {
        return combatants;
    }

    public void setCombatants(List<Dnd5eCombatant> combatants) {
        this.combatants = combatants;
    }

    @Override
    public String toString() {
        return "Dnd5eCombat{id=" + id + ", name='" + encounterName + "', round=" + currentRound +
                ", status=" + status + "}";
    }
}
