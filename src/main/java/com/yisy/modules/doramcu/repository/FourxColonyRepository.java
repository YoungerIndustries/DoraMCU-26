package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.fourx.FourxColonyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FourxColonyRepository extends JpaRepository<FourxColonyEntity, Long> {

    Optional<FourxColonyEntity> findByColonyId(String colonyId);

    List<FourxColonyEntity> findByOwnerFactionId(String ownerFactionId);

    List<FourxColonyEntity> findBySession_SessionId(String sessionId);
}
