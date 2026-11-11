package com.yisy.modules.doramcu.dnd5e.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * JPA entity representing a combatant in a D&D 5e combat encounter.
 * <p>
 * A combatant can be a player character or a monster/NPC with AC, HP,
 * initiative, and conditions.
 * </p>
 */
@Entity
@Table(name = "dnd5e_combatants", indexes = {
        @Index(name = "idx_dnd5e_combatant_combat", columnList = "combat_id")
})
public class Dnd5eCombatant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combat_id", nullable = false)
    @JsonIgnore
    private Dnd5eCombat combat;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "armor_class", nullable = false)
    private int armorClass = 10;

    @Column(name = "current_hp", nullable = false)
    private int currentHp;

    @Column(name = "max_hp", nullable = false)
    private int maxHp;

    @Column
    private Integer initiative;

    @Column(columnDefinition = "JSON")
    private String conditions;

    @Column(name = "is_character", nullable = false)
    private boolean isCharacter = false;

    @Column(name = "turn_order")
    private Integer turnOrder;

    public Dnd5eCombatant() {
    }

    public Dnd5eCombatant(String name, int armorClass, int maxHp) {
        this.name = name;
        this.armorClass = armorClass;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
    }

    public boolean isAlive() {
        return currentHp > 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Dnd5eCombat getCombat() {
        return combat;
    }

    public void setCombat(Dnd5eCombat combat) {
        this.combat = combat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getArmorClass() {
        return armorClass;
    }

    public void setArmorClass(int armorClass) {
        this.armorClass = armorClass;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public Integer getInitiative() {
        return initiative;
    }

    public void setInitiative(Integer initiative) {
        this.initiative = initiative;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public boolean isCharacter() {
        return isCharacter;
    }

    public void setCharacter(boolean character) {
        isCharacter = character;
    }

    public Integer getTurnOrder() {
        return turnOrder;
    }

    public void setTurnOrder(Integer turnOrder) {
        this.turnOrder = turnOrder;
    }

    @Override
    public String toString() {
        return "Dnd5eCombatant{name='" + name + "', hp=" + currentHp + "/" + maxHp +
                ", ac=" + armorClass + "}";
    }
}
