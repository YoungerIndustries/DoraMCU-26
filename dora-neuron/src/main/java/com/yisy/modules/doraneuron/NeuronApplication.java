package com.yisy.modules.doraneuron;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * DoraMCU Neuron — standalone AI satellite node and player gateway.
 * Acts as the primary entry point: player login, platform detection,
 * client distribution, and real-time game state synchronization.
 *
 * Can also be included as a library in DoraMCU via DoraMcuLauncher.
 * When included as a library, this class is skipped unless neuron.standalone=true.
 */
@Configuration
@ConditionalOnProperty(name = "neuron.standalone", havingValue = "true", matchIfMissing = false)
public class NeuronApplication {

    /**
     * Standalone entry point for running dora-neuron independently.
     * Use only when running dora-neuron as a separate service.
     * Sets neuron.standalone=true so the @ConditionalOnProperty matches.
     */
    public static void main(String[] args) {
        System.setProperty("neuron.standalone", "true");
        SpringApplication.run(NeuronApplication.class, args);
    }
}
