package com.yisy.modules.doramcu.star_trek_4x.repository;

import com.yisy.modules.doramcu.star_trek_4x.model.MissionUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionUpdateRepository extends JpaRepository<MissionUpdate, Long> {
    List<MissionUpdate> findByGameSessionId(Long gameSessionId);
    List<MissionUpdate> findByProcessedFalse();
    List<MissionUpdate> findByCaptainLogMissionId(Long captainLogMissionId);
}
