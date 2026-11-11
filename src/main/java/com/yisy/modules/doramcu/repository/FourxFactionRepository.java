package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.fourx.FourxFactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FourxFactionRepository extends JpaRepository<FourxFactionEntity, Long> {

    Optional<FourxFactionEntity> findByFactionId(String factionId);

    List<FourxFactionEntity> findBySession_SessionId(String sessionId);

    List<FourxFactionEntity> findByGameModule(String gameModule);
}
