package com.yisy.modules.doramcu.fullthrust.model;

/**
 * Represents a missile or torpedo in flight.
 * Missiles have their own position, heading, and speed.
 */
public class Missile {
    private String id;
    private String ownerId; // Ship that launched this missile
    private String ownerName; // Name of the ship
    private HexPosition position;
    private int heading; // Direction of travel (0-5)
    private int speed; // Current speed
    private int damage;
    private int turnsRemaining; // Turns until missile expires
    private boolean hit; // Whether this missile has hit a target

    public Missile() {
        this.position = new HexPosition();
        this.heading = 0;
        this.speed = 0;
        this.hit = false;
    }

    public Missile(String id, String ownerId, String ownerName, HexPosition position, int heading, int damage, int turnsRemaining) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.position = position;
        this.heading = heading;
        this.speed = 2; // Missiles move 2 hexes per turn
        this.damage = damage;
        this.turnsRemaining = turnsRemaining;
        this.hit = false;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public HexPosition getPosition() { return position; }
    public void setPosition(HexPosition position) { this.position = position; }
    public int getHeading() { return heading; }
    public void setHeading(int heading) { this.heading = heading; }
    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }
    public int getTurnsRemaining() { return turnsRemaining; }
    public void setTurnsRemaining(int turnsRemaining) { this.turnsRemaining = turnsRemaining; }
    public boolean isHit() { return hit; }
    public void setHit(boolean hit) { this.hit = hit; }

    /**
     * Move the missile forward
     */
    public void move() {
        // Direction vectors for hex grid
        int[] dq = {1, 1, 0, -1, -1, 0};
        int[] dr = {0, 1, 1, 0, -1, -1};

        for (int i = 0; i < speed; i++) {
            position.setQ(position.getQ() + dq[heading]);
            position.setR(position.getR() + dr[heading]);
        }
        turnsRemaining--;
    }

    /**
     * Check if missile has expired
     */
    public boolean isExpired() {
        return turnsRemaining <= 0;
    }
}
