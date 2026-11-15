package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.WorkOrder;
import com.yisy.modules.doramcu.earth2026.repository.WorkOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@DisplayName("WorkOrderService Tests")
@ExtendWith(MockitoExtension.class)
class WorkOrderServiceTest {

    @Mock
    private WorkOrderRepository workOrderRepository;

    @InjectMocks
    private WorkOrderService workOrderService;

    private WorkOrder pendingOrder;
    private WorkOrder assignedOrder;
    private WorkOrder inProgressOrder;

    @BeforeEach
    void setUp() {
        pendingOrder = createWorkOrder(1L, "Mine Iron Ore", WorkOrder.WorkOrderStatus.PENDING,
                1L, null, 10L, 5);
        assignedOrder = createWorkOrder(2L, "Produce Steel", WorkOrder.WorkOrderStatus.ASSIGNED,
                1L, 2L, 20L, 7);
        inProgressOrder = createWorkOrder(3L, "Restock Store", WorkOrder.WorkOrderStatus.IN_PROGRESS,
                1L, 3L, 30L, 3);
    }

    private WorkOrder createWorkOrder(Long id, String title, WorkOrder.WorkOrderStatus status,
                                      Long requestingNpcId, Long assignedWorkerId,
                                      Long structureId, int priority) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setId(id);
        workOrder.setTitle(title);
        workOrder.setDescription("Test description for " + title);
        workOrder.setRequestingNpcId(requestingNpcId);
        workOrder.setAssignedWorkerId(assignedWorkerId);
        workOrder.setStatus(status);
        workOrder.setStructureId(structureId);
        workOrder.setPriority(priority);
        workOrder.setRequiredResourcesJson("{}");
        workOrder.setOutputItemsJson("{\"items\": 100}");
        return workOrder;
    }

    @Nested
    @DisplayName("Query Methods")
    class QueryTests {

        @Test
        @DisplayName("should return all work orders")
        void shouldReturnAllWorkOrders() {
            List<WorkOrder> orders = Arrays.asList(pendingOrder, assignedOrder, inProgressOrder);
            when(workOrderRepository.findAll()).thenReturn(orders);

            List<WorkOrder> result = workOrderService.getAllWorkOrders();

            assertEquals(3, result.size());
            verify(workOrderRepository).findAll();
        }

        @Test
        @DisplayName("should return work order by ID")
        void shouldReturnWorkOrderById() {
            when(workOrderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

            Optional<WorkOrder> result = workOrderService.getWorkOrderById(1L);

            assertTrue(result.isPresent());
            assertEquals("Mine Iron Ore", result.get().getTitle());
        }

        @Test
        @DisplayName("should return empty for non-existent ID")
        void shouldReturnEmptyForNonExistentId() {
            when(workOrderRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<WorkOrder> result = workOrderService.getWorkOrderById(999L);

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should return work orders by status")
        void shouldReturnWorkOrdersByStatus() {
            when(workOrderRepository.findByStatus(WorkOrder.WorkOrderStatus.PENDING))
                    .thenReturn(Arrays.asList(pendingOrder));

            List<WorkOrder> result = workOrderService.getWorkOrdersByStatus(WorkOrder.WorkOrderStatus.PENDING);

            assertEquals(1, result.size());
            assertEquals(WorkOrder.WorkOrderStatus.PENDING, result.get(0).getStatus());
        }

        @Test
        @DisplayName("should return work orders by requesting NPC")
        void shouldReturnWorkOrdersByRequestingNpc() {
            when(workOrderRepository.findByRequestingNpcId(1L))
                    .thenReturn(Arrays.asList(pendingOrder, assignedOrder));

            List<WorkOrder> result = workOrderService.getWorkOrdersByRequestingNpc(1L);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("should return work orders by assigned worker")
        void shouldReturnWorkOrdersByAssignedWorker() {
            when(workOrderRepository.findByAssignedWorkerId(2L))
                    .thenReturn(Arrays.asList(assignedOrder));

            List<WorkOrder> result = workOrderService.getWorkOrdersByAssignedWorker(2L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("should return work orders by structure")
        void shouldReturnWorkOrdersByStructure() {
            when(workOrderRepository.findByStructureId(10L))
                    .thenReturn(Arrays.asList(pendingOrder));

            List<WorkOrder> result = workOrderService.getWorkOrdersByStructure(10L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("should return pending work orders")
        void shouldReturnPendingWorkOrders() {
            when(workOrderRepository.findPendingWorkOrders())
                    .thenReturn(Arrays.asList(pendingOrder));

            List<WorkOrder> result = workOrderService.getPendingWorkOrders();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("should return work orders by status and structure")
        void shouldReturnWorkOrdersByStatusAndStructure() {
            when(workOrderRepository.findByStatusAndStructureId(
                    WorkOrder.WorkOrderStatus.PENDING, 10L))
                    .thenReturn(Arrays.asList(pendingOrder));

            List<WorkOrder> result =
                    workOrderService.getWorkOrdersByStatusAndStructure(
                            WorkOrder.WorkOrderStatus.PENDING, 10L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("should count work orders by status")
        void shouldCountWorkOrdersByStatus() {
            when(workOrderRepository.countByStatus(WorkOrder.WorkOrderStatus.PENDING)).thenReturn(5L);

            long count = workOrderService.countByStatus(WorkOrder.WorkOrderStatus.PENDING);

            assertEquals(5, count);
        }
    }

    @Nested
    @DisplayName("Create Work Order")
    class CreateTests {

        @Test
        @DisplayName("should create and save a new work order")
        void shouldCreateAndSaveWorkOrder() {
            when(workOrderRepository.save(any(WorkOrder.class))).thenReturn(pendingOrder);

            WorkOrder result = workOrderService.createWorkOrder(pendingOrder);

            assertEquals("Mine Iron Ore", result.getTitle());
            verify(workOrderRepository).save(pendingOrder);
        }
    }

    @Nested
    @DisplayName("Assign Worker")
    class AssignWorkerTests {

        @Test
        @DisplayName("should assign worker to pending work order")
        void shouldAssignWorkerToPendingOrder() {
            when(workOrderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
            when(workOrderRepository.save(any(WorkOrder.class))).thenReturn(pendingOrder);

            Optional<WorkOrder> result = workOrderService.assignWorker(1L, 5L);

            assertTrue(result.isPresent());
            assertEquals(WorkOrder.WorkOrderStatus.ASSIGNED, result.get().getStatus());
            assertEquals(5L, result.get().getAssignedWorkerId());
        }

        @Test
        @DisplayName("should not assign worker to non-pending work order")
        void shouldNotAssignWorkerToNonPendingOrder() {
            when(workOrderRepository.findById(2L)).thenReturn(Optional.of(assignedOrder));

            Optional<WorkOrder> result = workOrderService.assignWorker(2L, 5L);

            assertTrue(result.isPresent());
            assertEquals(WorkOrder.WorkOrderStatus.ASSIGNED, result.get().getStatus());
            verify(workOrderRepository, never()).save(any());
        }

        @Test
        @DisplayName("should return empty for non-existent work order")
        void shouldReturnEmptyForNonExistentOrder() {
            when(workOrderRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<WorkOrder> result = workOrderService.assignWorker(999L, 5L);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("Start Work Order")
    class StartWorkOrderTests {

        @Test
        @DisplayName("should start assigned work order")
        void shouldStartAssignedWorkOrder() {
            when(workOrderRepository.findById(2L)).thenReturn(Optional.of(assignedOrder));
            when(workOrderRepository.save(any(WorkOrder.class))).thenReturn(assignedOrder);

            Optional<WorkOrder> result = workOrderService.startWorkOrder(2L);

            assertTrue(result.isPresent());
            assertEquals(WorkOrder.WorkOrderStatus.IN_PROGRESS, result.get().getStatus());
        }

        @Test
        @DisplayName("should not start non-assigned work order")
        void shouldNotStartNonAssignedWorkOrder() {
            when(workOrderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

            Optional<WorkOrder> result = workOrderService.startWorkOrder(1L);

            assertTrue(result.isPresent());
            assertEquals(WorkOrder.WorkOrderStatus.PENDING, result.get().getStatus());
            verify(workOrderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Complete Work Order")
    class CompleteWorkOrderTests {

        @Test
        @DisplayName("should complete in-progress work order")
        void shouldCompleteInProgressWorkOrder() {
            when(workOrderRepository.findById(3L)).thenReturn(Optional.of(inProgressOrder));
            when(workOrderRepository.save(any(WorkOrder.class))).thenReturn(inProgressOrder);

            Optional<WorkOrder> result = workOrderService.completeWorkOrder(3L);

            assertTrue(result.isPresent());
            assertEquals(WorkOrder.WorkOrderStatus.COMPLETED, result.get().getStatus());
            assertNotNull(result.get().getCompletedAt());
        }

        @Test
        @DisplayName("should not complete non-in-progress work order")
        void shouldNotCompleteNonInProgressWorkOrder() {
            when(workOrderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

            Optional<WorkOrder> result = workOrderService.completeWorkOrder(1L);

            assertTrue(result.isPresent());
            assertEquals(WorkOrder.WorkOrderStatus.PENDING, result.get().getStatus());
            verify(workOrderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Fail Work Order")
    class FailWorkOrderTests {

        @Test
        @DisplayName("should fail in-progress work order")
        void shouldFailInProgressWorkOrder() {
            when(workOrderRepository.findById(3L)).thenReturn(Optional.of(inProgressOrder));
            when(workOrderRepository.save(any(WorkOrder.class))).thenReturn(inProgressOrder);

            Optional<WorkOrder> result = workOrderService.failWorkOrder(3L, "Equipment broken");

            assertTrue(result.isPresent());
            assertEquals(WorkOrder.WorkOrderStatus.FAILED, result.get().getStatus());
            assertTrue(result.get().getDescription().contains("FAILED"));
            assertNotNull(result.get().getCompletedAt());
        }

        @Test
        @DisplayName("should not fail completed work order")
        void shouldNotFailCompletedWorkOrder() {
            WorkOrder completedOrder = createWorkOrder(4L, "Done", WorkOrder.WorkOrderStatus.COMPLETED,
                    1L, 2L, 10L, 5);
            when(workOrderRepository.findById(4L)).thenReturn(Optional.of(completedOrder));

            Optional<WorkOrder> result = workOrderService.failWorkOrder(4L, "Error");

            assertTrue(result.isPresent());
            assertEquals(WorkOrder.WorkOrderStatus.COMPLETED, result.get().getStatus());
            verify(workOrderRepository, never()).save(any());
        }

        @Test
        @DisplayName("should fail with null reason")
        void shouldFailWithNullReason() {
            when(workOrderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
            when(workOrderRepository.save(any(WorkOrder.class))).thenReturn(pendingOrder);

            Optional<WorkOrder> result = workOrderService.failWorkOrder(1L, null);

            assertTrue(result.isPresent());
            assertEquals(WorkOrder.WorkOrderStatus.FAILED, result.get().getStatus());
        }
    }

    @Nested
    @DisplayName("Update Priority")
    class UpdatePriorityTests {

        @Test
        @DisplayName("should update priority of work order")
        void shouldUpdatePriority() {
            when(workOrderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
            when(workOrderRepository.save(any(WorkOrder.class))).thenReturn(pendingOrder);

            Optional<WorkOrder> result = workOrderService.updatePriority(1L, 10);

            assertTrue(result.isPresent());
            assertEquals(10, result.get().getPriority());
        }

        @Test
        @DisplayName("should return empty for non-existent work order")
        void shouldReturnEmptyForNonExistentOrder() {
            when(workOrderRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<WorkOrder> result = workOrderService.updatePriority(999L, 10);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("Delete Work Order")
    class DeleteTests {

        @Test
        @DisplayName("should delete existing work order and return true")
        void shouldDeleteExistingWorkOrder() {
            when(workOrderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
            doNothing().when(workOrderRepository).delete(pendingOrder);

            boolean result = workOrderService.deleteWorkOrder(1L);

            assertTrue(result);
            verify(workOrderRepository).delete(pendingOrder);
        }

        @Test
        @DisplayName("should return false for non-existent ID")
        void shouldReturnFalseForNonExistentId() {
            when(workOrderRepository.findById(999L)).thenReturn(Optional.empty());

            boolean result = workOrderService.deleteWorkOrder(999L);

            assertFalse(result);
            verify(workOrderRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Work Order Lifecycle")
    class LifecycleTests {

        @Test
        @DisplayName("should complete full lifecycle: create → assign → start → complete")
        void shouldCompleteFullLifecycle() {
            // Create
            when(workOrderRepository.save(any(WorkOrder.class))).thenReturn(pendingOrder);
            WorkOrder created = workOrderService.createWorkOrder(pendingOrder);
            assertEquals(WorkOrder.WorkOrderStatus.PENDING, created.getStatus());

            // Assign
            when(workOrderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
            Optional<WorkOrder> assigned = workOrderService.assignWorker(1L, 5L);
            assertTrue(assigned.isPresent());
            assertEquals(WorkOrder.WorkOrderStatus.ASSIGNED, assigned.get().getStatus());

            // Start
            when(workOrderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
            Optional<WorkOrder> started = workOrderService.startWorkOrder(1L);
            assertTrue(started.isPresent());
            assertEquals(WorkOrder.WorkOrderStatus.IN_PROGRESS, started.get().getStatus());

            // Complete
            when(workOrderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
            Optional<WorkOrder> completed = workOrderService.completeWorkOrder(1L);
            assertTrue(completed.isPresent());
            assertEquals(WorkOrder.WorkOrderStatus.COMPLETED, completed.get().getStatus());
        }
    }
}
