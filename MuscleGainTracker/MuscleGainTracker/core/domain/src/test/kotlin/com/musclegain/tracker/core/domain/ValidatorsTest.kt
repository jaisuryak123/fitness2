package com.musclegain.tracker.core.domain

import com.musclegain.tracker.core.domain.TestData.monday
import com.musclegain.tracker.core.domain.model.ActivityType
import com.musclegain.tracker.core.domain.model.TrainingDay
import com.musclegain.tracker.core.domain.model.UserProfileInput
import com.musclegain.tracker.core.domain.model.WorkoutSession
import com.musclegain.tracker.core.domain.validation.Severity
import com.musclegain.tracker.core.domain.validation.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {
    @Test
    fun negativeCaloriesAndMacrosAreErrors() {
        val issues = Validators.nutrition(TestData.nutrition(monday, -1.0, protein = -5.0))
        assertTrue(issues.any { it.code == "INVALID_CALORIES" && it.severity == Severity.ERROR })
        assertTrue(issues.any { it.code == "INVALID_MACRO" && it.severity == Severity.ERROR })
    }

    @Test
    fun macroMismatchIsOnlyAWarning() {
        val mismatch = Validators.nutrition(TestData.nutrition(monday, 1000.0, 30.0, 50.0, 10.0))
        assertEquals(listOf(Severity.WARNING), mismatch.map { it.severity })
        assertEquals("MACRO_CALORIE_MISMATCH", mismatch.single().code)
        assertTrue(Validators.nutrition(TestData.nutrition(monday, 410.0, 30.0, 50.0, 10.0)).isEmpty())
    }

    @Test
    fun invalidProfileAndBodyWeight() {
        val codes = Validators.profile(UserProfileInput(heightCm = 10.0, weightKg = 5.0)).map { it.code }
        assertTrue("INVALID_HEIGHT" in codes && "INVALID_WEIGHT" in codes)
        assertEquals("INVALID_WEIGHT", Validators.bodyWeight(0.0).single().code)
    }

    @Test
    fun invalidDistanceAndDuration() {
        val session = WorkoutSession(type = ActivityType.RUNNING, date = monday, durationMinutes = -5, distanceKm = -2.0)
        val codes = Validators.workout(session, emptyList()).map { it.code }
        assertTrue("INVALID_DURATION" in codes && "INVALID_DISTANCE" in codes)
    }

    @Test
    fun missingRunningDurationIsWarningNotError() {
        val session = WorkoutSession(type = ActivityType.RUNNING, date = monday, distanceKm = 5.0)
        val issues = Validators.workout(session, emptyList())
        assertEquals(listOf("MISSING_DURATION"), issues.map { it.code })
        assertEquals(Severity.WARNING, issues.single().severity)
    }

    @Test
    fun restDayCannotHaveSessions() {
        val days = TrainingDay.defaultWeek().toMutableList()
        days[0] = days[0].copy(plannedRest = true)
        assertTrue(Validators.schedule(days).any { it.code == "CONFLICTING_PLAN" })
        assertTrue(Validators.schedule(TrainingDay.defaultWeek()).isEmpty())
    }
}
