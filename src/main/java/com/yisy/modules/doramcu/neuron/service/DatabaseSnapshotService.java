package com.yisy.modules.doramcu.neuron.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exports all JPA entity data as a JSON Lines archive for database snapshots.
 * Used by the game client to initialize its local database replica.
 */
@Service
public class DatabaseSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSnapshotService.class);

    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

    public DatabaseSnapshotService(EntityManager entityManager, ObjectMapper objectMapper) {
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    /**
     * Exports all JPA entities as a gzipped JSON Lines string.
     * Each line is a JSON object with entity type, ID, and data.
     *
     * @return JSON Lines content as a string
     */
    public String exportSnapshot() {
        log.info("Starting database snapshot export...");
        StringBuilder sb = new StringBuilder();

        // List of entity classes to export
        List<String> entityNames = List.of(
                "User", "Player", "Device", "Galaxy", "SolarSystem", "Sector",
                "SubSector", "Planet", "Moon", "Star", "SpaceStation",
                "AsteroidBelt", "GasGiant", "Nebula", "BlackHole", "Comet",
                "GameSession", "PersistedGameSession", "StrategicResource",
                "Treasury", "EveCorporation", "AiCharacter"
        );

        for (String entityName : entityNames) {
            try {
                List<?> entities = entityManager.createQuery(
                        "SELECT e FROM " + entityName + " e", Object.class).getResultList();
                for (Object entity : entities) {
                    String json = objectMapper.writeValueAsString(entity);
                    sb.append("{\"entity\":\"").append(entityName)
                      .append("\",\"data\":")
                      .append(json)
                      .append("}\n");
                }
                log.info("Exported {} entities of type {}", entities.size(), entityName);
            } catch (Exception e) {
                log.warn("Failed to export entity type {}: {}", entityName, e.getMessage());
            }
        }

        log.info("Database snapshot export complete");
        return sb.toString();
    }
}
