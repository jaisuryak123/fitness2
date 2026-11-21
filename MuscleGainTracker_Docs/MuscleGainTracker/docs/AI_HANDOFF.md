# AI_HANDOFF

## Stage 1: backend foundation (complete)
Delivered: project structure, version catalog, `:core:domain`, `:core:data`, minimal `:app` shell, unit tests, docs, CLAUDE.md.

### Verification status (honest report)
- Verified: `:core:domain` main sources compile with Kotlin 2.0.21, and calculators were smoke-run (running 360.2 kcal gross, walking 154.4, gym 420.0, weekly adherence 57.1%, matching the unit tests). The compile used a minimal stub of `kotlinx.coroutines.flow.combine`/`Flow` because Maven Central was unreachable.
- NOT verified in the build environment (no Android SDK, Gradle distribution or Maven access): Gradle sync, KSP/Room code generation, `:core:data` compilation, JUnit and Robolectric test execution, `:app` build. Dependency versions in `gradle/libs.versions.toml` were chosen as mutually compatible from knowledge, not resolved online; update them in Android Studio if newer stable versions are preferred.
- First action on your machine: `./gradlew test`, fix anything reported, then update this file.

## Continue with Google Antigravity
1. Open the `MuscleGainTracker` folder as the workspace; let Android Studio/Gradle sync first (JDK 17).
2. Give the agent this instruction: "Read CLAUDE.md, docs/AI_HANDOFF.md and docs/INTEGRATION_CONTRACT.md. Build only the Jetpack Compose UI inside :app. Use MuscleGainContainer via (application as MuscleGainApp).container. Do not touch :core:domain or :core:data and do not recreate any database or calculation logic."
3. Add Compose dependencies and the Kotlin Compose plugin to `app/build.gradle.kts` only (`buildFeatures { compose = true }`).
4. Replace the placeholder `MainActivity` with a `ComponentActivity` and Compose navigation. Suggested screens: Onboarding (profile + goals), Today (DailySummary), Food log, Workout log, Body weight, Weekly summary, Schedule, Supplements, Settings (reminders, backup, CSV export).
5. Create ViewModels that expose `StateFlow` from the contract's Flows and call the suspend functions; surface `ValidationIssue` messages next to the matching `field`.
6. Verify in Google AI Studio: run `./gradlew test` and `./gradlew :app:assembleDebug`, then test on a device or emulator.
7. Backend change requests go back to the backend owner; do not edit the calculation engine in the UI branch.

## Next stages (not started)
Reminder scheduling (frontend), backup file picker (frontend), optional Room migrations when the schema changes.
