package com.musclegain.tracker.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val title = inputData.getString("title") ?: "MuscleGainTracker Reminder"
        val message = inputData.getString("message") ?: "Time for your daily fitness check-in!"

        NotificationHelper.showReminderNotification(applicationContext, title, message)
        return Result.success()
    }
}
