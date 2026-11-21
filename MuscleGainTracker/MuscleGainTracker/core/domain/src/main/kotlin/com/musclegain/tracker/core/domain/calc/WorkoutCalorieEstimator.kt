package com.musclegain.tracker.core.domain.calc

import com.musclegain.tracker.core.domain.model.ActivityType
import com.musclegain.tracker.core.domain.model.Intensity

data class CalorieEstimate(
    val met: Double,
    val grossKcal: Double,
    val activeKcal: Double,
    val averageSpeedKmh: Double?,
    val method: String,
    val bodyWeightKg: Double,
    val durationMinutes: Int
)

object WorkoutCalorieEstimator {
    const val METHOD_SPEED = "MET_SPEED_TABLE_V1"
    const val METHOD_INTENSITY = "MET_INTENSITY_V1"
    const val ESTIMATE_LABEL = "Estimated value, not exact"

    private val runningMetBySpeed = listOf(
        0.0 to 6.0, 8.0 to 8.3, 9.7 to 9.8, 11.3 to 11.0, 12.9 to 11.8,
        14.5 to 12.8, 16.1 to 14.5, 17.7 to 16.0, 19.3 to 19.0
    )

    private val walkingMetBySpeed = listOf(
        0.0 to 2.0, 3.2 to 2.8, 4.0 to 3.0, 4.8 to 3.5,
        5.6 to 4.3, 6.4 to 5.0, 7.2 to 7.0, 8.0 to 8.3
    )

    fun estimate(
        type: ActivityType,
        durationMinutes: Int?,
        distanceKm: Double?,
        intensity: Intensity?,
        bodyWeightKg: Double?
    ): CalorieEstimate? {
        if (bodyWeightKg == null || bodyWeightKg <= 0.0) return null
        if (durationMinutes == null || durationMinutes <= 0) return null
        return when (type) {
            ActivityType.RUNNING, ActivityType.WALKING -> {
                if (distanceKm == null || distanceKm <= 0.0) return null
                val speed = distanceKm / (durationMinutes / 60.0)
                val table = if (type == ActivityType.RUNNING) runningMetBySpeed else walkingMetBySpeed
                val met = table.last { speed >= it.first }.second
                build(met, METHOD_SPEED, speed.roundTo(2), bodyWeightKg, durationMinutes)
            }
            ActivityType.GYM, ActivityType.OTHER_CARDIO -> {
                val level = intensity ?: return null
                build(metForIntensity(type, level), METHOD_INTENSITY, null, bodyWeightKg, durationMinutes)
            }
        }
    }

    fun metForIntensity(type: ActivityType, intensity: Intensity): Double = when (type) {
        ActivityType.GYM -> when (intensity) {
            Intensity.LOW -> 3.5
            Intensity.MODERATE -> 5.0
            Intensity.HIGH -> 6.0
        }
        else -> when (intensity) {
            Intensity.LOW -> 3.5
            Intensity.MODERATE -> 5.5
            Intensity.HIGH -> 8.0
        }
    }

    fun grossKcal(met: Double, bodyWeightKg: Double, durationMinutes: Int): Double =
        met * 3.5 * bodyWeightKg * durationMinutes / 200.0

    fun activeKcal(met: Double, bodyWeightKg: Double, durationMinutes: Int): Double =
        (met - 1.0) * 3.5 * bodyWeightKg * durationMinutes / 200.0

    private fun build(
        met: Double,
        method: String,
        speed: Double?,
        weight: Double,
        minutes: Int
    ) = CalorieEstimate(
        met = met,
        grossKcal = grossKcal(met, weight, minutes).roundTo(1),
        activeKcal = activeKcal(met, weight, minutes).roundTo(1),
        averageSpeedKmh = speed,
        method = method,
        bodyWeightKg = weight,
        durationMinutes = minutes
    )
}
