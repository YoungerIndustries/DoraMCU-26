package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.service.AiAdminService;
import com.yisy.modules.doramcu.dto.GalaxyComparisonDto;
import com.yisy.modules.doramcu.dto.GalaxyComparisonSummaryDto;
import com.yisy.modules.doramcu.model.Galaxy;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service that compares galaxies side by side.
 * <p>
 * Provides detailed two-galaxy comparisons and all-galaxy summary grids
 * for admin dashboards, computing deltas, rankings, and human-readable insights.
 * </p>
 */
@Service
public class GalaxyComparisonService {

    private static final Logger log = LoggerFactory.getLogger(GalaxyComparisonService.class);

    private final GalaxyRepository galaxyRepository;
    private final GalaxyOverseerService galaxyOverseerService;
    private final GalaxyService galaxyService;
    private final BackgroundGalaxyBuilder backgroundGalaxyBuilder;
    private final AiAdminService aiAdminService;
    private final AiCharacterRepository aiCharacterRepository;
    private final PlayerRepository playerRepository;

    public GalaxyComparisonService(GalaxyRepository galaxyRepository,
                                    GalaxyOverseerService galaxyOverseerService,
                                    GalaxyService galaxyService,
                                    BackgroundGalaxyBuilder backgroundGalaxyBuilder,
                                    AiAdminService aiAdminService,
                                    AiCharacterRepository aiCharacterRepository,
                                    PlayerRepository playerRepository) {
        this.galaxyRepository = galaxyRepository;
        this.galaxyOverseerService = galaxyOverseerService;
        this.galaxyService = galaxyService;
        this.backgroundGalaxyBuilder = backgroundGalaxyBuilder;
        this.aiAdminService = aiAdminService;
        this.aiCharacterRepository = aiCharacterRepository;
        this.playerRepository = playerRepository;
    }

