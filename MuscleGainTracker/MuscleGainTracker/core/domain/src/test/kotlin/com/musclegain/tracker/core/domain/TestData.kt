package com.musclegain.tracker.core.domain

import com.musclegain.tracker.core.domain.model.ActivityType
import com.musclegain.tracker.core.domain.model.BodyWeightEntry
import com.musclegain.tracker.core.domain.model.CompletionStatus
import com.musclegain.tracker.core.domain.model.MealCategory
import com.musclegain.tracker.core.domain.model.NutritionEntry
import com.musclegain.tracker.core.domain.model.WorkoutExercise
import com.musclegain.tracker.core.domain.model.WorkoutSession
import com.musclegain.tracker.core.domain.model.WorkoutSessionWithExercises
import java.time.LocalDate
import java.time.LocalTime

object TestData {
    val monday: LocalDate = LocalDate.of(2026, 11, 16)

    fun nutrition(
        date: LocalDate,
        calories: Double,
        protein: Double? = null,
        carbs: Double? = null,
        fat: Double? = null,
        name: String = "Food"
    ) = NutritionEntry(
        foodName = name,
        mealCategory = MealCategory.LUNCH,
        calories = calories,
        proteinG = protein,
        carbsG = carbs,
        fatG = fat,
        date = date,
        time = LocalTime.NOON
    )

    fun session(
        date: LocalDate,
        type: ActivityType,
        status: CompletionStatus = CompletionStatus.COMPLETED,
        exercises: List<WorkoutExercise> = emptyList()
    ) = WorkoutSessionWithExercises(
        WorkoutSession(type = type, date = date, durationMinutes = 45, status = status),
        exercises
    )

    fun weight(date: LocalDate, kg: Double) = BodyWeightEntry(weightKg = kg, date = date)
}
