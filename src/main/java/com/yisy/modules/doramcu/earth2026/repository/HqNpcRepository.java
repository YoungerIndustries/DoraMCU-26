package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.HqNpc;
import com.yisy.modules.doramcu.earth2026.model.HqNpcType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HqNpcRepository extends JpaRepository<HqNpc, Long> {
    List<HqNpc> findByHqIdAndIsActiveTrue(Long hqId);
    List<HqNpc> findByHqIdAndNpcTypeAndIsActiveTrue(Long hqId, HqNpcType npcType);
    List<HqNpc> findByFloorLocationAndIsActiveTrue(String floor);
    List<HqNpc> findByIsActiveTrue();
}
