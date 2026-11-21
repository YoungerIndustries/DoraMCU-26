package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminTrainingScenario;
import com.yisy.modules.doramcu.admin.model.AiAdminTrainingSession;
import com.yisy.modules.doramcu.admin.repository.AiAdminTrainingScenarioRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminTrainingSessionRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.ai.client.AiRestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AiAdminTrainingService Tests")
@ExtendWith(MockitoExtension.class)
class AiAdminTrainingServiceTest {

    @Mock
    private AiAdminTrainingScenarioRepository scenarioRepository;

    @Mock
    private AiAdminTrainingSessionRepository sessionRepository;

    @Mock
    private AiCharacterRepository aiCharacterRepository;

    @Mock
    private AiRestClient aiRestClient;

    @InjectMocks
    private AiAdminTrainingService trainingService;

    private AiCharacter testAdmin;
    private AiAdminTrainingScenario testScenario;

    @BeforeEach
    void setUp() {
        testAdmin = new AiCharacter();
        testAdmin.setId(1L);
        testAdmin.setName("Test Admin");
        testAdmin.setAiAdmin(true);
        testAdmin.setActive(true);
        testAdmin.setEmpireName("Test Empire");
        testAdmin.setRole("Galaxy Overseer");

        testScenario = new AiAdminTrainingScenario();
        testScenario.setId(1L);
        testScenario.setName("Fleet Deployment");
        testScenario.setDescription("Practice fleet deployment");
        testScenario.setDifficulty(AiAdminTrainingScenario.Difficulty.BEGINNER);
        testScenario.setIncidentType(com.yisy.modules.doramcu.admin.model.AiAdminIncident.IncidentType.HEALTH);
        testScenario.setSeverity(com.yisy.modules.doramcu.admin.model.AiAdminIncident.Severity.HIGH);
        testScenario.setScenarioData("{\"action\": \"deploy\"}");
        testScenario.setTimeLimitMinutes(30);
        testScenario.setXpReward(50);
        testScenario.setIsActive(true);
    }

