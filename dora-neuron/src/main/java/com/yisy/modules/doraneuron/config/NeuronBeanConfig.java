package com.yisy.modules.doraneuron.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doraneuron.config.edge.NeuronEdgeConfig;
import com.yisy.modules.doraneuron.model.NeuronIdentity;
import com.yisy.modules.doraneuron.util.edge.SystemResourceDetector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Spring configuration for the Neuron module.
 * Only active when running the neuron standalone (not when integrated into DoraMCU).
 */
@Configuration
@EnableConfigurationProperties(NeuronConfig.class)
@ConditionalOnProperty(prefix = "neuron", name = "standalone", havingValue = "true", matchIfMissing = false)
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

    /**
     * Jackson ObjectMapper for JSON serialization/deserialization.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * Neuron identity bean — auto-generated from config properties.
     */
    @Bean
    public NeuronIdentity neuronIdentity(NeuronConfig config) {
        NeuronIdentity identity = new NeuronIdentity();
        String nodeId = config.getNodeId();
        if (nodeId == null || nodeId.isEmpty()) {
            nodeId = java.util.UUID.randomUUID().toString();
        }
        identity.setNodeId(nodeId);
        identity.setNodeName(config.getNodeName() != null ? config.getNodeName() : "Neuron-" + nodeId.substring(0, 8));
        identity.setRegion(config.getNodeRegion());
        identity.setVoteWeight(config.getVoteWeight());
        return identity;
    }
}
