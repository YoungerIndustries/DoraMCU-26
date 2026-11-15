package com.yisy.modules.doramcu.fps.core;

import com.yisy.modules.doramcu.fps.core.model.FpsWeapon;

import java.util.UUID;

/**
 * Represents the server-authoritative state of a single FPS player.
 */
public class FpsPlayerState {

    private String playerId;
    private String playerName;
    private double posX, posY, posZ;
    private double pitch;
    private double yaw;
    private double velX, velY, velZ;
    private int health;
    private int maxHealth;
    private int ammo;
    private int reserveAmmo;
    private String currentWeaponId;
    private double lastFireTime;
    private double lastReloadTime;
    private boolean isReloading;
    private boolean isSprinting;
    private boolean isCrouching;
    private boolean isJumping;
    private long ping;
    private int kills;
    private int deaths;
    private int assists;
    private int team;
    private boolean alive;
    private long lastSeenTick;
    private double groundY;

    public FpsPlayerState() {
    }

    public FpsPlayerState(String playerId, String playerName, int team) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.team = team;
        this.maxHealth = 100;
        this.health = 100;
        this.alive = true;
        this.ammo = 30;
        this.reserveAmmo = 120;
        this.groundY = 0.0;
    }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public double getPosX() { return posX; }
    public void setPosX(double posX) { this.posX = posX; }
    public double getPosY() { return posY; }
    public void setPosY(double posY) { this.posY = posY; }
    public double getPosZ() { return posZ; }
    public void setPosZ(double posZ) { this.posZ = posZ; }
    public void setPosition(double x, double y, double z) { this.posX = x; this.posY = y; this.posZ = z; }
    public double getPitch() { return pitch; }
    public void setPitch(double pitch) { this.pitch = pitch; }
    public double getYaw() { return yaw; }
    public void setYaw(double yaw) { this.yaw = yaw; }
    public double getVelX() { return velX; }
    public void setVelX(double velX) { this.velX = velX; }
    public double getVelY() { return velY; }
    public void setVelY(double velY) { this.velY = velY; }
    public double getVelZ() { return velZ; }
    public void setVelZ(double velZ) { this.velZ = velZ; }
    public void setVelocity(double x, double y, double z) { this.velX = x; this.velY = y; this.velZ = z; }
    public int getHealth() { return health; }
    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(maxHealth, health));
        if (this.health <= 0) { this.alive = false; }
    }
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }
    public int getAmmo() { return ammo; }
    public void setAmmo(int ammo) { this.ammo = ammo; }
    public int getReserveAmmo() { return reserveAmmo; }
    public void setReserveAmmo(int reserveAmmo) { this.reserveAmmo = reserveAmmo; }
    public String getCurrentWeaponId() { return currentWeaponId; }
    public void setCurrentWeaponId(String currentWeaponId) { this.currentWeaponId = currentWeaponId; }
    public double getLastFireTime() { return lastFireTime; }
    public void setLastFireTime(double lastFireTime) { this.lastFireTime = lastFireTime; }
    public double getLastReloadTime() { return lastReloadTime; }
    public void setLastReloadTime(double lastReloadTime) { this.lastReloadTime = lastReloadTime; }
    public boolean isReloading() { return isReloading; }
    public void setReloading(boolean reloading) { isReloading = reloading; }
    public boolean isSprinting() { return isSprinting; }
    public void setSprinting(boolean sprinting) { isSprinting = sprinting; }
    public boolean isCrouching() { return isCrouching; }
    public void setCrouching(boolean crouching) { isCrouching = crouching; }
    public boolean isJumping() { return isJumping; }
    public void setJumping(boolean jumping) { isJumping = jumping; }
    public long getPing() { return ping; }
    public void setPing(long ping) { this.ping = ping; }
    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    public void addKill() { this.kills++; }
    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    public void addDeath() { this.deaths++; }
    public int getAssists() { return assists; }
    public void setAssists(int assists) { this.assists = assists; }
    public int getTeam() { return team; }
    public void setTeam(int team) { this.team = team; }
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
    public long getLastSeenTick() { return lastSeenTick; }
    public void setLastSeenTick(long lastSeenTick) { this.lastSeenTick = lastSeenTick; }
    public double getGroundY() { return groundY; }
    public void setGroundY(double groundY) { this.groundY = groundY; }

    public void respawn(double x, double y, double z, FpsWeapon weapon) {
        this.setPosition(x, y, z);
        this.setVelocity(0, 0, 0);
        this.setPitch(0);
        this.setYaw(0);
        this.setHealth(maxHealth);
        this.setAmmo(weapon != null ? weapon.getMagSize() : 30);
        this.setReloading(false);
        this.setAlive(true);
        this.alive = true;
    }
}
