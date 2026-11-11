package com.yisy.modules.doramcu.star_trek_4x.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Links Captain's Log missions to 4x game events.
 * <p>
 * When a Captain's Log mission is completed, it can trigger 4x events such as
 * sector discovery, fleet deployment, or diplomatic changes. Conversely, 4x events
 * can trigger new Captain's Log missions.
 * </p>
 */
@Entity
@Table(name = "st4x_mission_updates")
public class MissionUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Captain's Log mission ID. */
    @Column
    private Long captainLogMissionId;

    /** Type of 4x event: SECTOR_DISCOVERED, COLONY_ESTABLISHED, FLEET_DEPLOYED, BATTLE_WON, TREATY_SIGNED. */
    @Column(nullable = false, length = 50)
    private String eventType;

    /** Description of the triggered action. */
    @Column(length = 500)
    private String actionDescription;

    /** Whether this update has been processed. */
    @Column(nullable = false)
    private boolean processed = false;

    /** Timestamp when this update was created. */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    public MissionUpdate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCaptainLogMissionId() { return captainLogMissionId; }
    public void setCaptainLogMissionId(Long captainLogMissionId) { this.captainLogMissionId = captainLogMissionId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getActionDescription() { return actionDescription; }
    public void setActionDescription(String actionDescription) { this.actionDescription = actionDescription; }

    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public GameSession getGameSession() { return gameSession; }
    public void setGameSession(GameSession gameSession) { this.gameSession = gameSession; }
}
