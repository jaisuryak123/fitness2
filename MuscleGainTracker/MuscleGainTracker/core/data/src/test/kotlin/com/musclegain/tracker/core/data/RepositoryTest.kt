package com.musclegain.tracker.core.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.musclegain.tracker.core.domain.model.ActivityType
import com.musclegain.tracker.core.domain.model.BodyWeightEntry
import com.musclegain.tracker.core.domain.model.CsvTable
import com.musclegain.tracker.core.domain.model.FitnessGoal
import com.musclegain.tracker.core.domain.model.Intensity
import com.musclegain.tracker.core.domain.model.MealCategory
import com.musclegain.tracker.core.domain.model.NutritionEntry
import com.musclegain.tracker.core.domain.model.TrainingDay
import com.musclegain.tracker.core.domain.model.UserProfileInput
import com.musclegain.tracker.core.domain.usecase.ExerciseInput
import com.musclegain.tracker.core.domain.usecase.WorkoutInput
import com.musclegain.tracker.core.domain.validation.ValidationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RepositoryTest {
    private lateinit var database: MuscleGainDatabase
    private lateinit var container: MuscleGainContainer
    private val date = LocalDate.of(2026, 11, 16)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = MuscleGainDatabase.inMemory(context)
        container = MuscleGainContainer(context, database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun food(name: String = "Rice", calories: Double = 400.0) = NutritionEntry(
        foodName = name, mealCategory = MealCategory.LUNCH, calories = calories,
        proteinG = 8.0, carbsG = 80.0, fatG = 1.0, date = date, time = LocalTime.of(13, 0)
    )

    @Test
    fun nutritionInsertEditDeleteAndDuplicateRejection() = runBlocking {
        val repo = container.nutritionRepository
        val id = repo.upsert(food()).getOrThrow().id
        val duplicate = repo.upsert(food())
        assertTrue(duplicate.exceptionOrNull() is ValidationException)

        repo.upsert(food(calories = 450.0).copy(id = id)).getOrThrow()
        assertEquals(450.0, repo.observeEntriesForDate(date).first().single().calories, 0.001)

        assertTrue(repo.delete(id))
        assertTrue(repo.observeEntriesForDate(date).first().isEmpty())
        assertFalse(repo.delete(id))
    }

    @Test
    fun invalidNutritionIsRejectedAndNotStored() = runBlocking {
        val result = container.nutritionRepository.upsert(food(calories = -5.0))
        assertTrue(result.isFailure)
        assertTrue(container.nutritionRepository.observeEntriesForDate(date).first().isEmpty())
    }

    @Test
    fun workoutWithExercisesStoresEstimateInputsAndCascadesOnDelete() = runBlocking {
        container.userProfileRepository.saveProfile(UserProfileInput(175.0, 70.0, 30, FitnessGoal.MUSCLE_GAIN)).getOrThrow()
        val id = container.logWorkout(
            WorkoutInput(
                type = ActivityType.GYM, date = date, durationMinutes = 60, intensity = Intensity.MODERATE,
                exercises = listOf(ExerciseInput("Bench Press", 3, 10, 60.0), ExerciseInput("Row", 3, 10, 50.0))
            )
        ).getOrThrow().id

        val saved = container.workoutRepository.getSession(id)!!
        assertEquals(2, saved.exercises.size)
        assertEquals(5.0, saved.session.metUsed!!, 0.001)
        assertEquals(70.0, saved.session.bodyWeightKgUsed!!, 0.001)
        assertEquals(367.5, saved.session.estimatedGrossKcal!!, 0.1)

        container.logWorkout(
            WorkoutInput(
                id = id, type = ActivityType.GYM, date = date, durationMinutes = 60, intensity = Intensity.HIGH,
                exercises = listOf(ExerciseInput("Squat", 5, 5, 100.0))
            )
        ).getOrThrow()
        assertEquals(listOf("Squat"), container.workoutRepository.getSession(id)!!.exercises.map { it.name })

        assertTrue(container.workoutRepository.deleteSession(id))
        assertNull(container.workoutRepository.getSession(id))
        val remaining = database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM workout_exercises").use {
            it.moveToFirst()
            it.getInt(0)
        }
        assertEquals(0, remaining)
    }

    @Test
    fun runningWithoutDurationIsSavedWithoutInventedEstimate() = runBlocking {
        container.userProfileRepository.saveProfile(UserProfileInput(175.0, 70.0)).getOrThrow()
        val outcome = container.logWorkout(WorkoutInput(type = ActivityType.RUNNING, date = date, distanceKm = 5.0)).getOrThrow()
        assertTrue(outcome.warnings.any { it.code == "MISSING_DURATION" })
        assertNull(container.workoutRepository.getSession(outcome.id)!!.session.estimatedGrossKcal)
    }

    @Test
    fun bodyWeightKeepsOneRecordPerDayAndUpdatesProfile() = runBlocking {
        container.userProfileRepository.saveProfile(UserProfileInput(175.0, 70.0)).getOrThrow()
        container.logBodyWeight(BodyWeightEntry(weightKg = 71.0, date = date)).getOrThrow()
        container.logBodyWeight(BodyWeightEntry(weightKg = 71.5, date = date)).getOrThrow()
        val entries = container.bodyWeightRepository.observeBetween(date, date).first()
        assertEquals(1, entries.size)
        assertEquals(71.5, entries.single().weightKg, 0.001)
        assertEquals(71.5, container.userProfileRepository.getProfile()!!.weightKg, 0.001)
    }

    @Test
    fun goalsAreVersionedByEffectiveDate() = runBlocking {
        val repo = container.goalsRepository
        val first = com.musclegain.tracker.core.domain.model.DailyGoals.defaults(date)
        repo.saveGoals(first).getOrThrow()
        repo.saveGoals(first.copy(effectiveDate = date.plusDays(7), proteinTarget = 160.0)).getOrThrow()
        assertEquals(140.0, repo.getGoalsFor(date.plusDays(3)).proteinTarget, 0.001)
        assertEquals(160.0, repo.getGoalsFor(date.plusDays(7)).proteinTarget, 0.001)
        repo.saveGoals(first.copy(proteinTarget = 150.0)).getOrThrow()
        assertEquals(2, repo.observeAllGoals().first().size)
    }

    @Test
    fun scheduleDefaultsAndEdits() = runBlocking {
        val repo = container.trainingScheduleRepository
        assertEquals(TrainingDay.defaultWeek(), repo.observeSchedule().first())
        val edited = TrainingDay.defaultWeek().map { it.copy(plannedGym = false, plannedRunning = false, plannedRest = true) }
        repo.saveSchedule(edited).getOrThrow()
        assertTrue(repo.observeSchedule().first().all { it.plannedRest })
    }

    @Test
    fun jsonBackupRoundTripAndCsvExport() = runBlocking {
        container.nutritionRepository.upsert(food(name = "Oats, rolled")).getOrThrow()
        container.bodyWeightRepository.upsert(BodyWeightEntry(weightKg = 72.0, date = date)).getOrThrow()
        val json = container.backupRepository.exportJson()
        val csv = container.backupRepository.exportCsv(CsvTable.NUTRITION)
        assertTrue(csv.contains("\"Oats, rolled\""))

        container.backupRepository.deleteAllData()
        assertTrue(container.nutritionRepository.observeEntriesForDate(date).first().isEmpty())

        container.backupRepository.restoreJson(json).getOrThrow()
        assertEquals("Oats, rolled", container.nutritionRepository.observeEntriesForDate(date).first().single().foodName)
        assertNotNull(container.bodyWeightRepository.getLatest())
        assertTrue(container.backupRepository.restoreJson("{}").isFailure)
    }
}
