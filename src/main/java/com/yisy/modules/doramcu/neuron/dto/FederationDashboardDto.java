package com.yisy.modules.doramcu.neuron.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive dashboard DTO for Federation Health monitoring.
 *
 * Aggregates all federation state — peers, neurons, sync status, model versions,
 * and health assessment — into a single response for admin dashboards.
 */
public class FederationDashboardDto {

    /** Whether federation is enabled. */
    private boolean enabled;

    /** Whether this coordinator is the primary. */
    private boolean isPrimary;

    /** Region identifier for this coordinator. */
    private String region;

    /** Overall federation health status. */
    private HealthStatus healthStatus;

    /** Health score 0-100 based on peer connectivity, model version alignment, and sync freshness. */
    private int healthScore;

    /** List of peer health summaries. */
    private List<PeerHealthDto> peers;

    /** Local neuron statistics. */
    private NeuronStatsDto localNeurons;

    /** Model version comparison across all regions. */
    private List<ModelVersionDto> modelVersions;

    /** Sync history for recent sync operations. */
    private List<SyncHistoryDto> syncHistory;

    /** Recommendations based on health assessment. */
    private List<String> recommendations;

    /** Timestamp of this dashboard snapshot. */
    private Instant timestamp;

    /**
     * Overall health status of the federation.
     */
    public enum HealthStatus {
        HEALTHY,
        DEGRADED,
        CRITICAL
    }

    public FederationDashboardDto() {
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public HealthStatus getHealthStatus() { return healthStatus; }
    public void setHealthStatus(HealthStatus healthStatus) { this.healthStatus = healthStatus; }

    public int getHealthScore() { return healthScore; }
    public void setHealthScore(int healthScore) { this.healthScore = healthScore; }

    public List<PeerHealthDto> getPeers() { return peers; }
    public void setPeers(List<PeerHealthDto> peers) { this.peers = peers; }

    public NeuronStatsDto getLocalNeurons() { return localNeurons; }
    public void setLocalNeurons(NeuronStatsDto localNeurons) { this.localNeurons = localNeurons; }

    public List<ModelVersionDto> getModelVersions() { return modelVersions; }
    public void setModelVersions(List<ModelVersionDto> modelVersions) { this.modelVersions = modelVersions; }

    public List<SyncHistoryDto> getSyncHistory() { return syncHistory; }
    public void setSyncHistory(List<SyncHistoryDto> syncHistory) { this.syncHistory = syncHistory; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
