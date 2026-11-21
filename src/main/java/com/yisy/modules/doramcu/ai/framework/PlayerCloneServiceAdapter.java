package com.yisy.modules.doramcu.ai.framework;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.ai.PlayerCloneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts PlayerCloneService to the unified AiService interface.
 * <p>
 * This adapter makes PlayerCloneService discoverable by the AiFrameworkRegistry
 * and provides standardized lifecycle management and metrics for AI player
 * clone creation and personality caching.
 * </p>
 */
@Service
public class PlayerCloneServiceAdapter implements AiService {

    private static final Logger log = LoggerFactory.getLogger(PlayerCloneServiceAdapter.class);

    private final PlayerCloneService playerCloneService;
    private AiServiceStatus status = AiServiceStatus.STOPPED;

    public PlayerCloneServiceAdapter(PlayerCloneService playerCloneService) {
        this.playerCloneService = playerCloneService;
    }

    @Override
    public String getServiceId() {
        return "player-clone-service";
    }

    @Override
    public String getServiceName() {
        return "Player Clone Service";
    }

    @Override
    public AiServiceStatus getStatus() {
        return status;
    }

    @Override
    public void initialize() {
        status = AiServiceStatus.RUNNING;
        log.info("Player Clone Service initialized");
    }

    @Override
    public void shutdown() {
        status = AiServiceStatus.STOPPED;
        playerCloneService.clearAllCaches();
        log.info("Player Clone Service shutdown");
    }

    @Override
    public boolean isHealthy() {
        return status == AiServiceStatus.RUNNING;
    }

    @Override
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("status", status.name());
        return metrics;
    }

    /**
     * Returns the underlying PlayerCloneService.
     *
     * @return the PlayerCloneService
     */
    public PlayerCloneService getDelegate() {
        return playerCloneService;
    }
}
