package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.captainslog.service.AIMissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts AIMissionService to the unified AiService interface.
 * <p>
 * This adapter makes AIMissionService discoverable by the AiFrameworkRegistry
 * and provides standardized lifecycle management and metrics for AI-generated
 * mission creation in the Captain's Log module.
 * </p>
 */
@Service
public class AIMissionServiceAdapter implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AIMissionServiceAdapter.class);

    private final AIMissionService aiMissionService;
    private AiServiceStatus status = AiServiceStatus.STOPPED;

    public AIMissionServiceAdapter(AIMissionService aiMissionService) {
        this.aiMissionService = aiMissionService;
    }

    @Override
    public String getServiceId() {
        return "ai-mission-service";
    }

    @Override
    public String getServiceName() {
        return "AI Mission Service";
    }

    @Override
    public AiServiceStatus getStatus() {
        return status;
    }

    @Override
    public void initialize() {
        status = AiServiceStatus.RUNNING;
        log.info("AI Mission Service initialized");
    }

    @Override
    public void shutdown() {
        status = AiServiceStatus.STOPPED;
        log.info("AI Mission Service shutdown");
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
     * Returns the underlying AIMissionService.
     *
     * @return the AIMissionService
     */
    public AIMissionService getDelegate() {
        return aiMissionService;
    }
}
