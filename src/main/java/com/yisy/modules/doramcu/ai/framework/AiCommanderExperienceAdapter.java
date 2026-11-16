package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.service.AiCommanderExperienceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts AiCommanderExperienceService to the unified AiService interface.
 * <p>
 * This adapter makes AiCommanderExperienceService discoverable by the AiFrameworkRegistry
 * and provides standardized lifecycle management and metrics.
 * </p>
 */
@Service
public class AiCommanderExperienceAdapter implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiCommanderExperienceAdapter.class);

    private final AiCommanderExperienceService aiCommanderExperienceService;
    private AiServiceStatus status = AiServiceStatus.STOPPED;

    public AiCommanderExperienceAdapter(AiCommanderExperienceService aiCommanderExperienceService) {
        this.aiCommanderExperienceService = aiCommanderExperienceService;
    }

    @Override
    public String getServiceId() {
        return "ai-commander-experience";
    }

    @Override
    public String getServiceName() {
        return "AI Commander Experience Service";
    }

    @Override
    public AiServiceStatus getStatus() {
        return status;
    }

    @Override
    public void initialize() {
        status = AiServiceStatus.RUNNING;
        log.info("AI Commander Experience Service initialized");
    }

    @Override
    public void shutdown() {
        status = AiServiceStatus.STOPPED;
        log.info("AI Commander Experience Service shutdown");
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
     * Returns the underlying AiCommanderExperienceService.
     *
     * @return the AiCommanderExperienceService
     */
    public AiCommanderExperienceService getDelegate() {
        return aiCommanderExperienceService;
    }
}
