package com.musclegain.tracker

import com.musclegain.tracker.core.domain.model.MealCategory
import com.musclegain.tracker.core.domain.model.NutritionEntry
import com.musclegain.tracker.core.domain.repository.NutritionRepository
import com.musclegain.tracker.core.domain.validation.SaveOutcome
import com.musclegain.tracker.viewmodel.FoodEntryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class FoodEntryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val mockNutritionRepository = object : NutritionRepository {
        val stored = mutableListOf<NutritionEntry>()

        override fun observeEntriesForDate(date: LocalDate): Flow<List<NutritionEntry>> =
            flowOf(stored.filter { it.date == date })

        override fun observeEntriesBetween(start: LocalDate, end: LocalDate): Flow<List<NutritionEntry>> =
            flowOf(stored.filter { !it.date.isBefore(start) && !it.date.isAfter(end) })

        override suspend fun getEntry(id: Long): NutritionEntry? =
            stored.find { it.id == id }

        override suspend fun upsert(entry: NutritionEntry): Result<SaveOutcome> {
            val id = if (entry.id == 0L) (stored.size + 1).toLong() else entry.id
            stored.removeAll { it.id == id }
            stored.add(entry.copy(id = id))
            return Result.success(SaveOutcome(id))
        }

        override suspend fun delete(id: Long): Boolean =
            stored.removeIf { it.id == id }
    }

    private lateinit var viewModel: FoodEntryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FoodEntryViewModel(mockNutritionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `form validation detects empty food name and invalid calories`() = runTest {
        viewModel.onFoodNameChanged("")
        viewModel.onCaloriesChanged("-50")
        viewModel.saveEntry()

        val state = viewModel.formState.value
        assertTrue(state.errors.containsKey("foodName"))
        assertTrue(state.errors.containsKey("calories"))
    }

    @Test
    fun `valid food entry saves successfully`() = runTest {
        viewModel.onFoodNameChanged("Chicken Breast 200g")
        viewModel.onMealCategoryChanged(MealCategory.LUNCH)
        viewModel.onCaloriesChanged("330")
        viewModel.onProteinChanged("62")
        viewModel.onCarbsChanged("0")
        viewModel.onFatChanged("7")
        viewModel.saveEntry()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.formState.value
        assertTrue(state.saveSuccess)
        assertEquals(1, mockNutritionRepository.stored.size)
        assertEquals("Chicken Breast 200g", mockNutritionRepository.stored.first().foodName)
    }

    @Test
    fun `macro calorie mismatch triggers warning banner`() = runTest {
        viewModel.onCaloriesChanged("100")
        // Protein 50g -> 200 kcal, which differs noticeably from 100 kcal entered
        viewModel.onProteinChanged("50")

        val state = viewModel.formState.value
        assertNotNull(state.warning)
    }

    @Test
    fun `load entry for edit populates form state`() = runTest {
        val entry = NutritionEntry(
            id = 42L,
            foodName = "Oatmeal with Milk",
            mealCategory = MealCategory.BREAKFAST,
            calories = 380.0,
            proteinG = 15.0,
            carbsG = 60.0,
            fatG = 8.0,
            date = LocalDate.now(),
            time = java.time.LocalTime.of(8, 30)
        )
        mockNutritionRepository.stored.add(entry)

        viewModel.loadEntryForEdit(42L)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.formState.value
        assertEquals(42L, state.id)
        assertEquals("Oatmeal with Milk", state.foodName)
        assertEquals("380.0", state.calories)
        assertEquals("15.0", state.protein)
    }
}
