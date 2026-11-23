# DoraMCU Agent Guidelines

This file is read by Junie at the start of every new agent session. Follow these instructions before doing anything else.

## Mandatory Startup Procedure

**Before any other action, read these two documents in order:**

1. **STARTING_POINT.md** — The static project overview. Contains agent behavior rules, deployment workflow, technology stack, build instructions, current state, and what to do next.
2. **MEMORY.md** — The living session log. Contains the chronological record of what previous agents did, resolved blockers, and the current state of the project.

**Why this order:** STARTING_POINT.md gives you the context and rules. MEMORY.md tells you what was just done and what needs attention now.

## Agent Behavior Rules

These rules are also documented in STARTING_POINT.md Section 0, but restated here for quick reference:

### No-Questions Policy
- **Do NOT ask the user questions** unless the decision involves a major architectural change or new feature design.
- **Assume yes** for routine implementation decisions, deployment steps, and configuration changes.
- **Execute immediately** based on the to-do list in STARTING_POINT.md.
- **Only ask** when there is a genuine ambiguity that could lead to incorrect implementation.

### 30-Second Status Updates
- **All agents MUST provide status updates every 30 seconds** during long operations.
- Include: current log size, line count, any errors found, process status.
- Format: `=== Xs: Log Y MB, Z lines ===` followed by key findings.
- Never leave the user waiting more than 30 seconds without an update.

### Deployment Workflow
- **All changes to the Pi server MUST flow through the PC server, never directly.**
- PC Server → Make code changes, build JAR locally.
- Pi Server → Deploy via file server (port 8888) + tunnel (port 9000).
- Back to PC Server → Verify endpoints, update documentation, commit changes.

### Memory Update
- **Update MEMORY.md at the end of every session.** Add a new entry at the top of the Session Log with what was done, what was blocked, files changed, and next actions.

## Key Files

| File | Purpose |
|------|---------|
| `STARTING_POINT.md` | Read me first — project overview, agent rules, deployment |
| `MEMORY.md` | Living memory — session log, current state, blockers |
| `docs/` | Reference documentation (server structure, API, architecture, etc.) |
| `docs/modules/` | Individual module deep dives (19 universe modules) |

## Project Context

- **DoraMCU** — Multi-universe 4X strategy game platform.
- **Server:** Java 27, Spring Boot 4.1.2, H2 file-based database, Gradle builds.
- **Client:** Unity 2022.3 LTS (C#), Godot 4.7.2 alternative.
- **Pi Brain Stem:** Production server at 192.168.1.111:8080 (ethernet).
- **Architecture:** Pi = Brain Stem, PC/other servers = Brain Nodes syncing with Pi.
- **Plugin-based:** Each universe is a separate hotswappable module JAR.
