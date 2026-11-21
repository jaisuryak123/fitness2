package com.musclegain.tracker.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.musclegain.tracker.ui.screens.BackupScreen
import com.musclegain.tracker.ui.screens.FoodEntryScreen
import com.musclegain.tracker.ui.screens.HistoryScreen
import com.musclegain.tracker.ui.screens.NotificationSettingsScreen
import com.musclegain.tracker.ui.screens.ProfileScreen
import com.musclegain.tracker.ui.screens.SupplementLogScreen
import com.musclegain.tracker.ui.screens.TodayScreen
import com.musclegain.tracker.ui.screens.TrainingPlanScreen
import com.musclegain.tracker.ui.screens.WeeklyScreen
import com.musclegain.tracker.ui.screens.WorkoutEntryScreen
import com.musclegain.tracker.ui.theme.DarkBackground
import com.musclegain.tracker.ui.theme.DarkBorder
import com.musclegain.tracker.ui.theme.DarkSurface
import com.musclegain.tracker.ui.theme.ElectricLime
import com.musclegain.tracker.ui.theme.OnElectricLime
import com.musclegain.tracker.ui.theme.TextMuted
import com.musclegain.tracker.ui.theme.TextSecondary
import com.musclegain.tracker.viewmodel.BackupViewModel
import com.musclegain.tracker.viewmodel.FoodEntryViewModel
import com.musclegain.tracker.viewmodel.HistoryViewModel
import com.musclegain.tracker.viewmodel.NotificationViewModel
import com.musclegain.tracker.viewmodel.ProfileViewModel
import com.musclegain.tracker.viewmodel.TodayViewModel
import com.musclegain.tracker.viewmodel.TrainingPlanViewModel
import com.musclegain.tracker.viewmodel.ViewModelFactory
import com.musclegain.tracker.viewmodel.WeeklyViewModel
import com.musclegain.tracker.viewmodel.WorkoutEntryViewModel
import java.time.LocalDate

@Composable
fun AppNavGraph(
    navController: NavHostController,
    factory: ViewModelFactory,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Today.route,
        Screen.History.route,
        Screen.Weekly.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder)
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(imageVector = item.icon, contentDescription = item.title)
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OnElectricLime,
                                selectedTextColor = ElectricLime,
                                indicatorColor = ElectricLime,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextMuted
                            )
                        )
                    }
                }
            }
        },
        containerColor = DarkBackground,
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Today.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Bottom Bar Destinations
            composable(Screen.Today.route) {
                val viewModel: TodayViewModel = viewModel(factory = factory)
                TodayScreen(
                    viewModel = viewModel,
                    onNavigateToFoodEntry = { date ->
                        navController.navigate(Screen.FoodEntry.createRoute(date))
                    },
                    onNavigateToWorkoutEntry = { date ->
                        navController.navigate(Screen.WorkoutEntry.createRoute(date))
                    },
                    onNavigateToSupplementLog = { date ->
                        navController.navigate(Screen.SupplementLog.createRoute(date))
                    }
                )
            }

            composable(Screen.History.route) {
                val viewModel: HistoryViewModel = viewModel(factory = factory)
                HistoryScreen(
                    viewModel = viewModel,
                    onNavigateToEditFood = { date, id ->
                        navController.navigate(Screen.FoodEntry.createRoute(date, id))
                    },
                    onNavigateToEditWorkout = { date, id ->
                        navController.navigate(Screen.WorkoutEntry.createRoute(date, id))
                    }
                )
            }

            composable(Screen.Weekly.route) {
                val viewModel: WeeklyViewModel = viewModel(factory = factory)
                WeeklyScreen(viewModel = viewModel)
            }

            composable(Screen.Profile.route) {
                val viewModel: ProfileViewModel = viewModel(factory = factory)
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToTrainingPlan = { navController.navigate(Screen.TrainingPlan.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.NotificationSettings.route) },
                    onNavigateToBackup = { navController.navigate(Screen.Backup.route) }
                )
            }

            // Food Entry / Edit Screen
            composable(
                route = Screen.FoodEntry.route,
                arguments = listOf(
                    navArgument("date") { type = NavType.StringType; defaultValue = LocalDate.now().toString() },
                    navArgument("id") { type = NavType.LongType; defaultValue = 0L }
                )
            ) { backStackEntry ->
                val date = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString()
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                val viewModel: FoodEntryViewModel = viewModel(factory = factory)
                FoodEntryScreen(
                    viewModel = viewModel,
                    initialDate = date,
                    initialId = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Workout Entry / Edit Screen
            composable(
                route = Screen.WorkoutEntry.route,
                arguments = listOf(
                    navArgument("date") { type = NavType.StringType; defaultValue = LocalDate.now().toString() },
                    navArgument("id") { type = NavType.LongType; defaultValue = 0L }
                )
            ) { backStackEntry ->
                val date = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString()
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                val viewModel: WorkoutEntryViewModel = viewModel(factory = factory)
                WorkoutEntryScreen(
                    viewModel = viewModel,
                    initialDate = date,
                    initialId = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Training Plan
            composable(Screen.TrainingPlan.route) {
                val viewModel: TrainingPlanViewModel = viewModel(factory = factory)
                TrainingPlanScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Supplement Log
            composable(
                route = Screen.SupplementLog.route,
                arguments = listOf(
                    navArgument("date") { type = NavType.StringType; defaultValue = LocalDate.now().toString() }
                )
            ) { backStackEntry ->
                val date = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString()
                val viewModel: ProfileViewModel = viewModel(factory = factory)
                SupplementLogScreen(
                    viewModel = viewModel,
                    initialDate = date,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Notification Settings
            composable(Screen.NotificationSettings.route) {
                val viewModel: NotificationViewModel = viewModel(factory = factory)
                NotificationSettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Backup & Export
            composable(Screen.Backup.route) {
                val viewModel: BackupViewModel = viewModel(factory = factory)
                BackupScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
