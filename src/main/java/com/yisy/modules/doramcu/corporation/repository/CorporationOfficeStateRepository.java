package com.yisy.modules.doramcu.corporation.repository;

import com.yisy.modules.doramcu.corporation.model.CorporationOfficeState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for querying corporation office state.
 */
@Repository
public interface CorporationOfficeStateRepository extends JpaRepository<CorporationOfficeState, Long> {

    /**
     * Find office state for a specific corporation.
     *
     * @param corporationId the corporation ID
     * @return optional office state
     */
    Optional<CorporationOfficeState> findByCorporationId(Long corporationId);
}
