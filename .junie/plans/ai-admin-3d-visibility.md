---
sessionId: session-261117-051457-4ssl
---

# AI Admin 3D Visibility — Feature Plan

## Phase 1: Core 3D Visibility — Complete ✓

All three delivery steps implemented, tested, committed, and pushed to `Lapis111426`.

- **V101 migration**: Added `pos_x`, `pos_y`, `pos_z`, `galaxy_name` to `ai_characters`
- **Galaxy3DService integration**: AI admins render as `AI_ADMIN` bodies in 3D view
- **Proximity API + WebSocket**: `AiAdminVisibilityService`, REST endpoints, WebSocket broadcast

## Phase 2: Seed AI Admin Positions — Complete ✓

V102 migration seeds 3D positions for all 34 Earth-based AI admins. Committed and pushed to `Lapis111426`.

- 20 country leaders at capital cities
- 12 region governors near regional capitals
- UN Secretary-General at Earth center (0, 0, 0)

## Phase 3: Order-Aware Positioning — Complete ✓

AI admin positions are now dynamic — admins move to order target locations and return home after completion, with idle admins shifting slightly to simulate activity. Committed and pushed to `Lapis111426`.

### What was implemented

**V104__add_ai_order_location.sql (new migration):**
- Added `target_galaxy`, `target_pos_x`, `target_pos_y`, `target_pos_z` to `ai_admin_orders`
- Added `home_pos_x`, `home_pos_y`, `home_pos_z` to `ai_characters`
- Backfilled home positions from current positions (V102 seeded data)

**AiAdminOrder.java:**
- Added `targetGalaxy`, `targetPosX`, `targetPosY`, `targetPosZ` fields with getters/setters
- Added `hasTargetLocation()` helper

**AiAdminOrderRequest.java:**
- Added optional location fields: `targetGalaxy`, `targetPosX`, `targetPosY`, `targetPosZ`

**AiCharacter.java:**
- Added `homePosX`, `homePosY`, `homePosZ` fields with getters/setters
- Added `hasHomePosition()` helper

**AiAdminService.java:**
- Added overloaded `submitOrder()` with location parameters (backward-compatible)

**AiAdminController.java:**
- Updated order submission to pass location fields
- Response includes target location if specified

**AiAdminDispatchService.java:**
- `dispatchSingleOrder()`: saves home position before moving, moves AI admin to order target if specified
- `completeOrderForAdmin()`: returns AI admin to home position after completion
- `releaseAdminFromOrder()`: returns AI admin to home position on failure/retry

**AiAdminIdleMovementService.java (new):**
- `@Scheduled(fixedRate = 600000)` — runs every 10 minutes
- Moves idle admins (no active orders) by random offset up to 2 units
- Logs movement for debugging

**AiCharacterRepository.java:**
- Added `findByAiAdminTrueAndActiveTrueAndCurrentOrderIdIsNullAndPositionNotNull()` query

### Verification
- `compileJava` BUILD SUCCESSFUL — all changes compile with zero errors
- Test compilation has pre-existing errors (missing doradl packages) unrelated to these changes

### Next: Commit and push to `Lapis111426`

---

## Delivery Steps

### ✓ Step 1: Add 3D position fields to AiCharacter (V101)
Spatial coordinates added to AI characters so they can be placed in the 3D game world.

### ✓ Step 2: Integrate AI admins into Galaxy3DService
AI admins now render as `AI_ADMIN` bodies in the 3D galaxy view.

### ✓ Step 3: Add proximity-based visibility API and WebSocket
AI admins appear when players are nearby via proximity queries and WebSocket broadcast.

### ✓ Step 4: Seed AI admin positions (V102)
All 34 Earth-based AI admins positioned in Milky Way galaxy.

### ✓ Step 5: Add order-aware positioning (V104)
Orders can target specific locations; AI admins move to targets and return home. Idle admins shift periodically.

### ✓ Step 6: Commit and push order-aware positioning to Lapis111426
Committed (e0f7fc4) and pushed to Lapis111426. 10 files changed (1192 insertions, 109 deletions).

### ✓ Step 7: Merge Lapis111426 into master and push to origin
All 37 commits from Lapis111426 merged into local master via fast-forward, then pushed to origin/master. The AI admin 3D visibility feature along with all other features (SFB combat, DL4j fixes, tests, etc.) are now on the master branch.