    /**
     * Compares two galaxies side by side.
     *
     * @param nameA first galaxy name
     * @param nameB second galaxy name
     * @return detailed comparison DTO with deltas and insights
     */
    @Transactional(readOnly = true)
    public GalaxyComparisonDto compareGalaxies(String nameA, String nameB) {
        Optional<Galaxy> optA = galaxyRepository.findByName(nameA);
        Optional<Galaxy> optB = galaxyRepository.findByName(nameB);

        if (optA.isEmpty()) {
            throw new IllegalArgumentException("Galaxy not found: " + nameA);
        }
        if (optB.isEmpty()) {
            throw new IllegalArgumentException("Galaxy not found: " + nameB);
        }

        Galaxy galaxyA = optA.get();
        Galaxy galaxyB = optB.get();

        // Assess both galaxies
        GalaxyOverseerService.GalaxyAssessment assessmentA = galaxyOverseerService.assessGalaxyNeeds(galaxyA);
        GalaxyOverseerService.GalaxyAssessment assessmentB = galaxyOverseerService.assessGalaxyNeeds(galaxyB);

        // Get workforce reports
        GalaxyOverseerService.WorkforceReport workforceA = galaxyOverseerService.monitorWorkforce(galaxyA.getId());
        GalaxyOverseerService.WorkforceReport workforceB = galaxyOverseerService.monitorWorkforce(galaxyB.getId());

        // Get build status
        GalaxyBuildStatus buildStatusA = backgroundGalaxyBuilder.getBuildStatus(nameA);
        GalaxyBuildStatus buildStatusB = backgroundGalaxyBuilder.getBuildStatus(nameB);

        // Get pending work orders
        int pendingA = aiAdminService.getOrdersByPrefixAndStatus("GalaxyOverseer:" + nameA, AiAdminOrder.OrderStatus.PENDING).size();
        int pendingB = aiAdminService.getOrdersByPrefixAndStatus("GalaxyOverseer:" + nameB, AiAdminOrder.OrderStatus.PENDING).size();

        // Count players
        int playerCountA = playerRepository.findAll().size();
        int playerCountB = playerRepository.findAll().size();

        // Build DTO
        GalaxyComparisonDto dto = new GalaxyComparisonDto();

        // Galaxy A
        dto.setGalaxyAName(nameA);
        dto.setGalaxyAId(galaxyA.getId());
        dto.setGalaxyAGameType(galaxyA.getGameType());
        dto.setGalaxyADifficulty(galaxyA.getDifficultyLevel());
        dto.setGalaxyAHealthScore(assessmentA.getHealthScore());
        dto.setGalaxyAHealthStatus(getHealthStatus(assessmentA.getHealthScore()));
        dto.setGalaxyACompletionPercent(galaxyService.getGalaxyCompletionPercent(nameA));
        dto.setGalaxyASolarSystemCount(assessmentA.getSolarSystemCount());
        dto.setGalaxyAAiAgentCount(workforceA.getTotalAiAgents());
        dto.setGalaxyAPlayerCount(playerCountA);
        dto.setGalaxyAPendingWorkOrders(pendingA);
        dto.setGalaxyACriticalFindings(countSeverity(assessmentA, "CRITICAL"));
        dto.setGalaxyAIsPlayable(galaxyService.isGalaxyPlayable(nameA));
        dto.setGalaxyABuildRunning(buildStatusA != null && buildStatusA.isRunning());

        // Galaxy B
        dto.setGalaxyBName(nameB);
        dto.setGalaxyBId(galaxyB.getId());
        dto.setGalaxyBGameType(galaxyB.getGameType());
        dto.setGalaxyBDifficulty(galaxyB.getDifficultyLevel());
        dto.setGalaxyBHealthScore(assessmentB.getHealthScore());
        dto.setGalaxyBHealthStatus(getHealthStatus(assessmentB.getHealthScore()));
        dto.setGalaxyBCompletionPercent(galaxyService.getGalaxyCompletionPercent(nameB));
        dto.setGalaxyBSolarSystemCount(assessmentB.getSolarSystemCount());
        dto.setGalaxyBAiAgentCount(workforceB.getTotalAiAgents());
        dto.setGalaxyBPlayerCount(playerCountB);
        dto.setGalaxyBPendingWorkOrders(pendingB);
        dto.setGalaxyBCriticalFindings(countSeverity(assessmentB, "CRITICAL"));
        dto.setGalaxyBIsPlayable(galaxyService.isGalaxyPlayable(nameB));
        dto.setGalaxyBBuildRunning(buildStatusB != null && buildStatusB.isRunning());

        // Deltas (A - B)
        dto.setDeltaHealthScore(dto.getGalaxyAHealthScore() - dto.getGalaxyBHealthScore());
        dto.setDeltaCompletionPercent(dto.getGalaxyACompletionPercent() - dto.getGalaxyBCompletionPercent());
        dto.setDeltaSolarSystemCount(dto.getGalaxyASolarSystemCount() - dto.getGalaxyBSolarSystemCount());
        dto.setDeltaAiAgentCount(dto.getGalaxyAAiAgentCount() - dto.getGalaxyBAiAgentCount());
        dto.setDeltaPlayerCount(dto.getGalaxyAPlayerCount() - dto.getGalaxyBPlayerCount());
        dto.setDeltaPendingWorkOrders(dto.getGalaxyAPendingWorkOrders() - dto.getGalaxyBPendingWorkOrders());
        dto.setDeltaCriticalFindings(dto.getGalaxyACriticalFindings() - dto.getGalaxyBCriticalFindings());

        // Comparison summary
        dto.setHealthierGalaxy(compareWinner(dto.getGalaxyAHealthScore(), dto.getGalaxyBHealthScore()));
        dto.setMoreCompleteGalaxy(compareWinner(dto.getGalaxyACompletionPercent(), dto.getGalaxyBCompletionPercent()));
        dto.setMorePopulatedGalaxy(compareWinner(dto.getGalaxyAPlayerCount(), dto.getGalaxyBPlayerCount()));
        dto.setMoreStaffedGalaxy(compareWinner(dto.getGalaxyAAiAgentCount(), dto.getGalaxyBAiAgentCount()));
        dto.setFewerWorkOrders(compareLoser(dto.getGalaxyAPendingWorkOrders(), dto.getGalaxyBPendingWorkOrders()));

        // Combined scores (weighted: health 40%, completion 30%, staffing 20%, work orders 10%)
        double scoreA = computeCombinedScore(
                dto.getGalaxyAHealthScore(),
                dto.getGalaxyACompletionPercent(),
                dto.getGalaxyAAiAgentCount(),
                dto.getGalaxyAPendingWorkOrders()
        );
        double scoreB = computeCombinedScore(
                dto.getGalaxyBHealthScore(),
                dto.getGalaxyBCompletionPercent(),
                dto.getGalaxyBAiAgentCount(),
                dto.getGalaxyBPendingWorkOrders()
        );
        dto.setGalaxyAScore(Math.round(scoreA * 100.0) / 100.0);
        dto.setGalaxyBScore(Math.round(scoreB * 100.0) / 100.0);
        dto.setOverallWinner(compareWinner((int) scoreA, (int) scoreB));

        // Generate insights
        dto.setInsights(generateInsights(dto));

        log.info("Galaxy comparison: {} vs {} — winner: {}", nameA, nameB, dto.getOverallWinner());
        return dto;
    }

