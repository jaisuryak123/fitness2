# INTEGRATION_CONTRACT

Everything the Compose frontend needs. The frontend must NOT recreate the database, calculations or validation.
All sources below are copied verbatim from `:core:domain`; packages start with `com.musclegain.tracker.core.domain`.

## 1. Dependency setup
- `:app` depends on `:core:domain` and `:core:data` (already configured). Add Compose (Kotlin Compose plugin, Compose BOM, activity-compose, lifecycle-viewmodel-compose, lifecycle-runtime-compose) inside `:app` only.
- Do not add Room, DataStore or calculation code to `:app`.

## 2. Dependency injection
No DI framework. `MuscleGainApp` (in `:app`) owns one `MuscleGainContainer` created in `onCreate`.
```kotlin
val container = (application as MuscleGainApp).container
```
Container members (all `val`):
`userProfileRepository`, `goalsRepository`, `nutritionRepository`, `workoutRepository`, `supplementRepository`, `bodyWeightRepository`, `trainingScheduleRepository`, `reminderRepository`, `appPreferencesRepository`, `backupRepository`,
use cases: `observeDailySummary`, `observeWeeklySummary`, `logWorkout`, `logBodyWeight`.
Create ViewModels with a `ViewModelProvider.Factory` that receives the container.

## 3. Conventions
- Dates are `java.time.LocalDate`, times `java.time.LocalTime`; stored as ISO text in SQLite.
- Observe functions return `Flow` (collect with `collectAsStateWithLifecycle`); write functions are `suspend` (call from `viewModelScope`).
- Save functions return `Result<SaveOutcome>`. On failure `exceptionOrNull()` is a `ValidationException` whose `issues` carry `field`, `code`, `message`. On success `SaveOutcome.warnings` may hold `Severity.WARNING` issues (show them, do not block).
- `id = 0L` means insert; non-zero id means edit. Delete functions return `Boolean`.
- Units: kg, cm, km, minutes, grams, kcal. Nullable numbers mean "not recorded"; display a dash, never 0.
- Calorie estimates are estimates: label them with `WorkoutCalorieEstimator.ESTIMATE_LABEL`.

## 4. Screen to API map
- Dashboard / day view: `observeDailySummary(date): Flow<DailySummary>`
- Weekly view: `observeWeeklySummary(dateInWeek, today = LocalDate.now()): Flow<WeeklySummary>`
- Food log: `NutritionRepository` (`observeEntriesForDate`, `upsert`, `delete`)
- Workout log: `logWorkout(WorkoutInput)` to save (calculates MET calories), `WorkoutRepository` to observe/delete/load for edit
- Body weight: `logBodyWeight(BodyWeightEntry)` (also keeps profile weight current), `BodyWeightRepository` for charts (`observeRecent`, `observeBetween`)
- Settings: profile, goals (new `effectiveDate` creates a new goal version), training schedule (always 7 days), reminders, backup/export
- Backup: `exportJson()` returns a String (frontend writes it to a user-chosen file via the Storage Access Framework); `restoreJson(String)`; `exportCsv(CsvTable)`; `deleteAllData()` (ask the user to confirm)
- Onboarding: `appPreferencesRepository.onboardingCompleted`

## 5. Editing workouts
`WorkoutInput(id = existingId, ...)` replaces the session and its exercises and recalculates calories with the body weight valid for that date. Reading never recalculates, so history does not change silently. Stored per session: `metUsed`, `calculationMethod`, `bodyWeightKgUsed`, `averageSpeedKmh`, `estimatedGrossKcal`, `estimatedActiveKcal`.

## 6. Reminders
Only settings are stored (`ReminderRepository`). Scheduling notifications (WorkManager/AlarmManager, POST_NOTIFICATIONS permission) is a frontend task.

## 7. Source of truth

### model/Enums.kt
```kotlin
package com.musclegain.tracker.core.domain.model

enum class MealCategory { BREAKFAST, LUNCH, DINNER, SNACK }

enum class ActivityType { GYM, RUNNING, WALKING, OTHER_CARDIO }

enum class Intensity { LOW, MODERATE, HIGH }

enum class FitnessGoal { MUSCLE_GAIN, MAINTENANCE }

enum class CompletionStatus { PLANNED, COMPLETED, SKIPPED }

enum class CsvTable { NUTRITION, WORKOUTS, EXERCISES, BODY_WEIGHT, SUPPLEMENTS }
```

