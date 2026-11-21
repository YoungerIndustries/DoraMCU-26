package com.yisy.modules.doramcu.ai.framework;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.service.ai.AiPlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts AiPlayerService to the unified AiService interface.
 * <p>
 * This adapter makes AiPlayerService discoverable by the AiFrameworkRegistry
 * and provides standardized lifecycle management and metrics.
 * </p>
 */
@Service
public class AiPlayerServiceAdapter implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiPlayerServiceAdapter.class);

    private final AiPlayerService aiPlayerService;
    private AiServiceStatus status = AiServiceStatus.STOPPED;

    public AiPlayerServiceAdapter(AiPlayerService aiPlayerService) {
        this.aiPlayerService = aiPlayerService;
    }

    @Override
    public String getServiceId() {
        return "ai-player-service";
    }

    @Override
    public String getServiceName() {
        return "AI Player Service";
    }

    @Override
    public AiServiceStatus getStatus() {
        return status;
    }

    @Override
    public void initialize() {
        status = AiServiceStatus.RUNNING;
        log.info("AI Player Service initialized with {} AI cores", aiPlayerService.getAiCoreCount());
    }

    @Override
    public void shutdown() {
        status = AiServiceStatus.STOPPED;
        log.info("AI Player Service shutdown");
    }

    @Override
    public boolean isHealthy() {
        return status == AiServiceStatus.RUNNING;
    }

    @Override
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("aiCoreCount", aiPlayerService.getAiCoreCount());
        metrics.put("status", status.name());
        return metrics;
    }

    /**
     * Returns the underlying AiPlayerService.
     *
     * @return the AiPlayerService
     */
    public AiPlayerService getDelegate() {
        return aiPlayerService;
    }
}
