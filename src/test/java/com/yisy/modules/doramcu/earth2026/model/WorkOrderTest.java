package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WorkOrder Tests")
class WorkOrderTest {

    @Nested
    @DisplayName("WorkOrderStatus Enum")
    class WorkOrderStatusTests {

        @Test
        @DisplayName("should have all expected statuses")
        void shouldHaveAllExpectedStatuses() {
            WorkOrder.WorkOrderStatus[] statuses = WorkOrder.WorkOrderStatus.values();
            assertEquals(5, statuses.length);
        }

        @Test
        @DisplayName("should return correct display name for PENDING")
        void shouldReturnDisplayNameForPending() {
            assertEquals("Pending", WorkOrder.WorkOrderStatus.PENDING.getDisplayName());
        }

        @Test
        @DisplayName("should return correct description for PENDING")
        void shouldReturnDescriptionForPending() {
            assertEquals("Work order created, waiting for assignment",
                    WorkOrder.WorkOrderStatus.PENDING.getDescription());
        }

        @Test
        @DisplayName("should return correct display name for ASSIGNED")
        void shouldReturnDisplayNameForAssigned() {
            assertEquals("Assigned", WorkOrder.WorkOrderStatus.ASSIGNED.getDisplayName());
        }

        @Test
        @DisplayName("should return correct display name for IN_PROGRESS")
        void shouldReturnDisplayNameForInProgress() {
            assertEquals("In Progress", WorkOrder.WorkOrderStatus.IN_PROGRESS.getDisplayName());
        }

        @Test
        @DisplayName("should return correct display name for COMPLETED")
        void shouldReturnDisplayNameForCompleted() {
            assertEquals("Completed", WorkOrder.WorkOrderStatus.COMPLETED.getDisplayName());
        }

        @Test
        @DisplayName("should return correct display name for FAILED")
        void shouldReturnDisplayNameForFailed() {
            assertEquals("Failed", WorkOrder.WorkOrderStatus.FAILED.getDisplayName());
        }
    }

    @Nested
    @DisplayName("Entity Fields")
    class EntityFieldTests {

        @Test
        @DisplayName("should set and get title")
        void shouldSetAndGetTitle() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setTitle("Mine Iron Ore");
            assertEquals("Mine Iron Ore", workOrder.getTitle());
        }

