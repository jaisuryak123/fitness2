package com.musclegain.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.musclegain.tracker.notification.NotificationHelper
import com.musclegain.tracker.ui.navigation.AppNavGraph
import com.musclegain.tracker.ui.theme.MuscleGainTheme
import com.musclegain.tracker.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MuscleGainApp
        val container = app.container
        val viewModelFactory = ViewModelFactory(container)

        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this)

        setContent {
            MuscleGainTheme(darkTheme = true) {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    factory = viewModelFactory
                )
            }
        }
    }
}
