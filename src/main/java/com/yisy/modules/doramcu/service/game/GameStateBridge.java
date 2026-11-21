package com.yisy.modules.doramcu.service.game;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.service.battle.BattleService;
import com.yisy.modules.doramcu.service.session.GameSessionService;
import com.yisy.modules.doramcu.service.jump_travel.JumpTravelService;
import com.yisy.modules.doramcu.service.trade.TradeService;
import com.yisy.modules.doramcu.service.wormhole.WormholeConnectionService;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.dto.battle.BattleSummaryDto;
import com.yisy.modules.doramcu.dto.fleet.FleetDto;
import com.yisy.modules.doramcu.dto.session.GameStateResponse;
import com.yisy.modules.doramcu.dto.players.PlayerSummary;
import com.yisy.modules.doramcu.dto.misc.ResourcePipelineSummary;
import com.yisy.modules.doramcu.dto.wormhole.WormholeConnectionDto;
import com.yisy.modules.doramcu.dto.wormhole.WormholeInfoDto;
import com.yisy.modules.doramcu.model.session.GameSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Bridges real game state from backend services to the 3D WebSocket handlers.
 * <p>
 * Provides methods for converting real game data (fleets, players, battles,
 * pipelines, trade routes, wormholes) into WebSocket message format that the
 * frontend 3D visualization modules can consume directly.
 * </p>
 * <p>
 * This service enables graceful degradation: when real game sessions are
 * unavailable, the WebSocket handlers fall back to simulated data.
 * </p>
 */
@Service
public class GameStateBridge {

    private static final Logger log = LoggerFactory.getLogger(GameStateBridge.class);

    private final GameSessionService gameSessionService;
    private final BattleService battleService;
    private final TradeService tradeService;
    private final JumpTravelService jumpTravelService;
    private final WormholeConnectionService wormholeConnectionService;
    private final ObjectMapper objectMapper;

    public GameStateBridge(GameSessionService gameSessionService,
                           BattleService battleService,
                           TradeService tradeService,
                           JumpTravelService jumpTravelService,
                           WormholeConnectionService wormholeConnectionService) {
        this.gameSessionService = gameSessionService;
        this.battleService = battleService;
        this.tradeService = tradeService;
        this.jumpTravelService = jumpTravelService;
        this.wormholeConnectionService = wormholeConnectionService;
        this.objectMapper = new ObjectMapper();
    }

    // ==================== Fleet Data ====================

