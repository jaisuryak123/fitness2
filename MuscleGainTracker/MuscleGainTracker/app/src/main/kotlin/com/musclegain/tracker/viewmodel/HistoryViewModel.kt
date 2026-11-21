package com.musclegain.tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musclegain.tracker.core.domain.model.BodyWeightEntry
import com.musclegain.tracker.core.domain.model.DailySummary
import com.musclegain.tracker.core.domain.model.NutritionEntry
import com.musclegain.tracker.core.domain.model.SupplementEntry
import com.musclegain.tracker.core.domain.model.WorkoutSessionWithExercises
import com.musclegain.tracker.core.domain.repository.BodyWeightRepository
import com.musclegain.tracker.core.domain.repository.NutritionRepository
import com.musclegain.tracker.core.domain.repository.SupplementRepository
import com.musclegain.tracker.core.domain.repository.WorkoutRepository
import com.musclegain.tracker.core.domain.usecase.ObserveDailySummaryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val observeDailySummary: ObserveDailySummaryUseCase,
    private val nutritionRepository: NutritionRepository,
    private val workoutRepository: WorkoutRepository,
    private val supplementRepository: SupplementRepository,
    private val bodyWeightRepository: BodyWeightRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val dailySummary: StateFlow<DailySummary?> = _selectedDate
        .flatMapLatest { date -> observeDailySummary(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val foodEntries: StateFlow<List<NutritionEntry>> = _selectedDate
        .flatMapLatest { date -> nutritionRepository.observeEntriesForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val workoutSessions: StateFlow<List<WorkoutSessionWithExercises>> = _selectedDate
        .flatMapLatest { date -> workoutRepository.observeSessionsForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val supplements: StateFlow<List<SupplementEntry>> = _selectedDate
        .flatMapLatest { date -> supplementRepository.observeEntriesForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val bodyWeight: StateFlow<BodyWeightEntry?> = _selectedDate
        .flatMapLatest { date -> bodyWeightRepository.observeForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun deleteFood(id: Long) {
        viewModelScope.launch {
            nutritionRepository.delete(id)
        }
    }

    fun deleteWorkout(id: Long) {
        viewModelScope.launch {
            workoutRepository.deleteSession(id)
        }
    }

    fun deleteSupplement(id: Long) {
        viewModelScope.launch {
            supplementRepository.delete(id)
        }
    }

    fun deleteBodyWeight(id: Long) {
        viewModelScope.launch {
            bodyWeightRepository.delete(id)
        }
    }
}
