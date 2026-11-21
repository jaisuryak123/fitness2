package com.musclegain.tracker.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.musclegain.tracker.core.data.dao.BodyWeightDao
import com.musclegain.tracker.core.data.dao.GoalsDao
import com.musclegain.tracker.core.data.dao.NutritionDao
import com.musclegain.tracker.core.data.dao.ReminderDao
import com.musclegain.tracker.core.data.dao.SupplementDao
import com.musclegain.tracker.core.data.dao.TrainingScheduleDao
import com.musclegain.tracker.core.data.dao.UserProfileDao
import com.musclegain.tracker.core.data.dao.WorkoutDao
import com.musclegain.tracker.core.data.entity.BodyWeightEntryEntity
import com.musclegain.tracker.core.data.entity.DailyGoalsEntity
import com.musclegain.tracker.core.data.entity.NutritionEntryEntity
import com.musclegain.tracker.core.data.entity.ReminderSettingsEntity
import com.musclegain.tracker.core.data.entity.SupplementEntryEntity
import com.musclegain.tracker.core.data.entity.TrainingDayEntity
import com.musclegain.tracker.core.data.entity.UserProfileEntity
import com.musclegain.tracker.core.data.entity.WorkoutExerciseEntity
import com.musclegain.tracker.core.data.entity.WorkoutSessionEntity

const val DATABASE_VERSION = 1
const val DATABASE_NAME = "musclegain.db"

object Migrations {
    val ALL: Array<Migration> = emptyArray()
}

@Database(
    entities = [
        UserProfileEntity::class,
        DailyGoalsEntity::class,
        NutritionEntryEntity::class,
        WorkoutSessionEntity::class,
        WorkoutExerciseEntity::class,
        SupplementEntryEntity::class,
        BodyWeightEntryEntity::class,
        TrainingDayEntity::class,
        ReminderSettingsEntity::class
    ],
    version = DATABASE_VERSION,
    exportSchema = true
)
abstract class MuscleGainDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun goalsDao(): GoalsDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun supplementDao(): SupplementDao
    abstract fun bodyWeightDao(): BodyWeightDao
    abstract fun trainingScheduleDao(): TrainingScheduleDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        fun build(context: Context): MuscleGainDatabase =
            Room.databaseBuilder(context.applicationContext, MuscleGainDatabase::class.java, DATABASE_NAME)
                .addMigrations(*Migrations.ALL)
                .build()

        fun inMemory(context: Context): MuscleGainDatabase =
            Room.inMemoryDatabaseBuilder(context.applicationContext, MuscleGainDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }
}
