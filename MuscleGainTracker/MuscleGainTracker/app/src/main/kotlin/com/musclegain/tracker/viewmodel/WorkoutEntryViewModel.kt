package com.musclegain.tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musclegain.tracker.core.domain.calc.CalorieEstimate
import com.musclegain.tracker.core.domain.calc.WorkoutCalorieEstimator
import com.musclegain.tracker.core.domain.model.ActivityType
import com.musclegain.tracker.core.domain.model.CompletionStatus
import com.musclegain.tracker.core.domain.model.Intensity
import com.musclegain.tracker.core.domain.model.WorkoutSessionWithExercises
import com.musclegain.tracker.core.domain.repository.BodyWeightRepository
import com.musclegain.tracker.core.domain.repository.UserProfileRepository
import com.musclegain.tracker.core.domain.repository.WorkoutRepository
import com.musclegain.tracker.core.domain.usecase.ExerciseInput
import com.musclegain.tracker.core.domain.usecase.LogWorkoutUseCase
import com.musclegain.tracker.core.domain.usecase.WorkoutInput
import com.musclegain.tracker.core.domain.validation.ValidationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class WorkoutExerciseForm(
    val name: String = "",
    val sets: String = "3",
    val reps: String = "10",
    val weightKg: String = "0"
)

data class WorkoutFormState(
    val id: Long = 0L,
    val type: ActivityType = ActivityType.GYM,
    val durationMinutes: String = "45",
    val distanceKm: String = "",
    val intensity: Intensity = Intensity.MODERATE,
    val status: CompletionStatus = CompletionStatus.COMPLETED,
    val exercises: List<WorkoutExerciseForm> = listOf(WorkoutExerciseForm()),
    val notes: String = "",
    val errors: Map<String, String> = emptyMap(),
    val warnings: List<String> = emptyList(),
    val isSubmitting: Boolean = false,
    val saveSuccess: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutEntryViewModel(
    private val logWorkout: LogWorkoutUseCase,
    private val workoutRepository: WorkoutRepository,
    private val bodyWeightRepository: BodyWeightRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _formState = MutableStateFlow(WorkoutFormState())
    val formState: StateFlow<WorkoutFormState> = _formState.asStateFlow()

    private val _calorieEstimatePreview = MutableStateFlow<CalorieEstimate?>(null)
    val calorieEstimatePreview: StateFlow<CalorieEstimate?> = _calorieEstimatePreview.asStateFlow()

    val sessionsForDate: StateFlow<List<WorkoutSessionWithExercises>> = _selectedDate
        .flatMapLatest { date -> workoutRepository.observeSessionsForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
        recalculateEstimate()
    }

    fun onActivityTypeChanged(type: ActivityType) {
        _formState.value = _formState.value.copy(
            type = type,
            errors = emptyMap()
        )
        recalculateEstimate()
    }

    fun onDurationChanged(duration: String) {
        _formState.value = _formState.value.copy(
            durationMinutes = duration,
            errors = _formState.value.errors - "durationMinutes"
        )
        recalculateEstimate()
    }

    fun onDistanceChanged(distance: String) {
        _formState.value = _formState.value.copy(
            distanceKm = distance,
            errors = _formState.value.errors - "distanceKm"
        )
        recalculateEstimate()
    }

    fun onIntensityChanged(intensity: Intensity) {
        _formState.value = _formState.value.copy(intensity = intensity)
        recalculateEstimate()
    }

    fun onStatusChanged(status: CompletionStatus) {
        _formState.value = _formState.value.copy(status = status)
        recalculateEstimate()
    }

    fun onNotesChanged(notes: String) {
        _formState.value = _formState.value.copy(notes = notes)
    }

    // Exercise Builder helpers
    fun addExercise() {
        val current = _formState.value.exercises
        _formState.value = _formState.value.copy(
            exercises = current + WorkoutExerciseForm()
        )
    }

    fun removeExercise(index: Int) {
        val current = _formState.value.exercises.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _formState.value = _formState.value.copy(exercises = current)
        }
    }

    fun updateExercise(index: Int, exercise: WorkoutExerciseForm) {
        val current = _formState.value.exercises.toMutableList()
        if (index in current.indices) {
            current[index] = exercise
            _formState.value = _formState.value.copy(exercises = current)
        }
    }

    private fun recalculateEstimate() {
        viewModelScope.launch {
            val state = _formState.value
            val weight = bodyWeightRepository.getLatestOnOrBefore(_selectedDate.value)?.weightKg
                ?: userProfileRepository.getProfile()?.weightKg
            val duration = state.durationMinutes.toIntOrNull()
            val distance = state.distanceKm.toDoubleOrNull()

            _calorieEstimatePreview.value = WorkoutCalorieEstimator.estimate(
                type = state.type,
                durationMinutes = duration,
                distanceKm = distance,
                intensity = state.intensity,
                bodyWeightKg = weight
            )
        }
    }

    fun loadSessionForEdit(id: Long) {
        if (id <= 0) return
        viewModelScope.launch {
            val sessionWithEx = workoutRepository.getSession(id) ?: return@launch
            val session = sessionWithEx.session
            _formState.value = WorkoutFormState(
                id = session.id,
                type = session.type,
                durationMinutes = session.durationMinutes?.toString() ?: "",
                distanceKm = session.distanceKm?.toString() ?: "",
                intensity = session.intensity ?: Intensity.MODERATE,
                status = session.status,
                exercises = sessionWithEx.exercises.map {
                    WorkoutExerciseForm(
                        name = it.name,
                        sets = it.sets.toString(),
                        reps = it.reps.toString(),
                        weightKg = it.weightKg.toString()
                    )
                }.ifEmpty { listOf(WorkoutExerciseForm()) },
                notes = session.notes ?: ""
            )
            recalculateEstimate()
        }
    }

    fun saveWorkout(onSuccess: () -> Unit = {}) {
        val state = _formState.value
        val duration = state.durationMinutes.toIntOrNull()
        val distance = state.distanceKm.toDoubleOrNull()
        val errors = mutableMapOf<String, String>()

        if (state.durationMinutes.isNotBlank() && (duration == null || duration !in 1..1440)) {
            errors["durationMinutes"] = "Duration must be 1-1440 minutes"
        }
        if (state.distanceKm.isNotBlank() && (distance == null || distance !in 0.01..500.0)) {
            errors["distanceKm"] = "Distance must be 0.01-500 km"
        }

        val parsedExercises = mutableListOf<ExerciseInput>()
        if (state.type == ActivityType.GYM) {
            state.exercises.forEachIndexed { index, ex ->
                if (ex.name.isNotBlank()) {
                    val sets = ex.sets.toIntOrNull()
                    val reps = ex.reps.toIntOrNull()
                    val weight = ex.weightKg.toDoubleOrNull()
                    if (sets == null || sets !in 1..100) errors["exercises[$index].sets"] = "Sets must be 1-100"
                    if (reps == null || reps !in 1..1000) errors["exercises[$index].reps"] = "Reps must be 1-1000"
                    if (weight == null || weight !in 0.0..1000.0) errors["exercises[$index].weightKg"] = "Weight must be 0-1000 kg"
                    if (sets != null && reps != null && weight != null) {
                        parsedExercises.add(ExerciseInput(ex.name.trim(), sets, reps, weight))
                    }
                }
            }
        }

        if (errors.isNotEmpty()) {
            _formState.value = state.copy(errors = errors)
            return
        }

        viewModelScope.launch {
            _formState.value = state.copy(isSubmitting = true)
            val input = WorkoutInput(
                id = state.id,
                type = state.type,
                date = _selectedDate.value,
                durationMinutes = duration,
                distanceKm = distance,
                intensity = state.intensity,
                notes = state.notes.ifBlank { null },
                status = state.status,
                exercises = parsedExercises
            )

            val result = logWorkout(input)
            result.fold(
                onSuccess = { outcome ->
                    _formState.value = WorkoutFormState(
                        saveSuccess = true,
                        warnings = outcome.warnings.map { it.message }
                    )
                    onSuccess()
                },
                onFailure = { error ->
                    val issueErrors = mutableMapOf<String, String>()
                    if (error is ValidationException) {
                        error.issues.forEach { issue ->
                            issueErrors[issue.field] = issue.message
                        }
                    } else {
                        issueErrors["general"] = error.message ?: "Failed to save workout"
                    }
                    _formState.value = state.copy(isSubmitting = false, errors = issueErrors)
                }
            )
        }
    }

    fun deleteWorkout(id: Long) {
        viewModelScope.launch {
            workoutRepository.deleteSession(id)
            if (_formState.value.id == id) {
                resetForm()
            }
        }
    }

    fun resetForm() {
        _formState.value = WorkoutFormState()
        recalculateEstimate()
    }
}
