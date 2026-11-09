package com.yisy.modules.doramcu.sfb.dto;

/**
 * Snapshot of a ship's current state for game state sync.
 */
public class SfbShipSnapshot {
    private String id;
    private String name;
    private int side;
    private int positionQ;
    private int positionR;
    private int facing;
    private int speed;
    private int shieldFrontRight;
    private int shieldFrontLeft;
    private int shieldLeft;
    private int shieldRight;
    private int shieldRearLeft;
    private int shieldRearRight;
    private int hullIntegrity;
    private boolean cloaked;
    private boolean crippled;
    private boolean destroyed;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getSide() { return side; }
    public void setSide(int side) { this.side = side; }
    public int getPositionQ() { return positionQ; }
    public void setPositionQ(int positionQ) { this.positionQ = positionQ; }
    public int getPositionR() { return positionR; }
    public void setPositionR(int positionR) { this.positionR = positionR; }
    public int getFacing() { return facing; }
    public void setFacing(int facing) { this.facing = facing; }
    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
    public int getShieldFrontRight() { return shieldFrontRight; }
    public void setShieldFrontRight(int shieldFrontRight) { this.shieldFrontRight = shieldFrontRight; }
    public int getShieldFrontLeft() { return shieldFrontLeft; }
    public void setShieldFrontLeft(int shieldFrontLeft) { this.shieldFrontLeft = shieldFrontLeft; }
    public int getShieldLeft() { return shieldLeft; }
    public void setShieldLeft(int shieldLeft) { this.shieldLeft = shieldLeft; }
    public int getShieldRight() { return shieldRight; }
    public void setShieldRight(int shieldRight) { this.shieldRight = shieldRight; }
    public int getShieldRearLeft() { return shieldRearLeft; }
    public void setShieldRearLeft(int shieldRearLeft) { this.shieldRearLeft = shieldRearLeft; }
    public int getShieldRearRight() { return shieldRearRight; }
    public void setShieldRearRight(int shieldRearRight) { this.shieldRearRight = shieldRearRight; }
    public int getHullIntegrity() { return hullIntegrity; }
    public void setHullIntegrity(int hullIntegrity) { this.hullIntegrity = hullIntegrity; }
    public boolean isCloaked() { return cloaked; }
    public void setCloaked(boolean cloaked) { this.cloaked = cloaked; }
    public boolean isCrippled() { return crippled; }
    public void setCrippled(boolean crippled) { this.crippled = crippled; }
    public boolean isDestroyed() { return destroyed; }
    public void setDestroyed(boolean destroyed) { this.destroyed = destroyed; }
}
