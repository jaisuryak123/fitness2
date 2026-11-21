package com.musclegain.tracker.core.domain

import com.musclegain.tracker.core.domain.calc.WorkoutCalorieEstimator
import com.musclegain.tracker.core.domain.model.ActivityType
import com.musclegain.tracker.core.domain.model.Intensity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class WorkoutCalorieEstimatorTest {
    @Test
    fun runningUsesSpeedTableAndGrossFormula() {
        val result = WorkoutCalorieEstimator.estimate(ActivityType.RUNNING, 30, 5.0, null, 70.0)!!
        assertEquals(9.8, result.met, 0.001)
        assertEquals(10.0, result.averageSpeedKmh!!, 0.01)
        assertEquals(360.2, result.grossKcal, 0.1)
        assertEquals(323.4, result.activeKcal, 0.1)
        assertEquals(WorkoutCalorieEstimator.METHOD_SPEED, result.method)
    }

    @Test
    fun walkingUsesWalkingTable() {
        val result = WorkoutCalorieEstimator.estimate(ActivityType.WALKING, 36, 3.0, null, 70.0)!!
        assertEquals(3.5, result.met, 0.001)
        assertEquals(154.4, result.grossKcal, 0.1)
    }

    @Test
    fun gymUsesIntensityMet() {
        val result = WorkoutCalorieEstimator.estimate(ActivityType.GYM, 60, null, Intensity.MODERATE, 80.0)!!
        assertEquals(5.0, result.met, 0.001)
        assertEquals(420.0, result.grossKcal, 0.1)
        assertEquals(336.0, result.activeKcal, 0.1)
        assertNull(result.averageSpeedKmh)
    }

    @Test
    fun missingInputsProduceNoEstimate() {
        assertNull(WorkoutCalorieEstimator.estimate(ActivityType.RUNNING, null, 5.0, null, 70.0))
        assertNull(WorkoutCalorieEstimator.estimate(ActivityType.RUNNING, 30, null, null, 70.0))
        assertNull(WorkoutCalorieEstimator.estimate(ActivityType.GYM, 60, null, null, 70.0))
        assertNull(WorkoutCalorieEstimator.estimate(ActivityType.GYM, 60, null, Intensity.LOW, null))
    }

    @Test
    fun slowRunUsesJoggingFloor() {
        val result = WorkoutCalorieEstimator.estimate(ActivityType.RUNNING, 60, 6.0, null, 70.0)
        assertNotNull(result)
        assertEquals(6.0, result!!.met, 0.001)
    }
}
