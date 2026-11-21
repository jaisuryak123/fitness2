package com.musclegain.tracker.core.domain.model

import java.time.LocalDate
import java.time.LocalTime

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
