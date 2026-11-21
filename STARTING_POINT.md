# DoraMCU — Starting Point Document

> **Purpose:** This document provides a complete overview of the DoraMCU project for any new developer, agent, or contributor. Read this first before making any changes.

---

## 0. Agent Behavior (READ FIRST)

**This section applies to all AI agents working on this project.**

### Living Memory

- **Read MEMORY.md first** — it is the persistent log of what previous agents did and what to do next. Update it at the end of every session.
- **This document (STARTING_POINT.md)** is the static "read me first" with project overview, agent behavior rules, and deployment workflow.

### No-Questions Policy

- **Do NOT ask the user questions** unless the decision involves a major architectural change or new feature design.
- **Assume yes** for routine implementation decisions, deployment steps, and configuration changes.
- **Execute immediately** based on the to-do list in this document.
- **Only ask** when there is a genuine ambiguity that could lead to incorrect implementation.

### Deployment Workflow

**All changes to the Pi server MUST flow through the PC server, never directly:**

1. **PC Server** → Make code changes on Windows, build the JAR locally
2. **Pi Server** → Deploy via file server (port 8888) + tunnel (port 9000) for remote commands
3. **Back to PC Server** → Verify endpoints, update documentation, commit changes

**Why this workflow:**
- The PC server is the source of truth for all code and builds
- The Pi server receives deployments, not direct code edits
- All verification and documentation happens on the PC

**Key files for deployment:**
- Deployment/RaspberryPi/file-server.js — Serves JAR and config files on port 8888
- Deployment/RaspberryPi/tunnel-server.js — Remote command execution on port 9000
- Deployment/RaspberryPi/config/doramcu-tunnel.service — Systemd service for tunnel
- Deployment/RaspberryPi/update-pi.sh — Full deployment script for the Pi

### Pi Server Access

- **Pi IP:** 192.168.50.147
- **Pi server port:** 8080 (Spring Boot, PostgreSQL)
- **Tunnel port:** 9000 (Node.js, remote exec via POST /exec) — **RELIABLE, use this for all Pi commands**
- **File server:** Windows port 8888 (serves deployment files)
- **SSH key:** `~/.ssh/doramcu-pi` (ED25519, user `doramcu`) — key deployed during setup
- **sudo:** NOPASSWD configured for `doramcu` user via `/etc/sudoers.d/doramcu`
- **WiFi:** Primary connection (wlan0, gateway 192.168.50.1, ~8ms latency)
- **Bluetooth PAN:** Fallback (pan0 bridge, 172.16.0.1, bt-network NAP mode)
- **SSH tunnel:** `doramcu-tunnel.bat` forwards Pi ports to localhost (WiFi-based, faster than Bluetooth)

### Reliable Pi Command Execution

**Always use the HTTP tunnel (port 9000) for remote commands — it never hangs:**

```powershell
# PowerShell example
$body = '{"command":"your-command-here","timeout":30000}'
Invoke-WebRequest -Uri "http://192.168.50.147:9000/exec" -Method POST `
  -Body $body -ContentType "application/json" -UseBasicParsing | Select-Object -ExpandProperty Content
