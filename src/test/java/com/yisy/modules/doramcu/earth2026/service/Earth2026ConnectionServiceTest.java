package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Connection;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026ConnectionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Earth2026ConnectionServiceTest {

    @Mock
    private Earth2026ConnectionRepository repository;

    @InjectMocks
    private Earth2026ConnectionService service;

    private Earth2026Connection createConnection() {
        Earth2026Connection connection = new Earth2026Connection();
        connection.setId(1L);
        connection.setSourceType(Earth2026Connection.EntityType.CHARACTER);
        connection.setSourceId(100L);
        connection.setTargetType(Earth2026Connection.EntityType.NPC);
        connection.setTargetId(200L);
        connection.setConnectionType(Earth2026Connection.ConnectionType.ALLY);
        connection.setStrength(50);
        connection.setSecrecyLevel(0);
        connection.setActive(true);
        connection.setNotes("Test connection");
        return connection;
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("findById returns connection when found")
        void findById_returnsConnection_whenFound() {
            Earth2026Connection connection = createConnection();
            when(repository.findById(1L)).thenReturn(Optional.of(connection));

            Optional<Earth2026Connection> result = service.findById(1L);

            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
            assertEquals(Earth2026Connection.ConnectionType.ALLY, result.get().getConnectionType());
            verify(repository).findById(1L);
        }

        @Test
        @DisplayName("findById returns empty when not found")
        void findById_returnsEmpty_whenNotFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026Connection> result = service.findById(999L);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("findAll returns all connections")
        void findAll_returnsAllConnections() {
            List<Earth2026Connection> connections = Arrays.asList(createConnection(), createConnection());
            connections.get(1).setId(2L);
            when(repository.findAll()).thenReturn(connections);

            List<Earth2026Connection> result = service.findAll();

            assertEquals(2, result.size());
            verify(repository).findAll();
        }

        @Test
        @DisplayName("save persists connection")
        void save_persistsConnection() {
            Earth2026Connection connection = createConnection();
            when(repository.save(connection)).thenReturn(connection);

            Earth2026Connection result = service.save(connection);

            assertEquals(connection, result);
            verify(repository).save(connection);
        }

        @Test
        @DisplayName("deleteById deletes connection")
        void deleteById_deletesConnection() {
            service.deleteById(1L);
            verify(repository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("Query Operations")
    class QueryOperations {

        @Test
        @DisplayName("findBySourceEntity returns connections for source")
        void findBySourceEntity_returnsConnections() {
            Earth2026Connection connection = createConnection();
            when(repository.findBySourceTypeAndSourceId(Earth2026Connection.EntityType.CHARACTER, 100L))
                    .thenReturn(List.of(connection));

            List<Earth2026Connection> result = service.findBySourceEntity(Earth2026Connection.EntityType.CHARACTER, 100L);

            assertEquals(1, result.size());
            assertEquals(100L, result.get(0).getSourceId());
        }

        @Test
        @DisplayName("findActiveBySourceEntity returns only active connections")
        void findActiveBySourceEntity_returnsActiveConnections() {
            Earth2026Connection connection = createConnection();
            when(repository.findBySourceTypeAndSourceIdAndActiveTrue(Earth2026Connection.EntityType.CHARACTER, 100L))
                    .thenReturn(List.of(connection));

            List<Earth2026Connection> result = service.findActiveBySourceEntity(Earth2026Connection.EntityType.CHARACTER, 100L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("findByTargetEntity returns connections for target")
        void findByTargetEntity_returnsConnections() {
            Earth2026Connection connection = createConnection();
            when(repository.findByTargetTypeAndTargetId(Earth2026Connection.EntityType.NPC, 200L))
                    .thenReturn(List.of(connection));

            List<Earth2026Connection> result = service.findByTargetEntity(Earth2026Connection.EntityType.NPC, 200L);

            assertEquals(1, result.size());
            assertEquals(200L, result.get(0).getTargetId());
        }

        @Test
        @DisplayName("findByEntity returns connections as source and target")
        void findByEntity_returnsSourceAndTargetConnections() {
            Earth2026Connection sourceConn = createConnection();
            Earth2026Connection targetConn = createConnection();
            targetConn.setId(2L);
            targetConn.setSourceType(Earth2026Connection.EntityType.NPC);
            targetConn.setSourceId(300L);
            targetConn.setTargetType(Earth2026Connection.EntityType.CHARACTER);
            targetConn.setTargetId(100L);

            when(repository.findBySourceTypeAndSourceIdAndActiveTrue(Earth2026Connection.EntityType.CHARACTER, 100L))
                    .thenReturn(List.of(sourceConn));
            when(repository.findByTargetTypeAndTargetIdAndActiveTrue(Earth2026Connection.EntityType.CHARACTER, 100L))
                    .thenReturn(List.of(targetConn));

            List<Earth2026Connection> result = service.findByEntity(Earth2026Connection.EntityType.CHARACTER, 100L);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("findByConnectionType returns connections of type")
        void findByConnectionType_returnsConnections() {
            Earth2026Connection connection = createConnection();
            when(repository.findByConnectionType(Earth2026Connection.ConnectionType.ALLY))
                    .thenReturn(List.of(connection));

            List<Earth2026Connection> result = service.findByConnectionType(Earth2026Connection.ConnectionType.ALLY);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("findDiscoverableConnections returns connections below secrecy threshold")
        void findDiscoverableConnections_returnsConnections() {
            Earth2026Connection connection = createConnection();
            connection.setSecrecyLevel(30);
            when(repository.findBySecrecyLevelLessThanEqualAndActiveTrue(50))
                    .thenReturn(List.of(connection));

            List<Earth2026Connection> result = service.findDiscoverableConnections(50);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("findBySourceAndType returns connections by source and type")
        void findBySourceAndType_returnsConnections() {
            Earth2026Connection connection = createConnection();
            when(repository.findBySourceTypeAndSourceIdAndConnectionTypeAndActiveTrue(
                    Earth2026Connection.EntityType.CHARACTER, 100L, Earth2026Connection.ConnectionType.ALLY))
                    .thenReturn(List.of(connection));

            List<Earth2026Connection> result = service.findBySourceAndType(
                    Earth2026Connection.EntityType.CHARACTER, 100L, Earth2026Connection.ConnectionType.ALLY);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("Strength Management")
    class StrengthManagement {

        @Test
        @DisplayName("increaseStrength increases strength")
        void increaseStrength_increasesStrength() {
            Earth2026Connection connection = createConnection();
            when(repository.findById(1L)).thenReturn(Optional.of(connection));
            when(repository.save(connection)).thenReturn(connection);

            Optional<Earth2026Connection> result = service.increaseStrength(1L, 20);

            assertTrue(result.isPresent());
            assertEquals(70, result.get().getStrength());
            verify(repository).save(connection);
        }

        @Test
        @DisplayName("increaseStrength clamps to 100")
        void increaseStrength_clampsTo100() {
            Earth2026Connection connection = createConnection();
            connection.setStrength(90);
            when(repository.findById(1L)).thenReturn(Optional.of(connection));
            when(repository.save(connection)).thenReturn(connection);

            Optional<Earth2026Connection> result = service.increaseStrength(1L, 20);

            assertTrue(result.isPresent());
            assertEquals(100, result.get().getStrength());
        }

        @Test
        @DisplayName("decreaseStrength decreases strength")
        void decreaseStrength_decreasesStrength() {
            Earth2026Connection connection = createConnection();
            when(repository.findById(1L)).thenReturn(Optional.of(connection));
            when(repository.save(connection)).thenReturn(connection);

            Optional<Earth2026Connection> result = service.decreaseStrength(1L, 20);

            assertTrue(result.isPresent());
            assertEquals(30, result.get().getStrength());
        }

        @Test
        @DisplayName("decreaseStrength clamps to 0")
        void decreaseStrength_clampsTo0() {
            Earth2026Connection connection = createConnection();
            connection.setStrength(10);
            when(repository.findById(1L)).thenReturn(Optional.of(connection));
            when(repository.save(connection)).thenReturn(connection);

            Optional<Earth2026Connection> result = service.decreaseStrength(1L, 20);

            assertTrue(result.isPresent());
            assertEquals(0, result.get().getStrength());
        }

        @Test
        @DisplayName("increaseStrength returns empty when not found")
        void increaseStrength_returnsEmpty_whenNotFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026Connection> result = service.increaseStrength(999L, 10);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("decreaseStrength returns empty when not found")
        void decreaseStrength_returnsEmpty_whenNotFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026Connection> result = service.decreaseStrength(999L, 10);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Connection Management")
    class ConnectionManagement {

        @Test
        @DisplayName("createConnection creates and saves connection")
        void createConnection_createsAndSaves() {
            Earth2026Connection saved = createConnection();
            when(repository.save(any())).thenReturn(saved);

            Earth2026Connection result = service.createConnection(
                    Earth2026Connection.EntityType.CHARACTER, 100L,
                    Earth2026Connection.EntityType.NPC, 200L,
                    Earth2026Connection.ConnectionType.ALLY, 50, 0);

            assertNotNull(result);
            assertEquals(Earth2026Connection.EntityType.CHARACTER, result.getSourceType());
            assertEquals(100L, result.getSourceId());
            assertEquals(Earth2026Connection.ConnectionType.ALLY, result.getConnectionType());
            assertEquals(50, result.getStrength());
            assertEquals(0, result.getSecrecyLevel());
            assertTrue(result.isActive());
            verify(repository).save(any());
        }

        @Test
        @DisplayName("deactivateConnection deactivates connection")
        void deactivateConnection_deactivates() {
            Earth2026Connection connection = createConnection();
            when(repository.findById(1L)).thenReturn(Optional.of(connection));
            when(repository.save(connection)).thenReturn(connection);

            boolean result = service.deactivateConnection(1L);

            assertTrue(result);
            assertFalse(connection.isActive());
            verify(repository).save(connection);
        }

        @Test
        @DisplayName("deactivateConnection returns false when not found")
        void deactivateConnection_returnsFalse_whenNotFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            boolean result = service.deactivateConnection(999L);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Relationship Graph Queries")
    class RelationshipGraphQueries {

        @Test
        @DisplayName("getConnectionCount counts source and target connections")
        void getConnectionCount_countsSourceAndTarget() {
            when(repository.findBySourceTypeAndSourceIdAndActiveTrue(Earth2026Connection.EntityType.CHARACTER, 100L))
                    .thenReturn(List.of(createConnection()));
            when(repository.findByTargetTypeAndTargetIdAndActiveTrue(Earth2026Connection.EntityType.CHARACTER, 100L))
                    .thenReturn(List.of(createConnection(), createConnection()));

            int count = service.getConnectionCount(Earth2026Connection.EntityType.CHARACTER, 100L);

            assertEquals(3, count);
        }

        @Test
        @DisplayName("hasConnection returns true when active connection exists")
        void hasConnection_returnsTrue_whenActiveConnectionExists() {
            Earth2026Connection connection = createConnection();
            when(repository.findBySourceTypeAndSourceIdAndTargetTypeAndTargetId(
                    Earth2026Connection.EntityType.CHARACTER, 100L,
                    Earth2026Connection.EntityType.NPC, 200L))
                    .thenReturn(List.of(connection));

            boolean result = service.hasConnection(
                    Earth2026Connection.EntityType.CHARACTER, 100L,
                    Earth2026Connection.EntityType.NPC, 200L);

            assertTrue(result);
        }

        @Test
        @DisplayName("hasConnection returns false when no active connection")
        void hasConnection_returnsFalse_whenNoActiveConnection() {
            Earth2026Connection connection = createConnection();
            connection.setActive(false);
            when(repository.findBySourceTypeAndSourceIdAndTargetTypeAndTargetId(
                    Earth2026Connection.EntityType.CHARACTER, 100L,
                    Earth2026Connection.EntityType.NPC, 200L))
                    .thenReturn(List.of(connection));

            boolean result = service.hasConnection(
                    Earth2026Connection.EntityType.CHARACTER, 100L,
                    Earth2026Connection.EntityType.NPC, 200L);

            assertFalse(result);
        }

        @Test
        @DisplayName("hasConnectionType returns true when type matches")
        void hasConnectionType_returnsTrue_whenTypeMatches() {
            Earth2026Connection connection = createConnection();
            when(repository.findBySourceTypeAndSourceIdAndConnectionTypeAndActiveTrue(
                    Earth2026Connection.EntityType.CHARACTER, 100L, Earth2026Connection.ConnectionType.ALLY))
                    .thenReturn(List.of(connection));

            boolean result = service.hasConnectionType(
                    Earth2026Connection.EntityType.CHARACTER, 100L,
                    Earth2026Connection.ConnectionType.ALLY);

            assertTrue(result);
        }

        @Test
        @DisplayName("hasConnectionType checks reverse direction")
        void hasConnectionType_checksReverseDirection() {
            Earth2026Connection connection = createConnection();
            connection.setSourceType(Earth2026Connection.EntityType.NPC);
            connection.setSourceId(200L);
            connection.setTargetType(Earth2026Connection.EntityType.CHARACTER);
            connection.setTargetId(100L);

            when(repository.findBySourceTypeAndSourceIdAndConnectionTypeAndActiveTrue(
                    Earth2026Connection.EntityType.CHARACTER, 100L, Earth2026Connection.ConnectionType.ALLY))
                    .thenReturn(List.of());
            when(repository.findByTargetTypeAndTargetIdAndActiveTrue(
                    Earth2026Connection.EntityType.CHARACTER, 100L))
                    .thenReturn(List.of(connection));

            boolean result = service.hasConnectionType(
                    Earth2026Connection.EntityType.CHARACTER, 100L,
                    Earth2026Connection.ConnectionType.ALLY);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("Entity Methods")
    class EntityMethods {

        @Test
        @DisplayName("Connection defaults are correct")
        void connectionDefaults_areCorrect() {
            Earth2026Connection connection = new Earth2026Connection();
            assertEquals(50, connection.getStrength());
            assertEquals(0, connection.getSecrecyLevel());
            assertTrue(connection.isActive());
        }

        @Test
        @DisplayName("Connection constructor sets fields")
        void connectionConstructor_setsFields() {
            Earth2026Connection connection = new Earth2026Connection(
                    Earth2026Connection.EntityType.CHARACTER, 100L,
                    Earth2026Connection.EntityType.NPC, 200L,
                    Earth2026Connection.ConnectionType.FAMILY);

            assertEquals(Earth2026Connection.EntityType.CHARACTER, connection.getSourceType());
            assertEquals(100L, connection.getSourceId());
            assertEquals(Earth2026Connection.EntityType.NPC, connection.getTargetType());
            assertEquals(200L, connection.getTargetId());
            assertEquals(Earth2026Connection.ConnectionType.FAMILY, connection.getConnectionType());
        }

        @Test
        @DisplayName("setStrength clamps to 0-100")
        void setStrength_clampsToRange() {
            Earth2026Connection connection = new Earth2026Connection();
            connection.setStrength(-10);
            assertEquals(0, connection.getStrength());
            connection.setStrength(150);
            assertEquals(100, connection.getStrength());
        }

        @Test
        @DisplayName("setSecrecyLevel clamps to 0-100")
        void setSecrecyLevel_clampsToRange() {
            Earth2026Connection connection = new Earth2026Connection();
            connection.setSecrecyLevel(-10);
            assertEquals(0, connection.getSecrecyLevel());
            connection.setSecrecyLevel(150);
            assertEquals(100, connection.getSecrecyLevel());
        }

        @Test
        @DisplayName("increaseStrength clamps to 100")
        void increaseStrength_clampsTo100() {
            Earth2026Connection connection = new Earth2026Connection();
            connection.setStrength(90);
            connection.increaseStrength(20);
            assertEquals(100, connection.getStrength());
        }

        @Test
        @DisplayName("decreaseStrength clamps to 0")
        void decreaseStrength_clampsTo0() {
            Earth2026Connection connection = new Earth2026Connection();
            connection.setStrength(10);
            connection.decreaseStrength(20);
            assertEquals(0, connection.getStrength());
        }

        @Test
        @DisplayName("toString returns meaningful string")
        void toString_returnsMeaningfulString() {
            Earth2026Connection connection = createConnection();
            String str = connection.toString();
            assertTrue(str.contains("Earth2026Connection"));
            assertTrue(str.contains("CHARACTER"));
            assertTrue(str.contains("ALLY"));
        }

        @Test
        @DisplayName("EntityType enum has all expected values")
        void entityTypeEnum_hasAllValues() {
            Earth2026Connection.EntityType[] types = Earth2026Connection.EntityType.values();
            assertArrayEquals(
                    new Earth2026Connection.EntityType[]{
                            Earth2026Connection.EntityType.CHARACTER,
                            Earth2026Connection.EntityType.NPC,
                            Earth2026Connection.EntityType.AI_CHARACTER,
                            Earth2026Connection.EntityType.COMPANION,
                            Earth2026Connection.EntityType.MONSTER
                    }, types);
        }

        @Test
        @DisplayName("ConnectionType enum has all expected values")
        void connectionTypeEnum_hasAllValues() {
            Earth2026Connection.ConnectionType[] types = Earth2026Connection.ConnectionType.values();
            assertTrue(types.length >= 17);
            assertNotNull(Arrays.stream(types).filter(t -> t.name().equals("SPY_HANDLER")).findFirst().orElse(null));
            assertNotNull(Arrays.stream(types).filter(t -> t.name().equals("DOUBLE_AGENT")).findFirst().orElse(null));
            assertNotNull(Arrays.stream(types).filter(t -> t.name().equals("BLACKMAILED")).findFirst().orElse(null));
        }
    }
}
