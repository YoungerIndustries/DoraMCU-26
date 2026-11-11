package com.yisy.modules.doramcu.zero_zero_seven.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CovertOperation Tests")
class CovertOperationTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            CovertOperation op = new CovertOperation();
            assertNull(op.getId());
            assertNull(op.getName());
            assertEquals(5, op.getDifficulty());
            assertEquals(0, op.getReward());
            assertEquals(50, op.getSuccessProbability());
            assertEquals(0, op.getTurnsRemaining());
            assertEquals(CovertOperation.OperationStatus.PLANNED, op.getStatus());
            assertNull(op.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Difficulty clamping")
    class DifficultyClamping {

        @Test
        @DisplayName("Difficulty clamps to 1-10")
        void clampDifficulty() {
            CovertOperation op = new CovertOperation();
            op.setDifficulty(0);
            assertEquals(1, op.getDifficulty());
            op.setDifficulty(15);
            assertEquals(10, op.getDifficulty());
            op.setDifficulty(7);
            assertEquals(7, op.getDifficulty());
        }
    }

    @Nested
    @DisplayName("Success probability clamping")
    class SuccessProbabilityClamping {

        @Test
        @DisplayName("Clamps to 0-100")
        void clampProbability() {
            CovertOperation op = new CovertOperation();
            op.setSuccessProbability(-10);
            assertEquals(0, op.getSuccessProbability());
            op.setSuccessProbability(150);
            assertEquals(100, op.getSuccessProbability());
            op.setSuccessProbability(75);
            assertEquals(75, op.getSuccessProbability());
        }
    }

    @Nested
    @DisplayName("Turn advancement")
    class TurnAdvancement {

        @Test
        @DisplayName("Advances in progress operation")
        void advanceInProgress() {
            CovertOperation op = new CovertOperation();
            op.setStatus(CovertOperation.OperationStatus.IN_PROGRESS);
            op.setTurnsRemaining(3);
            op.advanceTurn();
            assertEquals(2, op.getTurnsRemaining());
        }

        @Test
        @DisplayName("Does not advance planned operation")
        void advancePlanned() {
            CovertOperation op = new CovertOperation();
            op.setStatus(CovertOperation.OperationStatus.PLANNED);
            op.setTurnsRemaining(3);
            op.advanceTurn();
            assertEquals(3, op.getTurnsRemaining());
        }

        @Test
        @DisplayName("Sets completed at when turns reach zero")
        void completesAtZero() {
            CovertOperation op = new CovertOperation();
            op.setStatus(CovertOperation.OperationStatus.IN_PROGRESS);
            op.setTurnsRemaining(1);
            assertNull(op.getCompletedAt());
            op.advanceTurn();
            assertEquals(0, op.getTurnsRemaining());
            assertNotNull(op.getCompletedAt());
        }

        @Test
        @DisplayName("Does not go below zero turns")
        void noNegativeTurns() {
            CovertOperation op = new CovertOperation();
            op.setStatus(CovertOperation.OperationStatus.IN_PROGRESS);
            op.setTurnsRemaining(0);
            op.advanceTurn();
            assertEquals(0, op.getTurnsRemaining());
        }
    }

    @Nested
    @DisplayName("Active status")
    class ActiveStatus {

        @Test
        @DisplayName("Planned operation is active")
        void plannedActive() {
            CovertOperation op = new CovertOperation();
            op.setStatus(CovertOperation.OperationStatus.PLANNED);
            assertTrue(op.isActive());
        }

        @Test
        @DisplayName("In progress operation is active")
        void inProgressActive() {
            CovertOperation op = new CovertOperation();
            op.setStatus(CovertOperation.OperationStatus.IN_PROGRESS);
            assertTrue(op.isActive());
        }

        @Test
        @DisplayName("Completed operation is not active")
        void completedInactive() {
            CovertOperation op = new CovertOperation();
            op.setStatus(CovertOperation.OperationStatus.COMPLETED);
            assertFalse(op.isActive());
        }

        @Test
        @DisplayName("Failed operation is not active")
        void failedInactive() {
            CovertOperation op = new CovertOperation();
            op.setStatus(CovertOperation.OperationStatus.FAILED);
            assertFalse(op.isActive());
        }
    }

    @Nested
    @DisplayName("Enums")
    class Enums {

        @Test
        @DisplayName("OperationType values")
        void operationType() {
            assertSame(CovertOperation.OperationType.SABOTAGE, CovertOperation.OperationType.valueOf("SABOTAGE"));
            assertSame(CovertOperation.OperationType.ASSASSINATION, CovertOperation.OperationType.valueOf("ASSASSINATION"));
            assertSame(CovertOperation.OperationType.INTEL_GATHERING, CovertOperation.OperationType.valueOf("INTEL_GATHERING"));
            assertSame(CovertOperation.OperationType.PROPAGANDA, CovertOperation.OperationType.valueOf("PROPAGANDA"));
            assertSame(CovertOperation.OperationType.INFILTRATION, CovertOperation.OperationType.valueOf("INFILTRATION"));
        }

        @Test
        @DisplayName("OperationStatus values")
        void operationStatus() {
            assertSame(CovertOperation.OperationStatus.PLANNED, CovertOperation.OperationStatus.valueOf("PLANNED"));
            assertSame(CovertOperation.OperationStatus.IN_PROGRESS, CovertOperation.OperationStatus.valueOf("IN_PROGRESS"));
            assertSame(CovertOperation.OperationStatus.COMPLETED, CovertOperation.OperationStatus.valueOf("COMPLETED"));
            assertSame(CovertOperation.OperationStatus.FAILED, CovertOperation.OperationStatus.valueOf("FAILED"));
            assertSame(CovertOperation.OperationStatus.DETECTED, CovertOperation.OperationStatus.valueOf("DETECTED"));
        }
    }
}
