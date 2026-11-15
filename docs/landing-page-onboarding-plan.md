# Implementation Plan: Landing Page with Promo Code Entry Panel & Onboarding Sequence

## Problem Statement

Add a landing page (Dora News page) with a promo code entry panel that initiates the full onboarding sequence:

1. **Door Panel** — The landing page presents the "Do You Want to Live Forever?" door panel
2. **Promo Code Entry** — The player enters a landing key (promo code)
3. **Account Creation** — The player creates their account
4. **Corporation Creation** — The player defines their empire/corporation
5. **Character Creation** — The player creates their first character
6. **Earth 2026 Landing** — The character lands on Earth2026 with an apartment home location in San Diego, CA, USA

---

## Current State Analysis

### Backend (Already Implemented)

The backend already provides a complete onboarding API via `OnboardingController` (`/api/v1/onboarding/*`):

| Endpoint | Method | Purpose |
|---|---|---|
| `/api/v1/onboarding/config` | GET | Returns onboarding flow configuration and payment methods |
| `/api/v1/onboarding/promo/validate` | POST | Validates a promo code without consuming it |
| `/api/v1/onboarding/payment/verify` | POST | Verifies a payment method selection |
| `/api/v1/onboarding/register` | POST | Creates user account, provisions player record, applies promo code |
| `/api/v1/onboarding/character` | POST | Creates Earth 2026 character |
| `/api/v1/onboarding/earth` | GET | Returns Earth 2026 landing page data (locations, scenarios) |

**Key models and services:**
- `PromoCode` — JPA entity with code, description, type (PLAYER/ADMIN), usage limits, expiry, bonuses
- `PromoCodeService` — Validates, creates, and manages promo codes
- `Earth2026Service` — Creates characters with default location `"San Diego, CA"` (line 114)
- `DoraNetworkNewsController` — REST API for the live news feed (`/api/v1/news/feed`, `/api/v1/news/live` via SSE)

### Frontend (Not Yet Implemented)

- **Electron app** (`dora-electron/renderer/`) — Game launcher with Dashboard, Games, Sessions, Galaxy, Settings tabs. No onboarding or News UI.
- **PWA** (`dora-pwa/`) — Lightweight neuron client. Not the main UI.
- **Unity client** (`unity-client/`) — Game renderer only. No landing page or onboarding UI.

**Gap:** No frontend page exists for the onboarding flow. The "Dora News page" mentioned in the issue is the landing page that needs to be created.

---

## Implementation Plan

### Phase 1: Backend Enhancements

#### 1.1 Add "Door Panel" Endpoint to OnboardingController

**File:** `src/main/java/com/yisy/modules/doramcu/controller/OnboardingController.java`

Add a new endpoint that returns the door panel configuration:

```
GET /api/v1/onboarding/door
```

Returns:
- The door panel message ("Do You Want to Live Forever?")
- The latest news feed items (from `DoraNetworkNewsController`/`GalaxyEventService`)
- A flag indicating whether the user is already registered

**Why:** The door panel is the first step — it combines the dramatic landing page message with the live news feed, giving the visitor context about the DoraMCU universe before they commit.

#### 1.2 Add Apartment Home Location to Earth2026 Character Creation

**File:** `src/main/java/com/yisy/modules/doramcu/earth2026/service/Earth2026Service.java`

When creating a character, ensure the character is assigned an apartment home location in San Diego, CA. Currently the character's `earthLocation` is set to `"San Diego, CA"` (line 114), but no specific structure (apartment) is assigned.

**Changes needed:**
- In `createCharacter()`, after saving the character, create or assign an `Earth2026Structure` of type "Apartment" in San Diego
- Link the structure to the character as their home location
- Return the home location details in the character creation response

**Related files:**
- `Earth2026Structure` model
- `Earth2026StructureRepository`
- `Earth2026Location` model (San Diego location must exist)

#### 1.3 Ensure San Diego Location is Seeded

**File:** `src/main/java/com/yisy/modules/doramcu/config/Earth2026Initializer.java` (or create if not exists)

Ensure that on application startup, the following are seeded if not present:
- `Earth2026Country`: USA
- `Earth2026Region`: California
- `Earth2026Location`: San Diego (city, type="City")
- `Earth2026Structure`: Default apartment template in San Diego

