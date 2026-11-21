package com.musclegain.tracker.core.domain

import com.musclegain.tracker.core.domain.TestData.monday
import com.musclegain.tracker.core.domain.calc.WeeklySummaryCalculator
import com.musclegain.tracker.core.domain.calc.Weeks
import com.musclegain.tracker.core.domain.model.ActivityType
import com.musclegain.tracker.core.domain.model.BodyWeightEntry
import com.musclegain.tracker.core.domain.model.CompletionStatus
import com.musclegain.tracker.core.domain.model.DailyGoals
import com.musclegain.tracker.core.domain.model.NutritionEntry
import com.musclegain.tracker.core.domain.model.SessionState
import com.musclegain.tracker.core.domain.model.TrainingDay
import com.musclegain.tracker.core.domain.model.WorkoutExercise
import com.musclegain.tracker.core.domain.model.WorkoutSessionWithExercises
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeklySummaryCalculatorTest {
    private val schedule = TrainingDay.defaultWeek()
    private val goals = listOf(DailyGoals.defaults(monday))

    private fun build(
        today: LocalDate,
        nutrition: List<NutritionEntry> = emptyList(),
        workouts: List<WorkoutSessionWithExercises> = emptyList(),
        weights: List<BodyWeightEntry> = emptyList()
    ) = WeeklySummaryCalculator.build(monday, today, nutrition, workouts, goals, schedule, weights)

    @Test
    fun averagesUseOnlyTrackedDays() {
        val summary = build(
            today = monday.plusDays(4),
            nutrition = listOf(
                TestData.nutrition(monday, 2000.0, protein = 120.0),
                TestData.nutrition(monday.plusDays(2), 3000.0, protein = 160.0)
            )
        )
        assertEquals(2, summary.daysWithNutrition)
        assertEquals(5000.0, summary.totalCalories!!, 0.001)
        assertEquals(2500.0, summary.averageDailyCalories!!, 0.001)
        assertEquals(140.0, summary.averageProteinG!!, 0.001)
        assertNull(summary.averageCarbsG)
        assertEquals(1, summary.proteinTargetDaysAchieved)
        assertEquals(40.0, summary.trackingCompletionRatePercent, 0.001)
    }

    @Test
    fun adherenceCountsMissedSessionsButNotRestDays() {
        val summary = build(
            today = monday.plusDays(6),
            workouts = listOf(
                TestData.session(monday, ActivityType.GYM),
                TestData.session(monday.plusDays(1), ActivityType.GYM),
                TestData.session(monday.plusDays(1), ActivityType.RUNNING),
                TestData.session(monday.plusDays(3), ActivityType.GYM)
            )
        )
        assertEquals(3, summary.gymSessionsCompleted)
        assertEquals(5, summary.gymSessionsPlanned)
        assertEquals(1, summary.runningSessionsCompleted)
        assertEquals(2, summary.runningSessionsPlanned)
        assertEquals(57.1, summary.workoutAdherencePercent!!, 0.05)
        assertTrue(summary.days.last().isRestDay)
        assertTrue(summary.days.last().plannedSessions.isEmpty())
        assertEquals(180, summary.totalWorkoutDurationMinutes)
    }

    @Test
    fun todaysUnfinishedSessionIsPendingNotMissed() {
        val summary = build(
            today = monday.plusDays(2),
            workouts = listOf(
                TestData.session(monday, ActivityType.GYM),
                TestData.session(monday.plusDays(1), ActivityType.GYM)
            )
        )
        val wednesday = summary.days[2].plannedSessions.single()
        assertEquals(SessionState.PENDING, wednesday.state)
        assertEquals(66.7, summary.workoutAdherencePercent!!, 0.05)
    }

    @Test
    fun skippedAndPlannedSessionsDoNotCountAsCompleted() {
        val summary = build(
            today = monday.plusDays(6),
            workouts = listOf(
                TestData.session(monday, ActivityType.GYM, CompletionStatus.SKIPPED),
                TestData.session(monday.plusDays(1), ActivityType.GYM, CompletionStatus.PLANNED)
            )
        )
        assertEquals(0, summary.gymSessionsCompleted)
        assertEquals(0.0, summary.workoutAdherencePercent!!, 0.001)
    }

    @Test
    fun emptyFutureWeekKeepsMissingValuesNull() {
        val summary = build(today = monday.minusDays(3))
        assertNull(summary.averageDailyCalories)
        assertNull(summary.totalCalories)
        assertNull(summary.weightChangeKg)
        assertNull(summary.estimatedGrossKcal)
        assertNull(summary.workoutAdherencePercent)
        assertEquals(0.0, summary.trackingCompletionRatePercent, 0.001)
        assertFalse(summary.days.any { day -> day.plannedSessions.any { it.state == SessionState.MISSED } })
    }

    @Test
    fun weekBoundariesAreMondayToSunday() {
        assertEquals(monday, Weeks.startOf(monday.plusDays(6)))
        assertEquals(monday.plusDays(7), Weeks.startOf(monday.plusDays(7)))
        val summary = build(
            today = monday.plusDays(6),
            nutrition = listOf(
                TestData.nutrition(monday.minusDays(1), 1000.0),
                TestData.nutrition(monday.plusDays(6), 2000.0),
                TestData.nutrition(monday.plusDays(7), 3000.0)
            )
        )
        assertEquals(1, summary.daysWithNutrition)
        assertEquals(2000.0, summary.totalCalories!!, 0.001)
    }

    @Test
    fun weightChangeAndStrengthVolume() {
        val exercises = listOf(WorkoutExercise(name = "Squat", sets = 3, reps = 5, weightKg = 100.0))
        val summary = build(
            today = monday.plusDays(6),
            workouts = listOf(TestData.session(monday, ActivityType.GYM, exercises = exercises)),
            weights = listOf(TestData.weight(monday, 75.0), TestData.weight(monday.plusDays(6), 75.6))
        )
        assertEquals(0.6, summary.weightChangeKg!!, 0.001)
        assertEquals(1500.0, summary.totalStrengthVolumeKg!!, 0.001)
    }

    @Test
    fun singleWeightMeasurementGivesNoChange() {
        val summary = build(today = monday.plusDays(6), weights = listOf(TestData.weight(monday, 75.0)))
        assertNull(summary.weightChangeKg)
    }
}
