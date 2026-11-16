package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.service.AiEntityRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts AiEntityRegistrationService to the unified AiService interface.
 * <p>
 * This adapter makes AiEntityRegistrationService discoverable by the AiFrameworkRegistry
 * and provides standardized lifecycle management and metrics for registering ships,
 * space stations, ground vehicles, and facilities as AI characters in the AI network.
 * </p>
 */
@Service
public class AiEntityRegistrationServiceAdapter implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiEntityRegistrationServiceAdapter.class);

    private final AiEntityRegistrationService aiEntityRegistrationService;
    private AiServiceStatus status = AiServiceStatus.STOPPED;

    public AiEntityRegistrationServiceAdapter(AiEntityRegistrationService aiEntityRegistrationService) {
        this.aiEntityRegistrationService = aiEntityRegistrationService;
    }

    @Override
    public String getServiceId() {
        return "ai-entity-registration-service";
    }

    @Override
    public String getServiceName() {
        return "AI Entity Registration Service";
    }

    @Override
    public AiServiceStatus getStatus() {
        return status;
    }

    @Override
    public void initialize() {
        status = AiServiceStatus.RUNNING;
        log.info("AI Entity Registration Service initialized");
    }

    @Override
    public void shutdown() {
        status = AiServiceStatus.STOPPED;
        log.info("AI Entity Registration Service shutdown");
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
     * Returns the underlying AiEntityRegistrationService.
     *
     * @return the AiEntityRegistrationService
     */
    public AiEntityRegistrationService getDelegate() {
        return aiEntityRegistrationService;
    }
}
