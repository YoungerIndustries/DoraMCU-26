package com.yisy.modules.doramcu.earth2026.survival;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurvivalStateRepository extends JpaRepository<SurvivalState, Long> {

    Optional<SurvivalState> findByCharacterId(Long characterId);

    List<SurvivalState> findByIsActiveTrue();

    List<SurvivalState> findByIsActiveFalse();
}
