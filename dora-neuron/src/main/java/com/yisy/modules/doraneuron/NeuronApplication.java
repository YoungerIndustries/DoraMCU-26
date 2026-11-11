package com.yisy.modules.doraneuron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot entry point for a DoraMCU Neuron node.
 *
 * Each Neuron is a lightweight, independently deployable satellite AI node that:
 * <ul>
 *   <li>Serves as a web gateway directing users to the game server</li>
 *   <li>Registers with the central Neuron Coordinator (embedded in DoraMCU)</li>
 *   <li>Sends periodic heartbeats to maintain cluster membership</li>
 * </ul>
 *
 * Usage:
 *   ./gradlew -p dora-neuron bootRun
 *   java -jar dora-neuron.jar --neuron.node-name=Neuron-Alpha
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
public class NeuronApplication {

    private static final Logger log = LoggerFactory.getLogger(NeuronApplication.class);

    public static void main(String[] args) {
        log.info("Starting DoraMCU Neuron node");
        SpringApplication.run(NeuronApplication.class, args);
        log.info("DoraMCU Neuron started successfully");
    }
}
