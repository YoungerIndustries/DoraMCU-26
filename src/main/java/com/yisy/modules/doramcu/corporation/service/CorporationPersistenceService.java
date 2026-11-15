package com.yisy.modules.doramcu.corporation.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporationOfficeState;
import com.yisy.modules.doramcu.corporation.model.CorporationSave;
import com.yisy.modules.doramcu.corporation.repository.CorporateContractRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporationOfficeStateRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporationSaveRepository;
import com.yisy.modules.doramcu.repository.EveCorporationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service that handles automatic and manual persistence of corporation state.
 * <p>
 * Manages office building level persistence (replacing in-memory ConcurrentHashMap),
 * and provides manual save/load functionality for corporation snapshots.
 * </p>
 */
@Service
public class CorporationPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(CorporationPersistenceService.class);

    private final CorporationOfficeStateRepository officeStateRepository;
    private final CorporationSaveRepository saveRepository;
    private final EveCorporationRepository corporationRepository;
    private final CorporateContractRepository contractRepository;
    private final CorporateHiringRepository hiringRepository;

    public CorporationPersistenceService(
            CorporationOfficeStateRepository officeStateRepository,
            CorporationSaveRepository saveRepository,
            EveCorporationRepository corporationRepository,
            CorporateContractRepository contractRepository,
            CorporateHiringRepository hiringRepository) {
        this.officeStateRepository = officeStateRepository;
        this.saveRepository = saveRepository;
        this.corporationRepository = corporationRepository;
        this.contractRepository = contractRepository;
        this.hiringRepository = hiringRepository;
    }

    // ---- Office State Persistence ----

    /**
     * Save or update office building level for a corporation.
     *
     * @param corporationId the corporation ID
     * @param buildingLevel the building level
     */
    @Transactional
    public void saveOfficeState(Long corporationId, int buildingLevel) {
        Optional<CorporationOfficeState> existing = officeStateRepository.findByCorporationId(corporationId);
        CorporationOfficeState state;
        if (existing.isPresent()) {
            state = existing.get();
            state.setOfficeBuildingLevel(buildingLevel);
        } else {
            state = new CorporationOfficeState(corporationId);
            state.setOfficeBuildingLevel(buildingLevel);
        }
        officeStateRepository.save(state);
        log.info("Saved office state for corporation {}: building level {}", corporationId, buildingLevel);
    }

    /**
     * Load office state for a corporation. Creates a default record if none exists.
     *
     * @param corporationId the corporation ID
     * @return the office state
     */
    @Transactional
    public CorporationOfficeState loadOfficeState(Long corporationId) {
        CorporationOfficeState state = officeStateRepository.findByCorporationId(corporationId)
                .orElseGet(() -> {
                    CorporationOfficeState defaultState = new CorporationOfficeState(corporationId);
                    officeStateRepository.save(defaultState);
                    log.info("Created default office state for corporation {}", corporationId);
                    return defaultState;
                });
        return state;
    }

    /**
     * Get the office building level for a corporation.
     *
     * @param corporationId the corporation ID
     * @return the building level (0 if not set)
     */
    @Transactional(readOnly = true)
    public int getOfficeBuildingLevel(Long corporationId) {
        return officeStateRepository.findByCorporationId(corporationId)
                .map(CorporationOfficeState::getOfficeBuildingLevel)
                .orElse(0);
    }

    /**
     * Increment the office building level for a corporation.
     *
     * @param corporationId the corporation ID
     * @return the new building level
     */
    @Transactional
    public int incrementOfficeBuildingLevel(Long corporationId) {
        CorporationOfficeState state = loadOfficeState(corporationId);
        state.incrementBuildingLevel();
        officeStateRepository.save(state);
        log.info("Incremented office building level for corporation {} to {}", corporationId, state.getOfficeBuildingLevel());
        return state.getOfficeBuildingLevel();
    }

    // ---- Manual Save/Load ----

    /**
     * Create a manual save snapshot of the corporation's current state.
     *
     * @param corporationId the corporation ID
     * @param saveName optional save name (auto-generated if null)
     * @return the saved snapshot
     */
    @Transactional
    public CorporationSave createSave(Long corporationId, String saveName) {
        if (saveName == null || saveName.isBlank()) {
            saveName = "Save " + System.currentTimeMillis();
        }

        // Gather current state
        Long creditBalance = 0L;
        try {
            EveCorporation corp = corporationRepository.findById(corporationId).orElse(null);
            if (corp != null && corp.getCreditBalance() != null) {
                creditBalance = corp.getCreditBalance();
            }
        } catch (Exception e) {
            log.warn("Could not load corporation {} for save: {}", corporationId, e.getMessage());
        }

        // Active contracts
        List<CorporateContract> activeContracts = contractRepository
                .findByCorporationIdAndStatus(corporationId, CorporateContract.Status.ACTIVE);
        String contractsJson = buildContractsJson(activeContracts);

        // Crew state
        List<CorporateHiring> activeHires = hiringRepository
                .findByCorporationIdAndStatus(corporationId, CorporateHiring.Status.ACTIVE);
        String crewJson = buildCrewJson(activeHires);

        // Office state
        int buildingLevel = getOfficeBuildingLevel(corporationId);
        Map<String, Object> officeState = new HashMap<>();
        officeState.put("buildingLevel", buildingLevel);
        String officeJson = buildJson(officeState);

        // Create save
        CorporationSave save = new CorporationSave(corporationId, saveName);
        save.setCreditBalance(creditBalance);
        save.setActiveContracts(contractsJson);
        save.setCrewState(crewJson);
        save.setOfficeState(officeJson);
        save = saveRepository.save(save);

        log.info("Created save '{}' for corporation {}: balance={}, contracts={}, hires={}",
                saveName, corporationId, creditBalance, activeContracts.size(), activeHires.size());
        return save;
    }

    /**
     * List all saves for a corporation.
     *
     * @param corporationId the corporation ID
     * @return list of saves ordered by timestamp descending
     */
    @Transactional(readOnly = true)
    public List<CorporationSave> listSaves(Long corporationId) {
        return saveRepository.findSavesByCorporationId(corporationId);
    }

    /**
     * Load a save snapshot, restoring the corporation's state.
     * <p>
     * This sets the corporation's credit balance to the saved value and logs the restoration.
     * Contract and crew state are restored by reference â€” existing entities are not modified,
     * but the save provides a record of what was active at save time.
     * </p>
     *
     * @param corporationId the corporation ID
     * @param saveId the save ID
     * @return the loaded save
     * @throws IllegalArgumentException if save not found
     */
    @Transactional
    public CorporationSave loadSave(Long corporationId, Long saveId) {
        CorporationSave save = saveRepository.findById(saveId)
                .orElseThrow(() -> new IllegalArgumentException("Save not found: " + saveId));

        if (!save.getCorporationId().equals(corporationId)) {
            throw new IllegalArgumentException("Save does not belong to corporation: " + corporationId);
        }

        // Restore credit balance
        try {
            EveCorporation corp = corporationRepository.findById(corporationId).orElse(null);
            if (corp != null) {
                corp.setCreditBalance(save.getCreditBalance());
                corporationRepository.save(corp);
                log.info("Restored credit balance for corporation {} to {} from save {}",
                        corporationId, save.getCreditBalance(), saveId);
            }
        } catch (Exception e) {
            log.warn("Could not restore credit balance for corporation {}: {}", corporationId, e.getMessage());
        }

        // Restore office building level
        try {
            if (save.getOfficeState() != null) {
                Map<String, Object> officeState = parseJson(save.getOfficeState());
                if (officeState.containsKey("buildingLevel")) {
                    int level = ((Number) officeState.get("buildingLevel")).intValue();
                    saveOfficeState(corporationId, level);
                    log.info("Restored office building level for corporation {} to {} from save {}",
                            corporationId, level, saveId);
                }
            }
        } catch (Exception e) {
            log.warn("Could not restore office state for corporation {}: {}", corporationId, e.getMessage());
        }

        log.info("Loaded save '{}' for corporation {}. Contracts: {}, Crew: {}",
                save.getSaveName(), corporationId,
                save.getActiveContracts() != null ? "present" : "none",
                save.getCrewState() != null ? "present" : "none");
        return save;
    }

    /**
     * Delete a save snapshot.
     *
     * @param corporationId the corporation ID
     * @param saveId the save ID
     */
    @Transactional
    public void deleteSave(Long corporationId, Long saveId) {
        CorporationSave save = saveRepository.findById(saveId)
                .orElseThrow(() -> new IllegalArgumentException("Save not found: " + saveId));

        if (!save.getCorporationId().equals(corporationId)) {
            throw new IllegalArgumentException("Save does not belong to corporation: " + corporationId);
        }

        saveRepository.delete(save);
        log.info("Deleted save '{}' for corporation {}", save.getSaveName(), corporationId);
    }

    // ---- Helper Methods ----

    private String buildContractsJson(List<CorporateContract> contracts) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (CorporateContract c : contracts) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("contractId", c.getId());
            entry.put("title", c.getTitle());
            entry.put("type", c.getType().name());
            entry.put("status", c.getStatus().name());
            entry.put("turnsRemaining", c.getTurnsRemaining());
            entry.put("durationTurns", c.getDurationTurns());
            entry.put("payment", c.getPaymentCredits());
            list.add(entry);
        }
        return buildJson(list);
    }

    private String buildCrewJson(List<CorporateHiring> hires) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (CorporateHiring h : hires) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("hiringId", h.getId());
            entry.put("aiCharacterId", h.getAiCharacterId());
            entry.put("position", h.getPosition().name());
            entry.put("dailySalary", h.getDailySalary());
            entry.put("performanceRating", h.getPerformanceRating());
            list.add(entry);
        }
        return buildJson(list);
    }

    /**
     * Simple JSON builder for maps and lists.
     * Uses basic string formatting instead of a JSON library to avoid dependencies.
     */
    private String buildJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            if (map.isEmpty()) return "{}";
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escapeJson(entry.getKey().toString())).append("\":");
                sb.append(valueToJson(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            if (list.isEmpty()) return "[]";
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(",");
                first = false;
                sb.append(valueToJson(item));
            }
            sb.append("]");
            return sb.toString();
        }
        return valueToJson(obj);
    }

    private String valueToJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + escapeJson((String) value) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof Map || value instanceof List) return buildJson(value);
        return "\"" + escapeJson(value.toString()) + "\"";
    }

    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Simple JSON parser for basic objects.
     * Returns a Map for objects and List for arrays.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isBlank()) return new HashMap<>();
        // For simplicity, use a basic approach â€” in production, use Jackson/Gson
        // This handles the simple {"key":value} format we generate
        Map<String, Object> result = new HashMap<>();
        String trimmed = json.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            String inner = trimmed.substring(1, trimmed.length() - 1).trim();
            if (inner.isEmpty()) return result;
            // Simple key-value parsing for our generated format
            String[] pairs = splitJsonPairs(inner);
            for (String pair : pairs) {
                int colonIdx = pair.indexOf(':');
                if (colonIdx > 0) {
                    String key = pair.substring(0, colonIdx).trim().replace("\"", "");
                    String value = pair.substring(colonIdx + 1).trim();
                    result.put(key, parseJsonValue(value));
                }
            }
        }
        return result;
    }

    private String[] splitJsonPairs(String json) {
        List<String> pairs = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{' || c == '[') depth++;
            else if (c == '}' || c == ']') depth--;
            else if (c == ',' && depth == 0) {
                pairs.add(json.substring(start, i).trim());
                start = i + 1;
            }
        }
        if (start < json.length()) {
            pairs.add(json.substring(start).trim());
        }
        return pairs.toArray(new String[0]);
    }

    private Object parseJsonValue(String value) {
        if ("null".equals(value)) return null;
        if ("true".equals(value)) return true;
        if ("false".equals(value)) return false;
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1).replace("\\\"", "\"");
        }
        try {
            if (value.contains(".")) return Double.parseDouble(value);
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return value;
        }
    }
}
