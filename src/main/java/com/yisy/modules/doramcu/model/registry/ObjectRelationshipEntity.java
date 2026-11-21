package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for generic relationships between game objects.
 * <p>
 * Relationship types include: PART_OF, REQUIRES, PRODUCES, USES, etc.
 * </p>
 */
@Entity
@Table(name = "object_relationship", indexes = {
        @Index(name = "idx_or_source", columnList = "source_object_id"),
        @Index(name = "idx_or_target", columnList = "target_object_id"),
        @Index(name = "idx_or_relationship_type", columnList = "relationship_type")
})
public class ObjectRelationshipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "source_object_id", nullable = false)
    private long sourceObjectId;

    @Column(name = "target_object_id", nullable = false)
    private long targetObjectId;

    @Column(name = "relationship_type", nullable = false, length = 50)
    private String relationshipType;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    public ObjectRelationshipEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getSourceObjectId() {
        return sourceObjectId;
    }

    public void setSourceObjectId(long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    public long getTargetObjectId() {
        return targetObjectId;
    }

    public void setTargetObjectId(long targetObjectId) {
        this.targetObjectId = targetObjectId;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
