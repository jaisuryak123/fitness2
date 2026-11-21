package com.musclegain.tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musclegain.tracker.core.domain.model.Reminder
import com.musclegain.tracker.core.domain.model.ReminderSettings
import com.musclegain.tracker.core.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime

class NotificationViewModel(
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    val reminderSettings: StateFlow<ReminderSettings> = reminderRepository.observeSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReminderSettings()
        )

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun updateMealReminder(enabled: Boolean, time: LocalTime? = null) {
        val current = reminderSettings.value
        val updated = current.copy(
            meal = Reminder(enabled, time ?: current.meal.time)
        )
        saveSettings(updated)
    }

    fun updateWorkoutReminder(enabled: Boolean, time: LocalTime? = null) {
        val current = reminderSettings.value
        val updated = current.copy(
            workout = Reminder(enabled, time ?: current.workout.time)
        )
        saveSettings(updated)
    }

    fun updateBodyWeightReminder(enabled: Boolean, time: LocalTime? = null) {
        val current = reminderSettings.value
        val updated = current.copy(
            bodyWeight = Reminder(enabled, time ?: current.bodyWeight.time)
        )
        saveSettings(updated)
    }

    fun updateSupplementReminder(enabled: Boolean, time: LocalTime? = null) {
        val current = reminderSettings.value
        val updated = current.copy(
            supplement = Reminder(enabled, time ?: current.supplement.time)
        )
        saveSettings(updated)
    }

    private fun saveSettings(settings: ReminderSettings) {
        viewModelScope.launch {
            reminderRepository.saveSettings(settings)
            _statusMessage.value = "Reminder settings saved"
        }
    }

    fun clearMessage() {
        _statusMessage.value = null
    }
}
