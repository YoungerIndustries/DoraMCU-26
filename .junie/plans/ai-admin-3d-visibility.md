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

## Phase 3: Order-Aware Positioning — Implemented, Ready to Commit

AI admin positions are now dynamic — admins move to order target locations and return home after completion, with idle admins shifting slightly to simulate activity.

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

### * Step 6: Commit and push order-aware positioning to Lapis111426
Commit V104 migration, entity changes, dispatch integration, and idle movement service to the branch and push to remote.