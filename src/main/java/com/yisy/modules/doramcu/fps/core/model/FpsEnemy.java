package com.yisy.modules.doramcu.fps.core.model;

import java.util.UUID;

/**
 * Defines an AI enemy for the FPS game.
 */
public class FpsEnemy {

    public enum EnemyBehavior {
        PATROL, CHASE, ATTACK, FLANK, RETREAT, AMBUSH
    }

    private String id;
    private String name;
    private int health;
    private double speed;
    private EnemyBehavior behavior;
    private int damage;
    private double attackRange;
    private double attackCooldown;
    private String dropItem;
    private double dropChance;
    private int experienceReward;
    private String description;

    public FpsEnemy() {
    }

    public FpsEnemy(String name, int health, double speed, EnemyBehavior behavior, int damage) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.health = health;
        this.speed = speed;
        this.behavior = behavior;
        this.damage = damage;
        this.attackRange = 50.0;
        this.attackCooldown = 1.0;
        this.dropChance = 0.3;
        this.experienceReward = 100;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
    public EnemyBehavior getBehavior() { return behavior; }
    public void setBehavior(EnemyBehavior behavior) { this.behavior = behavior; }
    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }
    public double getAttackRange() { return attackRange; }
    public void setAttackRange(double attackRange) { this.attackRange = attackRange; }
    public double getAttackCooldown() { return attackCooldown; }
    public void setAttackCooldown(double attackCooldown) { this.attackCooldown = attackCooldown; }
    public String getDropItem() { return dropItem; }
    public void setDropItem(String dropItem) { this.dropItem = dropItem; }
    public double getDropChance() { return dropChance; }
    public void setDropChance(double dropChance) { this.dropChance = dropChance; }
    public int getExperienceReward() { return experienceReward; }
    public void setExperienceReward(int experienceReward) { this.experienceReward = experienceReward; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
