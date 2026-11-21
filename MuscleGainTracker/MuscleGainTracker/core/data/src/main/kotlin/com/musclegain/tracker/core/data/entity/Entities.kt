package com.musclegain.tracker.core.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long,
    val heightCm: Double,
    val weightKg: Double,
    val age: Int?,
    val goal: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "daily_goals",
    indices = [Index(value = ["effectiveDate"], unique = true)]
)
data class DailyGoalsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val effectiveDate: String,
    val calorieTarget: Double,
    val proteinTarget: Double,
    val carbsTarget: Double,
    val fatTarget: Double,
    val weeklyGymTarget: Int,
    val weeklyRunningTarget: Int,
    val runningDistanceTargetKm: Double
)

@Entity(
    tableName = "nutrition_entries",
    indices = [
        Index(value = ["entryDate"]),
        Index(value = ["entryDate", "entryTime", "foodName", "mealCategory"], unique = true)
    ]
)
data class NutritionEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val foodName: String,
    val mealCategory: String,
    val calories: Double,
    val proteinG: Double?,
    val carbsG: Double?,
    val fatG: Double?,
    val entryDate: String,
    val entryTime: String,
    val notes: String?
)

@Entity(
    tableName = "workout_sessions",
    indices = [Index(value = ["activityDate"])]
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val activityType: String,
    val activityDate: String,
    val durationMinutes: Int?,
    val distanceKm: Double?,
    val intensity: String?,
    val metUsed: Double?,
    val estimatedGrossKcal: Double?,
    val estimatedActiveKcal: Double?,
    val calculationMethod: String?,
    val bodyWeightKgUsed: Double?,
    val averageSpeedKmh: Double?,
    val notes: String?,
    val status: String
)

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val sessionId: Long,
    val name: String,
    val sets: Int,
    val reps: Int,
    val weightKg: Double
)

data class WorkoutSessionRelation(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(parentColumn = "id", entityColumn = "sessionId")
    val exercises: List<WorkoutExerciseEntity>
)

@Entity(
    tableName = "supplement_entries",
    indices = [
        Index(value = ["entryDate"]),
        Index(value = ["entryDate", "entryTime", "name"], unique = true)
    ]
)
data class SupplementEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
    val amount: Double,
    val unit: String,
    val entryDate: String,
    val entryTime: String,
    val notes: String?
)

@Entity(
    tableName = "body_weight_entries",
    indices = [Index(value = ["recordedDate"], unique = true)]
)
data class BodyWeightEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val weightKg: Double,
    val recordedDate: String,
    val notes: String?
)

@Entity(tableName = "training_schedule")
data class TrainingDayEntity(
    @PrimaryKey val weekday: Int,
    val plannedGym: Boolean,
    val plannedRunning: Boolean,
    val plannedRest: Boolean
)

@Entity(tableName = "reminder_settings")
data class ReminderSettingsEntity(
    @PrimaryKey val id: Long,
    val mealEnabled: Boolean,
    val mealTime: String,
    val workoutEnabled: Boolean,
    val workoutTime: String,
    val bodyWeightEnabled: Boolean,
    val bodyWeightTime: String,
    val supplementEnabled: Boolean,
    val supplementTime: String
)
