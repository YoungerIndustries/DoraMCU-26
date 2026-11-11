package com.yisy.modules.doraneuron.service.recovery;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.model.ExperienceSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists unflushed experience samples to disk so they survive neuron restarts.
 *
 * Uses a JSON lines format (one sample per line) for efficient append-only writes.
 * On startup, the service replays persisted samples back into the experience buffer
 * if the coordinator was unreachable when the neuron last shut down.
 */
@Service
public class ExperiencePersistenceService {

    private static final Logger log = LoggerFactory.getLogger(ExperiencePersistenceService.class);

    private final NeuronConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Directory for persisted experience data. */
    private Path persistenceDir;

    /** File containing persisted experience samples (JSON lines). */
    private Path experienceFile;

    /** Whether persistence is enabled. */
    private boolean enabled = true;

    /** Total samples persisted to disk. */
    private volatile long totalPersisted = 0;

    /** Total samples replayed from disk on startup. */
    private volatile long totalReplayed = 0;

    public ExperiencePersistenceService(NeuronConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        this.persistenceDir = Paths.get("backups/neuron/experience/").toAbsolutePath().normalize();
        this.experienceFile = persistenceDir.resolve("pending.jsonl");
        try {
            Files.createDirectories(persistenceDir);
            log.info("ExperiencePersistenceService initialized — file: {}", experienceFile);
        } catch (IOException e) {
            log.error("Failed to create persistence directory: {}", e.getMessage());
            enabled = false;
        }
    }

    /**
     * Persist an experience sample to disk.
     *
     * @param sample the sample to persist
     */
    public void persist(ExperienceSample sample) {
        if (!enabled) {
            return;
        }
        try {
            String line = objectMapper.writeValueAsString(sample);
            Files.writeString(experienceFile, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            totalPersisted++;
        } catch (IOException e) {
            log.warn("Failed to persist experience sample: {}", e.getMessage());
        }
    }

    /**
     * Persist multiple experience samples to disk.
     *
     * @param samples the samples to persist
     */
    public void persistAll(List<ExperienceSample> samples) {
        if (!enabled || samples.isEmpty()) {
            return;
        }
        try (BufferedWriter writer = Files.newBufferedWriter(experienceFile,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            for (ExperienceSample sample : samples) {
                objectMapper.writeValue(writer, sample);
                writer.newLine();
                totalPersisted++;
            }
        } catch (IOException e) {
            log.warn("Failed to persist {} experience samples: {}", samples.size(), e.getMessage());
        }
    }

    /**
     * Replay persisted experience samples from disk.
     *
     * @return list of replayed samples
     */
    public List<ExperienceSample> replay() {
        List<ExperienceSample> samples = new ArrayList<>();
        if (!enabled || !Files.exists(experienceFile)) {
            return samples;
        }

        try (BufferedReader reader = Files.newBufferedReader(experienceFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    ExperienceSample sample = objectMapper.readValue(line, ExperienceSample.class);
                    samples.add(sample);
                } catch (JsonProcessingException e) {
                    log.warn("Failed to parse persisted sample: {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Failed to read persisted experience file: {}", e.getMessage());
            return samples;
        }

        // Clear the file after successful replay
        try {
            Files.delete(experienceFile);
        } catch (IOException e) {
            log.warn("Failed to clear persisted experience file: {}", e.getMessage());
        }

        totalReplayed += samples.size();
        if (!samples.isEmpty()) {
            log.info("Replayed {} persisted experience samples from disk", samples.size());
        }
        return samples;
    }

    /**
     * Clear the persisted experience file.
     */
    public void clear() {
        if (!enabled || !Files.exists(experienceFile)) {
            return;
        }
        try {
            Files.delete(experienceFile);
            log.info("Cleared persisted experience file");
        } catch (IOException e) {
            log.warn("Failed to clear persisted experience file: {}", e.getMessage());
        }
    }

    /**
     * Get the number of persisted samples on disk.
     */
    public long getPendingCount() {
        if (!enabled || !Files.exists(experienceFile)) {
            return 0;
        }
        try {
            return Files.lines(experienceFile).filter(l -> !l.trim().isEmpty()).count();
        } catch (IOException e) {
            log.warn("Failed to count pending samples: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Get the total number of samples persisted since startup.
     */
    public long getTotalPersisted() {
        return totalPersisted;
    }

    /**
     * Get the total number of samples replayed since startup.
     */
    public long getTotalReplayed() {
        return totalReplayed;
    }

    /**
     * Check if persistence is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }
}
