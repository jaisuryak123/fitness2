package com.musclegain.tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musclegain.tracker.core.domain.model.DailyGoals
import com.musclegain.tracker.core.domain.model.DayOfWeek
import com.musclegain.tracker.core.domain.model.TrainingDay
import com.musclegain.tracker.core.domain.repository.GoalsRepository
import com.musclegain.tracker.core.domain.repository.TrainingScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@Suppress("MemberVisibilityCanBePrivate")
class TrainingPlanViewModel(
    private val trainingScheduleRepository: TrainingScheduleRepository,
    private val goalsRepository: GoalsRepository
) : ViewModel() {

    val schedule: StateFlow<List<TrainingDay>> = trainingScheduleRepository.observeSchedule()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TrainingDay.defaultWeek()
        )

    val goals: StateFlow<DailyGoals> = goalsRepository.observeGoalsFor(LocalDate.now())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailyGoals.defaults(LocalDate.now())
        )

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun updateDayPlan(
        weekday: DayOfWeek,
        plannedGym: Boolean,
        plannedRunning: Boolean,
        plannedRest: Boolean
    ) {
        val current = schedule.value.toMutableList()
        val index = current.indexOfFirst { it.weekday == weekday }
        if (index != -1) {
            val updated = if (plannedRest) {
                // If rest is true, gym and running cannot be true
                TrainingDay(weekday, plannedGym = false, plannedRunning = false, plannedRest = true)
            } else {
                TrainingDay(weekday, plannedGym = plannedGym, plannedRunning = plannedRunning, plannedRest = false)
            }
            current[index] = updated
            saveSchedule(current)
        }
    }

    private fun saveSchedule(days: List<TrainingDay>) {
        viewModelScope.launch {
            val result = trainingScheduleRepository.saveSchedule(days)
            result.fold(
                onSuccess = {
                    _statusMessage.value = "Schedule updated"
                    _errorMessage.value = null
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Failed to update schedule"
                }
            )
        }
    }

    fun updateGoals(
        gymSessionsTarget: Int,
        runningSessionsTarget: Int,
        runningDistanceKm: Double
    ) {
        viewModelScope.launch {
            val currentGoals = goals.value
            val updated = currentGoals.copy(
                effectiveDate = LocalDate.now(),
                weeklyGymTarget = gymSessionsTarget,
                weeklyRunningTarget = runningSessionsTarget,
                runningDistanceTargetKm = runningDistanceKm
            )
            val result = goalsRepository.saveGoals(updated)
            result.fold(
                onSuccess = {
                    _statusMessage.value = "Targets updated"
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Failed to update targets"
                }
            )
        }
    }

    fun clearMessages() {
        _statusMessage.value = null
        _errorMessage.value = null
    }
}
