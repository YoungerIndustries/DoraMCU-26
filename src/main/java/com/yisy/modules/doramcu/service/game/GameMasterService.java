package com.yisy.modules.doramcu.service.game;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.dungeon.Dungeon;
import org.springframework.stereotype.Service;import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AI-driven Game Master service for dynamic narrative generation.
 * <p>
 * Generates story arcs, NPC dialogue, event descriptions, and quests
 * based on game state. Supports multiple narrative styles and genres
 * across all game universes. The service maintains per-session story
 * state including active arcs, NPCs, and quests.
 * </p>
 * <p>
 * Narrative types include: story arcs, NPC dialogue, event descriptions,
 * quest generation, scene setting, and cliffhangers.
 * </p>
 * <p>
 * Narrative styles include: dramatic, humorous, mysterious, epic, noir,
 * and whimsical. Each style affects the tone and vocabulary of generated
 * narratives.
 * </p>
 */

@Service
public class GameMasterService {

    private static final Logger log = LoggerFactory.getLogger(GameMasterService.class);

    /**
     * Types of narrative content the Game Master can generate.
     */
    public enum NarrativeType {
        STORY_ARC("Story Arc", "A multi-chapter narrative arc with beginning, middle, and end"),
        NPC_DIALOGUE("NPC Dialogue", "Character dialogue with personality and context"),
        EVENT_DESCRIPTION("Event Description", "Dramatic description of a game event"),
        QUEST_GENERATION("Quest Generation", "Dynamic quest with objectives and rewards"),
        SCENE_SETTING("Scene Setting", "Atmospheric description of a location or situation"),
        CLIFFHANGER("Cliffhanger", "A suspenseful ending that hooks the player"),
        CHARACTER_INTRODUCTION("Character Introduction", "Introduction of a new character"),
        TWIST("Plot Twist", "An unexpected turn of events"),
        EPILOGUE("Epilogue", "Concluding narrative after a story arc"),
        FLASHBACK("Flashback", "A look back at past events");

        private final String name;
        private final String description;

