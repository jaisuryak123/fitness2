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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.musclegain.tracker.core.domain.model.DayOfWeek
import com.musclegain.tracker.ui.forms.textFieldColors
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
import com.musclegain.tracker.viewmodel.TrainingPlanViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPlanScreen(
    viewModel: TrainingPlanViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val schedule by viewModel.schedule.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var gymTargetText by remember(goals) { mutableStateOf(goals.weeklyGymTarget.toString()) }
    var runTargetText by remember(goals) { mutableStateOf(goals.weeklyRunningTarget.toString()) }
    var distTargetText by remember(goals) { mutableStateOf(goals.runningDistanceTargetKm.toString()) }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Weekly Training Plan", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
            // Training Plan Philosophy & Rules
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceHighlight)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "A missed planned workout is factored into your weekly adherence, but rest days never count as failures. Calorie and nutrition goals are never decreased if a session is missed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Target Frequency Configuration
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
                        text = "WEEKLY TARGETS",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricLime,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = gymTargetText,
                            onValueChange = { gymTargetText = it },
                            label = { Text("Gym Target") },
                            modifier = Modifier.weight(1f),
                            colors = textFieldColors()
                        )

                        OutlinedTextField(
                            value = runTargetText,
                            onValueChange = { runTargetText = it },
                            label = { Text("Running Target") },
                            modifier = Modifier.weight(1f),
                            colors = textFieldColors()
                        )

                        OutlinedTextField(
                            value = distTargetText,
                            onValueChange = { distTargetText = it },
                            label = { Text("Distance (km)") },
                            modifier = Modifier.weight(1f),
                            colors = textFieldColors()
                        )
                    }

                    Button(
                        onClick = {
                            val gym = gymTargetText.toIntOrNull() ?: 5
                            val run = runTargetText.toIntOrNull() ?: 2
                            val dist = distTargetText.toDoubleOrNull() ?: 5.0
                            viewModel.updateGoals(gym, run, dist)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnElectricLime)
                    ) {
                        Text("Save Targets", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 7-Day Plan Configuration
            Text(
                text = "PLANNED WEEKDAYS (MONDAY – SUNDAY)",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            schedule.forEach { day ->
                Box(
                    modifier = Modifier
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
                            Text(
                                text = day.weekday.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Rest Day", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = day.plannedRest,
                                    onCheckedChange = { isRest ->
                                        viewModel.updateDayPlan(day.weekday, day.plannedGym, day.plannedRunning, isRest)
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = ElectricLime, checkedTrackColor = DarkSurfaceHighlight)
                                )
                            }
                        }

                        if (!day.plannedRest) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = day.plannedGym,
                                        onCheckedChange = { checked ->
                                            viewModel.updateDayPlan(day.weekday, checked, day.plannedRunning, false)
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = CyanAccent)
                                    )
                                    Text("Gym Session", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = day.plannedRunning,
                                        onCheckedChange = { checked ->
                                            viewModel.updateDayPlan(day.weekday, day.plannedGym, checked, false)
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = ElectricLime)
                                    )
                                    Text("Running", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
