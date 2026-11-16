package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.admin.service.AiAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts AiAdminService to the unified AiService interface.
 * <p>
 * This adapter makes AiAdminService discoverable by the AiFrameworkRegistry
 * and provides standardized lifecycle management and metrics.
 * </p>
 */
@Service
public class AiAdminServiceAdapter implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminServiceAdapter.class);

    private final AiAdminService aiAdminService;
    private AiServiceStatus status = AiServiceStatus.STOPPED;

    public AiAdminServiceAdapter(AiAdminService aiAdminService) {
        this.aiAdminService = aiAdminService;
    }

    @Override
    public String getServiceId() {
        return "ai-admin-service";
    }

    @Override
    public String getServiceName() {
        return "AI Admin Service";
    }

    @Override
    public AiServiceStatus getStatus() {
        return status;
    }

    @Override
    public void initialize() {
        status = AiServiceStatus.RUNNING;
        log.info("AI Admin Service initialized");
    }

    @Override
    public void shutdown() {
        status = AiServiceStatus.STOPPED;
        log.info("AI Admin Service shutdown");
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
     * Returns the underlying AiAdminService.
     *
     * @return the AiAdminService
     */
    public AiAdminService getDelegate() {
        return aiAdminService;
    }
}
