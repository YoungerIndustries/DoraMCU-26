package com.yisy.modules.doramcu.lord_of_the_rings.repository;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LotrCharacterRepository extends JpaRepository<LotrCharacter, Long> {

    List<LotrCharacter> findByNameContainingIgnoreCase(String name);

    List<LotrCharacter> findByRace(String race);

    List<LotrCharacter> findByCalling(String calling);

    List<LotrCharacter> findByFaction(String faction);

    List<LotrCharacter> findByOneRingBearerTrue();

    Optional<LotrCharacter> findById(Long id);
}