## Phase 4: Seed Remaining AI Admin Positions — Complete ✓

V105 migration seeds 22 Younger Industries office managers and positions 5 entity-linked AI characters. Committed and pushed to master.

### ✓ Step 8: Create V105 migration for remaining AI admin positions
Created V105__seed_remaining_ai_admin_positions.sql with 22 office managers (IDs 35-56) and 5 entity-linked AI characters (IDs 2001-2005) positioned in 3D space.

### ✓ Step 9: Commit and push V105 migration to Lapis111426
Committed (716ae77) and pushed to Lapis111426. 2 files changed (317 insertions).

### ✓ Step 10: Merge V105 into master and push to origin
Fast-forward merged from Lapis111426 into local master, then pushed to origin/master. All 27 AI admin positions (22 office managers + 5 entity-linked) are now on the master branch.

## Phase 5: Admin Activity Dashboard — Complete ✓

Real-time dashboard for monitoring AI admin workload, dispatch status, 3D positions, and notifications. Accessible as a standalone page and via the desktop client sidebar.

### What was implemented

**AiAdminController.java:**
- Added `GET /api/v1/admin/ai-orders/dashboard/summary` — combined endpoint returning dispatch summary, admin list with positions, galaxy list, and notification counts
- Injected `AiAdminVisibilityService` dependency

**PresenceWebSocketHandler.java:**
- Added `broadcastDashboardUpdate(eventType, adminId)` — broadcasts `dashboard_update` WebSocket events

**AiAdminDispatchService.java:**
- Injected `PresenceWebSocketHandler` dependency
- `completeOrderForAdmin()`: broadcasts `admin_workload_changed` after order completion
- `releaseAdminFromOrder()`: broadcasts `admin_workload_changed` after release

**AiAdminIdleMovementService.java:**
- Injected `PresenceWebSocketHandler` dependency
- `moveIdleAdmins()`: broadcasts `admin_position_changed` after each idle admin moves

**dora-web/js/admin-dashboard-client.js (new):**
- JavaScript API client module with `getDashboardSummary()`, `getAdminsByGalaxy()`, `getAdminPositions()`, `subscribeToUpdates()`
- WebSocket subscription with auto-reconnect

**dora-web/admin-dashboard.html (new):**
- Standalone dashboard page with dark theme matching desktop client
- Summary cards: total admins, available, busy, active orders, pending orders, unread alerts
- Filter bar: galaxy, role, status dropdowns + name search + refresh button
- Admin table: name, role, galaxy tag, status badge, workload bar, current order, 3D position
- Real-time WebSocket updates with 60-second fallback polling

**dora-web/clients/desktop/index.html:**
- Added "🤖 AI Admin Dashboard" sidebar nav item
- Added iframe section panel loading the standalone dashboard page

### ✓ Step 11: Commit and push admin dashboard to master
Committed (9b0c894) and pushed to origin/master. 8 files changed (1534 insertions, 9 deletions). The admin activity dashboard is now on the master branch, accessible as a standalone page and via the desktop client sidebar navigation.

## Phase 6: Frontend 3D Rendering in Unity — Complete ✓

Make AI admin characters visible as 3D entities in the Unity client in both galaxy view and planet surface view.

### ✓ Step 12: Add AiAdminData and NetworkManager API client
Created AiAdminData.cs and updated NetworkManager.cs with API client for fetching AI admin positions.

### ✓ Step 13: Create AiAdminRenderer for galaxy view
Created AiAdminRenderer.cs with color-coded markers, labels, and position updates.

### ✓ Step 14: Extend PlanetSurfaceRenderer for AI admins
Added CreateAiAdminMarkers, ClearAiAdmins, and color coding to PlanetSurfaceRenderer.

### ✓ Step 15: Add click interaction and tooltip UI
Created AiAdminTooltip.cs and updated AiAdminRenderer with click detection and admin data map.

### ✓ Step 16: Commit and push Unity client changes to master
Committed (58c8d1d) and pushed to origin/master. 6 files changed (697 insertions, 3 deletions). The Unity 3D rendering for AI admin characters is now on the master branch, including galaxy view markers, planet surface markers, click tooltips, and API polling.

## Phase 7: AI Admin × Economic Production Integration — Complete ✓

Bridge AI admin work orders to build contracts so AI admins can execute economic production tasks (mining, processing, assembly, construction, repair). Committed and pushed to master.

