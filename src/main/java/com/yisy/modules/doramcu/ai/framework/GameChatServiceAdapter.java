package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.ai.GameChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts GameChatService to the unified AiService interface.
 * <p>
 * This adapter makes GameChatService discoverable by the AiFrameworkRegistry
 * and provides standardized lifecycle management and metrics for empire-aware
 * AI chat interactions.
 * </p>
 */
@Service
public class GameChatServiceAdapter implements AiService {

    private static final Logger log = LoggerFactory.getLogger(GameChatServiceAdapter.class);

    private final GameChatService gameChatService;
    private AiServiceStatus status = AiServiceStatus.STOPPED;

    public GameChatServiceAdapter(GameChatService gameChatService) {
        this.gameChatService = gameChatService;
    }

    @Override
    public String getServiceId() {
        return "game-chat-service";
    }

    @Override
    public String getServiceName() {
        return "Game Chat Service";
    }

    @Override
    public AiServiceStatus getStatus() {
        return status;
    }

    @Override
    public void initialize() {
        status = AiServiceStatus.RUNNING;
        log.info("Game Chat Service initialized");
    }

    @Override
    public void shutdown() {
        status = AiServiceStatus.STOPPED;
        log.info("Game Chat Service shutdown");
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
     * Returns the underlying GameChatService.
     *
     * @return the GameChatService
     */
    public GameChatService getDelegate() {
        return gameChatService;
    }
}
