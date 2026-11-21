package com.musclegain.tracker.ui.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musclegain.tracker.core.domain.calc.CalorieEstimate
import com.musclegain.tracker.core.domain.calc.WorkoutCalorieEstimator
import com.musclegain.tracker.core.domain.model.ActivityType
import com.musclegain.tracker.core.domain.model.CompletionStatus
import com.musclegain.tracker.core.domain.model.Intensity
import com.musclegain.tracker.ui.theme.AmberAccent
import com.musclegain.tracker.ui.theme.CoralError
import com.musclegain.tracker.ui.theme.CyanAccent
import com.musclegain.tracker.ui.theme.DarkBackground
import com.musclegain.tracker.ui.theme.DarkBorder
import com.musclegain.tracker.ui.theme.DarkSurface
import com.musclegain.tracker.ui.theme.DarkSurfaceHighlight
import com.musclegain.tracker.ui.theme.ElectricLime
import com.musclegain.tracker.ui.theme.OnElectricLime
import com.musclegain.tracker.ui.theme.TextMuted
import com.musclegain.tracker.ui.theme.TextPrimary
import com.musclegain.tracker.ui.theme.TextSecondary
import com.musclegain.tracker.viewmodel.WorkoutExerciseForm
import com.musclegain.tracker.viewmodel.WorkoutFormState
import java.util.Locale

