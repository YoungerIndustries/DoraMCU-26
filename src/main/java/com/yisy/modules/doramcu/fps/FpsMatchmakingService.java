package com.yisy.modules.doramcu.fps;
import com.yisy.modules.doramcu.model.players.Player;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.fps.core.FpsSession;
import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import com.yisy.modules.doramcu.fps.core.model.FpsMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Handles FPS matchmaking: room creation, joining, ready-up, and match lifecycle.
 * <p>
 * Players create or join rooms, ready up, and the match starts when all players are ready.
 * </p>
 */
@Service
public class FpsMatchmakingService {

    private static final Logger log = LoggerFactory.getLogger(FpsMatchmakingService.class);

    private final FpsGameServerWrapper gameServer;
    private final FpsMapService mapService;
    private final AtomicLong matchCounter;

    /** Room ID â†’ matchmaking room info */
    private final Map<String, MatchmakingRoom> rooms;

    /** Completed match records */
    private final List<FpsMatch> completedMatches;

    public FpsMatchmakingService(FpsGameServerWrapper gameServer, FpsMapService mapService) {
        this.gameServer = gameServer;
        this.mapService = mapService;
        this.matchCounter = new AtomicLong(0);
        this.rooms = new ConcurrentHashMap<>();
        this.completedMatches = new CopyOnWriteArrayList<>();
    }

    /**
     * Create a new matchmaking room.
     */
    public MatchmakingRoom createRoom(String creatorId, String creatorName, FpsMatch.GameMode mode, String mapId, int maxPlayers) {
        String roomId = "fps-" + matchCounter.incrementAndGet();

        // Find map or use default
        FpsMap map = mapService.getMap(mapId).orElse(null);
        if (map == null) {
            // Use first available map
            List<FpsMap> allMaps = mapService.getAllMaps();
            if (!allMaps.isEmpty()) {
                map = allMaps.get(0);
            }
        }

        MatchmakingRoom room = new MatchmakingRoom(roomId, mode, map, maxPlayers);
        room.addPlayer(creatorId, creatorName, 0);
        rooms.put(roomId, room);

        log.info("Created matchmaking room {} by {} with mode {} and map {}",
                roomId, creatorName, mode, map != null ? map.getName() : "default");

        return room;
    }

    /**
     * Join an existing room.
     */
    public Optional<MatchmakingRoom> joinRoom(String roomId, String playerId, String playerName) {
        MatchmakingRoom room = rooms.get(roomId);
        if (room == null) {
            return Optional.empty();
        }

        if (room.isFull()) {
            return Optional.empty();
        }

        // Assign team based on player count
        int team = room.getPlayerCount() % 2;
        room.addPlayer(playerId, playerName, team);

        log.info("Player {} joined room {} (team {})", playerName, roomId, team);
        return Optional.of(room);
    }

    /**
     * Leave a room.
     */
    public void leaveRoom(String roomId, String playerId) {
        MatchmakingRoom room = rooms.get(roomId);
        if (room != null) {
            room.removePlayer(playerId);
            if (room.getPlayerCount() == 0) {
                rooms.remove(roomId);
                log.info("Room {} removed (no players left)", roomId);
            }
        }
    }

    /**
     * Set a player as ready in a room.
     */
    public void readyUp(String roomId, String playerId) {
        MatchmakingRoom room = rooms.get(roomId);
        if (room != null) {
            room.setPlayerReady(playerId, true);

            // Check if all players are ready
            if (room.allReady() && room.getPlayerCount() >= 2) {
                startMatch(room);
            }
        }
    }

    /**
     * Get a room by ID.
     */
    public Optional<MatchmakingRoom> getRoom(String roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }

    /**
     * List all active rooms.
     */
    public List<MatchmakingRoom> getActiveRooms() {
        return new ArrayList<>(rooms.values());
    }

    /**
     * Find a room with available slots.
     */
    public Optional<MatchmakingRoom> findOpenRoom(FpsMatch.GameMode mode) {
        return rooms.values().stream()
                .filter(r -> r.getMode() == mode && !r.isFull() && r.getState() == RoomState.WAITING)
                .findFirst();
    }

