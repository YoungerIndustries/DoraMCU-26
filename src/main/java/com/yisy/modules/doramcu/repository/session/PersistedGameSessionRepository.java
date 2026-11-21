package com.yisy.modules.doramcu.repository.session;

import com.yisy.modules.doramcu.model.session.PersistedGameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersistedGameSessionRepository extends JpaRepository<PersistedGameSession, Long> {

    Optional<PersistedGameSession> findBySessionId(String sessionId);

    List<PersistedGameSession> findByLastActivityAtBefore(LocalDateTime threshold);

    @Modifying
    int deleteByLastActivityAtBefore(LocalDateTime threshold);
}
