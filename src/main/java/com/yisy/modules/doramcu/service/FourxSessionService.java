package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.fourx.*;
import com.yisy.modules.doramcu.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing 4X game sessions.
 * <p>
 * Handles session creation, turn progression, faction management,
 * and unlock state tracking.
 * </p>
 */
@Service
public class FourxSessionService {

    private static final Logger log = LoggerFactory.getLogger(FourxSessionService.class);

    private final FourxSessionRepository sessionRepository;
    private final FourxFactionRepository factionRepository;
    private final FourxColonyRepository colonyRepository;
    private final FourxTradeRouteRepository tradeRouteRepository;
    private final FourxDiplomaticRelationRepository diplomaticRelationRepository;

    public FourxSessionService(FourxSessionRepository sessionRepository,
                               FourxFactionRepository factionRepository,
                               FourxColonyRepository colonyRepository,
                               FourxTradeRouteRepository tradeRouteRepository,
                               FourxDiplomaticRelationRepository diplomaticRelationRepository) {
        this.sessionRepository = sessionRepository;
        this.factionRepository = factionRepository;
        this.colonyRepository = colonyRepository;
        this.tradeRouteRepository = tradeRouteRepository;
        this.diplomaticRelationRepository = diplomaticRelationRepository;
    }

    /**
     * Create a new 4X game session.
     */
    @Transactional
    public FourxSessionEntity createSession(String name, String gameType, String difficultyLevel, Long createdByPlayerId) {
        String sessionId = UUID.randomUUID().toString().substring(0, 8);
        FourxSessionEntity session = new FourxSessionEntity(sessionId, name, createdByPlayerId);
        session.setGameType(gameType);
        session.setDifficultyLevel(difficultyLevel);
        log.info("Created 4X session: {} ({})", sessionId, name);
        return sessionRepository.save(session);
    }

    /**
     * Start a game session.
     */
    @Transactional
    public FourxSessionEntity startSession(String sessionId) {
        FourxSessionEntity session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Session not found: " + sessionId));
        session.start();
        return sessionRepository.save(session);
    }

    /**
     * End a game session.
     */
    @Transactional
    public FourxSessionEntity endSession(String sessionId) {
        FourxSessionEntity session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Session not found: " + sessionId));
        session.end();
        return sessionRepository.save(session);
    }

    /**
     * Advance to the next turn.
     */
    @Transactional
    public FourxSessionEntity advanceTurn(String sessionId) {
        FourxSessionEntity session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Session not found: " + sessionId));
        session.advanceTurn();
        log.info("Session {} advanced to turn {}", sessionId, session.getCurrentTurn());
        return sessionRepository.save(session);
    }

    /**
     * Add a faction to a session.
     */
    @Transactional
    public FourxFactionEntity addFaction(String sessionId, String factionId, String name, String gameModule) {
        FourxSessionEntity session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Session not found: " + sessionId));
        FourxFactionEntity faction = new FourxFactionEntity(factionId, name, gameModule);
        faction.setSession(session);
        log.info("Added faction {} to session {}", name, sessionId);
        return factionRepository.save(faction);
    }

    /**
     * Get all sessions.
     */
    @Transactional(readOnly = true)
    public List<FourxSessionEntity> getAllSessions() {
        return sessionRepository.findAll();
    }

    /**
     * Get active sessions.
     */
    @Transactional(readOnly = true)
    public List<FourxSessionEntity> getActiveSessions() {
        return sessionRepository.findByStatus("ACTIVE");
    }

    /**
     * Get a session by ID.
     */
    @Transactional(readOnly = true)
    public Optional<FourxSessionEntity> getSession(String sessionId) {
        return sessionRepository.findBySessionId(sessionId);
    }

    /**
     * Get all factions for a session.
     */
    @Transactional(readOnly = true)
    public List<FourxFaction> getSessionFactions(String sessionId) {
        return factionRepository.findBySession_SessionId(sessionId).stream()
                .map(FourxFactionEntity::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get unlock state for a session.
     */
    @Transactional(readOnly = true)
    public FourxUnlockState getUnlockState(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
                .map(FourxSessionEntity::getUnlockState)
                .orElseGet(() -> {
                    FourxUnlockState state = new FourxUnlockState();
                    state.setExplorationUnlocked(true);
                    return state;
                });
    }

    /**
     * Unlock a layer for a session.
     */
    @Transactional
    public FourxSessionEntity unlockLayer(String sessionId, String layer) {
        FourxSessionEntity session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Session not found: " + sessionId));
        switch (layer.toLowerCase()) {
            case "exploration": session.setExplorationUnlocked(true); break;
            case "expansion": session.setExpansionUnlocked(true); break;
            case "exploitation": session.setExploitationUnlocked(true); break;
            case "extermination": session.setExterminationUnlocked(true); break;
            case "diplomacy": session.setDiplomacyUnlocked(true); break;
            default: throw new IllegalArgumentException("Unknown layer: " + layer);
        }
        log.info("Unlocked {} layer for session {}", layer, sessionId);
        return sessionRepository.save(session);
    }

    /**
     * Get session summary as a map.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSessionSummary(String sessionId) {
        FourxSessionEntity session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Session not found: " + sessionId));
        Map<String, Object> summary = new HashMap<>();
        summary.put("sessionId", session.getSessionId());
        summary.put("name", session.getName());
        summary.put("status", session.getStatus());
        summary.put("currentTurn", session.getCurrentTurn());
        summary.put("gameType", session.getGameType());
        summary.put("difficultyLevel", session.getDifficultyLevel());
        summary.put("factionCount", factionRepository.findBySession_SessionId(sessionId).size());
        summary.put("colonyCount", colonyRepository.findBySession_SessionId(sessionId).size());
        summary.put("tradeRouteCount", tradeRouteRepository.findBySession_SessionId(sessionId).size());
        summary.put("unlockState", session.getUnlockState().toMap());
        summary.put("createdAt", session.getCreatedAt());
        return summary;
    }

    /**
     * Delete a session and all its related data.
     */
    @Transactional
    public void deleteSession(String sessionId) {
        sessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            log.info("Deleting session {} and all related data", sessionId);
            sessionRepository.delete(session);
        });
    }
}
