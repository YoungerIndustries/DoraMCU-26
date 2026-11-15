package com.yisy.modules.doramcu.skin.repository;

import com.yisy.modules.doramcu.hex.Skin;

import com.yisy.modules.doramcu.skin.model.DecalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DecalRepository extends JpaRepository<DecalEntity, Long> {

    List<DecalEntity> findBySkinId(Long skinId);

    void deleteBySkinId(Long skinId);
}
