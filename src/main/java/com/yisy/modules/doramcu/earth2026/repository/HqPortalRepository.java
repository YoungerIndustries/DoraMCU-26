package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.HqPortal;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HqPortalRepository extends JpaRepository<HqPortal, Long> {
    @Query("SELECT p FROM HqPortal p WHERE p.hqId = :hqId")
    List<HqPortal> findByHqId(@Param("hqId") Long hqId);
    
    @Query("SELECT p FROM HqPortal p WHERE p.hqId = :hqId AND p.active = true")
    List<HqPortal> findByHqIdAndActiveTrue(@Param("hqId") Long hqId);
    
    @Query("SELECT p FROM HqPortal p WHERE p.hqId = :hqId AND p.gameType = :gameType")
    Optional<HqPortal> findByHqIdAndGameType(@Param("hqId") Long hqId, @Param("gameType") GameType gameType);
    
    @Query("SELECT p FROM HqPortal p WHERE p.active = true")
    List<HqPortal> findByActiveTrue();
}
