package com.yisy.modules.doramcu.fps;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.fps.core.FpsGameServer;
import com.yisy.modules.doramcu.fps.core.FpsSession;
import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import com.yisy.modules.doramcu.fps.core.model.FpsMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;

/**
 * Spring Boot wrapper for the FPS game server.
 * Delegates to the framework-agnostic FpsGameServer from dora-fps-core module.
 */
@Service
public class FpsGameServerWrapper {

    private static final Logger log = LoggerFactory.getLogger(FpsGameServerWrapper.class);

    private final FpsGameServer core;

    public FpsGameServerWrapper() {
        this.core = new FpsGameServer();
    }

    @PostConstruct
    public void start() {
        core.start();
    }

    @PreDestroy
    public void stop() {
        core.stop();
    }

    public FpsSession createSession(String sessionId, FpsMatch.GameMode gameMode, FpsMap map, int maxPlayers) {
        return core.createSession(sessionId, gameMode, map, maxPlayers);
    }

    public void removeSession(String sessionId) {
        core.removeSession(sessionId);
    }

    public FpsSession getSession(String sessionId) {
        return core.getSession(sessionId);
    }

    public List<FpsSession> getActiveSessions() {
        return core.getActiveSessions();
    }

    public void submitInput(String sessionId, FpsGameServer.FpsInputMessage input) {
        core.submitInput(sessionId, input);
    }
}
