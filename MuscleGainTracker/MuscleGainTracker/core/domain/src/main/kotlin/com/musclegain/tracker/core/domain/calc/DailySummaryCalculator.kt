package com.musclegain.tracker.core.domain.calc

import com.musclegain.tracker.core.domain.model.CompletionStatus
import com.musclegain.tracker.core.domain.model.DailyGoals
import com.musclegain.tracker.core.domain.model.DailySummary
import com.musclegain.tracker.core.domain.model.NutritionEntry
import com.musclegain.tracker.core.domain.model.TrainingDay
import com.musclegain.tracker.core.domain.model.WorkoutSessionWithExercises
import java.time.LocalDate

object DailySummaryCalculator {
    fun build(
        date: LocalDate,
        goals: DailyGoals,
        nutrition: List<NutritionEntry>,
        workouts: List<WorkoutSessionWithExercises>,
        schedule: List<TrainingDay>,
        bodyWeightKg: Double?
    ): DailySummary {
        val totals = NutritionCalculator.totals(nutrition)
        val completed = workouts.filter { it.session.status == CompletionStatus.COMPLETED }
        return DailySummary(
            date = date,
            goals = goals,
            nutritionEntryCount = nutrition.size,
            totals = totals,
            remaining = totals?.let { NutritionCalculator.remaining(it, goals) },
            completion = totals?.let { NutritionCalculator.completion(it, goals) },
            hasIncompleteMacros = NutritionCalculator.hasIncompleteMacros(nutrition),
            workouts = workouts,
            completedWorkoutCount = completed.size,
            estimatedGrossKcal = sumKnown(completed.map { it.session.estimatedGrossKcal }),
            estimatedActiveKcal = sumKnown(completed.map { it.session.estimatedActiveKcal }),
            plannedDay = schedule.firstOrNull { it.weekday == date.dayOfWeek },
            bodyWeightKg = bodyWeightKg
        )
    }
}
