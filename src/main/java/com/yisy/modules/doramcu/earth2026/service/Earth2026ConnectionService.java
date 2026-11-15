package com.yisy.modules.doramcu.earth2026.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Connection;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026ConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * Service for Earth 2026 Connections.
 * Provides CRUD operations for connections between any entity types,
 * as well as strength management and relationship graph queries.
 */
@Service
public class Earth2026ConnectionService {

    private final Earth2026ConnectionRepository repository;

    @Autowired
    public Earth2026ConnectionService(Earth2026ConnectionRepository repository) {
        this.repository = repository;
    }

    // --- CRUD Operations ---

    /**
     * Find all connections.
     */
    public List<Earth2026Connection> findAll() {
        return repository.findAll();
    }

    /**
     * Find all active connections.
     */
    public List<Earth2026Connection> findActiveConnections() {
        return repository.findByActiveTrue();
    }

    /**
     * Find connection by ID.
     */
    public Optional<Earth2026Connection> findById(Long id) {
        return repository.findById(id);
    }

    /**
     * Save a connection.
     */
    public Earth2026Connection save(Earth2026Connection connection) {
        return repository.save(connection);
    }

    /**
     * Delete a connection by ID.
     */
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    // --- Query Operations ---

    /**
     * Find all connections where a specific entity is the source.
     */
    public List<Earth2026Connection> findBySourceEntity(Earth2026Connection.EntityType entityType, Long entityId) {
        return repository.findBySourceTypeAndSourceId(entityType, entityId);
    }

    /**
     * Find all active connections where a specific entity is the source.
     */
    public List<Earth2026Connection> findActiveBySourceEntity(Earth2026Connection.EntityType entityType, Long entityId) {
        return repository.findBySourceTypeAndSourceIdAndActiveTrue(entityType, entityId);
    }

    /**
     * Find all connections where a specific entity is the target.
     */
    public List<Earth2026Connection> findByTargetEntity(Earth2026Connection.EntityType entityType, Long entityId) {
        return repository.findByTargetTypeAndTargetId(entityType, entityId);
    }

    /**
     * Find all active connections where a specific entity is the target.
     */
    public List<Earth2026Connection> findActiveByTargetEntity(Earth2026Connection.EntityType entityType, Long entityId) {
        return repository.findByTargetTypeAndTargetIdAndActiveTrue(entityType, entityId);
    }

    /**
     * Find all connections involving a specific entity (as source or target).
     */
    public List<Earth2026Connection> findByEntity(Earth2026Connection.EntityType entityType, Long entityId) {
        List<Earth2026Connection> sourceConnections = repository.findBySourceTypeAndSourceIdAndActiveTrue(entityType, entityId);
        List<Earth2026Connection> targetConnections = repository.findByTargetTypeAndTargetIdAndActiveTrue(entityType, entityId);
        List<Earth2026Connection> allConnections = new java.util.ArrayList<>(sourceConnections);
        allConnections.addAll(targetConnections);
        return allConnections;
    }

    /**
     * Find connections by type.
     */
    public List<Earth2026Connection> findByConnectionType(Earth2026Connection.ConnectionType connectionType) {
        return repository.findByConnectionType(connectionType);
    }

    /**
     * Find active connections by type.
     */
    public List<Earth2026Connection> findActiveByConnectionType(Earth2026Connection.ConnectionType connectionType) {
        return repository.findByConnectionTypeAndActiveTrue(connectionType);
    }

    /**
     * Find connections between two specific entities.
     */
    public List<Earth2026Connection> findByEntities(
            Earth2026Connection.EntityType sourceType, Long sourceId,
            Earth2026Connection.EntityType targetType, Long targetId) {
        return repository.findBySourceTypeAndSourceIdAndTargetTypeAndTargetId(sourceType, sourceId, targetType, targetId);
    }

    /**
     * Find active connections by source entity and connection type.
     */
    public List<Earth2026Connection> findBySourceAndType(
            Earth2026Connection.EntityType sourceType, Long sourceId,
            Earth2026Connection.ConnectionType connectionType) {
        return repository.findBySourceTypeAndSourceIdAndConnectionTypeAndActiveTrue(sourceType, sourceId, connectionType);
    }

