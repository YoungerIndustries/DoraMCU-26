package com.yisy.modules.doramcu.star_trek_4x.repository;

import com.yisy.modules.doramcu.star_trek_4x.model.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    List<GameSession> findByMode(GameSession.GameMode mode);
}
