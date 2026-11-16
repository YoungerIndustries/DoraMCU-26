package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.ai.AiDelegationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts AiDelegationService to the unified AiService interface.
 * <p>
 * This adapter makes AiDelegationService discoverable by the AiFrameworkRegistry
 * and provides standardized lifecycle management and metrics for AI delegation
 * settings management.
 * </p>
 */
@Service
public class AiDelegationServiceAdapter implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiDelegationServiceAdapter.class);

    private final AiDelegationService aiDelegationService;
    private AiServiceStatus status = AiServiceStatus.STOPPED;

    public AiDelegationServiceAdapter(AiDelegationService aiDelegationService) {
        this.aiDelegationService = aiDelegationService;
    }

    @Override
    public String getServiceId() {
        return "ai-delegation-service";
    }

    @Override
    public String getServiceName() {
        return "AI Delegation Service";
    }

    @Override
    public AiServiceStatus getStatus() {
        return status;
    }

    @Override
    public void initialize() {
        status = AiServiceStatus.RUNNING;
        log.info("AI Delegation Service initialized");
    }

    @Override
    public void shutdown() {
        status = AiServiceStatus.STOPPED;
        log.info("AI Delegation Service shutdown");
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
     * Returns the underlying AiDelegationService.
     *
     * @return the AiDelegationService
     */
    public AiDelegationService getDelegate() {
        return aiDelegationService;
    }
}
