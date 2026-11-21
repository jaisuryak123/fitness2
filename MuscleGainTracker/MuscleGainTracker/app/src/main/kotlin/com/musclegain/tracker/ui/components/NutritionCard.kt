package com.musclegain.tracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musclegain.tracker.core.domain.model.DailySummary
import com.musclegain.tracker.ui.theme.AmberAccent
import com.musclegain.tracker.ui.theme.DarkBorder
import com.musclegain.tracker.ui.theme.DarkSurface
import com.musclegain.tracker.ui.theme.DarkSurfaceHighlight
import com.musclegain.tracker.ui.theme.ElectricLime
import com.musclegain.tracker.ui.theme.MacroColors
import com.musclegain.tracker.ui.theme.TextMuted
import com.musclegain.tracker.ui.theme.TextPrimary
import com.musclegain.tracker.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun NutritionCard(
    summary: DailySummary?,
    onCardClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val goals = summary?.goals
    val totals = summary?.totals
    val remaining = summary?.remaining

    val targetCalories = goals?.calorieTarget ?: 2800.0
    val consumedCalories = totals?.calories ?: 0.0
    val caloriesRemaining = remaining?.calories ?: targetCalories

    val calorieProgress = if (targetCalories > 0) {
        (consumedCalories / targetCalories).toFloat().coerceIn(0f, 1f)
    } else 0f
    val animatedCalorieProgress by animateFloatAsState(targetValue = calorieProgress, label = "cal_progress")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .clickable { onCardClick() }
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceHighlight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Nutrition",
                            tint = ElectricLime,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "NUTRITION",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = if (totals != null) "${summary.nutritionEntryCount} logged today" else "Not logged yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                // Calorie Target Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceHighlight)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Target: ${String.format(Locale.US, "%.0f", targetCalories)} kcal",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Calorie Hero Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (totals != null) String.format(Locale.US, "%.0f", consumedCalories) else "—",
                        style = MaterialTheme.typography.displayMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Calories Consumed",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    val remainingColor = if (caloriesRemaining >= 0) ElectricLime else AmberAccent
                    Text(
                        text = if (totals != null) {
                            String.format(Locale.US, "%.0f", caloriesRemaining)
                        } else {
                            String.format(Locale.US, "%.0f", targetCalories)
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        color = remainingColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (caloriesRemaining >= 0) "kcal Remaining" else "kcal Over Target",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Big Calorie Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(DarkBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = animatedCalorieProgress)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    ElectricLime.copy(alpha = 0.8f),
                                    ElectricLime
                                )
                            )
                        )
                )
            }

            // Macro Breakdown Bars
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MacroProgressBar(
                    name = "Protein",
                    currentValue = totals?.proteinG,
                    targetValue = goals?.proteinTarget ?: 140.0,
                    color = MacroColors.Protein
                )

                MacroProgressBar(
                    name = "Carbs",
                    currentValue = totals?.carbsG,
                    targetValue = goals?.carbsTarget ?: 380.0,
                    color = MacroColors.Carbs
                )

                MacroProgressBar(
                    name = "Fat",
                    currentValue = totals?.fatG,
                    targetValue = goals?.fatTarget ?: 80.0,
                    color = MacroColors.Fat
                )
            }

            // Incomplete Macro Warning (if any)
            if (summary?.hasIncompleteMacros == true) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceHighlight)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Warning",
                        tint = AmberAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Some entries don't have all macros logged",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberAccent
                    )
                }
            }
        }
    }
}
