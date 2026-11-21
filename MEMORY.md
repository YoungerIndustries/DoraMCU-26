# DoraMCU Living Memory

> This document is the persistent memory of AI agents working on DoraMCU.
> **Update it at the end of every session. Read it at the start of every session.**
>
> Each agent session adds an entry at the top of the Session Log (most recent first).
> Keep the Current State section accurate so the next agent knows exactly where things stand.

---

## Current State

- **Last Updated:** 2026-11-20 19:05
- **Current Version:** v0.5.28
- **Server Status:**
  - Pi Server: ✅ Running on port 8080 (PostgreSQL, systemd service)
  - Windows Server: ✅ Running on port 8081 (H2 DB, peer communication)
- **Build Status:**
  - Single-module compile: ✅ BUILD SUCCESS (`mvn clean compile -DskipTests` passes, v0.5.24)
  - Multi-module Maven: ⚠️ IN PROGRESS — 21 modules defined, server-shared (860+ files) has ~100 remaining compilation errors
  - Full JAR build: ⚠️ BLOCKED — missing `com.yisy.modules.doradl` external dependency prevents building all 147+ controllers
- **Deployed JAR:** ⚠️ OUTDATED — Pi runs backup JAR (v0.5.2 with 7 controllers); source has 147+ controllers
- **Git Status:** ⚠️ IN PROGRESS — committed 1036 files, removed large JARs from history (repo reduced from 3.46 GB to 282 MB). Push to `origin/main` is timing out due to slow network.
- **Next Agent Action:** Complete `git push --force-with-lease origin main` to finish repository cleanup, then continue fixing remaining compilation errors in server-shared module.

---

## Active Blockers

1. **Missing `com.yisy.modules.doradl` dependency** — prevents building all 147+ controllers into a deployable JAR. Need to resolve doradl stubs or add the external dependency.
2. **Multi-module Maven compilation errors** — server-shared module (860+ merged files) has ~100 remaining errors (missing imports, structural issues, stubbed files). See build logs `build-optionb-*.txt`.
3. **Git push to GitHub timing out** — `git push --force-with-lease origin main` needed after history cleanup (repo reduced from 3.46 GB to 282 MB).
4. **Pi deployment pending** — Pi runs outdated backup JAR; new code (2,521 files, DNN, web UI) not yet deployed.

---

## Session Log

### 2026-11-20 — Living Memory Document Created
- **What was done:** Created MEMORY.md as the living agent memory document. Consolidated current state, blockers, and version history from STARTING_POINT.md and PROJECT_ROADMAP.md into a single chronological log.
- **What was blocked:** Nothing — documentation task completed.
- **Files changed:** `MEMORY.md` (new), `STARTING_POINT.md` (to be updated with reference to MEMORY.md)
- **Next actions:** Update STARTING_POINT.md to reference MEMORY.md as the living log.

### 2026-11-19 — v0.5.28: Bluetooth PAN, SSH Key Auth, Network Benchmark
- **What was done:** Bluetooth PAN setup with bt-network NAP mode, audio profile suppression. SSH key deployment in setup script, sudo NOPASSWD config. WiFi vs Bluetooth benchmark (WiFi wins: ~8ms latency, 0% loss). HTTP tunnel documented as reliable Pi command method. SSH tunnel bat/psm1 tools created but flaky.
- **Key files:** `doramcu-setup-bt-network.sh`, `doramcu-setup.sh`, `doramcu-tunnel.bat`, `pi-tunnel.psm1`, `test-network-performance.sh`
- **Next actions:** Use HTTP tunnel (port 9000) for all automated Pi commands; SSH tunnel is flaky.

### 2026-11-19 — v0.5.27: Git Repository Cleanup
- **What was done:** Committed 1036 files. Removed large JAR files (doramcu-server.jar, .jar.bak) from entire git history using filter-branch. Repository reduced from 3.46 GB to 282 MB.
- **What was blocked:** Push to GitHub timing out due to slow network.
- **Next actions:** Run `git push --force-with-lease origin main` to complete push to GitHub.

