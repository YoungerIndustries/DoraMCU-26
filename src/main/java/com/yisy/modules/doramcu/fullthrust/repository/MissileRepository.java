package com.yisy.modules.doramcu.fullthrust.repository;

import com.yisy.modules.doramcu.fullthrust.entity.MissileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissileRepository extends JpaRepository<MissileEntity, String> {
    List<MissileEntity> findByGameSessionId(String sessionId);
    void deleteByGameSessionId(String sessionId);
}