@Composable
fun WorkoutForm(
    state: WorkoutFormState,
    estimate: CalorieEstimate?,
    onActivityTypeChanged: (ActivityType) -> Unit,
    onDurationChanged: (String) -> Unit,
    onDistanceChanged: (String) -> Unit,
    onIntensityChanged: (Intensity) -> Unit,
    onStatusChanged: (CompletionStatus) -> Unit,
    onNotesChanged: (String) -> Unit,
    onAddExercise: () -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onUpdateExercise: (Int, WorkoutExerciseForm) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = if (state.id == 0L) "LOG WORKOUT SESSION" else "EDIT WORKOUT SESSION",
                style = MaterialTheme.typography.labelSmall,
                color = CyanAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            // Activity Type Selector Tabs
            TabRow(
                selectedTabIndex = state.type.ordinal,
                containerColor = DarkSurfaceHighlight,
                contentColor = CyanAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[state.type.ordinal]),
                        color = CyanAccent
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                ActivityType.values().forEach { type ->
                    val selected = state.type == type
                    Tab(
                        selected = selected,
                        onClick = { onActivityTypeChanged(type) },
                        text = {
                            Text(
                                text = when (type) {
                                    ActivityType.GYM -> "Gym"
                                    ActivityType.RUNNING -> "Running"
                                    ActivityType.WALKING -> "Walking"
                                    ActivityType.OTHER_CARDIO -> "Cardio"
                                },
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) CyanAccent else TextSecondary
                            )
                        }
                    )
                }
            }

            // Common Duration Field
            OutlinedTextField(
                value = state.durationMinutes,
                onValueChange = onDurationChanged,
                label = { Text("Duration (minutes) *") },
                placeholder = { Text("e.g. 45") },
                singleLine = true,
                isError = state.errors.containsKey("durationMinutes"),
                supportingText = state.errors["durationMinutes"]?.let { { Text(it, color = CoralError) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            // Distance Field (for Running and Walking)
            if (state.type == ActivityType.RUNNING || state.type == ActivityType.WALKING) {
                OutlinedTextField(
                    value = state.distanceKm,
                    onValueChange = onDistanceChanged,
                    label = { Text("Distance (km) *") },
                    placeholder = { Text("e.g. 5.0") },
                    singleLine = true,
                    isError = state.errors.containsKey("distanceKm"),
                    supportingText = state.errors["distanceKm"]?.let { { Text(it, color = CoralError) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                // Speed / Pace Preview
                if (estimate?.averageSpeedKmh != null) {
                    val speed = estimate.averageSpeedKmh
                    val paceMinPerKm = if (speed > 0) 60.0 / speed else 0.0
                    val paceMinutes = paceMinPerKm.toInt()
                    val paceSeconds = ((paceMinPerKm - paceMinutes) * 60).toInt()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceHighlight)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Calculated Speed & Pace:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Text(
                            text = String.format(Locale.US, "%.1f km/h (%d:%02d /km)", speed, paceMinutes, paceSeconds),
                            style = MaterialTheme.typography.labelMedium,
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Intensity Selector (Gym and Other Cardio)
            if (state.type == ActivityType.GYM || state.type == ActivityType.OTHER_CARDIO) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Workout Intensity", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Intensity.values().forEach { intensity ->
                            val selected = state.intensity == intensity
                            FilterChip(
                                selected = selected,
                                onClick = { onIntensityChanged(intensity) },
                                label = { Text(intensity.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanAccent,
                                    selectedLabelColor = OnElectricLime,
                                    containerColor = DarkSurfaceHighlight,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }

            // GYM: Exercise Builder
            if (state.type == ActivityType.GYM) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EXERCISES (${state.exercises.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(
                            onClick = onAddExercise,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Exercise")
                        }
                    }

                    state.exercises.forEachIndexed { index, ex ->
                        ExerciseRowItem(
                            index = index,
                            exercise = ex,
                            canDelete = state.exercises.size > 1,
                            onUpdate = { updated -> onUpdateExercise(index, updated) },
                            onDelete = { onRemoveExercise(index) }
                        )
                    }
                }
            }

            // Completion Status
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Completion Status", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompletionStatus.values().forEach { status ->
                        val selected = state.status == status
                        FilterChip(
                            selected = selected,
                            onClick = { onStatusChanged(status) },
                            label = { Text(status.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricLime,
                                selectedLabelColor = OnElectricLime,
                                containerColor = DarkSurfaceHighlight,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }

            // Real-time Calorie Estimate Preview
            if (estimate != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceHighlight)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Estimated Calories:", style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text(WorkoutCalorieEstimator.ESTIMATE_LABEL, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                        }
                    }
                    Text(
                        text = "${String.format(Locale.US, "%.0f", estimate.activeKcal)} active (${String.format(Locale.US, "%.0f", estimate.grossKcal)} gross)",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricLime,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Optional Notes
            OutlinedTextField(
                value = state.notes,
                onValueChange = onNotesChanged,
                label = { Text("Optional Notes") },
                placeholder = { Text("e.g. Felt strong on bench press today") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            // Submit Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (state.id != 0L) {
                    OutlinedButton(
                        onClick = onReset,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Cancel")
                    }
                }

                Button(
                    onClick = onSave,
                    enabled = !state.isSubmitting,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = OnElectricLime
                    )
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = OnElectricLime,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (state.id == 0L) "Log Workout" else "Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseRowItem(
    index: Int,
    exercise: WorkoutExerciseForm,
    canDelete: Boolean,
    onUpdate: (WorkoutExerciseForm) -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceHighlight)
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Exercise #${index + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold
                )
                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CoralError, modifier = Modifier.size(16.dp))
                    }
                }
            }

            OutlinedTextField(
                value = exercise.name,
                onValueChange = { onUpdate(exercise.copy(name = it)) },
                label = { Text("Exercise Name") },
                placeholder = { Text("e.g. Barbell Squat") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = exercise.sets,
                    onValueChange = { onUpdate(exercise.copy(sets = it)) },
                    label = { Text("Sets") },
                    placeholder = { Text("3") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors()
                )

                OutlinedTextField(
                    value = exercise.reps,
                    onValueChange = { onUpdate(exercise.copy(reps = it)) },
                    label = { Text("Reps") },
                    placeholder = { Text("10") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors()
                )

                OutlinedTextField(
                    value = exercise.weightKg,
                    onValueChange = { onUpdate(exercise.copy(weightKg = it)) },
                    label = { Text("Weight (kg)") },
                    placeholder = { Text("60") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors()
                )
            }
        }
    }
}