    /**
     * Returns a summary grid of all galaxies sorted by combined score.
     *
     * @return list of summary DTOs ranked by combined score
     */
    @Transactional(readOnly = true)
    public List<GalaxyComparisonSummaryDto> compareAllGalaxies() {
        List<Galaxy> galaxies = galaxyRepository.findAll();
        List<GalaxyComparisonSummaryDto> summaries = new ArrayList<>();

        for (Galaxy galaxy : galaxies) {
            GalaxyComparisonSummaryDto summary = new GalaxyComparisonSummaryDto();
            String name = galaxy.getName();

            GalaxyOverseerService.GalaxyAssessment assessment = galaxyOverseerService.assessGalaxyNeeds(galaxy);
            GalaxyOverseerService.WorkforceReport workforce = galaxyOverseerService.monitorWorkforce(galaxy.getId());
            GalaxyBuildStatus buildStatus = backgroundGalaxyBuilder.getBuildStatus(name);

            int pending = aiAdminService.getOrdersByPrefixAndStatus("GalaxyOverseer:" + name, AiAdminOrder.OrderStatus.PENDING).size();
            int completion = galaxyService.getGalaxyCompletionPercent(name);
            int playerCount = playerRepository.findAll().size();

            summary.setGalaxyName(name);
            summary.setGalaxyId(galaxy.getId());
            summary.setGameType(galaxy.getGameType());
            summary.setDifficultyLevel(galaxy.getDifficultyLevel());
            summary.setHealthScore(assessment.getHealthScore());
            summary.setHealthStatus(getHealthStatus(assessment.getHealthScore()));
            summary.setPlayable(galaxyService.isGalaxyPlayable(name));
            summary.setCompletionPercent(completion);
            summary.setSolarSystemCount(assessment.getSolarSystemCount());
            summary.setAiAgentCount(workforce.getTotalAiAgents());
            summary.setPlayerCount(playerCount);
            summary.setPendingWorkOrders(pending);
            summary.setCriticalFindings(countSeverity(assessment, "CRITICAL"));
            summary.setBuildRunning(buildStatus != null && buildStatus.isRunning());
            summary.setCreatedAt(galaxy.getCreatedAt());

            // Combined score
            double score = computeCombinedScore(
                    assessment.getHealthScore(),
                    completion,
                    workforce.getTotalAiAgents(),
                    pending
            );
            summary.setCombinedScore(Math.round(score * 100.0) / 100.0);

            summaries.add(summary);
        }

        // Sort by combined score descending and assign ranks
        summaries.sort(Comparator.comparingDouble(GalaxyComparisonSummaryDto::getCombinedScore).reversed());
        for (int i = 0; i < summaries.size(); i++) {
            summaries.get(i).setRank(i + 1);
        }

        log.info("Compared {} galaxies — top: {}", summaries.size(),
                summaries.isEmpty() ? "none" : summaries.get(0).getGalaxyName());
        return summaries;
    }

    // ---- Helpers ----

    private String getHealthStatus(int healthScore) {
        if (healthScore >= 80) return "HEALTHY";
        if (healthScore >= 50) return "WARNING";
        return "CRITICAL";
    }

    private int countSeverity(GalaxyOverseerService.GalaxyAssessment assessment, String severityName) {
        return (int) assessment.getFindings().stream()
                .filter(f -> f.getSeverity().name().equals(severityName))
                .count();
    }

    private String compareWinner(int a, int b) {
        if (a > b) return "A";
        if (b > a) return "B";
        return "TIE";
    }

    private String compareLoser(int a, int b) {
        // For work orders, fewer is better
        if (a < b) return "A";
        if (b < a) return "B";
        return "TIE";
    }

    private double computeCombinedScore(int healthScore, int completionPercent, int aiAgentCount, int pendingWorkOrders) {
        // Weighted score: health 40%, completion 30%, staffing 20%, work orders 10%
        // Staffing: more agents = better (capped at 100 for 50+ agents)
        double staffingScore = Math.min(100, aiAgentCount * 2);
        // Work orders: fewer pending = better (capped at 100 for 0 pending)
        double workOrderScore = Math.max(0, 100 - pendingWorkOrders * 10);

        return healthScore * 0.4 +
                completionPercent * 0.3 +
                staffingScore * 0.2 +
                workOrderScore * 0.1;
    }

