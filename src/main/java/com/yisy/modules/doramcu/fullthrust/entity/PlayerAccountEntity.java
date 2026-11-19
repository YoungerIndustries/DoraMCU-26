package com.yisy.modules.doramcu.fullthrust.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Persistent player account that survives across game sessions.
 * Stores player profile, stats, and battle record summaries.
 */
@Entity
@Table(name = "player_accounts")
public class PlayerAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    private String faction = "Independent";

    // Battle statistics
    @Column(name = "games_played")
    private int gamesPlayed = 0;

    @Column(name = "games_won")
    private int gamesWon = 0;

    @Column(name = "games_lost")
    private int gamesLost = 0;

    @Column(name = "ships_destroyed")
    private int shipsDestroyed = 0;

    @Column(name = "total_damage_dealt")
    private int totalDamageDealt = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // Google OAuth2 fields
    @Column(name = "google_email", unique = true, length = 100)
    private String googleEmail;

    @Column(name = "google_picture", length = 500)
    private String googlePicture;

    public PlayerAccountEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFaction() { return faction; }
    public void setFaction(String faction) { this.faction = faction; }

    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }

    public int getGamesWon() { return gamesWon; }
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }

    public int getGamesLost() { return gamesLost; }
    public void setGamesLost(int gamesLost) { this.gamesLost = gamesLost; }

    public int getShipsDestroyed() { return shipsDestroyed; }
    public void setShipsDestroyed(int shipsDestroyed) { this.shipsDestroyed = shipsDestroyed; }

    public int getTotalDamageDealt() { return totalDamageDealt; }
    public void setTotalDamageDealt(int totalDamageDealt) { this.totalDamageDealt = totalDamageDealt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public String getGoogleEmail() { return googleEmail; }
    public void setGoogleEmail(String googleEmail) { this.googleEmail = googleEmail; }

    public String getGooglePicture() { return googlePicture; }
    public void setGooglePicture(String googlePicture) { this.googlePicture = googlePicture; }

    /**
     * Calculate win rate as a percentage.
     */
    public double getWinRate() {
        if (gamesPlayed == 0) return 0.0;
        return (double) gamesWon / gamesPlayed * 100.0;
    }
}
