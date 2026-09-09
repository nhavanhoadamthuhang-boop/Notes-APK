package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.NoteEntity
import com.example.data.repository.ThemeMode
import com.example.ui.NoteSortOrder
import com.example.ui.NotesViewModel
import com.example.ui.components.ActivityTrendCard
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.DailyCheckInCard
import com.example.ui.components.DiamondGoalDialog
import com.example.ui.components.DiamondProgressCard
import com.example.ui.components.DiamondStoreDialog
import com.example.ui.components.DiamondTopBarBadge
import com.example.ui.components.JsonImportExportDialog
import com.example.ui.components.NoteCard
import com.example.ui.components.NoteEditorDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.StreakBadge
import com.example.ui.components.StreakCalendarProgressBar
import com.example.ui.components.StreakDialog
import com.example.ui.components.TrashManagerDialog
import com.example.ui.theme.PinGold

private val DEFAULT_SUGGESTED_CATEGORIES = listOf(
    "Công việc",
    "Cá nhân",
    "Học tập",
    "Ý tưởng",
    "Tài chính"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NotesViewModel,
    modifier: Modifier = Modifier,
    isEmbeddedInSplitPane: Boolean = false
) {
    val (pinnedNotes, unpinnedNotes) = viewModel.filteredNotes.collectAsStateWithLifecycle().value
    val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()
    val activeCategories by viewModel.activeCategories.collectAsStateWithLifecycle()
    val activeTags by viewModel.activeTags.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val isCreatingNewNote by viewModel.isCreatingNewNote.collectAsStateWithLifecycle()
    val editingNote by viewModel.editingNote.collectAsStateWithLifecycle()
    val rewardState by viewModel.rewardState.collectAsStateWithLifecycle()
    val rateLimitWarning by viewModel.rateLimitWarning.collectAsStateWithLifecycle()
    val autoBackupState by viewModel.autoBackupState.collectAsStateWithLifecycle()
    val streakState by viewModel.streakState.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val activityStats by viewModel.activityStats.collectAsStateWithLifecycle()
    val trashNotes by viewModel.trashNotes.collectAsStateWithLifecycle()
    val trashComments by viewModel.trashComments.collectAsStateWithLifecycle()
    val totalTrashCount by viewModel.totalTrashCount.collectAsStateWithLifecycle()

    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }
    var showTrashDialog by remember { mutableStateOf(false) }
    var showImportExportDialog by remember { mutableStateOf(false) }
    var showDiamondGoalDialog by remember { mutableStateOf(false) }
    var showDiamondStoreDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showStreakDialog by remember { mutableStateOf(false) }
    var showActivityTrendsCard by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    // Merge active categories and default categories for discoverability
    val displayCategories = remember(activeCategories) {
        val merged = (activeCategories + DEFAULT_SUGGESTED_CATEGORIES).distinct()
        merged
    }

    val isAnyFilterActive = searchQuery.isNotBlank() || selectedCategory != null || selectedTag != null
    val totalNotesCount = pinnedNotes.size + unpinnedNotes.size

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    DiamondTopBarBadge(
                        rewardState = rewardState,
                        onClick = { showDiamondStoreDialog = true },
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Ghi Chú",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // Sorting dropdown menu
                    Box {
                        IconButton(
                            onClick = { sortMenuExpanded = true },
                            modifier = Modifier.testTag("btn_sort_notes")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Sắp xếp ghi chú theo ngày tạo",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Mới nhất trước (Newest first)") },
                                onClick = {
                                    viewModel.setSortOrder(NoteSortOrder.NEWEST_FIRST)
                                    sortMenuExpanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (sortOrder == NoteSortOrder.NEWEST_FIRST) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Đang chọn",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("menu_sort_newest_first")
                            )
                            DropdownMenuItem(
                                text = { Text("Cũ nhất trước (Oldest first)") },
                                onClick = {
                                    viewModel.setSortOrder(NoteSortOrder.OLDEST_FIRST)
                                    sortMenuExpanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (sortOrder == NoteSortOrder.OLDEST_FIRST) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Đang chọn",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("menu_sort_oldest_first")
                            )
                        }
                    }

                    StreakBadge(
                        streakState = streakState,
                        onClick = { showStreakDialog = true },
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    // Trash & Recent Deleted Button with Badge
                    Box {
                        IconButton(
                            onClick = { showTrashDialog = true },
                            modifier = Modifier.testTag("btn_open_trash")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Thùng rác & Đã xoá gần đây",
                                tint = if (totalTrashCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                        if (totalTrashCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 4.dp)
                            ) {
                                Text(
                                    text = if (totalTrashCount > 99) "99+" else "$totalTrashCount",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { showActivityTrendsCard = !showActivityTrendsCard },
                        modifier = Modifier.testTag("btn_toggle_activity_trends")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Xu hướng hoạt động",
                            tint = if (showActivityTrendsCard) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("btn_toggle_theme")
                    ) {
                        Icon(
                            imageVector = if (themeMode == ThemeMode.DARK) Icons.Default.WbSunny else Icons.Default.DarkMode,
                            contentDescription = if (themeMode == ThemeMode.DARK) "Chuyển sang Chế độ sáng" else "Chuyển sang Chế độ tối",
                            tint = if (themeMode == ThemeMode.DARK) Color(0xFFFFB300) else MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { showImportExportDialog = true },
                        modifier = Modifier.testTag("btn_open_json_backup")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DataObject,
                            contentDescription = "Nhập / Xuất JSON",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("btn_open_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cài đặt & Tự động sao lưu",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.startCreateNote() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_note_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tạo ghi chú mới")
                    Text("Tạo ghi chú", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isMultiColumn = maxWidth >= 520.dp && !isEmbeddedInSplitPane
            val gridColumns = if (isMultiColumn) GridCells.Adaptive(minSize = 280.dp) else GridCells.Fixed(1)

            LazyVerticalGrid(
                columns = gridColumns,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("notes_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Search Box Header Item
                item(key = "hdr_search", span = { GridItemSpan(maxLineSpan) }) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = {
                                Text(
                                    "Tìm theo tiêu đề, mô tả, thẻ #nhãn...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Tìm kiếm ghi chú",
                                    tint = if (searchQuery.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = { viewModel.clearSearchQuery() },
                                        modifier = Modifier.testTag("clear_search_button")
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Xoá nội dung tìm kiếm",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_note_input"),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                // 2. Daily Streak Banner
                item(key = "hdr_streak", span = { GridItemSpan(maxLineSpan) }) {
                    StreakCalendarProgressBar(
                        streakState = streakState,
                        onClick = { showStreakDialog = true }
                    )
                }

                // 3. Diamond Reward Progress Card
                item(key = "hdr_diamond", span = { GridItemSpan(maxLineSpan) }) {
                    DiamondProgressCard(
                        rewardState = rewardState,
                        onClick = { showDiamondGoalDialog = true },
                        onOpenStore = { showDiamondStoreDialog = true },
                        onClaimCheckIn = { viewModel.claimDailyCheckIn() }
                    )
                }

                // 3.5 Daily Check-In Card
                item(key = "hdr_checkin", span = { GridItemSpan(maxLineSpan) }) {
                    DailyCheckInCard(
                        rewardState = rewardState,
                        onClaimCheckIn = { viewModel.claimDailyCheckIn() }
                    )
                }

                // 4. Activity Trends Line Chart Banner
                item(key = "hdr_trends", span = { GridItemSpan(maxLineSpan) }) {
                    AnimatedVisibility(
                        visible = showActivityTrendsCard,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        ActivityTrendCard(
                            stats = activityStats
                        )
                    }
                }

                // 5. Category Filter Chips Row
                item(key = "hdr_categories", span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .testTag("category_filter_row"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Quick Sort Order Toggle Chip
                        item(key = "sort_order_chip") {
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.toggleSortOrder() },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (sortOrder == NoteSortOrder.NEWEST_FIRST) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (sortOrder == NoteSortOrder.NEWEST_FIRST) "Mới nhất" else "Cũ nhất",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("filter_chip_sort_toggle"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        // "Tất cả" Chip
                        item(key = "category_all") {
                            val isAllSelected = selectedCategory == null
                            FilterChip(
                                selected = isAllSelected,
                                onClick = { viewModel.clearCategoryFilter() },
                                label = { Text("Tất cả (${allNotes.size})") },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("filter_chip_all_categories"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }

                        // Category Chips
                        items(displayCategories, key = { "cat_$it" }) { cat ->
                            val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                            val count = allNotes.count { it.category.equals(cat, ignoreCase = true) }
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectCategory(cat) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Folder,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(if (count > 0) "$cat ($count)" else cat)
                                },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("filter_chip_category_$cat"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                // 6. Tag Filter Chips Row
                if (activeTags.isNotEmpty()) {
                    item(key = "hdr_tags", span = { GridItemSpan(maxLineSpan) }) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tag_filter_row"),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(activeTags, key = { "tag_$it" }) { tag ->
                                val isSelected = selectedTag.equals(tag, ignoreCase = true)
                                val count = allNotes.count { note -> note.tagList.any { it.equals(tag, ignoreCase = true) } }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectTag(tag) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Label,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    label = {
                                        Text("#$tag ($count)", fontSize = 12.sp)
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.testTag("filter_chip_tag_$tag"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                )
                            }
                        }
                    }
                }

                // 7. Active Filter Status Banner
                if (isAnyFilterActive) {
                    item(key = "hdr_filter_banner", span = { GridItemSpan(maxLineSpan) }) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    val filterSummary = buildString {
                                        append("Đang lọc: $totalNotesCount kết quả")
                                        if (selectedCategory != null) append(" • Thư mục: '$selectedCategory'")
                                        if (selectedTag != null) append(" • Thẻ: '#$selectedTag'")
                                        if (searchQuery.isNotBlank()) append(" • Từ khóa: '$searchQuery'")
                                    }
                                    Text(
                                        text = filterSummary,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = if (totalNotesCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        maxLines = 1
                                    )
                                }

                                TextButton(
                                    onClick = { viewModel.clearAllFilters() },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    modifier = Modifier.testTag("clear_all_filters_button")
                                ) {
                                    Text("Xoá bộ lọc", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                // 8. Empty State OR Notes List
                if (totalNotesCount == 0) {
                    item(key = "hdr_empty_state", span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isAnyFilterActive) Icons.Default.Search else Icons.Default.Description,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = if (isAnyFilterActive) "Không tìm thấy ghi chú phù hợp" else "Chưa có ghi chú nào",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                val emptyMessage = when {
                                    selectedCategory != null && selectedTag != null ->
                                        "Không có ghi chú nào trong thư mục \"$selectedCategory\" có gắn thẻ \"#$selectedTag\"."
                                    selectedCategory != null ->
                                        "Chưa có ghi chú nào trong thư mục \"$selectedCategory\"."
                                    selectedTag != null ->
                                        "Chưa có ghi chú nào được gắn thẻ \"#$selectedTag\"."
                                    searchQuery.isNotBlank() ->
                                        "Không tìm thấy ghi chú nào khớp với từ khóa \"$searchQuery\"."
                                    else ->
                                        "Hãy bấm nút 'Tạo ghi chú' để thêm tiêu đề ghi chú, mô tả, thư mục, thẻ nhãn và bắt đầu thảo luận."
                                }

                                Text(
                                    text = emptyMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (!isAnyFilterActive) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.startCreateNote() },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("btn_empty_create_note")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Tạo ghi chú mới")
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { viewModel.clearAllFilters() },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("btn_reset_all_filters")
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Xoá tất cả bộ lọc")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Pinned Section Header & Notes
                    if (pinnedNotes.isNotEmpty()) {
                        item(key = "pinned_section_header", span = { GridItemSpan(maxLineSpan) }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PushPin,
                                    contentDescription = null,
                                    tint = PinGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "ĐÃ GHIM (${pinnedNotes.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PinGold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        items(pinnedNotes, key = { "pinned_${it.id}" }) { note ->
                            NoteCard(
                                note = note,
                                onClick = { viewModel.selectNote(note.id) },
                                onTogglePin = { viewModel.toggleNotePinned(note) },
                                onEdit = { viewModel.startEditNote(note) },
                                onDelete = { noteToDelete = note },
                                onCategoryClick = { viewModel.selectCategory(it) },
                                onTagClick = { viewModel.selectTag(it) }
                            )
                        }
                    }

                    // Unpinned Section Header & Notes
                    if (unpinnedNotes.isNotEmpty()) {
                        item(key = "unpinned_section_header", span = { GridItemSpan(maxLineSpan) }) {
                            val headerTitle = if (pinnedNotes.isNotEmpty()) "GHI CHÚ KHÁC" else "TẤT CẢ GHI CHÚ"
                            Text(
                                text = "$headerTitle (${unpinnedNotes.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }

                        items(unpinnedNotes, key = { "unpinned_${it.id}" }) { note ->
                            NoteCard(
                                note = note,
                                onClick = { viewModel.selectNote(note.id) },
                                onTogglePin = { viewModel.toggleNotePinned(note) },
                                onEdit = { viewModel.startEditNote(note) },
                                onDelete = { noteToDelete = note },
                                onCategoryClick = { viewModel.selectCategory(it) },
                                onTagClick = { viewModel.selectTag(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirm Delete Note Dialog
    val currentNoteToDelete = noteToDelete
    if (currentNoteToDelete != null) {
        ConfirmDeleteDialog(
            title = "Chuyển ghi chú vào Thùng rác?",
            message = "Ghi chú \"${currentNoteToDelete.title}\" sẽ được chuyển vào Thùng rác và lưu trữ trong ${rewardState.trashRetentionDays} ngày trước khi bị xoá vĩnh viễn. Bạn có thể khôi phục lại bất kỳ lúc nào từ Thùng rác.",
            confirmButtonText = "Chuyển vào Thùng rác",
            onConfirm = {
                viewModel.deleteNote(currentNoteToDelete.id)
                noteToDelete = null
            },
            onDismiss = { noteToDelete = null }
        )
    }

    // Trash & Recent Deleted Dialog
    if (showTrashDialog) {
        TrashManagerDialog(
            trashNotes = trashNotes,
            trashComments = trashComments,
            allNotes = allNotes,
            rewardState = rewardState,
            onRestoreNote = { noteId -> viewModel.restoreNote(noteId) },
            onPermanentlyDeleteNote = { noteId -> viewModel.permanentlyDeleteNote(noteId) },
            onRestoreAllNotes = { viewModel.restoreAllNotes() },
            onEmptyTrashNotes = { viewModel.emptyTrashNotes() },
            onRestoreComment = { commentId -> viewModel.restoreComment(commentId) },
            onPermanentlyDeleteComment = { commentId -> viewModel.permanentlyDeleteComment(commentId) },
            onRestoreAllComments = { viewModel.restoreAllComments() },
            onEmptyTrashComments = { viewModel.emptyTrashComments() },
            onEmptyAllTrash = { viewModel.emptyAllTrash() },
            onOpenStore = { showDiamondStoreDialog = true },
            onDismiss = { showTrashDialog = false }
        )
    }

    // Dialog for creating or editing notes
    if (isCreatingNewNote || editingNote != null) {
        NoteEditorDialog(
            initialNote = editingNote,
            onDismiss = { viewModel.dismissNoteDialog() },
            onSave = { title, description, isPinned, category, tags ->
                viewModel.saveNote(title, description, isPinned, category, tags)
            }
        )
    }

    // JSON Import/Export Backup Dialog
    if (showImportExportDialog) {
        JsonImportExportDialog(
            viewModel = viewModel,
            onDismiss = { showImportExportDialog = false }
        )
    }

    // Diamond Rewards and Limits Summary Dialog
    if (showDiamondGoalDialog) {
        DiamondGoalDialog(
            rewardState = rewardState,
            onOpenStore = { showDiamondStoreDialog = true },
            onClaimCheckIn = { viewModel.claimDailyCheckIn() },
            onDismiss = { showDiamondGoalDialog = false }
        )
    }

    // Diamond Store (Gói Nạp & Nâng Cấp) Dialog
    if (showDiamondStoreDialog) {
        DiamondStoreDialog(
            rewardState = rewardState,
            onTopUp = { amount -> viewModel.topUpDiamonds(amount) },
            onActivatePackage = { tier -> viewModel.activateStorePackage(tier) },
            onTopUpAndActivate = { tier -> viewModel.topUpAndActivatePackage(tier) },
            onClaimCheckIn = { viewModel.claimDailyCheckIn() },
            onDismiss = { showDiamondStoreDialog = false }
        )
    }

    // Settings & Auto Daily Backup Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            themeMode = themeMode,
            onSelectThemeMode = { mode -> viewModel.setThemeMode(mode) },
            activityStats = activityStats,
            autoBackupState = autoBackupState,
            onToggleAutoBackup = { enabled -> viewModel.setAutoBackupEnabled(enabled) },
            onManualBackup = { callback -> viewModel.triggerManualBackup(callback) },
            getBackupFiles = { viewModel.getBackupFiles() },
            onDeleteBackupFile = { file -> viewModel.deleteBackupFile(file) },
            onRestoreBackupFile = { file, replaceExisting -> viewModel.restoreFromLocalBackup(file, replaceExisting) },
            onOpenJsonImportExport = { showImportExportDialog = true },
            onDismiss = { showSettingsDialog = false }
        )
    }

    // Daily Streak Dialog
    if (showStreakDialog) {
        StreakDialog(
            streakState = streakState,
            onCreateNoteClicked = { viewModel.startCreateNote() },
            onClaimDailyCheckIn = { viewModel.claimDailyCheckIn() },
            hasCheckedInToday = rewardState.hasCheckedInToday,
            onDismiss = { showStreakDialog = false }
        )
    }

    // Rate Limit / Reward Warning Alert Dialog
    val warningMsg = rateLimitWarning
    if (warningMsg != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.clearRateLimitWarning() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Thông báo giới hạn",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = warningMsg,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearRateLimitWarning() },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Đã hiểu")
                }
            }
        )
    }
}