    private List<String> generateInsights(GalaxyComparisonDto dto) {
        List<String> insights = new ArrayList<>();

        // Health insight
        if (dto.getDeltaHealthScore() != 0) {
            String healthier = dto.getDeltaHealthScore() > 0 ? dto.getGalaxyAName() : dto.getGalaxyBName();
            String other = dto.getDeltaHealthScore() > 0 ? dto.getGalaxyBName() : dto.getGalaxyAName();
            insights.add(String.format("%s is healthier (%d vs %d, delta %d).",
                    healthier,
                    dto.getDeltaHealthScore() > 0 ? dto.getGalaxyAHealthScore() : dto.getGalaxyBHealthScore(),
                    dto.getDeltaHealthScore() > 0 ? dto.getGalaxyBHealthScore() : dto.getGalaxyAHealthScore(),
                    Math.abs(dto.getDeltaHealthScore())));
        } else {
            insights.add("Both galaxies have the same health score.");
        }

        // Completion insight
        if (dto.getDeltaCompletionPercent() != 0) {
            String moreComplete = dto.getDeltaCompletionPercent() > 0 ? dto.getGalaxyAName() : dto.getGalaxyBName();
            insights.add(String.format("%s is more complete (%d%% vs %d%%).",
                    moreComplete,
                    dto.getDeltaCompletionPercent() > 0 ? dto.getGalaxyACompletionPercent() : dto.getGalaxyBCompletionPercent(),
                    dto.getDeltaCompletionPercent() > 0 ? dto.getGalaxyBCompletionPercent() : dto.getGalaxyACompletionPercent()));
        }

        // Systems insight
        if (dto.getDeltaSolarSystemCount() != 0) {
            String moreSystems = dto.getDeltaSolarSystemCount() > 0 ? dto.getGalaxyAName() : dto.getGalaxyBName();
            insights.add(String.format("%s has %d more solar systems.",
                    moreSystems, Math.abs(dto.getDeltaSolarSystemCount())));
        }

        // Staffing insight
        if (dto.getDeltaAiAgentCount() != 0) {
            String moreStaffed = dto.getDeltaAiAgentCount() > 0 ? dto.getGalaxyAName() : dto.getGalaxyBName();
            insights.add(String.format("%s has %d more AI agents.",
                    moreStaffed, Math.abs(dto.getDeltaAiAgentCount())));
        }

        // Work orders insight
        if (dto.getDeltaPendingWorkOrders() != 0) {
            String fewerOrders = dto.getDeltaPendingWorkOrders() > 0 ? dto.getGalaxyBName() : dto.getGalaxyAName();
            insights.add(String.format("%s has %d fewer pending work orders.",
                    fewerOrders, Math.abs(dto.getDeltaPendingWorkOrders())));
        }

        // Critical findings insight
        if (dto.getDeltaCriticalFindings() != 0) {
            String fewerCritical = dto.getDeltaCriticalFindings() > 0 ? dto.getGalaxyBName() : dto.getGalaxyAName();
            insights.add(String.format("%s has %d fewer critical findings.",
                    fewerCritical, Math.abs(dto.getDeltaCriticalFindings())));
        }

        // Playable insight
        if (dto.isGalaxyAIsPlayable() != dto.isGalaxyBIsPlayable()) {
            String playable = dto.isGalaxyAIsPlayable() ? dto.getGalaxyAName() : dto.getGalaxyBName();
            insights.add(String.format("%s is playable while the other is not.", playable));
        }

        // Build running insight
        if (dto.isGalaxyABuildRunning() || dto.isGalaxyBBuildRunning()) {
            String building = dto.isGalaxyABuildRunning() ? dto.getGalaxyAName() : dto.getGalaxyBName();
            insights.add(String.format("%s is currently being built in the background.", building));
        }

        // Game type insight
        if (dto.getGalaxyAGameType() != null && dto.getGalaxyBGameType() != null &&
                !dto.getGalaxyAGameType().equals(dto.getGalaxyBGameType())) {
            insights.add(String.format("Different game types: %s (%s) vs %s (%s).",
                    dto.getGalaxyAName(), dto.getGalaxyAGameType(),
                    dto.getGalaxyBName(), dto.getGalaxyBGameType()));
        }

        return insights;
    }
}
