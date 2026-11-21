package com.yisy.modules.doramcu.fullthrust.entity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Records the result of a single battle (game session).
 * Links players to their performance in a specific game.
 */
@Entity
@Table(name = "battle_records")
public class BattleRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_session_id", length = 36, nullable = false)
    private String gameSessionId;

    @Column(name = "game_name", length = 100)
    private String gameName;

    @Column(name = "player_id", length = 36, nullable = false)
    private String playerId;

    @Column(name = "player_name", length = 50, nullable = false)
    private String playerName;

    @Column(name = "player_account_id")
    private Long playerAccountId;

    private String faction;

    private boolean isAI = false;

    private String aiDifficulty;

    // Battle results
    private boolean won;

    @Column(name = "ships_deployed")
    private int shipsDeployed = 0;

    @Column(name = "ships_lost")
    private int shipsLost = 0;

    @Column(name = "ships_destroyed")
    private int shipsDestroyed = 0;

    @Column(name = "damage_dealt")
    private int damageDealt = 0;

    @Column(name = "damage_received")
    private int damageReceived = 0;

    @Column(name = "turns_survived")
    private int turnsSurvived = 0;

    @Column(name = "final_hull")
    private int finalHull = 0;

    @Column(name = "final_shield")
    private int finalShield = 0;

    @Column(name = "final_fuel")
    private int finalFuel = 0;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public BattleRecordEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGameSessionId() { return gameSessionId; }
    public void setGameSessionId(String gameSessionId) { this.gameSessionId = gameSessionId; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public Long getPlayerAccountId() { return playerAccountId; }
    public void setPlayerAccountId(Long playerAccountId) { this.playerAccountId = playerAccountId; }

    public String getFaction() { return faction; }
    public void setFaction(String faction) { this.faction = faction; }

    public boolean isAI() { return isAI; }
    public void setAI(boolean isAI) { this.isAI = isAI; }

    public String getAiDifficulty() { return aiDifficulty; }
    public void setAiDifficulty(String aiDifficulty) { this.aiDifficulty = aiDifficulty; }

    public boolean isWon() { return won; }
    public void setWon(boolean won) { this.won = won; }

    public int getShipsDeployed() { return shipsDeployed; }
    public void setShipsDeployed(int shipsDeployed) { this.shipsDeployed = shipsDeployed; }

    public int getShipsLost() { return shipsLost; }
    public void setShipsLost(int shipsLost) { this.shipsLost = shipsLost; }

    public int getShipsDestroyed() { return shipsDestroyed; }
    public void setShipsDestroyed(int shipsDestroyed) { this.shipsDestroyed = shipsDestroyed; }

    public int getDamageDealt() { return damageDealt; }
    public void setDamageDealt(int damageDealt) { this.damageDealt = damageDealt; }

    public int getDamageReceived() { return damageReceived; }
    public void setDamageReceived(int damageReceived) { this.damageReceived = damageReceived; }

    public int getTurnsSurvived() { return turnsSurvived; }
    public void setTurnsSurvived(int turnsSurvived) { this.turnsSurvived = turnsSurvived; }

    public int getFinalHull() { return finalHull; }
    public void setFinalHull(int finalHull) { this.finalHull = finalHull; }

    public int getFinalShield() { return finalShield; }
    public void setFinalShield(int finalShield) { this.finalShield = finalShield; }

    public int getFinalFuel() { return finalFuel; }
    public void setFinalFuel(int finalFuel) { this.finalFuel = finalFuel; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
