# PROJECT_SPEC

Personal, offline-first Android app for MUSCLE GAIN. Native Kotlin, Room (SQLite), Coroutines/Flow, DataStore, Gradle Kotlin DSL, JUnit. No login, cloud, ads, analytics or network access.

Ownership
- Backend and shared foundation (`:core:domain`, `:core:data`, minimal `:app` shell): this repository, done.
- Frontend (Jetpack Compose): built separately in Google Antigravity, verified in Google AI Studio.

Scope of this stage
- Entities: UserProfile, DailyGoals, NutritionEntry, WorkoutSession, WorkoutExercise, SupplementEntry, BodyWeightEntry, TrainingSchedule, ReminderSettings.
- Calculation engine: nutrition totals, workout MET calories, daily and Monday-Sunday weekly summaries, adherence.
- Versioned JSON backup/restore, CSV export, validation, unit tests.

Non-negotiable rules
- Targets are user-editable; missed workouts never change calorie targets; exercise calories are never added to the food allowance.
- Missing data stays missing (null), never zero. Rest days are not missed sessions.
- User-entered calories are authoritative; macro mismatch is only a warning.
- Supplements are tracked only; no dose advice. Height is never used in MET calculations.