### What was implemented

**V106__add_ai_order_build_contract.sql (new migration):**
- Added `build_contract_id` to `ai_admin_orders` with index and foreign key constraint

**AiAdminOrder.java:**
- Added `buildContractId` field with getter/setter and `isProductionOrder()` helper

**AiAdminOrderRequest.java:**
- Added optional `buildContractId` field

**AiAdminService.java:**
- Added overloaded `submitOrder()` with `buildContractId` parameter (backward-compatible)

**AiAdminProductionService.java (new):**
- `assignAndStartContract()`: assigns AI admin as leader, starts the build contract
- `updateContractProgress()` / `updateContractAndOrderProgress()`: tracks progress
- `completeContract()`: completes the contract and awards XP
- `failContract()`: marks contract as failed
- `getContractForOrder()` / `isProductionOrder()`: query helpers

**AiAdminDispatchService.java:**
- Injected `AiAdminProductionService` dependency
- New execution path in `executeOrderAsync()`: production orders execute build contracts instead of running tests

**AiAdminController.java:**
- Updated `submitOrder()` to pass `buildContractId` from the request
- Response includes `buildContractId` and `orderType` for production orders
- `orderToMap()` helper includes progress, target location, and order type

### ✓ Step 17: Add buildContractId to AiAdminOrder (V106 migration)
Created V106 migration, added buildContractId field to AiAdminOrder, updated AiAdminOrderRequest DTO, and AiAdminService.submitOrder() overload.

### ✓ Step 18: Create AiAdminProductionService
Created AiAdminProductionService with assignAndStartContract, updateContractProgress, completeContract, and failContract methods.

### ✓ Step 19: Integrate production execution into AiAdminDispatchService
Injected AiAdminProductionService, added production execution path in executeOrderAsync.

### ✓ Step 20: Add production order submission API endpoint
Updated AiAdminController.submitOrder() to pass buildContractId, added orderType and buildContractId to the response and orderToMap helper.

### ✓ Step 21: Commit and push economic production integration to master
Committed (4056eb8) and pushed to origin/master. 8 files changed (533 insertions, 8 deletions). The AI admin × economic production integration is now on the master branch.

## Phase 9: More AI Admin Types — In Progress

Adding 80 specialized AI admin characters (4 types × 20 countries): Military Commanders, Diplomatic Envoys, Intelligence Directors, and Science Officers.

### ✓ Step 26: Create V108 migration for 80 specialized AI admins
V108__seed_specialized_ai_admins.sql created with 80 AI characters (20 each: Military Commanders, Diplomatic Envoys, Intelligence Directors, Science Officers).

### ✓ Step 27: Update dialogue, quest types, and 3D rendering
Added DIPLOMACY, ESPIONAGE, RESEARCH to AiAdminQuest.QuestType enum. Updated CharacterDialogueService with role-aware prompts for Military Commander, Diplomatic Envoy, Intelligence Director, and Science Officer. Galaxy3DService getAiAdminColor method supports all 8 role types. Unity AiAdminRenderer color coding updated to match.

## Phase 8: AI Admin Player Interaction — Complete ✓

Enable players to interact with AI admin characters through dialogue, quests, and dynamic relationships. Committed and pushed to master.

### ✓ Step 22: Create database schema and entities for quests and disposition
V107 migration, AiCharacter disposition fields, AiAdminQuest entity, AiAdminQuestRepository.

### ✓ Step 23: Implement AiAdminInteractionService for quest lifecycle
Quest assignment, progress tracking, completion with XP rewards, disposition changes.

### ✓ Step 24: Extend dialogue and add REST API endpoints
CharacterDialogueService chatWithAiAdmin with role-aware system prompts, AiAdminDialogueController for SSE chat, quest endpoints on AiAdminController (assign, progress, complete, fail, abandon, disposition).

### ✓ Step 25: Commit and push AI admin player interaction to master
Committed (dce0344) and pushed to origin/master. 12 files changed (2283 insertions, 2 deletions). The AI admin player interaction feature is now on the master branch, including dialogue, quests, and dynamic relationships.

## Phase 8 Summary

All 4 steps complete. 12 files changed (V107 migration, AiCharacter disposition, AiAdminQuest entity, AiAdminQuestRepository, AiAdminInteractionService, AiAdminDialogueController, AiAdminController quest endpoints, AiAdminOrderRepository fix, plus related test files).