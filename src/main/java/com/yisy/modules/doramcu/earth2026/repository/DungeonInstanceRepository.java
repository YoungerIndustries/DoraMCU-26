package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.DungeonInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DungeonInstanceRepository extends JpaRepository<DungeonInstance, Long> {
    List<DungeonInstance> findByHqId(Long hqId);
    List<DungeonInstance> findByBuildingId(Long buildingId);
    List<DungeonInstance> findByHqIdAndActiveTrue(Long hqId);
    List<DungeonInstance> findByBuildingIdAndActiveTrue(Long buildingId);
    List<DungeonInstance> findByStatus(String status);
    Optional<DungeonInstance> findByHqIdAndBasementLevel(Long hqId, int basementLevel);
    Optional<DungeonInstance> findByBuildingIdAndBasementLevel(Long buildingId, int basementLevel);
}
