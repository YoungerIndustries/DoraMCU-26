# Changelog

All notable changes to DoraMCU will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.6.3] - 2026-11-21

### Fixed
- **Landing and Boarding Flow:** Characters now land at Younger Industries HQ with an assigned office instead of "San Diego, CA" with an apartment.
  - `Earth2026Service.createCharacter()` now calls `CharacterLandingService.landCharacterAtHQ()` after character creation.
  - `Earth2026Service.startGame()` uses the character's existing HQ location instead of hardcoding "San Diego Convention Center".
  - `Earth2026Service.getStartingLocation()` returns "Younger Industries HQ" instead of "San Diego Convention Center".
  - `OnboardingController` door panel and Earth landing page updated to reference Younger Industries HQ and office.
  - Frontend `PortalController` text updated: "apartment" references changed to "office".

### Changed
- **Per-Module Versioning:** All 21 modules now have independent version numbers in `gradle.properties`. Each module can be versioned and deployed independently for hot-swap updates.
- **Circular Dependencies Resolved:** Removed `spring.main.allow-circular-references=true` from all property files. Broke two circular dependency cycles with `@Lazy` annotations.

## [0.6.2] - 2026-11-21

### Changed
- Prepared for per-module versioning system.

## [0.6.1] - 2026-11-21

### Fixed
- Fixed 70+ excluded test imports — test count restored from 2,870 to 10,833.
- Moved secrets to environment variables (JWT_SECRET, DB_PASSWORD).
- Re-enabled Flyway with baseline approach (baseline-version=999).
- Fixed Hibernate LocalDateTime type mismatches in 7 repositories.
- Fixed various pre-existing issues (PlanetApproach landing logic, Galaxy3DController radius, DaocEconomyBridge null faction).

### Changed
- Refactored `Dnd5eController` into 5 focused controllers (3,960 → 1,894 lines).

## [0.6.0] - 2026-11-21

### Added
- Brain Node Registration System — PC servers register with Pi Brain Stem.
- H2 file-based database standardized across all platforms.

### Changed
- Replaced DeepLearning4j with StrategyNetwork (pure-Java heuristic model).

### Fixed
- Fixed cross-platform SQL dialect issues (CLOB/LONGTEXT, @Lob → TEXT, NOW() in JPQL).
- Fixed JWT_SECRET circular reference and logback production profile collision.

## [0.5.34] - 2026-11-21

### Added
- Brain Node Registration System with heartbeat support.
- PC servers register with Pi Brain Stem via `/api/v1/nodes/register`.

## [0.5.33] - 2026-11-20

### Added
- Pi Brain Stem deployment at 192.168.50.147:8080 with all 16 galaxies and 19 hotswappable modules.

## [0.5.32] - 2026-11-20

### Added
- Pre-deployment testing workflow — test and production servers run simultaneously on PC before Pi deployment.

[Unreleased]: https://github.com/YoungerIndustries/DoraMCU-26/compare/v0.6.3...HEAD
[0.6.3]: https://github.com/YoungerIndustries/DoraMCU-26/compare/v0.6.2...v0.6.3
[0.6.2]: https://github.com/YoungerIndustries/DoraMCU-26/compare/v0.6.1...v0.6.2
[0.6.1]: https://github.com/YoungerIndustries/DoraMCU-26/compare/v0.6.0...v0.6.1
[0.6.0]: https://github.com/YoungerIndustries/DoraMCU-26/compare/v0.5.34...v0.6.0
[0.5.34]: https://github.com/YoungerIndustries/DoraMCU-26/compare/v0.5.33...v0.5.34
[0.5.33]: https://github.com/YoungerIndustries/DoraMCU-26/compare/v0.5.32...v0.5.33
[0.5.32]: https://github.com/YoungerIndustries/DoraMCU-26/releases/tag/v0.5.32
