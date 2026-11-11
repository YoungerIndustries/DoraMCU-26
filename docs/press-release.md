# FOR IMMEDIATE RELEASE

## DoraMCU Launches: The First Unified Platform for AI-Powered Tabletop RPG Simulation and Strategy Gaming

### Eight game universes. One engine. AI commanders that learn, adapt, and evolve — powered by deep learning and distributed neuron coordination.

**November 11, 2026** — Younger Industries today announced the release of **DoraMCU**, a multi-module Spring Boot platform that unifies eight distinct tabletop role-playing game and strategy systems under a single engine with deep-learning AI commanders, distributed neuron coordination, and cross-platform clients.

---

### A Unified Multi-Game Engine

DoraMCU natively supports eight game systems, each with full rules implementation:

- **Traveller RPG** — Complete classic Traveller implementation including character creation with lifepath careers, ship design (High Guard), naval and air combat, trade simulation, psionics, survival, mercenary and navy tours of duty, smuggling operations, and Universal World Profiles (UWP).
- **Star Trek 4X** — Full 4X strategy game with diplomacy, treaties, fleet warfare, ground combat, colony management, starbase construction, technology research, trade routes, first contact, scientific missions, and territory expansion.
- **Babylon 5** — B5 universe simulation with character creation, combat, diplomacy, economy, exploration, ship design, storylines with chapters, psionic abilities, alien races (Minbari, Centauri, Narn, Vorlons, Shadows), and wealth systems.
- **Earth 2026** — Near-future sci-fi RPG with countries, regions, locations, structures, tech levels, parties, contracts, scenarios, economy simulation, and real-time WebSocket chat.
- **Dungeons & Dragons 5E** — Full D&D 5E rules including character creation with feats and skills, combat with conditions, spellcasting with spell slots, inventory management, crafting system, exhaustion, horror mechanics (dark gifts, fear/dread), psionic powers, growing magic items, and heroic inspiration.
- **Captain's Log (Star Trek RPG)** — Narrative-driven Star Trek RPG with lifepath character creation, talent systems, crew management, starship creation with ship talents, mission generation with AI, career progression, downtime activities, injury system, dice rolls, difficulty scaling, narrative beats, and probability matrices.
- **Star Fleet Battles (SFB)** — Tactical starship combat with energy allocation, damage allocation, impulse movement, shield systems, weapon banks, crew stations, special systems, campaign engine, and AI opponents.
- **007 Spy & Espionage** — Covert operations game with spy networks, safehouses, infiltration, sabotage, surveillance, counter-intelligence, assassination, recruitment, diplomacy, treaties, territory management, and ground combat.

All eight systems share a common procedural foundation: the **Hex Galaxy Engine**, a 218+ class system providing procedurally generated hex-based galaxy simulation with themed templates, 3D hex mapping, empire management, resource economy, ship and station design, combat systems, trade and transport, dungeon generation, planet simulation, and RPG quest integration.

---

### AI That Learns and Evolves

DoraMCU's AI system represents a significant advancement in game AI:

- **Personality-Driven Decision Making** — Each AI-controlled empire has a unique personality derived from government type and ethos, with traits including aggression, expansion drive, economic focus, scientific focus, and risk tolerance. These personalities evolve based on experience and game events.
- **Deep Learning Integration** — Production-grade deep learning via Deeplearning4j (DL4J) provides neural network strategy prediction, hybrid AI decisions that blend neural network probabilities with heuristic scores, pluggable feature extraction, reinforcement learning, and persistent learning services.
- **Strategic and Tactical Layers** — Separate strategic layer (expansion, economy, research priorities) and tactical layer (combat, movement, scouting) with personality-weighted action evaluation.
- **Player Behavior Analysis** — The AI monitors and categorizes player actions, can learn from and mimic human player behavior, and supports hierarchical AI delegation with configurable areas.

---

### Distributed AI at Scale

DoraMCU introduces the **Neuron Federation**, a distributed AI coordination system for horizontal AI scaling:

