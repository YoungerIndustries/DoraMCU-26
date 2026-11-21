package com.yisy.modules.doramcu.corporation.repository;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.corporation.model.CharacterTrainingTour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CharacterTrainingTourRepository extends JpaRepository<CharacterTrainingTour, Long> {

    /**
     * Find all tours with a given status for a corporation.
     */
    List<CharacterTrainingTour> findByCorporationIdAndStatus(Long corporationId, CharacterTrainingTour.Status status);

    /**
     * Find all tours for a player character.
     */
    List<CharacterTrainingTour> findByTravellerCharacterId(Integer travellerCharacterId);

    /**
     * Find all tours for an AI character.
     */
    List<CharacterTrainingTour> findByAiCharacterId(Long aiCharacterId);

    /**
     * Find all tours for a corporation.
     */
    List<CharacterTrainingTour> findByCorporationId(Long corporationId);

    /**
     * Find tours filtered by corporation, status, and character type.
     */
    List<CharacterTrainingTour> findByCorporationIdAndStatusAndCharacterType(
            Long corporationId, CharacterTrainingTour.Status status, CharacterTrainingTour.CharacterType characterType);
}
