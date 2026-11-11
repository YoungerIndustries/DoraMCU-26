---
sessionId: session-261110-154407-1lif
---

# Requirements

### Overview & Goals
Integrate the existing AI admin testing system into the development workflow so that after creating new code, the AI agent can automatically run tests against the running DoraMCU system, analyze results, and report on what passed or failed. This turns the AI admin from a manual testing tool into an automated self-testing capability.

### Current State
The AI admin system is fully implemented with:
- **Database**: `ai_admin_orders`, `ai_admin_test_reports`, `ai_admin_test_cases` tables (V40 migration)
- **Entities**: `AiAdminOrder`, `AiAdminTestReport`, `AiAdminTestCase` with repositories
- **Services**: `AiAdminService` (orchestration), `TestExecutor` (runs tests), `TestReportGenerator` (aggregates results)
- **REST API**: `AiAdminController` at `/api/v1/admin/ai-orders` (submit, list, get orders/reports)
- **Test Categories**: API health (3 tests), game mechanics (2 tests), AI chat (1 test), auth (2 tests) = 8 tests total
- **Chat Integration**: `admin-test` mode in `AiChatService.buildSystemPrompt()`
- **Configuration**: Configurable `dora.admin.test.base-url` in `application.properties`
- **Tests**: `AiAdminServiceTest`, `TestExecutorTest` unit tests

### What's Missing for Development Integration
1. **Automatic test triggering**: No mechanism to trigger tests after code changes
2. **Targeted testing**: Cannot test specific modules or features — only runs all tests
3. **Test expansion**: Only 8 basic tests; no tests for specific new features
4. **Self-healing**: No feedback loop to fix failed tests
5. **Development workflow integration**: No hook into the build/deploy cycle
6. **Test registration**: Tests are hardcoded in `TestExecutor`; no plugin mechanism

### Goals
- Enable the AI agent to trigger tests programmatically after implementing new code
- Support targeted testing of specific categories or custom test definitions
- Expand test coverage to include new features as they are added
- Create a feedback loop where test results inform code fixes
- Integrate into the development cycle (after implementation, before commit)

# Technical Design

### Current Implementation

**TestExecutor** (`src/main/java/.../admin/service/TestExecutor.java`):
- Hardcoded test methods: `runApiHealthTests()`, `runGameMechanicsTests()`, `runAiChatTests()`, `runAuthTests()`
- `runAllTests()` runs all 4 categories
- `runTestsByCategory(category)` runs a single category
- Each test uses `runTest(name, category, runnable)` helper with `TestRunnable` functional interface
- Base URL configurable via `@Value("${dora.admin.test.base-url:...}")`

**AiAdminService** (`src/main/java/.../admin/service/AiAdminService.java`):
- `submitOrder(submittedBy, orderText)` — creates order, schedules async execution
- `scheduleTestExecution(order)` — background thread: RUNNING → runAllTests → generateReport → COMPLETED
- ThreadPoolTaskExecutor with 2-4 threads, queue capacity 10
- No way to pass custom test parameters or target specific tests

**TestReportGenerator** (`src/main/java/.../admin/service/TestReportGenerator.java`):
- `generateReport(orderId, testCases)` — counts pass/fail/skip, builds JSON, saves entities
- `generateSummary()` — human-readable text with failed test details

**AiAdminController** (`src/main/java/.../admin/controller/AiAdminController.java`):
- All endpoints require admin auth (`isAdmin(authentication)`)
- POST submit order, GET list/get orders, GET reports, GET download

### Key Decisions

1. **Plugin-based test registration**: Instead of hardcoding tests in `TestExecutor`, introduce a `TestProvider` interface that can be implemented by any module to register its own tests. This allows new features to add their own tests without modifying `TestExecutor`.

2. **Order-based test targeting**: Extend `AiAdminService.submitOrder()` to parse the order text and extract test categories or specific test IDs. The AI agent can say "test API health" or "test the new promo code feature" and only those tests run.

