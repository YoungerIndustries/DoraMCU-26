package com.yisy.modules.doramcu.skin.repository;

import com.yisy.modules.doramcu.hex.Skin;

import com.yisy.modules.doramcu.skin.model.FactionSkinThemeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FactionSkinThemeRepository extends JpaRepository<FactionSkinThemeEntity, Long> {

    Optional<FactionSkinThemeEntity> findByFactionId(String factionId);

    boolean existsByFactionId(String factionId);

    void deleteByFactionId(String factionId);
}
