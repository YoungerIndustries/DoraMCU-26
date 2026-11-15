package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for Earth 2026 Connection entities.
 */
@Repository
public interface Earth2026ConnectionRepository extends JpaRepository<Earth2026Connection, Long> {

    /**
     * Find connections where the source is a specific entity.
     */
    List<Earth2026Connection> findBySourceTypeAndSourceId(Earth2026Connection.EntityType sourceType, Long sourceId);

    /**
     * Find connections where the target is a specific entity.
     */
    List<Earth2026Connection> findByTargetTypeAndTargetId(Earth2026Connection.EntityType targetType, Long targetId);

    /**
     * Find active connections where the source is a specific entity.
     */
    List<Earth2026Connection> findBySourceTypeAndSourceIdAndActiveTrue(Earth2026Connection.EntityType sourceType, Long sourceId);

    /**
     * Find active connections where the target is a specific entity.
     */
    List<Earth2026Connection> findByTargetTypeAndTargetIdAndActiveTrue(Earth2026Connection.EntityType targetType, Long targetId);

    /**
     * Find connections by type.
     */
    List<Earth2026Connection> findByConnectionType(Earth2026Connection.ConnectionType connectionType);

    /**
     * Find active connections by type.
     */
    List<Earth2026Connection> findByConnectionTypeAndActiveTrue(Earth2026Connection.ConnectionType connectionType);

    /**
     * Find connections between two specific entities.
     */
    List<Earth2026Connection> findBySourceTypeAndSourceIdAndTargetTypeAndTargetId(
            Earth2026Connection.EntityType sourceType, Long sourceId,
            Earth2026Connection.EntityType targetType, Long targetId);

    /**
     * Find active connections by source entity and connection type.
     */
    List<Earth2026Connection> findBySourceTypeAndSourceIdAndConnectionTypeAndActiveTrue(
            Earth2026Connection.EntityType sourceType, Long sourceId,
            Earth2026Connection.ConnectionType connectionType);

    /**
     * Find all active connections.
     */
    List<Earth2026Connection> findByActiveTrue();

    /**
     * Find connections with secrecy level below a threshold.
     */
    List<Earth2026Connection> findBySecrecyLevelLessThanEqualAndActiveTrue(int secrecyLevel);
}
