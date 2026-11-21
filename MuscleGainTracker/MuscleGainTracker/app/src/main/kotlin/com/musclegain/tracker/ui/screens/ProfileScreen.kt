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
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musclegain.tracker.core.domain.model.BodyWeightEntry
import com.musclegain.tracker.core.domain.model.SupplementEntry
import com.musclegain.tracker.ui.components.ConfirmDeleteDialog
import com.musclegain.tracker.ui.forms.ProfileForm
import com.musclegain.tracker.ui.forms.SupplementForm
import com.musclegain.tracker.ui.theme.CoralError
import com.musclegain.tracker.ui.theme.DarkBackground
import com.musclegain.tracker.ui.theme.DarkBorder
import com.musclegain.tracker.ui.theme.DarkSurface
import com.musclegain.tracker.ui.theme.DarkSurfaceHighlight
import com.musclegain.tracker.ui.theme.ElectricLime
import com.musclegain.tracker.ui.theme.PurpleAccent
import com.musclegain.tracker.ui.theme.RoseAccent
import com.musclegain.tracker.ui.theme.TextMuted
import com.musclegain.tracker.ui.theme.TextPrimary
import com.musclegain.tracker.ui.theme.TextSecondary
import com.musclegain.tracker.viewmodel.ProfileViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToTrainingPlan: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToBackup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val goals by viewModel.currentGoals.collectAsStateWithLifecycle()
    val recentWeights by viewModel.recentWeights.collectAsStateWithLifecycle()
    val supplements by viewModel.todaySupplements.collectAsStateWithLifecycle()
    val knownNames by viewModel.knownSupplementNames.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var weightToDelete by remember { mutableStateOf<BodyWeightEntry?>(null) }
    var supplementToDelete by remember { mutableStateOf<SupplementEntry?>(null) }

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
                title = { Text("PROFILE & SETTINGS", fontWeight = FontWeight.ExtraBold, color = TextPrimary) },
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
            // Quick Settings Shortcuts
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsActionCard(
                    title = "Weekly Training Plan & Schedule",
                    subtitle = "Set gym and running frequency and plan rest days",
                    icon = Icons.Default.FitnessCenter,
                    onClick = onNavigateToTrainingPlan
                )

                SettingsActionCard(
                    title = "Reminders & Notifications",
                    subtitle = "Configure meal, workout, and evening check-in reminders",
                    icon = Icons.Default.Notifications,
                    onClick = onNavigateToNotifications
                )

                SettingsActionCard(
                    title = "Data Export & Backup",
                    subtitle = "Export JSON, CSV tables, or restore backup",
                    icon = Icons.Default.Backup,
                    onClick = onNavigateToBackup
                )
            }

            // Profile & Goals Editor Form
            ProfileForm(
                profile = profile,
                goals = goals,
                onSaveProfile = viewModel::saveProfile,
                onSaveGoals = viewModel::saveGoals
            )

            // Supplement Logging Form
            SupplementForm(
                knownNames = knownNames,
                onAdd = viewModel::addSupplement
            )

            // Today's Supplements History List
            if (supplements.isNotEmpty()) {
                Text(
                    text = "TODAY'S SUPPLEMENTS (${supplements.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                supplements.forEach { supp ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Medication, contentDescription = null, tint = RoseAccent, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(supp.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                    Text("${supp.amount} ${supp.unit} • ${supp.time}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            IconButton(onClick = { supplementToDelete = supp }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CoralError, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Weight History Interface
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
                        text = "WEIGHT MEASUREMENT HISTORY",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    if (recentWeights.isEmpty()) {
                        Text("No weight records logged yet.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    } else {
                        recentWeights.take(10).forEach { bw ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceHighlight)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", bw.weightKg)} kg",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = PurpleAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${bw.date}${if (!bw.notes.isNullOrBlank()) " • ${bw.notes}" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }

                                IconButton(onClick = { weightToDelete = bw }, modifier = Modifier.size(28.dp)) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CoralError, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    weightToDelete?.let { bw ->
        ConfirmDeleteDialog(
            title = "Delete Weight Entry",
            message = "Are you sure you want to delete the weight record for ${bw.date}?",
            onDismiss = { weightToDelete = null },
            onConfirm = {
                viewModel.deleteWeight(bw.id)
                weightToDelete = null
            }
        )
    }

    supplementToDelete?.let { supp ->
        ConfirmDeleteDialog(
            title = "Delete Supplement",
            message = "Are you sure you want to delete ${supp.name}?",
            onDismiss = { supplementToDelete = null },
            onConfirm = {
                viewModel.deleteSupplement(supp.id)
                supplementToDelete = null
            }
        )
    }
}

@Composable
private fun SettingsActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ElectricLime.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}
