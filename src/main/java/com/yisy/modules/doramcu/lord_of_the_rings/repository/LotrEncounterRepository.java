package com.yisy.modules.doramcu.lord_of_the_rings.repository;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrEncounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotrEncounterRepository extends JpaRepository<LotrEncounter, Long> {

    List<LotrEncounter> findByNameContainingIgnoreCase(String name);

    List<LotrEncounter> findByDifficulty(String difficulty);

    List<LotrEncounter> findByActiveTrue();

    List<LotrEncounter> findByPartyLevel(int partyLevel);
}
