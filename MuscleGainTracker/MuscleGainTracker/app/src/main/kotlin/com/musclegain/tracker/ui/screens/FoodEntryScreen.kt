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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restaurant
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
import com.musclegain.tracker.core.domain.model.MealCategory
import com.musclegain.tracker.core.domain.model.NutritionEntry
import com.musclegain.tracker.ui.components.ConfirmDeleteDialog
import com.musclegain.tracker.ui.components.EmptyState
import com.musclegain.tracker.ui.forms.FoodForm
import com.musclegain.tracker.ui.theme.CoralError
import com.musclegain.tracker.ui.theme.DarkBackground
import com.musclegain.tracker.ui.theme.DarkBorder
import com.musclegain.tracker.ui.theme.DarkSurface
import com.musclegain.tracker.ui.theme.DarkSurfaceHighlight
import com.musclegain.tracker.ui.theme.ElectricLime
import com.musclegain.tracker.ui.theme.TextMuted
import com.musclegain.tracker.ui.theme.TextPrimary
import com.musclegain.tracker.ui.theme.TextSecondary
import com.musclegain.tracker.viewmodel.FoodEntryViewModel
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodEntryScreen(
    viewModel: FoodEntryViewModel,
    initialDate: String,
    initialId: Long = 0L,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val entries by viewModel.entriesForDate.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var entryToDelete by remember { mutableStateOf<NutritionEntry?>(null) }

    LaunchedEffect(initialDate) {
        val parsed = runCatching { LocalDate.parse(initialDate) }.getOrNull() ?: LocalDate.now()
        viewModel.setDate(parsed)
    }

    LaunchedEffect(initialId) {
        if (initialId > 0) {
            viewModel.loadEntryForEdit(initialId)
        }
    }

    LaunchedEffect(formState.saveSuccess) {
        if (formState.saveSuccess) {
            snackbarHostState.showSnackbar("Food entry saved successfully")
            viewModel.resetForm()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Log Nutrition", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
            // Food Input Form
            FoodForm(
                state = formState,
                onFoodNameChanged = viewModel::onFoodNameChanged,
                onMealCategoryChanged = viewModel::onMealCategoryChanged,
                onCaloriesChanged = viewModel::onCaloriesChanged,
                onProteinChanged = viewModel::onProteinChanged,
                onCarbsChanged = viewModel::onCarbsChanged,
                onFatChanged = viewModel::onFatChanged,
                onNotesChanged = viewModel::onNotesChanged,
                onSave = { viewModel.saveEntry() },
                onReset = { viewModel.resetForm() }
            )

            // Day's Logged Entries Section
            Text(
                text = "LOGGED MEALS TODAY (${entries.size})",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            if (entries.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Restaurant,
                    title = "No meals logged today",
                    description = "Fill out the form above to add your first meal or snack."
                )
            } else {
                entries.forEach { entry ->
                    FoodEntryCard(
                        entry = entry,
                        onEdit = { viewModel.loadEntryForEdit(entry.id) },
                        onDelete = { entryToDelete = entry }
                    )
                }
            }
        }
    }

    entryToDelete?.let { entry ->
        ConfirmDeleteDialog(
            title = "Delete Food Entry",
            message = "Are you sure you want to delete \"${entry.foodName}\"? This will update your daily totals.",
            onDismiss = { entryToDelete = null },
            onConfirm = {
                viewModel.deleteEntry(entry.id)
                entryToDelete = null
            }
        )
    }
}

@Composable
fun FoodEntryCard(
    entry: NutritionEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurfaceHighlight)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = entry.mealCategory.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricLime,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = entry.foodName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Nutrition details
                val macros = buildList {
                    entry.proteinG?.let { add("P: ${String.format(Locale.US, "%.0f", it)}g") }
                    entry.carbsG?.let { add("C: ${String.format(Locale.US, "%.0f", it)}g") }
                    entry.fatG?.let { add("F: ${String.format(Locale.US, "%.0f", it)}g") }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${String.format(Locale.US, "%.0f", entry.calories)} kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ElectricLime,
                        fontWeight = FontWeight.Bold
                    )
                    if (macros.isNotEmpty()) {
                        Text(
                            text = " • " + macros.joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                if (!entry.notes.isNullOrBlank()) {
                    Text(
                        text = entry.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
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
    }
}
