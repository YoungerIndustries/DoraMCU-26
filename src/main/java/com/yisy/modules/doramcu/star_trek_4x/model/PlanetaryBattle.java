package com.yisy.modules.doramcu.star_trek_4x.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Records a ground battle on a planet.
 * <p>
 * Links to the Third Reich battle system via thirdReichBattleId and maps
 * battle outcomes to 4x consequences (system captured, invasion repelled, etc.).
 * </p>
 */
@Entity
@Table(name = "st4x_planetary_battles")
public class PlanetaryBattle {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Star system where the battle takes place. */
    @Column(nullable = false)
    private Long starSystemId;

    /** Attacker faction ID. */
    @Column(nullable = false)
    private Long attackerFactionId;

    /** Defender faction ID. */
    @Column(nullable = false)
    private Long defenderFactionId;

    /** Linked Third Reich battle ID. */
    @Column(length = 50)
    private String thirdReichBattleId;

    /** Battle outcome: ATTACKER_VICTORY, DEFENDER_VICTORY, DRAW, IN_PROGRESS. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BattleOutcome outcome = BattleOutcome.IN_PROGRESS;

    /** Attacker casualties. */
    @Column(nullable = false)
    private int attackerCasualties = 0;

    /** Defender casualties. */
    @Column(nullable = false)
    private int defenderCasualties = 0;

    /** Rounds fought. */
    @Column(nullable = false)
    private int roundsFought = 0;

    /** Narrative summary of the battle. */
    @Column(length = 2000)
    private String narrativeSummary;

    /** Battle events log. */
    @ElementCollection
    @CollectionTable(name = "st4x_battle_events", joinColumns = @JoinColumn(name = "battle_id"))
    @Column(name = "event_description")
    private List<String> events = new ArrayList<>();

    /** Timestamp when the battle started. */
    @Column(nullable = false)
    private LocalDateTime startedAt;

    /** Timestamp when the battle ended. */
    @Column
    private LocalDateTime endedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    public PlanetaryBattle() {
        this.startedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getStarSystemId() { return starSystemId; }
    public void setStarSystemId(Long starSystemId) { this.starSystemId = starSystemId; }

    public Long getAttackerFactionId() { return attackerFactionId; }
    public void setAttackerFactionId(Long attackerFactionId) { this.attackerFactionId = attackerFactionId; }

    public Long getDefenderFactionId() { return defenderFactionId; }
    public void setDefenderFactionId(Long defenderFactionId) { this.defenderFactionId = defenderFactionId; }

    public String getThirdReichBattleId() { return thirdReichBattleId; }
    public void setThirdReichBattleId(String thirdReichBattleId) { this.thirdReichBattleId = thirdReichBattleId; }

    public BattleOutcome getOutcome() { return outcome; }
    public void setOutcome(BattleOutcome outcome) { this.outcome = outcome; }

    public int getAttackerCasualties() { return attackerCasualties; }
    public void setAttackerCasualties(int attackerCasualties) { this.attackerCasualties = attackerCasualties; }

    public int getDefenderCasualties() { return defenderCasualties; }
    public void setDefenderCasualties(int defenderCasualties) { this.defenderCasualties = defenderCasualties; }

    public int getRoundsFought() { return roundsFought; }
    public void setRoundsFought(int roundsFought) { this.roundsFought = roundsFought; }

    public String getNarrativeSummary() { return narrativeSummary; }
    public void setNarrativeSummary(String narrativeSummary) { this.narrativeSummary = narrativeSummary; }

    public List<String> getEvents() { return events; }
    public void setEvents(List<String> events) { this.events = events; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }

    public GameSession getGameSession() { return gameSession; }
    public void setGameSession(GameSession gameSession) { this.gameSession = gameSession; }

    /**
     * Add a battle event.
     */
    public void addEvent(String event) {
        events.add(event);
    }

    /**
     * Mark the battle as ended.
     */
    public void endBattle() {
        this.endedAt = LocalDateTime.now();
    }

    public enum BattleOutcome {
        IN_PROGRESS,
        ATTACKER_VICTORY,
        DEFENDER_VICTORY,
        DRAW
    }
}
