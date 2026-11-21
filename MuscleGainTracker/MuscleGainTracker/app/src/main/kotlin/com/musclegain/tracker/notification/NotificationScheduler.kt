package com.musclegain.tracker.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.musclegain.tracker.core.domain.model.ReminderSettings
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun scheduleReminders(context: Context, settings: ReminderSettings) {
        val workManager = WorkManager.getInstance(context)

        // Meal logging reminder
        if (settings.meal.enabled) {
            val data = Data.Builder()
                .putString("title", "Nutrition Check-in")
                .putString("message", "Log your lunch and review your remaining protein target for today.")
                .build()

            val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
                .setInputData(data)
                .build()

            workManager.enqueueUniquePeriodicWork("reminder_meal", ExistingPeriodicWorkPolicy.UPDATE, request)
        } else {
            workManager.cancelUniqueWork("reminder_meal")
        }

        // Workout reminder
        if (settings.workout.enabled) {
            val data = Data.Builder()
                .putString("title", "Workout Time")
                .putString("message", "Time for today's planned training session. Stay consistent for muscle gain!")
                .build()

            val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
                .setInputData(data)
                .build()

            workManager.enqueueUniquePeriodicWork("reminder_workout", ExistingPeriodicWorkPolicy.UPDATE, request)
        } else {
            workManager.cancelUniqueWork("reminder_workout")
        }

        // Body weight reminder
        if (settings.bodyWeight.enabled) {
            val data = Data.Builder()
                .putString("title", "Morning Weight Log")
                .putString("message", "Track your morning body weight to keep accurate calorie calculations.")
                .build()

            val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
                .setInputData(data)
                .build()

            workManager.enqueueUniquePeriodicWork("reminder_weight", ExistingPeriodicWorkPolicy.UPDATE, request)
        } else {
            workManager.cancelUniqueWork("reminder_weight")
        }

        // Supplement reminder
        if (settings.supplement.enabled) {
            val data = Data.Builder()
                .putString("title", "Supplements Check")
                .putString("message", "Don't forget your daily creatine, protein, or vitamins.")
                .build()

            val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
                .setInputData(data)
                .build()

            workManager.enqueueUniquePeriodicWork("reminder_supplement", ExistingPeriodicWorkPolicy.UPDATE, request)
        } else {
            workManager.cancelUniqueWork("reminder_supplement")
        }
    }
}
