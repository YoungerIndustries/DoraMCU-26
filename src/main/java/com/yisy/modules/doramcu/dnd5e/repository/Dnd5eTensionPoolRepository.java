package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eTensionPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Dnd5eTensionPoolRepository extends JpaRepository<Dnd5eTensionPool, Long> {

    List<Dnd5eTensionPool> findByAreaId(String areaId);

    List<Dnd5eTensionPool> findByAreaIdAndActiveTrue(String areaId);

    List<Dnd5eTensionPool> findByActiveTrue();

    List<Dnd5eTensionPool> findByNameContainingIgnoreCase(String name);

    @Query("SELECT t FROM Dnd5eTensionPool t WHERE t.active = true AND " +
           "t.currentTension >= :tension")
    List<Dnd5eTensionPool> findByTensionAtLeast(@Param("tension") int tension);

    @Query("SELECT t FROM Dnd5eTensionPool t WHERE t.active = true AND " +
           "t.currentTension = t.maxTension")
    List<Dnd5eTensionPool> findMaxedTensionPools();

    @Query("SELECT t FROM Dnd5eTensionPool t WHERE t.active = true AND " +
           "t.currentTension = 0")
    List<Dnd5eTensionPool> findEmptyTensionPools();

    @Query("SELECT DISTINCT t.areaId FROM Dnd5eTensionPool t WHERE t.active = true")
    List<String> findDistinctAreaIds();

    @Query("SELECT t FROM Dnd5eTensionPool t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.areaId) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dnd5eTensionPool> searchByKeyword(@Param("keyword") String keyword);
}
