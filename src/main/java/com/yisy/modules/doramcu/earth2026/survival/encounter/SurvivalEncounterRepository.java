package com.yisy.modules.doramcu.earth2026.survival.encounter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurvivalEncounterRepository extends JpaRepository<SurvivalEncounter, Long> {

    List<SurvivalEncounter> findByCharacterId(Long characterId);

    List<SurvivalEncounter> findByCharacterIdOrderByEncounterTimeDesc(Long characterId);
}
