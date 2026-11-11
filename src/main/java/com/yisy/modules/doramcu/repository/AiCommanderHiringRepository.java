package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.eve.model.AiCommanderHiring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiCommanderHiringRepository extends JpaRepository<AiCommanderHiring, Long> {

    /**
     * Find active hiring contract for a given AI character.
     */
    Optional<AiCommanderHiring> findByAiCharacterIdAndStatus(Long aiCharacterId, AiCommanderHiring.Status status);

    /**
     * Find all hiring contracts for a given player.
     */
    List<AiCommanderHiring> findByPlayerIdAndStatus(String playerId, AiCommanderHiring.Status status);

    /**
     * Find all active hiring contracts.
     */
    List<AiCommanderHiring> findByStatus(AiCommanderHiring.Status status);

    /**
     * Check if an AI character is currently hired.
     */
    boolean existsByAiCharacterIdAndStatus(Long aiCharacterId, AiCommanderHiring.Status status);
}
