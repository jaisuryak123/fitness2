package com.musclegain.tracker.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musclegain.tracker.core.domain.model.DayOfWeek
import com.musclegain.tracker.ui.theme.CyanAccent
import com.musclegain.tracker.ui.theme.DarkBorder
import com.musclegain.tracker.ui.theme.DarkSurface
import com.musclegain.tracker.ui.theme.DarkSurfaceHighlight
import com.musclegain.tracker.ui.theme.ElectricLime
import com.musclegain.tracker.ui.theme.EmeraldSuccess
import com.musclegain.tracker.ui.theme.PurpleAccent
import com.musclegain.tracker.ui.theme.TextMuted
import com.musclegain.tracker.ui.theme.TextPrimary
import com.musclegain.tracker.ui.theme.TextSecondary
import java.time.LocalDate
import java.util.Locale

data class DayBarData(
    val dayLabel: String,
    val value: Double?,
    val isToday: Boolean = false
)

@Composable
fun WeeklyBarChart(
    title: String,
    data: List<DayBarData>,
    targetValue: Double?,
    unit: String = "kcal",
    barColor: Color = ElectricLime,
    modifier: Modifier = Modifier
) {
    val nonNullValues = data.mapNotNull { it.value }
    val maxBarValue = maxOf(nonNullValues.maxOrNull() ?: 1.0, targetValue ?: 1.0) * 1.15

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                if (targetValue != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(2.dp)
                                .background(CyanAccent)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Target: ${String.format(Locale.US, "%.0f", targetValue)} $unit",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 7-day Bar Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val chartHeight = size.height - 30f // space for text
                val barWidth = (size.width / 7) * 0.45f
                val stepX = size.width / 7

                // Target reference line
                if (targetValue != null && maxBarValue > 0) {
                    val targetY = chartHeight - ((targetValue / maxBarValue).toFloat() * chartHeight)
                    drawLine(
                        color = CyanAccent.copy(alpha = 0.5f),
                        start = Offset(0f, targetY),
                        end = Offset(size.width, targetY),
                        strokeWidth = 2f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                data.forEachIndexed { index, item ->
                    val centerX = (index * stepX) + (stepX / 2)
                    val left = centerX - (barWidth / 2)

                    // Draw track background
                    drawRoundRect(
                        color = DarkSurfaceHighlight,
                        topLeft = Offset(left, 0f),
                        size = Size(barWidth, chartHeight),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    // Draw filled bar if value is present
                    val valNumber = item.value
                    if (valNumber != null && valNumber > 0 && maxBarValue > 0) {
                        val filledHeight = ((valNumber / maxBarValue).toFloat() * chartHeight).coerceIn(4f, chartHeight)
                        val top = chartHeight - filledHeight
                        drawRoundRect(
                            color = if (item.isToday) barColor else barColor.copy(alpha = 0.85f),
                            topLeft = Offset(left, top),
                            size = Size(barWidth, filledHeight),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }
                }
            }

            // Day labels row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                data.forEach { item ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = item.dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (item.isToday) barColor else TextSecondary,
                            fontWeight = if (item.isToday) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = if (item.value != null) String.format(Locale.US, "%.0f", item.value) else "—",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (item.value != null) TextPrimary else TextMuted,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

data class WeightDataPoint(
    val dateLabel: String,
    val weightKg: Double
)

@Composable
fun WeightTrendChart(
    dataPoints: List<WeightDataPoint>,
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
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WEIGHT TREND",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                if (dataPoints.size >= 2) {
                    val delta = dataPoints.last().weightKg - dataPoints.first().weightKg
                    val deltaSign = if (delta > 0) "+" else ""
                    val deltaColor = if (delta >= 0) EmeraldSuccess else CyanAccent
                    Text(
                        text = "$deltaSign${String.format(Locale.US, "%.1f", delta)} kg",
                        style = MaterialTheme.typography.labelMedium,
                        color = deltaColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (dataPoints.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Not logged this week",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            } else if (dataPoints.size == 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", dataPoints.first().weightKg)} kg",
                            style = MaterialTheme.typography.headlineMedium,
                            color = PurpleAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Logged on ${dataPoints.first().dateLabel} (need at least 2 entries for trend)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                val minWeight = dataPoints.minOf { it.weightKg } - 0.5
                val maxWeight = dataPoints.maxOf { it.weightKg } + 0.5
                val weightRange = (maxWeight - minWeight).coerceAtLeast(1.0)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    val chartHeight = size.height - 20f
                    val stepX = size.width / (dataPoints.size - 1)
                    val points = dataPoints.mapIndexed { index, dp ->
                        val x = index * stepX
                        val y = chartHeight - ((dp.weightKg - minWeight) / weightRange).toFloat() * chartHeight
                        Offset(x, y)
                    }

                    // Draw connecting line
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = PurpleAccent,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw circles at data points
                    points.forEach { point ->
                        drawCircle(
                            color = DarkSurface,
                            radius = 6.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = PurpleAccent,
                            radius = 4.dp.toPx(),
                            center = point
                        )
                    }
                }

                // X-axis labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${dataPoints.first().dateLabel}: ${String.format(Locale.US, "%.1f", dataPoints.first().weightKg)} kg",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${dataPoints.last().dateLabel}: ${String.format(Locale.US, "%.1f", dataPoints.last().weightKg)} kg",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
