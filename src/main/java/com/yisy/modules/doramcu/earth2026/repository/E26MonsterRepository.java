package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.E26Monster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for Earth 2026 Monster entities.
 */
@Repository
public interface E26MonsterRepository extends JpaRepository<E26Monster, Long> {

    /**
     * Find monsters by threat level.
     */
    List<E26Monster> findByThreatLevel(int threatLevel);

    /**
     * Find monsters by type.
     */
    List<E26Monster> findByType(String type);

    /**
     * Find monsters by size.
     */
    List<E26Monster> findBySize(String size);

    /**
     * Find monsters by threat level and type.
     */
    List<E26Monster> findByThreatLevelAndType(int threatLevel, String type);

    /**
     * Find monsters by type and active status.
     */
    List<E26Monster> findByTypeAndActive(String type, boolean active);

    /**
     * Find monsters by threat level range.
     */
    @Query("SELECT m FROM E26Monster m WHERE m.threatLevel BETWEEN :minTL AND :maxTL AND m.active = true")
    List<E26Monster> findByThreatLevelRange(@Param("minTL") int minTL, @Param("maxTL") int maxTL);

    /**
     * Find monsters by subtype.
     */
    List<E26Monster> findBySubtype(String subtype);

    /**
     * Find monsters by size and active status.
     */
    List<E26Monster> findBySizeAndActive(String size, boolean active);

    /**
     * Find monsters by fictional/real flag.
     */
    List<E26Monster> findByIsFictional(boolean isFictional);

    /**
     * Find monsters by type and fictional/real flag.
     */
    List<E26Monster> findByTypeAndIsFictional(String type, boolean isFictional);
}
