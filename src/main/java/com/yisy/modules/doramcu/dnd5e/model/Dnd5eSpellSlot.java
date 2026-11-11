package com.yisy.modules.doramcu.dnd5e.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * JPA entity tracking spell slots for a D&D 5e character.
 * <p>
 * Each character has spell slots by level (0-9), with total and used counts.
 * </p>
 */
@Entity
@Table(name = "dnd5e_spell_slots")
public class Dnd5eSpellSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    @JsonIgnore
    private Dnd5eCharacter character;

    @Column(name = "slot_level", nullable = false)
    private int slotLevel;

    @Column(name = "total_slots", nullable = false)
    private int totalSlots = 0;

    @Column(name = "used_slots", nullable = false)
    private int usedSlots = 0;

    public Dnd5eSpellSlot() {
    }

    public Dnd5eSpellSlot(Dnd5eCharacter character, int slotLevel, int totalSlots) {
        this.character = character;
        this.slotLevel = slotLevel;
        this.totalSlots = totalSlots;
    }

    public int availableSlots() {
        return Math.max(0, totalSlots - usedSlots);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Dnd5eCharacter getCharacter() {
        return character;
    }

    public void setCharacter(Dnd5eCharacter character) {
        this.character = character;
    }

    public int getSlotLevel() {
        return slotLevel;
    }

    public void setSlotLevel(int slotLevel) {
        this.slotLevel = slotLevel;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(int totalSlots) {
        this.totalSlots = totalSlots;
    }

    public int getUsedSlots() {
        return usedSlots;
    }

    public void setUsedSlots(int usedSlots) {
        this.usedSlots = usedSlots;
    }

    @Override
    public String toString() {
        return "Dnd5eSpellSlot{level=" + slotLevel + ", used=" + usedSlots + "/" + totalSlots + "}";
    }
}
