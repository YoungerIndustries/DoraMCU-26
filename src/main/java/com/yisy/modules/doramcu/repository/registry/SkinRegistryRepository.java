package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.SkinRegistryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkinRegistryRepository extends JpaRepository<SkinRegistryEntity, Long> {

    Optional<SkinRegistryEntity> findBySkinCode(String skinCode);
}
