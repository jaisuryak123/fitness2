package com.musclegain.tracker.core.data

import android.content.Context
import com.musclegain.tracker.core.data.backup.BackupRepositoryImpl
import com.musclegain.tracker.core.data.preferences.AppPreferencesRepositoryImpl
import com.musclegain.tracker.core.data.repository.BodyWeightRepositoryImpl
import com.musclegain.tracker.core.data.repository.GoalsRepositoryImpl
import com.musclegain.tracker.core.data.repository.NutritionRepositoryImpl
import com.musclegain.tracker.core.data.repository.ReminderRepositoryImpl
import com.musclegain.tracker.core.data.repository.SupplementRepositoryImpl
import com.musclegain.tracker.core.data.repository.TrainingScheduleRepositoryImpl
import com.musclegain.tracker.core.data.repository.UserProfileRepositoryImpl
import com.musclegain.tracker.core.data.repository.WorkoutRepositoryImpl
import com.musclegain.tracker.core.domain.repository.AppPreferencesRepository
import com.musclegain.tracker.core.domain.repository.BackupRepository
import com.musclegain.tracker.core.domain.repository.BodyWeightRepository
import com.musclegain.tracker.core.domain.repository.GoalsRepository
import com.musclegain.tracker.core.domain.repository.NutritionRepository
import com.musclegain.tracker.core.domain.repository.ReminderRepository
import com.musclegain.tracker.core.domain.repository.SupplementRepository
import com.musclegain.tracker.core.domain.repository.TrainingScheduleRepository
import com.musclegain.tracker.core.domain.repository.UserProfileRepository
import com.musclegain.tracker.core.domain.repository.WorkoutRepository
import com.musclegain.tracker.core.domain.usecase.LogBodyWeightUseCase
import com.musclegain.tracker.core.domain.usecase.LogWorkoutUseCase
import com.musclegain.tracker.core.domain.usecase.ObserveDailySummaryUseCase
import com.musclegain.tracker.core.domain.usecase.ObserveWeeklySummaryUseCase

class MuscleGainContainer(
    context: Context,
    database: MuscleGainDatabase = MuscleGainDatabase.build(context)
) {
    val userProfileRepository: UserProfileRepository = UserProfileRepositoryImpl(database.userProfileDao())
    val goalsRepository: GoalsRepository = GoalsRepositoryImpl(database.goalsDao())
    val nutritionRepository: NutritionRepository = NutritionRepositoryImpl(database)
    val workoutRepository: WorkoutRepository = WorkoutRepositoryImpl(database)
    val supplementRepository: SupplementRepository = SupplementRepositoryImpl(database)
    val bodyWeightRepository: BodyWeightRepository = BodyWeightRepositoryImpl(database)
    val trainingScheduleRepository: TrainingScheduleRepository = TrainingScheduleRepositoryImpl(database.trainingScheduleDao())
    val reminderRepository: ReminderRepository = ReminderRepositoryImpl(database.reminderDao())
    val appPreferencesRepository: AppPreferencesRepository = AppPreferencesRepositoryImpl(context)
    val backupRepository: BackupRepository = BackupRepositoryImpl(database)

    val observeDailySummary = ObserveDailySummaryUseCase(
        nutritionRepository, workoutRepository, goalsRepository, trainingScheduleRepository, bodyWeightRepository
    )
    val observeWeeklySummary = ObserveWeeklySummaryUseCase(
        nutritionRepository, workoutRepository, goalsRepository, trainingScheduleRepository, bodyWeightRepository
    )
    val logWorkout = LogWorkoutUseCase(workoutRepository, userProfileRepository, bodyWeightRepository)
    val logBodyWeight = LogBodyWeightUseCase(bodyWeightRepository, userProfileRepository)
}
