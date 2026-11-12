package com.yisy.modules.doramcu.unified;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Wraps any {@link GameEngine} and provides a unified API for session management.
 * <p>
 * A GameSession tracks the lifecycle of a game instance, including start time,
 * current state, player actions, and cross-engine coordination.
 * </p>
 * <p>
 * This abstraction enables the unified system to manage multiple game engines
 * through a single interface, regardless of the underlying game type.
 * </p>
 */
public class GameSession {

    private static final Logger log = LoggerFactory.getLogger(GameSession.class);

    private final String sessionId;
    private final GameEngine engine;
    private final LocalDateTime startTime;
    private SessionState state;
    private final List<SessionEvent> events;
    private final Map<String, Object> metadata;

    /**
     * The current state of a game session.
     */
    public enum SessionState {
        CREATED, RUNNING, PAUSED, ENDED
    }

    /**
     * An event that occurred during a game session.
     */
    public static class SessionEvent {
        private final LocalDateTime timestamp;
        private final String type;
        private final String description;
        private final Map<String, Object> data;

        public SessionEvent(String type, String description, Map<String, Object> data) {
            this.timestamp = LocalDateTime.now();
            this.type = type;
            this.description = description;
            this.data = data != null ? Collections.unmodifiableMap(data) : Collections.emptyMap();
        }

        public LocalDateTime getTimestamp() { return timestamp; }
        public String getType() { return type; }
        public String getDescription() { return description; }
        public Map<String, Object> getData() { return data; }
    }

    /**
     * Creates a new game session for the given engine.
     *
     * @param engine the game engine
     */
    public GameSession(GameEngine engine) {
        this.sessionId = UUID.randomUUID().toString();
        this.engine = engine;
        this.startTime = LocalDateTime.now();
        this.state = SessionState.CREATED;
        this.events = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    /**
     * Starts the game session.
     */
    public void start() {
        if (state != SessionState.CREATED) {
            throw new IllegalStateException("Cannot start session in state: " + state);
        }
        engine.start();
        state = SessionState.RUNNING;
        addEvent("SESSION_START", "Game session started", null);
        log.info("Game session started: {} ({})", sessionId, engine.getName());
    }

    /**
     * Pauses the game session.
     */
    public void pause() {
        if (state != SessionState.RUNNING) {
            throw new IllegalStateException("Cannot pause session in state: " + state);
        }
        engine.pause();
        state = SessionState.PAUSED;
        addEvent("SESSION_PAUSE", "Game session paused", null);
        log.info("Game session paused: {} ({})", sessionId, engine.getName());
    }

    /**
     * Resumes the game session.
     */
    public void resume() {
        if (state != SessionState.PAUSED) {
            throw new IllegalStateException("Cannot resume session in state: " + state);
        }
        engine.start();
        state = SessionState.RUNNING;
        addEvent("SESSION_RESUME", "Game session resumed", null);
        log.info("Game session resumed: {} ({})", sessionId, engine.getName());
    }

    /**
     * Ends the game session.
     */
    public void end() {
        if (state == SessionState.ENDED) {
            throw new IllegalStateException("Session already ended");
        }
        engine.end();
        state = SessionState.ENDED;
        addEvent("SESSION_END", "Game session ended", null);
        log.info("Game session ended: {} ({})", sessionId, engine.getName());
    }

    /**
     * Advances the game by one turn.
     */
    public void nextTurn() {
        if (state != SessionState.RUNNING) {
            throw new IllegalStateException("Cannot advance turn in state: " + state);
        }
        engine.nextTurn();
        addEvent("TURN_ADVANCE", "Turn advanced to " + engine.getCurrentTurn(),
                Map.of("turn", engine.getCurrentTurn()));
    }

    /**
     * Processes a player action.
     *
     * @param action the action to process
     * @return the action result
     */
    public Map<String, Object> processAction(Map<String, Object> action) {
        if (state != SessionState.RUNNING) {
            throw new IllegalStateException("Cannot process action in state: " + state);
        }
        Map<String, Object> result = engine.processAction(action);
        addEvent("ACTION", action.toString(), result);
        return result;
    }

    /**
     * Adds a session event.
     */
    private void addEvent(String type, String description, Map<String, Object> data) {
        events.add(new SessionEvent(type, description, data));
    }

    // --- Getters ---

    public String getSessionId() {
        return sessionId;
    }

    public GameEngine getEngine() {
        return engine;
    }

    public GameType getGameType() {
        return engine.getGameType();
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public SessionState getState() {
        return state;
    }

    public List<SessionEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    /**
     * Returns the current game state from the engine.
     */
    public Map<String, Object> getGameState() {
        return engine.getGameState();
    }

    /**
     * Returns the available actions from the engine.
     */
    public Set<String> getAvailableActions() {
        return engine.getAvailableActions();
    }

    @Override
    public String toString() {
        return "GameSession{id='" + sessionId + "', engine=" + engine.getName() +
                ", state=" + state + ", turn=" + engine.getCurrentTurn() + "}";
    }
}