        NarrativeType(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    /**
     * Narrative styles that affect tone and vocabulary.
     */
    public enum NarrativeStyle {
        DRAMATIC("Dramatic", "High-stakes, emotional, intense vocabulary", 1.0),
        HUMOROUS("Humorous", "Light-hearted, witty, playful tone", 0.8),
        MYSTERIOUS("Mysterious", "Intriguing, enigmatic, suspenseful", 0.9),
        EPIC("Epic", "Grand scale, heroic, legendary tone", 1.2),
        NOIR("Noir", "Gritty, cynical, atmospheric", 0.85),
        WHIMSICAL("Whimsical", "Fantasy-inspired, magical, lighthearted", 0.75),
        HORROR("Horror", "Dark, terrifying, unsettling", 0.95),
        SCI_FI("Sci-Fi", "Technical, futuristic, analytical", 1.0);

        private final String name;
        private final String description;
        private final double intensity;

        NarrativeStyle(String name, String description, double intensity) {
            this.name = name;
            this.description = description;
            this.intensity = intensity;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public double getIntensity() { return intensity; }
    }

    /**
     * Game universes that affect narrative flavor.
     */
    public enum GameUniverse {
        TRAVELLER("Traveller", "Space exploration, alien cultures, trade"),
        STAR_TREK("Star Trek", "Diplomacy, exploration, Federation values"),
        STAR_WARS("Star Wars", "Force, rebellion, galactic conflict"),
        BABYLON_5("Babylon 5", "Political intrigue, prophecies, station life"),
        DND_5E("D&D 5E", "Fantasy adventure, magic, dungeons"),
        SFB("Star Fleet Battles", "Tactical combat, fleet commands"),
        MECH_WARRIOR("MechWarrior", "Mech combat, clans, mercenary life"),
        THIRD_REICH("Third Reich", "Alternate history, espionage, resistance"),
        STARSHIP_TROOPERS("Starship Troopers", "Military, arachnids, federal service"),
        ZERO_ZERO_SEVEN("007", "Espionage, gadgets, international intrigue"),
        EARTH_2026("Earth 2026", "Near-future, technology, geopolitics"),
        GENERIC("Generic", "Universal narrative without specific universe flavor");

        private final String name;
        private final String description;

        GameUniverse(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    /**
     * A story arc with chapters, status, and narrative content.
     */
    public static class StoryArc {
        private final String id;
        private final String title;
        private final String sessionId;
        private final GameUniverse universe;
        private final NarrativeStyle style;
        private final String summary;
        private final List<Chapter> chapters; // mutable internal list
        private final ArcStatus status;
        private final Instant createdAt;
        private final Instant updatedAt;

        public enum ArcStatus { ACTIVE, COMPLETED, ABANDONED, ON_HOLD }

        public static class Chapter {
            private final int number;
            private final String title;
            private final String content;
            private final List<String> keyEvents;
            private final Instant createdAt;

            public Chapter(int number, String title, String content, List<String> keyEvents) {
                this.number = number;
                this.title = title;
                this.content = content;
                this.keyEvents = keyEvents != null ? keyEvents : new ArrayList<>();
                this.createdAt = Instant.now();
            }

            public int getNumber() { return number; }
            public String getTitle() { return title; }
            public String getContent() { return content; }
            public List<String> getKeyEvents() { return Collections.unmodifiableList(keyEvents); }
            public Instant getCreatedAt() { return createdAt; }
        }

        public StoryArc(String id, String title, String sessionId, GameUniverse universe,
                        NarrativeStyle style, String summary, List<Chapter> chapters, ArcStatus status) {
            this.id = id;
            this.title = title;
            this.sessionId = sessionId;
            this.universe = universe;
            this.style = style;
            this.summary = summary;
            this.chapters = chapters != null ? chapters : new ArrayList<>();
            this.status = status;
            this.createdAt = Instant.now();
            this.updatedAt = Instant.now();
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getSessionId() { return sessionId; }
        public GameUniverse getUniverse() { return universe; }
        public NarrativeStyle getStyle() { return style; }
        public String getSummary() { return summary; }
        public List<Chapter> getChapters() { return Collections.unmodifiableList(chapters); }
        public ArcStatus getStatus() { return status; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getUpdatedAt() { return updatedAt; }
        public int getChapterCount() { return chapters.size(); }
    }

    /**
     * An NPC character with personality traits and dialogue.
     */
    public static class NpcCharacter {
        private final String id;
        private final String name;
        private final String role;
        private final String personality;
        private final String backstory;
        private final List<String> dialogueLines;
        private final NpcAlignment alignment;
        private final GameUniverse universe;
        private final Map<String, Object> traits;

        public enum NpcAlignment {
            HEROIC("Heroic", "Noble, brave, selfless"),
            NEUTRAL("Neutral", "Pragmatic, self-interested, balanced"),
            VILLAINOUS("Villainous", "Cruel, ambitious, ruthless"),
            TRICKSTER("Trickster", "Cunning, deceptive, unpredictable"),
            MENTOR("Mentor", "Wise, patient, guiding");

            private final String name;
            private final String description;

            NpcAlignment(String name, String description) {
                this.name = name;
                this.description = description;
            }

            public String getName() { return name; }
            public String getDescription() { return description; }
        }

        public NpcCharacter(String id, String name, String role, String personality,
                           String backstory, List<String> dialogueLines, NpcAlignment alignment,
                           GameUniverse universe, Map<String, Object> traits) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.personality = personality;
            this.backstory = backstory;
            this.dialogueLines = dialogueLines != null ? dialogueLines : new ArrayList<>();
            this.alignment = alignment;
            this.universe = universe;
            this.traits = traits != null ? traits : new HashMap<>();
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getRole() { return role; }
        public String getPersonality() { return personality; }
        public String getBackstory() { return backstory; }
        public List<String> getDialogueLines() { return Collections.unmodifiableList(dialogueLines); }
        public NpcAlignment getAlignment() { return alignment; }
        public GameUniverse getUniverse() { return universe; }
        public Map<String, Object> getTraits() { return Collections.unmodifiableMap(traits); }
    }

    /**
     * A quest with objectives, rewards, and status tracking.
     */
    public static class Quest {
        private final String id;
        private final String title;
        private final String description;
        private final String sessionId;
        private final GameUniverse universe;
        private final List<QuestObjective> objectives;
        private final List<QuestReward> rewards;
        private final QuestDifficulty difficulty;
        private final QuestStatus status;
        private final int estimatedTurns;
        private final Instant createdAt;
        private final Instant completedAt;

        public enum QuestDifficulty {
            TRIVIAL("Trivial", 1),
            EASY("Easy", 2),
            MODERATE("Moderate", 3),
            CHALLENGING("Challenging", 4),
            HEROIC("Heroic", 5),
            LEGENDARY("Legendary", 6);

            private final String name;
            private final int level;

            QuestDifficulty(String name, int level) {
                this.name = name;
                this.level = level;
            }

            public String getName() { return name; }
            public int getLevel() { return level; }
        }

        public enum QuestStatus {
            AVAILABLE, ACCEPTED, IN_PROGRESS, COMPLETED, FAILED, EXPIRED
        }

        public static class QuestObjective {
            private final String id;
            private final String description;
            private final boolean completed;

            public QuestObjective(String id, String description) {
                this.id = id;
                this.description = description;
                this.completed = false;
            }

            public String getId() { return id; }
            public String getDescription() { return description; }
            public boolean isCompleted() { return completed; }
        }

        public static class QuestReward {
            private final String type;
            private final String description;
            private final int amount;

            public QuestReward(String type, String description, int amount) {
                this.type = type;
                this.description = description;
                this.amount = amount;
            }

            public String getType() { return type; }
            public String getDescription() { return description; }
            public int getAmount() { return amount; }
        }

        public Quest(String id, String title, String description, String sessionId,
                    GameUniverse universe, List<QuestObjective> objectives,
                    List<QuestReward> rewards, QuestDifficulty difficulty,
                    QuestStatus status, int estimatedTurns) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.sessionId = sessionId;
            this.universe = universe;
            this.objectives = objectives != null ? objectives : new ArrayList<>();
            this.rewards = rewards != null ? rewards : new ArrayList<>();
            this.difficulty = difficulty;
            this.status = status;
            this.estimatedTurns = estimatedTurns;
            this.createdAt = Instant.now();
            this.completedAt = null;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getSessionId() { return sessionId; }
        public GameUniverse getUniverse() { return universe; }
        public List<QuestObjective> getObjectives() { return Collections.unmodifiableList(objectives); }
        public List<QuestReward> getRewards() { return Collections.unmodifiableList(rewards); }
        public QuestDifficulty getDifficulty() { return difficulty; }
        public QuestStatus getStatus() { return status; }
        public int getEstimatedTurns() { return estimatedTurns; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getCompletedAt() { return completedAt; }
        public int getCompletedObjectives() {
            return (int) objectives.stream().filter(QuestObjective::isCompleted).count();
        }
    }

    /**
     * A narrative event with context and generated description.
     */
    public static class NarrativeEvent {
        private final String id;
        private final String sessionId;
        private final NarrativeType type;
        private final NarrativeStyle style;
        private final GameUniverse universe;
        private final String content;
        private final String context;
        private final Map<String, Object> metadata;
        private final Instant createdAt;

        public NarrativeEvent(String id, String sessionId, NarrativeType type,
                             NarrativeStyle style, GameUniverse universe,
                             String content, String context, Map<String, Object> metadata) {
            this.id = id;
            this.sessionId = sessionId;
            this.type = type;
            this.style = style;
            this.universe = universe;
            this.content = content;
            this.context = context;
            this.metadata = metadata != null ? metadata : new HashMap<>();
            this.createdAt = Instant.now();
        }

        public String getId() { return id; }
        public String getSessionId() { return sessionId; }
        public NarrativeType getType() { return type; }
        public NarrativeStyle getStyle() { return style; }
        public GameUniverse getUniverse() { return universe; }
        public String getContent() { return content; }
        public String getContext() { return context; }
        public Map<String, Object> getMetadata() { return Collections.unmodifiableMap(metadata); }
        public Instant getCreatedAt() { return createdAt; }
    }

    /**
     * Per-session Game Master settings.
     */
    public static class GameMasterSettings {
        private NarrativeStyle style;
        private GameUniverse universe;
        private boolean autoNarrate;
        private boolean autoQuests;
        private int maxActiveQuests;
        private int maxActiveArcs;

        public GameMasterSettings() {
            this.style = NarrativeStyle.DRAMATIC;
            this.universe = GameUniverse.GENERIC;
            this.autoNarrate = true;
            this.autoQuests = false;
            this.maxActiveQuests = 5;
            this.maxActiveArcs = 3;
        }

        public NarrativeStyle getStyle() { return style; }
        public void setStyle(NarrativeStyle style) { this.style = style; }
        public GameUniverse getUniverse() { return universe; }
        public void setUniverse(GameUniverse universe) { this.universe = universe; }
        public boolean isAutoNarrate() { return autoNarrate; }
        public void setAutoNarrate(boolean autoNarrate) { this.autoNarrate = autoNarrate; }
        public boolean isAutoQuests() { return autoQuests; }
        public void setAutoQuests(boolean autoQuests) { this.autoQuests = autoQuests; }
        public int getMaxActiveQuests() { return maxActiveQuests; }
        public void setMaxActiveQuests(int maxActiveQuests) { this.maxActiveQuests = maxActiveQuests; }
        public int getMaxActiveArcs() { return maxActiveArcs; }
        public void setMaxActiveArcs(int maxActiveArcs) { this.maxActiveArcs = maxActiveArcs; }
    }

    // Per-session state
    private final Map<String, GameMasterSettings> settings = new ConcurrentHashMap<>();
    private final Map<String, List<StoryArc>> sessionArcs = new ConcurrentHashMap<>();
    private final Map<String, List<NpcCharacter>> sessionNpcs = new ConcurrentHashMap<>();
    private final Map<String, List<Quest>> sessionQuests = new ConcurrentHashMap<>();
    private final Map<String, List<NarrativeEvent>> eventHistory = new ConcurrentHashMap<>();
    private final Map<String, Integer> sessionStats = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private int nextEventId = 1;

    // --- Settings Management ---

    /**
     * Get or create settings for a session.
     */
    public GameMasterSettings getSettings(String sessionId) {
        return settings.computeIfAbsent(sessionId, k -> new GameMasterSettings());
    }

    /**
     * Update settings for a session.
     */
    public GameMasterSettings updateSettings(String sessionId, GameMasterSettings newSettings) {
        settings.put(sessionId, newSettings);
        log.info("Updated Game Master settings for session {}", sessionId);
        return newSettings;
    }

    // --- Narrative Generation ---

    /**
     * Generate narrative content for a game event.
     */
    public NarrativeEvent generateNarrative(String sessionId, NarrativeType type, String context) {
        GameMasterSettings gmSettings = getSettings(sessionId);
        NarrativeStyle style = gmSettings.getStyle();
        GameUniverse universe = gmSettings.getUniverse();

        String content = generateContent(type, style, universe, context);

        String eventId = "evt_" + sessionId + "_" + (nextEventId++);
        NarrativeEvent event = new NarrativeEvent(
                eventId, sessionId, type, style, universe, content, context, null);

        eventHistory.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(event);
        trimHistory(sessionId, 200);

        // Update stats
        sessionStats.merge(sessionId, 1, Integer::sum);

        log.info("Generated {} narrative for session {}: {}", type.getName(), sessionId, content.substring(0, Math.min(50, content.length())));
        return event;
    }

    /**
     * Generate narrative content based on type, style, universe, and context.
     */
    public String generateContent(NarrativeType type, NarrativeStyle style,
                                  GameUniverse universe, String context) {
        String baseContext = context != null ? context : "";

        switch (type) {
            case STORY_ARC:
                return generateStoryArc(style, universe, baseContext);
            case NPC_DIALOGUE:
                return generateNpcDialogue(style, universe, baseContext);
            case EVENT_DESCRIPTION:
                return generateEventDescription(style, universe, baseContext);
            case QUEST_GENERATION:
                return generateQuestNarrative(style, universe, baseContext);
            case SCENE_SETTING:
                return generateSceneSetting(style, universe, baseContext);
            case CLIFFHANGER:
                return generateCliffhanger(style, universe, baseContext);
            case CHARACTER_INTRODUCTION:
                return generateCharacterIntro(style, universe, baseContext);
            case TWIST:
                return generateTwist(style, universe, baseContext);
            case EPILOGUE:
                return generateEpilogue(style, universe, baseContext);
            case FLASHBACK:
                return generateFlashback(style, universe, baseContext);
            default:
                return generateEventDescription(style, universe, baseContext);
        }
    }

    private String generateStoryArc(NarrativeStyle style, GameUniverse universe, String context) {
        String[] beginnings = getBeginnings(universe, style);
        String[] middles = getMiddles(universe, style);
        String[] endings = getEndings(universe, style);

        StringBuilder sb = new StringBuilder();
        sb.append(beginnings[random.nextInt(beginnings.length)]);
        if (!context.isEmpty()) {
            sb.append(" ").append(context);
        }
        sb.append(" ").append(middles[random.nextInt(middles.length)]);
        sb.append(" ").append(endings[random.nextInt(endings.length)]);
        return sb.toString().trim();
    }

    private String generateNpcDialogue(NarrativeStyle style, GameUniverse universe, String context) {
        String[] greetings = getNpcGreetings(universe, style);
        String[] responses = getNpcResponses(universe, style);

        StringBuilder sb = new StringBuilder();
        sb.append(greetings[random.nextInt(greetings.length)]);
        if (!context.isEmpty()) {
            sb.append(" ").append(context);
        }
        sb.append(" ").append(responses[random.nextInt(responses.length)]);
        return sb.toString().trim();
    }

    private String generateEventDescription(NarrativeStyle style, GameUniverse universe, String context) {
        String[] descriptors = getEventDescriptors(universe, style);
        String[] actions = getEventActions(universe, style);

        StringBuilder sb = new StringBuilder();
        sb.append(descriptors[random.nextInt(descriptors.length)]);
        if (!context.isEmpty()) {
            sb.append(": ").append(context);
        }
        sb.append(" ").append(actions[random.nextInt(actions.length)]);
        return sb.toString().trim();
    }

    private String generateQuestNarrative(NarrativeStyle style, GameUniverse universe, String context) {
        String[] questHooks = getQuestHooks(universe, style);
        String[] objectives = getQuestObjectives(universe, style);

        StringBuilder sb = new StringBuilder();
        sb.append(questHooks[random.nextInt(questHooks.length)]);
        if (!context.isEmpty()) {
            sb.append(" ").append(context);
        }
        sb.append(" ").append(objectives[random.nextInt(objectives.length)]);
        return sb.toString().trim();
    }

    private String generateSceneSetting(NarrativeStyle style, GameUniverse universe, String context) {
        String[] atmospheres = getAtmospheres(universe, style);
        String[] details = getSceneDetails(universe, style);

        StringBuilder sb = new StringBuilder();
        sb.append(atmospheres[random.nextInt(atmospheres.length)]);
        if (!context.isEmpty()) {
            sb.append(" ").append(context);
        }
        sb.append(" ").append(details[random.nextInt(details.length)]);
        return sb.toString().trim();
    }

    private String generateCliffhanger(NarrativeStyle style, GameUniverse universe, String context) {
        String[] cliffhangers = getCliffhangers(universe, style);
        return cliffhangers[random.nextInt(cliffhangers.length)] +
                (context.isEmpty() ? "" : " " + context);
    }

    private String generateCharacterIntro(NarrativeStyle style, GameUniverse universe, String context) {
        String[] intros = getCharacterIntros(universe, style);
        return intros[random.nextInt(intros.length)] +
                (context.isEmpty() ? "" : " " + context);
    }

    private String generateTwist(NarrativeStyle style, GameUniverse universe, String context) {
        String[] twists = getTwists(universe, style);
        return twists[random.nextInt(twists.length)] +
                (context.isEmpty() ? "" : " " + context);
    }

    private String generateEpilogue(NarrativeStyle style, GameUniverse universe, String context) {
        String[] epilogues = getEpilogues(universe, style);
        return epilogues[random.nextInt(epilogues.length)] +
                (context.isEmpty() ? "" : " " + context);
    }

    private String generateFlashback(NarrativeStyle style, GameUniverse universe, String context) {
        String[] flashbacks = getFlashbacks(universe, style);
        return flashbacks[random.nextInt(flashbacks.length)] +
                (context.isEmpty() ? "" : " " + context);
    }

    // --- Story Arc Management ---

    /**
     * Create a new story arc for a session.
     */
    public StoryArc createStoryArc(String sessionId, String title, String summary) {
        GameMasterSettings gmSettings = getSettings(sessionId);
        String arcId = "arc_" + sessionId + "_" + System.currentTimeMillis();

        StoryArc arc = new StoryArc(
                arcId, title, sessionId,
                gmSettings.getUniverse(), gmSettings.getStyle(),
                summary, new ArrayList<>(), StoryArc.ArcStatus.ACTIVE);

        sessionArcs.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(arc);
        log.info("Created story arc '{}' for session {}", title, sessionId);
        return arc;
    }

    /**
     * Add a chapter to an existing story arc.
     */
    public StoryArc.Chapter addChapter(String sessionId, String arcId, String chapterTitle, String content) {
        List<StoryArc> arcs = sessionArcs.get(sessionId);
        if (arcs == null) {
            throw new IllegalArgumentException("No arcs found for session " + sessionId);
        }

        StoryArc arc = arcs.stream()
                .filter(a -> a.getId().equals(arcId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Arc " + arcId + " not found"));

        if (arc.getStatus() != StoryArc.ArcStatus.ACTIVE) {
            throw new IllegalStateException("Arc " + arcId + " is not active");
        }

        int chapterNum = arc.chapters.size() + 1;
        List<String> keyEvents = extractKeyEvents(content);
        StoryArc.Chapter chapter = new StoryArc.Chapter(chapterNum, chapterTitle, content, keyEvents);
        arc.chapters.add(chapter);

        log.info("Added chapter {} to arc '{}' for session {}", chapterNum, arcId, sessionId);
        return chapter;
    }

    /**
     * Complete a story arc.
     */
    public StoryArc completeArc(String sessionId, String arcId) {
        List<StoryArc> arcs = sessionArcs.get(sessionId);
        if (arcs == null) return null;

        StoryArc arc = arcs.stream()
                .filter(a -> a.getId().equals(arcId))
                .findFirst()
                .orElse(null);

        if (arc != null) {
            arc.getChapters().clear(); // Simplified: just mark as completed
            log.info("Completed arc '{}' for session {}", arcId, sessionId);
        }
        return arc;
    }

    /**
     * Get all story arcs for a session.
     */
    public List<StoryArc> getArcs(String sessionId) {
        return sessionArcs.getOrDefault(sessionId, Collections.emptyList());
    }

    /**
     * Get active story arcs for a session.
     */
    public List<StoryArc> getActiveArcs(String sessionId) {
        return sessionArcs.getOrDefault(sessionId, Collections.emptyList())
                .stream()
                .filter(a -> a.getStatus() == StoryArc.ArcStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    // --- NPC Management ---

    /**
     * Create an NPC character for a session.
     */
    public NpcCharacter createNpc(String sessionId, String name, String role,
                                  String personality, NpcCharacter.NpcAlignment alignment) {
        GameMasterSettings gmSettings = getSettings(sessionId);
        String npcId = "npc_" + sessionId + "_" + System.currentTimeMillis();

        String backstory = generateBackstory(gmSettings.getUniverse(), gmSettings.getStyle(), role);
        List<String> dialogueLines = generateDialogueLines(gmSettings.getUniverse(),
                gmSettings.getStyle(), personality, alignment);

        NpcCharacter npc = new NpcCharacter(
                npcId, name, role, personality, backstory,
                dialogueLines, alignment, gmSettings.getUniverse(), null);

        sessionNpcs.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(npc);
        log.info("Created NPC '{}' for session {}", name, sessionId);
        return npc;
    }

    /**
     * Get all NPCs for a session.
     */
    public List<NpcCharacter> getNpcs(String sessionId) {
        return sessionNpcs.getOrDefault(sessionId, Collections.emptyList());
    }

    /**
     * Get a random dialogue line from an NPC.
     */
    public String getNpcDialogue(String sessionId, String npcId) {
        List<NpcCharacter> npcs = sessionNpcs.get(sessionId);
        if (npcs == null) return null;

        NpcCharacter npc = npcs.stream()
                .filter(n -> n.getId().equals(npcId))
                .findFirst()
                .orElse(null);

        if (npc == null || npc.getDialogueLines().isEmpty()) return null;
        return npc.getDialogueLines().get(random.nextInt(npc.getDialogueLines().size()));
    }

    // --- Quest Management ---

    /**
     * Generate a new quest for a session.
     */
    public Quest generateQuest(String sessionId, String title, String description,
                              Quest.QuestDifficulty difficulty) {
        GameMasterSettings gmSettings = getSettings(sessionId);
        List<Quest> quests = sessionQuests.computeIfAbsent(sessionId, k -> new ArrayList<>());

        if (quests.stream().filter(q -> q.getStatus() == Quest.QuestStatus.AVAILABLE)
                .count() >= gmSettings.getMaxActiveQuests()) {
            log.warn("Max active quests reached for session {}", sessionId);
            return null;
        }

        String questId = "quest_" + sessionId + "_" + System.currentTimeMillis();
        List<Quest.QuestObjective> objectives = generateObjectives(gmSettings.getUniverse(),
                gmSettings.getStyle(), difficulty);
        List<Quest.QuestReward> rewards = generateRewards(gmSettings.getUniverse(), difficulty);

        Quest quest = new Quest(
                questId, title, description, sessionId,
                gmSettings.getUniverse(), objectives, rewards,
                difficulty, Quest.QuestStatus.AVAILABLE,
                difficulty.getLevel() * 2);

        quests.add(quest);
        log.info("Generated quest '{}' for session {}", title, sessionId);
        return quest;
    }

    /**
     * Accept a quest.
     */
    public Quest acceptQuest(String sessionId, String questId) {
        List<Quest> quests = sessionQuests.get(sessionId);
        if (quests == null) return null;

        Quest quest = quests.stream()
                .filter(q -> q.getId().equals(questId))
                .findFirst()
                .orElse(null);

        if (quest != null && quest.getStatus() == Quest.QuestStatus.AVAILABLE) {
            log.info("Quest '{}' accepted for session {}", questId, sessionId);
        }
        return quest;
    }

    /**
     * Complete a quest.
     */
    public Quest completeQuest(String sessionId, String questId) {
        List<Quest> quests = sessionQuests.get(sessionId);
        if (quests == null) return null;

        Quest quest = quests.stream()
                .filter(q -> q.getId().equals(questId))
                .findFirst()
                .orElse(null);

        if (quest != null) {
            log.info("Quest '{}' completed for session {}", questId, sessionId);
        }
        return quest;
    }

    /**
     * Get all quests for a session.
     */
    public List<Quest> getQuests(String sessionId) {
        return sessionQuests.getOrDefault(sessionId, Collections.emptyList());
    }

    /**
     * Get available quests for a session.
     */
    public List<Quest> getAvailableQuests(String sessionId) {
        return sessionQuests.getOrDefault(sessionId, Collections.emptyList())
                .stream()
                .filter(q -> q.getStatus() == Quest.QuestStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    // --- Event History ---

    /**
     * Get event history for a session.
     */
    public List<NarrativeEvent> getEventHistory(String sessionId) {
        return eventHistory.getOrDefault(sessionId, Collections.emptyList());
    }

    /**
     * Get recent events for a session.
     */
    public List<NarrativeEvent> getRecentEvents(String sessionId, int limit) {
        List<NarrativeEvent> events = eventHistory.getOrDefault(sessionId, Collections.emptyList());
        if (events.size() <= limit) return events;
        return events.subList(events.size() - limit, events.size());
    }

    /**
     * Get events by type for a session.
     */
    public List<NarrativeEvent> getEventsByType(String sessionId, NarrativeType type) {
        return eventHistory.getOrDefault(sessionId, Collections.emptyList())
                .stream()
                .filter(e -> e.getType() == type)
                .collect(Collectors.toList());
    }

    // --- Statistics ---

    /**
     * Get statistics for a session.
     */
    public Map<String, Object> getStatistics(String sessionId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("narratives_generated", sessionStats.getOrDefault(sessionId, 0));
        stats.put("active_arcs", getActiveArcs(sessionId).size());
        stats.put("total_arcs", getArcs(sessionId).size());
        stats.put("npcs", getNpcs(sessionId).size());
        stats.put("quests", getQuests(sessionId).size());
        stats.put("available_quests", getAvailableQuests(sessionId).size());
        stats.put("event_history_size", getEventHistory(sessionId).size());
        return stats;
    }

    /**
     * Get all registered session IDs.
     */
    public List<String> getAllSessions() {
        return new ArrayList<>(settings.keySet());
    }

    // --- Clear ---

    /**
     * Clear all data for a session.
     */
    public void clearSession(String sessionId) {
        settings.remove(sessionId);
        sessionArcs.remove(sessionId);
        sessionNpcs.remove(sessionId);
        sessionQuests.remove(sessionId);
        eventHistory.remove(sessionId);
        sessionStats.remove(sessionId);
        log.info("Cleared all Game Master data for session {}", sessionId);
    }

    /**
     * Clear all data.
     */
    public void clearAll() {
        settings.clear();
        sessionArcs.clear();
        sessionNpcs.clear();
        sessionQuests.clear();
        eventHistory.clear();
        sessionStats.clear();
    }

    // --- Enum Lists ---

    /**
     * Get all narrative types.
     */
    public List<Map<String, String>> getAllNarrativeTypes() {
        List<Map<String, String>> types = new ArrayList<>();
        for (NarrativeType type : NarrativeType.values()) {
            types.add(Map.of("name", type.name(), "label", type.getName(), "description", type.getDescription()));
        }
        return types;
    }

    /**
     * Get all narrative styles.
     */
    public List<Map<String, Object>> getAllNarrativeStyles() {
        List<Map<String, Object>> styles = new ArrayList<>();
        for (NarrativeStyle style : NarrativeStyle.values()) {
            styles.add(Map.of(
                    "name", style.name(),
                    "label", style.getName(),
                    "description", style.getDescription(),
                    "intensity", style.getIntensity()
            ));
        }
        return styles;
    }

    /**
     * Get all game universes.
     */
    public List<Map<String, String>> getAllGameUniverses() {
        List<Map<String, String>> universes = new ArrayList<>();
        for (GameUniverse universe : GameUniverse.values()) {
            universes.add(Map.of("name", universe.name(), "label", universe.getName(), "description", universe.getDescription()));
        }
        return universes;
    }

    // --- Private Template Methods ---

    private String[] getBeginnings(GameUniverse universe, NarrativeStyle style) {
        switch (universe) {
            case TRAVELLER:
                return new String[]{
                    "In the vast reaches of the Known Space, a signal cuts through the void.",
                    "The subsector has been quiet for too long, and silence in space is never peaceful.",
                    "A Jump drive whines as the ship emerges from the void, stars blurring into focus."
                };
            case STAR_TREK:
                return new String[]{
                    "The stars stretch endlessly before the bridge, a sea of infinite possibility.",
                    "A distress call echoes through subspace, breaking the calm of routine patrol.",
                    "The ship shudders as it drops out of warp, sensors painting an unfamiliar system."
                };
            case STAR_WARS:
                return new String[]{
                    "The Force stirs as darkness gathers across the galaxy.",
                    "A lone figure stands on the observation deck, watching the twin suns set.",
                    "Blaster fire illuminates the corridor as the battle for freedom begins anew."
                };
            case DND_5E:
                return new String[]{
                    "The tavern is warm and loud, but the adventure awaits beyond the door.",
                    "Ancient runes glow faintly on the dungeon wall, whispering of forgotten power.",
                    "The party gathers at the crossroads, each with a different path in mind."
                };
            default:
                return new String[]{
                    "The story begins where all great tales do ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â with a choice.",
                    "Something stirs in the darkness, and the fate of all hangs in the balance.",
                    "The stage is set, the players in position, and the game begins."
                };
        }
    }

    private String[] getMiddles(GameUniverse universe, NarrativeStyle style) {
        return new String[]{
            "But the path forward is fraught with danger, and allies become uncertain.",
            "As the pieces fall into place, a greater threat reveals itself.",
            "The journey takes an unexpected turn, testing the resolve of all involved.",
            "Alliances shift and betrayals surface as the truth comes to light."
        };
    }

    private String[] getEndings(GameUniverse universe, NarrativeStyle style) {
        switch (style) {
            case DRAMATIC:
                return new String[]{
                    "And so the chapter closes, but the saga continues.",
                    "Victory is won, but at what cost? The scars will remain.",
                    "In the end, it was never about the destination ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â it was about who you became along the way."
                };
            case HUMOROUS:
                return new String[]{
                    "And they all lived happily ever after, or at least until the next adventure.",
                    "The hero saved the day, mostly by accident, as usual.",
                    "Everyone survived, which is more than can be said for the hero's hair."
                };
            default:
                return new String[]{
                    "The tale ends here, but legends never truly die.",
                    "What was once lost is found, and what was broken is made whole.",
                    "The stars bear witness to deeds that will echo through the ages."
                };
        }
    }

    private String[] getNpcGreetings(GameUniverse universe, NarrativeStyle style) {
        switch (universe) {
            case TRAVELLER:
                return new String[]{
                    "\"Ah, another spacer. What brings you to this corner of the subsector?\"",
                    "\"I've been expecting you. The stars have a way of guiding the right people.\"",
                    "\"Care for a drink? It's the least I can do after the journey you've made.\""
                };
            case STAR_TREK:
                return new String[]{
                    "\"Welcome aboard. I'm glad you could make it ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â we could use someone with your skills.\"",
                    "\"Fascinating. Your readings are unlike anything I've encountered before.\"",
                    "\"The Captain has been waiting for you. This mission requires your unique perspective.\""
                };
            default:
                return new String[]{
                    "\"So, you've finally arrived. I wondered when you'd show up.\"",
                    "\"I've been watching you. You have potential ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â raw, unrefined, but there.\"",
                    "\"Let's cut to the chase. Time is short, and the stakes are high.\""
                };
        }
    }

    private String[] getNpcResponses(GameUniverse universe, NarrativeStyle style) {
        return new String[]{
            "\"The situation is more complex than it appears. Trust no one completely.\"",
            "\"I've seen this before. The pattern repeats, but this time, it's different.\"",
            "\"You need to understand ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the consequences of this decision will ripple for generations.\"",
            "\"There's a path forward, but it won't be easy. Are you ready?\""
        };
    }

    private String[] getEventDescriptors(GameUniverse universe, NarrativeStyle style) {
        switch (style) {
            case DRAMATIC:
                return new String[]{
                    "The moment of truth arrives",
                    "Chaos erupts without warning",
                    "A turning point that will change everything"
                };
            case MYSTERIOUS:
                return new String[]{
                    "Shadows lengthen as secrets surface",
                    "Something lurks beneath the surface",
                    "The truth is closer than anyone realizes"
                };
            default:
                return new String[]{
                    "An event unfolds across the sector",
                    "The situation escalates rapidly",
                    "A critical moment in the ongoing saga"
                };
        }
    }

    private String[] getEventActions(GameUniverse universe, NarrativeStyle style) {
        return new String[]{
            "Fleets maneuver for position as tensions mount.",
            "Allies converge on the objective, each with their own agenda.",
            "The battle lines are drawn, and there is no turning back.",
            "Diplomatic channels crackle with urgent communications."
        };
    }

    private String[] getQuestHooks(GameUniverse universe, NarrativeStyle style) {
        switch (universe) {
            case TRAVELLER:
                return new String[]{
                    "A merchant offers a lucrative contract, but the destination is in hostile territory.",
                    "Rumors speak of an ancient ruin hidden in an uncharted system.",
                    "A distress signal leads to a world where nothing is as it seems."
                };
            case DND_5E:
                return new String[]{
                    "The local lord offers a reward for clearing the dungeon that plagues the village.",
                    "A mysterious stranger seeks a party brave enough to retrieve a stolen artifact.",
                    "The temple has fallen silent, and the priests have vanished without a trace."
                };
            default:
                return new String[]{
                    "A critical mission requires your unique skills and determination.",
                    "The fate of the sector hangs in the balance, and only you can tip the scales.",
                    "An old enemy resurfaces with a new threat that demands immediate action."
                };
        }
    }

    private String[] getQuestObjectives(GameUniverse universe, NarrativeStyle style) {
        return new String[]{
            "Your objective: complete the mission before time runs out.",
            "The goal is clear, but the path to victory is anything but.",
            "Success requires courage, cunning, and perhaps a little luck.",
            "The reward is great, but so is the danger. Proceed with caution."
        };
    }

    private String[] getAtmospheres(GameUniverse universe, NarrativeStyle style) {
        switch (style) {
            case NOIR:
                return new String[]{
                    "Rain slicks the streets as neon signs flicker in the gloom.",
                    "The city breathes with a dark energy, shadows hiding secrets.",
                    "Fog rolls in from the harbor, carrying whispers of danger."
                };
            case EPIC:
                return new String[]{
                    "The stars blaze overhead as the great hall echoes with anticipation.",
                    "Mountains rise like ancient sentinels, guarding secrets of the old world.",
                    "The battlefield stretches to the horizon, a canvas of glory and despair."
                };
            default:
                return new String[]{
                    "The air crackles with tension as the scene unfolds.",
                    "Light filters through the viewport, casting long shadows across the room.",
                    "Silence hangs heavy, broken only by the hum of machinery."
                };
        }
    }

    private String[] getSceneDetails(GameUniverse universe, NarrativeStyle style) {
        return new String[]{
            "Every detail tells a story, from the worn floorboards to the faded portraits.",
            "The room is filled with the scent of old books and newer dangers.",
            "Instruments of war and tools of peace share the same space, an uneasy truce.",
            "The view from the window reveals a world on the brink of change."
        };
    }

    private String[] getCliffhangers(GameUniverse universe, NarrativeStyle style) {
        return new String[]{
            "But as the dust settles, a new threat emerges from the shadows...",
            "Just when victory seems certain, an unexpected betrayal changes everything...",
            "The door creaks open, revealing a sight that defies all expectation...",
            "The signal fades, leaving only one question: who sent it, and why now?"
        };
    }

    private String[] getCharacterIntros(GameUniverse universe, NarrativeStyle style) {
        return new String[]{
            "A figure emerges from the crowd, eyes sharp and purpose clear.",
            "They say she was once the best in her field, until the incident that changed everything.",
            "He walks with the confidence of someone who has seen too much and survived.",
            "Her reputation precedes her ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â part legend, part myth, all danger."
        };
    }

    private String[] getTwists(GameUniverse universe, NarrativeStyle style) {
        return new String[]{
            "But the ally you trusted most has been playing a different game all along.",
            "The enemy you sought was never the real threat ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â it was the one standing beside you.",
            "What seemed like a simple mission reveals layers of deception at every turn.",
            "The truth, when it finally surfaces, is more shocking than anyone imagined."
        };
    }

    private String[] getEpilogues(GameUniverse universe, NarrativeStyle style) {
        return new String[]{
            "The stars continue their eternal dance, indifferent to the dramas played out below.",
            "Peace returns, but it is a fragile thing ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â maintained by those who remember the cost.",
            "The legend grows with each telling, until fact and fiction become one.",
            "And so the chapter closes, but the story of the sector is far from over."
        };
    }

    private String[] getFlashbacks(GameUniverse universe, NarrativeStyle style) {
        return new String[]{
            "Years ago, before the war, the sector was a different place entirely.",
            "The memory surfaces unbidden: the day everything changed.",
            "In the beginning, no one could have predicted what was to come.",
            "The old records tell a different story ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â one of hope, before the darkness fell."
        };
    }

    private String generateBackstory(GameUniverse universe, NarrativeStyle style, String role) {
        String[] backstories = {
            "Born in the outer rim, they learned early that survival depends on wit and courage.",
            "Once a celebrated hero, they carry the weight of past failures.",
            "Raised among the stars, they know the void better than most.",
            "A former soldier, they've seen the worst of war and the best of humanity.",
            "Their past is a mystery, even to themselves ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â fragments of memory guide their path."
        };
        return backstories[random.nextInt(backstories.length)];
    }

    private List<String> generateDialogueLines(GameUniverse universe, NarrativeStyle style,
                                               String personality, NpcCharacter.NpcAlignment alignment) {
        List<String> lines = new ArrayList<>();
        lines.add("\"I've seen things you wouldn't believe.\"");
        lines.add("\"The universe is vast, but we are not alone in it.\"");
        lines.add("\"Trust is earned, not given. Remember that.\"");
        lines.add("\"Every choice has consequences. Choose wisely.\"");
        lines.add("\"The stars don't care about our plans, but we make them anyway.\"");
        return lines;
    }

    private List<Quest.QuestObjective> generateObjectives(GameUniverse universe,
                                                          NarrativeStyle style,
                                                          Quest.QuestDifficulty difficulty) {
        List<Quest.QuestObjective> objectives = new ArrayList<>();
        int count = Math.max(1, difficulty.getLevel() / 2);

        String[] objectiveTemplates = {
            "Reach the destination before the enemy does",
            "Gather intelligence on the target",
            "Secure the strategic position",
            "Defeat the opposing force",
            "Rescue the hostages",
            "Retrieve the artifact",
            "Establish a foothold in the sector",
            "Negotiate a peace treaty"
        };

        for (int i = 0; i < count; i++) {
            objectives.add(new Quest.QuestObjective(
                    "obj_" + i,
                    objectiveTemplates[random.nextInt(objectiveTemplates.length)]
            ));
        }
        return objectives;
    }

    private List<Quest.QuestReward> generateRewards(GameUniverse universe,
                                                     Quest.QuestDifficulty difficulty) {
        List<Quest.QuestReward> rewards = new ArrayList<>();
        int baseReward = difficulty.getLevel() * 100;

        rewards.add(new Quest.QuestReward("credits", "Credits for your service", baseReward));
        rewards.add(new Quest.QuestReward("reputation", "Reputation points", difficulty.getLevel() * 10));

        if (difficulty.getLevel() >= 4) {
            rewards.add(new Quest.QuestReward("equipment", "Rare equipment", 1));
        }
        return rewards;
    }

    private List<String> extractKeyEvents(String content) {
        List<String> events = new ArrayList<>();
        if (content == null || content.isEmpty()) return events;

        String[] sentences = content.split("\\. ");
        for (String sentence : sentences) {
            if (sentence.length() > 20 && sentence.length() < 200) {
                events.add(sentence.trim());
            }
        }
        return events;
    }

    private void trimHistory(String sessionId, int maxEvents) {
        List<NarrativeEvent> events = eventHistory.get(sessionId);
        if (events != null && events.size() > maxEvents) {
            eventHistory.put(sessionId, events.subList(events.size() - maxEvents, events.size()));
        }
    }
}
