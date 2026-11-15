package com.yisy.modules.doramcu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GameMasterServiceTest {

    private GameMasterService service;

    @BeforeEach
    void setUp() {
        service = new GameMasterService();
        service.clearAll();
    }

    @Nested
    class NarrativeTypes {
        @Test
        void getAllNarrativeTypes() {
            List<Map<String, String>> types = service.getAllNarrativeTypes();
            assertEquals(10, types.size());
        }

        @Test
        void narrativeTypeHasNameAndDescription() {
            List<Map<String, String>> types = service.getAllNarrativeTypes();
            types.forEach(type -> {
                assertNotNull(type.get("name"));
                assertNotNull(type.get("label"));
                assertNotNull(type.get("description"));
            });
        }

        @Test
        void narrativeTypeEnumValues() {
            assertAll("NarrativeType values",
                    () -> assertEquals("Story Arc", GameMasterService.NarrativeType.STORY_ARC.getName()),
                    () -> assertEquals("NPC Dialogue", GameMasterService.NarrativeType.NPC_DIALOGUE.getName()),
                    () -> assertEquals("Event Description", GameMasterService.NarrativeType.EVENT_DESCRIPTION.getName()),
                    () -> assertEquals("Quest Generation", GameMasterService.NarrativeType.QUEST_GENERATION.getName()),
                    () -> assertEquals("Scene Setting", GameMasterService.NarrativeType.SCENE_SETTING.getName()),
                    () -> assertEquals("Cliffhanger", GameMasterService.NarrativeType.CLIFFHANGER.getName()),
                    () -> assertEquals("Character Introduction", GameMasterService.NarrativeType.CHARACTER_INTRODUCTION.getName()),
                    () -> assertEquals("Plot Twist", GameMasterService.NarrativeType.TWIST.getName()),
                    () -> assertEquals("Epilogue", GameMasterService.NarrativeType.EPILOGUE.getName()),
                    () -> assertEquals("Flashback", GameMasterService.NarrativeType.FLASHBACK.getName())
            );
        }
    }

    @Nested
    class NarrativeStyles {
        @Test
        void getAllNarrativeStyles() {
            List<Map<String, Object>> styles = service.getAllNarrativeStyles();
            assertEquals(8, styles.size());
        }

        @Test
        void narrativeStyleHasIntensity() {
            List<Map<String, Object>> styles = service.getAllNarrativeStyles();
            styles.forEach(style -> {
                assertNotNull(style.get("intensity"));
                assertTrue((double) style.get("intensity") > 0);
            });
        }

        @Test
        void epicStyleHasHighestIntensity() {
            assertEquals(1.2, GameMasterService.NarrativeStyle.EPIC.getIntensity());
        }

        @Test
        void whimsicalStyleHasLowestIntensity() {
            assertEquals(0.75, GameMasterService.NarrativeStyle.WHIMSICAL.getIntensity());
        }
    }

    @Nested
    class GameUniverses {
        @Test
        void getAllGameUniverses() {
            List<Map<String, String>> universes = service.getAllGameUniverses();
            assertEquals(12, universes.size());
        }

        @Test
        void universeHasNameAndDescription() {
            List<Map<String, String>> universes = service.getAllGameUniverses();
            universes.forEach(universe -> {
                assertNotNull(universe.get("name"));
                assertNotNull(universe.get("label"));
                assertNotNull(universe.get("description"));
            });
        }

        @Test
        void travellerUniverseDescription() {
            assertEquals("Space exploration, alien cultures, trade",
                    GameMasterService.GameUniverse.TRAVELLER.getDescription());
        }

        @Test
        void dnd5eUniverseDescription() {
            assertEquals("Fantasy adventure, magic, dungeons",
                    GameMasterService.GameUniverse.DND_5E.getDescription());
        }
    }

    @Nested
    class Settings {
        @Test
        void getSettingsCreatesDefault() {
            GameMasterService.GameMasterSettings settings = service.getSettings("test1");
            assertNotNull(settings);
            assertEquals(GameMasterService.NarrativeStyle.DRAMATIC, settings.getStyle());
            assertEquals(GameMasterService.GameUniverse.GENERIC, settings.getUniverse());
            assertTrue(settings.isAutoNarrate());
            assertFalse(settings.isAutoQuests());
            assertEquals(5, settings.getMaxActiveQuests());
            assertEquals(3, settings.getMaxActiveArcs());
        }

        @Test
        void updateSettings() {
            GameMasterService.GameMasterSettings settings = service.getSettings("test1");
            settings.setStyle(GameMasterService.NarrativeStyle.EPIC);
            settings.setUniverse(GameMasterService.GameUniverse.STAR_WARS);
            settings.setAutoQuests(true);

            GameMasterService.GameMasterSettings updated = service.updateSettings("test1", settings);
            assertEquals(GameMasterService.NarrativeStyle.EPIC, updated.getStyle());
            assertEquals(GameMasterService.GameUniverse.STAR_WARS, updated.getUniverse());
            assertTrue(updated.isAutoQuests());
        }

        @Test
        void settingsArePerSession() {
            GameMasterService.GameMasterSettings s1 = service.getSettings("test1");
            s1.setStyle(GameMasterService.NarrativeStyle.EPIC);

            GameMasterService.GameMasterSettings s2 = service.getSettings("test2");
            assertEquals(GameMasterService.NarrativeStyle.DRAMATIC, s2.getStyle());
            assertEquals(GameMasterService.NarrativeStyle.EPIC, service.getSettings("test1").getStyle());
        }
    }

    @Nested
    class NarrativeGeneration {
        @Test
        void generateNarrativeReturnsEvent() {
            GameMasterService.NarrativeEvent event = service.generateNarrative(
                    "test1", GameMasterService.NarrativeType.EVENT_DESCRIPTION, "Fleet moves to sector");
            assertNotNull(event);
            assertEquals("test1", event.getSessionId());
            assertEquals(GameMasterService.NarrativeType.EVENT_DESCRIPTION, event.getType());
            assertNotNull(event.getContent());
            assertFalse(event.getContent().isEmpty());
        }

        @Test
        void generateNarrativeAddsToHistory() {
            service.generateNarrative("test1", GameMasterService.NarrativeType.EVENT_DESCRIPTION, "Event 1");
            service.generateNarrative("test1", GameMasterService.NarrativeType.CLIFFHANGER, "Event 2");

            List<GameMasterService.NarrativeEvent> history = service.getEventHistory("test1");
            assertEquals(2, history.size());
        }

        @Test
        void generateNarrativeUpdatesStats() {
            service.generateNarrative("test1", GameMasterService.NarrativeType.EVENT_DESCRIPTION, "Event");
            Map<String, Object> stats = service.getStatistics("test1");
            assertEquals(1, stats.get("narratives_generated"));
        }

        @Test
        void generateContentStoryArc() {
            String content = service.generateContent(
                    GameMasterService.NarrativeType.STORY_ARC,
                    GameMasterService.NarrativeStyle.DRAMATIC,
                    GameMasterService.GameUniverse.TRAVELLER,
                    "A fleet discovers an ancient ruin");
            assertNotNull(content);
            assertTrue(content.length() > 20);
        }

        @Test
        void generateContentNpcDialogue() {
            String content = service.generateContent(
                    GameMasterService.NarrativeType.NPC_DIALOGUE,
                    GameMasterService.NarrativeStyle.DRAMATIC,
                    GameMasterService.GameUniverse.STAR_TREK,
                    "The captain addresses the crew");
            assertNotNull(content);
            assertTrue(content.contains("\""));
        }

        @Test
        void generateContentEventDescription() {
            String content = service.generateContent(
                    GameMasterService.NarrativeType.EVENT_DESCRIPTION,
                    GameMasterService.NarrativeStyle.MYSTERIOUS,
                    GameMasterService.GameUniverse.GENERIC,
                    "A signal is detected");
            assertNotNull(content);
            assertTrue(content.length() > 10);
        }

        @Test
        void generateContentQuestGeneration() {
            String content = service.generateContent(
                    GameMasterService.NarrativeType.QUEST_GENERATION,
                    GameMasterService.NarrativeStyle.EPIC,
                    GameMasterService.GameUniverse.DND_5E,
                    "The dungeon awaits");
            assertNotNull(content);
            assertTrue(content.length() > 10);
        }

        @Test
        void generateContentSceneSetting() {
            String content = service.generateContent(
                    GameMasterService.NarrativeType.SCENE_SETTING,
                    GameMasterService.NarrativeStyle.NOIR,
                    GameMasterService.GameUniverse.GENERIC,
                    "A dark alley");
            assertNotNull(content);
            assertTrue(content.length() > 10);
        }

        @Test
        void generateContentCliffhanger() {
            String content = service.generateContent(
                    GameMasterService.NarrativeType.CLIFFHANGER,
                    GameMasterService.NarrativeStyle.DRAMATIC,
                    GameMasterService.GameUniverse.GENERIC,
                    "The enemy approaches");
            assertNotNull(content);
            assertTrue(content.length() > 10);
        }

        @Test
        void generateContentCharacterIntroduction() {
            String content = service.generateContent(
                    GameMasterService.NarrativeType.CHARACTER_INTRODUCTION,
                    GameMasterService.NarrativeStyle.EPIC,
                    GameMasterService.GameUniverse.GENERIC,
                    "A hero emerges");
            assertNotNull(content);
            assertTrue(content.length() > 10);
        }

        @Test
        void generateContentTwist() {
            String content = service.generateContent(
                    GameMasterService.NarrativeType.TWIST,
                    GameMasterService.NarrativeStyle.MYSTERIOUS,
                    GameMasterService.GameUniverse.GENERIC,
                    "The ally betrays");
            assertNotNull(content);
            assertTrue(content.length() > 10);
        }

        @Test
        void generateContentEpilogue() {
            String content = service.generateContent(
                    GameMasterService.NarrativeType.EPILOGUE,
                    GameMasterService.NarrativeStyle.DRAMATIC,
                    GameMasterService.GameUniverse.GENERIC,
                    "Peace returns");
            assertNotNull(content);
            assertTrue(content.length() > 10);
        }

        @Test
        void generateContentFlashback() {
            String content = service.generateContent(
                    GameMasterService.NarrativeType.FLASHBACK,
                    GameMasterService.NarrativeStyle.DRAMATIC,
                    GameMasterService.GameUniverse.GENERIC,
                    "The old days");
            assertNotNull(content);
            assertTrue(content.length() > 10);
        }

        @Test
        void generateContentWithNullContext() {
            String content = service.generateContent(
                    GameMasterService.NarrativeType.EVENT_DESCRIPTION,
                    GameMasterService.NarrativeStyle.DRAMATIC,
                    GameMasterService.GameUniverse.GENERIC,
                    null);
            assertNotNull(content);
            assertTrue(content.length() > 10);
        }

        @Test
        void generateContentWithEmptyContext() {
            String content = service.generateContent(
                    GameMasterService.NarrativeType.EVENT_DESCRIPTION,
                    GameMasterService.NarrativeStyle.DRAMATIC,
                    GameMasterService.GameUniverse.GENERIC,
                    "");
            assertNotNull(content);
            assertTrue(content.length() > 10);
        }

        @Test
        void differentUniversesProduceDifferentContent() {
            String traveller = service.generateContent(
                    GameMasterService.NarrativeType.STORY_ARC,
                    GameMasterService.NarrativeStyle.DRAMATIC,
                    GameMasterService.GameUniverse.TRAVELLER, "");
            String dnd = service.generateContent(
                    GameMasterService.NarrativeType.STORY_ARC,
                    GameMasterService.NarrativeStyle.DRAMATIC,
                    GameMasterService.GameUniverse.DND_5E, "");
            // Both should be non-empty
            assertTrue(traveller.length() > 10);
            assertTrue(dnd.length() > 10);
        }
    }

    @Nested
    class StoryArcs {
        @Test
        void createStoryArc() {
            GameMasterService.StoryArc arc = service.createStoryArc("test1", "The Lost Sector", "A journey into the unknown");
            assertNotNull(arc);
            assertEquals("The Lost Sector", arc.getTitle());
            assertEquals("test1", arc.getSessionId());
            assertEquals(GameMasterService.StoryArc.ArcStatus.ACTIVE, arc.getStatus());
            assertTrue(arc.getId().startsWith("arc_test1_"));
        }

        @Test
        void createStoryArcAddsToSession() {
            service.createStoryArc("test1", "Arc 1", "Summary 1");
            List<GameMasterService.StoryArc> arcs = service.getArcs("test1");
            assertEquals(1, arcs.size());
        }

        @Test
        void createMultipleArcs() {
            service.createStoryArc("test1", "Arc 1", "Summary 1");
            service.createStoryArc("test1", "Arc 2", "Summary 2");
            assertEquals(2, service.getArcs("test1").size());
        }

        @Test
        void addChapter() {
            GameMasterService.StoryArc arc = service.createStoryArc("test1", "Arc 1", "Summary");
            GameMasterService.StoryArc.Chapter chapter = service.addChapter(
                    "test1", arc.getId(), "Chapter 1", "The hero sets forth on a perilous journey. The stars align.");
            assertNotNull(chapter);
            assertEquals(1, chapter.getNumber());
            assertEquals("Chapter 1", chapter.getTitle());
            assertFalse(chapter.getKeyEvents().isEmpty());
        }

        @Test
        void addChapterIncrementsNumber() {
            GameMasterService.StoryArc arc = service.createStoryArc("test1", "Arc 1", "Summary");
            service.addChapter("test1", arc.getId(), "Ch 1", "First chapter content here. More text.");
            GameMasterService.StoryArc.Chapter ch2 = service.addChapter("test1", arc.getId(), "Ch 2", "Second chapter content here. More text.");
            assertEquals(2, ch2.getNumber());
        }

        @Test
        void addChapterToNonExistentSessionThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    service.addChapter("nonexistent", "arc1", "Ch 1", "Content"));
        }

        @Test
        void addChapterToNonExistentArcThrows() {
            service.createStoryArc("test1", "Arc 1", "Summary");
            assertThrows(IllegalArgumentException.class, () ->
                    service.addChapter("test1", "nonexistent", "Ch 1", "Content"));
        }

        @Test
        void getActiveArcs() {
            service.createStoryArc("test1", "Arc 1", "Summary 1");
            service.createStoryArc("test1", "Arc 2", "Summary 2");
            assertEquals(2, service.getActiveArcs("test1").size());
        }

        @Test
        void getArcsReturnsEmptyForUnknownSession() {
            assertTrue(service.getArcs("unknown").isEmpty());
        }

        @Test
        void storyArcHasUniverseAndStyle() {
            GameMasterService.GameMasterSettings settings = service.getSettings("test1");
            settings.setUniverse(GameMasterService.GameUniverse.STAR_WARS);
            settings.setStyle(GameMasterService.NarrativeStyle.EPIC);

            GameMasterService.StoryArc arc = service.createStoryArc("test1", "Arc", "Summary");
            assertEquals(GameMasterService.GameUniverse.STAR_WARS, arc.getUniverse());
            assertEquals(GameMasterService.NarrativeStyle.EPIC, arc.getStyle());
        }

        @Test
        void storyArcChapterCount() {
            GameMasterService.StoryArc arc = service.createStoryArc("test1", "Arc", "Summary");
            assertEquals(0, arc.getChapterCount());
        }
    }

    @Nested
    class NpcCharacters {
        @Test
        void createNpc() {
            GameMasterService.NpcCharacter npc = service.createNpc(
                    "test1", "Captain Rex", "Military Leader", "Brave and loyal",
                    GameMasterService.NpcCharacter.NpcAlignment.HEROIC);
            assertNotNull(npc);
            assertEquals("Captain Rex", npc.getName());
            assertEquals("Military Leader", npc.getRole());
            assertEquals(GameMasterService.NpcCharacter.NpcAlignment.HEROIC, npc.getAlignment());
        }

        @Test
        void createNpcAddsToSession() {
            service.createNpc("test1", "NPC 1", "Role 1", "Personality 1",
                    GameMasterService.NpcCharacter.NpcAlignment.NEUTRAL);
            assertEquals(1, service.getNpcs("test1").size());
        }

        @Test
        void createMultipleNpcs() {
            service.createNpc("test1", "NPC 1", "Role 1", "P1",
                    GameMasterService.NpcCharacter.NpcAlignment.HEROIC);
            service.createNpc("test1", "NPC 2", "Role 2", "P2",
                    GameMasterService.NpcCharacter.NpcAlignment.VILLAINOUS);
            assertEquals(2, service.getNpcs("test1").size());
        }

        @Test
        void npcHasBackstory() {
            GameMasterService.NpcCharacter npc = service.createNpc(
                    "test1", "NPC", "Role", "Personality",
                    GameMasterService.NpcCharacter.NpcAlignment.NEUTRAL);
            assertNotNull(npc.getBackstory());
            assertTrue(npc.getBackstory().length() > 10);
        }

        @Test
        void npcHasDialogueLines() {
            GameMasterService.NpcCharacter npc = service.createNpc(
                    "test1", "NPC", "Role", "Personality",
                    GameMasterService.NpcCharacter.NpcAlignment.NEUTRAL);
            assertFalse(npc.getDialogueLines().isEmpty());
        }

        @Test
        void getNpcDialogue() {
            service.createNpc("test1", "NPC", "Role", "Personality",
                    GameMasterService.NpcCharacter.NpcAlignment.NEUTRAL);
            List<GameMasterService.NpcCharacter> npcs = service.getNpcs("test1");
            String dialogue = service.getNpcDialogue("test1", npcs.get(0).getId());
            assertNotNull(dialogue);
            assertTrue(dialogue.contains("\""));
        }

        @Test
        void getNpcDialogueReturnsNullForUnknownNpc() {
            assertNull(service.getNpcDialogue("test1", "nonexistent"));
        }

        @Test
        void getNpcDialogueReturnsNullForUnknownSession() {
            assertNull(service.getNpcDialogue("unknown", "npc1"));
        }

        @Test
        void npcAlignmentValues() {
            assertEquals(5, GameMasterService.NpcCharacter.NpcAlignment.values().length);
            assertEquals("Heroic", GameMasterService.NpcCharacter.NpcAlignment.HEROIC.getName());
            assertEquals("Villainous", GameMasterService.NpcCharacter.NpcAlignment.VILLAINOUS.getName());
        }

        @Test
        void npcUniverseMatchesSettings() {
            GameMasterService.GameMasterSettings settings = service.getSettings("test1");
            settings.setUniverse(GameMasterService.GameUniverse.STAR_WARS);

            GameMasterService.NpcCharacter npc = service.createNpc(
                    "test1", "NPC", "Role", "Personality",
                    GameMasterService.NpcCharacter.NpcAlignment.NEUTRAL);
            assertEquals(GameMasterService.GameUniverse.STAR_WARS, npc.getUniverse());
        }

        @Test
        void getNpcsReturnsEmptyForUnknownSession() {
            assertTrue(service.getNpcs("unknown").isEmpty());
        }
    }

    @Nested
    class Quests {
        @Test
        void generateQuest() {
            GameMasterService.Quest quest = service.generateQuest(
                    "test1", "Rescue Mission", "Save the hostages",
                    GameMasterService.Quest.QuestDifficulty.MODERATE);
            assertNotNull(quest);
            assertEquals("Rescue Mission", quest.getTitle());
            assertEquals(GameMasterService.Quest.QuestDifficulty.MODERATE, quest.getDifficulty());
            assertEquals(GameMasterService.Quest.QuestStatus.AVAILABLE, quest.getStatus());
        }

        @Test
        void generateQuestAddsObjectives() {
            GameMasterService.Quest quest = service.generateQuest(
                    "test1", "Quest", "Desc",
                    GameMasterService.Quest.QuestDifficulty.HEROIC);
            assertFalse(quest.getObjectives().isEmpty());
            assertTrue(quest.getObjectives().size() >= 2);
        }

        @Test
        void generateQuestAddsRewards() {
            GameMasterService.Quest quest = service.generateQuest(
                    "test1", "Quest", "Desc",
                    GameMasterService.Quest.QuestDifficulty.MODERATE);
            assertFalse(quest.getRewards().isEmpty());
        }

        @Test
        void generateQuestEstimatedTurns() {
            GameMasterService.Quest quest = service.generateQuest(
                    "test1", "Quest", "Desc",
                    GameMasterService.Quest.QuestDifficulty.MODERATE);
            assertEquals(6, quest.getEstimatedTurns()); // level 3 * 2
        }

        @Test
        void generateQuestRespectsMaxActive() {
            GameMasterService.GameMasterSettings settings = service.getSettings("test1");
            settings.setMaxActiveQuests(2);

            service.generateQuest("test1", "Q1", "D1", GameMasterService.Quest.QuestDifficulty.EASY);
            service.generateQuest("test1", "Q2", "D2", GameMasterService.Quest.QuestDifficulty.EASY);
            GameMasterService.Quest q3 = service.generateQuest("test1", "Q3", "D3", GameMasterService.Quest.QuestDifficulty.EASY);

            assertNull(q3); // max reached
        }

        @Test
        void getAvailableQuests() {
            service.generateQuest("test1", "Q1", "D1", GameMasterService.Quest.QuestDifficulty.EASY);
            service.generateQuest("test1", "Q2", "D2", GameMasterService.Quest.QuestDifficulty.EASY);
            assertEquals(2, service.getAvailableQuests("test1").size());
        }

        @Test
        void getQuestsReturnsEmptyForUnknownSession() {
            assertTrue(service.getQuests("unknown").isEmpty());
        }

        @Test
        void questDifficultyLevels() {
            assertEquals(1, GameMasterService.Quest.QuestDifficulty.TRIVIAL.getLevel());
            assertEquals(6, GameMasterService.Quest.QuestDifficulty.LEGENDARY.getLevel());
        }

        @Test
        void questObjectiveModel() {
            GameMasterService.Quest.QuestObjective obj = new GameMasterService.Quest.QuestObjective("obj1", "Test objective");
            assertEquals("obj1", obj.getId());
            assertEquals("Test objective", obj.getDescription());
            assertFalse(obj.isCompleted());
        }

        @Test
        void questRewardModel() {
            GameMasterService.Quest.QuestReward reward = new GameMasterService.Quest.QuestReward("credits", "Gold", 100);
            assertEquals("credits", reward.getType());
            assertEquals("Gold", reward.getDescription());
            assertEquals(100, reward.getAmount());
        }

        @Test
        void legendaryQuestHasEquipmentReward() {
            GameMasterService.Quest quest = service.generateQuest(
                    "test1", "Quest", "Desc",
                    GameMasterService.Quest.QuestDifficulty.LEGENDARY);
            boolean hasEquipment = quest.getRewards().stream()
                    .anyMatch(r -> "equipment".equals(r.getType()));
            assertTrue(hasEquipment);
        }

        @Test
        void questCompletedObjectives() {
            GameMasterService.Quest quest = service.generateQuest(
                    "test1", "Quest", "Desc",
                    GameMasterService.Quest.QuestDifficulty.EASY);
            assertEquals(0, quest.getCompletedObjectives());
        }
    }

    @Nested
    class EventHistory {
        @Test
        void getEventHistory() {
            service.generateNarrative("test1", GameMasterService.NarrativeType.EVENT_DESCRIPTION, "Event 1");
            service.generateNarrative("test1", GameMasterService.NarrativeType.CLIFFHANGER, "Event 2");
            assertEquals(2, service.getEventHistory("test1").size());
        }

        @Test
        void getRecentEvents() {
            service.generateNarrative("test1", GameMasterService.NarrativeType.EVENT_DESCRIPTION, "E1");
            service.generateNarrative("test1", GameMasterService.NarrativeType.EVENT_DESCRIPTION, "E2");
            service.generateNarrative("test1", GameMasterService.NarrativeType.EVENT_DESCRIPTION, "E3");

            List<GameMasterService.NarrativeEvent> recent = service.getRecentEvents("test1", 2);
            assertEquals(2, recent.size());
        }

        @Test
        void getRecentEventsReturnsAllIfLimitExceeds() {
            service.generateNarrative("test1", GameMasterService.NarrativeType.EVENT_DESCRIPTION, "E1");
            List<GameMasterService.NarrativeEvent> recent = service.getRecentEvents("test1", 10);
            assertEquals(1, recent.size());
        }

        @Test
        void getEventsByType() {
            service.generateNarrative("test1", GameMasterService.NarrativeType.EVENT_DESCRIPTION, "E1");
            service.generateNarrative("test1", GameMasterService.NarrativeType.CLIFFHANGER, "E2");
            service.generateNarrative("test1", GameMasterService.NarrativeType.EVENT_DESCRIPTION, "E3");

            List<GameMasterService.NarrativeEvent> events = service.getEventsByType(
                    "test1", GameMasterService.NarrativeType.EVENT_DESCRIPTION);
            assertEquals(2, events.size());
        }

        @Test
        void getEventHistoryReturnsEmptyForUnknownSession() {
            assertTrue(service.getEventHistory("unknown").isEmpty());
        }

        @Test
        void narrativeEventModel() {
            GameMasterService.NarrativeEvent event = service.generateNarrative(
                    "test1", GameMasterService.NarrativeType.EVENT_DESCRIPTION, "Context");
            assertNotNull(event.getId());
            assertEquals("test1", event.getSessionId());
            assertEquals(GameMasterService.NarrativeType.EVENT_DESCRIPTION, event.getType());
            assertEquals("Context", event.getContext());
            assertNotNull(event.getCreatedAt());
        }
    }

    @Nested
    class Statistics {
        @Test
        void getStatisticsReturnsAllFields() {
            service.generateNarrative("test1", GameMasterService.NarrativeType.EVENT_DESCRIPTION, "Event");
            service.createStoryArc("test1", "Arc", "Summary");
            service.createNpc("test1", "NPC", "Role", "P",
                    GameMasterService.NpcCharacter.NpcAlignment.NEUTRAL);
            service.generateQuest("test1", "Quest", "Desc",
                    GameMasterService.Quest.QuestDifficulty.EASY);

            Map<String, Object> stats = service.getStatistics("test1");
            assertEquals(1, stats.get("narratives_generated"));
            assertEquals(1, stats.get("active_arcs"));
            assertEquals(1, stats.get("total_arcs"));
            assertEquals(1, stats.get("npcs"));
            assertEquals(1, stats.get("quests"));
            assertEquals(1, stats.get("available_quests"));
            assertEquals(1, stats.get("event_history_size"));
        }

        @Test
        void getStatisticsReturnsZeroForUnknownSession() {
            Map<String, Object> stats = service.getStatistics("unknown");
            assertEquals(0, stats.get("narratives_generated"));
            assertEquals(0, stats.get("active_arcs"));
        }
    }

    @Nested
    class SessionManagement {
        @Test
        void getAllSessions() {
            service.getSettings("test1");
            service.getSettings("test2");
            List<String> sessions = service.getAllSessions();
            assertEquals(2, sessions.size());
            assertTrue(sessions.contains("test1"));
            assertTrue(sessions.contains("test2"));
        }

        @Test
        void clearSession() {
            service.generateNarrative("test1", GameMasterService.NarrativeType.EVENT_DESCRIPTION, "Event");
            service.clearSession("test1");
            assertTrue(service.getEventHistory("test1").isEmpty());
            assertFalse(service.getAllSessions().contains("test1"));
        }

        @Test
        void clearAll() {
            service.getSettings("test1");
            service.getSettings("test2");
            service.clearAll();
            assertTrue(service.getAllSessions().isEmpty());
        }
    }

    @Nested
    class IntegrationScenarios {
        @Test
        void fullNarrativeWorkflow() {
            // Set up session
            GameMasterService.GameMasterSettings settings = service.getSettings("campaign1");
            settings.setUniverse(GameMasterService.GameUniverse.STAR_WARS);
            settings.setStyle(GameMasterService.NarrativeStyle.EPIC);

            // Create story arc
            GameMasterService.StoryArc arc = service.createStoryArc("campaign1", "Rebel Alliance", "The fight for freedom");
            assertNotNull(arc);

            // Add chapters
            GameMasterService.StoryArc.Chapter ch1 = service.addChapter(
                    "campaign1", arc.getId(), "Rising", "The rebellion begins to take shape. Key figures emerge.");
            assertEquals(1, ch1.getNumber());

            // Create NPCs
            GameMasterService.NpcCharacter npc = service.createNpc(
                    "campaign1", "Han Solo", "Smuggler", "Rogue with a heart of gold",
                    GameMasterService.NpcCharacter.NpcAlignment.HEROIC);
            assertNotNull(npc);

            // Generate quest
            GameMasterService.Quest quest = service.generateQuest(
                    "campaign1", "Death Star Plans", "Retrieve the plans",
                    GameMasterService.Quest.QuestDifficulty.HEROIC);
            assertNotNull(quest);

            // Generate narrative events
            service.generateNarrative("campaign1", GameMasterService.NarrativeType.SCENE_SETTING, "The Rebel base");
            service.generateNarrative("campaign1", GameMasterService.NarrativeType.CLIFFHANGER, "Imperial forces approach");

            // Check stats
            Map<String, Object> stats = service.getStatistics("campaign1");
            assertEquals(2, stats.get("narratives_generated"));
            assertEquals(1, stats.get("active_arcs"));
            assertEquals(1, stats.get("npcs"));
            assertEquals(1, stats.get("quests"));
        }

        @Test
        void multiSessionIsolation() {
            service.getSettings("s1").setUniverse(GameMasterService.GameUniverse.TRAVELLER);
            service.getSettings("s2").setUniverse(GameMasterService.GameUniverse.DND_5E);

            service.createStoryArc("s1", "Arc 1", "Summary");
            service.createStoryArc("s2", "Arc 2", "Summary");

            assertEquals(1, service.getArcs("s1").size());
            assertEquals(1, service.getArcs("s2").size());
            assertEquals(GameMasterService.GameUniverse.TRAVELLER, service.getSettings("s1").getUniverse());
            assertEquals(GameMasterService.GameUniverse.DND_5E, service.getSettings("s2").getUniverse());
        }

        @Test
        void questAndNarrativeCombined() {
            GameMasterService.Quest quest = service.generateQuest(
                    "test1", "Quest", "Desc",
                    GameMasterService.Quest.QuestDifficulty.MODERATE);
            assertNotNull(quest);
            assertFalse(quest.getObjectives().isEmpty());

            service.generateNarrative("test1", GameMasterService.NarrativeType.QUEST_GENERATION,
                    quest.getTitle());
            assertEquals(1, service.getEventHistory("test1").size());
        }
    }
}