        @Test
        @DisplayName("should set and get description")
        void shouldSetAndGetDescription() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setDescription("Extract iron ore from the mine");
            assertEquals("Extract iron ore from the mine", workOrder.getDescription());
        }

        @Test
        @DisplayName("should set and get requesting NPC ID")
        void shouldSetAndGetRequestingNpcId() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setRequestingNpcId(1L);
            assertEquals(1L, workOrder.getRequestingNpcId());
        }

        @Test
        @DisplayName("should set and get assigned worker ID")
        void shouldSetAndGetAssignedWorkerId() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setAssignedWorkerId(2L);
            assertEquals(2L, workOrder.getAssignedWorkerId());
        }

        @Test
        @DisplayName("should set and get status")
        void shouldSetAndGetStatus() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setStatus(WorkOrder.WorkOrderStatus.PENDING);
            assertEquals(WorkOrder.WorkOrderStatus.PENDING, workOrder.getStatus());
        }

        @Test
        @DisplayName("should set and get required resources JSON")
        void shouldSetAndGetRequiredResourcesJson() {
            WorkOrder workOrder = new WorkOrder();
            String json = "{\"iron_ore\": 100, \"copper\": 50}";
            workOrder.setRequiredResourcesJson(json);
            assertEquals(json, workOrder.getRequiredResourcesJson());
        }

        @Test
        @DisplayName("should set and get output items JSON")
        void shouldSetAndGetOutputItemsJson() {
            WorkOrder workOrder = new WorkOrder();
            String json = "{\"steel_beams\": 50, \"circuit_boards\": 25}";
            workOrder.setOutputItemsJson(json);
            assertEquals(json, workOrder.getOutputItemsJson());
        }

        @Test
        @DisplayName("should set and get priority")
        void shouldSetAndGetPriority() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setPriority(5);
            assertEquals(5, workOrder.getPriority());
        }

        @Test
        @DisplayName("should set and get structure ID")
        void shouldSetAndGetStructureId() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setStructureId(10L);
            assertEquals(10L, workOrder.getStructureId());
        }

        @Test
        @DisplayName("should set and get ID")
        void shouldSetAndGetId() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setId(42L);
            assertEquals(42L, workOrder.getId());
        }

        @Test
        @DisplayName("should set and get completed at")
        void shouldSetAndGetCompletedAt() {
            WorkOrder workOrder = new WorkOrder();
            LocalDateTime completed = LocalDateTime.of(2026, 11, 15, 12, 0);
            workOrder.setCompletedAt(completed);
            assertEquals(completed, workOrder.getCompletedAt());
        }

        @Test
        @DisplayName("should handle null priority")
        void shouldHandleNullPriority() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setPriority(null);
            assertNull(workOrder.getPriority());
        }

        @Test
        @DisplayName("should handle null NPC IDs")
        void shouldHandleNullNpcIds() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setRequestingNpcId(null);
            workOrder.setAssignedWorkerId(null);
            assertNull(workOrder.getRequestingNpcId());
            assertNull(workOrder.getAssignedWorkerId());
        }
    }

    @Nested
    @DisplayName("Lifecycle Callbacks")
    class LifecycleCallbackTests {

        @Test
        @DisplayName("should set timestamps and default status on create")
        void shouldSetTimestampsAndDefaultsOnCreate() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.onCreate();

            assertNotNull(workOrder.getCreatedAt());
            assertNotNull(workOrder.getUpdatedAt());
            assertEquals(WorkOrder.WorkOrderStatus.PENDING, workOrder.getStatus());
            assertEquals(0, workOrder.getPriority());
        }

        @Test
        @DisplayName("should not override explicit status on create")
        void shouldNotOverrideExplicitStatus() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setStatus(WorkOrder.WorkOrderStatus.ASSIGNED);
            workOrder.onCreate();

            assertEquals(WorkOrder.WorkOrderStatus.ASSIGNED, workOrder.getStatus());
        }

        @Test
        @DisplayName("should not override explicit priority on create")
        void shouldNotOverrideExplicitPriority() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setPriority(10);
            workOrder.onCreate();

            assertEquals(10, workOrder.getPriority());
        }

        @Test
        @DisplayName("should update timestamp on update")
        void shouldUpdateTimestampOnUpdate() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.onCreate();
            LocalDateTime created = workOrder.getCreatedAt();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // ignore
            }

            workOrder.onUpdate();
            assertEquals(created, workOrder.getCreatedAt(), "createdAt should not change");
            assertNotEquals(workOrder.getCreatedAt(), workOrder.getUpdatedAt(),
                    "updatedAt should be newer than createdAt");
        }
    }

    @Nested
    @DisplayName("Status Transitions")
    class StatusTransitionTests {

        @Test
        @DisplayName("should transition from PENDING to ASSIGNED")
        void shouldTransitionPendingToAssigned() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setStatus(WorkOrder.WorkOrderStatus.PENDING);
            assertEquals(WorkOrder.WorkOrderStatus.PENDING, workOrder.getStatus());

            workOrder.setStatus(WorkOrder.WorkOrderStatus.ASSIGNED);
            assertEquals(WorkOrder.WorkOrderStatus.ASSIGNED, workOrder.getStatus());
        }

        @Test
        @DisplayName("should transition from ASSIGNED to IN_PROGRESS")
        void shouldTransitionAssignedToInProgress() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setStatus(WorkOrder.WorkOrderStatus.ASSIGNED);

            workOrder.setStatus(WorkOrder.WorkOrderStatus.IN_PROGRESS);
            assertEquals(WorkOrder.WorkOrderStatus.IN_PROGRESS, workOrder.getStatus());
        }

        @Test
        @DisplayName("should transition from IN_PROGRESS to COMPLETED")
        void shouldTransitionInProgressToCompleted() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setStatus(WorkOrder.WorkOrderStatus.IN_PROGRESS);

            workOrder.setStatus(WorkOrder.WorkOrderStatus.COMPLETED);
            assertEquals(WorkOrder.WorkOrderStatus.COMPLETED, workOrder.getStatus());
        }

        @Test
        @DisplayName("should transition to FAILED from any status")
        void shouldTransitionToFailed() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setStatus(WorkOrder.WorkOrderStatus.IN_PROGRESS);

            workOrder.setStatus(WorkOrder.WorkOrderStatus.FAILED);
            assertEquals(WorkOrder.WorkOrderStatus.FAILED, workOrder.getStatus());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should include title and status in toString")
        void shouldIncludeTitleAndStatus() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setTitle("Test Work Order");
            workOrder.setStatus(WorkOrder.WorkOrderStatus.PENDING);
            workOrder.setPriority(5);
            workOrder.setStructureId(10L);

            String str = workOrder.toString();
            assertTrue(str.contains("Test Work Order"));
            assertTrue(str.contains("PENDING"));
            assertTrue(str.contains("priority=5"));
            assertTrue(str.contains("structureId=10"));
        }
    }

    @Nested
    @DisplayName("Full Work Order Creation")
    class FullWorkOrderTests {

        @Test
        @DisplayName("should create a complete mining work order")
        void shouldCreateCompleteMiningWorkOrder() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setId(1L);
            workOrder.setTitle("Mine San Diego Iron Mine");
            workOrder.setDescription("Extract iron ore from the mine");
            workOrder.setRequestingNpcId(1L);
            workOrder.setStructureId(10L);
            workOrder.setRequiredResourcesJson("{}");
            workOrder.setOutputItemsJson("{\"ore\": 100}");
            workOrder.setPriority(5);
            workOrder.setStatus(WorkOrder.WorkOrderStatus.PENDING);

            assertEquals(1L, workOrder.getId());
            assertEquals("Mine San Diego Iron Mine", workOrder.getTitle());
            assertEquals("Extract iron ore from the mine", workOrder.getDescription());
            assertEquals(1L, workOrder.getRequestingNpcId());
            assertEquals(10L, workOrder.getStructureId());
            assertEquals("{}", workOrder.getRequiredResourcesJson());
            assertEquals("{\"ore\": 100}", workOrder.getOutputItemsJson());
            assertEquals(5, workOrder.getPriority());
            assertEquals(WorkOrder.WorkOrderStatus.PENDING, workOrder.getStatus());
        }

        @Test
        @DisplayName("should create a production work order")
        void shouldCreateProductionWorkOrder() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setTitle("Produce at Steel Factory");
            workOrder.setRequiredResourcesJson("{\"ore\": 50, \"materials\": 25}");
            workOrder.setOutputItemsJson("{\"steel_beams\": 20, \"components\": 10}");
            workOrder.setPriority(7);

            assertEquals("Produce at Steel Factory", workOrder.getTitle());
            assertEquals("{\"ore\": 50, \"materials\": 25}", workOrder.getRequiredResourcesJson());
            assertEquals("{\"steel_beams\": 20, \"components\": 10}", workOrder.getOutputItemsJson());
            assertEquals(7, workOrder.getPriority());
        }

        @Test
        @DisplayName("should create a restocking work order")
        void shouldCreateRestockingWorkOrder() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setTitle("Restock Equipment Store");
            workOrder.setRequiredResourcesJson("{\"items\": 50}");
            workOrder.setOutputItemsJson("{\"inventory\": 50}");
            workOrder.setPriority(3);

            assertEquals("Restock Equipment Store", workOrder.getTitle());
            assertEquals(3, workOrder.getPriority());
        }

        @Test
        @DisplayName("should set completed time on completion")
        void shouldSetCompletedTime() {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setStatus(WorkOrder.WorkOrderStatus.IN_PROGRESS);
            assertNull(workOrder.getCompletedAt());

            LocalDateTime completed = LocalDateTime.of(2026, 11, 15, 12, 0);
            workOrder.setCompletedAt(completed);
            workOrder.setStatus(WorkOrder.WorkOrderStatus.COMPLETED);

            assertEquals(completed, workOrder.getCompletedAt());
            assertEquals(WorkOrder.WorkOrderStatus.COMPLETED, workOrder.getStatus());
        }
    }
}
