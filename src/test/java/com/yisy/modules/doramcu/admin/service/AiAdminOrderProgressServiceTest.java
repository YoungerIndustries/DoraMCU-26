package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.repository.AiAdminOrderRepository;
import com.yisy.modules.doramcu.controller.misc.OrderProgressWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AiAdminOrderProgressService Tests")
class AiAdminOrderProgressServiceTest {

    @Mock
    private AiAdminOrderRepository orderRepository;

    @Mock
    private OrderProgressWebSocketHandler orderProgressWebSocketHandler;

    private AiAdminOrderProgressService progressService;

    private AiAdminOrder order;

    @BeforeEach
    void setUp() {
        progressService = new AiAdminOrderProgressService(orderRepository, orderProgressWebSocketHandler);

        order = new AiAdminOrder("GalaxyOverseer:Alpha", "Deploy more ships", "API", "GalaxyOverseer:Alpha");
        order.setId(100L);
        order.setStatus(AiAdminOrder.OrderStatus.RUNNING);
        order.setProgressPercent(0);
        order.setCurrentTestName(null);
        order.setTestsCompleted(0);
        order.setTestsTotal(10);
        order.setCreatedAt(LocalDateTime.now());
    }

    // ------------------------------------------------------------------
    // updateProgress tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("updateProgress should update all progress fields")
    void updateProgress_shouldUpdateAllFields() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(AiAdminOrder.class))).thenReturn(order);

        progressService.updateProgress(100L, "testApi", 5, 10);

        assertEquals("testApi", order.getCurrentTestName());
        assertEquals(5, order.getTestsCompleted());
        assertEquals(10, order.getTestsTotal());
        assertEquals(50, order.getProgressPercent());
        verify(orderRepository).save(order);
        verify(orderProgressWebSocketHandler).broadcastOrderProgress(eq(100L), any(Map.class));
    }

    @Test
    @DisplayName("updateProgress should cap progress at 100")
    void updateProgress_shouldCapProgressAt100() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(AiAdminOrder.class))).thenReturn(order);

        progressService.updateProgress(100L, "testApi", 15, 10);

        assertEquals(100, order.getProgressPercent());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("updateProgress should do nothing for non-existent order")
    void updateProgress_shouldDoNothingForNonExistentOrder() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        progressService.updateProgress(999L, "testApi", 5, 10);

        verify(orderRepository, never()).save(any());
        verify(orderProgressWebSocketHandler, never()).broadcastOrderProgress(anyLong(), any(Map.class));
    }

    @Test
    @DisplayName("updateProgress should set 0 progress when total is 0")
    void updateProgress_shouldSetZeroProgressWhenTotalIsZero() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(AiAdminOrder.class))).thenReturn(order);

        progressService.updateProgress(100L, "testApi", 0, 0);

        assertEquals(0, order.getProgressPercent());
    }

    // ------------------------------------------------------------------
    // setTestsTotal tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("setTestsTotal should reset progress fields")
    void setTestsTotal_shouldResetProgressFields() {
        order.setProgressPercent(50);
        order.setTestsCompleted(5);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(AiAdminOrder.class))).thenReturn(order);

        progressService.setTestsTotal(100L, 20);

        assertEquals(20, order.getTestsTotal());
        assertEquals(0, order.getTestsCompleted());
        assertEquals(0, order.getProgressPercent());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("setTestsTotal should do nothing for non-existent order")
    void setTestsTotal_shouldDoNothingForNonExistentOrder() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        progressService.setTestsTotal(999L, 10);

        verify(orderRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // setCurrentTest tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("setCurrentTest should update test name")
    void setCurrentTest_shouldUpdateTestName() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(AiAdminOrder.class))).thenReturn(order);

        progressService.setCurrentTest(100L, "testNewGame");

        assertEquals("testNewGame", order.getCurrentTestName());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("setCurrentTest should do nothing for non-existent order")
    void setCurrentTest_shouldDoNothingForNonExistentOrder() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        progressService.setCurrentTest(999L, "testApi");

        verify(orderRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // incrementTestsCompleted tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("incrementTestsCompleted should increment and update progress")
    void incrementTestsCompleted_shouldIncrementAndUpdateProgress() {
        order.setTestsCompleted(4);
        order.setTestsTotal(10);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(AiAdminOrder.class))).thenReturn(order);

        progressService.incrementTestsCompleted(100L);

        assertEquals(5, order.getTestsCompleted());
        assertEquals(50, order.getProgressPercent());
        verify(orderRepository).save(order);
        verify(orderProgressWebSocketHandler).broadcastOrderProgress(eq(100L), any(Map.class));
    }

    @Test
    @DisplayName("incrementTestsCompleted should handle null completed")
    void incrementTestsCompleted_shouldHandleNullCompleted() {
        order.setTestsCompleted(null);
        order.setTestsTotal(10);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(AiAdminOrder.class))).thenReturn(order);

        progressService.incrementTestsCompleted(100L);

        assertEquals(1, order.getTestsCompleted());
        assertEquals(10, order.getProgressPercent());
    }

    @Test
    @DisplayName("incrementTestsCompleted should cap at 100")
    void incrementTestsCompleted_shouldCapAt100() {
        order.setTestsCompleted(9);
        order.setTestsTotal(10);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(AiAdminOrder.class))).thenReturn(order);

        progressService.incrementTestsCompleted(100L);

        assertEquals(10, order.getTestsCompleted());
        assertEquals(100, order.getProgressPercent());
    }

    @Test
    @DisplayName("incrementTestsCompleted should do nothing for non-existent order")
    void incrementTestsCompleted_shouldDoNothingForNonExistentOrder() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        progressService.incrementTestsCompleted(999L);

        verify(orderRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // getOrderProgress tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getOrderProgress should return progress DTO")
    void getOrderProgress_shouldReturnProgressDTO() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        Optional<AiAdminOrderProgressService.OrderProgress> result = progressService.getOrderProgress(100L);

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getOrderId());
        assertEquals("RUNNING", result.get().getStatus());
        assertEquals(0, result.get().getProgressPercent());
        assertEquals(0, result.get().getTestsCompleted());
        assertEquals(10, result.get().getTestsTotal());
        assertEquals("GalaxyOverseer:Alpha", result.get().getSubmittedBy());
    }

    @Test
    @DisplayName("getOrderProgress should return empty for non-existent order")
    void getOrderProgress_shouldReturnEmptyForNonExistentOrder() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<AiAdminOrderProgressService.OrderProgress> result = progressService.getOrderProgress(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getOrderProgress should handle null progress fields")
    void getOrderProgress_shouldHandleNullProgressFields() {
        order.setProgressPercent(null);
        order.setTestsCompleted(null);
        order.setTestsTotal(null);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        Optional<AiAdminOrderProgressService.OrderProgress> result = progressService.getOrderProgress(100L);

        assertTrue(result.isPresent());
        assertEquals(0, result.get().getProgressPercent());
        assertEquals(0, result.get().getTestsCompleted());
        assertEquals(0, result.get().getTestsTotal());
    }

    // ------------------------------------------------------------------
    // getRunningOrdersProgress tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getRunningOrdersProgress should return all running orders")
    void getRunningOrdersProgress_shouldReturnAllRunningOrders() {
        AiAdminOrder order2 = new AiAdminOrder("GalaxyOverseer:Beta", "Repair station", "GAME", "GalaxyOverseer:Beta");
        order2.setId(101L);
        order2.setStatus(AiAdminOrder.OrderStatus.RUNNING);
        order2.setProgressPercent(75);
        order2.setTestsCompleted(3);
        order2.setTestsTotal(4);

        when(orderRepository.findByStatus(AiAdminOrder.OrderStatus.RUNNING))
                .thenReturn(List.of(order, order2));

        List<AiAdminOrderProgressService.OrderProgress> result = progressService.getRunningOrdersProgress();

        assertEquals(2, result.size());
        assertEquals(100L, result.get(0).getOrderId());
        assertEquals(101L, result.get(1).getOrderId());
    }

    @Test
    @DisplayName("getRunningOrdersProgress should return empty when no running orders")
    void getRunningOrdersProgress_shouldReturnEmptyWhenNoRunningOrders() {
        when(orderRepository.findByStatus(AiAdminOrder.OrderStatus.RUNNING))
                .thenReturn(List.of());

        List<AiAdminOrderProgressService.OrderProgress> result = progressService.getRunningOrdersProgress();

        assertTrue(result.isEmpty());
    }

    // ------------------------------------------------------------------
    // broadcastProgress tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("broadcastProgress should call WebSocket handler")
    void broadcastProgress_shouldCallWebSocketHandler() {
        order.setProgressPercent(50);
        order.setCurrentTestName("testApi");
        order.setTestsCompleted(5);
        order.setTestsTotal(10);

        progressService.broadcastProgress(order);

        verify(orderProgressWebSocketHandler).broadcastOrderProgress(eq(100L), any(Map.class));
    }

    @Test
    @DisplayName("broadcastProgress should include all progress fields")
    void broadcastProgress_shouldIncludeAllFields() {
        order.setStatus(AiAdminOrder.OrderStatus.COMPLETED);
        order.setProgressPercent(100);
        order.setCurrentTestName("testFinal");
        order.setTestsCompleted(10);
        order.setTestsTotal(10);

        progressService.broadcastProgress(order);

        verify(orderProgressWebSocketHandler).broadcastOrderProgress(eq(100L), argThat(data -> {
            assertEquals("COMPLETED", data.get("status"));
            assertEquals(100, data.get("progressPercent"));
            assertEquals("testFinal", data.get("currentTestName"));
            assertEquals(10, data.get("testsCompleted"));
            assertEquals(10, data.get("testsTotal"));
            return true;
        }));
    }

    @Test
    @DisplayName("broadcastProgress should handle handler exception gracefully")
    void broadcastProgress_shouldHandleHandlerExceptionGracefully() {
        order.setProgressPercent(50);
        doThrow(new RuntimeException("WebSocket error")).when(orderProgressWebSocketHandler)
                .broadcastOrderProgress(anyLong(), any(Map.class));

        // Should not throw
        assertDoesNotThrow(() -> progressService.broadcastProgress(order));
    }
}
