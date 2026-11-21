package com.musclegain.tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musclegain.tracker.core.domain.calc.NutritionCalculator
import com.musclegain.tracker.core.domain.model.MealCategory
import com.musclegain.tracker.core.domain.model.NutritionEntry
import com.musclegain.tracker.core.domain.repository.NutritionRepository
import com.musclegain.tracker.core.domain.validation.ValidationException
import com.musclegain.tracker.core.domain.validation.ValidationIssue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.abs
import kotlin.math.max

data class FoodFormState(
    val id: Long = 0L,
    val foodName: String = "",
    val mealCategory: MealCategory = MealCategory.LUNCH,
    val calories: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    val notes: String = "",
    val errors: Map<String, String> = emptyMap(),
    val warning: String? = null,
    val isSubmitting: Boolean = false,
    val saveSuccess: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class FoodEntryViewModel(
    private val nutritionRepository: NutritionRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _formState = MutableStateFlow(FoodFormState())
    val formState: StateFlow<FoodFormState> = _formState.asStateFlow()

    val entriesForDate: StateFlow<List<NutritionEntry>> = _selectedDate
        .flatMapLatest { date -> nutritionRepository.observeEntriesForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun onFoodNameChanged(name: String) {
        _formState.value = _formState.value.copy(
            foodName = name,
            errors = _formState.value.errors - "foodName"
        )
    }

    fun onMealCategoryChanged(category: MealCategory) {
        _formState.value = _formState.value.copy(mealCategory = category)
    }

    fun onCaloriesChanged(calories: String) {
        _formState.value = _formState.value.copy(
            calories = calories,
            errors = _formState.value.errors - "calories"
        )
        checkMacroMismatch()
    }

    fun onProteinChanged(protein: String) {
        _formState.value = _formState.value.copy(
            protein = protein,
            errors = _formState.value.errors - "proteinG"
        )
        checkMacroMismatch()
    }

    fun onCarbsChanged(carbs: String) {
        _formState.value = _formState.value.copy(
            carbs = carbs,
            errors = _formState.value.errors - "carbsG"
        )
        checkMacroMismatch()
    }

    fun onFatChanged(fat: String) {
        _formState.value = _formState.value.copy(
            fat = fat,
            errors = _formState.value.errors - "fatG"
        )
        checkMacroMismatch()
    }

    fun onNotesChanged(notes: String) {
        _formState.value = _formState.value.copy(notes = notes)
    }

    private fun checkMacroMismatch() {
        val state = _formState.value
        val cal = state.calories.toDoubleOrNull() ?: return
        val p = state.protein.toDoubleOrNull()
        val c = state.carbs.toDoubleOrNull()
        val f = state.fat.toDoubleOrNull()
        if (p == null && c == null && f == null) {
            _formState.value = _formState.value.copy(warning = null)
            return
        }
        val implied = ((p ?: 0.0) * 4.0) + ((c ?: 0.0) * 4.0) + ((f ?: 0.0) * 9.0)
        if (abs(implied - cal) > max(50.0, cal * 0.2)) {
            _formState.value = _formState.value.copy(
                warning = "Entered calories (${cal.toInt()} kcal) differ noticeably from macros implied calories (${implied.toInt()} kcal)"
            )
        } else {
            _formState.value = _formState.value.copy(warning = null)
        }
    }

    fun loadEntryForEdit(id: Long) {
        if (id <= 0) return
        viewModelScope.launch {
            val entry = nutritionRepository.getEntry(id) ?: return@launch
            _formState.value = FoodFormState(
                id = entry.id,
                foodName = entry.foodName,
                mealCategory = entry.mealCategory,
                calories = entry.calories.toString(),
                protein = entry.proteinG?.toString() ?: "",
                carbs = entry.carbsG?.toString() ?: "",
                fat = entry.fatG?.toString() ?: "",
                notes = entry.notes ?: ""
            )
        }
    }

    fun saveEntry(onSuccess: () -> Unit = {}) {
        val state = _formState.value
        val cal = state.calories.toDoubleOrNull()
        val p = state.protein.toDoubleOrNull()
        val c = state.carbs.toDoubleOrNull()
        val f = state.fat.toDoubleOrNull()

        val errors = mutableMapOf<String, String>()
        if (state.foodName.isBlank()) errors["foodName"] = "Food name is required"
        if (cal == null || cal !in 0.0..20000.0) errors["calories"] = "Valid calories required (0-20,000)"
        if (state.protein.isNotBlank() && (p == null || p !in 0.0..2000.0)) errors["proteinG"] = "Protein must be 0-2000 g"
        if (state.carbs.isNotBlank() && (c == null || c !in 0.0..2000.0)) errors["carbsG"] = "Carbs must be 0-2000 g"
        if (state.fat.isNotBlank() && (f == null || f !in 0.0..2000.0)) errors["fatG"] = "Fat must be 0-2000 g"

        if (errors.isNotEmpty()) {
            _formState.value = state.copy(errors = errors)
            return
        }

        viewModelScope.launch {
            _formState.value = state.copy(isSubmitting = true)
            val entry = NutritionEntry(
                id = state.id,
                foodName = state.foodName.trim(),
                mealCategory = state.mealCategory,
                calories = cal!!,
                proteinG = p,
                carbsG = c,
                fatG = f,
                date = _selectedDate.value,
                time = LocalTime.now(),
                notes = state.notes.ifBlank { null }
            )

            val result = nutritionRepository.upsert(entry)
            result.fold(
                onSuccess = { outcome ->
                    _formState.value = FoodFormState(saveSuccess = true)
                    onSuccess()
                },
                onFailure = { error ->
                    val issueErrors = mutableMapOf<String, String>()
                    if (error is ValidationException) {
                        error.issues.forEach { issue ->
                            issueErrors[issue.field] = issue.message
                        }
                    } else {
                        issueErrors["general"] = error.message ?: "Failed to save entry"
                    }
                    _formState.value = state.copy(isSubmitting = false, errors = issueErrors)
                }
            )
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            nutritionRepository.delete(id)
            if (_formState.value.id == id) {
                resetForm()
            }
        }
    }

    fun resetForm() {
        _formState.value = FoodFormState()
    }
}
