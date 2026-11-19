package com.yisy.modules.doramcu.fullthrust.repository;

import com.yisy.modules.doramcu.fullthrust.entity.BoardObstacleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardObstacleRepository extends JpaRepository<BoardObstacleEntity, Long> {
    List<BoardObstacleEntity> findByGameSessionId(String sessionId);
    void deleteByGameSessionId(String sessionId);
}
