package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.ai.StationAiSupportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts StationAiSupportService to the unified AiService interface.
 * <p>
 * This adapter makes StationAiSupportService discoverable by the AiFrameworkRegistry
 * and provides standardized lifecycle management and metrics.
 * </p>
 */
@Service
public class StationAiSupportAdapter implements AiService {

    private static final Logger log = LoggerFactory.getLogger(StationAiSupportAdapter.class);

    private final StationAiSupportService stationAiSupportService;
    private AiServiceStatus status = AiServiceStatus.STOPPED;

    public StationAiSupportAdapter(StationAiSupportService stationAiSupportService) {
        this.stationAiSupportService = stationAiSupportService;
    }

    @Override
    public String getServiceId() {
        return "station-ai-support";
    }

    @Override
    public String getServiceName() {
        return "Station AI Support Service";
    }

    @Override
    public AiServiceStatus getStatus() {
        return status;
    }

    @Override
    public void initialize() {
        status = AiServiceStatus.RUNNING;
        log.info("Station AI Support Service initialized");
    }

    @Override
    public void shutdown() {
        status = AiServiceStatus.STOPPED;
        log.info("Station AI Support Service shutdown");
    }

    @Override
    public boolean isHealthy() {
        return status == AiServiceStatus.RUNNING;
    }

    @Override
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("status", status.name());
        metrics.put("maxCombinedBonus", StationAiSupportService.class.getDeclaredFields().length);
        return metrics;
    }

    /**
     * Returns the underlying StationAiSupportService.
     *
     * @return the StationAiSupportService
     */
    public StationAiSupportService getDelegate() {
        return stationAiSupportService;
    }
}
