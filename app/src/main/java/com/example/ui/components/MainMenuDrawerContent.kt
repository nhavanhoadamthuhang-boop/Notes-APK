package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.DiamondRewardState
import com.example.data.repository.StreakState
import com.example.data.repository.ThemeMode
import java.text.NumberFormat
import java.util.Locale

private val DiamondCyan = Color(0xFF00BCD4)
private val DiamondGold = Color(0xFFFFB300)
private val StreakFlameOrange = Color(0xFFFF5722)

@Composable
fun MainMenuDrawerContent(
    totalNotesCount: Int,
    pinnedNotesCount: Int,
    totalTrashCount: Int,
    categories: List<String>,
    selectedCategory: String?,
    onSelectCategory: (String?) -> Unit,
    rewardState: DiamondRewardState,
    streakState: StreakState,
    themeMode: ThemeMode,
    isTrendsVisible: Boolean,
    onToggleTrends: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenCreateNote: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenStreak: () -> Unit,
    onClaimDailyReward: () -> Unit,
    onOpenJsonBackup: () -> Unit,
    onOpenSettings: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    var showCategoriesSection by remember { mutableStateOf(true) }

    ModalDrawerSheet(
        modifier = modifier
            .width(320.dp)
            .fillMaxHeight()
            .testTag("main_menu_drawer_sheet"),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // Header: App branding & User Wallet Overview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Title and Icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Ghi Chú Thông Minh",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Trình đơn chính (Menu)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Diamond & Streak status cards
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Row 1: Diamond and Streak badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Diamond,
                                        contentDescription = null,
                                        tint = DiamondCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "${numberFormatter.format(rewardState.totalDiamonds)} kim cương",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = StreakFlameOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "${streakState.currentStreak} ngày",
                                        fontWeight = FontWeight.Bold,
                                        color = StreakFlameOrange,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            // Row 2: Package Tier & Trash Retention info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = rewardState.currentTier.badgeLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "Thùng rác: ${rewardState.trashRetentionDays} ngày",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Row 3: Daily check-in claim button
                            if (!rewardState.hasCheckedInToday) {
                                Button(
                                    onClick = {
                                        onClaimDailyReward()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2E7D32)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("menu_btn_claim_daily")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CardGiftcard,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Điểm danh hôm nay (+500 kim cương)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Create Note Button
            Button(
                onClick = {
                    onCloseDrawer()
                    onOpenCreateNote()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("menu_btn_create_note"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tạo ghi chú mới", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SECTION: QUẢN LÝ GHI CHÚ
            Text(
                text = "GHI CHÚ & THƯ MỤC",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )

            // All Notes
            NavigationDrawerItem(
                label = { Text("Tất cả ghi chú") },
                selected = selectedCategory == null,
                onClick = {
                    onSelectCategory(null)
                    onCloseDrawer()
                },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Notes,
                        contentDescription = null
                    )
                },
                badge = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "$totalNotesCount",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .testTag("menu_nav_all_notes")
            )

            // Trash & Deleted Items
            NavigationDrawerItem(
                label = { Text("Thùng rác (${rewardState.trashRetentionDays} ngày)") },
                selected = false,
                onClick = {
                    onCloseDrawer()
                    onOpenTrash()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = if (totalTrashCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                badge = {
                    if (totalTrashCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                text = if (totalTrashCount > 99) "99+" else "$totalTrashCount",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .testTag("menu_nav_trash")
            )

            // Categories Section toggle
            if (categories.isNotEmpty()) {
                NavigationDrawerItem(
                    label = { Text("Danh mục (${categories.size})") },
                    selected = false,
                    onClick = { showCategoriesSection = !showCategoriesSection },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("menu_nav_categories_toggle")
                )

                AnimatedVisibility(visible = showCategoriesSection) {
                    Column(
                        modifier = Modifier.padding(start = 24.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = selectedCategory == category
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = category,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                selected = isSelected,
                                onClick = {
                                    onSelectCategory(if (isSelected) null else category)
                                    onCloseDrawer()
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Folder,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier
                                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                                    .testTag("menu_nav_cat_$category")
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // SECTION: TÍNH NĂNG & TIỆN ÍCH
            Text(
                text = "TÍNH NĂNG & TIỆN ÍCH",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )

            // Diamond Store & Packages
            NavigationDrawerItem(
                label = { Text("Cửa hàng Kim Cương & Gói") },
                selected = false,
                onClick = {
                    onCloseDrawer()
                    onOpenStore()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = DiamondGold
                    )
                },
                badge = {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DiamondGold.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${numberFormatter.format(rewardState.totalDiamonds)} kim cương",
                            color = Color(0xFFD87D00),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .testTag("menu_nav_store")
            )

            // Streak & Rewards
            NavigationDrawerItem(
                label = { Text("Chuỗi ngày & Điểm danh") },
                selected = false,
                onClick = {
                    onCloseDrawer()
                    onOpenStreak()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = StreakFlameOrange
                    )
                },
                badge = {
                    Text(
                        text = "${streakState.currentStreak} ngày",
                        color = StreakFlameOrange,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .testTag("menu_nav_streak")
            )

            // Activity Trends toggle
            NavigationDrawerItem(
                label = { Text("Biểu đồ xu hướng") },
                selected = isTrendsVisible,
                onClick = {
                    onToggleTrends()
                },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                },
                badge = {
                    Switch(
                        checked = isTrendsVisible,
                        onCheckedChange = { onToggleTrends() },
                        modifier = Modifier.size(width = 38.dp, height = 24.dp)
                    )
                },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .testTag("menu_nav_trends")
            )

            // JSON Backup / Restore
            NavigationDrawerItem(
                label = { Text("Sao lưu / Phục hồi JSON") },
                selected = false,
                onClick = {
                    onCloseDrawer()
                    onOpenJsonBackup()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.DataObject,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .testTag("menu_nav_backup")
            )

            // Settings & Auto Backup
            NavigationDrawerItem(
                label = { Text("Cài đặt & Tự động sao lưu") },
                selected = false,
                onClick = {
                    onCloseDrawer()
                    onOpenSettings()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .testTag("menu_nav_settings")
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // SECTION: GIAO DIỆN & THIẾT LẬP
            Text(
                text = "GIAO DIỆN & HỆ THỐNG",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )

            // Dark / Light Theme toggle
            NavigationDrawerItem(
                label = {
                    Text(
                        if (themeMode == ThemeMode.DARK) "Chế độ tối (Bật)" else "Chế độ sáng"
                    )
                },
                selected = false,
                onClick = {
                    onToggleTheme()
                },
                icon = {
                    Icon(
                        imageVector = if (themeMode == ThemeMode.DARK) Icons.Default.DarkMode else Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = if (themeMode == ThemeMode.DARK) Color(0xFFFFB300) else Color(0xFFF57C00)
                    )
                },
                badge = {
                    Switch(
                        checked = themeMode == ThemeMode.DARK,
                        onCheckedChange = { onToggleTheme() },
                        modifier = Modifier.size(width = 38.dp, height = 24.dp)
                    )
                },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .testTag("menu_nav_theme")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Footer version
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ghi Chú • Phiên bản 1.2 (Room Database)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
