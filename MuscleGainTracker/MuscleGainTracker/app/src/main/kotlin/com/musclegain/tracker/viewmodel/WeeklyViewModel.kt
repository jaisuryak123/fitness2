package com.musclegain.tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musclegain.tracker.core.domain.calc.Weeks
import com.musclegain.tracker.core.domain.model.WeeklySummary
import com.musclegain.tracker.core.domain.usecase.ObserveWeeklySummaryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyViewModel(
    private val observeWeeklySummary: ObserveWeeklySummaryUseCase
) : ViewModel() {

    private val _selectedWeekDate = MutableStateFlow(LocalDate.now())
    val selectedWeekDate: StateFlow<LocalDate> = _selectedWeekDate.asStateFlow()

    val weeklySummary: StateFlow<WeeklySummary?> = _selectedWeekDate
        .flatMapLatest { dateInWeek ->
            observeWeeklySummary(dateInWeek, today = LocalDate.now())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun previousWeek() {
        _selectedWeekDate.value = _selectedWeekDate.value.minusWeeks(1)
    }

    fun nextWeek() {
        _selectedWeekDate.value = _selectedWeekDate.value.plusWeeks(1)
    }

    fun currentWeek() {
        _selectedWeekDate.value = LocalDate.now()
    }
}
