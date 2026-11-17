package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Meeting;
import com.yisy.modules.doramcu.earth2026.model.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByTargetCorporationIdAndStatus(Long targetCorporationId, MeetingStatus status);
    List<Meeting> findByRequesterCorporationId(Long requesterCorporationId);
    List<Meeting> findByTargetCorporationId(Long targetCorporationId);
    List<Meeting> findByTargetCorporationIdAndStatusIn(Long targetCorporationId, List<MeetingStatus> statuses);
}