**Verification:** Check existing initializers and data.sql files for current seeding logic.

#### 1.4 Add News Feed to Onboarding Config

**File:** `src/main/java/com/yisy/modules/doramcu/controller/OnboardingController.java`

Inject `GalaxyEventService` and include recent news events in the `/api/v1/onboarding/config` response, so the landing page can display the latest DoraMCU universe events.

---

### Phase 2: Frontend — Dora News Landing Page (Electron App)

#### 2.1 Add "News" Tab to Electron App

**File:** `dora-electron/renderer/index.html`

Add a new navigation item to the sidebar:
```html
<li class="nav-item" data-tab="news">
    <span class="nav-icon">📰</span> News
</li>
```

Add a new tab content section for the News page.

#### 2.2 Create News Page with Door Panel & Promo Code Entry

**File:** `dora-electron/renderer/index.html` (new tab content)

The News page should display:

1. **Door Panel Section** (hero area):
   - "Do You Want to Live Forever?" headline
   - A "Step Through" button that initiates the onboarding flow

2. **Promo Code Entry Panel** (visible after clicking "Step Through"):
   - Input field for the landing key (promo code)
   - "Validate" button that calls `POST /api/v1/onboarding/promo/validate`
   - Success/error feedback
   - "Continue" button that advances to registration

3. **Live News Feed** (sidebar or bottom section):
   - SSE-connected feed from `GET /api/v1/news/live`
   - Displays recent DoraMCU universe events

#### 2.3 Create Onboarding Flow UI

**File:** `dora-electron/renderer/js/onboarding.js` (new file)

A dedicated JavaScript module that manages the multi-step onboarding flow:

**Step 1 — Door Panel:**
- Display the door panel message
- On "Step Through" click, show the promo code panel

**Step 2 — Promo Code Entry:**
- Accept promo code input
- Validate via `POST /api/v1/onboarding/promo/validate`
- Show bonus information if valid
- Allow skipping (promo code is optional)
- On "Continue", advance to registration

**Step 3 — Account Registration:**
- Username, password, email fields
- Empire name, government type, ethos fields
- Promo code (pre-filled from Step 2)
- Submit via `POST /api/v1/onboarding/register`
- Store the returned JWT token

**Step 4 — Character Creation:**
- Character name, age, occupation fields
- Submit via `POST /api/v1/onboarding/character`
- Show confirmation with character details

**Step 5 — Earth 2026 Landing:**
- Fetch landing data via `GET /api/v1/onboarding/earth?username=...`
- Display the character's apartment location in San Diego, CA
- Show available scenarios
- "Begin Your Journey" button that transitions to the game

#### 2.4 Style the Onboarding Flow

**File:** `dora-electron/renderer/css/shell.css` (extend)

Add styles for:
- Door panel hero section (dramatic, cinematic feel)
- Promo code entry panel (input, validation feedback)
- Multi-step progress indicator
- Registration form
- Character creation form
- Earth 2026 landing page with location map

---

### Phase 3: Frontend — News Feed Integration

#### 3.1 Connect to Live News SSE

**File:** `dora-electron/renderer/js/news-feed.js` (new file)

- Establish SSE connection to `GET /api/v1/news/live`
- Render incoming events in the news feed section
- Handle reconnection on disconnect
- Display event types with appropriate icons

#### 3.2 Integrate News Feed into News Page

**File:** `dora-electron/renderer/index.html`

Embed the news feed component in the News tab, alongside the door panel and onboarding flow.

---

### Phase 4: Backend — Corporation Creation Enhancement

#### 4.1 Corporation/Empire Creation Step

The current `register` endpoint accepts `empireName`, `governmentType`, and `ethos` as optional fields. To make corporation creation a more distinct step:

**Option A (Recommended):** Keep it combined with registration but enhance the UI to make it feel like a separate step.

**Option B:** Split into two endpoints:
- `POST /api/v1/onboarding/register` — User account only
- `POST /api/v1/onboarding/corporation` — Empire/corporation creation

**Recommendation:** Use Option A for simplicity. The frontend can present it as a multi-step form where Step 3 is "Account" and Step 4 is "Corporation", but both are submitted together to the existing `/register` endpoint.

