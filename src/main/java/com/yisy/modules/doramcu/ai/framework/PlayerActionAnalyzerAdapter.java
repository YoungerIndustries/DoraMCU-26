package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.ai.PlayerActionAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts PlayerActionAnalyzer to the unified AiService interface.
 * <p>
 * This adapter makes PlayerActionAnalyzer discoverable by the AiFrameworkRegistry
 * and provides standardized lifecycle management and metrics for player behavioral
 * action tracking and personality trait computation.
 * </p>
 */
@Service
public class PlayerActionAnalyzerAdapter implements AiService {

    private static final Logger log = LoggerFactory.getLogger(PlayerActionAnalyzerAdapter.class);

    private final PlayerActionAnalyzer playerActionAnalyzer;
    private AiServiceStatus status = AiServiceStatus.STOPPED;

    public PlayerActionAnalyzerAdapter(PlayerActionAnalyzer playerActionAnalyzer) {
        this.playerActionAnalyzer = playerActionAnalyzer;
    }

    @Override
    public String getServiceId() {
        return "player-action-analyzer";
    }

    @Override
    public String getServiceName() {
        return "Player Action Analyzer";
    }

    @Override
    public AiServiceStatus getStatus() {
        return status;
    }

    @Override
    public void initialize() {
        status = AiServiceStatus.RUNNING;
        log.info("Player Action Analyzer initialized");
    }

    @Override
    public void shutdown() {
        status = AiServiceStatus.STOPPED;
        log.info("Player Action Analyzer shutdown");
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
     * Returns the underlying PlayerActionAnalyzer.
     *
     * @return the PlayerActionAnalyzer
     */
    public PlayerActionAnalyzer getDelegate() {
        return playerActionAnalyzer;
    }
}
