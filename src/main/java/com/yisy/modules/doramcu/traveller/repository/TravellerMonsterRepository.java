package com.yisy.modules.doramcu.traveller.repository;

import com.yisy.modules.doramcu.gamecore.AiBehaviorLevel;
import com.yisy.modules.doramcu.traveller.model.TravellerMonster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravellerMonsterRepository extends JpaRepository<TravellerMonster, Long> {

    List<TravellerMonster> findByThreatLevelAndActiveTrue(int threatLevel);

    List<TravellerMonster> findByTypeAndActiveTrue(String type);

    List<TravellerMonster> findBySizeAndActiveTrue(String size);

    List<TravellerMonster> findByAlignmentAndActiveTrue(String alignment);

    List<TravellerMonster> findByAiBehaviorLevelAndActiveTrue(AiBehaviorLevel aiBehaviorLevel);

    @Query("SELECT m FROM TravellerMonster m WHERE m.active = true AND m.threatLevel BETWEEN :minTl AND :maxTl ORDER BY m.threatLevel ASC")
    List<TravellerMonster> findByThreatLevelRange(@Param("minTl") int minTl, @Param("maxTl") int maxTl);

    @Query("SELECT m FROM TravellerMonster m WHERE m.active = true AND m.xp BETWEEN :minXp AND :maxXp ORDER BY m.xp ASC")
    List<TravellerMonster> findByXpRange(@Param("minXp") int minXp, @Param("maxXp") int maxXp);

    java.util.Optional<TravellerMonster> findByName(String name);

    List<TravellerMonster> findByIsFictional(boolean isFictional);

    List<TravellerMonster> findByTypeAndIsFictional(String type, boolean isFictional);

    @Query("SELECT m FROM TravellerMonster m WHERE m.active = true AND LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<TravellerMonster> searchByName(@Param("keyword") String keyword);

    long countByActiveTrue();
}
