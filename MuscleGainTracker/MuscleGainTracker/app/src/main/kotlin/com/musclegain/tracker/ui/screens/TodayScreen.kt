package com.musclegain.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musclegain.tracker.ui.components.LogWeightDialog
import com.musclegain.tracker.ui.components.NutritionCard
import com.musclegain.tracker.ui.components.QuickActionButtons
import com.musclegain.tracker.ui.components.WorkoutCard
import com.musclegain.tracker.ui.theme.DarkBackground
import com.musclegain.tracker.ui.theme.DarkBorder
import com.musclegain.tracker.ui.theme.DarkSurface
import com.musclegain.tracker.ui.theme.DarkSurfaceHighlight
import com.musclegain.tracker.ui.theme.ElectricLime
import com.musclegain.tracker.ui.theme.EmeraldSuccess
import com.musclegain.tracker.ui.theme.PurpleAccent
import com.musclegain.tracker.ui.theme.TextMuted
import com.musclegain.tracker.ui.theme.TextPrimary
import com.musclegain.tracker.ui.theme.TextSecondary
import com.musclegain.tracker.viewmodel.TodayViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onNavigateToFoodEntry: (date: String) -> Unit,
    onNavigateToWorkoutEntry: (date: String) -> Unit,
    onNavigateToSupplementLog: (date: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val dailySummary by viewModel.dailySummary.collectAsStateWithLifecycle()
    val userFeedback by viewModel.userFeedback.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showWeightDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userFeedback) {
        userFeedback?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    val isToday = selectedDate == LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", Locale.US)
    val dateString = selectedDate.toString()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isToday) "TODAY" else "DASHBOARD",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (isToday) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ElectricLime.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricLime,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                        Text(
                            text = selectedDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    // Date stepper
                    IconButton(onClick = { viewModel.setDate(selectedDate.minusDays(1)) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day", tint = TextSecondary)
                    }
                    if (!isToday) {
                        IconButton(onClick = { viewModel.setDate(LocalDate.now()) }) {
                            Icon(imageVector = Icons.Default.Today, contentDescription = "Jump to Today", tint = ElectricLime)
                        }
                    }
                    IconButton(onClick = { viewModel.setDate(selectedDate.plusDays(1)) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Brief Factual Progress Message
            dailySummary?.let { summary ->
                val consumed = summary.totals?.calories
                val target = summary.goals.calorieTarget
                val completedWorkouts = summary.completedWorkoutCount

                val progressMessage = buildString {
                    if (consumed != null) {
                        append("${String.format(Locale.US, "%.0f", consumed)} / ${String.format(Locale.US, "%.0f", target)} kcal")
                        val diff = target - consumed
                        if (diff >= 0) {
                            append(" (${String.format(Locale.US, "%.0f", diff)} remaining)")
                        } else {
                            append(" (${String.format(Locale.US, "%.0f", -diff)} over)")
                        }
                    } else {
                        append("Target: ${String.format(Locale.US, "%.0f", target)} kcal")
                    }
                    append(" • ")
                    if (completedWorkouts > 0) {
                        append("$completedWorkouts workout${if (completedWorkouts > 1) "s" else ""} completed")
                    } else if (summary.plannedDay?.plannedRest == true) {
                        append("Rest day")
                    } else {
                        append("No workout logged")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceHighlight)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ElectricLime)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = progressMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Quick Action Buttons
            QuickActionButtons(
                onAddFood = { onNavigateToFoodEntry(dateString) },
                onAddWorkout = { onNavigateToWorkoutEntry(dateString) },
                onAddSupplement = { onNavigateToSupplementLog(dateString) },
                onLogWeight = { showWeightDialog = true }
            )

            // Primary Nutrition Card
            NutritionCard(
                summary = dailySummary,
                onCardClick = { onNavigateToFoodEntry(dateString) }
            )

            // Workout Status Card
            WorkoutCard(
                summary = dailySummary,
                onCardClick = { onNavigateToWorkoutEntry(dateString) }
            )

            // Body Weight Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .clickable { showWeightDialog = true }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PurpleAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonitorWeight,
                                contentDescription = "Body Weight",
                                tint = PurpleAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "BODY WEIGHT",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            val weight = dailySummary?.bodyWeightKg
                            Text(
                                text = if (weight != null) "${String.format(Locale.US, "%.1f", weight)} kg" else "Not logged today",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (weight != null) TextPrimary else TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        text = if (dailySummary?.bodyWeightKg != null) "Update" else "+ Log",
                        style = MaterialTheme.typography.labelMedium,
                        color = PurpleAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showWeightDialog) {
        LogWeightDialog(
            initialWeight = dailySummary?.bodyWeightKg,
            onDismiss = { showWeightDialog = false },
            onConfirm = { weight, notes ->
                viewModel.logWeight(weight, notes)
                showWeightDialog = false
            }
        )
    }
}