3. **Development workflow hook**: After implementing new code, the AI agent calls `POST /api/v1/admin/ai-orders` with an order describing what to test, waits for completion, and reads the report to determine if the code is ready.

4. **Test result analysis**: The AI agent reads test reports and uses the results to decide whether to fix code, add more tests, or mark the feature as complete.

### Proposed Changes

#### 1. TestProvider Interface and Registration
- Create `TestProvider` interface with `registerTests()` method that returns a list of test definitions
- Create `DefaultTestProvider` containing the existing hardcoded tests (migrated from `TestExecutor`)
- Create `TestRegistry` service that collects all `TestProvider` beans and builds a test catalog
- `TestExecutor` queries `TestRegistry` instead of running hardcoded tests

#### 2. Order-based Test Targeting
- Extend `AiAdminService` to parse order text for category keywords (API, GAME, AI_CHAT, AUTH) or custom test names
- Pass selected test categories/IDs to `TestExecutor` instead of always running all tests
- Support natural language: "test all APIs", "test game combat", "test the new feature X"

#### 3. Development Workflow Integration
- Create `DevelopmentTestHelper` service that the AI agent calls after implementing code
- Flow: implement code → call helper → helper submits order → waits for completion → returns report → AI analyzes results
- Helper provides synchronous wait for test completion (polls order status)

#### 4. Expanded Test Coverage
- Add tests for specific features: promo codes, AI commander, galaxy time compression, etc.
- Each new feature module implements `TestProvider` to register its tests
- Tests verify both positive and negative scenarios

#### 5. Test Result Analysis
- Create `TestResultAnalyzer` service that processes test reports and generates recommendations
- Identifies patterns: flaky tests, recurring failures, performance degradation
- Suggests fixes based on error messages and test categories

### File Structure

```
src/main/java/com/yisy/modules/doramcu/admin/
├── service/
│   ├── TestExecutor.java              # Modified: uses TestRegistry instead of hardcoded tests
│   ├── AiAdminService.java            # Modified: parses order for test targeting
│   ├── TestReportGenerator.java       # No changes
│   ├── TestProvider.java              # NEW: interface for test registration
│   ├── DefaultTestProvider.java       # NEW: existing tests migrated here
│   ├── TestRegistry.java              # NEW: collects TestProvider beans
│   ├── DevelopmentTestHelper.java     # NEW: AI agent workflow helper
│   └── TestResultAnalyzer.java        # NEW: analyzes results, suggests fixes
├── model/
│   ├── TestDefinition.java            # NEW: test metadata (id, name, category, runnable)
│   └── ... (existing entities)
└── controller/
    └── AiAdminController.java         # Modified: add test catalog endpoint

src/test/java/com/yisy/modules/doramcu/admin/
├── service/
│   ├── TestRegistryTest.java          # NEW
│   ├── DevelopmentTestHelperTest.java # NEW
│   └── TestResultAnalyzerTest.java    # NEW
```

### Architecture Diagram

``` mermaid
graph LR
    A[AI Agent] -->|After code change| B(DevelopmentTestHelper)
    B -->|Submit order| C(AiAdminService)
    C -->|Parse & select| D(TestRegistry)
    D -->|Returns tests| C
    C -->|Schedule| E(TestExecutor)
    E -->|Run| F[DefaultTestProvider]
    E -->|Run| G[FeatureTestProvider]
    E -->|Results| H(TestReportGenerator)
    H -->|Store| I[(ai_admin_orders)]
    H -->|Store| J[(ai_admin_test_reports)]
    H -->|Store| K[(ai_admin_test_cases)]
    H -->|Report| L(TestResultAnalyzer)
    L -->|Recommendations| A
    B -->|Poll| C
    B -->|Get report| H
```

# Delivery Steps

### ✓ Step 1: Database schema and entities
Create the Flyway migration V40 and all JPA entities with repositories for the AI admin testing system.