### 2026-11-19 — v0.5.26: Session Handoff
- **What was done:** STARTING_POINT.md updated with current build status and next agent action.
- **What was blocked:** Multi-module JAR package blocked on server-shared dora-neuron dependency. Pi runs backup JAR.
- **Next actions:** Resolve dora-neuron dependency in server-shared module.

### 2026-11-19 — v0.5.25: Dora News Network Files Recreated
- **What was done:** Recreated 4 Java files (516 lines) for Dora News Network.
- **Next actions:** Integrate news files into build.

### 2026-11-19 — v0.5.24: Compilation Errors Fixed
- **What was done:** Fixed compilation errors. `mvn clean compile -DskipTests` now passes with BUILD SUCCESS.
- **What was blocked:** JAR contains only 4 news classes due to missing doradl dependency.
- **Next actions:** Resolve doradl dependency for full JAR build.

### 2026-11-19 — v0.5.22: Earth 2026 HQ Server Backend & Full Monorepo Integration
- **What was done:** Integrated 2,521 Java files from original monorepo. DNN code, Earth 2026 module (199 files including YoungerIndustriesHQ, HQ portals, kiosk upgrades), all 19 universes, AI system (59 files), admin system (66 files), corporation system (60 files). Updated pom.xml and application.properties.
- **Next actions:** Fix compilation errors for full build.

### 2026-11-19 — v0.5.21: Recovered Web UI Integration & Streamlining
- **What was done:** Integrated 34 web UI pages including younger-hq.html, corporate-office.html, and full landing-to-HQ flow. Build pipeline streamlined.
- **Next actions:** Continue build integration.

### 2026-11-19 — v0.5.20: Original Project Source Code Recovery
- **What was done:** Discovered complete original monorepo at original project path. Documented inventory of 2,521 Java files, DNN module (6 source files), Earth 2026 HQ system, 34 web UI pages, ~67,000 total files.
- **Key files:** `docs/lost-code-inventory.md`
- **Next actions:** Copy and integrate source code into build.

### 2026-11-19 — v0.5.19: Dora News Network Release
- **What was done:** Server-side DoraNewsNetwork with GalaxyEventService and Unity Client NewsPanel integration.
- **Next actions:** Connect to production server for live news.

### 2026-11-19 — v0.5.18: Code Improvements & Streamlining Analysis
- **What was done:** Analysis of code quality issues, build streamlining opportunities, and documentation improvements. Identified GameManager.cs (2234+ lines) as god class needing refactoring.
- **Next actions:** Implement recommended refactoring.

### 2026-11-19 — v0.5.17: Unity Client HQ Desk & Corporation Dashboard
- **What was done:** HQDeskPanel (820 lines) — central dashboard at Younger Industries HQ with character info, contracts, quick access.
- **Next actions:** Connect to production corporation API.

### 2026-11-19 — v0.5.16: Unity Client Onboarding UI Flow
- **What was done:** OnboardingPanel (1330 lines) — 9-step onboarding flow: account → lore → career → HQ spawn.
- **Next actions:** Test end-to-end onboarding flow.

### 2026-11-19 — v0.5.15: Unity Client Galaxy Selector & Multi-Universe Panel
- **What was done:** GalaxyPanel (710 lines) — browser for 19 universes with search and filters.
- **Next actions:** Test universe switching.

### 2026-11-19 — v0.5.14: Unity Client Logistics & Transportation Panel
- **What was done:** LogisticsPanel (860 lines) — shipments, deliveries, trade routes with tabs.
- **Next actions:** Connect to production transportation API.

### 2026-11-19 — v0.5.13: Unity Client Empire Management & Trade Integration
- **What was done:** EmpirePanel (270 lines) with 5 departments, TradePanel (430 lines) for planet selection and commodity trade.
- **Next actions:** Test trade routes.

### 2026-11-19 — v0.5.12: Unity Client Economy & Ship Building Integration
- **What was done:** EconomyManager connected to HUD, resource display, ShipBuildPanel (280 lines) for ship designs and build queue.
- **Next actions:** Test ship building flow.

