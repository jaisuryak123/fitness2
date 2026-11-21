package com.musclegain.tracker.core.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

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
