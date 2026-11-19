package com.yisy.modules.doramcu.lord_of_the_rings.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a Lord of the Rings monster.
 * <p>
 * Monsters include Orcs, Uruk-hai, Trolls, Balrogs, Nazgûl, Wargs,
 * and other creatures of Middle-earth.
 * </p>
 */
@Entity
@Table(name = "lotr_monsters", indexes = {
        @Index(name = "idx_lotr_monster_name", columnList = "name"),
        @Index(name = "idx_lotr_monster_type", columnList = "monsterType")
})
public class LotrMonster {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "monster_type", nullable = false, length = 50)
    private String monsterType;

    @Column(name = "challenge_rating", nullable = false)
    private double challengeRating;

    @Column(name = "hit_points", nullable = false)
    private int hitPoints;

    @Column(name = "armor_class", nullable = false)
    private int armorClass;

    @Column(nullable = false)
    private int speed = 30;

    @Column(name = "str_score", nullable = false)
    private int strScore = 10;

    @Column(name = "dex_score", nullable = false)
    private int dexScore = 10;

    @Column(name = "con_score", nullable = false)
    private int conScore = 10;

    @Column(name = "int_score", nullable = false)
    private int intScore = 10;

    @Column(name = "wis_score", nullable = false)
    private int wisScore = 10;

    @Column(name = "cha_score", nullable = false)
    private int chaScore = 10;

    @Column(name = "xp_value", nullable = false)
    private int xpValue;

    @Column(name = "faction", length = 50)
    private String faction;

    @Column(name = "corruption_value", nullable = false)
    private int corruptionValue = 0;

    @Column(name = "is_boss", nullable = false)
    private boolean isBoss = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

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

    public String getMonsterType() {
        return monsterType;
    }

    public void setMonsterType(String monsterType) {
        this.monsterType = monsterType;
    }

    public double getChallengeRating() {
        return challengeRating;
    }

    public void setChallengeRating(double challengeRating) {
        this.challengeRating = challengeRating;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(int hitPoints) {
        this.hitPoints = hitPoints;
    }

    public int getArmorClass() {
        return armorClass;
    }

    public void setArmorClass(int armorClass) {
        this.armorClass = armorClass;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getStrScore() {
        return strScore;
    }

    public void setStrScore(int strScore) {
        this.strScore = strScore;
    }

    public int getDexScore() {
        return dexScore;
    }

    public void setDexScore(int dexScore) {
        this.dexScore = dexScore;
    }

    public int getConScore() {
        return conScore;
    }

    public void setConScore(int conScore) {
        this.conScore = conScore;
    }

    public int getIntScore() {
        return intScore;
    }

    public void setIntScore(int intScore) {
        this.intScore = intScore;
    }

    public int getWisScore() {
        return wisScore;
    }

    public void setWisScore(int wisScore) {
        this.wisScore = wisScore;
    }

    public int getChaScore() {
        return chaScore;
    }

    public void setChaScore(int chaScore) {
        this.chaScore = chaScore;
    }

    public int getXpValue() {
        return xpValue;
    }

    public void setXpValue(int xpValue) {
        this.xpValue = xpValue;
    }

    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public int getCorruptionValue() {
        return corruptionValue;
    }

    public void setCorruptionValue(int corruptionValue) {
        this.corruptionValue = corruptionValue;
    }

    public boolean isBoss() {
        return isBoss;
    }

    public void setBoss(boolean boss) {
        isBoss = boss;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "LotrMonster{name='" + name + "', type='" + monsterType + "', CR=" + challengeRating + "}";
    }
}
