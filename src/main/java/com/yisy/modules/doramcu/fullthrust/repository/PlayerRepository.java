package com.yisy.modules.doramcu.fullthrust.repository;

import com.yisy.modules.doramcu.fullthrust.entity.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("playerRepository_0")
    @Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, String> {
    List<PlayerEntity> findByGameSessionId(String sessionId);
    void deleteByGameSessionId(String sessionId);
}
