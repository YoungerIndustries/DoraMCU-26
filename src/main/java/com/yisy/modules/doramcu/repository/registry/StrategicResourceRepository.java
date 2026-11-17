package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.StrategicResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StrategicResourceRepository extends JpaRepository<StrategicResourceEntity, Long> {

    Optional<StrategicResourceEntity> findByResourceType(String resourceType);
}
