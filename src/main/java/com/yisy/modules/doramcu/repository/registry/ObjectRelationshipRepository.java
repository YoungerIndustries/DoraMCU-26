package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.ObjectRelationshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObjectRelationshipRepository extends JpaRepository<ObjectRelationshipEntity, Long> {

    List<ObjectRelationshipEntity> findBySourceObjectId(long sourceObjectId);

    List<ObjectRelationshipEntity> findByTargetObjectId(long targetObjectId);

    List<ObjectRelationshipEntity> findByRelationshipType(String relationshipType);
}
