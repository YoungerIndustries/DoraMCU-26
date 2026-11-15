package com.yisy.modules.doraai.config;

import com.yisy.modules.doraai.service.OllamaService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ollama.enabled", havingValue = "true", matchIfMissing = true)
public class OllamaHealthIndicator implements HealthIndicator {

    private final OllamaService ollamaService;

    public OllamaHealthIndicator(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @Override
    public Health health() {
        if (ollamaService.isAvailable()) {
            return Health.up()
                    .withDetail("service", "Ollama")
                    .withDetail("status", "connected")
                    .build();
        } else {
            return Health.down()
                    .withDetail("service", "Ollama")
                    .withDetail("status", "unreachable")
                    .build();
        }
    }
}
