package com.yisy.modules.doraneuron.config;

import com.yisy.modules.doraneuron.config.edge.NeuronEdgeConfig;
import com.yisy.modules.doraneuron.util.edge.SystemResourceDetector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Spring configuration for the Neuron module.
 */
@Configuration
public class NeuronBeanConfig {

    /**
     * RestTemplate used to communicate with the Neuron Coordinator.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Security configuration properties for the Neuron node.
     */
    @Bean
    public NeuronSecurityConfig neuronSecurityConfig() {
        return new NeuronSecurityConfig();
    }

    /**
     * System resource detector for edge deployment (Phase 13+).
     */
    @Bean
    public SystemResourceDetector systemResourceDetector() {
        return new SystemResourceDetector();
    }

    /**
     * Edge-specific configuration properties for the Neuron node (Phase 13+).
     */
    @Bean
    public NeuronEdgeConfig neuronEdgeConfig() {
        return new NeuronEdgeConfig();
    }
}
