package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.util.ActivityStats
import com.example.ui.util.TrendDataPoint
import kotlin.math.max

enum class TrendPeriodMode {
    WEEKLY,
    DAILY
}

@Composable
fun ActivityTrendCard(
    stats: ActivityStats,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(TrendPeriodMode.WEEKLY) }
    val currentPoints = if (selectedMode == TrendPeriodMode.WEEKLY) stats.weeklyPoints else stats.dailyPoints

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_activity_trends"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Xu Hướng Hoạt Động",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Thống kê số ghi chú được tạo qua các tuần/ngày",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Metric Summary Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatMetricBadge(
                    label = "Tổng Ghi Chú",
                    value = "${stats.totalNotes}",
                    icon = Icons.Default.Notes,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatMetricBadge(
                    label = "TB / Tuần",
                    value = String.format("%.1f", stats.avgNotesPerWeek),
                    icon = Icons.Default.ShowChart,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                StatMetricBadge(
                    label = "Tuần Cao Điểm",
                    value = "${stats.peakPeriodCount}",
                    subText = stats.peakPeriodLabel,
                    icon = Icons.Default.BarChart,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Period Selection Tabs (Weekly / Daily)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PeriodTabChip(
                        title = "Theo Tuần (8 tuần)",
                        isSelected = selectedMode == TrendPeriodMode.WEEKLY,
                        onClick = { selectedMode = TrendPeriodMode.WEEKLY },
                        modifier = Modifier.weight(1f)
                    )
                    PeriodTabChip(
                        title = "Theo Ngày (7 ngày)",
                        isSelected = selectedMode == TrendPeriodMode.DAILY,
                        onClick = { selectedMode = TrendPeriodMode.DAILY },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Canvas Line Chart Visualization
            ActivityTrendLineChartCanvas(
                points = currentPoints,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
    }
}

@Composable
private fun StatMetricBadge(
    label: String,
    value: String,
    subText: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subText.isNullOrBlank()) {
                Text(
                    text = subText,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PeriodTabChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActivityTrendLineChartCanvas(
    points: List<TrendDataPoint>,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember(points) { mutableStateOf<Int?>(null) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val labelTextColor = MaterialTheme.colorScheme.onSurface

    val maxCount = max(1, points.maxOfOrNull { it.noteCount } ?: 1)
    val chartTopPadding = 35.dp
    val chartBottomPadding = 30.dp
    val chartSidePadding = 25.dp

    Column(modifier = modifier) {
        // Selected Point Tooltip Indicator
        val currentSelected = selectedIndex?.let { points.getOrNull(it) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp),
            contentAlignment = Alignment.Center
        ) {
            if (currentSelected != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = primaryColor,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${currentSelected.label}: ${currentSelected.noteCount} ghi chú (${currentSelected.commentCount} bình luận)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Chạm vào điểm trên biểu đồ để xem chi tiết",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(points) {
                    detectTapGestures { tapOffset ->
                        val width = size.width
                        val availableWidth = width - (chartSidePadding.toPx() * 2)
                        val stepX = if (points.size > 1) availableWidth / (points.size - 1) else availableWidth

                        var minDistance = Float.MAX_VALUE
                        var closestIndex = 0

                        points.forEachIndexed { index, _ ->
                            val pointX = chartSidePadding.toPx() + (index * stepX)
                            val dist = kotlin.math.abs(tapOffset.x - pointX)
                            if (dist < minDistance) {
                                minDistance = dist
                                closestIndex = index
                            }
                        }

                        if (minDistance < stepX * 0.8f) {
                            selectedIndex = if (selectedIndex == closestIndex) null else closestIndex
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            val leftPx = chartSidePadding.toPx()
            val rightPx = width - chartSidePadding.toPx()
            val topPx = 10.dp.toPx()
            val bottomPx = height - chartBottomPadding.toPx()

            val chartWidth = rightPx - leftPx
            val chartHeight = bottomPx - topPx

            // Draw horizontal dotted grid lines
            val gridSteps = 3
            val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

            for (i in 0..gridSteps) {
                val y = bottomPx - (i * (chartHeight / gridSteps))
                drawLine(
                    color = gridLineColor,
                    start = Offset(leftPx, y),
                    end = Offset(rightPx, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashPathEffect
                )
            }

            if (points.isEmpty()) return@Canvas

            val stepX = if (points.size > 1) chartWidth / (points.size - 1) else chartWidth

            // Calculate screen coordinates for data points
            val offsets = points.mapIndexed { index, point ->
                val x = leftPx + (index * stepX)
                val fraction = point.noteCount.toFloat() / maxCount.toFloat()
                val y = bottomPx - (fraction * chartHeight)
                Offset(x, y)
            }

            // Create Line Path
            val linePath = Path().apply {
                offsets.forEachIndexed { index, offset ->
                    if (index == 0) {
                        moveTo(offset.x, offset.y)
                    } else {
                        val prev = offsets[index - 1]
                        val controlX1 = prev.x + (offset.x - prev.x) / 2f
                        val controlY1 = prev.y
                        val controlX2 = prev.x + (offset.x - prev.x) / 2f
                        val controlY2 = offset.y
                        cubicTo(controlX1, controlY1, controlX2, controlY2, offset.x, offset.y)
                    }
                }
            }

            // Create Filled Area Path
            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(offsets.last().x, bottomPx)
                lineTo(offsets.first().x, bottomPx)
                close()
            }

            // Draw Area Gradient Fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.35f),
                        primaryColor.copy(alpha = 0.02f)
                    ),
                    startY = topPx,
                    endY = bottomPx
                )
            )

            // Draw Line
            drawPath(
                path = linePath,
                color = primaryColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Draw Points and X-Axis Labels
            offsets.forEachIndexed { index, offset ->
                val isSelected = selectedIndex == index
                val pointColor = if (isSelected) Color(0xFFD84315) else primaryColor

                // Outer halo if selected
                if (isSelected) {
                    drawCircle(
                        color = pointColor.copy(alpha = 0.25f),
                        radius = 12.dp.toPx(),
                        center = offset
                    )
                }

                // Inner Solid Circle
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 6.dp.toPx() else 4.5.dp.toPx(),
                    center = offset
                )
                drawCircle(
                    color = pointColor,
                    radius = if (isSelected) 4.5.dp.toPx() else 3.5.dp.toPx(),
                    center = offset
                )

                // Note Count Badge above point
                val countText = "${points[index].noteCount}"
                if (points[index].noteCount > 0 || isSelected) {
                    drawCircle(
                        color = if (isSelected) Color(0xFFD84315) else primaryColor,
                        radius = 8.dp.toPx(),
                        center = Offset(offset.x, offset.y - 14.dp.toPx())
                    )
                }
            }
        }

        // X Axis Labels Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = (chartSidePadding - 10.dp)),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEachIndexed { index, point ->
                val isSelected = selectedIndex == index
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isSelected) primaryColor else onSurfaceVariant
                )
            }
        }
    }
}
