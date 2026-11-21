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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.musclegain.tracker.core.domain.model.MealCategory
import com.musclegain.tracker.ui.theme.AmberAccent
import com.musclegain.tracker.ui.theme.CoralError
import com.musclegain.tracker.ui.theme.DarkBackground
import com.musclegain.tracker.ui.theme.DarkBorder
import com.musclegain.tracker.ui.theme.DarkSurface
import com.musclegain.tracker.ui.theme.DarkSurfaceHighlight
import com.musclegain.tracker.ui.theme.ElectricLime
import com.musclegain.tracker.ui.theme.MacroColors
import com.musclegain.tracker.ui.theme.OnElectricLime
import com.musclegain.tracker.ui.theme.TextMuted
import com.musclegain.tracker.ui.theme.TextPrimary
import com.musclegain.tracker.ui.theme.TextSecondary
import com.musclegain.tracker.viewmodel.FoodFormState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodForm(
    state: FoodFormState,
    onFoodNameChanged: (String) -> Unit,
    onMealCategoryChanged: (MealCategory) -> Unit,
    onCaloriesChanged: (String) -> Unit,
    onProteinChanged: (String) -> Unit,
    onCarbsChanged: (String) -> Unit,
    onFatChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
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
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = if (state.id == 0L) "ADD FOOD ENTRY" else "EDIT FOOD ENTRY",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricLime,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            // Food Name
            OutlinedTextField(
                value = state.foodName,
                onValueChange = onFoodNameChanged,
                label = { Text("Food Name *") },
                placeholder = { Text("e.g. Grilled Chicken Breast 200g") },
                singleLine = true,
                isError = state.errors.containsKey("foodName"),
                supportingText = state.errors["foodName"]?.let { { Text(it, color = CoralError) } },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            // Meal Category Filter Chips
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Meal Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MealCategory.values().forEach { category ->
                        val selected = state.mealCategory == category
                        FilterChip(
                            selected = selected,
                            onClick = { onMealCategoryChanged(category) },
                            label = {
                                Text(
                                    category.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
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

            // Calories & Protein Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = state.calories,
                    onValueChange = onCaloriesChanged,
                    label = { Text("Calories (kcal) *") },
                    placeholder = { Text("e.g. 350") },
                    singleLine = true,
                    isError = state.errors.containsKey("calories"),
                    supportingText = state.errors["calories"]?.let { { Text(it, color = CoralError) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors()
                )

                OutlinedTextField(
                    value = state.protein,
                    onValueChange = onProteinChanged,
                    label = { Text("Protein (g)") },
                    placeholder = { Text("e.g. 45") },
                    singleLine = true,
                    isError = state.errors.containsKey("proteinG"),
                    supportingText = state.errors["proteinG"]?.let { { Text(it, color = CoralError) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors()
                )
            }

            // Carbs & Fat Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = state.carbs,
                    onValueChange = onCarbsChanged,
                    label = { Text("Carbs (g)") },
                    placeholder = { Text("e.g. 10") },
                    singleLine = true,
                    isError = state.errors.containsKey("carbsG"),
                    supportingText = state.errors["carbsG"]?.let { { Text(it, color = CoralError) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors()
                )

                OutlinedTextField(
                    value = state.fat,
                    onValueChange = onFatChanged,
                    label = { Text("Fat (g)") },
                    placeholder = { Text("e.g. 5") },
                    singleLine = true,
                    isError = state.errors.containsKey("fatG"),
                    supportingText = state.errors["fatG"]?.let { { Text(it, color = CoralError) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors()
                )
            }

            // Optional Notes
            OutlinedTextField(
                value = state.notes,
                onValueChange = onNotesChanged,
                label = { Text("Optional Notes") },
                placeholder = { Text("e.g. Pre-workout meal") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            // Warning banner (e.g. Macro mismatch)
            if (state.warning != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceHighlight)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Warning",
                        tint = AmberAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.warning,
                        style = MaterialTheme.typography.bodySmall,
                        color = AmberAccent
                    )
                }
            }

            // Action Buttons
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
                        containerColor = ElectricLime,
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
                        Text(if (state.id == 0L) "Add Food" else "Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = ElectricLime,
    unfocusedBorderColor = DarkBorder,
    focusedContainerColor = DarkBackground,
    unfocusedContainerColor = DarkBackground,
    focusedLabelColor = ElectricLime,
    unfocusedLabelColor = TextSecondary
)
