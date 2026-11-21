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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Restaurant
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
import com.musclegain.tracker.core.domain.model.BodyWeightEntry
import com.musclegain.tracker.core.domain.model.NutritionEntry
import com.musclegain.tracker.core.domain.model.SupplementEntry
import com.musclegain.tracker.core.domain.model.WorkoutSessionWithExercises
import com.musclegain.tracker.ui.components.ConfirmDeleteDialog
import com.musclegain.tracker.ui.components.EmptyState
import com.musclegain.tracker.ui.components.NutritionCard
import com.musclegain.tracker.ui.components.WorkoutCard
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
import com.musclegain.tracker.viewmodel.HistoryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateToEditFood: (date: String, id: Long) -> Unit,
    onNavigateToEditWorkout: (date: String, id: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val dailySummary by viewModel.dailySummary.collectAsStateWithLifecycle()
    val foodEntries by viewModel.foodEntries.collectAsStateWithLifecycle()
    val workoutSessions by viewModel.workoutSessions.collectAsStateWithLifecycle()
    val supplements by viewModel.supplements.collectAsStateWithLifecycle()
    val bodyWeight by viewModel.bodyWeight.collectAsStateWithLifecycle()

    var foodToDelete by remember { mutableStateOf<NutritionEntry?>(null) }
    var workoutToDelete by remember { mutableStateOf<WorkoutSessionWithExercises?>(null) }
    var supplementToDelete by remember { mutableStateOf<SupplementEntry?>(null) }
    var weightToDelete by remember { mutableStateOf<BodyWeightEntry?>(null) }

    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.US)
    val dateString = selectedDate.toString()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "HISTORY LOG",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = selectedDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.selectDate(selectedDate.minusDays(1)) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day", tint = TextSecondary)
                    }
                    IconButton(onClick = { viewModel.selectDate(selectedDate.plusDays(1)) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day", tint = TextSecondary)
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
            // Horizontal 14-day Calendar Strip
            val daysList = remember(selectedDate) {
                (-7L..7L).map { selectedDate.plusDays(it) }
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(daysList) { day ->
                    val isSelected = day == selectedDate
                    val isToday = day == LocalDate.now()

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ElectricLime else DarkSurface)
                            .border(1.dp, if (isToday) ElectricLime else DarkBorder, RoundedCornerShape(12.dp))
                            .clickable { viewModel.selectDate(day) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = day.dayOfWeek.name.take(3),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) DarkBackground else TextSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = day.dayOfMonth.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) DarkBackground else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Summary Cards for Selected Date
            NutritionCard(summary = dailySummary, onCardClick = {})
            WorkoutCard(summary = dailySummary, onCardClick = {})

            // Body Weight Logged
            bodyWeight?.let { bw ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.MonitorWeight, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("BODY WEIGHT", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text("${String.format(Locale.US, "%.1f", bw.weightKg)} kg", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                bw.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = TextSecondary) }
                            }
                        }

                        IconButton(onClick = { weightToDelete = bw }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CoralError, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Food Entries for Selected Date
            if (foodEntries.isNotEmpty()) {
                Text(
                    text = "FOOD ENTRIES (${foodEntries.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                foodEntries.forEach { entry ->
                    FoodEntryCard(
                        entry = entry,
                        onEdit = { onNavigateToEditFood(dateString, entry.id) },
                        onDelete = { foodToDelete = entry }
                    )
                }
            }

            // Workout Sessions for Selected Date
            if (workoutSessions.isNotEmpty()) {
                Text(
                    text = "WORKOUT SESSIONS (${workoutSessions.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                workoutSessions.forEach { item ->
                    WorkoutDetailCard(
                        item = item,
                        onEdit = { onNavigateToEditWorkout(dateString, item.session.id) },
                        onDelete = { workoutToDelete = item }
                    )
                }
            }

            // Supplements for Selected Date
            if (supplements.isNotEmpty()) {
                Text(
                    text = "SUPPLEMENTS LOGGED (${supplements.size})",
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

            // If empty day
            if (foodEntries.isEmpty() && workoutSessions.isEmpty() && supplements.isEmpty() && bodyWeight == null) {
                EmptyState(
                    icon = Icons.Default.History,
                    title = "Nothing logged on this date",
                    description = "Use the Today dashboard or calendar stepper to select another date."
                )
            }
        }
    }

    // Confirmation Dialogs
    foodToDelete?.let { entry ->
        ConfirmDeleteDialog(
            title = "Delete Food Entry",
            message = "Are you sure you want to delete \"${entry.foodName}\"?",
            onDismiss = { foodToDelete = null },
            onConfirm = {
                viewModel.deleteFood(entry.id)
                foodToDelete = null
            }
        )
    }

    workoutToDelete?.let { item ->
        ConfirmDeleteDialog(
            title = "Delete Workout Session",
            message = "Are you sure you want to delete this workout?",
            onDismiss = { workoutToDelete = null },
            onConfirm = {
                viewModel.deleteWorkout(item.session.id)
                workoutToDelete = null
            }
        )
    }

    supplementToDelete?.let { supp ->
        ConfirmDeleteDialog(
            title = "Delete Supplement",
            message = "Are you sure you want to delete \"${supp.name}\"?",
            onDismiss = { supplementToDelete = null },
            onConfirm = {
                viewModel.deleteSupplement(supp.id)
                supplementToDelete = null
            }
        )
    }

    weightToDelete?.let { bw ->
        ConfirmDeleteDialog(
            title = "Delete Body Weight Record",
            message = "Are you sure you want to delete this body weight measurement?",
            onDismiss = { weightToDelete = null },
            onConfirm = {
                viewModel.deleteBodyWeight(bw.id)
                weightToDelete = null
            }
        )
    }
}
