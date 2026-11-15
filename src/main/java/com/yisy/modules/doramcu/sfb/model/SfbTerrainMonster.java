package com.yisy.modules.doramcu.sfb.model;

import java.util.*;

/**
 * Terrain monsters for Star Fleet Battles.
 * <p>
 * Space Amoebas and Dragons are hazardous terrain entities that can appear
 * during SFB battles, attacking ships and affecting movement. Based on the
 * SFB Terrain Monsters supplement rules.
 * </p>
 * <p>
 * Space Amoebas are gelatinous space creatures that consume ships and grow
 * larger. Dragons are massive space reptiles that breathe energy beams and
 * are highly mobile.
 * </p>
 */
public class SfbTerrainMonster {

    /**
     * Types of terrain monsters in SFB.
     */
    public enum MonsterType {
        /**
         * Space Amoeba — gelatinous creature that consumes ships.
         * Grows larger by eating ships. Slow but very durable.
         */
        SPACE_AMOEBA,
        /**
         * Space Dragon — massive reptilian creature.
         * Fast, agile, breathes energy beams. High damage output.
         */
        SPACE_DRAGON,
        /**
         * Space Whale — giant passive creature.
         * Generally harmless but can be agitated. Blocks movement.
         */
        SPACE_WHALE,
        /**
         * Plasma Wasp — small but numerous energy creatures.
         * Swarm behavior, high speed, low individual HP.
         */
        PLASMA_WASP,
        /**
         * Void Kraken — tentacled deep-space predator.
         * Grabs and holds ships, drains energy.
         */
        VOID_KRAKEN
    }

    /**
     * Size class of the monster.
     */
    public enum SizeClass {
        TINY, SMALL, MEDIUM, LARGE, HUGE, GIANT
    }

    private final String id;
    private final MonsterType type;
    private final String name;
    private final SizeClass sizeClass;
    private final int hitPoints;
    private final int maxHitPoints;
    private final int attackPower;
    private final int defense;
    private final int movementSpeed; // 1-32, like ships
    private final int attackRange; // hexes
    private final int specialAbility; // 0-10, determines special attack frequency
    private final List<String> abilities;
    private final SfbHexPosition position;
    private boolean active;
    private int hitPointsRemaining;
    private final Map<String, Object> properties;

    private SfbTerrainMonster(Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.name = builder.name;
        this.sizeClass = builder.sizeClass;
        this.hitPoints = builder.hitPoints;
        this.maxHitPoints = builder.hitPoints;
        this.attackPower = builder.attackPower;
        this.defense = builder.defense;
        this.movementSpeed = Math.max(1, Math.min(32, builder.movementSpeed));
        this.attackRange = builder.attackRange;
        this.specialAbility = Math.max(0, Math.min(10, builder.specialAbility));
        this.abilities = Collections.unmodifiableList(new ArrayList<>(builder.abilities));
        this.position = builder.position;
        this.active = true;
        this.hitPointsRemaining = builder.hitPoints;
        this.properties = new HashMap<>(builder.properties);
    }

    /**
     * Creates a predefined terrain monster of the given type.
     */
    public static SfbTerrainMonster createPredefined(MonsterType type, String positionX, String positionR) {
        int q = 0, r = 0;
        try { q = Integer.parseInt(positionX); } catch (NumberFormatException ignored) {}
        try { r = Integer.parseInt(positionR); } catch (NumberFormatException ignored) {}
        SfbHexPosition pos = new SfbHexPosition(q, r, 0);
        return switch (type) {
            case SPACE_AMOEBA -> new Builder(type)
                    .name("Space Amoeba")
                    .sizeClass(SizeClass.HUGE)
                    .hitPoints(100)
                    .attackPower(8)
                    .defense(4)
                    .movementSpeed(4)
                    .attackRange(1)
                    .specialAbility(3)
                    .ability("Consume Ship", "Grows by 10 HP per ship consumed")
                    .ability("Acid Cloud", "Deals 2 damage to adjacent ships each impulse")
                    .position(pos)
                    .property("consumedShips", 0)
                    .build();
            case SPACE_DRAGON -> new Builder(type)
                    .name("Space Dragon")
                    .sizeClass(SizeClass.GIANT)
                    .hitPoints(150)
                    .attackPower(10)
                    .defense(6)
                    .movementSpeed(20)
                    .attackRange(6)
                    .specialAbility(5)
                    .ability("Energy Beam", "Deals 10 damage in a straight line")
                    .ability("Tail Sweep", "Deals 5 damage to ships in rear arc")
                    .ability("High Maneuverability", "Can turn 3 modes per impulse")
                    .position(pos)
                    .property("beamCooldown", 0)
                    .build();
            case SPACE_WHALE -> new Builder(type)
                    .name("Space Whale")
                    .sizeClass(SizeClass.GIANT)
                    .hitPoints(200)
                    .attackPower(2)
                    .defense(8)
                    .movementSpeed(8)
                    .attackRange(1)
                    .specialAbility(1)
                    .ability("Passive", "Does not attack unless agitated")
                    .ability("Block Movement", "Ships cannot pass through occupied hex")
                    .position(pos)
                    .property("agitated", false)
                    .build();
            case PLASMA_WASP -> new Builder(type)
                    .name("Plasma Wasp Swarm")
                    .sizeClass(SizeClass.SMALL)
                    .hitPoints(20)
                    .attackPower(4)
                    .defense(2)
                    .movementSpeed(28)
                    .attackRange(3)
                    .specialAbility(7)
                    .ability("Swarm", "Attacks from multiple directions")
                    .ability("Energy Drain", "Reduces target shield recharge by 2")
                    .position(pos)
                    .property("swarmCount", 10)
                    .build();
            case VOID_KRAKEN -> new Builder(type)
                    .name("Void Kraken")
                    .sizeClass(SizeClass.HUGE)
                    .hitPoints(120)
                    .attackPower(7)
                    .defense(5)
                    .movementSpeed(12)
                    .attackRange(4)
                    .specialAbility(6)
                    .ability("Tentacle Grab", "Grabs ship, prevents movement for 2 impulses")
                    .ability("Energy Drain", "Drains 3 energy per impulse from grabbed ship")
                    .position(pos)
                    .property("grabbedShipId", null)
                    .build();
        };
    }

