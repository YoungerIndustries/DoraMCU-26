package com.yisy.modules.doramcu.corporation.repository;

import com.yisy.modules.doramcu.corporation.model.CorporationSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying corporation save snapshots.
 */
@Repository
public interface CorporationSaveRepository extends JpaRepository<CorporationSave, Long> {

    /**
     * Find all saves for a specific corporation, ordered by timestamp descending.
     *
     * @param corporationId the corporation ID
     * @return list of saves
     */
    @Query("SELECT s FROM CorporationSave s WHERE s.corporationId = :corpId ORDER BY s.timestamp DESC")
    List<CorporationSave> findSavesByCorporationId(@Param("corpId") Long corporationId);

    /**
     * Delete all saves for a specific corporation.
     *
     * @param corporationId the corporation ID
     */
    void deleteByCorporationId(Long corporationId);

    /**
     * Count saves for a specific corporation.
     *
     * @param corporationId the corporation ID
     * @return number of saves
     */
    long countByCorporationId(Long corporationId);
}
