package com.yisy.modules.doramcu.corporation.model;
import com.yisy.modules.doramcu.model.players.Player;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity that logs AI assistant decisions for auditability.
 * <p>
 * Every action taken by the AI assistant during autonomous corporation management
 * is recorded here so the player can review what the AI did, why it did it,
 * and what personality traits influenced the decision.
 * </p>
 */
@Entity
@Table(name = "ai_assistant_actions", indexes = {
        @Index(name = "idx_ai_action_player", columnList = "playerId"),
        @Index(name = "idx_ai_action_type", columnList = "actionType"),
        @Index(name = "idx_ai_action_timestamp", columnList = "timestamp")
})
public class AiAssistantAction {

    /**
     * Type of action taken by the AI assistant.
     */
    public enum ActionType {
        PROCESS_CONTRACT,  // AI processed a contract turn
        ACCEPT_CONTRACT,   // AI accepted a new contract from the pool
        HIRE_CREW,         // AI hired a new crew member
        FIRE_CREW,         // AI fired an underperforming crew member
        SKIP_TURN          // AI skipped a turn (no action taken)
    }

    /**
     * Result of the AI action.
     */
    public enum Result {
        SUCCESS,   // Action completed successfully
        FAILED,    // Action failed (e.g., not enough credits)
        SKIPPED    // Action was skipped (e.g., no contracts available)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Reference to the human player whose AI assistant took this action. */
    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActionType actionType;

    /** Target of the action — contractId or hiringId depending on action type. */
    @Column(name = "target_id")
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Result result;

    @Column(length = 1000)
    private String description;

    /** JSON snapshot of personality traits used for this decision. */
    @Column(name = "personality_snapshot", length = 2000)
    private String personalitySnapshot;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    public AiAssistantAction() {
        this.timestamp = LocalDateTime.now();
    }

    public AiAssistantAction(Long playerId, ActionType actionType, Long targetId, Result result, String description, String personalitySnapshot) {
        this.playerId = playerId;
        this.actionType = actionType;
        this.targetId = targetId;
        this.result = result;
        this.description = description;
        this.personalitySnapshot = personalitySnapshot;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPersonalitySnapshot() {
        return personalitySnapshot;
    }

    public void setPersonalitySnapshot(String personalitySnapshot) {
        this.personalitySnapshot = personalitySnapshot;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "AiAssistantAction{" +
                "id=" + id +
                ", playerId=" + playerId +
                ", actionType=" + actionType +
                ", targetId=" + targetId +
                ", result=" + result +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
