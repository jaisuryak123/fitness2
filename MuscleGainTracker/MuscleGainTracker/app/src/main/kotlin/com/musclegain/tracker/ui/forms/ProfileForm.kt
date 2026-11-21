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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
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
import com.musclegain.tracker.core.domain.model.DailyGoals
import com.musclegain.tracker.core.domain.model.FitnessGoal
import com.musclegain.tracker.core.domain.model.UserProfile
import com.musclegain.tracker.ui.theme.CoralError
import com.musclegain.tracker.ui.theme.DarkBorder
import com.musclegain.tracker.ui.theme.DarkSurface
import com.musclegain.tracker.ui.theme.DarkSurfaceHighlight
import com.musclegain.tracker.ui.theme.ElectricLime
import com.musclegain.tracker.ui.theme.OnElectricLime
import com.musclegain.tracker.ui.theme.TextMuted
import com.musclegain.tracker.ui.theme.TextPrimary
import com.musclegain.tracker.ui.theme.TextSecondary

@Composable
fun ProfileForm(
    profile: UserProfile?,
    goals: DailyGoals,
    onSaveProfile: (heightCm: Double, weightKg: Double, age: Int?, goal: FitnessGoal) -> Unit,
    onSaveGoals: (cal: Double, pro: Double, carbs: Double, fat: Double, gym: Int, run: Int, dist: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    // Profile Fields
    var heightText by remember(profile) { mutableStateOf(profile?.heightCm?.toString() ?: "175.0") }
    var weightText by remember(profile) { mutableStateOf(profile?.weightKg?.toString() ?: "75.0") }
    var ageText by remember(profile) { mutableStateOf(profile?.age?.toString() ?: "25") }
    var selectedGoal by remember(profile) { mutableStateOf(profile?.goal ?: FitnessGoal.MUSCLE_GAIN) }

    // Goal Fields
    var calTargetText by remember(goals) { mutableStateOf(goals.calorieTarget.toInt().toString()) }
    var proTargetText by remember(goals) { mutableStateOf(goals.proteinTarget.toInt().toString()) }
    var carbsTargetText by remember(goals) { mutableStateOf(goals.carbsTarget.toInt().toString()) }
    var fatTargetText by remember(goals) { mutableStateOf(goals.fatTarget.toInt().toString()) }
    var gymTargetText by remember(goals) { mutableStateOf(goals.weeklyGymTarget.toString()) }
    var runTargetText by remember(goals) { mutableStateOf(goals.weeklyRunningTarget.toString()) }
    var distTargetText by remember(goals) { mutableStateOf(goals.runningDistanceTargetKm.toString()) }

    var profileError by remember { mutableStateOf<String?>(null) }
    var goalsError by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Physical Profile Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "BODY METRICS & GOAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricLime,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it; profileError = null },
                        label = { Text("Height (cm)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )

                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it; profileError = null },
                        label = { Text("Weight (kg)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                }

                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it; profileError = null },
                    label = { Text("Age (optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Fitness Objective", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FitnessGoal.values().forEach { goal ->
                            val selected = selectedGoal == goal
                            FilterChip(
                                selected = selected,
                                onClick = { selectedGoal = goal },
                                label = {
                                    Text(
                                        when (goal) {
                                            FitnessGoal.MUSCLE_GAIN -> "Muscle Gain"
                                            FitnessGoal.MAINTENANCE -> "Maintenance"
                                        }
                                    )
                                },
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

                if (profileError != null) {
                    Text(text = profileError!!, color = CoralError, style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        val h = heightText.toDoubleOrNull()
                        val w = weightText.toDoubleOrNull()
                        val a = ageText.toIntOrNull()
                        if (h == null || h !in 50.0..272.0) {
                            profileError = "Height must be 50 - 272 cm"
                        } else if (w == null || w !in 20.0..400.0) {
                            profileError = "Weight must be 20 - 400 kg"
                        } else {
                            onSaveProfile(h, w, a, selectedGoal)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnElectricLime)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Profile", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Daily Targets Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "DAILY NUTRITION & WORKOUT TARGETS",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricLime,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = calTargetText,
                        onValueChange = { calTargetText = it; goalsError = null },
                        label = { Text("Calories (kcal)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )

                    OutlinedTextField(
                        value = proTargetText,
                        onValueChange = { proTargetText = it; goalsError = null },
                        label = { Text("Protein (g)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = carbsTargetText,
                        onValueChange = { carbsTargetText = it; goalsError = null },
                        label = { Text("Carbs (g)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )

                    OutlinedTextField(
                        value = fatTargetText,
                        onValueChange = { fatTargetText = it; goalsError = null },
                        label = { Text("Fat (g)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = gymTargetText,
                        onValueChange = { gymTargetText = it; goalsError = null },
                        label = { Text("Gym/wk") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )

                    OutlinedTextField(
                        value = runTargetText,
                        onValueChange = { runTargetText = it; goalsError = null },
                        label = { Text("Runs/wk") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )

                    OutlinedTextField(
                        value = distTargetText,
                        onValueChange = { distTargetText = it; goalsError = null },
                        label = { Text("Run km") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                }

                if (goalsError != null) {
                    Text(text = goalsError!!, color = CoralError, style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        val cal = calTargetText.toDoubleOrNull()
                        val pro = proTargetText.toDoubleOrNull()
                        val carbs = carbsTargetText.toDoubleOrNull()
                        val fat = fatTargetText.toDoubleOrNull()
                        val gym = gymTargetText.toIntOrNull()
                        val run = runTargetText.toIntOrNull()
                        val dist = distTargetText.toDoubleOrNull()

                        if (cal == null || cal !in 500.0..10000.0) {
                            goalsError = "Calorie target must be 500 - 10,000"
                        } else if (pro == null || pro !in 0.0..2000.0) {
                            goalsError = "Protein target must be 0 - 2,000 g"
                        } else if (gym == null || gym !in 0..7) {
                            goalsError = "Weekly gym target must be 0 - 7"
                        } else if (run == null || run !in 0..7) {
                            goalsError = "Weekly running target must be 0 - 7"
                        } else {
                            onSaveGoals(cal, pro, carbs ?: 0.0, fat ?: 0.0, gym, run, dist ?: 5.0)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnElectricLime)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Goals", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
