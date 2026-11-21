package com.musclegain.tracker.core.domain.calc

import com.musclegain.tracker.core.domain.model.ActivityType
import com.musclegain.tracker.core.domain.model.BodyWeightEntry
import com.musclegain.tracker.core.domain.model.CompletionStatus
import com.musclegain.tracker.core.domain.model.DailyGoals
import com.musclegain.tracker.core.domain.model.DayAdherence
import com.musclegain.tracker.core.domain.model.MacroTotals
import com.musclegain.tracker.core.domain.model.NutritionEntry
import com.musclegain.tracker.core.domain.model.PlannedSessionResult
import com.musclegain.tracker.core.domain.model.SessionState
import com.musclegain.tracker.core.domain.model.TrainingDay
import com.musclegain.tracker.core.domain.model.WeeklySummary
import com.musclegain.tracker.core.domain.model.WorkoutSessionWithExercises
import com.musclegain.tracker.core.domain.model.effectiveOn
import java.time.LocalDate

object WeeklySummaryCalculator {
    fun build(
        weekStart: LocalDate,
        today: LocalDate,
        nutrition: List<NutritionEntry>,
        workouts: List<WorkoutSessionWithExercises>,
        goalsTimeline: List<DailyGoals>,
        schedule: List<TrainingDay>,
        weights: List<BodyWeightEntry>
    ): WeeklySummary {
        val dates = Weeks.datesOf(weekStart)
        val weekEnd = dates.last()

        val nutritionByDate = nutrition.filter { it.date in weekStart..weekEnd }.groupBy { it.date }
        val dailyTotals: List<Pair<LocalDate, MacroTotals>> = dates.mapNotNull { date ->
            nutritionByDate[date]?.let { entries ->
                NutritionCalculator.totals(entries)?.let { date to it }
            }
        }

        val proteinDays = dailyTotals.count { (date, totals) ->
            totals.proteinG != null && totals.proteinG >= goalsTimeline.effectiveOn(date).proteinTarget
        }

        val completed = workouts.filter {
            it.session.status == CompletionStatus.COMPLETED && it.session.date in weekStart..weekEnd
        }
        val completedTypesByDate = completed.groupBy({ it.session.date }, { it.session.type })
        val planByDay = schedule.associateBy { it.weekday }

        val days = dates.map { date ->
            val plan = planByDay[date.dayOfWeek]
            val plannedTypes = buildList {
                if (plan?.plannedGym == true) add(ActivityType.GYM)
                if (plan?.plannedRunning == true) add(ActivityType.RUNNING)
            }
            val doneTypes = completedTypesByDate[date].orEmpty().toSet()
            DayAdherence(
                date = date,
                weekday = date.dayOfWeek,
                isRestDay = plan?.plannedRest == true && plannedTypes.isEmpty(),
                plannedSessions = plannedTypes.map { type ->
                    val state = when {
                        type in doneTypes -> SessionState.COMPLETED
                        date.isBefore(today) -> SessionState.MISSED
                        else -> SessionState.PENDING
                    }
                    PlannedSessionResult(date, type, state)
                }
            )
        }

        val planned = days.flatMap { it.plannedSessions }
        val completedPlanned = planned.count { it.state == SessionState.COMPLETED }
        val missedPlanned = planned.count { it.state == SessionState.MISSED }
        val dueSessions = completedPlanned + missedPlanned

        val weekWeights = weights.filter { it.date in weekStart..weekEnd }.sortedBy { it.date }
        val elapsedDays = dates.count { !it.isAfter(today) }
        val trackedDays = dailyTotals.count { !it.first.isAfter(today) }
        val gym = completed.filter { it.session.type == ActivityType.GYM }
        val running = completed.filter { it.session.type == ActivityType.RUNNING }

        return WeeklySummary(
            weekStart = weekStart,
            weekEnd = weekEnd,
            daysWithNutrition = dailyTotals.size,
            totalCalories = sumKnown(dailyTotals.map { it.second.calories }),
            averageDailyCalories = average(dailyTotals.map { it.second.calories }),
            averageProteinG = average(dailyTotals.mapNotNull { it.second.proteinG }),
            averageCarbsG = average(dailyTotals.mapNotNull { it.second.carbsG }),
            averageFatG = average(dailyTotals.mapNotNull { it.second.fatG }),
            proteinTargetDaysAchieved = proteinDays,
            gymSessionsCompleted = gym.size,
            gymSessionsPlanned = planned.count { it.type == ActivityType.GYM },
            runningSessionsCompleted = running.size,
            runningSessionsPlanned = planned.count { it.type == ActivityType.RUNNING },
            totalRunningDistanceKm = sumKnown(running.map { it.session.distanceKm }) ?: 0.0,
            totalWorkoutDurationMinutes = completed.sumOf { it.session.durationMinutes ?: 0 },
            totalStrengthVolumeKg = sumKnown(
                gym.flatMap { it.exercises }.map { it.sets * it.reps * it.weightKg }
            ),
            estimatedGrossKcal = sumKnown(completed.map { it.session.estimatedGrossKcal }),
            estimatedActiveKcal = sumKnown(completed.map { it.session.estimatedActiveKcal }),
            weightChangeKg = if (weekWeights.size >= 2) {
                (weekWeights.last().weightKg - weekWeights.first().weightKg).roundTo(2)
            } else null,
            trackingCompletionRatePercent =
                if (elapsedDays == 0) 0.0 else (trackedDays * 100.0 / elapsedDays).roundTo(1),
            workoutAdherencePercent =
                if (dueSessions == 0) null else (completedPlanned * 100.0 / dueSessions).roundTo(1),
            days = days
        )
    }

    private fun average(values: List<Double>): Double? {
        val total = sumKnown(values) ?: return null
        return (total / values.size).roundTo(1)
    }
}
