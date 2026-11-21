package com.musclegain.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musclegain.tracker.core.domain.calc.WorkoutCalorieEstimator
import com.musclegain.tracker.core.domain.model.ActivityType
import com.musclegain.tracker.core.domain.model.DailySummary
import com.musclegain.tracker.core.domain.model.WorkoutSessionWithExercises
import com.musclegain.tracker.ui.theme.CyanAccent
import com.musclegain.tracker.ui.theme.DarkBorder
import com.musclegain.tracker.ui.theme.DarkSurface
import com.musclegain.tracker.ui.theme.DarkSurfaceHighlight
import com.musclegain.tracker.ui.theme.ElectricLime
import com.musclegain.tracker.ui.theme.EmeraldSuccess
import com.musclegain.tracker.ui.theme.TextMuted
import com.musclegain.tracker.ui.theme.TextPrimary
import com.musclegain.tracker.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun WorkoutCard(
    summary: DailySummary?,
    onCardClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val plannedDay = summary?.plannedDay
    val completedWorkouts = summary?.workouts ?: emptyList()
    val completedCount = summary?.completedWorkoutCount ?: 0

    val isRestDay = plannedDay?.plannedRest == true
    val hasPlannedGym = plannedDay?.plannedGym == true
    val hasPlannedRunning = plannedDay?.plannedRunning == true

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .clickable { onCardClick() }
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceHighlight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Workout",
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "TODAY'S WORKOUT",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = if (isRestDay) "Planned Rest Day" else buildString {
                                val plans = mutableListOf<String>()
                                if (hasPlannedGym) plans.add("Gym")
                                if (hasPlannedRunning) plans.add("Running")
                                if (plans.isEmpty()) append("Unscheduled") else append(plans.joinToString(" + "))
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Completion status chip
                val statusText = when {
                    completedCount > 0 -> "Completed ($completedCount)"
                    isRestDay -> "Rest Day"
                    else -> "Pending"
                }
                val statusColor = when {
                    completedCount > 0 -> EmeraldSuccess
                    isRestDay -> TextSecondary
                    else -> CyanAccent
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Completed Sessions List or Empty State
            if (completedWorkouts.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    completedWorkouts.forEach { item ->
                        WorkoutSessionItemRow(item)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceHighlight)
                        .padding(vertical = 14.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isRestDay) "Enjoy your rest day! Active recovery recommended." else "No workouts logged yet today. Tap + to log.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // Total Calorie Expenditure footer (if available)
            if (summary?.estimatedGrossKcal != null || summary?.estimatedActiveKcal != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceHighlight)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Calories",
                            tint = ElectricLime,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Activity Calories:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${String.format(Locale.US, "%.0f", summary.estimatedActiveKcal ?: summary.estimatedGrossKcal)} kcal active",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricLime,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = WorkoutCalorieEstimator.ESTIMATE_LABEL,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutSessionItemRow(item: WorkoutSessionWithExercises) {
    val session = item.session
    val icon = when (session.type) {
        ActivityType.GYM -> Icons.Default.FitnessCenter
        ActivityType.RUNNING, ActivityType.WALKING -> Icons.Default.DirectionsRun
        ActivityType.OTHER_CARDIO -> Icons.Default.FitnessCenter
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceHighlight)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = session.type.name,
                tint = CyanAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = when (session.type) {
                        ActivityType.GYM -> "Gym Session"
                        ActivityType.RUNNING -> "Running"
                        ActivityType.WALKING -> "Walking"
                        ActivityType.OTHER_CARDIO -> "Other Cardio"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                val details = buildList {
                    session.durationMinutes?.let { add("${it} min") }
                    session.distanceKm?.let { add("${String.format(Locale.US, "%.1f", it)} km") }
                    if (session.type == ActivityType.GYM && item.exercises.isNotEmpty()) {
                        add("${item.exercises.size} exercises")
                    }
                }
                if (details.isNotEmpty()) {
                    Text(
                        text = details.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        if (session.estimatedActiveKcal != null) {
            Text(
                text = "${String.format(Locale.US, "%.0f", session.estimatedActiveKcal)} kcal",
                style = MaterialTheme.typography.bodyMedium,
                color = ElectricLime,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
