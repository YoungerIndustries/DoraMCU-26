package com.yisy.modules.doramcu.fullthrust.repository;

import com.yisy.modules.doramcu.fullthrust.entity.GameSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("gameSessionRepository_0")
    @Repository
public interface GameSessionRepository extends JpaRepository<GameSessionEntity, String> {
}
