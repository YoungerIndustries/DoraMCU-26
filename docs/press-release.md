# FOR IMMEDIATE RELEASE

## DoraMCU Reaches Feature-Complete Milestone: A Unified Multi-Universe Gaming Platform with AI-Powered Gameplay

**November 14, 2026** — Younger Industries is proud to announce that **DoraMCU**, its flagship multi-game simulation platform, has achieved full feature completion. With **50 completed features**, **12,607+ passing tests**, and **12 integrated game universes**, DoraMCU stands as one of the most comprehensive tabletop role-playing game (TTRPG) and strategy game platforms ever built.

---

### A Platform That Unifies Gaming Universes

DoraMCU is a Spring Boot-based application that brings together an unprecedented range of game systems under a single, cohesive architecture. Players can seamlessly experience:

- **Traveller** — Classic sci-fi RPG with full economic simulation, psionics, smuggling, and alien races
- **Star Trek 4X** — Explore, expand, exploit, and exterminate in the Star Trek universe
- **Star Fleet Battles** — Tactical fleet combat with campaign management, AI strategy, and terrain-based encounters
- **Babylon 5** — Canonical galaxy integration with the beloved sci-fi universe
- **Dungeons & Dragons 5E** — Full D&D 5th Edition rules and character system
- **Star Wars** — 23 REST endpoints powering the galaxy far, far away
- **Starship Troopers** — Mobile infantry combat with 22 dedicated endpoints
- **MechWarrior** — Mech-on-mech combat with AI pilots and mission systems
- **007 (Zero Zero Seven)** — Spy networks, covert operations, and diplomacy
- **Third Reich** — Ground battle engine with 3D battlefield simulation
- **FPS Module** — First-person shooter game server with matchmaking
- **Earth 2026** — Alternate history sci-fi RPG

Cross-game characters allow players to carry a unified identity across all 12 universes, creating a truly interconnected gaming experience.

---

### AI at the Core

What sets DoraMCU apart is its deep integration of artificial intelligence throughout the platform:

- **DoraAI Platform** — AI-powered game mastering with narrative generation, story arcs, NPC creation, and quest design across 10 narrative types and 8 styles
- **Neuron Federation** — Distributed AI coordination with change data capture (CDC) and snapshot support, enabling scalable AI inference across edge nodes
- **Deep Learning** — Built on Deeplearning4j (DL4j), the platform supports both local and REST-based deep learning modes for AI commanders with configurable difficulty levels
- **ONNX Runtime Web** — Browser-based AI inference for real-time gameplay decisions

---

### Built for Scale

DoraMCU is engineered for production-grade deployment:

- **Java 27** with **Spring Boot 4.1.2** — Cutting-edge runtime and framework
- **12,462+ Java tests** and **145 Electron desktop tests** — Zero excluded tests, full coverage
- **512+ classes** and **216+ test classes** — Well-architected, maintainable codebase
- **Docker Compose** — Six-service stack including MySQL 8.4, Redis, OpenHands AI, and Python DL service
- **Kubernetes** — Production-ready manifests with Kustomize for dev and prod overlays
- **Prometheus & Grafana** — Full observability with health endpoints and metrics
- **OpenAPI 3.0** — Swagger UI with 10 API groups covering 100+ endpoints

---

### Multi-Platform Client Ecosystem

DoraMCU reaches players wherever they are:

- **Electron Desktop Client** — Full-featured desktop application with modular architecture
- **Progressive Web App (PWA)** — Browser-based game node with local game logic and AI
- **Browser Extension** — Manifest V3 extension for offline AI inference and cached game state
- **Unity 3D Client** — 3D game client for immersive gameplay
- **VR Galaxy Viewer** — 3D galaxy data export for WebGL/Three.js visualization

The player gateway architecture, powered by **dora-neuron**, serves as a single entry point that detects the player's platform and distributes the appropriate client bundle, with real-time state synchronization via WebSocket.

---

### Community-First Features

DoraMCU is designed for community-driven growth:

- **Plugin Marketplace** — Community content distribution with search, ratings, and downloads
- **Community Server Hosting** — Server registration, content sharing, reviews, and templates
- **Modding Support** — User-created game modes, rulesets, and content packs
- **Voice Chat** — WebRTC-based real-time voice communication
- **Spectator Mode** — Read-only observer access for streaming
- **TTS Narration** — Text-to-speech game event narration
- **Game Replay** — Full action logging with query, statistics, and export

---

### Technical Highlights

- **Hex Galaxy System** — Procedurally generated galaxies with themed templates for Traveller, Star Trek, and Babylon 5
- **Real-Time Communication** — WebSocket and Server-Sent Events (SSE) for live game sessions
- **Game State Persistence** — Sessions survive server restarts via JSON serialization with auto-restore
- **Save/Load System** — Export and import game sessions as JSON
- **Unified Credit System** — Currency bridging Traveller credits and DoraMCU resources
- **Memory Guard** — OOM crash prevention for deep learning workloads
- **Flyway Migrations** — Database versioning with MySQL and H2 support

---

### About Younger Industries

Younger Industries is the development team behind DoraMCU, a next-generation platform that reimagines how tabletop and strategy games are played, shared, and experienced. The project is hosted on GitHub at `github.com/YoungerIndustries/DoraMCU-26`.

---

### Looking Ahead

With all 50 roadmap features complete, all backlog items delivered, and all long-term vision goals achieved, DoraMCU enters a new phase of stability and community growth. The platform is ready for players, game masters, and modders to explore, create, and share their own gaming experiences across 12 iconic universes.

---

**For more information, documentation, and source code, visit:**
https://github.com/YoungerIndustries/DoraMCU-26

**Contact:**
Younger Industries
DoraMCU Development Team

---

*This press release was generated on November 14, 2026, based on the DoraMCU v0.3.0 project documentation.*
