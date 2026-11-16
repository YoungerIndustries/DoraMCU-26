package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.corporation.service.AiAssistantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts AiAssistantService to the unified AiService interface.
 * <p>
 * This adapter makes AiAssistantService discoverable by the AiFrameworkRegistry
 * and provides standardized lifecycle management and metrics for autonomous
 * corporation management per game turn.
 * </p>
 */
@Service
public class AiAssistantServiceAdapter implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantServiceAdapter.class);

    private final AiAssistantService aiAssistantService;
    private AiServiceStatus status = AiServiceStatus.STOPPED;

    public AiAssistantServiceAdapter(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @Override
    public String getServiceId() {
        return "ai-assistant-service";
    }

    @Override
    public String getServiceName() {
        return "AI Assistant Service";
    }

    @Override
    public AiServiceStatus getStatus() {
        return status;
    }

    @Override
    public void initialize() {
        status = AiServiceStatus.RUNNING;
        log.info("AI Assistant Service initialized");
    }

    @Override
    public void shutdown() {
        status = AiServiceStatus.STOPPED;
        log.info("AI Assistant Service shutdown");
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
     * Returns the underlying AiAssistantService.
     *
     * @return the AiAssistantService
     */
    public AiAssistantService getDelegate() {
        return aiAssistantService;
    }
}
