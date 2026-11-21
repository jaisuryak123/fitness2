package com.musclegain.tracker.core.domain.validation

import com.musclegain.tracker.core.domain.calc.NutritionCalculator
import com.musclegain.tracker.core.domain.model.ActivityType
import com.musclegain.tracker.core.domain.model.CompletionStatus
import com.musclegain.tracker.core.domain.model.DailyGoals
import com.musclegain.tracker.core.domain.model.NutritionEntry
import com.musclegain.tracker.core.domain.model.SupplementEntry
import com.musclegain.tracker.core.domain.model.TrainingDay
import com.musclegain.tracker.core.domain.model.UserProfileInput
import com.musclegain.tracker.core.domain.model.WorkoutExercise
import com.musclegain.tracker.core.domain.model.WorkoutSession
import kotlin.math.abs
import kotlin.math.max

object Validators {
    private fun err(field: String, code: String, message: String) =
        ValidationIssue(field, code, message, Severity.ERROR)

    private fun warn(field: String, code: String, message: String) =
        ValidationIssue(field, code, message, Severity.WARNING)

    fun profile(input: UserProfileInput): List<ValidationIssue> = buildList {
        if (input.heightCm !in 50.0..272.0) add(err("heightCm", "INVALID_HEIGHT", "Height must be 50-272 cm"))
        if (input.weightKg !in 20.0..400.0) add(err("weightKg", "INVALID_WEIGHT", "Weight must be 20-400 kg"))
        val age = input.age
        if (age != null && age !in 5..120) add(err("age", "OUT_OF_RANGE", "Age must be 5-120"))
    }

    fun goals(goals: DailyGoals): List<ValidationIssue> = buildList {
        if (goals.calorieTarget !in 500.0..10000.0) add(err("calorieTarget", "INVALID_CALORIES", "Calorie target must be 500-10000"))
        listOf(
            "proteinTarget" to goals.proteinTarget,
            "carbsTarget" to goals.carbsTarget,
            "fatTarget" to goals.fatTarget
        ).forEach { (field, value) ->
            if (value !in 0.0..2000.0) add(err(field, "INVALID_MACRO", "$field must be 0-2000 g"))
        }
        if (goals.weeklyGymTarget !in 0..7) add(err("weeklyGymTarget", "OUT_OF_RANGE", "Gym target must be 0-7"))
        if (goals.weeklyRunningTarget !in 0..7) add(err("weeklyRunningTarget", "OUT_OF_RANGE", "Running target must be 0-7"))
        if (goals.runningDistanceTargetKm !in 0.0..500.0) add(err("runningDistanceTargetKm", "INVALID_DISTANCE", "Distance target must be 0-500 km"))
    }

    fun nutrition(entry: NutritionEntry): List<ValidationIssue> = buildList {
        if (entry.foodName.isBlank()) add(err("foodName", "REQUIRED_FIELD", "Food name is required"))
        if (entry.calories !in 0.0..20000.0) add(err("calories", "INVALID_CALORIES", "Calories must be 0-20000"))
        listOf(
            "proteinG" to entry.proteinG,
            "carbsG" to entry.carbsG,
            "fatG" to entry.fatG
        ).forEach { (field, value) ->
            if (value != null && value !in 0.0..2000.0) add(err(field, "INVALID_MACRO", "$field must be 0-2000 g"))
        }
        val implied = NutritionCalculator.impliedCalories(entry)
        if (implied != null && abs(implied - entry.calories) > max(50.0, entry.calories * 0.2)) {
            add(warn("calories", "MACRO_CALORIE_MISMATCH", "Calories differ noticeably from the energy implied by the macros"))
        }
    }

    fun workout(session: WorkoutSession, exercises: List<WorkoutExercise>): List<ValidationIssue> = buildList {
        val minutes = session.durationMinutes
        val km = session.distanceKm
        if (minutes != null && minutes !in 1..1440) add(err("durationMinutes", "INVALID_DURATION", "Duration must be 1-1440 minutes"))
        if (km != null && km !in 0.01..500.0) add(err("distanceKm", "INVALID_DISTANCE", "Distance must be 0.01-500 km"))
        val pace = session.type == ActivityType.RUNNING || session.type == ActivityType.WALKING
        if (pace && session.status == CompletionStatus.COMPLETED) {
            if (minutes == null) add(warn("durationMinutes", "MISSING_DURATION", "Duration missing, calorie estimate skipped"))
            if (km == null) add(warn("distanceKm", "MISSING_DISTANCE", "Distance missing, calorie estimate skipped"))
            if (minutes != null && km != null && minutes in 1..1440 && km in 0.01..500.0) {
                val speed = km / (minutes / 60.0)
                val limit = if (session.type == ActivityType.RUNNING) 30.0 else 10.0
                if (speed > limit) add(warn("distanceKm", "UNREALISTIC_SPEED", "Average speed looks unrealistic"))
            }
        }
        if (exercises.isNotEmpty() && session.type != ActivityType.GYM) {
            add(err("exercises", "INVALID_EXERCISES", "Exercises are only allowed on gym sessions"))
        }
        exercises.forEachIndexed { index, exercise ->
            val prefix = "exercises[$index]"
            if (exercise.name.isBlank()) add(err("$prefix.name", "REQUIRED_FIELD", "Exercise name is required"))
            if (exercise.sets !in 1..100) add(err("$prefix.sets", "OUT_OF_RANGE", "Sets must be 1-100"))
            if (exercise.reps !in 1..1000) add(err("$prefix.reps", "OUT_OF_RANGE", "Reps must be 1-1000"))
            if (exercise.weightKg !in 0.0..1000.0) add(err("$prefix.weightKg", "INVALID_WEIGHT", "Weight must be 0-1000 kg"))
        }
    }

    fun supplement(entry: SupplementEntry): List<ValidationIssue> = buildList {
        if (entry.name.isBlank()) add(err("name", "REQUIRED_FIELD", "Name is required"))
        if (entry.unit.isBlank()) add(err("unit", "REQUIRED_FIELD", "Unit is required"))
        if (entry.amount !in 0.0001..1000000.0) add(err("amount", "OUT_OF_RANGE", "Amount must be greater than 0"))
    }

    fun bodyWeight(weightKg: Double): List<ValidationIssue> = buildList {
        if (weightKg !in 20.0..400.0) add(err("weightKg", "INVALID_WEIGHT", "Weight must be 20-400 kg"))
    }

    fun schedule(days: List<TrainingDay>): List<ValidationIssue> = buildList {
        if (days.map { it.weekday }.toSet().size != 7 || days.size != 7) {
            add(err("days", "INVALID_SCHEDULE", "Schedule must contain exactly one entry for each weekday"))
        }
        days.filter { it.plannedRest && (it.plannedGym || it.plannedRunning) }.forEach {
            add(err("days.${it.weekday}", "CONFLICTING_PLAN", "A rest day cannot also have a planned session"))
        }
    }
}
