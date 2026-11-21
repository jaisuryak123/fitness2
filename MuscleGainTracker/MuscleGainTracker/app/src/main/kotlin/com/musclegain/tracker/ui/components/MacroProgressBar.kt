package com.musclegain.tracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.musclegain.tracker.ui.theme.DarkBorder
import com.musclegain.tracker.ui.theme.TextMuted
import com.musclegain.tracker.ui.theme.TextPrimary
import com.musclegain.tracker.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun MacroProgressBar(
    name: String,
    currentValue: Double?,
    targetValue: Double,
    unit: String = "g",
    color: Color,
    modifier: Modifier = Modifier
) {
    val current = currentValue ?: 0.0
    val progress = if (targetValue > 0) (current / targetValue).toFloat().coerceIn(0f, 1.5f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress.coerceAtMost(1f), label = "macro_progress")
    val percentText = if (currentValue != null && targetValue > 0) {
        String.format(Locale.US, "%.0f%%", (current / targetValue) * 100.0)
    } else {
        "—"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(verticalAlignment = Alignment.Bottom) {
                if (currentValue != null) {
                    Text(
                        text = String.format(Locale.US, "%.0f", current),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = " / ${String.format(Locale.US, "%.0f", targetValue)} $unit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "($percentText)",
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Progress track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(DarkBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = animatedProgress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}
