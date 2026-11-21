package com.musclegain.tracker.core.domain

import com.musclegain.tracker.core.domain.TestData.monday
import com.musclegain.tracker.core.domain.calc.DailySummaryCalculator
import com.musclegain.tracker.core.domain.calc.NutritionCalculator
import com.musclegain.tracker.core.domain.model.DailyGoals
import com.musclegain.tracker.core.domain.model.TrainingDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionCalculatorTest {
    private val goals = DailyGoals.defaults(monday)

    @Test
    fun dailyTotalsAndTargetCompletion() {
        val entries = listOf(
            TestData.nutrition(monday, 500.0, 30.0, 50.0, 10.0),
            TestData.nutrition(monday, 700.0, 40.0, 80.0, 20.0, name = "Other")
        )
        val summary = DailySummaryCalculator.build(monday, goals, entries, emptyList(), TrainingDay.defaultWeek(), null)
        assertEquals(1200.0, summary.totals!!.calories, 0.001)
        assertEquals(70.0, summary.totals!!.proteinG!!, 0.001)
        assertEquals(130.0, summary.totals!!.carbsG!!, 0.001)
        assertEquals(30.0, summary.totals!!.fatG!!, 0.001)
        assertEquals(1600.0, summary.remaining!!.calories, 0.001)
        assertEquals(70.0, summary.remaining!!.proteinG!!, 0.001)
        assertEquals(50.0, summary.completion!!.proteinPercent!!, 0.001)
        assertEquals(42.9, summary.completion!!.caloriesPercent, 0.001)
        assertFalse(summary.hasIncompleteMacros)
    }

    @Test
    fun emptyDayKeepsValuesMissing() {
        val summary = DailySummaryCalculator.build(monday, goals, emptyList(), emptyList(), emptyList(), null)
        assertNull(summary.totals)
        assertNull(summary.remaining)
        assertNull(summary.completion)
        assertNull(summary.estimatedGrossKcal)
        assertNull(summary.bodyWeightKg)
    }

    @Test
    fun missingMacrosAreNeverInvented() {
        val entries = listOf(
            TestData.nutrition(monday, 400.0, protein = 25.0),
            TestData.nutrition(monday, 300.0, name = "Unknown macros")
        )
        val totals = NutritionCalculator.totals(entries)!!
        assertEquals(700.0, totals.calories, 0.001)
        assertEquals(25.0, totals.proteinG!!, 0.001)
        assertNull(totals.carbsG)
        assertNull(totals.fatG)
        assertTrue(NutritionCalculator.hasIncompleteMacros(entries))
    }

    @Test
    fun plannedDayIsResolvedFromSchedule() {
        val summary = DailySummaryCalculator.build(monday, goals, emptyList(), emptyList(), TrainingDay.defaultWeek(), null)
        assertTrue(summary.plannedDay!!.plannedGym)
    }
}
