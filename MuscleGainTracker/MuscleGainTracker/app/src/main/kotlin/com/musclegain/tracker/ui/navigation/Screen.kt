package com.musclegain.tracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    // Bottom navigation main tabs
    data object Today : Screen("today")
    data object History : Screen("history")
    data object Weekly : Screen("weekly")
    data object Profile : Screen("profile")

    // Form and detail destinations
    data object FoodEntry : Screen("food_entry?date={date}&id={id}") {
        fun createRoute(date: String, id: Long = 0L) = "food_entry?date=$date&id=$id"
    }

    data object WorkoutEntry : Screen("workout_entry?date={date}&id={id}") {
        fun createRoute(date: String, id: Long = 0L) = "workout_entry?date=$date&id=$id"
    }

    data object TrainingPlan : Screen("training_plan")
    
    data object SupplementLog : Screen("supplement_log?date={date}") {
        fun createRoute(date: String) = "supplement_log?date=$date"
    }

    data object NotificationSettings : Screen("notification_settings")
    data object Backup : Screen("backup")
}

data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Today, "Today", Icons.Default.Today),
    BottomNavItem(Screen.History, "History", Icons.Default.CalendarMonth),
    BottomNavItem(Screen.Weekly, "Progress", Icons.Default.Equalizer),
    BottomNavItem(Screen.Profile, "Profile", Icons.Default.Person)
)