---

### Phase 5: Testing & Validation

#### 5.1 Backend Tests

- **OnboardingController:** Add tests for the new `/door` endpoint
- **Earth2026Service:** Verify that character creation assigns an apartment structure
- **Earth2026Initializer:** Verify San Diego location seeding

**Files to create/modify:**
- `src/test/java/com/yisy/modules/doramcu/controller/OnboardingControllerTest.java`
- `src/test/java/com/yisy/modules/doramcu/earth2026/service/Earth2026ServiceTest.java`

#### 5.2 Frontend Tests

- Manual end-to-end test of the full onboarding flow
- Verify SSE news feed connection
- Verify promo code validation feedback

---

## File Change Summary

| File | Action | Description |
|---|---|---|
| `OnboardingController.java` | Modify | Add `/door` endpoint, inject `GalaxyEventService`, include news in config |
| `Earth2026Service.java` | Modify | Assign apartment structure on character creation |
| `Earth2026Initializer.java` | Modify/Create | Seed San Diego location and apartment template |
| `dora-electron/renderer/index.html` | Modify | Add News tab, door panel, onboarding flow UI |
| `dora-electron/renderer/js/onboarding.js` | Create | Multi-step onboarding flow logic |
| `dora-electron/renderer/js/news-feed.js` | Create | SSE news feed component |
| `dora-electron/renderer/css/shell.css` | Modify | Styles for onboarding and news page |
| `OnboardingControllerTest.java` | Modify | Tests for new endpoints |
| `Earth2026ServiceTest.java` | Modify | Tests for apartment assignment |

---

## Sequence Diagram

```
Visitor                          Browser (Electron)              Backend API
  |                                   |                              |
  |  1. Open DoraMCU                  |                              |
  |  <---- Landing Page ------------>|  GET /onboarding/door        |
  |                                   |  <---- Response ------------>|
  |                                   |  (door message + news feed)  |
  |                                   |                              |
  |  2. Click "Step Through"          |                              |
  |  <---- Promo Panel -------------->|                              |
  |                                   |                              |
  |  3. Enter promo code              |                              |
  |                                   |  POST /onboarding/promo/     |
  |                                   |  validate                    |
  |                                   |  <---- Valid/Invalid ------->|
  |                                   |                              |
  |  4. Fill registration +           |                              |
  |     corporation info              |                              |
  |                                   |  POST /onboarding/register   |
  |                                   |  <---- JWT + player PIN ---->|
  |                                   |                              |
  |  5. Create character              |                              |
  |                                   |  POST /onboarding/character  |
  |                                   |  <---- Character obj -------->|
  |                                   |                              |
  |  6. Land on Earth 2026            |                              |
  |  <---- Earth Page --------------->|  GET /onboarding/earth       |
  |                                   |  <---- Locations + scenarios>|
  |                                   |                              |
  |  (Character is in San Diego, CA   |                              |
  |   with apartment home location)   |                              |
  |                                   |                              |
  |  7. Live news feed                |  GET /news/live (SSE)        |
  |  <---- Events ------------------>|  <---- Stream ------------- >|
  |                                   |                              |
```

---

## Dependencies & Risks

1. **Earth2026Structure seeding** — The apartment structure type must be defined in the `Earth2026Structure` model. Verify that "Apartment" is a valid structure type.
2. **SSE in Electron** — Ensure the Electron renderer can maintain SSE connections to the backend. May need CORS configuration.
3. **Payment integration** — The current payment verification is a placeholder. The onboarding flow should work without actual payment, but the UI should be ready for real payment gateway integration.
4. **Unity client integration** — The Unity client does not have onboarding UI. The onboarding flow is entirely in the Electron app. After onboarding, the player can launch the Unity client for gameplay.

---

## Success Criteria

- [ ] Visitor lands on the Dora News page and sees the "Do You Want to Live Forever?" door panel
- [ ] Visitor can enter a promo code (landing key) and see validation feedback
- [ ] Visitor can complete registration with account + corporation info
- [ ] Visitor can create their first character
- [ ] Character is placed on Earth 2026 in San Diego, CA with an apartment home location
- [ ] Live news feed displays recent DoraMCU universe events
- [ ] The full flow works end-to-end without errors