### model/Profile.kt
```kotlin
package com.musclegain.tracker.core.domain.model

data class UserProfile(
    val id: Long = 1L,
    val heightCm: Double,
    val weightKg: Double,
    val age: Int? = null,
    val goal: FitnessGoal = FitnessGoal.MUSCLE_GAIN,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

data class UserProfileInput(
    val heightCm: Double,
    val weightKg: Double,
    val age: Int? = null,
    val goal: FitnessGoal = FitnessGoal.MUSCLE_GAIN
)

data class DailyGoals(
    val id: Long = 0L,
    val effectiveDate: LocalDate,
    val calorieTarget: Double,
    val proteinTarget: Double,
    val carbsTarget: Double,
    val fatTarget: Double,
    val weeklyGymTarget: Int = DEFAULT_WEEKLY_GYM_TARGET,
    val weeklyRunningTarget: Int = DEFAULT_WEEKLY_RUNNING_TARGET,
    val runningDistanceTargetKm: Double = DEFAULT_RUNNING_DISTANCE_KM
) {
    companion object {
        const val DEFAULT_WEEKLY_GYM_TARGET = 5
        const val DEFAULT_WEEKLY_RUNNING_TARGET = 2
        const val DEFAULT_RUNNING_DISTANCE_KM = 5.0

        fun defaults(effectiveDate: LocalDate) = DailyGoals(
            effectiveDate = effectiveDate,
            calorieTarget = 2800.0,
            proteinTarget = 140.0,
            carbsTarget = 380.0,
            fatTarget = 80.0
        )
    }
}

fun List<DailyGoals>.effectiveOn(date: LocalDate): DailyGoals =
    filter { !it.effectiveDate.isAfter(date) }.maxByOrNull { it.effectiveDate }
        ?: minByOrNull { it.effectiveDate }
        ?: DailyGoals.defaults(date)

data class TrainingDay(
    val weekday: DayOfWeek,
    val plannedGym: Boolean,
    val plannedRunning: Boolean,
    val plannedRest: Boolean
) {
    companion object {
        fun defaultWeek(): List<TrainingDay> = DayOfWeek.values().map { day ->
            TrainingDay(
                weekday = day,
                plannedGym = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY,
                plannedRunning = day == DayOfWeek.TUESDAY || day == DayOfWeek.SATURDAY,
                plannedRest = day == DayOfWeek.SUNDAY
            )
        }
    }
}

data class Reminder(val enabled: Boolean, val time: LocalTime)

data class ReminderSettings(
    val meal: Reminder = Reminder(false, LocalTime.of(12, 0)),
    val workout: Reminder = Reminder(false, LocalTime.of(18, 0)),
    val bodyWeight: Reminder = Reminder(false, LocalTime.of(7, 0)),
    val supplement: Reminder = Reminder(false, LocalTime.of(9, 0))
)
```

### model/Entries.kt
```kotlin
package com.musclegain.tracker.core.domain.model

data class NutritionEntry(
    val id: Long = 0L,
    val foodName: String,
    val mealCategory: MealCategory,
    val calories: Double,
    val proteinG: Double? = null,
    val carbsG: Double? = null,
    val fatG: Double? = null,
    val date: LocalDate,
    val time: LocalTime,
    val notes: String? = null
)

data class WorkoutSession(
    val id: Long = 0L,
    val type: ActivityType,
    val date: LocalDate,
    val durationMinutes: Int? = null,
    val distanceKm: Double? = null,
    val intensity: Intensity? = null,
    val metUsed: Double? = null,
    val estimatedGrossKcal: Double? = null,
    val estimatedActiveKcal: Double? = null,
    val calculationMethod: String? = null,
    val bodyWeightKgUsed: Double? = null,
    val averageSpeedKmh: Double? = null,
    val notes: String? = null,
    val status: CompletionStatus = CompletionStatus.COMPLETED
)

data class WorkoutExercise(
    val id: Long = 0L,
    val sessionId: Long = 0L,
    val name: String,
    val sets: Int,
    val reps: Int,
    val weightKg: Double
)

data class WorkoutSessionWithExercises(
    val session: WorkoutSession,
    val exercises: List<WorkoutExercise> = emptyList()
)

data class SupplementEntry(
    val id: Long = 0L,
    val name: String,
    val amount: Double,
    val unit: String,
    val date: LocalDate,
    val time: LocalTime,
    val notes: String? = null
)

data class BodyWeightEntry(
    val id: Long = 0L,
    val weightKg: Double,
    val date: LocalDate,
    val notes: String? = null
)
```

