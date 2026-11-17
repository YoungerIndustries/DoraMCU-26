package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.RefinedResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefinedResourceRepository extends JpaRepository<RefinedResourceEntity, Long> {

    Optional<RefinedResourceEntity> findByResourceType(String resourceType);
}
