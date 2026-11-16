package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.thirdreich.commander.AICommanderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts AICommanderService to the unified AiService interface.
 * <p>
 * This adapter makes AICommanderService discoverable by the AiFrameworkRegistry
 * and provides standardized lifecycle management and metrics for AI commanders
 * in the Third Reich ground combat engine.
 * </p>
 */
@Service
public class AICommanderServiceAdapter implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AICommanderServiceAdapter.class);

    private final AICommanderService aiCommanderService;
    private AiServiceStatus status = AiServiceStatus.STOPPED;

    public AICommanderServiceAdapter(AICommanderService aiCommanderService) {
        this.aiCommanderService = aiCommanderService;
    }

    @Override
    public String getServiceId() {
        return "ai-commander-service";
    }

    @Override
    public String getServiceName() {
        return "AI Commander Service";
    }

    @Override
    public AiServiceStatus getStatus() {
        return status;
    }

    @Override
    public void initialize() {
        status = AiServiceStatus.RUNNING;
        log.info("AI Commander Service initialized");
    }

    @Override
    public void shutdown() {
        status = AiServiceStatus.STOPPED;
        log.info("AI Commander Service shutdown");
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
     * Returns the underlying AICommanderService.
     *
     * @return the AICommanderService
     */
    public AICommanderService getDelegate() {
        return aiCommanderService;
    }
}