### model/Summaries.kt
```kotlin
package com.musclegain.tracker.core.domain.model

data class MacroTotals(
    val calories: Double,
    val proteinG: Double?,
    val carbsG: Double?,
    val fatG: Double?
)

data class MacroRemaining(
    val calories: Double,
    val proteinG: Double?,
    val carbsG: Double?,
    val fatG: Double?
)

data class MacroCompletion(
    val caloriesPercent: Double,
    val proteinPercent: Double?,
    val carbsPercent: Double?,
    val fatPercent: Double?
)

data class DailySummary(
    val date: LocalDate,
    val goals: DailyGoals,
    val nutritionEntryCount: Int,
    val totals: MacroTotals?,
    val remaining: MacroRemaining?,
    val completion: MacroCompletion?,
    val hasIncompleteMacros: Boolean,
    val workouts: List<WorkoutSessionWithExercises>,
    val completedWorkoutCount: Int,
    val estimatedGrossKcal: Double?,
    val estimatedActiveKcal: Double?,
    val plannedDay: TrainingDay?,
    val bodyWeightKg: Double?
)

enum class SessionState { COMPLETED, MISSED, PENDING }

data class PlannedSessionResult(
    val date: LocalDate,
    val type: ActivityType,
    val state: SessionState
)

data class DayAdherence(
    val date: LocalDate,
    val weekday: DayOfWeek,
    val isRestDay: Boolean,
    val plannedSessions: List<PlannedSessionResult>
)

data class WeeklySummary(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val daysWithNutrition: Int,
    val totalCalories: Double?,
    val averageDailyCalories: Double?,
    val averageProteinG: Double?,
    val averageCarbsG: Double?,
    val averageFatG: Double?,
    val proteinTargetDaysAchieved: Int,
    val gymSessionsCompleted: Int,
    val gymSessionsPlanned: Int,
    val runningSessionsCompleted: Int,
    val runningSessionsPlanned: Int,
    val totalRunningDistanceKm: Double,
    val totalWorkoutDurationMinutes: Int,
    val totalStrengthVolumeKg: Double?,
    val estimatedGrossKcal: Double?,
    val estimatedActiveKcal: Double?,
    val weightChangeKg: Double?,
    val trackingCompletionRatePercent: Double,
    val workoutAdherencePercent: Double?,
    val days: List<DayAdherence>
)
```

### validation/Validation.kt
```kotlin
package com.musclegain.tracker.core.domain.validation

enum class Severity { ERROR, WARNING }

data class ValidationIssue(
    val field: String,
    val code: String,
    val message: String,
    val severity: Severity = Severity.ERROR
)

class ValidationException(val issues: List<ValidationIssue>) :
    Exception(issues.joinToString("; ") { it.message })

data class SaveOutcome(
    val id: Long,
    val warnings: List<ValidationIssue> = emptyList()
)
```

