package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.repository.AiAdminOrderRepository;
import com.yisy.modules.doramcu.controller.misc.OrderProgressWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for tracking and broadcasting AI admin order progress.
 * Provides real-time progress updates via WebSocket and polling endpoints.
 */
@Service
public class AiAdminOrderProgressService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminOrderProgressService.class);

    private final AiAdminOrderRepository orderRepository;
    private final OrderProgressWebSocketHandler orderProgressWebSocketHandler;

    public AiAdminOrderProgressService(
            AiAdminOrderRepository orderRepository,
            @Lazy OrderProgressWebSocketHandler orderProgressWebSocketHandler) {
        this.orderRepository = orderRepository;
        this.orderProgressWebSocketHandler = orderProgressWebSocketHandler;
    }

    /**
     * Updates the progress fields for an order.
     */
    @Transactional
    public void updateProgress(Long orderId, String currentTestName, int testsCompleted, int testsTotal) {
        Optional<AiAdminOrder> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return;
        }

        AiAdminOrder order = orderOpt.get();
        order.setCurrentTestName(currentTestName);
        order.setTestsCompleted(testsCompleted);
        order.setTestsTotal(testsTotal);

        // Calculate progress percentage
        int progress = testsTotal > 0 ? (int) ((testsCompleted * 100.0) / testsTotal) : 0;
        order.setProgressPercent(Math.min(progress, 100));
        orderRepository.save(order);

        // Broadcast progress update via WebSocket
        broadcastProgress(order);
    }

    /**
     * Sets the total number of tests expected for an order (called before execution starts).
     */
    @Transactional
    public void setTestsTotal(Long orderId, int testsTotal) {
        Optional<AiAdminOrder> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return;
        }

        AiAdminOrder order = orderOpt.get();
        order.setTestsTotal(testsTotal);
        order.setTestsCompleted(0);
        order.setProgressPercent(0);
        orderRepository.save(order);
    }

    /**
     * Sets the current test name being executed.
     */
    @Transactional
    public void setCurrentTest(Long orderId, String testName) {
        Optional<AiAdminOrder> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return;
        }

        AiAdminOrder order = orderOpt.get();
        order.setCurrentTestName(testName);
        orderRepository.save(order);
    }

    /**
     * Increments the tests completed count and updates progress.
     */
    @Transactional
    public void incrementTestsCompleted(Long orderId) {
        Optional<AiAdminOrder> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return;
        }

        AiAdminOrder order = orderOpt.get();
        int completed = order.getTestsCompleted() != null ? order.getTestsCompleted() + 1 : 1;
        int total = order.getTestsTotal() != null ? order.getTestsTotal() : 0;
        order.setTestsCompleted(completed);

        int progress = total > 0 ? (int) ((completed * 100.0) / total) : 0;
        order.setProgressPercent(Math.min(progress, 100));
        orderRepository.save(order);

        broadcastProgress(order);
    }

    /**
     * Gets the current progress for an order.
     */
    @Transactional(readOnly = true)
    public Optional<OrderProgress> getOrderProgress(Long orderId) {
        Optional<AiAdminOrder> orderOpt = orderRepository.findById(orderId);
        return orderOpt.map(this::toOrderProgress);
    }

    /**
     * Gets progress for all running orders.
     */
    @Transactional(readOnly = true)
    public List<OrderProgress> getRunningOrdersProgress() {
        List<AiAdminOrder> runningOrders = orderRepository.findByStatus(AiAdminOrder.OrderStatus.RUNNING);
        return runningOrders.stream()
                .map(this::toOrderProgress)
                .toList();
    }

    /**
     * Broadcasts a progress update via WebSocket to subscribers of this order.
     */
    public void broadcastProgress(AiAdminOrder order) {
        try {
            Map<String, Object> progressData = new HashMap<>();
            progressData.put("status", order.getStatus().name());
            progressData.put("progressPercent", order.getProgressPercent());
            progressData.put("currentTestName", order.getCurrentTestName());
            progressData.put("testsCompleted", order.getTestsCompleted());
            progressData.put("testsTotal", order.getTestsTotal());

            orderProgressWebSocketHandler.broadcastOrderProgress(order.getId(), progressData);
            log.debug("Broadcast progress update for order {}: {}%", order.getId(), order.getProgressPercent());
        } catch (Exception e) {
            log.warn("Failed to broadcast progress for order {}: {}", order.getId(), e.getMessage());
        }
    }

    private OrderProgress toOrderProgress(AiAdminOrder order) {
        OrderProgress progress = new OrderProgress();
        progress.setOrderId(order.getId());
        progress.setStatus(order.getStatus().name());
        progress.setProgressPercent(order.getProgressPercent() != null ? order.getProgressPercent() : 0);
        progress.setCurrentTestName(order.getCurrentTestName());
        progress.setTestsCompleted(order.getTestsCompleted() != null ? order.getTestsCompleted() : 0);
        progress.setTestsTotal(order.getTestsTotal() != null ? order.getTestsTotal() : 0);
        progress.setSubmittedBy(order.getSubmittedBy());
        progress.setOrderText(order.getOrderText());
        progress.setCreatedAt(order.getCreatedAt());
        return progress;
    }

    /**
     * DTO representing the current progress of an order.
     */
    public static class OrderProgress {
        private Long orderId;
        private String status;
        private Integer progressPercent;
        private String currentTestName;
        private Integer testsCompleted;
        private Integer testsTotal;
        private String submittedBy;
        private String orderText;
        private java.time.LocalDateTime createdAt;

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Integer getProgressPercent() { return progressPercent; }
        public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }
        public String getCurrentTestName() { return currentTestName; }
        public void setCurrentTestName(String currentTestName) { this.currentTestName = currentTestName; }
        public Integer getTestsCompleted() { return testsCompleted; }
        public void setTestsCompleted(Integer testsCompleted) { this.testsCompleted = testsCompleted; }
        public Integer getTestsTotal() { return testsTotal; }
        public void setTestsTotal(Integer testsTotal) { this.testsTotal = testsTotal; }
        public String getSubmittedBy() { return submittedBy; }
        public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }
        public String getOrderText() { return orderText; }
        public void setOrderText(String orderText) { this.orderText = orderText; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
