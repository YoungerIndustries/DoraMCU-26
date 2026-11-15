package com.yisy.modules.doramcu.earth2026.survival.base;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaseStructureRepository extends JpaRepository<BaseStructure, Long> {

    List<BaseStructure> findByCharacterId(Long characterId);

    List<BaseStructure> findByCharacterIdAndStructureType(Long characterId, String structureType);

    List<BaseStructure> findByStructureType(String structureType);
}