```

**PowerShell module for convenience:** Import `pi-tunnel.psm1`, then use `Pi-Run "command"`.

**Never use SSH for automated commands** — SSH tunnels hang due to keepalive issues. The HTTP tunnel is the only reliable method.

---

## 1. Project Overview

**DoraMCU** (Dora Multi-Universe Game Master) is a **multi-universe 4X strategy game platform** that supports multiple sci-fi and fantasy universes (Star Trek, Star Wars, Babylon 5, D&D, Traveller, Warhammer 40k, and more) through a plugin-based architecture.

The project consists of two main components:
- **Java Spring Boot Server** — REST API, game logic, database, peer-to-peer networking
- **Unity Client** — 3D hex-based game client with multiple universe support

---

## 2. Two Project Locations

| Component | Location | Description |
|-----------|----------|-------------|
| Server | Deployment/RaspberryPi/server-build/ | Spring Boot Java server, PostgreSQL/H2 |
| Unity Client | Library/, Assets/, Packages/ | Unity 3D game client |

---

## 3. Technology Stack

### Server
- **Java 27** with **Spring Boot 4.1.2**
- **PostgreSQL** (Pi) / **H2 in-memory** (Windows dev)
- **Maven** for builds — installed at `%LOCALAPPDATA%\Apache\apache-maven-3.10.0\bin\mvn.cmd` (Windows)
- **Node.js** for tunnel server and file server

### Client
- **Unity 2022.3 LTS** with C#
- **Godot 4.7.2** alternative client

### Infrastructure
- **Raspberry Pi 5** — Production server
- **Windows PC** — Development & Brain Node
- **systemd** — Service management on Pi

---
## 4. Reference Documentation

Static reference material has been moved to the docs/ directory for faster agent context loading:

| Document | Description |
|----------|-------------|
| [server-structure.md](docs/server-structure.md) | Server project structure (48 KB) |
| [unity-structure.md](docs/unity-structure.md) | Unity client structure (47 KB) |
| [architecture.md](docs/architecture.md) | Architecture overview (9 KB) |
| [api-endpoints.md](docs/api-endpoints.md) | API endpoints catalog + deep dives (60 KB) |
| [data-models.md](docs/data-models.md) | Key data models (12 KB) |
| [data-architecture.md](docs/data-architecture.md) | In-memory vs persisted data pattern |
| [data-flow.md](docs/data-flow.md) | Data flow diagrams |
| [testing.md](docs/testing.md) | Testing strategy |
| [security.md](docs/security.md) | Security overview |
| [universes.md](docs/universes.md) | Game universes supported |
| [integrations.md](docs/integrations.md) | Integration modules (EVE Online, etc.) |
| [operational.md](docs/operational.md) | Operational documentation |
| [build-pipeline.md](docs/build-pipeline.md) | Automated build & deploy pipeline |
| [deployment-workflow.md](docs/deployment-workflow.md) | Deployment workflow |
| [quick-reference.md](docs/quick-reference.md) | Quick reference cards |
| [version-history.md](docs/version-history.md) | Version history |
| [contact.md](docs/contact.md) | Contact & repository |

### Module Deep Dives

Individual module documentation in docs/modules/:

| [additional-modules.md](docs/modules/additional-modules.md) | additional-modules.md deep dive |
| [babylon5.md](docs/modules/babylon5.md) | babylon5.md deep dive |
| [dora-ai-openhands.md](docs/modules/dora-ai-openhands.md) | dora-ai-openhands.md deep dive |
| [dora-dl-python.md](docs/modules/dora-dl-python.md) | dora-dl-python.md deep dive |
| [dora-neuron.md](docs/modules/dora-neuron.md) | dora-neuron.md deep dive |
| [fourx.md](docs/modules/fourx.md) | fourx.md deep dive |
| [fullthrust.md](docs/modules/fullthrust.md) | fullthrust.md deep dive |
| [gamecore.md](docs/modules/gamecore.md) | gamecore.md deep dive |
| [game-messages.md](docs/modules/game-messages.md) | game-messages.md deep dive |
| [godot-client.md](docs/modules/godot-client.md) | godot-client.md deep dive |
| [lord_of_the_rings.md](docs/modules/lord_of_the_rings.md) | lord_of_the_rings.md deep dive |
| [mechwarrior.md](docs/modules/mechwarrior.md) | mechwarrior.md deep dive |
| [operational-enhancements.md](docs/modules/operational-enhancements.md) | operational-enhancements.md deep dive |
| [star_trek_4x.md](docs/modules/star_trek_4x.md) | star_trek_4x.md deep dive |
| [starshiptroopers.md](docs/modules/starshiptroopers.md) | starshiptroopers.md deep dive |
| [starwars.md](docs/modules/starwars.md) | starwars.md deep dive |
| [thirdreich.md](docs/modules/thirdreich.md) | thirdreich.md deep dive |
| [warhammer40k.md](docs/modules/warhammer40k.md) | warhammer40k.md deep dive |
| [zero_zero_seven.md](docs/modules/zero_zero_seven.md) | zero_zero_seven.md deep dive |

---

## 5. Key Configuration Files

### application.properties
- H2 in-memory database for local development
- Debug logging for com.doramcu.server package
- Server port: 8080

### application-pi.properties
- PostgreSQL database connection
- INFO logging level for production
- Server port: 8080

---

## 6. Spring Boot Configuration Profiles

- **Local dev:** Default profile, H2 in-memory DB
- **Pi production:** pi profile, PostgreSQL DB
- Switch via --spring.profiles.active=pi

---

## 7. Build and Run Instructions

### Build
```bash
cd Deployment/RaspberryPi/server-build
mvn clean package -DskipTests
```

### Run Local
```bash
java -jar target/doramcu-server.jar
```

### Run on Pi
```bash
sudo systemctl restart doramcu-server
```

---

## 8. Deployment Options

### Pi Server
- **systemd service:** doramcu-server (auto-start, auto-restart)
- **Tunnel service:** doramcu-tunnel (port 9000, remote exec)
- **Database:** PostgreSQL with doramcu user

### Windows Dev
- **Direct JAR run:** java -jar target/doramcu-server.jar
- **Database:** H2 in-memory

---

## 9. Key Documentation Files

- STARTING_POINT.md — This file (lean core)
- docs/ — Reference documentation
- docs/modules/ — Module deep dives
- unified-message-system.md — Message system documentation
- EVE_Online_Integration.md — EVE Online integration guide

---

## 10. Environment Variables

- SPRING_PROFILES_ACTIVE — Spring profile (local/pi)
- JAVA_HOME — Java installation path
- Database credentials in application-pi.properties

---

## 11. Database

- **Pi:** PostgreSQL, schema auto-created on first run
- **Windows:** H2 in-memory, fresh data on each restart
- **Celestial objects:** 17 seeded objects in PostgreSQL

---

## 12. Monitoring & Observability

- **systemd journal:** journalctl -u doramcu-server -f
- **Tunnel logs:** /tmp/tunnel.log
- **Server logs:** stdout via systemd

---

## 13. Known Build Artifacts

- target/doramcu-server.jar — Built JAR
- target/classes/ — Compiled classes

---

## 14. Quick Start Checklist for New Agents

1. Read Section 0 (Agent Behavior)
2. Understand the two project locations
3. Build the server: mvn clean package -DskipTests
4. Run locally: java -jar target/doramcu-server.jar
5. Test endpoints: curl http://localhost:8080/api/v1/nodes
6. For Pi deployment: use file server + tunnel workflow

---

## 15. Important Design Principles

- **Plugin-based architecture** — Each universe is a separate module
- **In-memory game state** — Only metadata persisted to DB
- **Unified message system** — namespace.action pattern
- **Peer-to-peer networking** — Brain Node ↔ BrainStem communication
- **Deployment via PC** — Never edit code directly on the Pi

---

## 16. Deployment Workflow

### Current Deployment Process
1. Build JAR on Windows PC
2. Start file server: node Deployment/RaspberryPi/file-server.js
3. On Pi: wget http://192.168.50.150:8888/doramcu-server.jar -O /tmp/doramcu-server.jar
4. Deploy: sudo cp /tmp/doramcu-server.jar /opt/doramcu/server/
5. Restart: sudo systemctl restart doramcu-server
6. Verify: curl http://localhost:8080/api/v1/celestial

---

### Known Issues

- **Celestial endpoint 404/500 (RESOLVED in v0.5.3):** Three root causes fixed: (1) @GetMapping("/") only matched trailing slash; (2) @Lob annotation on PostgreSQL mapped to OID type; (3) PostgreSQL permissions granted. Endpoint returns 17 celestial objects on Pi.
- **Tunnel crashes:** Restart with nohup node /tmp/tunnel-server.js > /tmp/tunnel.log 2>&1 &
- **Windows zip permissions:** Files extracted from Windows-created zips need sudo chmod -R 755 on the Pi.
- **Tunnel systemd service (RESOLVED in v0.5.3):** Service deployed and active on Pi.
- **Debug logging (RESOLVED in v0.5.4):** Downgraded from DEBUG to INFO for production.

### Current State

See [MEMORY.md](MEMORY.md) for the current state, active blockers, and next agent action. This section has been moved to the living memory document.

**Quick summary:**
- Pi Server: ✅ Running on port 8080 | Windows Server: ✅ Running on port 8081
- Single-module compile: ✅ BUILD SUCCESS | Multi-module: ⚠️ ~100 errors remaining
- Deployed JAR: ⚠️ OUTDATED (v0.5.2) | Git push: ⚠️ Timing out
- **Next action:** Complete git push, then fix server-shared compilation errors

### What to Do Next

See [MEMORY.md](MEMORY.md) for detailed next actions and active blockers. Summary:

1. **⚠️ Git push to GitHub** — Run `git push --force-with-lease origin main` to complete repository cleanup
2. **⚠️ Multi-module Maven** — Fix remaining ~100 compilation errors in server-shared module
3. **⚠️ Full JAR build** — Resolve missing `com.yisy.modules.doradl` dependency
4. **⚠️ Pi deployment** — Build and deploy new JAR with all 147+ controllers to Pi
### Code Improvements Summary

See section 17.5 for full details. Key items:
- **✅ RESOLVED - Source code recovered:** Full server source (2,521 Java files) found at original project path — see docs/lost-code-inventory.md
- **⚠️ BLOCKED - Full JAR build:** Missing `com.yisy.modules.doradl` external dependency prevents building all 147+ controllers into deployable JAR
- **⚠️ OUTDATED - Deployed JAR:** Pi server runs v0.5.2 JAR with only 7 controllers; source has 147+ controllers ready to deploy
- **✅ Unity server connection:** NetworkManager.cs updated to point to Pi server (192.168.50.147:8080)
- **GameManager.cs refactoring:** Split 2234+ line file into focused managers
- **GameHUD.cs refactoring:** Add panel registry pattern for dynamic management
- **NetworkManager.cs enhancements:** Add request caching and WebSocket support
- **Build streamlining:** Consolidate duplicate JAR locations and server sources
- **Git hygiene:** Add Library/, Temp/, UserSettings/, and .class files to .gitignore
- **Git repository cleanup (v0.5.27):** Removed large JAR files from entire git history, reducing repo from 3.46 GB to 282 MB. Push to GitHub needs completion.

---

## 17. Quick Reference Cards

### Add a New Universe
1. Create module in Deployment/RaspberryPi/server-build/src/main/java/com/doramcu/server/
2. Implement the 4X interfaces from fourx/ module
3. Register in gamecore/ module registry
4. Build and deploy

### Deploy to Pi
1. mvn clean package -DskipTests
2. node Deployment/RaspberryPi/file-server.js
3. On Pi: wget http://192.168.50.150:8888/doramcu-server.jar -O /tmp/doramcu-server.jar
4. sudo cp /tmp/doramcu-server.jar /opt/doramcu/server/
5. sudo systemctl restart doramcu-server

### Debug Pi Server
- sudo journalctl -u doramcu-server -f
- curl http://192.168.50.147:8080/api/v1/admin/status
- Tunnel: curl -X POST http://192.168.50.147:9000/exec -d '{"command":"your-command"}'

---

## 17.5 Code Improvements & Streamlining Opportunities

### ✅ RESOLVED: Original Project Source Code Recovered

**All source code found at `C:\Users\kenba\IdeaProjects\DoraMCU\`** (v0.5.20)
- The complete original monorepo was discovered at the original project path
- **2,521 Java files** including hex/ (317), earth2026/ (199), traveller/ (201), all 19 universes
- **DNN module** with 6 source files, running on port 8084 with beautiful glassmorphism UI (2,297 lines)
- **Earth 2026 HQ system** with YoungerIndustriesHQ, HQ portals, kiosk upgrades, and 18 HQ-related files
- **34 web UI pages** including younger-hq.html, corporate-office.html, and full landing-to-HQ flow
- **~67,000 total files** across all modules and clients (dora-pwa, dora-electron, dora-web, unity-client)
- See [docs/lost-code-inventory.md](docs/lost-code-inventory.md) for complete inventory
- **Next step:** ⚠️ BLOCKED — Build compiles (v0.5.24) but JAR contains only 4 news classes. Missing `com.yisy.modules.doradl` external dependency prevents building all 147+ controllers. Need to resolve doradl stubs or add the dependency to create a deployable JAR.

### Server Architecture — Source Recovered & Integrated (v0.5.22)

| Area | Status | Details |
|------|--------|---------|
| **Local vs Production** | ✅ INTEGRATED | All 2,521 Java files copied to build, package renamed to com.doramcu.server |
| **DNN Service** | ✅ INTEGRATED | DNN code in build + server-side DoraNetworkNewsController with GalaxyEventService |
| **Earth 2026 Module** | ✅ INTEGRATED | 199 files including YoungerIndustriesHQ, HQ portals, kiosk upgrades, tycoon mode |
| **Universe Modules** | ✅ INTEGRATED | All 19 universes with full source code (hex, traveller, dnd5e, LOTR, etc.) |
| **AI System** | ✅ INTEGRATED | 59 files with commanders, network, players |
| **Admin System** | ✅ INTEGRATED | 66 files with AI admin character system |
| **Corporation System** | ✅ INTEGRATED | 60 files with stocks, assets, hiring, contracts |
| **Config & Dependencies** | ✅ UPDATED | pom.xml with all dependencies from build.gradle, application.properties (216 lines) |
| **Build Status** | ⚠️ PARTIAL | BUILD SUCCESS achieved (v0.5.24) but JAR contains only 4 news classes due to missing `com.yisy.modules.doradl` dependency. Full build requires resolving doradl stubs or adding the external dependency. |
| **Deployed JAR** | ⚠️ OUTDATED | Pi runs v0.5.2 JAR with 7 controllers; source has 147+ controllers that cannot be built without doradl dependency |

### Unity Client Improvements

| Area | Current State | Recommended Improvement |
|------|---------------|------------------------|
| **NewsPanel** | ✅ NewsPanel implemented with category/galaxy filters | Connect to production server for live news |
| **Server Connection** | ✅ Updated | NetworkManager.cs points to Pi server (192.168.50.147:8080) for end-to-end testing |
| **Error Handling** | Basic try/catch in NetworkManager | Add retry logic, circuit breaker pattern, and user-friendly error messages |
| **Configuration** | Hardcoded server URLs in NetworkManager | Externalize to config file or Unity ScriptableObject |
| **Asset Management** | Large Library/ and Temp/ directories in git | Add to .gitignore to reduce repository size |

### Code Quality Observations

1. **GameManager.cs (2234+ lines)** – Single massive file handling all game logic. Consider splitting into:
   - `GameSessionManager` – Session lifecycle
   - `FleetManager` – Fleet operations
   - `CombatManager` – Combat resolution
   - `EconomyBridge` – Economy integration
   - `UIManager` – UI coordination

2. **GameHUD.cs (723+ lines)** – Central HUD manager with many panel types. Consider:
   - Panel registry pattern for dynamic panel management
   - Interface-based panel system for easier extension

3. **NetworkManager.cs** – Good structure with coroutine-based HTTP calls. Consider:
   - Adding request caching for frequently accessed endpoints
   - WebSocket support for real-time updates (already used by Earth 2026)

4. **EconomyManager.cs (775 lines)** – Well-structured with events. Consider:
   - Exposing more events for UI reactivity
   - Adding shipment creation API for Unity client

5. **TransportationNetwork.cs (483 lines)** – Static class with shipment logic. Consider:
   - Converting to instance-based for testability
   - Adding shipment history tracking

### Documentation Improvements

| Document | Issue | Recommendation |
|----------|-------|----------------|
| **api-endpoints.md** | Lists ~2,700 endpoints but source is missing | Mark endpoints as "Production Only – Source Pending" |
| **server-structure.md** | Shows full directory tree but files don't exist locally | Add note about production vs local discrepancy |
| **architecture.md** | References dora-news-network as Gradle subproject | Add link to actual source when available |

### Build & Deployment Streamlining

1. **Dual JAR locations** – JARs exist in both `modules/` and `Deployment/RaspberryPi/server-build/target/`. Consolidate to one location.
2. **Server source duplication** – `server-src/` and `server-build/src/` contain similar files. Use one canonical source.
3. **Compiled classes in git** – `.class` files in `Deployment/RaspberryPi/com/` and `out/` should be in .gitignore.
4. **Large binary files** – Library/, Temp/, and UserSettings/ should be excluded from git.

### Security Notes

1. **Hardcoded credentials** – Check for any hardcoded passwords in application-pi.properties
2. **Tunnel server** – The tunnel server (port 9000) allows remote command execution; ensure it's properly authenticated
3. **sudo password** – Pi sudo password (YIS2026) is documented in STARTING_POINT.md; consider using SSH keys instead

---

## 18. Version History

See [MEMORY.md](MEMORY.md) for the full chronological session log (v0.5.2 to v0.5.28).

### Recent Versions
- **v0.5.28** — Bluetooth PAN, SSH Key Auth, Network Benchmark (2026-11-19)
- **v0.5.27** — Git Repository Cleanup (2026-11-19)
- **v0.5.26** — Session Handoff (2026-11-19)
- **v0.5.25** — Dora News Network Files Recreated
- **v0.5.24** — Compilation Errors Fixed (BUILD SUCCESS)
- **v0.5.22** — Earth 2026 HQ Server Backend & Full Monorepo Integration
- **v0.5.21** — Recovered Web UI Integration & Streamlining
- **v0.5.20** — Original Project Source Code Recovery
- **v0.5.19** — Dora News Network Release
- **v0.5.18** — Code Improvements & Streamlining Analysis
- **v0.5.17** — Unity Client HQ Desk & Corporation Dashboard Release
- **v0.5.16** — Unity Client Onboarding UI Flow Release
- **v0.5.15** — Unity Client Galaxy Selector & Multi-Universe Panel Release
- **v0.5.14** — Unity Client Logistics & Transportation Panel Release
- **v0.5.13** — Unity Client Empire Management & Trade Integration Release
- **v0.5.12** — Unity Client Economy & Ship Building Integration Release
- **v0.5.11** — Unity Client 4X HUD & UI Integration Release
- **v0.5.10** — Unity Client Player Actions Integration Release
- **v0.5.9** — Unity Client 4X Game Session Integration Release
- **v0.5.8** — Galaxy Renderer Orbital Animation Release
- **v0.5.7** — Unity Client Celestial Integration Release
- **v0.5.6** — Documentation Split & Lean Core Document
- **v0.5.5** — Agent Behavior & Deployment Workflow Documentation
- **v0.5.4** — Cleanup & INFO Logging Release
- **v0.5.3** — Celestial Endpoint Fix, Tunnel Systemd Service & Pi Deployment
- **v0.5.2** — Admin Character & Client Completion Release

---

## 19. Unity UI Analysis & Improvement Plan

### 19.1 Current UI Inventory

**12 UI panels** in `Assets/Scripts/UI/` — all runtime-generated (no prefabs):

| Panel | Lines | Purpose |
|-------|-------|---------|
| `GameHUD.cs` | ~1100 | Central HUD manager, resource display, panel orchestration |
| `FleetPanel.cs` | ~350 | Fleet list, details, movement, creation |
| `SectorPanel.cs` | ~350 | Sector explorer, explore/scan/build actions |
| `EmpirePanel.cs` | ~270 | 5 departments (Military, Economic, Diplomatic, Infrastructure, Research) |
| `TradePanel.cs` | ~430 | Planet selection, commodity trade, trade routes |
| `ShipBuildPanel.cs` | ~280 | Ship designs, resource costs, build queue |
| `LogisticsPanel.cs` | ~860 | Shipments, deliveries, trade routes with tabs |
| `GalaxyPanel.cs` | ~710 | 19 universes browser, search, filters |
| `NewsPanel.cs` | ~650 | Dora News Network, category/galaxy filters, auto-refresh |
| `OnboardingPanel.cs` | ~1330 | 9-step onboarding flow (account → lore → career → HQ spawn) |
| `HQDeskPanel.cs` | ~820 | Corporation dashboard, character info, contracts, quick access |
| `ActionFeedback.cs` | ~190 | Toast notifications (success/failure/warning/info) |

**Core infrastructure files:**

| File | Lines | Role |
|------|-------|------|
| `GameManager.cs` | ~2440 | Main coordinator — state sync, fleet/sector actions, economy bridge |
| `NetworkManager.cs` | ~1750 | HTTP REST calls, SSE events, 4X DTOs, session/auth |
| `EconomyManager.cs` | ~775 | Production chain, transportation, resource pools, tax system |
| `TransportationNetwork.cs` | ~483 | Static class — shipments, deliveries, trade routes, cost calculations |

### 19.2 Architectural Patterns

1. **All UI is runtime-generated** — every panel creates its GameObjects, Buttons, Texts, and Images via `new GameObject()` in `CreatePanel()` methods. Zero UI prefabs exist.
2. **Singleton pattern** — `GameManager`, `NetworkManager`, `EconomyManager`, `GameHUD` all use `Singleton<T>` base class.
3. **Panel toggle via GameHUD** — `GameHUD` owns references to all panels and creates them in `Start()`.
4. **Legacy UI (UnityEngine.UI)** — uses `Text`, `Button`, `Image`, `InputField`, `ScrollRect` (not TextMeshPro in most panels).
5. **Hardcoded server URL** — `NetworkManager.serverUrl = "http://localhost:8080"` is a serialized field.
6. **Single scene** — `GetStarted_Scene.unity` is the only scene; all UI is code-created.
7. **Font inconsistency** — some panels use `LegacyRuntime.ttf`, others use `Arial` via `Resources.GetBuiltinResource`.
8. **Event-driven updates** — panels subscribe to manager events (e.g., `HQDeskPanel` → `CorporationManager.OnCorporationCreated`).
9. **Auto-refresh patterns** — NewsPanel refreshes every 10s, LogisticsPanel every 2s.

### 19.3 Top 20 Critical Problems

1. **GameManager.cs at 2440 lines** — god class handling session, fleets, sectors, economy, trade, combat, AI, onboarding, galaxy switching, notifications.
2. **No UI prefabs** — impossible to design in Unity Editor, no visual iteration possible.
3. **No scroll in FleetPanel/SectorPanel** — lists don't scroll when many items exist.
4. **No configuration externalization** — server URL, colors, sizes all hardcoded in code.
5. **No loading states** — no skeleton screens or progress indicators for async operations.
6. **No error recovery UI** — network failures show `Debug.Log` only, invisible to users.
7. **No modal dialogs** — no confirmation dialogs for destructive actions.
8. **No settings panel** — no audio, graphics, or server connection settings.
9. **No session info panel** — can't see current session details (players, turn, etc.).
10. **No statistics panel** — no personal stats (ships built, battles won, etc.).
11. **No notification center** — only transient toast messages, no persistent notification list.
12. **No tooltips or help system** — no context-sensitive help for UI elements.
13. **No keyboard shortcuts** — no hotkey support for panel toggles or actions.
14. **No minimap** — no spatial overview of current sector or galaxy.
15. **No 3D labels** — no world-space labels on planets, ships, or stations.
16. **No chat system** — no text chat for player communication.
17. **No inventory UI** — no personal inventory display for items or equipment.
18. **No mission tracker** — no active mission display or objectives panel.
19. **No debug panel** — no way to inspect game state at runtime.
20. **No version display** — no game version or build number visible in UI.

### 19.4 Improvement Roadmap

**Phase 1 — Foundation (High Priority)**
- Refactor `GameManager.cs` into focused managers (Session, Fleet, Combat, Economy, UI)
- Externalize UI configuration (server URL, colors, sizes) to ScriptableObject or config file
- Add scroll support to FleetPanel and SectorPanel
- Convert `TransportationNetwork` static class to instance-based for testability

**Phase 2 — UX Improvements (Medium Priority)**
- Add loading states and progress indicators for async operations
- Add error recovery UI with user-friendly messages and retry buttons
- Implement modal dialog system for confirmations
- Add tooltips and context-sensitive help
- Add keyboard shortcuts for panel toggles and common actions

**Phase 3 — New Features (Medium Priority)**
- Settings panel (audio, graphics, server connection, UI density)
- Session info panel (players, turn, session details)
- Statistics panel (personal stats, ships built, battles won)
- Notification center (persistent notification list)
- Minimap for spatial sector/galaxy overview

**Phase 4 — Advanced Features (Low Priority)**
- Chat system for player communication
- Inventory UI for personal items
- Mission/quest tracker panel
- Debug panel for runtime state inspection
- 3D labels on planets, ships, and stations
- FPS counter and performance monitoring
- Version display in HUD

### 19.5 Missing Features by Priority

**High Priority:**
- GameManager refactoring, UI prefabs, scroll support, config externalization, loading states, error UI, modal dialogs

**Medium Priority:**
- Settings panel, session info, statistics, notification center, tooltips, keyboard shortcuts, minimap, 3D labels

**Low Priority:**
- Chat system, inventory UI, mission tracker, debug panel, FPS counter, version display, dark/light theme, UI scaling, localization

---

## 20. Contact & Repository

- **GitHub Organization:** YoungerIndustries
- **Repository:** DoraMCU

---

*Document split from 302KB to ~15KB core + reference files in docs/ directory (v0.5.6)*
