package com.yisy.modules.doramcu.corporation.stock.repository;

import com.yisy.modules.doramcu.corporation.stock.model.DividendPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DividendPaymentRepository extends JpaRepository<DividendPayment, Long> {

    List<DividendPayment> findByStockListingId(Long stockListingId);

    List<DividendPayment> findByStockListingIdOrderByPaymentDateDesc(Long stockListingId);

    Optional<DividendPayment> findByStockListingIdAndQuarter(Long stockListingId, String quarter);

    List<DividendPayment> findByQuarter(String quarter);
}
