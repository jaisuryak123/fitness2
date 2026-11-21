package com.musclegain.tracker.core.domain.usecase

import com.musclegain.tracker.core.domain.calc.DailySummaryCalculator
import com.musclegain.tracker.core.domain.calc.WeeklySummaryCalculator
import com.musclegain.tracker.core.domain.calc.Weeks
import com.musclegain.tracker.core.domain.model.DailySummary
import com.musclegain.tracker.core.domain.model.WeeklySummary
import com.musclegain.tracker.core.domain.repository.BodyWeightRepository
import com.musclegain.tracker.core.domain.repository.GoalsRepository
import com.musclegain.tracker.core.domain.repository.NutritionRepository
import com.musclegain.tracker.core.domain.repository.TrainingScheduleRepository
import com.musclegain.tracker.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

class ObserveDailySummaryUseCase(
    private val nutrition: NutritionRepository,
    private val workouts: WorkoutRepository,
    private val goals: GoalsRepository,
    private val schedule: TrainingScheduleRepository,
    private val bodyWeight: BodyWeightRepository
) {
    operator fun invoke(date: LocalDate): Flow<DailySummary> = combine(
        nutrition.observeEntriesForDate(date),
        workouts.observeSessionsForDate(date),
        goals.observeGoalsFor(date),
        schedule.observeSchedule(),
        bodyWeight.observeForDate(date)
    ) { entries, sessions, dayGoals, days, weight ->
        DailySummaryCalculator.build(date, dayGoals, entries, sessions, days, weight?.weightKg)
    }
}

class ObserveWeeklySummaryUseCase(
    private val nutrition: NutritionRepository,
    private val workouts: WorkoutRepository,
    private val goals: GoalsRepository,
    private val schedule: TrainingScheduleRepository,
    private val bodyWeight: BodyWeightRepository
) {
    operator fun invoke(dateInWeek: LocalDate, today: LocalDate = LocalDate.now()): Flow<WeeklySummary> {
        val start = Weeks.startOf(dateInWeek)
        val end = Weeks.endOf(dateInWeek)
        return combine(
            nutrition.observeEntriesBetween(start, end),
            workouts.observeSessionsBetween(start, end),
            goals.observeAllGoals(),
            schedule.observeSchedule(),
            bodyWeight.observeBetween(start, end)
        ) { entries, sessions, timeline, days, weights ->
            WeeklySummaryCalculator.build(start, today, entries, sessions, timeline, days, weights)
        }
    }
}
