package com.yisy.modules.doramcu.traveller.exploration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("travellerCharacterPositionRepository")
public interface CharacterPositionRepository extends JpaRepository<CharacterPosition, Long> {
    Optional<CharacterPosition> findByCharacterId(String characterId);
}
