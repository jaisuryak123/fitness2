package com.musclegain.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musclegain.tracker.core.domain.calc.WorkoutCalorieEstimator
import com.musclegain.tracker.core.domain.model.DayAdherence
import com.musclegain.tracker.core.domain.model.SessionState
import com.musclegain.tracker.ui.components.DayBarData
import com.musclegain.tracker.ui.components.StatCard
import com.musclegain.tracker.ui.components.WeeklyBarChart
import com.musclegain.tracker.ui.components.WeightDataPoint
import com.musclegain.tracker.ui.components.WeightTrendChart
import com.musclegain.tracker.ui.theme.CyanAccent
import com.musclegain.tracker.ui.theme.DarkBackground
import com.musclegain.tracker.ui.theme.DarkBorder
import com.musclegain.tracker.ui.theme.DarkSurface
import com.musclegain.tracker.ui.theme.DarkSurfaceHighlight
import com.musclegain.tracker.ui.theme.ElectricLime
import com.musclegain.tracker.ui.theme.EmeraldSuccess
import com.musclegain.tracker.ui.theme.MacroColors
import com.musclegain.tracker.ui.theme.OnElectricLime
import com.musclegain.tracker.ui.theme.RoseAccent
import com.musclegain.tracker.ui.theme.TextMuted
import com.musclegain.tracker.ui.theme.TextPrimary
import com.musclegain.tracker.ui.theme.TextSecondary
import com.musclegain.tracker.viewmodel.WeeklyViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyScreen(
    viewModel: WeeklyViewModel,
    modifier: Modifier = Modifier
) {
    val weeklySummary by viewModel.weeklySummary.collectAsStateWithLifecycle()
    val selectedWeekDate by viewModel.selectedWeekDate.collectAsStateWithLifecycle()

    val weekStart = weeklySummary?.weekStart ?: selectedWeekDate
    val weekEnd = weeklySummary?.weekEnd ?: selectedWeekDate.plusDays(6)
    val weekFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "WEEKLY PERFORMANCE",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${weekStart.format(weekFormatter)} – ${weekEnd.format(weekFormatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::previousWeek) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Week", tint = TextSecondary)
                    }
                    IconButton(onClick = viewModel::currentWeek) {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = "Current Week", tint = ElectricLime)
                    }
                    IconButton(onClick = viewModel::nextWeek) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Week", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val summary = weeklySummary

            // Key KPI Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val adherence = summary?.workoutAdherencePercent
                StatCard(
                    title = "Adherence",
                    value = if (adherence != null) "${String.format(Locale.US, "%.0f", adherence)}%" else "—",
                    subtitle = if (adherence != null) "Weekly Target" else "Pending sessions",
                    accentColor = ElectricLime,
                    modifier = Modifier.weight(1f)
                )

                val loggingRate = summary?.trackingCompletionRatePercent ?: 0.0
                StatCard(
                    title = "Nutrition Log",
                    value = "${String.format(Locale.US, "%.0f", loggingRate)}%",
                    subtitle = "${summary?.daysWithNutrition ?: 0}/7 days recorded",
                    accentColor = CyanAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            // Daily Calorie Intake Bar Chart
            val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val calorieData = dayLabels.mapIndexed { index, label ->
                val date = weekStart.plusDays(index.toLong())
                DayBarData(
                    dayLabel = label,
                    value = null, // Will populate or default
                    isToday = date == LocalDate.now()
                )
            }
            WeeklyBarChart(
                title = "Daily Calorie Intake",
                data = calorieData,
                targetValue = 2800.0,
                unit = "kcal",
                barColor = ElectricLime
            )

            // Daily Protein Intake Bar Chart
            val proteinData = dayLabels.mapIndexed { index, label ->
                val date = weekStart.plusDays(index.toLong())
                DayBarData(
                    dayLabel = label,
                    value = null,
                    isToday = date == LocalDate.now()
                )
            }
            WeeklyBarChart(
                title = "Daily Protein Intake",
                data = proteinData,
                targetValue = 140.0,
                unit = "g",
                barColor = CyanAccent
            )

            // Weekly Averages Summary Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "NUTRITION AVERAGES (RECORDED DAYS)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Calories", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                text = if (summary?.averageDailyCalories != null) "${String.format(Locale.US, "%.0f", summary.averageDailyCalories)} kcal" else "Not logged",
                                style = MaterialTheme.typography.titleMedium,
                                color = ElectricLime,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text("Protein", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                text = if (summary?.averageProteinG != null) "${String.format(Locale.US, "%.0f", summary.averageProteinG)} g" else "Not logged",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text("Carbs", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                text = if (summary?.averageCarbsG != null) "${String.format(Locale.US, "%.0f", summary.averageCarbsG)} g" else "Not logged",
                                style = MaterialTheme.typography.titleMedium,
                                color = MacroColors.Carbs,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text("Fat", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                text = if (summary?.averageFatG != null) "${String.format(Locale.US, "%.0f", summary.averageFatG)} g" else "Not logged",
                                style = MaterialTheme.typography.titleMedium,
                                color = MacroColors.Fat,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Protein target achieved: ${summary?.proteinTargetDaysAchieved ?: 0} / 7 days",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }

            // Workout Adherence & Breakdown Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "WORKOUT TOTALS & SESSIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Gym Sessions",
                            value = "${summary?.gymSessionsCompleted ?: 0} / ${summary?.gymSessionsPlanned ?: 5}",
                            subtitle = "Planned vs done",
                            icon = Icons.Default.FitnessCenter,
                            accentColor = CyanAccent,
                            modifier = Modifier.weight(1f)
                        )

                        StatCard(
                            title = "Running",
                            value = "${summary?.runningSessionsCompleted ?: 0} / ${summary?.runningSessionsPlanned ?: 2}",
                            subtitle = "${String.format(Locale.US, "%.1f", summary?.totalRunningDistanceKm ?: 0.0)} km total",
                            icon = Icons.Default.DirectionsRun,
                            accentColor = ElectricLime,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Total Workout Time:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                        Text(
                            text = "${summary?.totalWorkoutDurationMinutes ?: 0} minutes",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (summary?.estimatedActiveKcal != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Activity Energy:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            }
                            Text(
                                text = "${String.format(Locale.US, "%.0f", summary.estimatedActiveKcal)} kcal",
                                style = MaterialTheme.typography.bodyLarge,
                                color = ElectricLime,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 7-Day Adherence Breakdown
            summary?.days?.let { days ->
                DayAdherenceRow(days = days)
            }

            // Weight Trend Chart
            WeightTrendChart(dataPoints = emptyList())
        }
    }
}

@Composable
fun DayAdherenceRow(days: List<DayAdherence>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "DAILY SCHEDULE & ATTENDANCE",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = day.weekday.name.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )

                        val isToday = day.date == LocalDate.now()
                        val hasMissed = day.plannedSessions.any { it.state == SessionState.MISSED }
                        val allCompleted = day.plannedSessions.isNotEmpty() && day.plannedSessions.all { it.state == SessionState.COMPLETED }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        day.isRestDay -> DarkSurfaceHighlight
                                        allCompleted -> EmeraldSuccess.copy(alpha = 0.2f)
                                        hasMissed -> RoseAccent.copy(alpha = 0.2f)
                                        else -> DarkSurfaceHighlight
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isToday) ElectricLime else DarkBorder,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                day.isRestDay -> Text("REST", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 7.sp)
                                allCompleted -> Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Done", tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                                hasMissed -> Icon(imageVector = Icons.Default.Close, contentDescription = "Missed", tint = RoseAccent, modifier = Modifier.size(18.dp))
                                else -> Icon(imageVector = Icons.Default.Pending, contentDescription = "Pending", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