    /**
     * Get real fleet data for a game session as a WebSocket message.
     *
     * @param sessionId the game session ID
     * @return fleet_update message with real fleet positions, or null if session not found
     */
    public ObjectNode getFleetSnapshot(String sessionId) {
        GameSession session = gameSessionService.getSession(sessionId);
        if (session == null) {
            log.debug("No game session found for fleet snapshot: {}", sessionId);
            return null;
        }

        try {
            GameStateResponse state = gameSessionService.getGameState(session.getPlayerIds().stream()
                    .findFirst().orElse(1L), sessionId);
            if (state == null || state.getFleets() == null) {
                return null;
            }

            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "fleet_update");
            message.put("sessionId", sessionId);

            ArrayNode fleetsArray = message.putArray("fleets");
            for (FleetDto fleet : state.getFleets()) {
                ObjectNode fleetNode = fleetsArray.addObject();
                fleetNode.put("fleetId", String.valueOf(fleet.getId()));
                fleetNode.put("name", fleet.getName());
                fleetNode.put("x", fleet.getPositionX());
                fleetNode.put("y", fleet.getPositionY());
                fleetNode.put("z", fleet.getPositionZ());
                fleetNode.put("subSectorRow", fleet.getSubSectorRow());
                fleetNode.put("subSectorCol", fleet.getSubSectorCol());
                fleetNode.put("systemId", fleet.getSystemId() != null ? fleet.getSystemId() : 0);
                fleetNode.put("health", fleet.getHealthRatio());
                fleetNode.put("maxHealth", 1.0);
                fleetNode.put("shipCount", fleet.getShipCount());
                fleetNode.put("shieldPoints", fleet.getCurrentShieldPoints());
                fleetNode.put("totalShieldPoints", fleet.getTotalShieldPoints());
                fleetNode.put("hullPoints", fleet.getCurrentHullPoints());
                fleetNode.put("totalHullPoints", fleet.getTotalHullPoints());
                fleetNode.put("mDriveSpeed", fleet.getMDriveSpeed());
                fleetNode.put("jDriveSpeed", fleet.getJDriveSpeed());
                fleetNode.put("timestamp", System.currentTimeMillis());
            }

            return message;
        } catch (Exception e) {
            log.warn("Failed to get fleet snapshot for session {}: {}", sessionId, e.getMessage());
            return null;
        }
    }

    /**
     * Get fleet count across all active sessions.
     */
    public int getTotalFleetCount() {
        int count = 0;
        for (GameSession session : listSessionsAsObjects()) {
            try {
                Long firstPlayer = session.getPlayerIds().stream().findFirst().orElse(null);
                if (firstPlayer == null) continue;
                GameStateResponse state = gameSessionService.getGameState(firstPlayer, session.getSessionId());
                if (state != null && state.getFleets() != null) {
                    count += state.getFleets().size();
                }
            } catch (Exception e) {
                // Skip sessions that fail
            }
        }
        return count;
    }

    // ==================== Player Data ====================

    /**
     * Get real player data for a game session as a WebSocket message.
     *
     * @param sessionId the game session ID
     * @return player_list message with real player data, or null if session not found
     */
    public ObjectNode getPlayerSnapshot(String sessionId) {
        GameSession session = gameSessionService.getSession(sessionId);
        if (session == null) {
            log.debug("No game session found for player snapshot: {}", sessionId);
            return null;
        }

        try {
            GameStateResponse state = gameSessionService.getGameState(session.getPlayerIds().stream()
                    .findFirst().orElse(1L), sessionId);
            if (state == null || state.getPlayers() == null) {
                return null;
            }

            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "player_list");
            message.put("sessionId", sessionId);

            ArrayNode playersArray = message.putArray("players");
            for (PlayerSummary player : state.getPlayers()) {
                ObjectNode playerNode = playersArray.addObject();
                playerNode.put("playerId", String.valueOf(player.getId()));
                playerNode.put("name", player.getName());
                playerNode.put("isAi", player.isAi());
                playerNode.put("fleetCount", player.getFleetCount());
                playerNode.put("planetCount", player.getPlanetCount());
                playerNode.put("isOnline", true);
                playerNode.put("timestamp", System.currentTimeMillis());
            }

            return message;
        } catch (Exception e) {
            log.warn("Failed to get player snapshot for session {}: {}", sessionId, e.getMessage());
            return null;
        }
    }

    /**
     * Get player count across all active sessions.
     */
    public int getTotalPlayerCount() {
        int count = 0;
        for (GameSession session : listSessionsAsObjects()) {
            count += session.getPlayerIds().size();
        }
        return count;
    }

    // ==================== Battle Data ====================

    /**
     * Get real battle data for a game session as a WebSocket message.
     *
     * @param sessionId the game session ID
     * @return fleet_combat message with real battle data, or null if session not found
     */
    public ObjectNode getBattleSnapshot(String sessionId) {
        GameSession session = gameSessionService.getSession(sessionId);
        if (session == null) {
            log.debug("No game session found for battle snapshot: {}", sessionId);
            return null;
        }

        try {
            GameStateResponse state = gameSessionService.getGameState(session.getPlayerIds().stream()
                    .findFirst().orElse(1L), sessionId);
            if (state == null || state.getActiveBattles() == null || state.getActiveBattles().isEmpty()) {
                return null;
            }

            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "fleet_combat");
            message.put("sessionId", sessionId);

            ArrayNode battlesArray = message.putArray("battles");
            for (BattleSummaryDto battle : state.getActiveBattles()) {
                ObjectNode battleNode = battlesArray.addObject();
                battleNode.put("battleId", String.valueOf(battle.getBattleId()));
                battleNode.put("state", battle.getState());
                battleNode.put("attackerName", battle.getAttackerName());
                battleNode.put("defenderName", battle.getDefenderName());
                battleNode.put("attackerShips", battle.getAttackerShipCount());
                battleNode.put("defenderShips", battle.getDefenderShipCount());
                battleNode.put("attackerHull", battle.getAttackerRemainingHull());
                battleNode.put("defenderHull", battle.getDefenderRemainingHull());
                battleNode.put("attackerShields", battle.getAttackerRemainingShields());
                battleNode.put("defenderShields", battle.getDefenderRemainingShields());
                battleNode.put("currentRound", battle.getCurrentRound());
                battleNode.put("maxRounds", battle.getMaxRounds());
                battleNode.put("location", battle.getLocation() != null ? battle.getLocation() : "0,0");
                battleNode.put("timestamp", System.currentTimeMillis());
            }

            return message;
        } catch (Exception e) {
            log.warn("Failed to get battle snapshot for session {}: {}", sessionId, e.getMessage());
            return null;
        }
    }

    /**
     * Get active battle count across all sessions.
     */
    public int getActiveBattleCount() {
        int count = 0;
        for (GameSession session : listSessionsAsObjects()) {
            try {
                Long firstPlayer = session.getPlayerIds().stream().findFirst().orElse(null);
                if (firstPlayer == null) continue;
                GameStateResponse state = gameSessionService.getGameState(firstPlayer, session.getSessionId());
                if (state != null && state.getActiveBattles() != null) {
                    count += state.getActiveBattles().size();
                }
            } catch (Exception e) {
                // Skip
            }
        }
        return count;
    }

    // ==================== Pipeline Data ====================

    /**
     * Get real resource pipeline data for a game session as a WebSocket message.
     *
     * @param sessionId the game session ID
     * @return pipeline_snapshot message with real pipeline data, or null if session not found
     */
    public ObjectNode getPipelineSnapshot(String sessionId) {
        GameSession session = gameSessionService.getSession(sessionId);
        if (session == null) {
            log.debug("No game session found for pipeline snapshot: {}", sessionId);
            return null;
        }

        try {
            Long firstPlayer = session.getPlayerIds().stream().findFirst().orElse(null);
            if (firstPlayer == null) return null;

            ResourcePipelineSummary summary = gameSessionService.getPipelineSummary(firstPlayer, sessionId);
            if (summary == null) {
                return null;
            }

            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "pipeline_snapshot");
            message.put("sessionId", sessionId);

            message.put("miningOperations", summary.getMiningOperations());
            message.put("activeProcessing", summary.getActiveProcessing());
            message.put("activeAssemblers", summary.getActiveAssemblers());
            message.put("constructionQueueSize", summary.getConstructionQueueSize());

            ObjectNode rawResources = message.putObject("rawResources");
            if (summary.getRawResources() != null) {
                for (Map.Entry<String, Integer> entry : summary.getRawResources().entrySet()) {
                    rawResources.put(entry.getKey(), entry.getValue());
                }
            }

            ObjectNode refinedResources = message.putObject("refinedResources");
            if (summary.getRefinedResources() != null) {
                for (Map.Entry<String, Integer> entry : summary.getRefinedResources().entrySet()) {
                    refinedResources.put(entry.getKey(), entry.getValue());
                }
            }

            ObjectNode stats = message.putObject("statistics");
            stats.put("miningOperations", summary.getMiningOperations());
            stats.put("activeProcessing", summary.getActiveProcessing());
            stats.put("activeAssemblers", summary.getActiveAssemblers());
            stats.put("constructionQueueSize", summary.getConstructionQueueSize());
            if (summary.getMiningRate() != null) {
                stats.put("totalMiningRate", summary.getMiningRate().values().stream().mapToInt(Integer::intValue).sum());
            }

            message.put("timestamp", System.currentTimeMillis());
            return message;
        } catch (Exception e) {
            log.warn("Failed to get pipeline snapshot for session {}: {}", sessionId, e.getMessage());
            return null;
        }
    }

    // ==================== Wormhole Data ====================

    /**
     * Get all active wormhole connections as a WebSocket message.
     */
    public ObjectNode getWormholeSnapshot() {
        try {
            List<WormholeConnectionDto> connections =
                    wormholeConnectionService.listAllConnections();

            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "wormhole_update");

            ArrayNode wormholesArray = message.putArray("wormholes");
            for (WormholeConnectionDto conn : connections) {
                WormholeInfoDto wa = conn.getWormholeA();
                WormholeInfoDto wb = conn.getWormholeB();
                if (wa == null || wb == null) continue;

                ObjectNode wormholeNode = wormholesArray.addObject();

                // Wormhole A
                ObjectNode a = wormholeNode.putObject("wormholeA");
                a.put("id", wa.getId());
                a.put("galaxyName", conn.getGalaxyA());
                a.put("x", wa.getPosX());
                a.put("y", wa.getPosY());
                a.put("z", wa.getPosZ());
                a.put("name", wa.getName());

                // Wormhole B
                ObjectNode b = wormholeNode.putObject("wormholeB");
                b.put("id", wb.getId());
                b.put("galaxyName", conn.getGalaxyB());
                b.put("x", wb.getPosX());
                b.put("y", wb.getPosY());
                b.put("z", wb.getPosZ());
                b.put("name", wb.getName());
            }

            message.put("timestamp", System.currentTimeMillis());
            return message;
        } catch (Exception e) {
            log.warn("Failed to get wormhole snapshot: {}", e.getMessage());
            return null;
        }
    }

    // ==================== Session Management ====================

    /**
     * Get all active game sessions as objects.
     */
    public List<GameSession> listSessionsAsObjects() {
        List<Map<String, Object>> sessionInfos = gameSessionService.listSessions();
        List<GameSession> sessions = new java.util.ArrayList<>();
        for (Map<String, Object> info : sessionInfos) {
            String sessionId = (String) info.get("sessionId");
            GameSession session = gameSessionService.getSession(sessionId);
            if (session != null) {
                sessions.add(session);
            }
        }
        return sessions;
    }

    /**
     * Get the default (first active) game session.
     */
    public Optional<GameSession> getDefaultSession() {
        GameSession session = gameSessionService.getDefaultSession();
        return Optional.ofNullable(session);
    }

    /**
     * Check if any game sessions are active.
     */
    public boolean hasActiveSessions() {
        return gameSessionService.getDefaultSession() != null;
    }

    /**
     * Get a combined snapshot of all real game state for a session.
     * Useful for initial connection snapshots.
     *
     * @param sessionId the game session ID
     * @return combined snapshot with fleets, players, battles, pipeline, or null if session not found
     */
    public Map<String, ObjectNode> getCombinedSnapshot(String sessionId) {
        Map<String, ObjectNode> snapshot = new java.util.HashMap<>();
        ObjectNode fleets = getFleetSnapshot(sessionId);
        ObjectNode players = getPlayerSnapshot(sessionId);
        ObjectNode battles = getBattleSnapshot(sessionId);
        ObjectNode pipeline = getPipelineSnapshot(sessionId);

        if (fleets != null) snapshot.put("fleets", fleets);
        if (players != null) snapshot.put("players", players);
        if (battles != null) snapshot.put("battles", battles);
        if (pipeline != null) snapshot.put("pipeline", pipeline);

        return snapshot.isEmpty() ? null : snapshot;
    }
}
