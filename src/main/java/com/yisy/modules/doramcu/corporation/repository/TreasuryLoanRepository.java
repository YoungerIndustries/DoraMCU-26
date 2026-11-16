package com.yisy.modules.doramcu.corporation.repository;

import com.yisy.modules.doramcu.corporation.model.TreasuryLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreasuryLoanRepository extends JpaRepository<TreasuryLoan, Long> {

    /**
     * Find the loan for a specific corporation.
     */
    Optional<TreasuryLoan> findByCorporationId(Long corporationId);

    /**
     * Find all active loans.
     */
    List<TreasuryLoan> findByStatus(TreasuryLoan.LoanStatus status);

    /**
     * Check if a corporation already has a loan.
     */
    boolean existsByCorporationId(Long corporationId);
}
