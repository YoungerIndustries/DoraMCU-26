package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.service.ai.AiNetworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts AiNetworkService to the unified AiService interface.
 * <p>
 * This adapter makes AiNetworkService discoverable by the AiFrameworkRegistry
 * and provides standardized lifecycle management and metrics.
 * </p>
 */
@Service
public class AiNetworkServiceAdapter implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiNetworkServiceAdapter.class);

    private final AiNetworkService aiNetworkService;
    private AiServiceStatus status = AiServiceStatus.STOPPED;

    public AiNetworkServiceAdapter(AiNetworkService aiNetworkService) {
        this.aiNetworkService = aiNetworkService;
    }

    @Override
    public String getServiceId() {
        return "ai-network-service";
    }

    @Override
    public String getServiceName() {
        return "AI Network Service";
    }

    @Override
    public AiServiceStatus getStatus() {
        return status;
    }

    @Override
    public void initialize() {
        status = AiServiceStatus.RUNNING;
        log.info("AI Network Service initialized");
    }

    @Override
    public void shutdown() {
        status = AiServiceStatus.STOPPED;
        log.info("AI Network Service shutdown");
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
     * Returns the underlying AiNetworkService.
     *
     * @return the AiNetworkService
     */
    public AiNetworkService getDelegate() {
        return aiNetworkService;
    }
}
