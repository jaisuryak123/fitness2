package com.musclegain.tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musclegain.tracker.core.domain.model.BodyWeightEntry
import com.musclegain.tracker.core.domain.model.DailyGoals
import com.musclegain.tracker.core.domain.model.FitnessGoal
import com.musclegain.tracker.core.domain.model.SupplementEntry
import com.musclegain.tracker.core.domain.model.UserProfile
import com.musclegain.tracker.core.domain.model.UserProfileInput
import com.musclegain.tracker.core.domain.repository.BodyWeightRepository
import com.musclegain.tracker.core.domain.repository.GoalsRepository
import com.musclegain.tracker.core.domain.repository.SupplementRepository
import com.musclegain.tracker.core.domain.repository.UserProfileRepository
import com.musclegain.tracker.core.domain.usecase.LogBodyWeightUseCase
import com.musclegain.tracker.core.domain.validation.ValidationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@Suppress("MemberVisibilityCanBePrivate")
class ProfileViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val goalsRepository: GoalsRepository,
    private val bodyWeightRepository: BodyWeightRepository,
    private val supplementRepository: SupplementRepository,
    private val logBodyWeight: LogBodyWeightUseCase
) : ViewModel() {

    val profile: StateFlow<UserProfile?> = userProfileRepository.observeProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val currentGoals: StateFlow<DailyGoals> = goalsRepository.observeGoalsFor(LocalDate.now())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailyGoals.defaults(LocalDate.now())
        )

    val recentWeights: StateFlow<List<BodyWeightEntry>> = bodyWeightRepository.observeRecent(30)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todaySupplements: StateFlow<List<SupplementEntry>> = supplementRepository.observeEntriesForDate(LocalDate.now())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val knownSupplementNames: StateFlow<List<String>> = supplementRepository.observeKnownNames()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun saveProfile(
        heightCm: Double,
        weightKg: Double,
        age: Int?,
        goal: FitnessGoal
    ) {
        viewModelScope.launch {
            val input = UserProfileInput(
                heightCm = heightCm,
                weightKg = weightKg,
                age = age,
                goal = goal
            )
            val result = userProfileRepository.saveProfile(input)
            result.fold(
                onSuccess = {
                    _statusMessage.value = "Profile updated successfully"
                    _errorMessage.value = null
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Failed to save profile"
                }
            )
        }
    }

    fun saveGoals(
        calorieTarget: Double,
        proteinTarget: Double,
        carbsTarget: Double,
        fatTarget: Double,
        weeklyGymTarget: Int,
        weeklyRunningTarget: Int,
        runningDistanceKm: Double
    ) {
        viewModelScope.launch {
            val newGoals = DailyGoals(
                effectiveDate = LocalDate.now(),
                calorieTarget = calorieTarget,
                proteinTarget = proteinTarget,
                carbsTarget = carbsTarget,
                fatTarget = fatTarget,
                weeklyGymTarget = weeklyGymTarget,
                weeklyRunningTarget = weeklyRunningTarget,
                runningDistanceTargetKm = runningDistanceKm
            )
            val result = goalsRepository.saveGoals(newGoals)
            result.fold(
                onSuccess = {
                    _statusMessage.value = "Nutrition and workout goals updated"
                    _errorMessage.value = null
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Failed to save goals"
                }
            )
        }
    }

    fun addSupplement(
        name: String,
        amount: Double,
        unit: String,
        notes: String?
    ) {
        viewModelScope.launch {
            val entry = SupplementEntry(
                name = name.trim(),
                amount = amount,
                unit = unit.trim(),
                date = LocalDate.now(),
                time = LocalTime.now(),
                notes = notes?.ifBlank { null }
            )
            val result = supplementRepository.upsert(entry)
            result.fold(
                onSuccess = {
                    _statusMessage.value = "Supplement logged"
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Failed to log supplement"
                }
            )
        }
    }

    fun deleteSupplement(id: Long) {
        viewModelScope.launch {
            supplementRepository.delete(id)
        }
    }

    fun deleteWeight(id: Long) {
        viewModelScope.launch {
            bodyWeightRepository.delete(id)
        }
    }

    fun clearMessages() {
        _statusMessage.value = null
        _errorMessage.value = null
    }
}