### repository/Repositories.kt
```kotlin
package com.musclegain.tracker.core.domain.repository

interface UserProfileRepository {
    fun observeProfile(): Flow<UserProfile?>
    suspend fun getProfile(): UserProfile?
    suspend fun saveProfile(input: UserProfileInput): Result<SaveOutcome>
}

interface GoalsRepository {
    fun observeAllGoals(): Flow<List<DailyGoals>>
    fun observeGoalsFor(date: LocalDate): Flow<DailyGoals>
    suspend fun getGoalsFor(date: LocalDate): DailyGoals
    suspend fun saveGoals(goals: DailyGoals): Result<SaveOutcome>
}

interface NutritionRepository {
    fun observeEntriesForDate(date: LocalDate): Flow<List<NutritionEntry>>
    fun observeEntriesBetween(start: LocalDate, end: LocalDate): Flow<List<NutritionEntry>>
    suspend fun getEntry(id: Long): NutritionEntry?
    suspend fun upsert(entry: NutritionEntry): Result<SaveOutcome>
    suspend fun delete(id: Long): Boolean
}

interface WorkoutRepository {
    fun observeSessionsForDate(date: LocalDate): Flow<List<WorkoutSessionWithExercises>>
    fun observeSessionsBetween(start: LocalDate, end: LocalDate): Flow<List<WorkoutSessionWithExercises>>
    suspend fun getSession(id: Long): WorkoutSessionWithExercises?
    suspend fun saveSession(value: WorkoutSessionWithExercises): Result<SaveOutcome>
    suspend fun deleteSession(id: Long): Boolean
}

interface SupplementRepository {
    fun observeEntriesForDate(date: LocalDate): Flow<List<SupplementEntry>>
    fun observeEntriesBetween(start: LocalDate, end: LocalDate): Flow<List<SupplementEntry>>
    fun observeKnownNames(): Flow<List<String>>
    suspend fun upsert(entry: SupplementEntry): Result<SaveOutcome>
    suspend fun delete(id: Long): Boolean
}

interface BodyWeightRepository {
    fun observeForDate(date: LocalDate): Flow<BodyWeightEntry?>
    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<BodyWeightEntry>>
    fun observeRecent(limit: Int): Flow<List<BodyWeightEntry>>
    suspend fun getLatest(): BodyWeightEntry?
    suspend fun getLatestOnOrBefore(date: LocalDate): BodyWeightEntry?
    suspend fun upsert(entry: BodyWeightEntry): Result<SaveOutcome>
    suspend fun delete(id: Long): Boolean
}

interface TrainingScheduleRepository {
    fun observeSchedule(): Flow<List<TrainingDay>>
    suspend fun saveSchedule(days: List<TrainingDay>): Result<SaveOutcome>
}

interface ReminderRepository {
    fun observeSettings(): Flow<ReminderSettings>
    suspend fun saveSettings(settings: ReminderSettings)
}

interface AppPreferencesRepository {
    val onboardingCompleted: Flow<Boolean>
    val lastBackupAtMillis: Flow<Long?>
    suspend fun setOnboardingCompleted(value: Boolean)
    suspend fun setLastBackupAtMillis(value: Long)
}

interface BackupRepository {
    suspend fun exportJson(): String
    suspend fun restoreJson(json: String): Result<Unit>
    suspend fun exportCsv(table: CsvTable): String
    suspend fun deleteAllData()
}
```

