package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.DiamondRewardState
import com.example.data.repository.StorePackageTier
import java.text.NumberFormat
import java.util.Locale

private val DiamondCyan = Color(0xFF00B4D8)
private val DiamondBlue = Color(0xFF0077B6)
private val DiamondGold = Color(0xFFFFB703)
private val DiamondPurple = Color(0xFF9D4EDD)

@Composable
fun DiamondTopBarBadge(
    rewardState: DiamondRewardState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormatter = NumberFormat.getNumberInstance(Locale.GERMANY)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = when (rewardState.currentTier) {
            StorePackageTier.PACKAGE_2 -> DiamondPurple.copy(alpha = 0.2f)
            StorePackageTier.PACKAGE_1 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        },
        border = BorderStroke(
            1.dp,
            when (rewardState.currentTier) {
                StorePackageTier.PACKAGE_2 -> DiamondPurple.copy(alpha = 0.7f)
                StorePackageTier.PACKAGE_1 -> DiamondGold.copy(alpha = 0.7f)
                else -> if (rewardState.isGoalCompleted) DiamondGold else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            }
        ),
        modifier = modifier.testTag("badge_diamond_topbar")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = when {
                    rewardState.currentTier == StorePackageTier.PACKAGE_2 -> Icons.Default.Stars
                    rewardState.currentTier == StorePackageTier.PACKAGE_1 -> Icons.Default.EmojiEvents
                    rewardState.isGoalCompleted -> Icons.Default.EmojiEvents
                    else -> Icons.Default.Diamond
                },
                contentDescription = "Kim cương",
                tint = when {
                    rewardState.currentTier == StorePackageTier.PACKAGE_2 -> DiamondPurple
                    rewardState.currentTier == StorePackageTier.PACKAGE_1 -> DiamondGold
                    rewardState.isGoalCompleted -> DiamondGold
                    else -> DiamondCyan
                },
                modifier = Modifier.size(16.dp)
            )
            AnimatedContent(
                targetState = rewardState.totalDiamonds,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInVertically { height -> height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut())
                    } else {
                        (slideInVertically { height -> -height } + fadeIn()).togetherWith(slideOutVertically { height -> height } + fadeOut())
                    }
                },
                label = "diamond_counter_anim"
            ) { count ->
                Text(
                    text = "${numberFormatter.format(count)} 💎",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (rewardState.currentTier != StorePackageTier.DEFAULT) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (rewardState.currentTier) {
                        StorePackageTier.PACKAGE_2 -> DiamondPurple
                        else -> DiamondGold
                    }
                ) {
                    Text(
                        text = if (rewardState.currentTier == StorePackageTier.PACKAGE_2) "VIP 2" else "VIP 1",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DiamondProgressCard(
    rewardState: DiamondRewardState,
    onClick: () -> Unit,
    onOpenStore: () -> Unit,
    onClaimCheckIn: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val numberFormatter = NumberFormat.getNumberInstance(Locale.GERMANY)
    val progress = (rewardState.totalDiamonds.toFloat() / rewardState.targetDiamonds.toFloat()).coerceIn(0f, 1f)
    val progressPercent = (progress * 100).toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("card_diamond_progress"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (rewardState.currentTier) {
                StorePackageTier.PACKAGE_2 -> DiamondPurple.copy(alpha = 0.08f)
                StorePackageTier.PACKAGE_1 -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = when (rewardState.currentTier) {
                StorePackageTier.PACKAGE_2 -> DiamondPurple.copy(alpha = 0.5f)
                StorePackageTier.PACKAGE_1 -> DiamondGold.copy(alpha = 0.6f)
                else -> if (rewardState.isGoalCompleted) DiamondGold.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header row with Diamond Icon, Goal & Store button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = when (rewardState.currentTier) {
                                    StorePackageTier.PACKAGE_2 -> DiamondPurple.copy(alpha = 0.2f)
                                    StorePackageTier.PACKAGE_1 -> DiamondGold.copy(alpha = 0.2f)
                                    else -> DiamondCyan.copy(alpha = 0.2f)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (rewardState.currentTier) {
                                StorePackageTier.PACKAGE_2 -> Icons.Default.Stars
                                StorePackageTier.PACKAGE_1 -> Icons.Default.EmojiEvents
                                else -> Icons.Default.Diamond
                            },
                            contentDescription = "Mục tiêu kim cương",
                            tint = when (rewardState.currentTier) {
                                StorePackageTier.PACKAGE_2 -> DiamondPurple
                                StorePackageTier.PACKAGE_1 -> DiamondGold
                                else -> DiamondCyan
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${numberFormatter.format(rewardState.totalDiamonds)} 💎",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (rewardState.currentTier != StorePackageTier.DEFAULT) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (rewardState.currentTier == StorePackageTier.PACKAGE_2) DiamondPurple else DiamondGold
                                ) {
                                    Text(
                                        text = rewardState.currentTier.title,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "+1 💎/ghi chú & cmt • Điểm danh: +500 💎",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Cửa Hàng Button
                FilledTonalButton(
                    onClick = onOpenStore,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("button_open_store")
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cửa hàng",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Daily Check-In Banner inside card if not checked in
            if (!rewardState.hasCheckedInToday && onClaimCheckIn != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    onClick = onClaimCheckIn,
                    shape = RoundedCornerShape(12.dp),
                    color = DiamondGold.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, DiamondGold.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("banner_claim_checkin_inside_card")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = DiamondGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Điểm danh hôm nay để nhận +500 💎!",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DiamondGold
                        ) {
                            Text(
                                text = "Điểm danh ngay",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .testTag("progress_diamonds_goal"),
                color = when (rewardState.currentTier) {
                    StorePackageTier.PACKAGE_2 -> DiamondPurple
                    StorePackageTier.PACKAGE_1 -> DiamondGold
                    else -> DiamondCyan
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Limits Quick Status Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Today's Notes Limit Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NoteAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Hôm nay: ${numberFormatter.format(rewardState.notesCreatedToday)}/${numberFormatter.format(rewardState.maxDailyNotes)} ghi chú",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Comment rate limit Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = if (rewardState.commentsInCurrentMinute >= (rewardState.maxCommentsPerMinute - 4)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${rewardState.commentsInCurrentMinute}/${rewardState.maxCommentsPerMinute} cmt/phút",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = if (rewardState.commentsInCurrentMinute >= (rewardState.maxCommentsPerMinute - 4)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DailyCheckInCard(
    rewardState: DiamondRewardState,
    onClaimCheckIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasCheckedIn = rewardState.hasCheckedInToday

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_daily_checkin"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasCheckedIn)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (hasCheckedIn)
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            else
                DiamondGold
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = if (hasCheckedIn) Color(0xFFE8F5E9) else DiamondGold.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (hasCheckedIn) Icons.Default.CheckCircle else Icons.Default.Stars,
                        contentDescription = "Điểm danh mỗi ngày",
                        tint = if (hasCheckedIn) Color(0xFF2E7D32) else DiamondGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Điểm Danh Mỗi Ngày",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (hasCheckedIn) Color(0xFFC8E6C9) else DiamondGold
                        ) {
                            Text(
                                text = "+500 💎",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (hasCheckedIn) Color(0xFF1B5E20) else Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (hasCheckedIn)
                            "Bạn đã điểm danh hôm nay! Quay lại vào ngày mai để nhận thêm 500 💎."
                        else
                            "Bấm điểm danh để nhận ngay 500 viên kim cương miễn phí!",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = if (hasCheckedIn) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (!hasCheckedIn) {
                Button(
                    onClick = onClaimCheckIn,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DiamondGold),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("btn_claim_daily_checkin")
                ) {
                    Icon(
                        imageVector = Icons.Default.Diamond,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Điểm danh",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Đã nhận",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiamondGoalDialog(
    rewardState: DiamondRewardState,
    onOpenStore: () -> Unit,
    onClaimCheckIn: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val numberFormatter = NumberFormat.getNumberInstance(Locale.GERMANY)
    val progress = (rewardState.totalDiamonds.toFloat() / rewardState.targetDiamonds.toFloat()).coerceIn(0f, 1f)
    val progressPercent = (progress * 100).toInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = null,
                    tint = DiamondCyan,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = "Tích Lũy & Hạn Mức",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .testTag("dialog_diamond_summary"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Daily Check-In Card
                if (onClaimCheckIn != null) {
                    DailyCheckInCard(
                        rewardState = rewardState,
                        onClaimCheckIn = onClaimCheckIn
                    )
                }

                // Goal Card Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TỔNG KIM CƯƠNG HIỆN CÓ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${numberFormatter.format(rewardState.totalDiamonds)} 💎",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "Tiến độ: $progressPercent% mục tiêu 8.000 💎",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = if (rewardState.isGoalCompleted) DiamondGold else DiamondCyan,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                    }
                }

                // Current Tier Info Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Gói hiện tại: ${rewardState.currentTier.title}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = rewardState.currentTier.badgeLabel,
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "• Giới hạn bình luận: ${rewardState.maxCommentsPerMinute} bình luận & phản hồi/phút (Hiện tại: ${rewardState.commentsInCurrentMinute})",
                            style = MaterialTheme.typography.bodySmall
                         )
                        Text(
                            text = "• Giới hạn tạo ghi chú: ${numberFormatter.format(rewardState.maxDailyNotes)} ghi chú/ngày (Hôm nay: ${numberFormatter.format(rewardState.notesCreatedToday)})",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Thời gian lưu thùng rác: ${rewardState.trashRetentionDays} ngày trước khi xoá vĩnh viễn",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Quick store banner button
                Button(
                    onClick = {
                        onDismiss()
                        onOpenStore()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mở Cửa Hàng Gói Nạp & Nâng Cấp")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun DiamondStoreDialog(
    rewardState: DiamondRewardState,
    onTopUp: (Int) -> Unit,
    onActivatePackage: (StorePackageTier) -> Unit,
    onTopUpAndActivate: (StorePackageTier) -> Unit,
    onClaimCheckIn: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val numberFormatter = NumberFormat.getNumberInstance(Locale.GERMANY)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = "Cửa Hàng Gói Nạp",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .testTag("dialog_diamond_store"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Daily Check-In Card
                if (onClaimCheckIn != null) {
                    DailyCheckInCard(
                        rewardState = rewardState,
                        onClaimCheckIn = onClaimCheckIn
                    )
                }

                // Balance Header Banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Số dư hiện tại",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${numberFormatter.format(rewardState.totalDiamonds)} 💎",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = rewardState.currentTier.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "CÁC GÓI NÂNG CẤP HẠN MỨC",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )

                // Package 1 Card (8.000 Diamonds)
                StorePackageCard(
                    tier = StorePackageTier.PACKAGE_1,
                    isActive = rewardState.currentTier == StorePackageTier.PACKAGE_1,
                    userDiamonds = rewardState.totalDiamonds,
                    accentColor = DiamondGold,
                    icon = Icons.Default.EmojiEvents,
                    onActivate = { onActivatePackage(StorePackageTier.PACKAGE_1) },
                    onTopUpAndActivate = { onTopUpAndActivate(StorePackageTier.PACKAGE_1) }
                )

                // Package 2 Card (12.000 Diamonds)
                StorePackageCard(
                    tier = StorePackageTier.PACKAGE_2,
                    isActive = rewardState.currentTier == StorePackageTier.PACKAGE_2,
                    userDiamonds = rewardState.totalDiamonds,
                    accentColor = DiamondPurple,
                    icon = Icons.Default.Stars,
                    onActivate = { onActivatePackage(StorePackageTier.PACKAGE_2) },
                    onTopUpAndActivate = { onTopUpAndActivate(StorePackageTier.PACKAGE_2) }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "NẠP KIM CƯƠNG NHANH",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )

                // Quick Top Up Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onTopUp(1000) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+1.000 💎", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { onTopUp(8000) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+8.000 💎", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { onTopUp(12000) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+12.000 💎", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Đóng")
            }
        }
    )
}

@Composable
private fun StorePackageCard(
    tier: StorePackageTier,
    isActive: Boolean,
    userDiamonds: Int,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onActivate: () -> Unit,
    onTopUpAndActivate: () -> Unit
) {
    val numberFormatter = NumberFormat.getNumberInstance(Locale.GERMANY)
    val canAfford = userDiamonds >= tier.diamondPrice

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) accentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(accentColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = tier.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Chi phí: ${numberFormatter.format(tier.diamondPrice)} 💎",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }

                if (isActive) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Đang kích hoạt",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Benefits breakdown
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Diamond, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Chi phí gói: ${numberFormatter.format(tier.diamondPrice)} Kim Cương (💎)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Giới hạn: ${tier.maxCommentsPerMinute} bình luận và phản hồi/phút",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.NoteAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Giới hạn: ${numberFormatter.format(tier.maxDailyNotes)} ghi chú/ngày",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.AutoDelete, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                    Text(
                        text = "Theo dõi thùng rác đã xóa gần đây trong vòng ${tier.trashRetentionDays} ngày trước khi bị xóa vĩnh viễn",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Action Buttons
            if (!isActive) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (canAfford) {
                        Button(
                            onClick = onActivate,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text("Dùng ${numberFormatter.format(tier.diamondPrice)} 💎 kích hoạt", fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = onTopUpAndActivate,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nạp & Kích hoạt (+${numberFormatter.format(tier.diamondPrice)} 💎)", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
