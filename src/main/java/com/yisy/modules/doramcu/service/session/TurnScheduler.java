package com.yisy.modules.doramcu.service.session;
import com.yisy.modules.doramcu.hex.Hex;
import com.yisy.modules.doramcu.controller.admin.AdminController;
import com.yisy.modules.doramcu.service.ai.AiPlayerService;
import com.yisy.modules.doramcu.dto.session.GameStateResponse;
import com.yisy.modules.doramcu.dto.players.PlayerSummary;
import com.yisy.modules.doramcu.dto.galaxy.SubSectorInfo;

import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.ai.framework.AiLearningService;
import com.yisy.modules.doramcu.corporation.service.AiAssistantService;
import com.yisy.modules.doramcu.corporation.service.CorporateRoleService;
import com.yisy.modules.doramcu.corporation.service.CorporationEventService;
import com.yisy.modules.doramcu.corporation.service.TreasuryLoanService;
import com.yisy.modules.doramcu.dto.session.GameEvent;
import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.model.misc.CorporateRole;
import com.yisy.modules.doramcu.model.session.GameSession;
import com.yisy.modules.doramcu.repository.misc.EveCorporationRepository;
import com.yisy.modules.doramcu.unified.GameEngine;
import com.yisy.modules.doramcu.unified.GlobalClock;
import com.yisy.modules.doramcu.unified.GlobalClockService;
import com.yisy.modules.doradl.dl.service.DoraAiLearningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages time-based automatic turn advancement for active game sessions.
 * <p>
 * Advances game time by 1 second on each tick. When accumulated time reaches
 * the configured turn duration, a full turn is processed for the session.
 * The scheduler can be enabled/disabled and the turn interval can be changed
 * at runtime via the AdminController.
 * </p>
 */
@Service
public class TurnScheduler {

    private static final Logger log = LoggerFactory.getLogger(TurnScheduler.class);

    /** Default turn interval: 30 seconds per turn (one in-game day). */
    private static final long DEFAULT_TURN_INTERVAL_SECONDS = 30;

    /** Whether the scheduler is currently running. */
    private volatile boolean enabled = true;

    /** Turn interval in seconds (configurable at runtime). */
    private volatile long turnIntervalSeconds = DEFAULT_TURN_INTERVAL_SECONDS;

    /** Accumulated seconds since last turn advance per session. */
    private final Map<String, Long> accumulatedSeconds = new ConcurrentHashMap<>();

    /** Thread pool for parallel turn processing across sessions. */
    private final ExecutorService turnExecutor;

    /** Maximum concurrent turn processing threads. */
    @Value("${doramcu.turn.parallel.max-threads:10}")
    public void setMaxTurnThreads(int maxThreads) {
        // Thread pool is initialized with the configured value
        log.debug("Turn scheduler parallel processing configured with max {} threads", maxThreads);
    }

    private final GameSessionService gameSessionService;
    private final AiPlayerService aiPlayerService;
    private final DoraAiLearningService doraAiLearningService;
    private final AiLearningService aiLearningService;
    private final boolean isDlEnabled;
    private final GlobalClockService globalClockService;
    private final AiAssistantService aiAssistantService;
    private final CorporateRoleService corporateRoleService;
    private final CorporationEventService corporationEventService;
    private final EveCorporationRepository eveCorporationRepository;
    private final TreasuryLoanService treasuryLoanService;

    @Value("${doramcu.corporation.management.enabled:true}")
    private boolean corporationManagementEnabled = true;

    @Value("${doramcu.turn.interval.seconds:30}")
    public void setDefaultTurnInterval(long interval) {
        this.turnIntervalSeconds = interval;
        log.info("Turn scheduler initialized with interval: {} seconds (one in-game day per turn)", interval);
    }