    /**
     * Start a match from a matchmaking room.
     */
    private void startMatch(MatchmakingRoom room) {
        room.setState(RoomState.IN_PROGRESS);

        // Create game session
        FpsSession session = gameServer.createSession(
                room.getRoomId(),
                room.getMode(),
                room.getMap(),
                room.getMaxPlayers()
        );

        // Transfer players to game session
        for (MatchmakingRoom.RoomPlayer player : room.getPlayers()) {
            session.addPlayer(player.playerId(), player.playerName(), player.team());
        }

        // Start the match
        session.startMatch();

        // Remove from matchmaking
        rooms.remove(room.getRoomId());

        log.info("Match started in room {} with {} players", room.getRoomId(), room.getPlayerCount());
    }

    /**
     * Record a completed match.
     */
    public void recordMatch(FpsMatch match) {
        completedMatches.add(match);
        log.info("Recorded completed match: {} ({})", match.getId(), match.getMode());
    }

    /**
     * Get completed matches.
     */
    public List<FpsMatch> getCompletedMatches() {
        return new ArrayList<>(completedMatches);
    }

    /**
     * Get player statistics.
     */
    public PlayerStats getPlayerStats(String playerId) {
        int totalMatches = 0;
        int totalKills = 0;
        int totalDeaths = 0;
        int totalAssists = 0;
        int wins = 0;

        for (FpsMatch match : completedMatches) {
            for (FpsMatch.PlayerScore score : match.getPlayerScores()) {
                if (score.playerId().equals(playerId)) {
                    totalMatches++;
                    totalKills += score.kills();
                    totalDeaths += score.deaths();
                    totalAssists += score.assists();
                    if (match.getWinner() != null && match.getWinner().equals(playerId)) {
                        wins++;
                    }
                }
            }
        }

        double kdRatio = totalDeaths > 0 ? (double) totalKills / totalDeaths : totalKills;
        return new PlayerStats(totalMatches, totalKills, totalDeaths, totalAssists, wins, kdRatio);
    }

    /**
     * Matchmaking room state.
     */
    public enum RoomState {
        WAITING, IN_PROGRESS, FINISHED
    }

    /**
     * Represents a matchmaking room waiting for players.
     */
    public static class MatchmakingRoom {
        private final String roomId;
        private final FpsMatch.GameMode mode;
        private final FpsMap map;
        private final int maxPlayers;
        private final List<RoomPlayer> players;
        private RoomState state;
        private String serverUrl;

        public MatchmakingRoom(String roomId, FpsMatch.GameMode mode, FpsMap map, int maxPlayers) {
            this.roomId = roomId;
            this.mode = mode;
            this.map = map;
            this.maxPlayers = maxPlayers;
            this.players = new CopyOnWriteArrayList<>();
            this.state = RoomState.WAITING;
            this.serverUrl = "ws://localhost:9000/ws/fps";
        }

        public String getServerUrl() {
            return serverUrl;
        }

        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        public record RoomPlayer(String playerId, String playerName, int team, boolean ready) {
        }

        public String getRoomId() {
            return roomId;
        }

        public FpsMatch.GameMode getMode() {
            return mode;
        }

        public FpsMap getMap() {
            return map;
        }

        public int getMaxPlayers() {
            return maxPlayers;
        }

        public RoomState getState() {
            return state;
        }

        public void setState(RoomState state) {
            this.state = state;
        }

        public List<RoomPlayer> getPlayers() {
            return players;
        }

        public int getPlayerCount() {
            return players.size();
        }

        public boolean isFull() {
            return players.size() >= maxPlayers;
        }

        public void addPlayer(String playerId, String playerName, int team) {
            players.add(new RoomPlayer(playerId, playerName, team, false));
        }

        public void removePlayer(String playerId) {
            players.removeIf(p -> p.playerId().equals(playerId));
        }

        public void setPlayerReady(String playerId, boolean ready) {
            for (int i = 0; i < players.size(); i++) {
                RoomPlayer p = players.get(i);
                if (p.playerId().equals(playerId)) {
                    players.set(i, new RoomPlayer(p.playerId(), p.playerName(), p.team(), ready));
                    break;
                }
            }
        }

        public boolean allReady() {
            return players.stream().allMatch(RoomPlayer::ready);
        }
    }

    /**
     * Player statistics.
     */
    public record PlayerStats(int matches, int kills, int deaths, int assists, int wins, double kdRatio) {
    }
}
