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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
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
import com.musclegain.tracker.core.domain.model.WorkoutSessionWithExercises
import com.musclegain.tracker.ui.components.ConfirmDeleteDialog
import com.musclegain.tracker.ui.components.EmptyState
import com.musclegain.tracker.ui.forms.WorkoutForm
import com.musclegain.tracker.ui.theme.CoralError
import com.musclegain.tracker.ui.theme.CyanAccent
import com.musclegain.tracker.ui.theme.DarkBackground
import com.musclegain.tracker.ui.theme.DarkBorder
import com.musclegain.tracker.ui.theme.DarkSurface
import com.musclegain.tracker.ui.theme.DarkSurfaceHighlight
import com.musclegain.tracker.ui.theme.ElectricLime
import com.musclegain.tracker.ui.theme.TextMuted
import com.musclegain.tracker.ui.theme.TextPrimary
import com.musclegain.tracker.ui.theme.TextSecondary
import com.musclegain.tracker.viewmodel.WorkoutEntryViewModel
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutEntryScreen(
    viewModel: WorkoutEntryViewModel,
    initialDate: String,
    initialId: Long = 0L,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val estimate by viewModel.calorieEstimatePreview.collectAsStateWithLifecycle()
    val sessions by viewModel.sessionsForDate.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var sessionToDelete by remember { mutableStateOf<WorkoutSessionWithExercises?>(null) }

    LaunchedEffect(initialDate) {
        val parsed = runCatching { LocalDate.parse(initialDate) }.getOrNull() ?: LocalDate.now()
        viewModel.setDate(parsed)
    }

    LaunchedEffect(initialId) {
        if (initialId > 0) {
            viewModel.loadSessionForEdit(initialId)
        }
    }

    LaunchedEffect(formState.saveSuccess) {
        if (formState.saveSuccess) {
            snackbarHostState.showSnackbar("Workout saved successfully")
            viewModel.resetForm()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Log Workout", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
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
            // Workout Form
            WorkoutForm(
                state = formState,
                estimate = estimate,
                onActivityTypeChanged = viewModel::onActivityTypeChanged,
                onDurationChanged = viewModel::onDurationChanged,
                onDistanceChanged = viewModel::onDistanceChanged,
                onIntensityChanged = viewModel::onIntensityChanged,
                onStatusChanged = viewModel::onStatusChanged,
                onNotesChanged = viewModel::onNotesChanged,
                onAddExercise = viewModel::addExercise,
                onRemoveExercise = viewModel::removeExercise,
                onUpdateExercise = viewModel::updateExercise,
                onSave = { viewModel.saveWorkout() },
                onReset = { viewModel.resetForm() }
            )

            // Logged Sessions Today
            Text(
                text = "LOGGED WORKOUTS TODAY (${sessions.size})",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            if (sessions.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.FitnessCenter,
                    title = "No workouts logged today",
                    description = "Choose an activity above and save to track your exercise."
                )
            } else {
                sessions.forEach { item ->
                    WorkoutDetailCard(
                        item = item,
                        onEdit = { viewModel.loadSessionForEdit(item.session.id) },
                        onDelete = { sessionToDelete = item }
                    )
                }
            }
        }
    }

    sessionToDelete?.let { item ->
        ConfirmDeleteDialog(
            title = "Delete Workout Session",
            message = "Are you sure you want to delete this workout session? This will remove associated exercise and calorie data.",
            onDismiss = { sessionToDelete = null },
            onConfirm = {
                viewModel.deleteWorkout(item.session.id)
                sessionToDelete = null
            }
        )
    }
}

@Composable
fun WorkoutDetailCard(
    item: WorkoutSessionWithExercises,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session = item.session
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyanAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = session.type.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "${session.durationMinutes ?: 0} min",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CoralError, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Distance / Pace if available
            session.distanceKm?.let { km ->
                Text(
                    text = "Distance: ${String.format(Locale.US, "%.1f", km)} km",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Exercise count or list
            if (item.exercises.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceHighlight)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item.exercises.forEach { ex ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(ex.name, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            Text("${ex.sets} sets x ${ex.reps} reps @ ${ex.weightKg} kg", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }

            // Calorie estimate
            if (session.estimatedActiveKcal != null) {
                Text(
                    text = "Estimated: ${String.format(Locale.US, "%.0f", session.estimatedActiveKcal)} active kcal",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricLime,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