### 2026-11-19 — v0.5.11: Unity Client 4X HUD & UI Integration
- **What was done:** GameHUD (1100 lines) — central HUD manager with resource display and panel orchestration. FleetPanel, SectorPanel, ActionFeedback.
- **Next actions:** Add scroll support to panels.

### 2026-11-19 — v0.5.10: Unity Client Player Actions Integration
- **What was done:** Fleet management, combat, station building actions integrated with Unity client.
- **Next actions:** Test combat flow.

### 2026-11-19 — v0.5.9: Unity Client 4X Game Session Integration
- **What was done:** FetchSectors + 3D sector rendering, login/register/session management.
- **Next actions:** Test session lifecycle.

### 2026-11-19 — v0.5.8: Galaxy Renderer Orbital Animation
- **What was done:** Orbital animation with eccentricity & inclination for 3D galaxy rendering.
- **Next actions:** Test with real celestial data.

### 2026-11-19 — v0.5.7: Unity Client Celestial Integration
- **What was done:** FetchCelestialObjects + 3D rendering of celestial bodies.
- **Next actions:** Connect to production celestial endpoint.

### 2026-11-19 — v0.5.6: Documentation Split & Lean Core Document
- **What was done:** Split 302KB STARTING_POINT.md to ~15KB core + reference files in docs/ directory.
- **Next actions:** Maintain lean documentation.

### 2026-11-19 — v0.5.5: Agent Behavior & Deployment Workflow Documentation
- **What was done:** Documented agent behavior rules, deployment workflow (PC → Pi via file server + tunnel), and Pi server access details.
- **Next actions:** Follow deployment workflow for all Pi changes.

### 2026-11-19 — v0.5.4: Cleanup & INFO Logging Release
- **What was done:** Downgraded logging from DEBUG to INFO for production.
- **Next actions:** Monitor production logs.

### 2026-11-19 — v0.5.3: Celestial Endpoint Fix, Tunnel Systemd Service & Pi Deployment
- **What was done:** Fixed three root causes of celestial endpoint 404/500: (1) @GetMapping("/") only matched trailing slash; (2) @Lob annotation on PostgreSQL mapped to OID type; (3) PostgreSQL permissions granted. Endpoint returns 17 celestial objects. Tunnel systemd service deployed and active on Pi.
- **Next actions:** Verify endpoint stability.

### 2026-11-19 — v0.5.2: Admin Character & Client Completion Release
- **What was done:** Admin character system and client completion. 7 controllers deployed to Pi.
- **Next actions:** Build on admin character foundation.

---

## Project Context (Quick Reference)

### What is DoraMCU?
DoraMCU (Dora Multi-Universe Game Master) is a **multi-universe 4X strategy game platform** supporting multiple sci-fi and fantasy universes (Star Trek, Star Wars, Babylon 5, D&D, Traveller, Warhammer 40k, and more) through a plugin-based architecture.

### Components
- **Java Spring Boot Server** — REST API, game logic, database, peer-to-peer networking
- **Unity Client** — 3D hex-based game client with multiple universe support
- **Web UI** — 34 HTML pages including DNN (glassmorphism UI), HQ desk, corporate office

### Key Stats
- **51 completed features** across 15 categories (see PROJECT_ROADMAP.md)
- **12,462+ passing Java tests**
- **2,521 Java source files** (all 19 universes integrated)
- **147+ controllers** in source (7 deployed on Pi)
- **12 Unity UI panels** (all runtime-generated, no prefabs)
- **~67,000 total files** across all modules and clients

### Documentation Files
| File | Purpose |
|------|---------|
| `STARTING_POINT.md` | Read me first — project overview, agent behavior, deployment workflow |
| `MEMORY.md` | **This file** — living agent memory, session log, current state |
| `PROJECT_ROADMAP.md` | Feature roadmap — 51 completed features, all milestones done |
| `MEMORY_CONFIG.md` | JVM/DL memory configuration (technical) |
| `.junie/plans/` | 13 historical feature plans with completed steps |
| `docs/` | Reference docs: server-structure, unity-structure, architecture, version-history |

---

*Last session log entry: 2026-11-20*
