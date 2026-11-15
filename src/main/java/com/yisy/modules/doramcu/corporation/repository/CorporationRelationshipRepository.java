package com.yisy.modules.doramcu.corporation.repository;

import com.yisy.modules.doramcu.corporation.model.CorporationRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for querying corporation relationships.
 */
@Repository
public interface CorporationRelationshipRepository extends JpaRepository<CorporationRelationship, Long> {

    /**
     * Find all relationships involving a specific corporation.
     *
     * @param corporationId the corporation ID
     * @return list of relationships
     */
    @Query("SELECT r FROM CorporationRelationship r WHERE r.corporation1Id = :corpId OR r.corporation2Id = :corpId")
    List<CorporationRelationship> findByCorporationId(@Param("corpId") Long corporationId);

    /**
     * Find relationship between two specific corporations.
     *
     * @param corp1Id the first corporation ID
     * @param corp2Id the second corporation ID
     * @return optional relationship
     */
    @Query("SELECT r FROM CorporationRelationship r WHERE " +
            "(r.corporation1Id = :corp1 AND r.corporation2Id = :corp2) OR " +
            "(r.corporation1Id = :corp2 AND r.corporation2Id = :corp1)")
    Optional<CorporationRelationship> findByBothCorporations(@Param("corp1") Long corp1Id, @Param("corp2") Long corp2Id);

    /**
     * Find all relationships of a specific type for a corporation.
     *
     * @param corporationId the corporation ID
     * @param relationshipType the relationship type
     * @return list of relationships
     */
    @Query("SELECT r FROM CorporationRelationship r WHERE " +
            "(r.corporation1Id = :corpId OR r.corporation2Id = :corpId) AND r.relationshipType = :type")
    List<CorporationRelationship> findByCorporationIdAndType(@Param("corpId") Long corporationId,
                                                             @Param("type") CorporationRelationship.RelationshipType relationshipType);

    /**
     * Get the other corporation ID in a relationship.
     *
     * @param corporationId the known corporation ID
     * @return the other corporation ID, or null if not found
     */
    @Query("SELECT CASE WHEN r.corporation1Id = :corpId THEN r.corporation2Id ELSE r.corporation1Id END " +
            "FROM CorporationRelationship r WHERE r.id = :relId")
    Optional<Long> findOtherCorporationId(@Param("relId") Long relId, @Param("corpId") Long corporationId);
}
