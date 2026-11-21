# CLAUDE.md

## Overview
MuscleGainTracker: offline-first native Android app focused on muscle gain. This repo is the backend and shared foundation only. Frontend (Jetpack Compose) is built separately in Google Antigravity.

## Technology
Kotlin 2.0.21, AGP 8.7.3, Room 2.6.1 (KSP), Coroutines/Flow, DataStore, JUnit 4 (+ Robolectric for Room tests), Gradle Kotlin DSL. Versions: `gradle/libs.versions.toml`. No DI framework, no network, no analytics.

## Modules
- `:core:domain` (pure Kotlin/JVM): models, repository interfaces, validation, calculators, use cases.
- `:core:data` (Android library): Room database, DAOs, repository implementations, backup/CSV, DataStore, `MuscleGainContainer`.
- `:app`: minimal shell (Application + placeholder Activity). Frontend work happens here only.

## Commands
- All tests: `./gradlew test`
- Domain tests: `./gradlew :core:domain:test`
- Data tests: `./gradlew :core:data:testDebugUnitTest`
- Build: `./gradlew :app:assembleDebug`

## Integration rules
- Contract: `docs/INTEGRATION_CONTRACT.md`. The UI uses only the container, repositories and use cases.
- No business logic in composables. No Room or calculations in `:app`.
- Keep nullable values null; never show missing data as zero.
- Update `docs/AI_HANDOFF.md` after every stage.

## Do not change without agreement
Calculation formulas and MET tables, rest-day and missed-session logic, calories-never-adjust-targets rule, entry/goal/schedule model fields, repository signatures, database schema (use migrations), JSON backup format, Monday-Sunday weeks.
