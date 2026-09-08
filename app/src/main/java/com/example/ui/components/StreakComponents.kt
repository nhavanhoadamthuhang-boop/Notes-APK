package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.repository.StreakState

val FlameActiveColor = Color(0xFFFF6D00)
val FlameActiveContainer = Color(0xFFFFF3E0)
val FlameInactiveColor = Color(0xFF9E9E9E)

@Composable
fun StreakBadge(
    streakState: StreakState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isInteracted = streakState.hasInteractedToday
    val streakCount = streakState.currentStreak

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isInteracted)
            Color(0xFFFFE0B2).copy(alpha = 0.85f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = if (isInteracted)
            androidx.compose.foundation.BorderStroke(1.dp, FlameActiveColor.copy(alpha = 0.6f))
        else
            null,
        modifier = modifier.testTag("badge_daily_streak")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Whatshot,
                contentDescription = "Chuỗi ngày liên tiếp",
                tint = if (isInteracted) FlameActiveColor else FlameInactiveColor,
                modifier = Modifier.size(18.dp)
            )

            Text(
                text = "$streakCount",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isInteracted) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isInteracted) {
                Surface(
                    shape = CircleShape,
                    color = FlameActiveColor,
                    modifier = Modifier.size(6.dp)
                ) {}
            }
        }
    }
}

@Composable
fun StreakDialog(
    streakState: StreakState,
    onCreateNoteClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    val isInteracted = streakState.hasInteractedToday
    val currentStreak = streakState.currentStreak
    val longestStreak = streakState.longestStreak
    val dayLabels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 680.dp)
                .clip(RoundedCornerShape(28.dp))
                .testTag("dialog_daily_streak"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Chuỗi Ngày Tương Tác",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_streak_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // =========================================================
                    // 1. HERO FLAME HEADER CARD
                    // =========================================================
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isInteracted) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isInteracted) FlameActiveColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isInteracted) FlameActiveColor.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.2f),
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Whatshot,
                                            contentDescription = null,
                                            tint = if (isInteracted) FlameActiveColor else FlameInactiveColor,
                                            modifier = Modifier.size(44.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "$currentStreak Ngày Liên Tiếp",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isInteracted) Color(0xFFD84315) else MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = streakState.streakLevelName,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Status Banner
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isInteracted) Color(0xFFE8F5E9) else Color(0xFFFFF8E1),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isInteracted) Icons.Default.CheckCircle else Icons.Default.Whatshot,
                                            contentDescription = null,
                                            tint = if (isInteracted) Color(0xFF2E7D32) else Color(0xFFF57F17),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = if (isInteracted)
                                                "Hôm nay bạn đã tương tác thành công! Chuỗi ngày được duy trì."
                                            else
                                                "Hôm nay chưa tương tác. Hãy tạo/sửa ghi chú để giữ chuỗi!",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isInteracted) Color(0xFF1B5E20) else Color(0xFFF57F17),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // =========================================================
                    // 2. PAST 7 DAYS WEEKLY TRACKER
                    // =========================================================
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Lịch sử tương tác 7 ngày qua",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                streakState.activeDaysPastWeek.forEachIndexed { index, isActive ->
                                    val dayName = dayLabels.getOrElse(index) { "" }
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isActive) FlameActiveColor else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (isActive) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                } else {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                                        modifier = Modifier.size(8.dp)
                                                    ) {}
                                                }
                                            }
                                        }

                                        Text(
                                            text = dayName,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isActive) FlameActiveColor else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // =========================================================
                    // 3. STATS SUMMARY GRID
                    // =========================================================
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$longestStreak Ngày",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Kỷ lục dài nhất",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MilitaryTech,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${streakState.nextMilestone} Ngày",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Cột mốc kế tiếp",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // =========================================================
                    // 4. MILESTONES ROADMAP
                    // =========================================================
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Các cột mốc danh hiệu",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                MilestoneRow(title = "Khởi Đầu Tốt Đẹp", days = 1, currentDays = currentStreak)
                                MilestoneRow(title = "Ngọn Lửa Bùng Cháy", days = 3, currentDays = currentStreak)
                                MilestoneRow(title = "Chuyên Gia Thói Quản", days = 7, currentDays = currentStreak)
                                MilestoneRow(title = "Bậc Thầy Kiên Trì", days = 14, currentDays = currentStreak)
                                MilestoneRow(title = "Huyền Thoại Ghi Chú", days = 30, currentDays = currentStreak)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isInteracted) {
                    Button(
                        onClick = {
                            onDismiss()
                            onCreateNoteClicked()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_streak_create_note_now"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FlameActiveColor)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tạo/Sửa Ghi ChúNgay Để Giữ Chuỗi!", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = if (!isInteracted) ButtonDefaults.outlinedButtonColors() else ButtonDefaults.buttonColors()
                ) {
                    Text("Đóng")
                }
            }
        }
    }
}

@Composable
private fun MilestoneRow(
    title: String,
    days: Int,
    currentDays: Int
) {
    val isReached = currentDays >= days

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isReached) Icons.Default.Stars else Icons.Default.MilitaryTech,
                contentDescription = null,
                tint = if (isReached) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "$title ($days ngày)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isReached) FontWeight.Bold else FontWeight.Normal,
                color = if (isReached) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isReached) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE8F5E9)
            ) {
                Text(
                    text = "Đã đạt 🏆",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        } else {
            Text(
                text = "Còn ${days - currentDays} ngày",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * A prominent visual streak progress bar and 7-day mini calendar view for the app bar header area.
 */
@Composable
fun StreakCalendarProgressBar(
    streakState: StreakState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isInteracted = streakState.hasInteractedToday
    val currentStreak = streakState.currentStreak
    val milestone = streakState.nextMilestone
    val progress = if (milestone > 0) (currentStreak.toFloat() / milestone.toFloat()).coerceIn(0f, 1f) else 1f
    val dayLabels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("bar_streak_calendar_progress"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInteracted)
                Color(0xFFFFF3E0).copy(alpha = 0.95f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isInteracted) FlameActiveColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Top Header Line: Flame Icon, Current Streak, Level Name & Target Milestone
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = "Chuỗi ngày",
                        tint = if (isInteracted) FlameActiveColor else FlameInactiveColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Chuỗi $currentStreak Ngày",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isInteracted) Color(0xFFD84315) else MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isInteracted) Color(0xFFFFE0B2) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = streakState.streakLevelName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isInteracted) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = if (currentStreak >= milestone) "Mục tiêu đạt! 🎉" else "Cột mốc: $milestone ngày",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Visual Progress Bar Towards Milestone Target
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = FlameActiveColor,
                trackColor = if (isInteracted) Color(0xFFFFE0B2) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mini 7-Day Calendar Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                streakState.activeDaysPastWeek.forEachIndexed { index, isActive ->
                    val dayName = dayLabels.getOrElse(index) { "" }
                    val isTodayIndex = (index == 6) // Index 6 is today

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = when {
                                isActive -> FlameActiveColor
                                isTodayIndex && !isInteracted -> Color(0xFFFFB300)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier.size(16.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isActive) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                } else if (isTodayIndex) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White,
                                        modifier = Modifier.size(5.dp)
                                    ) {}
                                }
                            }
                        }

                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = if (isTodayIndex) FontWeight.ExtraBold else FontWeight.Normal,
                            color = when {
                                isActive -> FlameActiveColor
                                isTodayIndex -> Color(0xFFE65100)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            }
                        )
                    }
                }
            }
        }
    }
}

