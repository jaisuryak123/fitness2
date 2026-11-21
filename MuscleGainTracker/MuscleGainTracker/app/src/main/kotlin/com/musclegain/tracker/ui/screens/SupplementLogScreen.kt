package com.musclegain.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Medication
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
import com.musclegain.tracker.core.domain.model.SupplementEntry
import com.musclegain.tracker.ui.components.ConfirmDeleteDialog
import com.musclegain.tracker.ui.components.EmptyState
import com.musclegain.tracker.ui.forms.SupplementForm
import com.musclegain.tracker.ui.theme.CoralError
import com.musclegain.tracker.ui.theme.DarkBackground
import com.musclegain.tracker.ui.theme.DarkBorder
import com.musclegain.tracker.ui.theme.DarkSurface
import com.musclegain.tracker.ui.theme.RoseAccent
import com.musclegain.tracker.ui.theme.TextMuted
import com.musclegain.tracker.ui.theme.TextPrimary
import com.musclegain.tracker.ui.theme.TextSecondary
import com.musclegain.tracker.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplementLogScreen(
    viewModel: ProfileViewModel,
    initialDate: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val supplements by viewModel.todaySupplements.collectAsStateWithLifecycle()
    val knownNames by viewModel.knownSupplementNames.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var supplementToDelete by remember { mutableStateOf<SupplementEntry?>(null) }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Log Supplements", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
            SupplementForm(
                knownNames = knownNames,
                onAdd = viewModel::addSupplement
            )

            Text(
                text = "TODAY'S SUPPLEMENTS (${supplements.size})",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            if (supplements.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Medication,
                    title = "No supplements logged today",
                    description = "Use the form above to log your vitamins and supplements."
                )
            } else {
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
                            Column {
                                Text(supp.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text("${supp.amount} ${supp.unit} • ${supp.time}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                supp.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = TextMuted) }
                            }
                            IconButton(onClick = { supplementToDelete = supp }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CoralError, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
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
