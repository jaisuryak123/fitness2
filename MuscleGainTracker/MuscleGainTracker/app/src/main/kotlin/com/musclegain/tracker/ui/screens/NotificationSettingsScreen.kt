package com.musclegain.tracker.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musclegain.tracker.ui.theme.DarkBackground
import com.musclegain.tracker.ui.theme.DarkBorder
import com.musclegain.tracker.ui.theme.DarkSurface
import com.musclegain.tracker.ui.theme.DarkSurfaceHighlight
import com.musclegain.tracker.ui.theme.ElectricLime
import com.musclegain.tracker.ui.theme.OnElectricLime
import com.musclegain.tracker.ui.theme.TextMuted
import com.musclegain.tracker.ui.theme.TextPrimary
import com.musclegain.tracker.ui.theme.TextSecondary
import com.musclegain.tracker.viewmodel.NotificationViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: NotificationViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.reminderSettings.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var permissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Reminder Notifications", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
            // Explanation rationale card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceHighlight)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OFFLINE FITNESS REMINDERS",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "MuscleGainTracker uses on-device scheduled reminders (WorkManager) to help keep your muscle gain consistency. We never access contacts, camera, or network. Notifications are completely local and private.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricLime,
                                contentColor = OnElectricLime
                            )
                        ) {
                            Text("Grant Notification Permission", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Text(
                text = "SCHEDULED REMINDERS",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Morning Planning / Body Weight
            ReminderItemCard(
                title = "Morning Planning & Weight Log",
                subtitle = "Log your morning fasted weight and view today's targets",
                time = settings.bodyWeight.time.format(timeFormatter),
                enabled = settings.bodyWeight.enabled,
                onToggle = { enabled -> viewModel.updateBodyWeightReminder(enabled) }
            )

            // Meal & Food Logging Reminder
            ReminderItemCard(
                title = "Midday Food Logging Reminder",
                subtitle = "Check your remaining calories and protein for lunch",
                time = settings.meal.time.format(timeFormatter),
                enabled = settings.meal.enabled,
                onToggle = { enabled -> viewModel.updateMealReminder(enabled) }
            )

            // Workout / Gym Reminder
            ReminderItemCard(
                title = "Workout Session Reminder",
                subtitle = "Time for your scheduled workout or run",
                time = settings.workout.time.format(timeFormatter),
                enabled = settings.workout.enabled,
                onToggle = { enabled -> viewModel.updateWorkoutReminder(enabled) }
            )

            // Supplement Reminder
            ReminderItemCard(
                title = "Supplement & Vitamin Reminder",
                subtitle = "Creatine, protein, or daily vitamins check-in",
                time = settings.supplement.time.format(timeFormatter),
                enabled = settings.supplement.enabled,
                onToggle = { enabled -> viewModel.updateSupplementReminder(enabled) }
            )
        }
    }
}

@Composable
private fun ReminderItemCard(
    title: String,
    subtitle: String,
    time: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Alarm, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(time, style = MaterialTheme.typography.labelMedium, color = ElectricLime, fontWeight = FontWeight.Bold)
                }
            }

            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ElectricLime,
                    checkedTrackColor = DarkSurfaceHighlight
                )
            )
        }
    }
}
