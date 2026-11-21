package com.musclegain.tracker.core.domain.calc

import com.musclegain.tracker.core.domain.model.DailyGoals
import com.musclegain.tracker.core.domain.model.MacroCompletion
import com.musclegain.tracker.core.domain.model.MacroRemaining
import com.musclegain.tracker.core.domain.model.MacroTotals
import com.musclegain.tracker.core.domain.model.NutritionEntry

object NutritionCalculator {
    const val PROTEIN_KCAL_PER_G = 4.0
    const val CARBS_KCAL_PER_G = 4.0
    const val FAT_KCAL_PER_G = 9.0

    fun totals(entries: List<NutritionEntry>): MacroTotals? {
        if (entries.isEmpty()) return null
        return MacroTotals(
            calories = sumKnown(entries.map { it.calories }) ?: 0.0,
            proteinG = sumKnown(entries.map { it.proteinG }),
            carbsG = sumKnown(entries.map { it.carbsG }),
            fatG = sumKnown(entries.map { it.fatG })
        )
    }

    fun remaining(totals: MacroTotals, goals: DailyGoals) = MacroRemaining(
        calories = (goals.calorieTarget - totals.calories).roundTo(2),
        proteinG = totals.proteinG?.let { (goals.proteinTarget - it).roundTo(2) },
        carbsG = totals.carbsG?.let { (goals.carbsTarget - it).roundTo(2) },
        fatG = totals.fatG?.let { (goals.fatTarget - it).roundTo(2) }
    )

    fun completion(totals: MacroTotals, goals: DailyGoals) = MacroCompletion(
        caloriesPercent = percent(totals.calories, goals.calorieTarget),
        proteinPercent = totals.proteinG?.let { percent(it, goals.proteinTarget) },
        carbsPercent = totals.carbsG?.let { percent(it, goals.carbsTarget) },
        fatPercent = totals.fatG?.let { percent(it, goals.fatTarget) }
    )

    fun percent(value: Double, target: Double): Double =
        if (target <= 0.0) 0.0 else (value / target * 100.0).roundTo(1)

    fun impliedCalories(entry: NutritionEntry): Double? {
        val protein = entry.proteinG ?: return null
        val carbs = entry.carbsG ?: return null
        val fat = entry.fatG ?: return null
        return protein * PROTEIN_KCAL_PER_G + carbs * CARBS_KCAL_PER_G + fat * FAT_KCAL_PER_G
    }

    fun hasIncompleteMacros(entries: List<NutritionEntry>): Boolean =
        entries.any { it.proteinG == null || it.carbsG == null || it.fatG == null }
}
