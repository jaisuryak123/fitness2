package com.musclegain.tracker.core.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

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
