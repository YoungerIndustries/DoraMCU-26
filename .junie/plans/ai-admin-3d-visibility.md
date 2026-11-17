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

## Phase 4: Seed Remaining AI Admin Positions — In Progress

V105 migration seeds 22 Younger Industries office managers and positions 5 entity-linked AI characters.

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