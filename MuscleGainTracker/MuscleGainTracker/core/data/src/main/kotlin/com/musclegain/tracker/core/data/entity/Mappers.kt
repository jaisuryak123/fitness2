package com.musclegain.tracker.core.data.entity

import com.musclegain.tracker.core.domain.model.ActivityType
import com.musclegain.tracker.core.domain.model.BodyWeightEntry
import com.musclegain.tracker.core.domain.model.CompletionStatus
import com.musclegain.tracker.core.domain.model.DailyGoals
import com.musclegain.tracker.core.domain.model.FitnessGoal
import com.musclegain.tracker.core.domain.model.Intensity
import com.musclegain.tracker.core.domain.model.MealCategory
import com.musclegain.tracker.core.domain.model.NutritionEntry
import com.musclegain.tracker.core.domain.model.Reminder
import com.musclegain.tracker.core.domain.model.ReminderSettings
import com.musclegain.tracker.core.domain.model.SupplementEntry
import com.musclegain.tracker.core.domain.model.TrainingDay
import com.musclegain.tracker.core.domain.model.UserProfile
import com.musclegain.tracker.core.domain.model.WorkoutExercise
import com.musclegain.tracker.core.domain.model.WorkoutSession
import com.musclegain.tracker.core.domain.model.WorkoutSessionWithExercises
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

internal fun LocalDate.toDb(): String = toString()

internal fun LocalTime.toDb(): String = format(timeFormat)

internal fun UserProfileEntity.toDomain() =
    UserProfile(id, heightCm, weightKg, age, FitnessGoal.valueOf(goal), createdAtMillis, updatedAtMillis)

internal fun DailyGoalsEntity.toDomain() = DailyGoals(
    id, LocalDate.parse(effectiveDate), calorieTarget, proteinTarget, carbsTarget, fatTarget,
    weeklyGymTarget, weeklyRunningTarget, runningDistanceTargetKm
)

internal fun DailyGoals.toEntity() = DailyGoalsEntity(
    id, effectiveDate.toDb(), calorieTarget, proteinTarget, carbsTarget, fatTarget,
    weeklyGymTarget, weeklyRunningTarget, runningDistanceTargetKm
)

internal fun NutritionEntryEntity.toDomain() = NutritionEntry(
    id, foodName, MealCategory.valueOf(mealCategory), calories, proteinG, carbsG, fatG,
    LocalDate.parse(entryDate), LocalTime.parse(entryTime), notes
)

internal fun NutritionEntry.toEntity() = NutritionEntryEntity(
    id, foodName.trim(), mealCategory.name, calories, proteinG, carbsG, fatG,
    date.toDb(), time.toDb(), notes
)

internal fun WorkoutSessionEntity.toDomain() = WorkoutSession(
    id, ActivityType.valueOf(activityType), LocalDate.parse(activityDate), durationMinutes, distanceKm,
    intensity?.let { Intensity.valueOf(it) }, metUsed, estimatedGrossKcal, estimatedActiveKcal,
    calculationMethod, bodyWeightKgUsed, averageSpeedKmh, notes, CompletionStatus.valueOf(status)
)

internal fun WorkoutSession.toEntity() = WorkoutSessionEntity(
    id, type.name, date.toDb(), durationMinutes, distanceKm, intensity?.name, metUsed,
    estimatedGrossKcal, estimatedActiveKcal, calculationMethod, bodyWeightKgUsed, averageSpeedKmh,
    notes, status.name
)

internal fun WorkoutExerciseEntity.toDomain() = WorkoutExercise(id, sessionId, name, sets, reps, weightKg)

internal fun WorkoutExercise.toEntity() = WorkoutExerciseEntity(id, sessionId, name.trim(), sets, reps, weightKg)

internal fun WorkoutSessionRelation.toDomain() = WorkoutSessionWithExercises(
    session.toDomain(),
    exercises.sortedBy { it.id }.map { it.toDomain() }
)

internal fun SupplementEntryEntity.toDomain() =
    SupplementEntry(id, name, amount, unit, LocalDate.parse(entryDate), LocalTime.parse(entryTime), notes)

internal fun SupplementEntry.toEntity() =
    SupplementEntryEntity(id, name.trim(), amount, unit.trim(), date.toDb(), time.toDb(), notes)

internal fun BodyWeightEntryEntity.toDomain() = BodyWeightEntry(id, weightKg, LocalDate.parse(recordedDate), notes)

internal fun BodyWeightEntry.toEntity() = BodyWeightEntryEntity(id, weightKg, date.toDb(), notes)

internal fun TrainingDayEntity.toDomain() =
    TrainingDay(DayOfWeek.of(weekday), plannedGym, plannedRunning, plannedRest)

internal fun TrainingDay.toEntity() = TrainingDayEntity(weekday.value, plannedGym, plannedRunning, plannedRest)

internal fun ReminderSettingsEntity.toDomain() = ReminderSettings(
    meal = Reminder(mealEnabled, LocalTime.parse(mealTime)),
    workout = Reminder(workoutEnabled, LocalTime.parse(workoutTime)),
    bodyWeight = Reminder(bodyWeightEnabled, LocalTime.parse(bodyWeightTime)),
    supplement = Reminder(supplementEnabled, LocalTime.parse(supplementTime))
)

internal fun ReminderSettings.toEntity() = ReminderSettingsEntity(
    1L,
    meal.enabled, meal.time.toDb(),
    workout.enabled, workout.time.toDb(),
    bodyWeight.enabled, bodyWeight.time.toDb(),
    supplement.enabled, supplement.time.toDb()
)
