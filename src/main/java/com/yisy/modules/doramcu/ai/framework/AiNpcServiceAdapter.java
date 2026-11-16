package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.earth2026.service.AiNpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts AiNpcService to the unified AiService interface.
 * <p>
 * This adapter makes AiNpcService discoverable by the AiFrameworkRegistry
 * and provides standardized lifecycle management and metrics for AI-driven
 * NPC decision-making in Earth2026 with hybrid rule-based + LLM approaches.
 * </p>
 */
@Service
public class AiNpcServiceAdapter implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiNpcServiceAdapter.class);

    private final AiNpcService aiNpcService;
    private AiServiceStatus status = AiServiceStatus.STOPPED;

    public AiNpcServiceAdapter(AiNpcService aiNpcService) {
        this.aiNpcService = aiNpcService;
    }

    @Override
    public String getServiceId() {
        return "ai-npc-service";
    }

    @Override
    public String getServiceName() {
        return "AI NPC Service";
    }

    @Override
    public AiServiceStatus getStatus() {
        return status;
    }

    @Override
    public void initialize() {
        status = AiServiceStatus.RUNNING;
        log.info("AI NPC Service initialized");
    }

    @Override
    public void shutdown() {
        status = AiServiceStatus.STOPPED;
        log.info("AI NPC Service shutdown");
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
     * Returns the underlying AiNpcService.
     *
     * @return the AiNpcService
     */
    public AiNpcService getDelegate() {
        return aiNpcService;
    }
}
