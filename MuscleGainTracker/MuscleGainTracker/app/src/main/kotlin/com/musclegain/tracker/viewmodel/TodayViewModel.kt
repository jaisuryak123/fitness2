package com.musclegain.tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musclegain.tracker.core.domain.model.BodyWeightEntry
import com.musclegain.tracker.core.domain.model.DailySummary
import com.musclegain.tracker.core.domain.repository.WorkoutRepository
import com.musclegain.tracker.core.domain.usecase.LogBodyWeightUseCase
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
class TodayViewModel(
    private val observeDailySummary: ObserveDailySummaryUseCase,
    private val logBodyWeight: LogBodyWeightUseCase,
    private val workoutRepository: WorkoutRepository
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

    private val _userFeedback = MutableStateFlow<String?>(null)
    val userFeedback: StateFlow<String?> = _userFeedback.asStateFlow()

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun logWeight(weightKg: Double, notes: String?) {
        viewModelScope.launch {
            val entry = BodyWeightEntry(
                weightKg = weightKg,
                date = _selectedDate.value,
                notes = notes
            )
            val result = logBodyWeight(entry)
            if (result.isSuccess) {
                _userFeedback.value = "Weight logged successfully (${weightKg} kg)"
            } else {
                _userFeedback.value = result.exceptionOrNull()?.message ?: "Failed to log weight"
            }
        }
    }

    fun clearFeedback() {
        _userFeedback.value = null
    }
}
