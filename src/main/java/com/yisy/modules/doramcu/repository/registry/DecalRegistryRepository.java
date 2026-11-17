package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.DecalRegistryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DecalRegistryRepository extends JpaRepository<DecalRegistryEntity, Long> {

    Optional<DecalRegistryEntity> findByDecalCode(String decalCode);
}
