package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.TerrainTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TerrainTypeRepository extends JpaRepository<TerrainTypeEntity, Long> {

    Optional<TerrainTypeEntity> findByTerrainCode(String terrainCode);
}
