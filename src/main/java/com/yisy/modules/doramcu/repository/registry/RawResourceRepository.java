package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.RawResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RawResourceRepository extends JpaRepository<RawResourceEntity, Long> {

    Optional<RawResourceEntity> findByResourceType(String resourceType);

    List<RawResourceEntity> findAllByDisplayNameContaining(String keyword);
}
