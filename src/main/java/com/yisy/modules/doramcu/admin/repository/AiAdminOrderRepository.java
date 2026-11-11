package com.yisy.modules.doramcu.admin.repository;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiAdminOrderRepository extends JpaRepository<AiAdminOrder, Long> {

    List<AiAdminOrder> findByStatus(AiAdminOrder.OrderStatus status);

    List<AiAdminOrder> findBySubmittedBy(String submittedBy);
}
