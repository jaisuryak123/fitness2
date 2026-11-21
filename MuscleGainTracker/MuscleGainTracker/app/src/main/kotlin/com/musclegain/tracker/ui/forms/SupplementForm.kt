package com.musclegain.tracker.ui.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musclegain.tracker.ui.theme.CoralError
import com.musclegain.tracker.ui.theme.DarkBorder
import com.musclegain.tracker.ui.theme.DarkSurface
import com.musclegain.tracker.ui.theme.DarkSurfaceHighlight
import com.musclegain.tracker.ui.theme.ElectricLime
import com.musclegain.tracker.ui.theme.OnElectricLime
import com.musclegain.tracker.ui.theme.RoseAccent
import com.musclegain.tracker.ui.theme.TextMuted
import com.musclegain.tracker.ui.theme.TextPrimary
import com.musclegain.tracker.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SupplementForm(
    knownNames: List<String>,
    onAdd: (name: String, amount: Double, unit: String, notes: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var nameText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("5") }
    var unitText by remember { mutableStateOf("g") }
    var notesText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val commonUnits = listOf("g", "mg", "scoop", "capsule", "tablet", "ml")
    val defaultSuggestions = listOf("Creatine Monohydrate", "Whey Protein", "Multivitamin", "Fish Oil", "Vitamin D3")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    tint = RoseAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LOG SUPPLEMENT / VITAMIN",
                    style = MaterialTheme.typography.labelSmall,
                    color = RoseAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }

            // Quick suggestion chips
            val suggestions = (knownNames + defaultSuggestions).distinct().take(6)
            if (suggestions.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    suggestions.forEach { suggestion ->
                        FilterChip(
                            selected = nameText.equals(suggestion, ignoreCase = true),
                            onClick = { nameText = suggestion },
                            label = { Text(suggestion, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoseAccent,
                                selectedLabelColor = DarkSurface,
                                containerColor = DarkSurfaceHighlight,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }

            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it; errorMessage = null },
                label = { Text("Supplement Name *") },
                placeholder = { Text("e.g. Creatine Monohydrate") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; errorMessage = null },
                    label = { Text("Amount *") },
                    placeholder = { Text("5") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors()
                )

                OutlinedTextField(
                    value = unitText,
                    onValueChange = { unitText = it; errorMessage = null },
                    label = { Text("Unit *") },
                    placeholder = { Text("g") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors()
                )
            }

            // Unit Quick Pick
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                commonUnits.forEach { u ->
                    FilterChip(
                        selected = unitText.equals(u, ignoreCase = true),
                        onClick = { unitText = u },
                        label = { Text(u, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricLime,
                            selectedLabelColor = OnElectricLime,
                            containerColor = DarkSurfaceHighlight,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Notes (optional)") },
                placeholder = { Text("e.g. Taken post-workout with water") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            if (errorMessage != null) {
                Text(text = errorMessage!!, color = CoralError, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (nameText.isBlank()) {
                        errorMessage = "Supplement name is required"
                    } else if (amt == null || amt <= 0) {
                        errorMessage = "Amount must be greater than 0"
                    } else if (unitText.isBlank()) {
                        errorMessage = "Unit is required"
                    } else {
                        onAdd(nameText, amt, unitText, notesText.ifBlank { null })
                        nameText = ""
                        notesText = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoseAccent,
                    contentColor = DarkSurface
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log Supplement", fontWeight = FontWeight.Bold)
            }
        }
    }
}