### usecase/LogUseCases.kt
```kotlin
package com.musclegain.tracker.core.domain.usecase

data class ExerciseInput(
    val name: String,
    val sets: Int,
    val reps: Int,
    val weightKg: Double
)

data class WorkoutInput(
    val id: Long = 0L,
    val type: ActivityType,
    val date: LocalDate,
    val durationMinutes: Int? = null,
    val distanceKm: Double? = null,
    val intensity: Intensity? = null,
    val notes: String? = null,
    val status: CompletionStatus = CompletionStatus.COMPLETED,
    val exercises: List<ExerciseInput> = emptyList()
)

class LogWorkoutUseCase(
    private val workouts: WorkoutRepository,
    private val profile: UserProfileRepository,
    private val bodyWeight: BodyWeightRepository
) {
    suspend operator fun invoke(input: WorkoutInput): Result<SaveOutcome> {
        val weight = bodyWeight.getLatestOnOrBefore(input.date)?.weightKg ?: profile.getProfile()?.weightKg
        val completed = input.status == CompletionStatus.COMPLETED
        val estimate = if (completed) {
            WorkoutCalorieEstimator.estimate(input.type, input.durationMinutes, input.distanceKm, input.intensity, weight)
        } else null
        val session = WorkoutSession(
            id = input.id,
            type = input.type,
            date = input.date,
            durationMinutes = input.durationMinutes,
            distanceKm = input.distanceKm,
            intensity = input.intensity,
            metUsed = estimate?.met,
            estimatedGrossKcal = estimate?.grossKcal,
            estimatedActiveKcal = estimate?.activeKcal,
            calculationMethod = estimate?.method,
            bodyWeightKgUsed = estimate?.bodyWeightKg,
            averageSpeedKmh = estimate?.averageSpeedKmh,
            notes = input.notes,
            status = input.status
        )
        val exercises = input.exercises.map {
            WorkoutExercise(sessionId = input.id, name = it.name, sets = it.sets, reps = it.reps, weightKg = it.weightKg)
        }
        val result = workouts.saveSession(WorkoutSessionWithExercises(session, exercises))
        if (weight == null && completed) {
            return result.map {
                it.copy(
                    warnings = it.warnings + ValidationIssue(
                        "bodyWeight", "NO_BODY_WEIGHT", "No body weight recorded, calorie estimate skipped", Severity.WARNING
                    )
                )
            }
        }
        return result
    }
}

class LogBodyWeightUseCase(
    private val bodyWeight: BodyWeightRepository,
    private val profile: UserProfileRepository
) {
    suspend operator fun invoke(entry: BodyWeightEntry): Result<SaveOutcome> {
        val result = bodyWeight.upsert(entry)
        val outcome = result.getOrNull() ?: return result
        val latest = bodyWeight.getLatest()
        val current = profile.getProfile()
        if (latest != null && latest.id == outcome.id && current != null && current.weightKg != latest.weightKg) {
            profile.saveProfile(UserProfileInput(current.heightCm, latest.weightKg, current.age, current.goal))
        }
        return result
    }
}
```

### usecase/SummaryUseCases.kt
```kotlin
package com.musclegain.tracker.core.domain.usecase

class ObserveDailySummaryUseCase(
    private val nutrition: NutritionRepository,
    private val workouts: WorkoutRepository,
    private val goals: GoalsRepository,
    private val schedule: TrainingScheduleRepository,
    private val bodyWeight: BodyWeightRepository
) {
    operator fun invoke(date: LocalDate): Flow<DailySummary> = combine(
        nutrition.observeEntriesForDate(date),
        workouts.observeSessionsForDate(date),
        goals.observeGoalsFor(date),
        schedule.observeSchedule(),
        bodyWeight.observeForDate(date)
    ) { entries, sessions, dayGoals, days, weight ->
        DailySummaryCalculator.build(date, dayGoals, entries, sessions, days, weight?.weightKg)
    }
}

class ObserveWeeklySummaryUseCase(
    private val nutrition: NutritionRepository,
    private val workouts: WorkoutRepository,
    private val goals: GoalsRepository,
    private val schedule: TrainingScheduleRepository,
    private val bodyWeight: BodyWeightRepository
) {
    operator fun invoke(dateInWeek: LocalDate, today: LocalDate = LocalDate.now()): Flow<WeeklySummary> {
        val start = Weeks.startOf(dateInWeek)
        val end = Weeks.endOf(dateInWeek)
        return combine(
            nutrition.observeEntriesBetween(start, end),
            workouts.observeSessionsBetween(start, end),
            goals.observeAllGoals(),
            schedule.observeSchedule(),
            bodyWeight.observeBetween(start, end)
        ) { entries, sessions, timeline, days, weights ->
            WeeklySummaryCalculator.build(start, today, entries, sessions, timeline, days, weights)
        }
    }
}
```

### Validation codes
ERROR: INVALID_HEIGHT, INVALID_WEIGHT, INVALID_CALORIES, INVALID_MACRO, INVALID_DISTANCE, INVALID_DURATION, INVALID_EXERCISES, INVALID_SCHEDULE, CONFLICTING_PLAN, REQUIRED_FIELD, OUT_OF_RANGE, DUPLICATE_RECORD, NOT_FOUND.

WARNING: MACRO_CALORIE_MISMATCH, MISSING_DURATION, MISSING_DISTANCE, UNREALISTIC_SPEED, NO_BODY_WEIGHT.
