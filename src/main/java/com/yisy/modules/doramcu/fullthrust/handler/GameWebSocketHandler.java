package com.yisy.modules.doramcu.fullthrust.handler;
import com.yisy.modules.doramcu.fullthrust.dto.ChatMessage;
import com.yisy.modules.doramcu.fullthrust.dto.GameActionMessage;
import com.yisy.modules.doramcu.fullthrust.model.GameSession;
import com.yisy.modules.doramcu.fullthrust.model.Player;
import com.yisy.modules.doramcu.fullthrust.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket message handler for real-time multiplayer game actions.
 * Receives actions from clients, processes them through GameService,
 * and broadcasts updated game state to all connected clients.
 */
@Component
public class GameWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(GameWebSocketHandler.class);

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    // Map of sessionId -> connected playerIds
    private final Map<String, Map<String, String>> sessionConnections = new ConcurrentHashMap<>();

    public GameWebSocketHandler(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handle a player joining a game session.
     */
    @MessageMapping("/join")
    @SendTo("/topic/game-updates")
    public GameActionMessage handleJoin(GameActionMessage message) {
        logger.info("Player {} joining session {}", message.getPlayerName(), message.getSessionId());

        // Track the connection
        sessionConnections.computeIfAbsent(message.getSessionId(), k -> new ConcurrentHashMap<>())
                .put(message.getPlayerId(), message.getPlayerName());

        // Broadcast the join event
        GameActionMessage response = new GameActionMessage();
        response.setType("PLAYER_JOINED");
        response.setSessionId(message.getSessionId());
        response.setPlayerName(message.getPlayerName());
        return response;
    }

    /**
     * Handle a player leaving a game session.
     */
    @MessageMapping("/leave")
    @SendTo("/topic/game-updates")
    public GameActionMessage handleLeave(GameActionMessage message) {
        logger.info("Player {} leaving session {}", message.getPlayerName(), message.getSessionId());

        // Remove the connection
        Map<String, String> connections = sessionConnections.get(message.getSessionId());
        if (connections != null) {
            connections.remove(message.getPlayerId());
        }

        GameActionMessage response = new GameActionMessage();
        response.setType("PLAYER_LEFT");
        response.setSessionId(message.getSessionId());
        response.setPlayerName(message.getPlayerName());
        return response;
    }

    /**
     * Handle creating a new game.
     */
    @MessageMapping("/create-game")
    @SendTo("/topic/game-updates")
    public GameSession handleCreateGame(GameActionMessage message) {
        logger.info("Creating game: {}", message.getGameName());
        GameSession session = gameService.createGame(message.getGameName());
        broadcastGameState(session.getId());
        return session;
    }

    /**
     * Handle adding a player to a game.
     */
    @MessageMapping("/add-player")
    @SendTo("/topic/game-updates")
    public GameSession handleAddPlayer(GameActionMessage message) {
        logger.info("Adding player {} to session {}", message.getPlayerName(), message.getSessionId());
        String aiDifficulty = message.getAiDifficulty() != null ? message.getAiDifficulty() : "medium";
        GameSession session = gameService.addPlayer(
                message.getSessionId(),
                message.getPlayerName(),
                message.getFaction() != null ? message.getFaction() : "Independent",
                message.isAI(),
                aiDifficulty
        );
        broadcastGameState(session.getId());
        return session;
    }

    /**
     * Handle deploying a ship.
     */
    @MessageMapping("/deploy-ship")
    @SendTo("/topic/game-updates")
    public GameSession handleDeployShip(GameActionMessage message) {
        logger.info("Deploying ship {} for player {} in session {}",
                message.getShipName(), message.getPlayerId(), message.getSessionId());
        GameSession session = gameService.deployShip(
                message.getSessionId(),
                message.getPlayerId(),
                message.getShipClassId(),
                message.getShipName(),
                message.getStartQ(),
                message.getStartR()
        );
        broadcastGameState(session.getId());
        return session;
    }

    /**
     * Handle starting a game.
     */
    @MessageMapping("/start-game")
    @SendTo("/topic/game-updates")
    public GameSession handleStartGame(GameActionMessage message) {
        logger.info("Starting game {}", message.getSessionId());
        GameSession session = gameService.startGame(message.getSessionId());
        broadcastGameState(session.getId());
        return session;
    }

    /**
     * Handle moving a ship.
     */
    @MessageMapping("/move")
    @SendTo("/topic/game-updates")
    public GameSession handleMove(GameActionMessage message) {
        logger.info("Player {} moving ship {} in session {}",
                message.getPlayerId(), message.getShipId(), message.getSessionId());

        GameSession updated = gameService.moveShip(
                message.getSessionId(),
                message.getShipId(),
                message.getHeadingChange(),
                message.getThrust()
        );
        broadcastGameState(updated.getId());
        return updated;
    }

    /**
     * Handle firing a weapon.
     */
    @MessageMapping("/fire")
    @SendTo("/topic/game-updates")
    public GameSession handleFire(GameActionMessage message) {
        logger.info("Player {} firing weapon in session {}",
                message.getPlayerId(), message.getSessionId());

        GameSession updated = gameService.fireWeapon(
                message.getSessionId(),
                message.getShipId(),
                message.getTargetId(),
                message.getWeaponIndex()
        );
        broadcastGameState(updated.getId());
        return updated;
    }

    /**
     * Handle ending a turn.
     */
    @MessageMapping("/end-turn")
    @SendTo("/topic/game-updates")
    public GameSession handleEndTurn(GameActionMessage message) {
        logger.info("Player {} ending turn in session {}",
                message.getPlayerId(), message.getSessionId());

        GameSession updated = gameService.endTurn(message.getSessionId());
        broadcastGameState(updated.getId());

        // Auto-process AI turn if needed
        if (updated.isStarted() && !updated.isEnded()) {
            String nextPlayerId = updated.getCurrentPlayerId();
            Player nextPlayer = updated.getPlayers().get(nextPlayerId);
            if (nextPlayer != null && nextPlayer.isAI()) {
                try {
                    Thread.sleep(500); // Brief delay for UX
                    GameSession aiResult = gameService.processAITurn(message.getSessionId());
                    broadcastGameState(aiResult.getId());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return updated;
    }

    /**
     * Handle requesting the current game state.
     */
    @MessageMapping("/get-state")
    public void handleGetState(GameActionMessage message) {
        logger.info("Player {} requesting state for session {}",
                message.getPlayerId(), message.getSessionId());

        gameService.getSession(message.getSessionId()).ifPresent(session -> {
            // Send state directly to the requesting player
            messagingTemplate.convertAndSendToUser(
                    message.getPlayerId(),
                    "/queue/game-state",
                    session
            );
        });
    }

    /**
     * Broadcast the current game state to all clients subscribed to the session topic.
     */
    private void broadcastGameState(String sessionId) {
        gameService.getSession(sessionId).ifPresent(session -> {
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, session);
        });
    }

    /**
     * Handle a chat message from a player.
     */
    @MessageMapping("/chat")
    public void handleChat(ChatMessage message) {
        logger.info("Chat from {} in session {}: {}", message.getPlayerName(), message.getSessionId(), message.getMessage());
        
        // Set timestamp if not already set
        if (message.getTimestamp() == null) {
            message.setTimestamp(java.time.LocalDateTime.now());
        }
        message.setType("CHAT");
        
        // Broadcast the chat message to all players in the session
        messagingTemplate.convertAndSend("/topic/game/" + message.getSessionId() + "/chat", message);
    }

    /**
     * Handle requesting the list of connected players for a session.
     */
    @MessageMapping("/player-list")
    public void handlePlayerList(GameActionMessage message) {
        List<Map<String, String>> players = getConnectedPlayers(message.getSessionId());
        
        // Send player list to the requesting player
        messagingTemplate.convertAndSendToUser(
                message.getPlayerId(),
                "/queue/player-list",
                players
        );
    }

    /**
     * Get the list of connected players for a session.
     */
    public List<Map<String, String>> getConnectedPlayers(String sessionId) {
        List<Map<String, String>> players = new ArrayList<>();
        Map<String, String> connections = sessionConnections.get(sessionId);
        if (connections != null) {
            for (Map.Entry<String, String> entry : connections.entrySet()) {
                Map<String, String> player = new ConcurrentHashMap<>();
                player.put("playerId", entry.getKey());
                player.put("playerName", entry.getValue());
                players.add(player);
            }
        }
        return players;
    }

    /**
     * Get the number of connected players for a session.
     */
    public int getConnectedPlayerCount(String sessionId) {
        Map<String, String> connections = sessionConnections.get(sessionId);
        return connections != null ? connections.size() : 0;
    }
}