- Create `V40__add_ai_admin_testing.sql` migration with `ai_admin_orders`, `ai_admin_test_reports`, and `ai_admin_test_cases` tables.
- Create `AiAdminOrder` entity with OrderStatus enum (PENDING, RUNNING, COMPLETED, FAILED).
- Create `AiAdminTestReport` entity with fields for summary, pass/fail counts, and JSON report.
- Create `AiAdminTestCase` entity with TestStatus enum (PASSED, FAILED, SKIPPED) and category field.
- Create repositories for all entities.
- Add `aiAdmin` boolean flag to `AiCharacter` entity.
- Seed AI admin character in migration.

**Status: COMPLETED** — All entities, repositories, and migration implemented.

### ✓ Step 2: Core services: AiAdminService, TestExecutor, TestReportGenerator
Implement the three core services that handle order processing, test execution, and report generation.

- Create `AiAdminService` — receives orders, schedules async execution, tracks status.
- Create `TestExecutor` — executes 4 test categories via RestTemplate.
- Create `TestReportGenerator` — aggregates results into reports with JSON.
- Create DTOs: `AiAdminOrderRequest`, `AiAdminReportDto`.
- Make base URL configurable via `application.properties`.

**Status: COMPLETED** — All services implemented, base URL configurable.

### ✓ Step 3: REST API controller and chat integration
Implement the AiAdminController REST endpoints and extend AiChatService with admin-test mode.

- Create `AiAdminController` at `/api/v1/admin/ai-orders` with 6 endpoints.
- Add admin rights check to all endpoints.
- Extend `AiChatService.buildSystemPrompt()` with `admin-test` mode.
- Create unit tests: `AiAdminServiceTest`, `TestExecutorTest`.

**Status: COMPLETED** — Controller, chat integration, and unit tests implemented.

### ✓ Step 4: TestProvider interface and TestRegistry
Introduce plugin-based test registration so new features can add their own tests.

- Create `TestProvider` interface with `registerTests()` method returning test definitions.
- Create `DefaultTestProvider` — migrate existing hardcoded tests from `TestExecutor` into this provider.
- Create `TestRegistry` service — collects all `TestProvider` beans at startup, builds test catalog.
- Modify `TestExecutor` to query `TestRegistry` instead of running hardcoded tests.
- Add `GET /api/v1/admin/ai-orders/tests` endpoint to list available tests.
- Create `TestRegistryTest` — verify test registration and catalog building.

### ✓ Step 5: Order-based test targeting and DevelopmentTestHelper
Enable targeted testing and create the development workflow helper.

- Extend `AiAdminService.submitOrder()` to parse order text for category keywords (API, GAME, AI_CHAT, AUTH) or test names.
- Pass selected test categories/IDs to `TestExecutor` instead of always running all tests.
- Create `DevelopmentTestHelper` service — synchronous helper for AI agent:
  - `runTestsAndReport(orderText)` — submits order, polls until completion, returns report.
  - Configurable timeout (default 60 seconds).
  - Returns `AiAdminTestReport` or throws exception on timeout/failure.
- Create `DevelopmentTestHelperTest` — verify order submission, polling, and report retrieval.

### ✓ Step 6: TestResultAnalyzer and expanded test coverage
Add test result analysis and expand test coverage for existing features.

- Create `TestResultAnalyzer` service:
  - `analyzeReport(report)` — processes test report, generates recommendations.
  - Identifies patterns: flaky tests, recurring failures, performance issues.
  - Returns structured analysis with suggested fixes.
- Add feature-specific tests via `TestProvider`:
  - Promo code tests (validate, redeem, expire).
  - AI commander tests (experience, promotion).
  - Galaxy time compression tests.
- Create `TestResultAnalyzerTest` — verify analysis and recommendations.
- Verify the complete flow: implement code → DevelopmentTestHelper → test execution → report → analysis.