- **Neuron Registry** — Dynamic registration and discovery of AI neuron nodes.
- **Consensus Protocol** — Distributed consensus for AI decisions across the federation.
- **Model Synchronization** — Automatic model version tracking and synchronization between neurons.
- **Experience Sharing** — Batch experience collection and streaming between neurons for collective learning.
- **Change Data Capture (CDC)** — Real-time CDC via WebSocket for state synchronization.
- **Kafka Integration** — Optional Kafka-backed event streaming for high-throughput coordination.
- **Leader Election** — Automatic leader election for federation coordination.
- **Health Monitoring** — Heartbeat-based health tracking and peer monitoring.

For edge deployment, the **dora-neuron** module provides ONNX-based inference with model quantization, cryptographic model signature validation, peer-to-peer mesh networking, and Unity client integration — enabling AI inference on resource-constrained devices.

---

### Multi-Provider AI Services

The **dora-ai** module provides unified access to multiple AI providers (DeepSeek, Google Gemini, and local Ollama) for conversational AI chat, source code analysis, AI-assisted code generation, speech transcription, multi-language translation, and integration with OpenHands AI coding agent — all with built-in retry logic, response filtering, and provider health monitoring.

---

### Cross-Platform Access

DoraMCU is accessible through three client platforms:

- **Unity 3D Client** — Full 3D space strategy game client with client-side AI inference, local database with CDC sync, procedural planet/ship/station rendering, interest management, and server communication.
- **Electron Desktop Client** — Cross-platform desktop wrapper (Windows/macOS/Linux) with game launcher that auto-launches the Unity client.
- **Progressive Web App (PWA)** — Browser-based PWA with service worker for offline support and installable app experience.

---

### Production-Grade Infrastructure

DoraMCU is built for production deployment from day one:

- **Java 27** and **Spring Boot 4.1.2** foundation
- **MySQL 8.x** for production, **H2 in-memory** for development
- **Docker** multi-stage builds and **Docker Compose** one-command deployment
- **Kubernetes** production-ready manifests with Kustomize overlays
- **Spring Security** with JWT-based authentication and authorization
- **Prometheus** metrics, **Grafana** dashboards, and **Spring Boot Actuator** health endpoints
- **Logstash-encoded JSON logging** for ELK/Grafana Loki integration
- **Flyway** database migration management
- **Caffeine** distributed caching
- **SpringDoc OpenAPI** for API documentation

---

### About the Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Java 27 |
| Framework | Spring Boot 4.1.2 |
| Deep Learning | Deeplearning4j (DL4J) |
| Edge Inference | ONNX Runtime |
| Messaging | Apache Kafka |
| Database | MySQL 8.x / H2 |
| Security | Spring Security + JWT |
| Metrics | Micrometer + Prometheus |
| Container | Docker + Docker Compose |
| Orchestration | Kubernetes + Kustomize |
| Desktop Client | Electron |
| Game Client | Unity 2022.3 LTS |
| AI Providers | DeepSeek, Google Gemini, Ollama |

---

### Key Differentiators

1. **Unified Multi-Game Engine** — Eight distinct RPG/strategy game systems sharing a common hex galaxy foundation.
2. **AI That Learns** — Deep learning AI commanders that improve through experience and share knowledge across a distributed neuron federation.
3. **Personality-Driven AI** — Each AI empire has a unique, evolving personality that shapes its strategic and tactical decisions.
4. **Edge-Ready AI** — ONNX-based edge inference with model quantization for deployment on resource-constrained devices.
5. **Distributed by Design** — Neuron federation with consensus, CDC, and mesh networking enables horizontal AI scaling.
6. **Cross-Platform** — Play via Unity 3D, Electron desktop, or any browser via PWA.
7. **Production-Grade** — Full observability, security, container deployment, and Kubernetes support out of the box.

---

### Availability

DoraMCU is available now on GitHub at [github.com/YoungerIndustries/DoraMCU-26](https://github.com/YoungerIndustries/DoraMCU-26).

---

**About Younger Industries**

Younger Industries develops advanced simulation platforms that combine tabletop gaming traditions with cutting-edge AI technology.

**Media Contact**

Younger Industries
Email: media@youngerindustries.com
[github.com/YoungerIndustries](https://github.com/YoungerIndustries)

###
