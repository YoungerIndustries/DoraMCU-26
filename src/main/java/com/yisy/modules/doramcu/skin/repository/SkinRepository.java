package com.yisy.modules.doramcu.skin.repository;

import com.yisy.modules.doramcu.hex.Skin;

import com.yisy.modules.doramcu.hex.SkinType;
import com.yisy.modules.doramcu.skin.model.SkinEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkinRepository extends JpaRepository<SkinEntity, Long> {

    Optional<SkinEntity> findBySkinId(String skinId);

    List<SkinEntity> findByType(SkinType type);

    List<SkinEntity> findByFactionId(String factionId);

    boolean existsBySkinId(String skinId);

    void deleteBySkinId(String skinId);

    @Query("SELECT s FROM SkinEntity s LEFT JOIN FETCH s.defaultDecals WHERE s.skinId = :skinId")
    Optional<SkinEntity> findBySkinIdWithDecals(String skinId);
}
