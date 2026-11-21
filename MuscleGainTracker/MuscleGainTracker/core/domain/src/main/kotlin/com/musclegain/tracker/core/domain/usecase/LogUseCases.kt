package com.musclegain.tracker.core.domain.usecase

import com.musclegain.tracker.core.domain.calc.WorkoutCalorieEstimator
import com.musclegain.tracker.core.domain.model.ActivityType
import com.musclegain.tracker.core.domain.model.BodyWeightEntry
import com.musclegain.tracker.core.domain.model.CompletionStatus
import com.musclegain.tracker.core.domain.model.Intensity
import com.musclegain.tracker.core.domain.model.UserProfileInput
import com.musclegain.tracker.core.domain.model.WorkoutExercise
import com.musclegain.tracker.core.domain.model.WorkoutSession
import com.musclegain.tracker.core.domain.model.WorkoutSessionWithExercises
import com.musclegain.tracker.core.domain.repository.BodyWeightRepository
import com.musclegain.tracker.core.domain.repository.UserProfileRepository
import com.musclegain.tracker.core.domain.repository.WorkoutRepository
import com.musclegain.tracker.core.domain.validation.SaveOutcome
import com.musclegain.tracker.core.domain.validation.Severity
import com.musclegain.tracker.core.domain.validation.ValidationIssue
import java.time.LocalDate

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
