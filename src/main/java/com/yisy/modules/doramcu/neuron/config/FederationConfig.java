package com.yisy.modules.doramcu.neuron.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for Multi-Region Federation.
 *
 * All properties are prefixed with {@code neuron.coordinator.federation} in application.properties.
 */
@Component
@ConfigurationProperties(prefix = "neuron.coordinator.federation")
public class FederationConfig {

    /** Enable federation mode. */
    private boolean enabled = false;

    /** Whether this coordinator is the primary (handles model training and global experience aggregation). */
    private boolean primary = true;

    /** Region identifier for this coordinator (e.g. "us-east", "eu-west", "apac"). */
    private String region = "default";

    /** List of peer coordinator URLs for federation. */
    private List<String> peerUrls = new ArrayList<>();

    /** Interval for registry sync with peers (milliseconds). */
    private long syncIntervalMs = 60000;

    /** Timeout for cross-region consensus requests (milliseconds). */
    private long consensusTimeoutMs = 10000;

    /** Enable experience forwarding to primary coordinator. */
    private boolean experienceForwardingEnabled = true;

    /** Interval for peer health checks (milliseconds). */
    private long healthCheckIntervalMs = 30000;

    /** Enable periodic registry sync with peers. */
    private boolean periodicSyncEnabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public List<String> getPeerUrls() {
        return peerUrls;
    }

    public void setPeerUrls(List<String> peerUrls) {
        this.peerUrls = peerUrls;
    }

    public long getSyncIntervalMs() {
        return syncIntervalMs;
    }

    public void setSyncIntervalMs(long syncIntervalMs) {
        this.syncIntervalMs = syncIntervalMs;
    }

    public long getConsensusTimeoutMs() {
        return consensusTimeoutMs;
    }

    public void setConsensusTimeoutMs(long consensusTimeoutMs) {
        this.consensusTimeoutMs = consensusTimeoutMs;
    }

    public boolean isExperienceForwardingEnabled() {
        return experienceForwardingEnabled;
    }

    public void setExperienceForwardingEnabled(boolean experienceForwardingEnabled) {
        this.experienceForwardingEnabled = experienceForwardingEnabled;
    }

    public long getHealthCheckIntervalMs() {
        return healthCheckIntervalMs;
    }

    public void setHealthCheckIntervalMs(long healthCheckIntervalMs) {
        this.healthCheckIntervalMs = healthCheckIntervalMs;
    }

    public boolean isPeriodicSyncEnabled() {
        return periodicSyncEnabled;
    }

    public void setPeriodicSyncEnabled(boolean periodicSyncEnabled) {
        this.periodicSyncEnabled = periodicSyncEnabled;
    }
}
