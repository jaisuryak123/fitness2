package com.musclegain.tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.musclegain.tracker.core.data.MuscleGainContainer

class ViewModelFactory(
    private val container: MuscleGainContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(TodayViewModel::class.java) -> {
                TodayViewModel(
                    observeDailySummary = container.observeDailySummary,
                    logBodyWeight = container.logBodyWeight,
                    workoutRepository = container.workoutRepository
                ) as T
            }
            modelClass.isAssignableFrom(FoodEntryViewModel::class.java) -> {
                FoodEntryViewModel(
                    nutritionRepository = container.nutritionRepository
                ) as T
            }
            modelClass.isAssignableFrom(WorkoutEntryViewModel::class.java) -> {
                WorkoutEntryViewModel(
                    logWorkout = container.logWorkout,
                    workoutRepository = container.workoutRepository,
                    bodyWeightRepository = container.bodyWeightRepository,
                    userProfileRepository = container.userProfileRepository
                ) as T
            }
            modelClass.isAssignableFrom(WeeklyViewModel::class.java) -> {
                WeeklyViewModel(
                    observeWeeklySummary = container.observeWeeklySummary
                ) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(
                    observeDailySummary = container.observeDailySummary,
                    nutritionRepository = container.nutritionRepository,
                    workoutRepository = container.workoutRepository,
                    supplementRepository = container.supplementRepository,
                    bodyWeightRepository = container.bodyWeightRepository
                ) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(
                    userProfileRepository = container.userProfileRepository,
                    goalsRepository = container.goalsRepository,
                    bodyWeightRepository = container.bodyWeightRepository,
                    supplementRepository = container.supplementRepository,
                    logBodyWeight = container.logBodyWeight
                ) as T
            }
            modelClass.isAssignableFrom(TrainingPlanViewModel::class.java) -> {
                TrainingPlanViewModel(
                    trainingScheduleRepository = container.trainingScheduleRepository,
                    goalsRepository = container.goalsRepository
                ) as T
            }
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> {
                NotificationViewModel(
                    reminderRepository = container.reminderRepository
                ) as T
            }
            modelClass.isAssignableFrom(BackupViewModel::class.java) -> {
                BackupViewModel(
                    backupRepository = container.backupRepository,
                    appPreferencesRepository = container.appPreferencesRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
