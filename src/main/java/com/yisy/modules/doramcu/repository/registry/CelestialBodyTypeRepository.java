package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.CelestialBodyTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CelestialBodyTypeRepository extends JpaRepository<CelestialBodyTypeEntity, Long> {

    Optional<CelestialBodyTypeEntity> findByBodyType(String bodyType);
}
