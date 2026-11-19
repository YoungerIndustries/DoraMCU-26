package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a node in the DoraMCU brain network.
 * <p>
 * Nodes are classified as Neuron (PDA), Node (tablet), or BrainCell (desktop)
 * based on hardware capabilities. They register with the BrainStem (Pi) and
 * participate in the distributed game server network.
 * </p>
 */
@Entity
@Table(name = "nodes")
public class Node {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "node_id", unique = true, nullable = false, length = 255)
    private String nodeId;

    @Column(name = "node_type", nullable = false, length = 50)
    private String nodeType;

    @Column(name = "device_id", nullable = false, length = 255)
    private String deviceId;

    @Column(name = "cpu_cores")
    private Integer cpuCores;

    @Column(name = "ram_mb")
    private Integer ramMb;

    @Column(name = "gpu_tier", length = 20)
    private String gpuTier;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "port")
    private Integer port;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Node() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(Integer cpuCores) {
        this.cpuCores = cpuCores;
    }

    public Integer getRamMb() {
        return ramMb;
    }

    public void setRamMb(Integer ramMb) {
        this.ramMb = ramMb;
    }

    public String getGpuTier() {
        return gpuTier;
    }

    public void setGpuTier(String gpuTier) {
        this.gpuTier = gpuTier;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