    /**
     * Takes damage from an attack.
     * @param damage amount of damage
     * @return remaining HP (0 if destroyed)
     */
    public int takeDamage(int damage) {
        if (!active || hitPointsRemaining <= 0) {
            return 0;
        }
        hitPointsRemaining = Math.max(0, hitPointsRemaining - damage);
        if (hitPointsRemaining <= 0) {
            active = false;
        }
        return hitPointsRemaining;
    }

    /**
     * Attacks a ship. Returns the damage dealt after defense.
     * @param shipDefense the ship's shield value in the attacked direction
     * @return damage dealt to the ship
     */
    public int attack(int shipDefense) {
        if (!active) return 0;
        int damage = Math.max(0, attackPower - (shipDefense / 2));
        return damage;
    }

    /**
     * Uses a special ability if conditions are met.
     * @param impulse current impulse number
     * @return the ability name used, or null if no ability used
     */
    public String useSpecialAbility(int impulse) {
        if (!active || specialAbility <= 0) return null;
        // Special ability triggers based on impulse and specialAbility value
        if (impulse % (11 - specialAbility) == 0) {
            return abilities.isEmpty() ? null : abilities.get(0);
        }
        return null;
    }

    /**
     * Moves the monster toward a target position.
     * @param targetX target hex X
     * @param targetY target hex Y
     */
    public void moveToward(int targetQ, int targetR) {
        if (!active) return;
        // Simple movement: move one hex toward target
        // In a full implementation, this would use hex pathfinding
        properties.put("lastTargetQ", targetQ);
        properties.put("lastTargetR", targetR);
    }

    // Getters
    public String getId() { return id; }
    public MonsterType getType() { return type; }
    public String getName() { return name; }
    public SizeClass getSizeClass() { return sizeClass; }
    public int getHitPoints() { return hitPoints; }
    public int getMaxHitPoints() { return maxHitPoints; }
    public int getHitPointsRemaining() { return hitPointsRemaining; }
    public int getAttackPower() { return attackPower; }
    public int getDefense() { return defense; }
    public int getMovementSpeed() { return movementSpeed; }
    public int getAttackRange() { return attackRange; }
    public int getSpecialAbility() { return specialAbility; }
    public List<String> getAbilities() { return abilities; }
    public SfbHexPosition getPosition() { return position; }
    public boolean isActive() { return active; }
    public Map<String, Object> getProperties() { return Collections.unmodifiableMap(properties); }

    /**
     * Checks if this monster can attack a ship at the given position.
     */
    public boolean canAttack(int shipQ, int shipR) {
        if (!active) return false;
        // Simple range check — in full implementation, use hex distance
        return attackRange > 0;
    }

    @Override
    public String toString() {
        return "SfbTerrainMonster{id=" + id + ", type=" + type + ", name=" + name +
                ", hp=" + hitPointsRemaining + "/" + maxHitPoints +
                ", active=" + active + "}";
    }

    /**
     * Builder for creating custom terrain monsters.
     */
    public static class Builder {
        private String id;
        private final MonsterType type;
        private String name;
        private SizeClass sizeClass = SizeClass.MEDIUM;
        private int hitPoints = 50;
        private int attackPower = 5;
        private int defense = 3;
        private int movementSpeed = 16;
        private int attackRange = 2;
        private int specialAbility = 0;
        private final List<String> abilities = new ArrayList<>();
        private SfbHexPosition position;
        private final Map<String, Object> properties = new HashMap<>();

        public Builder(MonsterType type) {
            this.type = type;
            this.id = UUID.randomUUID().toString().substring(0, 8);
            this.name = type.name().replace("_", " ");
        }

        public Builder name(String name) { this.name = name; return this; }
        public Builder sizeClass(SizeClass sizeClass) { this.sizeClass = sizeClass; return this; }
        public Builder hitPoints(int hitPoints) { this.hitPoints = hitPoints; return this; }
        public Builder attackPower(int attackPower) { this.attackPower = attackPower; return this; }
        public Builder defense(int defense) { this.defense = defense; return this; }
        public Builder movementSpeed(int movementSpeed) { this.movementSpeed = movementSpeed; return this; }
        public Builder attackRange(int attackRange) { this.attackRange = attackRange; return this; }
        public Builder specialAbility(int specialAbility) { this.specialAbility = specialAbility; return this; }
        public Builder ability(String name, String description) {
            this.abilities.add(name + ": " + description);
            return this;
        }
        public Builder position(SfbHexPosition position) { this.position = position; return this; }
        public Builder property(String key, Object value) { this.properties.put(key, value); return this; }

        public SfbTerrainMonster build() {
            return new SfbTerrainMonster(this);
        }
    }
}