    /**
     * Find connections with secrecy level below a threshold (discoverable connections).
     */
    public List<Earth2026Connection> findDiscoverableConnections(int maxSecrecyLevel) {
        return repository.findBySecrecyLevelLessThanEqualAndActiveTrue(maxSecrecyLevel);
    }

    // --- Strength Management ---

    /**
     * Increase the strength of a connection.
     * @param connectionId the connection ID
     * @param amount the amount to increase (clamped to 0-100)
     * @return the updated connection
     */
    public Optional<Earth2026Connection> increaseStrength(Long connectionId, int amount) {
        return repository.findById(connectionId).map(connection -> {
            connection.increaseStrength(amount);
            return repository.save(connection);
        });
    }

    /**
     * Decrease the strength of a connection.
     * @param connectionId the connection ID
     * @param amount the amount to decrease (clamped to 0-100)
     * @return the updated connection
     */
    public Optional<Earth2026Connection> decreaseStrength(Long connectionId, int amount) {
        return repository.findById(connectionId).map(connection -> {
            connection.decreaseStrength(amount);
            return repository.save(connection);
        });
    }

    // --- Connection Management ---

    /**
     * Create a new connection between two entities.
     * @param sourceType type of the source entity
     * @param sourceId ID of the source entity
     * @param targetType type of the target entity
     * @param targetId ID of the target entity
     * @param connectionType type of connection
     * @param strength initial strength (0-100)
     * @param secrecyLevel secrecy level (0-100)
     * @return the created connection
     */
    public Earth2026Connection createConnection(
            Earth2026Connection.EntityType sourceType, Long sourceId,
            Earth2026Connection.EntityType targetType, Long targetId,
            Earth2026Connection.ConnectionType connectionType,
            int strength, int secrecyLevel) {
        Earth2026Connection connection = new Earth2026Connection();
        connection.setSourceType(sourceType);
        connection.setSourceId(sourceId);
        connection.setTargetType(targetType);
        connection.setTargetId(targetId);
        connection.setConnectionType(connectionType);
        connection.setStrength(strength);
        connection.setSecrecyLevel(secrecyLevel);
        connection.setActive(true);
        return repository.save(connection);
    }

    /**
     * Deactivate a connection (soft delete).
     * @param connectionId the connection ID
     * @return true if the connection was deactivated
     */
    public boolean deactivateConnection(Long connectionId) {
        Optional<Earth2026Connection> optional = repository.findById(connectionId);
        if (optional.isPresent()) {
            Earth2026Connection connection = optional.get();
            connection.setActive(false);
            repository.save(connection);
            return true;
        }
        return false;
    }

    // --- Relationship Graph Queries ---

    /**
     * Get the number of active connections for an entity.
     */
    public int getConnectionCount(Earth2026Connection.EntityType entityType, Long entityId) {
        return repository.findBySourceTypeAndSourceIdAndActiveTrue(entityType, entityId).size() +
               repository.findByTargetTypeAndTargetIdAndActiveTrue(entityType, entityId).size();
    }

    /**
     * Check if two entities have an active connection.
     */
    public boolean hasConnection(
            Earth2026Connection.EntityType sourceType, Long sourceId,
            Earth2026Connection.EntityType targetType, Long targetId) {
        List<Earth2026Connection> connections = repository.findBySourceTypeAndSourceIdAndTargetTypeAndTargetId(
                sourceType, sourceId, targetType, targetId);
        return connections.stream().anyMatch(Earth2026Connection::isActive);
    }

    /**
     * Check if an entity has a connection of a specific type.
     */
    public boolean hasConnectionType(
            Earth2026Connection.EntityType entityType, Long entityId,
            Earth2026Connection.ConnectionType connectionType) {
        List<Earth2026Connection> sourceConnections = repository.findBySourceTypeAndSourceIdAndConnectionTypeAndActiveTrue(
                entityType, entityId, connectionType);
        if (!sourceConnections.isEmpty()) return true;

        // Also check reverse direction
        List<Earth2026Connection> allTargetConnections = repository.findByTargetTypeAndTargetIdAndActiveTrue(entityType, entityId);
        return allTargetConnections.stream().anyMatch(c -> c.getConnectionType() == connectionType);
    }
}
