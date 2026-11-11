package com.yisy.modules.doramcu.star_trek_4x.rpg;

import com.yisy.modules.doramcu.star_trek_4x.model.MissionUpdate;
import com.yisy.modules.doramcu.star_trek_4x.repository.MissionUpdateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for connecting 4x events to Captain's Log missions.
 * <p>
 * Links CaptainLogMission to 4x events and triggers actions based on mission completion.
 * </p>
 */
@Service
public class MissionUpdateService {

    private static final Logger log = LoggerFactory.getLogger(MissionUpdateService.class);

    private final MissionUpdateRepository missionUpdateRepository;

    public MissionUpdateService(MissionUpdateRepository missionUpdateRepository) {
        this.missionUpdateRepository = missionUpdateRepository;
    }

    /**
     * Create a mission update linking a 4x event to a mission.
     *
     * @param missionId   the mission ID
     * @param eventType   the 4x event type
     * @param description the event description
     * @return the mission update
     */
    public MissionUpdate createMissionUpdate(Long missionId, String eventType, String description) {
        log.info("Creating mission update: mission={}, event={}", missionId, eventType);

        MissionUpdate update = new MissionUpdate();
        update.setCaptainLogMissionId(missionId);
        update.setEventType(eventType);
        update.setActionDescription(description);
        update.setProcessed(false);

        return missionUpdateRepository.save(update);
    }

    /**
     * Get all mission updates for a mission.
     *
     * @param missionId the mission ID
     * @return list of mission updates
     */
    public List<MissionUpdate> getMissionUpdates(Long missionId) {
        return missionUpdateRepository.findByCaptainLogMissionId(missionId);
    }

    /**
     * Process pending mission updates.
     *
     * @return number of processed updates
     */
    public int processPendingUpdates() {
        List<MissionUpdate> pending = missionUpdateRepository.findByProcessedFalse();
        for (MissionUpdate update : pending) {
            update.setProcessed(true);
            missionUpdateRepository.save(update);
        }
        log.info("Processed {} pending mission updates", pending.size());
        return pending.size();
    }

    /**
     * Trigger a 4x event from a mission completion.
     *
     * @param missionId the mission ID
     * @param eventType the event type
     * @return event result
     */
    public Map<String, Object> trigger4xEvent(Long missionId, String eventType) {
        log.info("Triggering 4x event: mission={}, event={}", missionId, eventType);

        MissionUpdate update = createMissionUpdate(missionId, eventType,
                "4x event triggered by mission completion");

        return Map.of(
                "success", true,
                "missionId", missionId,
                "eventType", eventType,
                "updateId", update.getId(),
                "message", "4x event triggered successfully"
        );
    }
}
