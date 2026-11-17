package com.yisy.modules.doramcu.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.neuron.service.CdcEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a fallback no-op CdcEventPublisher when the neuron module is disabled.
 * The neuron package is excluded from component scan, but the IDE run configuration
 * may still include neuron classes on the classpath. This ensures CdcEventPublisher
 * is always available.
 */
@Configuration
public class NeuronFallbackConfig {

    @Bean
    @ConditionalOnMissingBean(CdcEventPublisher.class)
    public CdcEventPublisher cdcEventPublisherFallback(ObjectMapper objectMapper) {
        return new CdcEventPublisher(objectMapper);
    }
}