    @Autowired
    public TurnScheduler(GameSessionService gameSessionService,
                          @Lazy AiPlayerService aiPlayerService,
                          @Autowired(required = false) DoraAiLearningService doraAiLearningService,
                          @Autowired(required = false) AiLearningService aiLearningService,
                          GlobalClockService globalClockService,
                          @Lazy AiAssistantService aiAssistantService,
                          CorporateRoleService corporateRoleService,
                          CorporationEventService corporationEventService,
                          EveCorporationRepository eveCorporationRepository,
                          TreasuryLoanService treasuryLoanService) {
        this.gameSessionService = gameSessionService;
        this.aiPlayerService = aiPlayerService;
        this.doraAiLearningService = doraAiLearningService;
        this.aiLearningService = aiLearningService;
        this.globalClockService = globalClockService;
        this.aiAssistantService = aiAssistantService;
        this.corporateRoleService = corporateRoleService;
        this.corporationEventService = corporationEventService;
        this.eveCorporationRepository = eveCorporationRepository;
        this.isDlEnabled = (doraAiLearningService != null);
        this.treasuryLoanService = treasuryLoanService;
        // Initialize thread pool for parallel turn processing
        this.turnExecutor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                r -> {
                    Thread t = new Thread(r, "turn-processor-");
                    t.setDaemon(true);
                    return t;
                }
        );
        log.info("Turn scheduler initialized with parallel processing pool ({} threads)",
                Runtime.getRuntime().availableProcessors());
    }

    /**
     * Scheduled tick: runs every 1 second to advance game time.
     * When accumulated time reaches the turn interval, processes a full turn.
     * <p>
     * Uses parallel processing to handle multiple sessions concurrently,
     * eliminating the sequential bottleneck where one slow session blocks others.
     * Each session still uses its own write lock for thread safety.
     * </p>
     */
    @Scheduled(fixedRate = 1000)
    public void tick() {
        if (!enabled) {
            return;
        }

        // Collect sessions that need turn processing
        List<Map.Entry<String, GameSession>> sessionsToProcess = new ArrayList<>();

        for (Map.Entry<String, GameSession> entry : gameSessionService.getAllSessions().entrySet()) {
            String sessionId = entry.getKey();
            GameSession session = entry.getValue();
            GameEngine game = session.getGame();

            // Skip sessions with no players
            if (game.getPlayers().isEmpty()) {
                continue;
            }

            accumulatedSeconds.merge(sessionId, 1L, Long::sum);
            long accumulated = accumulatedSeconds.getOrDefault(sessionId, 0L);

            if (accumulated >= turnIntervalSeconds) {
                // Calculate how many full turns to advance
                long turnsToAdvance = accumulated / turnIntervalSeconds;
                accumulatedSeconds.put(sessionId, accumulated % turnIntervalSeconds);

                if (turnsToAdvance > 0) {
                    sessionsToProcess.add(entry);
                }
            }
        }

        // Process sessions in parallel using the thread pool
        if (sessionsToProcess.isEmpty()) {
            return;
        }

        log.debug("Processing {} sessions in parallel", sessionsToProcess.size());
        AtomicInteger processed = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        sessionsToProcess.parallelStream().forEach(entry -> {
            String sessionId = entry.getKey();
            GameSession session = entry.getValue();
            GameEngine game = session.getGame();

            long turnsToAdvance = accumulatedSeconds.getOrDefault(sessionId, 0L);
            // Recalculate from the accumulated value we already computed
            long accumulated = accumulatedSeconds.getOrDefault(sessionId, 0L);
            if (accumulated == 0) {
                // Already processed, recalculate turns
                return;
            }

            try {
                session.getLock().writeLock().lock();
                try {
                    game.nextTurn();
                    // Advance global clock by one turn (one in-game day)
                    GlobalClock clock = session.getGlobalClock();
                    if (clock != null) {
                        clock.advanceTurn();
                    }
                    // Simultaneous turns: all players act concurrently, no player cycling
                } finally {
                    session.getLock().writeLock().unlock();
                }

                log.debug("Auto-advanced turn {} in session {} (simultaneous turns)",
                        game.getCurrentTurn(), sessionId);

                // Execute AI player actions for this turn
                executeAiActionsForSession(sessionId, session);

                // Execute corporation management for VP mode players
                executeCorporationManagement(sessionId, session);

                // Broadcast turn change event - all players can act simultaneously
                gameSessionService.broadcastEvent(sessionId,
                        new com.yisy.modules.doramcu.dto.session.GameEvent(
                                com.yisy.modules.doramcu.dto.session.GameEvent.EventType.TURN_CHANGED,
                                sessionId,
                                Map.of(
                                        "turn", game.getCurrentTurn(),
                                        "autoAdvanced", true
                                )));
                processed.incrementAndGet();
            } catch (Exception e) {
                log.warn("Failed to auto-advance turn in session: {}", sessionId, e);
                failed.incrementAndGet();
            }
        });

        if (processed.get() > 0 || failed.get() > 0) {
            log.debug("Parallel turn processing complete: {} processed, {} failed",
                    processed.get(), failed.get());
        }
    }

    /**
     * Returns whether the scheduler is currently enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables the scheduler.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        log.info("Turn scheduler {}d", enabled ? "enab" : "disab");
    }

    /**
     * Returns the current turn interval in seconds.
     */
    public long getTurnIntervalSeconds() {
        return turnIntervalSeconds;
    }

    /**
     * Sets the turn interval in seconds.
     *
     * @param seconds the new turn interval (must be >= 1)
     */
    public void setTurnIntervalSeconds(long seconds) {
        if (seconds < 1) {
            throw new IllegalArgumentException("Turn interval must be at least 1 second");
        }
        this.turnIntervalSeconds = seconds;
        // Reset accumulated time when interval changes
        accumulatedSeconds.replaceAll((k, v) -> 0L);
        log.info("Turn interval changed to {} seconds", seconds);
    }

    /**
     * Returns the accumulated seconds for a session since last turn.
     */
    public long getAccumulatedSeconds(String sessionId) {
        return accumulatedSeconds.getOrDefault(sessionId, 0L);
    }

    /**
     * Executes AI player actions for all registered AI players in a session.
     * <p>
     * After each turn advance, this method triggers all AI players to take actions.
     * Each AI player's action is recorded with the Dora AI learning service so the
     * neural network can learn from the outcomes.
     * </p>
     * <p>
     * The AiLearningService framework interface (via DlGameAiLearningAdapter) is available
     * for unified learning operations; the direct DoraAiLearningService calls can be
     * migrated to use aiLearningService when the interface is extended with computeReward.
     * </p>
     */
    private void executeAiActionsForSession(String sessionId, GameSession session) {
        Map<Long, String> aiPlayerDifficulties = session.getAiPlayerDifficulties();
        if (aiPlayerDifficulties.isEmpty()) {
            return;
        }

        log.debug("Executing AI actions for {} AI players in session: {}",
                aiPlayerDifficulties.size(), sessionId);

        for (Map.Entry<Long, String> entry : aiPlayerDifficulties.entrySet()) {
            Long aiPlayerId = entry.getKey();
            String difficultyName = entry.getValue();

            try {
                AiPlayerService.Difficulty difficulty = AiPlayerService.Difficulty.valueOf(difficultyName);
                Map<Long, AiPlayerService.Difficulty> aiMap = Map.of(aiPlayerId, difficulty);

                // Capture game state before AI action for learning (summary mode to avoid full subsector iteration)
                com.yisy.modules.doramcu.dto.session.GameStateResponse stateBefore =
                        gameSessionService.getGameState(aiPlayerId, sessionId, true);

                // Execute AI turn
                aiPlayerService.executeAiTurn(sessionId, aiMap);

                // Capture game state after AI action for learning (summary mode to avoid full subsector iteration)
                com.yisy.modules.doramcu.dto.session.GameStateResponse stateAfter =
                        gameSessionService.getGameState(aiPlayerId, sessionId, true);

                // Compute reward and record for learning (skip if DL is disabled)
                if (isDlEnabled) {
                    double reward = doraAiLearningService.computeReward(
                            convertToDlGameState(stateBefore),
                            convertToDlGameState(stateAfter),
                            aiPlayerId);
                    log.debug("AI player {} (difficulty: {}) action completed with reward: {}",
                            aiPlayerId, difficulty, reward);
                }

            } catch (Exception e) {
                log.warn("AI action failed for player {} in session {}: {}",
                        aiPlayerId, sessionId, e.getMessage());
            }
        }
    }

    /**
     * Converts main module GameStateResponse to DL module GameStateResponse.
     */
    private com.yisy.modules.doradl.dto.GameStateResponse convertToDlGameState(
            com.yisy.modules.doramcu.dto.session.GameStateResponse source) {
        if (source == null) {
            return null;
        }
        com.yisy.modules.doradl.dto.GameStateResponse target = new com.yisy.modules.doradl.dto.GameStateResponse();
        target.setSessionId(source.getSessionId());
        target.setCurrentTurn(source.getCurrentTurn());
        target.setCurrentPlayerId(source.getCurrentPlayerId());
        target.setCurrentPlayerName(source.getCurrentPlayerName());
        target.setMyTurn(source.isMyTurn());

        // Convert players
        java.util.List<com.yisy.modules.doradl.dto.PlayerSummary> dlPlayers = new java.util.ArrayList<>();
        if (source.getPlayers() != null) {
            for (com.yisy.modules.doramcu.dto.players.PlayerSummary player : source.getPlayers()) {
                com.yisy.modules.doradl.dto.PlayerSummary dlPlayer = new com.yisy.modules.doradl.dto.PlayerSummary();
                dlPlayer.setId(player.getId());
                dlPlayer.setName(player.getName());
                dlPlayer.setResources(player.getResources());
                dlPlayer.setPlanetCount(player.getPlanetCount());
                dlPlayer.setFleetCount(player.getFleetCount());
                dlPlayer.setAi(player.isAi());
                dlPlayer.setDifficulty(player.getDifficulty());
                dlPlayers.add(dlPlayer);
            }
        }
        target.setPlayers(dlPlayers);

        // Convert subSectors
        java.util.List<com.yisy.modules.doradl.dto.SubSectorInfo> dlSubSectors = new java.util.ArrayList<>();
        if (source.getSubSectors() != null) {
            for (com.yisy.modules.doramcu.dto.galaxy.SubSectorInfo sub : source.getSubSectors()) {
                com.yisy.modules.doradl.dto.SubSectorInfo dlSub = new com.yisy.modules.doradl.dto.SubSectorInfo();
                dlSub.setId(sub.getId());
                dlSub.setName(sub.getName());
                dlSub.setSectorRow(sub.getSectorRow());
                dlSub.setSectorCol(sub.getSectorCol());
                dlSub.setSubRow(sub.getSubRow());
                dlSub.setSubCol(sub.getSubCol());
                dlSub.setGenerated(sub.isGenerated());
                dlSub.setSystemCount(sub.getSystemCount());
                dlSub.setSystems(sub.getSystems());
                dlSubSectors.add(dlSub);
            }
        }
        target.setSubSectors(dlSubSectors);

        return target;
    }

    /**
     * Executes corporation management for all VP mode players in a session.
     * <p>
     * After each turn advance, this method checks all players in the session.
     * If a player is in VP mode, it runs the AI assistant management cycle for
     * their corporation, then broadcasts any corporation events.
     * </p>
     */
    private void executeCorporationManagement(String sessionId, GameSession session) {
        if (!corporationManagementEnabled) {
            return;
        }

        // Get AI player IDs to skip them â€” only human players have corporations
        Map<Long, String> aiPlayerDifficulties = session.getAiPlayerDifficulties();

        GameEngine game = session.getGame();
        java.util.Collection<?> playersCollection = game.getPlayers();

        for (Object obj : playersCollection) {
            if (!(obj instanceof com.yisy.modules.doramcu.hex.Player)) {
                continue;
            }
            com.yisy.modules.doramcu.hex.Player hexPlayer = (com.yisy.modules.doramcu.hex.Player) obj;
            Long playerId = Long.valueOf(hexPlayer.getId());

            // Skip AI players â€” only human players have corporations
            if (aiPlayerDifficulties.containsKey(playerId)) {
                continue;
            }
            try {
                // Check if player is in VP mode
                Optional<CorporateRole> roleOpt = corporateRoleService.getPlayerRole(playerId);
                if (roleOpt.isEmpty() || roleOpt.get() != CorporateRole.VP) {
                    continue;
                }

                // Find the player's corporation
                Optional<EveCorporation> corpOpt = eveCorporationRepository.findByCeoPlayerId(String.valueOf(playerId));
                if (corpOpt.isEmpty()) {
                    log.debug("No corporation found for player {} in session {}", playerId, sessionId);
                    continue;
                }

                Long corporationId = corpOpt.get().getId();
                log.debug("Running corporation management for player {} (corp {}) in session {}",
                        playerId, corporationId, sessionId);

                // Run AI management cycle
                aiAssistantService.runManagementCycle(playerId, corporationId);

                // Check and broadcast corporation events
                corporationEventService.checkAndBroadcastAll(corporationId, sessionId);

            } catch (Exception e) {
                log.warn("Corporation management failed for player {} in session {}: {}",
                        playerId, sessionId, e.getMessage());
            }
        }

        // Process treasury loan payments for all active loans
        try {
            int paymentsProcessed = treasuryLoanService.processAllDuePayments();
            if (paymentsProcessed > 0) {
                log.info("Turn scheduler processed {} treasury loan payments", paymentsProcessed);
            }
        } catch (Exception e) {
            log.warn("Treasury loan payment processing failed: {}", e.getMessage());
        }
    }
}
