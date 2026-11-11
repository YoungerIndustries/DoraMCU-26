package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026OnlineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Earth2026OnlineStatusRepository extends JpaRepository<Earth2026OnlineStatus, Long> {

    Optional<Earth2026OnlineStatus> findByCharacterId(Long characterId);

    List<Earth2026OnlineStatus> findByIsOnlineTrue();

    List<Earth2026OnlineStatus> findByLocationId(Long locationId);

    long countByIsOnlineTrue();

    long countByLocationId(Long locationId);
}
