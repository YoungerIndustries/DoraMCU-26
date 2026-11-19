package com.yisy.modules.doramcu.fullthrust.repository;

import com.yisy.modules.doramcu.fullthrust.entity.ShipClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipClassRepository extends JpaRepository<ShipClassEntity, String> {
    List<ShipClassEntity> findByGameSessionId(String sessionId);
    void deleteByGameSessionId(String sessionId);
}
