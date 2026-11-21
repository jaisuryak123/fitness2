package com.musclegain.tracker

import com.musclegain.tracker.core.domain.calc.WorkoutCalorieEstimator
import com.musclegain.tracker.core.domain.model.ActivityType
import com.musclegain.tracker.core.domain.model.BodyWeightEntry
import com.musclegain.tracker.core.domain.model.CompletionStatus
import com.musclegain.tracker.core.domain.model.FitnessGoal
import com.musclegain.tracker.core.domain.model.Intensity
import com.musclegain.tracker.core.domain.model.UserProfile
import com.musclegain.tracker.core.domain.model.UserProfileInput
import com.musclegain.tracker.core.domain.model.WorkoutSessionWithExercises
import com.musclegain.tracker.core.domain.repository.BodyWeightRepository
import com.musclegain.tracker.core.domain.repository.UserProfileRepository
import com.musclegain.tracker.core.domain.repository.WorkoutRepository
import com.musclegain.tracker.core.domain.usecase.LogWorkoutUseCase
import com.musclegain.tracker.core.domain.validation.SaveOutcome
import com.musclegain.tracker.viewmodel.WorkoutEntryViewModel
import com.musclegain.tracker.viewmodel.WorkoutExerciseForm
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutEntryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val dummyProfile = UserProfile(
        id = 1L,
        heightCm = 180.0,
        weightKg = 75.0,
        age = 26,
        goal = FitnessGoal.MUSCLE_GAIN,
        createdAtMillis = 1000L,
        updatedAtMillis = 1000L
    )

    private val mockUserProfileRepository = object : UserProfileRepository {
        override fun observeProfile(): Flow<UserProfile?> = flowOf(dummyProfile)
        override suspend fun getProfile(): UserProfile = dummyProfile
        override suspend fun saveProfile(input: UserProfileInput): Result<SaveOutcome> =
            Result.success(SaveOutcome(1L))
    }

    private val mockBodyWeightRepository = object : BodyWeightRepository {
        val entry = BodyWeightEntry(id = 1L, weightKg = 75.0, date = LocalDate.now())
        override fun observeForDate(date: LocalDate): Flow<BodyWeightEntry?> = flowOf(entry)
        override fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<BodyWeightEntry>> = flowOf(listOf(entry))
        override fun observeRecent(limit: Int): Flow<List<BodyWeightEntry>> = flowOf(listOf(entry))
        override suspend fun getLatest(): BodyWeightEntry = entry
        override suspend fun getLatestOnOrBefore(date: LocalDate): BodyWeightEntry = entry
        override suspend fun upsert(entry: BodyWeightEntry): Result<SaveOutcome> = Result.success(SaveOutcome(entry.id))
        override suspend fun delete(id: Long): Boolean = true
    }

    private val mockWorkoutRepository = object : WorkoutRepository {
        val stored = mutableListOf<WorkoutSessionWithExercises>()
        override fun observeSessionsForDate(date: LocalDate): Flow<List<WorkoutSessionWithExercises>> = flowOf(stored)
        override fun observeSessionsBetween(start: LocalDate, end: LocalDate): Flow<List<WorkoutSessionWithExercises>> = flowOf(stored)
        override suspend fun getSession(id: Long): WorkoutSessionWithExercises? = stored.find { it.session.id == id }
        override suspend fun saveSession(value: WorkoutSessionWithExercises): Result<SaveOutcome> {
            stored.add(value)
            return Result.success(SaveOutcome(value.session.id))
        }
        override suspend fun deleteSession(id: Long): Boolean = stored.removeIf { it.session.id == id }
    }

    private lateinit var logWorkoutUseCase: LogWorkoutUseCase
    private lateinit var viewModel: WorkoutEntryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        logWorkoutUseCase = LogWorkoutUseCase(mockWorkoutRepository, mockUserProfileRepository, mockBodyWeightRepository)
        viewModel = WorkoutEntryViewModel(
            logWorkout = logWorkoutUseCase,
            workoutRepository = mockWorkoutRepository,
            bodyWeightRepository = mockBodyWeightRepository,
            userProfileRepository = mockUserProfileRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `exercise builder adds and removes exercises`() = runTest {
        assertEquals(1, viewModel.formState.value.exercises.size)

        viewModel.addExercise()
        assertEquals(2, viewModel.formState.value.exercises.size)

        viewModel.updateExercise(1, WorkoutExerciseForm(name = "Incline Dumbbell Press", sets = "4", reps = "8", weightKg = "28"))
        assertEquals("Incline Dumbbell Press", viewModel.formState.value.exercises[1].name)

        viewModel.removeExercise(0)
        assertEquals(1, viewModel.formState.value.exercises.size)
        assertEquals("Incline Dumbbell Press", viewModel.formState.value.exercises[0].name)
    }

    @Test
    fun `running calculates speed and MET estimate preview`() = runTest {
        viewModel.onActivityTypeChanged(ActivityType.RUNNING)
        viewModel.onDurationChanged("30")
        viewModel.onDistanceChanged("5.0")
        testDispatcher.scheduler.advanceUntilIdle()

        val estimate = viewModel.calorieEstimatePreview.value
        assertNotNull(estimate)
        assertEquals(10.0, estimate!!.averageSpeedKmh!!, 0.1) // 5 km in 30 min = 10 km/h
        assertTrue(estimate.activeKcal > 0)
    }

    @Test
    fun `invalid duration and distance are validated`() = runTest {
        viewModel.onActivityTypeChanged(ActivityType.RUNNING)
        viewModel.onDurationChanged("0") // invalid duration (must be 1-1440)
        viewModel.onDistanceChanged("-2.5") // invalid distance
        viewModel.saveWorkout()

        val state = viewModel.formState.value
        assertTrue(state.errors.containsKey("durationMinutes"))
        assertTrue(state.errors.containsKey("distanceKm"))
    }
}
