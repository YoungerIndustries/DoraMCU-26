package com.yisy.modules.doramcu.shared.repository;

import com.yisy.modules.doramcu.shared.model.SharedCharacterTravelLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for shared character travel logs.
 */
@Repository
public interface SharedCharacterTravelLogRepository extends JpaRepository<SharedCharacterTravelLog, Long> {

    List<SharedCharacterTravelLog> findByCharacterIdOrderByTraveledAtDesc(Long characterId);

    List<SharedCharacterTravelLog> findByFromSystem(String fromSystem);

    List<SharedCharacterTravelLog> findByToSystem(String toSystem);
}