    @Test
    @DisplayName("enrollAdmin: creates session for valid admin and scenario")
    void enrollAdmin_valid() {
        when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(testScenario));
        when(sessionRepository.findByAiAdminIdAndStatus(1L, AiAdminTrainingSession.Status.IN_PROGRESS))
                .thenReturn(List.of());
        when(sessionRepository.save(any(AiAdminTrainingSession.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<AiAdminTrainingSession> result = trainingService.enrollAdmin(1L, 1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getAiAdminId());
        assertEquals(1L, result.get().getScenarioId());
        assertEquals(AiAdminTrainingSession.Status.NOT_STARTED, result.get().getStatus());
        assertEquals(0, result.get().getScore());
        assertEquals(100, result.get().getMaxScore());
        verify(sessionRepository).save(any(AiAdminTrainingSession.class));
    }

    @Test
    @DisplayName("enrollAdmin: rejects non-admin character")
    void enrollAdmin_nonAdmin() {
        AiCharacter nonAdmin = new AiCharacter();
        nonAdmin.setId(2L);
        nonAdmin.setAiAdmin(false);
        when(aiCharacterRepository.findById(2L)).thenReturn(Optional.of(nonAdmin));

        Optional<AiAdminTrainingSession> result = trainingService.enrollAdmin(2L, 1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("enrollAdmin: rejects when admin not found")
    void enrollAdmin_notFound() {
        when(aiCharacterRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<AiAdminTrainingSession> result = trainingService.enrollAdmin(999L, 1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("enrollAdmin: rejects inactive scenario")
    void enrollAdmin_inactiveScenario() {
        testScenario.setIsActive(false);
        when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(testScenario));

        Optional<AiAdminTrainingSession> result = trainingService.enrollAdmin(1L, 1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("enrollAdmin: rejects when scenario not found")
    void enrollAdmin_scenarioNotFound() {
        when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(scenarioRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<AiAdminTrainingSession> result = trainingService.enrollAdmin(1L, 999L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("enrollAdmin: rejects when admin has active session")
    void enrollAdmin_activeSession() {
        when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(testScenario));
        when(sessionRepository.findByAiAdminIdAndStatus(1L, AiAdminTrainingSession.Status.IN_PROGRESS))
                .thenReturn(List.of(new AiAdminTrainingSession()));

        Optional<AiAdminTrainingSession> result = trainingService.enrollAdmin(1L, 1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("startSession: transitions from NOT_STARTED to IN_PROGRESS")
    void startSession_valid() {
        AiAdminTrainingSession session = new AiAdminTrainingSession();
        session.setId(1L);
        session.setStatus(AiAdminTrainingSession.Status.NOT_STARTED);
        session.setScore(0);
        session.setMaxScore(100);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(AiAdminTrainingSession.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<AiAdminTrainingSession> result = trainingService.startSession(1L);

        assertTrue(result.isPresent());
        assertEquals(AiAdminTrainingSession.Status.IN_PROGRESS, result.get().getStatus());
        assertNotNull(result.get().getStartTime());
    }

    @Test
    @DisplayName("startSession: rejects session not found")
    void startSession_notFound() {
        when(sessionRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<AiAdminTrainingSession> result = trainingService.startSession(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("startSession: rejects already started session")
    void startSession_alreadyStarted() {
        AiAdminTrainingSession session = new AiAdminTrainingSession();
        session.setId(1L);
        session.setStatus(AiAdminTrainingSession.Status.IN_PROGRESS);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        Optional<AiAdminTrainingSession> result = trainingService.startSession(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("submitAction: scores action and updates session")
    void submitAction_valid() {
        AiAdminTrainingSession session = new AiAdminTrainingSession();
        session.setId(1L);
        session.setScenarioId(1L);
        session.setStatus(AiAdminTrainingSession.Status.IN_PROGRESS);
        session.setScore(0);
        session.setMaxScore(100);
        session.setActionsTaken("[]");

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(testScenario));
        when(aiRestClient.chat(anyString(), anyString(), isNull())).thenReturn("{\"score\": 15}");
        when(sessionRepository.save(any(AiAdminTrainingSession.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<AiAdminTrainingSession> result = trainingService.submitAction(1L, "Deploy fleet", "Low fleet count");

        assertTrue(result.isPresent());
        assertEquals(15, result.get().getScore());
        String actions = result.get().getActionsTaken();
        assertNotNull(actions);
        assertTrue(actions.contains("Deploy fleet"));
    }

    @Test
    @DisplayName("submitAction: uses default score when LLM fails")
    void submitAction_llmFailure() {
        AiAdminTrainingSession session = new AiAdminTrainingSession();
        session.setId(1L);
        session.setScenarioId(1L);
        session.setStatus(AiAdminTrainingSession.Status.IN_PROGRESS);
        session.setScore(0);
        session.setMaxScore(100);
        session.setActionsTaken("[]");

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(testScenario));
        when(aiRestClient.chat(anyString(), anyString(), isNull())).thenThrow(new RuntimeException("LLM unavailable"));
        when(sessionRepository.save(any(AiAdminTrainingSession.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<AiAdminTrainingSession> result = trainingService.submitAction(1L, "Deploy fleet", "Low fleet count");

        assertTrue(result.isPresent());
        assertEquals(5, result.get().getScore());
    }

    @Test
    @DisplayName("submitAction: caps score at maxScore")
    void submitAction_scoreCap() {
        AiAdminTrainingSession session = new AiAdminTrainingSession();
        session.setId(1L);
        session.setScenarioId(1L);
        session.setStatus(AiAdminTrainingSession.Status.IN_PROGRESS);
        session.setScore(95);
        session.setMaxScore(100);
        session.setActionsTaken("[]");

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(testScenario));
        when(aiRestClient.chat(anyString(), anyString(), isNull())).thenReturn("{\"score\": 20}");
        when(sessionRepository.save(any(AiAdminTrainingSession.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<AiAdminTrainingSession> result = trainingService.submitAction(1L, "Deploy fleet", "Low fleet count");

        assertTrue(result.isPresent());
        assertEquals(100, result.get().getScore());
    }

    @Test
    @DisplayName("submitAction: rejects session not in progress")
    void submitAction_notInProgress() {
        AiAdminTrainingSession session = new AiAdminTrainingSession();
        session.setId(1L);
        session.setStatus(AiAdminTrainingSession.Status.NOT_STARTED);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        Optional<AiAdminTrainingSession> result = trainingService.submitAction(1L, "Deploy fleet", "Low fleet count");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("completeSession: marks completed and generates feedback")
    void completeSession_valid() {
        AiAdminTrainingSession session = new AiAdminTrainingSession();
        session.setId(1L);
        session.setScenarioId(1L);
        session.setAiAdminId(1L);
        session.setStatus(AiAdminTrainingSession.Status.IN_PROGRESS);
        session.setScore(80);
        session.setMaxScore(100);
        session.setActionsTaken("[{\"action\":\"test\"}]");

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(testScenario));
        when(aiRestClient.chat(anyString(), anyString(), isNull())).thenReturn("Great job!");
        when(sessionRepository.save(any(AiAdminTrainingSession.class))).thenAnswer(inv -> inv.getArgument(0));
        when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(testAdmin));

        Optional<AiAdminTrainingSession> result = trainingService.completeSession(1L);

        assertTrue(result.isPresent());
        assertEquals(AiAdminTrainingSession.Status.COMPLETED, result.get().getStatus());
        assertNotNull(result.get().getFeedback());
        assertEquals("Great job!", result.get().getFeedback());
        assertNotNull(result.get().getEndTime());
    }

    @Test
    @DisplayName("completeSession: uses fallback feedback when LLM fails")
    void completeSession_llmFailure() {
        AiAdminTrainingSession session = new AiAdminTrainingSession();
        session.setId(1L);
        session.setScenarioId(1L);
        session.setAiAdminId(1L);
        session.setStatus(AiAdminTrainingSession.Status.IN_PROGRESS);
        session.setScore(80);
        session.setMaxScore(100);
        session.setActionsTaken("[]");

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(testScenario));
        when(aiRestClient.chat(anyString(), anyString(), isNull())).thenThrow(new RuntimeException("LLM unavailable"));
        when(sessionRepository.save(any(AiAdminTrainingSession.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<AiAdminTrainingSession> result = trainingService.completeSession(1L);

        assertTrue(result.isPresent());
        assertEquals(AiAdminTrainingSession.Status.COMPLETED, result.get().getStatus());
        assertNotNull(result.get().getFeedback());
        assertTrue(result.get().getFeedback().contains("Training completed"));
    }

    @Test
    @DisplayName("completeSession: rejects session not in progress")
    void completeSession_notInProgress() {
        AiAdminTrainingSession session = new AiAdminTrainingSession();
        session.setId(1L);
        session.setStatus(AiAdminTrainingSession.Status.NOT_STARTED);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        Optional<AiAdminTrainingSession> result = trainingService.completeSession(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getTrainingProgress: returns sessions for admin")
    void getTrainingProgress() {
        AiAdminTrainingSession session = new AiAdminTrainingSession();
        session.setId(1L);
        session.setAiAdminId(1L);

        when(sessionRepository.findByAiAdminIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(session));

        List<AiAdminTrainingSession> result = trainingService.getTrainingProgress(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    @DisplayName("getTrainingProgress: returns empty list for new admin")
    void getTrainingProgress_empty() {
        when(sessionRepository.findByAiAdminIdOrderByCreatedAtDesc(999L)).thenReturn(List.of());

        List<AiAdminTrainingSession> result = trainingService.getTrainingProgress(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAvailableScenarios: returns active scenarios")
    void getAvailableScenarios() {
        when(scenarioRepository.findByIsActiveTrue()).thenReturn(List.of(testScenario));

        List<AiAdminTrainingScenario> result = trainingService.getAvailableScenarios();

        assertEquals(1, result.size());
        assertEquals("Fleet Deployment", result.get(0).getName());
    }

    @Test
    @DisplayName("getScenariosByDifficulty: filters by difficulty")
    void getScenariosByDifficulty() {
        when(scenarioRepository.findByIsActiveTrueAndDifficulty(AiAdminTrainingScenario.Difficulty.BEGINNER))
                .thenReturn(List.of(testScenario));

        List<AiAdminTrainingScenario> result = trainingService.getScenariosByDifficulty(AiAdminTrainingScenario.Difficulty.BEGINNER);

        assertEquals(1, result.size());
        assertEquals(AiAdminTrainingScenario.Difficulty.BEGINNER, result.get(0).getDifficulty());
    }

    @Test
    @DisplayName("getSession: returns session by id")
    void getSession() {
        AiAdminTrainingSession session = new AiAdminTrainingSession();
        session.setId(1L);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        Optional<AiAdminTrainingSession> result = trainingService.getSession(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @DisplayName("getFeedback: returns feedback for completed session")
    void getFeedback_completed() {
        AiAdminTrainingSession session = new AiAdminTrainingSession();
        session.setId(1L);
        session.setStatus(AiAdminTrainingSession.Status.COMPLETED);
        session.setFeedback("Great work!");

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        Optional<String> result = trainingService.getFeedback(1L);

        assertTrue(result.isPresent());
        assertEquals("Great work!", result.get());
    }

    @Test
    @DisplayName("getFeedback: returns empty for in-progress session")
    void getFeedback_inProgress() {
        AiAdminTrainingSession session = new AiAdminTrainingSession();
        session.setId(1L);
        session.setStatus(AiAdminTrainingSession.Status.IN_PROGRESS);
        session.setFeedback(null);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        Optional<String> result = trainingService.getFeedback(1L);

        assertTrue(result.isEmpty());
    }
}
